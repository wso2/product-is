/*
* Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.wso2.carbon.is.migration.service.v550.migrator;

import org.apache.commons.io.Charsets;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.core.util.CryptoException;
import org.wso2.carbon.core.util.CryptoUtil;
import org.wso2.carbon.identity.core.migrate.MigrationClientException;
import org.wso2.carbon.identity.oauth.tokenprocessor.HashingPersistenceProcessor;
import org.wso2.carbon.identity.oauth.tokenprocessor.TokenPersistenceProcessor;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;
import org.wso2.carbon.is.migration.service.Migrator;
import org.wso2.carbon.is.migration.service.v550.bean.AuthzCodeInfo;
import org.wso2.carbon.is.migration.service.v550.bean.ClientSecretInfo;
import org.wso2.carbon.is.migration.service.v550.bean.OauthTokenInfo;
import org.wso2.carbon.is.migration.service.v550.dao.AuthzCodeDAO;
import org.wso2.carbon.is.migration.service.v550.dao.OAuthDAO;
import org.wso2.carbon.is.migration.service.v550.dao.TokenDAO;
import org.wso2.carbon.is.migration.service.v550.util.OAuth2Util;
import org.wso2.carbon.is.migration.util.Constant;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class OAuthDataMigrator extends Migrator {

    private static final Log log = LogFactory
            .getLog(org.wso2.carbon.is.migration.service.v550.migrator.OAuthDataMigrator.class);
    boolean isTokenHashColumnsAvailable = false;
    boolean isAuthzCodeHashColumnAvailable = false;
    boolean isClientSecretHashColumnsAvailable = false;

    @Override
    public void migrate() throws MigrationClientException {
        try {
            addHashColumns();
            deleteClientSecretHashColumn();
            migrateOldEncryptedTokens();
            migrateAuthorizationCodes();
            migrateClientSecrets();
        } catch (SQLException e) {
            throw new MigrationClientException("Error while adding hash columns", e);
        }
    }

    public void addHashColumns() throws MigrationClientException, SQLException {

        try (Connection connection = getDataSource().getConnection()) {
            isTokenHashColumnsAvailable = TokenDAO.getInstance().isTokenHashColumnsAvailable(connection);
            isAuthzCodeHashColumnAvailable = AuthzCodeDAO.getInstance().isAuthzCodeHashColumnAvailable(connection);
        }
        if (!isTokenHashColumnsAvailable) {
            try (Connection connection = getDataSource().getConnection()) {
                TokenDAO.getInstance().addAccessTokenHashColumn(connection);
                TokenDAO.getInstance().addRefreshTokenHashColumn(connection);
                connection.commit();
            }
        }
        if (!isAuthzCodeHashColumnAvailable) {
            try (Connection connection = getDataSource().getConnection()) {
                AuthzCodeDAO.getInstance().addAuthzCodeHashColumns(connection);
                connection.commit();
            }
        }
    }

    public void deleteClientSecretHashColumn() throws MigrationClientException, SQLException {

        try (Connection connection = getDataSource().getConnection()) {
            isClientSecretHashColumnsAvailable = OAuthDAO.getInstance().isConsumerSecretHashColumnAvailable(connection);

        }
        if(isClientSecretHashColumnsAvailable){
            try (Connection connection = getDataSource().getConnection()) {
                OAuthDAO.getInstance().deleteConsumerSecretHashColumn(connection);
                connection.commit();
            }
        }
    }

    public void migrateOldEncryptedTokens() throws MigrationClientException, SQLException {

        log.info(Constant.MIGRATION_LOG + "Migration starting on OAuth2 access token table.");
        try {
            if(!isTokenHashColumnsAvailable && OAuth2Util.isEncryptionWithTransformationEnabled()){
                List<OauthTokenInfo> oauthTokenList;
                try (Connection connection = getDataSource().getConnection()) {
                    oauthTokenList = TokenDAO.getInstance().getAllAccessTokens(connection);
                }
                List<OauthTokenInfo> updatedOauthTokenList = transformFromOldToNewEncryption(oauthTokenList);
                try (Connection connection = getDataSource().getConnection()) {
                    TokenDAO.getInstance().updateNewTokens(updatedOauthTokenList, connection);
                }

            }
        } catch (IdentityOAuth2Exception e) {
            e.printStackTrace();
        } catch (CryptoException e) {
            e.printStackTrace();
        }
    }

    private List<OauthTokenInfo> transformFromOldToNewEncryption(List<OauthTokenInfo> oauthTokenList) throws CryptoException {
        List<OauthTokenInfo> updatedOauthTokenList = new ArrayList<>();

        for(OauthTokenInfo oauthTokenInfo : oauthTokenList){
            if(!CryptoUtil.getDefaultCryptoUtil().base64DecodeAndIsSelfContainedCipherText(oauthTokenInfo
                    .getAccessToken())){
                byte[] decryptedAccessToken = CryptoUtil.getDefaultCryptoUtil()
                        .base64DecodeAndDecrypt(oauthTokenInfo.getAccessToken(), "RSA");
                String newEncryptedAccesTOken = CryptoUtil.getDefaultCryptoUtil().encryptAndBase64Encode
                        (decryptedAccessToken);
                byte[] decryptedRefreshToken = CryptoUtil.getDefaultCryptoUtil()
                        .base64DecodeAndDecrypt(oauthTokenInfo.getRefreshToken(), "RSA");
                String newEncryptedRefreshToken = CryptoUtil.getDefaultCryptoUtil().encryptAndBase64Encode
                        (decryptedRefreshToken);
                TokenPersistenceProcessor tokenPersistenceProcessor = new HashingPersistenceProcessor();
                String accessTokenHash = null;
                String refreshTokenHash = null;
                try {
                    accessTokenHash = tokenPersistenceProcessor.getProcessedAccessTokenIdentifier
                            (new String(decryptedAccessToken,Charsets.UTF_8));
                    refreshTokenHash = tokenPersistenceProcessor.getProcessedRefreshToken(new String
                            (decryptedRefreshToken,Charsets.UTF_8));
                } catch (IdentityOAuth2Exception e) {
                    e.printStackTrace();
                }
                OauthTokenInfo updatedOauthTokenInfo = (new OauthTokenInfo(newEncryptedAccesTOken,newEncryptedRefreshToken,
                        oauthTokenInfo.getTokenId()));
                updatedOauthTokenInfo.setAccessTokenHash(accessTokenHash);
                updatedOauthTokenInfo.setRefreshTokenhash(refreshTokenHash);
                updatedOauthTokenList.add(updatedOauthTokenInfo);
            }
        }

        return updatedOauthTokenList;
    }

    /**
     * Method to migrate old encrypted authorization codes to new encrypted authorization codes
     *
     * @throws MigrationClientException
     * @throws SQLException
     */
    public void migrateAuthorizationCodes() throws MigrationClientException {

        log.info(Constant.MIGRATION_LOG + "Migration starting on OAuth2 authorization code table.");
        try {
            if (!isAuthzCodeHashColumnAvailable && OAuth2Util.isEncryptionWithTransformationEnabled()) {
                List<AuthzCodeInfo> authzCodeInfoList;
                try (Connection connection = getDataSource().getConnection()) {
                    authzCodeInfoList = AuthzCodeDAO.getInstance().getAllAuthzCodes(connection);
                }
                List<AuthzCodeInfo> updatedAuthzCodeInfoList = transformAuthzCodeFromOldToNewEncryption(
                        authzCodeInfoList);
                try (Connection connection = getDataSource().getConnection()) {
                    AuthzCodeDAO.getInstance().updateNewAuthzCodes(updatedAuthzCodeInfoList, connection);
                }
            }
        } catch (IdentityOAuth2Exception e) {
            throw new MigrationClientException(
                    "Error while checking configurations for encryption with " + "transformation is enabled. ", e);
        } catch (CryptoException e) {
            throw new MigrationClientException(
                    "Error while transforming authorization codes from old to new " + "encryption algorithm. ", e);
        } catch (SQLException e) {
            throw new MigrationClientException("Error while getting datasource connection. ", e);
        }
    }

    private List<AuthzCodeInfo> transformAuthzCodeFromOldToNewEncryption(List<AuthzCodeInfo> authzCodeInfoList)
            throws CryptoException, IdentityOAuth2Exception {

        List<AuthzCodeInfo> updatedAuthzCodeInfoList = new ArrayList<>();
        for (AuthzCodeInfo authzCodeInfo : authzCodeInfoList) {
            if (!CryptoUtil.getDefaultCryptoUtil()
                    .base64DecodeAndIsSelfContainedCipherText(authzCodeInfo.getAuthorizationCode())) {
                byte[] decryptedAuthzCode = CryptoUtil.getDefaultCryptoUtil()
                        .base64DecodeAndDecrypt(authzCodeInfo.getAuthorizationCode(), "RSA");
                String newEncryptedAuthzCode = CryptoUtil.getDefaultCryptoUtil()
                        .encryptAndBase64Encode(decryptedAuthzCode);
                TokenPersistenceProcessor tokenPersistenceProcessor = new HashingPersistenceProcessor();
                String authzCodeHash = null;
                authzCodeHash = tokenPersistenceProcessor
                        .getProcessedAuthzCode(new String(decryptedAuthzCode, Charsets.UTF_8));

                AuthzCodeInfo updatedAuthzCodeInfo = (new AuthzCodeInfo(newEncryptedAuthzCode,
                        authzCodeInfo.getCodeId()));
                updatedAuthzCodeInfo.setAuthorizationCodeHash(authzCodeHash);
                updatedAuthzCodeInfoList.add(updatedAuthzCodeInfo);
            }
        }
        return updatedAuthzCodeInfoList;
    }

    /**
     * Method to migrate old encrypted client secrets to new encrypted client secrets
     *
     * @throws MigrationClientException
     */
    public void migrateClientSecrets() throws MigrationClientException {

        log.info(Constant.MIGRATION_LOG + "Migration starting on OAuth2 consumer apps table.");
        try {
            try {
                if (!isClientSecretHashColumnsAvailable && OAuth2Util.isEncryptionWithTransformationEnabled()) {
                    List<ClientSecretInfo> clientSecretInfoList;
                    try (Connection connection = getDataSource().getConnection()) {
                        clientSecretInfoList = OAuthDAO.getInstance().getAllClientSecrets(connection);
                    }
                    List<ClientSecretInfo> updatedClientSecretInfoList = null;
                    try {
                        updatedClientSecretInfoList = transformClientSecretFromOldToNewEncryption(clientSecretInfoList);
                    } catch (IdentityOAuth2Exception e) {
                        throw new MigrationClientException(
                                "Error while transforming client secret from old to new " + "encryption. ", e);
                    }
                    try (Connection connection = getDataSource().getConnection()) {
                        OAuthDAO.getInstance().updateNewClientSecrets(updatedClientSecretInfoList, connection);
                    }
                }
            } catch (IdentityOAuth2Exception e) {
                throw new MigrationClientException("Error while checking encryption with transformation is enabled. ",
                        e);
            }
        } catch (SQLException e) {
            throw new MigrationClientException("Error while retrieving and updating client secrets. ", e);
        } catch (CryptoException e) {
            throw new MigrationClientException(
                    "Error while transforming client secret from old to new " + "encryption. ", e);
        }
    }

    private List<ClientSecretInfo> transformClientSecretFromOldToNewEncryption(
            List<ClientSecretInfo> clientSecretInfoList) throws CryptoException, IdentityOAuth2Exception {

        List<ClientSecretInfo> updatedClientSecretList = new ArrayList<>();
        for (ClientSecretInfo clientSecretInfo : clientSecretInfoList) {
            if (!CryptoUtil.getDefaultCryptoUtil()
                    .base64DecodeAndIsSelfContainedCipherText(clientSecretInfo.getClientSecret())) {
                byte[] decryptedClientSecret = CryptoUtil.getDefaultCryptoUtil()
                        .base64DecodeAndDecrypt(clientSecretInfo.getClientSecret(), "RSA");
                String newEncryptedClientSecret = CryptoUtil.getDefaultCryptoUtil()
                        .encryptAndBase64Encode(decryptedClientSecret);
                ClientSecretInfo updatedClientSecretInfo = (new ClientSecretInfo(newEncryptedClientSecret,
                        clientSecretInfo.getId()));
                updatedClientSecretList.add(updatedClientSecretInfo);
            }
        }
        return updatedClientSecretList;
    }

}

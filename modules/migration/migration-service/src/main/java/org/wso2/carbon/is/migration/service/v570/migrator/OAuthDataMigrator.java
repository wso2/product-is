package org.wso2.carbon.is.migration.service.v570.migrator;

import org.apache.commons.io.Charsets;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.wso2.carbon.core.util.CryptoException;
import org.wso2.carbon.core.util.CryptoUtil;
import org.wso2.carbon.identity.core.migrate.MigrationClientException;
import org.wso2.carbon.identity.oauth.config.OAuthServerConfiguration;
import org.wso2.carbon.identity.oauth.tokenprocessor.HashingPersistenceProcessor;
import org.wso2.carbon.identity.oauth.tokenprocessor.TokenPersistenceProcessor;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;
import org.wso2.carbon.is.migration.service.Migrator;
import org.wso2.carbon.is.migration.service.v550.bean.AuthzCodeInfo;
import org.wso2.carbon.is.migration.service.v550.bean.OauthTokenInfo;
import org.wso2.carbon.is.migration.service.v550.util.OAuth2Util;
import org.wso2.carbon.is.migration.service.v570.dao.OAuthDAO;
import org.wso2.carbon.is.migration.util.Constant;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class OAuthDataMigrator extends Migrator {

    private static final Log log = LogFactory.getLog(OAuthDataMigrator.class);
    private static String hashingAlgo = OAuthServerConfiguration.getInstance().getHashAlgorithm();
    private static final String ALGORITHM = "algorithm";
    private static final String HASH = "hash";

    @Override
    public void migrate() throws MigrationClientException {

        migrateTokenHash();
        migrateAuthzCodeHash();
    }

    public void migrateTokenHash() throws MigrationClientException {

        log.info(Constant.MIGRATION_LOG + "Migration starting on OAuth2 access token table.");

        List<OauthTokenInfo> tokenInfoList = getTokenList();
        try {
            List<OauthTokenInfo> updateTokenInfoList = updateHashColumnValues(tokenInfoList, hashingAlgo);
            try (Connection connection = getDataSource().getConnection()) {
                connection.setAutoCommit(false);
                //persists modified hash values
                OAuthDAO.getInstance().updateNewTokenHash(updateTokenInfoList, connection);
                connection.commit();
            } catch (SQLException e) {
                String error = "SQL error while updating token hash";
                throw new MigrationClientException(error, e);
            }
        } catch (CryptoException e) {
            throw new MigrationClientException("Error while encrypting tokens.", e);
        } catch (IdentityOAuth2Exception e) {
            throw new MigrationClientException("Error while migrating tokens.", e);
        }

    }

    public void migrateAuthzCodeHash() throws MigrationClientException {

        log.info(Constant.MIGRATION_LOG + "Migration starting on Authorization code table");

        List<AuthzCodeInfo> authzCodeInfos = getAuthzCoedList();
        try {
            List<AuthzCodeInfo> updatedAuthzCodeInfoList = updateAuthzCodeHashColumnValues(authzCodeInfos, hashingAlgo);
            try (Connection connection = getDataSource().getConnection()) {
                connection.setAutoCommit(false);
                // persists modified hash values
                OAuthDAO.getInstance().updateNewAuthzCodeHash(updatedAuthzCodeInfoList, connection);
                connection.commit();
            } catch (SQLException e) {
                String error = "SQL error while updating authorization code hash";
                throw new MigrationClientException(error, e);
            }
        } catch (CryptoException e) {
            throw new MigrationClientException("Error while encrypting authorization codes.", e);
        } catch (IdentityOAuth2Exception e) {
            throw new MigrationClientException("Error while migrating authorization codes.", e);
        }
    }

    private List<OauthTokenInfo> getTokenList() throws MigrationClientException {

        List<OauthTokenInfo> oauthTokenList;
        try (Connection connection = getDataSource().getConnection()) {
            oauthTokenList = OAuthDAO.getInstance().getAllAccessTokens(connection);
            connection.commit();
        } catch (SQLException e) {
            String error = "SQL error while retrieving token hash";
            throw new MigrationClientException(error, e);
        }

        return oauthTokenList;
    }

    private List<AuthzCodeInfo> getAuthzCoedList() throws MigrationClientException {

        List<AuthzCodeInfo> authzCodeInfoList;
        try (Connection connection = getDataSource().getConnection()) {
            authzCodeInfoList = OAuthDAO.getInstance().getAllAuthzCodes(connection);
            connection.commit();
        } catch (SQLException e) {
            String error = "SQL error while retrieving authorization code hash";
            throw new MigrationClientException(error, e);
        }

        return authzCodeInfoList;
    }

    private boolean isBase64DecodeAndIsSelfContainedCipherText(String text) throws CryptoException {

        return CryptoUtil.getDefaultCryptoUtil().base64DecodeAndIsSelfContainedCipherText(text);
    }

    private List<OauthTokenInfo> updateHashColumnValues(List<OauthTokenInfo> oauthTokenList, String hashAlgorithm)
            throws CryptoException, IdentityOAuth2Exception {

        List<OauthTokenInfo> updatedOauthTokenList = new ArrayList<>();
        if (oauthTokenList != null) {
            boolean encryptionWithTransformationEnabled = OAuth2Util.isEncryptionWithTransformationEnabled();
            JSONObject accessTokenHashObject;
            JSONObject refreshTokenHashObject;

            for (OauthTokenInfo tokenInfo : oauthTokenList) {

                String accessToken = tokenInfo.getAccessToken();
                String refreshToken = tokenInfo.getRefreshToken();

                if (encryptionWithTransformationEnabled) {
                    // Token encryption is enabled.
                    if (!isBase64DecodeAndIsSelfContainedCipherText(accessToken)) {
                        // Existing access tokens are not encrypted with OAEP.
                        byte[] decryptedAccessToken = CryptoUtil.getDefaultCryptoUtil()
                                .base64DecodeAndDecrypt(accessToken, "RSA");
                        String newEncryptedAccessToken = CryptoUtil.getDefaultCryptoUtil().encryptAndBase64Encode
                                (decryptedAccessToken);
                        byte[] decryptedRefreshToken = null;
                        String newEncryptedRefreshToken = null;
                        if (refreshToken != null) {
                            decryptedRefreshToken = CryptoUtil.getDefaultCryptoUtil()
                                    .base64DecodeAndDecrypt(refreshToken, "RSA");
                            newEncryptedRefreshToken = CryptoUtil.getDefaultCryptoUtil()
                                    .encryptAndBase64Encode(decryptedRefreshToken);
                        }
                        TokenPersistenceProcessor tokenPersistenceProcessor = new HashingPersistenceProcessor();
                        String accessTokenHash;
                        String refreshTokenHash = null;

                        accessTokenHash = tokenPersistenceProcessor
                                .getProcessedAccessTokenIdentifier(new String(decryptedAccessToken, Charsets.UTF_8));
                        if (refreshToken != null) {
                            refreshTokenHash = tokenPersistenceProcessor
                                    .getProcessedRefreshToken(new String(decryptedRefreshToken, Charsets.UTF_8));
                        }

                        OauthTokenInfo updatedOauthTokenInfo = (new OauthTokenInfo(newEncryptedAccessToken,
                                newEncryptedRefreshToken,
                                tokenInfo.getTokenId()));
                        updatedOauthTokenInfo.setAccessTokenHash(accessTokenHash);
                        if (refreshToken != null) {
                            updatedOauthTokenInfo.setRefreshTokenhash(refreshTokenHash);
                        }
                        updatedOauthTokenList.add(updatedOauthTokenInfo);
                    } else {
                        if (StringUtils.isBlank(tokenInfo.getAccessTokenHash())) {

                            byte[] decryptedAccessToken = CryptoUtil.getDefaultCryptoUtil()
                                    .base64DecodeAndDecrypt(accessToken);
                            byte[] decryptedRefreshToken = null;
                            if (refreshToken != null) {
                                decryptedRefreshToken = CryptoUtil.getDefaultCryptoUtil()
                                        .base64DecodeAndDecrypt(refreshToken);
                            }
                            TokenPersistenceProcessor tokenPersistenceProcessor = new HashingPersistenceProcessor();
                            String accessTokenHash;
                            String refreshTokenHash = null;

                            accessTokenHash = tokenPersistenceProcessor
                                    .getProcessedAccessTokenIdentifier(new String(decryptedAccessToken, Charsets.UTF_8));
                            if (refreshToken != null) {
                                refreshTokenHash = tokenPersistenceProcessor
                                        .getProcessedRefreshToken(new String(decryptedRefreshToken, Charsets.UTF_8));
                            }

                            OauthTokenInfo updatedOauthTokenInfo = (new OauthTokenInfo(accessToken,
                                    refreshToken,
                                    tokenInfo.getTokenId()));
                            updatedOauthTokenInfo.setAccessTokenHash(accessTokenHash);
                            if (refreshToken != null) {
                                updatedOauthTokenInfo.setRefreshTokenhash(refreshTokenHash);
                            }
                            updatedOauthTokenList.add(updatedOauthTokenInfo);
                        }
                    }
                } else {
                    // Token encryption is not enabled.
                    if (StringUtils.isBlank(tokenInfo.getAccessTokenHash())) {

                        OauthTokenInfo updatedOauthTokenInfo = getOauthTokenInfo(tokenInfo, accessToken, refreshToken);
                        updatedOauthTokenList.add(updatedOauthTokenInfo);
                    } else {
                        String oldAccessTokenHash = tokenInfo.getAccessTokenHash();
                        try {
                            //If hash column already is a JSON value, no need to update the record
                            new JSONObject(oldAccessTokenHash);
                        } catch (JSONException e) {
                            //Exception is thrown because the hash value is not a json
                            accessTokenHashObject = new JSONObject();
                            accessTokenHashObject.put(ALGORITHM, hashAlgorithm);
                            accessTokenHashObject.put(HASH, oldAccessTokenHash);
                            tokenInfo.setAccessTokenHash(accessTokenHashObject.toString());

                            refreshTokenHashObject = new JSONObject();
                            String oldRefreshTokenHash = tokenInfo.getRefreshTokenhash();
                            refreshTokenHashObject.put(ALGORITHM, hashAlgorithm);
                            refreshTokenHashObject.put(HASH, oldRefreshTokenHash);
                            tokenInfo.setRefreshTokenhash(refreshTokenHashObject.toString());
                            updatedOauthTokenList.add(tokenInfo);
                        }
                    }
                }
            }
        }
        return updatedOauthTokenList;
    }

    private OauthTokenInfo getOauthTokenInfo(OauthTokenInfo tokenInfo, String accessToken, String refreshToken)
            throws IdentityOAuth2Exception {

        TokenPersistenceProcessor tokenPersistenceProcessor = new HashingPersistenceProcessor();
        String accessTokenHash;
        String refreshTokenHash = null;

        accessTokenHash = tokenPersistenceProcessor.getProcessedAccessTokenIdentifier(accessToken);
        if (refreshToken != null) {
            refreshTokenHash = tokenPersistenceProcessor.getProcessedRefreshToken(refreshToken);
        }

        OauthTokenInfo updatedOauthTokenInfo = (new OauthTokenInfo(accessToken,
                refreshToken,
                tokenInfo.getTokenId()));
        updatedOauthTokenInfo.setAccessTokenHash(accessTokenHash);
        if (refreshToken != null) {
            updatedOauthTokenInfo.setRefreshTokenhash(refreshTokenHash);
        }
        return updatedOauthTokenInfo;
    }

    private AuthzCodeInfo getAuthzCodeInfo(AuthzCodeInfo authzCodeInfo, String authzCode)
            throws IdentityOAuth2Exception {

        TokenPersistenceProcessor tokenPersistenceProcessor = new HashingPersistenceProcessor();
        String authzCodeHash = tokenPersistenceProcessor.getProcessedAuthzCode(authzCode);

        AuthzCodeInfo updatedAuthzCodeInfo = new AuthzCodeInfo(authzCode, authzCodeInfo.getCodeId());
        updatedAuthzCodeInfo.setAuthorizationCodeHash(authzCodeHash);

        return updatedAuthzCodeInfo;
    }

    private List<AuthzCodeInfo> updateAuthzCodeHashColumnValues(List<AuthzCodeInfo> authzCodeInfos, String hashAlgorithm)
            throws IdentityOAuth2Exception, CryptoException {

        List<AuthzCodeInfo> updatedAuthzCodeList = new ArrayList<>();
        if (authzCodeInfos != null) {
            boolean encryptionWithTransformationEnabled = OAuth2Util.isEncryptionWithTransformationEnabled();

            for (AuthzCodeInfo authzCodeInfo : authzCodeInfos) {
                String authzCode = authzCodeInfo.getAuthorizationCode();

                if (encryptionWithTransformationEnabled) {
                    // Code encryption is enabled.
                    if (!isBase64DecodeAndIsSelfContainedCipherText(authzCode)) {
                        // Existing codes are not encrypted with OAEP.
                        byte[] decryptedAuthzCode = CryptoUtil.getDefaultCryptoUtil()
                                                                .base64DecodeAndDecrypt(authzCode, "RSA");
                        String newEncryptedAuthzCode = CryptoUtil.getDefaultCryptoUtil().encryptAndBase64Encode
                                (decryptedAuthzCode);
                        TokenPersistenceProcessor tokenPersistenceProcessor = new HashingPersistenceProcessor();
                        String authzCodeHash = tokenPersistenceProcessor
                                .getProcessedAuthzCode(new String(decryptedAuthzCode, Charsets.UTF_8));
                        AuthzCodeInfo updatedAuthzCodeInfo = (new AuthzCodeInfo(newEncryptedAuthzCode,
                                                                                  authzCodeInfo.getCodeId()));
                        updatedAuthzCodeInfo.setAuthorizationCodeHash(authzCodeHash);
                        updatedAuthzCodeList.add(updatedAuthzCodeInfo);
                    } else {
                        if (StringUtils.isBlank(authzCodeInfo.getAuthorizationCodeHash())) {

                            byte[] decryptedAuthzCode = CryptoUtil.getDefaultCryptoUtil()
                                                                    .base64DecodeAndDecrypt(authzCode);

                            TokenPersistenceProcessor tokenPersistenceProcessor = new HashingPersistenceProcessor();
                            String authzCodeHash = tokenPersistenceProcessor
                                    .getProcessedAuthzCode(new String(decryptedAuthzCode, Charsets.UTF_8));

                            AuthzCodeInfo updatedAuthzCodeInfo = (new AuthzCodeInfo(authzCode, authzCodeInfo
                                    .getCodeId()));
                            updatedAuthzCodeInfo.setAuthorizationCodeHash(authzCodeHash);
                            updatedAuthzCodeList.add(updatedAuthzCodeInfo);
                        }
                    }
                } else {
                    // Code encryption is not enabled.
                    if (StringUtils.isBlank(authzCodeInfo.getAuthorizationCodeHash())) {

                        AuthzCodeInfo updatedAuthzCodeInfo = getAuthzCodeInfo(authzCodeInfo, authzCode);
                        updatedAuthzCodeList.add(updatedAuthzCodeInfo);
                    } else {
                        String oldAuthzCodeHash = authzCodeInfo.getAuthorizationCodeHash();
                        try {
                            // If hash column already is a JSON value, no need to update the record
                            new JSONObject(oldAuthzCodeHash);
                        } catch (JSONException e) {
                            // Exception is thrown because the hash value is not a json
                            JSONObject authzCodeHashObject = new JSONObject();
                            authzCodeHashObject.put(ALGORITHM, hashAlgorithm);
                            authzCodeHashObject.put(HASH, oldAuthzCodeHash);
                            AuthzCodeInfo updatedAuthzCodeInfo = (new AuthzCodeInfo(authzCode, authzCodeInfo
                                    .getCodeId()));
                            updatedAuthzCodeInfo.setAuthorizationCodeHash(authzCodeHashObject.toString());
                            updatedAuthzCodeList.add(updatedAuthzCodeInfo);
                        }
                    }
                }
            }
        }
        return updatedAuthzCodeList;
    }
}

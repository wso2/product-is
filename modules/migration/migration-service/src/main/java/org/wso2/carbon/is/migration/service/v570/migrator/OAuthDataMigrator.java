package org.wso2.carbon.is.migration.service.v570.migrator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.wso2.carbon.identity.core.migrate.MigrationClientException;
import org.wso2.carbon.identity.oauth.config.OAuthServerConfiguration;
import org.wso2.carbon.is.migration.service.Migrator;
import org.wso2.carbon.is.migration.service.v550.bean.AuthzCodeInfo;
import org.wso2.carbon.is.migration.service.v550.bean.OauthTokenInfo;
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
        updateHashColumnValues(tokenInfoList, hashingAlgo);

        try (Connection connection = getDataSource().getConnection()) {
            //persists modified hash values
            OAuthDAO.getInstance().updateNewTokenHash(tokenInfoList, connection);
            connection.commit();
        } catch (SQLException e) {
            String error = "SQL error while updating token hash";
            throw new MigrationClientException(error, e);
        }

    }

    public void migrateAuthzCodeHash() throws MigrationClientException {

        log.info(Constant.MIGRATION_LOG + "Migration starting on Authorization code table");

        List<AuthzCodeInfo> authzCodeInfos = getAuthzCoedList();
        updateAuthzCodeHashColumnValues(authzCodeInfos, hashingAlgo);

        try (Connection connection = getDataSource().getConnection()) {
            //persists modified hash values
            OAuthDAO.getInstance().updateNewAuthzCodeHash(authzCodeInfos, connection);
            connection.commit();
        } catch (SQLException e) {
            String error = "SQL error while updating authorization code hash";
            throw new MigrationClientException(error, e);
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

    private void updateHashColumnValues(List<OauthTokenInfo> oauthTokenList, String hashAlgorithm) {

        if (oauthTokenList != null) {
            JSONObject accessTokenHashObject;
            JSONObject refreshTokenHashObject;
            List<OauthTokenInfo> alreadyProcessedRecords = new ArrayList<>();

            for (OauthTokenInfo tokenInfo : oauthTokenList) {

                String oldAccessTokenHash = tokenInfo.getAccessTokenHash();
                try {
                    //If hash column already is a JSON value, no need to update the record
                    new JSONObject(oldAccessTokenHash);
                    alreadyProcessedRecords.add(tokenInfo);
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
                }
            }
            oauthTokenList.removeAll(alreadyProcessedRecords);
        }
    }

    private void updateAuthzCodeHashColumnValues(List<AuthzCodeInfo> authzCodeInfos, String hashAlgorithm) {

        if (authzCodeInfos != null) {
            JSONObject authzCodeHashObject;
            List<AuthzCodeInfo> alreadyProcessedRecords = new ArrayList<>();

            for (AuthzCodeInfo authzCodeInfo : authzCodeInfos) {
                String oldAuthzCodeHash = authzCodeInfo.getAuthorizationCodeHash();
                try {
                    //If hash column already is a JSON value, no need to update the record
                    new JSONObject(oldAuthzCodeHash);
                    alreadyProcessedRecords.add(authzCodeInfo);
                } catch (JSONException e) {
                    //Exception is thrown because the hash value is not a json
                    authzCodeHashObject = new JSONObject();
                    authzCodeHashObject.put(ALGORITHM, hashAlgorithm);
                    authzCodeHashObject.put(HASH, oldAuthzCodeHash);
                    authzCodeInfo.setAuthorizationCodeHash(authzCodeHashObject.toString());
                }
            }
            authzCodeInfos.removeAll(alreadyProcessedRecords);
        }
    }
}

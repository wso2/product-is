package org.wso2.carbon.is.migration.service.v570.migrator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.wso2.carbon.identity.core.migrate.MigrationClientException;
import org.wso2.carbon.identity.oauth.config.OAuthServerConfiguration;
import org.wso2.carbon.is.migration.service.Migrator;
import org.wso2.carbon.is.migration.service.v550.bean.OauthTokenInfo;
import org.wso2.carbon.is.migration.service.v570.dao.TokenDAO;
import org.wso2.carbon.is.migration.util.Constant;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class OAuthDataMigrator extends Migrator {

    private static final Log log = LogFactory.getLog(OAuthDataMigrator.class);
    private static String hashingAlgo = OAuthServerConfiguration.getInstance().getHashAlgorithm();
    private static final String ALGORITHM = "algorithm";
    private static final String HASH = "hash";

    @Override
    public void migrate() throws MigrationClientException {

        migrateTokenHash();
    }

    public void migrateTokenHash() throws MigrationClientException {

        log.info(Constant.MIGRATION_LOG + "Migration starting on OAuth2 access/refresh token hash.");

        List<OauthTokenInfo> tokenInfoList = getTokenList();
        updateHashColumnValues(tokenInfoList, hashingAlgo);

        try (Connection connection = getDataSource().getConnection()) {
            TokenDAO.getInstance().updateNewTokenHash(tokenInfoList, connection);
            connection.commit();
        } catch (SQLException e) {
            String error = "SQL error while updating token hash";
            throw new MigrationClientException(error, e);
        }

    }

    private List<OauthTokenInfo> getTokenList() throws MigrationClientException {

        List<OauthTokenInfo> oauthTokenList;
        try (Connection connection = getDataSource().getConnection()) {
            oauthTokenList = TokenDAO.getInstance().getAllAccessTokens(connection);
            connection.commit();
        } catch (SQLException e) {
            String error = "SQL error while retrieving token hash";
            throw new MigrationClientException(error, e);
        }

        return oauthTokenList;
    }

    private void updateHashColumnValues(List<OauthTokenInfo> oauthTokenList, String hashAlgorithm) {

        if (oauthTokenList != null) {
            JSONObject accessTokenHashObject;
            JSONObject refreshTokenHashObject;

            for (OauthTokenInfo tokenInfo : oauthTokenList) {
                accessTokenHashObject = new JSONObject();
                String oldAccessTokenHash = tokenInfo.getAccessTokenHash();
                accessTokenHashObject.put(ALGORITHM, hashAlgorithm);
                accessTokenHashObject.put(HASH, oldAccessTokenHash);
                tokenInfo.setAccessTokenHash(accessTokenHashObject.toString());

                refreshTokenHashObject = new JSONObject();
                String oldRefreshTokenHash = tokenInfo.getRefreshTokenhash();
                refreshTokenHashObject.put(ALGORITHM, hashAlgorithm);
                refreshTokenHashObject.put(HASH,oldRefreshTokenHash);
                tokenInfo.setRefreshTokenhash(refreshTokenHashObject.toString());
            }
        }
    }
}

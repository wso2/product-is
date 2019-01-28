package org.wso2.carbon.is.migration.service.v570.dao;

import org.wso2.carbon.is.migration.service.v550.bean.AuthzCodeInfo;
import org.wso2.carbon.is.migration.service.v550.bean.OauthTokenInfo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class OAuthDAO {

    private static OAuthDAO instance = new OAuthDAO();

    public static final String UPDATE_ACCESS_TOKEN = "UPDATE IDN_OAUTH2_ACCESS_TOKEN SET ACCESS_TOKEN=?, " +
            "REFRESH_TOKEN=?, ACCESS_TOKEN_HASH=?, REFRESH_TOKEN_HASH=? WHERE TOKEN_ID=?";

    public static final String RETRIEVE_ALL_TOKENS = "SELECT ACCESS_TOKEN, REFRESH_TOKEN, TOKEN_ID, " +
            "ACCESS_TOKEN_HASH, REFRESH_TOKEN_HASH FROM IDN_OAUTH2_ACCESS_TOKEN";

    public static final String RETRIEVE_ALL_AUTHORIZATION_CODES = "SELECT AUTHORIZATION_CODE, CODE_ID, " +
            "AUTHORIZATION_CODE_HASH FROM IDN_OAUTH2_AUTHORIZATION_CODE";

    public static final String UPDATE_AUTHORIZATION_CODE =
            "UPDATE IDN_OAUTH2_AUTHORIZATION_CODE SET AUTHORIZATION_CODE=?, AUTHORIZATION_CODE_HASH=? WHERE CODE_ID=?";

    private OAuthDAO() { }

    public static OAuthDAO getInstance() {

        return instance;
    }

    /**
     * Method to retrieve access token records from database
     *
     * @param connection
     * @return list of token info
     * @throws SQLException
     */
    public List<OauthTokenInfo> getAllAccessTokens(Connection connection) throws SQLException {

        List<OauthTokenInfo> oauthTokenInfoList = new ArrayList<>();
        try (PreparedStatement preparedStatement = connection.prepareStatement(RETRIEVE_ALL_TOKENS);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                OauthTokenInfo tokenInfo = new OauthTokenInfo(resultSet.getString("ACCESS_TOKEN"),
                        resultSet.getString("REFRESH_TOKEN"),
                        resultSet.getString("TOKEN_ID"));
                tokenInfo.setAccessTokenHash(resultSet.getString("ACCESS_TOKEN_HASH"));
                tokenInfo.setRefreshTokenhash(resultSet.getString("REFRESH_TOKEN_HASH"));
                oauthTokenInfoList.add(tokenInfo);
            }
        }
        return oauthTokenInfoList;
    }

    /**
     * Method to persist modified token hash in database
     *
     * @param updatedOauthTokenList
     * @param connection
     * @throws SQLException
     */
    public void updateNewTokenHash(List<OauthTokenInfo> updatedOauthTokenList, Connection connection)
            throws SQLException {

        try (PreparedStatement preparedStatement = connection.prepareStatement(UPDATE_ACCESS_TOKEN)) {
            for (OauthTokenInfo oauthTokenInfo : updatedOauthTokenList) {
                preparedStatement.setString(1, oauthTokenInfo.getAccessToken());
                preparedStatement.setString(2, oauthTokenInfo.getRefreshToken());
                preparedStatement.setString(3, oauthTokenInfo.getAccessTokenHash());
                preparedStatement.setString(4, oauthTokenInfo.getRefreshTokenhash());
                preparedStatement.setString(5, oauthTokenInfo.getTokenId());
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
        }
    }

    /**
     * Method to retrieve all the authorization codes from the database
     *
     * @param connection
     * @return list of authorization codes
     * @throws SQLException
     */
    public List<AuthzCodeInfo> getAllAuthzCodes(Connection connection) throws SQLException {

        List<AuthzCodeInfo> authzCodeInfoList = new ArrayList<>();
        try (PreparedStatement preparedStatement = connection.prepareStatement(RETRIEVE_ALL_AUTHORIZATION_CODES);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            AuthzCodeInfo authzCodeInfo;
            while (resultSet.next()) {
                authzCodeInfo = new AuthzCodeInfo(resultSet.getString("AUTHORIZATION_CODE"),
                        resultSet.getString("CODE_ID"));
                authzCodeInfo.setAuthorizationCodeHash(resultSet.getString("AUTHORIZATION_CODE_HASH"));
                authzCodeInfoList.add(authzCodeInfo);
            }
        }
        return authzCodeInfoList;
    }

    /**
     * Method to update the authorization code table with modified authorization code hashes.
     *
     * @param updatedAuthzCodeList List of updated authorization codes
     * @param connection database connection
     * @throws SQLException
     */
    public void updateNewAuthzCodeHash(List<AuthzCodeInfo> updatedAuthzCodeList, Connection connection)
            throws SQLException {

        try (PreparedStatement preparedStatement = connection.prepareStatement(UPDATE_AUTHORIZATION_CODE)) {
            for (AuthzCodeInfo authzCodeInfo : updatedAuthzCodeList) {
                preparedStatement.setString(1, authzCodeInfo.getAuthorizationCode());
                preparedStatement.setString(2, authzCodeInfo.getAuthorizationCodeHash());
                preparedStatement.setString(3, authzCodeInfo.getCodeId());
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
        }
    }
}

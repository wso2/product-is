package org.wso2.carbon.is.migration.service.v570.dao;

import org.wso2.carbon.is.migration.service.v550.bean.OauthTokenInfo;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TokenDAO {

    private static TokenDAO instance = new TokenDAO();

    public static final String UPDATE_ACCESS_TOKEN = "UPDATE IDN_OAUTH2_ACCESS_TOKEN SET " +
            "ACCESS_TOKEN_HASH=?, REFRESH_TOKEN_HASH=? WHERE TOKEN_ID=?";

    public static final String RETRIEVE_ALL_TOKENS = "SELECT ACCESS_TOKEN_HASH, REFRESH_TOKEN_HASH, TOKEN_ID FROM " +
            "IDN_OAUTH2_ACCESS_TOKEN";

    private TokenDAO() { }

    public static TokenDAO getInstance() {

        return instance;
    }

    public List<OauthTokenInfo> getAllAccessTokens(Connection connection) throws SQLException {
        List<OauthTokenInfo> oauthTokenInfos = new ArrayList<>();
        try (PreparedStatement preparedStatement = connection.prepareStatement(RETRIEVE_ALL_TOKENS);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            OauthTokenInfo oauthTokenInfo;
            while (resultSet.next()) {
                oauthTokenInfo = new OauthTokenInfo(resultSet.getString("TOKEN_ID"));
                oauthTokenInfo.setAccessTokenHash(resultSet.getString("ACCESS_TOKEN_HASH"));
                oauthTokenInfo.setRefreshTokenhash(resultSet.getString("REFRESH_TOKEN_HASH"));
                oauthTokenInfos.add(oauthTokenInfo);
            }
        }
        return oauthTokenInfos;
    }

    public void updateNewTokenHash(List<OauthTokenInfo> updatedOauthTokenList, Connection connection)
            throws SQLException {

        try (PreparedStatement preparedStatement = connection.prepareStatement(UPDATE_ACCESS_TOKEN)) {
            for (OauthTokenInfo oauthTokenInfo : updatedOauthTokenList) {
                preparedStatement.setString(1, oauthTokenInfo.getAccessTokenHash());
                preparedStatement.setString(2, oauthTokenInfo.getRefreshTokenhash());
                preparedStatement.setString(3, oauthTokenInfo.getTokenId());
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
        }
    }

}

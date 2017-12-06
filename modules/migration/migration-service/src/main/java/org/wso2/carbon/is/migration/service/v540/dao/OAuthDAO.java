/*
* Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.is.migration.service.v540.dao;

import org.wso2.carbon.is.migration.service.v540.bean.OAuthConsumerApp;
import org.wso2.carbon.is.migration.service.v540.bean.OAuth2Scope;
import org.wso2.carbon.is.migration.service.v540.bean.OAuth2ScopeBinding;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.wso2.carbon.is.migration.service.v540.SQLConstants.ADD_SCOPE_BINDINGS;
import static org.wso2.carbon.is.migration.service.v540.SQLConstants.GET_ALL_OAUTH2_SCOPES;
import static org.wso2.carbon.is.migration.service.v540.SQLConstants.RETRIEVE_ALL_CONSUMER_APPS;
import static org.wso2.carbon.is.migration.service.v540.SQLConstants.UPDATE_EXPIRY_TIMES_IN_CONSUMER_APPS;
import static org.wso2.carbon.is.migration.service.v540.SQLConstants.UPDATE_OAUTH2_SCOPES;

/**
 * DAO which handles OAuth related operations.
 */
public class OAuthDAO {

    private static OAuthDAO instance = new OAuthDAO();

    private OAuthDAO() {

    }

    public static OAuthDAO getInstance() {

        return instance;
    }

    /**
     * Get all consumer apps.
     *
     * @param connection Database connection
     * @return List of consumer apps
     * @throws SQLException SQLException
     */
    public List<OAuthConsumerApp> getAllOAuthConsumerApps(Connection connection) throws SQLException {

        List<OAuthConsumerApp> consumerApps = new ArrayList<>();
        try (PreparedStatement preparedStatement = connection.prepareStatement(RETRIEVE_ALL_CONSUMER_APPS);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                consumerApps.add(new OAuthConsumerApp(resultSet.getString("CONSUMER_KEY"),
                        resultSet.getInt("TENANT_ID")));
            }
            connection.commit();
        }
        return consumerApps;
    }

    /**
     * Update expiry times defined for OAuth consumer apps.
     *
     * @param connection Database connection
     * @param updatedConsumerApps Updated OAuth consumer apps
     * @throws SQLException SQLException
     */
    public void updateExpiryTimesDefinedForOAuthConsumerApps(Connection connection, List<OAuthConsumerApp>
            updatedConsumerApps) throws SQLException {

        try (PreparedStatement preparedStatement = connection.prepareStatement(UPDATE_EXPIRY_TIMES_IN_CONSUMER_APPS)) {
            for (OAuthConsumerApp consumerApp : updatedConsumerApps) {
                preparedStatement.setLong(1, consumerApp.getUserAccessTokenExpiryTime());
                preparedStatement.setLong(2, consumerApp.getApplicationAccessTokenExpiryTime());
                preparedStatement.setLong(3, consumerApp.getRefreshTokenExpiryTime());
                preparedStatement.setString(4, consumerApp.getConsumerKey());
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
            connection.commit();
        }
    }

    /**
     * Get all OAuth2 scopes.
     *
     * @param connection Database connection
     * @return List of OAuth2 scopes
     * @throws SQLException SQLException
     */
    public List<OAuth2Scope> getAllOAuth2Scopes(Connection connection) throws SQLException {

        List<OAuth2Scope> oAuth2Scopes = new ArrayList<>();
        try (PreparedStatement preparedStatement = connection.prepareStatement(GET_ALL_OAUTH2_SCOPES);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                oAuth2Scopes.add(new OAuth2Scope(resultSet.getInt("SCOPE_ID"), resultSet.getString("SCOPE_KEY"),
                        resultSet.getString("NAME"), resultSet.getString("ROLES")));
            }
            connection.commit();
        }
        return oAuth2Scopes;
    }

    /**
     * Add OAuth2 scope bindings.
     *
     * @param connection Database connection
     * @param oAuth2ScopeBindings List of OAuth2 scope bindings
     * @throws SQLException SQLException
     */
    public void addOAuth2ScopeBindings(Connection connection, List<OAuth2ScopeBinding> oAuth2ScopeBindings)
            throws SQLException {

        try (PreparedStatement preparedStatement = connection.prepareStatement(ADD_SCOPE_BINDINGS)) {
            for (OAuth2ScopeBinding oAuth2ScopeBinding : oAuth2ScopeBindings) {
                preparedStatement.setInt(1, oAuth2ScopeBinding.getScopeId());
                preparedStatement.setString(2, oAuth2ScopeBinding.getScopeBinding());
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
            connection.commit();
        }
    }

    /**
     * Update OAuth2 scopes
     *
     * @param connection Database connection
     * @param updatedOAuth2Scopes Updated OAuth2 scopes
     * @throws SQLException SQLException
     */
    public void updateOAuth2Scopes(Connection connection, List<OAuth2Scope> updatedOAuth2Scopes) throws SQLException {

        try (PreparedStatement preparedStatement = connection.prepareStatement(UPDATE_OAUTH2_SCOPES)) {
            for (OAuth2Scope oAuth2Scope : updatedOAuth2Scopes) {
                preparedStatement.setString(1, oAuth2Scope.getName());
                preparedStatement.setInt(2, oAuth2Scope.getScopeId());
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
            connection.commit();
        }
    }
}

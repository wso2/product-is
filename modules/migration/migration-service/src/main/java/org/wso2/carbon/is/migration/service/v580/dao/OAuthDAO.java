/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.is.migration.service.v580.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class OAuthDAO {

    private static OAuthDAO instance = new OAuthDAO();

    private final String UPDATE_TOKENS_OF_LOCAL_USERS = "UPDATE IDN_OAUTH2_ACCESS_TOKEN " +
            "SET IDN_OAUTH2_ACCESS_TOKEN.IDP_ID = (SELECT ID FROM IDP " +
            "WHERE IDN_OAUTH2_ACCESS_TOKEN.TENANT_ID =  IDP.TENANT_ID AND IDP.NAME = \'LOCAL\') " +
            "WHERE IDN_OAUTH2_ACCESS_TOKEN.USER_DOMAIN != \'FEDERATED\'";

    private final String UPDATE_AUTH_CODES_OF_LOCAL_USERS = "UPDATE IDN_OAUTH2_AUTHORIZATION_CODE " +
            "SET IDN_OAUTH2_AUTHORIZATION_CODE.IDP_ID = (SELECT ID FROM IDP " +
            "WHERE IDN_OAUTH2_AUTHORIZATION_CODE.TENANT_ID =  IDP.TENANT_ID AND IDP.NAME = \'LOCAL\') " +
            "WHERE IDN_OAUTH2_AUTHORIZATION_CODE.USER_DOMAIN != \'FEDERATED\'";

    private OAuthDAO() { }

    public static OAuthDAO getInstance() {

        return instance;
    }

    /**
     * Method to persist IDP ID in database
     *
     * @param connection
     * @throws SQLException
     */
    public void updateTokensOfLocalUsers(Connection connection) throws SQLException {

        try (PreparedStatement preparedStatement = connection.prepareStatement(UPDATE_TOKENS_OF_LOCAL_USERS)) {
            preparedStatement.executeUpdate();
        }
    }

    /**
     * Method to persist IDP ID in database
     *
     * @param connection
     * @throws SQLException
     */
    public void updateAuthCodesOfLocalUsers(Connection connection) throws SQLException {

        try (PreparedStatement preparedStatement = connection.prepareStatement(UPDATE_AUTH_CODES_OF_LOCAL_USERS)) {
            preparedStatement.executeUpdate();
        }
    }
}

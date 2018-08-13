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
package org.wso2.carbon.is.migration.service.v550.dao;

import org.wso2.carbon.is.migration.service.v550.bean.AuthzCodeInfo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.wso2.carbon.is.migration.service.v550.SQLConstants.ADD_AUTHORIZATION_CODE_HASH_COLUMN;
import static org.wso2.carbon.is.migration.service.v550.SQLConstants.RETRIEVE_ALL_AUTHORIZATION_CODES;
import static org.wso2.carbon.is.migration.service.v550.SQLConstants.RETRIEVE_AUTHORIZATION_CODE_TABLE_DB2SQL;
import static org.wso2.carbon.is.migration.service.v550.SQLConstants.RETRIEVE_AUTHORIZATION_CODE_TABLE_INFORMIX;
import static org.wso2.carbon.is.migration.service.v550.SQLConstants.RETRIEVE_AUTHORIZATION_CODE_TABLE_MSSQL;
import static org.wso2.carbon.is.migration.service.v550.SQLConstants.RETRIEVE_AUTHORIZATION_CODE_TABLE_MYSQL;
import static org.wso2.carbon.is.migration.service.v550.SQLConstants.RETRIEVE_AUTHORIZATION_CODE_TABLE_ORACLE;
import static org.wso2.carbon.is.migration.service.v550.SQLConstants.UPDATE_PLAIN_TEXT_AUTHORIZATION_CODE;
import static org.wso2.carbon.is.migration.service.v550.SQLConstants.UPDATE_ENCRYPTED_AUTHORIZATION_CODE;

public class AuthzCodeDAO {

    private static AuthzCodeDAO instance = new AuthzCodeDAO();
    private static final String AUTHORIZATION_CODE_HASH = "AUTHORIZATION_CODE_HASH";

    private AuthzCodeDAO() {

    }

    public static AuthzCodeDAO getInstance() {

        return instance;
    }

    public boolean isAuthzCodeHashColumnAvailable(Connection connection) throws SQLException {

        String sql;
        boolean isAuthzCodeColumnsExist = false;
        if (connection.getMetaData().getDriverName().contains("MySQL") || connection.getMetaData().getDriverName()
                .contains("H2")) {
            sql = RETRIEVE_AUTHORIZATION_CODE_TABLE_MYSQL;
        } else if (connection.getMetaData().getDatabaseProductName().contains("DB2")) {
            sql = RETRIEVE_AUTHORIZATION_CODE_TABLE_DB2SQL;
        } else if (connection.getMetaData().getDriverName().contains("MS SQL") || connection.getMetaData()
                .getDriverName().contains("Microsoft")) {
            sql = RETRIEVE_AUTHORIZATION_CODE_TABLE_MSSQL;
        } else if (connection.getMetaData().getDriverName().contains("PostgreSQL")) {
            sql = RETRIEVE_AUTHORIZATION_CODE_TABLE_MYSQL;
        } else if (connection.getMetaData().getDriverName().contains("Informix")) {
            // Driver name = "IBM Informix JDBC Driver for IBM Informix Dynamic Server"
            sql = RETRIEVE_AUTHORIZATION_CODE_TABLE_INFORMIX;
        } else {
            sql = RETRIEVE_AUTHORIZATION_CODE_TABLE_ORACLE;
        }
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            try {
                ResultSet resultSet = preparedStatement.executeQuery();
                if (resultSet != null) {
                    resultSet.findColumn(AUTHORIZATION_CODE_HASH);
                    isAuthzCodeColumnsExist = true;

                }
            } catch (SQLException e) {
                isAuthzCodeColumnsExist = false;
            }
        } catch (SQLException e) {
            isAuthzCodeColumnsExist = false;
        }
        return isAuthzCodeColumnsExist;
    }

    public void addAuthzCodeHashColumns(Connection connection) throws SQLException {

        try (PreparedStatement preparedStatement = connection.prepareStatement(ADD_AUTHORIZATION_CODE_HASH_COLUMN)) {
            preparedStatement.executeUpdate();
        }
    }

    /**
     * Method to retrieve all the authorization codes from the database
     * @param connection
     * @return list of authorization codes
     * @throws SQLException
     */
    public List<AuthzCodeInfo> getAllAuthzCodes(Connection connection) throws SQLException {

        List<AuthzCodeInfo> authzCodeInfoList = new ArrayList<>();
        try (PreparedStatement preparedStatement = connection.prepareStatement(RETRIEVE_ALL_AUTHORIZATION_CODES);
                ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                authzCodeInfoList.add(new AuthzCodeInfo(resultSet.getString("AUTHORIZATION_CODE"),
                        resultSet.getString("CODE_ID")));
            }
            connection.commit();
        }
        return authzCodeInfoList;
    }

    /**
     * Method to update the authorization code table with updated authorization codes.
     * @param updatedAuthzCodeList List of updated authorization codes
     * @param connection database connection
     * @throws SQLException
     */
    public void updateNewEncryptedAuthzCodes(List<AuthzCodeInfo> updatedAuthzCodeList, Connection connection)
            throws SQLException {

        try (PreparedStatement preparedStatement = connection.prepareStatement(UPDATE_ENCRYPTED_AUTHORIZATION_CODE)) {
            for (AuthzCodeInfo authzCodeInfo : updatedAuthzCodeList) {
                preparedStatement.setString(1, authzCodeInfo.getAuthorizationCode());
                preparedStatement.setString(2, authzCodeInfo.getAuthorizationCodeHash());
                preparedStatement.setString(3, authzCodeInfo.getCodeId());
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
            connection.commit();
        }
    }

    /**
     * This method will update the plain text authorization code table with hashed authorization code value in the
     * hash column.
     *
     * @param updatedAuthzCodeList
     * @param connection
     * @throws SQLException
     */
    public void updatePlainTextAuthzCodes(List<AuthzCodeInfo> updatedAuthzCodeList, Connection connection)
            throws SQLException {

        try (PreparedStatement preparedStatement = connection.prepareStatement(UPDATE_PLAIN_TEXT_AUTHORIZATION_CODE)) {
            for (AuthzCodeInfo authzCodeInfo : updatedAuthzCodeList) {
                preparedStatement.setString(1, authzCodeInfo.getAuthorizationCodeHash());
                preparedStatement.setString(2, authzCodeInfo.getCodeId());
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
            connection.commit();
        }
    }


}

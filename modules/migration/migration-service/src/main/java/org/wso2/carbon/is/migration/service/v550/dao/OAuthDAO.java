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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.core.migrate.MigrationClientException;
import org.wso2.carbon.is.migration.service.v550.bean.ClientSecretInfo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.wso2.carbon.is.migration.service.v550.SQLConstants.DELETE_CONSUMER_SECRET_HASH_COLUMN;
import static org.wso2.carbon.is.migration.service.v550.SQLConstants.RETRIEVE_ALL_CONSUMER_SECRETS;
import static org.wso2.carbon.is.migration.service.v550.SQLConstants.RETRIEVE_CONSUMER_APPS_TABLE_DB2SQL;
import static org.wso2.carbon.is.migration.service.v550.SQLConstants.RETRIEVE_CONSUMER_APPS_TABLE_INFORMIX;
import static org.wso2.carbon.is.migration.service.v550.SQLConstants.RETRIEVE_CONSUMER_APPS_TABLE_MSSQL;
import static org.wso2.carbon.is.migration.service.v550.SQLConstants.RETRIEVE_CONSUMER_APPS_TABLE_MYSQL;
import static org.wso2.carbon.is.migration.service.v550.SQLConstants.RETRIEVE_CONSUMER_APPS_TABLE_ORACLE;
import static org.wso2.carbon.is.migration.service.v550.SQLConstants.UPDATE_CONSUMER_SECRET;

public class OAuthDAO {

    private static final Log log = LogFactory.getLog(OAuthDAO.class);
    private static OAuthDAO instance = new OAuthDAO();
    private static final String CONSUMER_SECRET_HASH = "CONSUMER_SECRET_HASH";

    private OAuthDAO() {

    }

    public static OAuthDAO getInstance() {

        return instance;
    }

    public boolean isConsumerSecretHashColumnAvailable(Connection connection) throws MigrationClientException {

        String sql;
        boolean isConsumerSecretHashColumnsExist = false;
        try {
            if (connection.getMetaData().getDriverName().contains("MySQL") || connection.getMetaData().getDriverName()
                    .contains("H2")) {
                sql = RETRIEVE_CONSUMER_APPS_TABLE_MYSQL;
            } else if (connection.getMetaData().getDatabaseProductName().contains("DB2")) {
                sql = RETRIEVE_CONSUMER_APPS_TABLE_DB2SQL;
            } else if (connection.getMetaData().getDriverName().contains("MS SQL") || connection.getMetaData()
                    .getDriverName().contains("Microsoft")) {
                sql = RETRIEVE_CONSUMER_APPS_TABLE_MSSQL;
            } else if (connection.getMetaData().getDriverName().contains("PostgreSQL")) {
                sql = RETRIEVE_CONSUMER_APPS_TABLE_MYSQL;
            } else if (connection.getMetaData().getDriverName().contains("Informix")) {
                // Driver name = "IBM Informix JDBC Driver for IBM Informix Dynamic Server"
                sql = RETRIEVE_CONSUMER_APPS_TABLE_INFORMIX;
            } else {
                sql = RETRIEVE_CONSUMER_APPS_TABLE_ORACLE;
            }
        } catch (Exception e) {
            throw new MigrationClientException("Error while retrieving metadata from connection.", e);
        }
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet != null) {
                    resultSet.findColumn(CONSUMER_SECRET_HASH);
                    isConsumerSecretHashColumnsExist = true;
                }
            }
        } catch (SQLException e) {
            isConsumerSecretHashColumnsExist = false;
            if (log.isDebugEnabled()) {
                log.debug("Error occurred while executing the sql query: " + sql, e);
            }
            log.info("CONSUMER_SECRET_HASH column does not exist.");
        }
        return isConsumerSecretHashColumnsExist;
    }

    public void deleteConsumerSecretHashColumn(Connection connection) throws SQLException {

        try (PreparedStatement preparedStatement = connection.prepareStatement(DELETE_CONSUMER_SECRET_HASH_COLUMN)) {
            preparedStatement.executeUpdate();
        }
    }

    /**
     * Method to retrieve all the client secrets from the database
     *
     * @param connection
     * @return list of client secrets
     * @throws SQLException
     */
    public List<ClientSecretInfo> getAllClientSecrets(Connection connection) throws SQLException {

        List<ClientSecretInfo> clientSecretInfoList = new ArrayList<>();
        try (PreparedStatement preparedStatement = connection.prepareStatement(RETRIEVE_ALL_CONSUMER_SECRETS);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                clientSecretInfoList
                        .add(new ClientSecretInfo(resultSet.getString("CONSUMER_SECRET"),
                                resultSet.getInt("ID")));
            }
            connection.commit();
        }
        return clientSecretInfoList;
    }

    /**
     * Update the client secrets encrypted with new algorithm to the database
     *
     * @param updatedClientSecretList updated list of client secrets
     * @param connection              identity database connection
     * @throws SQLException
     */
    public void updateNewClientSecrets(List<ClientSecretInfo> updatedClientSecretList, Connection connection)
            throws SQLException {

        try (PreparedStatement preparedStatement = connection.prepareStatement(UPDATE_CONSUMER_SECRET)) {
            for (ClientSecretInfo clientSecretInfo : updatedClientSecretList) {
                preparedStatement.setString(1, clientSecretInfo.getClientSecret());
                preparedStatement.setInt(2, clientSecretInfo.getId());
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
            connection.commit();
        }
    }
}

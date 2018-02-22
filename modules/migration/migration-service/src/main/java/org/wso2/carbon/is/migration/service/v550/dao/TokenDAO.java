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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.wso2.carbon.is.migration.service.v550.SQLConstants.ADD_ACCESS_TOKEN_HASH_COLUMN;
import static org.wso2.carbon.is.migration.service.v550.SQLConstants.RETRIEVE_ACCESS_TOKEN_TABLE_DB2SQL;
import static org.wso2.carbon.is.migration.service.v550.SQLConstants.RETRIEVE_ACCESS_TOKEN_TABLE_INFORMIX;
import static org.wso2.carbon.is.migration.service.v550.SQLConstants.RETRIEVE_ACCESS_TOKEN_TABLE_MSSQL;
import static org.wso2.carbon.is.migration.service.v550.SQLConstants.RETRIEVE_ACCESS_TOKEN_TABLE_MYSQL;
import static org.wso2.carbon.is.migration.service.v550.SQLConstants.RETRIEVE_ACCESS_TOKEN_TABLE_ORACLE;

public class TokenDAO {

    private static TokenDAO instance = new TokenDAO();
    private static final String ACCESS_TOKEN_HASH = "ACCESS_TOKEN_HASH";
    private static final String REFRESH_TOKEN_HASH = "REFRESH_TOKEN_HASH";

    private TokenDAO() {

    }

    public static TokenDAO getInstance() {

        return instance;
    }

    /*public boolean isHashColumnsAvailable(Connection connection) throws SQLException {
            return isTokenHashColumnsAvailable(connection);
    }*/

    public boolean isTokenHashColumnsAvailable(Connection connection) throws SQLException {

        String sql;
        boolean isTokenHashColumnsExist = false;
        if (connection.getMetaData().getDriverName().contains("MySQL") || connection.getMetaData().getDriverName()
                .contains("H2")) {
            sql = RETRIEVE_ACCESS_TOKEN_TABLE_MYSQL;
        } else if (connection.getMetaData().getDatabaseProductName().contains("DB2")) {
            sql = RETRIEVE_ACCESS_TOKEN_TABLE_DB2SQL;
        } else if (connection.getMetaData().getDriverName().contains("MS SQL") || connection.getMetaData()
                .getDriverName().contains("Microsoft")) {
            sql = RETRIEVE_ACCESS_TOKEN_TABLE_MSSQL;
        } else if (connection.getMetaData().getDriverName().contains("PostgreSQL")) {
            sql = RETRIEVE_ACCESS_TOKEN_TABLE_MYSQL;
        } else if (connection.getMetaData().getDriverName().contains("Informix")) {
            // Driver name = "IBM Informix JDBC Driver for IBM Informix Dynamic Server"
            sql = RETRIEVE_ACCESS_TOKEN_TABLE_INFORMIX;
        } else {
            sql = RETRIEVE_ACCESS_TOKEN_TABLE_ORACLE;
        }
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet != null) {
                try {
                    resultSet.findColumn(ACCESS_TOKEN_HASH);
                    resultSet.findColumn(REFRESH_TOKEN_HASH);
                    isTokenHashColumnsExist = true;
                } catch (SQLException e) {
                    isTokenHashColumnsExist = false;
                }
            }
        }
        return isTokenHashColumnsExist;
    }

    public void addTokenHashColumns(Connection connection) throws SQLException {

        try (PreparedStatement preparedStatement = connection.prepareStatement(ADD_ACCESS_TOKEN_HASH_COLUMN)) {
                preparedStatement.executeUpdate();
                connection.commit();
        }
    }

}

/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.is.migration.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.is.migration.service.v580.migrator.UMAPermissionTicketSchemaMigrator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Utility class for performing schema related operations.
 */
public class SchemaUtil {

    private static final Log log = LogFactory.getLog(UMAPermissionTicketSchemaMigrator.class);

    /**
     * Checks whether a column exist in a table.
     *
     * @param connection SQL connection.
     * @param columnName Column name to check.
     * @param tableName Table name to check the existence of the column.
     * @return true if the column exist. False otherwise.
     */
    public static boolean isColumnExist(Connection connection, String columnName, String tableName) {

        String isTokenIdColumnExistsMySql = "SELECT %s FROM %s LIMIT 1";
        String isTokenIdColumnExistsDb2 = "SELECT %s FROM %s FETCH FIRST 1 ROWS ONLY";
        String isTokenIdColumnExistsMsSql = "SELECT TOP 1 %s FROM %s";
        String isTokenIdColumnExistsOracle = "SELECT %s FROM %s WHERE ROWNUM < 2";

        if (connection != null) {
            try {
                connection.setAutoCommit(false);
                String sql;
                if (connection.getMetaData().getDriverName().contains("MySQL")
                    || connection.getMetaData().getDriverName().contains("H2")
                    || connection.getMetaData().getDriverName().contains("PostgreSQL")) {

                    sql = String.format(isTokenIdColumnExistsMySql, columnName, tableName);
                } else if (connection.getMetaData().getDatabaseProductName().contains("DB2")) {

                    sql = String.format(isTokenIdColumnExistsDb2, columnName, tableName);
                } else if (connection.getMetaData().getDriverName().contains("MS SQL") ||
                           connection.getMetaData().getDriverName().contains("Microsoft")) {

                    sql = String.format(isTokenIdColumnExistsMsSql, columnName, tableName);
                } else {
                    sql = String.format(isTokenIdColumnExistsOracle, columnName, tableName);
                }
                try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                    // Executing the query will return no results, if the needed database scheme is not there.
                    try (ResultSet results = preparedStatement.executeQuery()) {
                        if (results.next()) {
                            return true;
                        }
                    }
                } catch (SQLException ignore) {
                    //Ignore. Exception can be thrown when the table does not exist.
                } finally {
                    connection.commit();
                }
            } catch (SQLException e) {
                log.error("Error while retrieving table metadata for table: " + tableName + " for checking existence " +
                          "of the column:" + columnName, e);
            }
        }
        return false;
    }
}

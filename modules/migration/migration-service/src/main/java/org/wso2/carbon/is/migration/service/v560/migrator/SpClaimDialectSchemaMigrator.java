/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.is.migration.service.v560.migrator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.core.migrate.MigrationClientException;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.is.migration.service.SchemaMigrator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SpClaimDialectSchemaMigrator extends SchemaMigrator {
    private static final Log log = LogFactory.getLog(SpClaimDialectSchemaMigrator.class);

    public static final String IS_SP_CLAIM_DIALECT_TABLE_EXISTS_MYSQL = "SELECT ID FROM SP_CLAIM_DIALECT LIMIT 1";
    public static final String IS_SP_CLAIM_DIALECT_TABLE_EXISTS_DB2SQL = "SELECT ID FROM SP_CLAIM_DIALECT FETCH FIRST" +
            " 1 ROWS ONLY";
    public static final String IS_SP_CLAIM_DIALECT_TABLE_EXISTS_MSSQL = "SELECT TOP 1 ID FROM SP_CLAIM_DIALECT";
    public static final String IS_SP_CLAIM_DIALECT_TABLE_EXISTS_ORACLE = "SELECT ID FROM SP_CLAIM_DIALECT WHERE " +
            "ROWNUM < 2";

    @Override
    public void migrate() throws MigrationClientException {

        if (!isSPClaimDialectStoringSupportAvailable()) {
            log.info("SP_CLAIM_DIALECT does not exist in the database. Hence adding the table.");
            super.migrate();
        } else {
            log.info("SP_CLAIM_DIALECT already exist in the database. Hence skipping.");
        }
    }

    /**
     * SP claim dialect storing (in the DB) is shipped as an update to this version.
     * It needs a database schema change. If the schema change is not done, the existing code should work without an
     * error.
     * This method returns true, of the schema change is done, false otherwise.
     *
     * @return
     */
    private boolean isSPClaimDialectStoringSupportAvailable() {

        try (Connection connection = IdentityDatabaseUtil.getDBConnection()) {
            if (connection != null) {
                String sql;
                if (connection.getMetaData().getDriverName().contains("MySQL")
                        || connection.getMetaData().getDriverName().contains("H2")
                        || connection.getMetaData().getDriverName().contains("PostgreSQL")) {
                    sql = IS_SP_CLAIM_DIALECT_TABLE_EXISTS_MYSQL;
                } else if (connection.getMetaData().getDatabaseProductName().contains("DB2")) {
                    sql = IS_SP_CLAIM_DIALECT_TABLE_EXISTS_DB2SQL;
                } else if (connection.getMetaData().getDriverName().contains("MS SQL") ||
                        connection.getMetaData().getDriverName().contains("Microsoft")) {
                    sql = IS_SP_CLAIM_DIALECT_TABLE_EXISTS_MSSQL;
                } else {
                    sql = IS_SP_CLAIM_DIALECT_TABLE_EXISTS_ORACLE;
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
                }
            }
        } catch (SQLException e) {
            log.error("Error while retrieving the database connection to check the existence of SP_CLAIM_DIALECT " +
                    "table", e);
        }
        return false;
    }
}

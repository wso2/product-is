/*
 *  Copyright (c) 2005-2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.is.migration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.is.migration.client.internal.ISMigrationServiceDataHolder;
import org.wso2.carbon.is.migration.util.Constants;
import org.wso2.carbon.is.migration.util.ResourceUtil;
import org.wso2.carbon.utils.dbcreator.DatabaseCreator;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.StringTokenizer;

public class MigrationDatabaseCreator {

    private static Log log = LogFactory.getLog(MigrationDatabaseCreator.class);
    private DataSource dataSource;
    private DataSource umDataSource;
    private Connection conn = null;
    private Statement statement;
    private String delimiter = ";";

    public MigrationDatabaseCreator(DataSource dataSource, DataSource umDataSource) {

//        super(dataSource);
        this.dataSource = dataSource;
        this.umDataSource = umDataSource;
    }

    /**
     * Execute Migration Script
     *
     * @throws Exception
     */
    public void executeIdentityMigrationScript() throws Exception {

        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);
            String databaseType = DatabaseCreator.getDatabaseType(this.conn);
            if ("mysql".equals(databaseType)){
                ResourceUtil.setMySQLDBName(conn);
            }
            statement = conn.createStatement();
            DatabaseMetaData meta = conn.getMetaData();
            String schema = null;
            if ("oracle".equals(databaseType)){
                schema = ISMigrationServiceDataHolder.getIdentityOracleUser();
            }
            ResultSet res = meta.getTables(null, schema, "IDN_AUTH_SESSION_STORE", new String[] {"TABLE"});
            if (!res.next()) {
                String dbscriptName = getIdentityDbScriptLocation(databaseType, Constants.VERSION_5_0_0, Constants
                        .VERSION_5_0_0_SP1);
                executeSQLScript(dbscriptName);
            }
            String dbscriptName = getIdentityDbScriptLocation(databaseType, Constants.VERSION_5_0_0_SP1, Constants
                    .VERSION_5_1_0);
            executeSQLScript(dbscriptName);
            conn.commit();
            if (log.isTraceEnabled()) {
                log.trace("Migration script executed successfully.");
            }
        } catch (SQLException e) {
            String msg = "Failed to execute the migration script. " + e.getMessage();
            log.fatal(msg, e);
            throw new Exception(msg, e);
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                log.error("Failed to close database connection.", e);
            }
        }
    }

    public void executeUmMigrationScript() throws Exception {

        try {
            conn = umDataSource.getConnection();
            conn.setAutoCommit(false);
            String databaseType = DatabaseCreator.getDatabaseType(this.conn);
            if ("mysql".equals(databaseType)){
                ResourceUtil.setMySQLDBName(conn);
            }
            statement = conn.createStatement();

            String dbscriptName = getUmDbScriptLocation(databaseType, Constants.VERSION_5_0_0, Constants.VERSION_5_1_0);
            executeSQLScript(dbscriptName);
            conn.commit();
            if (log.isTraceEnabled()) {
                log.trace("Migration script executed successfully.");
            }
        } catch (SQLException e) {
            String msg = "Failed to execute the migration script. " + e.getMessage();
            log.fatal(msg, e);
            throw new Exception(msg, e);
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                log.error("Failed to close database connection.", e);
            }
        }
    }

    protected String getIdentityDbScriptLocation(String databaseType, String from, String to) {

        String scriptName = databaseType + ".sql";
        String carbonHome = System.getProperty("carbon.home");

        if (Constants.VERSION_5_0_0.equals(from) && Constants.VERSION_5_0_0_SP1.equals(to)) {
            return carbonHome + File.separator + "dbscripts" + File.separator + "identity" + File.separator +
                    "migration-5.0.0_to_5.0.0SP1" + File.separator + scriptName;
        } else if (Constants.VERSION_5_0_0_SP1.equals(from) && Constants.VERSION_5_1_0.equals(to)) {
            return carbonHome + File.separator + "dbscripts" + File.separator + "identity" + File.separator +
                    "migration-5.0.0SP1_to_5.1.0" + File.separator + scriptName;
        } else {
            throw new IllegalArgumentException("Invalid migration versions provided");
        }
    }

    protected String getUmDbScriptLocation(String databaseType, String from, String to) {

        String scriptName = databaseType + ".sql";
        String carbonHome = System.getProperty("carbon.home");

        if (Constants.VERSION_5_0_0.equals(from) && Constants.VERSION_5_1_0.equals(to)) {
            return carbonHome + File.separator + "dbscripts" + File.separator + "migration-5.0.0_to_5.1.0" + File
                    .separator + scriptName;
        } else {
            throw new IllegalArgumentException("Invalid migration versions provided");
        }
    }

    /**
     * executes content in SQL script
     *
     * @return StringBuffer
     * @throws Exception
     */
    private void executeSQLScript(String dbscriptName) throws Exception {

        String databaseType = DatabaseCreator.getDatabaseType(this.conn);
        boolean oracleUserChanged = true;
        boolean keepFormat = false;
        if ("oracle".equals(databaseType)) {
            delimiter = "/";
            oracleUserChanged = false;
        } else if ("db2".equals(databaseType)) {
            delimiter = "/";
        } else if ("openedge".equals(databaseType)) {
            delimiter = "/";
            keepFormat = true;
        }

        StringBuffer sql = new StringBuffer();
        BufferedReader reader = null;

        try {
            InputStream is = new FileInputStream(dbscriptName);
            reader = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!keepFormat) {
                    if (line.startsWith("//")) {
                        continue;
                    }
                    if (line.startsWith("--")) {
                        continue;
                    }
                    StringTokenizer st = new StringTokenizer(line);
                    if (st.hasMoreTokens()) {
                        String token = st.nextToken();
                        if ("REM".equalsIgnoreCase(token)) {
                            continue;
                        }
                    }
                }
                //add the oracle database owner
                if (!oracleUserChanged && "oracle".equals(databaseType) && line.contains("databasename :=")){
                    line = "databasename := '"+ISMigrationServiceDataHolder.getIdentityOracleUser()+"';";
                    oracleUserChanged = true;
                }
                sql.append(keepFormat ? "\n" : " ").append(line);

                // SQL defines "--" as a comment to EOL
                // and in Oracle it may contain a hint
                // so we cannot just remove it, instead we must end it
                if (!keepFormat && line.indexOf("--") >= 0) {
                    sql.append("\n");
                }
                if ((DatabaseCreator.checkStringBufferEndsWith(sql, delimiter))) {
                    executeSQL(sql.substring(0, sql.length() - delimiter.length()));
                    sql.replace(0, sql.length(), "");
                }
            }
            // Catch any statements not followed by ;
            if (sql.length() > 0) {
                executeSQL(sql.toString());
            }
        } catch (Exception e) {
            log.error("Error occurred while executing SQL script for migrating database", e);
            throw new Exception("Error occurred while executing SQL script for migrating database", e);

        } finally {
            if(reader != null){
                reader.close();
            }
        }
    }

    /**
     * executes given sql
     *
     * @param sql
     * @throws Exception
     */
    private void executeSQL(String sql) throws Exception {

        // Check and ignore empty statements
        if ("".equals(sql.trim())) {
            return;
        }

        ResultSet resultSet = null;
        try {
            if (log.isDebugEnabled()) {
                log.debug("SQL : " + sql);
            }

            boolean ret;
            int updateCount = 0, updateCountTotal = 0;
            ret = statement.execute(sql);
            updateCount = statement.getUpdateCount();
            resultSet = statement.getResultSet();
            do {
                if (!ret && updateCount != -1) {
                    updateCountTotal += updateCount;
                }
                ret = statement.getMoreResults();
                if (ret) {
                    updateCount = statement.getUpdateCount();
                    resultSet = statement.getResultSet();
                }
            } while (ret);

            if (log.isDebugEnabled()) {
                log.debug(sql + " : " + updateCountTotal + " rows affected");
            }
            SQLWarning warning = conn.getWarnings();
            while (warning != null) {
                log.debug(warning + " sql warning");
                warning = warning.getNextWarning();
            }
            conn.clearWarnings();
        } catch (SQLException e) {
            if (e.getSQLState().equals("X0Y32") || e.getSQLState().equals("42710")) {
                // eliminating the table already exception for the derby and DB2 database types
                if (log.isDebugEnabled()) {
                    log.info("Table Already Exists. Hence, skipping table creation");
                }
            } else {
                throw new Exception("Error occurred while executing : " + sql, e);
            }
        } finally {
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException e) {
                    log.error("Error occurred while closing result set.", e);
                }
            }
        }
    }
}

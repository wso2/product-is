package org.wso2.carbon.is.migration.client;


import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.is.migration.ISMigrationException;
import org.wso2.carbon.is.migration.SQLConstants;
import org.wso2.carbon.is.migration.bean.OAuth2Scope;
import org.wso2.carbon.is.migration.bean.OAuth2ScopeBinding;
import org.wso2.carbon.is.migration.client.internal.ISMigrationServiceDataHolder;
import org.wso2.carbon.is.migration.dao.IDNOAuth2ScopeBindingDAO;
import org.wso2.carbon.is.migration.dao.IDNOAuth2ScopeDAO;
import org.wso2.carbon.is.migration.util.Constants;
import org.wso2.carbon.is.migration.util.ResourceUtil;
import org.wso2.carbon.utils.dbcreator.DatabaseCreator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import javax.sql.DataSource;

public class MigrateIdentityDB {
    private static final Log log = LogFactory.getLog(MigrateIdentityDB.class);

    private DataSource dataSource;
    private boolean continueOnError;
    private boolean noBatchUpdate;

    private Connection conn = null;
    private Statement statement;
    private String delimiter = ";";

    public MigrateIdentityDB(DataSource dataSource, boolean continueOnError, boolean noBatchUpdate) {
        this.dataSource = dataSource;
        this.continueOnError = continueOnError;
        this.noBatchUpdate = noBatchUpdate;
    }


    /**
     * Migrating oath2 scopes binding to a separate table.
     *
     * @throws Exception
     */
    public void migrateOAuth2ScopeData() throws Exception {

        log.info(Constants.MIGRATION_LOG_PREFIX + "Migration starting OAuth2 Scope Bindings to the new table.");

        try {
            List<OAuth2ScopeBinding> oAuth2ScopeBindingList = new ArrayList<>();

            IDNOAuth2ScopeDAO idnoAuth2ScopeDAO = IDNOAuth2ScopeDAO.getInstance();

            List<OAuth2Scope> oAuth2ScopeRoles = idnoAuth2ScopeDAO.getOAuth2ScopeRoles(conn);

            if (!oAuth2ScopeRoles.isEmpty()) {
                for (OAuth2Scope oAuth2ScopeRole : oAuth2ScopeRoles) {
                    String roleString = oAuth2ScopeRole.getRoleString();
                    if (StringUtils.isNotBlank(roleString)) {
                        String[] roleStringArray = roleString.split(",");
                        for (String role : roleStringArray) {
                            oAuth2ScopeBindingList.add(new OAuth2ScopeBinding(oAuth2ScopeRole.getScopeId(), role));
                        }
                    }
                }
                IDNOAuth2ScopeBindingDAO idnoAuth2ScopeBindingDAO = IDNOAuth2ScopeBindingDAO.getInstance();
                idnoAuth2ScopeBindingDAO.addOAuth2ScopeBinding(oAuth2ScopeBindingList, continueOnError);

                idnoAuth2ScopeDAO.updateOAuth2ScopeBinding(oAuth2ScopeRoles);
                log.info(Constants.MIGRATION_LOG_PREFIX + "OAuth2 Scope Bindings Successfully migrated.");
            } else {
                log.info(Constants.MIGRATION_LOG_PREFIX + "No Oauth2 Scopes found.");
            }
        } catch (Exception e) {
            log.error(e);
            if(!continueOnError){
                log.warn(Constants.MIGRATION_CONTINUE_ON_ERROR_WARN);
                throw e;
            }
        }
    }

    /**
     * Migrating Identity DB with some schema changes in sql files.
     *
     * @throws Exception
     */
    public void migrateIdentityDB() throws Exception {

        if (ResourceUtil.isSchemaMigrated(dataSource)) {
            log.info(Constants.MIGRATION_LOG_PREFIX + "Identity Database will not be executed because of the schema "
                     + "is already migrated within previous starting.");
            return ;
        }
        log.info(Constants.MIGRATION_LOG_PREFIX + "Executing Identity Migration Scripts.");
        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);
            String databaseType = DatabaseCreator.getDatabaseType(this.conn);
            if ("mysql".equals(databaseType)) {
                ResourceUtil.setMySQLDBName(conn);
            }
            statement = conn.createStatement();

            String dbscriptName = getIdentityDbScriptLocation(databaseType, Constants.VERSION_5_3_0, Constants
                    .VERSION_5_4_0);
            executeSQLScript(dbscriptName);
            conn.commit();
            log.info(Constants.MIGRATION_LOG_PREFIX + "Identity DB Migration script executed successfully.");

        } catch (Exception e) {
            log.error(e);
            if(!continueOnError){
                log.warn(Constants.MIGRATION_CONTINUE_ON_ERROR_WARN);
                throw e;
            }
        } finally {
            try {
                if (statement != null) {
                    statement.close();
                }
            } catch (SQLException e) {
                log.error("Failed to close database statement.", e);
            }
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                log.error("Failed to close database connection.", e);
            }
        }
    }

    public void identityDBPostMigrationScript() throws Exception {

        log.info(Constants.MIGRATION_LOG_PREFIX + " Executing Identity Post Migration Scripts.");
        Connection connection = IdentityDatabaseUtil.getDBConnection();
        PreparedStatement prepStmt = null;

        String databaseType = DatabaseCreator.getDatabaseType(connection);
        String query = SQLConstants
                .getQuery(SQLConstants.DBSpecificQuerie.IDN_OAUTH2_SCOPE_TABLE_SCOPE_KEY_SET_NULL, databaseType);

        try {
            prepStmt = connection.prepareStatement(query);
            prepStmt.execute();
            connection.commit();
            log.info(Constants.MIGRATION_LOG_PREFIX +  "Identity Post Migration Scripts executed successfully.");
        } catch (Exception e) {
            log.error(e);
            if(!continueOnError){
                throw new ISMigrationException("Error while adding OAuth2ScopeBinding, " , e);
            }
        } finally {
            IdentityDatabaseUtil.closeStatement(prepStmt);
            IdentityDatabaseUtil.closeConnection(connection);
        }
    }


    protected String getIdentityDbScriptLocation(String databaseType, String from, String to) {

        String scriptName = databaseType + ".sql";
        String carbonHome = System.getProperty("carbon.home");

        if (Constants.VERSION_5_3_0.equals(from) && Constants.VERSION_5_4_0.equals(to)) {
            return carbonHome + File.separator + "dbscripts" + File.separator + "identity" + File.separator +
                   "migration-5.3.0_to_5.4.0" + File.separator + scriptName;
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
                if (!oracleUserChanged && "oracle".equals(databaseType) && line.contains("databasename :=")) {
                    line = "databasename := '" + ISMigrationServiceDataHolder.getIdentityOracleUser() + "';";
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
            if (reader != null) {
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

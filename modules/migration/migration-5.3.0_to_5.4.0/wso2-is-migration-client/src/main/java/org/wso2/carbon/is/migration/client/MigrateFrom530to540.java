/*
* Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.is.migration.client;

import org.apache.axiom.om.OMElement;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.core.util.IdentityConfigParser;
import org.wso2.carbon.identity.core.util.IdentityCoreConstants;
import org.wso2.carbon.is.migration.ISMigrationException;
import org.wso2.carbon.is.migration.client.internal.ISMigrationServiceDataHolder;
import org.wso2.carbon.is.migration.util.Constants;
import org.wso2.carbon.user.core.util.DatabaseUtil;
import org.wso2.carbon.utils.dbcreator.DatabaseCreator;

import java.sql.Connection;
import java.sql.SQLException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.xml.namespace.QName;


/**
 * MigrateFrom530to540 is the implementation of MigrationClient and use to trigger the migration process by the
 * identity.core.
 */
public class MigrateFrom530to540 implements MigrationClient {

    private static final Log log = LogFactory.getLog(MigrateFrom530to540.class);

    private DataSource dataSource;
    private DataSource umDataSource;


    /**
     * Initializing all the data sources within this constructor.
     *
     * @throws IdentityException
     */
    public MigrateFrom530to540() throws IdentityException {

        try {
            log.info("..............................  IS 5.3.0 to 5.4.0 Migration "
                     + "Process Stating ................................");
            log.info(Constants.MIGRATION_LOG + "Initializing identity and user management data sources.");
            initIdentityDataSource();
            initUMDataSource();
            initOracleDataSource();
            log.info(Constants.MIGRATION_LOG +
                     "Initialization was done for identity and user management data sources.");
        } catch (Exception e) {
            String errorMsg = "Error while initializing data sources.";
            log.error(errorMsg, e);
            throw new ISMigrationException(errorMsg, e);
        }
    }

    /**
     * Migration process started from this method and doing all the subsequent process within this.
     *
     * @throws Exception
     */
    public void databaseMigration() throws Exception {

        log.info(Constants.MIGRATION_LOG + "Database migration process going to be started. First load the system "
                 + "properties.");

        boolean selectiveMigration = false;

        String migrateIdentityDB = loadSystemParam("migrateIdentityDB");
        String migrateUMDB = loadSystemParam("migrateUMDB");
        String migrateClaimData = loadSystemParam("migrateClaimData");
        String migratePermissionData = loadSystemParam("migratePermissionData");

        String continueOnErrorDB = loadSystemParam("continueOnError");
        String noBatchUpdateDB = loadSystemParam("noBatchUpdate");

        boolean continueOnError = Boolean.parseBoolean(continueOnErrorDB);
        boolean noBatchUpdate = Boolean.parseBoolean(noBatchUpdateDB);

        IdentityDBMigrator identityDBMigrator = new IdentityDBMigrator(dataSource, continueOnError, noBatchUpdate);
        UMDBMigrator umDBMigrator = new UMDBMigrator(umDataSource, continueOnError, noBatchUpdate);
        ClaimDBMigrator claimDBMigrator = new ClaimDBMigrator(continueOnError, noBatchUpdate);
        PermissionDBMigrator permissionDBMigrator = new PermissionDBMigrator(dataSource, continueOnError, noBatchUpdate);


        if (Boolean.parseBoolean(migrateIdentityDB)) {
            selectiveMigration = true;
            identityDBMigrator.migrateIdentityDB();
            identityDBMigrator.migrateOAuth2ScopeData();
            identityDBMigrator.identityDBPostMigrationScript();
        }

        if (Boolean.parseBoolean(migrateUMDB)) {
            selectiveMigration = true;
            umDBMigrator.migrateUMDB();
        }

        if (Boolean.parseBoolean(migrateClaimData)) {
            selectiveMigration = true;
            claimDBMigrator.migrateClaimData();
        }

        if (Boolean.parseBoolean(migratePermissionData)) {
            selectiveMigration = true;
            permissionDBMigrator.migratePermissionData();
        }

        if (!selectiveMigration) {
            identityDBMigrator.migrateIdentityDB();
            identityDBMigrator.migrateOAuth2ScopeData();
            identityDBMigrator.identityDBPostMigrationScript();

            umDBMigrator.migrateUMDB();

            claimDBMigrator.migrateClaimData();
            permissionDBMigrator.migratePermissionData();
        }


        log.info(Constants.MIGRATION_LOG + "Database migration process finished. Please check the existing "
                 + "functionality are working fine or not. ");
    }

    /**
     * Init Oracle specific database.
     */
    private void initOracleDataSource() {

        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            if ("oracle".equals(DatabaseCreator.getDatabaseType(conn)) && ISMigrationServiceDataHolder
                                                                                  .getIdentityOracleUser() == null) {
                ISMigrationServiceDataHolder.setIdentityOracleUser(dataSource.getConnection().getMetaData()
                                                                           .getUserName());
                log.info(Constants.MIGRATION_LOG + "Initialized identity database in Oracle.");
            }
        } catch (Exception e) {
            log.error("Error occurred while initializing identity database for Oracle.", e);
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                log.error("Error while closing the identity database connection", e);
            }
        }


        try {
            conn = umDataSource.getConnection();
            if ("oracle".equals(DatabaseCreator.getDatabaseType(conn)) && ISMigrationServiceDataHolder
                                                                                  .getIdentityOracleUser() == null) {
                ISMigrationServiceDataHolder.setIdentityOracleUser(umDataSource.getConnection().getMetaData()
                                                                           .getUserName());
                log.info(Constants.MIGRATION_LOG + "Initialized user management database in Oracle.");
            }
        } catch (Exception e) {
            log.error("Error occurred while initializing user management database for Oracle.", e);
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                log.error("Error while closing the user manager database connection", e);
            }
        }
    }

    /**
     * Initialize the identity datasource.
     *
     * @throws IdentityException
     */
    private void initIdentityDataSource() throws IdentityException {

        try {
            OMElement persistenceManagerConfigElem = IdentityConfigParser.getInstance()
                    .getConfigElement("JDBCPersistenceManager");

            if (persistenceManagerConfigElem == null) {
                String errorMsg = "Identity Persistence Manager configuration is not available in " +
                                  "identity.xml file. Terminating the JDBC Persistence Manager " +
                                  "initialization. This may affect certain functionality.";
                log.error(errorMsg);
                throw new ISMigrationException(errorMsg);
            }

            OMElement dataSourceElem = persistenceManagerConfigElem.getFirstChildWithName(
                    new QName(IdentityCoreConstants.IDENTITY_DEFAULT_NAMESPACE, "DataSource"));

            if (dataSourceElem == null) {
                String errorMsg = "DataSource Element is not available for JDBC Persistence " +
                                  "Manager in identity.xml file. Terminating the JDBC Persistence Manager " +
                                  "initialization. This might affect certain features.";
                log.error(errorMsg);
                throw new ISMigrationException(errorMsg);
            }

            OMElement dataSourceNameElem = dataSourceElem.getFirstChildWithName(
                    new QName(IdentityCoreConstants.IDENTITY_DEFAULT_NAMESPACE, "Name"));

            if (dataSourceNameElem != null) {
                String dataSourceName = dataSourceNameElem.getText();
                Context ctx = new InitialContext();
                dataSource = (DataSource) ctx.lookup(dataSourceName);
                if (dataSource != null) {
                    log.info(Constants.MIGRATION_LOG + "Initialized the identity database successfully.");
                }
            }
        } catch (NamingException e) {
            String errorMsg = "Error when looking up the Identity Data Source.";
            throw new ISMigrationException(errorMsg, e);
        }
    }

    /**
     * Initialize UM Data Source.
     *
     * @throws ISMigrationException
     */
    private void initUMDataSource() throws ISMigrationException {
        umDataSource = DatabaseUtil.getRealmDataSource(ISMigrationServiceDataHolder.getRealmService()
                                                               .getBootstrapRealmConfiguration());
        if (umDataSource == null) {
            String errorMsg = "UM Datasource initialization error.";
            throw new ISMigrationException(errorMsg);
        }
    }

    private String loadSystemParam(String paramName) {
        String paramValue = System.getProperty(paramName);
        if (StringUtils.isNotEmpty(paramValue)) {
            log.info(Constants.MIGRATION_LOG + paramName + " = " + paramValue);
        }
        return paramValue;
    }
}

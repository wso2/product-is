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
 * MigrateFrom530to540 is constructed by the identity.core to do the migration process.
 *
 */
public class MigrateFrom530to540 implements MigrationClient {

    private static final Log log = LogFactory.getLog(MigrateFrom530to540.class);

    private DataSource dataSource;
    private DataSource umDataSource;


    /**
     *
     * This is the default constructor that is call by the identity.core using reflection.
     *
     * @throws IdentityException
     */
    public MigrateFrom530to540() throws IdentityException {

        try {
            initIdentityDataSource();
            initUMDataSource();

            Connection conn = null;
            try {
                conn = dataSource.getConnection();
                if ("oracle".equals(DatabaseCreator.getDatabaseType(conn)) && ISMigrationServiceDataHolder
                        .getIdentityOracleUser() == null) {
                    ISMigrationServiceDataHolder.setIdentityOracleUser(dataSource.getConnection().getMetaData()
                            .getUserName());
                }
            } catch (Exception e) {
                log.error("Error while reading the identity oracle username", e);
            } finally {
                try {
                    conn.close();
                } catch (SQLException e) {
                    log.warn("Error while closing the identity database connection", e);
                }
            }
            try {
                conn = umDataSource.getConnection();
                if ("oracle".equals(DatabaseCreator.getDatabaseType(conn)) && ISMigrationServiceDataHolder
                        .getIdentityOracleUser() == null) {
                    ISMigrationServiceDataHolder.setIdentityOracleUser(umDataSource.getConnection().getMetaData()
                            .getUserName());
                }
            } catch (Exception e) {
                log.error("Error while reading the user manager database oracle username", e);
            } finally {
                try {
                    conn.close();
                } catch (SQLException e) {
                    log.warn("Error while closing the user manager database connection", e);
                }
            }
        } catch (IdentityException e) {
            String errorMsg = "Error when reading the JDBC Configuration from the file.";
            log.error(errorMsg, e);
            throw new ISMigrationException(errorMsg, e);
        }
    }

    /**
     * Initialize the identity datasource.
     *
     * @throws IdentityException
     */
    private void initIdentityDataSource() throws IdentityException {

        log.info(Constants.MIGRATION_LOG_PREFIX + "Identity Database Initialization started.");

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
                log.info(Constants.MIGRATION_LOG_PREFIX + "Identity Database Initialization Done.");
            }
        } catch (NamingException e) {
            String errorMsg = "Error when looking up the Identity Data Source.";
            log.error(errorMsg, e);
            throw new ISMigrationException(errorMsg, e);
        }
    }

    private void initUMDataSource() throws ISMigrationException {
        umDataSource = DatabaseUtil.getRealmDataSource(ISMigrationServiceDataHolder.getRealmService()
                .getBootstrapRealmConfiguration());
        if (umDataSource == null) {
            String errorMsg = "UM Datasource reading error.";
            log.error(errorMsg);
            throw new ISMigrationException(errorMsg);
        }
    }


    /**
     * Migration process started from this method and doing all the subsequent process within this.
     *
     *
     * @throws Exception
     */
    public void databaseMigration() throws Exception {

        log.info(Constants.MIGRATION_LOG_PREFIX + "Database migration process started for the following parameters.");

        boolean selectiveMigration = false;
        String migrateIdentityDB = System.getProperty("migrateIdentityDB");
        String migrateClaimData = System.getProperty("migrateClaimData");
        String migratePermissionData = System.getProperty("migratePermissionData");
        String continueOnErrorDB = System.getProperty("continueOnError");
        String noBatchUpdateDB = System.getProperty("noBatchUpdate");

        log.info(Constants.MIGRATION_LOG_PREFIX + "migrateIdentityDB : " + migrateIdentityDB);
        log.info(Constants.MIGRATION_LOG_PREFIX + "migrateClaimData : " + migrateClaimData);
        log.info(Constants.MIGRATION_LOG_PREFIX + "migratePermissionData : " + migratePermissionData);
        log.info(Constants.MIGRATION_LOG_PREFIX + "continueOnError : " + continueOnErrorDB);
        log.info(Constants.MIGRATION_LOG_PREFIX + "noBatchUpdate : " + noBatchUpdateDB);


        boolean continueOnError = Boolean.parseBoolean(continueOnErrorDB) ;
        boolean noBatchUpdate = Boolean.parseBoolean(noBatchUpdateDB) ;

        MigrateIdentityDB migrateIdentity = new MigrateIdentityDB(dataSource,continueOnError,noBatchUpdate);
        MigrateClaimDB migrateClaimDB = new MigrateClaimDB(continueOnError,noBatchUpdate);
        MigratePermissionDB migratePermissionDB = new MigratePermissionDB(dataSource, continueOnError,noBatchUpdate);


        if (Boolean.parseBoolean(migrateIdentityDB)) {
            selectiveMigration = true;
            migrateIdentity.migrateIdentityDB();
            migrateIdentity.migrateOAuth2ScopeData();
            migrateIdentity.identityDBPostMigrationScript();
        }

        if (Boolean.parseBoolean(migrateClaimData)) {
            selectiveMigration = true;
            migrateClaimDB.migrateClaimData();
        }

        if (Boolean.parseBoolean(migratePermissionData)) {
            selectiveMigration = true;
            migratePermissionDB.migratePermissionData();
        }

        if (!selectiveMigration) {
            migrateIdentity.migrateIdentityDB();
            migrateIdentity.migrateOAuth2ScopeData();
            migrateIdentity.identityDBPostMigrationScript();

            migrateClaimDB.migrateClaimData();
            migratePermissionDB.migratePermissionData();
        }

    }



}

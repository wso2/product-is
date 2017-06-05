/*
* Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.wso2.carbon.is.migration.MigrationDatabaseCreator;
import org.wso2.carbon.is.migration.client.internal.ISMigrationServiceDataHolder;
import org.wso2.carbon.is.migration.util.ResourceUtil;
import org.wso2.carbon.user.core.util.DatabaseUtil;
import org.wso2.carbon.utils.dbcreator.DatabaseCreator;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.xml.namespace.QName;
import java.sql.Connection;
import java.sql.SQLException;

@SuppressWarnings("unchecked")
public class MigrateFrom510to520 implements MigrationClient {

    private static final Log log = LogFactory.getLog(MigrateFrom510to520.class);
    private static final String RESOURCES_XML = "/resources.xml";
    private DataSource dataSource;
    private DataSource umDataSource;

    public MigrateFrom510to520() throws IdentityException {
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
     * Initialize the identity datasource
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
            }
        } catch (NamingException e) {
            String errorMsg = "Error when looking up the Identity Data Source.";
            log.error(errorMsg, e);
            throw new ISMigrationException(errorMsg, e);
        }
    }

    private void initUMDataSource() {
        umDataSource = DatabaseUtil.getRealmDataSource(ISMigrationServiceDataHolder.getRealmService()
                .getBootstrapRealmConfiguration());
    }

    /*
     * @throws ISMigrationException
     * @throws SQLException
     */
    public void databaseMigration() throws Exception {
        boolean selectiveMigration = false;
        String migrateIdentityDB = System.getProperty("migrateIdentityDB");
        String migrateUMDB = System.getProperty("migrateUMDB");
        // to decide whether we need to migrate active tenants only.
        String migrateActiveTenants = System.getProperty("migrateActiveTenantsOnly");
        boolean migrateActiveTenantsOnly = Boolean.parseBoolean(migrateActiveTenants);
        if (migrateActiveTenantsOnly) {
            log.info("Migrate Active Tenants Only option enabled.");
        }

        if (Boolean.parseBoolean(migrateIdentityDB)) {
            selectiveMigration = true;
            migrateIdentityDB();
            log.info("Migrated the identity database schema");
        }
        if (Boolean.parseBoolean(migrateUMDB)) {
            selectiveMigration = true;
            migrateUMDB();
            log.info("Migrated the user management database schema");
        }

        if (!selectiveMigration) {
            migrateIdentityDB();
            log.info("Migration completed from IS 5.1.0 to IS 5.2.0");
        }
    }


    public void migrateIdentityDB() throws Exception {

        if (!ResourceUtil.isSchemaMigrated(dataSource)) {
            MigrationDatabaseCreator migrationDatabaseCreator = new MigrationDatabaseCreator(dataSource, umDataSource);
            migrationDatabaseCreator.executeIdentityMigrationScript();
        } else {
            log.info("Identity schema is already migrated");
        }
    }

    public void migrateUMDB() throws Exception {
        MigrationDatabaseCreator migrationDatabaseCreator = new MigrationDatabaseCreator(dataSource, umDataSource);
        migrationDatabaseCreator.executeUmMigrationScript();
    }

}

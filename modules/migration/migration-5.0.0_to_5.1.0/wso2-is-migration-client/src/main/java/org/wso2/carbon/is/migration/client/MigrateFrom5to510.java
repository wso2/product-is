/*
* Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.mgt.ApplicationConstants;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.core.util.IdentityConfigParser;
import org.wso2.carbon.identity.core.util.IdentityCoreConstants;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.is.migration.ISMigrationException;
import org.wso2.carbon.is.migration.MigrationDatabaseCreator;
import org.wso2.carbon.is.migration.client.internal.ISMigrationServiceDataHolder;
import org.wso2.carbon.is.migration.util.ResourceUtil;
import org.wso2.carbon.is.migration.util.SQLQueries;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.util.DatabaseUtil;
import org.wso2.carbon.user.core.util.UserCoreUtil;
import org.wso2.carbon.utils.dbcreator.DatabaseCreator;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.xml.namespace.QName;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

@SuppressWarnings("unchecked")
public class MigrateFrom5to510 implements MigrationClient {

    private static final Log log = LogFactory.getLog(MigrateFrom5to510.class);
    private DataSource dataSource;
    private DataSource umDataSource;

    public MigrateFrom5to510() throws IdentityException {
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

    private void initUMDataSource(){
        umDataSource = DatabaseUtil.getRealmDataSource(ISMigrationServiceDataHolder.getRealmService()
                .getBootstrapRealmConfiguration());
    }

    /**
     * This method is used to migrate database tables
     * This executes the database queries according to the user's db type and alters the tables
     *
     * @throws ISMigrationException
     * @throws SQLException
     */
    public void databaseMigration() throws Exception {
        if (!ResourceUtil.isSchemaMigrated(dataSource)) {
            MigrationDatabaseCreator migrationDatabaseCreator = new MigrationDatabaseCreator(dataSource, umDataSource);
            migrationDatabaseCreator.executeIdentityMigrationScript();
            migrationDatabaseCreator.executeUmMigrationScript();
            migrateIdentityData();
            migrateIdentityDBFinalize();
            migrateUMData();
        } else {
            log.info("Identity schema is already migrated");
        }
    }

    public void migrateIdentityDB() throws Exception{

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

    /**
     * migrate data in the identity database and finalize the database table restructuring
     */
    public void migrateIdentityData(){

        Connection identityConnection = null;
        PreparedStatement selectFromAccessTokenPS = null;
        PreparedStatement insertScopeAssociationPS = null;
        PreparedStatement insertTokenScopeHashPS = null;
        PreparedStatement insertTokenIdPS = null;
        PreparedStatement updateUserNamePS = null;
        PreparedStatement selectFromAuthorizationCodePS = null;
        PreparedStatement updateUserNameAuthorizationCodePS = null;
        PreparedStatement selectIdnAssociatedIdPS = null;
        PreparedStatement updateIdnAssociatedIdPS = null;
        PreparedStatement selectConsumerAppsPS = null;
        PreparedStatement updateConsumerAppsPS = null;

        ResultSet accessTokenRS = null;
        ResultSet authzCodeRS = null;
        ResultSet selectIdnAssociatedIdRS = null;
        ResultSet selectConsumerAppsRS = null;
        try {
            identityConnection = dataSource.getConnection();
            identityConnection.setAutoCommit(false);

            selectConsumerAppsPS = identityConnection.prepareStatement(SQLQueries.SELECT_FROM_CONSUMER_APPS);
            updateConsumerAppsPS = identityConnection.prepareStatement(SQLQueries.UPDATE_CONSUMER_APPS);

            selectConsumerAppsRS = selectConsumerAppsPS.executeQuery();
            while (selectConsumerAppsRS.next()){
                int id = selectConsumerAppsRS.getInt("ID");
                String username = selectConsumerAppsRS.getString("USERNAME");
                String userDomainFromDB = selectConsumerAppsRS.getString("USER_DOMAIN");

                if (userDomainFromDB == null) {
                    String userDomain = UserCoreUtil.extractDomainFromName(username);
                    username = UserCoreUtil.removeDomainFromName(username);

                    updateConsumerAppsPS.setString(1, username);
                    updateConsumerAppsPS.setString(2, userDomain);
                    updateConsumerAppsPS.setInt(3, id);
                    updateConsumerAppsPS.addBatch();
                }
            }
            updateConsumerAppsPS.executeBatch();

            String selectFromAccessToken = SQLQueries.SELECT_FROM_ACCESS_TOKEN;
            selectFromAccessTokenPS = identityConnection.prepareStatement(selectFromAccessToken);

            String insertScopeAssociation = SQLQueries.INSERT_SCOPE_ASSOCIATION;
            insertScopeAssociationPS = identityConnection.prepareStatement(insertScopeAssociation);

            String insertTokenScopeHash = SQLQueries.INSERT_TOKEN_SCOPE_HASH;
            insertTokenScopeHashPS = identityConnection.prepareStatement(insertTokenScopeHash);

            String insertTokenId = SQLQueries.INSERT_TOKEN_ID;
            insertTokenIdPS = identityConnection.prepareStatement(insertTokenId);

            String updateUserName = SQLQueries.UPDATE_USER_NAME;
            updateUserNamePS = identityConnection.prepareStatement(updateUserName);

            accessTokenRS = selectFromAccessTokenPS.executeQuery();
            while (accessTokenRS.next()){
                String accessToken = null;
                try {
                    accessToken = accessTokenRS.getString("ACCESS_TOKEN");
                    String scopeString = accessTokenRS.getString("TOKEN_SCOPE");
                    String authzUser = accessTokenRS.getString("AUTHZ_USER");
                    String tokenIdFromDB = accessTokenRS.getString("TOKEN_ID");

                    if (tokenIdFromDB == null) {
                        String tokenId = UUID.randomUUID().toString();

                        String username = UserCoreUtil.removeDomainFromName(MultitenantUtils.getTenantAwareUsername
                                (authzUser));
                        String userDomain = UserCoreUtil.extractDomainFromName(authzUser);
                        int tenantId = ISMigrationServiceDataHolder.getRealmService().getTenantManager().getTenantId
                                (MultitenantUtils.getTenantDomain(authzUser));

                        insertTokenIdPS.setString(1, tokenId);
                        insertTokenIdPS.setString(2, accessToken);
                        insertTokenIdPS.addBatch();

                        updateUserNamePS.setString(1, username);
                        updateUserNamePS.setInt(2, tenantId);
                        updateUserNamePS.setString(3, userDomain);
                        updateUserNamePS.setString(4, authzUser);
                        updateUserNamePS.setString(5, accessToken);
                        updateUserNamePS.addBatch();

                        insertTokenScopeHashPS.setString(1, DigestUtils.md5Hex(scopeString));
                        insertTokenScopeHashPS.setString(2, accessToken);
                        insertTokenScopeHashPS.addBatch();

                        if (scopeString != null) {
                            String scopes[] = scopeString.split(" ");
                            for (String scope : scopes) {
                                insertScopeAssociationPS.setString(1, tokenId);
                                insertScopeAssociationPS.setString(2, scope);
                                insertScopeAssociationPS.addBatch();
                            }
                        }
                    }
                } catch (UserStoreException e) {
                    log.warn("Error while migrating access token : " + accessToken);
                }
            }

            String selectFromAuthorizationCode = SQLQueries.SELECT_FROM_AUTHORIZATION_CODE;
            selectFromAuthorizationCodePS = identityConnection.prepareStatement(selectFromAuthorizationCode);

            String updateUserNameAuthorizationCode = SQLQueries.UPDATE_USER_NAME_AUTHORIZATION_CODE;
            updateUserNameAuthorizationCodePS = identityConnection.prepareStatement(updateUserNameAuthorizationCode);

            authzCodeRS = selectFromAuthorizationCodePS.executeQuery();
            while (authzCodeRS.next()){
                String authorizationCode = null;
                try {
                    authorizationCode = authzCodeRS.getString("AUTHORIZATION_CODE");
                    String authzUser = authzCodeRS.getString("AUTHZ_USER");
                    String userDomainFromDB = authzCodeRS.getString("USER_DOMAIN");

                    if (userDomainFromDB == null) {
                        String username = UserCoreUtil.removeDomainFromName(MultitenantUtils.getTenantAwareUsername
                                (authzUser));
                        String userDomain = UserCoreUtil.extractDomainFromName(authzUser);
                        int tenantId = ISMigrationServiceDataHolder.getRealmService().getTenantManager().getTenantId
                                (MultitenantUtils.getTenantDomain(authzUser));

                        updateUserNameAuthorizationCodePS.setString(1, username);
                        updateUserNameAuthorizationCodePS.setInt(2, tenantId);
                        updateUserNameAuthorizationCodePS.setString(3, userDomain);
                        updateUserNameAuthorizationCodePS.setString(4, UUID.randomUUID().toString());
                        updateUserNameAuthorizationCodePS.setString(5, authzUser);
                        updateUserNameAuthorizationCodePS.setString(6, authorizationCode);
                        updateUserNameAuthorizationCodePS.addBatch();
                    }
                } catch (UserStoreException e) {
                    log.warn("Error while migrating authorization code : " + authorizationCode);
                }
            }
            insertTokenIdPS.executeBatch();
            insertScopeAssociationPS.executeBatch();
            updateUserNamePS.executeBatch();
            insertTokenScopeHashPS.executeBatch();
            updateUserNameAuthorizationCodePS.executeBatch();

            String selectIdnAssociatedId = SQLQueries.SELECT_IDN_ASSOCIATED_ID;
            selectIdnAssociatedIdPS = identityConnection.prepareStatement(selectIdnAssociatedId);
            selectIdnAssociatedIdRS = selectIdnAssociatedIdPS.executeQuery();

            updateIdnAssociatedIdPS = identityConnection.prepareStatement(SQLQueries.UPDATE_IDN_ASSOCIATED_ID);

            while (selectIdnAssociatedIdRS.next()) {
                int id = selectIdnAssociatedIdRS.getInt("ID");
                String username = selectIdnAssociatedIdRS.getString("USER_NAME");
                String userDomainFromDB = selectIdnAssociatedIdRS.getString("DOMAIN_NAME");

                if (userDomainFromDB == null) {
                    updateIdnAssociatedIdPS.setString(1, UserCoreUtil.extractDomainFromName(username));
                    updateIdnAssociatedIdPS.setString(2, UserCoreUtil.removeDomainFromName(username));
                    updateIdnAssociatedIdPS.setInt(3, id);
                    updateIdnAssociatedIdPS.addBatch();
                }
            }
            updateIdnAssociatedIdPS.executeBatch();

            identityConnection.commit();

        } catch (SQLException e) {
            IdentityDatabaseUtil.rollBack(identityConnection);
            log.error(e);
        } catch (Exception e) {
            log.error(e);
        } finally {
            IdentityDatabaseUtil.closeResultSet(accessTokenRS);
            IdentityDatabaseUtil.closeResultSet(authzCodeRS);
            IdentityDatabaseUtil.closeResultSet(selectIdnAssociatedIdRS);
            IdentityDatabaseUtil.closeResultSet(selectConsumerAppsRS);

            IdentityDatabaseUtil.closeStatement(selectFromAccessTokenPS);
            IdentityDatabaseUtil.closeStatement(insertScopeAssociationPS);
            IdentityDatabaseUtil.closeStatement(insertTokenIdPS);
            IdentityDatabaseUtil.closeStatement(updateUserNamePS);
            IdentityDatabaseUtil.closeStatement(insertTokenScopeHashPS);
            IdentityDatabaseUtil.closeStatement(updateUserNameAuthorizationCodePS);
            IdentityDatabaseUtil.closeStatement(selectFromAuthorizationCodePS);
            IdentityDatabaseUtil.closeStatement(selectIdnAssociatedIdPS);
            IdentityDatabaseUtil.closeStatement(updateIdnAssociatedIdPS);
            IdentityDatabaseUtil.closeStatement(selectConsumerAppsPS);
            IdentityDatabaseUtil.closeStatement(updateConsumerAppsPS);

            IdentityDatabaseUtil.closeConnection(identityConnection);
        }
    }

    public void migrateIdentityDBFinalize(){
        Connection identityConnection = null;
        PreparedStatement primaryKeyPS = null;
        PreparedStatement authorizationCodePrimaryKeyPS = null;
        PreparedStatement foreignKeyPS = null;
        PreparedStatement dropColumnPS = null;
        PreparedStatement notNullPS = null;

        try {
            identityConnection = dataSource.getConnection();
            identityConnection.setAutoCommit(false);

            String databaseType = DatabaseCreator.getDatabaseType(identityConnection);

            String dropTokenScopeColumn = SQLQueries.DROP_TOKEN_SCOPE_COLUMN;
            String alterTokenIdNotNull;
            if ("oracle".equals(databaseType)){
                alterTokenIdNotNull = SQLQueries.ALTER_TOKEN_ID_NOT_NULL_ORACLE;
            } else if ("mssql".equals(databaseType)){
                alterTokenIdNotNull = SQLQueries.ALTER_TOKEN_ID_NOT_NULL_MSSQL;
            } else if ("postgresql".equals(databaseType)){
                alterTokenIdNotNull = SQLQueries.ALTER_TOKEN_ID_NOT_NULL_POSTGRESQL;
            } else if ("h2".equals(databaseType)) {
                alterTokenIdNotNull = SQLQueries.ALTER_TOKEN_ID_NOT_NULL_H2;
            } else {
                alterTokenIdNotNull = SQLQueries.ALTER_TOKEN_ID_NOT_NULL_MYSQL;
            }
            String setAccessTokenPrimaryKey = SQLQueries.SET_ACCESS_TOKEN_PRIMARY_KEY;
            String setAuthorizationCodePrimaryKey = SQLQueries.SET_AUTHORIZATION_CODE_PRIMARY_KEY;
            String setScopeAssociationPrimaryKey = SQLQueries.SET_SCOPE_ASSOCIATION_PRIMARY_KEY;

            dropColumnPS = identityConnection.prepareStatement(dropTokenScopeColumn);
            dropColumnPS.execute();

            notNullPS = identityConnection.prepareStatement(alterTokenIdNotNull);
            notNullPS.execute();

            primaryKeyPS = identityConnection.prepareStatement(setAccessTokenPrimaryKey);
            primaryKeyPS.execute();

            authorizationCodePrimaryKeyPS = identityConnection.prepareStatement(setAuthorizationCodePrimaryKey);
            authorizationCodePrimaryKeyPS.execute();

            foreignKeyPS = identityConnection.prepareStatement(setScopeAssociationPrimaryKey);
            foreignKeyPS.execute();
        } catch (Exception e) {
            log.error(e);
        }finally {
            IdentityDatabaseUtil.closeStatement(primaryKeyPS);
            IdentityDatabaseUtil.closeStatement(authorizationCodePrimaryKeyPS);
            IdentityDatabaseUtil.closeStatement(foreignKeyPS);
            IdentityDatabaseUtil.closeStatement(dropColumnPS);
            IdentityDatabaseUtil.closeStatement(notNullPS);
            IdentityDatabaseUtil.closeConnection(identityConnection);

        }
    }

    public void migrateUMData() {
        Connection identityConnection = null;
        Connection umConnection = null;

        PreparedStatement selectServiceProviders = null;
        PreparedStatement updateRole = null;

        ResultSet selectServiceProvidersRS = null;

        try {
            identityConnection = dataSource.getConnection();
            umConnection = umDataSource.getConnection();

            identityConnection.setAutoCommit(false);
            umConnection.setAutoCommit(false);

            selectServiceProviders = identityConnection.prepareStatement(SQLQueries.LOAD_APP_NAMES);
            selectServiceProvidersRS = selectServiceProviders.executeQuery();

            updateRole = umConnection.prepareStatement(SQLQueries.UPDATE_ROLES);
            while (selectServiceProvidersRS.next()) {
                String appName = selectServiceProvidersRS.getString("APP_NAME");
                int tenantId = selectServiceProvidersRS.getInt("TENANT_ID");
                updateRole.setString(1, ApplicationConstants.APPLICATION_DOMAIN + UserCoreConstants.DOMAIN_SEPARATOR
                        + appName);
                updateRole.setString(2, appName);
                updateRole.setInt(3, tenantId);
                updateRole.addBatch();
            }
            updateRole.executeBatch();

            identityConnection.commit();
            umConnection.commit();
        } catch (SQLException e) {
            log.error(e);
        } finally {
            IdentityDatabaseUtil.closeResultSet(selectServiceProvidersRS);
            IdentityDatabaseUtil.closeStatement(selectServiceProviders);
            IdentityDatabaseUtil.closeStatement(updateRole);
            IdentityDatabaseUtil.closeConnection(identityConnection);
            IdentityDatabaseUtil.closeConnection(umConnection);
        }
    }
}

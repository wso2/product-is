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
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.core.util.IdentityConfigParser;
import org.wso2.carbon.identity.core.util.IdentityCoreConstants;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.is.migration.ISMigrationException;
import org.wso2.carbon.is.migration.MigrationDatabaseCreator;
import org.wso2.carbon.is.migration.client.internal.ServiceHolder;
import org.wso2.carbon.is.migration.util.SQLQueries;
import org.wso2.carbon.user.api.Tenant;
import org.wso2.carbon.user.api.UserStoreException;
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
import java.util.List;
import java.util.UUID;

@SuppressWarnings("unchecked")
public class MigrateFrom5to510 implements MigrationClient {

    private static final Log log = LogFactory.getLog(MigrateFrom5to510.class);
    private List<Tenant> tenantsArray;
    private DataSource dataSource;


    public MigrateFrom5to510() throws UserStoreException {
        try {
            initDataSource();
        } catch (IdentityException e) {
            String errorMsg = "Error when reading the JDBC Configuration from the file.";
            log.error(errorMsg, e);
            throw new UserStoreException(errorMsg, e);
        }
    }


    private void initDataSource() throws IdentityException {
        try {
            OMElement persistenceManagerConfigElem = IdentityConfigParser.getInstance()
                    .getConfigElement("JDBCPersistenceManager");

            if (persistenceManagerConfigElem == null) {
                String errorMsg = "Identity Persistence Manager configuration is not available in " +
                        "identity.xml file. Terminating the JDBC Persistence Manager " +
                        "initialization. This may affect certain functionality.";
                log.error(errorMsg);
                throw new IdentityException(errorMsg);
            }

            OMElement dataSourceElem = persistenceManagerConfigElem.getFirstChildWithName(
                    new QName(IdentityCoreConstants.IDENTITY_DEFAULT_NAMESPACE, "DataSource"));

            if (dataSourceElem == null) {
                String errorMsg = "DataSource Element is not available for JDBC Persistence " +
                        "Manager in identity.xml file. Terminating the JDBC Persistence Manager " +
                        "initialization. This might affect certain features.";
                log.error(errorMsg);
                throw new IdentityException(errorMsg);
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
            throw new IdentityException(errorMsg, e);
        }
    }
    /**
     * This method is used to migrate database tables
     * This executes the database queries according to the user's db type and alters the tables
     *
     * @param migrateVersion version to be migrated
     * @throws ISMigrationException
     * @throws SQLException
     */
    public void databaseMigration(String migrateVersion) throws Exception {


        MigrationDatabaseCreator migrationDatabaseCreator = new MigrationDatabaseCreator(dataSource);
        migrationDatabaseCreator.executeMigrationScript();
        oauthMigration();

    }

    public void oauthMigration(){
        Connection connection = null;
        PreparedStatement selectFromAccessTokenPS = null;
        PreparedStatement insertScopeAssociationPS = null;
        PreparedStatement insertTokenScopeHashPS = null;
        PreparedStatement insertTokenIdPS = null;
        PreparedStatement updateUserNamePS = null;

        PreparedStatement selectFromAuthorizationCodePS = null;
        PreparedStatement updateUserNameAuthorizationCodePS = null;
        ResultSet accessTokenRS = null;
        ResultSet authzCodeRS = null;
        try {
            connection = dataSource.getConnection();
            connection.setAutoCommit(false);

            String selectFromAccessToken = SQLQueries.SELECT_FROM_ACCESS_TOKEN;
            selectFromAccessTokenPS = connection.prepareStatement(selectFromAccessToken);

            String insertScopeAssociation = SQLQueries.INSERT_SCOPE_ASSOCIATION;
            insertScopeAssociationPS = connection.prepareStatement(insertScopeAssociation);

            String insertTokenScopeHash = SQLQueries.INSERT_TOKEN_SCOPE_HASH;
            insertTokenScopeHashPS = connection.prepareStatement(insertTokenScopeHash);

            String insertTokenId = SQLQueries.INSERT_TOKEN_ID;
            insertTokenIdPS = connection.prepareStatement(insertTokenId);

            String updateUserName = SQLQueries.UPDATE_USER_NAME;
            updateUserNamePS = connection.prepareStatement(updateUserName);

            accessTokenRS = selectFromAccessTokenPS.executeQuery();
            while (accessTokenRS.next()){
                String accessToken = null;
                try {
                    accessToken = accessTokenRS.getString(1);
                    String scopeString = accessTokenRS.getString(2);
                    String authzUser = accessTokenRS.getString(3);

                    String tokenId = UUID.randomUUID().toString();

                    String username = UserCoreUtil.removeDomainFromName(MultitenantUtils.getTenantAwareUsername
                            (authzUser));
                    String userDomain = UserCoreUtil.extractDomainFromName(authzUser);
                    int tenantId = ServiceHolder.getRealmService().getTenantManager().getTenantId(MultitenantUtils
                            .getTenantDomain(authzUser));

                    insertTokenIdPS.setString(1, tokenId);
                    insertTokenIdPS.setString(2, accessToken);
                    insertTokenIdPS.addBatch();

                    updateUserNamePS.setString(1, username);
                    updateUserNamePS.setInt(2, tenantId);
                    updateUserNamePS.setString(3, userDomain);
                    updateUserNamePS.setString(4, accessToken);
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
                } catch (UserStoreException e) {
                    log.warn("Error while migrating access token : " + accessToken);
                }
            }

            String selectFromAuthorizationCode = SQLQueries.SELECT_FROM_AUTHORIZATION_CODE;
            selectFromAuthorizationCodePS = connection.prepareStatement(selectFromAuthorizationCode);

            String updateUserNameAuthorizationCode = SQLQueries.UPDATE_USER_NAME_AUTHORIZATION_CODE;
            updateUserNameAuthorizationCodePS = connection.prepareStatement(updateUserNameAuthorizationCode);

            authzCodeRS = selectFromAuthorizationCodePS.executeQuery();
            while (authzCodeRS.next()){
                String authorizationCode = null;
                try {
                    authorizationCode = authzCodeRS.getString(1);
                    String authzUser = authzCodeRS.getString(2);

                    String username = UserCoreUtil.removeDomainFromName(MultitenantUtils.getTenantAwareUsername
                            (authzUser));
                    String userDomain = UserCoreUtil.extractDomainFromName(authzUser);
                    int tenantId = ServiceHolder.getRealmService().getTenantManager().getTenantId(MultitenantUtils
                            .getTenantDomain(authzUser));


                    updateUserNameAuthorizationCodePS.setString(1, username);
                    updateUserNameAuthorizationCodePS.setInt(2, tenantId);
                    updateUserNameAuthorizationCodePS.setString(3, userDomain);
                    updateUserNameAuthorizationCodePS.setString(4, authorizationCode);
                    updateUserNameAuthorizationCodePS.addBatch();
                } catch (UserStoreException e) {
                    log.warn("Error while migrating authorization code : " + authorizationCode);
                }
            }
            insertTokenIdPS.executeBatch();
            insertScopeAssociationPS.executeBatch();
            updateUserNamePS.executeBatch();
            insertTokenScopeHashPS.executeBatch();
            updateUserNameAuthorizationCodePS.executeBatch();

            String databaseType = DatabaseCreator.getDatabaseType(connection);

            String dropTokenScopeColumn = SQLQueries.DROP_TOKEN_SCOPE_COLUMN;
            String alterTokenIdNotNull;
            if ("oracle".equals(databaseType)){
                alterTokenIdNotNull = SQLQueries.ALTER_TOKEN_ID_NOT_NULL_ORACLE;
            } else if ("mssql".equals(databaseType)){
                alterTokenIdNotNull = SQLQueries.ALTER_TOKEN_ID_NOT_NULL_MSSQL;
            } else if ("postgresql".equals(databaseType)){
                alterTokenIdNotNull = SQLQueries.ALTER_TOKEN_ID_NOT_NULL_POSTGRESQL;
            } else {
                alterTokenIdNotNull = SQLQueries.ALTER_TOKEN_ID_NOT_NULL_MYSQL;
            }
            String setAccessTokenPrimaryKey = SQLQueries.SET_ACCESS_TOKEN_PRIMARY_KEY;
            String setScopeAssociationPrimaryKey = SQLQueries.SET_SCOPE_ASSOCIATION_PRIMARY_KEY;

            PreparedStatement dropColumnPS = connection.prepareStatement(dropTokenScopeColumn);
            dropColumnPS.execute();

            PreparedStatement notNullPS = connection.prepareStatement(alterTokenIdNotNull);
            notNullPS.execute();

            PreparedStatement primaryKeyPS = connection.prepareStatement(setAccessTokenPrimaryKey);
            primaryKeyPS.execute();

            PreparedStatement foreignKeyPS = connection.prepareStatement(setScopeAssociationPrimaryKey);
            foreignKeyPS.execute();

            connection.commit();

        } catch (SQLException e) {
            IdentityDatabaseUtil.rollBack(connection);
            log.error(e);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            IdentityDatabaseUtil.closeStatement(selectFromAccessTokenPS);
            IdentityDatabaseUtil.closeStatement(insertScopeAssociationPS);
            IdentityDatabaseUtil.closeStatement(insertTokenIdPS);
            IdentityDatabaseUtil.closeStatement(updateUserNamePS);
            IdentityDatabaseUtil.closeStatement(insertTokenScopeHashPS);
            IdentityDatabaseUtil.closeStatement(updateUserNameAuthorizationCodePS);
            IdentityDatabaseUtil.closeStatement(selectFromAuthorizationCodePS);
            IdentityDatabaseUtil.closeResultSet(accessTokenRS);
            IdentityDatabaseUtil.closeResultSet(authzCodeRS);
            IdentityDatabaseUtil.closeConnection(connection);
        }
    }





}

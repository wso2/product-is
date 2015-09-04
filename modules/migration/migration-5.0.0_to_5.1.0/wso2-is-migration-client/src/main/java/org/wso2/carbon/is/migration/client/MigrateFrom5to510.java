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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.base.ServerConfigurationException;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.core.persistence.JDBCPersistenceManager;
import org.wso2.carbon.identity.core.util.IdentityConfigParser;
import org.wso2.carbon.identity.core.util.IdentityCoreConstants;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.is.migration.ISMigrationException;
import org.wso2.carbon.is.migration.MigrationDatabaseCreator;
import org.wso2.carbon.is.migration.client.internal.ServiceHolder;
import org.wso2.carbon.is.migration.util.ResourceUtil;
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
import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.StringTokenizer;
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
        } catch (ServerConfigurationException e) {
            String errorMsg = "Error when reading the JDBC Configuration from the file.";
            log.error(errorMsg, e);
            throw new IdentityException(errorMsg, e);
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

    }

    public void oauthMigration(){
        Connection connection = null;
        PreparedStatement selectPS = null;
        PreparedStatement insertScopesPS = null;
        PreparedStatement updateTokenIdPS = null;
        PreparedStatement updateUserPS = null;
        ResultSet rs = null;
        try {
            connection = JDBCPersistenceManager.getInstance().getDBConnection();
            connection.setAutoCommit(false);

            String query = "SELECT ACCESS_TOKEN, TOKEN_SCOPE, AUTHZ_USER FROM IDN_OAUTH2_ACCESS_TOKEN";
            selectPS = connection.prepareStatement(query);

            String insertQuery = "INSERT INTO IDN_OAUTH2_SCOPE_ASSOCIATION (TOKEN_ID, TOKEN_SCOPE) VALUES (?, ?)";
            insertScopesPS = connection.prepareStatement(insertQuery);

            String updateTokenIdQuery = "UPDATE IDN_OAUTH2_ACCESS_TOKEN SET TOKEN_ID=? WHERE ACCESS_TOKEN=?";
            updateTokenIdPS = connection.prepareStatement(updateTokenIdQuery);

            String updateUserQuery = "UPDATE IDN_OAUTH2_ACCESS_TOKEN SET AUTHZ_USER=?, TENANT_ID=?, USER_DOMAIN=? WHERE ACCESS_TOKEN=?";
            updateUserPS = connection.prepareStatement(updateUserQuery);

            rs = selectPS.executeQuery();
            while (rs.next()){
                String accessToken = null;
                try {
                    accessToken = rs.getString(1);
                    String scopeString = rs.getString(2);
                    String authzUser = rs.getString(3);

                    String tokenId = UUID.randomUUID().toString();

                    String username = UserCoreUtil.removeDomainFromName(MultitenantUtils.getTenantAwareUsername
                            (authzUser));
                    String userDomain = UserCoreUtil.extractDomainFromName(authzUser);
                    int tenantId = ServiceHolder.getRealmService().getTenantManager().getTenantId(MultitenantUtils
                            .getTenantDomain(authzUser));

                    updateTokenIdPS.setString(1, tokenId);
                    updateTokenIdPS.setString(2, accessToken);
                    updateTokenIdPS.addBatch();

                    updateUserPS.setString(1, username);
                    updateUserPS.setInt(2, tenantId);
                    updateUserPS.setString(3, userDomain);
                    updateUserPS.setString(4, accessToken);
                    updateUserPS.executeBatch();

                    if (scopeString != null) {
                        String scopes[] = scopeString.split(" ");
                        for (String scope : scopes) {
                            insertScopesPS.setString(1, tokenId);
                            insertScopesPS.setString(2, scope);
                            insertScopesPS.addBatch();
                        }
                    }
                } catch (UserStoreException e) {
                    log.warn("Error while migrating access token : " + accessToken);
                }
            }
            updateTokenIdPS.executeBatch();
            insertScopesPS.executeBatch();
            connection.commit();

        } catch (IdentityException e) {
            IdentityDatabaseUtil.rollBack(connection);
            log.error("Error while retrieving the database connection" , e);
        } catch (SQLException e) {
            IdentityDatabaseUtil.rollBack(connection);
            log.error(e);
        }finally {
            IdentityDatabaseUtil.closeConnection(connection);
            IdentityDatabaseUtil.closeResultSet(rs);
            IdentityDatabaseUtil.closeStatement(selectPS);
            IdentityDatabaseUtil.closeStatement(insertScopesPS);
            IdentityDatabaseUtil.closeStatement(updateTokenIdPS);
            IdentityDatabaseUtil.closeStatement(updateUserPS);
        }
    }





}

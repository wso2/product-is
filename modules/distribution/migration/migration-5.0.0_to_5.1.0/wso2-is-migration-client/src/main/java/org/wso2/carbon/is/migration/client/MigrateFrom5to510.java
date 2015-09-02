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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.core.persistence.JDBCPersistenceManager;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.is.migration.ISMigrationException;
import org.wso2.carbon.is.migration.client.internal.ServiceHolder;
import org.wso2.carbon.is.migration.util.ResourceUtil;
import org.wso2.carbon.user.api.Tenant;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.util.UserCoreUtil;
import org.wso2.carbon.utils.dbcreator.DatabaseCreator;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

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

    public MigrateFrom5to510() throws UserStoreException {

    }

    /**
     * This method is used to migrate database tables
     * This executes the database queries according to the user's db type and alters the tables
     *
     * @param migrateVersion version to be migrated
     * @throws ISMigrationException
     * @throws SQLException
     */
    public void databaseMigration(String migrateVersion) throws ISMigrationException, SQLException {
        log.info("Database migration for API Manager 1.8.0 started");
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            connection = JDBCPersistenceManager.getInstance().getDBConnection();
            String dbType = DatabaseCreator.getDatabaseType(connection);
            String dbScript = ResourceUtil.pickQueryFromResources(migrateVersion);
            BufferedReader bufferedReader;

            InputStream is = new FileInputStream(dbScript);
            bufferedReader = new BufferedReader(new InputStreamReader(is));
            String sqlQuery;
            while ((sqlQuery = bufferedReader.readLine()) != null) {
                if ("oracle".equals(dbType)) {
                    sqlQuery = sqlQuery.replace(";", "");
                }
                sqlQuery = sqlQuery.trim();
                if (sqlQuery.startsWith("//") || sqlQuery.startsWith("--")) {
                    continue;
                }
                StringTokenizer stringTokenizer = new StringTokenizer(sqlQuery);
                if (stringTokenizer.hasMoreTokens()) {
                    String token = stringTokenizer.nextToken();
                    if ("REM".equalsIgnoreCase(token)) {
                        continue;
                    }
                }

                if (sqlQuery.contains("\\n")) {
                    sqlQuery = sqlQuery.replace("\\n", "");
                }

                if (sqlQuery.length() > 0) {
                    preparedStatement = connection.prepareStatement(sqlQuery.trim());
                    preparedStatement.execute();
                    connection.commit();
                    preparedStatement.close();
                }
            }
        oauthMigration();

        } catch (IOException e) {
            //ResourceUtil.handleException("Error occurred while finding the query. Please check the file path.", e);
            log.error("Error occurred while migrating databases", e);
        } catch (Exception e) {
            //ResourceUtil.handleException("Error occurred while finding the query. Please check the file path.", e);
            log.error("Error occurred while migrating databases", e);
        } finally {
            if (connection != null) {
                connection.close();
            }
        }
        log.info("DB resource migration done for all the tenants");
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

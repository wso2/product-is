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
package org.wso2.carbon.is.migration.service.v510.migrator;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.core.migrate.MigrationClientException;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.is.migration.internal.ISMigrationServiceDataHolder;
import org.wso2.carbon.is.migration.service.Migrator;
import org.wso2.carbon.is.migration.service.v510.SQLQueries;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.util.UserCoreUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

/**
 * Identity Data Migrator Implementation.
 */
public class IdentityDataMigrator extends Migrator {

    private static final Log log = LogFactory.getLog(IdentityDataMigrator.class);

    @Override
    public void migrate() throws MigrationClientException {
        migrateIdentityData();
    }


    /**
     * migrate data in the identity database and finalize the database table restructuring
     */
    public void migrateIdentityData() throws MigrationClientException {
        log.info("MIGRATION-LOGS >> Going to start : migrateIdentityData.");
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
            identityConnection = getDataSource().getConnection();
            identityConnection.setAutoCommit(false);

            try {
                selectConsumerAppsPS = identityConnection.prepareStatement(SQLQueries.SELECT_FROM_CONSUMER_APPS);
                updateConsumerAppsPS = identityConnection.prepareStatement(SQLQueries.UPDATE_CONSUMER_APPS);

                selectConsumerAppsRS = selectConsumerAppsPS.executeQuery();
                log.info("MIGRATION-LOGS >> Executed query : " + selectConsumerAppsPS.toString());
                boolean isConsumerAppsAvail = false;
                while (selectConsumerAppsRS.next()) {
                    int id = selectConsumerAppsRS.getInt("ID");
                    String username = selectConsumerAppsRS.getString("USERNAME");
                    String userDomainFromDB = selectConsumerAppsRS.getString("USER_DOMAIN");

                    try {
                        if (userDomainFromDB == null) {
                            String userDomain = UserCoreUtil.extractDomainFromName(username);
                            username = UserCoreUtil.removeDomainFromName(username);

                            updateConsumerAppsPS.setString(1, username);
                            updateConsumerAppsPS.setString(2, userDomain);
                            updateConsumerAppsPS.setInt(3, id);
                            if(isBatchUpdate()) {
                                isConsumerAppsAvail = true;
                                updateConsumerAppsPS.addBatch();
                            }else{
                                updateConsumerAppsPS.executeUpdate();
                                log.info("MIGRATION-LOGS >> Executed query : " + updateConsumerAppsPS.toString());
                            }
                            if (log.isDebugEnabled()) {
                                log.debug("MIGRATION-LOGS >> migrating consumer app :" + id);
                            }
                        }
                    } catch (Exception e) {
                        log.error("MIGRATION-ERROR-LOGS-011 >> Error while executing the migration.", e);
                        if (!isContinueOnError()) {
                            throw new MigrationClientException("Error while executing the migration.", e);
                        }
                    }
                }
                if(isConsumerAppsAvail && isBatchUpdate()) {
                    int[] ints = updateConsumerAppsPS.executeBatch();
                    log.info("MIGRATION-LOGS >> Executed query : " + updateConsumerAppsPS.toString());
                }
            } catch (Exception e) {
                log.error("MIGRATION-ERROR-LOGS-012 >> Error while executing the migration.", e);
                if (!isContinueOnError()) {
                    throw new MigrationClientException("Error while executing the migration.", e);
                }
            }

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

            try {
                accessTokenRS = selectFromAccessTokenPS.executeQuery();
                log.info("MIGRATION-LOGS >> Executed query : " + selectFromAccessTokenPS.toString());
                while (accessTokenRS.next()) {
                    String accessToken = null;
                    try {
                        accessToken = accessTokenRS.getString("ACCESS_TOKEN");
                        String scopeString = accessTokenRS.getString("TOKEN_SCOPE");
                        String authzUser = accessTokenRS.getString("AUTHZ_USER");
                        String tokenIdFromDB = accessTokenRS.getString("TOKEN_ID");

                        if (tokenIdFromDB == null) {
                            String tokenId = UUID.randomUUID().toString();

                            String username = UserCoreUtil.removeDomainFromName(
                                    MultitenantUtils.getTenantAwareUsername(authzUser));
                            String userDomain = UserCoreUtil.extractDomainFromName(authzUser);
                            int tenantId = ISMigrationServiceDataHolder
                                    .getRealmService().getTenantManager()
                                    .getTenantId(MultitenantUtils.getTenantDomain(authzUser));

                            try {
                                insertTokenIdPS.setString(1, tokenId);
                                insertTokenIdPS.setString(2, accessToken);

                                if(isBatchUpdate()) {
                                    insertTokenIdPS.addBatch();
                                }else{
                                    insertTokenIdPS.executeUpdate();
                                    log.info("MIGRATION-LOGS >> Executed query : " + insertTokenIdPS.toString());
                                }
                            } catch (Exception e) {
                                log.error("MIGRATION-ERROR-LOGS-013 >> Error while executing the migration.", e);
                                if (!isContinueOnError()) {
                                    throw new MigrationClientException("Error while executing the migration.", e);
                                }
                            }

                            try {
                                updateUserNamePS.setString(1, username);
                                updateUserNamePS.setInt(2, tenantId);
                                updateUserNamePS.setString(3, userDomain);
                                updateUserNamePS.setString(4, authzUser);
                                updateUserNamePS.setString(5, accessToken);
                                if(isBatchUpdate()) {
                                    updateUserNamePS.addBatch();
                                }else{
                                    updateConsumerAppsPS.executeUpdate();
                                    log.info("MIGRATION-LOGS >> Executed query : " + updateConsumerAppsPS.toString());
                                }
                            } catch (Exception e) {
                                log.error("MIGRATION-ERROR-LOGS-014 >> Error while executing the migration.", e);
                                if (!isContinueOnError()) {
                                    throw new MigrationClientException("Error while executing the migration.", e);
                                }
                            }

                            try {
                                insertTokenScopeHashPS.setString(1, DigestUtils.md5Hex(scopeString));
                                insertTokenScopeHashPS.setString(2, accessToken);
                                if(isBatchUpdate()) {
                                    insertTokenScopeHashPS.addBatch();
                                }else{
                                    insertTokenScopeHashPS.executeUpdate();
                                    log.info("MIGRATION-LOGS >> Executed query : " + insertTokenScopeHashPS.toString());
                                }
                            } catch (Exception e) {
                                log.error("MIGRATION-ERROR-LOGS-015 >> Error while executing the migration.", e);
                                if (!isContinueOnError()) {
                                    throw new MigrationClientException("Error while executing the migration.", e);
                                }
                            }

                            if (log.isDebugEnabled()) {
                                log.debug("MIGRATION-LOGS >> migrating access token : " + accessToken);
                            }

                            if (scopeString != null) {
                                String scopes[] = scopeString.split(" ");
                                for (String scope : scopes) {
                                    try {
                                        insertScopeAssociationPS.setString(1, tokenId);
                                        insertScopeAssociationPS.setString(2, scope);
                                        if(isBatchUpdate()) {
                                            insertScopeAssociationPS.addBatch();
                                        }else{
                                            insertScopeAssociationPS.executeUpdate();
                                            log.info("MIGRATION-LOGS >> Executed query : " +
                                                     insertScopeAssociationPS.toString());
                                        }
                                    } catch (Exception e) {
                                        log.error("MIGRATION-ERROR-LOGS-016 >> Error while executing the migration.",
                                                  e);
                                        if (!isContinueOnError()) {
                                            throw new MigrationClientException("Error while executing the migration.",
                                                                               e);
                                        }
                                    }
                                }
                            }
                        }
                    } catch (UserStoreException e) {
                        log.error("MIGRATION-ERROR-LOGS-017 >> Error while executing the migration.", e);
                        if (!isContinueOnError()) {
                            throw new MigrationClientException("Error while executing the migration.", e);
                        }
                    }
                }
                if(isBatchUpdate()) {
                    try {
                        insertTokenIdPS.executeBatch();
                        log.info("MIGRATION-LOGS >> Executed query : " + insertTokenIdPS.toString());
                    } catch (SQLException e) {
                        log.error("MIGRATION-ERROR-LOGS-018 >> Error while executing the migration.", e);
                        if (!isContinueOnError()) {
                            throw new MigrationClientException("Error while executing the migration.", e);
                        }
                    }

                    try {
                        log.info("MIGRATION-LOGS >> Started : " + insertScopeAssociationPS.toString());
                        insertScopeAssociationPS.executeBatch();
                        log.info("MIGRATION-LOGS >> Executed query : " + insertScopeAssociationPS.toString());
                    } catch (SQLException e) {
                        log.error("MIGRATION-ERROR-LOGS-019 >> Error while executing the migration.", e);
                        if (!isContinueOnError()) {
                            throw new MigrationClientException("Error while executing the migration.", e);
                        }
                    }
                    try {
                        updateUserNamePS.executeBatch();
                        log.info("MIGRATION-LOGS >> Executed query : " + updateUserNamePS.toString());
                    } catch (SQLException e) {
                        log.error("MIGRATION-ERROR-LOGS-020 >> Error while executing the migration.", e);
                        if (!isContinueOnError()) {
                            throw new MigrationClientException("Error while executing the migration.", e);
                        }
                    }

                    try {
                        insertTokenScopeHashPS.executeBatch();
                        log.info("MIGRATION-LOGS >> Executed query : " + insertTokenScopeHashPS.toString());
                    } catch (SQLException e) {
                        log.error("MIGRATION-ERROR-LOGS-021 >> Error while executing the migration.", e);
                        if (!isContinueOnError()) {
                            throw new MigrationClientException("Error while executing the migration.", e);
                        }
                    }
                }
            } catch (Exception e) {
                log.error("MIGRATION-ERROR-LOGS-022 >> Error while executing the migration.", e);
                if (!isContinueOnError()) {
                    throw new MigrationClientException("Error while executing the migration.", e);
                }
            }

            String selectFromAuthorizationCode = SQLQueries.SELECT_FROM_AUTHORIZATION_CODE;
            selectFromAuthorizationCodePS = identityConnection.prepareStatement(selectFromAuthorizationCode);

            String updateUserNameAuthorizationCode = SQLQueries.UPDATE_USER_NAME_AUTHORIZATION_CODE;
            updateUserNameAuthorizationCodePS = identityConnection.prepareStatement(updateUserNameAuthorizationCode);

            try {
                authzCodeRS = selectFromAuthorizationCodePS.executeQuery();
                log.info("MIGRATION-LOGS >> Executed query : " + authzCodeRS.toString());
                while (authzCodeRS.next()) {
                    String authorizationCode = null;
                    try {
                        authorizationCode = authzCodeRS.getString("AUTHORIZATION_CODE");
                        String authzUser = authzCodeRS.getString("AUTHZ_USER");
                        String userDomainFromDB = authzCodeRS.getString("USER_DOMAIN");

                        if (userDomainFromDB == null) {
                            String username = UserCoreUtil.removeDomainFromName(
                                    MultitenantUtils.getTenantAwareUsername(authzUser));
                            String userDomain = UserCoreUtil.extractDomainFromName(authzUser);
                            int tenantId =
                                    ISMigrationServiceDataHolder.getRealmService().getTenantManager().getTenantId(
                                            MultitenantUtils.getTenantDomain(authzUser));

                            try {
                                updateUserNameAuthorizationCodePS.setString(1, username);
                                updateUserNameAuthorizationCodePS.setInt(2, tenantId);
                                updateUserNameAuthorizationCodePS.setString(3, userDomain);
                                updateUserNameAuthorizationCodePS.setString(4, UUID.randomUUID().toString());
                                updateUserNameAuthorizationCodePS.setString(5, authzUser);
                                updateUserNameAuthorizationCodePS.setString(6, authorizationCode);
                                if(isBatchUpdate()) {
                                    updateUserNameAuthorizationCodePS.addBatch();
                                }else{
                                    updateUserNameAuthorizationCodePS.executeUpdate();
                                    log.info("MIGRATION-LOGS >> Executed query : "
                                             + updateUserNameAuthorizationCodePS.toString());
                                }
                            } catch (Exception e) {
                                log.error("MIGRATION-ERROR-LOGS-023 >> Error while executing the migration.", e);
                                if (!isContinueOnError()) {
                                    throw new MigrationClientException("Error while executing the migration.", e);
                                }
                            }
                            if (log.isDebugEnabled()) {
                                log.debug("MIGRATION-LOGS >> migrating authorization code : " + authorizationCode);
                            }
                        }
                    } catch (UserStoreException e) {
                        log.warn("MIGRATION-LOGS >> Error while migrating authorization code : " + authorizationCode);
                        if (!isContinueOnError()) {
                            throw new MigrationClientException("Error while executing the migration.", e);
                        }
                    }
                }
                if(isBatchUpdate()) {
                    updateUserNameAuthorizationCodePS.executeBatch();
                    log.info("MIGRATION-LOGS >> Executed query : " + updateUserNameAuthorizationCodePS.toString());
                }
            } catch (Exception e) {
                log.error("MIGRATION-ERROR-LOGS-024 >> Error while executing the migration.", e);
                if (!isContinueOnError()) {
                    throw new MigrationClientException("Error while executing the migration.", e);
                }
            }

            String selectIdnAssociatedId = SQLQueries.SELECT_IDN_ASSOCIATED_ID;
            selectIdnAssociatedIdPS = identityConnection.prepareStatement(selectIdnAssociatedId);

            try {
                selectIdnAssociatedIdRS = selectIdnAssociatedIdPS.executeQuery();

                updateIdnAssociatedIdPS = identityConnection.prepareStatement(SQLQueries.UPDATE_IDN_ASSOCIATED_ID);

                while (selectIdnAssociatedIdRS.next()) {
                    int id = selectIdnAssociatedIdRS.getInt("ID");
                    String username = selectIdnAssociatedIdRS.getString("USER_NAME");
                    String userDomainFromDB = selectIdnAssociatedIdRS.getString("DOMAIN_NAME");

                    if (userDomainFromDB == null) {
                        try {
                            updateIdnAssociatedIdPS.setString(1, UserCoreUtil.extractDomainFromName(username));
                            updateIdnAssociatedIdPS.setString(2, UserCoreUtil.removeDomainFromName(username));
                            updateIdnAssociatedIdPS.setInt(3, id);
                            if(isBatchUpdate()) {
                                updateIdnAssociatedIdPS.addBatch();
                            }else{
                                updateIdnAssociatedIdPS.executeUpdate();
                                log.info("MIGRATION-LOGS >> Executed query : " + updateIdnAssociatedIdPS.toString());
                            }
                            if (log.isDebugEnabled()) {
                                log.debug("MIGRATION-LOGS >> migrating IdnAssociatedId : " + id);
                            }
                        } catch (Exception e) {
                            log.error("MIGRATION-ERROR-LOGS-024 >> Error while executing the migration.", e);
                            if (!isContinueOnError()) {
                                throw new MigrationClientException("Error while executing the migration.", e);
                            }
                        }
                    }
                }
                if(isBatchUpdate()) {
                    updateIdnAssociatedIdPS.executeBatch();
                    log.info("MIGRATION-LOGS >> Executed query : " + updateIdnAssociatedIdPS.toString());
                }
            } catch (Exception e) {
                log.error("MIGRATION-ERROR-LOGS-025 >> Error while executing the migration.", e);
                if (!isContinueOnError()) {
                    throw new MigrationClientException("Error while executing the migration.", e);
                }
            }

            identityConnection.commit();

        } catch (SQLException e) {
            IdentityDatabaseUtil.rollBack(identityConnection);
            log.error("MIGRATION-ERROR-LOGS--026 >> Error while executing the migration.", e);
            if (!isContinueOnError()) {
                throw new MigrationClientException("Error while executing the migration.", e);
            }
        } catch (Exception e) {
            log.error("MIGRATION-ERROR-LOGS-027 >> Error while executing the migration.", e);
            if (!isContinueOnError()) {
                throw new MigrationClientException("Error while executing the migration.", e);
            }
        } finally {
            try {
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
            } catch (Exception e) {
                log.error("MIGRATION-ERROR-LOGS-028 >> Error while executing the migration.", e);
            }
        }
        log.info("MIGRATION-LOGS >> Done : migrateIdentityData.");
    }
}
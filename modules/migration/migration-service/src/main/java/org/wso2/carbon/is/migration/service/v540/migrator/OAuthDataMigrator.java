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

package org.wso2.carbon.is.migration.service.v540.migrator;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.core.migrate.MigrationClientException;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.is.migration.service.Migrator;
import org.wso2.carbon.is.migration.service.v540.bean.OAuth2Scope;
import org.wso2.carbon.is.migration.service.v540.bean.OAuth2ScopeBinding;
import org.wso2.carbon.is.migration.service.v540.bean.OAuthConsumerApp;
import org.wso2.carbon.is.migration.service.v540.bean.SpOAuth2ExpiryTimeConfiguration;
import org.wso2.carbon.is.migration.service.v540.dao.OAuthDAO;
import org.wso2.carbon.is.migration.service.v540.util.RegistryUtil;
import org.wso2.carbon.is.migration.util.Constant;
import org.wso2.carbon.is.migration.util.Utility;
import org.wso2.carbon.user.api.Tenant;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This class handles the OAuth data migration.
 */
public class OAuthDataMigrator extends Migrator {

    private static final Log log = LogFactory.getLog(OAuthDataMigrator.class);

    @Override
    public void migrate() throws MigrationClientException {

        try {
            migrateOAuthConsumerAppData();
        } catch (Exception e) {
            String message = "Error occurred while migrating OAuth consumer apps.";
            if (isContinueOnError()) {
                log.error(message, e);
            } else {
                throw new MigrationClientException(message, e);
            }
        }

        try {
            migrateOAuth2ScopeData();
        } catch (Exception e) {
            String message = "Error occurred while migrating OAuth2 scope data.";
            if (isContinueOnError()) {
                log.error(message, e);
            } else {
                throw new MigrationClientException(message, e);
            }
        }
    }

    /**
     * Migrate OAuth consumer apps.
     *
     * @throws MigrationClientException MigrationClientException
     * @throws SQLException SQLException
     */
    private void migrateOAuthConsumerAppData() throws MigrationClientException, SQLException {

        log.info(Constant.MIGRATION_LOG + "Migration starting on OAuth2 consumer apps table.");

        List<OAuthConsumerApp> consumerApps;
        try (Connection connection = getDataSource().getConnection()) {
            consumerApps = OAuthDAO.getInstance().getAllOAuthConsumerApps(connection);
        }

        if (consumerApps.isEmpty()) {
            log.info(Constant.MIGRATION_LOG + "No data to migrate in OAuth2 consumer apps table.");
            return;
        }

        List<OAuthConsumerApp> updatedConsumerApps = new ArrayList<>();

        long applicationAccessTokenExpiryTime = 3600;
        if (StringUtils.isNotBlank(IdentityUtil.getProperty("OAuth.AccessTokenDefaultValidityPeriod"))) {
            applicationAccessTokenExpiryTime = Long.parseLong(IdentityUtil.getProperty("OAuth" +
                    ".AccessTokenDefaultValidityPeriod"));
        }

        long userAccessTokenExpiryTime = 3600;
        if (StringUtils.isNotBlank(IdentityUtil.getProperty("OAuth.UserAccessTokenDefaultValidityPeriod"))) {
            userAccessTokenExpiryTime = Long.parseLong(IdentityUtil.getProperty("OAuth" +
                    ".UserAccessTokenDefaultValidityPeriod"));
        }

        long refreshTokenExpiryTime = 84600;
        if (StringUtils.isNotBlank(IdentityUtil.getProperty("OAuth.RefreshTokenValidityPeriod"))) {
            refreshTokenExpiryTime = Long.parseLong(IdentityUtil.getProperty("OAuth" +
                    ".RefreshTokenValidityPeriod"));
        }

        boolean ignoreForInactiveTenants = isIgnoreForInactiveTenants();
        List<Integer> inactiveTenants = Utility.getInactiveTenants();
        Set<Integer> tenantRangeID = new HashSet<>();
        if (Utility.isMigrateTenantRange()) {
            for (Tenant tenant : Utility.getTenants()) {
                tenantRangeID.add(tenant.getId());
            }
        }
        for (OAuthConsumerApp consumerApp : consumerApps) {
            if (ignoreForInactiveTenants && inactiveTenants.contains(consumerApp.getTenantId())) {
                log.info("Skipping OAuth2 consumer apps table migration for inactive tenant: " +
                        consumerApp.getTenantId());
                continue;
            }
            if (Utility.isMigrateTenantRange() && !tenantRangeID.contains(consumerApp.getTenantId())) {
                log.info("Skipping OAuth2 consumer apps table migration for tenant : " + consumerApp.getTenantId());
                continue;
            }
            SpOAuth2ExpiryTimeConfiguration expiryTimeConfiguration = RegistryUtil.getSpTokenExpiryTimeConfig
                    (consumerApp.getConsumerKey(), consumerApp.getTenantId());

            if (expiryTimeConfiguration.getApplicationAccessTokenExpiryTime() != null) {
                consumerApp.setApplicationAccessTokenExpiryTime(expiryTimeConfiguration
                        .getApplicationAccessTokenExpiryTime() / 1000);
            } else {
                consumerApp.setApplicationAccessTokenExpiryTime(applicationAccessTokenExpiryTime);
            }
            if (expiryTimeConfiguration.getUserAccessTokenExpiryTime() != null) {
                consumerApp.setUserAccessTokenExpiryTime(expiryTimeConfiguration.getApplicationAccessTokenExpiryTime
                        () / 1000);
            } else {
                consumerApp.setUserAccessTokenExpiryTime(userAccessTokenExpiryTime);
            }
            if (expiryTimeConfiguration.getRefreshTokenExpiryTime() != null) {
                consumerApp.setRefreshTokenExpiryTime(expiryTimeConfiguration.getApplicationAccessTokenExpiryTime()
                        / 1000);
            } else {
                consumerApp.setRefreshTokenExpiryTime(refreshTokenExpiryTime);
            }

            if (consumerApp.getApplicationAccessTokenExpiryTime() != 3600
                    || consumerApp.getUserAccessTokenExpiryTime() != 3600
                    || consumerApp.getRefreshTokenExpiryTime() != 84600) {
                updatedConsumerApps.add(consumerApp);
            }
        }

        if (!updatedConsumerApps.isEmpty()) {
            try (Connection connection = getDataSource().getConnection()) {
                OAuthDAO.getInstance().updateExpiryTimesDefinedForOAuthConsumerApps(connection,
                        updatedConsumerApps);
            }
        }
        log.info(Constant.MIGRATION_LOG + "Migration succeeded for OAuth2 consumer apps table.");
    }

    /**
     * Migrate OAuth2 scope data.
     *
     * @throws MigrationClientException MigrationClientException
     * @throws SQLException SQLException
     */
    private void migrateOAuth2ScopeData() throws MigrationClientException, SQLException {

        log.info(Constant.MIGRATION_LOG + "Migration starting on OAuth2 Scope table.");

        List<OAuth2Scope> oAuth2Scopes;
        try (Connection connection = getDataSource().getConnection()) {
            oAuth2Scopes = OAuthDAO.getInstance().getAllOAuth2Scopes(connection);
        }

        if (oAuth2Scopes.isEmpty()) {
            log.info(Constant.MIGRATION_LOG + "No data to migrate in OAuth2 Scope table.");
            return;
        }

        List<OAuth2ScopeBinding> oAuth2ScopeBindings = new ArrayList<>();
        List<OAuth2Scope> updatedAuth2Scopes = new ArrayList<>();

        for (OAuth2Scope oAuth2Scope : oAuth2Scopes) {
            if (StringUtils.isNotBlank(oAuth2Scope.getRoles())) {
                String[] roles = oAuth2Scope.getRoles().split(",");
                for (String role : roles) {
                    oAuth2ScopeBindings.add(new OAuth2ScopeBinding(oAuth2Scope.getScopeId(), role));
                }
            }
            if (StringUtils.isBlank(oAuth2Scope.getName())) {
                oAuth2Scope.setName(oAuth2Scope.getScopeKey());
                updatedAuth2Scopes.add(oAuth2Scope);
            }
        }

        if (!oAuth2ScopeBindings.isEmpty()) {
            try (Connection connection = getDataSource().getConnection()) {
                OAuthDAO.getInstance().addOAuth2ScopeBindings(connection, oAuth2ScopeBindings);
            }
        }

        if (!updatedAuth2Scopes.isEmpty()) {
            try (Connection connection = getDataSource().getConnection()) {
                OAuthDAO.getInstance().updateOAuth2Scopes(connection, updatedAuth2Scopes);
            }
        }

        log.info(Constant.MIGRATION_LOG + "Migration succeeded on OAuth2 Scope table.");
    }
}

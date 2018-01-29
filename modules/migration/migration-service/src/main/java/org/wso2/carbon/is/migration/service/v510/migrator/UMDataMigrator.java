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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.mgt.ApplicationConstants;
import org.wso2.carbon.identity.core.migrate.MigrationClientException;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.is.migration.service.Migrator;
import org.wso2.carbon.is.migration.service.v510.SQLQueries;
import org.wso2.carbon.is.migration.util.Schema;
import org.wso2.carbon.user.core.UserCoreConstants;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * UM Data Migrator Implementation.
 */
public class UMDataMigrator extends Migrator {

    private static final Log log = LogFactory.getLog(UMDataMigrator.class);

    @Override
    public void migrate() throws MigrationClientException {
        migrateUMData();
    }

    public void migrateUMData() throws MigrationClientException {
        log.info("MIGRATION-LOGS >> Going to start : migrateUMData.");
        Connection identityConnection = null;
        Connection umConnection = null;

        PreparedStatement selectServiceProviders = null;
        PreparedStatement updateRole = null;

        ResultSet selectServiceProvidersRS = null;

        try {
            identityConnection = getDataSource(Schema.IDENTITY.getName()).getConnection();
            umConnection = getDataSource().getConnection();

            identityConnection.setAutoCommit(false);
            umConnection.setAutoCommit(false);

            selectServiceProviders = identityConnection.prepareStatement(SQLQueries.LOAD_APP_NAMES);
            selectServiceProvidersRS = selectServiceProviders.executeQuery();

            updateRole = umConnection.prepareStatement(SQLQueries.UPDATE_ROLES);
            while (selectServiceProvidersRS.next()) {
                try {
                    String appName = selectServiceProvidersRS.getString("APP_NAME");
                    int tenantId = selectServiceProvidersRS.getInt("TENANT_ID");
                    updateRole.setString(1, ApplicationConstants.APPLICATION_DOMAIN +
                                            UserCoreConstants.DOMAIN_SEPARATOR + appName);
                    updateRole.setString(2, appName);
                    updateRole.setInt(3, tenantId);
                    if(isBatchUpdate()) {
                        updateRole.addBatch();
                    }else{
                        updateRole.executeUpdate();
                        log.info("MIGRATION-LOGS >> Executed query : " + updateRole.toString());
                    }
                } catch (Exception e) {
                    log.error("MIGRATION-ERROR-LOGS-037 >> Error while executing the migration.", e);
                    if (!isContinueOnError()) {
                        throw new MigrationClientException("Error while executing the migration.", e);
                    }
                }
            }
            if(isBatchUpdate()){
                updateRole.executeBatch();
                log.info("MIGRATION-LOGS >> Executed query : " + updateRole.toString());
            }
            identityConnection.commit();
            umConnection.commit();
        } catch (SQLException e) {
            log.error("MIGRATION-ERROR-LOGS-038 >> Error while executing the migration.", e);
            if (!isContinueOnError()) {
                throw new MigrationClientException("Error while executing the migration.", e);
            }
        } finally {
            try {
                IdentityDatabaseUtil.closeResultSet(selectServiceProvidersRS);
                IdentityDatabaseUtil.closeStatement(selectServiceProviders);
                IdentityDatabaseUtil.closeStatement(updateRole);
                IdentityDatabaseUtil.closeConnection(identityConnection);
                IdentityDatabaseUtil.closeConnection(umConnection);
            } catch (Exception e) {

            }
        }
        log.info("MIGRATION-LOGS >> Done : migrateUMData.");
    }
}

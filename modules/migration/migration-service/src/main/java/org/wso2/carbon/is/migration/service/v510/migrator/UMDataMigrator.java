package org.wso2.carbon.is.migration.service.v510.migrator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.mgt.ApplicationConstants;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.is.migration.MigrationClientException;
import org.wso2.carbon.is.migration.service.Migrator;
import org.wso2.carbon.is.migration.service.v510.SQLQueries;
import org.wso2.carbon.is.migration.util.Schema;
import org.wso2.carbon.user.core.UserCoreConstants;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


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
                    updateRole.setString(1, ApplicationConstants.APPLICATION_DOMAIN + UserCoreConstants.DOMAIN_SEPARATOR
                                            + appName);
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
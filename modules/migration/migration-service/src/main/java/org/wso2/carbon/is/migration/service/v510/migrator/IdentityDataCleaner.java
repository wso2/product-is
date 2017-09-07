package org.wso2.carbon.is.migration.service.v510.migrator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.is.migration.MigrationClientException;
import org.wso2.carbon.is.migration.service.Migrator;
import org.wso2.carbon.is.migration.service.v510.SQLQueries;
import org.wso2.carbon.user.core.util.UserCoreUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class IdentityDataCleaner extends Migrator {
    private static final Log log = LogFactory.getLog(IdentityDataCleaner.class);
    @Override
    public void migrate() throws MigrationClientException {
        cleanIdentityData();
    }

    public void cleanIdentityData() throws MigrationClientException {
        log.info("MIGRATION-LOGS >> Going to start : cleanIdentityData.");
        Connection identityConnection = null;
        PreparedStatement selectFromAccessTokenPS = null;
        PreparedStatement selectDuplicateUserNamePS = null;
        PreparedStatement deleteDuplicateUserNamePS = null;
        PreparedStatement deleteDeleteRow = null;

        ResultSet accessTokenRS = null;
        ResultSet duplicateUserNameRS = null;

        try {
            identityConnection = getDataSource().getConnection();
            identityConnection.setAutoCommit(false);

            try {
                deleteDeleteRow = identityConnection.prepareStatement(SQLQueries.DELETE_USERNAME_WITH_SPACE);
                int rowCount = deleteDeleteRow.executeUpdate();
                log.info("MIGRATION-LOGS >> Deleting " + rowCount + " rows  having spaces in user name in IDN_OAUTH2_ACCESS_TOKEN");
                log.info("MIGRATION-LOGS >> Executed query : " + deleteDeleteRow.toString());
            } catch (Exception e) {
                log.error("MIGRATION-ERROR-LOGS-009 >> Error while executing the migration.", e);
                if (!isContinueOnError()) {
                    throw new MigrationClientException("Error while executing the migration.", e);
                }
            }

            try {
                selectFromAccessTokenPS = identityConnection.prepareStatement(SQLQueries
                                                                                      .SELECT_USERNAME_WITHOUT_TENANT);

                accessTokenRS = selectFromAccessTokenPS.executeQuery();
                log.info("MIGRATION-LOGS >> Executed query : " + selectFromAccessTokenPS.toString());

                selectDuplicateUserNamePS = identityConnection.prepareStatement(SQLQueries
                                                                                        .SELECT_INVALID_USERNAME_WITHOUT_TENANT);

                deleteDuplicateUserNamePS = identityConnection.prepareStatement(SQLQueries
                                                                                        .DELETE_CORRUPTED_ACCESS_TOKEN_DATA);
                while (accessTokenRS.next()) {
                    selectDuplicateUserNamePS.setString(1, accessTokenRS.getString("CONSUMER_KEY"));
                    selectDuplicateUserNamePS.setString(2, UserCoreUtil.addTenantDomainToEntry(accessTokenRS.getString
                            ("AUTHZ_USER"), MultitenantConstants.SUPER_TENANT_DOMAIN_NAME));
                    selectDuplicateUserNamePS.setString(3, accessTokenRS.getString("USER_TYPE"));
                    selectDuplicateUserNamePS.setString(4, accessTokenRS.getString("TOKEN_SCOPE"));

                    duplicateUserNameRS = selectDuplicateUserNamePS.executeQuery();
                    if (duplicateUserNameRS.next()) {
                        log.info("MIGRATION-LOGS >> deleting corrupted row : ACCESS_TOKEN-" + accessTokenRS.getString("ACCESS_TOKEN"));
                        deleteDuplicateUserNamePS.setString(1, accessTokenRS.getString("ACCESS_TOKEN"));
                        log.info(deleteDuplicateUserNamePS.toString());
                        log.info(deleteDuplicateUserNamePS);
                        int i = deleteDuplicateUserNamePS.executeUpdate();
                        log.info("MIGRATION-LOGS >> Deleting " + i);
                        log.info("MIGRATION-LOGS >> Executed query : " + deleteDuplicateUserNamePS.toString());
                    }
                }
            } catch (Exception e) {
                log.error("MIGRATION-ERROR-LOGS-010 >> Error while executing the migration.", e);
                if (!isContinueOnError()) {
                    throw new MigrationClientException("Error while executing the migration.", e);
                }
            }
            identityConnection.commit();
        } catch (SQLException e) {
            log.warn("MIGRATION-LOGS >> Error while cleaning identity data ", e);
            if (!isContinueOnError()) {
                throw new MigrationClientException("Error while executing the migration.", e);
            }
        } finally {
            try {
                IdentityDatabaseUtil.closeResultSet(accessTokenRS);
                IdentityDatabaseUtil.closeResultSet(duplicateUserNameRS);
                IdentityDatabaseUtil.closeStatement(selectFromAccessTokenPS);
                IdentityDatabaseUtil.closeStatement(selectDuplicateUserNamePS);
                IdentityDatabaseUtil.closeStatement(deleteDuplicateUserNamePS);
                IdentityDatabaseUtil.closeConnection(identityConnection);
            } catch (Exception e) {
            }
        }
        log.info("MIGRATION-LOGS >> Done : cleanIdentityData.");
    }
}

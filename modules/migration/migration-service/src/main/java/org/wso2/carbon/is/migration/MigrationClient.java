package org.wso2.carbon.is.migration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.is.migration.config.Config;
import org.wso2.carbon.is.migration.util.Constant;

import java.util.List;

/**
 * MigrationClient is the one that trigger by the relevant component trigger to start the migration service.
 *
 */
public class MigrationClient {

    private static final Log log = LogFactory.getLog(MigrationClient.class);

    public void execute() throws MigrationClientException {

        try {
            log.info("............................................................................................");
            log.info("............................................................................................");
            log.info("............................................................................................");
            log.info("............................................................................................");
            log.info("............................................................................................");
            log.info("............................................................................................");
            log.info("............................................................................................");
            log.info("............................................................................................");
            log.info("............................................................................................");
            log.info("............................................................................................");
            log.info("............................................................................................");
            log.info("............................................................................................");


            Config config = Config.getInstance();

            VersionMigrationHolder versionMigrationHolder = VersionMigrationHolder.getInstance();
            List<VersionMigration> versionMigrationList
                    = versionMigrationHolder.getVersionMigrationList();

            log.info("Migration Versions List.........................");
            for (VersionMigration versionMigration : versionMigrationList) {
                log.info(versionMigration.getPreviousVersion() + " to " + versionMigration.getCurrentVersion());
            }

            boolean isMigrationStarted = false ;

            for(VersionMigration versionMigration : versionMigrationList){
                log.info(Constant.MIGRATION_LOG + "Start Version : " + versionMigration.getPreviousVersion() + " to " + versionMigration.getCurrentVersion());
                if(!isMigrationStarted && versionMigration.getPreviousVersion().equals(config.getCurrentVersion())){
                    versionMigration.migrate();
                    isMigrationStarted = true ;
                    continue;
                }

                if(isMigrationStarted){
                    versionMigration.migrate();
                    if(versionMigration.getCurrentVersion().equals(config.getMigrateVersion())){
                        break;
                    }
                }
            }
            log.info(Constant.MIGRATION_LOG + "Execution was done through all the requested version list without "
                     + "having unexpected issues. There may be some steps that is not executed correctly but bypass "
                     + "that because of enabling 'continueOnError' property. Please see the above logs to more "
                     + "details.");
        } catch (Throwable e) {
            log.error("Migration process was stopped.", e);
        }

        log.info("............................................................................................");
        log.info("............................................................................................");
        log.info("............................................................................................");
        log.info("............................................................................................");
        log.info("............................................................................................");
        log.info("............................................................................................");
        log.info("............................................................................................");
        log.info("............................................................................................");
        log.info("............................................................................................");


    }

}

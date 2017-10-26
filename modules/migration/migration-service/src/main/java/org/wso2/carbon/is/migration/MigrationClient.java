package org.wso2.carbon.is.migration;

import org.wso2.carbon.is.migration.config.Config;

import java.util.List;

public class MigrationClient {

    public void execute() throws MigrationClientException {

        Config config = Config.getInstance();

        VersionMigrationHolder versionMigrationHolder = VersionMigrationHolder.getInstance();
        List<VersionMigration> versionMigrationList
                = versionMigrationHolder.getVersionMigrationList();

        boolean isMigrationStarted = false ;

        for(VersionMigration versionMigration : versionMigrationList){

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
    }

}

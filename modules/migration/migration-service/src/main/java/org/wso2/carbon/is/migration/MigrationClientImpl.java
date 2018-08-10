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
package org.wso2.carbon.is.migration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.core.migrate.MigrationClient;
import org.wso2.carbon.identity.core.migrate.MigrationClientException;
import org.wso2.carbon.is.migration.config.Config;
import org.wso2.carbon.is.migration.util.Constant;
import org.wso2.carbon.is.migration.util.Utility;

import java.util.List;

/**
 * MigrationClientImpl is the one that trigger by the relevant component trigger to start the migration service.
 *
 */
public class MigrationClientImpl implements MigrationClient {

    private static final Log log = LogFactory.getLog(MigrationClientImpl.class);

    @Override
    public void execute() throws MigrationClientException {

        try {

            Config config = Config.getInstance();

            if(!config.isMigrationEnable()){
                return;
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
            log.info("............................................................................................");
            log.info("............................................................................................");
            log.info("............................................................................................");

            VersionMigrationHolder versionMigrationHolder = VersionMigrationHolder.getInstance();
            List<VersionMigration> versionMigrationList
                    = versionMigrationHolder.getVersionMigrationList();

            log.info("Migration Versions List.........................");
            for (VersionMigration versionMigration : versionMigrationList) {
                log.info(versionMigration.getPreviousVersion() + " to " + versionMigration.getCurrentVersion());
            }

            boolean isMigrationStarted = false ;
            if (Utility.isMigrateTenantRange()) {
                log.info("Migration started for the tenant range " + Utility.getMigrationStartingTenantID() + " - "
                        + Utility.getMigrationEndingTenantID());
            }

            for(VersionMigration versionMigration : versionMigrationList){
                log.info(Constant.MIGRATION_LOG + "Start Version : " + versionMigration.getPreviousVersion() + " to "
                         + versionMigration.getCurrentVersion());
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

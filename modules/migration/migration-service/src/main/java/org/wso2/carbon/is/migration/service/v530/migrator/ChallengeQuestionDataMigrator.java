package org.wso2.carbon.is.migration.service.v530.migrator;

import org.wso2.carbon.is.migration.MigrationClientException;
import org.wso2.carbon.is.migration.service.Migrator;
import org.wso2.carbon.is.migration.service.v530.RegistryDataManager;


public class ChallengeQuestionDataMigrator extends Migrator {

    @Override
    public void migrate() throws MigrationClientException {
        migrateChallengeQuestionData();
    }

    public void migrateChallengeQuestionData()  {

        RegistryDataManager registryDataManager = RegistryDataManager.getInstance();
        try {
            registryDataManager.migrateChallengeQuestions(isIgnoreForInactiveTenants());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

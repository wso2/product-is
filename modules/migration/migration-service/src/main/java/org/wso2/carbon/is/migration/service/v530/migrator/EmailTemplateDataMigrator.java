package org.wso2.carbon.is.migration.service.v530.migrator;

import org.wso2.carbon.is.migration.MigrationClientException;
import org.wso2.carbon.is.migration.service.Migrator;
import org.wso2.carbon.is.migration.service.v530.RegistryDataManager;

public class EmailTemplateDataMigrator extends Migrator{


    @Override
    public void migrate() throws MigrationClientException {
        migrateEmailTemplateData();
    }



    public void migrateEmailTemplateData()  {
        RegistryDataManager registryDataManager = RegistryDataManager.getInstance();
        try {
            registryDataManager.migrateEmailTemplates(isIgnoreForInactiveTenants());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

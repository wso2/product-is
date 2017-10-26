package org.wso2.carbon.is.migration.service.v530.migrator;

import org.wso2.carbon.is.migration.MigrationClientException;
import org.wso2.carbon.is.migration.service.Migrator;
import org.wso2.carbon.is.migration.service.v530.ResidentIdpMetadataManager;

public class ResidentIdpMetadataMigrator extends Migrator {


    @Override
    public void migrate() throws MigrationClientException {
        migrateResidentIdpMetadata();
    }
    public void migrateResidentIdpMetadata()   {

        try {
            new ResidentIdpMetadataManager().migrateResidentIdpMetaData(isIgnoreForInactiveTenants());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
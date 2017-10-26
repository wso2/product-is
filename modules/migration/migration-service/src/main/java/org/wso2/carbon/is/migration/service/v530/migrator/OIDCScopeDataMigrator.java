package org.wso2.carbon.is.migration.service.v530.migrator;

import org.wso2.carbon.is.migration.MigrationClientException;
import org.wso2.carbon.is.migration.service.Migrator;
import org.wso2.carbon.is.migration.service.v530.RegistryDataManager;

public class OIDCScopeDataMigrator extends Migrator {
    @Override
    public void migrate() throws MigrationClientException {
        copyOIDCScopeData();
    }

    public void copyOIDCScopeData() {

        try {
            RegistryDataManager.getInstance().copyOIDCScopeData(isIgnoreForInactiveTenants());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
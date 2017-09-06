package org.wso2.carbon.is.migration.service.v520;

import org.wso2.carbon.is.migration.MigrationClientException;
import org.wso2.carbon.is.migration.VersionMigration;
import org.wso2.carbon.is.migration.config.Version;
import org.wso2.carbon.is.migration.service.Migrator;

public class V520Migration extends VersionMigration {
    @Override
    public String getPreviousVersion() {
        return "5.1.0";
    }

    @Override
    public String getCurrentVersion() {
        return "5.2.0";
    }
}

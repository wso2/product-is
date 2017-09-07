package org.wso2.carbon.is.migration.service.v510;

import org.wso2.carbon.is.migration.MigrationClientException;
import org.wso2.carbon.is.migration.VersionMigration;
import org.wso2.carbon.is.migration.config.Version;
import org.wso2.carbon.is.migration.service.Migrator;

public class V510Migration extends VersionMigration {
    @Override
    public String getPreviousVersion() {
        return "5.0.0-SP1";
    }

    @Override
    public String getCurrentVersion() {
        return "5.1.0";
    }

}

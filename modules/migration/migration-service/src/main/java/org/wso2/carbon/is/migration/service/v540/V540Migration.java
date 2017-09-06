package org.wso2.carbon.is.migration.service.v540;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.is.migration.MigrationClientException;
import org.wso2.carbon.is.migration.config.Config;
import org.wso2.carbon.is.migration.config.Version;
import org.wso2.carbon.is.migration.VersionMigration;
import org.wso2.carbon.is.migration.service.Migrator;

public class V540Migration extends VersionMigration {

    @Override
    public String getPreviousVersion() {
        return "5.3.0";
    }

    @Override
    public String getCurrentVersion() {
        return "5.4.0";
    }

}

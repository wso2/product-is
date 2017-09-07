package org.wso2.carbon.is.migration;

import org.wso2.carbon.is.migration.service.v500SP1.V500SP1Migration;
import org.wso2.carbon.is.migration.service.v510.V510Migration;
import org.wso2.carbon.is.migration.service.v520.V520Migration;
import org.wso2.carbon.is.migration.service.v530.V530Migration;
import org.wso2.carbon.is.migration.service.v540.V540Migration;

import java.util.ArrayList;
import java.util.List;

public class VersionMigrationHolder {

    private static VersionMigrationHolder versionMigrationHolder = new VersionMigrationHolder();
    private List<VersionMigration> versionMigrationList = new ArrayList<>();


    private VersionMigrationHolder(){
        versionMigrationList.add(new V500SP1Migration());
        versionMigrationList.add(new V510Migration());
        versionMigrationList.add(new V520Migration());
        versionMigrationList.add(new V530Migration());
        versionMigrationList.add(new V540Migration());
    }

    public static VersionMigrationHolder getInstance(){
        return VersionMigrationHolder.versionMigrationHolder;
    }

    public List<VersionMigration> getVersionMigrationList() {
        return versionMigrationList;
    }
}

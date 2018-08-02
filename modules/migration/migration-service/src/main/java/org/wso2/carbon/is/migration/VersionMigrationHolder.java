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

import org.wso2.carbon.is.migration.service.v500SP1.V500SP1Migration;
import org.wso2.carbon.is.migration.service.v510.V510Migration;
import org.wso2.carbon.is.migration.service.v520.V520Migration;
import org.wso2.carbon.is.migration.service.v530.V530Migration;
import org.wso2.carbon.is.migration.service.v540.V540Migration;
import org.wso2.carbon.is.migration.service.v550.V550Migration;
import org.wso2.carbon.is.migration.service.v560.V560Migration;
import org.wso2.carbon.is.migration.service.v570.V570Migration;

import java.util.ArrayList;
import java.util.List;

/**
 * Holder class to hold version migrator objects.
 */
public class VersionMigrationHolder {

    private static VersionMigrationHolder versionMigrationHolder = new VersionMigrationHolder();
    private List<VersionMigration> versionMigrationList = new ArrayList<>();


    private VersionMigrationHolder(){
        versionMigrationList.add(new V500SP1Migration());
        versionMigrationList.add(new V510Migration());
        versionMigrationList.add(new V520Migration());
        versionMigrationList.add(new V530Migration());
        versionMigrationList.add(new V540Migration());
        versionMigrationList.add(new V550Migration());
        versionMigrationList.add(new V560Migration());
        versionMigrationList.add(new V570Migration());
    }

    public static VersionMigrationHolder getInstance(){
        return VersionMigrationHolder.versionMigrationHolder;
    }

    public List<VersionMigration> getVersionMigrationList() {
        return versionMigrationList;
    }
}

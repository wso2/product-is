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
package org.wso2.carbon.is.migration.service.v500SP1;

import org.wso2.carbon.is.migration.VersionMigration;

/**
 * Migration implementation from 5.0.0 to 5.0.0SP1
 */
public class V500SP1Migration extends VersionMigration {

    @Override
    public String getPreviousVersion() {
        return "5.0.0";
    }

    @Override
    public String getCurrentVersion() {
        return "5.0.0-SP1";
    }

}

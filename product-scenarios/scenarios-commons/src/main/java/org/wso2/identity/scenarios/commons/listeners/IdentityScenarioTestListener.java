/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.identity.scenarios.commons.listeners;

import org.testng.IExecutionListener;
import org.wso2.identity.scenarios.commons.data.DeploymentDataHolder;

import static org.wso2.identity.scenarios.commons.util.DeploymentUtil.getDeploymentProperties;
import static org.wso2.identity.scenarios.commons.util.DeploymentUtil.setKeyStoreProperties;

/**
 * TestNg Listener to initialize system properties and deployment properties required by the test execution.
 */
public class IdentityScenarioTestListener implements IExecutionListener{

    /**
     * Read Keystore configurations and populate to JVM
     * Read TestGrid deployment properties and populate to a singleton data holder.
     */
    @Override
    public void onExecutionStart() {

        setKeyStoreProperties();
        DeploymentDataHolder.getInstance().setProperties(getDeploymentProperties());
    }

    @Override
    public void onExecutionFinish() {

    }
}

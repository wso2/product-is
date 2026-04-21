/*
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.identity.integration.test.application.mgt;

import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * Initializer for the is-tests-saas-app-creation test block.
 * Enables [saas] enable_app_creation = true before all tests in the block and restores the
 * original deployment.toml after, followed by a graceful server restart.
 */
public class SaasAppCreationInitializerTestCase extends ISIntegrationTest {

    private ServerConfigurationManager serverConfigurationManager;
    private File defaultConfigFile;

    @BeforeTest(alwaysRun = true)
    public void enableSaasAppCreation() throws Exception {

        super.init();
        serverConfigurationManager = new ServerConfigurationManager(isServer);
        defaultConfigFile = getDeploymentTomlFile(CarbonUtils.getCarbonHome());

        File fragmentFile = new File(getISResourceLocation() + File.separator
                + "application" + File.separator + "mgt" + File.separator
                + "saas_app_creation_enabled_fragment.toml");

        File mergedConfigFile = createMergedConfigFile(defaultConfigFile, fragmentFile);
        serverConfigurationManager.applyConfigurationWithoutRestart(mergedConfigFile, defaultConfigFile, true);
        serverConfigurationManager.restartGracefully();
    }

    @AfterTest(alwaysRun = true)
    public void disableSaasAppCreation() throws Exception {

        if (serverConfigurationManager == null) {
            return;
        }
        super.init();
        serverConfigurationManager.restoreToLastConfiguration(false);
        serverConfigurationManager.restartGracefully();
    }

    private File createMergedConfigFile(File baseConfig, File fragmentFile) throws Exception {

        String baseContent = new String(Files.readAllBytes(baseConfig.toPath()), StandardCharsets.UTF_8);
        String fragmentContent = new String(Files.readAllBytes(fragmentFile.toPath()), StandardCharsets.UTF_8);

        String merged = baseContent + "\n" + fragmentContent.trim() + "\n";
        File mergedFile = File.createTempFile("deployment-saas", ".toml");
        mergedFile.deleteOnExit();
        Files.write(mergedFile.toPath(), merged.getBytes(StandardCharsets.UTF_8));

        return mergedFile;
    }
}

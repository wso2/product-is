/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.identity.cypress.integration.identity.apps.test;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.identity.apps.test.container.CypressTestConstants;
import org.wso2.identity.apps.test.container.CypressTestContainer;
import org.wso2.identity.apps.test.container.exception.CypressContainerException;
import org.wso2.identity.integration.common.utils.ISIntegrationUITest;

import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Identity apps cypress integration test runner class
 */
public class CypressTestRunnerTestCase extends ISIntegrationUITest {

    private CypressTestContainer cypressTestContainer = CypressTestContainer.getInstance();

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.init();
    }

    @Test(description = "Execute identity apps cypress test suite.")
    public void runCyPressTests() throws IOException, URISyntaxException, XPathExpressionException,
            CypressContainerException {

        Path path = Paths
                .get(new File(getClass().getProtectionDomain().getCodeSource().getLocation().toURI()).getPath());
        String basePath = "tests/";

        URL backEndUrl = new URL(super.getBackendURL());
        String serverUrl = backEndUrl.getProtocol() + "://" + backEndUrl.getHost() + ":" + backEndUrl.getPort() + "/";

        Path scriptPath = path.resolve(basePath + CypressTestConstants.TEST_SUITE_RUNNER_FILE);
        Path envConfigPath = path.resolve(basePath + CypressTestConstants.CYPRESS_ENV_CONFIG_FILE);
        Path reportsPath = path.resolve(basePath + CypressTestConstants.MOCHA_RESULTS_DIR);

        cypressTestContainer.addOrOverwriteTestConfigProperty(envConfigPath,
                CypressTestConstants.EnvironmentConfigElements.SERVER_URL, serverUrl);

        try {
            cypressTestContainer.runTestSuite(scriptPath);
        } catch (InterruptedException e) {
            throw new CypressContainerException("An error occurred while running the Cypress test suite. " +
                    e.getMessage());
        }

        cypressTestContainer.endTestSuite(reportsPath);
    }
}

/*
 *Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *WSO2 Inc. licenses this file to you under the Apache License,
 *Version 2.0 (the "License"); you may not use this file except
 *in compliance with the License.
 *You may obtain a copy of the License at
 *
 *http://www.apache.org/licenses/LICENSE-2.0
 *
 *Unless required by applicable law or agreed to in writing,
 *software distributed under the License is distributed on an
 *"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *KIND, either express or implied.  See the License for the
 *specific language governing permissions and limitations
 *under the License.
 */

package org.wso2.identity.cypress.integration.execution;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.identity.integration.common.utils.ISIntegrationUITest;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class CypressTestRunnerTestCase extends ISIntegrationUITest {

    private static final Log LOG = LogFactory.getLog(CypressTestRunnerTestCase.class);
    @BeforeClass(alwaysRun = true)
    public void setUp() throws Exception {
        super.init();
    }
    @Test( description = "verify login to IS Server and execute cypress tests.")
    public void runCypressTest() throws IOException, InterruptedException {

        Process process;
        String line;
        final ClassLoader classLoader = getClass().getClassLoader();
        String filePath = classLoader.getResource("test.sh").getFile();
        File file = new File(filePath);
        if (!file.isFile()) {
            throw new IllegalArgumentException("The file" + filePath + " does not exist");
        }

        process = Runtime.getRuntime().exec(new String[] {"sh", filePath}, null);
        process.waitFor();
        BufferedReader reader =new BufferedReader(new InputStreamReader(
                process.getInputStream()));
        while((line = reader.readLine()) != null) {
            LOG.info(line);
        }
    }
}

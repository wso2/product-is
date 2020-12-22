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

package org.wso2.identity.apps.test.container;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.identity.apps.test.container.exception.CypressContainerException;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Cypress Integration Test Utils.
 */
public class CypressTestContainer {

    private static final CypressTestContainer INSTANCE = new CypressTestContainer();

    private static final Log LOG = LogFactory.getLog(CypressTestContainer.class);

    private CypressTestContainer() {

    }

    /**
     * Get the instance.
     *
     * @return instance.
     */
    public static CypressTestContainer getInstance() {

        return INSTANCE;
    }

    /**
     * Add or overwrite the environment configurations of cypress test suite.
     *
     * @param filePath File path of the cypress.env.json file.
     * @param propertyName Name of the cypress environment property.
     * @param propertyValue Value of the cypress environment property.
     */
    public void addOrOverwriteTestConfigProperty(Path filePath, String propertyName, String propertyValue)
            throws CypressContainerException {

        if (StringUtils.isBlank(propertyName)) {
            throw new CypressContainerException("Invalid config element propertyName.");
        }

        Gson gson = new Gson();

        try (Reader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
            JsonObject envConfigJSON = gson.fromJson(reader, JsonObject.class);
            if (envConfigJSON.get(propertyName) != null) {
                envConfigJSON.remove(propertyName);
            }
            envConfigJSON.addProperty(propertyName, propertyValue);

            try (Writer writer = Files.newBufferedWriter(filePath, StandardCharsets.UTF_8)) {
                gson.toJson(envConfigJSON, writer);
            }
            LOG.info("Updated " + CypressTestConstants.CYPRESS_ENV_CONFIG_FILE + " file with key: " + propertyName +
                    ", value: " + propertyValue);
        } catch (IOException e) {
            throw new RuntimeException("Failed to update " + CypressTestConstants.CYPRESS_ENV_CONFIG_FILE +
                    " with key: " + propertyName + ", value: " + propertyValue);
        }
    }

    /**
     * Execute the identity apps cypress test suite.
     *
     * @param scriptPath Path of the script to run the test suite.
     * @throws IOException When character encoding is not supported.
     */
    public void runTestSuite(Path scriptPath) throws IOException, CypressContainerException {

        if (!Files.exists(scriptPath)) {
            throw new CypressContainerException("Script `" + scriptPath.toAbsolutePath().toString() +
                    "` does not exists!");
        }

        Process process;
        if (System.getProperty("os.name").startsWith("Windows")) {
            // TODO run bat file.
            LOG.warn("Skipped the cypress integration test run.");
            return;
        } else {
            process = new ProcessBuilder("/bin/bash", scriptPath.toAbsolutePath().toString()).start();
        }

        try (BufferedReader r = new BufferedReader(
                new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8.name()))) {
            String line;
            while ((line = r.readLine()) != null) {
                LOG.info(line);
            }
        }
    }

    /**
     * Output the results of the tests.
     *
     * @return the Cypress test results.
     * @throws IOException
     */
    public void endTestSuite(Path reportsPath) throws CypressContainerException, IOException {

        CypressTestUtils testUtils = new CypressTestUtils().withMochawesomeReportsAt(reportsPath);
        CypressTestResults testResults = testUtils.getTestResults();

        if (testResults.getNumberOfFailedTests() > 0) {
            LOG.error(testResults);
            throw new CypressContainerException("There was a failure running the Cypress tests!");
        }

        LOG.info("All specs have passed!!!" + testResults);
    }
}

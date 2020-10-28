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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Util class for cypress test suite.
 */
public class CypressTestUtils {

    private static final Log LOG = LogFactory.getLog(CypressTestUtils.class);

    private static final TestResultsStrategy DEFAULT_TEST_RESULTS_STRATEGY = new MochawesomeResultsStrategy();

    private TestResultsStrategy testResultsStrategy = DEFAULT_TEST_RESULTS_STRATEGY;

    private CypressTestUtils withTestResultsStrategy(TestResultsStrategy strategy) {

        if (strategy == null) {
            throw new IllegalArgumentException("strategy should not be null");
        }

        testResultsStrategy = strategy;

        return this;
    }

    /**
     * Set the path (relative to the root of the project) where the Mochawesome reports are put.
     *
     * @param path the relative path.
     * @return the current instance.
     */
     CypressTestUtils withMochawesomeReportsAt(Path path) {

        return withTestResultsStrategy(new MochawesomeResultsStrategy(path));
    }

    /**
     * Waits until the Cypress tests are done and returns the results of the tests.
     *
     * @return the Cypress test results.
     * @throws IOException  When there was a problem parsing the Cypress test reports.
     */
    CypressTestResults getTestResults() throws IOException {

        CypressTestResults results = testResultsStrategy.gatherTestResults();
        LOG.info(results);

        if (results.getNumberOfFailedTests() > 0) {
            LOG.warn(results);
        }

        return results;
    }
}

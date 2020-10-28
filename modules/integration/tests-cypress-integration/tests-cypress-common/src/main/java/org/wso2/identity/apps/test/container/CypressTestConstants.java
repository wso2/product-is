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

/**
 * This class holds the constants related to cypress test suite.
 */
public final class CypressTestConstants {

    public static final String CYPRESS_ENV_CONFIG_FILE = "cypress.env.json";

    public static final String TEST_SUITE_RUNNER_FILE = "test-suite-runner.sh";

    public static final String MOCHA_RESULTS_DIR = "output/results";

    private CypressTestConstants() {

    }

    /**
     * Constants for cypress environment configuration properties.
     */
    public static class EnvironmentConfigElements {

        public static final String SERVER_URL = "SERVER_URL";

        public static final String CONSOLE_BASE_URL = "CONSOLE_BASE_URL";

        public static final String MY_ACCOUNT_BASE_URL = "MY_ACCOUNT_BASE_URL";

        public static final String AUTH_ENDPOINT_URL = "AUTH_ENDPOINT_URL";

        public static final String TENANT_DOMAIN = "TENANT_DOMAIN";

        public static final String SUPER_TENANT_DOMAIN = "SUPER_TENANT_DOMAIN";

        public static final String SCIM2_ENDPOINT = "SCIM2_ENDPOINT";

        public static final String USER_ENDPOINT = "USER_ENDPOINT";

        public static final String BULK_ENDPOINT = "BULK_ENDPOINT";

        public static final String SEARCH_ENDPOINT = "SEARCH_ENDPOINT";

        public static final String APPLICATION_ENDPOINT = "APPLICATION_ENDPOINT";

        public static final String SUPER_TENANT_USERNAME = "SUPER_TENANT_USERNAME";

        public static final String SUPER_TENANT_PASSWORD = "SUPER_TENANT_PASSWORD";

        public static final String TENANT_USERNAME = "TENANT_USERNAME";

        public static final String TENANT_PASSWORD = "TENANT_PASSWORD";

        public static final String LOGOUT_URL_QUERY = "LOGOUT_URL_QUERY";

        private EnvironmentConfigElements() {

        }
    }
}

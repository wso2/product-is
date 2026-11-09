/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.identity.integration.test.rest.api.server.session;

import io.restassured.RestAssured;
import org.apache.commons.lang3.StringUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.wso2.identity.integration.test.rest.api.server.common.RESTAPIServerTestBase;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Base test class for Session Management REST APIs.
 */
public class SessionMgtTestBase extends RESTAPIServerTestBase {

    public static final String API_DEFINITION_NAME = "configs.yaml";
    public static final String API_VERSION = "v1";
    public static final String API_PACKAGE_NAME = "org.wso2.carbon.identity.api.server.configs.v1";

    // URL paths.
    public static final String CONFIGS_API_PATH = "/configs";

    // Session configuration parameter keys.
    public static final String ENABLE_MAX_TIMEOUT_KEY = "enableMaximumSessionTimeoutPeriod";
    public static final String MAX_TIMEOUT_KEY = "maximumSessionTimeoutPeriod";

    // Patch operations.
    public static final String OPERATION_REPLACE = "REPLACE";

    protected static String swaggerDefinition;

    static {
        try {
            swaggerDefinition = getAPISwaggerDefinition(API_PACKAGE_NAME, API_DEFINITION_NAME);
        } catch (IOException e) {
            Assert.fail(String.format("Unable to read the swagger definition %s from %s", API_DEFINITION_NAME,
                    API_PACKAGE_NAME), e);
        }
    }

    @AfterClass(alwaysRun = true)
    public void testConclude() throws Exception {

        super.conclude();
    }

    @BeforeMethod(alwaysRun = true)
    public void testInit() {

        RestAssured.basePath = basePath;
    }

    @AfterMethod(alwaysRun = true)
    public void testFinish() {

        RestAssured.basePath = StringUtils.EMPTY;
    }

    /**
     * Creates a patch operation for configuration update.
     *
     * @param operation Operation type (REPLACE or REMOVE).
     * @param path      Configuration path.
     * @param value     Configuration value (can be null for REMOVE operation).
     * @return Map representing a patch operation.
     */
    protected Map<String, String> createPatchOperation(String operation, String path, String value) {

        Map<String, String> patch = new HashMap<>();
        patch.put("operation", operation);
        patch.put("path", "/" + path);
        if (value != null) {
            patch.put("value", value);
        }
        return patch;
    }

    /**
     * Converts patch operations to JSON string.
     *
     * @param patches List of patch operations.
     * @return JSON string representation of patches.
     */
    protected String convertPatchesToJson(List<Map<String, String>> patches) {

        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < patches.size(); i++) {
            Map<String, String> patch = patches.get(i);
            json.append("{");
            json.append("\"operation\":\"").append(patch.get("operation")).append("\",");
            json.append("\"path\":\"").append(patch.get("path")).append("\"");
            if (patch.containsKey("value")) {
                json.append(",\"value\":\"").append(patch.get("value")).append("\"");
            }
            json.append("}");
            if (i < patches.size() - 1) {
                json.append(",");
            }
        }
        json.append("]");
        return json.toString();
    }
}

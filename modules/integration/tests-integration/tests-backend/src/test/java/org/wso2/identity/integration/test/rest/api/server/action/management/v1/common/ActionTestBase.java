/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.identity.integration.test.rest.api.server.action.management.v1.common;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.restassured.RestAssured;
import org.apache.http.HttpStatus;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.identity.integration.test.rest.api.server.common.RESTAPIServerTestBase;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class ActionTestBase extends RESTAPIServerTestBase {

    private static final String API_DEFINITION_NAME = "Actions.yaml";
    protected static final String API_VERSION = "v1";

    protected static final String ACTION_MANAGEMENT_API_BASE_PATH = "/actions";
    protected static final String TYPES_API_PATH = "/types";
    protected static final String ACTION_DEACTIVATE_PATH = "/deactivate";
    protected static final String ACTION_ACTIVATE_PATH = "/activate";

    protected static final String TEST_ACTION_NAME = "Action name";
    protected static final String TEST_ACTION_DESCRIPTION = "This is the configuration of action.";
    protected static final String TEST_ACTION_ACTIVE_STATUS = "ACTIVE";
    protected static final String TEST_ACTION_INACTIVE_STATUS = "INACTIVE";
    protected static final String TEST_ENDPOINT_URI = "https://abc.com/token";
    protected static final String TEST_USERNAME_AUTH_PROPERTY = "username";
    protected static final String TEST_PASSWORD_AUTH_PROPERTY = "password";
    protected static final String TEST_USERNAME_AUTH_PROPERTY_VALUE = "admin";
    protected static final String TEST_PASSWORD_AUTH_PROPERTY_VALUE = "myPassword123";
    protected static final String TEST_UPDATED_USERNAME_AUTH_PROPERTY_VALUE = "adminUpdated";
    protected static final String TEST_UPDATED_PASSWORD_AUTH_PROPERTY_VALUE = "myPassword123Updated";
    protected static final String TEST_ACTION_UPDATED_NAME = "Action name Updated";
    protected static final String TEST_ACTION_UPDATED_DESCRIPTION = "This is the updated configuration of action.";
    protected static final String TEST_UPDATED_ENDPOINT_URI = "https://abc.com/tokenUpdated";
    protected static final String TEST_PROPERTIES_AUTH_ATTRIBUTE = "properties";
    protected static final String TEST_ACCESS_TOKEN_AUTH_PROPERTY = "accessToken";
    protected static final String TEST_ACCESS_TOKEN_AUTH_PROPERTY_VALUE = "24f64d17-9824-4e28-8413-de45728d8e84";
    protected static final String TEST_UPDATED_ACCESS_TOKEN_AUTH_PROPERTY_VALUE = "88f63a16-9824-4e28-e463-de11118d8e84";
    protected static final String TEST_APIKEY_HEADER_AUTH_PROPERTY = "header";
    protected static final String TEST_APIKEY_HEADER_AUTH_PROPERTY_VALUE = "key";
    protected static final String TEST_APIKEY_VALUE_AUTH_PROPERTY = "value";
    protected static final String TEST_APIKEY_VALUE_AUTH_PROPERTY_VALUE = "secret";
    protected static final String TEST_USERNAME_INVALID_AUTH_PROPERTY = "invalidUsername";
    protected static final String TEST_ACTION_INVALID_ID = "invalid_id";

    protected static final Set<String> NOT_IMPLEMENTED_ACTION_TYPE_PATHS = new HashSet<>();

    protected static String swaggerDefinition;

    static {
        NOT_IMPLEMENTED_ACTION_TYPE_PATHS.add("/preUpdateProfile");
        NOT_IMPLEMENTED_ACTION_TYPE_PATHS.add("/preRegistration");

        String API_PACKAGE_NAME = "org.wso2.carbon.identity.api.server.action.management.v1";
        try {
            swaggerDefinition = getAPISwaggerDefinition(API_PACKAGE_NAME, API_DEFINITION_NAME);
        } catch (IOException e) {
            Assert.fail(String.format("Unable to read the swagger definition %s from %s", API_DEFINITION_NAME,
                    API_PACKAGE_NAME), e);
        }
    }

    @BeforeClass(alwaysRun = true)
    public void init() throws IOException {

        super.testInit(API_VERSION, swaggerDefinition, tenant);
    }

    @AfterClass(alwaysRun = true)
    public void testConclude() throws Exception {

        super.conclude();
    }

    @BeforeMethod(alwaysRun = true)
    public void testInit() {

        RestAssured.basePath = basePath;
    }

    @DataProvider(name = "restAPIUserConfigProvider")
    public static Object[][] restAPIUserConfigProvider() {

        return new Object[][]{
                {TestUserMode.SUPER_TENANT_ADMIN},
                {TestUserMode.TENANT_ADMIN}
        };
    }

    /**
     * Delete an Action.
     *
     * @param actionTypePath Action Type URL Path.
     * @param actionId       ID of the Action.
     */
    protected void deleteAction(String actionTypePath, String actionId) {

        getResponseOfDelete(ACTION_MANAGEMENT_API_BASE_PATH + actionTypePath + "/" +
                actionId).then().assertThat().statusCode(HttpStatus.SC_NO_CONTENT);
    }

    /**
     * To convert object to a json string.
     *
     * @param object Respective java object.
     * @return Relevant json string.
     */
    protected String toJSONString(Object object) {

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(object);
    }

    /**
     * Build the base URL for the REST API.
     *
     * @return Base URL.
     */
    public String buildBaseURL() {

        if (this.tenant.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
            return String.format(API_SERVER_BASE_PATH, "v1");
        } else {
            return "/t/" + this.tenant + String.format(API_SERVER_BASE_PATH, "v1");
        }
    }
}


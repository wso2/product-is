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

package org.wso2.identity.integration.test.rest.api.server.action.management.v1;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.identity.integration.test.rest.api.server.action.management.v1.model.ActionModel;
import org.wso2.identity.integration.test.rest.api.server.action.management.v1.model.ActionUpdateModel;
import org.wso2.identity.integration.test.rest.api.server.action.management.v1.model.AuthenticationType;
import org.wso2.identity.integration.test.rest.api.server.action.management.v1.model.Endpoint;

import java.io.IOException;
import java.util.HashMap;

import static org.hamcrest.CoreMatchers.equalTo;

/**
 * Tests for negative paths of the Action Management REST API.
 */
public class ActionsFailureTest extends ActionsTestBase {

    private static final String TEST_USERNAME_INVALID_AUTH_PROPERTY = "invalidUsername";
    private static final String TEST_ACTION_INVALID_ID = "invalid_id";
    private static ActionModel action1;
    private static ActionModel action2;
    private static String testActionId2;

    @Factory(dataProvider = "restAPIUserConfigProvider")
    public ActionsFailureTest(TestUserMode userMode) throws Exception {

        super.init(userMode);
        this.context = isServer;
        this.authenticatingUserName = context.getContextTenant().getTenantAdmin().getUserName();
        this.authenticatingCredential = context.getContextTenant().getTenantAdmin().getPassword();
        this.tenant = context.getContextTenant().getDomain();
    }

    @BeforeClass(alwaysRun = true)
    public void init() throws IOException {

        super.testInit(API_VERSION, swaggerDefinition, tenant);
    }

    @AfterClass(alwaysRun = true)
    public void testConclude() throws Exception {

        action1 = null;
        action2 = null;
        testActionId2 = null;
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

    @Test
    public void testCreateActionWithInvalidEndpointAuthProperties() {

         action1 = new ActionModel()
                 .name(TEST_ACTION_NAME)
                 .description(TEST_ACTION_DESCRIPTION)
                 .endpoint(new Endpoint()
                         .uri(TEST_ENDPOINT_URI)
                         .authentication(new AuthenticationType()
                                 .type(AuthenticationType.TypeEnum.BASIC)
                                 .properties(new HashMap<String, Object>() {{
                                     put(TEST_USERNAME_INVALID_AUTH_PROPERTY, TEST_USERNAME_AUTH_PROPERTY_VALUE);
                                     put(TEST_PASSWORD_AUTH_PROPERTY, TEST_PASSWORD_AUTH_PROPERTY_VALUE);
                                 }})));

        String body = toJSONString(action1);
        Response responseOfPost = getResponseOfPost(ACTION_MANAGEMENT_API_BASE_PATH +
                PRE_ISSUE_ACCESS_TOKEN_PATH, body);
        responseOfPost.then()
                .log().ifValidationFails()
                .assertThat().statusCode(HttpStatus.SC_BAD_REQUEST)
                .body("description", equalTo("Required authentication properties are not " +
                        "provided or invalid."));
    }

    @Test(dependsOnMethods = {"testCreateActionWithInvalidEndpointAuthProperties"})
    public void testCreateActionWithEmptyEndpointAuthPropertyValues() {

        action1.getEndpoint().getAuthentication().setProperties(new HashMap<String, Object>() {{
            put(TEST_USERNAME_AUTH_PROPERTY, "");
            put(TEST_PASSWORD_AUTH_PROPERTY, TEST_PASSWORD_AUTH_PROPERTY_VALUE);
        }});

        String body = toJSONString(action1);
        Response responseOfPost = getResponseOfPost(ACTION_MANAGEMENT_API_BASE_PATH +
                PRE_ISSUE_ACCESS_TOKEN_PATH, body);
        responseOfPost.then()
                .log().ifValidationFails()
                .assertThat().statusCode(HttpStatus.SC_BAD_REQUEST)
                .body("description", equalTo("Authentication property values cannot be empty."));
    }

    @Test(dependsOnMethods = {"testCreateActionWithEmptyEndpointAuthPropertyValues"})
    public void testCreateActionAfterReachingMaxActionCount() {

        // Create an action.
        testActionId2 = createAction(PRE_ISSUE_ACCESS_TOKEN_PATH);

        // Create another action to exceed the maximum action count.
        ActionModel action = new ActionModel()
                .name(TEST_ACTION_NAME)
                .description(TEST_ACTION_DESCRIPTION)
                .endpoint(new Endpoint()
                        .uri(TEST_ENDPOINT_URI)
                        .authentication(new AuthenticationType()
                                .type(AuthenticationType.TypeEnum.BASIC)
                                .properties(new HashMap<String, Object>() {{
                                    put(TEST_USERNAME_AUTH_PROPERTY, TEST_USERNAME_AUTH_PROPERTY_VALUE);
                                    put(TEST_PASSWORD_AUTH_PROPERTY, TEST_PASSWORD_AUTH_PROPERTY_VALUE);
                                }})));

        String body = toJSONString(action);
        Response responseOfPost = getResponseOfPost(ACTION_MANAGEMENT_API_BASE_PATH +
                PRE_ISSUE_ACCESS_TOKEN_PATH, body);
        responseOfPost.then()
                .log().ifValidationFails()
                .assertThat().statusCode(HttpStatus.SC_BAD_REQUEST)
                .body("description", equalTo("Maximum number of actions per action type is reached."));
    }

    @Test(dependsOnMethods = {"testCreateActionAfterReachingMaxActionCount"})
    public void testGetActionByActionIdWithInvalidID() {

        Response responseOfGet = getResponseOfGet(ACTION_MANAGEMENT_API_BASE_PATH +
                PRE_ISSUE_ACCESS_TOKEN_PATH + "/" + TEST_ACTION_INVALID_ID);

        responseOfGet.then()
                .log().ifValidationFails()
                .assertThat().statusCode(HttpStatus.SC_NOT_FOUND)
                .body("description", equalTo("No action is found for given action id and action type"));
    }

    @Test(dependsOnMethods = {"testGetActionByActionIdWithInvalidID"})
    public void testCreateActionWithNotImplementedActionTypes() {

        for (String actionTypePath : NOT_IMPLEMENTED_ACTION_TYPE_PATHS) {
            String body = toJSONString(action1);
            Response responseOfPost = getResponseOfPost(ACTION_MANAGEMENT_API_BASE_PATH +
                    actionTypePath, body);
            responseOfPost.then()
                    .log().ifValidationFails()
                    .assertThat().statusCode(HttpStatus.SC_NOT_IMPLEMENTED)
                    .body("description", equalTo("The requested action type is not currently " +
                            "supported by the server."));
        }
    }

    @Test(dependsOnMethods = {"testCreateActionWithNotImplementedActionTypes"})
    public void testUpdateActionWithInvalidID() {

        // Update Action basic information with an invalid action id.
        ActionUpdateModel actionUpdateModel = new ActionUpdateModel()
                .name(TEST_ACTION_UPDATED_NAME);

        String body = toJSONString(actionUpdateModel);
        Response getResponseOfPatch = getResponseOfPatch(ACTION_MANAGEMENT_API_BASE_PATH +
                PRE_ISSUE_ACCESS_TOKEN_PATH + "/" + TEST_ACTION_INVALID_ID, body);
        getResponseOfPatch.then()
                .log().ifValidationFails()
                .assertThat().statusCode(HttpStatus.SC_NOT_FOUND)
                .body("description", equalTo("No Action is configured on the given Action Type and Id."));
    }

    @Test(dependsOnMethods = {"testUpdateActionWithInvalidID"})
    public void testActivateActionWithInvalidID() {

        getResponseOfPost(ACTION_MANAGEMENT_API_BASE_PATH + PRE_ISSUE_ACCESS_TOKEN_PATH +
                "/" + TEST_ACTION_INVALID_ID + ACTION_ACTIVATE_PATH, "")
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NOT_FOUND)
                .body("description", equalTo("No Action is configured on the given Action Type and Id."));
    }

    @Test(dependsOnMethods = {"testActivateActionWithInvalidID"})
    public void testDeactivateActionWithInvalidID() {

        getResponseOfPost(ACTION_MANAGEMENT_API_BASE_PATH + PRE_ISSUE_ACCESS_TOKEN_PATH +
                "/" + TEST_ACTION_INVALID_ID + ACTION_DEACTIVATE_PATH, "")
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NOT_FOUND)
                .body("description", equalTo("No Action is configured on the given Action Type and Id."));
    }

    @Test(dependsOnMethods = {"testDeactivateActionWithInvalidID"})
    public void testDeleteActionWithInvalidID() {

        getResponseOfDelete(ACTION_MANAGEMENT_API_BASE_PATH + PRE_ISSUE_ACCESS_TOKEN_PATH +
                "/" + TEST_ACTION_INVALID_ID)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NOT_FOUND)
                .body("description", equalTo("No Action is configured on the given Action Type and Id."));

        // Delete, created action.
        deleteAction(PRE_ISSUE_ACCESS_TOKEN_PATH , testActionId2);
    }

    /**
     * Create a sample Action.
     *
     * @return ID of the created Action.
     */
    private String createAction(String actionTypePath) {

        action2 = new ActionModel()
                .name(TEST_ACTION_NAME)
                .description(TEST_ACTION_DESCRIPTION)
                .endpoint(new Endpoint()
                        .uri(TEST_ENDPOINT_URI)
                        .authentication(new AuthenticationType()
                                .type(AuthenticationType.TypeEnum.BASIC)
                                .properties(new HashMap<String, Object>() {{
                                    put(TEST_USERNAME_AUTH_PROPERTY, TEST_USERNAME_AUTH_PROPERTY_VALUE);
                                    put(TEST_PASSWORD_AUTH_PROPERTY, TEST_PASSWORD_AUTH_PROPERTY_VALUE);
                                }})));

        String body = toJSONString(action2);
        Response responseOfPost = getResponseOfPost(ACTION_MANAGEMENT_API_BASE_PATH +
                actionTypePath, body);
        responseOfPost.then().assertThat().statusCode(HttpStatus.SC_CREATED);

        return responseOfPost.getBody().jsonPath().getString("id");
    }
}

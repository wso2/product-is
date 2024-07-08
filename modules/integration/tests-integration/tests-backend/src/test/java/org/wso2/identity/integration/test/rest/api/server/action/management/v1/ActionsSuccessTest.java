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
import org.apache.http.HttpStatus;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.identity.integration.test.rest.api.server.action.management.v1.model.ActionModel;
import org.wso2.identity.integration.test.rest.api.server.action.management.v1.model.AuthenticationType;
import org.wso2.identity.integration.test.rest.api.server.action.management.v1.model.Endpoint;

import java.io.IOException;
import java.util.HashMap;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.hasKey;

/**
 * Tests for happy paths of the Action Management REST API.
 */
public class ActionsSuccessTest extends ActionsTestBase {

    private static ActionModel action;
    private static String testActionId;

    @Factory(dataProvider = "restAPIUserConfigProvider")
    public ActionsSuccessTest(TestUserMode userMode) throws Exception {

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

        action = null;
        testActionId = null;
        super.conclude();
    }

    @BeforeMethod(alwaysRun = true)
    public void testInit() {

        RestAssured.basePath = basePath;
    }

    @Test
    public void testCreateAction() {

        action = new ActionModel()
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
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .body("id", notNullValue())
                .body("name", equalTo(TEST_ACTION_NAME))
                .body("description", equalTo(TEST_ACTION_DESCRIPTION))
                .body("endpoint.uri", equalTo(TEST_ENDPOINT_URI))
                .body("endpoint.authentication.type", equalTo(AuthenticationType.TypeEnum.BASIC.toString()))
                .body("endpoint.authentication.properties.username", equalTo(TEST_USERNAME_AUTH_PROPERTY_VALUE))
                .body("endpoint.authentication.properties.password", equalTo(TEST_PASSWORD_AUTH_PROPERTY_VALUE));

        testActionId = responseOfPost.getBody().jsonPath().getString("id");
    }

    @Test(dependsOnMethods = {"testCreateAction"})
    public void testGetActionByActionType() {

        Response responseOfGet = getResponseOfGet(ACTION_MANAGEMENT_API_BASE_PATH +
                PRE_ISSUE_ACCESS_TOKEN_PATH);
        responseOfGet.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body( "find { it.id == '" + testActionId + "' }.name", equalTo(TEST_ACTION_NAME))
                .body( "find { it.id == '" + testActionId + "' }.description", equalTo(TEST_ACTION_DESCRIPTION))
                .body( "find { it.id == '" + testActionId + "' }.status", equalTo(TEST_ACTION_ACTIVE_STATUS))
                .body( "find { it.id == '" + testActionId + "' }.endpoint.uri", equalTo(TEST_ENDPOINT_URI))
                .body( "find { it.id == '" + testActionId + "' }.endpoint.authentication.type",
                        equalTo(AuthenticationType.TypeEnum.BASIC.toString()))
                .body( "find { it.id == '" + testActionId + "' }.endpoint.authentication.properties.username",
                        equalTo(TEST_USERNAME_AUTH_PROPERTY_VALUE))
                .body( "find { it.id == '" + testActionId + "' }.endpoint.authentication.properties.password",
                        equalTo(TEST_PASSWORD_AUTH_PROPERTY_VALUE));
    }

    @Test(dependsOnMethods = {"testCreateAction"})
    public void testGetActions() {

        Response responseOfGet = getResponseOfGet(ACTION_MANAGEMENT_API_BASE_PATH + TYPES_API_PATH);
        responseOfGet.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body( "find { it.type == '" + PRE_ISSUE_ACCESS_TOKEN_ACTION_TYPE + "' }.displayName",
                        notNullValue())
                .body( "find { it.type == '" + PRE_ISSUE_ACCESS_TOKEN_ACTION_TYPE + "' }.description",
                        notNullValue())
                .body( "find { it.type == '" + PRE_ISSUE_ACCESS_TOKEN_ACTION_TYPE + "' }.count",
                        equalTo(1))
                .body( "find { it.type == '" + PRE_ISSUE_ACCESS_TOKEN_ACTION_TYPE + "' }.self", notNullValue())
                .body( "find { it.type == '" + PRE_UPDATE_PASSWORD_ACTION_TYPE + "' }.displayName",
                        notNullValue())
                .body( "find { it.type == '" + PRE_UPDATE_PASSWORD_ACTION_TYPE + "' }.description",
                        notNullValue())
                .body( "find { it.type == '" + PRE_UPDATE_PASSWORD_ACTION_TYPE + "' }.count",
                        equalTo(0))
                .body( "find { it.type == '" + PRE_UPDATE_PASSWORD_ACTION_TYPE + "' }.self", notNullValue());
    }

    @Test(dependsOnMethods = {"testGetActionByActionType"})
    public void testUpdateAction() {

        action.setName(TEST_ACTION_UPDATED_NAME);
        action.getEndpoint().setAuthentication(new AuthenticationType()
                .type(AuthenticationType.TypeEnum.BEARER)
                .properties(new HashMap<String, Object>() {{
                    put(TEST_ACCESS_TOKEN_AUTH_PROPERTY, TEST_ACCESS_TOKEN_AUTH_PROPERTY_VALUE);
                }}));

        String body = toJSONString(action);
        Response responseOfPut = getResponseOfPut(ACTION_MANAGEMENT_API_BASE_PATH +
                PRE_ISSUE_ACCESS_TOKEN_PATH + "/" + testActionId, body);

        responseOfPut.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("id", equalTo(testActionId))
                .body("name", equalTo(TEST_ACTION_UPDATED_NAME))
                .body("description", equalTo(TEST_ACTION_DESCRIPTION))
                .body("status", equalTo(TEST_ACTION_ACTIVE_STATUS))
                .body("endpoint.uri", equalTo(TEST_ENDPOINT_URI))
                .body("endpoint.authentication.type", equalTo(AuthenticationType.TypeEnum.BEARER.toString()))
                .body("endpoint.authentication.properties.accessToken",
                        equalTo(TEST_ACCESS_TOKEN_AUTH_PROPERTY_VALUE));
    }

    @Test(dependsOnMethods = {"testUpdateAction"})
    public void testDeactivateAction() {

        getResponseOfPost(ACTION_MANAGEMENT_API_BASE_PATH + PRE_ISSUE_ACCESS_TOKEN_PATH +
                "/" + testActionId + ACTION_DEACTIVATE_PATH, "")
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("id", equalTo(testActionId))
                .body("name", equalTo(TEST_ACTION_UPDATED_NAME))
                .body("description", equalTo(TEST_ACTION_DESCRIPTION))
                .body("status", equalTo(TEST_ACTION_INACTIVE_STATUS));
    }

    @Test(dependsOnMethods = {"testDeactivateAction"})
    public void testActivateAction() {

        getResponseOfPost(ACTION_MANAGEMENT_API_BASE_PATH + PRE_ISSUE_ACCESS_TOKEN_PATH +
                "/" + testActionId + ACTION_ACTIVATE_PATH, "")
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("id", equalTo(testActionId))
                .body("name", equalTo(TEST_ACTION_UPDATED_NAME))
                .body("description", equalTo(TEST_ACTION_DESCRIPTION))
                .body("status", equalTo(TEST_ACTION_ACTIVE_STATUS));
    }

    @Test(dependsOnMethods = {"testActivateAction"})
    public void testDeleteAction() {

        getResponseOfDelete(ACTION_MANAGEMENT_API_BASE_PATH + PRE_ISSUE_ACCESS_TOKEN_PATH + "/" +
                testActionId).then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NO_CONTENT);

        Response responseOfGet = getResponseOfGet(ACTION_MANAGEMENT_API_BASE_PATH + TYPES_API_PATH);
        responseOfGet.then()
                .log().ifValidationFails()
                .assertThat().statusCode(HttpStatus.SC_OK)
                .body( "find { it.type == '" + PRE_ISSUE_ACCESS_TOKEN_ACTION_TYPE + "' }.count",
                        equalTo(0));
    }

    @Test(dependsOnMethods = {"testDeleteAction"})
    public void testCreateActionWithExtraEndpointAuthProperties() {

        action = new ActionModel()
                .name(TEST_ACTION_NAME)
                .description(TEST_ACTION_DESCRIPTION)
                .endpoint(new Endpoint()
                        .uri(TEST_ENDPOINT_URI)
                        .authentication(new AuthenticationType()
                                .type(AuthenticationType.TypeEnum.BASIC)
                                .properties(new HashMap<String, Object>() {{
                                    put(TEST_USERNAME_AUTH_PROPERTY, TEST_USERNAME_AUTH_PROPERTY_VALUE);
                                    put(TEST_PASSWORD_AUTH_PROPERTY, TEST_PASSWORD_AUTH_PROPERTY_VALUE);
                                    put(TEST_ACCESS_TOKEN_AUTH_PROPERTY, TEST_ACCESS_TOKEN_AUTH_PROPERTY_VALUE);
                                }})));

        String body = toJSONString(action);
        Response responseOfPost = getResponseOfPost(ACTION_MANAGEMENT_API_BASE_PATH +
                PRE_ISSUE_ACCESS_TOKEN_PATH, body);
        responseOfPost.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .body("endpoint.authentication.type", equalTo(AuthenticationType.TypeEnum.BASIC.toString()))
                .body("endpoint.authentication.properties.username", equalTo(TEST_USERNAME_AUTH_PROPERTY_VALUE))
                .body("endpoint.authentication.properties.password", equalTo(TEST_PASSWORD_AUTH_PROPERTY_VALUE))
                .body("endpoint.authentication.properties", not(hasKey(TEST_ACCESS_TOKEN_AUTH_PROPERTY)));

        testActionId = responseOfPost.getBody().jsonPath().getString("id");
    }

    @Test(dependsOnMethods = {"testCreateActionWithExtraEndpointAuthProperties"})
    public void testUpdateActionWithExtraEndpointAuthProperties() {

        action.setName(TEST_ACTION_UPDATED_NAME);
        action.getEndpoint().setAuthentication(new AuthenticationType()
                .type(AuthenticationType.TypeEnum.BEARER)
                .properties(new HashMap<String, Object>() {{
                    put(TEST_ACCESS_TOKEN_AUTH_PROPERTY, TEST_ACCESS_TOKEN_AUTH_PROPERTY_VALUE);
                    put(TEST_USERNAME_AUTH_PROPERTY, TEST_USERNAME_AUTH_PROPERTY_VALUE);
                }}));

        String body = toJSONString(action);
        Response responseOfPut = getResponseOfPut(ACTION_MANAGEMENT_API_BASE_PATH +
                PRE_ISSUE_ACCESS_TOKEN_PATH + "/" + testActionId, body);

        responseOfPut.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("name", equalTo(TEST_ACTION_UPDATED_NAME))
                .body("endpoint.authentication.type", equalTo(AuthenticationType.TypeEnum.BEARER.toString()))
                .body("endpoint.authentication.properties.accessToken",
                        equalTo(TEST_ACCESS_TOKEN_AUTH_PROPERTY_VALUE))
                .body("endpoint.authentication.properties", not(hasKey(TEST_USERNAME_AUTH_PROPERTY)));

        // Delete, created action.
        deleteAction(PRE_ISSUE_ACCESS_TOKEN_PATH , testActionId);
    }
}

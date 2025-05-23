/*
 * Copyright (c) 2024-2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.identity.integration.test.rest.api.server.action.management.v1.preissueaccesstoken;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.apache.http.HttpStatus;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.identity.integration.test.rest.api.server.action.management.v1.common.model.ANDRule;
import org.wso2.identity.integration.test.rest.api.server.action.management.v1.common.model.ActionModel;
import org.wso2.identity.integration.test.rest.api.server.action.management.v1.common.model.ActionUpdateModel;
import org.wso2.identity.integration.test.rest.api.server.action.management.v1.common.model.AuthenticationType;
import org.wso2.identity.integration.test.rest.api.server.action.management.v1.common.model.Endpoint;
import org.wso2.identity.integration.test.rest.api.server.action.management.v1.common.model.EndpointUpdateModel;
import org.wso2.identity.integration.test.rest.api.server.action.management.v1.common.model.Expression;
import org.wso2.identity.integration.test.rest.api.server.action.management.v1.common.model.ORRule;

import java.io.IOException;
import java.util.HashMap;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.hasKey;

/**
 * Tests for happy paths of the Action Management REST API.
 */
public class PreIssueAccessTokenActionSuccessTest extends PreIssueAccessTokenTestBase {

    private static ActionModel action;
    private static String testActionId;

    @Factory(dataProvider = "restAPIUserConfigProvider")
    public PreIssueAccessTokenActionSuccessTest(TestUserMode userMode) throws Exception {

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
                .body("status", equalTo(TEST_ACTION_INACTIVE_STATUS))
                .body("endpoint.uri", equalTo(TEST_ENDPOINT_URI))
                .body("endpoint.authentication.type", equalTo(AuthenticationType.TypeEnum.BASIC.toString()))
                .body("endpoint.authentication", not(hasKey(TEST_PROPERTIES_AUTH_ATTRIBUTE)));

        testActionId = responseOfPost.getBody().jsonPath().getString("id");
    }

    @Test(dependsOnMethods = {"testCreateAction"})
    public void testGetActionsByActionType() {

        Response responseOfGet = getResponseOfGet(ACTION_MANAGEMENT_API_BASE_PATH +
                PRE_ISSUE_ACCESS_TOKEN_PATH);
        responseOfGet.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body( "find { it.id == '" + testActionId + "' }.name", equalTo(TEST_ACTION_NAME))
                .body( "find { it.id == '" + testActionId + "' }.description", equalTo(TEST_ACTION_DESCRIPTION))
                .body( "find { it.id == '" + testActionId + "' }.status", equalTo(TEST_ACTION_INACTIVE_STATUS))
                .body("find { it.id == '" + testActionId + "' }.links", notNullValue())
                .body("find { it.id == '" + testActionId + "' }.links.find { it.rel == 'self' }.href",
                        equalTo(buildBaseURL() + ACTION_MANAGEMENT_API_BASE_PATH +
                                PRE_ISSUE_ACCESS_TOKEN_PATH + "/" + testActionId))
                .body("find { it.id == '" + testActionId + "' }.links.find { it.rel == 'self' }.method",
                        equalTo("GET"));
    }

    @Test(dependsOnMethods = {"testGetActionsByActionType"})
    public void testGetActionByActionId() {

        Response responseOfGet = getResponseOfGet(ACTION_MANAGEMENT_API_BASE_PATH +
                PRE_ISSUE_ACCESS_TOKEN_PATH + "/" + testActionId);
        responseOfGet.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("id", equalTo(testActionId))
                .body("name", equalTo(TEST_ACTION_NAME))
                .body("description", equalTo(TEST_ACTION_DESCRIPTION))
                .body("status", equalTo(TEST_ACTION_INACTIVE_STATUS))
                .body("endpoint.uri", equalTo(TEST_ENDPOINT_URI))
                .body("endpoint.authentication.type", equalTo(AuthenticationType.TypeEnum.BASIC.toString()))
                .body("endpoint.authentication", not(hasKey(TEST_PROPERTIES_AUTH_ATTRIBUTE)));
    }

    @Test(dependsOnMethods = {"testGetActionByActionId"})
    public void testGetActionTypes() {

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
                .body( "find { it.type == '" + PRE_ISSUE_ACCESS_TOKEN_ACTION_TYPE + "' }.self", notNullValue());
    }

    @Test(dependsOnMethods = {"testGetActionTypes"})
    public void testUpdateActionUpdatingAllProperties() {

        ActionUpdateModel actionUpdateModel = new ActionUpdateModel()
                .name(TEST_ACTION_UPDATED_NAME)
                .description(TEST_ACTION_UPDATED_DESCRIPTION)
                .endpoint(new EndpointUpdateModel()
                        .uri(TEST_UPDATED_ENDPOINT_URI)
                        .authentication(new AuthenticationType()
                                .type(AuthenticationType.TypeEnum.API_KEY)
                                .properties(new HashMap<String, Object>() {{
                                    put(TEST_APIKEY_HEADER_AUTH_PROPERTY, TEST_APIKEY_HEADER_AUTH_PROPERTY_VALUE);
                                    put(TEST_APIKEY_VALUE_AUTH_PROPERTY, TEST_APIKEY_VALUE_AUTH_PROPERTY_VALUE);
                                }})));

        String body = toJSONString(actionUpdateModel);
        Response responseOfPatch = getResponseOfPatch(ACTION_MANAGEMENT_API_BASE_PATH +
                PRE_ISSUE_ACCESS_TOKEN_PATH + "/" + testActionId, body);

        responseOfPatch.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("id", equalTo(testActionId))
                .body("name", equalTo(TEST_ACTION_UPDATED_NAME))
                .body("description", equalTo(TEST_ACTION_UPDATED_DESCRIPTION))
                .body("status", equalTo(TEST_ACTION_INACTIVE_STATUS))
                .body("endpoint.uri", equalTo(TEST_UPDATED_ENDPOINT_URI))
                .body("endpoint.authentication.type", equalTo(AuthenticationType.TypeEnum.API_KEY.toString()))
                .body("endpoint.authentication", not(hasKey(TEST_PROPERTIES_AUTH_ATTRIBUTE)));
    }

    @Test(dependsOnMethods = {"testUpdateActionUpdatingAllProperties"})
    public void testUpdateActionUpdatingName() {

        ActionUpdateModel actionUpdateModel = new ActionUpdateModel().name(TEST_ACTION_NAME);

        String body = toJSONString(actionUpdateModel);
        Response responseOfPatch = getResponseOfPatch(ACTION_MANAGEMENT_API_BASE_PATH +
                PRE_ISSUE_ACCESS_TOKEN_PATH + "/" + testActionId, body);

        responseOfPatch.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("id", equalTo(testActionId))
                .body("name", equalTo(TEST_ACTION_NAME))
                .body("description", equalTo(TEST_ACTION_UPDATED_DESCRIPTION))
                .body("status", equalTo(TEST_ACTION_INACTIVE_STATUS))
                .body("endpoint.uri", equalTo(TEST_UPDATED_ENDPOINT_URI))
                .body("endpoint.authentication.type", equalTo(AuthenticationType.TypeEnum.API_KEY.toString()))
                .body("endpoint.authentication", not(hasKey(TEST_PROPERTIES_AUTH_ATTRIBUTE)));
    }

    @Test(dependsOnMethods = {"testUpdateActionUpdatingName"})
    public void testUpdateActionUpdatingEndpoint() {

        ActionUpdateModel actionUpdateModel =
                new ActionUpdateModel().endpoint(new EndpointUpdateModel().uri(TEST_ENDPOINT_URI));

        String body = toJSONString(actionUpdateModel);
        Response responseOfPatch = getResponseOfPatch(ACTION_MANAGEMENT_API_BASE_PATH +
                PRE_ISSUE_ACCESS_TOKEN_PATH + "/" + testActionId, body);

        responseOfPatch.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("id", equalTo(testActionId))
                .body("name", equalTo(TEST_ACTION_NAME))
                .body("description", equalTo(TEST_ACTION_UPDATED_DESCRIPTION))
                .body("status", equalTo(TEST_ACTION_INACTIVE_STATUS))
                .body("endpoint.uri", equalTo(TEST_ENDPOINT_URI))
                .body("endpoint.authentication.type", equalTo(AuthenticationType.TypeEnum.API_KEY.toString()))
                .body("endpoint.authentication", not(hasKey(TEST_PROPERTIES_AUTH_ATTRIBUTE)));
    }

    @Test(dependsOnMethods = {"testUpdateActionUpdatingEndpoint"})
    public void testUpdateActionUpdatingAuthentication() {

        ActionUpdateModel actionUpdateModel = new ActionUpdateModel()
                .endpoint(new EndpointUpdateModel()
                        .authentication(new AuthenticationType()
                                .type(AuthenticationType.TypeEnum.BEARER)
                                .properties(new HashMap<String, Object>() {{
                                    put(TEST_ACCESS_TOKEN_AUTH_PROPERTY, TEST_ACCESS_TOKEN_AUTH_PROPERTY_VALUE);
                                }})));

        String body = toJSONString(actionUpdateModel);
        Response responseOfPatch = getResponseOfPatch(ACTION_MANAGEMENT_API_BASE_PATH +
                PRE_ISSUE_ACCESS_TOKEN_PATH + "/" + testActionId, body);

        responseOfPatch.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("id", equalTo(testActionId))
                .body("name", equalTo(TEST_ACTION_NAME))
                .body("description", equalTo(TEST_ACTION_UPDATED_DESCRIPTION))
                .body("status", equalTo(TEST_ACTION_INACTIVE_STATUS))
                .body("endpoint.uri", equalTo(TEST_ENDPOINT_URI))
                .body("endpoint.authentication.type", equalTo(AuthenticationType.TypeEnum.BEARER.toString()))
                .body("endpoint.authentication", not(hasKey(TEST_PROPERTIES_AUTH_ATTRIBUTE)));
    }

    @Test(dependsOnMethods = {"testUpdateActionUpdatingAuthentication"})
    public void testUpdateActionUpdatingAuthenticationProperties() {

        ActionUpdateModel actionUpdateModel = new ActionUpdateModel()
                .endpoint(new EndpointUpdateModel()
                        .authentication(new AuthenticationType()
                                .type(AuthenticationType.TypeEnum.BEARER)
                                .properties(new HashMap<String, Object>() {{
                                    put(TEST_ACCESS_TOKEN_AUTH_PROPERTY, TEST_UPDATED_ACCESS_TOKEN_AUTH_PROPERTY_VALUE);
                                }})));

        String body = toJSONString(actionUpdateModel);
        Response responseOfPatch = getResponseOfPatch(ACTION_MANAGEMENT_API_BASE_PATH +
                PRE_ISSUE_ACCESS_TOKEN_PATH + "/" + testActionId, body);

        responseOfPatch.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("id", equalTo(testActionId))
                .body("name", equalTo(TEST_ACTION_NAME))
                .body("description", equalTo(TEST_ACTION_UPDATED_DESCRIPTION))
                .body("status", equalTo(TEST_ACTION_INACTIVE_STATUS))
                .body("endpoint.uri", equalTo(TEST_ENDPOINT_URI))
                .body("endpoint.authentication.type", equalTo(AuthenticationType.TypeEnum.BEARER.toString()))
                .body("endpoint.authentication", not(hasKey(TEST_PROPERTIES_AUTH_ATTRIBUTE)));
    }

    @Test(dependsOnMethods = {"testUpdateActionUpdatingAuthenticationProperties"})
    public void testUpdateActionUpdatingEndpointUriAndAuthentication() {

        ActionUpdateModel actionUpdateModel = new ActionUpdateModel()
                .endpoint(new EndpointUpdateModel()
                        .uri(TEST_UPDATED_ENDPOINT_URI)
                        .authentication(new AuthenticationType()
                                .type(AuthenticationType.TypeEnum.BASIC)
                                .properties(new HashMap<String, Object>() {{
                                    put(TEST_USERNAME_AUTH_PROPERTY, TEST_USERNAME_AUTH_PROPERTY_VALUE);
                                    put(TEST_PASSWORD_AUTH_PROPERTY, TEST_PASSWORD_AUTH_PROPERTY_VALUE);
                                }})));

        String body = toJSONString(actionUpdateModel);
        Response responseOfPatch = getResponseOfPatch(ACTION_MANAGEMENT_API_BASE_PATH +
                PRE_ISSUE_ACCESS_TOKEN_PATH + "/" + testActionId, body);

        responseOfPatch.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("id", equalTo(testActionId))
                .body("name", equalTo(TEST_ACTION_NAME))
                .body("description", equalTo(TEST_ACTION_UPDATED_DESCRIPTION))
                .body("status", equalTo(TEST_ACTION_INACTIVE_STATUS))
                .body("endpoint.uri", equalTo(TEST_UPDATED_ENDPOINT_URI))
                .body("endpoint.authentication.type", equalTo(AuthenticationType.TypeEnum.BASIC.toString()))
                .body("endpoint.authentication", not(hasKey(TEST_PROPERTIES_AUTH_ATTRIBUTE)));
    }

    @Test(dependsOnMethods = {"testUpdateActionUpdatingEndpointUriAndAuthentication"})
    public void testUpdateActionUpdatingEndpointUriAndAuthenticationProperties() {

        ActionUpdateModel actionUpdateModel = new ActionUpdateModel()
                .endpoint(new EndpointUpdateModel()
                        .uri(TEST_ENDPOINT_URI)
                        .authentication(new AuthenticationType()
                                .type(AuthenticationType.TypeEnum.BASIC)
                                .properties(new HashMap<String, Object>() {{
                                    put(TEST_USERNAME_AUTH_PROPERTY, TEST_UPDATED_USERNAME_AUTH_PROPERTY_VALUE);
                                    put(TEST_PASSWORD_AUTH_PROPERTY, TEST_UPDATED_PASSWORD_AUTH_PROPERTY_VALUE);
                                }})));

        String body = toJSONString(actionUpdateModel);
        Response responseOfPatch = getResponseOfPatch(ACTION_MANAGEMENT_API_BASE_PATH +
                PRE_ISSUE_ACCESS_TOKEN_PATH + "/" + testActionId, body);

        responseOfPatch.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("id", equalTo(testActionId))
                .body("name", equalTo(TEST_ACTION_NAME))
                .body("description", equalTo(TEST_ACTION_UPDATED_DESCRIPTION))
                .body("status", equalTo(TEST_ACTION_INACTIVE_STATUS))
                .body("endpoint.uri", equalTo(TEST_ENDPOINT_URI))
                .body("endpoint.authentication.type", equalTo(AuthenticationType.TypeEnum.BASIC.toString()))
                .body("endpoint.authentication", not(hasKey(TEST_PROPERTIES_AUTH_ATTRIBUTE)));
    }

    @Test(dependsOnMethods = {"testUpdateActionUpdatingEndpointUriAndAuthenticationProperties"})
    public void testUpdateActionAddingRule() {

        ORRule rule = new ORRule()
                .condition(ORRule.ConditionEnum.OR)
                .addRulesItem(new ANDRule().condition(ANDRule.ConditionEnum.AND)
                        .addExpressionsItem(new Expression().field("application").operator("equals")
                                .value("855bb864-2839-4bdf-aabd-4cc635b6faba"))
                        .addExpressionsItem(new Expression().field("grantType").operator("equals")
                                .value("authorization_code")));

        ActionUpdateModel actionUpdateModel = new ActionUpdateModel().rule(rule);

        String body = toJSONString(actionUpdateModel);
        Response responseOfPatch = getResponseOfPatch(ACTION_MANAGEMENT_API_BASE_PATH +
                PRE_ISSUE_ACCESS_TOKEN_PATH + "/" + testActionId, body);

        responseOfPatch.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("id", equalTo(testActionId))
                .body("name", equalTo(TEST_ACTION_NAME))
                .body("description", equalTo(TEST_ACTION_UPDATED_DESCRIPTION))
                .body("status", equalTo(TEST_ACTION_INACTIVE_STATUS))
                .body("endpoint.uri", equalTo(TEST_ENDPOINT_URI))
                .body("endpoint.authentication.type", equalTo(AuthenticationType.TypeEnum.BASIC.toString()))
                .body("endpoint.authentication", not(hasKey(TEST_PROPERTIES_AUTH_ATTRIBUTE)))
                .body("rule.condition", equalTo("OR"))
                .body("rule.rules[0].condition", equalTo("AND"))
                .body("rule.rules[0].expressions[0].field", equalTo("application"))
                .body("rule.rules[0].expressions[0].operator", equalTo("equals"))
                .body("rule.rules[0].expressions[0].value", equalTo("855bb864-2839-4bdf-aabd-4cc635b6faba"))
                .body("rule.rules[0].expressions[1].field", equalTo("grantType"))
                .body("rule.rules[0].expressions[1].operator", equalTo("equals"))
                .body("rule.rules[0].expressions[1].value", equalTo("authorization_code"));
    }

    @Test(dependsOnMethods = {"testUpdateActionAddingRule"})
    public void testUpdateActionUpdatingExistingRule() {

        ORRule rule = new ORRule()
                .condition(ORRule.ConditionEnum.OR)
                .addRulesItem(new ANDRule().condition(ANDRule.ConditionEnum.AND)
                        .addExpressionsItem(new Expression().field("application").operator("equals")
                                .value("855bb864-2839-4bdf-aabd-4cc635b6faba"))
                        .addExpressionsItem(new Expression().field("grantType").operator("equals")
                                .value("authorization_code")))
                .addRulesItem(new ANDRule().condition(ANDRule.ConditionEnum.AND)
                        .addExpressionsItem(new Expression().field("grantType").operator("equals")
                                .value("client_credentials")));

        ActionUpdateModel actionUpdateModel = new ActionUpdateModel().rule(rule);

        String body = toJSONString(actionUpdateModel);
        Response responseOfPatch = getResponseOfPatch(ACTION_MANAGEMENT_API_BASE_PATH +
                PRE_ISSUE_ACCESS_TOKEN_PATH + "/" + testActionId, body);

        responseOfPatch.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("id", equalTo(testActionId))
                .body("name", equalTo(TEST_ACTION_NAME))
                .body("description", equalTo(TEST_ACTION_UPDATED_DESCRIPTION))
                .body("status", equalTo(TEST_ACTION_INACTIVE_STATUS))
                .body("endpoint.uri", equalTo(TEST_ENDPOINT_URI))
                .body("endpoint.authentication.type", equalTo(AuthenticationType.TypeEnum.BASIC.toString()))
                .body("endpoint.authentication", not(hasKey(TEST_PROPERTIES_AUTH_ATTRIBUTE)))
                .body("rule.condition", equalTo("OR"))
                .body("rule.rules[0].condition", equalTo("AND"))
                .body("rule.rules[0].expressions[0].field", equalTo("application"))
                .body("rule.rules[0].expressions[0].operator", equalTo("equals"))
                .body("rule.rules[0].expressions[0].value", equalTo("855bb864-2839-4bdf-aabd-4cc635b6faba"))
                .body("rule.rules[0].expressions[1].field", equalTo("grantType"))
                .body("rule.rules[0].expressions[1].operator", equalTo("equals"))
                .body("rule.rules[0].expressions[1].value", equalTo("authorization_code"))
                .body("rule.rules[1].condition", equalTo("AND"))
                .body("rule.rules[1].expressions[0].field", equalTo("grantType"))
                .body("rule.rules[1].expressions[0].operator", equalTo("equals"))
                .body("rule.rules[1].expressions[0].value", equalTo("client_credentials"));
    }

    @Test(dependsOnMethods = {"testUpdateActionUpdatingExistingRule"})
    public void testUpdateActionRemovingExistingRule() {

        ORRule rule = new ORRule();

        ActionUpdateModel actionUpdateModel = new ActionUpdateModel().rule(rule);

        String body = toJSONString(actionUpdateModel);
        Response responseOfPatch = getResponseOfPatch(ACTION_MANAGEMENT_API_BASE_PATH +
                PRE_ISSUE_ACCESS_TOKEN_PATH + "/" + testActionId, body);

        responseOfPatch.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("id", equalTo(testActionId))
                .body("name", equalTo(TEST_ACTION_NAME))
                .body("description", equalTo(TEST_ACTION_UPDATED_DESCRIPTION))
                .body("status", equalTo(TEST_ACTION_INACTIVE_STATUS))
                .body("endpoint.uri", equalTo(TEST_ENDPOINT_URI))
                .body("endpoint.authentication.type", equalTo(AuthenticationType.TypeEnum.BASIC.toString()))
                .body("endpoint.authentication", not(hasKey(TEST_PROPERTIES_AUTH_ATTRIBUTE)))
                .body("$", not(hasKey("rule")));
    }

    @Test(dependsOnMethods = {"testUpdateActionRemovingExistingRule"})
    public void testActivateAction() {

        getResponseOfPost(ACTION_MANAGEMENT_API_BASE_PATH + PRE_ISSUE_ACCESS_TOKEN_PATH +
                "/" + testActionId + ACTION_ACTIVATE_PATH, "")
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("id", equalTo(testActionId))
                .body("name", equalTo(TEST_ACTION_NAME))
                .body("description", equalTo(TEST_ACTION_UPDATED_DESCRIPTION))
                .body("status", equalTo(TEST_ACTION_ACTIVE_STATUS));
    }

    @Test(dependsOnMethods = {"testActivateAction"})
    public void testDeactivateAction() {

        getResponseOfPost(ACTION_MANAGEMENT_API_BASE_PATH + PRE_ISSUE_ACCESS_TOKEN_PATH +
                "/" + testActionId + ACTION_DEACTIVATE_PATH, "")
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("id", equalTo(testActionId))
                .body("name", equalTo(TEST_ACTION_NAME))
                .body("description", equalTo(TEST_ACTION_UPDATED_DESCRIPTION))
                .body("status", equalTo(TEST_ACTION_INACTIVE_STATUS));
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
    public void testDeleteNonExistingAction() {

        getResponseOfDelete(ACTION_MANAGEMENT_API_BASE_PATH + PRE_ISSUE_ACCESS_TOKEN_PATH +
                "/" + TEST_ACTION_INVALID_ID)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NO_CONTENT);
    }

    @Test(dependsOnMethods = {"testDeleteNonExistingAction"})
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
                .body("endpoint.authentication.type", equalTo(AuthenticationType.TypeEnum.BASIC.toString()));

        // Delete, created action.
        deleteAction(PRE_ISSUE_ACCESS_TOKEN_PATH , responseOfPost.getBody().jsonPath().getString("id"));
    }

    @Test(dependsOnMethods = {"testCreateActionWithExtraEndpointAuthProperties"})
    public void testCreateActionWithRule() {

        ORRule rule = new ORRule()
                .condition(ORRule.ConditionEnum.OR)
                .addRulesItem(new ANDRule().condition(ANDRule.ConditionEnum.AND)
                        .addExpressionsItem(new Expression().field("application").operator("equals")
                                .value("855bb864-2839-4bdf-aabd-4cc635b6faba"))
                        .addExpressionsItem(new Expression().field("grantType").operator("equals")
                                .value("authorization_code")));

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
                                }})))
                .rule(rule);

        String body = toJSONString(action);
        Response responseOfPost = getResponseOfPost(ACTION_MANAGEMENT_API_BASE_PATH +
                PRE_ISSUE_ACCESS_TOKEN_PATH, body);
        responseOfPost.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .body("rule.condition", equalTo("OR"))
                .body("rule.rules[0].condition", equalTo("AND"))
                .body("rule.rules[0].expressions[0].field", equalTo("application"))
                .body("rule.rules[0].expressions[0].operator", equalTo("equals"))
                .body("rule.rules[0].expressions[0].value", equalTo("855bb864-2839-4bdf-aabd-4cc635b6faba"))
                .body("rule.rules[0].expressions[1].field", equalTo("grantType"))
                .body("rule.rules[0].expressions[1].operator", equalTo("equals"))
                .body("rule.rules[0].expressions[1].value", equalTo("authorization_code"));

        deleteAction(PRE_ISSUE_ACCESS_TOKEN_PATH, responseOfPost.getBody().jsonPath().getString("id"));
    }
}

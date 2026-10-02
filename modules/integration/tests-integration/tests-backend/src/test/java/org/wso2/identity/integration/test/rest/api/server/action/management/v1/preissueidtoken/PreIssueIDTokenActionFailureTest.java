/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.identity.integration.test.rest.api.server.action.management.v1.preissueidtoken;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.identity.integration.test.rest.api.server.action.management.v1.common.ActionTestBase;
import org.wso2.identity.integration.test.rest.api.server.action.management.v1.common.model.ANDRule;
import org.wso2.identity.integration.test.rest.api.server.action.management.v1.common.model.ActionModel;
import org.wso2.identity.integration.test.rest.api.server.action.management.v1.common.model.ActionUpdateModel;
import org.wso2.identity.integration.test.rest.api.server.action.management.v1.common.model.AuthenticationType;
import org.wso2.identity.integration.test.rest.api.server.action.management.v1.common.model.Endpoint;
import org.wso2.identity.integration.test.rest.api.server.action.management.v1.common.model.Expression;
import org.wso2.identity.integration.test.rest.api.server.action.management.v1.common.model.ORRule;

import java.io.IOException;
import java.util.HashMap;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.wso2.identity.integration.test.rest.api.server.action.management.v1.preissueidtoken
        .PreIssueIDTokenTestBase.PRE_ISSUE_ID_TOKEN_PATH;

/**
 * Tests for negative paths of the Pre Issue ID Token Action Management REST API.
 */
public class PreIssueIDTokenActionFailureTest extends ActionTestBase {

    private static ActionModel action1;
    private static ActionModel action2;
    private static String testActionId2;

    @Factory(dataProvider = "restAPIUserConfigProvider")
    public PreIssueIDTokenActionFailureTest(TestUserMode userMode) throws Exception {

        super.init(userMode);
        this.context = isServer;
        this.authenticatingUserName = context.getContextTenant().getTenantAdmin().getUserName();
        this.authenticatingCredential = context.getContextTenant().getTenantAdmin().getPassword();
        this.tenant = context.getContextTenant().getDomain();
    }

    @AfterClass(alwaysRun = true)
    @Override
    public void testConclude() throws Exception {

        action1 = null;
        action2 = null;
        testActionId2 = null;
        super.conclude();
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
                PRE_ISSUE_ID_TOKEN_PATH, body);
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
                PRE_ISSUE_ID_TOKEN_PATH, body);
        responseOfPost.then()
                .log().ifValidationFails()
                .assertThat().statusCode(HttpStatus.SC_BAD_REQUEST)
                .body("description", equalTo("Authentication property values cannot be empty."));
    }

    @Test(dependsOnMethods = {"testCreateActionWithEmptyEndpointAuthPropertyValues"})
    public void testCreateActionAfterReachingMaxActionCount() {

        // Create an action.
        testActionId2 = createAction(PRE_ISSUE_ID_TOKEN_PATH);

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
                PRE_ISSUE_ID_TOKEN_PATH, body);
        responseOfPost.then()
                .log().ifValidationFails()
                .assertThat().statusCode(HttpStatus.SC_BAD_REQUEST)
                .body("description", equalTo("Maximum number of actions per action type is reached."));
    }

    @Test(dependsOnMethods = {"testCreateActionWithEmptyEndpointAuthPropertyValues"})
    public void testCreateActionWithInvalidRule() {

        // Create an action, with an expression with an invalid field name.
        ORRule rule = new ORRule()
                .condition(ORRule.ConditionEnum.OR)
                .addRulesItem(new ANDRule().condition(ANDRule.ConditionEnum.AND)
                        .addExpressionsItem(new Expression().field("invalid").operator("equals")
                                .value("855bb864-2839-4bdf-aabd-4cc635b6faba")));

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
                                }})))
                .rule(rule);

        String body = toJSONString(action);
        Response responseOfPost = getResponseOfPost(ACTION_MANAGEMENT_API_BASE_PATH +
                PRE_ISSUE_ID_TOKEN_PATH, body);
        responseOfPost.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST).body("code", equalTo("ACTION-60013"))
                .body("message", equalTo("Invalid rule."))
                .body("description", equalTo("Rule validation failed: Field invalid is not supported"));
    }

    @Test(dependsOnMethods = {"testCreateActionWithInvalidRule"})
    public void testGetActionByActionIdWithInvalidID() {

        Response responseOfGet = getResponseOfGet(ACTION_MANAGEMENT_API_BASE_PATH +
                PRE_ISSUE_ID_TOKEN_PATH + "/" + TEST_ACTION_INVALID_ID);

        responseOfGet.then()
                .log().ifValidationFails()
                .assertThat().statusCode(HttpStatus.SC_NOT_FOUND)
                .body("description", equalTo("No action is found for given action id and action type"));
    }

    @Test(dependsOnMethods = {"testGetActionByActionIdWithInvalidID"})
    public void testUpdateActionWithInvalidID() {

        // Update Action basic information with an invalid action id.
        ActionUpdateModel actionUpdateModel = new ActionUpdateModel()
                .name(TEST_ACTION_UPDATED_NAME);

        String body = toJSONString(actionUpdateModel);
        Response getResponseOfPatch = getResponseOfPatch(ACTION_MANAGEMENT_API_BASE_PATH +
                PRE_ISSUE_ID_TOKEN_PATH + "/" + TEST_ACTION_INVALID_ID, body);
        getResponseOfPatch.then()
                .log().ifValidationFails()
                .assertThat().statusCode(HttpStatus.SC_NOT_FOUND)
                .body("description", equalTo("No Action is configured on the given Action Type and Id."));
    }

    @Test(dependsOnMethods = {"testUpdateActionWithInvalidID"})
    public void testUpdateActionWithInvalidRule() {

        String createdActionId = createActionWithRule();

        // Update the action, with an expression with an invalid operator.
        ORRule rule = new ORRule()
                .condition(ORRule.ConditionEnum.OR)
                .addRulesItem(new ANDRule().condition(ANDRule.ConditionEnum.AND)
                        .addExpressionsItem(new Expression().field("application").operator("invalid")
                                .value("855bb864-2839-4bdf-aabd-4cc635b6faba")));

        ActionUpdateModel actionUpdateModel = new ActionUpdateModel().rule(rule);

        String body = toJSONString(actionUpdateModel);
        Response responseOfPatch = getResponseOfPatch(ACTION_MANAGEMENT_API_BASE_PATH +
                PRE_ISSUE_ID_TOKEN_PATH + "/" + createdActionId, body);
        responseOfPatch.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST).body("code", equalTo("ACTION-60013"))
                .body("message", equalTo("Invalid rule."))
                .body("description",
                        equalTo("Rule validation failed: Operator invalid is not supported for field " +
                                "application"));

        // Delete, created action.
        deleteAction(PRE_ISSUE_ID_TOKEN_PATH, createdActionId);
    }

    @Test(dependsOnMethods = {"testUpdateActionWithInvalidID"})
    public void testActivateActionWithInvalidID() {

        getResponseOfPost(ACTION_MANAGEMENT_API_BASE_PATH + PRE_ISSUE_ID_TOKEN_PATH +
                "/" + TEST_ACTION_INVALID_ID + ACTION_ACTIVATE_PATH, "")
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NOT_FOUND)
                .body("description", equalTo("No Action is configured on the given Action Type and Id."));
    }

    @Test(dependsOnMethods = {"testActivateActionWithInvalidID"})
    public void testDeactivateActionWithInvalidID() {

        Response responseOfPost = getResponseOfPost(ACTION_MANAGEMENT_API_BASE_PATH + PRE_ISSUE_ID_TOKEN_PATH +
                "/" + TEST_ACTION_INVALID_ID + ACTION_DEACTIVATE_PATH, "");

        responseOfPost.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NOT_FOUND)
                .body("description", equalTo("No Action is configured on the given Action Type and Id."));

        // Delete, created action.
        deleteAction(PRE_ISSUE_ID_TOKEN_PATH , testActionId2);
    }

    @Test(dependsOnMethods = {"testDeactivateActionWithInvalidID"})
    public void testCreateActionWithPasswordCredentialMissingClientId() {

        assertPasswordCredentialMissingProperty(buildPasswordCredentialProperties(
                null, TEST_CLIENT_SECRET_AUTH_PROPERTY_VALUE, TEST_TOKEN_ENDPOINT_AUTH_PROPERTY_VALUE,
                TEST_USERNAME_AUTH_PROPERTY_VALUE, TEST_PASSWORD_AUTH_PROPERTY_VALUE));
    }

    @Test(dependsOnMethods = {"testCreateActionWithPasswordCredentialMissingClientId"})
    public void testCreateActionWithPasswordCredentialMissingClientSecret() {

        assertPasswordCredentialMissingProperty(buildPasswordCredentialProperties(
                TEST_CLIENT_ID_AUTH_PROPERTY_VALUE, null, TEST_TOKEN_ENDPOINT_AUTH_PROPERTY_VALUE,
                TEST_USERNAME_AUTH_PROPERTY_VALUE, TEST_PASSWORD_AUTH_PROPERTY_VALUE));
    }

    @Test(dependsOnMethods = {"testCreateActionWithPasswordCredentialMissingClientSecret"})
    public void testCreateActionWithPasswordCredentialMissingTokenEndpoint() {

        assertPasswordCredentialMissingProperty(buildPasswordCredentialProperties(
                TEST_CLIENT_ID_AUTH_PROPERTY_VALUE, TEST_CLIENT_SECRET_AUTH_PROPERTY_VALUE, null,
                TEST_USERNAME_AUTH_PROPERTY_VALUE, TEST_PASSWORD_AUTH_PROPERTY_VALUE));
    }

    @Test(dependsOnMethods = {"testCreateActionWithPasswordCredentialMissingTokenEndpoint"})
    public void testCreateActionWithPasswordCredentialMissingUsername() {

        assertPasswordCredentialMissingProperty(buildPasswordCredentialProperties(
                TEST_CLIENT_ID_AUTH_PROPERTY_VALUE, TEST_CLIENT_SECRET_AUTH_PROPERTY_VALUE,
                TEST_TOKEN_ENDPOINT_AUTH_PROPERTY_VALUE, null, TEST_PASSWORD_AUTH_PROPERTY_VALUE));
    }

    @Test(dependsOnMethods = {"testCreateActionWithPasswordCredentialMissingUsername"})
    public void testCreateActionWithPasswordCredentialMissingPassword() {

        assertPasswordCredentialMissingProperty(buildPasswordCredentialProperties(
                TEST_CLIENT_ID_AUTH_PROPERTY_VALUE, TEST_CLIENT_SECRET_AUTH_PROPERTY_VALUE,
                TEST_TOKEN_ENDPOINT_AUTH_PROPERTY_VALUE, TEST_USERNAME_AUTH_PROPERTY_VALUE, null));
    }

    @Test(dependsOnMethods = {"testCreateActionWithPasswordCredentialMissingPassword"})
    public void testCreateActionWithPasswordCredentialEmptyClientId() {

        assertPasswordCredentialEmptyProperty(buildPasswordCredentialProperties(
                "", TEST_CLIENT_SECRET_AUTH_PROPERTY_VALUE, TEST_TOKEN_ENDPOINT_AUTH_PROPERTY_VALUE,
                TEST_USERNAME_AUTH_PROPERTY_VALUE, TEST_PASSWORD_AUTH_PROPERTY_VALUE));
    }

    @Test(dependsOnMethods = {"testCreateActionWithPasswordCredentialEmptyClientId"})
    public void testCreateActionWithPasswordCredentialEmptyClientSecret() {

        assertPasswordCredentialEmptyProperty(buildPasswordCredentialProperties(
                TEST_CLIENT_ID_AUTH_PROPERTY_VALUE, "", TEST_TOKEN_ENDPOINT_AUTH_PROPERTY_VALUE,
                TEST_USERNAME_AUTH_PROPERTY_VALUE, TEST_PASSWORD_AUTH_PROPERTY_VALUE));
    }

    @Test(dependsOnMethods = {"testCreateActionWithPasswordCredentialEmptyClientSecret"})
    public void testCreateActionWithPasswordCredentialEmptyTokenEndpoint() {

        assertPasswordCredentialEmptyProperty(buildPasswordCredentialProperties(
                TEST_CLIENT_ID_AUTH_PROPERTY_VALUE, TEST_CLIENT_SECRET_AUTH_PROPERTY_VALUE, "",
                TEST_USERNAME_AUTH_PROPERTY_VALUE, TEST_PASSWORD_AUTH_PROPERTY_VALUE));
    }

    @Test(dependsOnMethods = {"testCreateActionWithPasswordCredentialEmptyTokenEndpoint"})
    public void testCreateActionWithPasswordCredentialEmptyUsername() {

        assertPasswordCredentialEmptyProperty(buildPasswordCredentialProperties(
                TEST_CLIENT_ID_AUTH_PROPERTY_VALUE, TEST_CLIENT_SECRET_AUTH_PROPERTY_VALUE,
                TEST_TOKEN_ENDPOINT_AUTH_PROPERTY_VALUE, "", TEST_PASSWORD_AUTH_PROPERTY_VALUE));
    }

    @Test(dependsOnMethods = {"testCreateActionWithPasswordCredentialEmptyUsername"})
    public void testCreateActionWithPasswordCredentialEmptyPassword() {

        assertPasswordCredentialEmptyProperty(buildPasswordCredentialProperties(
                TEST_CLIENT_ID_AUTH_PROPERTY_VALUE, TEST_CLIENT_SECRET_AUTH_PROPERTY_VALUE,
                TEST_TOKEN_ENDPOINT_AUTH_PROPERTY_VALUE, TEST_USERNAME_AUTH_PROPERTY_VALUE, ""));
    }

    private HashMap<String, Object> buildPasswordCredentialProperties(String clientId, String clientSecret,
                                                                     String tokenEndpoint, String username,
                                                                     String password) {

        HashMap<String, Object> properties = new HashMap<>();
        if (clientId != null) {
            properties.put(TEST_CLIENT_ID_AUTH_PROPERTY, clientId);
        }
        if (clientSecret != null) {
            properties.put(TEST_CLIENT_SECRET_AUTH_PROPERTY, clientSecret);
        }
        if (tokenEndpoint != null) {
            properties.put(TEST_TOKEN_ENDPOINT_AUTH_PROPERTY, tokenEndpoint);
        }
        if (username != null) {
            properties.put(TEST_USERNAME_AUTH_PROPERTY, username);
        }
        if (password != null) {
            properties.put(TEST_PASSWORD_AUTH_PROPERTY, password);
        }
        return properties;
    }

    private void assertPasswordCredentialMissingProperty(HashMap<String, Object> properties) {

        ActionModel passwordCredentialAction = new ActionModel()
                .name(TEST_ACTION_NAME)
                .description(TEST_ACTION_DESCRIPTION)
                .endpoint(new Endpoint()
                        .uri(TEST_ENDPOINT_URI)
                        .authentication(new AuthenticationType()
                                .type(AuthenticationType.TypeEnum.PASSWORD_CREDENTIAL)
                                .properties(properties)));

        Response responseOfPost = getResponseOfPost(ACTION_MANAGEMENT_API_BASE_PATH +
                PRE_ISSUE_ID_TOKEN_PATH, toJSONString(passwordCredentialAction));
        responseOfPost.then()
                .log().ifValidationFails()
                .assertThat().statusCode(HttpStatus.SC_BAD_REQUEST)
                .body("description", equalTo("Required authentication properties are not " +
                        "provided or invalid."));
    }

    private void assertPasswordCredentialEmptyProperty(HashMap<String, Object> properties) {

        ActionModel passwordCredentialAction = new ActionModel()
                .name(TEST_ACTION_NAME)
                .description(TEST_ACTION_DESCRIPTION)
                .endpoint(new Endpoint()
                        .uri(TEST_ENDPOINT_URI)
                        .authentication(new AuthenticationType()
                                .type(AuthenticationType.TypeEnum.PASSWORD_CREDENTIAL)
                                .properties(properties)));

        Response responseOfPost = getResponseOfPost(ACTION_MANAGEMENT_API_BASE_PATH +
                PRE_ISSUE_ID_TOKEN_PATH, toJSONString(passwordCredentialAction));
        responseOfPost.then()
                .log().ifValidationFails()
                .assertThat().statusCode(HttpStatus.SC_BAD_REQUEST)
                .body("description", equalTo("Authentication property values cannot be empty."));
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

    private String createActionWithRule() {

        ORRule rule = new ORRule()
                .condition(ORRule.ConditionEnum.OR)
                .addRulesItem(new ANDRule().condition(ANDRule.ConditionEnum.AND)
                        .addExpressionsItem(new Expression().field("application").operator("equals")
                                .value("855bb864-2839-4bdf-aabd-4cc635b6faba")));

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
                                }})))
                .rule(rule);

        String body = toJSONString(action);
        Response responseOfPost = getResponseOfPost(ACTION_MANAGEMENT_API_BASE_PATH +
                PRE_ISSUE_ID_TOKEN_PATH, body);
        responseOfPost.then().assertThat().statusCode(HttpStatus.SC_CREATED);

        return responseOfPost.getBody().jsonPath().getString("id");
    }
}


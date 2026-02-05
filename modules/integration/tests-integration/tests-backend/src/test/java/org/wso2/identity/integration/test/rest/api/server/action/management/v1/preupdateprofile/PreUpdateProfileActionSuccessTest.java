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

package org.wso2.identity.integration.test.rest.api.server.action.management.v1.preupdateprofile;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.apache.http.HttpStatus;
import org.testng.annotations.*;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.identity.integration.test.rest.api.server.action.management.v1.common.model.ActionModel;
import org.wso2.identity.integration.test.rest.api.server.action.management.v1.common.model.ActionUpdateModel;
import org.wso2.identity.integration.test.rest.api.server.action.management.v1.common.model.AuthenticationType;
import org.wso2.identity.integration.test.rest.api.server.action.management.v1.common.model.Endpoint;
import org.wso2.identity.integration.test.rest.api.server.action.management.v1.common.model.EndpointUpdateModel;
import org.wso2.identity.integration.test.rest.api.server.action.management.v1.preupdateprofile.model.PreUpdateProfileActionModel;
import org.wso2.identity.integration.test.rest.api.server.action.management.v1.preupdateprofile.model.PreUpdateProfileActionUpdateModel;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasKey;

public class PreUpdateProfileActionSuccessTest extends PreUpdateProfileTestBase {

    private static ActionModel action;
    private static String testActionId;

    @Factory(dataProvider = "restAPIUserConfigProvider")
    public PreUpdateProfileActionSuccessTest(TestUserMode userMode) throws Exception {

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

        action = new PreUpdateProfileActionModel()
                .attributes(TEST_ATTRIBUTES)
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
                PRE_UPDATE_PROFILE_PATH, body);
        responseOfPost.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .body("id", notNullValue())
                .body("name", equalTo(TEST_ACTION_NAME))
                .body("description", equalTo(TEST_ACTION_DESCRIPTION))
                .body("version", equalTo(TEST_ACTION_VERSION))
                .body("status", equalTo(TEST_ACTION_INACTIVE_STATUS))
                .body("endpoint.uri", equalTo(TEST_ENDPOINT_URI))
                .body("endpoint.authentication.type", equalTo(AuthenticationType.TypeEnum.BASIC.toString()))
                .body("endpoint.authentication", not(hasKey(TEST_PROPERTIES_AUTH_ATTRIBUTE)))
                .body("attributes", containsInAnyOrder(TEST_ATTRIBUTES.toArray()));

        testActionId = responseOfPost.getBody().jsonPath().getString("id");
    }

    @Test(dependsOnMethods = {"testCreateAction"})
    public void testGetActionsByActionType() {

        Response responseOfGet = getResponseOfGet(ACTION_MANAGEMENT_API_BASE_PATH +
                PRE_UPDATE_PROFILE_PATH);
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
                                PRE_UPDATE_PROFILE_PATH + "/" + testActionId))
                .body("find { it.id == '" + testActionId + "' }.links.find { it.rel == 'self' }.method",
                        equalTo("GET"));
    }

    @Test(dependsOnMethods = {"testGetActionsByActionType"})
    public void testGetActionByActionId() {

        Response responseOfGet = getResponseOfGet(ACTION_MANAGEMENT_API_BASE_PATH +
                PRE_UPDATE_PROFILE_PATH+ "/" + testActionId);
        responseOfGet.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("id", equalTo(testActionId))
                .body("name", equalTo(TEST_ACTION_NAME))
                .body("description", equalTo(TEST_ACTION_DESCRIPTION))
                .body("version", equalTo(TEST_ACTION_VERSION))
                .body("status", equalTo(TEST_ACTION_INACTIVE_STATUS))
                .body("endpoint.uri", equalTo(TEST_ENDPOINT_URI))
                .body("endpoint.authentication.type", equalTo(AuthenticationType.TypeEnum.BASIC.toString()))
                .body("endpoint.authentication", not(hasKey(TEST_PROPERTIES_AUTH_ATTRIBUTE)))
                .body("attributes", containsInAnyOrder(TEST_ATTRIBUTES.toArray()));
    }

    @Test(dependsOnMethods = {"testGetActionByActionId"})
    public void testGetActionTypes() {

        Response responseOfGet = getResponseOfGet(ACTION_MANAGEMENT_API_BASE_PATH + TYPES_API_PATH);
        responseOfGet.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body( "find { it.type == '" + PRE_UPDATE_PROFILE_ACTION_TYPE + "' }.displayName",
                        notNullValue())
                .body( "find { it.type == '" + PRE_UPDATE_PROFILE_ACTION_TYPE + "' }.description",
                        notNullValue())
                .body( "find { it.type == '" + PRE_UPDATE_PROFILE_ACTION_TYPE + "' }.count",
                        equalTo(1))
                .body( "find { it.type == '" + PRE_UPDATE_PROFILE_ACTION_TYPE + "' }.self", notNullValue());
    }

    @Test(dependsOnMethods = {"testGetActionTypes"})
    public void testUpdateActionUpdatingAllProperties() {

        ActionUpdateModel actionUpdateModel = new PreUpdateProfileActionUpdateModel()
                .attributes(TEST_UPDATED_ATTRIBUTES)
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
                PRE_UPDATE_PROFILE_PATH + "/" + testActionId, body);

        responseOfPatch.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("id", equalTo(testActionId))
                .body("name", equalTo(TEST_ACTION_UPDATED_NAME))
                .body("description", equalTo(TEST_ACTION_UPDATED_DESCRIPTION))
                .body("version", equalTo(TEST_ACTION_VERSION))
                .body("status", equalTo(TEST_ACTION_INACTIVE_STATUS))
                .body("endpoint.uri", equalTo(TEST_UPDATED_ENDPOINT_URI))
                .body("endpoint.authentication.type", equalTo(AuthenticationType.TypeEnum.API_KEY.toString()))
                .body("endpoint.authentication", not(hasKey(TEST_PROPERTIES_AUTH_ATTRIBUTE)))
                .body("attributes", containsInAnyOrder(TEST_UPDATED_ATTRIBUTES.toArray()));
    }

    @Test(dependsOnMethods = {"testUpdateActionUpdatingAllProperties"})
    public void testUpdateActionUpdatingName() {

        ActionUpdateModel actionUpdateModel = new PreUpdateProfileActionUpdateModel()
                .name(TEST_ACTION_NAME);

        String body = toJSONString(actionUpdateModel);
        Response responseOfPatch = getResponseOfPatch(ACTION_MANAGEMENT_API_BASE_PATH +
                PRE_UPDATE_PROFILE_PATH + "/" + testActionId, body);

        responseOfPatch.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("id", equalTo(testActionId))
                .body("name", equalTo(TEST_ACTION_NAME))
                .body("description", equalTo(TEST_ACTION_UPDATED_DESCRIPTION))
                .body("version", equalTo(TEST_ACTION_VERSION))
                .body("status", equalTo(TEST_ACTION_INACTIVE_STATUS))
                .body("endpoint.uri", equalTo(TEST_UPDATED_ENDPOINT_URI))
                .body("endpoint.authentication.type", equalTo(AuthenticationType.TypeEnum.API_KEY.toString()))
                .body("endpoint.authentication", not(hasKey(TEST_PROPERTIES_AUTH_ATTRIBUTE)))
                .body("attributes", containsInAnyOrder(TEST_UPDATED_ATTRIBUTES.toArray()));
    }

    @Test(dependsOnMethods = {"testUpdateActionUpdatingName"})
    public void testUpdateActionUpdatingEndpoint() {

        ActionUpdateModel actionUpdateModel = new PreUpdateProfileActionUpdateModel()
                .endpoint(new EndpointUpdateModel().uri(TEST_ENDPOINT_URI));

        String body = toJSONString(actionUpdateModel);
        Response responseOfPatch = getResponseOfPatch(ACTION_MANAGEMENT_API_BASE_PATH +
                PRE_UPDATE_PROFILE_PATH + "/" + testActionId, body);

        responseOfPatch.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("id", equalTo(testActionId))
                .body("name", equalTo(TEST_ACTION_NAME))
                .body("description", equalTo(TEST_ACTION_UPDATED_DESCRIPTION))
                .body("version", equalTo(TEST_ACTION_VERSION))
                .body("status", equalTo(TEST_ACTION_INACTIVE_STATUS))
                .body("endpoint.uri", equalTo(TEST_ENDPOINT_URI))
                .body("endpoint.authentication.type", equalTo(AuthenticationType.TypeEnum.API_KEY.toString()))
                .body("endpoint.authentication", not(hasKey(TEST_PROPERTIES_AUTH_ATTRIBUTE)))
                .body("attributes", containsInAnyOrder(TEST_UPDATED_ATTRIBUTES.toArray()));
    }

    @Test(dependsOnMethods = {"testUpdateActionUpdatingEndpoint"})
    public void testUpdateActionUpdatingAuthentication() {

        ActionUpdateModel actionUpdateModel = new PreUpdateProfileActionUpdateModel()
                .endpoint(new EndpointUpdateModel()
                        .authentication(new AuthenticationType()
                                .type(AuthenticationType.TypeEnum.BEARER)
                                .properties(new HashMap<String, Object>() {{
                                    put(TEST_ACCESS_TOKEN_AUTH_PROPERTY, TEST_ACCESS_TOKEN_AUTH_PROPERTY_VALUE);
                                }})));

        String body = toJSONString(actionUpdateModel);
        Response responseOfPatch = getResponseOfPatch(ACTION_MANAGEMENT_API_BASE_PATH +
                PRE_UPDATE_PROFILE_PATH + "/" + testActionId, body);

        responseOfPatch.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("id", equalTo(testActionId))
                .body("name", equalTo(TEST_ACTION_NAME))
                .body("description", equalTo(TEST_ACTION_UPDATED_DESCRIPTION))
                .body("version", equalTo(TEST_ACTION_VERSION))
                .body("status", equalTo(TEST_ACTION_INACTIVE_STATUS))
                .body("endpoint.uri", equalTo(TEST_ENDPOINT_URI))
                .body("endpoint.authentication.type", equalTo(AuthenticationType.TypeEnum.BEARER.toString()))
                .body("endpoint.authentication", not(hasKey(TEST_PROPERTIES_AUTH_ATTRIBUTE)))
                .body("attributes", containsInAnyOrder(TEST_UPDATED_ATTRIBUTES.toArray()));
    }

    @Test(dependsOnMethods = {"testUpdateActionUpdatingAuthentication"})
    public void testUpdateActionUpdatingAuthenticationProperties() {

        ActionUpdateModel actionUpdateModel = new PreUpdateProfileActionUpdateModel()
                .endpoint(new EndpointUpdateModel()
                        .authentication(new AuthenticationType()
                                .type(AuthenticationType.TypeEnum.BEARER)
                                .properties(new HashMap<String, Object>() {{
                                    put(TEST_ACCESS_TOKEN_AUTH_PROPERTY, TEST_UPDATED_ACCESS_TOKEN_AUTH_PROPERTY_VALUE);
                                }})));

        String body = toJSONString(actionUpdateModel);
        Response responseOfPatch = getResponseOfPatch(ACTION_MANAGEMENT_API_BASE_PATH +
                PRE_UPDATE_PROFILE_PATH + "/" + testActionId, body);

        responseOfPatch.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("id", equalTo(testActionId))
                .body("name", equalTo(TEST_ACTION_NAME))
                .body("description", equalTo(TEST_ACTION_UPDATED_DESCRIPTION))
                .body("version", equalTo(TEST_ACTION_VERSION))
                .body("status", equalTo(TEST_ACTION_INACTIVE_STATUS))
                .body("endpoint.uri", equalTo(TEST_ENDPOINT_URI))
                .body("endpoint.authentication.type", equalTo(AuthenticationType.TypeEnum.BEARER.toString()))
                .body("endpoint.authentication", not(hasKey(TEST_PROPERTIES_AUTH_ATTRIBUTE)))
                .body("attributes", containsInAnyOrder(TEST_UPDATED_ATTRIBUTES.toArray()));
    }

    @Test(dependsOnMethods = {"testUpdateActionUpdatingAuthenticationProperties"})
    public void testUpdateActionUpdatingEndpointUriAndAuthentication() {

        ActionUpdateModel actionUpdateModel = new PreUpdateProfileActionUpdateModel()
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
                PRE_UPDATE_PROFILE_PATH + "/" + testActionId, body);

        responseOfPatch.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("id", equalTo(testActionId))
                .body("name", equalTo(TEST_ACTION_NAME))
                .body("description", equalTo(TEST_ACTION_UPDATED_DESCRIPTION))
                .body("version", equalTo(TEST_ACTION_VERSION))
                .body("status", equalTo(TEST_ACTION_INACTIVE_STATUS))
                .body("endpoint.uri", equalTo(TEST_UPDATED_ENDPOINT_URI))
                .body("endpoint.authentication.type", equalTo(AuthenticationType.TypeEnum.BASIC.toString()))
                .body("endpoint.authentication", not(hasKey(TEST_PROPERTIES_AUTH_ATTRIBUTE)))
                .body("attributes", containsInAnyOrder(TEST_UPDATED_ATTRIBUTES.toArray()));
    }

    @Test(dependsOnMethods = {"testUpdateActionUpdatingEndpointUriAndAuthentication"})
    public void testUpdateActionUpdatingEndpointUriAndAuthenticationProperties() {

        ActionUpdateModel actionUpdateModel = new PreUpdateProfileActionUpdateModel()
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
                PRE_UPDATE_PROFILE_PATH + "/" + testActionId, body);

        responseOfPatch.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("id", equalTo(testActionId))
                .body("name", equalTo(TEST_ACTION_NAME))
                .body("description", equalTo(TEST_ACTION_UPDATED_DESCRIPTION))
                .body("version", equalTo(TEST_ACTION_VERSION))
                .body("status", equalTo(TEST_ACTION_INACTIVE_STATUS))
                .body("endpoint.uri", equalTo(TEST_ENDPOINT_URI))
                .body("endpoint.authentication.type", equalTo(AuthenticationType.TypeEnum.BASIC.toString()))
                .body("endpoint.authentication", not(hasKey(TEST_PROPERTIES_AUTH_ATTRIBUTE)))
                .body("attributes", containsInAnyOrder(TEST_UPDATED_ATTRIBUTES.toArray()));
    }

    @Test(dependsOnMethods = {"testUpdateActionUpdatingEndpointUriAndAuthenticationProperties"})
    public void testUpdateActionUpdatingAttributes() {

        ActionUpdateModel actionUpdateModel = new PreUpdateProfileActionUpdateModel()
                .attributes(TEST_ATTRIBUTES);

        String body = toJSONString(actionUpdateModel);
        Response responseOfPatch = getResponseOfPatch(ACTION_MANAGEMENT_API_BASE_PATH +
                PRE_UPDATE_PROFILE_PATH + "/" + testActionId, body);

        responseOfPatch.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("id", equalTo(testActionId))
                .body("name", equalTo(TEST_ACTION_NAME))
                .body("description", equalTo(TEST_ACTION_UPDATED_DESCRIPTION))
                .body("version", equalTo(TEST_ACTION_VERSION))
                .body("status", equalTo(TEST_ACTION_INACTIVE_STATUS))
                .body("endpoint.uri", equalTo(TEST_ENDPOINT_URI))
                .body("endpoint.authentication.type", equalTo(AuthenticationType.TypeEnum.BASIC.toString()))
                .body("endpoint.authentication", not(hasKey(TEST_PROPERTIES_AUTH_ATTRIBUTE)))
                .body("attributes", containsInAnyOrder(TEST_ATTRIBUTES.toArray()));
    }

    @Test(dependsOnMethods = {"testUpdateActionUpdatingAttributes"})
    public void testUpdateActionUpdatingWithDuplicatedAttributes() {

        ActionUpdateModel actionUpdateModel = new PreUpdateProfileActionUpdateModel()
                .attributes(TEST_DUPLICATED_ATTRIBUTES);

        String body = toJSONString(actionUpdateModel);
        Response responseOfPatch = getResponseOfPatch(ACTION_MANAGEMENT_API_BASE_PATH +
                PRE_UPDATE_PROFILE_PATH + "/" + testActionId, body);

        responseOfPatch.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("id", equalTo(testActionId))
                .body("name", equalTo(TEST_ACTION_NAME))
                .body("description", equalTo(TEST_ACTION_UPDATED_DESCRIPTION))
                .body("version", equalTo(TEST_ACTION_VERSION))
                .body("status", equalTo(TEST_ACTION_INACTIVE_STATUS))
                .body("endpoint.uri", equalTo(TEST_ENDPOINT_URI))
                .body("endpoint.authentication.type", equalTo(AuthenticationType.TypeEnum.BASIC.toString()))
                .body("endpoint.authentication", not(hasKey(TEST_PROPERTIES_AUTH_ATTRIBUTE)))
                .body("attributes", equalTo(Collections.singletonList(TEST_DUPLICATED_ATTRIBUTES.get(0))));
    }

    @Test(dependsOnMethods = {"testUpdateActionUpdatingWithDuplicatedAttributes"})
    public void testActivateAction() {

        getResponseOfPost(ACTION_MANAGEMENT_API_BASE_PATH + PRE_UPDATE_PROFILE_PATH +
                "/" + testActionId + ACTION_ACTIVATE_PATH, "")
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("id", equalTo(testActionId))
                .body("name", equalTo(TEST_ACTION_NAME))
                .body("description", equalTo(TEST_ACTION_UPDATED_DESCRIPTION))
                .body("version", equalTo(TEST_ACTION_VERSION))
                .body("status", equalTo(TEST_ACTION_ACTIVE_STATUS));
    }

    @Test(dependsOnMethods = {"testActivateAction"})
    public void testDeactivateAction() {

        getResponseOfPost(ACTION_MANAGEMENT_API_BASE_PATH + PRE_UPDATE_PROFILE_PATH +
                "/" + testActionId + ACTION_DEACTIVATE_PATH, "")
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("id", equalTo(testActionId))
                .body("name", equalTo(TEST_ACTION_NAME))
                .body("description", equalTo(TEST_ACTION_UPDATED_DESCRIPTION))
                .body("version", equalTo(TEST_ACTION_VERSION))
                .body("status", equalTo(TEST_ACTION_INACTIVE_STATUS));
    }

    @Test(dependsOnMethods = {"testDeactivateAction"})
    public void testDeleteAction() {

        getResponseOfDelete(ACTION_MANAGEMENT_API_BASE_PATH + PRE_UPDATE_PROFILE_PATH + "/" +
                testActionId).then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NO_CONTENT);

        Response responseOfGet = getResponseOfGet(ACTION_MANAGEMENT_API_BASE_PATH + TYPES_API_PATH);
        responseOfGet.then()
                .log().ifValidationFails()
                .assertThat().statusCode(HttpStatus.SC_OK)
                .body( "find { it.type == '" + PRE_UPDATE_PROFILE_ACTION_TYPE + "' }.count",
                        equalTo(0));
    }

    @Test(dependsOnMethods = {"testDeleteAction"})
    public void testCreateActionWithoutAttributes() {

        action = new PreUpdateProfileActionModel()
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
                PRE_UPDATE_PROFILE_PATH, body);
        responseOfPost.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .body("id", notNullValue())
                .body("name", equalTo(TEST_ACTION_NAME))
                .body("description", equalTo(TEST_ACTION_DESCRIPTION))
                .body("version", equalTo(TEST_ACTION_VERSION))
                .body("status", equalTo(TEST_ACTION_INACTIVE_STATUS))
                .body("endpoint.uri", equalTo(TEST_ENDPOINT_URI))
                .body("endpoint.authentication.type", equalTo(AuthenticationType.TypeEnum.BASIC.toString()))
                .body("endpoint.authentication", not(hasKey(TEST_PROPERTIES_AUTH_ATTRIBUTE)))
                .body("attributes", not(hasKey(ATTRIBUTES)));

        deleteAction(PRE_UPDATE_PROFILE_PATH, responseOfPost.getBody().jsonPath().getString("id"));
    }

    @Test(dependsOnMethods = {"testCreateActionWithoutAttributes"})
    public void testDeleteNonExistingAction() {

        getResponseOfDelete(ACTION_MANAGEMENT_API_BASE_PATH + PRE_UPDATE_PROFILE_PATH +
                "/" + TEST_ACTION_INVALID_ID)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NO_CONTENT);
    }

    @Test(dependsOnMethods = {"testDeleteNonExistingAction"})
    public void testCreateActionWithExtraEndpointAuthProperties() {

        action = new PreUpdateProfileActionModel()
                .attributes(TEST_ATTRIBUTES)
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
                PRE_UPDATE_PROFILE_PATH, body);
        responseOfPost.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .body("endpoint.authentication.type", equalTo(AuthenticationType.TypeEnum.BASIC.toString()));

        deleteAction(PRE_UPDATE_PROFILE_PATH , responseOfPost.getBody().jsonPath().getString("id"));
    }

    @Test(dependsOnMethods = {"testCreateActionWithExtraEndpointAuthProperties"})
    public void testCreateActionWithDuplicatedAttributes() {

        action = new PreUpdateProfileActionModel()
                .attributes(TEST_DUPLICATED_ATTRIBUTES)
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
                PRE_UPDATE_PROFILE_PATH, body);
        responseOfPost.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .body("id", notNullValue())
                .body("name", equalTo(TEST_ACTION_NAME))
                .body("description", equalTo(TEST_ACTION_DESCRIPTION))
                .body("version", equalTo(TEST_ACTION_VERSION))
                .body("status", equalTo(TEST_ACTION_INACTIVE_STATUS))
                .body("endpoint.uri", equalTo(TEST_ENDPOINT_URI))
                .body("endpoint.authentication.type", equalTo(AuthenticationType.TypeEnum.BASIC.toString()))
                .body("endpoint.authentication", not(hasKey(TEST_PROPERTIES_AUTH_ATTRIBUTE)))
                .body("attributes", equalTo(Collections.singletonList(TEST_DUPLICATED_ATTRIBUTES.get(0))));

        deleteAction(PRE_UPDATE_PROFILE_PATH , responseOfPost.getBody().jsonPath().getString("id"));
    }
}

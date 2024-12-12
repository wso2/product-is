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

package org.wso2.identity.integration.test.rest.api.server.authenticator.management.v1;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.identity.api.server.authenticators.v1.model.AuthenticationType;
import org.wso2.carbon.identity.api.server.authenticators.v1.model.UserDefinedLocalAuthenticatorCreation;
import org.wso2.carbon.identity.api.server.authenticators.v1.model.UserDefinedLocalAuthenticatorUpdate;
import org.wso2.carbon.identity.application.common.model.UserDefinedAuthenticatorEndpointConfig;
import org.wso2.carbon.identity.application.common.model.UserDefinedLocalAuthenticatorConfig;
import org.wso2.carbon.identity.base.AuthenticatorPropertyConstants;
import org.wso2.identity.integration.test.rest.api.server.authenticator.management.v1.util.UserDefinedLocalAuthenticatorPayload;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;

/**
 * Tests for happy paths of the authentication Management REST API.
 */
public class AuthenticatorSuccessTest extends AuthenticatorTestBase {

    private UserDefinedLocalAuthenticatorConfig testAuthenticatorConfig;
    private UserDefinedLocalAuthenticatorCreation creationPayload;
    private UserDefinedLocalAuthenticatorUpdate updatePayload;

    private final String AUTHENTICATOR_NAME = "customAuthenticator";
    private final String customIdPId = Base64.getUrlEncoder().withoutPadding().encodeToString(
            AUTHENTICATOR_NAME.getBytes(StandardCharsets.UTF_8));
    private final String AUTHENTICATOR_DISPLAY_NAME = "ABC custom authenticator";
    private final String AUTHENTICATOR_ENDPOINT_URI = "https://test.com/authenticate";
    private final String UPDATE_VALUE_POSTFIX = "Updated";

    @Factory(dataProvider = "restAPIUserConfigProvider")
    public AuthenticatorSuccessTest(TestUserMode userMode) throws Exception {

        super.init(userMode);
        this.context = isServer;
        this.authenticatingUserName = context.getContextTenant().getTenantAdmin().getUserName();
        this.authenticatingCredential = context.getContextTenant().getTenantAdmin().getPassword();
        this.tenant = context.getContextTenant().getDomain();
    }

    @BeforeClass(alwaysRun = true)
    public void init() throws IOException, JSONException {

        super.testInit(API_VERSION, swaggerDefinition, tenant);
        testAuthenticatorConfig = createBaseUserDefinedLocalAuthenticator(
                AuthenticatorPropertyConstants.AuthenticationType.IDENTIFICATION);
    }

    @AfterClass(alwaysRun = true)
    public void testConclude() {

        super.conclude();
    }

    @BeforeMethod(alwaysRun = true)
    public void testInit() {

        RestAssured.basePath = basePath;
        creationPayload = UserDefinedLocalAuthenticatorPayload
                .getBasedUserDefinedLocalAuthenticatorCreation(testAuthenticatorConfig);
        updatePayload = UserDefinedLocalAuthenticatorPayload
                .getBasedUserDefinedLocalAuthenticatorUpdate(testAuthenticatorConfig);
    }

    @AfterMethod(alwaysRun = true)
    public void testFinish() {

        RestAssured.basePath = StringUtils.EMPTY;
    }

    @DataProvider(name = "restAPIUserConfigProvider")
    public static Object[][] restAPIUserConfigProvider() {

        return new Object[][]{
                {TestUserMode.SUPER_TENANT_ADMIN},
                {TestUserMode.TENANT_ADMIN}
        };
    }

    @Test
    public void getAuthenticators() throws JSONException {

        Response response = getResponseOfGet(AUTHENTICATOR_API_BASE_PATH);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);

        JSONArray jsonArray = new JSONArray(response.body().asString());
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject authenticator = jsonArray.getJSONObject(i);
            Assert.assertTrue(authenticator.has("id"));
            Assert.assertTrue(authenticator.has("name"));
            Assert.assertTrue(authenticator.has("displayName"));
            Assert.assertTrue(authenticator.has("type"));
            Assert.assertEquals(authenticator.getString("definedBy"), "SYSTEM");
        }
    }

    @Test(dependsOnMethods = {"getAuthenticators"})
    public void testCreateUserDefinedLocalAuthenticator() throws JsonProcessingException {

        String body = UserDefinedLocalAuthenticatorPayload.convertToJasonPayload(creationPayload);
        Response response = getResponseOfPost(AUTHENTICATOR_CUSTOM_API_BASE_PATH, body);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .header(HttpHeaders.LOCATION, notNullValue())
                .body("id", equalTo(customIdPId))
                .body("name", equalTo(AUTHENTICATOR_NAME))
                .body("displayName", equalTo(AUTHENTICATOR_DISPLAY_NAME))
                .body("type", equalTo("LOCAL"))
                .body("definedBy", equalTo("USER"))
                .body("isEnabled", equalTo(true))
                .body("self", equalTo(getTenantedRelativePath(
                        AUTHENTICATOR_CONFIG_API_BASE_PATH + customIdPId, tenant)));
    }

    @Test(dependsOnMethods = {"testCreateUserDefinedLocalAuthenticator"})
    public void testUpdateUserDefinedLocalAuthenticator() throws JsonProcessingException {

        updatePayload.displayName(AUTHENTICATOR_DISPLAY_NAME + UPDATE_VALUE_POSTFIX);
        updatePayload.isEnabled(false);
        updatePayload.getEndpoint().uri(AUTHENTICATOR_ENDPOINT_URI + UPDATE_VALUE_POSTFIX);
        String body = UserDefinedLocalAuthenticatorPayload.convertToJasonPayload(updatePayload);
        Response response = getResponseOfPutWithNoFilter(AUTHENTICATOR_CUSTOM_API_BASE_PATH + PATH_SEPARATOR +
                customIdPId, body);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("id", equalTo(customIdPId))
                .body("name", equalTo(AUTHENTICATOR_NAME))
                .body("displayName", equalTo(AUTHENTICATOR_DISPLAY_NAME + UPDATE_VALUE_POSTFIX))
                .body("type", equalTo("LOCAL"))
                .body("definedBy", equalTo("USER"))
                .body("isEnabled", equalTo(false))
                .body("self", equalTo(getTenantedRelativePath(
                        AUTHENTICATOR_CONFIG_API_BASE_PATH + customIdPId, tenant)));
    }

    @Test(dependsOnMethods = {"testUpdateUserDefinedLocalAuthenticator"})
    public void testDeleteUserDefinedLocalAuthenticator() throws JsonProcessingException {

        Response response = getResponseOfDelete(AUTHENTICATOR_CUSTOM_API_BASE_PATH + PATH_SEPARATOR
                + customIdPId);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NO_CONTENT);
    }

    private UserDefinedLocalAuthenticatorConfig createBaseUserDefinedLocalAuthenticator(
            AuthenticatorPropertyConstants.AuthenticationType type) {

        UserDefinedLocalAuthenticatorConfig config = new UserDefinedLocalAuthenticatorConfig(type);
        config.setName(AUTHENTICATOR_NAME);
        config.setDisplayName(AUTHENTICATOR_DISPLAY_NAME);
        config.setEnabled(true);

        UserDefinedAuthenticatorEndpointConfig.UserDefinedAuthenticatorEndpointConfigBuilder endpointConfig =
                new UserDefinedAuthenticatorEndpointConfig.UserDefinedAuthenticatorEndpointConfigBuilder();
        endpointConfig.uri(AUTHENTICATOR_ENDPOINT_URI);
        endpointConfig.authenticationType(String.valueOf(AuthenticationType.TypeEnum.BASIC));
        endpointConfig.authenticationProperties(Map.of("username", "admin", "password", "admin"));
        config.setEndpointConfig(endpointConfig.build());

        return config;
    }
}


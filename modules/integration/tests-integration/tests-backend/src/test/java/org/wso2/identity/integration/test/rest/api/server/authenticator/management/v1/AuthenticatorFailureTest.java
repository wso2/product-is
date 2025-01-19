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
import org.apache.http.HttpStatus;
import org.json.JSONException;
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
import org.wso2.carbon.identity.application.common.model.UserDefinedLocalAuthenticatorConfig;
import org.wso2.carbon.identity.base.AuthenticatorPropertyConstants;
import org.wso2.identity.integration.test.rest.api.server.authenticator.management.v1.util.UserDefinedLocalAuthenticatorPayload;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.equalTo;

/**
 * Test authenticator failure scenarios.
 */
public class AuthenticatorFailureTest extends AuthenticatorTestBase {

    private UserDefinedLocalAuthenticatorConfig testAuthenticatorConfig;
    private UserDefinedLocalAuthenticatorCreation creationPayload;
    private UserDefinedLocalAuthenticatorUpdate updatePayload;

    @Factory(dataProvider = "restAPIUserConfigProvider")
    public AuthenticatorFailureTest(TestUserMode userMode) throws Exception {

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
        addUserDefinedAuthenticatorToIS();
    }

    @AfterClass(alwaysRun = true)
    public void testConclude() {

        super.conclude();
        getResponseOfDelete(AUTHENTICATOR_CUSTOM_API_BASE_PATH + PATH_SEPARATOR + customIdPId);
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

    @Test(priority = 1)
    public void createUserDefinedLocalAuthenticatorInvalidName() throws JsonProcessingException {

        creationPayload.setName("invalid@name");
        String body = UserDefinedLocalAuthenticatorPayload.convertToJasonPayload(creationPayload);
        Response response = getResponseOfPost(AUTHENTICATOR_CUSTOM_API_BASE_PATH, body);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body("code", equalTo("AUT-60014"))
                .body("message", equalTo("Authenticator name is invalid."))
                .body("description", equalTo("The provided authenticator name invalid@name is not in the " +
                        "expected format ^[a-zA-Z0-9][a-zA-Z0-9-_]*$."));
    }

    @Test(priority = 2)
    public void createUserDefinedLocalAuthenticatorMissingEndpointProperty() throws JsonProcessingException {

        creationPayload.getEndpoint().getAuthentication().getProperties().remove("username");
        String body = UserDefinedLocalAuthenticatorPayload.convertToJasonPayload(creationPayload);
        Response response = getResponseOfPost(AUTHENTICATOR_CUSTOM_API_BASE_PATH, body);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body("code", equalTo("AUT-60011"))
                .body("message", equalTo("Invalid endpoint configuration provided."))
                .body("description", equalTo("The property username must be provided as an authentication " +
                        "property for the BASIC authentication type."));
    }

    @Test(priority = 3)
    public void createUserDefinedLocalAuthenticatorEmptyDisplayName() throws JsonProcessingException {

        creationPayload.setDisplayName("");
        String body = UserDefinedLocalAuthenticatorPayload.convertToJasonPayload(creationPayload);
        Response response = getResponseOfPost(AUTHENTICATOR_CUSTOM_API_BASE_PATH, body);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body("code", equalTo("AUT-60015"))
                .body("message", equalTo("Invalid empty or blank value."))
                .body("description", equalTo("Value for displayName should not be empty or blank."));
    }

    @Test(priority = 4)
    public void createUserDefinedLocalAuthenticatorInvalidEndpointUri() throws JsonProcessingException {

        creationPayload.getEndpoint().setUri("htt://test.com");
        String body = UserDefinedLocalAuthenticatorPayload.convertToJasonPayload(creationPayload);
        Response response = getResponseOfPostNoFilter(AUTHENTICATOR_CUSTOM_API_BASE_PATH, body);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body("code", equalTo("UE-10000"))
                .body("message", equalTo("Invalid Request"))
                .body("description", equalTo("must match \"^https?://.+\""));
    }

    @Test(priority = 5)
    public void createUserDefinedLocalAuthenticatorEmptyEndpointProperty() throws JsonProcessingException {

        creationPayload.getEndpoint().getAuthentication().getProperties().put("username", "");
        String body = UserDefinedLocalAuthenticatorPayload.convertToJasonPayload(creationPayload);
        Response response = getResponseOfPost(AUTHENTICATOR_CUSTOM_API_BASE_PATH, body);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body("code", equalTo("AUT-60011"))
                .body("message", equalTo("Invalid endpoint configuration provided."))
                .body("description", equalTo("The Property username cannot be blank."));
    }

    @Test(priority = 6)
    public void createUserDefinedLocalAuthenticatorInvalidEndpointAuthenticationType() throws JsonProcessingException {

        String body = UserDefinedLocalAuthenticatorPayload.convertToJasonPayload(creationPayload);
        body = StringUtils.replace(body, AuthenticatorPropertyConstants.AuthenticationType.IDENTIFICATION.toString(),
                "InvalidAuthenticationType.");
        Response response = getResponseOfPostNoFilter(AUTHENTICATOR_CUSTOM_API_BASE_PATH, body);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body("code", equalTo("UE-10000"))
                .body("message", equalTo("Invalid Request."))
                .body("description", equalTo("Provided request body content is not in the expected format."));
    }

    @Test(priority = 7)
    public void createUserDefinedLocalAuthenticatorInvalidAuthenticationType() throws JsonProcessingException {

        String body = UserDefinedLocalAuthenticatorPayload.convertToJasonPayload(creationPayload);
        body = StringUtils.replace(body, AuthenticationType.TypeEnum.BASIC.toString(), "InvalidType.");
        Response response = getResponseOfPostNoFilter(AUTHENTICATOR_CUSTOM_API_BASE_PATH, body);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body("code", equalTo("UE-10000"))
                .body("message", equalTo("Invalid Request."))
                .body("description", equalTo("Provided request body content is not in the expected format."));
    }

    @Test(priority = 8)
    public void updateNonExistingUserDefinedLocalAuthenticator() throws JsonProcessingException {

        String body = UserDefinedLocalAuthenticatorPayload.convertToJasonPayload(updatePayload);
        Response response = getResponseOfPutWithNoFilter(AUTHENTICATOR_CUSTOM_API_BASE_PATH + PATH_SEPARATOR +
                customIdPId, body);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NOT_FOUND);
    }


    @Test(priority = 9)
    public void createUserDefinedLocalAuthenticatorWithExistingAuthenticatorName() throws JsonProcessingException {

        String body = UserDefinedLocalAuthenticatorPayload.convertToJasonPayload(creationPayload);
        Response response = getResponseOfPost(AUTHENTICATOR_CUSTOM_API_BASE_PATH, body);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED);

        Response responseForDuplication = getResponseOfPost(AUTHENTICATOR_CUSTOM_API_BASE_PATH, body);
        responseForDuplication.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body("code", equalTo("AUT-60013"))
                .body("message", equalTo("The authenticator already exists."))
                .body("description", equalTo("The authenticator already exists for the given name:" +
                        " custom_Authenticator."));
    }

    @Test(priority = 10)
    public void updateUserDefinedLocalAuthenticatorMissingEndpointProperty() throws JsonProcessingException {

        updatePayload.getEndpoint().getAuthentication().getProperties().remove("username");
        String body = UserDefinedLocalAuthenticatorPayload.convertToJasonPayload(updatePayload);
        Response response = getResponseOfPutWithNoFilter(AUTHENTICATOR_CUSTOM_API_BASE_PATH + PATH_SEPARATOR +
                customIdPId, body);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body("code", equalTo("AUT-60011"))
                .body("message", equalTo("Invalid endpoint configuration provided."))
                .body("description", equalTo("The property username must be provided as an authentication " +
                        "property for the BASIC authentication type."));
    }

    @Test(priority = 11)
    public void updateUserDefinedLocalAuthenticatorEmptyDisplayName() throws JsonProcessingException {

        updatePayload.setDisplayName("");
        String body = UserDefinedLocalAuthenticatorPayload.convertToJasonPayload(updatePayload);
        Response response = getResponseOfPutWithNoFilter(AUTHENTICATOR_CUSTOM_API_BASE_PATH + PATH_SEPARATOR +
                customIdPId, body);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body("code", equalTo("AUT-60015"))
                .body("message", equalTo("Invalid empty or blank value."))
                .body("description", equalTo("Value for displayName should not be empty or blank."));
    }

    @Test(priority = 12)
    public void updateUserDefinedLocalAuthenticatorInvalidEndpointUri() throws JsonProcessingException {

        updatePayload.getEndpoint().setUri("htt://test.com");
        String body = UserDefinedLocalAuthenticatorPayload.convertToJasonPayload(updatePayload);
        Response response = getResponseOfPutWithNoFilter(AUTHENTICATOR_CUSTOM_API_BASE_PATH + PATH_SEPARATOR +
                customIdPId, body);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body("code", equalTo("UE-10000"))
                .body("message", equalTo("Invalid Request"))
                .body("description", equalTo("must match \"^https?://.+\""));
    }

    @Test(priority = 13)
    public void updateUserDefinedLocalAuthenticatorInvalidEndpointAuthenticationType() throws JsonProcessingException {

        String body = UserDefinedLocalAuthenticatorPayload.convertToJasonPayload(updatePayload);
        body = StringUtils.replace(body, AuthenticationType.TypeEnum.BASIC.toString(), "InvalidType.");
        Response response = getResponseOfPutWithNoFilter(AUTHENTICATOR_CUSTOM_API_BASE_PATH + PATH_SEPARATOR +
                customIdPId, body);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body("code", equalTo("UE-10000"))
                .body("message", equalTo("Invalid Request."))
                .body("description", equalTo("Provided request body content is not in the expected format."));
    }

    private void addUserDefinedAuthenticatorToIS() throws JsonProcessingException {

        creationPayload = UserDefinedLocalAuthenticatorPayload
                .getBasedUserDefinedLocalAuthenticatorCreation(testAuthenticatorConfig);
        String body = UserDefinedLocalAuthenticatorPayload.convertToJasonPayload(creationPayload);
        getResponseOfPost(AUTHENTICATOR_CUSTOM_API_BASE_PATH, body);
    }
}

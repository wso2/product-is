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

package org.wso2.identity.integration.test.rest.api.server.notification.sender.v2;

import com.google.gson.Gson;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.identity.integration.test.base.MockOAuth2TokenServer;
import org.wso2.identity.integration.test.rest.api.server.notification.sender.v2.model.Authentication;
import org.wso2.identity.integration.test.rest.api.server.notification.sender.v2.util.AuthenticationBuilder;
import org.wso2.identity.integration.test.rest.api.server.notification.sender.v2.util.SMSSenderRequestBuilder;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.testng.Assert.assertNotNull;

/**
 * Test class for SMS Senders V2 REST APIs success paths.
 */
public class SMSSenderSuccessTest extends SMSSenderTestBase {

    private MockOAuth2TokenServer mockOAuth2TokenServer;

    @Factory(dataProvider = "restAPIUserConfigProvider")
    public SMSSenderSuccessTest(TestUserMode userMode) throws Exception {

        super.init(userMode);
        this.context = isServer;
        this.authenticatingUserName = context.getContextTenant().getTenantAdmin().getUserName();
        this.authenticatingCredential = context.getContextTenant().getTenantAdmin().getPassword();
        this.tenant = context.getContextTenant().getDomain();
    }

    @BeforeClass(alwaysRun = true)
    public void init() throws IOException, InterruptedException {

        mockOAuth2TokenServer = new MockOAuth2TokenServer();
        mockOAuth2TokenServer.start();

        super.testInit(API_VERSION, swaggerDefinition, tenant);
    }

    @AfterClass(alwaysRun = true)
    public void testConclude() {

        super.conclude();

        mockOAuth2TokenServer.clearData();
        mockOAuth2TokenServer.stop();
    }

    @BeforeMethod(alwaysRun = true)
    public void testInit() {

        RestAssured.basePath = basePath;
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

    private String smsNotificationSenderName;

    @Test
    public void testAddSmsSenderWithBasicAuth() throws IOException {

        String body = new Gson().toJson(SMSSenderRequestBuilder.createAddSMSSenderJSON(Authentication.TypeEnum.BASIC,
                this.getClass()));
        Response response =
                getResponseOfPost(NOTIFICATION_SENDER_API_BASE_PATH + PATH_SEPARATOR + SMS_SENDERS_PATH, body);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .header(HttpHeaders.LOCATION, notNullValue());
        String location = response.getHeader(HttpHeaders.LOCATION);
        assertNotNull(location);
        smsNotificationSenderName = location.substring(location.lastIndexOf("/") + 1);
        assertNotNull(smsNotificationSenderName);
    }

    @Test(dependsOnMethods = {"testAddSmsSenderWithBasicAuth"})
    public void testGetSMSSender() {

        Response response = getResponseOfGet(
                NOTIFICATION_SENDER_API_BASE_PATH + PATH_SEPARATOR + SMS_SENDERS_PATH + PATH_SEPARATOR +
                        smsNotificationSenderName);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("provider", equalTo("Custom"))
                .body("providerURL", equalTo("https://example.sms.sender"))
                .body("contentType", equalTo("JSON"))
                .body("authentication.type", equalTo("BASIC"))
                .body("authentication.properties.username", equalTo(AuthenticationBuilder.BASIC_AUTH_USERNAME))
                .body("authentication.properties.password", nullValue())
                .body("properties", notNullValue());
    }

    @Test(dependsOnMethods = {"testGetSMSSender"})
    public void testGetSMSSenders() throws UnsupportedEncodingException {

        String baseIdentifier =
                "find{ it.name == '" + URLDecoder.decode(smsNotificationSenderName, StandardCharsets.UTF_8.name()) +
                        "' }.";
        Response response = getResponseOfGet(NOTIFICATION_SENDER_API_BASE_PATH + PATH_SEPARATOR + SMS_SENDERS_PATH);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body(baseIdentifier + "provider", equalTo("Custom"))
                .body(baseIdentifier + "providerURL",
                        equalTo("https://example.sms.sender"))
                .body(baseIdentifier + "contentType", equalTo("JSON"))
                .body(baseIdentifier + "authentication.type", equalTo("BASIC"))
                .body(baseIdentifier + "properties", notNullValue());
    }

    @Test(dependsOnMethods = {"testGetSMSSenders"})
    public void testUpdateSMSSenderWithApiKeyAuth() throws IOException {

        String body = new Gson().toJson(SMSSenderRequestBuilder.createUpdateSMSSenderJSON(Authentication.TypeEnum.API_KEY,
                this.getClass()));
        Response response = getResponseOfPut(NOTIFICATION_SENDER_API_BASE_PATH + PATH_SEPARATOR + SMS_SENDERS_PATH +
                PATH_SEPARATOR + smsNotificationSenderName, body);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("provider", equalTo("Custom"))
                .body("providerURL", equalTo("https://example.sms.sender"))
                .body("sender", equalTo("0123456789"))
                .body("contentType", equalTo("JSON"))
                .body("authentication.type", equalTo("API_KEY"))
                .body("authentication.properties.header", equalTo(AuthenticationBuilder.API_KEY_HEADER))
                .body("authentication.properties.value", nullValue())
                .body("properties", notNullValue());
    }

    @Test(dependsOnMethods = {"testUpdateSMSSenderWithApiKeyAuth"})
    public void testGetUpdatedSMSSender() throws IOException {

        Response response = getResponseOfGet(
                NOTIFICATION_SENDER_API_BASE_PATH + PATH_SEPARATOR + SMS_SENDERS_PATH + PATH_SEPARATOR +
                        smsNotificationSenderName);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("provider", equalTo("Custom"))
                .body("providerURL", equalTo("https://example.sms.sender"))
                .body("sender", equalTo("0123456789"))
                .body("contentType", equalTo("JSON"))
                .body("authentication.type", equalTo("API_KEY"))
                .body("authentication.properties.header", equalTo(AuthenticationBuilder.API_KEY_HEADER))
                .body("authentication.properties.value", nullValue());

        testDeleteSmsSender();
    }

    @Test(dependsOnMethods = {"testGetUpdatedSMSSender"})
    public void testAddSmsSenderWithBearerAuth() throws IOException {

        String body = new Gson().toJson(SMSSenderRequestBuilder.createAddSMSSenderJSON(Authentication.TypeEnum.BEARER,
                this.getClass()));
        Response response =
                getResponseOfPost(NOTIFICATION_SENDER_API_BASE_PATH + PATH_SEPARATOR + SMS_SENDERS_PATH, body);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .header(HttpHeaders.LOCATION, notNullValue());
        String location = response.getHeader(HttpHeaders.LOCATION);
        assertNotNull(location);
        smsNotificationSenderName = location.substring(location.lastIndexOf("/") + 1);
        assertNotNull(smsNotificationSenderName);
    }

    @Test(dependsOnMethods = {"testAddSmsSenderWithBearerAuth"})
    public void testGetSMSSenderWithBearerAuth() {

        Response response = getResponseOfGet(
                NOTIFICATION_SENDER_API_BASE_PATH + PATH_SEPARATOR + SMS_SENDERS_PATH + PATH_SEPARATOR +
                        smsNotificationSenderName);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("authentication.type", equalTo("BEARER"))
                .body("authentication.properties.accessToken", nullValue());

        testDeleteSmsSender();
    }

    @Test(dependsOnMethods = {"testGetSMSSenderWithBearerAuth"})
    public void testAddSmsSenderWithClientCredentialAuth() throws IOException, InterruptedException {

        String body = new Gson().toJson(SMSSenderRequestBuilder.createAddSMSSenderJSON(
                Authentication.TypeEnum.CLIENT_CREDENTIAL, this.getClass()));
        Response response =
                getResponseOfPost(NOTIFICATION_SENDER_API_BASE_PATH + PATH_SEPARATOR + SMS_SENDERS_PATH, body);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .header(HttpHeaders.LOCATION, notNullValue());
        String location = response.getHeader(HttpHeaders.LOCATION);
        assertNotNull(location);
        smsNotificationSenderName = location.substring(location.lastIndexOf("/") + 1);
        assertNotNull(smsNotificationSenderName);
    }

    @Test(dependsOnMethods = {"testAddSmsSenderWithClientCredentialAuth"})
    public void testGetSMSSenderWithClientCredentialAuth() {

        Response response = getResponseOfGet(
                NOTIFICATION_SENDER_API_BASE_PATH + PATH_SEPARATOR + SMS_SENDERS_PATH + PATH_SEPARATOR +
                        smsNotificationSenderName);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("authentication.type", equalTo("CLIENT_CREDENTIAL"))
                .body("authentication.properties.clientId", equalTo(AuthenticationBuilder.CLIENT_CREDENTIAL_CLIENT_ID))
                .body("authentication.properties.clientSecret", nullValue())
                .body("authentication.properties.tokenEndpoint", equalTo(MockOAuth2TokenServer.TOKEN_ENDPOINT_URL))
                .body("authentication.properties.scopes", equalTo(AuthenticationBuilder.CLIENT_CREDENTIAL_SCOPES));

        testDeleteSmsSender();
    }

    private void testDeleteSmsSender() {

        Response response =
                getResponseOfDelete(NOTIFICATION_SENDER_API_BASE_PATH + PATH_SEPARATOR + SMS_SENDERS_PATH +
                        PATH_SEPARATOR + smsNotificationSenderName);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NO_CONTENT);
    }
}

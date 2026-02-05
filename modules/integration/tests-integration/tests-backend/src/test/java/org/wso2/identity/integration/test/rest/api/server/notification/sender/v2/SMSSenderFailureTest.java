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
import org.wso2.identity.integration.test.rest.api.server.notification.sender.v2.model.Authentication;
import org.wso2.identity.integration.test.rest.api.server.notification.sender.v2.util.SMSSenderRequestBuilder;

import java.io.IOException;

import static org.hamcrest.core.IsNull.notNullValue;
import static org.testng.Assert.assertNotNull;

/**
 * Test class for SMS Senders V2 REST APIs failure paths.
 */
public class SMSSenderFailureTest extends SMSSenderTestBase {

    private String smsNotificationSenderName;

    @Factory(dataProvider = "restAPIUserConfigProvider")
    public SMSSenderFailureTest(TestUserMode userMode) throws Exception {

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
    public void testConclude() {

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

    @DataProvider(name = "restAPIUserConfigProvider")
    public static Object[][] restAPIUserConfigProvider() {

        return new Object[][]{
                {TestUserMode.SUPER_TENANT_ADMIN},
                {TestUserMode.TENANT_ADMIN}
        };
    }

    @Test
    public void testAddSmsSenderConflict() throws IOException {

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

        // Try to create another SMS sender with the same configuration - should conflict
        response = getResponseOfPost(NOTIFICATION_SENDER_API_BASE_PATH + PATH_SEPARATOR + SMS_SENDERS_PATH, body);
        validateErrorResponse(response, HttpStatus.SC_CONFLICT, "NSM-60002");

        // Cleanup the first SMS sender created
        cleanupSmsSender();
    }

    @Test
    public void testGetSmsSenderByInvalidName() {

        Response response = getResponseOfGet(
                NOTIFICATION_SENDER_API_BASE_PATH + PATH_SEPARATOR + SMS_SENDERS_PATH + PATH_SEPARATOR +
                        "randomName");
        validateErrorResponse(response, HttpStatus.SC_NOT_FOUND, "NSM-60006");
    }

    @Test
    public void testDeleteSmsSenderByInvalidName() {

        Response response = getResponseOfDelete(
                NOTIFICATION_SENDER_API_BASE_PATH + PATH_SEPARATOR + SMS_SENDERS_PATH + PATH_SEPARATOR +
                        "randomName");
        validateErrorResponse(response, HttpStatus.SC_NOT_FOUND, "NSM-60006");
    }

    @Test
    public void testAddSmsSenderWithMissingBasicAuthUsername() throws IOException {

        // Create SMS sender with BASIC auth but missing username
        String body = "{"
                + "\"provider\": \"Custom\","
                + "\"providerURL\": \"https://example.sms.sender\","
                + "\"contentType\": \"JSON\","
                + "\"authentication\": {"
                + "  \"type\": \"BASIC\","
                + "  \"properties\": {"
                + "    \"password\": \"testpass\""
                + "  }"
                + "},"
                + "\"properties\": ["
                + "  {\"key\": \"body\", \"value\": \"{\\\"content\\\": {{body}}, \\\"to\\\": {{mobile}} }\"}"
                + "]"
                + "}";

        Response response =
                getResponseOfPost(NOTIFICATION_SENDER_API_BASE_PATH + PATH_SEPARATOR + SMS_SENDERS_PATH, body);
        validateErrorResponse(response, HttpStatus.SC_BAD_REQUEST, "NSM-60012");
    }

    @Test
    public void testAddSmsSenderWithMissingBasicAuthPassword() throws IOException {

        // Create SMS sender with BASIC auth but missing password
        String body = "{"
                + "\"provider\": \"Custom\","
                + "\"providerURL\": \"https://example.sms.sender\","
                + "\"contentType\": \"JSON\","
                + "\"authentication\": {"
                + "  \"type\": \"BASIC\","
                + "  \"properties\": {"
                + "    \"username\": \"testuser\""
                + "  }"
                + "},"
                + "\"properties\": ["
                + "  {\"key\": \"body\", \"value\": \"{\\\"content\\\": {{body}}, \\\"to\\\": {{mobile}} }\"}"
                + "]"
                + "}";

        Response response =
                getResponseOfPost(NOTIFICATION_SENDER_API_BASE_PATH + PATH_SEPARATOR + SMS_SENDERS_PATH, body);
        validateErrorResponse(response, HttpStatus.SC_BAD_REQUEST, "NSM-60012");
    }

    @Test
    public void testAddSmsSenderWithMissingApiKeyHeader() throws IOException {

        // Create SMS sender with API_KEY auth but missing header
        String body = "{"
                + "\"provider\": \"Custom\","
                + "\"providerURL\": \"https://example.sms.sender\","
                + "\"contentType\": \"JSON\","
                + "\"authentication\": {"
                + "  \"type\": \"API_KEY\","
                + "  \"properties\": {"
                + "    \"value\": \"test-api-key\""
                + "  }"
                + "},"
                + "\"properties\": ["
                + "  {\"key\": \"body\", \"value\": \"{\\\"content\\\": {{body}}, \\\"to\\\": {{mobile}} }\"}"
                + "]"
                + "}";

        Response response =
                getResponseOfPost(NOTIFICATION_SENDER_API_BASE_PATH + PATH_SEPARATOR + SMS_SENDERS_PATH, body);
        validateErrorResponse(response, HttpStatus.SC_BAD_REQUEST, "NSM-60012");
    }

    @Test
    public void testAddSmsSenderWithMissingApiKeyValue() throws IOException {

        // Create SMS sender with API_KEY auth but missing value
        String body = "{"
                + "\"provider\": \"Custom\","
                + "\"providerURL\": \"https://example.sms.sender\","
                + "\"contentType\": \"JSON\","
                + "\"authentication\": {"
                + "  \"type\": \"API_KEY\","
                + "  \"properties\": {"
                + "    \"header\": \"X-API-Key\""
                + "  }"
                + "},"
                + "\"properties\": ["
                + "  {\"key\": \"body\", \"value\": \"{\\\"content\\\": {{body}}, \\\"to\\\": {{mobile}} }\"}"
                + "]"
                + "}";

        Response response =
                getResponseOfPost(NOTIFICATION_SENDER_API_BASE_PATH + PATH_SEPARATOR + SMS_SENDERS_PATH, body);
        validateErrorResponse(response, HttpStatus.SC_BAD_REQUEST, "NSM-60012");
    }

    @Test
    public void testAddSmsSenderWithMissingBearerToken() throws IOException {

        // Create SMS sender with BEARER auth but missing accessToken
        String body = "{"
                + "\"provider\": \"Custom\","
                + "\"providerURL\": \"https://example.sms.sender\","
                + "\"contentType\": \"JSON\","
                + "\"authentication\": {"
                + "  \"type\": \"BEARER\","
                + "  \"properties\": {}"
                + "},"
                + "\"properties\": ["
                + "  {\"key\": \"body\", \"value\": \"{\\\"content\\\": {{body}}, \\\"to\\\": {{mobile}} }\"}"
                + "]"
                + "}";

        Response response =
                getResponseOfPost(NOTIFICATION_SENDER_API_BASE_PATH + PATH_SEPARATOR + SMS_SENDERS_PATH, body);
        validateErrorResponse(response, HttpStatus.SC_BAD_REQUEST, "NSM-60012");
    }

    @Test
    public void testAddSmsSenderWithMissingClientCredentialClientId() throws IOException {

        // Create SMS sender with CLIENT_CREDENTIAL auth but missing clientId
        String body = "{"
                + "\"provider\": \"Custom\","
                + "\"providerURL\": \"https://example.sms.sender\","
                + "\"contentType\": \"JSON\","
                + "\"authentication\": {"
                + "  \"type\": \"CLIENT_CREDENTIAL\","
                + "  \"properties\": {"
                + "    \"clientSecret\": \"testSecret\","
                + "    \"tokenEndpoint\": \"https://auth.example.com/token\""
                + "  }"
                + "},"
                + "\"properties\": ["
                + "  {\"key\": \"body\", \"value\": \"{\\\"content\\\": {{body}}, \\\"to\\\": {{mobile}} }\"}"
                + "]"
                + "}";

        Response response =
                getResponseOfPost(NOTIFICATION_SENDER_API_BASE_PATH + PATH_SEPARATOR + SMS_SENDERS_PATH, body);
        validateErrorResponse(response, HttpStatus.SC_BAD_REQUEST, "NSM-60012");
    }

    @Test
    public void testAddSmsSenderWithMissingClientCredentialClientSecret() throws IOException {

        // Create SMS sender with CLIENT_CREDENTIAL auth but missing clientSecret
        String body = "{"
                + "\"provider\": \"Custom\","
                + "\"providerURL\": \"https://example.sms.sender\","
                + "\"contentType\": \"JSON\","
                + "\"authentication\": {"
                + "  \"type\": \"CLIENT_CREDENTIAL\","
                + "  \"properties\": {"
                + "    \"clientId\": \"testClient\","
                + "    \"tokenEndpoint\": \"https://auth.example.com/token\""
                + "  }"
                + "},"
                + "\"properties\": ["
                + "  {\"key\": \"body\", \"value\": \"{\\\"content\\\": {{body}}, \\\"to\\\": {{mobile}} }\"}"
                + "]"
                + "}";

        Response response =
                getResponseOfPost(NOTIFICATION_SENDER_API_BASE_PATH + PATH_SEPARATOR + SMS_SENDERS_PATH, body);
        validateErrorResponse(response, HttpStatus.SC_BAD_REQUEST, "NSM-60012");
    }

    @Test
    public void testAddSmsSenderWithMissingClientCredentialTokenEndpoint() throws IOException {

        // Create SMS sender with CLIENT_CREDENTIAL auth but missing tokenEndpoint
        String body = "{"
                + "\"provider\": \"Custom\","
                + "\"providerURL\": \"https://example.sms.sender\","
                + "\"contentType\": \"JSON\","
                + "\"authentication\": {"
                + "  \"type\": \"CLIENT_CREDENTIAL\","
                + "  \"properties\": {"
                + "    \"clientId\": \"testClient\","
                + "    \"clientSecret\": \"testSecret\""
                + "  }"
                + "},"
                + "\"properties\": ["
                + "  {\"key\": \"body\", \"value\": \"{\\\"content\\\": {{body}}, \\\"to\\\": {{mobile}} }\"}"
                + "]"
                + "}";

        Response response =
                getResponseOfPost(NOTIFICATION_SENDER_API_BASE_PATH + PATH_SEPARATOR + SMS_SENDERS_PATH, body);
        validateErrorResponse(response, HttpStatus.SC_BAD_REQUEST, "NSM-60012");
    }

    @Test
    public void testUpdateSmsSenderWithInvalidAuthentication() throws IOException {

        // First create a valid SMS sender
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

        // Try to update with invalid authentication (missing required fields)
        String updateBody = "{"
                + "\"provider\": \"Custom\","
                + "\"providerURL\": \"https://example.sms.sender\","
                + "\"contentType\": \"JSON\","
                + "\"authentication\": {"
                + "  \"type\": \"BASIC\","
                + "  \"properties\": {"
                + "    \"username\": \"testuser\""
                + "  }"
                + "},"
                + "\"properties\": ["
                + "  {\"key\": \"body\", \"value\": \"{\\\"content\\\": {{body}}, \\\"to\\\": {{mobile}} }\"}"
                + "]"
                + "}";

        response = getResponseOfPut(NOTIFICATION_SENDER_API_BASE_PATH + PATH_SEPARATOR + SMS_SENDERS_PATH +
                PATH_SEPARATOR + smsNotificationSenderName, updateBody);
        validateErrorResponse(response, HttpStatus.SC_BAD_REQUEST, "NSM-60012");

        // Cleanup
        cleanupSmsSender();
    }

    /**
     * Helper method to cleanup the SMS sender created during tests.
     */
    private void cleanupSmsSender() {

        if (smsNotificationSenderName != null) {
            Response response = getResponseOfDelete(NOTIFICATION_SENDER_API_BASE_PATH + PATH_SEPARATOR +
                    SMS_SENDERS_PATH + PATH_SEPARATOR + smsNotificationSenderName);
            response.then()
                    .log().ifValidationFails()
                    .assertThat()
                    .statusCode(HttpStatus.SC_NO_CONTENT);
            smsNotificationSenderName = null;
        }
    }
}

/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
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
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.identity.integration.test.rest.api.server.notification.sender.v2.util.EmailSenderRequestBuilder;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.testng.Assert.assertNotNull;

/**
 * Test class for Email Senders V2 REST APIs success paths.
 * Covers HTTP-based email providers with authentication types: BASIC, CLIENT_CREDENTIAL, BEARER, API_KEY, NONE.
 * Covers SMTP-based email providers with authentication types: BASIC, CLIENT_CREDENTIAL.
 */
public class EmailSenderSuccessTest extends EmailSenderTestBase {

    private String emailNotificationSenderName = EMAIL_SENDER_NAME;

    @Factory(dataProvider = "restAPIUserConfigProvider")
    public EmailSenderSuccessTest(TestUserMode userMode) throws Exception {

        super.init(userMode);
        this.context = isServer;
        this.authenticatingUserName = context.getContextTenant().getTenantAdmin().getUserName();
        this.authenticatingCredential = context.getContextTenant().getTenantAdmin().getPassword();
        this.tenant = context.getContextTenant().getDomain();
    }

    @BeforeClass(alwaysRun = true)
    public void init() throws IOException {

        super.testInit(API_VERSION, swaggerDefinition, tenant);

        // Cleanup any leftover email sender from previous test runs.
        if (!StringUtils.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, tenant)) {
            getResponseOfDelete(NOTIFICATION_SENDER_API_BASE_PATH + PATH_SEPARATOR +
                    EMAIL_SENDERS_PATH + PATH_SEPARATOR + emailNotificationSenderName);
            // Ignore the response status - it may be 204 (deleted) or 404 (not found).
        }
    }

    @Override
    @AfterClass(alwaysRun = true)
    public void testConclude() {

        try {
            // Cleanup any leftover email sender to prevent conflicts in subsequent test classes.
            if (!StringUtils.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, tenant)) {
                RestAssured.basePath = basePath;
                getResponseOfDelete(NOTIFICATION_SENDER_API_BASE_PATH + PATH_SEPARATOR +
                        EMAIL_SENDERS_PATH + PATH_SEPARATOR + emailNotificationSenderName);
                // Ignore the response status - it may be 204 (deleted) or 404 (not found).
            }
        } finally {
            super.conclude();
        }
    }

    @Override
    @BeforeMethod(alwaysRun = true)
    public void testInit() {

        RestAssured.basePath = basePath;
    }

    @Override
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

    /**
     * Performs a DELETE request for the current email sender and asserts the expected status code
     * based on the tenant type.
     */
    private void deleteEmailSenderAndValidate() {

        Response response =
                getResponseOfDelete(NOTIFICATION_SENDER_API_BASE_PATH + PATH_SEPARATOR + EMAIL_SENDERS_PATH +
                        PATH_SEPARATOR + emailNotificationSenderName);
        if (!StringUtils.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, tenant)) {
            response.then()
                    .log().ifValidationFails()
                    .assertThat()
                    .statusCode(HttpStatus.SC_NO_CONTENT);
        } else {
            response.then()
                    .log().ifValidationFails()
                    .assertThat()
                    .statusCode(HttpStatus.SC_METHOD_NOT_ALLOWED);
        }
    }

    @Test
    public void testAddHTTPEmailSenderWithBasicAuth() throws IOException {

        String body = new Gson().toJson(EmailSenderRequestBuilder.createAddHTTPEmailSenderJSON(
                AUTH_TYPE_BASIC, this.getClass()));
        Response response =
                getResponseOfPost(NOTIFICATION_SENDER_API_BASE_PATH + PATH_SEPARATOR + EMAIL_SENDERS_PATH, body);
        if (!StringUtils.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, tenant)) {
            response.then()
                    .log().ifValidationFails()
                    .assertThat()
                    .statusCode(HttpStatus.SC_CREATED)
                    .header(HttpHeaders.LOCATION, notNullValue());
            String location = response.getHeader(HttpHeaders.LOCATION);
            assertNotNull(location);
            emailNotificationSenderName = location.substring(location.lastIndexOf("/") + 1);
            assertNotNull(emailNotificationSenderName);
        } else {
            response.then()
                    .log().ifValidationFails()
                    .assertThat()
                    .statusCode(HttpStatus.SC_METHOD_NOT_ALLOWED);
        }
    }

    @Test(dependsOnMethods = {"testAddHTTPEmailSenderWithBasicAuth"})
    public void testGetHTTPEmailSenderWithBasicAuth() {

        Response response = getResponseOfGet(
                NOTIFICATION_SENDER_API_BASE_PATH + PATH_SEPARATOR + EMAIL_SENDERS_PATH + PATH_SEPARATOR +
                        emailNotificationSenderName);
        if (!StringUtils.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, tenant)) {
            response.then()
                    .log().ifValidationFails()
                    .assertThat()
                    .statusCode(HttpStatus.SC_OK)
                    .body("provider", equalTo(HTTP_PROVIDER))
                    .body("providerURL", equalTo(HTTP_PROVIDER_URL))
                    .body("authType", equalTo(AUTH_TYPE_BASIC))
                    .body(PROPERTY_CONTENT_TYPE, equalTo(HTTP_CONTENT_TYPE))
                    .body(PROPERTY_HTTP_METHOD, equalTo(HTTP_CLIENT_METHOD))
                    .body(PROPERTY_HTTP_HEADERS, equalTo(HTTP_HEADERS))
                    .body(PROPERTY_USERNAME, equalTo(BASIC_AUTH_USERNAME))
                    .body(PROPERTY_PASSWORD, nullValue());
        } else {
            response.then()
                    .log().ifValidationFails()
                    .assertThat()
                    .statusCode(HttpStatus.SC_METHOD_NOT_ALLOWED);
        }
    }

    @Test(dependsOnMethods = {"testGetHTTPEmailSenderWithBasicAuth"})
    public void testGetHTTPEmailSenders() throws UnsupportedEncodingException {

        Response response = getResponseOfGet(NOTIFICATION_SENDER_API_BASE_PATH + PATH_SEPARATOR + EMAIL_SENDERS_PATH);
        if (!StringUtils.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, tenant)) {
            String baseIdentifier =
                    "find{ it.name == '" +
                            URLDecoder.decode(emailNotificationSenderName, StandardCharsets.UTF_8.name()) + "' }.";
            response.then()
                    .log().ifValidationFails()
                    .assertThat()
                    .statusCode(HttpStatus.SC_OK)
                    .body(baseIdentifier + "provider", equalTo(HTTP_PROVIDER))
                    .body(baseIdentifier + "providerURL", equalTo(HTTP_PROVIDER_URL))
                    .body(baseIdentifier + "authType", equalTo(AUTH_TYPE_BASIC))
                    .body(baseIdentifier + PROPERTY_CONTENT_TYPE, equalTo(HTTP_CONTENT_TYPE))
                    .body(baseIdentifier + PROPERTY_HTTP_METHOD, equalTo(HTTP_CLIENT_METHOD));
        } else {
            response.then()
                    .log().ifValidationFails()
                    .assertThat()
                    .statusCode(HttpStatus.SC_METHOD_NOT_ALLOWED);
        }
    }

    @Test(dependsOnMethods = {"testGetHTTPEmailSenders"})
    public void testUpdateHTTPEmailSenderWithApiKeyAuth() throws IOException {

        String body = new Gson().toJson(EmailSenderRequestBuilder.createUpdateHTTPEmailSenderJSON(
                AUTH_TYPE_API_KEY, this.getClass()));
        Response response = getResponseOfPut(NOTIFICATION_SENDER_API_BASE_PATH + PATH_SEPARATOR +
                EMAIL_SENDERS_PATH + PATH_SEPARATOR + emailNotificationSenderName, body);
        if (!StringUtils.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, tenant)) {
            response.then()
                    .log().ifValidationFails()
                    .assertThat()
                    .statusCode(HttpStatus.SC_OK)
                    .body("provider", equalTo(HTTP_PROVIDER))
                    .body("providerURL", equalTo(HTTP_PROVIDER_URL_UPDATED))
                    .body("authType", equalTo(AUTH_TYPE_API_KEY))
                    .body(PROPERTY_CONTENT_TYPE, equalTo(HTTP_CONTENT_TYPE))
                    .body(PROPERTY_HTTP_METHOD, equalTo(HTTP_CLIENT_METHOD))
                    .body(PROPERTY_HTTP_HEADERS, equalTo(HTTP_HEADERS_UPDATED));
        } else {
            response.then()
                    .log().ifValidationFails()
                    .assertThat()
                    .statusCode(HttpStatus.SC_METHOD_NOT_ALLOWED);
        }
    }

    @Test(dependsOnMethods = {"testUpdateHTTPEmailSenderWithApiKeyAuth"})
    public void testDeleteHTTPEmailSenderWithBasicAuth() {

        deleteEmailSenderAndValidate();
    }

    @Test(dependsOnMethods = {"testDeleteHTTPEmailSenderWithBasicAuth"})
    public void testAddHTTPEmailSenderWithBearerAuth() throws IOException {

        String body = new Gson().toJson(EmailSenderRequestBuilder.createAddHTTPEmailSenderJSON(
                AUTH_TYPE_BEARER, this.getClass()));
        Response response =
                getResponseOfPost(NOTIFICATION_SENDER_API_BASE_PATH + PATH_SEPARATOR + EMAIL_SENDERS_PATH, body);
        if (!StringUtils.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, tenant)) {
            response.then()
                    .log().ifValidationFails()
                    .assertThat()
                    .statusCode(HttpStatus.SC_CREATED)
                    .header(HttpHeaders.LOCATION, notNullValue());
            String location = response.getHeader(HttpHeaders.LOCATION);
            assertNotNull(location);
            emailNotificationSenderName = location.substring(location.lastIndexOf("/") + 1);
            assertNotNull(emailNotificationSenderName);
        } else {
            response.then()
                    .log().ifValidationFails()
                    .assertThat()
                    .statusCode(HttpStatus.SC_METHOD_NOT_ALLOWED);
        }
    }

    @Test(dependsOnMethods = {"testAddHTTPEmailSenderWithBearerAuth"})
    public void testGetHTTPEmailSenderWithBearerAuth() {

        Response response = getResponseOfGet(
                NOTIFICATION_SENDER_API_BASE_PATH + PATH_SEPARATOR + EMAIL_SENDERS_PATH + PATH_SEPARATOR +
                        emailNotificationSenderName);
        if (!StringUtils.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, tenant)) {
            response.then()
                    .log().ifValidationFails()
                    .assertThat()
                    .statusCode(HttpStatus.SC_OK)
                    .body("provider", equalTo(HTTP_PROVIDER))
                    .body("providerURL", equalTo(HTTP_PROVIDER_URL))
                    .body("authType", equalTo(AUTH_TYPE_BEARER))
                    .body(PROPERTY_CONTENT_TYPE, equalTo(HTTP_CONTENT_TYPE))
                    .body(PROPERTY_HTTP_METHOD, equalTo(HTTP_CLIENT_METHOD))
                    .body(PROPERTY_HTTP_HEADERS, equalTo(HTTP_HEADERS))
                    .body(PROPERTY_ACCESS_TOKEN, nullValue());
        } else {
            response.then()
                    .log().ifValidationFails()
                    .assertThat()
                    .statusCode(HttpStatus.SC_METHOD_NOT_ALLOWED);
        }
    }

    @Test(dependsOnMethods = {"testGetHTTPEmailSenderWithBearerAuth"})
    public void testDeleteHTTPEmailSenderWithBearerAuth() {

        deleteEmailSenderAndValidate();
    }

    @Test(dependsOnMethods = {"testDeleteHTTPEmailSenderWithBearerAuth"})
    public void testAddHTTPEmailSenderWithClientCredentialAuth() throws IOException {

        String body = new Gson().toJson(EmailSenderRequestBuilder.createAddHTTPEmailSenderJSON(
                AUTH_TYPE_CLIENT_CREDENTIAL, this.getClass()));
        Response response =
                getResponseOfPost(NOTIFICATION_SENDER_API_BASE_PATH + PATH_SEPARATOR + EMAIL_SENDERS_PATH, body);
        if (!StringUtils.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, tenant)) {
            response.then()
                    .log().ifValidationFails()
                    .assertThat()
                    .statusCode(HttpStatus.SC_CREATED)
                    .header(HttpHeaders.LOCATION, notNullValue());
            String location = response.getHeader(HttpHeaders.LOCATION);
            assertNotNull(location);
            emailNotificationSenderName = location.substring(location.lastIndexOf("/") + 1);
            assertNotNull(emailNotificationSenderName);
        } else {
            response.then()
                    .log().ifValidationFails()
                    .assertThat()
                    .statusCode(HttpStatus.SC_METHOD_NOT_ALLOWED);
        }
    }

    @Test(dependsOnMethods = {"testAddHTTPEmailSenderWithClientCredentialAuth"})
    public void testGetHTTPEmailSenderWithClientCredentialAuth() {

        Response response = getResponseOfGet(
                NOTIFICATION_SENDER_API_BASE_PATH + PATH_SEPARATOR + EMAIL_SENDERS_PATH + PATH_SEPARATOR +
                        emailNotificationSenderName);
        if (!StringUtils.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, tenant)) {
            response.then()
                    .log().ifValidationFails()
                    .assertThat()
                    .statusCode(HttpStatus.SC_OK)
                    .body("provider", equalTo(HTTP_PROVIDER))
                    .body("providerURL", equalTo(HTTP_PROVIDER_URL))
                    .body("authType", equalTo(AUTH_TYPE_CLIENT_CREDENTIAL))
                    .body(PROPERTY_CONTENT_TYPE, equalTo(HTTP_CONTENT_TYPE))
                    .body(PROPERTY_HTTP_METHOD, equalTo(HTTP_CLIENT_METHOD))
                    .body(PROPERTY_HTTP_HEADERS, equalTo(HTTP_HEADERS))
                    .body(PROPERTY_CLIENT_ID, equalTo(CLIENT_CREDENTIAL_CLIENT_ID))
                    .body(PROPERTY_TOKEN_ENDPOINT, equalTo(CLIENT_CREDENTIAL_TOKEN_ENDPOINT))
                    .body(PROPERTY_SCOPES, equalTo(CLIENT_CREDENTIAL_SCOPES))
                    .body(PROPERTY_CLIENT_SECRET, nullValue());
        } else {
            response.then()
                    .log().ifValidationFails()
                    .assertThat()
                    .statusCode(HttpStatus.SC_METHOD_NOT_ALLOWED);
        }
    }

    @Test(dependsOnMethods = {"testGetHTTPEmailSenderWithClientCredentialAuth"})
    public void testDeleteHTTPEmailSenderWithClientCredentialAuth() {

        deleteEmailSenderAndValidate();
    }

    @Test(dependsOnMethods = {"testDeleteHTTPEmailSenderWithClientCredentialAuth"})
    public void testAddHTTPEmailSenderWithApiKeyAuth() throws IOException {

        String body = new Gson().toJson(EmailSenderRequestBuilder.createAddHTTPEmailSenderJSON(
                AUTH_TYPE_API_KEY, this.getClass()));
        Response response =
                getResponseOfPost(NOTIFICATION_SENDER_API_BASE_PATH + PATH_SEPARATOR + EMAIL_SENDERS_PATH, body);
        if (!StringUtils.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, tenant)) {
            response.then()
                    .log().ifValidationFails()
                    .assertThat()
                    .statusCode(HttpStatus.SC_CREATED)
                    .header(HttpHeaders.LOCATION, notNullValue());
            String location = response.getHeader(HttpHeaders.LOCATION);
            assertNotNull(location);
            emailNotificationSenderName = location.substring(location.lastIndexOf("/") + 1);
            assertNotNull(emailNotificationSenderName);
        } else {
            response.then()
                    .log().ifValidationFails()
                    .assertThat()
                    .statusCode(HttpStatus.SC_METHOD_NOT_ALLOWED);
        }
    }

    @Test(dependsOnMethods = {"testAddHTTPEmailSenderWithApiKeyAuth"})
    public void testGetHTTPEmailSenderWithApiKeyAuth() {

        Response response = getResponseOfGet(
                NOTIFICATION_SENDER_API_BASE_PATH + PATH_SEPARATOR + EMAIL_SENDERS_PATH + PATH_SEPARATOR +
                        emailNotificationSenderName);
        if (!StringUtils.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, tenant)) {
            response.then()
                    .log().ifValidationFails()
                    .assertThat()
                    .statusCode(HttpStatus.SC_OK)
                    .body("provider", equalTo(HTTP_PROVIDER))
                    .body("providerURL", equalTo(HTTP_PROVIDER_URL))
                    .body("authType", equalTo(AUTH_TYPE_API_KEY))
                    .body(PROPERTY_CONTENT_TYPE, equalTo(HTTP_CONTENT_TYPE))
                    .body(PROPERTY_HTTP_METHOD, equalTo(HTTP_CLIENT_METHOD))
                    .body(PROPERTY_HTTP_HEADERS, equalTo(HTTP_HEADERS))
                    .body(PROPERTY_API_KEY_HEADER, equalTo(API_KEY_HEADER))
                    .body(PROPERTY_API_KEY_VALUE, nullValue());
        } else {
            response.then()
                    .log().ifValidationFails()
                    .assertThat()
                    .statusCode(HttpStatus.SC_METHOD_NOT_ALLOWED);
        }
    }

    @Test(dependsOnMethods = {"testGetHTTPEmailSenderWithApiKeyAuth"})
    public void testDeleteHTTPEmailSenderWithApiKeyAuth() {

        deleteEmailSenderAndValidate();
    }

    @Test(dependsOnMethods = {"testDeleteHTTPEmailSenderWithApiKeyAuth"})
    public void testAddHTTPEmailSenderWithNoAuth() throws IOException {

        String body = new Gson().toJson(EmailSenderRequestBuilder.createAddHTTPEmailSenderJSON(
                AUTH_TYPE_NONE, this.getClass()));
        Response response =
                getResponseOfPost(NOTIFICATION_SENDER_API_BASE_PATH + PATH_SEPARATOR + EMAIL_SENDERS_PATH, body);
        if (!StringUtils.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, tenant)) {
            response.then()
                    .log().ifValidationFails()
                    .assertThat()
                    .statusCode(HttpStatus.SC_CREATED)
                    .header(HttpHeaders.LOCATION, notNullValue());
            String location = response.getHeader(HttpHeaders.LOCATION);
            assertNotNull(location);
            emailNotificationSenderName = location.substring(location.lastIndexOf("/") + 1);
            assertNotNull(emailNotificationSenderName);
        } else {
            response.then()
                    .log().ifValidationFails()
                    .assertThat()
                    .statusCode(HttpStatus.SC_METHOD_NOT_ALLOWED);
        }
    }

    @Test(dependsOnMethods = {"testAddHTTPEmailSenderWithNoAuth"})
    public void testGetHTTPEmailSenderWithNoAuth() {

        Response response = getResponseOfGet(
                NOTIFICATION_SENDER_API_BASE_PATH + PATH_SEPARATOR + EMAIL_SENDERS_PATH + PATH_SEPARATOR +
                        emailNotificationSenderName);
        if (!StringUtils.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, tenant)) {
            response.then()
                    .log().ifValidationFails()
                    .assertThat()
                    .statusCode(HttpStatus.SC_OK)
                    .body("provider", equalTo(HTTP_PROVIDER))
                    .body("providerURL", equalTo(HTTP_PROVIDER_URL))
                    .body("authType", equalTo(AUTH_TYPE_NONE))
                    .body(PROPERTY_CONTENT_TYPE, equalTo(HTTP_CONTENT_TYPE))
                    .body(PROPERTY_HTTP_METHOD, equalTo(HTTP_CLIENT_METHOD))
                    .body(PROPERTY_HTTP_HEADERS, equalTo(HTTP_HEADERS));
        } else {
            response.then()
                    .log().ifValidationFails()
                    .assertThat()
                    .statusCode(HttpStatus.SC_METHOD_NOT_ALLOWED);
        }
    }

    @Test(dependsOnMethods = {"testGetHTTPEmailSenderWithNoAuth"})
    public void testDeleteHTTPEmailSenderWithNoAuth() {

        deleteEmailSenderAndValidate();
    }

    @Test(dependsOnMethods = {"testDeleteHTTPEmailSenderWithNoAuth"})
    public void testAddSMTPEmailSenderWithBasicAuth() throws IOException {

        String body = new Gson().toJson(EmailSenderRequestBuilder.createAddSMTPEmailSenderJSON(
                AUTH_TYPE_BASIC, this.getClass()));
        Response response =
                getResponseOfPost(NOTIFICATION_SENDER_API_BASE_PATH + PATH_SEPARATOR + EMAIL_SENDERS_PATH, body);
        if (!StringUtils.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, tenant)) {
            response.then()
                    .log().ifValidationFails()
                    .assertThat()
                    .statusCode(HttpStatus.SC_CREATED)
                    .header(HttpHeaders.LOCATION, notNullValue());
            String location = response.getHeader(HttpHeaders.LOCATION);
            assertNotNull(location);
            emailNotificationSenderName = location.substring(location.lastIndexOf("/") + 1);
            assertNotNull(emailNotificationSenderName);
        } else {
            response.then()
                    .log().ifValidationFails()
                    .assertThat()
                    .statusCode(HttpStatus.SC_METHOD_NOT_ALLOWED);
        }
    }

    @Test(dependsOnMethods = {"testAddSMTPEmailSenderWithBasicAuth"})
    public void testGetSMTPEmailSenderWithBasicAuth() {

        Response response = getResponseOfGet(
                NOTIFICATION_SENDER_API_BASE_PATH + PATH_SEPARATOR + EMAIL_SENDERS_PATH + PATH_SEPARATOR +
                        emailNotificationSenderName);
        if (!StringUtils.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, tenant)) {
            response.then()
                    .log().ifValidationFails()
                    .assertThat()
                    .statusCode(HttpStatus.SC_OK)
                    .body("smtpServerHost", equalTo(SMTP_SERVER_HOST))
                    .body("smtpPort", equalTo(SMTP_PORT))
                    .body("fromAddress", equalTo(SMTP_FROM_ADDRESS))
                    .body("authType", equalTo(AUTH_TYPE_BASIC))
                    .body("properties", notNullValue())
                    .body(PROPERTY_USERNAME, equalTo(BASIC_AUTH_USERNAME))
                    .body(PROPERTY_PASSWORD, nullValue());
        } else {
            response.then()
                    .log().ifValidationFails()
                    .assertThat()
                    .statusCode(HttpStatus.SC_METHOD_NOT_ALLOWED);
        }
    }

    @Test(dependsOnMethods = {"testGetSMTPEmailSenderWithBasicAuth"})
    public void testGetSMTPEmailSenders() throws UnsupportedEncodingException {

        Response response = getResponseOfGet(NOTIFICATION_SENDER_API_BASE_PATH + PATH_SEPARATOR + EMAIL_SENDERS_PATH);
        if (!StringUtils.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, tenant)) {
            String baseIdentifier =
                    "find{ it.name == '" +
                            URLDecoder.decode(emailNotificationSenderName, StandardCharsets.UTF_8.name()) + "' }.";
            response.then()
                    .log().ifValidationFails()
                    .assertThat()
                    .statusCode(HttpStatus.SC_OK)
                    .body(baseIdentifier + "smtpServerHost", equalTo(SMTP_SERVER_HOST))
                    .body(baseIdentifier + "smtpPort", equalTo(SMTP_PORT))
                    .body(baseIdentifier + "fromAddress", equalTo(SMTP_FROM_ADDRESS))
                    .body(baseIdentifier + "authType", equalTo(AUTH_TYPE_BASIC))
                    .body(baseIdentifier + "properties", notNullValue());
        } else {
            response.then()
                    .log().ifValidationFails()
                    .assertThat()
                    .statusCode(HttpStatus.SC_METHOD_NOT_ALLOWED);
        }
    }

    @Test(dependsOnMethods = {"testGetSMTPEmailSenders"})
    public void testUpdateSMTPEmailSenderWithClientCredentialAuth() throws IOException {

        String body = new Gson().toJson(EmailSenderRequestBuilder.createUpdateSMTPEmailSenderJSON(
                AUTH_TYPE_CLIENT_CREDENTIAL, this.getClass()));
        Response response = getResponseOfPut(NOTIFICATION_SENDER_API_BASE_PATH + PATH_SEPARATOR +
                EMAIL_SENDERS_PATH + PATH_SEPARATOR + emailNotificationSenderName, body);
        if (!StringUtils.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, tenant)) {
            response.then()
                    .log().ifValidationFails()
                    .assertThat()
                    .statusCode(HttpStatus.SC_OK)
                    .body("smtpServerHost", equalTo(SMTP_SERVER_HOST_UPDATED))
                    .body("smtpPort", equalTo(SMTP_PORT))
                    .body("fromAddress", equalTo(SMTP_FROM_ADDRESS_UPDATED))
                    .body("authType", equalTo(AUTH_TYPE_CLIENT_CREDENTIAL))
                    .body("properties", notNullValue())
                    .body(PROPERTY_CLIENT_ID, equalTo(CLIENT_CREDENTIAL_CLIENT_ID))
                    .body(PROPERTY_TOKEN_ENDPOINT, equalTo(CLIENT_CREDENTIAL_TOKEN_ENDPOINT))
                    .body(PROPERTY_SCOPES, equalTo(CLIENT_CREDENTIAL_SCOPES))
                    .body(PROPERTY_CLIENT_SECRET, nullValue());

        } else {
            response.then()
                    .log().ifValidationFails()
                    .assertThat()
                    .statusCode(HttpStatus.SC_METHOD_NOT_ALLOWED);
        }
    }

    @Test(dependsOnMethods = {"testUpdateSMTPEmailSenderWithClientCredentialAuth"})
    public void testDeleteSMTPEmailSenderWithBasicAuth() {

        deleteEmailSenderAndValidate();
    }

    @Test(dependsOnMethods = {"testDeleteSMTPEmailSenderWithBasicAuth"})
    public void testAddSMTPEmailSenderWithClientCredentialAuth() throws IOException {

        String body = new Gson().toJson(EmailSenderRequestBuilder.createAddSMTPEmailSenderJSON(
                AUTH_TYPE_CLIENT_CREDENTIAL, this.getClass()));
        Response response =
                getResponseOfPost(NOTIFICATION_SENDER_API_BASE_PATH + PATH_SEPARATOR + EMAIL_SENDERS_PATH, body);
        if (!StringUtils.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, tenant)) {
            response.then()
                    .log().ifValidationFails()
                    .assertThat()
                    .statusCode(HttpStatus.SC_CREATED)
                    .header(HttpHeaders.LOCATION, notNullValue());
            String location = response.getHeader(HttpHeaders.LOCATION);
            assertNotNull(location);
            emailNotificationSenderName = location.substring(location.lastIndexOf("/") + 1);
            assertNotNull(emailNotificationSenderName);
        } else {
            response.then()
                    .log().ifValidationFails()
                    .assertThat()
                    .statusCode(HttpStatus.SC_METHOD_NOT_ALLOWED);
        }
    }

    @Test(dependsOnMethods = {"testAddSMTPEmailSenderWithClientCredentialAuth"})
    public void testGetSMTPEmailSenderWithClientCredentialAuth() {

        Response response = getResponseOfGet(
                NOTIFICATION_SENDER_API_BASE_PATH + PATH_SEPARATOR + EMAIL_SENDERS_PATH + PATH_SEPARATOR +
                        emailNotificationSenderName);
        if (!StringUtils.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, tenant)) {
            response.then()
                    .log().ifValidationFails()
                    .assertThat()
                    .statusCode(HttpStatus.SC_OK)
                    .body("smtpServerHost", equalTo(SMTP_SERVER_HOST))
                    .body("smtpPort", equalTo(SMTP_PORT))
                    .body("fromAddress", equalTo(SMTP_FROM_ADDRESS))
                    .body("authType", equalTo(AUTH_TYPE_CLIENT_CREDENTIAL))
                    .body("properties", notNullValue())
                    .body(PROPERTY_CLIENT_ID, equalTo(CLIENT_CREDENTIAL_CLIENT_ID))
                    .body(PROPERTY_TOKEN_ENDPOINT, equalTo(CLIENT_CREDENTIAL_TOKEN_ENDPOINT))
                    .body(PROPERTY_SCOPES, equalTo(CLIENT_CREDENTIAL_SCOPES))
                    .body(PROPERTY_CLIENT_SECRET, nullValue());
        } else {
            response.then()
                    .log().ifValidationFails()
                    .assertThat()
                    .statusCode(HttpStatus.SC_METHOD_NOT_ALLOWED);
        }
    }

    @Test(dependsOnMethods = {"testGetSMTPEmailSenderWithClientCredentialAuth"})
    public void testDeleteSMTPEmailSenderWithClientCredentialAuth() {

        deleteEmailSenderAndValidate();
    }
}

/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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

package org.wso2.identity.integration.test.rest.api.server.notification.sender.v1;

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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.testng.Assert.assertNotNull;

/**
 * Test class for Notification Senders REST APIs success paths.
 */
public class NotificationSenderSuccessTest extends NotificationSenderTestBase {

    private String emailNotificationSenderName = "EmailPublisher";
    private String smsNotificationSenderName = "SMSPublisher";

    @Factory(dataProvider = "restAPIUserConfigProvider")
    public NotificationSenderSuccessTest(TestUserMode userMode) throws Exception {

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
    public void testAddEmailSender() throws IOException {

        String body = readResource("add-email-sender.json");
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

    @Test(dependsOnMethods = {"testAddEmailSender"})
    public void testGetEmailSender() throws IOException {

        Response response = getResponseOfGet(
                NOTIFICATION_SENDER_API_BASE_PATH + PATH_SEPARATOR + EMAIL_SENDERS_PATH + PATH_SEPARATOR +
                        emailNotificationSenderName);
        if (!StringUtils.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, tenant)) {
            response.then()
                    .log().ifValidationFails()
                    .assertThat()
                    .statusCode(HttpStatus.SC_OK)
                    .body("name", equalTo("EmailPublisher"))
                    .body("fromAddress", equalTo("iam@gmail.com"))
                    .body("userName", equalTo("iam"))
                    .body("password", equalTo("iam123"))
                    .body("properties", notNullValue());
        } else {
            response.then()
                    .log().ifValidationFails()
                    .assertThat()
                    .statusCode(HttpStatus.SC_METHOD_NOT_ALLOWED);
        }
    }

    @Test(dependsOnMethods = {"testGetEmailSender"})
    public void testGetEmailSenders() throws UnsupportedEncodingException {

        String baseIdentifier =
                "find{ it.name == '" + URLDecoder.decode(emailNotificationSenderName, StandardCharsets.UTF_8.name()) +
                        "' }.";
        Response response = getResponseOfGet(NOTIFICATION_SENDER_API_BASE_PATH + PATH_SEPARATOR + EMAIL_SENDERS_PATH);
        if (!StringUtils.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, tenant)) {
            response.then()
                    .log().ifValidationFails()
                    .assertThat()
                    .statusCode(HttpStatus.SC_OK)
                    .body(baseIdentifier + "name", equalTo("EmailPublisher"))
                    .body(baseIdentifier + "fromAddress", equalTo("iam@gmail.com"))
                    .body(baseIdentifier + "userName", equalTo("iam"))
                    .body(baseIdentifier + "password", equalTo("iam123"))
                    .body(baseIdentifier + "properties", notNullValue());
        } else {
            response.then()
                    .log().ifValidationFails()
                    .assertThat()
                    .statusCode(HttpStatus.SC_METHOD_NOT_ALLOWED);
        }
    }

    @Test(dependsOnMethods = {"testGetEmailSenders"})
    public void testUpdateEmailSender() throws IOException {

        String body = readResource("update-email-sender.json");
        Response response = getResponseOfPut(NOTIFICATION_SENDER_API_BASE_PATH + PATH_SEPARATOR + EMAIL_SENDERS_PATH +
                PATH_SEPARATOR + emailNotificationSenderName, body);
        if (!StringUtils.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, tenant)) {
            response.then()
                    .log().ifValidationFails()
                    .assertThat()
                    .statusCode(HttpStatus.SC_OK)
                    .body("name", equalTo("EmailPublisher"))
                    .body("fromAddress", equalTo("iambu@gmail.com"))
                    .body("userName", equalTo("iambu"))
                    .body("password", equalTo("iambu123"))
                    .body("properties", notNullValue());
        } else {
            response.then()
                    .log().ifValidationFails()
                    .assertThat()
                    .statusCode(HttpStatus.SC_METHOD_NOT_ALLOWED);
        }
    }

    @Test(dependsOnMethods = {"testUpdateEmailSender"})
    public void testDeleteEmailSender() throws IOException {

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
    public void testAddSmsSender() throws IOException {

        String body = readResource("add-sms-sender.json");
        Response response =
                getResponseOfPost(NOTIFICATION_SENDER_API_BASE_PATH + PATH_SEPARATOR + SMS_SENDERS_PATH, body);
        response.then().log().ifValidationFails().assertThat().statusCode(HttpStatus.SC_CREATED)
                .header(HttpHeaders.LOCATION, notNullValue());
        String location = response.getHeader(HttpHeaders.LOCATION);
        assertNotNull(location);
        smsNotificationSenderName = location.substring(location.lastIndexOf("/") + 1);
        assertNotNull(smsNotificationSenderName);
    }

    @Test(dependsOnMethods = {"testAddSmsSender"})
    public void testGetSMSSender() throws IOException {

        Response response = getResponseOfGet(
                NOTIFICATION_SENDER_API_BASE_PATH + PATH_SEPARATOR + SMS_SENDERS_PATH + PATH_SEPARATOR +
                        smsNotificationSenderName);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("name", equalTo("SMSPublisher"))
                .body("provider", equalTo("Vonage"))
                .body("providerURL", equalTo("https://webhook.site/9b79bebd-445a-4dec-ad5e-622b856fa184"))
                .body("key", equalTo("1234"))
                .body("secret", equalTo("12345"))
                .body("sender", equalTo("073923902"))
                .body("contentType", equalTo("JSON"))
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
                .body(baseIdentifier + "name", equalTo("SMSPublisher"))
                .body(baseIdentifier + "provider", equalTo("Vonage"))
                .body(baseIdentifier + "providerURL",
                        equalTo("https://webhook.site/9b79bebd-445a-4dec-ad5e-622b856fa184"))
                .body(baseIdentifier + "key", equalTo("1234"))
                .body(baseIdentifier + "secret", equalTo("12345"))
                .body(baseIdentifier + "sender", equalTo("073923902"))
                .body(baseIdentifier + "contentType", equalTo("JSON"))
                .body(baseIdentifier + "properties", notNullValue());
    }

    @Test(dependsOnMethods = {"testGetSMSSenders"})
    public void testUpdateSMSSender() throws IOException {

        String body = readResource("update-sms-sender.json");
        Response response = getResponseOfPut(NOTIFICATION_SENDER_API_BASE_PATH + PATH_SEPARATOR + SMS_SENDERS_PATH +
                PATH_SEPARATOR + smsNotificationSenderName, body);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("name", equalTo("SMSPublisher"))
                .body("provider", equalTo("Clickatell"))
                .body("providerURL", equalTo("https://webhook.site/9b79bebd-445a-4dec-ad5e-622b856fa185"))
                .body("key", equalTo("123"))
                .body("secret", equalTo("123456"))
                .body("sender", equalTo("0773923902"))
                .body("contentType", equalTo("JSON"))
                .body("properties", notNullValue());
    }

    @Test(dependsOnMethods = {"testUpdateSMSSender"})
    public void testDeleteSMsSender() throws IOException {

        Response response =
                getResponseOfDelete(NOTIFICATION_SENDER_API_BASE_PATH + PATH_SEPARATOR + SMS_SENDERS_PATH +
                        PATH_SEPARATOR + smsNotificationSenderName);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NO_CONTENT);
    }

}

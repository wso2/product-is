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

import static org.hamcrest.core.IsNull.notNullValue;
import static org.testng.Assert.assertNotNull;

/**
 * Test class for Notification Senders REST APIs failure paths.
 */
public class NotificationSenderFailureTest extends NotificationSenderTestBase {

    private String emailNotificationSenderName;
    private String smsNotificationSenderName;

    @Factory(dataProvider = "restAPIUserConfigProvider")
    public NotificationSenderFailureTest(TestUserMode userMode) throws Exception {

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
    public void testAddEmailSenderConflict() throws IOException {

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
            response = getResponseOfPost(NOTIFICATION_SENDER_API_BASE_PATH + PATH_SEPARATOR + EMAIL_SENDERS_PATH, body);
            validateErrorResponse(response, HttpStatus.SC_CONFLICT, "NSM-60002");
        } else {
            response.then()
                    .log().ifValidationFails()
                    .assertThat()
                    .statusCode(HttpStatus.SC_METHOD_NOT_ALLOWED);
        }
    }

    @Test
    public void testAddEmailSenderWithNonExistingEventPublisherName() throws IOException {

        String body = readResource("add-email-sender-2.json");
        Response response =
                getResponseOfPost(NOTIFICATION_SENDER_API_BASE_PATH + PATH_SEPARATOR + EMAIL_SENDERS_PATH, body);
        if (!StringUtils.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, tenant)) {
            validateErrorResponse(response, HttpStatus.SC_NOT_FOUND, "NSM-60001");
        } else {
            response.then()
                    .log().ifValidationFails()
                    .assertThat()
                    .statusCode(HttpStatus.SC_METHOD_NOT_ALLOWED);
        }
    }

    @Test
    public void testGetEmailSenderByInvalidName() {

        Response response = getResponseOfGet(
                NOTIFICATION_SENDER_API_BASE_PATH + PATH_SEPARATOR + EMAIL_SENDERS_PATH + PATH_SEPARATOR +
                        "randomName");
        if (!StringUtils.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, tenant)) {
            validateErrorResponse(response, HttpStatus.SC_NOT_FOUND, "NSM-60006");
        } else {
            response.then()
                    .log().ifValidationFails()
                    .assertThat()
                    .statusCode(HttpStatus.SC_METHOD_NOT_ALLOWED);
        }
    }

    @Test
    public void testDeleteEmailSenderByInvalidName() {

        Response response = getResponseOfDelete(
                NOTIFICATION_SENDER_API_BASE_PATH + PATH_SEPARATOR + EMAIL_SENDERS_PATH + PATH_SEPARATOR +
                        "randomName");
        if (!StringUtils.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, tenant)) {
            validateErrorResponse(response, HttpStatus.SC_NOT_FOUND, "NSM-60006");
        } else {
            response.then()
                    .log().ifValidationFails()
                    .assertThat()
                    .statusCode(HttpStatus.SC_METHOD_NOT_ALLOWED);
        }
    }

    @Test
    public void testAddSmsSenderConflict() throws IOException {

        String body = readResource("add-sms-sender.json");
        Response response =
                getResponseOfPost(NOTIFICATION_SENDER_API_BASE_PATH + PATH_SEPARATOR + SMS_SENDERS_PATH, body);
        response.then().log().ifValidationFails().assertThat().statusCode(HttpStatus.SC_CREATED)
                .header(HttpHeaders.LOCATION, notNullValue());
        String location = response.getHeader(HttpHeaders.LOCATION);
        assertNotNull(location);
        smsNotificationSenderName = location.substring(location.lastIndexOf("/") + 1);
        assertNotNull(smsNotificationSenderName);
        response = getResponseOfPost(NOTIFICATION_SENDER_API_BASE_PATH + PATH_SEPARATOR + SMS_SENDERS_PATH, body);
        validateErrorResponse(response, HttpStatus.SC_CONFLICT, "NSM-60002");

        // Cleaning the first sms provider created.
        response =
                getResponseOfDelete(NOTIFICATION_SENDER_API_BASE_PATH + PATH_SEPARATOR + SMS_SENDERS_PATH + PATH_SEPARATOR +
                smsNotificationSenderName);
        response.then().log().ifValidationFails().assertThat().statusCode(HttpStatus.SC_NO_CONTENT);
    }

    @Test
    public void testAddSmsSenderWithNonExistingEventPublisherName() throws IOException {

        String body = readResource("add-sms-sender-2.json");
        Response response =
                getResponseOfPost(NOTIFICATION_SENDER_API_BASE_PATH + PATH_SEPARATOR + SMS_SENDERS_PATH, body);

        validateErrorResponse(response, HttpStatus.SC_NOT_FOUND, "NSM-60001");
    }

    @Test
    public void testAddSmsSenderWithUndefinedSmsProvider() throws IOException {

        String body = readResource("add-sms-sender-invalid-provider.json");
        Response response =
                getResponseOfPost(NOTIFICATION_SENDER_API_BASE_PATH + PATH_SEPARATOR + SMS_SENDERS_PATH, body);

        validateErrorResponse(response, HttpStatus.SC_BAD_REQUEST, "NSM-60004");
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
}

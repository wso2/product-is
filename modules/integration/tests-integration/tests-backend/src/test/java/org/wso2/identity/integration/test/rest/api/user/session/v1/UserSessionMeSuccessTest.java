/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.identity.integration.test.rest.api.user.session.v1;

import io.restassured.RestAssured;
import org.apache.axis2.AxisFault;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.junit.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;

import java.rmi.RemoteException;
import java.util.List;
import javax.xml.xpath.XPathExpressionException;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.Is.is;

/**
 * Test REST API for managing logged in user's sessions.
 */
public class UserSessionMeSuccessTest extends UserSessionTest {

    @Factory(dataProvider = "restAPIUserConfigProvider")
    public UserSessionMeSuccessTest(TestUserMode userMode, String username1, String username2) throws Exception {

        super.init(userMode);
        this.context = isServer;
        this.tenant = context.getContextTenant().getDomain();
        this.session_test_user1 = username1;
        this.session_test_user2 = username2;
        this.authenticatingUserName = session_test_user1;
        this.authenticatingCredential = TEST_USER_PASSWORD;
    }

    @DataProvider(name = "restAPIUserConfigProvider")
    public static Object[][] restAPIUserConfigProvider() {
        return new Object[][]{
                {TestUserMode.SUPER_TENANT_ADMIN, "sessionTestUser1", "sessionTestUser2"}
        };
    }

    @BeforeClass(alwaysRun = true)
    public void init() throws XPathExpressionException, RemoteException {

        super.testInit(API_VERSION, swaggerDefinition, tenant);
        initUrls("me");
    }

    @AfterClass(alwaysRun = true)
    public void testConclude() throws Exception {

        super.conclude();
        cleanUp();
    }

    @BeforeMethod(alwaysRun = true)
    public void testInit() {

        RestAssured.basePath = basePath;
    }

    @AfterMethod(alwaysRun = true)
    public void testFinish() {

        RestAssured.basePath = StringUtils.EMPTY;
    }

    @Test
    public void testGetUserSessions() {

        getResponseOfGet(this.sessionsEndpointURI)
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .log().ifValidationFails()
                .body("size()", is(2))
                .body("userId", notNullValue())
                .body("sessions", notNullValue())
                .body("sessions.applications", notNullValue())
                .body("sessions.applications.subject", notNullValue())
                .body("sessions.applications.appName[0]", notNullValue())
                .body("sessions.applications.appName[1]", notNullValue())
                .body("sessions.applications.appId", notNullValue())
                .body("sessions.userAgent", notNullValue())
                .body("sessions.ip", notNullValue())
                .body("sessions.loginTime", notNullValue())
                .body("sessions.lastAccessTime", notNullValue())
                .body("sessions.id", notNullValue());
    }

    @Test(dependsOnMethods = "testGetUserSessions")
    public void testDeleteUserSessionById() {

        List<String> sessionIdList = getResponseOfGet(this.sessionsEndpointURI).jsonPath().getList("sessions.id");

        String endpointURI = String.format(this.sessionEndpointURI, sessionIdList.get(0));
        getResponseOfDelete(endpointURI).then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NO_CONTENT);

        List<String> newSessionIdList = getResponseOfGet(this.sessionsEndpointURI).jsonPath().getList("sessions.id");

        Assert.assertFalse(newSessionIdList.contains(sessionIdList.get(0)));
    }

    @Test(dependsOnMethods = "testDeleteUserSessionById")
    public void testDeleteUserSessions() {

        getResponseOfDelete(this.sessionsEndpointURI)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NO_CONTENT);

        getResponseOfGet(this.sessionsEndpointURI)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("size()", is(0));
    }
}

/*
 * Copyright (c) 2022, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.identity.integration.test.rest.api.user.session.v1;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;

import java.rmi.RemoteException;
import javax.xml.xpath.XPathExpressionException;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.core.Is.is;

/**
 * Test REST API for managing user's sessions.
 */
public class UserSessionAdminSuccessTest extends UserSessionTest {

    private static final String SESSION_SEARCH_ENDPOINT_URI = "/sessions";
    private static final String PAGE_LINK_NEXT = "next";

    @Factory(dataProvider = "restAPIUserConfigProvider")
    public UserSessionAdminSuccessTest(TestUserMode userMode, String username1, String username2) throws Exception {

        super.init(userMode);
        this.context = isServer;
        this.tenant = context.getContextTenant().getDomain();
        this.session_test_user1 = username1;
        this.session_test_user2 = username2;
    }

    @DataProvider(name = "restAPIUserConfigProvider")
    public static Object[][] restAPIUserConfigProvider() {
        return new Object[][]{
                {TestUserMode.SUPER_TENANT_ADMIN, "sessionTestUser1", "sessionTestUser2"}
        };
    }

    @DataProvider(name = "getValidSearchData")
    public static Object[][] getValidSearchData() {
        return new Object[][]{
                {null, 3},
                {"userAgent co Apache", 3},
                {"loginId sw sessionTestUser1", 2},
                {"appName eq " + SERVICE_PROVIDER_NAME_TRAVELOCITY, 2},
                {"loginId sw sessionTestUser2 and appName eq " + SERVICE_PROVIDER_NAME_AVIS, 1}
        };
    }

    @DataProvider(name = "getInvalidSearchData")
    public static Object[][] getInvalidSearchData() {
        return new Object[][]{
                {"userId eq user1"},
                {"loginTime ge 2021-01-01T12:00:00"},
                {"ipAddress sw 192.168."},
        };
    }

    @BeforeClass(alwaysRun = true)
    public void init() throws XPathExpressionException, RemoteException {

        super.testInit(API_VERSION, swaggerDefinition, tenant);
    }

    @AfterClass(alwaysRun = true)
    public void testConclude() throws Exception {

        super.conclude();
        appMgtclient.deleteApplication(serviceProviderTravelocity.getApplicationName());
        appMgtclient.deleteApplication(serviceProviderAvis.getApplicationName());
        userMgtClient.deleteUser(session_test_user1);
        userMgtClient.deleteUser(session_test_user2);
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
    public void testErrorAtGetSessionsForInvalidUser() {

        authenticateUser(this.session_test_user2);
        getResponseOfGet(getSearchEndpointURI(null, null)).then().assertThat()
                .log().ifValidationFails()
                .statusCode(HttpStatus.SC_FORBIDDEN);
    }

    @Test(dataProvider = "getInvalidSearchData")
    public void testGetSessionsWithInvalidData(String filter) {

        authenticateUser(this.session_test_user1);
        getResponseOfGet(getSearchEndpointURI(null, filter)).then().assertThat()
                .log().ifValidationFails()
                .statusCode(HttpStatus.SC_BAD_REQUEST);
    }

    @Test(dataProvider = "getValidSearchData")
    public void testGetSessionsWithValidData(String filter, Integer expectedResults) {

        authenticateUser(this.session_test_user1);
        getResponseOfGet(getSearchEndpointURI(null, filter)).then().assertThat()
                .log().ifValidationFails()
                .statusCode(HttpStatus.SC_OK)
                .body("Resources", notNullValue())
                .body("Resources.size()", greaterThanOrEqualTo(expectedResults))
                .body("Resources.session", notNullValue())
                .body("Resources.session.userId", notNullValue())
                .body("Resources.session.applications", notNullValue())
                .body("Resources.session.applications.id", notNullValue())
                .body("Resources.session.applications.subject", notNullValue())
                .body("Resources.session.applications.appName", notNullValue())
                .body("Resources.session.applications.appId", notNullValue())
                .body("Resources.session.userAgent", notNullValue())
                .body("Resources.session.ip", notNullValue())
                .body("Resources.session.loginTime", notNullValue())
                .body("Resources.session.lastAccessTime", notNullValue())
                .body("Resources.session.id", notNullValue());
    }

    @Test
    public void testGetSessionsWithPagination() {

        int limit = 1;
        authenticateUser(this.session_test_user1);

        Response page1 = getResponseOfGet(getSearchEndpointURI(limit, null));
        page1.then().assertThat()
                .log().ifValidationFails()
                .statusCode(HttpStatus.SC_OK)
                .body("previous", nullValue())
                .body("next", notNullValue())
                .body("Resources", notNullValue())
                .body("Resources.size()", is(limit));

        Response page2 = getResponseOfGet(getSearchEndpointURIFromResponse(page1, PAGE_LINK_NEXT));
        page2.then().assertThat()
                .log().ifValidationFails()
                .statusCode(HttpStatus.SC_OK)
                .body("previous", notNullValue())
                .body("next", notNullValue())
                .body("Resources", notNullValue())
                .body("Resources.size()", is(limit));

        Response page3 = getResponseOfGet(getSearchEndpointURIFromResponse(page2, PAGE_LINK_NEXT));
        page3.then().assertThat()
                .log().ifValidationFails()
                .statusCode(HttpStatus.SC_OK)
                .body("previous", notNullValue())
                .body("Resources", notNullValue())
                .body("Resources.size()", is(limit));
    }

    private void authenticateUser(String user) {

        this.authenticatingUserName = user + "@" + tenant;
        this.authenticatingCredential = TEST_USER_PASSWORD;
    }

    private String getSearchEndpointURI(Integer limit, String filter) {

        if (filter != null && limit != null) {
            return SESSION_SEARCH_ENDPOINT_URI + "?limit=" + limit + "&filter=" + filter;
        } else if (limit != null) {
            return SESSION_SEARCH_ENDPOINT_URI + "?limit=" + limit;
        } else if (filter != null) {
            return SESSION_SEARCH_ENDPOINT_URI + "?filter=" + filter;
        }
        return SESSION_SEARCH_ENDPOINT_URI;
    }

    private String getSearchEndpointURIFromResponse(Response response, String page) {

        String uri = response.body().jsonPath().get(page);
        return uri.substring(uri.lastIndexOf("/"));
    }
}

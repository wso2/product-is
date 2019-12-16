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

package org.wso2.identity.integration.test.rest.api.user.application.v1;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.apache.http.HttpStatus;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.identity.application.common.model.xsd.ServiceProvider;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;

public class UserDiscoverableApplicationSuccessTest extends UserDiscoverableApplicationServiceTestBase {

    private static final String PAGINATION_LINK_QUERY_PARAM_STRING = "?offset=%d&limit=%d";

    @Factory(dataProvider = "restAPIUserConfigProvider")
    public UserDiscoverableApplicationSuccessTest(TestUserMode userMode) throws Exception {

        super.init(userMode);
        this.context = isServer;
        this.authenticatingUserName = context.getContextTenant().getTenantAdmin().getUserName();
        this.authenticatingCredential = context.getContextTenant().getTenantAdmin().getPassword();
        this.tenant = context.getContextTenant().getDomain();
    }

    @DataProvider(name = "restAPIUserConfigProvider")
    public static Object[][] restAPIUserConfigProvider() {

        return new Object[][]{
                {TestUserMode.SUPER_TENANT_ADMIN},
                {TestUserMode.TENANT_ADMIN}
        };
    }

    @DataProvider(name = "offsetLimitProvider")
    public static Object[][] paginationLimitOffsetProvider() {

        return new Object[][]{
                {0, 5},
                {4, 5},
                {5, 5},
                {10, 5},
                {15, 5},
        };
    }

    @BeforeClass(alwaysRun = true)
    public void testStart() throws Exception {

        super.testInit(API_VERSION, swaggerDefinition, tenant);
        super.testStart();
    }

    @Test(description = "Test listing all applications.")
    public void testListAllApplications() {

        Response response = getResponseOfGet(USER_APPLICATION_ENDPOINT_URI);
        response.then().log().ifValidationFails().assertThat().statusCode(HttpStatus.SC_OK);

        response.then().log().ifValidationFails().assertThat().body("totalResults", equalTo
                (TOTAL_DISCOVERABLE_APP_COUNT));
        response.then().log().ifValidationFails().assertThat().body("startIndex", equalTo(1));
        response.then().log().ifValidationFails().assertThat().body("count", equalTo(TOTAL_DISCOVERABLE_APP_COUNT));
        response.then().log().ifValidationFails().body("links", hasSize(0));
        response.then().log().ifValidationFails()
                .body("applications", hasSize(TOTAL_DISCOVERABLE_APP_COUNT));

        assertForAllApplications(response);

    }

    @Test(description = "Test listing applications with offset and limit.", dataProvider = "offsetLimitProvider")
    public void testListApplicationsWithOffsetLimit(int offset, int limit) {

        Map<String, Object> params = new HashMap<String, Object>() {{
            put("offset", offset);
            put("limit", limit);

        }};
        Response response = getResponseOfGet(USER_APPLICATION_ENDPOINT_URI, params);

        response.then().log().ifValidationFails().assertThat().body("totalResults", equalTo
                (TOTAL_DISCOVERABLE_APP_COUNT));
        response.then().log().ifValidationFails().assertThat().body("startIndex", equalTo(offset + 1));

        // Assert for applications and application count.
        assertApplicationsAndApplicationCountInPaginatedResponse(offset, limit, response);
        assertNextLink(offset, limit, response);
        assertPreviousLink(offset, limit, response);

    }

    @Test(description = "Test filtering applications by name with eq operator.")
    public void testFilterApplicationsByNameForEQ() {

        Map<String, Object> params = new HashMap<String, Object>() {{
            put("filter", "name eq " + serviceProviders.get(0).getApplicationName());

        }};
        Response response = getResponseOfGet(USER_APPLICATION_ENDPOINT_URI, params);
        response.then().log().ifValidationFails().assertThat().body("totalResults", equalTo(1));
        response.then().log().ifValidationFails().assertThat().body("startIndex", equalTo(1));
        response.then().log().ifValidationFails().assertThat().body("count", equalTo(1));

        assertForApplication(serviceProviders.get(0), response);
    }

    @Test(description = "Test filtering applications by name with co operator.")
    public void testFilterApplicationsByNameForCO() {

        Map<String, Object> params = new HashMap<String, Object>() {{
            put("filter", "name co APP");

        }};
        Response response = getResponseOfGet(USER_APPLICATION_ENDPOINT_URI, params);
        response.then().log().ifValidationFails().assertThat().body("totalResults", equalTo
                (TOTAL_DISCOVERABLE_APP_COUNT));
        response.then().log().ifValidationFails().assertThat().body("startIndex", equalTo(1));
        response.then().log().ifValidationFails().assertThat().body("count", equalTo(serviceProviders.size()));
        response.then().log().ifValidationFails().body("applications", hasSize(serviceProviders.size()));

        //All application created matches the given filter
        assertForAllApplications(response);

    }

    @Test(description = "Test filtering applications by name with sw operator.")
    public void testFilterApplicationsByNameForSW() {

        Map<String, Object> params = new HashMap<String, Object>() {{
            put("filter", "name sw APP");

        }};
        Response response = getResponseOfGet(USER_APPLICATION_ENDPOINT_URI, params);
        response.then().log().ifValidationFails().assertThat().body("totalResults", equalTo
                (TOTAL_DISCOVERABLE_APP_COUNT));
        response.then().log().ifValidationFails().assertThat().body("startIndex", equalTo(1));
        response.then().log().ifValidationFails().assertThat().body("count", equalTo(serviceProviders.size()));
        response.then().log().ifValidationFails().body("applications", hasSize(serviceProviders.size()));

        //All application created matches the given filter
        assertForAllApplications(response);
    }

    @Test(description = "Test filtering applications by name with ew operator.")
    public void testFilteringApplicationsByNameForEW() {

        Map<String, Object> params = new HashMap<String, Object>() {{
            put("filter", "name ew 13");

        }};
        Response response = getResponseOfGet(USER_APPLICATION_ENDPOINT_URI, params);
        response.then().log().ifValidationFails().assertThat().body("totalResults", equalTo(1));
        response.then().log().ifValidationFails().assertThat().body("startIndex", equalTo(1));
        response.then().log().ifValidationFails().assertThat().body("count", equalTo(1));

        // Only 0th index application matches the given filter
        assertForApplication(serviceProviders.get(0), response);
    }

    @Test(description = "Test get application.")
    public void testGetApplication() {

        Response response = getResponseOfGet(USER_APPLICATION_ENDPOINT_URI + "/" + serviceProviders.get(0)
                .getApplicationResourceId());

        response.then().log().ifValidationFails().assertThat().body("id", equalTo(serviceProviders.get(0)
                .getApplicationResourceId()));
        response.then().log().ifValidationFails().assertThat().body("name", equalTo(serviceProviders.get(0)
                .getApplicationName()));
        response.then().log().ifValidationFails().assertThat().body("description", equalTo(serviceProviders.get(0)
                .getDescription()));
    }

    private void assertApplicationsAndApplicationCountInPaginatedResponse(int offset, int limit, Response response) {

        int applicationCount;
        if ((offset + limit) < TOTAL_DISCOVERABLE_APP_COUNT) {
            assertForApplicationsInRange(offset, (offset + limit), response);
            applicationCount = limit;
        } else if (offset < TOTAL_DISCOVERABLE_APP_COUNT && (offset + limit) > TOTAL_DISCOVERABLE_APP_COUNT) {
            assertForApplicationsInRange(offset, TOTAL_DISCOVERABLE_APP_COUNT, response);
            applicationCount = TOTAL_DISCOVERABLE_APP_COUNT - offset;
        } else if (offset > TOTAL_DISCOVERABLE_APP_COUNT) {
            applicationCount = 0;
        } else {
            throw new RuntimeException("Offset: " + offset + " and limit: " + limit + " is not acceptable for this " +
                    "test case.");
        }

        response.then().log().ifValidationFails().assertThat().body("count", equalTo(applicationCount));
        response.then().log().ifValidationFails().body("applications", hasSize(applicationCount));
    }

    private void assertForApplication(ServiceProvider serviceProvider, Response response) {

        response.then().log().ifValidationFails().assertThat().body("applications.id", hasItem(serviceProvider
                .getApplicationResourceId()));
        response.then().log().ifValidationFails().assertThat().body("applications.name", hasItem(serviceProvider
                .getApplicationName()));
        response.then().log().ifValidationFails().assertThat().body("applications.description", hasItem(serviceProvider
                .getDescription()));
    }

    private void assertForApplicationsInRange(int startIndex, int endIndex, Response response) {

        IntStream.range(startIndex, endIndex).forEach(i -> {
            response.then().log().ifValidationFails()
                    .body("applications.find{ it.id == '" + serviceProviders.get(i).getApplicationResourceId() +
                                    "'}.name",
                            equalTo(serviceProviders.get(i).getApplicationName()))
                    .body("applications.find{ it.id == '" + serviceProviders.get(i).getApplicationResourceId() +
                                    "'}.description",
                            equalTo(serviceProviders.get(i).getDescription()));
        });
    }

    private void assertForAllApplications(Response response) {

        serviceProviders.forEach(serviceProvider -> {
            response.then().log().ifValidationFails()
                    .body("applications.find{ it.id == '" + serviceProvider.getApplicationResourceId() + "'}.name",
                            equalTo(serviceProvider.getApplicationName()))
                    .body("applications.find{ it.id == '" + serviceProvider.getApplicationResourceId() + "'}.image",
                            equalTo(serviceProvider.getImageUrl()))
                    .body("applications.find{ it.id == '" + serviceProvider.getApplicationResourceId() + "'}" +
                                    ".accessUrl", equalTo(serviceProvider.getAccessUrl()))
                    .body("applications.find{ it.id == '" + serviceProvider.getApplicationResourceId() + "'}" +
                                    ".description",
                            equalTo(serviceProvider.getDescription()));
        });
    }

    private void assertNextLink(int offset, int limit, Response response) {

        if ((offset + limit) < TOTAL_DISCOVERABLE_APP_COUNT) {

            response.then().log().ifValidationFails().body("links.rel", hasItem("next"));
            response.then().log().ifValidationFails().body("links.find { it.rel == 'next'}.href", equalTo
                    (String.format(RestAssured.basePath + USER_APPLICATION_ENDPOINT_URI +
                            PAGINATION_LINK_QUERY_PARAM_STRING, (offset + limit), limit)));
        } else {
            response.then().log().ifValidationFails().body("links", not(hasItem("next")));
        }
    }

    private void assertPreviousLink(int offset, int limit, Response response) {

        if (offset > 0) { // Previous link exists only if offset is greater than 0.
            int expectedOffsetQueryParam;
            int expectedLimitQueryParam;
            response.then().log().ifValidationFails().body("links.rel", hasItem("previous"));
            if ((offset - limit) >= 0) { // A previous page of size 'limit' exists
                expectedOffsetQueryParam = calculateOffsetForPreviousLink(offset, limit, TOTAL_DISCOVERABLE_APP_COUNT);
                expectedLimitQueryParam = limit;
            } else { // A previous page exists but it's size is less than the specified limit
                expectedOffsetQueryParam = 0;
                expectedLimitQueryParam = offset;
            }

            response.then().log().ifValidationFails().body("links.find { it.rel == 'previous'}.href", equalTo
                    (String.format(RestAssured.basePath + USER_APPLICATION_ENDPOINT_URI +
                            PAGINATION_LINK_QUERY_PARAM_STRING, expectedOffsetQueryParam, expectedLimitQueryParam)));

        } else if (offset == 0) {
            response.then().log().ifValidationFails().body("links", not(hasItem("previous")));
        } else {
            throw new RuntimeException("Offset: " + offset + " is not acceptable for this " +
                    "test case.");
        }
    }

    private int calculateOffsetForPreviousLink(int offset, int limit, int total) {

        int newOffset = (offset - limit);
        if (newOffset < total) {
            return newOffset;
        }

        return calculateOffsetForPreviousLink(newOffset, limit, total);
    }
}

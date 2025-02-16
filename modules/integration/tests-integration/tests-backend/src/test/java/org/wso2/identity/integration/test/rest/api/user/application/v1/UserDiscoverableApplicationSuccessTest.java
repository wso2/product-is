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

import io.restassured.response.Response;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.base.MultitenantConstants;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
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

    @BeforeClass(alwaysRun = true)
    public void testStart() throws Exception {

        super.testInit(API_VERSION, swaggerDefinition, tenant);
        super.testStart();
    }

    @Test
    public void testTest() {

    }

    @DataProvider(name = "testListAllApplications")
    public Object[][] testListAllApplications() {
        return new Object[][]{
                {1, false},
                {2, false},
                {3, false},
                {1, true},
                {2, true},
                {3, true}
        };
    }

    @Test(description = "Test listing all applications.", dataProvider = "testListAllApplications")
    public void testListAllApplications(int userNum, boolean isSubOrg) {

        Response response;
        if (isSubOrg) {
            String oldBasePath = this.basePath;
            this.basePath = convertToOrgBasePath(oldBasePath);
            response = getResponseOfGetWithOAuth2(USER_APPLICATION_ENDPOINT_URI, SUB_ORG_USER_TOKENS[userNum - 1]);
            this.basePath = oldBasePath;
        } else {
            response = getResponseOfGetWithOAuth2(USER_APPLICATION_ENDPOINT_URI, USER_TOKENS[userNum - 1]);
        }
        response.then().log().ifValidationFails().assertThat().statusCode(HttpStatus.SC_OK);

        response.then().log().ifValidationFails().assertThat().body("totalResults", equalTo
                (USER_DISCOVERABLE_APPS[userNum - 1].length));
        response.then().log().ifValidationFails().assertThat().body("startIndex", equalTo(1));
        response.then().log().ifValidationFails().assertThat()
                .body("count", equalTo(USER_DISCOVERABLE_APPS[userNum - 1].length));
        response.then().log().ifValidationFails().body("links", hasSize(0));
        response.then().log().ifValidationFails()
                .body("applications", hasSize(USER_DISCOVERABLE_APPS[userNum - 1].length));

        assertForAllApplications(response, userNum, isSubOrg);
    }

    @DataProvider(name = "offsetLimitProvider")
    public static Object[][] paginationLimitOffsetProvider() {

        return new Object[][]{
                {1, false, 0, 5},
                {1, false, 4, 5},
                {1, false, 5, 5},
                {1, false, 10, 5},
                {1, false, 15, 5},
                {1, false, 17, 4},
                {2, false, 0, 5},
                {2, false, 4, 10},
                {2, false, 5, 5},
                {2, false, 10, 7},
                {2, false, 15, 4},
                {2, false, 17, 5},
                {2, false, 25, 3},
                {3, false, 0, 5},
                {3, false, 4, 5},
                {3, false, 5, 4},
                {3, false, 8, 2},
                {1, true, 0, 5},
                {1, true, 4, 5},
                {1, true, 5, 5},
                {1, true, 10, 5},
                {1, true, 15, 5},
                {1, true, 17, 4},
                {2, true, 0, 5},
                {2, true, 4, 10},
                {2, true, 5, 5},
                {2, true, 10, 7},
                {2, true, 15, 4},
                {2, true, 17, 5},
                {2, true, 25, 3},
                {3, true, 0, 5},
                {3, true, 4, 5},
                {3, true, 5, 4},
                {3, true, 8, 2}
        };
    }

    @Test(description = "Test listing applications with offset and limit.", dataProvider = "offsetLimitProvider",
            dependsOnMethods = "testListAllApplications")
    public void testListApplicationsWithOffsetLimit(int userNum, boolean isSubOrg, int offset, int limit) {

        Map<String, Object> params = new HashMap<String, Object>() {{
            put("offset", offset);
            put("limit", limit);

        }};
        Response response;
        if (isSubOrg) {
            String oldBasePath = this.basePath;
            this.basePath = convertToOrgBasePath(oldBasePath);
            response = getResponseOfGetWithOAuth2(USER_APPLICATION_ENDPOINT_URI, SUB_ORG_USER_TOKENS[userNum - 1],
                    params);
            this.basePath = oldBasePath;
        } else {
            response = getResponseOfGetWithOAuth2(USER_APPLICATION_ENDPOINT_URI, USER_TOKENS[userNum - 1], params);
        }

        response.then().log().ifValidationFails().assertThat().body("totalResults", equalTo
                (USER_DISCOVERABLE_APPS[userNum - 1].length));
        response.then().log().ifValidationFails().assertThat().body("startIndex", equalTo(offset + 1));

        // Assert for applications and application count.
        assertApplicationsAndApplicationCountInPaginatedResponse(offset, limit, response, userNum, isSubOrg);
        assertNextLink(offset, limit, response, userNum, isSubOrg);
        assertPreviousLink(offset, limit, response, userNum, isSubOrg);
    }

    @DataProvider(name = "testFilterApplicationsByNameWithAppNum")
    public Object[][] filterApplicationsByNameWithAppNum() {

        return new Object[][]{
                {1, false, 19},
                {1, false, 1},
                {2, false, 16},
                {2, false, 10},
                {3, false, 17},
                {1, true, 19},
                {1, true, 1},
                {2, true, 16},
                {2, true, 10},
                {3, true, 17}
        };
    }

    @Test(description = "Test filtering applications by name with eq operator.",
            dataProvider = "testFilterApplicationsByNameWithAppNum",
            dependsOnMethods = "testListApplicationsWithOffsetLimit")
    public void testFilterApplicationsByNameForEQ(int userNum, boolean isSubOrg, int appNum) {

        Map<String, Object> params = new HashMap<String, Object>() {{
            put("filter", "name eq " + APP_NAME_PREFIX + appNum);

        }};
        Response response;
        if (isSubOrg) {
            String oldBasePath = this.basePath;
            this.basePath = convertToOrgBasePath(oldBasePath);
            response = getResponseOfGetWithOAuth2(USER_APPLICATION_ENDPOINT_URI, SUB_ORG_USER_TOKENS[userNum - 1],
                    params);
            this.basePath = oldBasePath;
        } else {
            response = getResponseOfGetWithOAuth2(USER_APPLICATION_ENDPOINT_URI, USER_TOKENS[userNum - 1], params);
        }
        response.then().log().ifValidationFails().assertThat().body("totalResults", equalTo(1));
        response.then().log().ifValidationFails().assertThat().body("startIndex", equalTo(1));
        response.then().log().ifValidationFails().assertThat().body("count", equalTo(1));

        assertForApplication(appNum, isSubOrg, response);
    }

    @DataProvider(name = "testFilterApplicationsByNameWithoutAppNum")
    public Object[][] filterApplicationsByNameWithoutAppNum() {

        return new Object[][]{
                {1, false},
                {2, false},
                {3, false},
                {1, true},
                {2, true},
                {3, true}
        };
    }

    @Test(description = "Test filtering applications by name with co operator.",
            dataProvider = "testFilterApplicationsByNameWithoutAppNum",
            dependsOnMethods = "testFilterApplicationsByNameForEQ")
    public void testFilterApplicationsByNameForCO(int userNum, boolean isSubOrg) {

        Map<String, Object> params = new HashMap<String, Object>() {{
            put("filter", "name co APP");

        }};
        Response response;
        if (isSubOrg) {
            String oldBasePath = this.basePath;
            this.basePath = convertToOrgBasePath(oldBasePath);
            response = getResponseOfGetWithOAuth2(USER_APPLICATION_ENDPOINT_URI, SUB_ORG_USER_TOKENS[userNum - 1],
                    params);
            this.basePath = oldBasePath;
        } else {
            response = getResponseOfGetWithOAuth2(USER_APPLICATION_ENDPOINT_URI, USER_TOKENS[userNum - 1], params);
        }
        response.then().log().ifValidationFails().assertThat().body("totalResults", equalTo
                (USER_DISCOVERABLE_APPS[userNum - 1].length));
        response.then().log().ifValidationFails().assertThat().body("startIndex", equalTo(1));
        response.then().log().ifValidationFails().assertThat().body("count", equalTo(
                USER_DISCOVERABLE_APPS[userNum - 1].length));
        response.then().log().ifValidationFails()
                .body("applications", hasSize(USER_DISCOVERABLE_APPS[userNum - 1].length));

        //All application created matches the given filter
        assertForAllApplications(response, userNum, isSubOrg);
    }

    @Test(description = "Test filtering applications by name with sw operator.",
            dependsOnMethods = "testFilterApplicationsByNameForCO",
            dataProvider = "testFilterApplicationsByNameWithoutAppNum")
    public void testFilterApplicationsByNameForSW(int userNum, boolean isSubOrg) {

        Map<String, Object> params = new HashMap<String, Object>() {{
            put("filter", "name sw APP");
        }};
        Response response;
        if (isSubOrg) {
            String oldBasePath = this.basePath;
            this.basePath = convertToOrgBasePath(oldBasePath);
            response = getResponseOfGetWithOAuth2(USER_APPLICATION_ENDPOINT_URI, SUB_ORG_USER_TOKENS[userNum - 1],
                    params);
            this.basePath = oldBasePath;
        } else {
            response = getResponseOfGetWithOAuth2(USER_APPLICATION_ENDPOINT_URI, USER_TOKENS[userNum - 1], params);
        }
        response.then().log().ifValidationFails().assertThat().body("totalResults", equalTo
                (USER_DISCOVERABLE_APPS[userNum - 1].length));
        response.then().log().ifValidationFails().assertThat().body("startIndex", equalTo(1));
        response.then().log().ifValidationFails().assertThat().body("count", equalTo(
                USER_DISCOVERABLE_APPS[userNum - 1].length));
        response.then().log().ifValidationFails()
                .body("applications", hasSize(USER_DISCOVERABLE_APPS[userNum - 1].length));

        //All application created matches the given filter
        assertForAllApplications(response, userNum, isSubOrg);
    }

    @Test(description = "Test filtering applications by name with ew operator.",
            dependsOnMethods = "testFilterApplicationsByNameForSW",
            dataProvider = "testFilterApplicationsByNameWithAppNum")
    public void testFilteringApplicationsByNameForEW(int userNum, boolean isSubOrg, int appNum) {

        Map<String, Object> params = new HashMap<String, Object>() {{
            put("filter", "name ew " + appNum);

        }};
        Response response;
        if (isSubOrg) {
            String oldBasePath = this.basePath;
            this.basePath = convertToOrgBasePath(oldBasePath);
            response = getResponseOfGetWithOAuth2(USER_APPLICATION_ENDPOINT_URI, SUB_ORG_USER_TOKENS[userNum - 1],
                    params);
            this.basePath = oldBasePath;
        } else {
            response = getResponseOfGetWithOAuth2(USER_APPLICATION_ENDPOINT_URI, USER_TOKENS[userNum - 1], params);
        }
        response.then().log().ifValidationFails().assertThat().body("totalResults", equalTo(1));
        response.then().log().ifValidationFails().assertThat().body("startIndex", equalTo(1));
        response.then().log().ifValidationFails().assertThat().body("count", equalTo(1));

        // Only 0th index application matches the given filter
        assertForApplication(appNum, isSubOrg, response);
    }

    @DataProvider(name = "testFilterApplicationsByNameWithSpacesWithAppNum")
    public Object[][] filterApplicationsByNameWithSpacesWithAppNum() {

        return new Object[][]{
                {1, false, APP_NAME_WITH_SPACES_APP_NUM},
                {1, false, APP_NAME_WITH_SPACES_APP_NUM_WITHOUT_GROUPS},
                {1, true, APP_NAME_WITH_SPACES_APP_NUM},
                {1, true, APP_NAME_WITH_SPACES_APP_NUM_WITHOUT_GROUPS}
        };
    }

    @Test(description = "Test filtering applications by name with eq operator when name contains spaces.",
            dataProvider = "testFilterApplicationsByNameWithSpacesWithAppNum",
            dependsOnMethods = "testFilteringApplicationsByNameForEW")
    public void testFilterApplicationsByNameWithSpacesForEQ(int userNum, boolean isSubOrg, int appNum) {

        Map<String, Object> params = new HashMap<String, Object>() {{
            put("filter", "name eq " + APP_NAME_WITH_SPACES + appNum);
        }};
        Response response;
        if (isSubOrg) {
            String oldBasePath = this.basePath;
            this.basePath = convertToOrgBasePath(oldBasePath);
            response = getResponseOfGetWithOAuth2(USER_APPLICATION_ENDPOINT_URI, SUB_ORG_USER_TOKENS[userNum - 1],
                    params);
            this.basePath = oldBasePath;
        } else {
            response = getResponseOfGetWithOAuth2(USER_APPLICATION_ENDPOINT_URI, USER_TOKENS[userNum - 1], params);
        }
        response.then().log().ifValidationFails().assertThat().body("totalResults", equalTo(1));
        response.then().log().ifValidationFails().assertThat().body("startIndex", equalTo(1));
        response.then().log().ifValidationFails().assertThat().body("count", equalTo(1));

        assertForApplication(appNum, isSubOrg, response);
    }

    @DataProvider(name = "testFilterApplicationsByNameWithSpaces")
    public Object[][] filterApplicationsByNameWithSpaces() {

        return new Object[][]{
                {1, false},
                {1, true},
        };
    }

    @Test(description = "Test filtering applications by name with co operator when name contains spaces.",
            dataProvider = "testFilterApplicationsByNameWithSpaces",
            dependsOnMethods = "testFilterApplicationsByNameWithSpacesForEQ")
    public void testFilterApplicationsByNameWithSpacesForCO(int userNum, boolean isSubOrg) {

        Map<String, Object> params = new HashMap<String, Object>() {{
            put("filter", "name co " + APP_NAME_WITH_SPACES);

        }};
        Response response;
        if (isSubOrg) {
            String oldBasePath = this.basePath;
            this.basePath = convertToOrgBasePath(oldBasePath);
            response = getResponseOfGetWithOAuth2(USER_APPLICATION_ENDPOINT_URI, SUB_ORG_USER_TOKENS[userNum - 1],
                    params);
            this.basePath = oldBasePath;
        } else {
            response = getResponseOfGetWithOAuth2(USER_APPLICATION_ENDPOINT_URI, USER_TOKENS[userNum - 1], params);
        }
        response.then().log().ifValidationFails().assertThat().body("totalResults", equalTo(2));
        response.then().log().ifValidationFails().assertThat().body("startIndex", equalTo(1));
        response.then().log().ifValidationFails().assertThat().body("count", equalTo(2));

        assertForApplication(APP_NAME_WITH_SPACES_APP_NUM_WITHOUT_GROUPS, isSubOrg, response, 0);
        assertForApplication(APP_NAME_WITH_SPACES_APP_NUM, isSubOrg, response, 1);
    }

    @DataProvider(name = "testGetApplicationByResourceId")
    public Object[][] testGetApplicationByResourceId() {

        return new Object[][] {
                {1, false, 19},
                {2, false, 11},
                {3, false, 15},
                {1, true, 3},
                {2, true, 9},
                {3, true, 13}
        };
    }

    @Test(description = "Test get application.", dataProvider = "testGetApplicationByResourceId",
            dependsOnMethods = "testFilterApplicationsByNameWithSpacesForCO")
    public void testGetApplication(int userNum, boolean isSubOrg, int appNum) {

        Response response;
        if (isSubOrg) {
            String oldBasePath = this.basePath;
            this.basePath = convertToOrgBasePath(oldBasePath);
            response = getResponseOfGetWithOAuth2(
                    USER_APPLICATION_ENDPOINT_URI + URL_SEPARATOR + SUB_ORG_DISCOVERABLE_APP_IDS[appNum - 1],
                    SUB_ORG_USER_TOKENS[userNum - 1]);
            this.basePath = oldBasePath;
        } else {
            response = getResponseOfGetWithOAuth2(
                    USER_APPLICATION_ENDPOINT_URI + URL_SEPARATOR + DISCOVERABLE_APP_IDS[appNum - 1],
                    USER_TOKENS[userNum - 1]);
        }

        response.then().log().ifValidationFails().assertThat()
            .body("id", equalTo(isSubOrg ?
                    SUB_ORG_DISCOVERABLE_APP_IDS[appNum - 1] : DISCOVERABLE_APP_IDS[appNum - 1]))
            .body("name", equalTo(getApplicationName(String.valueOf(appNum))))
            .body("image", isSubOrg ? nullValue() : equalTo(APP_IMAGE_URL))
            .body("accessUrl", equalTo(APP_ACCESS_URL))
            .body("description", equalTo(APP_DESC_PREFIX + appNum));
    }

    /**
     * Assert for applications and application count in paginated response.
     *
     * @param offset   Offset.
     * @param limit    Limit.
     * @param response Response.
     */
    private void assertApplicationsAndApplicationCountInPaginatedResponse(int offset, int limit, Response response,
                                                                          int userNum, boolean isSubOrg) {

        int applicationCount;
        int appCount = USER_DISCOVERABLE_APPS[userNum - 1].length;
        if ((offset + limit) <= appCount) {
            assertForApplicationsInRange(offset, (offset + limit), response, userNum, isSubOrg);
            applicationCount = limit;
        } else if (offset < appCount && (offset + limit) > appCount) {
            assertForApplicationsInRange(offset, appCount, response, userNum, isSubOrg);
            applicationCount = appCount - offset;
        } else if (offset >= appCount) {
            applicationCount = 0;
        } else {
            throw new RuntimeException("Offset: " + offset + " and limit: " + limit + " is not acceptable for this " +
                    "test case.");
        }

        response.then().log().ifValidationFails().assertThat().body("count", equalTo(applicationCount));
        response.then().log().ifValidationFails().body("applications", hasSize(applicationCount));
    }

    /**
     * Assert for application.
     *
     * @param appNum   Application number.
     * @param response Response.
     */
    private void assertForApplication(int appNum, boolean isSubOrg, Response response) {

        assertForApplication(appNum, isSubOrg, response, 0);
    }

    /**
     * Assert for application in the given index.
     *
     * @param appNum   Application number.
     * @param response Response.
     */
    private void assertForApplication(int appNum, boolean isSubOrg, Response response, int appIndex) {

        response.then().log().ifValidationFails()
                .body("applications[" + appIndex + "].name", equalTo(getApplicationName(String.valueOf(appNum))))
                .body("applications[" + appIndex + "].image", isSubOrg ? nullValue() : equalTo(APP_IMAGE_URL))
                .body("applications[" + appIndex + "].accessUrl", equalTo(APP_ACCESS_URL))
                .body("applications[" + appIndex + "].description", equalTo(APP_DESC_PREFIX + appNum));
    }

    /**
     * Assert for applications in the given range.
     *
     * @param startIndex Start index.
     * @param endIndex   End index.
     * @param response   Response.
     */
    private void assertForApplicationsInRange(int startIndex, int endIndex, Response response, int userNum,
                                              boolean isSubOrg) {

        String[] discoverableApps = USER_DISCOVERABLE_APPS[userNum - 1];
        IntStream.range(startIndex, endIndex).forEach(i -> {
            response.then().log().ifValidationFails()
                    .body("applications[" + (i - startIndex) + "].name",
                            equalTo(getApplicationName(discoverableApps[i])))
                    .body("applications[" + (i - startIndex) + "].image",
                            isSubOrg ? nullValue() : equalTo(APP_IMAGE_URL))
                    .body("applications[" + (i - startIndex) + "].accessUrl", equalTo(APP_ACCESS_URL))
                    .body("applications[" + (i - startIndex) + "].description",
                            equalTo(APP_DESC_PREFIX + discoverableApps[i]));
        });
    }

    /**
     * Assert for all applications.
     *
     * @param response Response.
     * @param isSubOrg Whether the user is from a sub organization.
     */
    private void assertForAllApplications(Response response, int userNum, boolean isSubOrg) {

        String[] discoverableApps = USER_DISCOVERABLE_APPS[userNum - 1];
        for (int i = 0; i < discoverableApps.length; i++) {
            response.then().log().ifValidationFails()
                    .body("applications[" + i + "].name", equalTo(getApplicationName(discoverableApps[i])))
                    .body("applications[" + i + "].image", isSubOrg ? nullValue() : equalTo(APP_IMAGE_URL))
                    .body("applications[" + i + "].accessUrl", equalTo(APP_ACCESS_URL))
                    .body("applications[" + i + "].description", equalTo(APP_DESC_PREFIX + discoverableApps[i]));
        }
    }

    /**
     * Assert for next link.
     *
     * @param offset   Offset.
     * @param limit    Limit.
     * @param response Response.
     */
    private void assertNextLink(int offset, int limit, Response response, int userNum, boolean isSubOrg) {

        if ((offset + limit) < USER_DISCOVERABLE_APPS[userNum - 1].length) {
            response.then().log().ifValidationFails().body("links.rel", hasItem("next"));
            response.then().log().ifValidationFails().body("links.find { it.rel == 'next'}.href", equalTo(
                    String.format((isSubOrg ? convertToOrgBasePath(this.basePath) : this.basePath) +
                            USER_APPLICATION_ENDPOINT_URI + PAGINATION_LINK_QUERY_PARAM_STRING, (offset + limit),
                            limit)));
        } else {
            response.then().log().ifValidationFails().body("links", not(hasItem("next")));
        }
    }

    /**
     * Assert for previous link.
     *
     * @param offset   Offset.
     * @param limit    Limit.
     * @param response Response.
     */
    private void assertPreviousLink(int offset, int limit, Response response, int userNum, boolean isSubOrg) {

        if (offset > 0) { // Previous link exists only if offset is greater than 0.
            int expectedOffsetQueryParam;
            int expectedLimitQueryParam;
            response.then().log().ifValidationFails().body("links.rel", hasItem("previous"));
            if ((offset - limit) >= 0) { // A previous page of size 'limit' exists
                expectedOffsetQueryParam =
                        calculateOffsetForPreviousLink(offset, limit, USER_DISCOVERABLE_APPS[userNum - 1].length);
                expectedLimitQueryParam = limit;
            } else { // A previous page exists but it's size is less than the specified limit
                expectedOffsetQueryParam = 0;
                expectedLimitQueryParam = offset;
            }

            response.then().log().ifValidationFails().body("links.find { it.rel == 'previous'}.href", equalTo
                    (String.format((isSubOrg ? convertToOrgBasePath(this.basePath) : this.basePath) +
                            USER_APPLICATION_ENDPOINT_URI +
                            PAGINATION_LINK_QUERY_PARAM_STRING, expectedOffsetQueryParam, expectedLimitQueryParam)));
        } else if (offset == 0) {
            response.then().log().ifValidationFails().body("links", not(hasItem("previous")));
        } else {
            throw new RuntimeException("Offset: " + offset + " is not acceptable for this " +
                    "test case.");
        }
    }

    /**
     * Calculate the offset for the previous link.
     *
     * @param offset Offset.
     * @param limit  Limit.
     * @param total  Total.
     * @return Offset for the previous link.
     */
    private int calculateOffsetForPreviousLink(int offset, int limit, int total) {

        int newOffset = (offset - limit);
        if (newOffset < total) {
            return newOffset;
        }

        return calculateOffsetForPreviousLink(newOffset, limit, total);
    }

    /**
     * Get the organization base path.
     *
     * @param basePath Tenant base path.
     * @return Organization base path.
     */
    private String convertToOrgBasePath(String basePath) {

        if (StringUtils.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, tenant)) {
            return ORGANIZATION_PATH_SPECIFIER + basePath;
        } else {
            return basePath.replace(tenant, tenant + ORGANIZATION_PATH_SPECIFIER);
        }
    }
}

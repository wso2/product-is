/*
 * Copyright (c) 2019-2025, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.identity.integration.test.rest.api.server.application.management.v1;

import io.restassured.response.Response;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;
import org.json.JSONObject;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.identity.application.mgt.ApplicationConstants;

import static org.hamcrest.core.IsNull.notNullValue;
import static org.wso2.identity.integration.test.rest.api.server.application.management.v1.Utils.assertNotBlank;

/**
 * Tests for negative paths of the Application Management REST API.
 */
public class ApplicationManagementFailureTest extends ApplicationManagementBaseTest {

    private static final String INVALID_APPLICATION_ID = "xxx";
    private Set<String> createdApps = new HashSet<>();

    @Factory(dataProvider = "restAPIUserConfigProvider")
    public ApplicationManagementFailureTest(TestUserMode userMode) throws Exception {

        super(userMode);
    }

    @AfterMethod(alwaysRun = true)
    @Override
    public void testFinish() {

        cleanUpApplications(createdApps);
        super.testFinish();
    }

    @Test
    public void testGetApplicationWithInvalidId() {

        Response response =
                getResponseOfGet(APPLICATION_MANAGEMENT_API_BASE_PATH + PATH_SEPARATOR + INVALID_APPLICATION_ID);
        validateErrorResponse(response, HttpStatus.SC_NOT_FOUND, "APP-60006");
    }

    @Test
    public void testGetApplicationsWithSortByQueryParam() {

        Response response = getResponseOfGet(APPLICATION_MANAGEMENT_API_BASE_PATH + "?sortBy=xxx");
        validateErrorResponse(response, HttpStatus.SC_NOT_IMPLEMENTED, "APP-65002");
    }

    @Test
    public void testGetApplicationsWithSortOrderQueryParam() {

        Response response = getResponseOfGet(APPLICATION_MANAGEMENT_API_BASE_PATH + "?sortOrder=ASC");
        validateErrorResponse(response, HttpStatus.SC_NOT_IMPLEMENTED, "APP-65002");
    }

    @Test
    public void testGetApplicationsWithAttributesQueryParam() {

        Response response = getResponseOfGet(APPLICATION_MANAGEMENT_API_BASE_PATH + "?attributes=name,imageUrl");
        validateErrorResponse(response, HttpStatus.SC_BAD_REQUEST, "APP-60505");
    }

    @Test
    public void testGetApplicationsWithInvalidFilterFormat() {

        Response response = getResponseOfGet(APPLICATION_MANAGEMENT_API_BASE_PATH + "?filter=name");
        validateErrorResponse(response, HttpStatus.SC_BAD_REQUEST, "APP-60004");
    }

    @Test
    public void testGetApplicationsWithUnsupportedFilterAttribute() {

        Response response = getResponseOfGet(APPLICATION_MANAGEMENT_API_BASE_PATH + "?filter=id eq abc");
        validateErrorResponse(response, HttpStatus.SC_BAD_REQUEST, "APP-60004");
    }

    @Test
    public void testGetApplicationsWithInvalidFilterOperation() {

        Response response = getResponseOfGet(APPLICATION_MANAGEMENT_API_BASE_PATH + "?filter=name coo abc");
        validateErrorResponse(response, HttpStatus.SC_BAD_REQUEST, "APP-60004");
    }

    @Test
    public void testCreateApplicationWithTemplate() throws Exception {

        JSONObject createRequest = new JSONObject();
        createRequest.put("name", "application create fail");
        String payload = createRequest.toString();

        Response response = getResponseOfPost(APPLICATION_MANAGEMENT_API_BASE_PATH + "?template=dummy", payload);
        validateErrorResponse(response, HttpStatus.SC_NOT_IMPLEMENTED, "APP-65501");
    }

    @Test
    public void testCreateApplicationWithABlankName() throws Exception {

        JSONObject createRequest = new JSONObject();
        createRequest.put("name", "");
        String payload = createRequest.toString();

        Response response = getResponseOfPost(APPLICATION_MANAGEMENT_API_BASE_PATH, payload);
        validateErrorResponse(response, HttpStatus.SC_BAD_REQUEST, "APP-60001");
    }

    @Test
    public void testCreateApplicationWithNameNotAdheringToAllowedRegex() throws Exception {

        JSONObject createRequest = new JSONObject();
        createRequest.put("name", "test@failing.com");
        String payload = createRequest.toString();

        Response response = getResponseOfPost(APPLICATION_MANAGEMENT_API_BASE_PATH, payload);
        validateErrorResponse(response, HttpStatus.SC_BAD_REQUEST, "APP-60001");
    }

    @Test
    public void testCreateApplicationWithResidentSpName() throws Exception {

        JSONObject createRequest = new JSONObject();
        createRequest.put("name", "wso2carbon-local-sp");
        String payload = createRequest.toString();

        Response response = getResponseOfPost(APPLICATION_MANAGEMENT_API_BASE_PATH, payload);
        validateErrorResponse(response, HttpStatus.SC_CONFLICT, "APP-60007");
    }

    @Test
    public void testUpdateAppNameToReservedResidentServiceProviderName() throws Exception {

        // Create an app first
        Response responseOfPost = createApplication("dummy test app");
        String createdAppId = getApplicationId(responseOfPost);
        assertNotBlank(createdAppId);
        // Add the application to be cleaned up at the end.
        createdApps.add(createdAppId);

        JSONObject patchRequest = new JSONObject();
        patchRequest.put("name", ApplicationConstants.LOCAL_SP);

        String applicationPath = APPLICATION_MANAGEMENT_API_BASE_PATH + "/" + createdAppId;
        Response responseOfPatch = getResponseOfPatch(applicationPath, patchRequest.toString());
        validateErrorResponse(responseOfPatch, HttpStatus.SC_FORBIDDEN, "APP-60008");
    }

    @Test
    public void testUpdateAppNameToAnotherExistingAppName() throws Exception {

        String firstAppName = "firstAppName";
        String secondAppName = "secondAppName";

        // Create the first app.
        Response createFirstAppResponse = createApplication(firstAppName);
        String firstAppId = getApplicationId(createFirstAppResponse);
        assertNotBlank(firstAppId);
        createdApps.add(firstAppId);

        // Create the second app.
        Response createSecondAppResponse = createApplication(secondAppName);
        String secondAppId = getApplicationId(createSecondAppResponse);
        assertNotBlank(secondAppId);
        createdApps.add(secondAppId);

        // Try to rename the first app to second app's name.
        JSONObject patchRequest = new JSONObject();
        patchRequest.put("name", secondAppName);

        String applicationPath = APPLICATION_MANAGEMENT_API_BASE_PATH + "/" + firstAppId;
        Response responseOfPatch = getResponseOfPatch(applicationPath, patchRequest.toString());
        validateErrorResponse(responseOfPatch, HttpStatus.SC_CONFLICT, "APP-60007");
    }

    @Test(description = "Tests whether accessUrl value is validated when creating a discoverable app.")
    public void testCreateDiscoverableAppWithoutAccessUrl() throws Exception {

        String payload = readResource("invalid-discoverable-app.json");

        Response response = getResponseOfPost(APPLICATION_MANAGEMENT_API_BASE_PATH, payload);
        validateErrorResponse(response, HttpStatus.SC_BAD_REQUEST, "APP-60001");
    }

    @Test(description = "Test configuring invalid discoverable groups for an application.")
    public void testAddInvalidDiscoverableGroups() throws Exception {

        String payload = readResource("invalid-discoverable-groups.json");
        Response response = getResponseOfPost(APPLICATION_MANAGEMENT_API_BASE_PATH, payload);
        validateErrorResponse(response, HttpStatus.SC_BAD_REQUEST, "APP-60001");
        response.then()
                .body("description", Matchers.equalTo(String.format(
                        "Invalid application configuration for application: 'Test Invalid App' of tenantDomain: %s." +
                                " No group found for the given group ID: 'invalid-id-1'. No group found for the" +
                                " given group ID: 'invalid-id-2'. The provided user store: 'INVALID_USER_STORE_1'" +
                                " is not found.",
                        tenant)));
        JSONObject applicationPayload = new JSONObject(payload);
        JSONObject advancedConfigs = applicationPayload.getJSONObject("advancedConfigurations");
        advancedConfigs.put("discoverableByEndUsers", false);
        payload = applicationPayload.toString();
        response = getResponseOfPost(APPLICATION_MANAGEMENT_API_BASE_PATH, payload);
        validateErrorResponse(response, HttpStatus.SC_BAD_REQUEST, "APP-60001");
        response.then()
                .body("description", Matchers.equalTo(String.format(
                        "Invalid application configuration for application: 'Test Invalid App' of tenantDomain: %s." +
                                " Discoverable groups are defined for a non-discoverable application.",
                        tenant)));
    }

    @Test(description = "Tests whether inbound unique key is validated during application creation.")
    public void testCreateApplicationsWithConflictingInboundKeys() throws Exception {

        // Create the first passive sts app.
        String payload = readResource("create-passive-sts-app.json");
        Response createFirstAppResponse = getResponseOfPost(APPLICATION_MANAGEMENT_API_BASE_PATH, payload);
        createFirstAppResponse.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .header(HttpHeaders.LOCATION, notNullValue());
        // Add app id to be cleaned up after the tests.
        createdApps.add(getApplicationId(createFirstAppResponse));

        String secondAppPayload = readResource("create-passive-sts-app-conflicting-inbound-key.json");
        Response createSecondAppResponse = getResponseOfPost(APPLICATION_MANAGEMENT_API_BASE_PATH, secondAppPayload);
        validateErrorResponse(createSecondAppResponse, HttpStatus.SC_CONFLICT, "APP-60009");

    }

    @Test(description = "Tests whether the allowed CORS origins are validated during application creation.")
    public void testCreateApplicationsWithInvalidAllowedOrigins() throws Exception {

        // Payload with an invalid URI: https//localhost.com, the colon is missing.
        String payload = readResource("create-oauth-app-with-invalid-allowed-origins.json");
        Response createAppResponse = getResponseOfPost(APPLICATION_MANAGEMENT_API_BASE_PATH, payload);

        validateErrorResponse(createAppResponse, HttpStatus.SC_BAD_REQUEST, "APP-60001");
    }

    @Test(description = "Test groups metadata endpoint with an invalid domain.")
    public void testGetGroupsMetadataWithInvalidDomain() {

        Map<String, Object> queryParam = new HashMap<>();
        queryParam.put("domain", "invalid");
        Response response = getResponseOfGetWithQueryParams(GROUPS_METADATA_PATH, queryParam);
        validateErrorResponse(response, HttpStatus.SC_BAD_REQUEST, "APP-60001");
    }

    @DataProvider(name = "testGetGroupsMetadataWithInvalidFilters")
    public Object[][] testGetGroupsMetadataWithInvalidFilters() {

        return new Object[][]{
                { "name eq Group_1_1" },
                { "name sw Gro" },
                { "name ew _1" },
                { "id co 12" }
        };
    }

    @Test(description = "Test groups metadata endpoint with invalid filters.",
            dataProvider = "testGetGroupsMetadataWithInvalidFilters")
    public void testGetGroupsMetadataWithInvalidFilters(String filter) {

        Map<String, Object> queryParam = new HashMap<>();
        queryParam.put("filter", filter);
        Response response = getResponseOfGetWithQueryParams(GROUPS_METADATA_PATH, queryParam);
        validateErrorResponse(response, HttpStatus.SC_BAD_REQUEST, "APP-60004");
    }
}

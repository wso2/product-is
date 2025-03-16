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

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.restassured.response.Response;

import java.util.HashMap;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.identity.application.mgt.ApplicationConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationListItem;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationListResponse;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.GroupBasicInfo;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.testng.Assert.assertNotNull;
import static org.wso2.identity.integration.test.rest.api.server.application.management.v1.Utils.assertNotBlank;
import static org.wso2.identity.integration.test.rest.api.server.application.management.v1.Utils.extractApplicationIdFromLocationHeader;

/**
 * Tests for happy paths of the Application Management REST API.
 */
public class ApplicationManagementSuccessTest extends ApplicationManagementBaseTest {

    private static final String MY_ACCOUNT = "My Account";
    private static final String CREATED_APP_NAME = "My SAMPLE APP";
    private static final String CREATED_APP_TEMPLATE_ID = "Test_template_1";
    private static final String CREATED_APP_TEMPLATE_VERSION = "v1.0.0";
    private static final int GROUPS_COUNT = 2;
    private static final String GROUP_NAME_PREFIX = "Group_1_";
    private String createdAppId;
    private String[] groupIDs;

    @Factory(dataProvider = "restAPIUserConfigProvider")
    public ApplicationManagementSuccessTest(TestUserMode userMode) throws Exception {

        super(userMode);
    }

    @BeforeClass(alwaysRun = true)
    public void testStart() throws Exception {

        super.init();
        groupIDs = super.createGroups(GROUPS_COUNT, GROUP_NAME_PREFIX);
    }

    @AfterClass(alwaysRun = true)
    public void testEnd() throws Exception {

        super.deleteGroups(groupIDs);
        super.testConclude();
    }

    @Test
    public void testGetAllApplications() throws IOException {

        Response response = getResponseOfGet(APPLICATION_MANAGEMENT_API_BASE_PATH);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);
        ObjectMapper jsonWriter = new ObjectMapper(new JsonFactory());
        ApplicationListResponse listResponse = jsonWriter.readValue(response.asString(), ApplicationListResponse.class);

        assertNotNull(listResponse);
        Assert.assertFalse(listResponse.getApplications()
                        .stream()
                        .anyMatch(appBasicInfo -> appBasicInfo.getName().equals(ApplicationConstants.LOCAL_SP)),
                "Default resident service provider '" + ApplicationConstants.LOCAL_SP + "' is listed by the API");

        if (StringUtils.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, tenant)) {
            // Check whether the default "My Account" app exists.
            Assert.assertTrue(listResponse.getApplications()
                            .stream()
                            .anyMatch(appBasicInfo -> appBasicInfo.getName().equals(MY_ACCOUNT)),
                    "Default application 'My Account' is not listed by the API.");
        }
    }

    @Test
    public void testGetAllApplicationsExcludingSystemApps() throws IOException {

        Map<String, Object> params = new HashMap<>();
        params.put("excludeSystemPortals", true);
        Response response = getResponseOfGet(APPLICATION_MANAGEMENT_API_BASE_PATH, params);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);
        ObjectMapper jsonWriter = new ObjectMapper(new JsonFactory());
        ApplicationListResponse listResponse = jsonWriter.readValue(response.asString(), ApplicationListResponse.class);
        Assert.assertFalse(listResponse.getApplications()
                        .stream()
                        .anyMatch(appBasicInfo -> appBasicInfo.getName().equals(ApplicationConstants.CONSOLE_APPLICATION_NAME)),
                "Default resident service provider '" + ApplicationConstants.CONSOLE_APPLICATION_NAME + "' is listed by the API");
        Assert.assertFalse(listResponse.getApplications()
                        .stream()
                        .anyMatch(appBasicInfo -> appBasicInfo.getName().equals(ApplicationConstants.MY_ACCOUNT_APPLICATION_NAME)),
                "Default resident service provider '" + ApplicationConstants.MY_ACCOUNT_APPLICATION_NAME + "' is listed by the API");
    }

    @Test
    public void testGetResidentApplication() throws IOException {

        Response response = getResponseOfGet(RESIDENT_APP_API_BASE_PATH);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .log().ifValidationFails()
                .body("provisioningConfigurations", notNullValue());
    }

    @Test
    public void createApplication() throws Exception {

        String body = readResource("create-basic-application.json");
        body = super.addDiscoverableGroupsToApplicationPayload(new JSONObject(body), "PRIMARY", groupIDs).toString();
        Response responseOfPost = getResponseOfPost(APPLICATION_MANAGEMENT_API_BASE_PATH, body);
        responseOfPost.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .header(HttpHeaders.LOCATION, notNullValue());

        String location = responseOfPost.getHeader(HttpHeaders.LOCATION);
        createdAppId = extractApplicationIdFromLocationHeader(location);
        assertNotBlank(createdAppId);
    }

    @Test(dependsOnMethods = {"createApplication"})
    public void testGetAllApplicationsWithParams() throws IOException {

        Map<String, Object> params = new HashMap<>();
        params.put("attributes", "templateId,templateVersion,advancedConfigurations");
        Response response = getResponseOfGet(APPLICATION_MANAGEMENT_API_BASE_PATH, params);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);
        ObjectMapper jsonWriter = new ObjectMapper(new JsonFactory());
        ApplicationListResponse listResponse = jsonWriter.readValue(response.asString(), ApplicationListResponse.class);

        assertNotNull(listResponse);

        // Verify that the newly created app exists and has the required properties.
        Optional<ApplicationListItem> newlyCreatedAppData = listResponse.getApplications().stream()
                .filter(appBasicInfo -> appBasicInfo.getName().equals(CREATED_APP_NAME)).findFirst();
        Assert.assertTrue(newlyCreatedAppData.isPresent(),
                "Newly Created application '" + CREATED_APP_NAME + "' is not listed by the API.");
        Assert.assertEquals(newlyCreatedAppData.get().getTemplateId(), CREATED_APP_TEMPLATE_ID);
        Assert.assertEquals(newlyCreatedAppData.get().getTemplateVersion(), CREATED_APP_TEMPLATE_VERSION);
        Assert.assertEquals(newlyCreatedAppData.get().getAdvancedConfigurations().getDiscoverableGroups().size(), 1);
        Assert.assertEquals(
                newlyCreatedAppData.get().getAdvancedConfigurations().getDiscoverableGroups().get(0).getUserStore(),
                "PRIMARY");
        Assert.assertEquals(
                newlyCreatedAppData.get().getAdvancedConfigurations().getDiscoverableGroups().get(0).getGroups()
                        .size(),
                GROUPS_COUNT);
        Assert.assertEqualsNoOrder(
                newlyCreatedAppData.get().getAdvancedConfigurations().getDiscoverableGroups().get(0).getGroups()
                        .stream()
                        .map(GroupBasicInfo::getId).toArray(),
                groupIDs);
    }

    @Test(dependsOnMethods = {"createApplication"})
    public void testGetApplicationById() throws Exception {

        getResponseOfGet(APPLICATION_MANAGEMENT_API_BASE_PATH + "/" + createdAppId)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("name", equalTo(CREATED_APP_NAME))
                .body("templateId", equalTo(CREATED_APP_TEMPLATE_ID))
                .body("templateVersion", equalTo(CREATED_APP_TEMPLATE_VERSION))
                .body("advancedConfigurations.discoverableGroups", hasSize(1))
                .body("advancedConfigurations.discoverableGroups[0].userStore", Matchers.equalTo("PRIMARY"))
                .body("advancedConfigurations.discoverableGroups[0].groups[0].id", Matchers.oneOf(groupIDs))
                .body("advancedConfigurations.discoverableGroups[0].groups[1].id", Matchers.oneOf(groupIDs));
    }

    @Test(dependsOnMethods = {"createApplication"})
    public void testGetConfiguredAuthenticatorsOfApplication() throws Exception {

        getResponseOfGet(APPLICATION_MANAGEMENT_API_BASE_PATH + "/" + createdAppId + "/" + "authenticators")
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("size()", is(1));
    }

    @Test(dependsOnMethods = {"createApplication"})
    public void testGetBasicOAuth2ApplicationDetailsWithFilter() throws Exception {

        // Create application with predefined clientId.
        String body = readResource("create-oauth-app-with-predefined-clientid.json");
        Response responseOfPost = getResponseOfPost(APPLICATION_MANAGEMENT_API_BASE_PATH, body);
        responseOfPost.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .header(HttpHeaders.LOCATION, notNullValue());

        String location = responseOfPost.getHeader(HttpHeaders.LOCATION);
        String createdOAuth2AppId = extractApplicationIdFromLocationHeader(location);
        assertNotBlank(createdOAuth2AppId);

        // Perform the eq operation with name filter.
        Map<String, Object> params = new HashMap<>();
        params.put("filter", "name eq OAuth Application With ClientId");
        getResponseOfGet(APPLICATION_MANAGEMENT_API_BASE_PATH, params)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("totalResults", equalTo(1));

        // Perform the eq operation with clientId filter.
        params.put("filter", "clientId eq my_custom_client_id");
        getResponseOfGet(APPLICATION_MANAGEMENT_API_BASE_PATH, params)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("totalResults", equalTo(1));

        // Perform the eq operation with clientId or name filter.
        params.put("filter", "name eq app or clientId eq my_custom_client_id");
        getResponseOfGet(APPLICATION_MANAGEMENT_API_BASE_PATH, params)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("totalResults", equalTo(1));

        // Perform the eq operation with clientId and name filter.
        params.put("filter", "name eq app and clientId eq my_custom_client_id");
        getResponseOfGet(APPLICATION_MANAGEMENT_API_BASE_PATH, params)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("totalResults", equalTo(0));

        params.put("filter", "name eq OAuth Application With ClientId and clientId eq my_custom_client_id");
        getResponseOfGet(APPLICATION_MANAGEMENT_API_BASE_PATH, params)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("totalResults", equalTo(1));

        // Delete the OAuth2 application to release the clientId for other testcases.
        getResponseOfDelete(APPLICATION_MANAGEMENT_API_BASE_PATH + "/" + createdOAuth2AppId)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NO_CONTENT);
    }

    @Test(dependsOnMethods = {"createApplication"})
    public void testGetBasicSAMLApplicationDetailsWithFilter() throws Exception {

        // Create application with predefined SAML issuer.
        String body = readResource("create-saml-app-with-manual-config.json");
        Response responseOfPost = getResponseOfPost(APPLICATION_MANAGEMENT_API_BASE_PATH, body);
        responseOfPost.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .header(HttpHeaders.LOCATION, notNullValue());

        String location = responseOfPost.getHeader(HttpHeaders.LOCATION);
        String createdSAMLAppId = extractApplicationIdFromLocationHeader(location);
        assertNotBlank(createdSAMLAppId);

        // Perform the eq operation with name filter.
        Map<String, Object> params = new HashMap<>();
        params.put("filter", "name eq My SAML App");
        getResponseOfGet(APPLICATION_MANAGEMENT_API_BASE_PATH, params)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("totalResults", equalTo(1));

        // Perform the eq operation with issuer filter.
        params.put("filter", "issuer eq https://sp.wso2.com");
        getResponseOfGet(APPLICATION_MANAGEMENT_API_BASE_PATH, params)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("totalResults", equalTo(1));

        // Perform the eq operation with issuer or name filter.
        params.put("filter", "name eq app or issuer eq https://sp.wso2.com");
        getResponseOfGet(APPLICATION_MANAGEMENT_API_BASE_PATH, params)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("totalResults", equalTo(1));

        // Perform the eq operation with issuer and name filter.
        params.put("filter", "name eq app and issuer eq https://sp.wso2.com");
        getResponseOfGet(APPLICATION_MANAGEMENT_API_BASE_PATH, params)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("totalResults", equalTo(0));

        params.put("filter", "name eq My SAML App and issuer eq https://sp.wso2.com");
        getResponseOfGet(APPLICATION_MANAGEMENT_API_BASE_PATH, params)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("totalResults", equalTo(1));

        // Delete the SAML application to release the issuer for other testcases.
        getResponseOfDelete(APPLICATION_MANAGEMENT_API_BASE_PATH + "/" + createdSAMLAppId)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NO_CONTENT);
    }

    @Test(dependsOnMethods = {"testGetApplicationById", "testGetConfiguredAuthenticatorsOfApplication"})
    public void testDeleteApplicationById() throws Exception {

        getResponseOfDelete(APPLICATION_MANAGEMENT_API_BASE_PATH + "/" + createdAppId)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NO_CONTENT);

        // Verify that the application is not available.
        getResponseOfGet(APPLICATION_MANAGEMENT_API_BASE_PATH + "/" + createdAppId)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @DataProvider(name = "testGetGroupsMetadataFromApplicationEndpoint")
    public Object[][] testGetGroupsMetadataFromApplicationEndpoint() {

        return new Object[][]{
                { null, null, GROUPS_COUNT },
                { null, "name co Gro", GROUPS_COUNT },
                { null,"name co rou", GROUPS_COUNT },
                { null, "name co adm", 1 },
                { null, "name co Group_1_1", 1 },
                { "PRIMARY", "name co Gro", GROUPS_COUNT }
        };
    }

    @Test(description = "Test to get groups metadata from application endpoint.",
            dataProvider = "testGetGroupsMetadataFromApplicationEndpoint")
    public void testGetGroupsMetadataFromApplicationEndpoint(String domain, String filter, int expectedGroupCount) {

        Map<String, Object> queryParams = new HashMap<>();
        if (filter != null) {
            queryParams.put("filter", filter);
        }
        if (domain != null) {
            queryParams.put("domain", domain);
        }
        Response response = getResponseOfGetWithQueryParams(GROUPS_METADATA_PATH, queryParams);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("size()",
                        domain == null && filter == null ? greaterThan(expectedGroupCount) : is(expectedGroupCount));
    }
}

/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.identity.integration.test.rest.api.server.branding.preference.management.v1;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.identity.integration.test.restclients.OAuth2RestClient;
import org.wso2.identity.integration.test.restclients.OrgMgtRestClient;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.testng.Assert.assertNotNull;
import static org.wso2.identity.integration.test.entitlement.EntitlementJSONSupportMultiDecisionProfileTestCase.areJSONObjectsEqual;

/**
 * Tests for happy paths of the managing application branding using Branding Preference Management REST API.
 */
public class AppBrandingPreferenceManagementSuccessTest extends AppBrandingPreferenceManagementTestBase {

    private OrgMgtRestClient orgMgtRestClient;
    private String rootAppId;
    private String l1AppId;
    private String l2AppId;
    private String l1OrgId;
    private String l2OrgId;

    @Factory(dataProvider = "restAPIUserConfigProvider")
    public AppBrandingPreferenceManagementSuccessTest(TestUserMode userMode) throws Exception {

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
    public void init() throws Exception {

        super.testInit(API_VERSION, swaggerDefinition, tenant);
        oAuth2RestClient = new OAuth2RestClient(serverURL, tenantInfo);
        orgMgtRestClient = new OrgMgtRestClient(context, tenantInfo, serverURL,
                new JSONObject(readResource("organization-self-service-apis.json")));

        // Create a three level (including root organization) organization hierarchy.
        l1OrgId = orgMgtRestClient.addOrganization("l1-org");
        l2OrgId = orgMgtRestClient.addSubOrganization("l2-org", l1OrgId);

        // Create a B2B application and share it with all children organizations.
        rootAppId = createTestApp();
        shareAppWithAllChildren(rootAppId);

        l1AppId = oAuth2RestClient.getAppIdUsingAppNameInOrganization(TEST_APP_NAME,
                orgMgtRestClient.switchM2MToken(l1OrgId));
        l2AppId = oAuth2RestClient.getAppIdUsingAppNameInOrganization(TEST_APP_NAME,
                orgMgtRestClient.switchM2MToken(l2OrgId));
    }

    @AfterClass(alwaysRun = true)
    public void testConclude() throws Exception {

        super.conclude();
        deleteTestApp(rootAppId);
        deleteOrgBranding();
        deleteOrganizations();
        oAuth2RestClient.closeHttpClient();
        orgMgtRestClient.closeHttpClient();
    }

    @Test
    public void testResolveRootAppBrandingWithNoBrandingConfigurations() throws Exception {

        Response response = getResponseOfGet(BRANDING_PREFERENCE_RESOLVE_PATH + String.format
                (PREFERENCE_COMPONENT_WITH_QUERY_PARAM, APPLICATION_TYPE, rootAppId, DEFAULT_LOCALE));
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test(dependsOnMethods = {"testResolveRootAppBrandingWithNoBrandingConfigurations"})
    public void testResolveL1AppBrandingWithNoBrandingConfigurations() throws Exception {

        Response response = getResolvedAppBrandingInOrg(l1OrgId, l1AppId);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test(dependsOnMethods = {"testResolveL1AppBrandingWithNoBrandingConfigurations"})
    public void testResolveL2AppBrandingWithNoBrandingConfigurations() throws Exception {

        Response response = getResolvedAppBrandingInOrg(l2OrgId, l2AppId);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test(dependsOnMethods = {"testResolveL2AppBrandingWithNoBrandingConfigurations"})
    public void testResolveRootAppBrandingAfterRootOrgBrandingAddition() throws Exception {

        addRootOrgBrandingPreference();

        Response response = getResponseOfGet(BRANDING_PREFERENCE_RESOLVE_PATH +
                String.format(PREFERENCE_COMPONENT_WITH_QUERY_PARAM, APPLICATION_TYPE, rootAppId,
                        DEFAULT_LOCALE));
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("type", equalTo(ORGANIZATION_TYPE))
                .body("name", equalTo(tenant))
                .body("locale", equalTo(DEFAULT_LOCALE));

        assertBrandingPreferences(ADD_ROOT_ORG_BRANDING_RESOURCE_FILE, response);
    }

    @Test(dependsOnMethods = {"testResolveRootAppBrandingAfterRootOrgBrandingAddition"})
    public void testResolveL1AppBrandingAfterRootOrgBrandingAddition() throws Exception {

        Response response = getResolvedAppBrandingInOrg(l1OrgId, l1AppId);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("type", equalTo(ORGANIZATION_TYPE))
                .body("name", equalTo(tenant))
                .body("locale", equalTo(DEFAULT_LOCALE));

        assertBrandingPreferences(ADD_ROOT_ORG_BRANDING_RESOURCE_FILE, response);
    }

    @Test(dependsOnMethods = {"testResolveL1AppBrandingAfterRootOrgBrandingAddition"})
    public void testResolveL2AppBrandingAfterRootOrgBrandingAddition() throws Exception {

        Response response = getResolvedAppBrandingInOrg(l2OrgId, l2AppId);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("type", equalTo(ORGANIZATION_TYPE))
                .body("name", equalTo(tenant))
                .body("locale", equalTo(DEFAULT_LOCALE));

        assertBrandingPreferences(ADD_ROOT_ORG_BRANDING_RESOURCE_FILE, response);
    }

    @Test(dependsOnMethods = {"testResolveL2AppBrandingAfterRootOrgBrandingAddition"})
    public void testAddRootAppBrandingPreference() throws Exception {

        String body = readResource(ADD_ROOT_APP_BRANDING_RESOURCE_FILE)
                .replace(APP_ID_PLACEHOLDER, rootAppId);
        Response response = getResponseOfPost(BRANDING_PREFERENCE_API_BASE_PATH, body);

        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .header(HttpHeaders.LOCATION, notNullValue())
                .body("type", equalTo(APPLICATION_TYPE))
                .body("name", equalTo(rootAppId))
                .body("locale", equalTo(DEFAULT_LOCALE));
        String location = response.getHeader(HttpHeaders.LOCATION);
        assertNotNull(location);

        assertBrandingPreferences(ADD_ROOT_APP_BRANDING_RESOURCE_FILE, response);
    }

    @Test(dependsOnMethods = {"testAddRootAppBrandingPreference"})
    public void testGetRootAppBrandingAfterRootAppBrandingAddition() throws Exception {

        Response response = getResponseOfGet(BRANDING_PREFERENCE_API_BASE_PATH + String.format
                (PREFERENCE_COMPONENT_WITH_QUERY_PARAM, APPLICATION_TYPE, rootAppId, DEFAULT_LOCALE));
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("type", equalTo(APPLICATION_TYPE))
                .body("name", equalTo(rootAppId))
                .body("locale", equalTo(DEFAULT_LOCALE));

        assertBrandingPreferences(ADD_ROOT_APP_BRANDING_RESOURCE_FILE, response);
    }

    @Test(dependsOnMethods = {"testGetRootAppBrandingAfterRootAppBrandingAddition"})
    public void testResolveRootAppBrandingAfterRootAppBrandingAddition() throws Exception {

        Response response = getResponseOfGet(BRANDING_PREFERENCE_RESOLVE_PATH +
                String.format(PREFERENCE_COMPONENT_WITH_QUERY_PARAM, APPLICATION_TYPE, rootAppId,
                        DEFAULT_LOCALE));
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("type", equalTo(APPLICATION_TYPE))
                .body("name", equalTo(rootAppId))
                .body("locale", equalTo(DEFAULT_LOCALE));

        assertBrandingPreferences(ADD_ROOT_APP_BRANDING_RESOURCE_FILE, response);
    }

    @Test(dependsOnMethods = {"testResolveRootAppBrandingAfterRootAppBrandingAddition"})
    public void testResolveL1AppBrandingAfterRootAppBrandingAddition() throws Exception {

        Response response = getResolvedAppBrandingInOrg(l1OrgId, l1AppId);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("type", equalTo(APPLICATION_TYPE))
                .body("name", equalTo(rootAppId))
                .body("locale", equalTo(DEFAULT_LOCALE));

        assertBrandingPreferences(ADD_ROOT_APP_BRANDING_RESOURCE_FILE, response);
    }

    @Test(dependsOnMethods = {"testResolveL1AppBrandingAfterRootAppBrandingAddition"})
    public void testResolveL2AppBrandingAfterRootAppBrandingAddition() throws Exception {

        Response response = getResolvedAppBrandingInOrg(l2OrgId, l2AppId);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("type", equalTo(APPLICATION_TYPE))
                .body("name", equalTo(rootAppId))
                .body("locale", equalTo(DEFAULT_LOCALE));

        assertBrandingPreferences(ADD_ROOT_APP_BRANDING_RESOURCE_FILE, response);
    }

    @Test(dependsOnMethods = {"testResolveL2AppBrandingAfterRootAppBrandingAddition"})
    public void testUpdateRootAppBrandingPreference() throws Exception {

        String body = readResource(UPDATE_ROOT_APP_BRANDING_RESOURCE_FILE)
                .replace(APP_ID_PLACEHOLDER, rootAppId);
        Response response = getResponseOfPut(BRANDING_PREFERENCE_API_BASE_PATH, body);

        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("type", equalTo(APPLICATION_TYPE))
                .body("name", equalTo(rootAppId))
                .body("locale", equalTo(DEFAULT_LOCALE));

        assertBrandingPreferences(UPDATE_ROOT_APP_BRANDING_RESOURCE_FILE, response);
    }

    @Test(dependsOnMethods = {"testUpdateRootAppBrandingPreference"})
    public void testGetRootAppBrandingAfterRootAppBrandingUpdate() throws Exception {

        Response response = getResponseOfGet(BRANDING_PREFERENCE_API_BASE_PATH + String.format
                (PREFERENCE_COMPONENT_WITH_QUERY_PARAM, APPLICATION_TYPE, rootAppId, DEFAULT_LOCALE));
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("type", equalTo(APPLICATION_TYPE))
                .body("name", equalTo(rootAppId))
                .body("locale", equalTo(DEFAULT_LOCALE));

        assertBrandingPreferences(UPDATE_ROOT_APP_BRANDING_RESOURCE_FILE, response);
    }

    @Test(dependsOnMethods = {"testGetRootAppBrandingAfterRootAppBrandingUpdate"})
    public void testResolveRootAppBrandingAfterRootAppBrandingUpdate() throws Exception {

        Response response = getResponseOfGet(BRANDING_PREFERENCE_RESOLVE_PATH +
                String.format(PREFERENCE_COMPONENT_WITH_QUERY_PARAM, APPLICATION_TYPE, rootAppId,
                        DEFAULT_LOCALE));
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("type", equalTo(APPLICATION_TYPE))
                .body("name", equalTo(rootAppId))
                .body("locale", equalTo(DEFAULT_LOCALE));

        assertBrandingPreferences(UPDATE_ROOT_APP_BRANDING_RESOURCE_FILE, response);
    }

    @Test(dependsOnMethods = {"testResolveRootAppBrandingAfterRootAppBrandingUpdate"})
    public void testResolveL1AppBrandingAfterRootAppBrandingUpdate() throws Exception {

        Response response = getResolvedAppBrandingInOrg(l1OrgId, l1AppId);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("type", equalTo(APPLICATION_TYPE))
                .body("name", equalTo(rootAppId))
                .body("locale", equalTo(DEFAULT_LOCALE));

        assertBrandingPreferences(UPDATE_ROOT_APP_BRANDING_RESOURCE_FILE, response);

    }

    @Test(dependsOnMethods = {"testResolveL1AppBrandingAfterRootAppBrandingUpdate"})
    public void testResolveL2AppBrandingAfterRootAppBrandingUpdate() throws Exception {

        Response response = getResolvedAppBrandingInOrg(l2OrgId, l2AppId);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("type", equalTo(APPLICATION_TYPE))
                .body("name", equalTo(rootAppId))
                .body("locale", equalTo(DEFAULT_LOCALE));

        assertBrandingPreferences(UPDATE_ROOT_APP_BRANDING_RESOURCE_FILE, response);
    }

    @Test(dependsOnMethods = {"testResolveL2AppBrandingAfterRootAppBrandingUpdate"})
    public void testResolveL1AppBrandingAfterL1OrgBrandingAddition() throws Exception {

        addOrgBrandingPreference(l1OrgId, ADD_L1_ORG_BRANDING_RESOURCE_FILE);

        Response response = getResolvedAppBrandingInOrg(l1OrgId, l1AppId);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("type", equalTo(ORGANIZATION_TYPE))
                .body("name", equalTo(l1OrgId))
                .body("locale", equalTo(DEFAULT_LOCALE));

        assertBrandingPreferences(ADD_L1_ORG_BRANDING_RESOURCE_FILE, response);
    }

    @Test(dependsOnMethods = {"testResolveL1AppBrandingAfterL1OrgBrandingAddition"})
    public void testResolveL2AppBrandingAfterL1OrgBrandingAddition() throws Exception {

        Response response = getResolvedAppBrandingInOrg(l2OrgId, l2AppId);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("type", equalTo(ORGANIZATION_TYPE))
                .body("name", equalTo(l1OrgId))
                .body("locale", equalTo(DEFAULT_LOCALE));

        assertBrandingPreferences(ADD_L1_ORG_BRANDING_RESOURCE_FILE, response);
    }

    @Test(dependsOnMethods = {"testResolveL2AppBrandingAfterL1OrgBrandingAddition"})
    public void testAddL1AppBrandingPreference() throws Exception {

        RestAssured.basePath = buildTenantedBasePathForOrg(tenant);
        String body = readResource(ADD_L1_APP_BRANDING_RESOURCE_FILE)
                .replace(APP_ID_PLACEHOLDER, l1AppId);
        Response response = getResponseOfPostWithOAuth2(BRANDING_PREFERENCE_API_BASE_PATH, body,
                orgMgtRestClient.switchM2MToken(l1OrgId));

        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .header(HttpHeaders.LOCATION, notNullValue())
                .body("type", equalTo(APPLICATION_TYPE))
                .body("name", equalTo(l1AppId))
                .body("locale", equalTo(DEFAULT_LOCALE));
        String location = response.getHeader(HttpHeaders.LOCATION);
        assertNotNull(location);

        assertBrandingPreferences(ADD_L1_APP_BRANDING_RESOURCE_FILE, response);
    }

    @Test(dependsOnMethods = {"testAddL1AppBrandingPreference"})
    public void testResolveL1AppBrandingAfterL1AppBrandingAddition() throws Exception {

        Response response = getResolvedAppBrandingInOrg(l1OrgId, l1AppId);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("type", equalTo(APPLICATION_TYPE))
                .body("name", equalTo(l1AppId))
                .body("locale", equalTo(DEFAULT_LOCALE));

        assertBrandingPreferences(ADD_L1_APP_BRANDING_RESOURCE_FILE, response);
    }

    @Test(dependsOnMethods = {"testResolveL1AppBrandingAfterL1AppBrandingAddition"})
    public void testResolveL2AppBrandingAfterL1AppBrandingAddition() throws Exception {

        Response response = getResolvedAppBrandingInOrg(l2OrgId, l2AppId);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("type", equalTo(APPLICATION_TYPE))
                .body("name", equalTo(l1AppId))
                .body("locale", equalTo(DEFAULT_LOCALE));

        assertBrandingPreferences(ADD_L1_APP_BRANDING_RESOURCE_FILE, response);
    }

    @Test(dependsOnMethods = {"testResolveL2AppBrandingAfterL1AppBrandingAddition"})
    public void testUpdateL1AppBrandingPreference() throws Exception {

        RestAssured.basePath = buildTenantedBasePathForOrg(tenant);
        String body = readResource(UPDATE_L1_APP_BRANDING_RESOURCE_FILE)
                .replace(APP_ID_PLACEHOLDER, l1AppId);
        Response response = getResponseOfPutWithOAuth2(BRANDING_PREFERENCE_API_BASE_PATH, body,
                orgMgtRestClient.switchM2MToken(l1OrgId));
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("type", equalTo(APPLICATION_TYPE))
                .body("name", equalTo(l1AppId))
                .body("locale", equalTo(DEFAULT_LOCALE));

        assertBrandingPreferences(UPDATE_L1_APP_BRANDING_RESOURCE_FILE, response);
    }

    @Test(dependsOnMethods = {"testUpdateL1AppBrandingPreference"})
    public void testResolveL1AppBrandingAfterL1AppBrandingUpdate() throws Exception {

        Response response = getResolvedAppBrandingInOrg(l1OrgId, l1AppId);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("type", equalTo(APPLICATION_TYPE))
                .body("name", equalTo(l1AppId))
                .body("locale", equalTo(DEFAULT_LOCALE));

        assertBrandingPreferences(UPDATE_L1_APP_BRANDING_RESOURCE_FILE, response);
    }

    @Test(dependsOnMethods = {"testResolveL1AppBrandingAfterL1AppBrandingUpdate"})
    public void testResolveL2AppBrandingAfterL1AppBrandingUpdate() throws Exception {

        Response response = getResolvedAppBrandingInOrg(l2OrgId, l2AppId);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("type", equalTo(APPLICATION_TYPE))
                .body("name", equalTo(l1AppId))
                .body("locale", equalTo(DEFAULT_LOCALE));

        assertBrandingPreferences(UPDATE_L1_APP_BRANDING_RESOURCE_FILE, response);
    }

    @Test(dependsOnMethods = {"testResolveL2AppBrandingAfterL1AppBrandingUpdate"})
    public void testResolveL2AppBrandingAfterL2OrgBrandingAddition() throws Exception {

        addOrgBrandingPreference(l2OrgId, ADD_L2_ORG_BRANDING_RESOURCE_FILE);

        Response response = getResolvedAppBrandingInOrg(l2OrgId, l2AppId);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("type", equalTo(ORGANIZATION_TYPE))
                .body("name", equalTo(l2OrgId))
                .body("locale", equalTo(DEFAULT_LOCALE));

        assertBrandingPreferences(ADD_L2_ORG_BRANDING_RESOURCE_FILE, response);
    }

    @Test(dependsOnMethods = {"testResolveL2AppBrandingAfterL2OrgBrandingAddition"})
    public void testAddL2AppBrandingPreference() throws Exception {

        RestAssured.basePath = buildTenantedBasePathForOrg(tenant);
        String body = readResource(ADD_L2_APP_BRANDING_RESOURCE_FILE)
                .replace(APP_ID_PLACEHOLDER, l2AppId);
        Response response = getResponseOfPostWithOAuth2(BRANDING_PREFERENCE_API_BASE_PATH, body,
                orgMgtRestClient.switchM2MToken(l2OrgId));

        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .header(HttpHeaders.LOCATION, notNullValue())
                .body("type", equalTo(APPLICATION_TYPE))
                .body("name", equalTo(l2AppId))
                .body("locale", equalTo(DEFAULT_LOCALE));
        String location = response.getHeader(HttpHeaders.LOCATION);
        assertNotNull(location);

        assertBrandingPreferences(ADD_L2_APP_BRANDING_RESOURCE_FILE, response);
    }

    @Test(dependsOnMethods = {"testAddL2AppBrandingPreference"})
    public void testResolveL2AppBrandingAfterL2AppBrandingAddition() throws Exception {

        Response response = getResolvedAppBrandingInOrg(l2OrgId, l2AppId);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("type", equalTo(APPLICATION_TYPE))
                .body("name", equalTo(l2AppId))
                .body("locale", equalTo(DEFAULT_LOCALE));

        assertBrandingPreferences(ADD_L2_APP_BRANDING_RESOURCE_FILE, response);
    }

    @Test(dependsOnMethods = {"testResolveL2AppBrandingAfterL2AppBrandingAddition"})
    public void testUpdateL2AppBrandingPreference() throws Exception {

        RestAssured.basePath = buildTenantedBasePathForOrg(tenant);
        String body = readResource(UPDATE_L2_APP_BRANDING_RESOURCE_FILE)
                .replace(APP_ID_PLACEHOLDER, l2AppId);
        Response response = getResponseOfPutWithOAuth2(BRANDING_PREFERENCE_API_BASE_PATH, body,
                orgMgtRestClient.switchM2MToken(l2OrgId));

        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("type", equalTo(APPLICATION_TYPE))
                .body("name", equalTo(l2AppId))
                .body("locale", equalTo(DEFAULT_LOCALE));

        assertBrandingPreferences(UPDATE_L2_APP_BRANDING_RESOURCE_FILE, response);
    }

    @Test(dependsOnMethods = {"testUpdateL2AppBrandingPreference"})
    public void testResolveL2AppBrandingAfterL2AppBrandingUpdate() throws Exception {

        Response response = getResolvedAppBrandingInOrg(l2OrgId, l2AppId);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("type", equalTo(APPLICATION_TYPE))
                .body("name", equalTo(l2AppId))
                .body("locale", equalTo(DEFAULT_LOCALE));

        assertBrandingPreferences(UPDATE_L2_APP_BRANDING_RESOURCE_FILE, response);
    }

    @Test(dependsOnMethods = {"testResolveL2AppBrandingAfterL2AppBrandingUpdate"})
    public void testDeleteL2AppBrandingPreference() throws Exception {

        RestAssured.basePath = buildTenantedBasePathForOrg(tenant);
        Response response = getResponseOfDeleteWithOAuth2(BRANDING_PREFERENCE_API_BASE_PATH +
                String.format(PREFERENCE_COMPONENT_WITH_QUERY_PARAM, APPLICATION_TYPE, l2AppId,
                        DEFAULT_LOCALE), orgMgtRestClient.switchM2MToken(l2OrgId));
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NO_CONTENT);
    }

    @Test(dependsOnMethods = {"testDeleteL2AppBrandingPreference"})
    public void testDeleteL1AppBrandingPreference() throws Exception {

        RestAssured.basePath = buildTenantedBasePathForOrg(tenant);
        Response response = getResponseOfDeleteWithOAuth2(BRANDING_PREFERENCE_API_BASE_PATH +
                String.format(PREFERENCE_COMPONENT_WITH_QUERY_PARAM, APPLICATION_TYPE, l1AppId,
                        DEFAULT_LOCALE), orgMgtRestClient.switchM2MToken(l1OrgId));
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NO_CONTENT);
    }

    @Test(dependsOnMethods = {"testDeleteL1AppBrandingPreference"})
    public void testDeleteRootAppBrandingPreference() throws IOException {

        Response response = getResponseOfDelete(BRANDING_PREFERENCE_API_BASE_PATH + String.format
                (PREFERENCE_COMPONENT_WITH_QUERY_PARAM, APPLICATION_TYPE, rootAppId, DEFAULT_LOCALE));
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NO_CONTENT);
    }

    private void assertBrandingPreferences(String filename, Response response) throws Exception {

        JSONObject expectedJSONResponse = new JSONObject(readResource(filename));
        JSONObject receivedJSONResponse = new JSONObject(response.asString());

        JSONObject expectedPreference = new JSONObject(expectedJSONResponse.get("preference").toString());
        JSONObject receivedPreference = new JSONObject(receivedJSONResponse.get("preference").toString());
        Assert.assertTrue(areJSONObjectsEqual(expectedPreference, receivedPreference),
                "App branding preference schema in the Response is incorrect.");
    }

    private void deleteOrganizations() throws Exception {

        orgMgtRestClient.deleteSubOrganization(l2OrgId, l1OrgId);
        orgMgtRestClient.deleteOrganization(l1OrgId);
    }

    private void deleteOrgBranding() throws Exception {

        deleteOrgBrandingPreference(l2OrgId);
        deleteOrgBrandingPreference(l1OrgId);
        deleteRootOrgBrandingPreference();
    }

    private void addRootOrgBrandingPreference() throws IOException, JSONException {

        String body = readResource(ADD_ROOT_ORG_BRANDING_RESOURCE_FILE);
        Response response = getResponseOfPost(BRANDING_PREFERENCE_API_BASE_PATH, body);

        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .header(HttpHeaders.LOCATION, notNullValue());
    }

    private void addOrgBrandingPreference(String orgId, String orgBrandingRequestBodyFile) throws Exception {

        RestAssured.basePath = buildTenantedBasePathForOrg(tenant);
        String body = readResource(orgBrandingRequestBodyFile)
                .replace(ORG_NAME_PLACEHOLDER, orgId);
        Response response = getResponseOfPostWithOAuth2(BRANDING_PREFERENCE_API_BASE_PATH, body,
                orgMgtRestClient.switchM2MToken(orgId));

        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .header(HttpHeaders.LOCATION, notNullValue());
    }

    private void deleteRootOrgBrandingPreference() throws Exception {

        RestAssured.basePath = basePath;
        Response response = getResponseOfDelete(BRANDING_PREFERENCE_API_BASE_PATH + String.format
                (PREFERENCE_COMPONENT_WITH_QUERY_PARAM, ORGANIZATION_TYPE, tenant, DEFAULT_LOCALE));
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NO_CONTENT);
        RestAssured.basePath = StringUtils.EMPTY;
    }

    private void deleteOrgBrandingPreference(String orgId) throws Exception {

        RestAssured.basePath = buildTenantedBasePathForOrg(tenant);
        Response response = getResponseOfDeleteWithOAuth2(BRANDING_PREFERENCE_API_BASE_PATH + String.format
                        (PREFERENCE_COMPONENT_WITH_QUERY_PARAM, ORGANIZATION_TYPE, orgId, DEFAULT_LOCALE),
                orgMgtRestClient.switchM2MToken(orgId));
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NO_CONTENT);
    }

    private Response getResolvedAppBrandingInOrg(String orgId, String appId)
            throws Exception {

        RestAssured.basePath = PATH_SEPARATOR + ORGANIZATION_PATH + orgId + PATH_SEPARATOR + API_SERVER_BASE_PATH;
        return getResponseOfGetWithOAuth2(BRANDING_PREFERENCE_RESOLVE_PATH +
                String.format(PREFERENCE_COMPONENT_WITH_QUERY_PARAM, APPLICATION_TYPE, appId,
                        DEFAULT_LOCALE), orgMgtRestClient.switchM2MToken(orgId));
    }

    private String buildTenantedBasePathForOrg(String tenantDomain) {

        if (tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
            return PATH_SEPARATOR + ORGANIZATION_PATH + API_SERVER_BASE_PATH;
        } else {
            return PATH_SEPARATOR + TENANT_PATH + tenantDomain + PATH_SEPARATOR + ORGANIZATION_PATH +
                    API_SERVER_BASE_PATH;
        }
    }
}

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
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
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
import org.wso2.identity.integration.common.clients.Idp.IdentityProviderMgtServiceClient;
import org.wso2.identity.integration.test.restclients.OAuth2RestClient;
import org.wso2.identity.integration.test.restclients.OrgMgtRestClient;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.testng.Assert.assertNotNull;
import static org.wso2.identity.integration.test.util.Utils.areJSONObjectsEqual;

/**
 * Tests for happy paths of the managing application branding using Branding Preference Management REST API.
 */
public class AppBrandingPreferenceManagementSuccessTest extends AppBrandingPreferenceManagementTestBase {

    private OrgMgtRestClient orgMgtRestClient;
    protected IdentityProviderMgtServiceClient identityProviderMgtServiceClient;
    private String rootAppId;
    private String level1AppId;
    private String level2AppId;
    private String level1OrgId;
    private String level2OrgId;

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
        level1OrgId = orgMgtRestClient.addOrganization("l1-org");
        level2OrgId = orgMgtRestClient.addSubOrganization("l2-org", level1OrgId);

        // Create a B2B application and share it with all children organizations.
        rootAppId = createTestApp();
        shareAppWithAllChildren(rootAppId);

        level1AppId = oAuth2RestClient.getAppIdUsingAppNameInOrganization(TEST_APP_NAME,
                orgMgtRestClient.switchM2MToken(level1OrgId));
        level2AppId = oAuth2RestClient.getAppIdUsingAppNameInOrganization(TEST_APP_NAME,
                orgMgtRestClient.switchM2MToken(level2OrgId));
        ConfigurationContext configContext =
                ConfigurationContextFactory.createConfigurationContextFromFileSystem(null, null);
        identityProviderMgtServiceClient =
                new IdentityProviderMgtServiceClient(sessionCookie, backendURL, configContext);
    }

    @AfterClass(alwaysRun = true)
    public void testConclude() throws Exception {

        super.conclude();
        deleteTestApp(rootAppId);
        deleteOrganizations();
        identityProviderMgtServiceClient.deleteIdP("SSO");
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

        Response response = getResolvedAppBrandingInOrg(level1OrgId, level1AppId);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test(dependsOnMethods = {"testResolveL1AppBrandingWithNoBrandingConfigurations"})
    public void testResolveL2AppBrandingWithNoBrandingConfigurations() throws Exception {

        Response response = getResolvedAppBrandingInOrg(level2OrgId, level2AppId);
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

        Response response = getResolvedAppBrandingInOrg(level1OrgId, level1AppId);
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

        Response response = getResolvedAppBrandingInOrg(level2OrgId, level2AppId);
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

        Response response = getResolvedAppBrandingInOrg(level1OrgId, level1AppId);
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

        Response response = getResolvedAppBrandingInOrg(level2OrgId, level2AppId);
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
    public void testUpdateUnpublishedRootAppBrandingPreference() throws Exception {

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

    @Test(dependsOnMethods = {"testUpdateUnpublishedRootAppBrandingPreference"})
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
    public void testResolveRootAppBrandingRestrictedToPublishedAfterRootAppBrandingUpdate() throws Exception {

        // Resolve the branding preference by setting the restrictToPublished query parameter to true.
        Response response = getResponseOfGet(BRANDING_PREFERENCE_RESOLVE_PATH +
                String.format(PREFERENCE_COMPONENT_WITH_QUERY_PARAM + AMPERSAND + RESTRICTED_TO_PUBLISHED_QUERY_PARAM,
                        APPLICATION_TYPE, rootAppId, DEFAULT_LOCALE, TRUE));
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("type", equalTo(ORGANIZATION_TYPE))
                .body("name", equalTo(tenant))
                .body("locale", equalTo(DEFAULT_LOCALE));

        assertBrandingPreferences(ADD_ROOT_ORG_BRANDING_RESOURCE_FILE, response);
    }

    @Test(dependsOnMethods = {"testResolveRootAppBrandingRestrictedToPublishedAfterRootAppBrandingUpdate"})
    public void testResolveL1AppBrandingAfterRootAppBrandingUpdate() throws Exception {

        Response response = getResolvedAppBrandingInOrg(level1OrgId, level1AppId);
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
    public void testResolveL1AppBrandingRestrictedToPublishedAfterRootAppBrandingUpdate() throws Exception {

        // Resolve the branding preference by setting the restrictToPublished query parameter to true.
        Response response = getResolvedAppBrandingRestrictedToPublishedInOrg(level1OrgId, level1AppId);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("type", equalTo(ORGANIZATION_TYPE))
                .body("name", equalTo(tenant))
                .body("locale", equalTo(DEFAULT_LOCALE));

        assertBrandingPreferences(ADD_ROOT_ORG_BRANDING_RESOURCE_FILE, response);
    }

    @Test(dependsOnMethods = {"testResolveL1AppBrandingRestrictedToPublishedAfterRootAppBrandingUpdate"})
    public void testResolveL2AppBrandingAfterRootAppBrandingUpdate() throws Exception {

        Response response = getResolvedAppBrandingInOrg(level2OrgId, level2AppId);
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
    public void testResolveL2AppBrandingRestrictedToPublishedAfterRootAppBrandingUpdate() throws Exception {

        // Resolve the branding preference by setting the restrictToPublished query parameter to true.
        Response response = getResolvedAppBrandingRestrictedToPublishedInOrg(level2OrgId, level2AppId);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("type", equalTo(ORGANIZATION_TYPE))
                .body("name", equalTo(tenant))
                .body("locale", equalTo(DEFAULT_LOCALE));

        assertBrandingPreferences(ADD_ROOT_ORG_BRANDING_RESOURCE_FILE, response);
    }

    @Test(dependsOnMethods = {"testResolveL2AppBrandingRestrictedToPublishedAfterRootAppBrandingUpdate"})
    public void testResolveL1AppBrandingAfterL1OrgBrandingAddition() throws Exception {

        addOrgBrandingPreference(level1OrgId, ADD_L1_ORG_BRANDING_RESOURCE_FILE);

        Response response = getResolvedAppBrandingInOrg(level1OrgId, level1AppId);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("type", equalTo(ORGANIZATION_TYPE))
                .body("name", equalTo(level1OrgId))
                .body("locale", equalTo(DEFAULT_LOCALE));

        assertBrandingPreferences(ADD_L1_ORG_BRANDING_RESOURCE_FILE, response);
    }

    @Test(dependsOnMethods = {"testResolveL1AppBrandingAfterL1OrgBrandingAddition"})
    public void testResolveL2AppBrandingAfterL1OrgBrandingAddition() throws Exception {

        Response response = getResolvedAppBrandingInOrg(level2OrgId, level2AppId);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("type", equalTo(ORGANIZATION_TYPE))
                .body("name", equalTo(level1OrgId))
                .body("locale", equalTo(DEFAULT_LOCALE));

        assertBrandingPreferences(ADD_L1_ORG_BRANDING_RESOURCE_FILE, response);
    }

    @Test(dependsOnMethods = {"testResolveL2AppBrandingAfterL1OrgBrandingAddition"})
    public void testAddL1AppBrandingPreference() throws Exception {

        RestAssured.basePath = buildTenantedBasePathForOrg(tenant);
        String body = readResource(ADD_L1_APP_BRANDING_RESOURCE_FILE)
                .replace(APP_ID_PLACEHOLDER, level1AppId);
        Response response = getResponseOfPostWithOAuth2(BRANDING_PREFERENCE_API_BASE_PATH, body,
                orgMgtRestClient.switchM2MToken(level1OrgId));

        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .header(HttpHeaders.LOCATION, notNullValue())
                .body("type", equalTo(APPLICATION_TYPE))
                .body("name", equalTo(level1AppId))
                .body("locale", equalTo(DEFAULT_LOCALE));
        String location = response.getHeader(HttpHeaders.LOCATION);
        assertNotNull(location);

        assertBrandingPreferences(ADD_L1_APP_BRANDING_RESOURCE_FILE, response);
    }

    @Test(dependsOnMethods = {"testAddL1AppBrandingPreference"})
    public void testResolveL1AppBrandingAfterL1AppBrandingAddition() throws Exception {

        Response response = getResolvedAppBrandingInOrg(level1OrgId, level1AppId);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("type", equalTo(APPLICATION_TYPE))
                .body("name", equalTo(level1AppId))
                .body("locale", equalTo(DEFAULT_LOCALE));

        assertBrandingPreferences(ADD_L1_APP_BRANDING_RESOURCE_FILE, response);
    }

    @Test(dependsOnMethods = {"testResolveL1AppBrandingAfterL1AppBrandingAddition"})
    public void testResolveL2AppBrandingAfterL1AppBrandingAddition() throws Exception {

        Response response = getResolvedAppBrandingInOrg(level2OrgId, level2AppId);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("type", equalTo(APPLICATION_TYPE))
                .body("name", equalTo(level1AppId))
                .body("locale", equalTo(DEFAULT_LOCALE));

        assertBrandingPreferences(ADD_L1_APP_BRANDING_RESOURCE_FILE, response);
    }

    @Test(dependsOnMethods = {"testResolveL2AppBrandingAfterL1AppBrandingAddition"})
    public void testUpdateUnpublishedL1AppBrandingPreference() throws Exception {

        RestAssured.basePath = buildTenantedBasePathForOrg(tenant);
        String body = readResource(UPDATE_L1_APP_BRANDING_RESOURCE_FILE)
                .replace(APP_ID_PLACEHOLDER, level1AppId);
        Response response = getResponseOfPutWithOAuth2(BRANDING_PREFERENCE_API_BASE_PATH, body,
                orgMgtRestClient.switchM2MToken(level1OrgId));
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("type", equalTo(APPLICATION_TYPE))
                .body("name", equalTo(level1AppId))
                .body("locale", equalTo(DEFAULT_LOCALE));

        assertBrandingPreferences(UPDATE_L1_APP_BRANDING_RESOURCE_FILE, response);
    }

    @Test(dependsOnMethods = {"testUpdateUnpublishedL1AppBrandingPreference"})
    public void testResolveL1AppBrandingAfterL1AppBrandingUpdate() throws Exception {

        Response response = getResolvedAppBrandingInOrg(level1OrgId, level1AppId);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("type", equalTo(APPLICATION_TYPE))
                .body("name", equalTo(level1AppId))
                .body("locale", equalTo(DEFAULT_LOCALE));

        assertBrandingPreferences(UPDATE_L1_APP_BRANDING_RESOURCE_FILE, response);
    }

    @Test(dependsOnMethods = {"testResolveL1AppBrandingAfterL1AppBrandingUpdate"})
    public void testResolveL1AppBrandingRestrictedToPublishedAfterL1AppBrandingUpdate() throws Exception {

        // Resolve the branding preference by setting the restrictToPublished query parameter to true.
        Response response = getResolvedAppBrandingRestrictedToPublishedInOrg(level1OrgId, level1AppId);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("type", equalTo(ORGANIZATION_TYPE))
                .body("name", equalTo(level1OrgId))
                .body("locale", equalTo(DEFAULT_LOCALE));

        assertBrandingPreferences(ADD_L1_ORG_BRANDING_RESOURCE_FILE, response);
    }

    @Test(dependsOnMethods = {"testResolveL1AppBrandingAfterL1AppBrandingUpdate"})
    public void testResolveL2AppBrandingAfterL1AppBrandingUpdate() throws Exception {

        Response response = getResolvedAppBrandingInOrg(level2OrgId, level2AppId);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("type", equalTo(APPLICATION_TYPE))
                .body("name", equalTo(level1AppId))
                .body("locale", equalTo(DEFAULT_LOCALE));

        assertBrandingPreferences(UPDATE_L1_APP_BRANDING_RESOURCE_FILE, response);
    }

    @Test(dependsOnMethods = {"testResolveL2AppBrandingAfterL1AppBrandingUpdate"})
    public void testResolveL2AppBrandingRestrictedToPublishedAfterL1AppBrandingUpdate() throws Exception {

        // Resolve the branding preference by setting the restrictToPublished query parameter to true.
        Response response = getResolvedAppBrandingRestrictedToPublishedInOrg(level2OrgId, level2AppId);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("type", equalTo(ORGANIZATION_TYPE))
                .body("name", equalTo(level1OrgId))
                .body("locale", equalTo(DEFAULT_LOCALE));

        assertBrandingPreferences(ADD_L1_ORG_BRANDING_RESOURCE_FILE, response);
    }

    @Test(dependsOnMethods = {"testResolveL2AppBrandingRestrictedToPublishedAfterL1AppBrandingUpdate"})
    public void testResolveL2AppBrandingAfterL2OrgBrandingAddition() throws Exception {

        addOrgBrandingPreference(level2OrgId, ADD_L2_ORG_BRANDING_RESOURCE_FILE);

        Response response = getResolvedAppBrandingInOrg(level2OrgId, level2AppId);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("type", equalTo(ORGANIZATION_TYPE))
                .body("name", equalTo(level2OrgId))
                .body("locale", equalTo(DEFAULT_LOCALE));

        assertBrandingPreferences(ADD_L2_ORG_BRANDING_RESOURCE_FILE, response);
    }

    @Test(dependsOnMethods = {"testResolveL2AppBrandingAfterL2OrgBrandingAddition"})
    public void testAddL2AppBrandingPreference() throws Exception {

        RestAssured.basePath = buildTenantedBasePathForOrg(tenant);
        String body = readResource(ADD_L2_APP_BRANDING_RESOURCE_FILE)
                .replace(APP_ID_PLACEHOLDER, level2AppId);
        Response response = getResponseOfPostWithOAuth2(BRANDING_PREFERENCE_API_BASE_PATH, body,
                orgMgtRestClient.switchM2MToken(level2OrgId));

        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .header(HttpHeaders.LOCATION, notNullValue())
                .body("type", equalTo(APPLICATION_TYPE))
                .body("name", equalTo(level2AppId))
                .body("locale", equalTo(DEFAULT_LOCALE));
        String location = response.getHeader(HttpHeaders.LOCATION);
        assertNotNull(location);

        assertBrandingPreferences(ADD_L2_APP_BRANDING_RESOURCE_FILE, response);
    }

    @Test(dependsOnMethods = {"testAddL2AppBrandingPreference"})
    public void testResolveL2AppBrandingAfterL2AppBrandingAddition() throws Exception {

        Response response = getResolvedAppBrandingInOrg(level2OrgId, level2AppId);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("type", equalTo(APPLICATION_TYPE))
                .body("name", equalTo(level2AppId))
                .body("locale", equalTo(DEFAULT_LOCALE));

        assertBrandingPreferences(ADD_L2_APP_BRANDING_RESOURCE_FILE, response);
    }

    @Test(dependsOnMethods = {"testResolveL2AppBrandingAfterL2AppBrandingAddition"})
    public void testUpdateUnpublishedL2AppBrandingPreference() throws Exception {

        RestAssured.basePath = buildTenantedBasePathForOrg(tenant);
        String body = readResource(UPDATE_L2_APP_BRANDING_RESOURCE_FILE)
                .replace(APP_ID_PLACEHOLDER, level2AppId);
        Response response = getResponseOfPutWithOAuth2(BRANDING_PREFERENCE_API_BASE_PATH, body,
                orgMgtRestClient.switchM2MToken(level2OrgId));

        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("type", equalTo(APPLICATION_TYPE))
                .body("name", equalTo(level2AppId))
                .body("locale", equalTo(DEFAULT_LOCALE));

        assertBrandingPreferences(UPDATE_L2_APP_BRANDING_RESOURCE_FILE, response);
    }

    @Test(dependsOnMethods = {"testUpdateUnpublishedL2AppBrandingPreference"})
    public void testResolveL2AppBrandingAfterL2AppBrandingUpdate() throws Exception {

        Response response = getResolvedAppBrandingInOrg(level2OrgId, level2AppId);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("type", equalTo(APPLICATION_TYPE))
                .body("name", equalTo(level2AppId))
                .body("locale", equalTo(DEFAULT_LOCALE));

        assertBrandingPreferences(UPDATE_L2_APP_BRANDING_RESOURCE_FILE, response);
    }

    @Test(dependsOnMethods = {"testResolveL2AppBrandingAfterL2AppBrandingUpdate"})
    public void testResolveL2AppBrandingRestrictedToPublishedAfterL2AppBrandingUpdate() throws Exception {

        // Resolve the branding preference by setting the restrictToPublished query parameter to true.
        Response response = getResolvedAppBrandingRestrictedToPublishedInOrg(level2OrgId, level2AppId);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("type", equalTo(ORGANIZATION_TYPE))
                .body("name", equalTo(level2OrgId))
                .body("locale", equalTo(DEFAULT_LOCALE));

        assertBrandingPreferences(ADD_L2_ORG_BRANDING_RESOURCE_FILE, response);
    }

    @Test(dependsOnMethods = {"testResolveL2AppBrandingRestrictedToPublishedAfterL2AppBrandingUpdate"})
    public void testDeleteL2AppBrandingPreference() throws Exception {

        RestAssured.basePath = buildTenantedBasePathForOrg(tenant);
        Response response = getResponseOfDeleteWithOAuth2(BRANDING_PREFERENCE_API_BASE_PATH +
                String.format(PREFERENCE_COMPONENT_WITH_QUERY_PARAM, APPLICATION_TYPE, level2AppId,
                        DEFAULT_LOCALE), orgMgtRestClient.switchM2MToken(level2OrgId));
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NO_CONTENT);
    }

    @Test(dependsOnMethods = {"testDeleteL2AppBrandingPreference"})
    public void testResolveL2AppBrandingAfterL2AppBrandingDelete() throws Exception {

        Response response = getResolvedAppBrandingInOrg(level2OrgId, level2AppId);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("type", equalTo(ORGANIZATION_TYPE))
                .body("name", equalTo(level2OrgId))
                .body("locale", equalTo(DEFAULT_LOCALE));

        assertBrandingPreferences(ADD_L2_ORG_BRANDING_RESOURCE_FILE, response);
    }

    @Test(dependsOnMethods = {"testResolveL2AppBrandingAfterL2AppBrandingDelete"})
    public void testResolveL2AppBrandingAfterL2OrgBrandingDelete() throws Exception {

        // Delete level 2 organization's org-level branding preferences.
        deleteOrgBrandingPreference(level2OrgId);

        Response response = getResolvedAppBrandingInOrg(level2OrgId, level2AppId);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("type", equalTo(APPLICATION_TYPE))
                .body("name", equalTo(level1AppId))
                .body("locale", equalTo(DEFAULT_LOCALE));

        assertBrandingPreferences(UPDATE_L1_APP_BRANDING_RESOURCE_FILE, response);
    }

    @Test(dependsOnMethods = {"testResolveL2AppBrandingAfterL2OrgBrandingDelete"})
    public void testDeleteL1AppBrandingPreference() throws Exception {

        RestAssured.basePath = buildTenantedBasePathForOrg(tenant);
        Response response = getResponseOfDeleteWithOAuth2(BRANDING_PREFERENCE_API_BASE_PATH +
                String.format(PREFERENCE_COMPONENT_WITH_QUERY_PARAM, APPLICATION_TYPE, level1AppId,
                        DEFAULT_LOCALE), orgMgtRestClient.switchM2MToken(level1OrgId));
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NO_CONTENT);
    }

    @Test(dependsOnMethods = {"testDeleteL1AppBrandingPreference"})
    public void testResolveL1AppBrandingAfterL1AppBrandingDelete() throws Exception {

        Response response = getResolvedAppBrandingInOrg(level1OrgId, level1AppId);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("type", equalTo(ORGANIZATION_TYPE))
                .body("name", equalTo(level1OrgId))
                .body("locale", equalTo(DEFAULT_LOCALE));

        assertBrandingPreferences(ADD_L1_ORG_BRANDING_RESOURCE_FILE, response);
    }

    @Test(dependsOnMethods = {"testResolveL1AppBrandingAfterL1AppBrandingDelete"})
    public void testResolveL2AppBrandingAfterL1AppBrandingDelete() throws Exception {

        Response response = getResolvedAppBrandingInOrg(level2OrgId, level2AppId);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("type", equalTo(ORGANIZATION_TYPE))
                .body("name", equalTo(level1OrgId))
                .body("locale", equalTo(DEFAULT_LOCALE));

        assertBrandingPreferences(ADD_L1_ORG_BRANDING_RESOURCE_FILE, response);
    }

    @Test(dependsOnMethods = {"testResolveL2AppBrandingAfterL1AppBrandingDelete"})
    public void testResolveL1AppBrandingAfterL1OrgBrandingDelete() throws Exception {

        // Delete level 1 organization's org-level branding preferences.
        deleteOrgBrandingPreference(level1OrgId);

        Response response = getResolvedAppBrandingInOrg(level1OrgId, level1AppId);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("type", equalTo(APPLICATION_TYPE))
                .body("name", equalTo(rootAppId))
                .body("locale", equalTo(DEFAULT_LOCALE));

        assertBrandingPreferences(UPDATE_ROOT_APP_BRANDING_RESOURCE_FILE, response);
    }

    @Test(dependsOnMethods = {"testResolveL1AppBrandingAfterL1OrgBrandingDelete"})
    public void testResolveL2AppBrandingAfterL1OrgBrandingDelete() throws Exception {

        Response response = getResolvedAppBrandingInOrg(level2OrgId, level2AppId);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("type", equalTo(APPLICATION_TYPE))
                .body("name", equalTo(rootAppId))
                .body("locale", equalTo(DEFAULT_LOCALE));

        assertBrandingPreferences(UPDATE_ROOT_APP_BRANDING_RESOURCE_FILE, response);
    }

    @Test(dependsOnMethods = {"testResolveL2AppBrandingAfterL1OrgBrandingDelete"})
    public void testDeleteRootAppBrandingPreference() throws IOException {

        Response response = getResponseOfDelete(BRANDING_PREFERENCE_API_BASE_PATH + String.format
                (PREFERENCE_COMPONENT_WITH_QUERY_PARAM, APPLICATION_TYPE, rootAppId, DEFAULT_LOCALE));
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NO_CONTENT);
    }

    @Test(dependsOnMethods = {"testDeleteRootAppBrandingPreference"})
    public void testGetRootAppBrandingAfterRootAppBrandingDelete() throws Exception {

        Response response = getResponseOfGet(BRANDING_PREFERENCE_API_BASE_PATH + String.format
                (PREFERENCE_COMPONENT_WITH_QUERY_PARAM, APPLICATION_TYPE, rootAppId, DEFAULT_LOCALE));
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test(dependsOnMethods = {"testGetRootAppBrandingAfterRootAppBrandingDelete"})
    public void testResolveRootAppBrandingAfterRootAppBrandingDelete() throws Exception {

        Response response = getResponseOfGet(BRANDING_PREFERENCE_RESOLVE_PATH + String.format
                (PREFERENCE_COMPONENT_WITH_QUERY_PARAM, APPLICATION_TYPE, rootAppId, DEFAULT_LOCALE));

        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("type", equalTo(ORGANIZATION_TYPE))
                .body("name", equalTo(tenant))
                .body("locale", equalTo(DEFAULT_LOCALE));

        assertBrandingPreferences(ADD_ROOT_ORG_BRANDING_RESOURCE_FILE, response);
    }

    @Test(dependsOnMethods = {"testResolveRootAppBrandingAfterRootAppBrandingDelete"})
    public void testResolveL1AppBrandingAfterRootAppBrandingDelete() throws Exception {

        Response response = getResolvedAppBrandingInOrg(level1OrgId, level1AppId);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("type", equalTo(ORGANIZATION_TYPE))
                .body("name", equalTo(tenant))
                .body("locale", equalTo(DEFAULT_LOCALE));

        assertBrandingPreferences(ADD_ROOT_ORG_BRANDING_RESOURCE_FILE, response);
    }

    @Test(dependsOnMethods = {"testResolveL1AppBrandingAfterRootAppBrandingDelete"})
    public void testResolveL2AppBrandingAfterRootAppBrandingDelete() throws Exception {

        Response response = getResolvedAppBrandingInOrg(level2OrgId, level2AppId);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("type", equalTo(ORGANIZATION_TYPE))
                .body("name", equalTo(tenant))
                .body("locale", equalTo(DEFAULT_LOCALE));

        assertBrandingPreferences(ADD_ROOT_ORG_BRANDING_RESOURCE_FILE, response);
    }

    @Test(dependsOnMethods = {"testResolveL2AppBrandingAfterRootAppBrandingDelete"})
    public void testResolveRootAppBrandingAfterRootOrgBrandingDelete() throws Exception {

        // Delete root organization's org-level branding preferences.
        deleteRootOrgBrandingPreference();

        Response response = getResponseOfGet(BRANDING_PREFERENCE_RESOLVE_PATH + String.format
                (PREFERENCE_COMPONENT_WITH_QUERY_PARAM, APPLICATION_TYPE, rootAppId, DEFAULT_LOCALE));
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test(dependsOnMethods = {"testResolveRootAppBrandingAfterRootOrgBrandingDelete"})
    public void testResolveL1AppBrandingAfterRootOrgBrandingDelete() throws Exception {

        Response response = getResolvedAppBrandingInOrg(level1OrgId, level1AppId);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test(dependsOnMethods = {"testResolveL1AppBrandingAfterRootOrgBrandingDelete"})
    public void testResolveL2AppBrandingAfterRootOrgBrandingDelete() throws Exception {

        Response response = getResolvedAppBrandingInOrg(level2OrgId, level2AppId);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NOT_FOUND);
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

        orgMgtRestClient.deleteSubOrganization(level2OrgId, level1OrgId);
        orgMgtRestClient.deleteOrganization(level1OrgId);
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

        Response response = getResponseOfDelete(BRANDING_PREFERENCE_API_BASE_PATH + String.format
                (PREFERENCE_COMPONENT_WITH_QUERY_PARAM, ORGANIZATION_TYPE, tenant, DEFAULT_LOCALE));
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NO_CONTENT);
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

    private Response getResolvedAppBrandingRestrictedToPublishedInOrg(String orgId, String appId)
            throws Exception {

        RestAssured.basePath = PATH_SEPARATOR + ORGANIZATION_PATH + orgId + PATH_SEPARATOR + API_SERVER_BASE_PATH;
        return getResponseOfGetWithOAuth2(BRANDING_PREFERENCE_RESOLVE_PATH +
                String.format(PREFERENCE_COMPONENT_WITH_QUERY_PARAM + AMPERSAND + RESTRICTED_TO_PUBLISHED_QUERY_PARAM,
                        APPLICATION_TYPE, appId, DEFAULT_LOCALE, TRUE), orgMgtRestClient.switchM2MToken(orgId));
    }

    private String buildTenantedBasePathForOrg(String tenantDomain) {

        if (tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
            return PATH_SEPARATOR + ORGANIZATION_PATH + API_SERVER_BASE_PATH;
        }
        return PATH_SEPARATOR + TENANT_PATH + tenantDomain + PATH_SEPARATOR + ORGANIZATION_PATH +
                API_SERVER_BASE_PATH;
    }
}

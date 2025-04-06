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

import io.restassured.response.Response;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.identity.integration.test.restclients.OAuth2RestClient;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.testng.Assert.assertNotNull;

/**
 * Tests for error scenarios when managing application branding using Branding Preference Management REST API.
 */
public class AppBrandingPreferenceManagementFailureTest extends AppBrandingPreferenceManagementTestBase {

    private static final String ADD_EMPTY_APP_BRANDING_RESOURCE_FILE = "add-empty-app-branding-preference.json";

    private String testAppId;

    @Factory(dataProvider = "restAPIUserConfigProvider")
    public AppBrandingPreferenceManagementFailureTest(TestUserMode userMode) throws Exception {

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
        testAppId = createTestApp();
    }

    @AfterClass(alwaysRun = true)
    public void testConclude() throws Exception {

        super.conclude();
        deleteTestApp(testAppId);
        oAuth2RestClient.closeHttpClient();
        identityProviderMgtServiceClient.deleteIdP("SSO");
        identityProviderMgtServiceClient = null;
    }

    @Test
    public void testAddBrandingPreferenceWithEmptyJsonPreference() throws Exception {

        String body = readResource(ADD_EMPTY_APP_BRANDING_RESOURCE_FILE);
        Response response = getResponseOfPost(BRANDING_PREFERENCE_API_BASE_PATH, body);
        validateErrorResponse(response, HttpStatus.SC_BAD_REQUEST, "BPM-60001");
    }

    @Test
    public void testAddBrandingPreferenceConflict() throws Exception {

        // Add app branding preference.
        addValidAppBranding();

        // Add conflicting app branding preference.
        String body = readResource(ADD_ROOT_APP_BRANDING_RESOURCE_FILE).replace(APP_ID_PLACEHOLDER, testAppId);
        Response conflictingResponse = getResponseOfPost(BRANDING_PREFERENCE_API_BASE_PATH, body);
        validateErrorResponse(conflictingResponse, HttpStatus.SC_CONFLICT, "BPM-60003");

        // Delete app branding preference.
        deleteValidAppBranding();
    }

    @Test
    public void testGetNotExistingAppBrandingPreference() {

        Response response = getResponseOfGet(BRANDING_PREFERENCE_API_BASE_PATH + String.format
                (PREFERENCE_COMPONENT_WITH_QUERY_PARAM, APPLICATION_TYPE, testAppId, DEFAULT_LOCALE));
        validateErrorResponse(response, HttpStatus.SC_NOT_FOUND, "BPM-60002");
    }

    @Test
    public void testResolveNotExistingAppBrandingPreference() {

        Response response = getResponseOfGet(BRANDING_PREFERENCE_RESOLVE_PATH + String.format
                (PREFERENCE_COMPONENT_WITH_QUERY_PARAM, APPLICATION_TYPE, testAppId, DEFAULT_LOCALE));
        validateErrorResponse(response, HttpStatus.SC_NOT_FOUND, "BPM-60002");
    }

    @Test
    public void testUpdateNotExistingAppBrandingPreference() throws Exception {

        String body = readResource(UPDATE_ROOT_APP_BRANDING_RESOURCE_FILE).replace(APP_ID_PLACEHOLDER, testAppId);
        Response response = getResponseOfPut(BRANDING_PREFERENCE_API_BASE_PATH, body);
        validateErrorResponse(response, HttpStatus.SC_NOT_FOUND, "BPM-60002");
    }

    @Test
    public void testUpdateAppBrandingPreferenceWithEmptyJsonPreference() throws Exception {

        // Add app branding preference.
        addValidAppBranding();

        // Update Branding Preference with empty JSON preference.
        String body = readResource(ADD_EMPTY_APP_BRANDING_RESOURCE_FILE);
        Response response = getResponseOfPut(BRANDING_PREFERENCE_API_BASE_PATH, body);
        validateErrorResponse(response, HttpStatus.SC_BAD_REQUEST, "BPM-60001");

        // Delete app branding preference.
        deleteValidAppBranding();
    }

    @Test
    public void testDeleteNotExistingAppBrandingPreference() {

        Response response = getResponseOfDelete(BRANDING_PREFERENCE_API_BASE_PATH + String.format
                (PREFERENCE_COMPONENT_WITH_QUERY_PARAM, APPLICATION_TYPE, testAppId, DEFAULT_LOCALE));
        validateErrorResponse(response, HttpStatus.SC_NOT_FOUND, "BPM-60002");
    }

    private void addValidAppBranding() throws Exception {

        String body = readResource(ADD_ROOT_APP_BRANDING_RESOURCE_FILE).replace(APP_ID_PLACEHOLDER, testAppId);
        Response creationResponse = getResponseOfPost(BRANDING_PREFERENCE_API_BASE_PATH, body);
        creationResponse.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .header(HttpHeaders.LOCATION, notNullValue());
        String location = creationResponse.getHeader(HttpHeaders.LOCATION);
        assertNotNull(location);
    }

    private void deleteValidAppBranding() {

        Response deletionResponse = getResponseOfDelete(BRANDING_PREFERENCE_API_BASE_PATH + String.format
                (PREFERENCE_COMPONENT_WITH_QUERY_PARAM, APPLICATION_TYPE, testAppId, DEFAULT_LOCALE));
        deletionResponse.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NO_CONTENT);
    }
}

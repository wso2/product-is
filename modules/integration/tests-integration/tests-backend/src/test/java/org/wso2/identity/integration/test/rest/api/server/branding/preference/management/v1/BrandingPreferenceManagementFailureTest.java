/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.com).
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

package org.wso2.identity.integration.test.rest.api.server.branding.preference.management.v1;

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

import java.io.IOException;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.testng.Assert.assertNotNull;

/**
 * Test class for Branding Preference Management REST APIs failure paths.
 */
public class BrandingPreferenceManagementFailureTest extends BrandingPreferenceManagementTestBase {

    @Factory(dataProvider = "restAPIUserConfigProvider")
    public BrandingPreferenceManagementFailureTest(TestUserMode userMode) throws Exception {

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

    @Test
    public void testAddBrandingPreferenceWithEmptyJsonPreference() throws IOException {

        String body = readResource("add-empty-json-preference.json");
        Response response = getResponseOfPost(BRANDING_PREFERENCE_API_BASE_PATH, body);
        validateErrorResponse(response, HttpStatus.SC_BAD_REQUEST, "BPM-60001");
    }

    @Test
    public void testAddBrandingPreferenceConflict() throws IOException {

        String body = readResource("add-branding-preference.json");
        // Add Branding Preference.
        Response response = getResponseOfPost(BRANDING_PREFERENCE_API_BASE_PATH, body);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .header(HttpHeaders.LOCATION, notNullValue());
        String location = response.getHeader(HttpHeaders.LOCATION);
        assertNotNull(location);

        // Add conflicting Branding Preference.
        response = getResponseOfPost(BRANDING_PREFERENCE_API_BASE_PATH, body);
        validateErrorResponse(response, HttpStatus.SC_CONFLICT, "BPM-60003");

        // Delete Branding Preference.
        response = getResponseOfDelete(BRANDING_PREFERENCE_API_BASE_PATH);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NO_CONTENT);
    }

    @Test
    public void testGetNotExistingBrandingPreference() {

        Response response = getResponseOfGet(BRANDING_PREFERENCE_API_BASE_PATH);
        validateErrorResponse(response, HttpStatus.SC_NOT_FOUND, "BPM-60002");
    }

    @Test
    public void testUpdateNotExistingBrandingPreference() throws IOException {

        String body = readResource("update-branding-preference.json");
        Response response = getResponseOfPut(BRANDING_PREFERENCE_API_BASE_PATH, body);
        validateErrorResponse(response, HttpStatus.SC_NOT_FOUND, "BPM-60002");
    }

    @Test
    public void testUpdateBrandingPreferenceWithEmptyJsonPreference() throws IOException {

        String body = readResource("add-branding-preference.json");
        // Add Branding Preference.
        Response response = getResponseOfPost(BRANDING_PREFERENCE_API_BASE_PATH, body);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .header(HttpHeaders.LOCATION, notNullValue());
        String location = response.getHeader(HttpHeaders.LOCATION);
        assertNotNull(location);

        // Update Branding Preference with empty JSON preference.
        body = readResource("add-empty-json-preference.json");
        response = getResponseOfPut(BRANDING_PREFERENCE_API_BASE_PATH, body);
        validateErrorResponse(response, HttpStatus.SC_BAD_REQUEST, "BPM-60001");

        // Delete Branding Preference.
        response = getResponseOfDelete(BRANDING_PREFERENCE_API_BASE_PATH);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NO_CONTENT);
    }

    @Test
    public void testDeleteNotExistingBrandingPreference() {

        Response response = getResponseOfDelete(BRANDING_PREFERENCE_API_BASE_PATH);
        validateErrorResponse(response, HttpStatus.SC_NOT_FOUND, "BPM-60002");
    }
}

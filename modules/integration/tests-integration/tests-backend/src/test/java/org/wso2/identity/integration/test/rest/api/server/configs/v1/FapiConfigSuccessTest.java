/*
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.identity.integration.test.rest.api.server.configs.v1;

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

import java.io.IOException;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.Matchers.notNullValue;

/**
 * Integration tests for the tenant FAPI configuration API (GET/PATCH /configs/fapi).
 * Covers Group 1 (happy paths) and Group 5 (OIDC metadata reflection) from the test plan.
 */
public class FapiConfigSuccessTest extends ConfigTestBase {

    private static final String FAPI1_ADVANCED = "FAPI1_ADVANCED";
    private static final String FAPI2_SECURITY = "FAPI2_SECURITY";
    private static final String OIDC_METADATA_PATH = "/applications/meta/inbound-protocols/oidc";

    @Factory(dataProvider = "restAPIUserConfigProvider")
    public FapiConfigSuccessTest(TestUserMode userMode) throws Exception {

        super.init(userMode);
        this.context = isServer;
        this.authenticatingUserName = context.getContextTenant().getTenantAdmin().getUserName();
        this.authenticatingCredential = context.getContextTenant().getTenantAdmin().getPassword();
        this.tenant = context.getContextTenant().getDomain();
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

    @DataProvider(name = "restAPIUserConfigProvider")
    public static Object[][] restAPIUserConfigProvider() {

        return new Object[][]{
                {TestUserMode.SUPER_TENANT_ADMIN},
                {TestUserMode.TENANT_ADMIN}
        };
    }

    // Group 1: Tenant FAPI Config — Happy Paths

    /**
     * 1.1 - GET /configs/fapi returns the server default when no configuration has been persisted.
     * Expected: enabled=true, supportedProfiles=["FAPI1_ADVANCED"]
     */
    @Test
    public void testGetDefaultFapiConfig() {

        Response response = getResponseOfGet(FAPI_CONFIGS_API_BASE_PATH);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("enabled", equalTo(true))
                .body("supportedProfiles", hasItem(FAPI1_ADVANCED));
    }

    /**
     * 1.2 - PATCH /configs/fapi to enable coexistence (both FAPI1_ADVANCED and FAPI2_SECURITY).
     */
    @Test(dependsOnMethods = "testGetDefaultFapiConfig")
    public void testPatchFapiConfigCoexistence() throws IOException {

        String body = readResource("fapi-config-coexistence.json");
        Response response = getResponseOfPatch(FAPI_CONFIGS_API_BASE_PATH, body);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("enabled", equalTo(true))
                .body("supportedProfiles", hasItems(FAPI1_ADVANCED, FAPI2_SECURITY));
    }

    /**
     * 1.3 - GET after PATCH confirms coexistence config is persisted.
     */
    @Test(dependsOnMethods = "testPatchFapiConfigCoexistence")
    public void testGetFapiConfigCoexistencePersistence() {

        Response response = getResponseOfGet(FAPI_CONFIGS_API_BASE_PATH);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("enabled", equalTo(true))
                .body("supportedProfiles", hasItems(FAPI1_ADVANCED, FAPI2_SECURITY));
    }

    /**
     * 1.4 - PATCH to FAPI2 only (migration scenario).
     */
    @Test(dependsOnMethods = "testGetFapiConfigCoexistencePersistence")
    public void testPatchFapiConfigFapi2Only() throws IOException {

        String body = readResource("fapi-config-fapi2-only.json");
        Response response = getResponseOfPatch(FAPI_CONFIGS_API_BASE_PATH, body);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("enabled", equalTo(true))
                .body("supportedProfiles", hasItem(FAPI2_SECURITY));

        List<String> profiles = response.jsonPath().getList("supportedProfiles");
        org.testng.Assert.assertEquals(profiles.size(), 1, "Should have only FAPI2_SECURITY after migration");
    }

    /**
     * 1.5 - PATCH to disable FAPI enforcement.
     */
    @Test(dependsOnMethods = "testPatchFapiConfigFapi2Only")
    public void testPatchFapiConfigDisabled() throws IOException {

        String body = readResource("fapi-config-disabled.json");
        Response response = getResponseOfPatch(FAPI_CONFIGS_API_BASE_PATH, body);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("enabled", equalTo(false));
    }

    /**
     * 1.6 - PATCH back to FAPI1 only (downgrade / restore default).
     */
    @Test(dependsOnMethods = "testPatchFapiConfigDisabled")
    public void testRestoreFapiConfigToDefault() throws IOException {

        String body = readResource("fapi-config-fapi1-only.json");
        Response response = getResponseOfPatch(FAPI_CONFIGS_API_BASE_PATH, body);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("enabled", equalTo(true))
                .body("supportedProfiles", hasItem(FAPI1_ADVANCED));
    }

    // Group 5: OIDC Metadata — allowedFapiProfiles reflects tenant config

    /**
     * 5.1 - OIDC metadata reflects FAPI1-only tenant configuration.
     */
    @Test(dependsOnMethods = "testRestoreFapiConfigToDefault")
    public void testOidcMetadataReflectsFapi1Only() throws IOException {

        String body = readResource("fapi-config-fapi1-only.json");
        getResponseOfPatch(FAPI_CONFIGS_API_BASE_PATH, body)
                .then().assertThat().statusCode(HttpStatus.SC_OK);

        Response response = getResponseOfGetNoFilter(OIDC_METADATA_PATH);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("fapiMetadata.allowedFapiProfiles.options", notNullValue())
                .body("fapiMetadata.allowedFapiProfiles.options", hasItem(FAPI1_ADVANCED));

        List<String> profiles = response.jsonPath().getList("fapiMetadata.allowedFapiProfiles.options");
        org.testng.Assert.assertEquals(profiles.size(), 1,
                "OIDC metadata should list only FAPI1_ADVANCED when tenant is FAPI1-only");
    }

    /**
     * 5.2 - OIDC metadata reflects both profiles when tenant supports coexistence.
     */
    @Test(dependsOnMethods = "testOidcMetadataReflectsFapi1Only")
    public void testOidcMetadataReflectsBothProfiles() throws IOException {

        String body = readResource("fapi-config-coexistence.json");
        getResponseOfPatch(FAPI_CONFIGS_API_BASE_PATH, body)
                .then().assertThat().statusCode(HttpStatus.SC_OK);

        Response response = getResponseOfGetNoFilter(OIDC_METADATA_PATH);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("fapiMetadata.allowedFapiProfiles.options", hasItems(FAPI1_ADVANCED, FAPI2_SECURITY));
    }

    /**
     * 5.3 - OIDC metadata reflects FAPI2-only after migration.
     */
    @Test(dependsOnMethods = "testOidcMetadataReflectsBothProfiles")
    public void testOidcMetadataReflectsFapi2Only() throws IOException {

        String body = readResource("fapi-config-fapi2-only.json");
        getResponseOfPatch(FAPI_CONFIGS_API_BASE_PATH, body)
                .then().assertThat().statusCode(HttpStatus.SC_OK);

        Response response = getResponseOfGetNoFilter(OIDC_METADATA_PATH);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("fapiMetadata.allowedFapiProfiles.options", hasItem(FAPI2_SECURITY));

        List<String> profiles = response.jsonPath().getList("fapiMetadata.allowedFapiProfiles.options");
        org.testng.Assert.assertEquals(profiles.size(), 1,
                "OIDC metadata should list only FAPI2_SECURITY after full migration");
    }

    /**
     * Restore FAPI config to the server default (FAPI1_ADVANCED, enabled) after all tests.
     */
    @Test(dependsOnMethods = "testOidcMetadataReflectsFapi2Only", alwaysRun = true)
    public void testRestoreAfterMetadataTests() throws IOException {

        String body = readResource("fapi-config-fapi1-only.json");
        getResponseOfPatch(FAPI_CONFIGS_API_BASE_PATH, body)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);
    }
}

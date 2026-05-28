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

package org.wso2.identity.integration.test.rest.api.server.application.management.v1;

import java.io.IOException;

import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import static org.wso2.identity.integration.test.rest.api.server.application.management.v1.Utils.assertNotBlank;
import static org.wso2.identity.integration.test.rest.api.server.application.management.v1.Utils.extractApplicationIdFromLocationHeader;
import org.wso2.identity.integration.test.rest.api.server.configs.v1.ConfigTestBase;

import io.restassured.RestAssured;
import static io.restassured.RestAssured.given;
import io.restassured.config.EncoderConfig;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

/**
 * Integration tests for the application-level FAPI profile field (fapiProfile in OIDC inbound config).
 * Covers Group 3 (happy paths) and Group 4 (error paths) from the test plan.
 *
 * <p>Prerequisite: The tenant FAPI configuration must support both FAPI1_ADVANCED and FAPI2_SECURITY
 * profiles. This is set up in {@link #initFapiConfig()} and restored in {@link #cleanupFapiConfig()}.
 */
public class ApplicationManagementFapiProfileSuccessTest extends ApplicationManagementBaseTest {

    private static final String INBOUND_PROTOCOLS_OIDC_CONTEXT_PATH = "/inbound-protocols/oidc";
    private static final String CONFIGS_FAPI_PATH = "/configs/fapi";
    private static final String FAPI1_ADVANCED = "FAPI1_ADVANCED";
    private static final String FAPI2_SECURITY = "FAPI2_SECURITY";

    private String fapi1AppId;
    private String fapi2AppId;
    private String legacyFapiAppId;

    @Factory(dataProvider = "restAPIUserConfigProvider")
    public ApplicationManagementFapiProfileSuccessTest(TestUserMode userMode) throws Exception {

        super(userMode);
    }

    /**
     * Enable both FAPI profiles at tenant level so that FAPI1 and FAPI2 apps can be created.
     * Runs after the parent's {@code init()} because TestNG always executes the parent's
     * {@code @BeforeClass} before the subclass's {@code @BeforeClass}.
     */
    @BeforeClass(alwaysRun = true)
    public void initFapiConfig() throws IOException {

        String coexistenceBody = readResource("fapi-config-coexistence.json", ConfigTestBase.class);
        given()
                .auth().preemptive().basic(authenticatingUserName, authenticatingCredential)
                .contentType(ContentType.JSON)
                .header(HttpHeaders.ACCEPT, ContentType.JSON)
                .body(coexistenceBody)
                .when()
                .put(basePath + CONFIGS_FAPI_PATH)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);
    }

    /**
     * Delete all test apps and restore the tenant FAPI config to the default (FAPI1_ADVANCED, enabled).
     */
    @AfterClass(alwaysRun = true)
    public void cleanupFapiConfig() throws IOException {

        String defaultBody = readResource("fapi-config-fapi1-only.json", ConfigTestBase.class);
        given()
            .auth().preemptive().basic(authenticatingUserName, authenticatingCredential)
            .contentType(ContentType.JSON)
            .header(HttpHeaders.ACCEPT, ContentType.JSON)
            .body(defaultBody)
            .when()
            .put(basePath + CONFIGS_FAPI_PATH)
            .then()
            .log().ifValidationFails()
            .assertThat()
            .statusCode(HttpStatus.SC_OK);

        RestAssured.basePath = basePath;
        deleteAppIfPresent(fapi1AppId);
        deleteAppIfPresent(fapi2AppId);
        deleteAppIfPresent(legacyFapiAppId);
    }

    // Group 3: App FAPI Profile — Happy Paths

    /**
     * 3.1 - Create a FAPI application with fapiProfile=FAPI1_ADVANCED.
     */
    @Test
    public void testCreateFapi1App() throws IOException {

        String body = readResource("create-fapi1-app.json");
        Response response = getResponseOfPost(APPLICATION_MANAGEMENT_API_BASE_PATH, body);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .header(HttpHeaders.LOCATION, notNullValue());

        String location = response.getHeader(HttpHeaders.LOCATION);
        fapi1AppId = extractApplicationIdFromLocationHeader(location);
        assertNotBlank(fapi1AppId);
    }

    /**
     * 3.2 - GET OIDC inbound config confirms fapiProfile is FAPI1_ADVANCED.
     */
    @Test(dependsOnMethods = "testCreateFapi1App")
    public void testGetFapi1AppProfile() {

        String path = APPLICATION_MANAGEMENT_API_BASE_PATH + "/" + fapi1AppId + INBOUND_PROTOCOLS_OIDC_CONTEXT_PATH;
        getResponseOfGet(path)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("isFAPIApplication", equalTo(true))
                .body("fapiProfile", equalTo(FAPI1_ADVANCED));
    }

    /**
     * 3.3 - Create a FAPI application with fapiProfile=FAPI2_SECURITY.
     */
    @Test(dependsOnMethods = "testGetFapi1AppProfile")
    public void testCreateFapi2App() throws IOException {

        String body = readResource("create-fapi2-app.json");
        Response response = getResponseOfPost(APPLICATION_MANAGEMENT_API_BASE_PATH, body);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .header(HttpHeaders.LOCATION, notNullValue());

        String location = response.getHeader(HttpHeaders.LOCATION);
        fapi2AppId = extractApplicationIdFromLocationHeader(location);
        assertNotBlank(fapi2AppId);
    }

    /**
     * 3.4 - GET OIDC inbound config confirms fapiProfile is FAPI2_SECURITY.
     */
    @Test(dependsOnMethods = "testCreateFapi2App")
    public void testGetFapi2AppProfile() {

        String path = APPLICATION_MANAGEMENT_API_BASE_PATH + "/" + fapi2AppId + INBOUND_PROTOCOLS_OIDC_CONTEXT_PATH;
        getResponseOfGet(path)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("isFAPIApplication", equalTo(true))
                .body("fapiProfile", equalTo(FAPI2_SECURITY));
    }

    /**
     * 3.5 - Create a legacy FAPI app (isFAPIApplication=true, no fapiProfile).
     * Server should default fapiProfile to FAPI1_ADVANCED.
     */
    @Test(dependsOnMethods = "testGetFapi2AppProfile")
    public void testCreateLegacyFapiApp() throws IOException {

        String body = readResource("create-legacy-fapi-app.json");
        Response response = getResponseOfPost(APPLICATION_MANAGEMENT_API_BASE_PATH, body);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .header(HttpHeaders.LOCATION, notNullValue());

        String location = response.getHeader(HttpHeaders.LOCATION);
        legacyFapiAppId = extractApplicationIdFromLocationHeader(location);
        assertNotBlank(legacyFapiAppId);
    }

    /**
     * 3.5 (verify) - Legacy FAPI app defaults to FAPI1_ADVANCED when no fapiProfile was specified.
     */
    @Test(dependsOnMethods = "testCreateLegacyFapiApp")
    public void testLegacyFapiAppDefaultsToFapi1() {

        String path = APPLICATION_MANAGEMENT_API_BASE_PATH + "/" + legacyFapiAppId +
                INBOUND_PROTOCOLS_OIDC_CONTEXT_PATH;
        getResponseOfGet(path)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("isFAPIApplication", equalTo(true))
                .body("fapiProfile", equalTo(FAPI1_ADVANCED));
    }

    /**
     * 3.6 - Migrate the FAPI1 app to FAPI2 by updating the OIDC inbound profile.
     */
    @Test(dependsOnMethods = "testLegacyFapiAppDefaultsToFapi1")
    public void testMigrateFapi1AppToFapi2() throws IOException {

        String body = readResource("update-oidc-fapi2-profile.json");
        String path = APPLICATION_MANAGEMENT_API_BASE_PATH + "/" + fapi1AppId + INBOUND_PROTOCOLS_OIDC_CONTEXT_PATH;
        getResponseOfPut(path, body)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);
    }

    /**
     * 3.7 - GET after migration confirms fapiProfile is now FAPI2_SECURITY.
     */
    @Test(dependsOnMethods = "testMigrateFapi1AppToFapi2")
    public void testGetMigratedAppHasFapi2Profile() {

        String path = APPLICATION_MANAGEMENT_API_BASE_PATH + "/" + fapi1AppId + INBOUND_PROTOCOLS_OIDC_CONTEXT_PATH;
        getResponseOfGet(path)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("fapiProfile", equalTo(FAPI2_SECURITY));
    }

    /**
     * 3.8 - Revert the migrated app back to FAPI1_ADVANCED.
     */
    @Test(dependsOnMethods = "testGetMigratedAppHasFapi2Profile")
    public void testRevertFapi2AppToFapi1() throws IOException {

        String body = readResource("update-oidc-fapi1-profile.json");
        String path = APPLICATION_MANAGEMENT_API_BASE_PATH + "/" + fapi1AppId + INBOUND_PROTOCOLS_OIDC_CONTEXT_PATH;
        getResponseOfPut(path, body)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);
    }

    /**
     * 3.8 (verify) - GET after revert confirms fapiProfile is FAPI1_ADVANCED again.
     */
    @Test(dependsOnMethods = "testRevertFapi2AppToFapi1")
    public void testGetRevertedAppHasFapi1Profile() {

        String path = APPLICATION_MANAGEMENT_API_BASE_PATH + "/" + fapi1AppId + INBOUND_PROTOCOLS_OIDC_CONTEXT_PATH;
        getResponseOfGet(path)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("fapiProfile", equalTo(FAPI1_ADVANCED));
    }

    // Group 4: App FAPI Profile — Error Paths

    /**
     * 4.1 - Attempt to set an invalid fapiProfile value returns 400 Bad Request.
     * Uses a raw RestAssured call (no OpenApiValidationFilter) so the request reaches the server;
     * the filter would otherwise reject "INVALID_PROFILE" client-side before the server sees it.
     */
    @Test(dependsOnMethods = "testGetRevertedAppHasFapi1Profile")
    public void testSetInvalidFapiProfileReturns400() throws IOException {

        String body = readResource("update-oidc-invalid-profile.json");
        String path = APPLICATION_MANAGEMENT_API_BASE_PATH + "/" + fapi1AppId + INBOUND_PROTOCOLS_OIDC_CONTEXT_PATH;
        Response response = given()
                .auth().preemptive().basic(authenticatingUserName, authenticatingCredential)
                .config(RestAssured.config().encoderConfig(
                        new EncoderConfig().appendDefaultContentCharsetToContentTypeIfUndefined(false)))
                .contentType(ContentType.JSON)
                .header(HttpHeaders.ACCEPT, ContentType.JSON)
                .body(body)
                .log().ifValidationFails()
                .when()
                .log().ifValidationFails()
                .put(path);
        validateHttpStatusCode(response, HttpStatus.SC_BAD_REQUEST);
    }

    private void deleteAppIfPresent(String appId) {

        if (appId == null) {
            return;
        }
        String path = APPLICATION_MANAGEMENT_API_BASE_PATH + "/" + appId;
        Response response = getResponseOfDelete(path);
        if (response.getStatusCode() == HttpStatus.SC_NO_CONTENT) {
            getResponseOfGet(path).then().assertThat().statusCode(HttpStatus.SC_NOT_FOUND);
        }
    }
}

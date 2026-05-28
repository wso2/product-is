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
import io.restassured.config.EncoderConfig;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;

import static io.restassured.RestAssured.given;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;

import java.io.IOException;

/**
 * Integration tests for error paths of the tenant FAPI configuration API (GET/PUT /configs/fapi).
 * Covers Group 2 (failure paths) from the test plan.
 */
public class FapiConfigFailureTest extends ConfigTestBase {

    @Factory(dataProvider = "restAPIUserConfigProvider")
    public FapiConfigFailureTest(TestUserMode userMode) throws Exception {

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

    // Group 2: Tenant FAPI Config — Error Paths

    /**
     * 2.1 - GET /configs/fapi without authentication returns 401.
     */
    @Test
    public void testGetFapiConfigWithoutAuth() {

        Response response = getResponseOfGetWithoutAuthentication(FAPI_CONFIGS_API_BASE_PATH, "application/json");
        validateHttpStatusCode(response, HttpStatus.SC_UNAUTHORIZED);
    }

    /**
     * 2.3 - PUT /configs/fapi with an unrecognised profile string returns 400.
     * Uses a raw RestAssured call (no OpenApiValidationFilter) so the request reaches the server;
     * the filter would otherwise reject "INVALID_PROFILE" client-side before the server sees it.
     */
    @Test(dependsOnMethods = "testGetFapiConfigWithoutAuth")
    public void testPatchFapiConfigWithInvalidProfile() throws IOException {

        String body = readResource("fapi-config-invalid-profile.json");
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
                .put(FAPI_CONFIGS_API_BASE_PATH);
        validateHttpStatusCode(response, HttpStatus.SC_BAD_REQUEST);
    }

    /**
     * 2.4 - PUT /configs/fapi with malformed JSON returns 400.
     * Uses a raw RestAssured call (no OpenApiValidationFilter) so the request reaches the server;
     * the filter may also reject unparseable JSON body client-side.
     */
    @Test(dependsOnMethods = "testPatchFapiConfigWithInvalidProfile")
    public void testPatchFapiConfigWithMalformedJson() {

        String malformedBody = "{ enabled: true, supportedProfiles: [FAPI1_ADVANCED }";
        Response response = given()
                .auth().preemptive().basic(authenticatingUserName, authenticatingCredential)
                .config(RestAssured.config().encoderConfig(
                        new EncoderConfig().appendDefaultContentCharsetToContentTypeIfUndefined(false)))
                .contentType(ContentType.JSON)
                .header(HttpHeaders.ACCEPT, ContentType.JSON)
                .body(malformedBody)
                .log().ifValidationFails()
                .when()
                .log().ifValidationFails()
                .put(FAPI_CONFIGS_API_BASE_PATH);
        validateHttpStatusCode(response, HttpStatus.SC_BAD_REQUEST);
    }

    /**
     * 2.5 - PUT /configs/fapi with enabled=true and an empty supportedProfiles list returns 400.
     * The service layer rejects this because FAPI enforcement cannot be active without at least one profile.
     */
    @Test(dependsOnMethods = "testPatchFapiConfigWithMalformedJson")
    public void testPatchFapiConfigEnabledWithEmptyProfiles() {

        String body = "{\"enabled\":true,\"supportedProfiles\":[]}";
        Response response = getResponseOfPut(FAPI_CONFIGS_API_BASE_PATH, body);
        validateHttpStatusCode(response, HttpStatus.SC_BAD_REQUEST);
    }
}

/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.identity.integration.test.rest.api.server.configs.v1;

import io.restassured.RestAssured;
import io.restassured.config.EncoderConfig;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.apache.commons.lang3.StringUtils;
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
 * Test class for OpenID4VP Configuration REST APIs failure paths.
 */
public class OpenID4VPConfigFailureTest extends OpenID4VPConfigTestBase {

    @Factory(dataProvider = "restAPIUserConfigProvider")
    public OpenID4VPConfigFailureTest(TestUserMode userMode) throws Exception {

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

    @Test
    public void testUpdateWithInvalidClientIdScheme() throws IOException {

        // Uses getResponseOfPutWithNoFilter so the request reaches the server;
        // the OpenAPI filter would otherwise reject "unknown_scheme" client-side.
        String body = "{\"clientIdScheme\":\"unknown_scheme\",\"responseMode\":\"direct_post\"}";
        Response response = getResponseOfPutWithNoFilter(OPENID4VP_CONFIG_API_PATH, body);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    public void testUpdateWithInvalidResponseMode() throws IOException {

        // Uses getResponseOfPutWithNoFilter so the request reaches the server;
        // the OpenAPI filter would otherwise reject "unknown_mode" client-side.
        String body = "{\"clientIdScheme\":\"x509_san_dns\",\"responseMode\":\"unknown_mode\"}";
        Response response = getResponseOfPutWithNoFilter(OPENID4VP_CONFIG_API_PATH, body);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    public void testUpdateWithMissingBody() {

        // Uses a raw RestAssured call with malformed JSON (no OpenApiValidationFilter) so the
        // request reaches the server and it returns 400 for unparseable content.
        String malformedBody = "{ clientIdScheme: missing_quotes }";
        given()
                .auth().preemptive().basic(authenticatingUserName, authenticatingCredential)
                .config(RestAssured.config().encoderConfig(
                        new EncoderConfig().appendDefaultContentCharsetToContentTypeIfUndefined(false)))
                .contentType(ContentType.JSON)
                .header(HttpHeaders.ACCEPT, ContentType.JSON.toString())
                .body(malformedBody)
                .log().ifValidationFails()
                .when()
                .log().ifValidationFails()
                .put(OPENID4VP_CONFIG_API_PATH)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    public void testUnauthorizedAccess() {

        // Use getResponseOfGetWithoutAuthentication to verify that unauthenticated requests are rejected.
        Response response = getResponseOfGetWithoutAuthentication(OPENID4VP_CONFIG_API_PATH, "application/json");
        validateHttpStatusCode(response, HttpStatus.SC_UNAUTHORIZED);
    }
}

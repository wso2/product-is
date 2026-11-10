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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.json.JSONObject;
import org.testng.Assert;

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

    private static final Log log = LogFactory.getLog(OpenID4VPConfigFailureTest.class);

    private static final String NON_ADMIN_BASE_NAME = "vpconfig_nonadmin";
    private static final String NON_ADMIN_PASSWORD  = "Admin@123!VP";

    private String nonAdminUserId;
    private String nonAdminAuthUser;

    @Factory(dataProvider = "restAPIUserConfigProvider")
    public OpenID4VPConfigFailureTest(TestUserMode userMode) throws Exception {

        super.init(userMode);
        this.context = isServer;
        this.authenticatingUserName = context.getContextTenant().getTenantAdmin().getUserName();
        this.authenticatingCredential = context.getContextTenant().getTenantAdmin().getPassword();
        this.tenant = context.getContextTenant().getDomain();
    }

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {

        super.testInit(API_VERSION, swaggerDefinition, tenant);

        boolean isSuperTenant = "carbon.super".equals(tenant);
        nonAdminAuthUser = isSuperTenant ? NON_ADMIN_BASE_NAME : NON_ADMIN_BASE_NAME + "@" + tenant;
        String tenantPath = isSuperTenant ? "" : "/t/" + tenant;
        String scimUrl = RestAssured.baseURI + tenantPath + "/scim2/Users";

        Response listResp = given()
                .auth().preemptive().basic(authenticatingUserName, authenticatingCredential)
                .queryParam("filter", "userName eq \"" + NON_ADMIN_BASE_NAME + "\"")
                .get(scimUrl);
        if (listResp.getStatusCode() == HttpStatus.SC_OK) {
            String staleId = listResp.jsonPath().getString("Resources[0].id");
            if (staleId != null) {
                given()
                        .auth().preemptive().basic(authenticatingUserName, authenticatingCredential)
                        .delete(scimUrl + "/" + staleId);
            }
        }

        JSONObject userJson = new JSONObject();
        userJson.put("schemas", new org.json.JSONArray().put("urn:ietf:params:scim:schemas:core:2.0:User"));
        userJson.put("userName", NON_ADMIN_BASE_NAME);
        userJson.put("password", NON_ADMIN_PASSWORD);
        userJson.put("emails", new org.json.JSONArray().put(
                new JSONObject().put("primary", true).put("value", NON_ADMIN_BASE_NAME + "@wso2test.com")));

        Response createResp = given()
                .auth().preemptive().basic(authenticatingUserName, authenticatingCredential)
                .contentType(ContentType.JSON)
                .body(userJson.toString())
                .post(scimUrl);
        Assert.assertEquals(createResp.getStatusCode(), HttpStatus.SC_CREATED,
                "Failed to create non-admin test user '" + nonAdminAuthUser + "' (status "
                        + createResp.getStatusCode() + "): " + createResp.asString());
        nonAdminUserId = createResp.jsonPath().getString("id");
    }

    @AfterClass(alwaysRun = true)
    public void testConclude() {

        if (nonAdminUserId != null) {
            boolean isSuperTenant = "carbon.super".equals(tenant);
            String tenantPath = isSuperTenant ? "" : "/t/" + tenant;
            String deleteUrl = RestAssured.baseURI + tenantPath + "/scim2/Users/" + nonAdminUserId;
            try {
                given()
                    .auth().preemptive().basic(authenticatingUserName, authenticatingCredential)
                    .delete(deleteUrl);
            } catch (Exception e) {
                log.warn("Failed to delete non-admin test user '" + nonAdminAuthUser + "': " + e.getMessage());
            }
        }
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

        String body = "{\"clientIdScheme\":\"unknown_scheme\",\"responseMode\":\"direct_post\"}";
        Response response = getResponseOfPutWithNoFilter(OPENID4VP_CONFIG_API_PATH, body);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    public void testUpdateWithInvalidResponseMode() throws IOException {

        String body = "{\"clientIdScheme\":\"x509_san_dns\",\"responseMode\":\"unknown_mode\"}";
        Response response = getResponseOfPutWithNoFilter(OPENID4VP_CONFIG_API_PATH, body);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    public void testUpdateWithMalformedBody() {

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

        Response response = getResponseOfGetWithoutAuthentication(OPENID4VP_CONFIG_API_PATH, "application/json");
        validateHttpStatusCode(response, HttpStatus.SC_UNAUTHORIZED);
    }

    @Test
    public void testNonAdminUserGetForbidden() {

        given()
                .auth().preemptive().basic(nonAdminAuthUser, NON_ADMIN_PASSWORD)
                .header(HttpHeaders.ACCEPT, ContentType.JSON.toString())
                .log().ifValidationFails()
                .when()
                .get(OPENID4VP_CONFIG_API_PATH)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_FORBIDDEN);
    }

    @Test
    public void testNonAdminUserPutForbidden() {

        String body = "{\"clientIdScheme\":\"x509_san_dns\",\"responseMode\":\"direct_post\"}";
        given()
                .auth().preemptive().basic(nonAdminAuthUser, NON_ADMIN_PASSWORD)
                .contentType(ContentType.JSON)
                .header(HttpHeaders.ACCEPT, ContentType.JSON.toString())
                .body(body)
                .log().ifValidationFails()
                .when()
                .put(OPENID4VP_CONFIG_API_PATH)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_FORBIDDEN);
    }
}

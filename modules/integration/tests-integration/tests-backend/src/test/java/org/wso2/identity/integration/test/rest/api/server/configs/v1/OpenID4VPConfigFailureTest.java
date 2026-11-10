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
import org.json.JSONArray;
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
import org.wso2.identity.integration.test.rest.api.user.common.model.Email;
import org.wso2.identity.integration.test.rest.api.user.common.model.UserObject;
import org.wso2.identity.integration.test.restclients.SCIM2RestClient;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Test class for OpenID4VP Configuration REST APIs failure paths.
 */
public class OpenID4VPConfigFailureTest extends OpenID4VPConfigTestBase {

    private static final Log log = LogFactory.getLog(OpenID4VPConfigFailureTest.class);

    private static final String NON_ADMIN_BASE_NAME = "vpconfig_nonadmin";
    private static final String NON_ADMIN_PASSWORD  = "Admin@123!VP";

    private SCIM2RestClient scim2Client;
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

        scim2Client = new SCIM2RestClient(serverURL, isServer.getContextTenant());

        String filter = URLEncoder.encode("userName eq \"" + NON_ADMIN_BASE_NAME + "\"", StandardCharsets.UTF_8);
        JSONObject existing = new JSONObject(scim2Client.filterUsers(filter).toJSONString());
        if (existing.optLong("totalResults", 0) > 0) {
            JSONArray resources = existing.optJSONArray("Resources");
            if (resources != null) {
                for (int i = 0; i < resources.length(); i++) {
                    scim2Client.deleteUser(resources.getJSONObject(i).getString("id"));
                }
            }
        }

        UserObject user = new UserObject()
                .userName(NON_ADMIN_BASE_NAME)
                .password(NON_ADMIN_PASSWORD)
                .addEmail(new Email().value(NON_ADMIN_BASE_NAME + "@wso2test.com").primary(true));
        nonAdminUserId = scim2Client.createUser(user);
        Assert.assertNotNull(nonAdminUserId,
                "Failed to create non-admin test user '" + nonAdminAuthUser + "'");
    }

    @AfterClass(alwaysRun = true)
    public void testConclude() {

        if (nonAdminUserId != null) {
            try {
                scim2Client.deleteUser(nonAdminUserId);
            } catch (Exception e) {
                log.warn("Failed to delete non-admin test user '" + nonAdminAuthUser + "': " + e.getMessage());
            }
        }
        if (scim2Client != null) {
            try {
                scim2Client.closeHttpClient();
            } catch (IOException e) {
                log.warn("Failed to close SCIM2 client: " + e.getMessage());
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

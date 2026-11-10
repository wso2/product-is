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
import io.restassured.response.Response;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;

/**
 * Test class for OpenID4VP Configuration REST APIs success paths.
 */
public class OpenID4VPConfigSuccessTest extends OpenID4VPConfigTestBase {

    private static final Log log = LogFactory.getLog(OpenID4VPConfigSuccessTest.class);

    private String originalClientIdScheme;
    private String originalResponseMode;

    @Factory(dataProvider = "restAPIUserConfigProvider")
    public OpenID4VPConfigSuccessTest(TestUserMode userMode) throws Exception {

        super.init(userMode);
        this.context = isServer;
        this.authenticatingUserName = context.getContextTenant().getTenantAdmin().getUserName();
        this.authenticatingCredential = context.getContextTenant().getTenantAdmin().getPassword();
        this.tenant = context.getContextTenant().getDomain();
    }

    @BeforeClass(alwaysRun = true)
    public void init() throws IOException {

        super.testInit(API_VERSION, swaggerDefinition, tenant);
        RestAssured.basePath = basePath;
        Response existing = getResponseOfGet(OPENID4VP_CONFIG_API_PATH);
        if (existing.getStatusCode() != HttpStatus.SC_OK) {
            throw new AssertionError("Failed to retrieve current OpenID4VP config (status "
                    + existing.getStatusCode() + "); cannot proceed without snapshot.");
        }
        originalClientIdScheme = existing.jsonPath().getString("clientIdScheme");
        originalResponseMode   = existing.jsonPath().getString("responseMode");
    }

    @AfterClass(alwaysRun = true)
    public void testConclude() {

        if (originalClientIdScheme != null && originalResponseMode != null) {
            String restoreBody = "{\"clientIdScheme\":\"" + originalClientIdScheme
                    + "\",\"responseMode\":\"" + originalResponseMode + "\"}";
            Response restoreResp = getResponseOfPut(OPENID4VP_CONFIG_API_PATH, restoreBody);
            if (restoreResp.getStatusCode() != HttpStatus.SC_OK) {
                log.warn("Failed to restore OpenID4VP config in teardown (status "
                        + restoreResp.getStatusCode() + ").");
            }
        }
        super.conclude();
    }

    @DataProvider(name = "restAPIUserConfigProvider")
    public static Object[][] restAPIUserConfigProvider() {

        return new Object[][]{
                {TestUserMode.SUPER_TENANT_ADMIN},
                {TestUserMode.TENANT_ADMIN}
        };
    }

    @Test
    public void testGetOpenID4VPConfig() throws Exception {

        Response response = getResponseOfGet(OPENID4VP_CONFIG_API_PATH);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("clientIdScheme", notNullValue())
                .body("responseMode", notNullValue());
    }

    @Test(dependsOnMethods = {"testGetOpenID4VPConfig"})
    public void testUpdateConfigDirectPost() throws Exception {

        String body = "{\"clientIdScheme\":\"x509_san_dns\",\"responseMode\":\"direct_post\"}";
        Response response = getResponseOfPut(OPENID4VP_CONFIG_API_PATH, body);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);

        getResponseOfGet(OPENID4VP_CONFIG_API_PATH).then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("clientIdScheme", equalTo("x509_san_dns"))
                .body("responseMode", equalTo("direct_post"));
    }

    @Test(dependsOnMethods = {"testUpdateConfigDirectPost"})
    public void testUpdateConfigDirectPostJwt() throws Exception {

        String body = "{\"clientIdScheme\":\"x509_hash\",\"responseMode\":\"direct_post.jwt\"}";
        Response response = getResponseOfPut(OPENID4VP_CONFIG_API_PATH, body);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);

        getResponseOfGet(OPENID4VP_CONFIG_API_PATH).then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("clientIdScheme", equalTo("x509_hash"))
                .body("responseMode", equalTo("direct_post.jwt"));
    }

    @Test(dependsOnMethods = {"testUpdateConfigDirectPostJwt"})
    public void testUpdateConfigRoundTrip() throws Exception {

        String body = "{\"clientIdScheme\":\"x509_san_dns\",\"responseMode\":\"direct_post\"}";
        getResponseOfPut(OPENID4VP_CONFIG_API_PATH, body).then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);

        getResponseOfPut(OPENID4VP_CONFIG_API_PATH, body).then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);
    }

}

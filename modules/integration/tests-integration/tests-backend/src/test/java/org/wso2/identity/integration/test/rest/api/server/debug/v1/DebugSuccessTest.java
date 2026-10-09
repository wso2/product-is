/*
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.identity.integration.test.rest.api.server.debug.v1;

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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.startsWith;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * Integration tests for the success paths of the Debug Framework REST API.
 *
 * Covers {@code POST /debug/idp} for OIDC, Google, and GitHub identity providers and
 * verifies the returned authorization URL carries PKCE, state, and nonce parameters.
 */
public class DebugSuccessTest extends DebugTestBase {

    private static final String IDP_NAME_PLACEHOLDER = "<IDP_NAME>";

    private static final String OIDC_IDP_NAME = "Debug Test OIDC IDP";
    private static final String GOOGLE_IDP_NAME = "Debug Test Google IDP";
    private static final String GITHUB_IDP_NAME = "Debug Test GitHub IDP";

    private static final String STATUS_SUCCESS_INCOMPLETE = "SUCCESS_INCOMPLETE";

    private String oidcIdpId;
    private String googleIdpId;
    private String githubIdpId;

    @Factory(dataProvider = "restAPIUserConfigProvider")
    public DebugSuccessTest(TestUserMode userMode) throws Exception {

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
        oidcIdpId = createIdp("add-oidc-idp.json", OIDC_IDP_NAME);
        googleIdpId = createIdp("add-google-idp.json", GOOGLE_IDP_NAME);
        githubIdpId = createIdp("add-github-idp.json", GITHUB_IDP_NAME);
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() {

        deleteIdp(oidcIdpId);
        deleteIdp(googleIdpId);
        deleteIdp(githubIdpId);
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
    public void testStartDebugSessionForOidcIdp() {

        Response response = startDebugSession(oidcIdpId);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("debugId", notNullValue())
                .body("debugId", startsWith(DEBUG_ID_PREFIX))
                .body("status", equalTo(STATUS_SUCCESS_INCOMPLETE))
                .body("metadata.authorizationUrl", notNullValue())
                .body("metadata.authorizationUrl", containsString("https://oidc.example.com/authz"))
                .body("metadata.authorizationUrl", containsString("client_id=debug-test-oidc-client-id"));
    }

    @Test
    public void testStartDebugSessionForGoogleIdp() {

        Response response = startDebugSession(googleIdpId);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("debugId", notNullValue())
                .body("debugId", startsWith(DEBUG_ID_PREFIX))
                .body("status", equalTo(STATUS_SUCCESS_INCOMPLETE))
                .body("metadata.authorizationUrl", notNullValue())
                .body("metadata.authorizationUrl", containsString("accounts.google.com"));
    }

    @Test
    public void testStartDebugSessionForGithubIdp() {

        Response response = startDebugSession(githubIdpId);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("debugId", notNullValue())
                .body("debugId", startsWith(DEBUG_ID_PREFIX))
                .body("status", equalTo(STATUS_SUCCESS_INCOMPLETE))
                .body("metadata.authorizationUrl", notNullValue())
                .body("metadata.authorizationUrl", containsString("github.com"));
    }

    @Test
    public void testAuthorizationUrlContainsPKCE() {

        Response response = startDebugSession(oidcIdpId);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("metadata.authorizationUrl", containsString("code_challenge="))
                .body("metadata.authorizationUrl", containsString("code_challenge_method=S256"));
    }

    @Test
    public void testAuthorizationUrlContainsStateMatchingDebugId() {

        Response response = startDebugSession(oidcIdpId);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);

        String debugId = response.jsonPath().getString("debugId");
        String authorizationUrl = response.jsonPath().getString("metadata.authorizationUrl");
        assertNotNull(debugId, "debugId must not be null in start-session response.");
        assertNotNull(authorizationUrl, "authorizationUrl must not be null in start-session response.");
        assertTrue(authorizationUrl.contains("state=" + debugId),
                "authorizationUrl must carry state equal to the returned debugId. URL: " + authorizationUrl);
    }

    @Test
    public void testAuthorizationUrlContainsNonce() {

        Response response = startDebugSession(oidcIdpId);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("metadata.authorizationUrl", containsString("nonce="));
    }

    @Test
    public void testDebugIdHasExpectedPrefix() {

        Response response = startDebugSession(oidcIdpId);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("debugId", startsWith(DEBUG_ID_PREFIX));
    }

    /**
     * Result retrieval requires a COMPLETED session, which can only be produced by the
     * OIDC callback (a real browser login). Two consecutive lookups against an unknown
     * debug ID must consistently return 404; this asserts the no-session path of
     * {@code GET /debug/{debugId}/result}.
     */
    @Test
    public void testGetResultReturnsNotFoundForUnknownDebugId() {

        String unknownDebugId = DEBUG_ID_PREFIX + "nonexistent-success-test";

        getResponseOfGet(DEBUG_API_BASE_PATH + PATH_SEPARATOR + unknownDebugId + PATH_SEPARATOR + RESULT_PATH)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NOT_FOUND)
                .body("code", equalTo("DSM-60002"));

        getResponseOfGet(DEBUG_API_BASE_PATH + PATH_SEPARATOR + unknownDebugId + PATH_SEPARATOR + RESULT_PATH)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NOT_FOUND)
                .body("code", equalTo("DSM-60002"));
    }

    private Response startDebugSession(String connectionId) {

        String payload = "{\"connectionId\": \"" + connectionId + "\"}";
        return getResponseOfPost(DEBUG_API_BASE_PATH + PATH_SEPARATOR + RESOURCE_TYPE_IDP, payload);
    }

    private String createIdp(String resourceFile, String idpName) throws IOException {

        String payload = readResource(resourceFile).replace(IDP_NAME_PLACEHOLDER, idpName);
        Response response = getResponseOfPost(IDP_API_BASE_PATH, payload);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .header(HttpHeaders.LOCATION, notNullValue());
        String location = response.getHeader(HttpHeaders.LOCATION);
        assertNotNull(location, "Location header must be present after IDP creation for " + idpName);
        return location.substring(location.lastIndexOf("/") + 1);
    }

    private void deleteIdp(String idpId) {

        if (idpId == null) {
            return;
        }
        getResponseOfDelete(IDP_API_BASE_PATH + PATH_SEPARATOR + idpId)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NO_CONTENT);
    }
}

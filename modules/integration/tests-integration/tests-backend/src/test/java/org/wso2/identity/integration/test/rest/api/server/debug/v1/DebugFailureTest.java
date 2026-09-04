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
import static org.hamcrest.core.IsNull.notNullValue;
import static org.testng.Assert.assertNotNull;

/**
 * Integration tests for the failure paths of the Debug Framework REST API.
 *
 * Covers validation, routing, and protocol-resolution errors for both
 * {@code POST /debug/{resourceType}} and {@code GET /debug/{debugId}/result}.
 */
public class DebugFailureTest extends DebugTestBase {

    private static final String IDP_NAME_PLACEHOLDER = "<IDP_NAME>";

    private static final String SAML_IDP_NAME = "Debug Test SAML IDP";
    private static final String NO_AUTH_IDP_NAME = "Debug Test No-Auth IDP";

    private static final String ERROR_CODE_INVALID_REQUEST = "DSM-60001";
    private static final String ERROR_CODE_EXECUTOR_NOT_FOUND = "DSM-60005";
    private static final String ERROR_CODE_RESULT_NOT_FOUND_API = "DSM-60002";

    private String samlIdpId;
    private String noAuthIdpId;

    @Factory(dataProvider = "restAPIUserConfigProvider")
    public DebugFailureTest(TestUserMode userMode) throws Exception {

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
        samlIdpId = createIdp("add-saml-idp.json", SAML_IDP_NAME);
        noAuthIdpId = createIdp("add-idp-without-authenticator.json", NO_AUTH_IDP_NAME);
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() {

        deleteIdp(samlIdpId);
        deleteIdp(noAuthIdpId);
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
    public void testStartSessionMissingConnectionId() {

        Response response = getResponseOfPost(
                DEBUG_API_BASE_PATH + PATH_SEPARATOR + RESOURCE_TYPE_IDP, "{}");
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body("code", equalTo(ERROR_CODE_INVALID_REQUEST));
    }

    @Test
    public void testStartSessionEmptyConnectionId() {

        Response response = getResponseOfPost(
                DEBUG_API_BASE_PATH + PATH_SEPARATOR + RESOURCE_TYPE_IDP,
                "{\"connectionId\": \"\"}");
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body("code", equalTo(ERROR_CODE_INVALID_REQUEST));
    }

    @Test
    public void testStartSessionUnknownConnectionId() {

        Response response = getResponseOfPost(
                DEBUG_API_BASE_PATH + PATH_SEPARATOR + RESOURCE_TYPE_IDP,
                "{\"connectionId\": \"nonexistent-idp-uuid\"}");
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body("code", equalTo(ERROR_CODE_EXECUTOR_NOT_FOUND));
    }

    @Test
    public void testStartSessionUnknownResourceType() {

        Response response = getResponseOfPost(
                DEBUG_API_BASE_PATH + PATH_SEPARATOR + "unknown-resource-type",
                "{\"connectionId\": \"any\"}");
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body("code", equalTo(ERROR_CODE_INVALID_REQUEST));
    }

    @Test
    public void testStartSessionForSamlIdpHasNoRegisteredProvider() {

        Response response = getResponseOfPost(
                DEBUG_API_BASE_PATH + PATH_SEPARATOR + RESOURCE_TYPE_IDP,
                "{\"connectionId\": \"" + samlIdpId + "\"}");
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body("code", equalTo(ERROR_CODE_EXECUTOR_NOT_FOUND));
    }

    @Test
    public void testStartSessionForIdpWithoutAuthenticator() {

        Response response = getResponseOfPost(
                DEBUG_API_BASE_PATH + PATH_SEPARATOR + RESOURCE_TYPE_IDP,
                "{\"connectionId\": \"" + noAuthIdpId + "\"}");
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body("code", equalTo(ERROR_CODE_EXECUTOR_NOT_FOUND));
    }

    @Test
    public void testGetResultUnknownDebugId() {

        getResponseOfGet(DEBUG_API_BASE_PATH + PATH_SEPARATOR + DEBUG_ID_PREFIX + "doesnotexist"
                + PATH_SEPARATOR + RESULT_PATH)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NOT_FOUND)
                .body("code", equalTo(ERROR_CODE_RESULT_NOT_FOUND_API));
    }

    @Test
    public void testGetResultMalformedDebugId() {

        getResponseOfGet(DEBUG_API_BASE_PATH + PATH_SEPARATOR + "not-a-debug-id"
                + PATH_SEPARATOR + RESULT_PATH)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NOT_FOUND)
                .body("code", equalTo(ERROR_CODE_RESULT_NOT_FOUND_API));
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

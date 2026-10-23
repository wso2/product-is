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

package org.wso2.identity.integration.test.rest.api.server.vp.template.management.v1;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.apache.commons.lang3.StringUtils;
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
import static org.hamcrest.Matchers.notNullValue;

/**
 * Test class for Presentation Definition REST API failure paths.
 */
public class PresentationDefinitionsApiFailureTest extends PresentationDefinitionsApiTestBase {

    private static final String NON_EXISTENT_ID = "00000000-0000-0000-0000-000000000000";
    private static final String EXISTING_IDENTIFIER = "test-conflict-definition";
    private String existingDefinitionId;

    @Factory(dataProvider = "restAPIUserConfigProvider")
    public PresentationDefinitionsApiFailureTest(TestUserMode userMode) throws Exception {

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

        // Create one valid definition to use for duplicate/conflict tests.
        String body = readResource("create-presentation-definition-conflict.json");
        Response response = getResponseOfPost(PRESENTATION_DEFINITIONS_API_BASE_PATH, body);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .body("id", notNullValue());
        existingDefinitionId = response.jsonPath().getString("id");
    }

    @AfterClass(alwaysRun = true)
    public void testConclude() {

        RestAssured.basePath = basePath;
        if (existingDefinitionId != null) {
            getResponseOfDelete(PRESENTATION_DEFINITIONS_API_BASE_PATH + PATH_SEPARATOR + existingDefinitionId);
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
    public void testCreateWithMissingIdentifier() throws IOException {

        String body = readResource("create-presentation-definition-no-identifier.json");
        Response response = getResponseOfPostNoFilter(PRESENTATION_DEFINITIONS_API_BASE_PATH, body);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    public void testCreateWithMissingDisplayName() throws IOException {

        String body = readResource("create-presentation-definition-no-display-name.json");
        Response response = getResponseOfPostNoFilter(PRESENTATION_DEFINITIONS_API_BASE_PATH, body);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    public void testCreateWithMissingCredentials() {

        String body = "{\"identifier\":\"test-missing-creds\",\"displayName\":\"Test\"}";
        Response response = getResponseOfPostNoFilter(PRESENTATION_DEFINITIONS_API_BASE_PATH, body);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    public void testCreateWithEmptyCredentialsList() throws IOException {

        String body = readResource("create-presentation-definition-empty-credentials.json");
        Response response = getResponseOfPostNoFilter(PRESENTATION_DEFINITIONS_API_BASE_PATH, body);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    public void testCreateWithInvalidIdentifierChars() {

        String body = "{\"identifier\":\"test invalid\",\"displayName\":\"Test\","
                + "\"credentials\":[{\"id\":\"CredA\",\"type\":\"CredA\"}]}";
        Response response = getResponseOfPostNoFilter(PRESENTATION_DEFINITIONS_API_BASE_PATH, body);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    public void testCreateWithInvalidIdentifierSpecialChars() {

        String body = "{\"identifier\":\"test@invalid\",\"displayName\":\"Test\","
                + "\"credentials\":[{\"id\":\"CredA\",\"type\":\"CredA\"}]}";
        Response response = getResponseOfPostNoFilter(PRESENTATION_DEFINITIONS_API_BASE_PATH, body);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    public void testCreateDuplicateIdentifier() throws IOException {

        // Attempt to create another definition with the same identifier as existingDefinition.
        String body = readResource("create-presentation-definition-conflict.json");
        Response response = getResponseOfPost(PRESENTATION_DEFINITIONS_API_BASE_PATH, body);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CONFLICT);
    }

    @Test
    public void testGetNonExistentDefinition() {

        Response response = getResponseOfGet(PRESENTATION_DEFINITIONS_API_BASE_PATH + PATH_SEPARATOR
                + NON_EXISTENT_ID);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    public void testPatchNonExistentDefinition() {

        String body = "{\"displayName\":\"Should Not Exist\"}";
        Response response = getResponseOfPatch(PRESENTATION_DEFINITIONS_API_BASE_PATH + PATH_SEPARATOR
                + NON_EXISTENT_ID, body);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    public void testDeleteNonExistentDefinition() {

        Response response = getResponseOfDelete(PRESENTATION_DEFINITIONS_API_BASE_PATH + PATH_SEPARATOR
                + NON_EXISTENT_ID);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    public void testGetIssuerConfigsNonExistentCredential() {

        Response response = getResponseOfGet(PRESENTATION_DEFINITIONS_API_BASE_PATH + PATH_SEPARATOR
                + existingDefinitionId + PATH_SEPARATOR + CREDENTIALS_PATH + PATH_SEPARATOR
                + "nonexistent-credential" + PATH_SEPARATOR + ISSUER_CONFIGS_PATH);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    public void testPutIssuerConfigsInvalidKeySourceType() {

        String body = "{\"issuerConfigs\":[{\"issuerUrl\":\"https://issuer.example.com\","
                + "\"keySourceType\":\"invalid_type\","
                + "\"keySource\":\"https://issuer.example.com/.well-known/jwks.json\"}]}";
        Response response = getResponseOfPutWithNoFilter(PRESENTATION_DEFINITIONS_API_BASE_PATH + PATH_SEPARATOR
                + existingDefinitionId + PATH_SEPARATOR + CREDENTIALS_PATH + PATH_SEPARATOR
                + "ConflictCredential" + PATH_SEPARATOR + ISSUER_CONFIGS_PATH, body);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    public void testPutIssuerConfigsMissingIssuerUrl() {

        // issuerUrl is optional in the schema; the server accepts the request and returns 200.
        String body = "{\"issuerConfigs\":[{"
                + "\"keySourceType\":\"jwks_uri\","
                + "\"keySource\":\"https://issuer.example.com/.well-known/jwks.json\"}]}";
        Response response = getResponseOfPut(PRESENTATION_DEFINITIONS_API_BASE_PATH + PATH_SEPARATOR
                + existingDefinitionId + PATH_SEPARATOR + CREDENTIALS_PATH + PATH_SEPARATOR
                + "ConflictCredential" + PATH_SEPARATOR + ISSUER_CONFIGS_PATH, body);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);
    }

    @Test
    public void testPutIssuerConfigsMalformedBase64() {

        String body = "{\"issuerConfigs\":[{\"issuerUrl\":\"https://issuer.example.com\","
                + "\"keySourceType\":\"pem\","
                + "\"keySource\":\"not-base64!!!\"}]}";
        Response response = getResponseOfPut(PRESENTATION_DEFINITIONS_API_BASE_PATH + PATH_SEPARATOR
                + existingDefinitionId + PATH_SEPARATOR + CREDENTIALS_PATH + PATH_SEPARATOR
                + "ConflictCredential" + PATH_SEPARATOR + ISSUER_CONFIGS_PATH, body);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    public void testUnauthorizedAccess() {

        // 401 tested by invoking without valid credentials.
        getResponseOfGetWithoutAuthentication(PRESENTATION_DEFINITIONS_API_BASE_PATH,
                "application/json")
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_UNAUTHORIZED);
    }
}

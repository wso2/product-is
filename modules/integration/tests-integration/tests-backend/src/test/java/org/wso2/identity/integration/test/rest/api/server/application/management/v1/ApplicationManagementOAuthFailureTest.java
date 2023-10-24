/*
 * Copyright (c) 2021, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.identity.integration.test.rest.api.server.application.management.v1;

import io.restassured.response.Response;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;

import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.Matchers.notNullValue;
import static org.wso2.identity.integration.test.rest.api.server.application.management.v1.Utils.assertNotBlank;
import static org.wso2.identity.integration.test.rest.api.server.application.management.v1.Utils.extractApplicationIdFromLocationHeader;

/**
 * Tests for error scenarios when the managing OAuth2/OIDC applications using Application Management REST API.
 */
public class ApplicationManagementOAuthFailureTest extends ApplicationManagementBaseTest {

    private final Set<String> createdApps = new HashSet<>();

    @Factory(dataProvider = "restAPIUserConfigProvider")
    public ApplicationManagementOAuthFailureTest(TestUserMode userMode) throws Exception {

        super(userMode);
    }

    @AfterMethod(alwaysRun = true)
    @Override
    public void testFinish() {

        cleanUpApplications(createdApps);
        super.testFinish();
    }

    @Test (description = "Tests error scenario when two OAuth applications are created with the same name.")
    public void testCreateOAuthApplicationsWithSameName() throws Exception {

        String body = readResource("create-oauth-app.json");
        Response responseOfPost = getResponseOfPost(APPLICATION_MANAGEMENT_API_BASE_PATH, body);
        responseOfPost.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .header(HttpHeaders.LOCATION, notNullValue());

        String location = responseOfPost.getHeader(HttpHeaders.LOCATION);
        String createdAppId = extractApplicationIdFromLocationHeader(location);
        assertNotBlank(createdAppId);
        // Mark for cleanup.
        createdApps.add(createdAppId);

        // Try to create the application using the same name again.
        Response responseOfSecondCreateAttempt = getResponseOfPost(APPLICATION_MANAGEMENT_API_BASE_PATH, body);
        validateErrorResponse(responseOfSecondCreateAttempt, HttpStatus.SC_CONFLICT, "OAUTH-60008");
    }

    @Test (description = "Tests error scenario when two OAuth applications are created with the same clientId.")
    public void testCreateOAuthApplicationsWithClientId() throws Exception {

        String body = readResource("create-oauth-app-with-predefined-clientid.json");
        Response responseOfPost = getResponseOfPost(APPLICATION_MANAGEMENT_API_BASE_PATH, body);
        responseOfPost.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .header(HttpHeaders.LOCATION, notNullValue());

        String location = responseOfPost.getHeader(HttpHeaders.LOCATION);
        String createdAppId = extractApplicationIdFromLocationHeader(location);
        assertNotBlank(createdAppId);

        // Try to create the application using the same name again.
        String secondRequest = readResource("create-duplicate-oauth-app-with-predefined-clientid.json");
        Response responseOfSecondCreateAttempt = getResponseOfPost(APPLICATION_MANAGEMENT_API_BASE_PATH, secondRequest);

        validateErrorResponse(responseOfSecondCreateAttempt, HttpStatus.SC_CONFLICT, "OAUTH-60008");

        // Delete the OAuth2 application to release the clientId for other testcases.
        getResponseOfDelete(APPLICATION_MANAGEMENT_API_BASE_PATH + "/" + createdAppId)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NO_CONTENT);
    }

    @Test (description = "Tests error scenario when an OIDC application is created with invalid audience values.")
    public void testCreateOAuthApplicationsWithInvalidAudience() throws Exception {

        String body = readResource("create-oauth-app-with-invalid-audience-values.json");
        Response responseOfPost = getResponseOfPost(APPLICATION_MANAGEMENT_API_BASE_PATH, body);
        validateErrorResponse(responseOfPost, HttpStatus.SC_BAD_REQUEST, "OAUTH-60001");
    }

    @Test (description = "Tests error scenario when an OIDC application is created with no/empty redirect URIs and " +
            "code grant.")
    public void testCreateOAuthApplicationsWithInvalidRedirectURIs() throws Exception {

        String body = readResource("create-oauth-app-with-no-redirect-uris.json");
        Response responseOfPost = getResponseOfPost(APPLICATION_MANAGEMENT_API_BASE_PATH, body);
        validateErrorResponse(responseOfPost, HttpStatus.SC_BAD_REQUEST, "OAUTH-60001");

        String SecondRequestBody = readResource("create-oauth-app-with-empty-redirect-uri.json");
        Response responseOfSecondPost = getResponseOfPost(APPLICATION_MANAGEMENT_API_BASE_PATH, SecondRequestBody);
        validateErrorResponse(responseOfSecondPost, HttpStatus.SC_BAD_REQUEST, "OAUTH-60001");
    }

    @Test (description = "Tests error scenario when an OIDC application is created with invalid grant type value.")
    public void testCreateOAuthApplicationsWithInvalidGrantType() throws Exception {

        String body = readResource("create-oauth-app-with-invalid-grant-type.json");
        Response responseOfPost = getResponseOfPost(APPLICATION_MANAGEMENT_API_BASE_PATH, body);
        validateErrorResponse(responseOfPost, HttpStatus.SC_BAD_REQUEST, "OAUTH-60001");
    }

    @Test (description = "Tests error scenario when an OIDC application is created with invalid signature algorithm.")
    public void testCreateOAuthApplicationsWithInvalidIdTokenSigningAlgorithm() throws Exception {

        String body = readResource("create-oauth-app-with-invalid-id-token-signing-algorithm.json");
        Response responseOfPost = getResponseOfPost(APPLICATION_MANAGEMENT_API_BASE_PATH, body);
        validateErrorResponse(responseOfPost, HttpStatus.SC_BAD_REQUEST, "OAUTH-60001");
    }

    @Test (description = "Tests error scenario when an OIDC application is created with invalid signature algorithm")
    public void testCreateOAuthApplicationsWithInvalidRequestObjectSigningAlgorithm() throws Exception {

        String body = readResource("create-oauth-app-with-invalid-request-object-signing-algorithm.json");
        Response responseOfPost = getResponseOfPost(APPLICATION_MANAGEMENT_API_BASE_PATH, body);
        validateErrorResponse(responseOfPost, HttpStatus.SC_BAD_REQUEST, "OAUTH-60001");
    }

    @Test (description = "Tests error scenario when an OIDC application is created with invalid client auth method")
    public void testCreateOAuthApplicationsWithInvalidClientAuthenticationMethod() throws Exception {

        String body = readResource("create-oauth-app-with-invalid-client-authentication-method.json");
        Response responseOfPost = getResponseOfPost(APPLICATION_MANAGEMENT_API_BASE_PATH, body);
        validateErrorResponse(responseOfPost, HttpStatus.SC_BAD_REQUEST, "OAUTH-60001");
    }

    @Test (description = "Tests error scenario when an OIDC application is created with invalid encryption algorithm.")
    public void testCreateOAuthApplicationsWithInvalidRequestObjectEncryptionAlgorithm() throws Exception {

        String body = readResource("create-oauth-app-with-invalid-request-object-encryption-algorithm.json");
        Response responseOfPost = getResponseOfPost(APPLICATION_MANAGEMENT_API_BASE_PATH, body);
        validateErrorResponse(responseOfPost, HttpStatus.SC_BAD_REQUEST, "OAUTH-60001");
    }

    @Test (description = "Tests error scenario when an OIDC application is created with invalid encryption method.")
    public void testCreateOAuthApplicationsWithInvalidRequestObjectEncryptionMethod() throws Exception {

        String body = readResource("create-oauth-app-with-invalid-request-object-encryption-method.json");
        Response responseOfPost = getResponseOfPost(APPLICATION_MANAGEMENT_API_BASE_PATH, body);
        validateErrorResponse(responseOfPost, HttpStatus.SC_BAD_REQUEST, "OAUTH-60001");
    }
}

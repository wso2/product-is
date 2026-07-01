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

package org.wso2.identity.integration.test.rest.api.server.consent.management.v2;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.identity.integration.test.rest.api.user.common.model.Email;
import org.wso2.identity.integration.test.rest.api.user.common.model.Name;
import org.wso2.identity.integration.test.rest.api.user.common.model.UserObject;
import org.wso2.identity.integration.test.restclients.SCIM2RestClient;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;

/**
 * Test class for Consent Management API v2 failure/negative paths.
 *
 * <p>Covers:
 * <ul>
 *   <li>404 Not Found for non-existent resources</li>
 *   <li>400 Bad Request for invalid payloads</li>
 *   <li>409 Conflict for duplicate resource creation</li>
 * </ul>
 */
public class ConsentManagementV2FailureTest extends ConsentManagementV2TestBase {

    private static final String NON_EXISTENT_ID = "ffffffff-ffff-ffff-ffff-ffffffffffff";
    private static final String FAILURE_TEST_USER_PASSWORD = "Admin@123";

    private SCIM2RestClient scim2RestClient;

    // Cross-user test state — created in @BeforeClass, used across the 4 cross-user tests.
    private String crossUserConsentId;
    private String crossUserPurposeId;
    private String crossUserElementId;
    private String crossUserUserAId;
    private String crossUserUserBId;
    private String crossUserUserBAuthName;

    @Factory(dataProvider = "restAPIUserConfigProvider")
    public ConsentManagementV2FailureTest(TestUserMode userMode) throws Exception {

        super.init(userMode);
        this.context = isServer;
        this.authenticatingUserName = context.getContextTenant().getTenantAdmin().getUserName();
        this.authenticatingCredential = context.getContextTenant().getTenantAdmin().getPassword();
        this.tenant = context.getContextTenant().getDomain();
    }

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {

        super.testInit(tenant);
        scim2RestClient = new SCIM2RestClient(serverURL, tenantInfo);
        setupCrossUserTestData();
    }

    @AfterClass(alwaysRun = true)
    @Override
    public void testConclude() throws Exception {

        RestAssured.basePath = basePath;
        try {
            if (crossUserUserAId != null) {
                scim2RestClient.deleteUser(crossUserUserAId);
            }
            if (crossUserUserBId != null) {
                scim2RestClient.deleteUser(crossUserUserBId);
            }
            if (crossUserPurposeId != null) {
                getResponseOfDelete(PURPOSES_ENDPOINT + "/" + crossUserPurposeId);
            }
            if (crossUserElementId != null) {
                getResponseOfDelete(ELEMENTS_ENDPOINT + "/" + crossUserElementId);
            }
        } finally {
            if (scim2RestClient != null) {
                scim2RestClient.closeHttpClient();
            }
            RestAssured.basePath = "";
            super.testConclude();
        }
    }

    // =========================================================================
    // Element failure tests
    // =========================================================================

    @Test
    public void testGetNonExistentElementReturns404() {

        getResponseOfGet(ELEMENTS_ENDPOINT + "/" + NON_EXISTENT_ID)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NOT_FOUND)
                .body("code", notNullValue());
    }

    @Test
    public void testDeleteNonExistentElementReturns404() {

        getResponseOfDelete(ELEMENTS_ENDPOINT + "/" + NON_EXISTENT_ID)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    public void testCreateElementWithMissingRequiredFieldReturns400() throws IOException {

        String body = readResource("create-element-missing-required.json");
        getResponseOfPostNoFilter(ELEMENTS_ENDPOINT, body)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body("code", notNullValue());
    }

    @Test
    public void testCreateDuplicateElementReturns409() throws IOException {

        String body = readResource("create-element-for-conflict.json");
        Response createResponse = getResponseOfPost(ELEMENTS_ENDPOINT, body);
        createResponse.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED);

        String elementId = createResponse.jsonPath().getString("id");

        try {
            getResponseOfPost(ELEMENTS_ENDPOINT, body)
                    .then()
                    .log().ifValidationFails()
                    .assertThat()
                    .statusCode(HttpStatus.SC_CONFLICT)
                    .body("code", notNullValue());
        } finally {
            getResponseOfDelete(ELEMENTS_ENDPOINT + "/" + elementId);
        }
    }

    // =========================================================================
    // Purpose failure tests
    // =========================================================================

    @Test
    public void testGetNonExistentPurposeReturns404() {

        getResponseOfGet(PURPOSES_ENDPOINT + "/" + NON_EXISTENT_ID)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NOT_FOUND)
                .body("code", notNullValue());
    }

    @Test
    public void testDeleteNonExistentPurposeReturns404() {

        getResponseOfDelete(PURPOSES_ENDPOINT + "/" + NON_EXISTENT_ID)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    public void testCreatePurposeWithMissingRequiredFieldReturns400() throws IOException {

        String body = readResource("create-purpose-missing-required.json");
        getResponseOfPostNoFilter(PURPOSES_ENDPOINT, body)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body("code", notNullValue());
    }

    @Test
    public void testCreateDuplicatePurposeReturns409() throws IOException {

        String body = readResource("create-purpose-for-conflict.json");
        Response createResponse = getResponseOfPost(PURPOSES_ENDPOINT, body);
        createResponse.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED);

        String purposeId = createResponse.jsonPath().getString("id");

        try {
            getResponseOfPost(PURPOSES_ENDPOINT, body)
                    .then()
                    .log().ifValidationFails()
                    .assertThat()
                    .statusCode(HttpStatus.SC_CONFLICT)
                    .body("code", notNullValue());
        } finally {
            getResponseOfDelete(PURPOSES_ENDPOINT + "/" + purposeId);
        }
    }

    // =========================================================================
    // Purpose version failure tests
    // =========================================================================

    @Test
    public void testGetPurposeVersionForNonExistentPurposeReturns404() {

        getResponseOfGet(PURPOSES_ENDPOINT + "/" + NON_EXISTENT_ID + VERSIONS_ENDPOINT + "/" + NON_EXISTENT_ID)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    public void testListPurposeVersionsForNonExistentPurposeReturns404() {

        getResponseOfGet(PURPOSES_ENDPOINT + "/" + NON_EXISTENT_ID + VERSIONS_ENDPOINT)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    public void testCreatePurposeVersionForNonExistentPurposeReturns404() throws IOException {

        String body = readResource("create-purpose-version-empty.json");
        getResponseOfPost(PURPOSES_ENDPOINT + "/" + NON_EXISTENT_ID + VERSIONS_ENDPOINT, body)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    public void testCreatePurposeVersionWithMissingVersionFieldReturns400() throws IOException {

        String purposeBody = readResource("create-purpose-for-conflict.json");
        Response purposeResponse = getResponseOfPost(PURPOSES_ENDPOINT, purposeBody);
        // If already exists from a prior run, just use NON_EXISTENT_ID to trigger 404 — the 400
        // schema validation fires before the purpose lookup, so we can use any UUID.
        String purposeId = purposeResponse.statusCode() == HttpStatus.SC_CREATED
                ? purposeResponse.jsonPath().getString("id")
                : NON_EXISTENT_ID;

        try {
            String body = readResource("create-purpose-version-missing-required.json");
            getResponseOfPostNoFilter(PURPOSES_ENDPOINT + "/" + purposeId + VERSIONS_ENDPOINT, body)
                    .then()
                    .log().ifValidationFails()
                    .assertThat()
                    .statusCode(HttpStatus.SC_BAD_REQUEST)
                    .body("code", notNullValue());
        } finally {
            if (!NON_EXISTENT_ID.equals(purposeId)) {
                getResponseOfDelete(PURPOSES_ENDPOINT + "/" + purposeId);
            }
        }
    }

    @Test
    public void testSetLatestVersionForNonExistentPurposeReturns404() throws IOException {

        String body = readResource("set-latest-version.json")
                .replace("VERSION_ID_PLACEHOLDER", NON_EXISTENT_ID);
        getResponseOfPut(PURPOSES_ENDPOINT + "/" + NON_EXISTENT_ID + "/versions/latest", body)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    public void testSetLatestVersionWithNonExistentVersionReturns404() throws IOException {

        String purposeBody = readResource("create-purpose-for-conflict.json");
        Response purposeResponse = getResponseOfPost(PURPOSES_ENDPOINT, purposeBody);
        purposeResponse.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED);
        String purposeId = purposeResponse.jsonPath().getString("id");

        try {
            String body = readResource("set-latest-version.json")
                    .replace("VERSION_ID_PLACEHOLDER", NON_EXISTENT_ID);
            getResponseOfPut(PURPOSES_ENDPOINT + "/" + purposeId + "/versions/latest", body)
                    .then()
                    .log().ifValidationFails()
                    .assertThat()
                    .statusCode(HttpStatus.SC_NOT_FOUND);
        } finally {
            if (!NON_EXISTENT_ID.equals(purposeId)) {
                getResponseOfDelete(PURPOSES_ENDPOINT + "/" + purposeId);
            }
        }
    }

    @Test
    public void testDeletePurposeVersionForNonExistentPurposeReturns404() {

        getResponseOfDelete(
                PURPOSES_ENDPOINT + "/" + NON_EXISTENT_ID + VERSIONS_ENDPOINT + "/" + NON_EXISTENT_ID)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    public void testCreateDuplicatePurposeVersionReturns409() throws IOException {

        String purposeBody = readResource("create-purpose-for-version-conflict.json");
        Response purposeResponse = getResponseOfPost(PURPOSES_ENDPOINT, purposeBody);
        purposeResponse.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED);
        String purposeId = purposeResponse.jsonPath().getString("id");

        try {
            String versionBody = readResource("create-purpose-version-empty.json");
            // First creation must succeed.
            getResponseOfPost(PURPOSES_ENDPOINT + "/" + purposeId + VERSIONS_ENDPOINT, versionBody)
                    .then()
                    .log().ifValidationFails()
                    .assertThat()
                    .statusCode(HttpStatus.SC_CREATED);

            // Second creation with the same version label must conflict.
            getResponseOfPost(PURPOSES_ENDPOINT + "/" + purposeId + VERSIONS_ENDPOINT, versionBody)
                    .then()
                    .log().ifValidationFails()
                    .assertThat()
                    .statusCode(HttpStatus.SC_CONFLICT)
                    .body("code", notNullValue());
        } finally {
            if (!NON_EXISTENT_ID.equals(purposeId)) {
                getResponseOfDelete(PURPOSES_ENDPOINT + "/" + purposeId);
            }
        }
    }

    // =========================================================================
    // Consent failure tests
    // =========================================================================

    @Test
    public void testGetNonExistentConsentReturns404() {

        getResponseOfGet(CONSENTS_ENDPOINT + "/" + NON_EXISTENT_ID)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    public void testRevokeNonExistentConsentReturns404() {

        getResponseOfPost(CONSENTS_ENDPOINT + "/" + NON_EXISTENT_ID + "/revoke", "")
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    public void testCreateConsentWithMissingRequiredFieldReturns400() throws IOException {

        String body = readResource("create-consent-missing-required.json");
        getResponseOfPostNoFilter(CONSENTS_ENDPOINT, body)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body("code", notNullValue());
    }

    @Test
    public void testValidateNonExistentConsentReturns404() {

        getResponseOfGet(CONSENTS_ENDPOINT + "/" + NON_EXISTENT_ID + "/validate")
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    public void testAuthorizeNonExistentConsentReturns404() {

        // The authorize endpoint lives on the user API (/me/consents/{id}/authorize).
        given()
                .auth().preemptive().basic(crossUserUserBAuthName, FAILURE_TEST_USER_PASSWORD)
                .contentType(ContentType.JSON)
                .header(HttpHeaders.ACCEPT, ContentType.JSON)
                .log().ifValidationFails()
                .body("{\"state\": \"APPROVED\"}")
                .post(getUserConsentApiBaseUrl() + "/" + NON_EXISTENT_ID + "/authorize")
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NOT_FOUND);
    }

    // =========================================================================
    // Protected resource tests - deletion with associated resources
    // =========================================================================

    @Test
    public void testDeleteElementWithAssociatedPurposeReturns409() throws IOException {

        String elementId = null;
        String purposeId = null;

        try {
            String elementBody = readResource("create-element-with-purpose.json");
            Response elementResponse = getResponseOfPost(ELEMENTS_ENDPOINT, elementBody);
            elementResponse.then()
                    .log().ifValidationFails()
                    .assertThat()
                    .statusCode(HttpStatus.SC_CREATED);
            elementId = elementResponse.jsonPath().getString("id");

            String purposeBody = readResource("create-purpose-with-element.json")
                    .replace("\"id\": \"1\"", "\"id\": \"" + elementId + "\"");
            Response purposeResponse = getResponseOfPost(PURPOSES_ENDPOINT, purposeBody);
            purposeResponse.then()
                    .log().ifValidationFails()
                    .assertThat()
                    .statusCode(HttpStatus.SC_CREATED);
            purposeId = purposeResponse.jsonPath().getString("id");

            // Attempt to delete the element that is associated with a purpose.
            getResponseOfDelete(ELEMENTS_ENDPOINT + "/" + elementId)
                    .then()
                    .log().ifValidationFails()
                    .assertThat()
                    .statusCode(HttpStatus.SC_CONFLICT)
                    .body("code", notNullValue());
        } finally {
            // Delete purpose first to unblock element deletion.
            if (purposeId != null) {
                getResponseOfDelete(PURPOSES_ENDPOINT + "/" + purposeId);
            }
            if (elementId != null) {
                getResponseOfDelete(ELEMENTS_ENDPOINT + "/" + elementId);
            }
        }
    }

    @Test
    public void testDeletePurposeWithAssociatedConsentReturns409() throws Exception {

        String elementId = null;
        String purposeId = null;
        String userId = null;

        try {
            String elementBody = readResource("create-element-delete-purpose.json");
            Response elementResponse = getResponseOfPost(ELEMENTS_ENDPOINT, elementBody);
            elementResponse.then()
                    .log().ifValidationFails()
                    .assertThat()
                    .statusCode(HttpStatus.SC_CREATED);
            elementId = elementResponse.jsonPath().getString("id");

            String purposeBody = readResource("create-purpose-delete-purpose.json");
            Response purposeResponse = getResponseOfPost(PURPOSES_ENDPOINT, purposeBody);
            purposeResponse.then()
                    .log().ifValidationFails()
                    .assertThat()
                    .statusCode(HttpStatus.SC_CREATED);
            purposeId = purposeResponse.jsonPath().getString("id");

            String userName = "consent_fail_delpurpose_user";
            userId = createTestUser(userName);
            String userAuthName = buildUserAuthName(userName);

            String consentBody = "{\"serviceId\": \"test-integration-service\", \"language\": \"en\","
                    + " \"purposes\": [{\"id\": \"" + purposeId + "\","
                    + " \"elements\": [{\"id\": \"" + elementId + "\"}]}]}";
            given()
                    .auth().preemptive().basic(userAuthName, FAILURE_TEST_USER_PASSWORD)
                    .contentType(ContentType.JSON)
                    .header(HttpHeaders.ACCEPT, ContentType.JSON)
                    .log().ifValidationFails()
                    .body(consentBody)
                    .post(getUserConsentApiBaseUrlForUser(userName))
                    .then()
                    .log().ifValidationFails()
                    .assertThat()
                    .statusCode(HttpStatus.SC_CREATED);

            // Attempt to delete the purpose that has an associated consent.
            getResponseOfDelete(PURPOSES_ENDPOINT + "/" + purposeId)
                    .then()
                    .log().ifValidationFails()
                    .assertThat()
                    .statusCode(HttpStatus.SC_CONFLICT)
                    .body("code", notNullValue());
        } finally {
            // Deleting the user cascades their consents, unblocking purpose/element deletion.
            if (userId != null) {
                scim2RestClient.deleteUser(userId);
            }
            if (purposeId != null) {
                getResponseOfDelete(PURPOSES_ENDPOINT + "/" + purposeId);
            }
            if (elementId != null) {
                getResponseOfDelete(ELEMENTS_ENDPOINT + "/" + elementId);
            }
        }
    }

    @Test
    public void testDeletePurposeVersionWithAssociatedConsentReturns409() throws Exception {

        String elementId = null;
        String secondElementId = null;
        String purposeId = null;
        String userId = null;

        try {
            String elementBody = readResource("create-element-delete-version-1.json");
            Response elementResponse = getResponseOfPost(ELEMENTS_ENDPOINT, elementBody);
            elementResponse.then()
                    .log().ifValidationFails()
                    .assertThat()
                    .statusCode(HttpStatus.SC_CREATED);
            elementId = elementResponse.jsonPath().getString("id");

            String secondElementBody = readResource("create-element-delete-version-2.json");
            Response secondElementResponse = getResponseOfPost(ELEMENTS_ENDPOINT, secondElementBody);
            secondElementResponse.then()
                    .log().ifValidationFails()
                    .assertThat()
                    .statusCode(HttpStatus.SC_CREATED);
            secondElementId = secondElementResponse.jsonPath().getString("id");

            String purposeBody = readResource("create-purpose-delete-version.json");
            Response purposeResponse = getResponseOfPost(PURPOSES_ENDPOINT, purposeBody);
            purposeResponse.then()
                    .log().ifValidationFails()
                    .assertThat()
                    .statusCode(HttpStatus.SC_CREATED);
            purposeId = purposeResponse.jsonPath().getString("id");

            String versionBody = readResource("create-purpose-version.json")
                    .replace("\"id\": \"2\"", "\"id\": \"" + secondElementId + "\"");
            Response versionResponse = getResponseOfPost(
                    PURPOSES_ENDPOINT + "/" + purposeId + "/versions", versionBody);
            versionResponse.then()
                    .log().ifValidationFails()
                    .assertThat()
                    .statusCode(HttpStatus.SC_CREATED);
            String versionId = versionResponse.jsonPath().getString("id");

            String setLatestVersionBody = readResource("set-latest-version.json")
                    .replace("VERSION_ID_PLACEHOLDER", versionId);
            getResponseOfPut(PURPOSES_ENDPOINT + "/" + purposeId + "/versions/latest", setLatestVersionBody)
                    .then()
                    .log().ifValidationFails()
                    .assertThat()
                    .statusCode(HttpStatus.SC_NO_CONTENT);

            String userName = "consent_fail_delversion_user";
            userId = createTestUser(userName);
            String userAuthName = buildUserAuthName(userName);

            String consentBody = "{\"serviceId\": \"test-integration-service\", \"language\": \"en\","
                    + " \"purposes\": [{\"id\": \"" + purposeId + "\","
                    + " \"elements\": [{\"id\": \"" + elementId + "\"}]}]}";
            given()
                    .auth().preemptive().basic(userAuthName, FAILURE_TEST_USER_PASSWORD)
                    .contentType(ContentType.JSON)
                    .header(HttpHeaders.ACCEPT, ContentType.JSON)
                    .log().ifValidationFails()
                    .body(consentBody)
                    .post(getUserConsentApiBaseUrlForUser(userName))
                    .then()
                    .log().ifValidationFails()
                    .assertThat()
                    .statusCode(HttpStatus.SC_CREATED);

            // Attempt to delete the purpose version that has an associated consent.
            getResponseOfDelete(PURPOSES_ENDPOINT + "/" + purposeId + "/versions/" + versionId)
                    .then()
                    .log().ifValidationFails()
                    .assertThat()
                    .statusCode(HttpStatus.SC_CONFLICT)
                    .body("code", notNullValue());
        } finally {
            // Deleting the user cascades their consents, unblocking purpose/element deletion.
            if (userId != null) {
                scim2RestClient.deleteUser(userId);
            }
            // Deleting the purpose cascades all its versions.
            if (purposeId != null) {
                getResponseOfDelete(PURPOSES_ENDPOINT + "/" + purposeId);
            }
            if (secondElementId != null) {
                getResponseOfDelete(ELEMENTS_ENDPOINT + "/" + secondElementId);
            }
            if (elementId != null) {
                getResponseOfDelete(ELEMENTS_ENDPOINT + "/" + elementId);
            }
        }
    }

    // =========================================================================
    // Cross-user access tests — non-admin subject enforcement
    // =========================================================================

    @Test
    public void testGetConsentOfAnotherUserReturns403() {

        // User B attempts to fetch User A's consent via the user API — must be rejected.
        given()
                .auth().preemptive().basic(crossUserUserBAuthName, FAILURE_TEST_USER_PASSWORD)
                .contentType(ContentType.JSON)
                .header(HttpHeaders.ACCEPT, ContentType.JSON)
                .log().ifValidationFails()
                .get(getUserConsentApiBaseUrl() + "/" + crossUserConsentId)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_FORBIDDEN)
                .body("code", notNullValue());
    }

    @Test
    public void testValidateConsentOfAnotherUserReturns403() {

        given()
                .auth().preemptive().basic(crossUserUserBAuthName, FAILURE_TEST_USER_PASSWORD)
                .contentType(ContentType.JSON)
                .header(HttpHeaders.ACCEPT, ContentType.JSON)
                .log().ifValidationFails()
                .get(getUserConsentApiBaseUrl() + "/" + crossUserConsentId + "/validate")
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_FORBIDDEN)
                .body("code", notNullValue());
    }

    @Test
    public void testRevokeConsentOfAnotherUserReturns403() {

        given()
                .auth().preemptive().basic(crossUserUserBAuthName, FAILURE_TEST_USER_PASSWORD)
                .contentType(ContentType.JSON)
                .header(HttpHeaders.ACCEPT, ContentType.JSON)
                .log().ifValidationFails()
                .post(getUserConsentApiBaseUrl() + "/" + crossUserConsentId + "/revoke")
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_FORBIDDEN)
                .body("code", notNullValue());
    }

    @Test
    public void testListConsentsAsUserScopedToCaller() {

        // User B's list on the user API must return only their own consents (none in this case).
        given()
                .auth().preemptive().basic(crossUserUserBAuthName, FAILURE_TEST_USER_PASSWORD)
                .contentType(ContentType.JSON)
                .header(HttpHeaders.ACCEPT, ContentType.JSON)
                .log().ifValidationFails()
                .get(getUserConsentApiBaseUrl())
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("size()", equalTo(0));
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private void setupCrossUserTestData() throws Exception {

        RestAssured.basePath = basePath;
        try {
            Response elementResponse = getResponseOfPost(ELEMENTS_ENDPOINT,
                    "{\"name\": \"consent_fail_crossuser_elem\", \"displayName\": \"Cross User Elem\","
                            + " \"description\": \"Cross user test element\"}");
            elementResponse.then().assertThat().statusCode(HttpStatus.SC_CREATED);
            crossUserElementId = elementResponse.jsonPath().getString("id");

            Response purposeResponse = getResponseOfPost(PURPOSES_ENDPOINT,
                    "{\"name\": \"consent_fail_crossuser_purpose\", \"description\": \"Cross user purpose\","
                            + " \"type\": \"Core\", \"version\": \"1\","
                            + " \"elements\": [{\"id\": \"" + crossUserElementId + "\", \"mandatory\": true}]}");
            purposeResponse.then().assertThat().statusCode(HttpStatus.SC_CREATED);
            crossUserPurposeId = purposeResponse.jsonPath().getString("id");

            crossUserUserAId = createTestUser("consent_fail_crossuser_A");
            crossUserUserBId = createTestUser("consent_fail_crossuser_B");
            crossUserUserBAuthName = buildUserAuthName("consent_fail_crossuser_B");
            String userAAuthName = buildUserAuthName("consent_fail_crossuser_A");

            // Create the consent via the user API — user A creates their own consent.
            String consentBody = "{\"serviceId\": \"cross-user-test\", \"language\": \"en\","
                    + " \"purposes\": [{\"id\": \"" + crossUserPurposeId + "\","
                    + " \"elements\": [{\"id\": \"" + crossUserElementId + "\"}]}]}";
            Response consentResponse = given()
                    .auth().preemptive().basic(userAAuthName, FAILURE_TEST_USER_PASSWORD)
                    .contentType(ContentType.JSON)
                    .header(HttpHeaders.ACCEPT, ContentType.JSON)
                    .body(consentBody)
                    .post(getUserConsentApiBaseUrlForUser("consent_fail_crossuser_A"));
            consentResponse.then().assertThat().statusCode(HttpStatus.SC_CREATED);
            crossUserConsentId = consentResponse.jsonPath().getString("id");
        } finally {
            RestAssured.basePath = "";
        }
    }

    private String createTestUser(String userName) throws Exception {

        UserObject user = new UserObject();
        user.setUserName(userName);
        user.setPassword(FAILURE_TEST_USER_PASSWORD);
        user.setName(new Name().givenName("Failure").familyName("TestUser"));
        user.addEmail(new Email().value(userName + "@wso2.com"));
        return scim2RestClient.createUser(user);
    }

    private String buildUserAuthName(String userName) {

        return MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenant)
                ? userName
                : userName + "@" + tenant;
    }

    private String getUserConsentApiBaseUrl() {

        if (MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenant)) {
            return serverURL + "api/users/v1/me/consents";
        }
        return serverURL + "t/" + tenant + "/api/users/v1/me/consents";
    }

    private String getUserConsentApiBaseUrlForUser(String userName) {

        return getUserConsentApiBaseUrl();
    }
}

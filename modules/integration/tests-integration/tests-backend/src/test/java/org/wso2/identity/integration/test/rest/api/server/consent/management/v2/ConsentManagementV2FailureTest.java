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
import io.restassured.response.Response;
import org.apache.commons.lang.StringUtils;
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
import java.rmi.RemoteException;

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

    private static final int NON_EXISTENT_ID = 999999;
    private static final String NON_EXISTENT_RECEIPT_ID = "non-existent-receipt-id-00000000";

    @Factory(dataProvider = "restAPIUserConfigProvider")
    public ConsentManagementV2FailureTest(TestUserMode userMode) throws Exception {

        super.init(userMode);
        this.context = isServer;
        this.authenticatingUserName = context.getContextTenant().getTenantAdmin().getUserName();
        this.authenticatingCredential = context.getContextTenant().getTenantAdmin().getPassword();
        this.tenant = context.getContextTenant().getDomain();
    }

    @BeforeClass(alwaysRun = true)
    public void init() throws RemoteException {

        super.testInit(tenant);
    }

    @AfterClass(alwaysRun = true)
    public void testConclude() throws Exception {

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

        // Create an element.
        String body = readResource("create-element-for-conflict.json");
        Response createResponse = getResponseOfPost(ELEMENTS_ENDPOINT, body);
        createResponse.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED);

        int elementId = createResponse.jsonPath().getInt("id");

        try {
            // Attempt to create duplicate element with the same name.
            getResponseOfPost(ELEMENTS_ENDPOINT, body)
                    .then()
                    .log().ifValidationFails()
                    .assertThat()
                    .statusCode(HttpStatus.SC_CONFLICT)
                    .body("code", notNullValue());
        } finally {
            // Clean up.
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

        // Create a purpose.
        String body = readResource("create-purpose-for-conflict.json");
        Response createResponse = getResponseOfPost(PURPOSES_ENDPOINT, body);
        createResponse.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED);

        int purposeId = createResponse.jsonPath().getInt("id");

        try {
            // Attempt to create a duplicate purpose with the same name.
            getResponseOfPost(PURPOSES_ENDPOINT, body)
                    .then()
                    .log().ifValidationFails()
                    .assertThat()
                    .statusCode(HttpStatus.SC_CONFLICT)
                    .body("code", notNullValue());
        } finally {
            // Clean up.
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

        String body = readResource("create-purpose-version.json");
        getResponseOfPost(PURPOSES_ENDPOINT + "/" + NON_EXISTENT_ID + VERSIONS_ENDPOINT, body)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NOT_FOUND);
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

    // =========================================================================
    // Consent failure tests
    // =========================================================================

    @Test
    public void testGetNonExistentConsentReturns404() {

        getResponseOfGet(CONSENTS_ENDPOINT + "/" + NON_EXISTENT_RECEIPT_ID)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    public void testRevokeNonExistentConsentReturns404() {

        getResponseOfPost(CONSENTS_ENDPOINT + "/" + NON_EXISTENT_RECEIPT_ID + "/revoke", "")
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

    // =========================================================================
    // Protected resource tests - deletion with associated consents
    // =========================================================================

    @Test
    public void testDeleteElementWithAssociatedPurposeReturns409() throws IOException {

        // Create an element with unique name
        String elementBody = readResource("create-element-with-purpose.json");
        Response elementResponse = getResponseOfPost(ELEMENTS_ENDPOINT, elementBody);
        elementResponse.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED);
        int elementId = elementResponse.jsonPath().getInt("id");

        // Create a purpose that uses this element
        String purposeBody = readResource("create-purpose-with-element.json")
                .replace("\"elementId\": 1", "\"elementId\": " + elementId);
        Response purposeResponse = getResponseOfPost(PURPOSES_ENDPOINT, purposeBody);
        purposeResponse.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED);

        // Attempt to delete the element that is associated with a purpose
        getResponseOfDelete(ELEMENTS_ENDPOINT + "/" + elementId)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CONFLICT)
                .body("code", notNullValue());
        // Note: Cleanup is handled by test framework teardown
    }

    @Test
    public void testDeletePurposeWithAssociatedConsentReturns409() throws IOException {

        // Create an element with unique name
        String elementBody = readResource("create-element-delete-purpose.json");
        Response elementResponse = getResponseOfPost(ELEMENTS_ENDPOINT, elementBody);
        elementResponse.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED);
        int elementId = elementResponse.jsonPath().getInt("id");

        // Create a purpose with unique name for this test
        String purposeBody = readResource("create-purpose-delete-purpose.json");
        Response purposeResponse = getResponseOfPost(PURPOSES_ENDPOINT, purposeBody);
        purposeResponse.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED);
        int purposeId = purposeResponse.jsonPath().getInt("id");

        // Create a consent with this purpose
        String consentBody = readResource("create-consent.json")
                .replace("\"purposeId\": 1", "\"purposeId\": " + purposeId)
                .replace("\"elementId\": 1", "\"elementId\": " + elementId);
        getResponseOfPost(CONSENTS_ENDPOINT, consentBody)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED);

        // Attempt to delete the purpose that has an associated consent
        getResponseOfDelete(PURPOSES_ENDPOINT + "/" + purposeId)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CONFLICT)
                .body("code", notNullValue());
        // Note: Cleanup is handled by test framework teardown (purposes and elements with consents
        // cannot be deleted and will be cleaned up with the test database reset)
    }

    @Test
    public void testDeletePurposeVersionWithAssociatedConsentReturns409() throws IOException {

        // Create elements with unique names
        String elementBody = readResource("create-element-delete-version-1.json");
        Response elementResponse = getResponseOfPost(ELEMENTS_ENDPOINT, elementBody);
        elementResponse.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED);
        int elementId = elementResponse.jsonPath().getInt("id");

        String secondElementBody = readResource("create-element-delete-version-2.json");
        Response secondElementResponse = getResponseOfPost(ELEMENTS_ENDPOINT, secondElementBody);
        secondElementResponse.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED);
        int secondElementId = secondElementResponse.jsonPath().getInt("id");

        // Create a purpose with unique name for this test
        String purposeBody = readResource("create-purpose-delete-version.json");
        Response purposeResponse = getResponseOfPost(PURPOSES_ENDPOINT, purposeBody);
        purposeResponse.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED);
        int purposeId = purposeResponse.jsonPath().getInt("id");

        // Create a version
        String versionBody = readResource("create-purpose-version.json")
                .replace("\"elementId\": 2", "\"elementId\": " + secondElementId);
        Response versionResponse = getResponseOfPost(PURPOSES_ENDPOINT + "/" + purposeId + "/versions", versionBody);
        versionResponse.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED);
        int versionId = versionResponse.jsonPath().getInt("id");

        // Create a consent with this purpose
        String consentBody = readResource("create-consent.json")
                .replace("\"purposeId\": 1", "\"purposeId\": " + purposeId)
                .replace("\"elementId\": 1", "\"elementId\": " + elementId);
        getResponseOfPost(CONSENTS_ENDPOINT, consentBody)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED);

        // Attempt to delete the purpose version that has an associated consent
        getResponseOfDelete(PURPOSES_ENDPOINT + "/" + purposeId + "/versions/" + versionId)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CONFLICT)
                .body("code", notNullValue());
        // Note: Cleanup is handled by test framework teardown (elements and purposes with consents
        // cannot be deleted and will be cleaned up with the test database reset)
    }
}

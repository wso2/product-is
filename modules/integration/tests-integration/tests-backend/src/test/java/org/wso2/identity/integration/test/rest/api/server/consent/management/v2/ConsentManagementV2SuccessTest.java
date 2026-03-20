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
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.io.IOException;
import java.rmi.RemoteException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

/**
 * Test class for Consent Management API v2 success paths.
 *
 * <p>Covers:
 * <ul>
 *   <li>Element CRUD: create, get, list, delete</li>
 *   <li>Purpose CRUD: create, get, list, delete</li>
 *   <li>Purpose version CRUD: create, get, list, delete</li>
 *   <li>Consent CRUD: create, get, list, revoke</li>
 * </ul>
 */
public class ConsentManagementV2SuccessTest extends ConsentManagementV2TestBase {

    private static String createdElementId;
    private static String createdSecondElementId;
    private static String createdPurposeId;
    private static String createdVersionLabel;
    private static String createdVersionId;
    private static String createdSecondVersionLabel;
    private static String createdSecondVersionId;
    private static String createdReceiptId;
    private static String deletableElementId;
    private static String deletablePurposeId;

    @Factory(dataProvider = "restAPIUserConfigProvider")
    public ConsentManagementV2SuccessTest(TestUserMode userMode) throws Exception {

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

    @DataProvider(name = "restAPIUserConfigProvider")
    public static Object[][] restAPIUserConfigProvider() {

        return new Object[][]{
                {TestUserMode.SUPER_TENANT_ADMIN},
                {TestUserMode.TENANT_ADMIN}
        };
    }

    // =========================================================================
    // Element tests
    // =========================================================================

    @Test(groups = "wso2.is")
    public void testCreateElement() throws IOException {

        String body = readResource("create-element.json");
        Response response = getResponseOfPost(ELEMENTS_ENDPOINT, body);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .header(HttpHeaders.LOCATION, notNullValue())
                .body("elementId", notNullValue())
                .body("name", equalTo("email_address"))
                .body("displayName", equalTo("Email Address"))
                .body("description", equalTo("User's primary email address"));

        createdElementId = response.jsonPath().getString("elementId");
    }

    @Test(groups = "wso2.is", dependsOnMethods = {"testCreateElement"})
    public void testGetElement() {

        getResponseOfGet(ELEMENTS_ENDPOINT + "/" + createdElementId)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("elementId", equalTo(createdElementId))
                .body("name", equalTo("email_address"))
                .body("displayName", equalTo("Email Address"))
                .body("description", equalTo("User's primary email address"));
    }

    @Test(groups = "wso2.is", dependsOnMethods = {"testGetElement"})
    public void testCreateSecondElement() throws IOException {

        String body = readResource("create-element-second.json");
        Response response = getResponseOfPost(ELEMENTS_ENDPOINT, body);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .header(HttpHeaders.LOCATION, notNullValue())
                .body("elementId", notNullValue())
                .body("name", equalTo("phone_number"))
                .body("displayName", equalTo("Phone Number"));

        createdSecondElementId = response.jsonPath().getString("elementId");
    }

    @Test(groups = "wso2.is", dependsOnMethods = {"testCreateSecondElement"})
    public void testListElements() {

        getResponseOfGet(ELEMENTS_ENDPOINT)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("items", notNullValue())
                .body("count", greaterThanOrEqualTo(2))
                .body("items.elementId", hasSize(greaterThanOrEqualTo(2)))
                .body("items.find { it.elementId == '" + createdElementId + "' }.name", equalTo("email_address"))
                .body("items.find { it.elementId == '" + createdSecondElementId + "' }.name", equalTo("phone_number"));
    }

    // =========================================================================
    // Purpose tests
    // =========================================================================

    @Test(groups = "wso2.is", dependsOnMethods = {"testCreateElement"})
    public void testCreatePurpose() throws IOException {

        // Replace placeholder element ID with the actual created element ID.
        String body = readResource("create-purpose.json")
                .replace("\"elementId\": 1", "\"elementId\": \"" + createdElementId + "\"");
        Response response = getResponseOfPost(PURPOSES_ENDPOINT, body);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .header(HttpHeaders.LOCATION, notNullValue())
                .body("purposeId", notNullValue())
                .body("name", equalTo("User Authentication"))
                .body("description", equalTo("To authenticate users and manage their identity"))
                .body("type", equalTo("Core Identity"))
                .body("latestVersion.version", equalTo("1"));

        createdPurposeId = response.jsonPath().getString("purposeId");
    }

    @Test(groups = "wso2.is", dependsOnMethods = {"testCreatePurpose"})
    public void testGetPurpose() {

        getResponseOfGet(PURPOSES_ENDPOINT + "/" + createdPurposeId)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("purposeId", equalTo(createdPurposeId))
                .body("name", equalTo("User Authentication"))
                .body("description", equalTo("To authenticate users and manage their identity"))
                .body("type", equalTo("Core Identity"))
                .body("latestVersion.version", equalTo("1"))
                .body("elements", hasSize(1))
                .body("elements.find { it.elementId == '" + createdElementId + "' }", notNullValue())
                .body("elements.find { it.elementId == '" + createdElementId + "' }.mandatory", is(true));
    }

    @Test(groups = "wso2.is", dependsOnMethods = {"testGetPurpose"})
    public void testListPurposes() {

        getResponseOfGet(PURPOSES_ENDPOINT)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("items", notNullValue())
                .body("count", greaterThanOrEqualTo(1))
                .body("items.find { it.purposeId == '" + createdPurposeId + "' }.name", equalTo("User Authentication"))
                .body("items.find { it.purposeId == '" + createdPurposeId + "' }.type", equalTo("Core Identity"));
    }

    @Test(groups = "wso2.is", dependsOnMethods = {"testGetPurpose"})
    public void testListPurposesWithGroupFilter() {

        getResponseOfGet(PURPOSES_ENDPOINT + "?type=Core Identity")
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("items", notNullValue())
                .body("count", greaterThanOrEqualTo(1))
                .body("items.find { it.purposeId == '" + createdPurposeId + "' }.name", equalTo("User Authentication"))
                .body("items.findAll { it.type != 'Core Identity' }.size()", equalTo(0));
    }

    // =========================================================================
    // Purpose version tests
    // =========================================================================

    /**
     * Creates a second explicit version with a user-provided version label.
     * The version field is mandatory; the API no longer auto-snapshots.
     */
    @Test(groups = "wso2.is", dependsOnMethods = { "testCreateSecondElement", "testGetPurpose" })
    public void testCreatePurposeVersion() throws IOException {

        // v2 uses the second element (phone_number) only.
        String body = readResource("create-purpose-version.json")
                .replace("\"elementId\": 2", "\"elementId\": \"" + createdSecondElementId + "\"");
        Response response = getResponseOfPost(
                PURPOSES_ENDPOINT + "/" + createdPurposeId + VERSIONS_ENDPOINT, body);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .header(HttpHeaders.LOCATION, notNullValue())
                .body("versionId", notNullValue())
                .body("version", equalTo("v2"))
                .body("description", equalTo("Updated consent elements for enhanced authentication"))
                // v2 must contain the second element (phone_number).
                .body("elements.find { it.elementId == '" + createdSecondElementId + "' }", notNullValue())
                // v2 must NOT contain the first element (email_address).
                .body("elements.find { it.elementId == '" + createdElementId + "' }", equalTo(null));

        createdVersionLabel = response.jsonPath().getString("version");
        createdVersionId = response.jsonPath().getString("versionId");
    }

    /**
     * After creating an explicit version, the list must contain exactly 2 entries:
     * the initial version created with the purpose ("1") and the newly created version ("v2").
     */
    @Test(groups = "wso2.is", dependsOnMethods = {"testCreatePurposeVersion"})
    public void testVersionListContainsTwoVersionsAfterVersionCreate() {

        getResponseOfGet(PURPOSES_ENDPOINT + "/" + createdPurposeId + VERSIONS_ENDPOINT)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("count", equalTo(2))
                .body("items.size()", equalTo(2));
    }

    /**
     * Verifies that version "1" (created atomically with the purpose) contains the original
     * elements, and version "v2" contains only the second element.
     * Elements are not included in list responses (PurposeVersionSummaryDTO has no elements field);
     * this test uses GET single-version to verify element content per version.
     */
    @Test(groups = "wso2.is", dependsOnMethods = {"testVersionListContainsTwoVersionsAfterVersionCreate"})
    public void testVersionContentMatchesExpectedElements() {

        // Extract version "1"'s versionId from the list.
        Response versionsResponse = getResponseOfGet(
                PURPOSES_ENDPOINT + "/" + createdPurposeId + VERSIONS_ENDPOINT);
        versionsResponse.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);

        String v1Id = versionsResponse.jsonPath()
                .getString("items.find { it.version == '1' }.versionId");

        // GET version "1" directly and verify it contains the original element (email_address).
        getResponseOfGet(PURPOSES_ENDPOINT + "/" + createdPurposeId + VERSIONS_ENDPOINT + "/" + v1Id)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("version", equalTo("1"))
                .body("elements.find { it.elementId == '" + createdElementId + "' }", notNullValue())
                .body("elements.find { it.elementId == '" + createdSecondElementId + "' }", equalTo(null));

        // GET version "v2" directly and verify it contains only the second element (phone_number).
        getResponseOfGet(PURPOSES_ENDPOINT + "/" + createdPurposeId + VERSIONS_ENDPOINT + "/" + createdVersionId)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("version", equalTo("v2"))
                .body("elements.find { it.elementId == '" + createdSecondElementId + "' }", notNullValue())
                .body("elements.find { it.elementId == '" + createdElementId + "' }", equalTo(null));
    }

    @Test(groups = "wso2.is", dependsOnMethods = {"testVersionContentMatchesExpectedElements"})
    public void testGetPurposeVersion() {

        getResponseOfGet(PURPOSES_ENDPOINT + "/" + createdPurposeId + VERSIONS_ENDPOINT + "/" + createdVersionId)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("versionId", notNullValue())
                .body("version", equalTo("v2"))
                .body("description", equalTo("Updated consent elements for enhanced authentication"))
                .body("elements", hasSize(1))
                .body("elements.find { it.elementId == '" + createdSecondElementId + "' }", notNullValue())
                .body("elements.find { it.elementId == '" + createdSecondElementId + "' }.mandatory", is(true));
    }

    /**
     * Creates a third version ("v3") and verifies the list contains exactly 3 entries.
     * No auto-snapshot should occur.
     */
    @Test(groups = "wso2.is", dependsOnMethods = {"testGetPurposeVersion"})
    public void testSubsequentVersionCreationDoesNotTriggerAutoSnapshot() throws IOException {

        // v3 uses both elements (email_address + phone_number).
        String body = readResource("create-purpose-version-second.json")
                .replace("\"elementId\": 1", "\"elementId\": \"" + createdElementId + "\"")
                .replace("\"elementId\": 2", "\"elementId\": \"" + createdSecondElementId + "\"");
        Response response = getResponseOfPost(
                PURPOSES_ENDPOINT + "/" + createdPurposeId + VERSIONS_ENDPOINT, body);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .header(HttpHeaders.LOCATION, notNullValue())
                .body("versionId", notNullValue())
                .body("version", equalTo("v3"))
                .body("description", equalTo("Third version with both elements"))
                // v3 must contain both elements.
                .body("elements", hasSize(2))
                .body("elements.find { it.elementId == '" + createdElementId + "' }", notNullValue())
                .body("elements.find { it.elementId == '" + createdElementId + "' }.mandatory", is(true))
                .body("elements.find { it.elementId == '" + createdSecondElementId + "' }", notNullValue())
                .body("elements.find { it.elementId == '" + createdSecondElementId + "' }.mandatory", is(false));

        createdSecondVersionLabel = response.jsonPath().getString("version");
        createdSecondVersionId = response.jsonPath().getString("versionId");

        // Verify count is exactly 3, not 4 — no extra auto-snapshot.
        getResponseOfGet(PURPOSES_ENDPOINT + "/" + createdPurposeId + VERSIONS_ENDPOINT)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("count", equalTo(3))
                .body("items.size()", equalTo(3));
    }

    @Test(groups = "wso2.is", dependsOnMethods = {"testSubsequentVersionCreationDoesNotTriggerAutoSnapshot"})
    public void testListPurposeVersions() {

        getResponseOfGet(PURPOSES_ENDPOINT + "/" + createdPurposeId + VERSIONS_ENDPOINT)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("items", notNullValue())
                .body("count", equalTo(3))
                .body("items.size()", equalTo(3))
                .body("items.version", hasSize(3))
                .body("items.find { it.version == '1' }", notNullValue())
                .body("items.find { it.version == 'v2' }.versionId", notNullValue())
                .body("items.find { it.version == 'v3' }.versionId", notNullValue());
    }

    @Test(groups = "wso2.is", dependsOnMethods = {"testListPurposeVersions"})
    public void testSetLatestVersion() throws IOException {

        // Set v2 as the latest version (createdVersionId holds v2's UUID).
        String body = readResource("set-latest-version.json")
                .replace("VERSION_ID_PLACEHOLDER", createdVersionId);
        getResponseOfPut(PURPOSES_ENDPOINT + "/" + createdPurposeId + "/latest-version", body)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NO_CONTENT);
    }

    @Test(groups = "wso2.is", dependsOnMethods = {"testSetLatestVersion"})
    public void testDeletePurposeVersion() {

        getResponseOfDelete(PURPOSES_ENDPOINT + "/" + createdPurposeId + VERSIONS_ENDPOINT + "/" + createdSecondVersionId)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NO_CONTENT);
    }

    // =========================================================================
    // Consent tests
    // =========================================================================

    @Test(groups = "wso2.is", dependsOnMethods = { "testDeletePurposeVersion" })
    public void testCreateConsent() throws IOException {

        String body = readResource("create-consent.json")
                .replace("\"subjectId\": \"1\"", "\"subjectId\": \"" + MultitenantUtils.getTenantAwareUsername(authenticatingUserName) + "\"")
                .replace("\"purposeId\": \"1\"", "\"purposeId\": \"" + createdPurposeId + "\"")
                .replace("\"elementId\": \"1\"", "\"elementId\": \"" + createdElementId + "\"");
        Response response = getResponseOfPost(CONSENTS_ENDPOINT, body);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .header(HttpHeaders.LOCATION, notNullValue())
                .body("consentId", notNullValue());

        createdReceiptId = response.jsonPath().getString("consentId");
    }

    @Test(groups = "wso2.is", dependsOnMethods = {"testCreateConsent"})
    public void testGetConsent() {

        getResponseOfGet(CONSENTS_ENDPOINT + "/" + createdReceiptId)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("consentId", equalTo(createdReceiptId))
                .body("subjectId", equalTo(MultitenantUtils.getTenantAwareUsername(authenticatingUserName)))
                .body("serviceId", equalTo("test-integration-service"))
                .body("language", equalTo("en"))
                .body("state", equalTo("ACTIVE"))
                .body("purposes", hasSize(1))
                .body("purposes[0].purposeId", equalTo(createdPurposeId))
                .body("purposes[0].purposeVersionId", equalTo(createdVersionId))
                .body("purposes[0].elements", hasSize(1))
                .body("purposes[0].elements[0].elementId", equalTo(createdElementId));
    }

    @Test(groups = "wso2.is", dependsOnMethods = {"testGetConsent"})
    public void testListConsents() {

        getResponseOfGet(CONSENTS_ENDPOINT)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("items", notNullValue())
                .body("count", greaterThanOrEqualTo(1))
                .body("items.find { it.consentId == '" + createdReceiptId + "' }", notNullValue())
                .body("items.find { it.consentId == '" + createdReceiptId + "' }.serviceId",
                        equalTo("test-integration-service"))
                .body("items.find { it.consentId == '" + createdReceiptId + "' }.state",
                        equalTo("ACTIVE"));
    }

    @Test(groups = "wso2.is", dependsOnMethods = {"testListConsents"})
    public void testListConsentsWithPurposeIdFilter() {

        getResponseOfGet(CONSENTS_ENDPOINT + "?purposeId=" + createdPurposeId)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("count", greaterThanOrEqualTo(1))
                .body("items.find { it.consentId == '" + createdReceiptId + "' }", notNullValue());
    }

    @Test(groups = "wso2.is", dependsOnMethods = {"testListConsentsWithPurposeIdFilter"})
    public void testRevokeConsent() {

        getResponseOfPost(CONSENTS_ENDPOINT + "/" + createdReceiptId + "/revoke", "")
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NO_CONTENT);
    }

    @Test(groups = "wso2.is", dependsOnMethods = {"testRevokeConsent"})
    public void testRevokeConsentIdempotent() {

        // Second revoke of an already-revoked consent must also succeed with 204.
        getResponseOfPost(CONSENTS_ENDPOINT + "/" + createdReceiptId + "/revoke", "")
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NO_CONTENT);
    }

    @Test(groups = "wso2.is", dependsOnMethods = {"testRevokeConsentIdempotent"})
    public void testGetRevokedConsentStillAccessible() {

        getResponseOfGet(CONSENTS_ENDPOINT + "/" + createdReceiptId)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("consentId", equalTo(createdReceiptId))
                .body("state", equalTo("REVOKED"));
    }

    // =========================================================================
    // Cleanup / Delete tests (ordered after all read tests)
    // =========================================================================

    @Test(groups = "wso2.is", dependsOnMethods = {"testRevokeConsent"})
    public void testCreateDeletableElement() throws IOException {

        String body = readResource("create-element-deletable.json");
        Response response = getResponseOfPost(ELEMENTS_ENDPOINT, body);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .header(HttpHeaders.LOCATION, notNullValue())
                .body("elementId", notNullValue())
                .body("name", equalTo("address"))
                .body("displayName", equalTo("Address"))
                .body("description", equalTo("User's physical address"));

        deletableElementId = response.jsonPath().getString("elementId");
    }

    @Test(groups = "wso2.is", dependsOnMethods = {"testCreateDeletableElement"})
    public void testDeleteElement() {

        getResponseOfDelete(ELEMENTS_ENDPOINT + "/" + deletableElementId)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NO_CONTENT);
    }

    @Test(groups = "wso2.is", dependsOnMethods = {"testRevokeConsent"})
    public void testCreateDeletablePurpose() throws IOException {

        String body = readResource("create-purpose-deletable.json")
                .replace("\"elementId\": 1", "\"elementId\": \"" + createdElementId + "\"");
        Response response = getResponseOfPost(PURPOSES_ENDPOINT, body);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .header(HttpHeaders.LOCATION, notNullValue())
                .body("purposeId", notNullValue())
                .body("name", equalTo("Data Cleanup"))
                .body("description", equalTo("To remove user data upon account deletion"))
                .body("type", equalTo("Core Identity"));

        deletablePurposeId = response.jsonPath().getString("purposeId");
    }

    @Test(groups = "wso2.is", dependsOnMethods = {"testCreateDeletablePurpose"})
    public void testDeletePurpose() {

        getResponseOfDelete(PURPOSES_ENDPOINT + "/" + deletablePurposeId)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NO_CONTENT);
    }

    @Test(groups = "wso2.is", dependsOnMethods = { "testGetRevokedConsentStillAccessible", "testDeletePurpose" })
    public void testGetDeletedPurposeReturns404() {

        getResponseOfGet(PURPOSES_ENDPOINT + "/" + deletablePurposeId)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test(groups = "wso2.is", dependsOnMethods = { "testDeleteElement" })
    public void testGetDeletedElementReturns404() {

        getResponseOfGet(ELEMENTS_ENDPOINT + "/" + deletableElementId)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NOT_FOUND);
    }
}

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
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.identity.integration.test.rest.api.user.common.model.Email;
import org.wso2.identity.integration.test.rest.api.user.common.model.Name;
import org.wso2.identity.integration.test.rest.api.user.common.model.UserObject;
import org.wso2.identity.integration.test.restclients.SCIM2RestClient;

import java.io.IOException;
import java.rmi.RemoteException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
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

    private static final String CONSENT_TEST_USER_NAME = "consent_test_user";
    private static final String CONSENT_TEST_USER_PASSWORD = "Admin@123";

    private static String createdElementId;
    private static String createdSecondElementId;
    private static String createdPurposeId;
    private static String createdVersionLabel;
    private static String createdVersionId;
    private static String createdSecondVersionLabel;
    private static String createdSecondVersionId;
    private static String createdReceiptId;
    private static String testUserId;

    private SCIM2RestClient scim2RestClient;
    private String consentTestUserAuthName;

    @Factory(dataProvider = "restAPIUserConfigProvider")
    public ConsentManagementV2SuccessTest(TestUserMode userMode) throws Exception {

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
        consentTestUserAuthName = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenant)
                ? CONSENT_TEST_USER_NAME
                : CONSENT_TEST_USER_NAME + "@" + tenant;

        UserObject testUser = new UserObject();
        testUser.setUserName(CONSENT_TEST_USER_NAME);
        testUser.setPassword(CONSENT_TEST_USER_PASSWORD);
        testUser.setName(new Name().givenName("Consent").familyName("TestUser"));
        testUser.addEmail(new Email().value("consent_test_user@wso2.com"));
        testUserId = scim2RestClient.createUser(testUser);
    }

    @AfterClass(alwaysRun = true)
    @Override
    public void testConclude() throws Exception {

        // @AfterMethod resets RestAssured.basePath; restore it so cleanup requests hit the right endpoints.
        RestAssured.basePath = basePath;
        try {
            if (scim2RestClient != null && testUserId != null) {
                scim2RestClient.deleteUser(testUserId);
            }
            if (createdPurposeId != null) {
                getResponseOfDelete(PURPOSES_ENDPOINT + "/" + createdPurposeId);
            }
            if (createdElementId != null) {
                getResponseOfDelete(ELEMENTS_ENDPOINT + "/" + createdElementId);
            }
            if (createdSecondElementId != null) {
                getResponseOfDelete(ELEMENTS_ENDPOINT + "/" + createdSecondElementId);
            }
        } finally {
            if (scim2RestClient != null) {
                scim2RestClient.closeHttpClient();
            }
            super.testConclude();
        }
    }

    private String getUserConsentApiBaseUrl() {

        if (MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenant)) {
            return serverURL + "api/users/v1/me/consents";
        }
        return serverURL + "t/" + tenant + "/api/users/v1/me/consents";
    }

    private Response userApiGet(String url) {

        return given()
                .auth().preemptive().basic(consentTestUserAuthName, CONSENT_TEST_USER_PASSWORD)
                .contentType(ContentType.JSON)
                .header(HttpHeaders.ACCEPT, ContentType.JSON)
                .log().ifValidationFails()
                .get(url);
    }

    private Response userApiPost(String url, String body) {

        return given()
                .auth().preemptive().basic(consentTestUserAuthName, CONSENT_TEST_USER_PASSWORD)
                .contentType(ContentType.JSON)
                .header(HttpHeaders.ACCEPT, ContentType.JSON)
                .log().ifValidationFails()
                .body(body)
                .post(url);
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
                .body("id", notNullValue())
                .body("name", equalTo("email_address"))
                .body("displayName", equalTo("Email Address"))
                .body("description", equalTo("User's primary email address"))
                .body("properties.dataCategory", equalTo("personal"))
                .body("properties.retentionPeriod", equalTo("365"));

        createdElementId = response.jsonPath().getString("id");
    }

    @Test(groups = "wso2.is", dependsOnMethods = {"testCreateElement"})
    public void testGetElement() {

        getResponseOfGet(ELEMENTS_ENDPOINT + "/" + createdElementId)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("id", equalTo(createdElementId))
                .body("name", equalTo("email_address"))
                .body("displayName", equalTo("Email Address"))
                .body("description", equalTo("User's primary email address"))
                .body("properties.dataCategory", equalTo("personal"))
                .body("properties.retentionPeriod", equalTo("365"));
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
                .body("id", notNullValue())
                .body("name", equalTo("phone_number"))
                .body("displayName", equalTo("Phone Number"));

        createdSecondElementId = response.jsonPath().getString("id");
    }

    @Test(groups = "wso2.is", dependsOnMethods = {"testCreateSecondElement"})
    public void testListElements() {

        getResponseOfGet(ELEMENTS_ENDPOINT)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("Elements", notNullValue())
                .body("totalResults", greaterThanOrEqualTo(2))
                .body("Elements.id", hasSize(greaterThanOrEqualTo(2)))
                .body("Elements.find { it.id == '" + createdElementId + "' }.name", equalTo("email_address"))
                .body("Elements.find { it.id == '" + createdElementId + "' }.properties.dataCategory",
                        equalTo("personal"))
                .body("Elements.find { it.id == '" + createdSecondElementId + "' }.name", equalTo("phone_number"))
                .body("links", notNullValue());
    }

    // =========================================================================
    // Purpose tests
    // =========================================================================

    @Test(groups = "wso2.is", dependsOnMethods = {"testCreateElement"})
    public void testCreatePurpose() throws IOException {

        // Replace placeholder element ID with the actual created element ID.
        String body = readResource("create-purpose.json")
                .replace("\"id\": \"1\"", "\"id\": \"" + createdElementId + "\"");
        Response response = getResponseOfPost(PURPOSES_ENDPOINT, body);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .header(HttpHeaders.LOCATION, notNullValue())
                .body("id", notNullValue())
                .body("name", equalTo("User Authentication"))
                .body("description", equalTo("To authenticate users and manage their identity"))
                .body("type", equalTo("Core Identity"))
                .body("latestVersion.version", equalTo("1"));

        createdPurposeId = response.jsonPath().getString("id");
    }

    @Test(groups = "wso2.is", dependsOnMethods = {"testCreatePurpose"})
    public void testGetPurpose() {

        getResponseOfGet(PURPOSES_ENDPOINT + "/" + createdPurposeId)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("id", equalTo(createdPurposeId))
                .body("name", equalTo("User Authentication"))
                .body("description", equalTo("To authenticate users and manage their identity"))
                .body("type", equalTo("Core Identity"))
                .body("latestVersion.version", equalTo("1"))
                .body("elements", hasSize(1))
                .body("elements.find { it.id == '" + createdElementId + "' }", notNullValue())
                .body("elements.find { it.id == '" + createdElementId + "' }.mandatory", is(true));
    }

    @Test(groups = "wso2.is", dependsOnMethods = {"testGetPurpose"})
    public void testListPurposes() {

        getResponseOfGet(PURPOSES_ENDPOINT)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("Purposes", notNullValue())
                .body("totalResults", greaterThanOrEqualTo(1))
                .body("Purposes.find { it.id == '" + createdPurposeId + "' }.name", equalTo("User Authentication"))
                .body("Purposes.find { it.id == '" + createdPurposeId + "' }.type", equalTo("Core Identity"))
                .body("links", notNullValue());
    }

    @Test(groups = "wso2.is", dependsOnMethods = {"testGetPurpose"})
    public void testListPurposesWithTypeFilter() {

        getResponseOfGet(PURPOSES_ENDPOINT + "?filter=type eq \"Core Identity\"")
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("Purposes", notNullValue())
                .body("totalResults", greaterThanOrEqualTo(1))
                .body("Purposes.find { it.id == '" + createdPurposeId + "' }.name", equalTo("User Authentication"))
                .body("Purposes.findAll { it.type != 'Core Identity' }.size()", equalTo(0))
                .body("links", notNullValue());
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
                .replace("\"id\": \"2\"", "\"id\": \"" + createdSecondElementId + "\"");
        Response response = getResponseOfPost(
                PURPOSES_ENDPOINT + "/" + createdPurposeId + VERSIONS_ENDPOINT, body);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .header(HttpHeaders.LOCATION, notNullValue())
                .body("id", notNullValue())
                .body("version", equalTo("v2"))
                .body("description", equalTo("Updated consent elements for enhanced authentication"))
                // v2 must contain the second element (phone_number).
                .body("elements.find { it.id == '" + createdSecondElementId + "' }", notNullValue())
                // v2 must NOT contain the first element (email_address).
                .body("elements.find { it.id == '" + createdElementId + "' }", equalTo(null));

        createdVersionLabel = response.jsonPath().getString("version");
        createdVersionId = response.jsonPath().getString("id");
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
                .body("totalResults", equalTo(2))
                .body("Versions.size()", equalTo(2))
                .body("links", notNullValue());
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
                .getString("Versions.find { it.version == '1' }.id");

        // GET version "1" directly and verify it contains the original element (email_address).
        getResponseOfGet(PURPOSES_ENDPOINT + "/" + createdPurposeId + VERSIONS_ENDPOINT + "/" + v1Id)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("version", equalTo("1"))
                .body("elements.find { it.id == '" + createdElementId + "' }", notNullValue())
                .body("elements.find { it.id == '" + createdSecondElementId + "' }", equalTo(null));

        // GET version "v2" directly and verify it contains only the second element (phone_number).
        getResponseOfGet(PURPOSES_ENDPOINT + "/" + createdPurposeId + VERSIONS_ENDPOINT + "/" + createdVersionId)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("version", equalTo("v2"))
                .body("elements.find { it.id == '" + createdSecondElementId + "' }", notNullValue())
                .body("elements.find { it.id == '" + createdElementId + "' }", equalTo(null));
    }

    @Test(groups = "wso2.is", dependsOnMethods = {"testVersionContentMatchesExpectedElements"})
    public void testGetPurposeVersion() {

        getResponseOfGet(PURPOSES_ENDPOINT + "/" + createdPurposeId + VERSIONS_ENDPOINT + "/" + createdVersionId)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("id", notNullValue())
                .body("version", equalTo("v2"))
                .body("description", equalTo("Updated consent elements for enhanced authentication"))
                .body("elements", hasSize(1))
                .body("elements.find { it.id == '" + createdSecondElementId + "' }", notNullValue())
                .body("elements.find { it.id == '" + createdSecondElementId + "' }.mandatory", is(true));
    }

    /**
     * Creates a third version ("v3") and verifies the list contains exactly 3 entries.
     * No auto-snapshot should occur.
     */
    @Test(groups = "wso2.is", dependsOnMethods = {"testGetPurposeVersion"})
    public void testSubsequentVersionCreationDoesNotTriggerAutoSnapshot() throws IOException {

        // v3 uses both elements (email_address + phone_number).
        String body = readResource("create-purpose-version-second.json")
                .replace("\"id\": \"1\"", "\"id\": \"" + createdElementId + "\"")
                .replace("\"id\": \"2\"", "\"id\": \"" + createdSecondElementId + "\"");
        Response response = getResponseOfPost(
                PURPOSES_ENDPOINT + "/" + createdPurposeId + VERSIONS_ENDPOINT, body);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .header(HttpHeaders.LOCATION, notNullValue())
                .body("id", notNullValue())
                .body("version", equalTo("v3"))
                .body("description", equalTo("Third version with both elements"))
                // v3 must contain both elements.
                .body("elements", hasSize(2))
                .body("elements.find { it.id == '" + createdElementId + "' }", notNullValue())
                .body("elements.find { it.id == '" + createdElementId + "' }.mandatory", is(true))
                .body("elements.find { it.id == '" + createdSecondElementId + "' }", notNullValue())
                .body("elements.find { it.id == '" + createdSecondElementId + "' }.mandatory", is(false));

        createdSecondVersionLabel = response.jsonPath().getString("version");
        createdSecondVersionId = response.jsonPath().getString("id");

        // Verify count is exactly 3, not 4 — no extra auto-snapshot.
        getResponseOfGet(PURPOSES_ENDPOINT + "/" + createdPurposeId + VERSIONS_ENDPOINT)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("totalResults", equalTo(3))
                .body("Versions.size()", equalTo(3));
    }

    @Test(groups = "wso2.is", dependsOnMethods = {"testSubsequentVersionCreationDoesNotTriggerAutoSnapshot"})
    public void testListPurposeVersions() {

        getResponseOfGet(PURPOSES_ENDPOINT + "/" + createdPurposeId + VERSIONS_ENDPOINT)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("Versions", notNullValue())
                .body("totalResults", equalTo(3))
                .body("Versions.size()", equalTo(3))
                .body("Versions.version", hasSize(3))
                .body("Versions.find { it.version == '1' }", notNullValue())
                .body("Versions.find { it.version == 'v2' }.id", notNullValue())
                .body("Versions.find { it.version == 'v3' }.id", notNullValue())
                .body("links", notNullValue());
    }

    @Test(groups = "wso2.is", dependsOnMethods = {"testListPurposeVersions"})
    public void testSetLatestVersion() throws IOException {

        // Set v2 as the latest version (createdVersionId holds v2's UUID).
        String body = readResource("set-latest-version.json")
                .replace("VERSION_ID_PLACEHOLDER", createdVersionId);
        getResponseOfPut(PURPOSES_ENDPOINT + "/" + createdPurposeId + VERSIONS_ENDPOINT + "/latest", body)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NO_CONTENT);
    }

    @Test(groups = "wso2.is", dependsOnMethods = {"testSetLatestVersion"})
    public void testGetPurposeReflectsLatestVersionAfterSet() {

        getResponseOfGet(PURPOSES_ENDPOINT + "/" + createdPurposeId)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("latestVersion.id", equalTo(createdVersionId))
                .body("latestVersion.version", equalTo("v2"));
    }

    @Test(groups = "wso2.is", dependsOnMethods = {"testGetPurposeReflectsLatestVersionAfterSet"})
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
    public void testCreateConsent() {

        // User API does not require subjectId — the caller is always the subject.
        String body = "{\"serviceId\": \"test-integration-service\", \"language\": \"en\","
                + " \"purposes\": [{\"id\": \"" + createdPurposeId + "\","
                + " \"elements\": [{\"id\": \"" + createdElementId + "\"}]}],"
                + " \"properties\": {\"testKey\": \"testValue\"}}";
        Response response = userApiPost(getUserConsentApiBaseUrl(), body);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .body("id", notNullValue());

        createdReceiptId = response.jsonPath().getString("id");
    }

    @Test(groups = "wso2.is", dependsOnMethods = {"testCreateConsent"})
    public void testGetConsent() {

        userApiGet(getUserConsentApiBaseUrl() + "/" + createdReceiptId)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("id", equalTo(createdReceiptId))
                .body("subjectId", equalTo(CONSENT_TEST_USER_NAME))
                .body("serviceId", equalTo("test-integration-service"))
                .body("language", equalTo("en"))
                .body("state", equalTo("ACTIVE"))
                .body("purposes", hasSize(1))
                .body("purposes[0].id", equalTo(createdPurposeId))
                .body("purposes[0].versionId", equalTo(createdVersionId))
                .body("purposes[0].elements", hasSize(1))
                .body("purposes[0].elements[0].id", equalTo(createdElementId));
    }

    @Test(groups = "wso2.is", dependsOnMethods = {"testGetConsent"})
    public void testListConsents() {

        // User API returns a plain JSON array scoped to the authenticated user.
        userApiGet(getUserConsentApiBaseUrl())
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("size()", greaterThanOrEqualTo(1))
                .body("find { it.id == '" + createdReceiptId + "' }", notNullValue())
                .body("find { it.id == '" + createdReceiptId + "' }.serviceId",
                        equalTo("test-integration-service"))
                .body("find { it.id == '" + createdReceiptId + "' }.state",
                        equalTo("ACTIVE"));
    }

    @Test(groups = "wso2.is", dependsOnMethods = {"testListConsents"})
    public void testListConsentsWithPurposeIdFilter() {

        userApiGet(getUserConsentApiBaseUrl() + "?purposeId=" + createdPurposeId)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("size()", greaterThanOrEqualTo(1))
                .body("id", hasItem(createdReceiptId));
    }

    @Test(groups = "wso2.is", dependsOnMethods = {"testListConsentsWithPurposeIdFilter"})
    public void testListConsentsWithPurposeVersionIdFilter() {

        userApiGet(getUserConsentApiBaseUrl() + "?purposeVersionId=" + createdVersionId)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("size()", greaterThanOrEqualTo(1))
                .body("id", hasItem(createdReceiptId));
    }

    @Test(groups = "wso2.is", dependsOnMethods = {"testListConsents"})
    public void testListConsentsWithStateFilter() {

        userApiGet(getUserConsentApiBaseUrl() + "?state=ACTIVE")
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("size()", greaterThanOrEqualTo(1))
                .body("id", hasItem(createdReceiptId))
                .body("findAll { it.state != 'ACTIVE' }.size()", equalTo(0));
    }

    @Test(groups = "wso2.is", dependsOnMethods = {"testListConsents"})
    public void testListConsentsWithServiceIdFilter() {

        userApiGet(getUserConsentApiBaseUrl() + "?serviceId=test-integration-service")
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("size()", greaterThanOrEqualTo(1))
                .body("id", hasItem(createdReceiptId));
    }

    @Test(groups = "wso2.is", dependsOnMethods = {"testGetConsent"})
    public void testValidateActiveConsent() {

        userApiGet(getUserConsentApiBaseUrl() + "/" + createdReceiptId + "/validate")
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("state", equalTo("ACTIVE"))
                .body("expiryTime", equalTo(null));
    }

    @Test(groups = "wso2.is", dependsOnMethods = {"testValidateActiveConsent"})
    public void testValidateExpiredConsent() {

        long expiredTime = System.currentTimeMillis() - 60_000L;
        String body = "{\"serviceId\": \"test-integration-service\", \"language\": \"en\","
                + " \"expiryTime\": " + expiredTime + ","
                + " \"purposes\": [{\"id\": \"" + createdPurposeId + "\","
                + " \"elements\": [{\"id\": \"" + createdElementId + "\"}]}]}";

        Response response = userApiPost(getUserConsentApiBaseUrl(), body);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .body("id", notNullValue());

        String expiredReceiptId = response.jsonPath().getString("id");

        userApiGet(getUserConsentApiBaseUrl() + "/" + expiredReceiptId + "/validate")
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("state", equalTo("EXPIRED"));

        userApiGet(getUserConsentApiBaseUrl() + "/" + expiredReceiptId)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("state", equalTo("EXPIRED"));
    }

    @Test(groups = "wso2.is", dependsOnMethods = {
            "testListConsentsWithPurposeVersionIdFilter",
            "testListConsentsWithStateFilter",
            "testListConsentsWithServiceIdFilter",
            "testValidateActiveConsent"
    })
    public void testRevokeConsent() {

        // User API revoke returns 200.
        userApiPost(getUserConsentApiBaseUrl() + "/" + createdReceiptId + "/revoke", "")
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);
    }

    @Test(groups = "wso2.is", dependsOnMethods = {"testRevokeConsent"})
    public void testRevokeConsentIdempotent() {

        // Second revoke of an already-revoked consent must also succeed with 200.
        userApiPost(getUserConsentApiBaseUrl() + "/" + createdReceiptId + "/revoke", "")
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);
    }

    @Test(groups = "wso2.is", dependsOnMethods = {"testRevokeConsentIdempotent"})
    public void testGetRevokedConsentStillAccessible() {

        userApiGet(getUserConsentApiBaseUrl() + "/" + createdReceiptId)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("id", equalTo(createdReceiptId))
                .body("state", equalTo("REVOKED"));
    }

    @Test(groups = "wso2.is", dependsOnMethods = {"testGetRevokedConsentStillAccessible"})
    public void testValidateRevokedConsent() {

        userApiGet(getUserConsentApiBaseUrl() + "/" + createdReceiptId + "/validate")
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("state", equalTo("REVOKED"));
    }

    @Test(groups = "wso2.is", dependsOnMethods = {"testGetRevokedConsentStillAccessible"})
    public void testListRevokedConsentsWithStateFilter() {

        userApiGet(getUserConsentApiBaseUrl() + "?state=REVOKED")
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("size()", greaterThanOrEqualTo(1))
                .body("id", hasItem(createdReceiptId))
                .body("findAll { it.state != 'REVOKED' }.size()", equalTo(0));
    }

    // =========================================================================
    // Cleanup / Delete tests (ordered after all read tests)
    // =========================================================================

    @Test(groups = "wso2.is", dependsOnMethods = {
            "testValidateRevokedConsent",
            "testListRevokedConsentsWithStateFilter"
    })
    public void testDeleteConsentTestUser() throws Exception {

        scim2RestClient.deleteUser(testUserId);
        testUserId = null; // Avoid cleanup in @AfterClass since user is already deleted.
    }

    @Test(groups = "wso2.is", dependsOnMethods = {"testDeleteConsentTestUser"})
    public void testConsentsDeletedAfterUserDeletion() {

        getResponseOfGet(CONSENTS_ENDPOINT + "?subjectId=" + CONSENT_TEST_USER_NAME)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("totalResults", equalTo(0));
    }

    @Test(groups = "wso2.is", dependsOnMethods = {"testConsentsDeletedAfterUserDeletion"})
    public void testDeletePurpose() {

        getResponseOfDelete(PURPOSES_ENDPOINT + "/" + createdPurposeId)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NO_CONTENT);
        createdPurposeId = null; // Avoid cleanup in @AfterClass since purpose is already deleted.
    }

    @Test(groups = "wso2.is", dependsOnMethods = {"testDeletePurpose"})
    public void testDeleteElements() {

        getResponseOfDelete(ELEMENTS_ENDPOINT + "/" + createdElementId)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NO_CONTENT);
        createdElementId = null; // Avoid cleanup in @AfterClass since element is already deleted.
        getResponseOfDelete(ELEMENTS_ENDPOINT + "/" + createdSecondElementId)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NO_CONTENT);
        createdSecondElementId = null; // Avoid cleanup in @AfterClass since element is already deleted.
    }
}

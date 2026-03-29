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

package org.wso2.identity.integration.test.rest.api.server.consent.management.v1;

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
import java.rmi.RemoteException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;

/**
 * Test class for Consent Management API v1 success paths.
 *
 * <p>Covers:
 * <ul>
 *   <li>PII Category CRUD: create, get, list, delete</li>
 *   <li>Purpose Category CRUD: create, get, list, delete</li>
 *   <li>Purpose CRUD: create, get, list, delete</li>
 *   <li>Consent CRUD: add, get receipt, list, revoke</li>
 * </ul>
 */
public class ConsentManagementV1SuccessTest extends ConsentManagementV1TestBase {

    private static int createdPiiCategoryId;
    private static int createdSecondPiiCategoryId;
    private static int createdPurposeCategoryId;
    private static int createdPurposeId;
    private static String createdReceiptId;

    @Factory(dataProvider = "restAPIUserConfigProvider")
    public ConsentManagementV1SuccessTest(TestUserMode userMode) throws Exception {

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
    // PII Category tests
    // =========================================================================

    @Test(groups = "wso2.is")
    public void testCreatePiiCategory() throws IOException {

        String body = readResource("create-pii-category.json");
        Response response = getResponseOfPost(PII_CATEGORIES_ENDPOINT, body);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .body("piiCategoryId", notNullValue())
                .body("piiCategory", equalTo("email"))
                .body("description", equalTo("User's primary email address"))
                .body("sensitive", equalTo(false));

        createdPiiCategoryId = response.jsonPath().getInt("piiCategoryId");
    }

    @Test(groups = "wso2.is", dependsOnMethods = {"testCreatePiiCategory"})
    public void testGetPiiCategory() {

        getResponseOfGet(PII_CATEGORIES_ENDPOINT + "/" + createdPiiCategoryId)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("piiCategoryId", equalTo(createdPiiCategoryId))
                .body("piiCategory", equalTo("email"))
                .body("description", equalTo("User's primary email address"))
                .body("sensitive", equalTo(false));
    }

    @Test(groups = "wso2.is", dependsOnMethods = {"testGetPiiCategory"})
    public void testCreateSecondPiiCategory() throws IOException {

        String body = readResource("create-pii-category-second.json");
        Response response = getResponseOfPost(PII_CATEGORIES_ENDPOINT, body);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .body("piiCategoryId", notNullValue())
                .body("piiCategory", equalTo("phone"));

        createdSecondPiiCategoryId = response.jsonPath().getInt("piiCategoryId");
    }

    @Test(groups = "wso2.is", dependsOnMethods = {"testCreateSecondPiiCategory"})
    public void testListPiiCategories() {

        getResponseOfGet(PII_CATEGORIES_ENDPOINT)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("size()", greaterThanOrEqualTo(2));
    }

    // =========================================================================
    // Purpose Category tests
    // =========================================================================

    @Test(groups = "wso2.is")
    public void testCreatePurposeCategory() throws IOException {

        String body = readResource("create-purpose-category.json");
        Response response = getResponseOfPost(PURPOSE_CATEGORIES_ENDPOINT, body);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .body("purposeCategoryId", notNullValue())
                .body("purposeCategory", equalTo("Marketing"))
                .body("description", equalTo("For marketing related activities"));

        createdPurposeCategoryId = response.jsonPath().getInt("purposeCategoryId");
    }

    @Test(groups = "wso2.is", dependsOnMethods = {"testCreatePurposeCategory"})
    public void testGetPurposeCategory() {

        getResponseOfGet(PURPOSE_CATEGORIES_ENDPOINT + "/" + createdPurposeCategoryId)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("purposeCategoryId", equalTo(createdPurposeCategoryId))
                .body("purposeCategory", equalTo("Marketing"))
                .body("description", equalTo("For marketing related activities"));
    }

    @Test(groups = "wso2.is", dependsOnMethods = {"testGetPurposeCategory"})
    public void testListPurposeCategories() {

        getResponseOfGet(PURPOSE_CATEGORIES_ENDPOINT)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("size()", greaterThanOrEqualTo(1));
    }

    // =========================================================================
    // Purpose tests
    // =========================================================================

    @Test(groups = "wso2.is", dependsOnMethods = {"testCreatePiiCategory", "testCreatePurposeCategory"})
    public void testCreatePurpose() throws IOException {

        // Replace placeholder PII category ID with the actual created ID.
        String body = readResource("create-purpose.json")
                .replace("\"piiCategoryId\": 1", "\"piiCategoryId\": " + createdPiiCategoryId);
        Response response = getResponseOfPost(PURPOSES_ENDPOINT, body);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .body("purposeId", notNullValue())
                .body("purpose", equalTo("User Authentication"))
                .body("description", equalTo("To authenticate users and manage their identity"));

        createdPurposeId = response.jsonPath().getInt("purposeId");
    }

    @Test(groups = "wso2.is", dependsOnMethods = {"testCreatePurpose"})
    public void testGetPurpose() {

        getResponseOfGet(PURPOSES_ENDPOINT + "/" + createdPurposeId)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("purposeId", equalTo(createdPurposeId))
                .body("purpose", equalTo("User Authentication"))
                .body("description", equalTo("To authenticate users and manage their identity"));
    }

    @Test(groups = "wso2.is", dependsOnMethods = {"testGetPurpose"})
    public void testListPurposes() {

        getResponseOfGet(PURPOSES_ENDPOINT)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("size()", greaterThanOrEqualTo(1));
    }

    @Test(groups = "wso2.is", dependsOnMethods = {"testGetPurpose"})
    public void testListPurposesWithGroupFilter() {

        getResponseOfGet(PURPOSES_ENDPOINT + "?group=SIGNIN&groupType=SYSTEM")
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);
    }

    // =========================================================================
    // Consent tests
    // =========================================================================

    @Test(groups = "wso2.is", dependsOnMethods = {"testCreatePurpose", "testCreatePiiCategory",
            "testCreatePurposeCategory"})
    public void testAddConsent() throws IOException {

        String body = readResource("add-consent.json")
                .replace("\"purposeId\": 1", "\"purposeId\": " + createdPurposeId)
                .replace("\"purposeCategoryId\": 1", "\"purposeCategoryId\": " + createdPurposeCategoryId)
                .replace("\"piiCategoryId\": 1", "\"piiCategoryId\": " + createdPiiCategoryId);
        Response response = getResponseOfPost(CONSENTS_ENDPOINT, body);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .header(HttpHeaders.LOCATION, notNullValue())
                .body("consentReceiptID", notNullValue());

        createdReceiptId = response.jsonPath().getString("consentReceiptID");
    }

    @Test(groups = "wso2.is", dependsOnMethods = {"testAddConsent"})
    public void testGetConsentReceipt() {

        getResponseOfGet(RECEIPTS_ENDPOINT + "/" + createdReceiptId)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("consentReceiptID", equalTo(createdReceiptId))
                .body("state", equalTo("ACTIVE"))
                .body("services", notNullValue());
    }

    @Test(groups = "wso2.is", dependsOnMethods = {"testGetConsentReceipt"})
    public void testListConsents() {

        getResponseOfGet(CONSENTS_ENDPOINT)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("size()", greaterThanOrEqualTo(1));
    }

    // =========================================================================
    // Cleanup / Delete tests (ordered after all read tests)
    // =========================================================================

    @Test(groups = "wso2.is", dependsOnMethods = {"testListConsents"})
    public void testRevokeConsent() {

        getResponseOfDelete(RECEIPTS_ENDPOINT + "/" + createdReceiptId)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);
    }

    @Test(groups = "wso2.is", dependsOnMethods = {"testRevokeConsent"})
    public void testDeletePurpose() {

        getResponseOfDelete(PURPOSES_ENDPOINT + "/" + createdPurposeId)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);
    }

    @Test(groups = "wso2.is", dependsOnMethods = {"testDeletePurpose"})
    public void testDeletePurposeCategory() {

        getResponseOfDelete(PURPOSE_CATEGORIES_ENDPOINT + "/" + createdPurposeCategoryId)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);
    }

    @Test(groups = "wso2.is", dependsOnMethods = {"testDeletePurposeCategory"})
    public void testDeletePiiCategory() {

        getResponseOfDelete(PII_CATEGORIES_ENDPOINT + "/" + createdPiiCategoryId)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);
    }

    @Test(groups = "wso2.is", dependsOnMethods = {"testDeletePiiCategory"})
    public void testDeleteSecondPiiCategory() {

        getResponseOfDelete(PII_CATEGORIES_ENDPOINT + "/" + createdSecondPiiCategoryId)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);
    }
}

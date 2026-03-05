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
 * Test class for Consent Management API v1 failure/negative paths.
 *
 * <p>Covers:
 * <ul>
 *   <li>404 Not Found for non-existent resources</li>
 *   <li>400 Bad Request for invalid payloads</li>
 *   <li>409 Conflict for duplicate resource creation</li>
 * </ul>
 */
public class ConsentManagementV1FailureTest extends ConsentManagementV1TestBase {

    private static final int NON_EXISTENT_ID = 999999;
    private static final String NON_EXISTENT_RECEIPT_ID = "non-existent-receipt-id-00000000";

    @Factory(dataProvider = "restAPIUserConfigProvider")
    public ConsentManagementV1FailureTest(TestUserMode userMode) throws Exception {

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
    // PII Category failure tests
    // =========================================================================

    @Test
    public void testGetNonExistentPiiCategoryReturns404() {

        getResponseOfGet(PII_CATEGORIES_ENDPOINT + "/" + NON_EXISTENT_ID)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    public void testDeleteNonExistentPiiCategoryReturns404() {

        getResponseOfDelete(PII_CATEGORIES_ENDPOINT + "/" + NON_EXISTENT_ID)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    public void testCreatePiiCategoryWithMissingRequiredFieldReturns400() throws IOException {

        String body = readResource("create-pii-category-missing-required.json");
        getResponseOfPost(PII_CATEGORIES_ENDPOINT, body)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    public void testCreateDuplicatePiiCategoryReturns409() throws IOException {

        String body = readResource("create-pii-category-for-conflict.json");
        Response createResponse = getResponseOfPost(PII_CATEGORIES_ENDPOINT, body);
        createResponse.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED);

        int piiCategoryId = createResponse.jsonPath().getInt("piiCategoryId");

        try {
            // Attempt to create a duplicate PII category with the same name.
            getResponseOfPost(PII_CATEGORIES_ENDPOINT, body)
                    .then()
                    .log().ifValidationFails()
                    .assertThat()
                    .statusCode(HttpStatus.SC_CONFLICT);
        } finally {
            // Clean up.
            getResponseOfDelete(PII_CATEGORIES_ENDPOINT + "/" + piiCategoryId);
        }
    }

    // =========================================================================
    // Purpose Category failure tests
    // =========================================================================

    @Test
    public void testGetNonExistentPurposeCategoryReturns404() {

        getResponseOfGet(PURPOSE_CATEGORIES_ENDPOINT + "/" + NON_EXISTENT_ID)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    public void testDeleteNonExistentPurposeCategoryReturns404() {

        getResponseOfDelete(PURPOSE_CATEGORIES_ENDPOINT + "/" + NON_EXISTENT_ID)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    public void testCreatePurposeCategoryWithMissingRequiredFieldReturns400() throws IOException {

        String body = readResource("create-purpose-category-missing-required.json");
        getResponseOfPost(PURPOSE_CATEGORIES_ENDPOINT, body)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    public void testCreateDuplicatePurposeCategoryReturns409() throws IOException {

        String body = readResource("create-purpose-category-for-conflict.json");
        Response createResponse = getResponseOfPost(PURPOSE_CATEGORIES_ENDPOINT, body);
        createResponse.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED);

        int purposeCategoryId = createResponse.jsonPath().getInt("purposeCategoryId");

        try {
            // Attempt to create a duplicate purpose category with the same name.
            getResponseOfPost(PURPOSE_CATEGORIES_ENDPOINT, body)
                    .then()
                    .log().ifValidationFails()
                    .assertThat()
                    .statusCode(HttpStatus.SC_CONFLICT);
        } finally {
            // Clean up.
            getResponseOfDelete(PURPOSE_CATEGORIES_ENDPOINT + "/" + purposeCategoryId);
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
                .statusCode(HttpStatus.SC_NOT_FOUND);
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
        getResponseOfPost(PURPOSES_ENDPOINT, body)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    public void testCreateDuplicatePurposeReturns409() throws IOException {

        String body = readResource("create-purpose-for-conflict.json");
        Response createResponse = getResponseOfPost(PURPOSES_ENDPOINT, body);
        createResponse.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED);

        int purposeId = createResponse.jsonPath().getInt("purposeId");

        try {
            // Attempt to create a duplicate purpose with the same name.
            getResponseOfPost(PURPOSES_ENDPOINT, body)
                    .then()
                    .log().ifValidationFails()
                    .assertThat()
                    .statusCode(HttpStatus.SC_CONFLICT);
        } finally {
            // Clean up.
            getResponseOfDelete(PURPOSES_ENDPOINT + "/" + purposeId);
        }
    }

    // =========================================================================
    // Consent failure tests
    // =========================================================================

    @Test
    public void testGetNonExistentConsentReceiptReturns404() {

        getResponseOfGet(RECEIPTS_ENDPOINT + "/" + NON_EXISTENT_RECEIPT_ID)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    public void testRevokeNonExistentConsentReturns404() {

        getResponseOfDelete(RECEIPTS_ENDPOINT + "/" + NON_EXISTENT_RECEIPT_ID)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    public void testAddConsentWithMissingRequiredServiceFieldReturns400() throws IOException {

        String body = readResource("add-consent-missing-required.json");
        getResponseOfPost(CONSENTS_ENDPOINT, body)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST);
    }
}

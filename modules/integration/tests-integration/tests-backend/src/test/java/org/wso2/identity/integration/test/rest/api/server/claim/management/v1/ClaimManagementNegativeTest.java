/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.identity.integration.test.rest.api.server.claim.management.v1;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;
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
import static org.hamcrest.core.IsNull.notNullValue;

/**
 * Test class for Claim Management REST APIs negative path.
 */
public class ClaimManagementNegativeTest extends ClaimManagementTestBase {

    private static final String testDialectId = "aHR0cDovL3VwZGF0ZWRkdW1teS5vcmcvY6xhaW0";
    private static final String testClaimId = "aHR0cDovL2ludmFsaWRkdW1teS5vcmcvY2xhaW0vZW1haWxhZGRyZXNz";

    @Factory(dataProvider = "restAPIUserConfigProvider")
    public ClaimManagementNegativeTest(TestUserMode userMode) throws Exception {

        super.init(userMode);
        this.context = isServer;
        this.authenticatingUserName = context.getContextTenant().getTenantAdmin().getUserName();
        this.authenticatingCredential = context.getContextTenant().getTenantAdmin().getPassword();
        this.tenant = context.getContextTenant().getDomain();
    }

    @BeforeClass(alwaysRun = true)
    public void init() throws RemoteException {

        super.testInit(API_VERSION, swaggerDefinition, tenant);
    }

    @AfterClass(alwaysRun = true)
    public void testConclude() {

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
    public void testGetDialectWithInvalidDialectId() {

        Response response = getResponseOfGet(CLAIM_DIALECTS_ENDPOINT_URI + "/" + testDialectId);
        validateErrorResponse(response, HttpStatus.SC_NOT_FOUND, "CMT-50016", testDialectId);
    }

    @Test
    public void testRemoveSystemDefaultDialect() {

        String dialectId = "local";
        removeDialect(dialectId);
        getResponseOfGet(CLAIM_DIALECTS_ENDPOINT_URI + "/" + dialectId)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("id", equalTo(dialectId))
                .body("dialectURI", equalTo("http://wso2.org/claims"));

        dialectId = "dXJuOmlldGY6cGFyYW1zOnNjaW06c2NoZW1hczpjb3JlOjIuMA";
        getResponseOfDelete(CLAIM_DIALECTS_ENDPOINT_URI + "/" + dialectId)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_FORBIDDEN)
                .body("code", equalTo("CMT-60008"));
    }

    @Test
    public void testAddExistingDialect() throws IOException {

        String dialectId = createDialect();

        String body = readResource("claim-management-add-dialect.json");
        Response response = getResponseOfPost(CLAIM_DIALECTS_ENDPOINT_URI, body);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CONFLICT)
                .body("code", equalTo("CMT-60002"));

        removeDialect(dialectId);
    }

    @Test
    public void testUpdateDefaultDialectURI() throws IOException {

        String dialectId = "aHR0cDovL3dzbzIub3JnL29pZGMvY2xhaW0";
        String body = readResource("claim-management-update-dialect.json");
        Response response = getResponseOfPut(CLAIM_DIALECTS_ENDPOINT_URI + "/" + dialectId, body);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_FORBIDDEN)
                .body("code", equalTo("CMT-60007"));
    }

    @Test
    public void testGetLocalClaimsWithInvalidClaimId() {

        Response response =
                getResponseOfGet(CLAIM_DIALECTS_ENDPOINT_URI + LOCAL_CLAIMS_ENDPOINT_URI + "/" + testClaimId);
        validateErrorResponse(response, HttpStatus.SC_NOT_FOUND, "CMT-50019", testClaimId);
    }

    @Test
    public void testUpdateExistingLocalClaimUri() throws IOException {

        String localClaimUri = "http://wso2.org/claims/dummyemailaddress";
        String claimId = createLocalClaim();

        String body = readResource("claim-management-update-local-claim-conflict.json");
        Response response = getResponseOfPut(CLAIM_DIALECTS_ENDPOINT_URI + LOCAL_CLAIMS_ENDPOINT_URI + "/" + claimId,
                body);
        validateErrorResponse(response, HttpStatus.SC_CONFLICT, "CMT-50021", localClaimUri);

        removeLocalClaim(claimId);
    }

    @Test
    public void testChangeSharedProfileValueResolvingMethodOfSystemClaim() throws IOException {

        String firstNameClaimURI = "http://wso2.org/claims/givenname";
        String firstNameClaimId = "aHR0cDovL3dzbzIub3JnL2NsYWltcy9naXZlbm5hbWU";
        String body = readResource("claim-management-update-sharedProfileValueResolvingMethod-of-system-claim.json");
        Response response =
                getResponseOfPut(CLAIM_DIALECTS_ENDPOINT_URI + LOCAL_CLAIMS_ENDPOINT_URI + "/" + firstNameClaimId,
                        body);
        validateErrorResponse(response, HttpStatus.SC_BAD_REQUEST, "CMT-60013", firstNameClaimURI);
    }

    @Test
    public void testAddLocalClaimWithInvalidUsertore() throws IOException {

        String userstore = "DUMMY";
        String body = readResource("claim-management-add-local-claim-invalid-userstore.json");
        Response response = getResponseOfPost(CLAIM_DIALECTS_ENDPOINT_URI + LOCAL_CLAIMS_ENDPOINT_URI, body);
        validateErrorResponse(response, HttpStatus.SC_BAD_REQUEST, "CMT-50026", userstore);
    }

    @Test
    public void testRemoveLocalClaimWithExternalClaimAssociation() throws IOException {

        String localClaimId = createLocalClaim();
        String dialectId = createDialect();
        String claimId = createExternalClaimMappedToCustomLocalClaim(dialectId);

        Response response = getResponseOfDelete(CLAIM_DIALECTS_ENDPOINT_URI + LOCAL_CLAIMS_ENDPOINT_URI + "/"
                + localClaimId);
        validateErrorResponse(response, HttpStatus.SC_BAD_REQUEST, "CMT-50031");

        removeExternalClaim(dialectId, claimId);
        removeDialect(dialectId);
        removeLocalClaim(localClaimId);
    }

    @Test
    public void testRemoveDefaultLocalClaim() throws IOException {

        String dialectId = "dXJuOmlldGY6cGFyYW1zOnNjaW06c2NoZW1hczpjb3JlOjIuMA";
        String claimId = "dXJuOmlldGY6cGFyYW1zOnNjaW06c2NoZW1hczpjb3JlOjIuMDptZXRhLnJlc291cmNlVHlwZQ==";

        String body = readResource("claim-management-update-default-external-claim.json");
        Response response = getResponseOfPut(CLAIM_DIALECTS_ENDPOINT_URI + "/" + dialectId + CLAIMS_ENDPOINT_URI +
                "/" + claimId, body);
        response.then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);

        String defaultLocalClaimId = "aHR0cDovL3dzbzIub3JnL2NsYWltcy9yZXNvdXJjZVR5cGU=";

        response = getResponseOfDelete(CLAIM_DIALECTS_ENDPOINT_URI + LOCAL_CLAIMS_ENDPOINT_URI +
                "/" + defaultLocalClaimId);
        validateErrorResponse(response, HttpStatus.SC_FORBIDDEN, "CMT-60006");
    }

    @Test
    public void testAddLocalClaimWithExistingURI() throws IOException {

        String body = readResource("claim-management-add-local-claim-with-existing-uri.json");
        Response response = getResponseOfPost(CLAIM_DIALECTS_ENDPOINT_URI + LOCAL_CLAIMS_ENDPOINT_URI, body);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CONFLICT);
    }

    @Test
    public void testGetClaimsWithInvalidDialectId() {

        Response response = getResponseOfGet(CLAIM_DIALECTS_ENDPOINT_URI + "/" + testDialectId + CLAIMS_ENDPOINT_URI);
        validateErrorResponse(response, HttpStatus.SC_NOT_FOUND, "CMT-50016", testDialectId);
    }

    @Test
    public void testGetExternalClaimsWhenEmpty() throws IOException {

        String dialectId = createDialect();

        Response response = getResponseOfGet(CLAIM_DIALECTS_ENDPOINT_URI + "/" + dialectId + CLAIMS_ENDPOINT_URI);

        response.then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("isEmpty()", Matchers.is(true));

        removeDialect(dialectId);
    }

    @Test
    public void testGetExternalClaimsWithInvalidClaimId() throws IOException {

        String dialectId = createDialect();
        String claimId = createExternalClaimMappedToDefaultLocalClaim(dialectId);

        Response response = getResponseOfGet(CLAIM_DIALECTS_ENDPOINT_URI + "/" + dialectId + CLAIMS_ENDPOINT_URI +
                "/" + testClaimId);
        validateErrorResponse(response, HttpStatus.SC_NOT_FOUND, "CMT-50018", testClaimId, dialectId);

        removeExternalClaim(dialectId, claimId);
        removeDialect(dialectId);
    }

    @Test
    public void testUpdateExistingExternalClaimUri() throws IOException {

        String externalClaimUri = "http://updateddummy.org/claim/emailaddress";
        String dialectId = createDialect();
        String claimId = createExternalClaimMappedToDefaultLocalClaim(dialectId);

        String body = readResource("claim-management-update-external-claim-conflict.json");
        Response response = getResponseOfPut(CLAIM_DIALECTS_ENDPOINT_URI + "/" + dialectId + CLAIMS_ENDPOINT_URI +
                "/" + claimId, body);
        validateErrorResponse(response, HttpStatus.SC_CONFLICT, "CMT-50020", externalClaimUri, dialectId);

        removeExternalClaim(dialectId, claimId);
        removeDialect(dialectId);
    }

    @Test
    public void testAddExternalClaimWithInvalidDialect() throws IOException {

        String body = readResource("claim-management-add-external-claim.json");
        Response response = getResponseOfPost(CLAIM_DIALECTS_ENDPOINT_URI + "/" + testDialectId + CLAIMS_ENDPOINT_URI
                , body);
        validateErrorResponse(response, HttpStatus.SC_NOT_FOUND, "CMT-50027", testDialectId);
    }

    @Test
    public void testAddExternalClaimWithInvalidMappedClaim() throws IOException {

        String dialectId = createDialect();

        String body = readResource("claim-management-add-external-claim-invalid-mapped-claim.json");
        Response response = getResponseOfPost(CLAIM_DIALECTS_ENDPOINT_URI + "/" + dialectId + CLAIMS_ENDPOINT_URI,
                body);
        validateErrorResponse(response, HttpStatus.SC_BAD_REQUEST, "CMT-50036");

        removeDialect(dialectId);
    }

    @Test
    public void testAddExternalClaimWithExistingClaimURI() throws IOException {

        String dialectId = createDialect();
        String claimId = createExternalClaimMappedToDefaultLocalClaim(dialectId);

        String body = readResource("claim-management-add-external-claim.json");
        Response response = getResponseOfPost(CLAIM_DIALECTS_ENDPOINT_URI + "/" + dialectId +
                CLAIMS_ENDPOINT_URI, body);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CONFLICT)
                .body("code", equalTo("CMT-50038"));

        removeExternalClaim(dialectId, claimId);
        removeDialect(dialectId);
    }

    @Test
    public void testAddExternalClaimWithAlreadyMappedLocalClaim() throws IOException {

        String dialectId = createDialect();
        String claimId = createExternalClaimMappedToDefaultLocalClaim(dialectId);

        String body = readResource("claim-management-add-external-claim-already-mapped.json");
        Response response = getResponseOfPost(CLAIM_DIALECTS_ENDPOINT_URI + "/" + dialectId +
                CLAIMS_ENDPOINT_URI, body);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body("code", equalTo("CMT-60004"));

        removeExternalClaim(dialectId, claimId);
        removeDialect(dialectId);
    }

    @Test
    public void testPaginationLimitNotImplemented() {

        Response response = getResponseOfGet(CLAIM_DIALECTS_ENDPOINT_URI + LOCAL_CLAIMS_ENDPOINT_URI + "?limit=0");
        validateErrorResponse(response, HttpStatus.SC_NOT_IMPLEMENTED, "CMT-50022");
    }

    @Test
    public void testPaginationOffsetNotImplemented() {

        Response response = getResponseOfGet(CLAIM_DIALECTS_ENDPOINT_URI + LOCAL_CLAIMS_ENDPOINT_URI + "?offset=0");
        validateErrorResponse(response, HttpStatus.SC_NOT_IMPLEMENTED, "CMT-50022");
    }

    @Test
    public void testFilteringNotImplemented() {

        Response response = getResponseOfGet(CLAIM_DIALECTS_ENDPOINT_URI + LOCAL_CLAIMS_ENDPOINT_URI + "?filter" +
                "='supportedByDefault=true'");
        validateErrorResponse(response, HttpStatus.SC_NOT_IMPLEMENTED, "CMT-50023");
    }

    @Test
    public void testSortingNotImplemented() {

        Response response = getResponseOfGet(CLAIM_DIALECTS_ENDPOINT_URI + LOCAL_CLAIMS_ENDPOINT_URI + "?sort" +
                "='claimURI=asc'");
        validateErrorResponse(response, HttpStatus.SC_NOT_IMPLEMENTED, "CMT-50024");
    }

    @Test
    public void testAttributeFilteringNotImplemented() {

        Response response = getResponseOfGet(CLAIM_DIALECTS_ENDPOINT_URI + LOCAL_CLAIMS_ENDPOINT_URI + "?attributes" +
                "='claimURI,description'");
        validateErrorResponse(response, HttpStatus.SC_NOT_IMPLEMENTED, "CMT-50025");
    }

    private String createDialect() throws IOException {

        String body = readResource("claim-management-add-dialect.json");
        Response response = getResponseOfPost(CLAIM_DIALECTS_ENDPOINT_URI, body);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .header(HttpHeaders.LOCATION, notNullValue());

        String location = response.getHeader(HttpHeaders.LOCATION);
        return location.substring(location.lastIndexOf("/") + 1);
    }

    private void removeDialect(String dialectId) {

        getResponseOfDelete(CLAIM_DIALECTS_ENDPOINT_URI + "/" + dialectId)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NO_CONTENT);
    }

    private String createExternalClaimMappedToDefaultLocalClaim(String dialectId) throws IOException {

        String body = readResource("claim-management-add-external-claim.json");
        Response response = getResponseOfPost(CLAIM_DIALECTS_ENDPOINT_URI + "/" + dialectId +
                CLAIMS_ENDPOINT_URI, body);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .header(HttpHeaders.LOCATION, notNullValue());

        String location = response.getHeader(HttpHeaders.LOCATION);
        return location.substring(location.lastIndexOf("/") + 1);
    }

    private String createExternalClaimMappedToCustomLocalClaim(String dialectId) throws IOException {

        String body = readResource("claim-management-add-external-claim-mapped-to-custom-local-claim.json");
        Response response = getResponseOfPost(CLAIM_DIALECTS_ENDPOINT_URI + "/" + dialectId +
                CLAIMS_ENDPOINT_URI, body);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .header(HttpHeaders.LOCATION, notNullValue());

        String location = response.getHeader(HttpHeaders.LOCATION);
        return location.substring(location.lastIndexOf("/") + 1);
    }

    private void removeExternalClaim(String dialectId, String externalClaimId) {

        getResponseOfDelete(CLAIM_DIALECTS_ENDPOINT_URI + "/" + dialectId + CLAIMS_ENDPOINT_URI + "/" +
                externalClaimId)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NO_CONTENT);
    }

    private String createLocalClaim() throws IOException {

        String body = readResource("claim-management-add-local-claim.json");
        Response response = getResponseOfPost(CLAIM_DIALECTS_ENDPOINT_URI + LOCAL_CLAIMS_ENDPOINT_URI, body);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .header(HttpHeaders.LOCATION, notNullValue());

        String location = response.getHeader(HttpHeaders.LOCATION);
        return location.substring(location.lastIndexOf("/") + 1);
    }

    private void removeLocalClaim(String localClaimId) {

        getResponseOfDelete(CLAIM_DIALECTS_ENDPOINT_URI + LOCAL_CLAIMS_ENDPOINT_URI + "/" + localClaimId)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NO_CONTENT);
    }
}

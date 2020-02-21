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
import org.apache.axis2.AxisFault;
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

import static org.hamcrest.core.IsNull.notNullValue;

/**
 * Test class for Claim Management REST APIs negative path.
 */
public class ClaimManagementNegativeTest extends ClaimManagementTestBase {

    private static String testDialectId = "aHR0cDovL3VwZGF0ZWRkdW1teS5vcmcvY6xhaW0";
    private static String testClaimId = "aHR0cDovL2ludmFsaWRkdW1teS5vcmcvY2xhaW0vZW1haWxhZGRyZXNz";

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
    public void testGetClaimsWithInvalidDialectId() {

        Response response = getResponseOfGet(CLAIM_DIALECTS_ENDPOINT_URI + "/" + testDialectId + CLAIMS_ENDPOINT_URI);
        validateErrorResponse(response, HttpStatus.SC_NOT_FOUND, "CMT-50017", testDialectId);
    }

    @Test
    public void testGetExternalClaimsWhenEmpty() throws IOException {

        String dialectId = createDialect();

        Response response = getResponseOfGet(CLAIM_DIALECTS_ENDPOINT_URI + "/" + dialectId + CLAIMS_ENDPOINT_URI);
        validateErrorResponse(response, HttpStatus.SC_NOT_FOUND, "CMT-50017", dialectId);

        removeDialect(dialectId);
    }

    @Test
    public void testGetExternalClaimsWithInvalidClaimId() throws IOException {

        String dialectId = createDialect();
        String claimId = createExternalClaim(dialectId);

        Response response = getResponseOfGet(CLAIM_DIALECTS_ENDPOINT_URI + "/" + dialectId + CLAIMS_ENDPOINT_URI +
                "/" + testClaimId);
        validateErrorResponse(response, HttpStatus.SC_NOT_FOUND, "CMT-50018", testClaimId, dialectId);

        removeExternalClaim(dialectId, claimId);
        removeDialect(dialectId);
    }

    @Test
    public void testGetLocalClaimsWithInvalidClaimId() {

        Response response =
                getResponseOfGet(CLAIM_DIALECTS_ENDPOINT_URI + LOCAL_CLAIMS_ENDPOINT_URI + "/" + testClaimId);
        validateErrorResponse(response, HttpStatus.SC_NOT_FOUND, "CMT-50019", testClaimId);
    }

    @Test
    public void testUpdateExistingExternalClaimUri() throws IOException {

        String externalClaimUri = "http://updateddummy.org/claim/emailaddress";
        String dialectId = createDialect();
        String claimId = createExternalClaim(dialectId);

        String body = readResource("claim-management-update-external-claim-conflict.json");
        Response response = getResponseOfPut(CLAIM_DIALECTS_ENDPOINT_URI + "/" + dialectId + CLAIMS_ENDPOINT_URI +
                "/" + claimId, body);
        validateErrorResponse(response, HttpStatus.SC_CONFLICT, "CMT-50020", externalClaimUri, dialectId);

        removeExternalClaim(dialectId, claimId);
        removeDialect(dialectId);
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

    @Test
    public void testAddLocalClaimWithInvalidUsertore() throws IOException {

        String userstore = "DUMMY";
        String body = readResource("claim-management-add-local-claim-invalid-userstore.json");
        Response response = getResponseOfPost(CLAIM_DIALECTS_ENDPOINT_URI + LOCAL_CLAIMS_ENDPOINT_URI, body);
        validateErrorResponse(response, HttpStatus.SC_BAD_REQUEST, "CMT-50026", userstore);
    }

    @Test
    public void testAddExternalClaimWithInvalidDialect() throws IOException {

        String body = readResource("claim-management-add-external-claim.json");
        Response response = getResponseOfPost(CLAIM_DIALECTS_ENDPOINT_URI + "/" + testDialectId + CLAIMS_ENDPOINT_URI
                , body);
        validateErrorResponse(response, HttpStatus.SC_NOT_FOUND, "CMT-50027", testDialectId);
    }

    @Test
    public void testRemoveLocalClaimWithExternalClaimAssociation() throws IOException {

        String mappedLocalClaimId = "aHR0cDovL3dzbzIub3JnL2NsYWltcy9lbWFpbGFkZHJlc3M";
        String dialectId = createDialect();
        String claimId = createExternalClaim(dialectId);

        Response response =
                getResponseOfDelete(CLAIM_DIALECTS_ENDPOINT_URI + LOCAL_CLAIMS_ENDPOINT_URI + "/" + mappedLocalClaimId);
        validateErrorResponse(response, HttpStatus.SC_BAD_REQUEST, "CMT-50031");

        removeExternalClaim(dialectId, claimId);
        removeDialect(dialectId);
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

    private String createExternalClaim(String dialectId) throws IOException {

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

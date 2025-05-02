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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.IsNull.notNullValue;

/**
 * Test class for Claim Management REST APIs success path.
 */
public class ClaimManagementSuccessTest extends ClaimManagementTestBase {

    private static String localClaimId;
    private static String externalClaimId;
    private static String dialectId;

    @Factory(dataProvider = "restAPIUserConfigProvider")
    public ClaimManagementSuccessTest(TestUserMode userMode) throws Exception {

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
    public void testAddLocalClaim() throws IOException {

        String body = readResource("claim-management-add-local-claim.json");
        Response response = getResponseOfPost(CLAIM_DIALECTS_ENDPOINT_URI + LOCAL_CLAIMS_ENDPOINT_URI, body);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .header(HttpHeaders.LOCATION, notNullValue());

        String location = response.getHeader(HttpHeaders.LOCATION);
        localClaimId = location.substring(location.lastIndexOf("/") + 1);
    }

    @Test(dependsOnMethods = {"testAddLocalClaim"})
    public void testGetLocalClaim() {

        getResponseOfGet(CLAIM_DIALECTS_ENDPOINT_URI + LOCAL_CLAIMS_ENDPOINT_URI + "/" + localClaimId)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("id", equalTo(localClaimId))
                .body("claimURI", equalTo("http://wso2.org/claims/dummyemailaddress"))
                .body("dialectURI", equalTo("http://wso2.org/claims"))
                .body("description", equalTo("Dummy Email Address"))
                .body("displayOrder", equalTo(6))
                .body("displayName", equalTo("Dummy Email"))
                .body("readOnly", equalTo(false))
                .body("regEx", equalTo("^\\S+@\\S+$"))
                .body("required", equalTo(true))
                .body("supportedByDefault", equalTo(true))
                .body("sharedProfileValueResolvingMethod", equalTo("FromFirstFoundInHierarchy"))
                .body("attributeMapping", notNullValue())
                .body("attributeMapping[0].mappedAttribute", equalTo("dummymail"))
                .body("attributeMapping[0].userstore", equalTo("PRIMARY"))
                .body("properties", notNullValue())
                .body("properties[0].key", equalTo("isVerifiable"))
                .body("properties[0].value", equalTo("true"));
    }

    @Test(dependsOnMethods = {"testGetLocalClaim"})
    public void testUpdateLocalClaim() throws IOException {

        String body = readResource("claim-management-update-local-claim.json");
        getResponseOfPut(CLAIM_DIALECTS_ENDPOINT_URI + LOCAL_CLAIMS_ENDPOINT_URI + "/" + localClaimId, body)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);
    }

    @Test(dependsOnMethods = {"testUpdateLocalClaim"})
    public void testGetLocalClaims() {

        String baseIdentifier = "find{ it.id == '" + localClaimId + "' }.";
        getResponseOfGet(CLAIM_DIALECTS_ENDPOINT_URI + LOCAL_CLAIMS_ENDPOINT_URI)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body(baseIdentifier + "claimURI", equalTo("http://wso2.org/claims/dummyemailaddress"))
                .body(baseIdentifier + "dialectURI", equalTo("http://wso2.org/claims"))
                .body(baseIdentifier + "description", equalTo("Dummy Email Address update"))
                .body(baseIdentifier + "displayOrder", equalTo(7))
                .body(baseIdentifier + "displayName", equalTo("Dummy Email update"))
                .body(baseIdentifier + "readOnly", equalTo(false))
                .body(baseIdentifier + "regEx", equalTo(""))
                .body(baseIdentifier + "required", equalTo(true))
                .body(baseIdentifier + "supportedByDefault", equalTo(true))
                .body(baseIdentifier + "sharedProfileValueResolvingMethod", equalTo("FromSharedProfile"))
                .body(baseIdentifier + "attributeMapping", notNullValue())
                .body(baseIdentifier + "attributeMapping[0].mappedAttribute", equalTo("dummymail"))
                .body(baseIdentifier + "attributeMapping[0].userstore", equalTo("PRIMARY"))
                .body(baseIdentifier + "properties", notNullValue())
                .body(baseIdentifier + "properties[0].key", equalTo("isVerifiable"))
                .body(baseIdentifier + "properties[0].value", equalTo("true"));
    }

    @Test(dependsOnMethods = {"testGetLocalClaims"})
    public void testAddClaimDialect() throws IOException {

        String body = readResource("claim-management-add-dialect.json");
        Response response = getResponseOfPost(CLAIM_DIALECTS_ENDPOINT_URI, body);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .header(HttpHeaders.LOCATION, notNullValue());

        String location = response.getHeader(HttpHeaders.LOCATION);
        dialectId = location.substring(location.lastIndexOf("/") + 1);
    }

    @Test(dependsOnMethods = {"testAddClaimDialect"})
    public void testGetDialect() {

        getResponseOfGet(CLAIM_DIALECTS_ENDPOINT_URI + "/" + dialectId)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("id", equalTo(dialectId))
                .body("dialectURI", equalTo("http://dummy.org/claim"));
    }

    @Test(dependsOnMethods = {"testGetDialect"})
    public void testUpdateDialect() throws IOException {

        String body = readResource("claim-management-update-dialect.json");
        Response response = getResponseOfPut(CLAIM_DIALECTS_ENDPOINT_URI + "/" + dialectId, body);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .header(HttpHeaders.LOCATION, notNullValue());

        String location = response.getHeader(HttpHeaders.LOCATION);
        dialectId = location.substring(location.lastIndexOf("/") + 1);
    }

    @Test(dependsOnMethods = {"testUpdateDialect"})
    public void testGetDialects() {

        String baseIdentifier = "find{ it.id == '" + dialectId + "' }.";
        getResponseOfGet(CLAIM_DIALECTS_ENDPOINT_URI)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body(baseIdentifier + "dialectURI", equalTo("http://updateddummy.org/claim"));
    }

    @Test(dependsOnMethods = {"testGetDialects"})
    public void testAddExternalClaim() throws IOException {

        String body = readResource("claim-management-add-external-claim.json");
        Response response = getResponseOfPost(CLAIM_DIALECTS_ENDPOINT_URI + "/" + dialectId +
                CLAIMS_ENDPOINT_URI, body);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .header(HttpHeaders.LOCATION, notNullValue());

        String location = response.getHeader(HttpHeaders.LOCATION);
        externalClaimId = location.substring(location.lastIndexOf("/") + 1);
    }

    @Test(dependsOnMethods = {"testAddExternalClaim"})
    public void testGetExternalClaim() {

        getResponseOfGet(CLAIM_DIALECTS_ENDPOINT_URI + "/" + dialectId + CLAIMS_ENDPOINT_URI + "/" + externalClaimId)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("id", equalTo(externalClaimId))
                .body("claimURI", equalTo("http://updateddummy.org/claim/emailaddress"))
                .body("claimDialectURI", equalTo("http://updateddummy.org/claim"))
                .body("mappedLocalClaimURI", equalTo("http://wso2.org/claims/emailaddress"));
    }

    @Test(dependsOnMethods = {"testGetExternalClaim"})
    public void testUpdateExternalClaim() throws IOException {

        String body = readResource("claim-management-update-external-claim.json");
        getResponseOfPut(CLAIM_DIALECTS_ENDPOINT_URI + "/" + dialectId + CLAIMS_ENDPOINT_URI + "/" +
                        externalClaimId, body)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);
    }

    @Test(dependsOnMethods = {"testUpdateExternalClaim"})
    public void testGetExternalClaims() {

        String baseIdentifier = "find{ it.id == '" + externalClaimId + "' }.";
        getResponseOfGet(CLAIM_DIALECTS_ENDPOINT_URI + "/" + dialectId + CLAIMS_ENDPOINT_URI)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body(baseIdentifier + "claimURI", equalTo("http://updateddummy.org/claim/emailaddress"))
                .body(baseIdentifier + "claimDialectURI", equalTo("http://updateddummy.org/claim"))
                .body(baseIdentifier + "mappedLocalClaimURI", equalTo("http://wso2.org/claims/dummyemailaddress"));
    }

    @Test(dependsOnMethods = {"testGetExternalClaims"})
    public void testDeleteExternalClaim() {

        getResponseOfDelete(CLAIM_DIALECTS_ENDPOINT_URI + "/" + dialectId + CLAIMS_ENDPOINT_URI + "/" +
                externalClaimId)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NO_CONTENT);

        getResponseOfGet(CLAIM_DIALECTS_ENDPOINT_URI + "/" + dialectId + CLAIMS_ENDPOINT_URI + "/"
                + externalClaimId)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test(dependsOnMethods = {"testDeleteExternalClaim"})
    public void testDeleteLocalClaim() {

        getResponseOfDelete(CLAIM_DIALECTS_ENDPOINT_URI + LOCAL_CLAIMS_ENDPOINT_URI + "/" + localClaimId)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NO_CONTENT);

        getResponseOfGet(CLAIM_DIALECTS_ENDPOINT_URI + LOCAL_CLAIMS_ENDPOINT_URI + "/" + localClaimId)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test(dependsOnMethods = {"testDeleteLocalClaim"})
    public void testDeleteDialect() {

        getResponseOfDelete(CLAIM_DIALECTS_ENDPOINT_URI + "/" + dialectId)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NO_CONTENT);

        getResponseOfGet(CLAIM_DIALECTS_ENDPOINT_URI + "/" + dialectId)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    public void testGetLocalClaimsWithHiddenClaimsFiltered() {

        Map<String, Object> params = new HashMap<>();
        params.put("exclude-hidden-claims", "true");

        List<String> hiddenClaims = Arrays.asList(
            "http://wso2.org/claims/identity/askPassword",
            "http://wso2.org/claims/identity/tenantAdminAskPassword",
            "http://wso2.org/claims/identity/adminForcedPasswordReset",
            "http://wso2.org/claims/identity/secretkey",
            "http://wso2.org/claims/identity/verifySecretkey",
            "http://wso2.org/claims/identity/backupCodes",
            "http://wso2.org/claims/identity/verifyEmail",
            "http://wso2.org/claims/identity/verifyMobile"
        );

        getResponseOfGet(CLAIM_DIALECTS_ENDPOINT_URI + LOCAL_CLAIMS_ENDPOINT_URI, params)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("claimURI", not(hasItems(hiddenClaims.toArray())));
    }
}

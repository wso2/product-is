/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.identity.integration.test.scim2.rest.api;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.core.Is.is;
import static org.wso2.identity.integration.test.scim2.SCIM2BaseTestCase.SCHEMAS_ENDPOINT;

public class SCIM2SchemasTest extends SCIM2BaseTest {

    private static final String CLAIM_DIALECTS_ENDPOINT_CONTEXT = "claim-dialects/";
    private static final String LOCAL_CLAIMS_ENDPOINT_CONTEXT = "local/claims/";
    private static final String API_SERVER_BASE_CONTEXT = "/api/server/v1/";
    private static final String CARBON_SUPER = "carbon.super";
    private static final String IDENTITY_EMAIL_VERIFIED_CLAIM_URI = "http://wso2.org/claims/identity/emailVerified";
    private Response response;

    @Factory(dataProvider = "restAPIUserConfigProvider")
    public SCIM2SchemasTest(TestUserMode userMode) throws Exception {

        super.init(userMode);
        this.context = isServer;
        this.authenticatingUserName = context.getContextTenant().getTenantAdmin().getUserName();
        this.authenticatingCredential = context.getContextTenant().getTenantAdmin().getPassword();
        this.tenant = context.getContextTenant().getDomain();
    }

    @DataProvider(name = "restAPIUserConfigProvider")
    public static Object[][] restAPIUserConfigProvider() {

        return new Object[][]{
                {TestUserMode.SUPER_TENANT_ADMIN},
                {TestUserMode.TENANT_ADMIN}
        };
    }

    @BeforeClass(alwaysRun = true)
    public void init() throws IOException, InterruptedException {

        super.testInit(swaggerDefinition, tenant);
    }

    @AfterClass(alwaysRun = true)
    public void finish() {

        super.conclude();
    }

    @BeforeMethod(alwaysRun = true)
    public void testInit() {

        RestAssured.basePath = basePath;
    }

    @AfterMethod(alwaysRun = true)
    public void testFinish() {

        supportEmailVerifiedClaimByDefault(false);
    }

    @Test()
    public void getSchemas() {

        supportEmailVerifiedClaimByDefault(true);
        String endpointURL = SCHEMAS_ENDPOINT;
        this.response = getResponseOfGet(endpointURL, "application/json");
        Assert.assertNotNull(response);

        this.response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);
    }

    @Test(dependsOnMethods = "getSchemas")
    public void validateUserSchemaComplexElement() {

        String baseIdentifier = "find{ it.name == 'User' }.attributes.find {it.name == 'name'}.";
        this.response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body(baseIdentifier + "type", is("COMPLEX"))
                .body(baseIdentifier + "subAttributes.size()", is(2))
                .body(baseIdentifier + "subAttributes.displayName", hasItems("Last Name", "First Name"));
    }

    @Test(dependsOnMethods = "getSchemas")
    public void validateUserSchemaElement() {

        String baseIdentifier = "find{ it.name == 'User' }.attributes.find {it.name == 'profileUrl'}.";
        this.response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body(baseIdentifier + "type", is("STRING"))
                .body(baseIdentifier + "displayName", is("URL"));

    }

    @Test(dependsOnMethods = "getSchemas")
    public void validateUserExtensionSchemaElement() {

        String baseIdentifier = "find{ it.name == 'SystemUser' }.attributes.find {it.name == 'country'}.";
        this.response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body(baseIdentifier + "type", is("STRING"))
                .body(baseIdentifier + "displayName", is("Country"));

    }

    @Test(dependsOnMethods = "getSchemas")
    public void validateUserExtensionSchemaBooleanElement() {

        String baseIdentifier = "find{ it.name == 'SystemUser' }.attributes.find {it.name == 'emailVerified'}.";
        this.response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body(baseIdentifier + "type", is("BOOLEAN"))
                .body(baseIdentifier + "displayName", is("Email Verified"));
    }

    /**
     * Change the enabled by default value of the IDENTITY_EMAIL_VERIFIED_CLAIM_URI
     *
     * @param state enabled or not state
     */
    private void supportEmailVerifiedClaimByDefault(boolean state) {

        String basePath = RestAssured.basePath;
        RestAssured.basePath = "";
        String localClaimId = Base64.getEncoder().encodeToString(IDENTITY_EMAIL_VERIFIED_CLAIM_URI.getBytes(StandardCharsets.UTF_8));
        String body = null;
        try {
            body = String.format(readResource("claim-management-update-emailverified-claim.json"), state);
        } catch (IOException e) {
            Assert.fail("Unable to read API request body.", e);
        }

        String endpointURI = getContext() + CLAIM_DIALECTS_ENDPOINT_CONTEXT +
                LOCAL_CLAIMS_ENDPOINT_CONTEXT + localClaimId;
        given().auth().preemptive().basic(authenticatingUserName, authenticatingCredential)
                .contentType(ContentType.JSON)
                .header(HttpHeaders.ACCEPT, ContentType.JSON)
                .body(body)
                .log().ifValidationFails()
                .put(endpointURI).then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);
        RestAssured.basePath = basePath;
    }

    private String getContext() {

        if (CARBON_SUPER.equals(tenant)) {
            return API_SERVER_BASE_CONTEXT;
        } else {
            return ISIntegrationTest.getTenantedRelativePath(API_SERVER_BASE_CONTEXT, tenant);
        }
    }
}

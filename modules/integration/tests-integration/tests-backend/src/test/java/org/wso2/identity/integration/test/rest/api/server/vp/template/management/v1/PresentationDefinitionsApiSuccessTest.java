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

package org.wso2.identity.integration.test.rest.api.server.vp.template.management.v1;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.identity.integration.test.oid4vp.VPTestUtils;
import org.wso2.identity.integration.test.utils.SDJwtVcTestBuilder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;

/**
 * Test class for Presentation Definition REST API success paths.
 */
public class PresentationDefinitionsApiSuccessTest extends PresentationDefinitionsApiTestBase {

    private String presentationDefinitionId;
    private String secondDefinitionId;
    private final String credentialIdentifier = "UpdatedCredential";

    @Factory(dataProvider = "restAPIUserConfigProvider")
    public PresentationDefinitionsApiSuccessTest(TestUserMode userMode) throws Exception {

        super.init(userMode);
        this.context = isServer;
        this.authenticatingUserName = context.getContextTenant().getTenantAdmin().getUserName();
        this.authenticatingCredential = context.getContextTenant().getTenantAdmin().getPassword();
        this.tenant = context.getContextTenant().getDomain();
    }

    @BeforeClass(alwaysRun = true)
    public void init() throws IOException {

        super.testInit(API_VERSION, swaggerDefinition, tenant);
    }

    @AfterClass(alwaysRun = true)
    public void testConclude() {

        RestAssured.basePath = basePath;
        if (presentationDefinitionId != null) {
            getResponseOfDelete(PRESENTATION_DEFINITIONS_API_BASE_PATH + PATH_SEPARATOR + presentationDefinitionId);
        }
        if (secondDefinitionId != null) {
            getResponseOfDelete(PRESENTATION_DEFINITIONS_API_BASE_PATH + PATH_SEPARATOR + secondDefinitionId);
        }
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
    public void testCreatePresentationDefinition() {

        String body = "{\"identifier\":\"test-person-verification\",\"displayName\":\"Test Person Verification\","
                + "\"description\":\"A test presentation definition for person verification\","
                + "\"credentials\":[{\"id\":\"PersonCredential\",\"type\":\"PersonCredential\","
                + "\"format\":\"dc+sd-jwt\"}]}";
        Response response = getResponseOfPost(PRESENTATION_DEFINITIONS_API_BASE_PATH, body);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .body("id", notNullValue())
                .body("identifier", equalTo("test-person-verification"))
                .body("displayName", equalTo("Test Person Verification"))
                .body("credentials[0].id", equalTo("PersonCredential"));

        presentationDefinitionId = response.jsonPath().getString("id");
    }

    @Test(dependsOnMethods = {"testCreatePresentationDefinition"})
    public void testGetPresentationDefinition() {

        Response response = getResponseOfGet(PRESENTATION_DEFINITIONS_API_BASE_PATH + PATH_SEPARATOR
                + presentationDefinitionId);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("id", equalTo(presentationDefinitionId))
                .body("identifier", equalTo("test-person-verification"))
                .body("displayName", equalTo("Test Person Verification"))
                .body("credentials[0].id", equalTo("PersonCredential"));
    }

    @Test(dependsOnMethods = {"testGetPresentationDefinition"})
    public void testListPresentationDefinitions() {

        Response response = getResponseOfGet(PRESENTATION_DEFINITIONS_API_BASE_PATH);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("presentationDefinitions", notNullValue())
                .body("presentationDefinitions.size()", greaterThanOrEqualTo(1));
    }

    @Test(groups = "wso2.is",
            description = "Presentation definitions are strictly tenant-isolated: a super-tenant definition must not "
                    + "be listed by a secondary tenant, and a secondary tenant admin must not be able to access "
                    + "the super-tenant presentation-definitions endpoint.",
            dependsOnMethods = {"testCreatePresentationDefinition"})
    public void testPresentationDefinitionTenantIsolation() throws Exception {

        if (!MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenant)) {

            String superTenantUrl = RestAssured.baseURI.replaceAll("/$", "")
                    + "/api/server/v1/presentation-definitions";
            Response crossTenantResponse = RestAssured.given()
                    .auth().preemptive().basic(authenticatingUserName, authenticatingCredential)
                    .when()
                    .get(superTenantUrl);

            int crossTenantStatus = crossTenantResponse.getStatusCode();
            Assert.assertTrue(
                    crossTenantStatus == HttpStatus.SC_UNAUTHORIZED
                            || crossTenantStatus == HttpStatus.SC_FORBIDDEN,
                    "Tenant admin must be denied access to the super-tenant presentation-definitions endpoint "
                            + "(expected 401 or 403, but got " + crossTenantStatus + ").");
            return;
        }

        Assert.assertNotNull(presentationDefinitionId,
                "Presentation definition must exist before testing tenant isolation.");

        AutomationContext tenantContext = new AutomationContext("IDENTITY", TestUserMode.TENANT_ADMIN);
        String tenantAdminUser = tenantContext.getContextTenant().getTenantAdmin().getUserName();
        String tenantAdminPass = tenantContext.getContextTenant().getTenantAdmin().getPassword();
        String tenantDomain = tenantContext.getContextTenant().getDomain();

        String baseUri = RestAssured.baseURI.replaceAll("/$", "");
        String tenantAdminUrl = baseUri + "/t/" + tenantDomain + "/api/server/v1/presentation-definitions";
        Response response = RestAssured.given()
                .auth().preemptive().basic(tenantAdminUser, tenantAdminPass)
                .when()
                .get(tenantAdminUrl);

        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);

        List<String> identifiers = response.jsonPath().getList("presentationDefinitions.identifier");
        Assert.assertFalse(
                identifiers != null && identifiers.contains("test-person-verification"),
                "Super-tenant definition 'test-person-verification' must not be visible in the "
                        + tenantDomain + " tenant.");

        String directGetUrl = baseUri + "/t/" + tenantDomain + "/api/server/v1/presentation-definitions/"
                + presentationDefinitionId;
        Response directResponse = RestAssured.given()
                .auth().preemptive().basic(tenantAdminUser, tenantAdminPass)
                .when()
                .get(directGetUrl);
        Assert.assertEquals(directResponse.getStatusCode(), HttpStatus.SC_NOT_FOUND,
                "Tenant admin must receive 404 when fetching a super-tenant definition by ID directly. "
                        + "Got status " + directResponse.getStatusCode() + ".");
    }

    @Test(dependsOnMethods = {"testListPresentationDefinitions"})
    public void testPatchDisplayName() {

        String body = "{\"displayName\":\"Updated Display Name\"}";
        Response response = getResponseOfPatch(PRESENTATION_DEFINITIONS_API_BASE_PATH + PATH_SEPARATOR
                + presentationDefinitionId, body);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("displayName", equalTo("Updated Display Name"));
    }

    @Test(dependsOnMethods = {"testPatchDisplayName"})
    public void testPatchDescription() {

        String body = "{\"description\":\"Updated description\"}";
        Response response = getResponseOfPatch(PRESENTATION_DEFINITIONS_API_BASE_PATH + PATH_SEPARATOR
                + presentationDefinitionId, body);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("description", equalTo("Updated description"));
    }

    @Test(dependsOnMethods = {"testPatchDescription"})
    public void testPatchCredentials() {

        String body = "{\"credentials\":[{\"id\":\"UpdatedCredential\",\"type\":\"UpdatedCredential\","
                + "\"format\":\"dc+sd-jwt\"}]}";
        Response response = getResponseOfPatch(PRESENTATION_DEFINITIONS_API_BASE_PATH + PATH_SEPARATOR
                + presentationDefinitionId, body);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("credentials", notNullValue())
                .body("credentials[0].id", equalTo("UpdatedCredential"))
                .body("credentials[0].type", equalTo("UpdatedCredential"));
    }

    @Test(dependsOnMethods = {"testPatchCredentials"})
    public void testGetConnectedIdpsEmpty() {

        Response response = getResponseOfGet(PRESENTATION_DEFINITIONS_API_BASE_PATH + PATH_SEPARATOR
                + presentationDefinitionId + PATH_SEPARATOR + CONNECTED_IDPS_PATH);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("connectedIdps", emptyIterable());
    }

    @Test(dependsOnMethods = {"testGetConnectedIdpsEmpty"})
    public void testUpdateIssuerConfigsJwksUri() {

        String body = "{\"issuerConfigs\":[{\"issuerUrl\":\"https://test-issuer.example.com\","
                + "\"keySourceType\":\"jwks_uri\","
                + "\"keySource\":\"https://test-issuer.example.com/.well-known/jwks.json\"}]}";
        Response response = getResponseOfPut(PRESENTATION_DEFINITIONS_API_BASE_PATH + PATH_SEPARATOR
                + presentationDefinitionId + PATH_SEPARATOR + CREDENTIALS_PATH + PATH_SEPARATOR
                + credentialIdentifier + PATH_SEPARATOR + ISSUER_CONFIGS_PATH, body);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("issuerConfigs[0].keySourceType", equalTo("jwks_uri"))
                .body("issuerConfigs[0].issuerUrl", equalTo("https://test-issuer.example.com"));
    }

    @Test(dependsOnMethods = {"testUpdateIssuerConfigsJwksUri"})
    public void testUpdateIssuerConfigsPem() throws Exception {

        com.nimbusds.jose.jwk.ECKey issuerKey = SDJwtVcTestBuilder.generateSigningKey();
        String pemCert = VPTestUtils.buildSelfSignedPemCertificate(issuerKey);
        String base64Pem = Base64.getEncoder().encodeToString(pemCert.getBytes(StandardCharsets.UTF_8));
        String body = "{\"issuerConfigs\":[{\"issuerUrl\":\"https://pem-issuer.example.com\","
                + "\"keySourceType\":\"pem\","
                + "\"keySource\":\"" + base64Pem + "\"}]}";

        Response response = getResponseOfPut(PRESENTATION_DEFINITIONS_API_BASE_PATH + PATH_SEPARATOR
                + presentationDefinitionId + PATH_SEPARATOR + CREDENTIALS_PATH + PATH_SEPARATOR
                + credentialIdentifier + PATH_SEPARATOR + ISSUER_CONFIGS_PATH, body);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("issuerConfigs[0].keySourceType", equalTo("pem"))
                .body("issuerConfigs[0].issuerUrl", equalTo("https://pem-issuer.example.com"));
    }

    @Test(dependsOnMethods = {"testUpdateIssuerConfigsPem"})
    public void testUpdateIssuerConfigsX5c() throws Exception {

        com.nimbusds.jose.jwk.ECKey issuerKey = SDJwtVcTestBuilder.generateSigningKey();
        byte[] derBytes = VPTestUtils.buildSelfSignedDerCertificate(issuerKey);
        String base64Der = Base64.getEncoder().encodeToString(derBytes);
        String body = "{\"issuerConfigs\":[{\"issuerUrl\":\"https://x5c-issuer.example.com\","
                + "\"keySourceType\":\"x5c\","
                + "\"keySource\":\"" + base64Der + "\"}]}";

        Response response = getResponseOfPut(PRESENTATION_DEFINITIONS_API_BASE_PATH + PATH_SEPARATOR
                + presentationDefinitionId + PATH_SEPARATOR + CREDENTIALS_PATH + PATH_SEPARATOR
                + credentialIdentifier + PATH_SEPARATOR + ISSUER_CONFIGS_PATH, body);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("issuerConfigs[0].keySourceType", equalTo("x5c"))
                .body("issuerConfigs[0].issuerUrl", equalTo("https://x5c-issuer.example.com"));
    }

    @Test(dependsOnMethods = {"testUpdateIssuerConfigsX5c"})
    public void testGetIssuerConfigs() {

        Response response = getResponseOfGet(PRESENTATION_DEFINITIONS_API_BASE_PATH + PATH_SEPARATOR
                + presentationDefinitionId + PATH_SEPARATOR + CREDENTIALS_PATH + PATH_SEPARATOR
                + credentialIdentifier + PATH_SEPARATOR + ISSUER_CONFIGS_PATH);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("issuerConfigs", notNullValue())
                .body("issuerConfigs[0].issuerUrl", equalTo("https://x5c-issuer.example.com"))
                .body("issuerConfigs[0].keySourceType", equalTo("x5c"));
    }

    @Test(dependsOnMethods = {"testGetIssuerConfigs"})
    public void testReplaceIssuerConfigs() {

        String body = "{\"issuerConfigs\":[{\"issuerUrl\":\"https://replacement-issuer.example.com\","
                + "\"keySourceType\":\"jwks_uri\","
                + "\"keySource\":\"https://replacement-issuer.example.com/.well-known/jwks.json\"}]}";
        Response response = getResponseOfPut(PRESENTATION_DEFINITIONS_API_BASE_PATH + PATH_SEPARATOR
                + presentationDefinitionId + PATH_SEPARATOR + CREDENTIALS_PATH + PATH_SEPARATOR
                + credentialIdentifier + PATH_SEPARATOR + ISSUER_CONFIGS_PATH, body);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("issuerConfigs", notNullValue())
                .body("issuerConfigs[0].issuerUrl", equalTo("https://replacement-issuer.example.com"))
                .body("issuerConfigs[0].keySourceType", equalTo("jwks_uri"));

        Response getResponse = getResponseOfGet(PRESENTATION_DEFINITIONS_API_BASE_PATH + PATH_SEPARATOR
                + presentationDefinitionId + PATH_SEPARATOR + CREDENTIALS_PATH + PATH_SEPARATOR
                + credentialIdentifier + PATH_SEPARATOR + ISSUER_CONFIGS_PATH);
        getResponse.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("issuerConfigs", hasSize(1))
                .body("issuerConfigs[0].issuerUrl", equalTo("https://replacement-issuer.example.com"));
    }

    @Test
    public void testCreateSecondDefinition() {

        String body = "{\"identifier\":\"test-second-definition\",\"displayName\":\"Second Test Definition\","
                + "\"credentials\":[{\"id\":\"IDCredential\",\"type\":\"IDCredential\"}]}";
        Response response = getResponseOfPost(PRESENTATION_DEFINITIONS_API_BASE_PATH, body);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .body("id", notNullValue())
                .body("identifier", equalTo("test-second-definition"));

        secondDefinitionId = response.jsonPath().getString("id");
    }

    @Test(dependsOnMethods = {"testCreateSecondDefinition"})
    public void testListWithLimitPagination() {

        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put("limit", 1);
        Response response = getResponseOfGetWithQueryParams(PRESENTATION_DEFINITIONS_API_BASE_PATH, queryParams);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("presentationDefinitions", notNullValue())
                .body("presentationDefinitions.size()", equalTo(1))
                .body("links", notNullValue());

        List<Map<String, String>> links = response.jsonPath().getList("links");
        boolean hasNextLink = links.stream().anyMatch(link -> "next".equals(link.get("rel")));
        Assert.assertTrue(hasNextLink, "Expected a 'next' pagination link in the response.");
    }

    @Test(dependsOnMethods = {"testListWithLimitPagination"})
    public void testListWithAfterCursor() {

        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put("limit", 1);
        Response firstPageResponse = getResponseOfGetWithQueryParams(PRESENTATION_DEFINITIONS_API_BASE_PATH,
                queryParams);
        firstPageResponse.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);

        List<Map<String, String>> links = firstPageResponse.jsonPath().getList("links");
        String afterCursor = null;
        if (links != null) {
            for (Map<String, String> link : links) {
                if ("next".equals(link.get("rel"))) {
                    afterCursor = link.get("after");
                    if (afterCursor == null) {
                        String href = link.get("href");
                        if (href != null && href.contains("after=")) {
                            afterCursor = href.substring(href.indexOf("after=") + 6);
                            if (afterCursor.contains("&")) {
                                afterCursor = afterCursor.substring(0, afterCursor.indexOf("&"));
                            }
                        }
                    }
                    break;
                }
            }
        }

        Assert.assertNotNull(afterCursor, "Expected an 'after' cursor in the next pagination link.");

        Map<String, Object> secondPageParams = new HashMap<>();
        secondPageParams.put("limit", 1);
        secondPageParams.put("after", afterCursor);
        Response secondPageResponse = getResponseOfGetWithQueryParams(PRESENTATION_DEFINITIONS_API_BASE_PATH,
                secondPageParams);
        secondPageResponse.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("presentationDefinitions", notNullValue())
                .body("presentationDefinitions.size()", greaterThanOrEqualTo(1));
    }

    @Test(dependsOnMethods = {"testListWithAfterCursor"})
    public void testListWithFilter() {

        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put("filter", "identifier sw \"test\"");
        Response response = getResponseOfGetWithQueryParams(PRESENTATION_DEFINITIONS_API_BASE_PATH, queryParams);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("presentationDefinitions", notNullValue())
                .body("presentationDefinitions.size()", greaterThanOrEqualTo(1));
    }

    @Test(dependsOnMethods = {"testReplaceIssuerConfigs", "testListWithFilter"})
    public void testDeletePresentationDefinition() {

        getResponseOfDelete(PRESENTATION_DEFINITIONS_API_BASE_PATH + PATH_SEPARATOR + presentationDefinitionId)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NO_CONTENT);
    }

    @Test(dependsOnMethods = {"testDeletePresentationDefinition"})
    public void testGetAfterDelete() {

        getResponseOfGet(PRESENTATION_DEFINITIONS_API_BASE_PATH + PATH_SEPARATOR + presentationDefinitionId)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NOT_FOUND);

        presentationDefinitionId = null;
    }

    @Test(dependsOnMethods = {"testListWithFilter"})
    public void testDeleteSecondDefinition() {

        getResponseOfDelete(PRESENTATION_DEFINITIONS_API_BASE_PATH + PATH_SEPARATOR + secondDefinitionId)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NO_CONTENT);
    }

    @Test(dependsOnMethods = {"testDeleteSecondDefinition", "testGetAfterDelete"})
    public void testListWhenEmpty() {

        Response response = getResponseOfGet(PRESENTATION_DEFINITIONS_API_BASE_PATH);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);

        List<String> ids = response.jsonPath().getList("presentationDefinitions.id");
        Assert.assertFalse(ids != null && ids.contains(secondDefinitionId),
                "Second definition should not appear in the list after deletion.");

        secondDefinitionId = null;
    }
}

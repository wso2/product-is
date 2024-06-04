/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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
package org.wso2.identity.integration.test.rest.api.server.organization.management.v1;

import com.nimbusds.oauth2.sdk.AccessTokenResponse;
import com.nimbusds.oauth2.sdk.AuthorizationGrant;
import com.nimbusds.oauth2.sdk.ClientCredentialsGrant;
import com.nimbusds.oauth2.sdk.Scope;
import com.nimbusds.oauth2.sdk.TokenRequest;
import com.nimbusds.oauth2.sdk.TokenResponse;
import com.nimbusds.oauth2.sdk.auth.ClientAuthentication;
import com.nimbusds.oauth2.sdk.auth.ClientSecretBasic;
import com.nimbusds.oauth2.sdk.auth.Secret;
import com.nimbusds.oauth2.sdk.http.HTTPResponse;
import com.nimbusds.oauth2.sdk.id.ClientID;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.*;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationListItem;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.OpenIDConnectConfiguration;
import org.wso2.identity.integration.test.restclients.OAuth2RestClient;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.io.IOException;
import java.net.URI;
import java.util.Iterator;
import java.util.Optional;

import static io.restassured.RestAssured.given;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.testng.Assert.assertNotNull;

/**
 * Tests for failure cases of the Organization Management REST APIs.
 */
public class OrganizationManagementFailureTest extends OrganizationManagementBaseTest {

    private String organizationID;
    private String selfServiceAppId;
    private String selfServiceAppClientId;
    private String selfServiceAppClientSecret;
    private String m2mToken;
    private final String invalidM2MToken = "06c1f4e2-3339-44e4-a825-96585e3653b1";
    private final String invalidOrganizationID = "06c1f4e2-3339-44e4-a825-96585e3653b1";
    private HttpClient client;
    protected OAuth2RestClient restClient;

    @Factory(dataProvider = "restAPIUserConfigProvider")
    public OrganizationManagementFailureTest(TestUserMode userMode) throws Exception {

        super.init(userMode);
        this.context = isServer;
        this.authenticatingUserName = context.getContextTenant().getTenantAdmin().getUserName();
        this.authenticatingCredential = context.getContextTenant().getTenantAdmin().getPassword();
        this.tenant = context.getContextTenant().getDomain();
    }

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {

        super.testInit(API_VERSION, swaggerDefinition, tenant);
        oAuth2RestClient = new OAuth2RestClient(serverURL, tenantInfo);
        client = HttpClientBuilder.create().build();
    }

    @AfterClass(alwaysRun = true)
    public void testConclude() throws Exception {

        super.conclude();
        deleteApplication(selfServiceAppId);
        oAuth2RestClient.closeHttpClient();
    }

    @DataProvider(name = "restAPIUserConfigProvider")
    public static Object[][] restAPIUserConfigProvider() {

        return new Object[][]{
                {TestUserMode.SUPER_TENANT_ADMIN},
                {TestUserMode.TENANT_ADMIN}
        };
    }

    @Test(groups = "selfOnboardingTests")
    public void createApplicationForSelfOrganizationOnboardService() throws IOException, JSONException {

        String endpointURL = "applications";
        String body = readResource("create-organization-self-service-app-request.body.json");

        Response response = given().auth().preemptive().basic(authenticatingUserName, authenticatingCredential)
                .contentType(ContentType.JSON)
                .body(body).when().post(endpointURL);
        response.then()
                .log().ifValidationFails().assertThat().statusCode(HttpStatus.SC_CREATED);

        Optional<ApplicationListItem> b2bSelfServiceApp = oAuth2RestClient.getAllApplications().getApplications()
                .stream().filter(application -> application.getName().equals("b2b-self-service-app")).findAny();
        Assert.assertTrue(b2bSelfServiceApp.isPresent(), "B2B self service application is not created");
        selfServiceAppId = b2bSelfServiceApp.get().getId();

        JSONObject jsonObject = new JSONObject(readResource("organization-self-service-apis.json"));

        for (Iterator<String> apiNameIterator = jsonObject.keys(); apiNameIterator.hasNext(); ) {
            String apiName = apiNameIterator.next();
            Object requiredScopes = jsonObject.get(apiName);

            Response aPIResource =
                    given().auth().preemptive().basic(authenticatingUserName, authenticatingCredential).when()
                            .queryParam("filter", "identifier eq " + apiName).get("api-resources");
            aPIResource.then().log().ifValidationFails().assertThat().statusCode(HttpStatus.SC_OK);
            String apiUUID = aPIResource.getBody().jsonPath().getString("apiResources[0].id");

            JSONObject authorizedAPIRequestBody = new JSONObject();
            authorizedAPIRequestBody.put("id", apiUUID);
            authorizedAPIRequestBody.put("policyIdentifier", "RBAC");
            authorizedAPIRequestBody.put("scopes", requiredScopes);

            Response authorizedAPIResponse =
                    given().auth().preemptive().basic(authenticatingUserName, authenticatingCredential)
                            .contentType(ContentType.JSON).body(authorizedAPIRequestBody.toString()).when()
                            .post("applications/" + selfServiceAppId + "/authorized-apis");
            authorizedAPIResponse.then().log().ifValidationFails().assertThat().statusCode(HttpStatus.SC_OK);
        }
    }

    @Test(groups = "selfOnboardingTests", dependsOnMethods = "createApplicationForSelfOrganizationOnboardService")
    public void getM2MAccessToken() throws Exception {

        OpenIDConnectConfiguration openIDConnectConfiguration = oAuth2RestClient
                .getOIDCInboundDetails(selfServiceAppId);
        selfServiceAppClientId = openIDConnectConfiguration.getClientId();
        selfServiceAppClientSecret = openIDConnectConfiguration.getClientSecret();
        AuthorizationGrant clientCredentialsGrant = new ClientCredentialsGrant();
        ClientID clientID = new ClientID(selfServiceAppClientId);
        Secret clientSecret = new Secret(selfServiceAppClientSecret);
        ClientAuthentication clientAuth = new ClientSecretBasic(clientID, clientSecret);
        Scope scope = new Scope("SYSTEM");

        URI tokenEndpoint = new URI(getTenantQualifiedURL(OAuth2Constant.ACCESS_TOKEN_ENDPOINT,
                tenantInfo.getDomain()));
        TokenRequest request = new TokenRequest(tokenEndpoint, clientAuth, clientCredentialsGrant, scope);
        HTTPResponse tokenHTTPResp = request.toHTTPRequest().send();
        Assert.assertNotNull(tokenHTTPResp, "Access token http response is null.");

        TokenResponse tokenResponse = TokenResponse.parse(tokenHTTPResp);
        AccessTokenResponse accessTokenResponse = tokenResponse.toSuccessResponse();
        m2mToken = accessTokenResponse.getTokens().getAccessToken().getValue();
        Assert.assertNotNull(m2mToken, "The retrieved M2M Token is null in the token response.");

        Scope scopesInResponse = accessTokenResponse.getTokens().getAccessToken().getScope();
        Assert.assertTrue(scopesInResponse.contains("internal_organization_create"),
                "Requested scope is missing in the token response");
    }

    @Test(groups = "selfOnboardingTests", dependsOnMethods = "getM2MAccessToken")
    public void testSelfOnboardOrganization() throws IOException {

        String body = readResource("add-organization-request-body.json");
        Response response = given().auth().preemptive().oauth2(m2mToken)
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post(ORGANIZATION_MANAGEMENT_API_BASE_PATH);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .header(HttpHeaders.LOCATION, notNullValue());

        String location = response.getHeader(HttpHeaders.LOCATION);
        assertNotNull(location);
        organizationID = location.substring(location.lastIndexOf("/") + 1);
        assertNotNull(organizationID);
    }

    @Test(groups = "discoveryConfigTests", dependsOnGroups = "selfOnboardingTests")
    public void testAddDiscoveryAttributesWithoutAddingConfig() throws IOException {

        String endpointURL = ORGANIZATION_MANAGEMENT_API_BASE_PATH + ORGANIZATION_DISCOVERY_API_PATH;
        String requestBody = readResource("add-discovery-attributes-request-body.json");
        Response response = given().auth().preemptive().oauth2(invalidM2MToken)
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post(endpointURL);
        validateHttpStatusCode(response, HttpStatus.SC_UNAUTHORIZED);
    }

    @Test(groups = "discoveryConfigTests", dependsOnMethods = "testAddDiscoveryAttributesWithoutAddingConfig")
    public void testAddInvalidDiscoveryConfig() throws IOException {

        String endpointURL = ORGANIZATION_CONFIGS_API_BASE_PATH + ORGANIZATION_DISCOVERY_API_PATH;
        String invalidRequestBody = readResource("invalid-discovery-config-request-body.json");
        Response response = given().auth().preemptive().oauth2(m2mToken)
                .contentType(ContentType.JSON)
                .body(invalidRequestBody)
                .when()
                .post(endpointURL);
        validateHttpStatusCode(response, HttpStatus.SC_BAD_REQUEST);
    }

    @Test(groups = "discoveryConfigTests", dependsOnMethods = "testAddInvalidDiscoveryConfig")
    public void testAddDiscoveryConfigUnauthorized() throws IOException {

        String endpointURL = ORGANIZATION_CONFIGS_API_BASE_PATH + ORGANIZATION_DISCOVERY_API_PATH;
        String requestBody = readResource("add-discovery-config-request-body.json");
        Response response = given().auth().preemptive().oauth2(invalidM2MToken)
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post(endpointURL);
        validateHttpStatusCode(response, HttpStatus.SC_UNAUTHORIZED);
    }

    @Test(groups = "discoveryConfigTests", dependsOnMethods = "testAddDiscoveryConfigUnauthorized")
    public void testAddExistingDiscoveryConfig() throws IOException {

        String endpointURL = ORGANIZATION_CONFIGS_API_BASE_PATH + ORGANIZATION_DISCOVERY_API_PATH;
        String requestBody = readResource("add-discovery-config-request-body.json");
        Response firstResponse = given().auth().preemptive().oauth2(m2mToken)
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post(endpointURL);
        validateHttpStatusCode(firstResponse, HttpStatus.SC_CREATED);
        Response secondResponse = given().auth().preemptive().oauth2(m2mToken)
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post(endpointURL);
        validateHttpStatusCode(secondResponse, HttpStatus.SC_CONFLICT);
    }

    @Test(groups = "discoveryConfigTests", dependsOnMethods = "testAddExistingDiscoveryConfig")
    public void testGetDiscoveryConfigUnauthorized() {

        String endpointURL = ORGANIZATION_CONFIGS_API_BASE_PATH + ORGANIZATION_DISCOVERY_API_PATH;
        Response response = given().auth().preemptive().oauth2(invalidM2MToken)
                .contentType(ContentType.JSON)
                .when()
                .get(endpointURL);
        validateHttpStatusCode(response, HttpStatus.SC_UNAUTHORIZED);
    }

    @Test(groups = "discoveryTests", dependsOnGroups = "discoveryConfigTests")
    public void testAddInvalidDiscoveryAttributesToOrganization() throws IOException {

        String endpointURL = ORGANIZATION_MANAGEMENT_API_BASE_PATH + ORGANIZATION_DISCOVERY_API_PATH;
        String invalidRequestBody = readResource("add-invalid-discovery-attributes-request-body.json");
        invalidRequestBody = invalidRequestBody.replace("${organizationID}", organizationID);
        Response response = given().auth().preemptive().oauth2(m2mToken)
                .contentType(ContentType.JSON)
                .body(invalidRequestBody)
                .when()
                .post(endpointURL);
        validateHttpStatusCode(response, HttpStatus.SC_BAD_REQUEST);
    }

    @Test(groups = "discoveryTests", dependsOnMethods = "testAddInvalidDiscoveryAttributesToOrganization")
    public void testAddDiscoveryAttributesToNonExistingOrganization() throws IOException {

        String endpointURL = ORGANIZATION_MANAGEMENT_API_BASE_PATH + ORGANIZATION_DISCOVERY_API_PATH;
        String invalidRequestBody = readResource("add-discovery-attributes-request-body.json");
        invalidRequestBody = invalidRequestBody.replace(invalidOrganizationID, organizationID);
        Response response = given().auth().preemptive().oauth2(m2mToken)
                .contentType(ContentType.JSON)
                .body(invalidRequestBody)
                .when()
                .post(endpointURL);
        validateHttpStatusCode(response, HttpStatus.SC_NOT_FOUND);
    }

    @Test(groups = "discoveryTests", dependsOnMethods = "testAddDiscoveryAttributesToNonExistingOrganization")
    public void testAddDiscoveryAttributesToOrganizationUnauthorized() throws IOException {

        String endpointURL = ORGANIZATION_MANAGEMENT_API_BASE_PATH + ORGANIZATION_DISCOVERY_API_PATH;
        String requestBody = readResource("add-discovery-attributes-request-body.json");
        Response response = given().auth().preemptive().oauth2(invalidM2MToken)
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post(endpointURL);
        validateHttpStatusCode(response, HttpStatus.SC_UNAUTHORIZED);
    }

    @Test(groups = "discoveryTests", dependsOnMethods = "testAddDiscoveryAttributesToOrganizationUnauthorized")
    public void testAddExistingDiscoveryAttributesToOrganization() throws IOException {

        String endpointURL = ORGANIZATION_MANAGEMENT_API_BASE_PATH + ORGANIZATION_DISCOVERY_API_PATH;
        String requestBody = readResource("add-discovery-attributes-request-body.json");
        requestBody = requestBody.replace("${organizationID}", organizationID);
        Response firstResponse = given().auth().preemptive().oauth2(m2mToken)
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post(endpointURL);
        validateHttpStatusCode(firstResponse, HttpStatus.SC_CREATED);
        Response secondResponse = given().auth().preemptive().oauth2(m2mToken)
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post(endpointURL);
        validateHttpStatusCode(secondResponse, HttpStatus.SC_BAD_REQUEST);
    }

    @Test(groups = "discoveryTests", dependsOnMethods = "testAddExistingDiscoveryAttributesToOrganization")
    public void testGetDiscoveryAttributesToOrganizationsUnauthorized() {

        String endpointURL = ORGANIZATION_MANAGEMENT_API_BASE_PATH + ORGANIZATION_DISCOVERY_API_PATH;
        Response response = given().auth().preemptive().oauth2(invalidM2MToken)
                .contentType(ContentType.JSON)
                .when()
                .get(endpointURL);
        validateHttpStatusCode(response, HttpStatus.SC_UNAUTHORIZED);
    }

    @Test(groups = "discoveryTests", dependsOnMethods = "testGetDiscoveryAttributesToOrganizationsUnauthorized")
    public void testGetDiscoveryAttributesOfNonExistingOrganization() {

        String endpointURL = ORGANIZATION_MANAGEMENT_API_BASE_PATH + PATH_SEPARATOR + invalidOrganizationID
                + ORGANIZATION_DISCOVERY_API_PATH;
        Response response = given().auth().preemptive().oauth2(m2mToken)
                .contentType(ContentType.JSON)
                .when()
                .get(endpointURL);
        validateHttpStatusCode(response, HttpStatus.SC_NOT_FOUND);
    }

    @Test(groups = "discoveryTests", dependsOnMethods = "testGetDiscoveryAttributesOfNonExistingOrganization")
    public void testGetDiscoveryAttributesToOrganizationUnauthorized() {

        String endpointURL = ORGANIZATION_MANAGEMENT_API_BASE_PATH + PATH_SEPARATOR + organizationID
                + ORGANIZATION_DISCOVERY_API_PATH;
        Response response = given().auth().preemptive().oauth2(invalidM2MToken)
                .contentType(ContentType.JSON)
                .when()
                .get(endpointURL);
        validateHttpStatusCode(response, HttpStatus.SC_UNAUTHORIZED);
    }

    @Test(groups = "discoveryTests", dependsOnMethods = "testGetDiscoveryAttributesToOrganizationUnauthorized")
    public void testDeleteDiscoveryAttributesOfNonExistingOrganization() {

        String endpointURL = ORGANIZATION_MANAGEMENT_API_BASE_PATH + PATH_SEPARATOR + invalidOrganizationID
                + ORGANIZATION_DISCOVERY_API_PATH;
        Response response = given().auth().preemptive().oauth2(m2mToken)
                .contentType(ContentType.JSON)
                .when()
                .get(endpointURL);
        validateHttpStatusCode(response, HttpStatus.SC_NOT_FOUND);
    }

    @Test(groups = "discoveryTests", dependsOnMethods = "testDeleteDiscoveryAttributesOfNonExistingOrganization")
    public void testDeleteDiscoveryAttributesUnauthorized() {

        String endpointURL = ORGANIZATION_MANAGEMENT_API_BASE_PATH + PATH_SEPARATOR + organizationID
                + ORGANIZATION_DISCOVERY_API_PATH;
        Response response = given().auth().preemptive().oauth2(invalidM2MToken)
                .contentType(ContentType.JSON)
                .when()
                .delete(endpointURL);
        validateHttpStatusCode(response, HttpStatus.SC_UNAUTHORIZED);
    }

    @Test(groups = "discoveryTests", dependsOnMethods = "testDeleteDiscoveryAttributesUnauthorized")
    public void testUpdateDiscoveryAttributesOfNonExistingOrganization() throws IOException {

        String endpointURL = ORGANIZATION_MANAGEMENT_API_BASE_PATH + PATH_SEPARATOR + invalidOrganizationID
                + ORGANIZATION_DISCOVERY_API_PATH;
        String requestBody = readResource("update-discovery-config-request-body.json");
        Response response = given().auth().preemptive().oauth2(m2mToken)
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .put(endpointURL);
        validateHttpStatusCode(response, HttpStatus.SC_NOT_FOUND);
    }

    @Test(groups = "discoveryTests", dependsOnMethods = "testUpdateDiscoveryAttributesOfNonExistingOrganization")
    public void testUpdateDiscoveryAttributesUnauthorized() throws IOException {

        String endpointURL = ORGANIZATION_MANAGEMENT_API_BASE_PATH + PATH_SEPARATOR + organizationID
                + ORGANIZATION_DISCOVERY_API_PATH;
        String requestBody = readResource("update-discovery-config-request-body.json");
        Response response = given().auth().preemptive().oauth2(invalidM2MToken)
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .put(endpointURL);
        validateHttpStatusCode(response, HttpStatus.SC_UNAUTHORIZED);
    }

    @Test(groups = "discoveryTests", dependsOnMethods = "testUpdateDiscoveryAttributesUnauthorized")
    public void testCheckDiscoveryAttributeExistsUnauthorized() throws IOException {

        String endpointURL = ORGANIZATION_MANAGEMENT_API_BASE_PATH + PATH_SEPARATOR + "check-discovery";
        String requestBody = readResource("check-discovery-attributes-request-body.json");
        Response response = given().auth().preemptive().oauth2(invalidM2MToken)
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post(endpointURL);
        validateHttpStatusCode(response, HttpStatus.SC_UNAUTHORIZED);
    }

    @Test(groups = "discoveryTests", dependsOnMethods = "testCheckDiscoveryAttributeExistsUnauthorized")
    public void testGetDeletedDiscoveryAttributes() {

        deleteDiscoveryAttributes();
        String endpointURL = ORGANIZATION_MANAGEMENT_API_BASE_PATH + PATH_SEPARATOR + organizationID
                + ORGANIZATION_DISCOVERY_API_PATH;
        Response response = given().auth().preemptive().oauth2(m2mToken)
                .contentType(ContentType.JSON)
                .when()
                .get(endpointURL);
        //Returns an empty "attributes": []
        validateHttpStatusCode(response, HttpStatus.SC_OK);
    }

    @Test(dependsOnGroups = "discoveryTests", dependsOnMethods = "testGetDeletedDiscoveryAttributes")
    public void testDeleteDiscoveryConfigUnauthorized() {

        String endpointURL = ORGANIZATION_CONFIGS_API_BASE_PATH + ORGANIZATION_DISCOVERY_API_PATH;
        Response response = given().auth().preemptive().oauth2(invalidM2MToken)
                .accept(ContentType.JSON)
                .when()
                .delete(endpointURL);
        validateHttpStatusCode(response, HttpStatus.SC_UNAUTHORIZED);
    }

    private void deleteDiscoveryAttributes() {

        String endpointURL = ORGANIZATION_MANAGEMENT_API_BASE_PATH + PATH_SEPARATOR + organizationID
                                + ORGANIZATION_DISCOVERY_API_PATH;
        Response response = given().auth().preemptive().oauth2(m2mToken)
                .accept(ContentType.JSON)
                .when()
                .delete(endpointURL);
        validateHttpStatusCode(response, HttpStatus.SC_NO_CONTENT);
    }

    private void deleteApplication(String applicationId) throws Exception {

        oAuth2RestClient.deleteApplication(applicationId);
    }
}

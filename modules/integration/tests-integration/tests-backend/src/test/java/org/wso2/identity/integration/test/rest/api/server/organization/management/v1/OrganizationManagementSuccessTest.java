/*
 * Copyright (c) 2023-2024, WSO2 LLC. (http://www.wso2.com).
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

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationListItem;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationSharePOSTRequest;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.InboundProtocols;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.OpenIDConnectConfiguration;
import org.wso2.identity.integration.test.restclients.OAuth2RestClient;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.testng.Assert.assertNotNull;
import static org.wso2.identity.integration.test.restclients.RestBaseClient.API_SERVER_PATH;
import static org.wso2.identity.integration.test.restclients.RestBaseClient.CONTENT_TYPE_ATTRIBUTE;
import static org.wso2.identity.integration.test.restclients.RestBaseClient.ORGANIZATION_PATH;
import static org.wso2.identity.integration.test.restclients.RestBaseClient.TENANT_PATH;
import static org.wso2.identity.integration.test.scim2.SCIM2BaseTestCase.SCIM2_USERS_ENDPOINT;

/**
 * Tests for successful cases of the Organization Management REST APIs.
 */
public class OrganizationManagementSuccessTest extends OrganizationManagementBaseTest {

    private String organizationID;
    private String childOrganizationID;
    private String selfServiceAppId;
    private String selfServiceAppClientId;
    private String selfServiceAppClientSecret;
    private String m2mToken;
    private String switchedM2MToken;
    private String b2bApplicationID;
    private HttpClient client;
    private final int NUM_OF_ORGANIZATIONS_FOR_PAGINATION_TESTS = 20;
    private List<Map<String, String>> organizations;

    protected OAuth2RestClient restClient;

    @Factory(dataProvider = "restAPIUserConfigProvider")
    public OrganizationManagementSuccessTest(TestUserMode userMode) throws Exception {

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
        deleteApplication(b2bApplicationID);
        oAuth2RestClient.closeHttpClient();
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
    public void createApplicationForSelfOrganizationOnboardService() throws IOException, JSONException {

        String endpointURL = "applications";
        String body = readResource("create-organization-self-service-app-request-body.json");

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

            Response apiResource = given().auth().preemptive().basic(authenticatingUserName, authenticatingCredential)
                    .when().queryParam("filter", "identifier eq " + apiName).get("api-resources");
            apiResource.then().log().ifValidationFails().assertThat().statusCode(HttpStatus.SC_OK);
            String apiUUID = apiResource.getBody().jsonPath().getString("apiResources[0].id");

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

    @Test(dependsOnMethods = "createApplicationForSelfOrganizationOnboardService")
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

    @Test(dependsOnMethods = "getM2MAccessToken")
    public void testSelfOnboardOrganization() throws IOException {

        String body = readResource("add-greater-hospital-organization-request-body.json");
        body = body.replace("${parentId}", StringUtils.EMPTY);
        Response response = getResponseOfPostWithOAuth2(ORGANIZATION_MANAGEMENT_API_BASE_PATH, body, m2mToken);
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

    @Test(dependsOnMethods = "testSelfOnboardOrganization")
    public void testGetOrganization() {

        Response response = getResponseOfGet(ORGANIZATION_MANAGEMENT_API_BASE_PATH + PATH_SEPARATOR
                                        + organizationID);
        validateHttpStatusCode(response, HttpStatus.SC_OK);
        Assert.assertNotNull(response.asString());
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("id", equalTo(organizationID));
    }

    @DataProvider(name = "dataProviderForFilterOrganizations")
    public Object[][] dataProviderForFilterOrganizations() {

        return new Object[][] {
                {"name co G", false, false},
                {"attributes.Country co S", true, false},
                {"attributes.Country eq Sri Lanka and name co Greater", true, false},
                {"attributes.Country eq Sri Lanka and attributes.Language eq Sinhala", true, false},
                {"attributes.Country eq USA", false, true}
        };
    }

    @Test(dependsOnMethods = "testGetOrganization", dataProvider = "dataProviderForFilterOrganizations")
    public void testFilterOrganizations(String filterQuery, boolean expectAttributes, boolean expectEmptyList) {

        String query = "?filter=" + filterQuery + "&limit=1&recursive=false";
        String endpointURL = ORGANIZATION_MANAGEMENT_API_BASE_PATH + query;
        Response response = getResponseOfGetWithOAuth2(endpointURL, m2mToken);
        Assert.assertNotNull(response.asString());
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);

        if (expectEmptyList) {
            response.then()
                    .assertThat().body(equalTo("{}"));
        } else {
            response.then()
                    .body("organizations.size()", equalTo(1))
                    .body("organizations[0].id", equalTo(organizationID));
            if (expectAttributes) {
                response.then()
                        .body("organizations[0].attributes.size()", equalTo(2))
                        .body("organizations[0].attributes[0].key", equalTo("Country"))
                        .body("organizations[0].attributes[0].value", equalTo("Sri Lanka"));
            }
        }
    }

    @Test(dependsOnMethods = "testFilterOrganizations")
    public void switchM2MToken() throws IOException, ParseException, InterruptedException {

        ApplicationSharePOSTRequest applicationSharePOSTRequest = new ApplicationSharePOSTRequest();
        applicationSharePOSTRequest.setShareWithAllChildren(false);
        applicationSharePOSTRequest.setSharedOrganizations(Collections.singletonList(organizationID));
        oAuth2RestClient.shareApplication(selfServiceAppId, applicationSharePOSTRequest);

        // Since application sharing is an async operation, wait for sometime before switching the organization.
        Thread.sleep(5000);

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.GRANT_TYPE_NAME, "organization_switch"));
        urlParameters.add(new BasicNameValuePair("token", m2mToken));
        urlParameters.add(new BasicNameValuePair("scope", "SYSTEM"));
        urlParameters.add(new BasicNameValuePair("switching_organization", organizationID));

        HttpPost httpPost = new HttpPost(getTenantQualifiedURL(OAuth2Constant.ACCESS_TOKEN_ENDPOINT, tenant));
        httpPost.setHeader("Authorization", "Basic " + new String(Base64.encodeBase64(
                        (selfServiceAppClientId + ":" + selfServiceAppClientSecret).getBytes())));
        httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
        httpPost.setEntity(new UrlEncodedFormEntity(urlParameters));

        HttpResponse response = client.execute(httpPost);
        String responseString = EntityUtils.toString(response.getEntity(), "UTF-8");
        EntityUtils.consume(response.getEntity());
        JSONParser parser = new JSONParser();
        org.json.simple.JSONObject json = (org.json.simple.JSONObject) parser.parse(responseString);
        Assert.assertNotNull(json, "Access token response is null.");
        Assert.assertNotNull(json.get(OAuth2Constant.ACCESS_TOKEN), "Access token is null.");
        switchedM2MToken = (String) json.get(OAuth2Constant.ACCESS_TOKEN);
        Assert.assertNotNull(switchedM2MToken);
    }

    @Test(dependsOnMethods = "switchM2MToken")
    public void createUserInOrganization() throws IOException {

        String body = readResource("add-admin-user-in-organization-request-body.json");
        HttpPost request = new HttpPost(serverURL + TENANT_PATH + tenant + PATH_SEPARATOR + ORGANIZATION_PATH
                                    + SCIM2_USERS_ENDPOINT);
        Header[] headerList = new Header[2];
        headerList[0] = new BasicHeader("Authorization", "Bearer " + switchedM2MToken);
        headerList[1] = new BasicHeader(CONTENT_TYPE_ATTRIBUTE, "application/scim+json");
        request.setHeaders(headerList);
        request.setEntity(new StringEntity(body));
        HttpResponse response = client.execute(request);
        Assert.assertEquals(response.getStatusLine().getStatusCode(), 201);
    }

    @Test(dependsOnMethods = "createUserInOrganization")
    public void addB2BApplication() throws Exception {

        ApplicationModel application = new ApplicationModel();
        List<String> grantTypes = new ArrayList<>();
        Collections.addAll(grantTypes, "authorization_code");
        OpenIDConnectConfiguration oidcConfig = new OpenIDConnectConfiguration();
        oidcConfig.setGrantTypes(grantTypes);
        oidcConfig.setCallbackURLs(Collections.singletonList(OAuth2Constant.CALLBACK_URL));
        InboundProtocols inboundProtocolsConfig = new InboundProtocols();
        inboundProtocolsConfig.setOidc(oidcConfig);
        application.setInboundProtocolConfiguration(inboundProtocolsConfig);
        application.setName("Guardio-Business-App");
        b2bApplicationID = oAuth2RestClient.createApplication(application);
        Assert.assertNotNull(b2bApplicationID);
    }

    @Test(dependsOnMethods = "addB2BApplication")
    public void shareB2BApplication() throws JSONException {

        if (!SUPER_TENANT_DOMAIN.equals(tenant)) {
            return;
        }
        String shareApplicationUrl = ORGANIZATION_MANAGEMENT_API_BASE_PATH + "/" + SUPER_ORGANIZATION_ID
                                + "/applications/" + b2bApplicationID + "/share";
        org.json.JSONObject shareAppObject = new org.json.JSONObject();
        shareAppObject.put("shareWithAllChildren", true);
        getResponseOfPost(shareApplicationUrl, shareAppObject.toString());
    }

    @Test(dependsOnMethods = "shareB2BApplication")
    public void unShareB2BApplication() throws JSONException {

        if (!SUPER_TENANT_DOMAIN.equals(tenant)) {
            return;
        }
        String shareApplicationUrl = ORGANIZATION_MANAGEMENT_API_BASE_PATH + "/" + SUPER_ORGANIZATION_ID
                                + "/applications/" + b2bApplicationID + "/share";
        org.json.JSONObject shareAppObject = new org.json.JSONObject();
        shareAppObject.put("shareWithAllChildren", false);
        getResponseOfPost(shareApplicationUrl, shareAppObject.toString());
    }

    @Test(dependsOnMethods = "unShareB2BApplication")
    public void testOnboardChildOrganization() throws IOException {

        String body = readResource("add-smaller-hospital-organization-request-body.json");
        body = body.replace("${parentId}", organizationID);
        HttpPost request = new HttpPost(serverURL + TENANT_PATH + tenant + PATH_SEPARATOR + ORGANIZATION_PATH
                                    + API_SERVER_PATH + ORGANIZATION_MANAGEMENT_API_BASE_PATH);
        Header[] headerList = new Header[3];
        headerList[0] = new BasicHeader("Authorization", "Bearer " + switchedM2MToken);
        headerList[1] = new BasicHeader(CONTENT_TYPE_ATTRIBUTE, "application/json");
        headerList[2] = new BasicHeader(HttpHeaders.ACCEPT, "application/json");
        request.setHeaders(headerList);
        request.setEntity(new StringEntity(body));
        HttpResponse response = client.execute(request);
        Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_CREATED);

        String jsonResponse = EntityUtils.toString(response.getEntity());
        JsonObject responseObject = JsonParser.parseString(jsonResponse).getAsJsonObject();
        childOrganizationID = responseObject.get("id").getAsString();
        assertNotNull(childOrganizationID);
    }

    @DataProvider(name = "dataProviderForGetOrganizationsMetaAttributes")
    public Object[][] dataProviderForGetOrganizationsMetaAttributes() {

        return new Object[][] {
                {"attributes eq Country", false, false},
                {"attributes sw C and attributes ew try", false, false},
                {"attributes eq Region", true, false},
                {"attributes co A", true, true},
        };
    }

    @Test(dependsOnMethods  = "testOnboardChildOrganization",
          dataProvider      = "dataProviderForGetOrganizationsMetaAttributes")
    public void testGetOrganizationsMetaAttributes(String filter, boolean isRecursive, boolean expectEmptyList) {

        String query = "?filter=" + filter + "&limit=1&recursive=" + isRecursive;
        String endpointURL = ORGANIZATION_MANAGEMENT_API_BASE_PATH + "/meta-attributes" + query;
        Response response = getResponseOfGetWithOAuth2(endpointURL, m2mToken);
        Assert.assertNotNull(response.asString());
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);
        if (expectEmptyList) {
            response.then()
                    .assertThat().body(equalTo("{}"));
        } else if (isRecursive) {
            response.then()
                    .body("attributes.size()", equalTo(1))
                    .body("attributes[0]", equalTo("Region"));
        } else {
            response.then()
                    .body("attributes.size()", equalTo(1))
                    .body("attributes[0]", equalTo("Country"));
        }
    }

    @Test(dependsOnMethods = "testGetOrganizationsMetaAttributes")
    public void testAddDiscoveryConfig() throws IOException {

        String endpointURL = ORGANIZATION_CONFIGS_API_BASE_PATH + ORGANIZATION_DISCOVERY_API_PATH;
        String requestBody = readResource("add-discovery-config-request-body.json");
        Response response = getResponseOfPostWithOAuth2(endpointURL, requestBody, m2mToken);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .contentType(ContentType.JSON)
                .body("properties[0].key", equalTo("emailDomain.enable"))
                .body("properties[0].value", equalTo("true"));
    }

    @Test(dependsOnMethods = "testAddDiscoveryConfig")
    public void testGetDiscoveryConfig() {

        String endpointURL = ORGANIZATION_CONFIGS_API_BASE_PATH + ORGANIZATION_DISCOVERY_API_PATH;
        Response response = getResponseOfGetWithOAuth2(endpointURL, m2mToken);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .contentType(ContentType.JSON)
                .body("properties[0].key", equalTo("emailDomain.enable"))
                .body("properties[0].value", equalTo("true"));
    }

    @Test(dependsOnMethods = "testGetDiscoveryConfig")
    public void testAddDiscoveryAttributesToOrganization() throws IOException {

        String endpointURL = ORGANIZATION_MANAGEMENT_API_BASE_PATH + ORGANIZATION_DISCOVERY_API_PATH;
        String requestBody = readResource("add-discovery-attributes-request-body.json");
        requestBody = requestBody.replace("${organizationID}", organizationID);
        Response response = getResponseOfPostWithOAuth2(endpointURL, requestBody, m2mToken);
        validateHttpStatusCode(response, HttpStatus.SC_CREATED);
    }

    @Test(dependsOnMethods = "testAddDiscoveryAttributesToOrganization")
    public void testGetDiscoveryAttributesOfOrganizations() {

        String endpointURL = ORGANIZATION_MANAGEMENT_API_BASE_PATH + ORGANIZATION_DISCOVERY_API_PATH;
        Response response = getResponseOfGetWithOAuth2(endpointURL, m2mToken);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("totalResults", equalTo(1))
                .body("organizations[0].organizationId", equalTo(organizationID))
                .body("organizations[0].organizationName", equalTo("Greater Hospital"))
                .body("organizations[0].attributes[0].type", equalTo("emailDomain"))
                .body("organizations[0].attributes[0].values[0]", equalTo("abc.com"));
    }

    @Test(dependsOnMethods = "testGetDiscoveryAttributesOfOrganizations")
    public void testGetDiscoveryAttributesOfOrganization() {

        String endpointURL = ORGANIZATION_MANAGEMENT_API_BASE_PATH + PATH_SEPARATOR + organizationID
                        + ORGANIZATION_DISCOVERY_API_PATH;
        Response response = getResponseOfGetWithOAuth2(endpointURL, m2mToken);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("attributes[0].type", equalTo("emailDomain"))
                .body("attributes[0].values[0]", equalTo("abc.com"));
    }

    @Test(dependsOnMethods = "testGetDiscoveryAttributesOfOrganization")
    public void testUpdateDiscoveryAttributesOfOrganization() throws IOException {

        String endpointURL = ORGANIZATION_MANAGEMENT_API_BASE_PATH + PATH_SEPARATOR + organizationID
                        + ORGANIZATION_DISCOVERY_API_PATH;
        String requestBody = readResource("update-discovery-attributes-request-body.json");
        Response response = getResponseOfPutWithOAuth2(endpointURL, requestBody, m2mToken);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("attributes[0].type", equalTo("emailDomain"))
                .body("attributes[0].values", containsInAnyOrder("xyz.com", "example.com"));
    }

    @DataProvider(name = "checkDiscoveryAttributes")
    public Object[][] checkDiscoveryAttributeFilePaths() {

        return new Object[][]{
                {"check-discovery-attributes-available-request-body.json", true},
                {"check-discovery-attributes-unavailable-request-body.json", false}
        };
    }

    @Test(dependsOnMethods = "testUpdateDiscoveryAttributesOfOrganization", dataProvider = "checkDiscoveryAttributes")
    public void testCheckDiscoveryAttributeExists(String requestBodyFileName, boolean expectedAvailability)
            throws IOException {

        String endpointURL = ORGANIZATION_MANAGEMENT_API_BASE_PATH + PATH_SEPARATOR + "check-discovery";
        String requestBody = readResource(requestBodyFileName);
        Response response = getResponseOfPostWithOAuth2(endpointURL, requestBody, m2mToken);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("available", equalTo(expectedAvailability));
    }

    @Test(dependsOnMethods = "testCheckDiscoveryAttributeExists")
    public void testDeleteDiscoveryAttributesOfOrganization() {

        String endpointURL = ORGANIZATION_MANAGEMENT_API_BASE_PATH + PATH_SEPARATOR + organizationID
                        + ORGANIZATION_DISCOVERY_API_PATH;
        Response response = getResponseOfDeleteWithOAuth2(endpointURL, m2mToken);
        validateHttpStatusCode(response, HttpStatus.SC_NO_CONTENT);
    }

    @Test(dependsOnMethods = "testDeleteDiscoveryAttributesOfOrganization")
    public void testDeleteDiscoveryConfig() {

        String endpointURL = ORGANIZATION_CONFIGS_API_BASE_PATH + ORGANIZATION_DISCOVERY_API_PATH;
        Response response = getResponseOfDeleteWithOAuth2(endpointURL, m2mToken);
        validateHttpStatusCode(response, HttpStatus.SC_NO_CONTENT);
    }

    @Test(dependsOnMethods = "testDeleteDiscoveryConfig")
    public void testDeleteChildOrganization() throws IOException {

        HttpDelete request = new HttpDelete(serverURL + TENANT_PATH + tenant + PATH_SEPARATOR + ORGANIZATION_PATH
                                        + API_SERVER_PATH + ORGANIZATION_MANAGEMENT_API_BASE_PATH + PATH_SEPARATOR
                                        + childOrganizationID);
        Header[] headerList = new Header[1];
        headerList[0] = new BasicHeader("Authorization", "Bearer " + switchedM2MToken);
        request.setHeaders(headerList);
        HttpResponse response = client.execute(request);
        Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_NO_CONTENT);
    }

    @Test(dependsOnMethods = "testDeleteChildOrganization")
    public void testDisablingOrganization() throws IOException {

        String endpoint = ORGANIZATION_MANAGEMENT_API_BASE_PATH + PATH_SEPARATOR + organizationID;
        String body = readResource("disable-organization-request-body.json");
        Response response = getResponseOfPatch(endpoint, body);
        validateHttpStatusCode(response, HttpStatus.SC_OK);
    }

    @Test(dependsOnMethods = "testDisablingOrganization")
    public void testDeleteOrganization() {

        String organizationPath = ORGANIZATION_MANAGEMENT_API_BASE_PATH + "/" + organizationID;
        Response response = getResponseOfDelete(organizationPath);
        validateHttpStatusCode(response, HttpStatus.SC_NO_CONTENT);
    }

    private void deleteApplication(String applicationId) throws Exception {

        oAuth2RestClient.deleteApplication(applicationId);
    }

    @Test(dependsOnMethods = "testDeleteOrganization")
    public void createOrganizationsForPaginationTests() throws JSONException {

        organizations = createOrganizations(NUM_OF_ORGANIZATIONS_FOR_PAGINATION_TESTS);

        if (organizations.size() != NUM_OF_ORGANIZATIONS_FOR_PAGINATION_TESTS) {
            throw new RuntimeException("Failed to create the expected number of organizations for testing pagination.");
        }
    }

    @DataProvider(name = "paginationLimitsDataProvider")
    public Object[][] paginationLimitsDataProvider() {

        return new Object[][]{
                {10},
                {20},
                {25},
        };
    }

    @Test(dataProvider = "paginationLimitsDataProvider", dependsOnMethods = "createOrganizationsForPaginationTests")
    public void testGetPaginatedOrganizationsWithLimit(int limit) {

        String endpointURL = ORGANIZATION_MANAGEMENT_API_BASE_PATH + QUESTION_MARK + LIMIT_QUERY_PARAM + limit;
        Response response = getResponseOfGetWithOAuth2(endpointURL, m2mToken);

        validateHttpStatusCode(response, HttpStatus.SC_OK);

        int actualOrganizationCount = response.jsonPath().getList(ORGANIZATIONS_PATH_PARAM).size();
        int expectedOrganizationCount = Math.min(limit, NUM_OF_ORGANIZATIONS_FOR_PAGINATION_TESTS);
        Assert.assertEquals(actualOrganizationCount, expectedOrganizationCount);

        String nextLink = response.jsonPath().getString("links.find { it.rel == 'next' }.href");
        String afterValue = null;

        if (nextLink != null && nextLink.contains(AFTER_QUERY_PARAM)) {
            afterValue = nextLink.substring(nextLink.indexOf(AFTER_QUERY_PARAM) + 6);
        }

        String storedAfterValue = afterValue;

        if (NUM_OF_ORGANIZATIONS_FOR_PAGINATION_TESTS > limit) {
            Assert.assertNotNull(storedAfterValue);
        } else {
            Assert.assertNull(storedAfterValue);
        }
    }

    @Test(dependsOnMethods = "createOrganizationsForPaginationTests")
    public void testGetPaginatedOrganizationsWithCursor() {

        String after;
        String before;
        int limit = 2;
        int orgLimit = 20;
        int largeLimit = 30;

        // Step 1: Call the first page.
        String firstPageUrl =
                ORGANIZATION_MANAGEMENT_API_BASE_PATH + QUESTION_MARK + LIMIT_QUERY_PARAM + limit + AMPERSAND +
                        RECURSIVE_QUERY_PARAM + FALSE;
        Response firstPageResponse = getResponseOfGetWithOAuth2(firstPageUrl, m2mToken);

        validateHttpStatusCode(firstPageResponse, HttpStatus.SC_OK);

        List<Map<String, String>> firstPageLinks = firstPageResponse.jsonPath().getList(LINKS_PATH_PARAM);
        after = getLink(firstPageLinks, LINK_REL_NEXT);
        before = getLink(firstPageLinks, LINK_REL_PREVIOUS);

        Assert.assertNotNull(after, "After value should not be null on the first page.");
        Assert.assertNull(before, "Before value should be null on the first page.");

        // Validate the first page organizations
        validateOrganizationsOnPage(firstPageResponse, 1, NUM_OF_ORGANIZATIONS_FOR_PAGINATION_TESTS, limit);

        // Step 2: Call the second page using the 'after' value.
        String secondPageUrl = ORGANIZATION_MANAGEMENT_API_BASE_PATH + QUESTION_MARK + LIMIT_QUERY_PARAM + limit
                + AMPERSAND + RECURSIVE_QUERY_PARAM + FALSE + AMPERSAND + AFTER_QUERY_PARAM + after;
        Response secondPageResponse = getResponseOfGetWithOAuth2(secondPageUrl, m2mToken);

        validateHttpStatusCode(secondPageResponse, HttpStatus.SC_OK);

        List<Map<String, String>> secondPageLinks = secondPageResponse.jsonPath().getList(LINKS_PATH_PARAM);
        before = getLink(secondPageLinks, LINK_REL_PREVIOUS);
        after = getLink(secondPageLinks, LINK_REL_NEXT);

        Assert.assertNotNull(before, "Before value should not be null on the second page.");
        if (NUM_OF_ORGANIZATIONS_FOR_PAGINATION_TESTS > limit * 2) {
            Assert.assertNotNull(after, "After value should not be null if there are more pages.");
        } else {
            Assert.assertNull(after, "After value should be null if this is the last page.");
        }

        // Validate the second page organizations
        validateOrganizationsOnPage(secondPageResponse, 2, NUM_OF_ORGANIZATIONS_FOR_PAGINATION_TESTS, limit);

        // Step 3: Call the previous page using the 'before' value.
        String previousPageUrl = ORGANIZATION_MANAGEMENT_API_BASE_PATH + QUESTION_MARK + LIMIT_QUERY_PARAM + limit
                + AMPERSAND + RECURSIVE_QUERY_PARAM + FALSE + AMPERSAND + BEFORE_QUERY_PARAM + before;
        Response previousPageResponse = getResponseOfGetWithOAuth2(previousPageUrl, m2mToken);

        validateHttpStatusCode(previousPageResponse, HttpStatus.SC_OK);

        List<Map<String, String>> previousPageLinks = previousPageResponse.jsonPath().getList(LINKS_PATH_PARAM);
        after = getLink(previousPageLinks, LINK_REL_NEXT);
        before = getLink(previousPageLinks, LINK_REL_PREVIOUS);

        Assert.assertNotNull(after, "After value should not be null on the previous (first) page.");
        Assert.assertNull(before, "Before value should be null on the previous (first) page.");

        // Validate the previous page organizations
        validateOrganizationsOnPage(previousPageResponse, 1, NUM_OF_ORGANIZATIONS_FOR_PAGINATION_TESTS, limit);

        // Step 4: Test with orgLimit. (equal to total org count)
        validatePaginationForNonPaginatedLimit(orgLimit);

        // Step 5: Test with largeLimit. (greater than total org count)
        validatePaginationForNonPaginatedLimit(largeLimit);
    }

    private List<Map<String, String>> createOrganizations(int numberOfOrganizations) throws JSONException {

        List<Map<String, String>> newOrganizations = new ArrayList<>();

        for (int i = 0; i < numberOfOrganizations; i++) {
            JSONObject body = new JSONObject()
                    .put(ORGANIZATION_NAME, String.format(ORGANIZATION_NAME_FORMAT, i))
                    .put(ORGANIZATION_DESCRIPTION, String.format(ORGANIZATION_DESCRIPTION_FORMAT, i));

            Response response =
                    getResponseOfPostWithOAuth2(ORGANIZATION_MANAGEMENT_API_BASE_PATH, body.toString(), m2mToken);

            if (response.getStatusCode() == HttpStatus.SC_CREATED) {
                // Store the created organization details.
                Map<String, String> org = new HashMap<>();
                org.put(ORGANIZATION_NAME, String.format(ORGANIZATION_NAME_FORMAT, i));
                org.put(ORGANIZATION_DESCRIPTION, String.format(ORGANIZATION_DESCRIPTION_FORMAT, i));
                newOrganizations.add(org);

            } else {
                throw new RuntimeException("Failed to create organization " + i);
            }
        }

        return newOrganizations;
    }

    private void validatePaginationForNonPaginatedLimit(int limit) {

        String limitUrl =
                ORGANIZATION_MANAGEMENT_API_BASE_PATH + QUESTION_MARK + LIMIT_QUERY_PARAM + limit + AMPERSAND +
                        RECURSIVE_QUERY_PARAM + FALSE;
        Response response = getResponseOfGetWithOAuth2(limitUrl, m2mToken);

        validateHttpStatusCode(response, HttpStatus.SC_OK);

        List<Map<String, String>> links = response.jsonPath().getList(LINKS_PATH_PARAM);

        Assert.assertNull(links, "Links should be null when all organizations are returned in one page.");

        int returnedOrgCount = response.jsonPath().getList(ORGANIZATIONS_PATH_PARAM).size();
        Assert.assertEquals(returnedOrgCount, NUM_OF_ORGANIZATIONS_FOR_PAGINATION_TESTS,
                "The number of returned organizations should match the total created organizations.");
    }

    private String getLink(List<Map<String, String>> links, String rel) {

        for (Map<String, String> link : links) {
            if (rel.equals(link.get(REL))) {
                String href = link.get(HREF);
                if (href.contains(AFTER_QUERY_PARAM)) {
                    return href.substring(href.indexOf(AFTER_QUERY_PARAM) + 6);
                } else if (href.contains(BEFORE_QUERY_PARAM)) {
                    return href.substring(href.indexOf(BEFORE_QUERY_PARAM) + 7);
                }
            }
        }
        return null;
    }

    private void validateOrganizationsOnPage(Response response, int pageNum, int totalOrganizations, int limit) {

        // Validate the organization count
        int expectedOrgCount = Math.min(limit, totalOrganizations - (pageNum - 1) * limit);
        int actualOrgCount = response.jsonPath().getList(ORGANIZATIONS_PATH_PARAM).size();
        Assert.assertEquals(actualOrgCount, expectedOrgCount, "Organization count mismatch on page " + pageNum);

        // Validate the organization names and descriptions
        List<String> actualOrgNames = response.jsonPath().getList(ORGANIZATIONS_PATH_PARAM + ".name");

        for (int i = 0; i < expectedOrgCount; i++) {
            int orgIndex = totalOrganizations - ((pageNum - 1) * limit) - i - 1;

            String expectedOrgName = String.format(ORGANIZATION_NAME_FORMAT, orgIndex);
            Assert.assertEquals(actualOrgNames.get(i), expectedOrgName,
                    "Organization name mismatch on page " + pageNum + " at index " + i);
        }
    }


}

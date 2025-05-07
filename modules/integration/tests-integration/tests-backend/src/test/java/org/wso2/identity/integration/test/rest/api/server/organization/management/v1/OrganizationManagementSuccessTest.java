/*
 * Copyright (c) 2023-2025, WSO2 LLC. (http://www.wso2.com).
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
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Lookup;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.cookie.CookieSpecProvider;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.RFC6265CookieSpecProvider;
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
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationSharePOSTRequest;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.OpenIDConnectConfiguration;
import org.wso2.identity.integration.test.restclients.OAuth2RestClient;
import org.wso2.identity.integration.test.restclients.SCIM2RestClient;
import org.wso2.identity.integration.test.utils.DataExtractUtil;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.wso2.carbon.identity.api.resource.mgt.constant.APIResourceManagementConstants.APIResourceTypes.SYSTEM;
import static org.wso2.identity.integration.test.restclients.RestBaseClient.API_SERVER_PATH;
import static org.wso2.identity.integration.test.restclients.RestBaseClient.AUTHORIZATION_ATTRIBUTE;
import static org.wso2.identity.integration.test.restclients.RestBaseClient.BASIC_AUTHORIZATION_ATTRIBUTE;
import static org.wso2.identity.integration.test.restclients.RestBaseClient.CONTENT_TYPE_ATTRIBUTE;
import static org.wso2.identity.integration.test.restclients.RestBaseClient.ORGANIZATION_PATH;
import static org.wso2.identity.integration.test.restclients.RestBaseClient.TENANT_PATH;
import static org.wso2.identity.integration.test.restclients.RestBaseClient.USER_AGENT_ATTRIBUTE;
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
    private String b2bAppClientId;
    private String b2bAppClientSecret;
    private String b2bUserID;
    private HttpClient client;
    private HttpClient httpClientWithoutAutoRedirections;
    private List<Map<String, String>> organizations;
    private List<String> metaAttributes;

    protected OAuth2RestClient restClient;

    private Lookup<CookieSpecProvider> cookieSpecRegistry;
    private RequestConfig requestConfig;
    private final CookieStore cookieStore = new BasicCookieStore();

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
        scim2RestClient = new SCIM2RestClient(serverURL, tenantInfo);

        client = HttpClientBuilder.create().build();

        cookieSpecRegistry = RegistryBuilder.<CookieSpecProvider>create()
                .register(CookieSpecs.DEFAULT, new RFC6265CookieSpecProvider())
                .build();
        requestConfig = RequestConfig.custom()
                .setCookieSpec(CookieSpecs.DEFAULT)
                .build();
        httpClientWithoutAutoRedirections = HttpClientBuilder.create()
                .setDefaultRequestConfig(requestConfig)
                .setDefaultCookieSpecRegistry(cookieSpecRegistry)
                .disableRedirectHandling()
                .setDefaultCookieStore(cookieStore).build();
    }

    @AfterClass(alwaysRun = true)
    public void testConclude() throws Exception {

        super.conclude();
        deleteApplication(selfServiceAppId);
        deleteApplication(b2bApplicationID);
        oAuth2RestClient.closeHttpClient();
        scim2RestClient.closeHttpClient();
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

        return new Object[][] {
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

        JSONObject jsonObject = new JSONObject(readResource(ORGANIZATION_SELF_SERVICE_APIS));

        for (Iterator<String> apiNameIterator = jsonObject.keys(); apiNameIterator.hasNext(); ) {
            String apiName = apiNameIterator.next();
            Object requiredScopes = jsonObject.get(apiName);

            Response apiResource = given().auth().preemptive().basic(authenticatingUserName, authenticatingCredential)
                    .when().queryParam(FILTER_QUERY_PARAM, "identifier eq " + apiName).get("api-resources");
            apiResource.then().log().ifValidationFails().assertThat().statusCode(HttpStatus.SC_OK);
            String apiUUID = apiResource.getBody().jsonPath().getString("apiResources[0].id");

            JSONObject authorizedAPIRequestBody = new JSONObject();
            authorizedAPIRequestBody.put(ORGANIZATION_ID, apiUUID);
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
        Scope scope = new Scope(SYSTEM);

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

        String body = readResource(ADD_GREATER_HOSPITAL_ORGANIZATION_REQUEST_BODY);
        body = body.replace(PARENT_ID_PLACEHOLDER, StringUtils.EMPTY);
        Response response = getResponseOfPostWithOAuth2(ORGANIZATION_MANAGEMENT_API_BASE_PATH, body, m2mToken);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .header(HttpHeaders.LOCATION, notNullValue());
        String location = response.getHeader(HttpHeaders.LOCATION);
        assertNotNull(location);
        organizationID = location.substring(location.lastIndexOf(PATH_SEPARATOR) + 1);
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
                .body(ORGANIZATION_ID, equalTo(organizationID));
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

        b2bApplicationID = addApplication(B2B_APP_NAME);
    }

    @Test(dependsOnMethods = "addB2BApplication")
    public void shareB2BApplication() throws JSONException {

        if (!SUPER_TENANT_DOMAIN.equals(tenant)) {
            return;
        }
        String shareApplicationUrl = ORGANIZATION_MANAGEMENT_API_BASE_PATH + PATH_SEPARATOR + SUPER_ORGANIZATION_ID
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
        String shareApplicationUrl = ORGANIZATION_MANAGEMENT_API_BASE_PATH + PATH_SEPARATOR + SUPER_ORGANIZATION_ID
                + "/applications/" + b2bApplicationID + "/share";
        org.json.JSONObject shareAppObject = new org.json.JSONObject();
        shareAppObject.put("shareWithAllChildren", false);
        getResponseOfPost(shareApplicationUrl, shareAppObject.toString());
    }

    @Test(dependsOnMethods = "unShareB2BApplication")
    public void testOnboardChildOrganization() throws IOException {

        String body = readResource(ADD_SMALLER_HOSPITAL_ORGANIZATION_REQUEST_BODY);
        body = body.replace(PARENT_ID_PLACEHOLDER, organizationID);
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
        childOrganizationID = responseObject.get(ORGANIZATION_ID).getAsString();
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

    @Test(dependsOnMethods = "testOnboardChildOrganization",
            dataProvider = "dataProviderForGetOrganizationsMetaAttributes")
    public void testGetOrganizationsMetaAttributes(String filter, boolean isRecursive, boolean expectEmptyList) {

        String query = "?filter=" + filter + "&limit=1&recursive=" + isRecursive;
        String endpointURL = ORGANIZATION_MANAGEMENT_API_BASE_PATH + ORGANIZATION_META_ATTRIBUTES_API_PATH + query;
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
        String requestBody = readResource(ADD_DISCOVERY_CONFIG_REQUEST_BODY);
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
        String requestBody = readResource(ADD_DISCOVERY_ATTRIBUTES_REQUEST_BODY);
        requestBody = requestBody.replace(ORGANIZATION_ID_PLACEHOLDER, organizationID);
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
                .body(TOTAL_RESULTS_PATH_PARAM, equalTo(1))
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
    public void prepareForTestLoginHintParamInAuthRequest() throws Exception {

        b2bAppClientId = getAppClientId(b2bApplicationID);
        b2bAppClientSecret = getAppClientSecret(b2bApplicationID);
        shareApplication(b2bApplicationID);

        b2bUserID = createB2BUser(switchedM2MToken);
    }

    @DataProvider(name = "loginHintParamDataProvider")
    public Object[][] loginHintParamDataProvider() {

        return new Object[][] {
                // Include organization discovery type.
                {true},
                // Exclude organization discovery type.
                {false}
        };
    }

    @Test(dependsOnMethods = "prepareForTestLoginHintParamInAuthRequest", dataProvider = "loginHintParamDataProvider")
    public void testLoginHintParamInAuthRequest(boolean addOrgDiscoveryType) throws Exception {

        String sessionDataKey = sendAuthorizationRequest(addOrgDiscoveryType);
        String authorizationCode = sendLoginPost(sessionDataKey);
        String accessToken = getAccessToken(authorizationCode);
        validateAccessToken(accessToken);

        // Clear cookies of the http client to avoid session conflicts.
        cookieStore.clear();
    }

    @Test(dependsOnMethods = "testLoginHintParamInAuthRequest")
    public void cleanupAfterTestLoginHintParamInAuthRequest() throws Exception {

        unShareApplication(b2bApplicationID);
        scim2RestClient.deleteSubOrgUser(b2bUserID, switchedM2MToken);
    }

    @Test(dependsOnMethods = "cleanupAfterTestLoginHintParamInAuthRequest")
    public void testUpdateDiscoveryAttributesOfOrganization() throws IOException {

        String endpointURL = ORGANIZATION_MANAGEMENT_API_BASE_PATH + PATH_SEPARATOR + organizationID
                + ORGANIZATION_DISCOVERY_API_PATH;
        String requestBody = readResource(UPDATE_DISCOVERY_ATTRIBUTES_REQUEST_BODY);
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

        return new Object[][] {
                {CHECK_DISCOVERY_ATTRIBUTES_AVAILABLE_REQUEST_BODY, true},
                {CHECK_DISCOVERY_ATTRIBUTES_UNAVAILABLE_REQUEST_BODY, false}
        };
    }

    @Test(dependsOnMethods = "testUpdateDiscoveryAttributesOfOrganization", dataProvider = "checkDiscoveryAttributes")
    public void testCheckDiscoveryAttributeExists(String requestBodyFileName, boolean expectedAvailability)
            throws IOException {

        String endpointURL = ORGANIZATION_MANAGEMENT_API_BASE_PATH + CHECK_DISCOVERY_API_PATH;
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

        String organizationPath = ORGANIZATION_MANAGEMENT_API_BASE_PATH + PATH_SEPARATOR + organizationID;
        Response response = getResponseOfDelete(organizationPath);
        validateHttpStatusCode(response, HttpStatus.SC_NO_CONTENT);
    }

    private void deleteApplication(String applicationId) throws Exception {

        oAuth2RestClient.deleteApplication(applicationId);
    }

    @Test(groups = "organizationPaginationTests", dependsOnMethods = "testDeleteOrganization")
    public void createOrganizationsForPaginationTests() throws JSONException {

        organizations = createOrganizations(NUM_OF_ORGANIZATIONS_FOR_PAGINATION_TESTS);
        assertEquals(organizations.size(), NUM_OF_ORGANIZATIONS_FOR_PAGINATION_TESTS);
    }

    @DataProvider(name = "organizationLimitValidationDataProvider")
    public Object[][] organizationLimitValidationDataProvider() {

        return new Object[][] {
                {10},
                {20},
                {25},
        };
    }

    @Test(groups = "organizationPaginationTests", dependsOnMethods = "createOrganizationsForPaginationTests",
            dataProvider = "organizationLimitValidationDataProvider")
    public void testGetPaginatedOrganizationsWithLimit(int limit) {

        String endpointURL = ORGANIZATION_MANAGEMENT_API_BASE_PATH + QUESTION_MARK + LIMIT_QUERY_PARAM + EQUAL + limit;
        Response response = getResponseOfGetWithOAuth2(endpointURL, m2mToken);

        validateHttpStatusCode(response, HttpStatus.SC_OK);

        int actualOrganizationCount = response.jsonPath().getList(ORGANIZATIONS_PATH_PARAM).size();
        int expectedOrganizationCount = Math.min(limit, NUM_OF_ORGANIZATIONS_FOR_PAGINATION_TESTS);
        Assert.assertEquals(actualOrganizationCount, expectedOrganizationCount);

        String nextLink = response.jsonPath().getString(
                String.format("links.find { it.%s == '%s' }.%s", REL, LINK_REL_NEXT, HREF));

        String afterValue = null;

        if (nextLink != null && nextLink.contains(AFTER_QUERY_PARAM + EQUAL)) {
            afterValue = nextLink.substring(nextLink.indexOf(AFTER_QUERY_PARAM + EQUAL) + 6);
        }

        String storedAfterValue = afterValue;

        if (NUM_OF_ORGANIZATIONS_FOR_PAGINATION_TESTS > limit) {
            Assert.assertNotNull(storedAfterValue);
        } else {
            Assert.assertNull(storedAfterValue);
        }
    }

    @DataProvider(name = "organizationPaginationValidationDataProvider")
    public Object[][] organizationPaginationValidationProvider() {

        return new Object[][] {
                {1}, {2}, {5}, {6}, {10}, {17}
        };
    }

    @Test(groups = "organizationPaginationTests", dependsOnMethods = "createOrganizationsForPaginationTests",
            dataProvider = "organizationPaginationValidationDataProvider")
    public void testGetPaginatedOrganizations(int limit) {

        String after;
        String before;

        // Step 1: Call the first page.
        String firstPageUrl = ORGANIZATION_MANAGEMENT_API_BASE_PATH + QUESTION_MARK + LIMIT_QUERY_PARAM + EQUAL + limit
                + AMPERSAND + RECURSIVE_QUERY_PARAM + EQUAL + FALSE;

        Response firstPageResponse = getResponseOfGetWithOAuth2(firstPageUrl, m2mToken);

        validateHttpStatusCode(firstPageResponse, HttpStatus.SC_OK);

        List<Map<String, String>> firstPageLinks = firstPageResponse.jsonPath().getList(LINKS_PATH_PARAM);
        after = getLink(firstPageLinks, LINK_REL_NEXT);
        before = getLink(firstPageLinks, LINK_REL_PREVIOUS);

        Assert.assertNotNull(after, "After value should not be null on the first page.");
        Assert.assertNull(before, "Before value should be null on the first page.");

        // Validate the first page organizations.
        validateOrganizationsOnPage(firstPageResponse, 1, NUM_OF_ORGANIZATIONS_FOR_PAGINATION_TESTS, limit);

        // Step 2: Call the second page using the 'after' value.
        String secondPageUrl = ORGANIZATION_MANAGEMENT_API_BASE_PATH + QUESTION_MARK + LIMIT_QUERY_PARAM + EQUAL + limit
                + AMPERSAND + RECURSIVE_QUERY_PARAM + EQUAL + FALSE + AMPERSAND + AFTER_QUERY_PARAM + EQUAL + after;
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

        // Validate the second page organizations.
        validateOrganizationsOnPage(secondPageResponse, 2, NUM_OF_ORGANIZATIONS_FOR_PAGINATION_TESTS, limit);

        // Step 3: Call the previous page using the 'before' value.
        String previousPageUrl = ORGANIZATION_MANAGEMENT_API_BASE_PATH + QUESTION_MARK + LIMIT_QUERY_PARAM + EQUAL +
                limit + AMPERSAND + RECURSIVE_QUERY_PARAM + EQUAL + FALSE + AMPERSAND + BEFORE_QUERY_PARAM + EQUAL +
                before;
        Response previousPageResponse = getResponseOfGetWithOAuth2(previousPageUrl, m2mToken);

        validateHttpStatusCode(previousPageResponse, HttpStatus.SC_OK);

        List<Map<String, String>> previousPageLinks = previousPageResponse.jsonPath().getList(LINKS_PATH_PARAM);
        after = getLink(previousPageLinks, LINK_REL_NEXT);
        before = getLink(previousPageLinks, LINK_REL_PREVIOUS);

        Assert.assertNotNull(after, "After value should not be null on the previous (first) page.");
        Assert.assertNull(before, "Before value should be null on the previous (first) page.");

        // Validate the previous page organizations.
        validateOrganizationsOnPage(previousPageResponse, 1, NUM_OF_ORGANIZATIONS_FOR_PAGINATION_TESTS, limit);
    }

    @DataProvider(name = "organizationDiscoveryInvalidOffsetAtLimitAndLimitZeroDataProvider")
    public Object[][] organizationDiscoveryInvalidOffsetAtLimitAndLimitZeroDataProvider() {

        return new Object[][]{
                {"20", "0"},
                {"25", "0"}
        };
    }

    @Test(groups = "organizationPaginationTests",
            dependsOnMethods = "createOrganizationsForPaginationTests",
            dataProvider = "organizationDiscoveryInvalidOffsetAtLimitAndLimitZeroDataProvider")
    public void testGetPaginatedOrganizationsDiscoveryWithInvalidOffsetAndLimitZero(String offset,
                                                                                    String limit) {

        String url = ORGANIZATION_MANAGEMENT_API_BASE_PATH + ORGANIZATION_DISCOVERY_API_PATH + QUESTION_MARK +
                OFFSET_QUERY_PARAM + EQUAL + offset + AMPERSAND + LIMIT_QUERY_PARAM + EQUAL + limit;

        Response response = getResponseOfGetWithOAuth2(url, m2mToken);
        validateHttpStatusCode(response, HttpStatus.SC_OK);
        List<Map<String, String>> returnedOrganizations = response.jsonPath().getList(ORGANIZATIONS_PATH_PARAM);
        Assert.assertNull(returnedOrganizations);
        int totalResults = response.jsonPath().getInt("totalResults");
        Assert.assertEquals(totalResults, 0, "Total results should be 0 when the limit is 0.");
    }

    @DataProvider(name = "organizationPaginationNumericEdgeCasesOfLimitDataProvider")
    public Object[][] organizationPaginationNumericEdgeCasesOfLimitDataProvider() {

        return new Object[][] {
                {0}, {20}, {25}
        };
    }

    @Test(groups = "organizationPaginationTests",
            dependsOnMethods = "testGetPaginatedOrganizationsDiscoveryWithInvalidOffsetAndLimitZero",
            dataProvider = "organizationPaginationNumericEdgeCasesOfLimitDataProvider")
    public void testGetPaginatedOrganizationsForNumericEdgeCasesOfLimit(int limit) {

        String limitUrl = ORGANIZATION_MANAGEMENT_API_BASE_PATH + QUESTION_MARK + LIMIT_QUERY_PARAM + EQUAL + limit +
                AMPERSAND + RECURSIVE_QUERY_PARAM + EQUAL + FALSE;
        Response response = getResponseOfGetWithOAuth2(limitUrl, m2mToken);

        validateHttpStatusCode(response, HttpStatus.SC_OK);

        List<Map<String, String>> links = response.jsonPath().getList(LINKS_PATH_PARAM);

        Assert.assertNull(links, "Links should be null when all organizations are returned in one page.");

        // Validate the only page organizations.
        validateOrganizationsOnPage(response, 1, NUM_OF_ORGANIZATIONS_FOR_PAGINATION_TESTS, limit);
    }

    @DataProvider(name = "organizationPaginationNonNumericEdgeCasesOfLimitDataProvider")
    public Object[][] organizationPaginationNonNumericEdgeCasesOfLimitProvider() {

        return new Object[][] {
                {AMPERSAND + LIMIT_QUERY_PARAM + EQUAL},  // Test case 1: URL with LIMIT_QUERY_PARAM but no value.
                {""}  // Test case 2: URL without LIMIT_QUERY_PARAM.
        };
    }

    @Test(groups = "organizationPaginationTests", dependsOnMethods = "createOrganizationsForPaginationTests",
            dataProvider = "organizationPaginationNonNumericEdgeCasesOfLimitDataProvider")
    public void testGetPaginatedOrganizationsForNonNumericEdgeCasesOfLimit(String limitQueryParam) {

        String endpointURL = ORGANIZATION_MANAGEMENT_API_BASE_PATH + QUESTION_MARK + RECURSIVE_QUERY_PARAM + EQUAL +
                FALSE + limitQueryParam;

        Response response = getResponseOfGetWithOAuth2(endpointURL, m2mToken);

        validateHttpStatusCode(response, HttpStatus.SC_OK);

        validateOrganizationsForDefaultLimit(response, NUM_OF_ORGANIZATIONS_FOR_PAGINATION_TESTS);
    }

    @Test(groups = "organizationDiscoveryPaginationTests", dependsOnGroups = "organizationPaginationTests")
    public void testEnableEmailDomainDiscovery() {

        String enableDiscoveryPayload = "{\"properties\":[{\"key\":\"emailDomain.enable\",\"value\":true}]}";
        String emailDomainIsEnabled = "properties.find { it.key == 'emailDomain.enable' }.value";

        // Send POST request to enable email domain discovery.
        Response response = getResponseOfPostWithOAuth2(
                ORGANIZATION_CONFIGS_API_BASE_PATH + ORGANIZATION_DISCOVERY_API_PATH,
                enableDiscoveryPayload,
                m2mToken);

        // Validate that the request was successful.
        validateHttpStatusCode(response, HttpStatus.SC_CREATED);

        // Validate the response content.
        boolean isEnabled = response.jsonPath().getBoolean(emailDomainIsEnabled);
        Assert.assertTrue(isEnabled, "Email domain discovery was not successfully enabled.");
    }

    @Test(groups = "organizationDiscoveryPaginationTests", dependsOnMethods = "testEnableEmailDomainDiscovery")
    public void testAddEmailDomainsToOrganization() {

        for (int i = 0; i < NUM_OF_ORGANIZATIONS_FOR_PAGINATION_TESTS; i++) {
            String organizationId = organizations.get(i).get(ORGANIZATION_ID);
            addEmailDomainsToOrganization(organizationId, String.format(ORGANIZATION_EMAIL_FORMAT_1, i),
                    String.format(ORGANIZATION_EMAIL_FORMAT_2, i));
        }

    }

    @DataProvider(name = "organizationDiscoveryLimitValidationDataProvider")
    public Object[][] organizationDiscoveryLimitValidationDataProvider() {

        return new Object[][] {
                {3}, {5}, {10}, {15}, {17}, {20}, {25}
        };
    }

    @Test(groups = "organizationDiscoveryPaginationTests", dependsOnMethods = "testAddEmailDomainsToOrganization",
            dataProvider = "organizationDiscoveryLimitValidationDataProvider")
    public void testGetPaginatedOrganizationsDiscoveryWithLimit(int limit) {

        int offset = 0;
        List<String> accumulatedOrganizationNames = new ArrayList<>();

        // Loop through each page to test the organization discovery GET API limit.
        while (offset < NUM_OF_ORGANIZATIONS_FOR_PAGINATION_TESTS) {
            String queryUrl = ORGANIZATION_MANAGEMENT_API_BASE_PATH + ORGANIZATION_DISCOVERY_API_PATH + QUESTION_MARK +
                            OFFSET_QUERY_PARAM + EQUAL + offset + AMPERSAND + LIMIT_QUERY_PARAM + EQUAL + limit;
            Response response = getResponseOfGetWithOAuth2(queryUrl, m2mToken);

            validateHttpStatusCode(response, HttpStatus.SC_OK);

            List<Map<String, String>> returnedOrganizations = response.jsonPath().getList(ORGANIZATIONS_PATH_PARAM);

            Assert.assertEquals(returnedOrganizations.size(),
                    Math.min(limit, NUM_OF_ORGANIZATIONS_FOR_PAGINATION_TESTS - offset));

            // Validate no duplicate organization names.
            for (Map<String, String> org : returnedOrganizations) {
                String orgName = org.get(ORGANIZATION_NAME_ATTRIBUTE);
                assertFalse(accumulatedOrganizationNames.contains(orgName),
                        "Duplicate organization found: " + orgName);
                accumulatedOrganizationNames.add(orgName);
            }

            offset += limit;
        }

        // Sort the list based on the numeric part of the organization name.
        accumulatedOrganizationNames.sort(Comparator.comparingInt(s -> Integer.parseInt(s.split("-")[1])));

        // Compare accumulated organization names with the original list (order does not matter).
        validateOrgNamesForOrganizationDiscoveryGet(accumulatedOrganizationNames);
    }

    @DataProvider(name = "organizationDiscoveryPaginationValidationDataProvider")
    public Object[][] organizationDiscoveryPaginationValidationProvider() {

        return new Object[][] {
                {1}, {2}, {5}, {6}, {10}, {17}
        };
    }

    @Test(groups = "organizationDiscoveryPaginationTests", dependsOnMethods = "testAddEmailDomainsToOrganization",
            dataProvider = "organizationDiscoveryPaginationValidationDataProvider")
    public void testGetPaginatedOrganizationsDiscovery(int limit) {

        int offset = 0;
        String nextLink;
        String previousLink;
        String queryUrl = buildQueryUrl(offset, limit);

        List<Map<String, String>> links;
        List<String> forwardAccumulatedOrganizationNames = new ArrayList<>();
        List<String> backwardAccumulatedOrganizationNames = new ArrayList<>();

        // Forward Pagination.
        do {
            links = getPaginationLinksForOrganizationDiscovery(queryUrl, offset, limit,
                    forwardAccumulatedOrganizationNames, true);
            nextLink = getLink(links, LINK_REL_NEXT);
            previousLink = getLink(links, LINK_REL_PREVIOUS);
            queryUrl = buildNewQueryUrl(nextLink, queryUrl);

            validatePaginationLinksForOrganizationDiscovery(offset == 0,
                    offset + limit >= NUM_OF_ORGANIZATIONS_FOR_PAGINATION_TESTS,
                    nextLink, previousLink);

            offset += limit;

        } while (nextLink != null);

        // Backward Pagination.
        do {
            links = getPaginationLinksForOrganizationDiscovery(queryUrl, offset, limit,
                    backwardAccumulatedOrganizationNames, false);
            nextLink = getLink(links, LINK_REL_NEXT);
            previousLink = getLink(links, LINK_REL_PREVIOUS);
            queryUrl = buildNewQueryUrl(previousLink, queryUrl);

            validatePaginationLinksForOrganizationDiscovery(offset == limit,
                    offset >= NUM_OF_ORGANIZATIONS_FOR_PAGINATION_TESTS,
                    nextLink, previousLink);

            offset -= limit;

        } while (previousLink != null);

        forwardAccumulatedOrganizationNames.sort(Comparator.comparingInt(s -> Integer.parseInt(s.split("-")[1])));
        validateOrgNamesForOrganizationDiscoveryGet(forwardAccumulatedOrganizationNames);

        backwardAccumulatedOrganizationNames.sort(Comparator.comparingInt(s -> Integer.parseInt(s.split("-")[1])));
        validateOrgNamesForOrganizationDiscoveryGet(backwardAccumulatedOrganizationNames);
    }

    @DataProvider(name = "organizationDiscoveryPaginationNumericEdgeCasesOfLimitDataProvider")
    public Object[][] organizationDiscoveryPaginationNumericEdgeCasesOfLimitDataProvider() {

        return new Object[][] {
                {0, 0}, {0, 20}, {0, 25},
                {2, 0}, {2, 20}, {2, 25}
        };
    }

    @Test(groups = "organizationDiscoveryPaginationTests", dependsOnMethods = "testAddEmailDomainsToOrganization",
            dataProvider = "organizationDiscoveryPaginationNumericEdgeCasesOfLimitDataProvider")
    public void testGetPaginatedOrganizationsDiscoveryForNumericEdgeCasesOfLimit(int offset, int limit) {

        String queryUrl = buildQueryUrl(offset, limit);

        Response response = getResponseOfGetWithOAuth2(queryUrl, m2mToken);

        validateHttpStatusCode(response, HttpStatus.SC_OK);

        // Validate the response content.
        int actualCount = response.jsonPath().getInt(COUNT_PATH_PARAM);
        int totalResults = response.jsonPath().getInt(TOTAL_RESULTS_PATH_PARAM);
        int startIndex = response.jsonPath().getInt(START_INDEX_PATH_PARAM);
        List<Map<String, String>> links = response.jsonPath().getList(LINKS_PATH_PARAM);
        List<Map<String, String>> returnedOrganizations = response.jsonPath().getList("organizations");

        int expectedCount = Math.min(limit, (NUM_OF_ORGANIZATIONS_FOR_PAGINATION_TESTS - offset));

        Assert.assertEquals(actualCount, expectedCount,
                "Unexpected number of organizations returned for limit: " + limit);
        Assert.assertEquals(totalResults, NUM_OF_ORGANIZATIONS_FOR_PAGINATION_TESTS,
                "Total results should match the number of organizations available.");
        Assert.assertEquals(startIndex, offset + 1,
                "Start index should always be 1 greater than the offset.");

        validateOrganizationDiscoveryLimitEdgeCaseLinks(links, limit, offset);
        validateOrganizationDiscoveryLimitEdgeCaseOrganizations(returnedOrganizations, limit, offset);
    }

    @DataProvider(name = "organizationDiscoveryPaginationNonNumericEdgeCasesOfLimitDataProvider")
    public Object[][] organizationDiscoveryPaginationNonNumericEdgeCasesOfLimitProvider() {

        return new Object[][] {
                {AMPERSAND + LIMIT_QUERY_PARAM + EQUAL},  // Test case 1: URL with LIMIT_QUERY_PARAM but no value.
                {""}  // Test case 2: URL without LIMIT_QUERY_PARAM.
        };
    }

    @Test(groups = "organizationDiscoveryPaginationTests", dependsOnMethods = "testAddEmailDomainsToOrganization",
            dataProvider = "organizationDiscoveryPaginationNonNumericEdgeCasesOfLimitDataProvider")
    public void testGetPaginatedOrganizationsDiscoveryForNonNumericEdgeCasesOfLimit(String limitQueryParam) {

        String endpointURL = ORGANIZATION_MANAGEMENT_API_BASE_PATH + ORGANIZATION_DISCOVERY_API_PATH + QUESTION_MARK +
                FILTER_QUERY_PARAM + EQUAL + AMPERSAND + OFFSET_QUERY_PARAM + EQUAL + ZERO +
                limitQueryParam;

        Response response = getResponseOfGetWithOAuth2(endpointURL, m2mToken);
        validateHttpStatusCode(response, HttpStatus.SC_OK);

        validateResponseForOrganizationDiscoveryLimitDefaultCases(response);
    }

    @DataProvider(name = "organizationDiscoveryOffsetValidationDataProvider")
    public Object[][] organizationDiscoveryOffsetValidationDataProvider() {

        return new Object[][] {
                {0, 1}, {0, 5}, {0, 10},
                {5, 1}, {5, 5}, {5, 10},
                {10, 1}, {10, 5}, {10, 10}
        };
    }

    @Test(groups = "organizationDiscoveryPaginationTests", dependsOnMethods = "testAddEmailDomainsToOrganization",
            dataProvider = "organizationDiscoveryOffsetValidationDataProvider")
    public void testGetPaginatedOrganizationsDiscoveryWithOffset(int offset, int limit) {

        String queryUrl = buildQueryUrl(offset, limit);

        Response response = getResponseOfGetWithOAuth2(queryUrl, m2mToken);

        validateHttpStatusCode(response, HttpStatus.SC_OK);

        // Validate the response content.
        int totalResults = response.jsonPath().getInt(TOTAL_RESULTS_PATH_PARAM);
        int startIndex = response.jsonPath().getInt(START_INDEX_PATH_PARAM);
        int count = response.jsonPath().getInt(COUNT_PATH_PARAM);
        List<Map<String, String>> links = response.jsonPath().getList(LINKS_PATH_PARAM);

        int expectedCount = Math.min(limit, totalResults - offset);

        Assert.assertEquals(totalResults, NUM_OF_ORGANIZATIONS_FOR_PAGINATION_TESTS, TOTAL_RESULT_MISMATCH_ERROR);
        Assert.assertEquals(startIndex, offset + 1, "Start index should be offset + 1.");
        Assert.assertEquals(count, expectedCount, "The count of returned organizations is incorrect.");

        validateOrganizationDiscoveryOffsetLinks(links, limit, offset);
    }

    @DataProvider(name = "numericEdgeCasesOfOffsetAndOffsetWithLimitDataProvider")
    public Object[][] numericEdgeCasesOfOffsetAndOffsetWithLimitDataProvider() {

        return new Object[][] {
                {20, 5},
                {20, 17},
                {20, 20},
                {20, 25},
                {20, 89},
                {25, 5},
                {25, 17},
                {25, 20},
                {25, 25},
                {25, 89}
        };
    }

    @Test(groups = "organizationDiscoveryPaginationTests", dependsOnMethods = "testAddEmailDomainsToOrganization",
            dataProvider = "numericEdgeCasesOfOffsetAndOffsetWithLimitDataProvider")
    public void testGetPaginatedOrganizationsDiscoveryForNumericEdgeCasesOfOffsetAndOffsetWithLimit(int offset,
                                                                                                    int limit) {

        String queryUrl = buildQueryUrl(offset, limit);
        Response response = getResponseOfGetWithOAuth2(queryUrl, m2mToken);

        validateHttpStatusCode(response, HttpStatus.SC_OK);

        int totalResults = response.jsonPath().getInt(TOTAL_RESULTS_PATH_PARAM);
        int startIndex = response.jsonPath().getInt(START_INDEX_PATH_PARAM);
        int count = response.jsonPath().getInt(COUNT_PATH_PARAM);
        List<Map<String, String>> links = response.jsonPath().getList(LINKS_PATH_PARAM);

        // Validate based on the offset and limit.
        Assert.assertEquals(totalResults, NUM_OF_ORGANIZATIONS_FOR_PAGINATION_TESTS, TOTAL_RESULT_MISMATCH_ERROR);
        Assert.assertEquals(startIndex, offset + 1, START_INDEX_MISMATCH_ERROR);
        Assert.assertEquals(count, 0, COUNT_MISMATCH_ERROR);

        // Validate links.
        String nextLink = getLink(links, LINK_REL_NEXT);
        String previousLink = getLink(links, LINK_REL_PREVIOUS);
        Assert.assertNull(nextLink, "Next link should be null.");
        Assert.assertNotNull(previousLink, "Previous link should be present.");

        int expectedOffset = getExpectedOffsetInLinksForOffsetAndLimitEdgeCases(offset, limit);
        validateOrganizationDiscoveryOffsetIsInLinks(previousLink, expectedOffset);

    }

    @Test(groups = "organizationDiscoveryPaginationTests", dependsOnMethods = "testAddEmailDomainsToOrganization")
    public void testGetPaginatedOrganizationsDiscoveryForNonNumericEdgeCasesOfOffsetAndOffsetWithLimit() {

        // Case 1: When offset param is present (limit = 0).
        validatePaginationScenarioWithOffsetAndLimit(0, NUM_OF_ORGANIZATIONS_FOR_PAGINATION_TESTS, 1, 0, true);

        // Case 2: When offset param is present (limit = 5).
        validatePaginationScenarioWithOffsetAndLimit(5, NUM_OF_ORGANIZATIONS_FOR_PAGINATION_TESTS, 1, 5, true);

        // Case 3: When offset param is present (limit = NUM_OF_ORGANIZATIONS_FOR_PAGINATION_TESTS).
        validatePaginationScenarioWithOffsetAndLimit(NUM_OF_ORGANIZATIONS_FOR_PAGINATION_TESTS,
                NUM_OF_ORGANIZATIONS_FOR_PAGINATION_TESTS, 1, NUM_OF_ORGANIZATIONS_FOR_PAGINATION_TESTS, false);

        // Case 4: When offset param is present (limit > NUM_OF_ORGANIZATIONS_FOR_PAGINATION_TESTS).
        validatePaginationScenarioWithOffsetAndLimit(25, NUM_OF_ORGANIZATIONS_FOR_PAGINATION_TESTS, 1,
                NUM_OF_ORGANIZATIONS_FOR_PAGINATION_TESTS, false);

        // Case 5: When offset param is not present (limit = 0).
        validatePaginationScenarioWithLimitOnly(0, NUM_OF_ORGANIZATIONS_FOR_PAGINATION_TESTS, 1, 0, true);

        // Case 6: When offset param is not present (limit = 5).
        validatePaginationScenarioWithLimitOnly(5, NUM_OF_ORGANIZATIONS_FOR_PAGINATION_TESTS, 1, 5, true);

        // Case 7: When offset param is not present (limit = NUM_OF_ORGANIZATIONS_FOR_PAGINATION_TESTS).
        validatePaginationScenarioWithLimitOnly(NUM_OF_ORGANIZATIONS_FOR_PAGINATION_TESTS,
                NUM_OF_ORGANIZATIONS_FOR_PAGINATION_TESTS, 1, NUM_OF_ORGANIZATIONS_FOR_PAGINATION_TESTS, false);

        // Case 8: When offset param is not present (limit > NUM_OF_ORGANIZATIONS_FOR_PAGINATION_TESTS).
        validatePaginationScenarioWithLimitOnly(25, NUM_OF_ORGANIZATIONS_FOR_PAGINATION_TESTS, 1,
                NUM_OF_ORGANIZATIONS_FOR_PAGINATION_TESTS, false);

        // Case 9: Offset= and limit= are equal to no value.
        validatePaginationScenarioWithOffsetAndLimitAndDefaultLimit();

        // Case 10: Offset is not present and limit is not present.
        validatePaginationScenarioWithNoOffsetAndLimit();
    }

    @Test(groups = "organizationDiscoveryPaginationTests", dependsOnMethods = "testEnableEmailDomainDiscovery")
    public void testDisableEmailDomainDiscovery() {

        String emailDomainIsEnabled = "properties.find { it.key == 'emailDomain.enable' }?.value ?: false";

        // Send DELETE request to disable email domain discovery.
        Response response = getResponseOfDeleteWithOAuth2(
                ORGANIZATION_CONFIGS_API_BASE_PATH + ORGANIZATION_DISCOVERY_API_PATH,
                m2mToken);

        validateHttpStatusCode(response, HttpStatus.SC_NO_CONTENT);

        // Send a GET request to validate that the email domain discovery is disabled.
        Response getResponse = getResponseOfGetWithOAuth2(
                ORGANIZATION_CONFIGS_API_BASE_PATH + ORGANIZATION_DISCOVERY_API_PATH,
                m2mToken);

        boolean isEnabled = getResponse.jsonPath().getBoolean(emailDomainIsEnabled);
        Assert.assertFalse(isEnabled, "Email domain discovery was not successfully disabled.");
    }

    @Test(groups = "organizationMetaAttributesPaginationTests",
            dependsOnGroups = "organizationDiscoveryPaginationTests")
    public void testAddMetaAttributesToOrganizations() {

        // Initialize meta attributes in sorted order.
        String[] attributes = {"1", "2", "3", ":", "@", "A", "B", "C", "LMN", "PQR", "STU", "a", "b", "c", "fg", "jKL",
                "mNo", "x", "y", "z"};

        metaAttributes = new ArrayList<>(Arrays.asList(attributes));
        List<Map<String, String>> addedAttributes = new ArrayList<>();
        List<String> shuffledAttributes = new ArrayList<>(Arrays.asList(attributes));
        Collections.shuffle(shuffledAttributes); // Randomize attribute distribution.

        int remainingAttributes = attributes.length;

        for (int i = 0; i < NUM_OF_ORGANIZATIONS_WITH_META_ATTRIBUTES; i++) {
            String organizationId = organizations.get(i).get(ORGANIZATION_ID);

            int attributesToAssign = calculateAttributesToAssign(remainingAttributes, i);

            for (int j = 0; j < attributesToAssign; j++) {
                String attribute = shuffledAttributes.remove(0);
                String requestBody = createMetaAttributeCreationPatchRequestBody(attribute);
                String endpointURL = buildOrganizationApiEndpoint(organizationId);

                Response response = getResponseOfPatch(endpointURL, requestBody);
                validateHttpStatusCode(response, HttpStatus.SC_OK);

                addedAttributes.add(createAttributeMap(attribute));
                remainingAttributes--;
            }
        }

        validateAttributesAddedSuccessfully(addedAttributes);
    }

    @DataProvider(name = "metaAttributesLimitValidationDataProvider")
    public Object[][] metaAttributesLimitValidationDataProvider() {

        return new Object[][] {
                {1}, {2}, {3}, {5}, {10}, {13}
        };
    }

    @Test(groups = "organizationMetaAttributesPaginationTests",
            dependsOnMethods = "testAddMetaAttributesToOrganizations",
            dataProvider = "metaAttributesLimitValidationDataProvider")
    public void testGetPaginatedMetaAttributesWithLimit(int limit) {

        String endpointURL = ORGANIZATION_MANAGEMENT_API_BASE_PATH + ORGANIZATION_META_ATTRIBUTES_API_PATH +
                QUESTION_MARK + LIMIT_QUERY_PARAM + EQUAL + limit + AMPERSAND + RECURSIVE_QUERY_PARAM + EQUAL + false;
        Response response = getResponseOfGetWithOAuth2(endpointURL, m2mToken);

        validateHttpStatusCode(response, HttpStatus.SC_OK);
        List<String> attributes = response.jsonPath().getList(ORGANIZATION_MULTIPLE_META_ATTRIBUTE_ATTRIBUTES);
        Assert.assertEquals(attributes.size(), limit);

        // Validate the order of the returned attributes.
        for (int i = 0; i < limit; i++) {
            Assert.assertEquals(attributes.get(i), metaAttributes.get(i));
        }
    }

    @Test(groups = "organizationMetaAttributesPaginationTests",
            dependsOnMethods = "testGetMetaAttributesPaginationForNumericEdgeCasesOfLimit",
            dataProvider = "metaAttributesLimitValidationDataProvider")
    public void testGetPaginatedMetaAttributes(int limit) {

        String after = null;
        String before = null;

        // Forward Pagination.
        int startIndex = 0;
        do {
            String endpointURL = getMetaAttributesEndpoint(true, limit, after, before);
            Response response = getResponseOfGetWithOAuth2(endpointURL, m2mToken);
            validateHttpStatusCode(response, HttpStatus.SC_OK);

            List<String> returnedMetaAttributes =
                    response.jsonPath().getList(ORGANIZATION_MULTIPLE_META_ATTRIBUTE_ATTRIBUTES);
            List<String> expectedMetaAttributes =
                    metaAttributes.subList(startIndex, startIndex + returnedMetaAttributes.size());

            after = getLink(response.jsonPath().getList(LINKS_PATH_PARAM), LINK_REL_NEXT);
            before = getLink(response.jsonPath().getList(LINKS_PATH_PARAM), LINK_REL_PREVIOUS);

            Assert.assertEquals(returnedMetaAttributes, expectedMetaAttributes,
                    "Attributes in the response do not match the expected order.");
            validatePaginationLinksForOrganizationDiscovery(before == null, after == null, after, before);
            validateReturnedMetaAttributesOrder(startIndex, returnedMetaAttributes);

            startIndex += limit;
        } while (after != null);

        // Reset the start index for reverse validation.
        startIndex -= 2 * limit;

        // Backward Pagination.
        do {
            String endpointURL = getMetaAttributesEndpoint(false, limit, after, before);
            Response response = getResponseOfGetWithOAuth2(endpointURL, m2mToken);
            validateHttpStatusCode(response, HttpStatus.SC_OK);

            List<String> returnedMetaAttributes =
                    response.jsonPath().getList(ORGANIZATION_MULTIPLE_META_ATTRIBUTE_ATTRIBUTES);
            List<String> expectedMetaAttributes =
                    metaAttributes.subList(startIndex, startIndex + returnedMetaAttributes.size());

            after = getLink(response.jsonPath().getList(LINKS_PATH_PARAM), LINK_REL_NEXT);
            before = getLink(response.jsonPath().getList(LINKS_PATH_PARAM), LINK_REL_PREVIOUS);

            Assert.assertEquals(returnedMetaAttributes, expectedMetaAttributes,
                    "Attributes in the response do not match the expected order.");
            validatePaginationLinksForOrganizationDiscovery(before == null, after == null, after, before);
            validateReturnedMetaAttributesOrder(startIndex, returnedMetaAttributes);

            startIndex -= limit;
        } while (before != null);
    }

    @DataProvider(name = "metaAttributesPaginationNumericEdgeCasesOfLimitDataProvider")
    public Object[][] metaAttributesPaginationNumericEdgeCasesOfLimitDataProvider() {

        return new Object[][] {
                {0}, {20}, {25}
        };
    }

    @Test(groups = "organizationMetaAttributesPaginationTests", dependsOnMethods = "testGetPaginatedMetaAttributesWithLimit",
            dataProvider = "metaAttributesPaginationNumericEdgeCasesOfLimitDataProvider")
    public void testGetMetaAttributesPaginationForNumericEdgeCasesOfLimit(int limit) {

        String endpointURL = ORGANIZATION_MANAGEMENT_API_BASE_PATH + ORGANIZATION_META_ATTRIBUTES_API_PATH +
                QUESTION_MARK + LIMIT_QUERY_PARAM + EQUAL + limit + AMPERSAND + RECURSIVE_QUERY_PARAM + EQUAL + false;
        Response response = getResponseOfGetWithOAuth2(endpointURL, m2mToken);
        validateHttpStatusCode(response, HttpStatus.SC_OK);

        if (limit == 0) {
            Assert.assertNull(response.jsonPath().getList(ORGANIZATION_MULTIPLE_META_ATTRIBUTE_ATTRIBUTES));
        } else {
            List<String> attributes = response.jsonPath().getList(ORGANIZATION_MULTIPLE_META_ATTRIBUTE_ATTRIBUTES);
            Assert.assertEquals(attributes.size(), metaAttributes.size());
        }
    }

    @DataProvider(name = "metaAttributesPaginationNonNumericEdgeCasesOfLimitProvider")
    public Object[][] metaAttributesPaginationNonNumericEdgeCasesOfLimitProvider() {

        return new Object[][] {
                {LIMIT_QUERY_PARAM + EQUAL},  // Case with limit= (no value), default limit is 15.
                {""}  // Case with no limit parameter, default limit is 15.
        };
    }

    @Test(groups = "organizationMetaAttributesPaginationTests", dependsOnMethods = "testGetPaginatedMetaAttributesWithLimit",
            dataProvider = "metaAttributesPaginationNonNumericEdgeCasesOfLimitProvider")
    public void testGetMetaAttributesForNonNumericEdgeCasesOfLimit(String limitQueryParam) {

        String endpointURL = ORGANIZATION_MANAGEMENT_API_BASE_PATH + ORGANIZATION_META_ATTRIBUTES_API_PATH +
                QUESTION_MARK + RECURSIVE_QUERY_PARAM + EQUAL + false + AMPERSAND + limitQueryParam;
        Response response = getResponseOfGetWithOAuth2(endpointURL, m2mToken);
        validateHttpStatusCode(response, HttpStatus.SC_OK);

        List<String> attributes = response.jsonPath().getList(ORGANIZATION_MULTIPLE_META_ATTRIBUTE_ATTRIBUTES);

        if (metaAttributes.size() <= DEFAULT_META_ATTRIBUTES_LIMIT) {
            Assert.assertEquals(attributes.size(), metaAttributes.size());
        } else {
            Assert.assertEquals(attributes.size(), DEFAULT_META_ATTRIBUTES_LIMIT);
            validateNextLinkBasedOnMetaAttributeCount(response);
        }
    }

    @Test(dependsOnGroups = "organizationMetaAttributesPaginationTests")
    public void testDeleteOrganizationsForPagination() {

        for (Map<String, String> org : new ArrayList<>(organizations)) {
            deleteSingleOrganization(org);
        }

        Assert.assertTrue(organizations.isEmpty(), "All organizations should be deleted, but the list is not empty.");
    }

    private void validateNextLink(Response response, boolean expectNextLink) {

        List<Map<String, String>> links = response.jsonPath().getList(LINKS_PATH_PARAM);
        String nextLink = getLink(links, LINK_REL_NEXT);
        if (expectNextLink) {
            Assert.assertNotNull(nextLink, "Next link should be present.");
        } else {
            Assert.assertNull(nextLink, "Next link should not be present.");
        }
    }

    private void validateNextLinkBasedOnOrganizationCount(Response response) {

        List<Map<String, String>> links = response.jsonPath().getList(LINKS_PATH_PARAM);
        if (NUM_OF_ORGANIZATIONS_FOR_PAGINATION_TESTS > DEFAULT_ORG_LIMIT) {
            Assert.assertNotNull(getLink(links, LINK_REL_NEXT),
                    "'next' link should be present when organizations exceed default limit.");
        } else {
            Assert.assertNull(getLink(links, LINK_REL_NEXT),
                    "'next' link should not be present when organizations are within default limit.");
        }
    }

    private void validateOrganizationsOnPage(Response response, int pageNum, int totalOrganizations, int limit) {

        // Validate the organization count.
        int expectedOrgCount = Math.min(limit, totalOrganizations - (pageNum - 1) * limit);
        List<Map<String, String>> actualOrganizations = response.jsonPath().getList(ORGANIZATIONS_PATH_PARAM);
        int actualOrgCount = (actualOrganizations != null) ? actualOrganizations.size() : 0;

        Assert.assertEquals(actualOrgCount, expectedOrgCount,
                "Organization count mismatch on page " + pageNum);

        // Validate the organization names.
        List<String> actualOrgNames = response.jsonPath().getList(ORGANIZATIONS_PATH_PARAM + ".name");

        for (int i = 0; i < expectedOrgCount; i++) {
            int orgIndex = totalOrganizations - ((pageNum - 1) * limit) - i - 1;

            String expectedOrgName = String.format(ORGANIZATION_NAME_FORMAT, orgIndex);
            Assert.assertEquals(actualOrgNames.get(i), expectedOrgName,
                    "Organization name mismatch on page " + pageNum + " at index " + i);
        }
    }

    private void validateOrganizationsForDefaultLimit(Response response, int totalOrganizations) {

        // Validate the organization count.
        int expectedOrgCount = Math.min(DEFAULT_ORG_LIMIT, totalOrganizations);
        List<Map<String, String>> actualOrganizations = response.jsonPath().getList(ORGANIZATIONS_PATH_PARAM);
        int actualOrgCount = (actualOrganizations != null) ? actualOrganizations.size() : 0;

        Assert.assertEquals(actualOrgCount, expectedOrgCount,
                "Organization count mismatch with default limit.");

        // Validate the organization names.
        List<String> actualOrgNames = response.jsonPath().getList(ORGANIZATIONS_PATH_PARAM + ".name");

        for (int i = 0; i < expectedOrgCount; i++) {
            int orgIndex = totalOrganizations - i - 1;

            String expectedOrgName = String.format(ORGANIZATION_NAME_FORMAT, orgIndex);
            Assert.assertEquals(actualOrgNames.get(i), expectedOrgName,
                    "Organization name mismatch with default limit at index " + i);
        }

        // Validate pagination links.
        List<Map<String, String>> links = response.jsonPath().getList(LINKS_PATH_PARAM);

        if (totalOrganizations > DEFAULT_ORG_LIMIT) {
            String after = getLink(links, LINK_REL_NEXT);
            Assert.assertNotNull(after,
                    "'after' link should be present when organizations exceed default limit.");
        } else {
            Assert.assertNull(getLink(links, LINK_REL_NEXT),
                    "'after' link should not be present when organizations are within default limit.");
        }
    }

    private void validateOrgNamesForOrganizationDiscoveryGet(List<String> accumulatedOrganizationNames) {

        // Ensure both sets contain the same organization names.
        for (int i = 0; i < NUM_OF_ORGANIZATIONS_FOR_PAGINATION_TESTS; i++) {
            assertEquals(accumulatedOrganizationNames.get(i), organizations.get(i).get(ORGANIZATION_NAME),
                    "Organization names do not match.");
        }
    }

    private void validatePaginationLinksForOrganizationDiscovery(boolean isFirstPage, boolean isLastPage,
                                                                 String nextLink,
                                                                 String previousLink) {

        if (isFirstPage) {
            Assert.assertNotNull(nextLink, "Next link should be available on the first page.");
            Assert.assertNull(previousLink, "Previous link should be null on the first page.");
        } else if (isLastPage) {
            Assert.assertNull(nextLink, "Next link should be null on the last page.");
            Assert.assertNotNull(previousLink, "Previous link should be available on the last page.");
        } else {
            Assert.assertNotNull(nextLink, "Next link should be available on middle pages.");
            Assert.assertNotNull(previousLink, "Previous link should be available on middle pages.");
        }
    }

    private void validateOrganizationDiscoveryLimitEdgeCaseLinks(List<Map<String, String>> links, int limit,
                                                                 int offset) {

        if (limit == 0) {
            if (offset == 0) {
                Assert.assertNotNull(getLink(links, LINK_REL_NEXT),
                        "'next' link should be present when the limit and offset is 0.");
            } else {
                Assert.assertNotNull(getLink(links, LINK_REL_NEXT),
                        "'next' link should be present when the limit is 0 but the offset is non-zero.");
                Assert.assertNotNull(getLink(links, LINK_REL_PREVIOUS),
                        "'previous' link should be present when the limit is 0 but the offset is non-zero.");
            }
        } else {
            if (offset == 0) {
                Assert.assertTrue(links.isEmpty(),
                        "'links' should be empty for non-zero edge case limits.");
            } else {
                Assert.assertNotNull(getLink(links, LINK_REL_PREVIOUS),
                        "'previous' link should be present for non-zero edge case limits with non-zero offset.");
            }
        }

    }

    private void validateOrganizationDiscoveryLimitEdgeCaseOrganizations(List<Map<String, String>> organizations,
                                                                         int limit, int offset) {

        if (limit == 0) {
            Assert.assertNull(organizations,
                    "No organizations should be returned when limit is 0.");
        } else {
            Assert.assertEquals(organizations.size(), Math.min(limit,
                            (NUM_OF_ORGANIZATIONS_FOR_PAGINATION_TESTS - offset)),
                    "Number of organizations in the response does not match the expected count.");

            // Validation to ensure correct organization data is returned - Only for non-zero offset.
            /* Since, the organizations are ordered in ORG_ID, we cannot validate the OrganizationName results
            for non-zero offsets.*/
            if (offset == 0) {
                validateOrgNamesOfOrganizationDiscoveryGet(organizations);
            }
        }
    }

    private void validateOrgNamesOfOrganizationDiscoveryGet(List<Map<String, String>> organizations) {

        List<String> accumulatedOrganizationNames = new ArrayList<>();
        for (Map<String, String> org : organizations) {
            String orgName = org.get(ORGANIZATION_NAME_ATTRIBUTE);
            accumulatedOrganizationNames.add(orgName);
        }
        accumulatedOrganizationNames.sort(Comparator.comparingInt(s -> Integer.parseInt(s.split("-")[1])));

        validateOrgNamesForOrganizationDiscoveryGet(accumulatedOrganizationNames);
    }

    private void validateResponseForOrganizationDiscoveryLimitDefaultCases(Response response) {

        int actualCount = response.jsonPath().getInt(COUNT_PATH_PARAM);
        int totalResults = response.jsonPath().getInt(TOTAL_RESULTS_PATH_PARAM);
        int startIndex = response.jsonPath().getInt(START_INDEX_PATH_PARAM);
        List<Map<String, String>> links = response.jsonPath().getList(LINKS_PATH_PARAM);
        List<Map<String, String>> returnedOrganizations = response.jsonPath().getList("organizations");

        Assert.assertEquals(actualCount, DEFAULT_ORG_LIMIT, "Unexpected count of organizations returned.");
        Assert.assertEquals(totalResults, NUM_OF_ORGANIZATIONS_FOR_PAGINATION_TESTS, "Unexpected total results.");
        Assert.assertEquals(startIndex, 1, "Start index should be 1.");

        if (totalResults > DEFAULT_ORG_LIMIT) {
            Assert.assertNotNull(getLink(links, LINK_REL_NEXT), "'next' link should be present.");
        } else {
            Assert.assertTrue(links.isEmpty(), "'links' should be empty for non-zero edge case limits.");
        }

        Assert.assertEquals(returnedOrganizations.size(),
                Math.min(DEFAULT_ORG_LIMIT, NUM_OF_ORGANIZATIONS_FOR_PAGINATION_TESTS),
                "Number of organizations in the response does not match the expected count.");
    }

    private void validateOrganizationDiscoveryOffsetLinks(List<Map<String, String>> links, int limit,
                                                          int offset) {

        String nextLink = getLink(links, LINK_REL_NEXT);
        if (offset + limit < NUM_OF_ORGANIZATIONS_FOR_PAGINATION_TESTS) {
            Assert.assertNotNull(nextLink, "The 'next' link should be present in first/middle pages.");
            int expectedOffset = offset + limit;
            validateOrganizationDiscoveryOffsetIsInLinks(nextLink, expectedOffset);
        } else {
            Assert.assertNull(nextLink, "The 'next' link should not be present in the last page.");
        }

        String previousLink = getLink(links, LINK_REL_PREVIOUS);
        if (offset > 0) {
            Assert.assertNotNull(previousLink, "The 'previous' link should be present in last/middle pages.");
            int expectedOffset = Math.max((offset - limit), 0);
            validateOrganizationDiscoveryOffsetIsInLinks(previousLink, expectedOffset);
        } else {
            Assert.assertNull(previousLink, "The 'previous' link should not be present in the first page.");
        }
    }

    private void validateOrganizationDiscoveryOffsetIsInLinks(String link, int expectedOffset) {

        int offsetStartIndex = link.indexOf(OFFSET_QUERY_PARAM + EQUAL);

        if (offsetStartIndex != -1) {
            offsetStartIndex += (OFFSET_QUERY_PARAM + EQUAL).length();
            int offsetEndIndex = link.indexOf(AMPERSAND, offsetStartIndex);

            if (offsetEndIndex == -1) offsetEndIndex = link.length();

            int actualOffset = Integer.parseInt(link.substring(offsetStartIndex, offsetEndIndex));

            Assert.assertEquals(actualOffset, expectedOffset, "Offset in the link is incorrect.");
        } else {
            Assert.fail("Offset parameter is missing in the link.");
        }
    }

    private void validatePaginationScenarioWithOffsetAndLimit(int limit, int expectedTotalResults,
                                                              int expectedStartIndex, int expectedCount,
                                                              boolean expectNextLink) {

        String queryUrl = buildQueryUrlWithOffsetAndLimit(limit);
        Response response = getResponseOfGetWithOAuth2(queryUrl, m2mToken);

        validateCommonAssertions(response, expectedTotalResults, expectedStartIndex, expectedCount);
        validateNextLink(response, expectNextLink);
    }

    private void validatePaginationScenarioWithLimitOnly(int limit, int expectedTotalResults, int expectedStartIndex,
                                                         int expectedCount, boolean expectNextLink) {

        String queryUrl = buildQueryUrlWithLimit(limit);
        Response response = getResponseOfGetWithOAuth2(queryUrl, m2mToken);

        validateCommonAssertions(response, expectedTotalResults, expectedStartIndex, expectedCount);
        validateNextLink(response, expectNextLink);
    }

    private void validatePaginationScenarioWithOffsetAndLimitAndDefaultLimit() {

        String queryUrl = buildQueryUrlWithOffsetAndLimitAndDefaultLimit();
        Response response = getResponseOfGetWithOAuth2(queryUrl, m2mToken);

        validateCommonAssertions(response, NUM_OF_ORGANIZATIONS_FOR_PAGINATION_TESTS, 1,
                Math.min(DEFAULT_ORG_LIMIT, NUM_OF_ORGANIZATIONS_FOR_PAGINATION_TESTS));
        validateNextLinkBasedOnOrganizationCount(response);
    }

    private void validatePaginationScenarioWithNoOffsetAndLimit() {

        String queryUrl = buildQueryUrlWithNoOffsetAndLimit();
        Response response = getResponseOfGetWithOAuth2(queryUrl, m2mToken);

        validateCommonAssertions(response, NUM_OF_ORGANIZATIONS_FOR_PAGINATION_TESTS, 1,
                Math.min(DEFAULT_ORG_LIMIT, NUM_OF_ORGANIZATIONS_FOR_PAGINATION_TESTS));
        validateNextLinkBasedOnOrganizationCount(response);
    }

    private void validateCommonAssertions(Response response, int expectedTotalResults, int expectedStartIndex,
                                          int expectedCount) {

        validateHttpStatusCode(response, HttpStatus.SC_OK);

        Assert.assertEquals(response.jsonPath().getInt(TOTAL_RESULTS_PATH_PARAM), expectedTotalResults,
                TOTAL_RESULT_MISMATCH_ERROR);
        Assert.assertEquals(response.jsonPath().getInt(START_INDEX_PATH_PARAM), expectedStartIndex,
                START_INDEX_MISMATCH_ERROR);
        Assert.assertEquals(response.jsonPath().getInt(COUNT_PATH_PARAM), expectedCount,
                COUNT_MISMATCH_ERROR);
    }

    private void validateAttributesAddedSuccessfully(List<Map<String, String>> addedAttributes) {

        Assert.assertEquals(metaAttributes.size(), addedAttributes.size(),
                "Meta attributes were not added successfully.");
    }

    private void validateNextLinkBasedOnMetaAttributeCount(Response response) {

        List<Map<String, String>> links = response.jsonPath().getList(LINKS_PATH_PARAM);
        if (metaAttributes.size() > DEFAULT_META_ATTRIBUTES_LIMIT) {
            Assert.assertNotNull(getLink(links, LINK_REL_NEXT),
                    "'next' link should be present when meta attributes exceed the default limit of 15.");
        } else {
            Assert.assertNull(links,
                    "'links' should not be present when meta attributes are within the default limit.");
        }
    }

    private void validateReturnedMetaAttributesOrder(int startIndex, List<String> attributes) {

        for (int i = 0; i < attributes.size(); i++) {
            String expectedAttribute = metaAttributes.get(startIndex + i);
            Assert.assertEquals(attributes.get(i), expectedAttribute,
                    "The attribute at index " + i + " does not match the expected value.");
        }
    }

    private String getLink(List<Map<String, String>> links, String rel) {

        for (Map<String, String> link : links) {
            if (rel.equals(link.get(REL))) {
                String href = link.get(HREF);
                if (href.contains(AFTER_QUERY_PARAM + EQUAL)) {
                    return href.substring(href.indexOf(AFTER_QUERY_PARAM + EQUAL) + AFTER_QUERY_PARAM.length() + 1);
                } else if (href.contains(BEFORE_QUERY_PARAM + EQUAL)) {
                    return href.substring(href.indexOf(BEFORE_QUERY_PARAM + EQUAL) + BEFORE_QUERY_PARAM.length() + 1);
                } else {
                    return href;
                }
            }
        }
        return null;
    }

    private List<Map<String, String>> getPaginationLinksForOrganizationDiscovery(
            String queryUrl, int offset, int limit, List<String> accumulatedOrganizationNames, boolean isForward) {

        Response response = getResponseOfGetWithOAuth2(queryUrl, m2mToken);
        validateHttpStatusCode(response, HttpStatus.SC_OK);

        List<Map<String, String>> returnedOrganizations = response.jsonPath().getList(ORGANIZATIONS_PATH_PARAM);

        int expectedSize = isForward
                ? Math.min(limit, NUM_OF_ORGANIZATIONS_FOR_PAGINATION_TESTS - offset)
                : Math.min(limit, NUM_OF_ORGANIZATIONS_FOR_PAGINATION_TESTS - offset + limit);

        Assert.assertEquals(returnedOrganizations.size(), expectedSize);

        addReturnedOrganizationsToList(returnedOrganizations, accumulatedOrganizationNames);

        return response.jsonPath().getList(LINKS_PATH_PARAM);
    }

    private int getExpectedOffsetInLinksForOffsetAndLimitEdgeCases(int offset, int limit) {

        if (offset == NUM_OF_ORGANIZATIONS_FOR_PAGINATION_TESTS) {
            return Math.max(0, offset - limit);
        } else if (offset > NUM_OF_ORGANIZATIONS_FOR_PAGINATION_TESTS) {
            int left = offset - limit;
            while (left >= NUM_OF_ORGANIZATIONS_FOR_PAGINATION_TESTS) left -= limit;

            return Math.max(0, left);
        } else {
            return 0;
        }
    }

    private void addEmailDomainsToOrganization(String organizationId, String... domains) {

        String addDomainsPayload = String.format(
                "{" +
                        "\"attributes\": [{" +
                        "\"type\": \"emailDomain\"," +
                        "\"values\": [\"%s\"]" +
                        "}]," +
                        "\"organizationId\": \"%s\"" +
                        "}",
                String.join("\",\"", domains),
                organizationId);

        Response response =
                getResponseOfPostWithOAuth2(ORGANIZATION_MANAGEMENT_API_BASE_PATH +
                        ORGANIZATION_DISCOVERY_API_PATH, addDomainsPayload, m2mToken);
        validateHttpStatusCode(response, HttpStatus.SC_CREATED);
    }

    private String getMetaAttributesEndpoint(boolean isForward, int limit, String after, String before) {

        String baseEndpoint = ORGANIZATION_MANAGEMENT_API_BASE_PATH + ORGANIZATION_META_ATTRIBUTES_API_PATH +
                QUESTION_MARK + LIMIT_QUERY_PARAM + EQUAL + limit +
                AMPERSAND + RECURSIVE_QUERY_PARAM + EQUAL + false;

        if (isForward) {
            return baseEndpoint + (after != null ? AMPERSAND + AFTER_QUERY_PARAM + EQUAL + after : "");
        } else {
            return baseEndpoint + (before != null ? AMPERSAND + BEFORE_QUERY_PARAM + EQUAL + before : "");
        }
    }

    private void addReturnedOrganizationsToList(List<Map<String, String>> returnedOrganizations,
                                                List<String> accumulatedOrganizationNames) {

        for (Map<String, String> org : returnedOrganizations) {
            accumulatedOrganizationNames.add(org.get(ORGANIZATION_NAME_ATTRIBUTE));
        }
    }

    private List<Map<String, String>> createOrganizations(int numberOfOrganizations) throws JSONException {

        List<Map<String, String>> newOrganizations = new ArrayList<>();

        for (int i = 0; i < numberOfOrganizations; i++) {
            JSONObject body = new JSONObject()
                    .put(ORGANIZATION_NAME, String.format(ORGANIZATION_NAME_FORMAT, i));

            Response response =
                    getResponseOfPostWithOAuth2(ORGANIZATION_MANAGEMENT_API_BASE_PATH, body.toString(), m2mToken);

            if (response.getStatusCode() == HttpStatus.SC_CREATED) {
                // Extract the organization ID (UUID) from the response body.
                JSONObject responseBody = new JSONObject(response.getBody().asString());
                String organizationId = responseBody.getString("id");

                // Store the created organization details.
                Map<String, String> org = new HashMap<>();
                org.put(ORGANIZATION_NAME, String.format(ORGANIZATION_NAME_FORMAT, i));
                org.put(ORGANIZATION_ID, organizationId);
                newOrganizations.add(org);
            } else {
                throw new RuntimeException("Failed to create organization " + i);
            }
        }

        return newOrganizations;
    }

    private String createMetaAttributeCreationPatchRequestBody(String attribute) {

        return String.format("[{\"operation\":\"ADD\",\"path\":\"/attributes/%s\",\"value\":\"value-%s\"}]",
                attribute, attribute);
    }

    private Map<String, String> createAttributeMap(String attribute) {

        Map<String, String> attrMap = new HashMap<>();
        attrMap.put(attribute, "value-" + attribute);
        return attrMap;
    }

    private void deleteSingleOrganization(Map<String, String> org) {

        String organizationId = org.get(ORGANIZATION_ID);
        String deleteEndpointURL = ORGANIZATION_MANAGEMENT_API_BASE_PATH + PATH_SEPARATOR + organizationId;

        Response response = getResponseOfDelete(deleteEndpointURL);

        validateHttpStatusCode(response, HttpStatus.SC_NO_CONTENT);

        // Remove the organization from the list after successful deletion.
        organizations.remove(org);
    }

    private int calculateAttributesToAssign(int remainingAttributes, int organizationIndex) {

        return Math.min(remainingAttributes,
                remainingAttributes / (NUM_OF_ORGANIZATIONS_WITH_META_ATTRIBUTES - organizationIndex));
    }

    private String buildQueryUrl(int offset, int limit) {

        return ORGANIZATION_MANAGEMENT_API_BASE_PATH + ORGANIZATION_DISCOVERY_API_PATH + QUESTION_MARK +
                OFFSET_QUERY_PARAM + EQUAL + offset + AMPERSAND + LIMIT_QUERY_PARAM + EQUAL + limit;
    }

    private String buildNewQueryUrl(String link, String queryUrl) {

        return link != null ?
                link.substring(link.lastIndexOf(
                        ORGANIZATION_MANAGEMENT_API_BASE_PATH + ORGANIZATION_DISCOVERY_API_PATH)) : queryUrl;
    }

    private String buildQueryUrlWithOffsetAndLimit(int limit) {

        return ORGANIZATION_MANAGEMENT_API_BASE_PATH + ORGANIZATION_DISCOVERY_API_PATH + QUESTION_MARK +
                OFFSET_QUERY_PARAM + EQUAL + AMPERSAND + LIMIT_QUERY_PARAM + EQUAL + limit;
    }

    private String buildQueryUrlWithLimit(int limit) {

        return ORGANIZATION_MANAGEMENT_API_BASE_PATH + ORGANIZATION_DISCOVERY_API_PATH + QUESTION_MARK +
                LIMIT_QUERY_PARAM + EQUAL + limit;
    }

    private String buildQueryUrlWithOffsetAndLimitAndDefaultLimit() {

        return ORGANIZATION_MANAGEMENT_API_BASE_PATH + ORGANIZATION_DISCOVERY_API_PATH + QUESTION_MARK +
                OFFSET_QUERY_PARAM + EQUAL + AMPERSAND + LIMIT_QUERY_PARAM + EQUAL;
    }

    private String buildQueryUrlWithNoOffsetAndLimit() {

        return ORGANIZATION_MANAGEMENT_API_BASE_PATH + ORGANIZATION_DISCOVERY_API_PATH + QUESTION_MARK +
                FILTER_QUERY_PARAM + EQUAL;
    }

    private String buildOrganizationApiEndpoint(String organizationId) {

        return String.format("%s/%s", ORGANIZATION_MANAGEMENT_API_BASE_PATH, organizationId);
    }

    private String sendAuthorizationRequest(boolean addOrgDiscoveryType) throws Exception {

        List<NameValuePair> queryParams = new ArrayList<>();
        queryParams.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_SCOPE,
                OAuth2Constant.OAUTH2_SCOPE_OPENID + PLUS + OAuth2Constant.OAUTH2_SCOPE_EMAIL));
        queryParams.add(
                new BasicNameValuePair(OAuth2Constant.OAUTH2_RESPONSE_TYPE, OAuth2Constant.AUTHORIZATION_CODE_NAME));
        queryParams.add(new BasicNameValuePair(OAuth2Constant.REDIRECT_URI_NAME, OAuth2Constant.CALLBACK_URL));
        queryParams.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_CLIENT_ID, b2bAppClientId));
        queryParams.add(new BasicNameValuePair(LOGIN_HINT_QUERY_PARAM, B2B_USER_EMAIL));
        queryParams.add(new BasicNameValuePair(FIDP_QUERY_PARAM, ORGANIZATION_SSO));
        if (addOrgDiscoveryType) {
            queryParams.add(new BasicNameValuePair(ORG_DISCOVERY_TYPE_QUERY_PARAM, EMAIL_DOMAIN_DISCOVERY));
        }

        String endpointURL = buildGetRequestURL(serverURL + AUTHORIZE_ENDPOINT, tenant, queryParams);

        HttpResponse authorizeResponse = sendGetRequest(endpointURL, httpClientWithoutAutoRedirections);
        Assert.assertNotNull(authorizeResponse, "Authorize response is null.");
        Assert.assertEquals(authorizeResponse.getStatusLine().getStatusCode(), HttpStatus.SC_MOVED_TEMPORARILY,
                "Authorize response status code is invalid.");
        Header authorizeLocationHeader = authorizeResponse.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        Assert.assertNotNull(authorizeLocationHeader, "Authorize response header location is null.");
        EntityUtils.consume(authorizeResponse.getEntity());

        HttpResponse authorizeRedirectResponse =
                sendGetRequest(authorizeLocationHeader.getValue(), httpClientWithoutAutoRedirections);
        Assert.assertNotNull(authorizeRedirectResponse, "Redirected authorize response is null.");
        Assert.assertEquals(authorizeRedirectResponse.getStatusLine().getStatusCode(), HttpStatus.SC_MOVED_TEMPORARILY,
                "Redirected authorize response status code is invalid.");
        Header authorizeRedirectLocationHeader =
                authorizeRedirectResponse.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        Assert.assertNotNull(authorizeRedirectLocationHeader, "Redirected authorize response header location is null.");
        Assert.assertTrue(authorizeRedirectLocationHeader.getValue().contains(ORGANIZATION_PATH + organizationID),
                "Not redirected to child organization login page.");
        EntityUtils.consume(authorizeRedirectResponse.getEntity());

        HttpResponse childOrgLoginPageResponse =
                sendGetRequest(authorizeRedirectLocationHeader.getValue(), httpClientWithoutAutoRedirections);
        Assert.assertNotNull(childOrgLoginPageResponse, "Child organization login page is empty.");
        Assert.assertEquals(childOrgLoginPageResponse.getStatusLine().getStatusCode(), HttpStatus.SC_OK,
                "Child organization login redirection status code is invalid.");

        Map<String, Integer> keyPositionMap = new HashMap<>(1);
        keyPositionMap.put("name=\"sessionDataKey\"", 1);
        List<DataExtractUtil.KeyValue> keyValues =
                DataExtractUtil.extractDataFromResponse(childOrgLoginPageResponse, keyPositionMap);
        Assert.assertNotNull(keyValues, "Retrieved key value pairs are empty.");

        String sessionDataKey = keyValues.get(0).getValue();
        Assert.assertNotNull(sessionDataKey, "Session data key is null.");
        EntityUtils.consume(childOrgLoginPageResponse.getEntity());

        return sessionDataKey;
    }

    private String sendLoginPost(String sessionDataKey) throws Exception {

        String commonAuthURL = serverURL + ORGANIZATION_PATH + organizationID + COMMON_AUTH_ENDPOINT;
        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair(USERNAME_PARAM, B2B_USER_EMAIL));
        urlParameters.add(new BasicNameValuePair(PASSWORD_PARAM, B2B_USER_PASSWORD));
        urlParameters.add(new BasicNameValuePair(SESSION_DATA_KEY_PARAM, sessionDataKey));

        HttpResponse loginPostResponse =
                sendPostRequest(commonAuthURL, urlParameters, httpClientWithoutAutoRedirections);

        Assert.assertNotNull(loginPostResponse, "Login request failed. Login response is null.");
        Assert.assertEquals(loginPostResponse.getStatusLine().getStatusCode(), HttpStatus.SC_MOVED_TEMPORARILY,
                "Login status code is invalid.");
        Header childOrgAuthRedirectionLocation =
                loginPostResponse.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        Assert.assertNotNull(childOrgAuthRedirectionLocation, "Login response location header is null.");
        EntityUtils.consume(loginPostResponse.getEntity());

        HttpResponse childOrgAuthRedirectResponse =
                sendGetRequest(childOrgAuthRedirectionLocation.getValue(), httpClientWithoutAutoRedirections);
        Assert.assertEquals(childOrgAuthRedirectResponse.getStatusLine().getStatusCode(),
                HttpStatus.SC_MOVED_TEMPORARILY, "Child organization auth redirection status code is invalid.");
        Assert.assertNotNull(childOrgAuthRedirectResponse,
                "Child organization authorize redirection response is null.");

        Header rootOrgCommonAuthRedirectionLocation =
                childOrgAuthRedirectResponse.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        Assert.assertNotNull(rootOrgCommonAuthRedirectionLocation,
                "Child organization authorize redirection response location header is null.");
        EntityUtils.consume(childOrgAuthRedirectResponse.getEntity());

        HttpResponse rootOrgCommonAuthRedirectionResponse =
                sendGetRequest(rootOrgCommonAuthRedirectionLocation.getValue(), httpClientWithoutAutoRedirections);
        Assert.assertEquals(rootOrgCommonAuthRedirectionResponse.getStatusLine().getStatusCode(),
                HttpStatus.SC_MOVED_TEMPORARILY, "Root organization common auth redirection status code is invalid.");
        Assert.assertNotNull(rootOrgCommonAuthRedirectionResponse, "Root organization common auth response is null.");

        Header rootOrgAuthRedirectionLocation =
                rootOrgCommonAuthRedirectionResponse.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        Assert.assertNotNull(rootOrgAuthRedirectionLocation,
                "Root organization common auth response location header is null.");
        EntityUtils.consume(rootOrgCommonAuthRedirectionResponse.getEntity());

        HttpResponse authCodeResponse =
                sendGetRequest(rootOrgAuthRedirectionLocation.getValue(), httpClientWithoutAutoRedirections);
        Assert.assertNotNull(authCodeResponse, "Authorization code response is null.");
        Assert.assertEquals(authCodeResponse.getStatusLine().getStatusCode(), HttpStatus.SC_MOVED_TEMPORARILY,
                "Authorization code retrieval status code is invalid.");

        Header authCodeRedirectionLocation =
                authCodeResponse.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        Assert.assertNotNull(authCodeRedirectionLocation, "Authorization code response location header is null.");
        EntityUtils.consume(authCodeResponse.getEntity());

        URI authCodeRedirectionURI = new URI(authCodeRedirectionLocation.getValue());

        // Extract the authorization code from the location header.
        String code = Arrays.stream(authCodeRedirectionURI.getQuery().split(AMPERSAND))
                .filter(param -> param.startsWith(OAuth2Constant.AUTHORIZATION_CODE_NAME))
                .map(param -> param.split(EQUAL)[1])
                .findFirst()
                .orElse(null);
        Assert.assertNotNull(code, "Authorization code is null.");
        return code;
    }

    private String getAccessToken(String code) throws Exception {

        String tokenEndpoint = getTenantQualifiedURL(serverURL + TOKEN_ENDPOINT, tenant);
        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.GRANT_TYPE_NAME,
                OAuth2Constant.OAUTH2_GRANT_TYPE_AUTHORIZATION_CODE));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.AUTHORIZATION_CODE_NAME, code));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.REDIRECT_URI_NAME, OAuth2Constant.CALLBACK_URL));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_CLIENT_ID, b2bAppClientId));
        urlParameters.add(new BasicNameValuePair(CLIENT_SECRET_PARAM, b2bAppClientSecret));

        HttpResponse tokenResponse = sendPostRequest(tokenEndpoint, urlParameters, httpClientWithoutAutoRedirections);

        Assert.assertNotNull(tokenResponse, "Access token response is null.");
        Assert.assertEquals(tokenResponse.getStatusLine().getStatusCode(), HttpStatus.SC_OK,
                "Access token request failed.");

        JSONObject tokenResponseBody = new JSONObject(EntityUtils.toString(tokenResponse.getEntity()));
        String accessToken = tokenResponseBody.getString(OAuth2Constant.ACCESS_TOKEN);
        String idToken = tokenResponseBody.getString(OAuth2Constant.ID_TOKEN);

        Assert.assertNotNull(accessToken, "Access token is null.");
        Assert.assertNotNull(idToken, "ID token is null.");

        return accessToken;
    }

    private void validateAccessToken(String accessToken) throws Exception {

        String introspectEndpoint = getTenantQualifiedURL(serverURL + INTROSPECT_ENDPOINT, tenant);
        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_RESPONSE_TYPE_TOKEN, accessToken));

        HttpPost request = new HttpPost(introspectEndpoint);
        request.setHeader(USER_AGENT_ATTRIBUTE, OAuth2Constant.USER_AGENT);
        request.setHeader(AUTHORIZATION_ATTRIBUTE, BASIC_AUTHORIZATION_ATTRIBUTE + new String(
                Base64.encodeBase64((authenticatingUserName + COLON + authenticatingCredential).getBytes())));
        request.setEntity(new UrlEncodedFormEntity(urlParameters));
        HttpResponse introspectResponse = httpClientWithoutAutoRedirections.execute(request);

        Assert.assertNotNull(introspectResponse, "Introspect response is null.");
        Assert.assertEquals(introspectResponse.getStatusLine().getStatusCode(), HttpStatus.SC_OK,
                "Introspect request failed.");

        JSONObject introspectionResponseBody = new JSONObject(EntityUtils.toString(introspectResponse.getEntity()));
        boolean activeStatus = Boolean.parseBoolean(introspectionResponseBody.getString("active"));
        Assert.assertTrue(activeStatus, "Received access token is not valid.");
    }
}

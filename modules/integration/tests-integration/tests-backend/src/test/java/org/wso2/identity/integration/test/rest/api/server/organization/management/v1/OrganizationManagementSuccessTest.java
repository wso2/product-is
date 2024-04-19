/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
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
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationPatchModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationSharePOSTRequest;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.AssociatedRolesConfig;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.InboundProtocols;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.OpenIDConnectConfiguration;
import org.wso2.identity.integration.test.restclients.OAuth2RestClient;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.testng.Assert.assertNotNull;
import static org.wso2.identity.integration.test.restclients.RestBaseClient.CONTENT_TYPE_ATTRIBUTE;
import static org.wso2.identity.integration.test.restclients.RestBaseClient.ORGANIZATION_PATH;
import static org.wso2.identity.integration.test.restclients.RestBaseClient.TENANT_PATH;
import static org.wso2.identity.integration.test.scim2.SCIM2BaseTestCase.SCIM2_USERS_ENDPOINT;

/**
 * Tests for successful cases of the Organization Management REST APIs.
 */
public class OrganizationManagementSuccessTest extends OrganizationManagementBaseTest {

    private String organizationID;
    private String selfServiceAppId;
    private String selfServiceAppClientId;
    private String selfServiceAppClientSecret;
    private String m2mToken;
    private String switchedM2MToken;
    private String b2bApplicationID;
    private HttpClient client;
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
        String body = readResource("create-organization-self-service-app-request.body.json");

        Response response = given().auth().preemptive().basic(authenticatingUserName, authenticatingCredential)
                .contentType(ContentType.JSON)
                .body(body).when().post(endpointURL);
        response.then()
                .log().ifValidationFails().assertThat().statusCode(HttpStatus.SC_CREATED);

        Optional<ApplicationListItem> b2bSelfServiceApp = oAuth2RestClient.getAllApplications().getApplications().stream()
                .filter(application -> application.getName().equals("b2b-self-service-app"))
                .findAny();
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

    @Test(dependsOnMethods = "createApplicationForSelfOrganizationOnboardService")
    public void getM2MAccessToken() throws Exception {

        OpenIDConnectConfiguration openIDConnectConfiguration = oAuth2RestClient.getOIDCInboundDetails(selfServiceAppId);
        selfServiceAppClientId = openIDConnectConfiguration.getClientId();
        selfServiceAppClientSecret = openIDConnectConfiguration.getClientSecret();
        AuthorizationGrant clientCredentialsGrant = new ClientCredentialsGrant();
        ClientID clientID = new ClientID(selfServiceAppClientId);
        Secret clientSecret = new Secret(selfServiceAppClientSecret);
        ClientAuthentication clientAuth = new ClientSecretBasic(clientID, clientSecret);
        Scope scope = new Scope("SYSTEM");

        URI tokenEndpoint = new URI(getTenantQualifiedURL(OAuth2Constant.ACCESS_TOKEN_ENDPOINT, tenantInfo.getDomain()));
        TokenRequest request = new TokenRequest(tokenEndpoint, clientAuth, clientCredentialsGrant, scope);
        HTTPResponse tokenHTTPResp = request.toHTTPRequest().send();
        Assert.assertNotNull(tokenHTTPResp, "Access token http response is null.");

        TokenResponse tokenResponse = TokenResponse.parse(tokenHTTPResp);
        AccessTokenResponse accessTokenResponse = tokenResponse.toSuccessResponse();
        m2mToken = accessTokenResponse.getTokens().getAccessToken().getValue();
        Assert.assertNotNull(m2mToken, "The retrieved M2M Token is null in the token response.");

        Scope scopesInResponse = accessTokenResponse.getTokens().getAccessToken().getScope();
        Assert.assertTrue(scopesInResponse.contains("internal_organization_create"), "Requested scope is missing in the token response");
    }

    @Test(dependsOnMethods = "getM2MAccessToken")
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

    @Test(dependsOnMethods = "testSelfOnboardOrganization")
    public void testGetOrganization() {

        Response response = getResponseOfGet(ORGANIZATION_MANAGEMENT_API_BASE_PATH + PATH_SEPARATOR + organizationID);
        validateHttpStatusCode(response, HttpStatus.SC_OK);
        Assert.assertNotNull(response.asString());
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("id", equalTo(organizationID));
    }

    @Test(dependsOnMethods = "testSelfOnboardOrganization")
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
        httpPost.setHeader("Authorization", "Basic " +
                new String(Base64.encodeBase64((selfServiceAppClientId + ":" + selfServiceAppClientSecret).getBytes())));
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
        HttpPost request = new HttpPost(serverURL + TENANT_PATH + tenant + PATH_SEPARATOR + ORGANIZATION_PATH + SCIM2_USERS_ENDPOINT);
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
        String shareApplicationUrl = ORGANIZATION_MANAGEMENT_API_BASE_PATH +
                "/" + SUPER_ORGANIZATION_ID + "/applications/" + b2bApplicationID + "/share";
        org.json.JSONObject shareAppObject = new org.json.JSONObject();
        shareAppObject.put("shareWithAllChildren", true);
        getResponseOfPost(shareApplicationUrl, shareAppObject.toString());
    }

    @Test(dependsOnMethods = "shareB2BApplication")
    public void unShareB2BApplication() throws JSONException {

        if (!SUPER_TENANT_DOMAIN.equals(tenant)) {
            return;
        }
        String shareApplicationUrl = ORGANIZATION_MANAGEMENT_API_BASE_PATH +
                "/" + SUPER_ORGANIZATION_ID + "/applications/" + b2bApplicationID + "/share";
        org.json.JSONObject shareAppObject = new org.json.JSONObject();
        shareAppObject.put("shareWithAllChildren", false);
        getResponseOfPost(shareApplicationUrl, shareAppObject.toString());
    }

    @Test(dependsOnMethods = "unShareB2BApplication")
    public void testDisablingOrganization() throws IOException {

        String endpoint = ORGANIZATION_MANAGEMENT_API_BASE_PATH + PATH_SEPARATOR + organizationID;
        String body = readResource("disable-organization-request-body.json");
        Response response = getResponseOfPatch(endpoint, body);
        validateHttpStatusCode(response, HttpStatus.SC_OK);
    }

    @Test(dependsOnMethods = "testDisablingOrganization")
    public void testDeleteOrganization() {

        String organizationPath = ORGANIZATION_MANAGEMENT_API_BASE_PATH + "/" + organizationID;
        Response responseOfDelete = getResponseOfDelete(organizationPath);
        responseOfDelete.then()
                .log()
                .ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NO_CONTENT);
    }

    private void deleteApplication(String applicationId) throws Exception {

        oAuth2RestClient.deleteApplication(applicationId);
    }
}

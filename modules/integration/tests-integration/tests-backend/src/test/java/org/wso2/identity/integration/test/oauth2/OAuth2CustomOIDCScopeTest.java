/*
 * Copyright (c) 2022, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.identity.integration.test.oauth2;

import com.nimbusds.oauth2.sdk.AccessTokenResponse;
import com.nimbusds.oauth2.sdk.AuthorizationCode;
import com.nimbusds.oauth2.sdk.AuthorizationCodeGrant;
import com.nimbusds.oauth2.sdk.AuthorizationGrant;
import com.nimbusds.oauth2.sdk.ResourceOwnerPasswordCredentialsGrant;
import com.nimbusds.oauth2.sdk.Scope;
import com.nimbusds.oauth2.sdk.TokenErrorResponse;
import com.nimbusds.oauth2.sdk.TokenRequest;
import com.nimbusds.oauth2.sdk.TokenResponse;
import com.nimbusds.oauth2.sdk.auth.ClientAuthentication;
import com.nimbusds.oauth2.sdk.auth.ClientSecretBasic;
import com.nimbusds.oauth2.sdk.auth.Secret;
import com.nimbusds.oauth2.sdk.http.HTTPResponse;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.id.Identifier;
import com.nimbusds.openid.connect.sdk.OIDCTokenResponse;
import com.nimbusds.openid.connect.sdk.OIDCTokenResponseParser;
import com.nimbusds.openid.connect.sdk.token.OIDCTokens;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.identity.application.common.model.xsd.ServiceProvider;
import org.wso2.carbon.identity.oauth.stub.dto.OAuthConsumerAppDTO;
import org.wso2.identity.integration.test.utils.DataExtractUtil;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.wso2.identity.integration.test.utils.OAuth2Constant.ACCESS_TOKEN;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.ACCESS_TOKEN_ENDPOINT;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.AUTHORIZATION_CODE_NAME;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.AUTHORIZE_ENDPOINT_URL;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.ID_TOKEN;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.OAUTH2_CLIENT_ID;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.OAUTH2_GRANT_TYPE_CODE;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.OAUTH2_NONCE;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.OAUTH2_REDIRECT_URI;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.OAUTH2_RESPONSE_TYPE;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.OAUTH2_SCOPE;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.SESSION_DATA_KEY;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.SESSION_DATA_KEY_CONSENT;

/**
 * Tests related to retrieving user attributes with custom OIDC scopes.
 */
public class OAuth2CustomOIDCScopesTest extends OAuth2ServiceAbstractIntegrationTest {

    private static final String CALLBACK_URL = "https://localhost/callback";
    private CloseableHttpClient client;
    private String sessionDataKey;
    private String sessionDataKeyConsent;
    private AuthorizationCode authorizationCode;
    private final TestUserMode adminUserMode;
    private final TestUserMode loginUserMode;
    private String tenantDomain;
    private String adminUsername;
    private String adminPassword;
    private String loginUsername;
    private String loginPassword;
    private String requestedScopes;
    private static final String REQUESTED_OIDC_SCOPE_STRING = "openid email profile phone address internal_login";
    private String customScopeName;

    @DataProvider(name = "configProvider")
    public static Object[][] configProvider() {

        return new Object[][]{{TestUserMode.SUPER_TENANT_ADMIN, TestUserMode.SUPER_TENANT_USER}, {TestUserMode.TENANT_ADMIN, TestUserMode.TENANT_USER}};
    }

    @Factory(dataProvider = "configProvider")
    public OAuth2CustomOIDCScopeTest(TestUserMode adminUserMode, TestUserMode loggedInUserMode) {

        this.adminUserMode = adminUserMode;
        this.loginUserMode = loggedInUserMode;
    }

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.init(adminUserMode);

        tenantDomain = tenantInfo.getDomain();
        // Setup admin credentials.
        adminUsername = userInfo.getUserName();
        adminPassword = userInfo.getPassword();

        AutomationContext context = new AutomationContext("IDENTITY", loginUserMode);
        loginUsername = context.getContextTenant().getContextUser().getUserName();
        loginPassword = context.getContextTenant().getContextUser().getPassword();

        customScopeName = "custom_" + tenantDomain;
        requestedScopes = REQUESTED_OIDC_SCOPE_STRING + " " + customScopeName;

        client = HttpClientBuilder.create().disableRedirectHandling().build();
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {

        deleteApplication();
        removeOAuthApplicationData();
        client.close();
    }

    @Test(groups = "wso2.is", description = "Check Oauth2 application registration.")
    public void testRegisterApplication() throws Exception {

        OAuthConsumerAppDTO oAuthConsumerAppDTO = getBasicOAuthApp(CALLBACK_URL);
        ServiceProvider serviceProvider = registerServiceProviderWithOAuthInboundConfigs(oAuthConsumerAppDTO);
        Assert.assertNotNull(serviceProvider, "OAuth App creation failed.");
        Assert.assertNotNull(consumerKey, "Consumer Key is null.");
        Assert.assertNotNull(consumerSecret, "Consumer Secret is null.");
    }

    // Create a new OIDC claim
    // Create a new OIDC scope and new OIDC claim to it
    @Test(groups = "wso2.is", description = "Create a new OIDC scope.", dependsOnMethods = "testRegisterApplication")
    public void testCreateCustomOIDCScope() throws Exception {
        // Get a token with required scopes
        String accessToken = getAccessTokenToCallAPI("internal_application_mgt_create", "internal_application_mgt_view");
        Assert.assertNotNull(accessToken, "Could not get an access token.");

        JSONObject scopeCreateRequest = new JSONObject();
        scopeCreateRequest.put("name", customScopeName);
        scopeCreateRequest.put("displayName", "Custom Scope " + tenantDomain);
        scopeCreateRequest.put("claims", new JSONArray());

        HttpPost httpPost = new HttpPost(getOIDCSCopeEndpoint(tenantDomain));
        String authorizationHeader = "Bearer " + accessToken;
        httpPost.setHeader("Authorization", authorizationHeader);
        httpPost.setHeader("Content-Type", "application/json");
        httpPost.setEntity(new StringEntity(scopeCreateRequest.toString()));
        HttpResponse response = client.execute(httpPost);
        int statusCode = response.getStatusLine().getStatusCode();
        Assert.assertEquals(statusCode, 201);

        HttpGet httpGet = new HttpGet(getOIDCSCopeEndpoint(tenantDomain) + "/" + customScopeName);
        httpGet.setHeader("Authorization", authorizationHeader);
        response = client.execute(httpGet);
        String responseString = EntityUtils.toString(response.getEntity(), "UTF-8");
        EntityUtils.consume(response.getEntity());
        JSONParser parser = new JSONParser();
        org.json.simple.JSONObject json = (org.json.simple.JSONObject) parser.parse(responseString);
        Assert.assertNotNull(json);
        Assert.assertEquals(json.get("name"), customScopeName);
    }

    private String getOIDCSCopeEndpoint(String tenantDomain) {

        return "carbon.super".equalsIgnoreCase(tenantDomain) ? OAuth2Constant.SCOPE_ENDPOINT : OAuth2Constant.TENANT_SCOPE_ENDPOINT;
    }

    private String getAccessTokenToCallAPI(String... scopes) throws Exception {

        Secret adminSecret = new Secret(this.adminPassword);
        AuthorizationGrant passwordGrant = new ResourceOwnerPasswordCredentialsGrant(adminUsername, adminSecret);

        ClientID clientID = new ClientID(consumerKey);
        Secret clientSecret = new Secret(consumerSecret);
        ClientAuthentication clientAuth = new ClientSecretBasic(clientID, clientSecret);

        Scope requestedScope = Scope.parse(Arrays.asList(scopes));
        URI tokenEndpoint = new URI(ACCESS_TOKEN_ENDPOINT);
        TokenRequest request = new TokenRequest(tokenEndpoint, clientAuth, passwordGrant, requestedScope);

        HTTPResponse tokenHTTPResp = request.toHTTPRequest().send();
        if (tokenHTTPResp != null) {
            TokenResponse tokenResponse = TokenResponse.parse(tokenHTTPResp);
            if (tokenResponse.indicatesSuccess()) {
                AccessTokenResponse successResponse = tokenResponse.toSuccessResponse();
                return successResponse.getTokens().getBearerAccessToken().getValue();
            }
        }
        return null;
    }

    // Create a user having values for

    @Test(groups = "wso2.is", description = "Send authorize user request for authorization code grant type.", dependsOnMethods = "testCreateCustomOIDCScope")
    public void testAuthCodeGrantSendAuthRequestPost() throws Exception {

        // Send a direct auth code request to IS instance.
        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair(OAUTH2_RESPONSE_TYPE, OAUTH2_GRANT_TYPE_CODE));
        urlParameters.add(new BasicNameValuePair(OAUTH2_CLIENT_ID, consumerKey));
        urlParameters.add(new BasicNameValuePair(OAUTH2_REDIRECT_URI, CALLBACK_URL));
        urlParameters.add(new BasicNameValuePair(OAUTH2_SCOPE, requestedScopes));

        HttpResponse response = sendPostRequestWithParameters(client, urlParameters, AUTHORIZE_ENDPOINT_URL);
        Assert.assertNotNull(response, "Authorization request failed. Authorized response is null");

        String locationValue = getLocationHeaderValue(response);
        Assert.assertTrue(locationValue.contains(SESSION_DATA_KEY), "sessionDataKey not found in response.");

        // Extract sessionDataKey from the location value.
        sessionDataKey = DataExtractUtil.getParamFromURIString(locationValue, SESSION_DATA_KEY);
        Assert.assertNotNull(sessionDataKey, "Session data key is null.");
        EntityUtils.consume(response.getEntity());
    }

    @Test(groups = "wso2.is", description = "Send login post request.", dependsOnMethods = "testAuthCodeGrantSendAuthRequestPost")
    public void testAuthCodeGrantSendLoginPost() throws Exception {

        sessionDataKeyConsent = completeLogin(client, sessionDataKey, loginUsername, loginPassword);
        Assert.assertNotNull(sessionDataKeyConsent, "Invalid session key consent.");
    }

    @Test(groups = "wso2.is", description = "Send approval post request.", dependsOnMethods = "testAuthCodeGrantSendLoginPost")
    public void testAuthCodeGrantSendApprovalPost() throws Exception {

        HttpResponse response = sendApprovalPost(client, sessionDataKeyConsent);
        Assert.assertNotNull(response, "Approval request failed. response is invalid.");

        Header locationHeader = response.getFirstHeader(HTTP_RESPONSE_HEADER_LOCATION);
        Assert.assertNotNull(locationHeader, "Approval request failed. Location header is null.");

        String code = getLocationHeaderValue(response);
        Assert.assertTrue(code.contains(AUTHORIZATION_CODE_NAME), "Authorization code not found in the response.");

        // Extract authorization code from the location value.
        authorizationCode = new AuthorizationCode(DataExtractUtil.getParamFromURIString(code, AUTHORIZATION_CODE_NAME));
        Assert.assertNotNull(authorizationCode, "Authorization code is null.");
        EntityUtils.consume(response.getEntity());
    }

    @Test(groups = "wso2.is", description = "Send get access token request.", dependsOnMethods = "testAuthCodeGrantSendApprovalPost")
    public void testAuthCodeGrantSendGetTokensPost() throws Exception {

        ClientID clientID = new ClientID(consumerKey);
        Secret clientSecret = new Secret(consumerSecret);
        ClientSecretBasic clientSecretBasic = new ClientSecretBasic(clientID, clientSecret);

        URI callbackURI = new URI(CALLBACK_URL);
        AuthorizationCodeGrant codeGrant = new AuthorizationCodeGrant(authorizationCode, callbackURI);

        TokenRequest tokenReq = new TokenRequest(new URI(ACCESS_TOKEN_ENDPOINT), clientSecretBasic, codeGrant);
        HTTPResponse tokenHTTPResp = tokenReq.toHTTPRequest().send();
        Assert.assertNotNull(tokenHTTPResp, "Access token http response is null.");

        TokenResponse tokenResponse = OIDCTokenResponseParser.parse(tokenHTTPResp);
        Assert.assertNotNull(tokenResponse, "Access token response is null.");
        Assert.assertFalse(tokenResponse instanceof TokenErrorResponse, "Access token response contains errors.");
        Assert.assertTrue(tokenResponse instanceof OIDCTokenResponse, "Access token response in not an OIDC response.");

        OIDCTokenResponse oidcTokenResponse = (OIDCTokenResponse) tokenResponse;
        OIDCTokens oidcTokens = oidcTokenResponse.getOIDCTokens();
        Assert.assertNotNull(oidcTokens, "OIDC Tokens object is null.");

        String idToken = oidcTokens.getIDTokenString();
        Assert.assertNotNull(idToken, "ID token is null");

        // Validate whether requested scopes are present.
        Scope scope = oidcTokenResponse.getOIDCTokens().getAccessToken().getScope();
        Assert.assertTrue(containAllRequestedOIDCScopes(scope), "Access token does not contain all requested scopes.");
    }

    @Test(groups = "wso2.is", description = "Send authorize user request for implicit grant type.", dependsOnMethods = "testAuthCodeGrantSendGetTokensPost")
    public void testImplicitGrantSendAuthRequestPost() throws Exception {

        // Remove previous data from variables.
        sessionDataKey = null;
        sessionDataKeyConsent = null;

        // Reset client.
        client = HttpClientBuilder.create().disableRedirectHandling().build();

        // Send a direct implicit token request to IS instance.
        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair(OAUTH2_RESPONSE_TYPE, "id_token token"));
        urlParameters.add(new BasicNameValuePair(OAUTH2_NONCE, UUID.randomUUID().toString()));
        urlParameters.add(new BasicNameValuePair(OAUTH2_CLIENT_ID, consumerKey));
        urlParameters.add(new BasicNameValuePair(OAUTH2_REDIRECT_URI, CALLBACK_URL));
        urlParameters.add(new BasicNameValuePair(OAUTH2_SCOPE, requestedScopes));

        HttpResponse response = sendPostRequestWithParameters(client, urlParameters, AUTHORIZE_ENDPOINT_URL);
        Assert.assertNotNull(response, "Authorization request failed. Authorized response is null");

        String locationValue = getLocationHeaderValue(response);
        Assert.assertTrue(locationValue.contains(SESSION_DATA_KEY), "sessionDataKey not found in response.");

        // Extract sessionDataKey from the location value.
        sessionDataKey = DataExtractUtil.getParamFromURIString(locationValue, SESSION_DATA_KEY);
        Assert.assertNotNull(sessionDataKey, "Session data key is null.");
        EntityUtils.consume(response.getEntity());
    }

    @Test(groups = "wso2.is", description = "Send login post request.", dependsOnMethods = "testImplicitGrantSendAuthRequestPost")
    public void testImplicitGrantSendLoginPost() throws Exception {

        sessionDataKeyConsent = completeLogin(client, sessionDataKey, loginUsername, loginPassword);
        Assert.assertNotNull(sessionDataKeyConsent, "Invalid session key consent.");
    }

    @Test(groups = "wso2.is", description = "Send approval post request.", dependsOnMethods = "testImplicitGrantSendLoginPost")
    public void testImplicitGrantSendApprovalPost() throws Exception {

        HttpResponse response = sendApprovalPost(client, sessionDataKeyConsent);
        Assert.assertNotNull(response, "Approval request failed. response is invalid.");

        Header locationHeader = response.getFirstHeader(HTTP_RESPONSE_HEADER_LOCATION);
        Assert.assertNotNull(locationHeader, "Approval request failed. Location header is null.");

        // Extract authorization code from the location value.
        String idToken = DataExtractUtil.extractParamFromURIFragment(locationHeader.getValue(), ID_TOKEN);
        Assert.assertNotNull(idToken, "ID token is null.");
        String accessToken = DataExtractUtil.extractParamFromURIFragment(locationHeader.getValue(), ACCESS_TOKEN);
        Assert.assertNotNull(idToken, "Access token is null.");

        // Test whether all requested scopes were returned.
        String introspectionUrl = getIntrospectionUrl(tenantDomain);
        org.json.simple.JSONObject introspectionResponse = introspectTokenWithTenant(client, accessToken, introspectionUrl, adminUsername, adminPassword);
        Assert.assertTrue(introspectionResponse.containsKey("scope"));
        String scope = introspectionResponse.get("scope").toString();
        Scope returnedScope = Scope.parse(scope);
        Assert.assertTrue(containAllRequestedOIDCScopes(returnedScope), "Access token does not contain all requested scopes.");

        EntityUtils.consume(response.getEntity());
    }

    private String getIntrospectionUrl(String tenantDomain) {

        return "carbon.super".equalsIgnoreCase(tenantDomain) ? OAuth2Constant.INTRO_SPEC_ENDPOINT : OAuth2Constant.TENANT_INTRO_SPEC_ENDPOINT;
    }

    @Test(groups = "wso2.is", description = "Send authorize user request for resource owner grant type.", dependsOnMethods = "testImplicitGrantSendApprovalPost")
    public void testResourceOwnerGrantSendAuthRequestPost() throws Exception {

        // Reset client.
        client = HttpClientBuilder.create().disableRedirectHandling().build();

        Secret password = new Secret(loginPassword);
        AuthorizationGrant passwordGrant = new ResourceOwnerPasswordCredentialsGrant(loginUsername, password);

        ClientID clientID = new ClientID(consumerKey);
        Secret clientSecret = new Secret(consumerSecret);
        ClientAuthentication clientAuth = new ClientSecretBasic(clientID, clientSecret);

        Scope requestedScope = Scope.parse(requestedScopes);
        URI tokenEndpoint = new URI(ACCESS_TOKEN_ENDPOINT);

        TokenRequest request = new TokenRequest(tokenEndpoint, clientAuth, passwordGrant, requestedScope);

        HTTPResponse tokenHTTPResp = request.toHTTPRequest().send();
        Assert.assertNotNull(tokenHTTPResp, "Access token http response is null.");

        TokenResponse tokenResponse = OIDCTokenResponseParser.parse(tokenHTTPResp);
        Assert.assertNotNull(tokenResponse, "Access token response is null.");

        Assert.assertFalse(tokenResponse instanceof TokenErrorResponse, "Access token response contains errors.");

        OIDCTokenResponse oidcTokenResponse = (OIDCTokenResponse) tokenResponse;
        OIDCTokens oidcTokens = oidcTokenResponse.getOIDCTokens();
        Assert.assertNotNull(oidcTokens, "OIDC Tokens object is null.");

        String idToken = oidcTokens.getIDTokenString();
        Assert.assertNotNull(idToken, "ID token is null");

        // Validate whether requested scopes are present.
        Scope scope = oidcTokenResponse.getOIDCTokens().getAccessToken().getScope();
        Assert.assertTrue(containAllRequestedOIDCScopes(scope), "Access token does not contain all requested scopes.");
    }

    /**
     * Extract the location header value from a HttpResponse.
     *
     * @param response HttpResponse object that needs the header extracted.
     * @return String value of the location header.
     */
    private String getLocationHeaderValue(HttpResponse response) {

        Header location = response.getFirstHeader(HTTP_RESPONSE_HEADER_LOCATION);
        Assert.assertNotNull(location);
        return location.getValue();
    }

    /**
     * Sends a log in post to the IS instance and extract and return the sessionDataKeyConsent from the response.
     *
     * @param client         CloseableHttpClient object to send the login post.
     * @param sessionDataKey String sessionDataKey obtained.
     * @return Extracted sessionDataKeyConsent.
     * @throws IOException
     * @throws URISyntaxException
     */
    private String completeLogin(CloseableHttpClient client, String sessionDataKey, String username, String password) throws IOException, URISyntaxException {

        HttpResponse response = sendLoginPostForCustomUsers(client, sessionDataKey, username, password);
        Assert.assertNotNull(response, "Login request failed. response is null.");

        Header locationHeader = response.getFirstHeader(HTTP_RESPONSE_HEADER_LOCATION);
        Assert.assertNotNull(locationHeader, "Login response header is null");
        EntityUtils.consume(response.getEntity());

        // Request will return with a 302 to the authorize end point. Doing a GET will give the sessionDataKeyConsent
        response = sendGetRequest(client, locationHeader.getValue());

        String locationValue = getLocationHeaderValue(response);
        Assert.assertTrue(locationValue.contains(SESSION_DATA_KEY_CONSENT), "sessionDataKeyConsent not found in response.");

        EntityUtils.consume(response.getEntity());

        // Extract sessionDataKeyConsent from the location value.
        return DataExtractUtil.getParamFromURIString(locationValue, SESSION_DATA_KEY_CONSENT);
    }

    private boolean containAllRequestedOIDCScopes(Scope returnedScopes) {

        Set<String> requestedScopes = Arrays.stream(this.requestedScopes.split(" ")).collect(Collectors.toSet());
        return returnedScopes.stream().map(Identifier::getValue).collect(Collectors.toSet()).containsAll(requestedScopes);
    }
}

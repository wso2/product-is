/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.config.Lookup;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.cookie.CookieSpecProvider;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.RFC6265CookieSpecProvider;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.AdvancedApplicationConfiguration;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.AccessTokenConfiguration;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationResponseModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.InboundProtocols;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.OpenIDConnectConfiguration;
import org.wso2.identity.integration.test.rest.api.server.roles.v2.model.Audience;
import org.wso2.identity.integration.test.rest.api.server.roles.v2.model.Permission;
import org.wso2.identity.integration.test.rest.api.server.roles.v2.model.RoleV2;
import org.wso2.identity.integration.test.rest.api.user.common.model.Email;
import org.wso2.identity.integration.test.rest.api.user.common.model.ListObject;
import org.wso2.identity.integration.test.rest.api.user.common.model.Name;
import org.wso2.identity.integration.test.rest.api.user.common.model.PatchOperationRequestObject;
import org.wso2.identity.integration.test.rest.api.user.common.model.RoleItemAddGroupobj;
import org.wso2.identity.integration.test.rest.api.user.common.model.UserObject;
import org.wso2.identity.integration.test.restclients.SCIM2RestClient;
import org.wso2.identity.integration.test.utils.CarbonUtils;
import org.wso2.identity.integration.test.utils.DataExtractUtil;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.wso2.identity.integration.test.utils.DPoPProofGenerator.genarateDPoPProof;
import static org.wso2.identity.integration.test.utils.DPoPProofGenerator.getThumbprintOfKeyFromDpopProof;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.ACCESS_TOKEN_ENDPOINT;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.AUTHORIZATION_HEADER;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.AUTHORIZE_ENDPOINT_URL;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.BASIC_HEADER;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.CALLBACK_URL;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.OAUTH2_GRANT_TYPE_AUTHORIZATION_CODE;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.OAUTH2_GRANT_TYPE_CLIENT_CREDENTIALS;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.OAUTH2_GRANT_TYPE_CODE;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.OAUTH2_GRANT_TYPE_REFRESH_TOKEN;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.OAUTH2_GRANT_TYPE_RESOURCE_OWNER;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.PAR_ENDPOINT;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.USER_AGENT;


public class OAuth2DPopTestCase extends OAuth2ServiceAbstractIntegrationTest {

    private static final String USERS = "users";
    private static final String DPOP_HEADER = "DPoP";
    private static final String TOKEN_TYPE = "DPoP";
    private static final String BINDING_TYPE = "DPoP";
    private static final String SCIM2_USERS_API = "/scim2/Users";
    public final static String SCIM2_USERS_ENDPOINT = "https://localhost:9853/scim2/Users";
    private final static String INTERNAL_USER_MGT_LIST  = "internal_user_mgt_list";
    private static final String TEST_USER = "test_user";
    private static final String ADMIN_WSO2 = "Admin@wso2";
    private static final String USER_EMAIL = "dpopUser@wso2.com";
    private static final String APPLICATION_AUDIENCE = "APPLICATION";
    private static final String TEST_ROLE_APPLICATION = "test_role_application";
    private static final String TEST_USER_FIRST_NAME = "DpopUserFirst";
    private static final String TEST_USER_LAST_NAME =  "DpopUserLast";

    private CloseableHttpClient client;
    private OpenIDConnectConfiguration oidcConfig;
    private SCIM2RestClient scim2RestClient;
    private AccessTokenConfiguration accessTokenConfig;
    private final CookieStore cookieStore = new BasicCookieStore();
    private Lookup<CookieSpecProvider> cookieSpecRegistry;
    private RequestConfig requestConfig;
    private final TestUserMode userMode;

    private String appId;
    private String clientId;
    private String accessToken;
    private String jkt;
    private String sessionDataKey;
    private String authorizationCode;
    private String userId;
    private String refreshToken;
    private String roleId;
    private String requestURI;


    @Factory(dataProvider = "testExecutionContextProvider")
    public OAuth2DPopTestCase(TestUserMode testUserMode) {

        this.userMode = testUserMode;
    }

    @DataProvider(name = "testExecutionContextProvider")
    public static Object[][] getTestExecutionContext() {

        return new Object[][]{
                {TestUserMode.SUPER_TENANT_USER},
                {TestUserMode.TENANT_USER}
        };
    }

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {
        super.init(userMode);
        super.setSystemproperties();

        tenantInfo = isServer.getContextTenant();
        scim2RestClient = new SCIM2RestClient(serverURL, tenantInfo);



        cookieSpecRegistry = RegistryBuilder.<CookieSpecProvider>create()
                .register(CookieSpecs.DEFAULT, new RFC6265CookieSpecProvider())
                .build();
        requestConfig = RequestConfig.custom()
                .setCookieSpec(CookieSpecs.DEFAULT)
                .build();
        client = HttpClientBuilder.create()
                .setDefaultCookieStore(cookieStore)
                .setDefaultCookieSpecRegistry(cookieSpecRegistry)
                .setDefaultRequestConfig(requestConfig)
                .setRedirectStrategy(new DefaultRedirectStrategy() {
                    @Override
                    protected boolean isRedirectable(String method) {

                        return false;
                    }
                })
                .build();


        ApplicationResponseModel application = getApplicationWithDpopEnabled();

        appId = application.getId();
        assertNotNull(appId, "Error while creating the DPoP enabled JWT application.");

        this.oidcConfig = super.restClient.getOIDCInboundDetails(this.appId);
        assertNotNull(this.oidcConfig, "Error while retrieving the OIDC configuration of the application.");


        this.clientId = this.oidcConfig.getClientId();
        assertNotNull(this.clientId, "Error while retrieving the client id of the application.");

        addUser();
    }

    @AfterClass(alwaysRun = true)
    public void testClear() throws Exception {
        deleteRole(roleId);
        super.deleteApp(this.appId);
        scim2RestClient.deleteUser(userId);
        cookieStore.clear();

        this.client.close();
        super.restClient.closeHttpClient();
        scim2RestClient.closeHttpClient();
    }

    private ApplicationResponseModel getApplicationWithDpopEnabled() throws Exception {

        final ApplicationModel application = new ApplicationModel();

        accessTokenConfig = new AccessTokenConfiguration().type("JWT");
        accessTokenConfig.setUserAccessTokenExpiryInSeconds(3600L);
        accessTokenConfig.setApplicationAccessTokenExpiryInSeconds(3600L);

        accessTokenConfig.setBindingType(BINDING_TYPE);

        List<String> callBackUrls = new ArrayList<>();
        Collections.addAll(callBackUrls, CALLBACK_URL);

        oidcConfig = new OpenIDConnectConfiguration();

        List<String> grantTypes = new ArrayList<>();
        Collections.addAll(grantTypes,OAUTH2_GRANT_TYPE_CLIENT_CREDENTIALS, OAUTH2_GRANT_TYPE_AUTHORIZATION_CODE,
                OAUTH2_GRANT_TYPE_REFRESH_TOKEN, OAUTH2_GRANT_TYPE_RESOURCE_OWNER);

        oidcConfig.setCallbackURLs(callBackUrls);
        oidcConfig.setGrantTypes(grantTypes);
        oidcConfig.setAccessToken(accessTokenConfig);

        InboundProtocols inboundProtocolsConfig = new InboundProtocols();
        inboundProtocolsConfig.setOidc(oidcConfig);

        application.setAdvancedConfigurations(new AdvancedApplicationConfiguration().skipLoginConsent(true));

        application.setInboundProtocolConfiguration(inboundProtocolsConfig);
        application.setName("DPoPTestApp");

        String appId = addApplication(application);

        if (!CarbonUtils.isLegacyAuthzRuntimeEnabled()) {
            authorizeSystemAPIs(appId, Collections.singletonList(SCIM2_USERS_API));
        }

        return getApplication(appId);
    }

    @Test(groups = "wso2.is", description = "Get a DPoP access token with client credentials")
    public void getDPoPTokenWithClientCredentials() throws Exception {

        String dpopProof = genarateDPoPProof("RSA","DUMMYJTI", "POST",
                getTenantQualifiedURL(ACCESS_TOKEN_ENDPOINT, tenantInfo.getDomain()),
                new Date(System.currentTimeMillis()),"dpop+jwt" );

        jkt = getThumbprintOfKeyFromDpopProof(dpopProof);

        List<NameValuePair> parameters = new ArrayList<>();
        parameters.add(new BasicNameValuePair("grant_type", OAUTH2_GRANT_TYPE_CLIENT_CREDENTIALS));
        parameters.add(new BasicNameValuePair("scope", INTERNAL_USER_MGT_LIST ));

        List<Header> headers = new ArrayList<>();
        headers.add(new BasicHeader(AUTHORIZATION_HEADER, BASIC_HEADER + " " +
                getBase64EncodedString(clientId, oidcConfig.getClientSecret())));
        headers.add(new BasicHeader("Content-Type", "application/x-www-form-urlencoded"));
        headers.add(new BasicHeader("User-Agent", USER_AGENT));
        headers.add(new BasicHeader(DPOP_HEADER, dpopProof));

        HttpResponse response = sendPostRequest(client, headers, parameters,
                getTenantQualifiedURL(ACCESS_TOKEN_ENDPOINT, tenantInfo.getDomain()));

        String responseString = EntityUtils.toString(response.getEntity(), "UTF-8");
        JSONObject jsonResponse = new JSONObject(responseString);

        assertTrue(jsonResponse.has("access_token"), "Access token is not present in the response.");
        assertTrue(jsonResponse.has("token_type"), "Token type is not present in the response.");
        assertTrue(jsonResponse.has("expires_in"), "Expires in is not present in the response.");

        String  accessToken = jsonResponse.getString("access_token");
        this.accessToken = accessToken;
        assertNotNull(accessToken, "Access token is null.");

        assertTrue(jsonResponse.getString("token_type").equalsIgnoreCase(TOKEN_TYPE),
                "Token type is not DPoP.");

        JWTClaimsSet jwtClaimsSet = SignedJWT.parse(accessToken).getJWTClaimsSet();

        assertTrue(jwtClaimsSet.getStringClaim("binding_type").equalsIgnoreCase(BINDING_TYPE),
                "Binding type is not DPoP.");

        Map<String, Object> cnfClaim = jwtClaimsSet.getJSONObjectClaim("cnf");
        assertNotNull(cnfClaim, "'cnf' claim is missing in the access token");
        assertNotNull(cnfClaim.get("jkt"), "'jkt' claim is missing inside the 'cnf' claim");
    }

    @Test(groups = "wso2.is", description = "Get a DPoP access token with an expired proof",
            dependsOnMethods = "getDPoPTokenWithClientCredentials")
    public void getDPoPTokenWithExpiredDPoPProof() throws Exception {

        String dpopProof = genarateDPoPProof("RSA","DUMMYJTI", "POST",
                getTenantQualifiedURL(ACCESS_TOKEN_ENDPOINT, tenantInfo.getDomain()),
                new Date(System.currentTimeMillis() - 3_000_001), "dpop+jwt");

        List<NameValuePair> parameters = new ArrayList<>();
        parameters.add(new BasicNameValuePair("grant_type", OAUTH2_GRANT_TYPE_CLIENT_CREDENTIALS));

        List<Header> headers = new ArrayList<>();
        headers.add(new BasicHeader(AUTHORIZATION_HEADER, BASIC_HEADER + " " +
                getBase64EncodedString(clientId, oidcConfig.getClientSecret())));
        headers.add(new BasicHeader("Content-Type", "application/x-www-form-urlencoded"));
        headers.add(new BasicHeader("User-Agent", USER_AGENT));
        headers.add(new BasicHeader(DPOP_HEADER, dpopProof));

        HttpResponse response = sendPostRequest(client, headers, parameters,
                getTenantQualifiedURL(ACCESS_TOKEN_ENDPOINT, tenantInfo.getDomain()));

        String responseString = EntityUtils.toString(response.getEntity(), "UTF-8");
        JSONObject jsonResponse = new JSONObject(responseString);

        assertTrue(jsonResponse.has("error"), "Error is not present in the response.");
        assertTrue(jsonResponse.has("error_description"), "Error description is not present in the response.");
    }

    @Test(groups = "wso2.is", description = "Get SCIM users list using DPoP access token and proof",
            dependsOnMethods = "getDPoPTokenWithExpiredDPoPProof")
    public void accessResourceWithDPoPTokenFromClientCredentialsWithDifferentThumbprint() throws Exception {

        String dpopProof = genarateDPoPProof("EC", "1233482123", "GET",
                getTenantQualifiedURL(SCIM2_USERS_ENDPOINT, tenantInfo.getDomain()),
                new Date(System.currentTimeMillis()), accessToken, "dpop+jwt");

        HttpGet getRequest = new HttpGet(getTenantQualifiedURL(SCIM2_USERS_ENDPOINT, tenantInfo.getDomain()));

        getRequest.setHeader(AUTHORIZATION_HEADER, DPOP_HEADER + " " + accessToken);
        getRequest.setHeader("Content-Type", "application/x-www-form-urlencoded");
        getRequest.setHeader("User-Agent", USER_AGENT);
        getRequest.setHeader(DPOP_HEADER, dpopProof);

        HttpResponse response = client.execute(getRequest);

        assertEquals(response.getStatusLine().getStatusCode(), 401, "Response for User listing has" +
                " failed");
    }

    @Test(groups = "wso2.is", description = "Get SCIM users list using DPoP access token and proof",
            dependsOnMethods = "accessResourceWithDPoPTokenFromClientCredentialsWithDifferentThumbprint")
    public void accessResourceWithDPoPTokenFromClientCredentials() throws Exception {

        String dpopProof = genarateDPoPProof("RSA", "1233482123", "GET",
                getTenantQualifiedURL(SCIM2_USERS_ENDPOINT, tenantInfo.getDomain()),
                new Date(System.currentTimeMillis()), accessToken, "dpop+jwt");

        jkt = getThumbprintOfKeyFromDpopProof(dpopProof);

        HttpGet getRequest = new HttpGet(getTenantQualifiedURL(SCIM2_USERS_ENDPOINT, tenantInfo.getDomain()));

        getRequest.setHeader(AUTHORIZATION_HEADER, DPOP_HEADER + " " + accessToken);
        getRequest.setHeader("Content-Type", "application/x-www-form-urlencoded");
        getRequest.setHeader("User-Agent", USER_AGENT);
        getRequest.setHeader(DPOP_HEADER, dpopProof);

        HttpResponse response = client.execute(getRequest);

        assertEquals(response.getStatusLine().getStatusCode(), 200, "Response for User listing is" +
                " failed");
    }

    @Test(groups = "wso2.is", description = "Send authorize user request and bind DPoP proofs thumbprint",
            dependsOnMethods = "accessResourceWithDPoPTokenFromClientCredentials")
    public void testSendAuthorizeRequest() throws Exception{

        refreshHTTPClient();

        accessTokenConfig.setType("Default");
        oidcConfig.setAccessToken(accessTokenConfig);
        updateApplicationInboundConfig(appId, oidcConfig, OIDC);

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("response_type", OAUTH2_GRANT_TYPE_CODE));
        urlParameters.add(new BasicNameValuePair("client_id", clientId));
        urlParameters.add(new BasicNameValuePair("redirect_uri", CALLBACK_URL));
        urlParameters.add(new BasicNameValuePair("scope", INTERNAL_USER_MGT_LIST));
        urlParameters.add(new BasicNameValuePair("dpop_jkt", jkt));

        HttpResponse response = sendPostRequestWithParameters(client, urlParameters,
                getTenantQualifiedURL(AUTHORIZE_ENDPOINT_URL, tenantInfo.getDomain()));

        Header locationHeader = response.getFirstHeader(HTTP_RESPONSE_HEADER_LOCATION);
        assertNotNull(locationHeader, "Location header for authorization request is null.");
        EntityUtils.consume(response.getEntity());

        response = sendGetRequest(client, locationHeader.getValue());

        Map<String, Integer> keyPositionMap = new HashMap<>(1);
        keyPositionMap.put("name=\"sessionDataKey\"", 1);
        List<DataExtractUtil.KeyValue> keyValues = DataExtractUtil.extractDataFromResponse(response, keyPositionMap);
        assertNotNull(keyValues, "Session data key is null.");

        sessionDataKey = keyValues.get(0).getValue();
        assertNotNull(sessionDataKey, "Session data key is null.");
        EntityUtils.consume(response.getEntity());
    }

    @Test(groups = "wso2.is", description = "Send login post request", dependsOnMethods = "testSendAuthorizeRequest")
    public void testSendLoginPost() throws Exception {

        HttpResponse response = sendLoginPostForCustomUsers(client, sessionDataKey, TEST_USER, ADMIN_WSO2);

        Header locationHeader = response.getFirstHeader(HTTP_RESPONSE_HEADER_LOCATION);
        assertNotNull(locationHeader, "Location header for post login is null.");
        EntityUtils.consume(response.getEntity());

        response = sendGetRequest(client, locationHeader.getValue());
        locationHeader = response.getFirstHeader(HTTP_RESPONSE_HEADER_LOCATION);
        assertNotNull(locationHeader, "Redirection URL to the application with authorization code is null");
        EntityUtils.consume(response.getEntity());

        authorizationCode = getAuthorizationCodeFromURL(locationHeader.getValue());
        assertNotNull(authorizationCode, "Authorization code is null.");
    }

    @Test(groups = "wso2.is", description = "Get DPoP access token with Authorization code",
            dependsOnMethods = "testSendLoginPost")
    public void testGetAccessTokenWithAuthorizationCode() throws Exception {

        String dpopProof = genarateDPoPProof("RSA","DUMMYJTI", "POST",
                getTenantQualifiedURL(ACCESS_TOKEN_ENDPOINT, tenantInfo.getDomain()),
                new Date(System.currentTimeMillis()),"dpop+jwt" );

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("code", authorizationCode));
        urlParameters.add(new BasicNameValuePair("grant_type", OAUTH2_GRANT_TYPE_AUTHORIZATION_CODE));
        urlParameters.add(new BasicNameValuePair("redirect_uri", CALLBACK_URL));
        urlParameters.add(new BasicNameValuePair("client_id", clientId));

        List<Header> headers = new ArrayList<>();
        headers.add(new BasicHeader(AUTHORIZATION_HEADER, BASIC_HEADER + " " +
                getBase64EncodedString(clientId, oidcConfig.getClientSecret())));
        headers.add(new BasicHeader("Content-Type", "application/x-www-form-urlencoded"));
        headers.add(new BasicHeader("User-Agent", USER_AGENT));
        headers.add(new BasicHeader(DPOP_HEADER, dpopProof));

        HttpResponse response = sendPostRequest(client, headers, urlParameters,
                getTenantQualifiedURL(ACCESS_TOKEN_ENDPOINT, tenantInfo.getDomain()));
        assertNotNull(response, "Failed to receive response for access token request.");

        String responseString = EntityUtils.toString(response.getEntity(), "UTF-8");
        JSONObject jsonResponse = new JSONObject(responseString);

        assertTrue(jsonResponse.has("access_token"), "Access token is not present in the response.");
        accessToken = jsonResponse.getString("access_token");
        assertNotNull(accessToken, "Access token is null.");

        refreshToken = jsonResponse.getString("refresh_token");
        assertNotNull(refreshToken, "Refresh token is null.");

        String scope = jsonResponse.getString("scope");
        assertTrue(scope.equalsIgnoreCase(INTERNAL_USER_MGT_LIST), "Scope is not present in the response.");

        assertTrue(jsonResponse.getString("token_type").equalsIgnoreCase(TOKEN_TYPE),
                "Token type is not DPoP.");
    }


    @Test(groups = "wso2.is", description = "Get SCIM2 users list using DPoP access token and proof",
            dependsOnMethods = "testGetAccessTokenWithAuthorizationCode")
    public void accessResourceWithDPoPTokenFromAuthCode() throws Exception{

        String dpopProof = genarateDPoPProof("RSA", "1233482123", "GET",
                getTenantQualifiedURL(SCIM2_USERS_ENDPOINT, tenantInfo.getDomain()),
                new Date(System.currentTimeMillis()), accessToken, "dpop+jwt");

        HttpGet getRequest = new HttpGet(getTenantQualifiedURL(SCIM2_USERS_ENDPOINT, tenantInfo.getDomain()));

        getRequest.setHeader(AUTHORIZATION_HEADER, DPOP_HEADER + " " + accessToken);
        getRequest.setHeader("Content-Type", "application/x-www-form-urlencoded");
        getRequest.setHeader("User-Agent", USER_AGENT);
        getRequest.setHeader(DPOP_HEADER, dpopProof);

        HttpResponse response = client.execute(getRequest);

        assertEquals(response.getStatusLine().getStatusCode(), 200, "Response for User listing is" +
                " failed");
    }

    @Test(groups = "wso2.is", description = "Get a DPoP access token using a refresh token",
            dependsOnMethods = "accessResourceWithDPoPTokenFromAuthCode")
    public void testGetAccessTokenWithRefreshToken() throws Exception {

        String dpopProof = genarateDPoPProof("RSA","DUMMYJTI", "POST",
                getTenantQualifiedURL(ACCESS_TOKEN_ENDPOINT, tenantInfo.getDomain()),
                new Date(System.currentTimeMillis()),"dpop+jwt" );

        List<NameValuePair> parameters = new ArrayList<>();
        parameters.add(new BasicNameValuePair("grant_type", OAUTH2_GRANT_TYPE_REFRESH_TOKEN));
        parameters.add(new BasicNameValuePair(OAUTH2_GRANT_TYPE_REFRESH_TOKEN, refreshToken));

        List<Header> headers = new ArrayList<>();
        headers.add(new BasicHeader(AUTHORIZATION_HEADER, BASIC_HEADER + " " +
                getBase64EncodedString(clientId, oidcConfig.getClientSecret())));
        headers.add(new BasicHeader("Content-Type", "application/x-www-form-urlencoded"));
        headers.add(new BasicHeader("User-Agent", USER_AGENT));
        headers.add(new BasicHeader(DPOP_HEADER, dpopProof));

        HttpResponse response = sendPostRequest(client, headers, parameters,
                getTenantQualifiedURL(ACCESS_TOKEN_ENDPOINT, tenantInfo.getDomain()));

        String responseString = EntityUtils.toString(response.getEntity(), "UTF-8");
        JSONObject jsonResponse = new JSONObject(responseString);

        assertTrue(jsonResponse.has("access_token"), "Access token is not present in the response.");
        assertTrue(jsonResponse.has("refresh_token"), "Refresh token not found in the token response.");
        assertTrue(jsonResponse.has("expires_in"), "Expiry time not found in the token response.");
        assertTrue(jsonResponse.has("token_type"), "Token type not found in the token response.");

        accessToken = jsonResponse.getString("access_token");
        assertNotNull(accessToken, "Access token is null.");

        refreshToken = jsonResponse.getString("refresh_token");
        assertNotNull(refreshToken, "Refresh token is null.");

        String tokenType = jsonResponse.getString("token_type");
        assertTrue(tokenType.equalsIgnoreCase(TOKEN_TYPE), "Token type is not DPoP.");
    }

    @Test(groups = "wso2.is", description = "Get a DPoP access token using the password grant",
            dependsOnMethods = "testGetAccessTokenWithRefreshToken")
    public void testGetAccessTokenWithPassword() throws Exception {

        String dpopProof = genarateDPoPProof("RSA","DUMMYJTIQWE", "POST",
                getTenantQualifiedURL(ACCESS_TOKEN_ENDPOINT, tenantInfo.getDomain()),
                new Date(System.currentTimeMillis()),"dpop+jwt" );

        List<NameValuePair> parameters = new ArrayList<>();
        parameters.add(new BasicNameValuePair("grant_type", OAuth2Constant.OAUTH2_GRANT_TYPE_RESOURCE_OWNER));
        parameters.add(new BasicNameValuePair("username", TEST_USER));
        parameters.add(new BasicNameValuePair("password", ADMIN_WSO2));
        parameters.add(new BasicNameValuePair("scope", INTERNAL_USER_MGT_LIST));

        List<Header> headers = new ArrayList<>();
        headers.add(new BasicHeader(AUTHORIZATION_HEADER, BASIC_HEADER + " " +
                getBase64EncodedString(clientId, oidcConfig.getClientSecret())));
        headers.add(new BasicHeader("Content-Type", "application/x-www-form-urlencoded"));
        headers.add(new BasicHeader("User-Agent", USER_AGENT));
        headers.add(new BasicHeader(DPOP_HEADER, dpopProof));

        HttpResponse response = sendPostRequest(client, headers, parameters,
                getTenantQualifiedURL(ACCESS_TOKEN_ENDPOINT, tenantInfo.getDomain()));

        String responseString = EntityUtils.toString(response.getEntity(), "UTF-8");
        JSONObject jsonResponse = new JSONObject(responseString);

        assertTrue(jsonResponse.has("access_token"), "Access token is not present in the response.");
        assertTrue(jsonResponse.has("expires_in"), "Expires in is not present in the response.");
        accessToken = jsonResponse.getString("access_token");
        assertNotNull(accessToken, "Access token is null.");

        String tokenType = jsonResponse.getString("token_type");
        assertTrue(tokenType.equalsIgnoreCase(TOKEN_TYPE), "Token type is not DPoP.");

        String scope = jsonResponse.getString("scope");
        assertTrue(scope.equalsIgnoreCase(INTERNAL_USER_MGT_LIST), "Scope is not present in the response.");
    }

    @Test(groups = "wso2.is", description = "Send a Push Authorization Request with DPoP proof thumbprint",
            dependsOnMethods = "testGetAccessTokenWithPassword")
    public void testSendPar() throws Exception {

        refreshHTTPClient();

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("response_type", OAUTH2_GRANT_TYPE_CODE));
        urlParameters.add(new BasicNameValuePair("client_id", clientId));
        urlParameters.add(new BasicNameValuePair("redirect_uri", CALLBACK_URL));
        urlParameters.add(new BasicNameValuePair("scope", INTERNAL_USER_MGT_LIST));
        urlParameters.add(new BasicNameValuePair("dpop_jkt", jkt));

        List<Header> headers = new ArrayList<>();
        headers.add(new BasicHeader(AUTHORIZATION_HEADER, BASIC_HEADER + " " +
                getBase64EncodedString(clientId, oidcConfig.getClientSecret())));
        headers.add(new BasicHeader("Content-Type", "application/x-www-form-urlencoded"));
        headers.add(new BasicHeader("User-Agent", USER_AGENT));

        HttpResponse response = sendPostRequest(client, headers, urlParameters,
                getTenantQualifiedURL(PAR_ENDPOINT, tenantInfo.getDomain()));

        String responseString = EntityUtils.toString(response.getEntity(), "UTF-8");
        JSONObject jsonResponse = new JSONObject(responseString);

        assertTrue(jsonResponse.has("request_uri"), "Request URI is not present in the response.");
        assertTrue(jsonResponse.has("expires_in"), "Expires in is not present in the response.");

        requestURI = jsonResponse.getString("request_uri");
        assertNotNull(requestURI, "Request URI is null.");
    }

    @Test(groups = "wso2.is", description = "Send a DPoP authorization request with the request URI",
            dependsOnMethods = "testSendPar")
    public void testSendAuthorizationRequestWithPar() throws Exception {

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("client_id", clientId));
        urlParameters.add(new BasicNameValuePair("request_uri", requestURI));

        HttpResponse response = sendPostRequestWithParameters(client, urlParameters,
                getTenantQualifiedURL(AUTHORIZE_ENDPOINT_URL, tenantInfo.getDomain()));

        Header locationHeader = response.getFirstHeader(HTTP_RESPONSE_HEADER_LOCATION);
        assertNotNull(locationHeader, "Location header for authorization request is null.");
        EntityUtils.consume(response.getEntity());

        response = sendGetRequest(client, locationHeader.getValue());

        Map<String, Integer> keyPositionMap = new HashMap<>(1);
        keyPositionMap.put("name=\"sessionDataKey\"", 1);
        List<DataExtractUtil.KeyValue> keyValues = DataExtractUtil.extractDataFromResponse(response, keyPositionMap);
        assertNotNull(keyValues, "Session data key is null.");

        sessionDataKey = keyValues.get(0).getValue();
        assertNotNull(sessionDataKey, "Session data key is null.");
    }

    @Test(groups = "wso2.is", description = "Send login post request with the request URI",
            dependsOnMethods = "testSendAuthorizationRequestWithPar")
    public void testSendLoginPostWithPar() throws Exception {

        HttpResponse response = sendLoginPostForCustomUsers(client, sessionDataKey, TEST_USER, ADMIN_WSO2);

        Header locationHeader = response.getFirstHeader(HTTP_RESPONSE_HEADER_LOCATION);
        assertNotNull(locationHeader, "Location header for post login is null.");
        EntityUtils.consume(response.getEntity());

        response = sendGetRequest(client, locationHeader.getValue());
        locationHeader = response.getFirstHeader(HTTP_RESPONSE_HEADER_LOCATION);
        assertNotNull(locationHeader, "Redirection URL to the application with authorization code is null");
        EntityUtils.consume(response.getEntity());

        authorizationCode = getAuthorizationCodeFromURL(locationHeader.getValue());
        assertNotNull(authorizationCode, "Authorization code is null.");
    }

    @Test(groups = "wso2.is", description = "Get DPoP access token with Authorization code using PAR with DPoP proof" +
            " not bound to the authorization request",
            dependsOnMethods = "testSendLoginPostWithPar")
    public void testGetAccessTokenWithAuthorizationCodeUsingParWithDifferentThumbprint() throws Exception {

        String dpopProof = genarateDPoPProof("EC","DUMMYJTI", "POST",
                getTenantQualifiedURL(ACCESS_TOKEN_ENDPOINT, tenantInfo.getDomain()),
                new Date(System.currentTimeMillis()),"dpop+jwt" );

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("code", authorizationCode));
        urlParameters.add(new BasicNameValuePair("grant_type", OAUTH2_GRANT_TYPE_AUTHORIZATION_CODE));
        urlParameters.add(new BasicNameValuePair("redirect_uri", CALLBACK_URL));
        urlParameters.add(new BasicNameValuePair("client_id", clientId));

        List<Header> headers = new ArrayList<>();
        headers.add(new BasicHeader(AUTHORIZATION_HEADER, BASIC_HEADER + " " +
                getBase64EncodedString(clientId, oidcConfig.getClientSecret())));
        headers.add(new BasicHeader("Content-Type", "application/x-www-form-urlencoded"));
        headers.add(new BasicHeader("User-Agent", USER_AGENT));
        headers.add(new BasicHeader(DPOP_HEADER, dpopProof));

        HttpResponse response = sendPostRequest(client, headers, urlParameters,
                getTenantQualifiedURL(ACCESS_TOKEN_ENDPOINT, tenantInfo.getDomain()));

        String responseString = EntityUtils.toString(response.getEntity(), "UTF-8");
        JSONObject jsonResponse = new JSONObject(responseString);

        assertTrue(jsonResponse.has("error"), "Error is not present in the response.");
        assertTrue(jsonResponse.has("error_description"),
                "Error description is not present in the response.");
    }

    @Test(groups = "wso2.is", description = "Get DPoP access token with Authorization code using PAR",
            dependsOnMethods = "testGetAccessTokenWithAuthorizationCodeUsingParWithDifferentThumbprint")
    public void testGetAccessTokenWithAuthorizationCodeUsingPar() throws Exception {

        String dpopProof = genarateDPoPProof("RSA","DUMMYJTI", "POST",
                getTenantQualifiedURL(ACCESS_TOKEN_ENDPOINT, tenantInfo.getDomain()),
                new Date(System.currentTimeMillis()),"dpop+jwt" );

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("code", authorizationCode));
        urlParameters.add(new BasicNameValuePair("grant_type", OAUTH2_GRANT_TYPE_AUTHORIZATION_CODE));
        urlParameters.add(new BasicNameValuePair("redirect_uri", CALLBACK_URL));
        urlParameters.add(new BasicNameValuePair("client_id", clientId));

        List<Header> headers = new ArrayList<>();
        headers.add(new BasicHeader(AUTHORIZATION_HEADER, BASIC_HEADER + " " +
                getBase64EncodedString(clientId, oidcConfig.getClientSecret())));
        headers.add(new BasicHeader("Content-Type", "application/x-www-form-urlencoded"));
        headers.add(new BasicHeader("User-Agent", USER_AGENT));
        headers.add(new BasicHeader(DPOP_HEADER, dpopProof));

        HttpResponse response = sendPostRequest(client, headers, urlParameters,
                getTenantQualifiedURL(ACCESS_TOKEN_ENDPOINT, tenantInfo.getDomain()));
        assertNotNull(response, "Failed to receive response for access token request.");

        String responseString = EntityUtils.toString(response.getEntity(), "UTF-8");
        JSONObject jsonResponse = new JSONObject(responseString);

        assertTrue(jsonResponse.has("access_token"), "Access token is not present in the response.");
        accessToken = jsonResponse.getString("access_token");
        assertNotNull(accessToken, "Access token is null.");

        refreshToken = jsonResponse.getString("refresh_token");
        assertNotNull(refreshToken, "Refresh token is null.");

        String scope = jsonResponse.getString("scope");
        assertTrue(scope.equalsIgnoreCase(INTERNAL_USER_MGT_LIST), "Scope is not present in the response.");

        assertTrue(jsonResponse.getString("token_type").equalsIgnoreCase(TOKEN_TYPE),
                "Token type is not DPoP.");
    }

    /**
     * Get authorization code from the provided URL.
     *
     * @param location Location header
     * @return Authorization code
     */
    private String getAuthorizationCodeFromURL(String location) {

        URI uri = URI.create(location);
        return URLEncodedUtils.parse(uri, StandardCharsets.UTF_8).stream()
                .filter(param -> "code".equals(param.getName()))
                .map(NameValuePair::getValue)
                .findFirst()
                .orElse(null);
    }

    /**
     * Create a user with an application audience role with SCIM2 User API  list permission.
     *
     * @throws Exception If an error occurred while adding a user.
     */
    private void addUser() throws Exception {

        List<Permission> userPermissions = new ArrayList<>();
        Collections.addAll(userPermissions, new Permission(INTERNAL_USER_MGT_LIST));

        Audience roleAudience = new Audience(APPLICATION_AUDIENCE , appId);
        RoleV2 role = new RoleV2(roleAudience, TEST_ROLE_APPLICATION, userPermissions, Collections.emptyList());
        roleId = addRole(role);

        UserObject userInfo = new UserObject();
        userInfo.setUserName(TEST_USER);
        userInfo.setPassword(ADMIN_WSO2);
        userInfo.setName(new Name().givenName(TEST_USER_FIRST_NAME));
        userInfo.getName().setFamilyName(TEST_USER_LAST_NAME);
        userInfo.addEmail(new Email().value(USER_EMAIL));

        userId = scim2RestClient.createUser(userInfo);

        RoleItemAddGroupobj rolePatchReqObject = new RoleItemAddGroupobj();
        rolePatchReqObject.setOp(RoleItemAddGroupobj.OpEnum.ADD);
        rolePatchReqObject.setPath(USERS);
        rolePatchReqObject.addValue(new ListObject().value(userId));
        scim2RestClient.updateUserRole(new PatchOperationRequestObject().addOperations(rolePatchReqObject), roleId);
    }

    /**
     * Refresh the cookie store and http client.
     */
    private void refreshHTTPClient () throws Exception {

        if (client != null) {
            client.close();
        }

        cookieStore.clear();
        client = HttpClientBuilder.create()
                .setDefaultCookieStore(cookieStore)
                .setDefaultCookieSpecRegistry(cookieSpecRegistry)
                .setDefaultRequestConfig(requestConfig)
                .setRedirectStrategy(new DefaultRedirectStrategy() {
                    @Override
                    protected boolean isRedirectable(String method) {

                        return false;
                    }
                })
                .build();
    }
}

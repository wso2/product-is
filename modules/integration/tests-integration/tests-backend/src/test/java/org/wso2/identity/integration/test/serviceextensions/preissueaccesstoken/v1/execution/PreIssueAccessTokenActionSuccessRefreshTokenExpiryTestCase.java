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

package org.wso2.identity.integration.test.serviceextensions.preissueaccesstoken.v1.execution;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.apache.commons.lang.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.identity.integration.test.oauth2.dataprovider.model.ApplicationConfig;
import org.wso2.identity.integration.test.oauth2.dataprovider.model.UserClaimConfig;
import org.wso2.identity.integration.test.rest.api.server.action.management.v1.common.model.ActionModel;
import org.wso2.identity.integration.test.rest.api.server.action.management.v1.common.model.AuthenticationType;
import org.wso2.identity.integration.test.rest.api.server.action.management.v1.common.model.Endpoint;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationResponseModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.OpenIDConnectConfiguration;
import org.wso2.identity.integration.test.rest.api.user.common.model.Email;
import org.wso2.identity.integration.test.rest.api.user.common.model.Name;
import org.wso2.identity.integration.test.rest.api.user.common.model.UserObject;
import org.wso2.identity.integration.test.restclients.SCIM2RestClient;
import org.wso2.identity.integration.test.serviceextensions.common.execution.ActionsBaseTestCase;
import org.wso2.identity.integration.test.serviceextensions.common.execution.mockservices.ServiceExtensionMockServer;
import org.wso2.identity.integration.test.utils.DataExtractUtil;
import org.wso2.identity.integration.test.utils.FileUtils;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.xpath.XPathExpressionException;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.ACCESS_TOKEN_ENDPOINT;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.AUTHORIZATION_HEADER;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.AUTHORIZE_ENDPOINT_URL;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.INTRO_SPEC_ENDPOINT;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.OAUTH2_GRANT_TYPE_AUTHORIZATION_CODE;

/**
 * Tests the pre-issue access token action success scenarios with refresh token grant type.
 */
public class PreIssueAccessTokenActionSuccessRefreshTokenExpiryTestCase extends ActionsBaseTestCase {

    private static final String TEST_USER = "test_user";
    private static final String TEST_WSO2 = "Test@wso2";
    private static final String PRE_ISSUE_ACCESS_TOKEN_API_PATH = "preIssueAccessToken";
    private static final String MOCK_SERVER_ENDPOINT_RESOURCE_PATH = "/test/action";
    private static final int APP_CONFIGURED_ACCESS_TOKEN_EXPIRY_TIME = 3600;
    private static final int APP_CONFIGURED_REFRESH_TOKEN_EXPIRY_TIME = 84600;
    private static final int UPDATED_REFRESH_TOKEN_EXPIRY_TIME_BY_ACTION_AT_CODE_GRANT = 47400;
    private static final int UPDATED_REFRESH_TOKEN_EXPIRY_TIME_BY_ACTION_AT_REFRESH_GRANT = 48600;
    private CloseableHttpClient client;
    private SCIM2RestClient scim2RestClient;
    private List<String> requestedScopes;
    private String sessionDataKey;
    private String authorizationCode;
    private String clientId;
    private String clientSecret;
    private String actionId;
    private String applicationId;
    private String userId;
    private String accessToken;
    private String refreshToken;
    private JWTClaimsSet accessTokenClaims;
    private final TestUserMode userMode;
    private ServiceExtensionMockServer serviceExtensionMockServer;

    @Factory(dataProvider = "testExecutionContextProvider")
    public PreIssueAccessTokenActionSuccessRefreshTokenExpiryTestCase(TestUserMode testUserMode) {

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
        client = HttpClientBuilder.create()
                .setRedirectStrategy(new DefaultRedirectStrategy() {
                    @Override
                    protected boolean isRedirectable(String method) {

                        return false;
                    }
                }).build();

        scim2RestClient = new SCIM2RestClient(serverURL, tenantInfo);
        applicationId = createOIDCAppWithClaims();
        actionId = createPreIssueAccessTokenAction();
        addUser();

        requestedScopes = new ArrayList<>(Arrays.asList("openid", "profile"));

        serviceExtensionMockServer = new ServiceExtensionMockServer();
        serviceExtensionMockServer.startServer();
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {

        serviceExtensionMockServer.stopServer();

        deleteAction(PRE_ISSUE_ACCESS_TOKEN_API_PATH, actionId);
        deleteApp(applicationId);
        scim2RestClient.deleteUser(userId);

        restClient.closeHttpClient();
        scim2RestClient.closeHttpClient();
        actionsRestClient.closeHttpClient();
        client.close();

        serviceExtensionMockServer = null;
        authorizationCode = null;
    }

    @BeforeMethod
    public void setupMockServerStub(Method method) throws Exception {

        if (method.getName().equals("testGetRefreshTokenWithCodeGrant")) {
            serviceExtensionMockServer.setupStub(MOCK_SERVER_ENDPOINT_RESOURCE_PATH,
                    "Basic " + getBase64EncodedString(MOCK_SERVER_AUTH_BASIC_USERNAME, MOCK_SERVER_AUTH_BASIC_PASSWORD),
                    FileUtils.readFileInClassPathAsString(
                            "actions/response/refresh-token-expiry-response-code-grant.json"), 200);
        } else if (method.getName().equals("testGetRefreshTokenFromRefreshGrant")) {
            serviceExtensionMockServer.setupStub(MOCK_SERVER_ENDPOINT_RESOURCE_PATH,
                    "Basic " + getBase64EncodedString(MOCK_SERVER_AUTH_BASIC_USERNAME, MOCK_SERVER_AUTH_BASIC_PASSWORD),
                    FileUtils.readFileInClassPathAsString(
                            "actions/response/refresh-token-expiry-response-refresh-grant.json"),
                    200);
        } else if (method.getName().equals("testGetRefreshTokenWithCodeGrantForReLogin")) {
            serviceExtensionMockServer.setupStub(MOCK_SERVER_ENDPOINT_RESOURCE_PATH,
                    "Basic " + getBase64EncodedString(MOCK_SERVER_AUTH_BASIC_USERNAME, MOCK_SERVER_AUTH_BASIC_PASSWORD),
                    FileUtils.readFileInClassPathAsString(
                            "actions/response/refresh-token-expiry-response-code-grant.json"), 200);
        }
    }

    @Test(groups = "wso2.is", description =
            "Test obtaining a refresh token using authorization code grant when the pre-issue access token action completes successfully.")
    public void testGetRefreshTokenWithCodeGrant() throws Exception {

        sendAuthorizeRequest();
        performUserLogin();
        HttpResponse response = sendTokenRequestForCodeGrant();

        String responseString = EntityUtils.toString(response.getEntity(), "UTF-8");
        JSONObject jsonResponse = new JSONObject(responseString);

        assertTokenResponse(jsonResponse);

        accessTokenClaims = getJWTClaimSetFromToken(accessToken);
        assertNotNull(accessTokenClaims);
    }

    @Test(groups = "wso2.is", description =
            "Verify that the refresh token expiry time is correctly updated by the action in the authorization code grant flow.",
            dependsOnMethods = "testGetRefreshTokenWithCodeGrant")
    public void tesRefreshTokenExpiryTimeInIntrospectForCodeGrant() throws Exception {

        JSONObject jsonResponse = invokeIntrospectEndpoint(refreshToken);
        assertIntrospectResponse(jsonResponse, UPDATED_REFRESH_TOKEN_EXPIRY_TIME_BY_ACTION_AT_CODE_GRANT);
    }

    @Test(groups = "wso2.is", description =
            "Test obtaining a refresh token using refresh token grant when the pre-issue access token action completes successfully.",
            dependsOnMethods = "tesRefreshTokenExpiryTimeInIntrospectForCodeGrant")
    public void testGetRefreshTokenFromRefreshGrant() throws Exception {

        HttpResponse response = sendTokenRequestForRefreshGrant();

        String responseString = EntityUtils.toString(response.getEntity(), "UTF-8");
        JSONObject jsonResponse = new JSONObject(responseString);

        assertTokenResponse(jsonResponse);

        accessTokenClaims = getJWTClaimSetFromToken(accessToken);
        assertNotNull(accessTokenClaims);
    }

    @Test(groups = "wso2.is", description =
            "Verify that the refresh token expiry time is correctly updated by the action in the refresh token grant flow.",
            dependsOnMethods = "testGetRefreshTokenFromRefreshGrant")
    public void tesRefreshTokenExpiryTimeInIntrospectForRefreshGrant() throws Exception {

        JSONObject jsonResponse = invokeIntrospectEndpoint(refreshToken);
        assertIntrospectResponse(jsonResponse, UPDATED_REFRESH_TOKEN_EXPIRY_TIME_BY_ACTION_AT_REFRESH_GRANT);
    }

    @Test(groups = "wso2.is", description =
            "Test obtaining a refresh token using authorization code grant when the pre-issue access token " +
                    "action completes successfully for re-login.",
            dependsOnMethods = "tesRefreshTokenExpiryTimeInIntrospectForRefreshGrant")
    public void testGetRefreshTokenWithCodeGrantForReLogin() throws Exception {

        sendAuthorizeRequestForReLogin();
        HttpResponse response = sendTokenRequestForCodeGrant();

        String responseString = EntityUtils.toString(response.getEntity(), "UTF-8");
        JSONObject jsonResponse = new JSONObject(responseString);

        assertTokenResponse(jsonResponse);

        accessTokenClaims = getJWTClaimSetFromToken(accessToken);
        assertNotNull(accessTokenClaims);
    }

    @Test(groups = "wso2.is", description =
            "Verify that the refresh token expiry time is correctly updated by the action in the " +
                    "authorization code grant on re-login.",
            dependsOnMethods = "testGetRefreshTokenWithCodeGrantForReLogin")
    public void tesRefreshTokenExpiryTimeInIntrospectForCodeGrantForReLogin() throws Exception {

        JSONObject jsonResponse = invokeIntrospectEndpoint(refreshToken);
        assertIntrospectResponse(jsonResponse, UPDATED_REFRESH_TOKEN_EXPIRY_TIME_BY_ACTION_AT_CODE_GRANT);
    }

    private void assertTokenResponse(JSONObject jsonResponse) throws JSONException {

        assertTrue(jsonResponse.has("access_token"), "Access token not found in the token response.");
        assertTrue(jsonResponse.has("refresh_token"), "Refresh token not found in the token response.");
        assertTrue(jsonResponse.has("expires_in"), "Expiry time not found in the token response.");
        assertTrue(jsonResponse.has("token_type"), "Token type not found in the token response.");

        accessToken = jsonResponse.getString("access_token");
        assertNotNull(accessToken, "Access token is null.");

        refreshToken = jsonResponse.getString("refresh_token");
        assertNotNull(refreshToken, "Refresh token is null.");

        int expiresIn = jsonResponse.getInt("expires_in");
        assertEquals(expiresIn, APP_CONFIGURED_ACCESS_TOKEN_EXPIRY_TIME, "Invalid expiry time for the access token.");

        String tokenType = jsonResponse.getString("token_type");
        assertEquals(tokenType, "Bearer", "Invalid token type for the access token.");
    }

    private void assertIntrospectResponse(JSONObject jsonResponse, long expectedRefreshTokenExpiryInSeconds)
            throws JSONException {

        assertTrue(jsonResponse.has("nbf"), "Not Before value not found in the refresh token introspection response");
        assertTrue(jsonResponse.has("exp"), "Expiry timestamp not found in the refresh token introspection response");
        long exp = jsonResponse.getLong("exp");
        assertTrue(jsonResponse.has("iat"),
                "Issued at timestamp not found in the refresh token introspection response");
        long iat = jsonResponse.getLong("iat");

        assertEquals((exp - iat), expectedRefreshTokenExpiryInSeconds,
                "Invalid expiry time for the refresh token.");

        assertTrue(jsonResponse.has("scope"), "Scopes not found in the refresh token introspection response");
        List<String> authorizedScopes = Arrays.asList(jsonResponse.getString("scope").split(" "));
        List<String> expectedScopes = requestedScopes;
        for (String expectedScope : expectedScopes) {
            assertTrue(authorizedScopes.contains(expectedScope),
                    "Scope " + expectedScope + " not found in the refresh token introspection.");
        }

        assertTrue((Boolean) jsonResponse.get("active"), "Refresh token is inactive");
        assertEquals(jsonResponse.get("token_type"), "Refresh", "Invalid token type");
        assertEquals(jsonResponse.get("client_id"), clientId,
                "Invalid client id in the refresh token introspection response");
    }

    private JSONObject invokeIntrospectEndpoint(String refreshToken)
            throws XPathExpressionException, IOException, JSONException {

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("token", refreshToken));

        List<Header> headers = new ArrayList<>();
        headers.add(new BasicHeader(AUTHORIZATION_HEADER, OAuth2Constant.BASIC_HEADER + " " +
                getBase64EncodedString(isServer.getContextTenant().getTenantAdmin().getUserName(),
                        isServer.getContextTenant().getTenantAdmin().getPassword())));
        headers.add(new BasicHeader("Content-Type", "application/x-www-form-urlencoded"));
        headers.add(new BasicHeader("User-Agent", OAuth2Constant.USER_AGENT));

        HttpResponse response = sendPostRequest(client, headers, urlParameters,
                getTenantQualifiedURL(INTRO_SPEC_ENDPOINT, tenantInfo.getDomain()));

        assertNotNull(response, "Failed to receive a response for introspection request.");
        assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_OK,
                response.getStatusLine().getReasonPhrase());

        String responseString = EntityUtils.toString(response.getEntity(), "UTF-8");
        EntityUtils.consume(response.getEntity());
        assertTrue(StringUtils.isNotBlank(responseString));

        return new JSONObject(responseString);
    }

    private HttpResponse sendTokenRequestForRefreshGrant() throws IOException {

        List<NameValuePair> parameters = new ArrayList<>();
        parameters.add(new BasicNameValuePair("grant_type", OAuth2Constant.OAUTH2_GRANT_TYPE_REFRESH_TOKEN));
        parameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_GRANT_TYPE_REFRESH_TOKEN, refreshToken));

        List<Header> headers = new ArrayList<>();
        headers.add(new BasicHeader(AUTHORIZATION_HEADER,
                OAuth2Constant.BASIC_HEADER + " " + getBase64EncodedString(clientId, clientSecret)));
        headers.add(new BasicHeader("Content-Type", "application/x-www-form-urlencoded"));
        headers.add(new BasicHeader("User-Agent", OAuth2Constant.USER_AGENT));

        return sendPostRequest(client, headers, parameters,
                getTenantQualifiedURL(ACCESS_TOKEN_ENDPOINT, tenantInfo.getDomain()));
    }

    private void sendAuthorizeRequest() throws Exception {

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("response_type", OAuth2Constant.OAUTH2_GRANT_TYPE_CODE));
        urlParameters.add(new BasicNameValuePair("client_id", clientId));
        urlParameters.add(new BasicNameValuePair("redirect_uri", OAuth2Constant.CALLBACK_URL));

        String scopes = String.join(" ", requestedScopes);
        urlParameters.add(new BasicNameValuePair("scope", scopes));

        HttpResponse response = sendPostRequestWithParameters(client, urlParameters,
                getTenantQualifiedURL(AUTHORIZE_ENDPOINT_URL, tenantInfo.getDomain()));

        Header locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        assertNotNull(locationHeader, "Location header expected for authorize request is not available");
        EntityUtils.consume(response.getEntity());

        response = sendGetRequest(client, locationHeader.getValue());

        Map<String, Integer> keyPositionMap = new HashMap<>(1);
        keyPositionMap.put("name=\"sessionDataKey\"", 1);
        List<DataExtractUtil.KeyValue> keyValues =
                DataExtractUtil.extractDataFromResponse(response, keyPositionMap);
        assertNotNull(keyValues, "SessionDataKey key value is null");

        sessionDataKey = keyValues.get(0).getValue();
        assertNotNull(sessionDataKey, "Session data key is null");
        EntityUtils.consume(response.getEntity());
    }

    private void sendAuthorizeRequestForReLogin() throws Exception {

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("response_type", OAuth2Constant.OAUTH2_GRANT_TYPE_CODE));
        urlParameters.add(new BasicNameValuePair("client_id", clientId));
        urlParameters.add(new BasicNameValuePair("redirect_uri", OAuth2Constant.CALLBACK_URL));

        String scopes = String.join(" ", requestedScopes);
        urlParameters.add(new BasicNameValuePair("scope", scopes));

        HttpResponse response = sendPostRequestWithParameters(client, urlParameters,
                getTenantQualifiedURL(AUTHORIZE_ENDPOINT_URL, tenantInfo.getDomain()));

        Header locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        assertNotNull(locationHeader, "Redirection URL to the application with authorization code is null.");
        EntityUtils.consume(response.getEntity());

        authorizationCode = getAuthorizationCodeFromURL(locationHeader.getValue());
        assertNotNull(authorizationCode);
    }

    private void performUserLogin() throws Exception {

        HttpResponse response = sendLoginPostForCustomUsers(client, sessionDataKey, TEST_USER, TEST_WSO2);

        Header locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        assertNotNull(locationHeader, "Location header expected post login is not available.");
        EntityUtils.consume(response.getEntity());

        response = sendGetRequest(client, locationHeader.getValue());
        locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        assertNotNull(locationHeader, "Redirection URL to the application with authorization code is null.");
        EntityUtils.consume(response.getEntity());

        authorizationCode = getAuthorizationCodeFromURL(locationHeader.getValue());
        assertNotNull(authorizationCode);
    }

    private HttpResponse sendTokenRequestForCodeGrant() throws Exception {

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("code", authorizationCode));
        urlParameters.add(new BasicNameValuePair("grant_type", OAUTH2_GRANT_TYPE_AUTHORIZATION_CODE));
        urlParameters.add(new BasicNameValuePair("redirect_uri", OAuth2Constant.CALLBACK_URL));
        urlParameters.add(new BasicNameValuePair("client_id", clientId));

        String scopes = String.join(" ", requestedScopes);
        urlParameters.add(new BasicNameValuePair("scope", scopes));

        List<Header> headers = new ArrayList<>();
        headers.add(new BasicHeader(AUTHORIZATION_HEADER,
                OAuth2Constant.BASIC_HEADER + " " + getBase64EncodedString(clientId, clientSecret)));
        headers.add(new BasicHeader("Content-Type", "application/x-www-form-urlencoded"));
        headers.add(new BasicHeader("User-Agent", OAuth2Constant.USER_AGENT));

        return sendPostRequest(client, headers, urlParameters,
                getTenantQualifiedURL(ACCESS_TOKEN_ENDPOINT, tenantInfo.getDomain()));
    }

    private String getAuthorizationCodeFromURL(String location) {

        URI uri = URI.create(location);
        return URLEncodedUtils.parse(uri, StandardCharsets.UTF_8).stream()
                .filter(param -> "code".equals(param.getName()))
                .map(NameValuePair::getValue)
                .findFirst()
                .orElse(null);
    }

    private String createPreIssueAccessTokenAction() throws IOException {

        AuthenticationType authenticationType = new AuthenticationType();
        authenticationType.setType(AuthenticationType.TypeEnum.BASIC);
        Map<String, Object> authProperties = new HashMap<>();
        authProperties.put(USERNAME_PROPERTY, MOCK_SERVER_AUTH_BASIC_USERNAME);
        authProperties.put(PASSWORD_PROPERTY, MOCK_SERVER_AUTH_BASIC_PASSWORD);
        authenticationType.setProperties(authProperties);

        Endpoint endpoint = new Endpoint();
        endpoint.setUri(EXTERNAL_SERVICE_URI);
        endpoint.setAuthentication(authenticationType);
        endpoint.addAllowedHeadersItem("testHeader");
        endpoint.addAllowedParametersItem("testParam");

        ActionModel actionModel = new ActionModel();
        actionModel.setName("Access Token Pre Issue");
        actionModel.setDescription("This is a test pre issue access token type");
        actionModel.setEndpoint(endpoint);

        return createAction(PRE_ISSUE_ACCESS_TOKEN_API_PATH, actionModel);
    }

    private void addUser() throws Exception {

        UserObject userInfo = new UserObject();
        userInfo.setUserName(TEST_USER);
        userInfo.setPassword(TEST_WSO2);
        userInfo.setName(new Name().givenName("test_user_given_name"));
        userInfo.getName().setFamilyName("test_user_last_name");
        userInfo.addEmail(new Email().value("test.user@gmail.com"));
        userId = scim2RestClient.createUser(userInfo);
    }

    private String createOIDCAppWithClaims() throws Exception {

        List<UserClaimConfig> userClaimConfigs = Arrays.asList(
                new UserClaimConfig.Builder().localClaimUri("http://wso2.org/claims/givenname").
                        oidcClaimUri("given_name").build(),
                new UserClaimConfig.Builder().localClaimUri("http://wso2.org/claims/lastname").
                        oidcClaimUri("family_name").build()
                                                              );

        ApplicationConfig applicationConfig = new ApplicationConfig.Builder()
                .claimsList(userClaimConfigs)
                .grantTypes(new ArrayList<>(Arrays.asList("authorization_code", "refresh_token")))
                .tokenType(ApplicationConfig.TokenType.JWT)
                .expiryTime(APP_CONFIGURED_ACCESS_TOKEN_EXPIRY_TIME)
                .refreshTokenExpiryTime(APP_CONFIGURED_REFRESH_TOKEN_EXPIRY_TIME)
                .skipConsent(true)
                .build();

        ApplicationResponseModel application = addApplication(applicationConfig);
        String applicationId = application.getId();

        OpenIDConnectConfiguration oidcConfig = getOIDCInboundDetailsOfApplication(applicationId);
        clientId = oidcConfig.getClientId();
        clientSecret = oidcConfig.getClientSecret();

        return applicationId;
    }

    private JWTClaimsSet getJWTClaimSetFromToken(String jwtToken) throws ParseException {

        SignedJWT signedJWT = SignedJWT.parse(jwtToken);
        return signedJWT.getJWTClaimsSet();
    }

}

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

package org.wso2.identity.integration.test.actions;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.apache.commons.lang.ArrayUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.identity.integration.test.actions.mockserver.ActionsMockServer;
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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.ACCESS_TOKEN_ENDPOINT;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.AUTHORIZATION_HEADER;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.AUTHORIZE_ENDPOINT_URL;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.OAUTH2_GRANT_TYPE_AUTHORIZATION_CODE;

/**
 * Tests the pre-issue access token action success scenarios with refresh token grant type.
 */
public class PreIssueAccessTokenActionSuccessRefreshTokenGrantTestCase extends ActionsBaseTestCase {

    private static final String TEST_USER = "test_user";
    private static final String TEST_WSO2 = "Test@wso2";
    private static final String PRE_ISSUE_ACCESS_TOKEN_API_PATH = "preIssueAccessToken";
    private static final String MOCK_SERVER_ENDPOINT_RESOURCE_PATH = "/test/action";
    private static final int APP_CONFIGURED_EXPIRY_TIME = 3600;
    private static final int UPDATED_EXPIRY_TIME_BY_ACTION = 7200;
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
    private ActionsMockServer actionsMockServer;

    @Factory(dataProvider = "testExecutionContextProvider")
    public PreIssueAccessTokenActionSuccessRefreshTokenGrantTestCase(TestUserMode testUserMode) {

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

        actionsMockServer = new ActionsMockServer();
        actionsMockServer.startServer();
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {

        actionsMockServer.stopServer();

        deleteAction(PRE_ISSUE_ACCESS_TOKEN_API_PATH, actionId);
        deleteApp(applicationId);
        scim2RestClient.deleteUser(userId);

        restClient.closeHttpClient();
        scim2RestClient.closeHttpClient();
        actionsRestClient.closeHttpClient();
        client.close();

        actionsMockServer = null;
        authorizationCode = null;
    }

    @BeforeMethod
    public void setupMockServerStub(Method method) throws Exception {

        if (method.getName().equals("testGetAccessTokenWithCodeGrant")) {
            actionsMockServer.setupStub(MOCK_SERVER_ENDPOINT_RESOURCE_PATH,
                    "Basic " + getBase64EncodedString(MOCK_SERVER_AUTH_BASIC_USERNAME, MOCK_SERVER_AUTH_BASIC_PASSWORD),
                    FileUtils.readFileInClassPathAsString(
                            "actions/response/pre-issue-access-token-response-code-before-refresh.json"), 200);
        } else if (method.getName().equals("testGetAccessTokenFromRefreshToken")) {
            actionsMockServer.setupStub(MOCK_SERVER_ENDPOINT_RESOURCE_PATH,
                    "Basic " + getBase64EncodedString(MOCK_SERVER_AUTH_BASIC_USERNAME, MOCK_SERVER_AUTH_BASIC_PASSWORD),
                    FileUtils.readFileInClassPathAsString("actions/response/pre-issue-access-token-response.json"),
                    200);
        }
    }

    @Test(groups = "wso2.is", description =
            "Get access token with authorization code grant when pre-issue access token action is successful")
    public void testGetAccessTokenWithCodeGrant() throws Exception {

        sendAuthorizeRequest();
        performUserLogin();
        HttpResponse response = sendTokenRequestForCodeGrant();

        String responseString = EntityUtils.toString(response.getEntity(), "UTF-8");
        JSONObject jsonResponse = new JSONObject(responseString);

        assertTrue(jsonResponse.has("access_token"), "Access token not found in the token response.");
        assertTrue(jsonResponse.has("refresh_token"), "Refresh token not found in the token response.");
        assertTrue(jsonResponse.has("expires_in"), "Expiry time not found in the token response.");
        assertTrue(jsonResponse.has("token_type"), "Token type not found in the token response.");

        accessToken = jsonResponse.getString("access_token");
        assertNotNull(accessToken, "Access token is null.");

        refreshToken = jsonResponse.getString("refresh_token");
        assertNotNull(refreshToken, "Refresh token is null.");

        int expiresIn = jsonResponse.getInt("expires_in");
        assertEquals(expiresIn, APP_CONFIGURED_EXPIRY_TIME, "Invalid expiry time for the access token.");

        String tokenType = jsonResponse.getString("token_type");
        assertEquals(tokenType, "Bearer", "Invalid token type for the access token.");

        accessTokenClaims = getJWTClaimSetFromToken(accessToken);
        assertNotNull(accessTokenClaims);
    }

    @Test(groups = "wso2.is", description = "Verify the custom string claim in the access token added by action",
            dependsOnMethods = "testGetAccessTokenWithCodeGrant")
    public void testClaimAddOperationFromPreIssueAccessTokenActionForCodeGrant() throws Exception {

        String claimValue = accessTokenClaims.getStringClaim("custom_claim_string_0");
        Assert.assertEquals(claimValue, "testCustomClaim0");
    }

    @Test(groups = "wso2.is", description =
            "Get access token from refresh token when pre-issue access token action is successful",
            dependsOnMethods = "testGetAccessTokenWithCodeGrant")
    public void testGetAccessTokenFromRefreshToken() throws Exception {

        HttpResponse response = sendTokenRequestForRefreshGrant();

        String responseString = EntityUtils.toString(response.getEntity(), "UTF-8");
        JSONObject jsonResponse = new JSONObject(responseString);

        assertTrue(jsonResponse.has("access_token"), "Access token not found in the token response.");
        assertTrue(jsonResponse.has("refresh_token"), "Refresh token not found in the token response.");
        assertTrue(jsonResponse.has("expires_in"), "Expiry time not found in the token response.");
        assertTrue(jsonResponse.has("token_type"), "Token type not found in the token response.");

        accessToken = jsonResponse.getString("access_token");
        assertNotNull(accessToken, "Access token is null.");

        refreshToken = jsonResponse.getString("refresh_token");
        assertNotNull(refreshToken, "Refresh token is null.");

        int expiresIn = jsonResponse.getInt("expires_in");
        assertEquals(expiresIn, UPDATED_EXPIRY_TIME_BY_ACTION, "Invalid expiry time for the access token.");

        String tokenType = jsonResponse.getString("token_type");
        assertEquals(tokenType, "Bearer", "Invalid token type for the access token.");

        accessTokenClaims = getJWTClaimSetFromToken(accessToken);
        assertNotNull(accessTokenClaims);
    }

    @Test(groups = "wso2.is", description = "Verify the custom string claim added by action in " +
            "code grant is available in the access token", dependsOnMethods = "testGetAccessTokenFromRefreshToken")
    public void testClaimAddForAccessTokenFromPreIssueAccessTokenActionForRefreshTokenGrant()
            throws Exception {

        testClaimAddOperationFromPreIssueAccessTokenActionForCodeGrant();
    }

    @Test(groups = "wso2.is", description = "Verify the custom boolean claim added by action in the access token",
            dependsOnMethods = "testGetAccessTokenFromRefreshToken")
    public void testBooleanClaimAddOperationFromPreIssueAccessTokenActionForRefreshTokenGrant() throws Exception {

        boolean claimValue = accessTokenClaims.getBooleanClaim("custom_claim_boolean_1");
        Assert.assertTrue(claimValue);
    }

    @Test(groups = "wso2.is", description = "Verify the custom string claim added by action in the access token",
            dependsOnMethods = "testGetAccessTokenFromRefreshToken")
    public void testStringClaimAddOperationFromPreIssueAccessTokenActionForRefreshTokenGrant() throws Exception {

        String claimValue = accessTokenClaims.getStringClaim("custom_claim_string_1");
        Assert.assertEquals(claimValue, "testCustomClaim1");
    }

    @Test(groups = "wso2.is", description = "Verify the custom number claim added by action in the access token",
            dependsOnMethods = "testGetAccessTokenFromRefreshToken")
    public void testNumberClaimAddOperationFromPreIssueAccessTokenActionForRefreshTokenGrant() throws Exception {

        int claimValue = accessTokenClaims.getIntegerClaim("custom_claim_number_1");
        Assert.assertEquals(claimValue, 78);
    }

    @Test(groups = "wso2.is", description = "Verify the custom string array claim added by action in the " +
            "access token", dependsOnMethods = "testGetAccessTokenFromRefreshToken")
    public void testClaimArrayAddOperationFromPreIssueAccessTokenActionForRefreshTokenGrant()
            throws Exception {

        String[] expectedClaimArrayInToken = {"TestCustomClaim1", "TestCustomClaim2", "TestCustomClaim3"};

        String[] addedClaimArrayToToken = accessTokenClaims.getStringArrayClaim("custom_claim_string_array_1");
        Assert.assertEquals(addedClaimArrayToToken, expectedClaimArrayInToken);
    }

    @Test(groups = "wso2.is", description = "Verify the given_name claim replaced by the action in " +
            "access token", dependsOnMethods = "testGetAccessTokenFromRefreshToken")
    public void testGivenNameReplaceOperationFromPreIssueAccessTokenActionForRefreshTokenGrant()
            throws Exception {

        String givenNameClaim = accessTokenClaims.getStringClaim("given_name");
        Assert.assertEquals(givenNameClaim, "replaced_given_name");
    }

    @Test(groups = "wso2.is", description = "Verify the 'aud' claim updated by action in the " +
            "access token", dependsOnMethods = "testGetAccessTokenFromRefreshToken")
    public void testAUDUpdateOperationsFromPreIssueAccessTokenActionForRefreshTokenGrant() throws Exception {

        String[] audValueArray = accessTokenClaims.getStringArrayClaim("aud");

        Assert.assertTrue(ArrayUtils.contains(audValueArray, "zzz1.com"));
        Assert.assertTrue(ArrayUtils.contains(audValueArray, "zzz2.com"));
        Assert.assertTrue(ArrayUtils.contains(audValueArray, "zzz3.com"));
        Assert.assertTrue(ArrayUtils.contains(audValueArray, "zzzR.com"));
        Assert.assertFalse(ArrayUtils.contains(audValueArray, clientId));
    }

    @Test(groups = "wso2.is", description = "Verify the scopes updated by action in the access token ",
            dependsOnMethods = "testGetAccessTokenFromRefreshToken")
    public void testScopeUpdateOperationsFromPreIssueAccessTokenActionForRefreshTokenGrant() throws Exception {

        String[] scopes = accessTokenClaims.getStringClaim("scope").split("\\s+");

        Assert.assertTrue(ArrayUtils.contains(scopes, "new_test_custom_scope_1"));
        Assert.assertTrue(ArrayUtils.contains(scopes, "new_test_custom_scope_2"));
        Assert.assertTrue(ArrayUtils.contains(scopes, "new_test_custom_scope_3"));
        Assert.assertTrue(ArrayUtils.contains(scopes, "replaced_scope"));
    }

    @Test(groups = "wso2.is", description = "Verify the 'expires_in' claim updated by action in the access token",
            dependsOnMethods = "testGetAccessTokenFromRefreshToken")
    public void testExpiresInClaimReplaceOperationFromPreIssueAccessTokenActionForRefreshTokenGrant() throws Exception {

        Date exp = accessTokenClaims.getDateClaim("exp");
        Date iat = accessTokenClaims.getDateClaim("iat");
        long expiresIn = (exp.getTime() - iat.getTime()) / 1000;

        Assert.assertEquals(expiresIn, UPDATED_EXPIRY_TIME_BY_ACTION);
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

    public void performUserLogin() throws Exception {

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
                .expiryTime(APP_CONFIGURED_EXPIRY_TIME)
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

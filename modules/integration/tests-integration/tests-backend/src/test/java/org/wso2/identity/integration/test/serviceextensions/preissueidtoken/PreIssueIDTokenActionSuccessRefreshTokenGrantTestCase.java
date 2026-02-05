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

package org.wso2.identity.integration.test.serviceextensions.preissueidtoken;

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
import org.wso2.identity.integration.test.serviceextensions.common.ActionsBaseTestCase;
import org.wso2.identity.integration.test.serviceextensions.mockservices.ServiceExtensionMockServer;
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
 * Tests the pre-issue ID token action success scenarios with refresh token grant type.
 */
public class PreIssueIDTokenActionSuccessRefreshTokenGrantTestCase extends ActionsBaseTestCase {

    private static final String TEST_USER = "test_user";
    private static final String TEST_WSO2 = "Test@wso2";
    private static final String PRE_ISSUE_ID_TOKEN_API_PATH = "preIssueIdToken";
    private static final String MOCK_SERVER_ENDPOINT_RESOURCE_PATH = "/test/action";
    private static final String FIRST_NAME_CLAIM = "given_name";
    private static final String REPLACED_FIRST_NAME = "replaced_given_name";

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
    private String idToken;
    private String refreshToken;
    private JWTClaimsSet idTokenClaims;
    private final TestUserMode userMode;
    private ServiceExtensionMockServer serviceExtensionMockServer;

    @Factory(dataProvider = "testExecutionContextProvider")
    public PreIssueIDTokenActionSuccessRefreshTokenGrantTestCase(TestUserMode testUserMode) {

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
        actionId = createPreIssueIDTokenAction();
        addUser();

        requestedScopes = new ArrayList<>(Arrays.asList("openid", "profile"));

        serviceExtensionMockServer = new ServiceExtensionMockServer();
        serviceExtensionMockServer.startServer();
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {

        serviceExtensionMockServer.stopServer();

        deleteAction(PRE_ISSUE_ID_TOKEN_API_PATH, actionId);
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

        if (method.getName().equals("testGetIDTokenWithCodeGrant")) {
            serviceExtensionMockServer.setupStub(MOCK_SERVER_ENDPOINT_RESOURCE_PATH,
                    "Basic " + getBase64EncodedString(MOCK_SERVER_AUTH_BASIC_USERNAME, MOCK_SERVER_AUTH_BASIC_PASSWORD),
                    FileUtils.readFileInClassPathAsString(
                            "actions/response/pre-issue-id-token-response.json"), 200);
        } else if (method.getName().equals("testGetIDTokenFromRefreshToken")) {
            serviceExtensionMockServer.setupStub(MOCK_SERVER_ENDPOINT_RESOURCE_PATH,
                    "Basic " + getBase64EncodedString(MOCK_SERVER_AUTH_BASIC_USERNAME, MOCK_SERVER_AUTH_BASIC_PASSWORD),
                    FileUtils.readFileInClassPathAsString("actions/response/pre-issue-id-token-response.json"),
                    200);
        }
    }

    @Test(groups = "wso2.is", description =
            "Get ID token with authorization code grant when pre-issue ID token action is successful")
    public void testGetIDTokenWithCodeGrant() throws Exception {

        sendAuthorizeRequest();
        performUserLogin();
        HttpResponse response = sendTokenRequestForCodeGrant();

        String responseString = EntityUtils.toString(response.getEntity(), "UTF-8");
        JSONObject jsonResponse = new JSONObject(responseString);

        assertTrue(jsonResponse.has("access_token"), "Access token not found in the token response.");
        assertTrue(jsonResponse.has("id_token"), "ID token not found in the token response.");
        assertTrue(jsonResponse.has("refresh_token"), "Refresh token not found in the token response.");
        assertTrue(jsonResponse.has("token_type"), "Token type not found in the token response.");

        idToken = jsonResponse.getString("id_token");
        assertNotNull(idToken, "ID token is null.");

        refreshToken = jsonResponse.getString("refresh_token");
        assertNotNull(refreshToken, "Refresh token is null.");

        String tokenType = jsonResponse.getString("token_type");
        assertEquals(tokenType, "Bearer", "Invalid token type.");

        idTokenClaims = getJWTClaimSetFromToken(idToken);
        assertNotNull(idTokenClaims);
    }

    @Test(groups = "wso2.is", description = "Verify the custom string claim in the ID token added by action",
            dependsOnMethods = "testGetIDTokenWithCodeGrant")
    public void testClaimAddOperationFromPreIssueIDTokenActionForCodeGrant() throws Exception {

        String claimValue = idTokenClaims.getStringClaim("custom_claim_string_1");
        Assert.assertEquals(claimValue, "testCustomClaim1");
    }

    @Test(groups = "wso2.is", description =
            "Get ID token from refresh token when pre-issue ID token action is successful",
            dependsOnMethods = "testGetIDTokenWithCodeGrant")
    public void testGetIDTokenFromRefreshToken() throws Exception {

        HttpResponse response = sendTokenRequestForRefreshGrant();

        String responseString = EntityUtils.toString(response.getEntity(), "UTF-8");
        JSONObject jsonResponse = new JSONObject(responseString);

        assertTrue(jsonResponse.has("access_token"), "Access token not found in the token response.");
        assertTrue(jsonResponse.has("id_token"), "ID token not found in the token response.");
        assertTrue(jsonResponse.has("refresh_token"), "Refresh token not found in the token response.");
        assertTrue(jsonResponse.has("token_type"), "Token type not found in the token response.");

        idToken = jsonResponse.getString("id_token");
        assertNotNull(idToken, "ID token is null.");

        refreshToken = jsonResponse.getString("refresh_token");
        assertNotNull(refreshToken, "Refresh token is null.");

        String tokenType = jsonResponse.getString("token_type");
        assertEquals(tokenType, "Bearer", "Invalid token type.");

        idTokenClaims = getJWTClaimSetFromToken(idToken);
        assertNotNull(idTokenClaims);
    }

    @Test(groups = "wso2.is", description = "Verify the custom string claim added by action in " +
            "code grant is available in the ID token", dependsOnMethods = "testGetIDTokenFromRefreshToken")
    public void testClaimAddForIDTokenFromPreIssueIDTokenActionForRefreshTokenGrant()
            throws Exception {

        testClaimAddOperationFromPreIssueIDTokenActionForCodeGrant();
    }

    @Test(groups = "wso2.is", description = "Verify the custom boolean claim added by action in the ID token",
            dependsOnMethods = "testGetIDTokenFromRefreshToken")
    public void testBooleanClaimAddOperationFromPreIssueIDTokenActionForRefreshTokenGrant() throws Exception {

        boolean claimValue = idTokenClaims.getBooleanClaim("custom_claim_boolean_1");
        Assert.assertTrue(claimValue);
    }

    @Test(groups = "wso2.is", description = "Verify the custom number claim added by action in the ID token",
            dependsOnMethods = "testGetIDTokenFromRefreshToken")
    public void testNumberClaimAddOperationFromPreIssueIDTokenActionForRefreshTokenGrant() throws Exception {

        int claimValue = idTokenClaims.getIntegerClaim("custom_claim_number_1");
        Assert.assertEquals(claimValue, 78);
    }

    @Test(groups = "wso2.is", description = "Verify the custom string array claim added by action in the " +
            "ID token", dependsOnMethods = "testGetIDTokenFromRefreshToken")
    public void testClaimArrayAddOperationFromPreIssueIDTokenActionForRefreshTokenGrant()
            throws Exception {

        String[] expectedClaimArrayInToken = {"TestCustomClaim1", "TestCustomClaim2", "TestCustomClaim3"};

        String[] addedClaimArrayToToken = idTokenClaims.getStringArrayClaim("custom_claim_string_array_1");
        Assert.assertEquals(addedClaimArrayToToken, expectedClaimArrayInToken);
    }

    @Test(groups = "wso2.is", description = "Verify the given_name claim replaced by the action in " +
            "ID token", dependsOnMethods = "testGetIDTokenFromRefreshToken")
    public void testGivenNameReplaceOperationFromPreIssueIDTokenActionForRefreshTokenGrant()
            throws Exception {

        String givenNameClaim = idTokenClaims.getStringClaim(FIRST_NAME_CLAIM);
        Assert.assertEquals(givenNameClaim, REPLACED_FIRST_NAME);
    }

    @Test(groups = "wso2.is", description = "Verify the aud claim operations in the ID token",
            dependsOnMethods = "testGetIDTokenFromRefreshToken")
    public void testAudClaimOperationsFromPreIssueIDTokenActionForRefreshTokenGrant() throws Exception {

        String[] audValueArray = idTokenClaims.getStringArrayClaim("aud");

        Assert.assertTrue(ArrayUtils.contains(audValueArray, "zzz2.com"));
        Assert.assertTrue(ArrayUtils.contains(audValueArray, "zzzR.com"));
        Assert.assertTrue(ArrayUtils.contains(audValueArray, "zzz3.com"));
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
        assertNotNull(locationHeader);
        EntityUtils.consume(response.getEntity());

        response = sendGetRequest(client, locationHeader.getValue());

        Map<String, Integer> keyPositionMap = new HashMap<>(1);
        keyPositionMap.put("name=\"sessionDataKey\"", 1);
        List<DataExtractUtil.KeyValue> keyValues = DataExtractUtil.extractDataFromResponse(response, keyPositionMap);
        assertNotNull(keyValues);

        sessionDataKey = keyValues.get(0).getValue();
        assertNotNull(sessionDataKey);
        EntityUtils.consume(response.getEntity());
    }

    private void performUserLogin() throws Exception {

        HttpResponse response = sendLoginPostForCustomUsers(client, sessionDataKey, TEST_USER, TEST_WSO2);

        Header locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        assertNotNull(locationHeader);
        EntityUtils.consume(response.getEntity());

        response = sendGetRequest(client, locationHeader.getValue());
        locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        assertNotNull(locationHeader);
        EntityUtils.consume(response.getEntity());

        authorizationCode = getAuthorizationCodeFromURL(locationHeader.getValue());
        assertNotNull(authorizationCode);
    }

    private HttpResponse sendTokenRequestForCodeGrant() throws IOException {

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("code", authorizationCode));
        urlParameters.add(new BasicNameValuePair("grant_type", OAUTH2_GRANT_TYPE_AUTHORIZATION_CODE));
        urlParameters.add(new BasicNameValuePair("redirect_uri", OAuth2Constant.CALLBACK_URL));
        urlParameters.add(new BasicNameValuePair("client_id", clientId));

        List<Header> headers = new ArrayList<>();
        headers.add(new BasicHeader(AUTHORIZATION_HEADER,
                OAuth2Constant.BASIC_HEADER + " " + getBase64EncodedString(clientId, clientSecret)));
        headers.add(new BasicHeader("Content-Type", "application/x-www-form-urlencoded"));
        headers.add(new BasicHeader("User-Agent", OAuth2Constant.USER_AGENT));

        return sendPostRequest(client, headers, urlParameters,
                getTenantQualifiedURL(ACCESS_TOKEN_ENDPOINT, tenantInfo.getDomain()));
    }

    private HttpResponse sendTokenRequestForRefreshGrant() throws IOException {

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("grant_type", "refresh_token"));
        urlParameters.add(new BasicNameValuePair("refresh_token", refreshToken));

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

    private JWTClaimsSet getJWTClaimSetFromToken(String jwtToken) throws ParseException {

        SignedJWT signedJWT = SignedJWT.parse(jwtToken);
        return signedJWT.getJWTClaimsSet();
    }

    private String createPreIssueIDTokenAction() throws IOException {

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
        actionModel.setName("ID Token Pre Issue");
        actionModel.setDescription("This is a test pre issue ID token type");
        actionModel.setEndpoint(endpoint);

        return createAction(PRE_ISSUE_ID_TOKEN_API_PATH, actionModel);
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
                .grantTypes(new ArrayList<>(Arrays.asList(OAUTH2_GRANT_TYPE_AUTHORIZATION_CODE, "refresh_token")))
                .tokenType(ApplicationConfig.TokenType.JWT)
                .expiryTime(3600)
                .skipConsent(true)
                .build();

        ApplicationResponseModel application = addApplication(applicationConfig);
        String applicationId = application.getId();

        OpenIDConnectConfiguration oidcConfig = getOIDCInboundDetailsOfApplication(applicationId);
        clientId = oidcConfig.getClientId();
        clientSecret = oidcConfig.getClientSecret();

        return applicationId;
    }

    private void addUser() throws Exception {

        UserObject userInfo = new UserObject();
        userInfo.setUserName(TEST_USER);
        userInfo.setPassword(TEST_WSO2);
        userInfo.setName(new Name().givenName("Test"));
        userInfo.getName().setFamilyName("User");
        userInfo.addEmail(new Email().value("test.user@gmail.com"));
        userId = scim2RestClient.createUser(userInfo);
    }
}


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
package org.wso2.identity.integration.test.oauth2;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.config.Lookup;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.cookie.CookieSpecProvider;
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
import org.wso2.identity.integration.test.oauth2.dataprovider.model.ApplicationConfig;
import org.wso2.identity.integration.test.oauth2.dataprovider.model.AuthorizedAccessTokenContext;
import org.wso2.identity.integration.test.oauth2.dataprovider.model.AuthorizingUser;
import org.wso2.identity.integration.test.oauth2.dataprovider.model.TokenScopes;
import org.wso2.identity.integration.test.oauth2.dataprovider.model.UserClaimConfig;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationResponseModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.OpenIDConnectConfiguration;
import org.wso2.identity.integration.test.rest.api.user.common.model.Email;
import org.wso2.identity.integration.test.rest.api.user.common.model.Name;
import org.wso2.identity.integration.test.rest.api.user.common.model.UserObject;
import org.wso2.identity.integration.test.restclients.SCIM2RestClient;
import org.wso2.identity.integration.test.utils.DataExtractUtil;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

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
import static org.wso2.identity.integration.test.utils.DataExtractUtil.KeyValue;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.ACCESS_TOKEN_ENDPOINT;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.AUTHORIZATION_HEADER;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.AUTHORIZE_ENDPOINT_URL;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.INTRO_SPEC_ENDPOINT;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.OAUTH2_GRANT_TYPE_AUTHORIZATION_CODE;

public class OAuth2AuthorizationCodeGrantJWTTokenTestCase extends OAuth2ServiceAbstractIntegrationTest {

    private Lookup<CookieSpecProvider> cookieSpecRegistry;
    private RequestConfig requestConfig;
    private CloseableHttpClient client;
    private SCIM2RestClient scim2RestClient;

    private ApplicationConfig applicationConfig;
    private TokenScopes tokenScopes;
    private AuthorizingUser authorizingUser;

    private String applicationId;
    private String clientId;
    private String clientSecret;

    private String accessToken;
    private String refreshToken;
    private String sessionDataKey;
    private String authorizationCode;
    private JWTClaimsSet accessTokenClaims;

    @Factory(dataProvider = "testExecutionContextProvider")
    public OAuth2AuthorizationCodeGrantJWTTokenTestCase(ApplicationConfig applicationConfig, TokenScopes tokenScopes,
                                                        AuthorizingUser authorizingUser) {

        this.applicationConfig = applicationConfig;
        this.tokenScopes = tokenScopes;
        this.authorizingUser = authorizingUser;
    }

    @DataProvider(name = "testExecutionContextProvider")
    public static Object[][] getTestExecutionContext() {

        UserClaimConfig emailClaimConfig = new UserClaimConfig.Builder().localClaimUri(
                "http://wso2.org/claims/emailaddress").oidcClaimUri("email").build();
        UserClaimConfig givenNameClaimConfig = new UserClaimConfig.Builder().localClaimUri(
                "http://wso2.org/claims/givenname").oidcClaimUri("given_name").build();
        UserClaimConfig familyNameClaimConfig =
                new UserClaimConfig.Builder().localClaimUri("http://wso2.org/claims/lastname")
                        .oidcClaimUri("family_name").build();

        return new Object[][]{
                {new ApplicationConfig.Builder().tokenType(ApplicationConfig.TokenType.JWT)
                        .grantTypes(Arrays.asList("authorization_code", "refresh_token")).expiryTime(300)
                        .refreshTokenExpiryTime(86400)
                        .audienceList(Arrays.asList("audience1", "audience2", "audience3"))
                        .claimsList(Arrays.asList(emailClaimConfig, givenNameClaimConfig, familyNameClaimConfig))
                        .skipConsent(true).build(),
                        new TokenScopes.Builder().requestedScopes(Arrays.asList("openid", "email", "profile"))
                                .grantedScopes(Arrays.asList("openid", "email", "profile")).build(),
                        new AuthorizingUser.Builder().username("alice").password("Alice@123").userClaims(
                                new HashMap<UserClaimConfig, Object>() {{
                                    put(emailClaimConfig, "alice@aol.com");
                                }}).build()}
                //todo: add another iteration for authorized api scopes
        };
    }

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.init(TestUserMode.TENANT_ADMIN);

        cookieSpecRegistry = RegistryBuilder.<CookieSpecProvider>create()
                .register(CookieSpecs.DEFAULT, new RFC6265CookieSpecProvider())
                .build();
        requestConfig = RequestConfig.custom()
                .setCookieSpec(CookieSpecs.DEFAULT)
                .build();
        client = HttpClientBuilder.create()
                .setDefaultRequestConfig(requestConfig)
                .setDefaultCookieSpecRegistry(cookieSpecRegistry)
                .setRedirectStrategy(new DefaultRedirectStrategy() {
                    @Override
                    protected boolean isRedirectable(String method) {

                        return false;
                    }
                }).build();

        scim2RestClient = new SCIM2RestClient(serverURL, tenantInfo);

        String userId = addUser(authorizingUser);
        authorizingUser.setUserId(userId);

        addApp();
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {

        deleteApp(applicationId);
        deleteUser(authorizingUser.getUserId());
        restClient.closeHttpClient();
        client.close();
    }

    @Test(groups = "wso2.is", description = "Initiate authorize request.")
    public void testSendAuthorizeRequest() throws Exception {

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("response_type", OAuth2Constant.OAUTH2_GRANT_TYPE_CODE));
        urlParameters.add(new BasicNameValuePair("client_id", clientId));
        urlParameters.add(new BasicNameValuePair("redirect_uri", OAuth2Constant.CALLBACK_URL));
        urlParameters.add(new BasicNameValuePair("scope", String.join(" ", tokenScopes.getRequestedScopes())));

        HttpResponse response = sendPostRequestWithParameters(client, urlParameters,
                getTenantQualifiedURL(AUTHORIZE_ENDPOINT_URL, tenantInfo.getDomain()));

        Header locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        assertNotNull(locationHeader, "Location header expected for authorize request is not available.");
        EntityUtils.consume(response.getEntity());

        response = sendGetRequest(client, locationHeader.getValue());

        Map<String, Integer> keyPositionMap = new HashMap<>(1);
        keyPositionMap.put("name=\"sessionDataKey\"", 1);
        List<KeyValue> keyValues = DataExtractUtil.extractDataFromResponse(response, keyPositionMap);
        assertNotNull(keyValues, "SessionDataKey key value is null");

        sessionDataKey = keyValues.get(0).getValue();
        assertNotNull(sessionDataKey, "Session data key is null.");
        EntityUtils.consume(response.getEntity());
    }

    @Test(groups = "wso2.is", description = "Perform login", dependsOnMethods = "testSendAuthorizeRequest")
    public void testSendLoginPost() throws Exception {

        HttpResponse response = sendLoginPostForCustomUsers(client, sessionDataKey, authorizingUser.getUsername(),
                authorizingUser.getPassword());

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

    @Test(groups = "wso2.is", description = "Get access token", dependsOnMethods = "testSendLoginPost")
    public void testGetAccessToken() throws Exception {

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

        HttpResponse response = sendPostRequest(client, headers, urlParameters,
                getTenantQualifiedURL(ACCESS_TOKEN_ENDPOINT, tenantInfo.getDomain()));
        assertNotNull(response, "Failed to receive a response for access token request.");

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
        assertEquals(expiresIn, applicationConfig.getExpiryTime(), "Invalid expiry time for the access token.");

        String tokenType = jsonResponse.getString("token_type");
        assertEquals(tokenType, "Bearer", "Invalid token type for the access token.");
    }

    @Test(groups = "wso2.is", description = "Extract access token claims", dependsOnMethods = "testGetAccessToken")
    public void testExtractJWTAccessTokenClaims() throws Exception {

        accessTokenClaims = getJWTClaimSetFromToken(accessToken);
        assertNotNull(accessTokenClaims);
    }

    @Test(groups = "wso2.is", description = "Validate JWT token identifier", dependsOnMethods = "testExtractJWTAccessTokenClaims")
    public void testValidateJWTID() {

        assertNotNull(accessTokenClaims.getJWTID());
    }

    @Test(groups = "wso2.is", description = "Validate issuer", dependsOnMethods = "testExtractJWTAccessTokenClaims")
    public void testValidateIssuer() {

        assertEquals(accessTokenClaims.getIssuer(),
                getTenantQualifiedURL(ACCESS_TOKEN_ENDPOINT, tenantInfo.getDomain()));
    }

    @Test(groups = "wso2.is", description = "Validate client id", dependsOnMethods = "testExtractJWTAccessTokenClaims")
    public void testValidateClientId() {

        assertEquals(accessTokenClaims.getClaim("client_id"), clientId);
    }

    @Test(groups = "wso2.is", description = "Validate audiences", dependsOnMethods = "testExtractJWTAccessTokenClaims")
    public void testValidateAudiences() {

        List<String> audienceList = accessTokenClaims.getAudience();
        assertEquals(audienceList.get(0), clientId, "Audience value does not include the client id.");

        List<String> expectedAudiences = applicationConfig.getAudienceList();
        for (String expectedAudience : expectedAudiences) {
            assertTrue(audienceList.contains(expectedAudience),
                    "Audience " + expectedAudience + " not found in the access token.");
        }
    }

    @Test(groups = "wso2.is", description = "Validate expiry time", dependsOnMethods = "testExtractJWTAccessTokenClaims")
    public void testValidateExpiryTime() {

        // Convert expiry time to seconds as that is how expiry is incorporated in the JWT token claims.
        assertEquals(accessTokenClaims.getExpirationTime().getTime() / 1000,
                calculateExpiryTime(accessTokenClaims.getIssueTime().getTime() / 1000,
                        applicationConfig.getExpiryTime()),
                "Invalid expiry time for the access token.");
    }

    @Test(groups = "wso2.is", description = "Validate scopes", dependsOnMethods = "testExtractJWTAccessTokenClaims")
    public void testValidateScopes() throws Exception {

        assertNotNull(accessTokenClaims.getStringClaim("scope"));
        List<String> authorizedScopes = Arrays.asList(accessTokenClaims.getStringClaim("scope").split(" "));
        List<String> expectedScopes = tokenScopes.getGrantedScopes();
        for (String expectedScope : expectedScopes) {
            assertTrue(authorizedScopes.contains(expectedScope),
                    "Scope " + expectedScope + " not found in the access token.");
        }
    }

    @Test(groups = "wso2.is", description = "Validate additional user claims", dependsOnMethods = "testExtractJWTAccessTokenClaims")
    public void testValidateAdditionalUserClaims() {

        applicationConfig.getRequestedClaimList().forEach(claim -> {
            if (authorizingUser.getUserClaims().get(claim) != null) {
                assertNotNull(accessTokenClaims.getClaim(claim.getOidcClaimUri()),
                        "Claim " + claim.getOidcClaimUri() + " not found in the access token.");
                assertEquals(accessTokenClaims.getClaim(claim.getOidcClaimUri()),
                        authorizingUser.getUserClaims().get(claim),
                        "Value for claim " + claim.getOidcClaimUri() + " is incorrect in the access token.");
            }
        });
    }

    @Test(groups = "wso2.is", description = "Call introspect endpoint", dependsOnMethods = "testExtractJWTAccessTokenClaims")
    public void testIntrospectRefreshToken() throws Exception {

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
        JSONObject jsonResponse = new JSONObject(responseString);

        assertTrue(jsonResponse.has("nbf"), "Not Before value not found in the refresh token introspection response");
        assertTrue(jsonResponse.has("exp"), "Expiry timestamp not found in the refresh token introspection response");
        long exp = jsonResponse.getLong("exp");
        assertTrue(jsonResponse.has("iat"),
                "Issued at timestamp not found in the refresh token introspection response");
        long iat = jsonResponse.getLong("iat");

        assertEquals((exp - iat), applicationConfig.getRefreshTokenExpiryTime(),
                "Invalid expiry time for the refresh token.");

        assertTrue(jsonResponse.has("scope"), "Scopes not found in the refresh token introspection response");
        List<String> authorizedScopes = Arrays.asList(jsonResponse.getString("scope").split(" "));
        List<String> expectedScopes = tokenScopes.getGrantedScopes();
        for (String expectedScope : expectedScopes) {
            assertTrue(authorizedScopes.contains(expectedScope),
                    "Scope " + expectedScope + " not found in the refresh token introspection.");
        }

        assertTrue((Boolean) jsonResponse.get("active"), "Refresh token is inactive");
        assertEquals(jsonResponse.get("token_type"), "Refresh", "Invalid token type");
        assertEquals(jsonResponse.get("client_id"), clientId,
                "Invalid client id in the refresh token introspection response");
    }

    @Test(groups = "wso2.is", description = "Validate additional user claims", dependsOnMethods = "testExtractJWTAccessTokenClaims")
    public void testRefreshTokenGrant() throws Exception {

        AuthorizedAccessTokenContext tokenContext =
                new AuthorizedAccessTokenContext.Builder().accessToken(accessToken).refreshToken(refreshToken)
                        .grantType(OAUTH2_GRANT_TYPE_AUTHORIZATION_CODE).accessTokenClaims(accessTokenClaims)
                        .clientId(clientId).clientSecret(clientSecret).build();

        OAuth2RefreshGrantJWTTokenTestCase refreshGrantJWTTokenTestCase =
                new OAuth2RefreshGrantJWTTokenTestCase(applicationConfig, tokenScopes, authorizingUser, tokenContext);

        refreshGrantJWTTokenTestCase.testGetAccessTokenFromRefreshToken();
        refreshGrantJWTTokenTestCase.testValidateJWTID();
        refreshGrantJWTTokenTestCase.testValidateIssuer();
        refreshGrantJWTTokenTestCase.testValidateClientId();
        refreshGrantJWTTokenTestCase.testValidateAudiences();
        refreshGrantJWTTokenTestCase.testValidateExpiryTime();
        refreshGrantJWTTokenTestCase.testValidateScopes();
        refreshGrantJWTTokenTestCase.testValidateAdditionalUserClaims();
        refreshGrantJWTTokenTestCase.testIntrospectRefreshToken();
    }

    private String addUser(AuthorizingUser user) throws Exception {

        UserObject userInfo = new UserObject();
        userInfo.setUserName(user.getUsername());
        userInfo.setPassword(user.getPassword());

        for (Map.Entry<UserClaimConfig, Object> entry : user.getUserClaims().entrySet()) {
            if (entry.getKey().getOidcClaimUri().equals("email")) {
                userInfo.addEmail(new Email().value((String) entry.getValue()));
            } else if (entry.getKey().getOidcClaimUri().equals("given_name")) {
                userInfo.setName(new Name().givenName((String) entry.getValue()));
            } else if (entry.getKey().getOidcClaimUri().equals("family_name")) {
                userInfo.getName().setFamilyName((String) entry.getValue());
            }
        }

        return scim2RestClient.createUser(userInfo);
    }

    private void deleteUser(String userId) throws Exception {

        scim2RestClient.deleteUser(userId);
    }

    private void addApp() throws Exception {

        ApplicationResponseModel application = addApplication(applicationConfig);
        applicationId = application.getId();

        OpenIDConnectConfiguration oidcConfig = getOIDCInboundDetailsOfApplication(applicationId);
        clientId = oidcConfig.getClientId();
        clientSecret = oidcConfig.getClientSecret();
    }

    private String getAuthorizationCodeFromURL(String location) {

        URI uri = URI.create(location);
        return URLEncodedUtils.parse(uri, StandardCharsets.UTF_8).stream()
                .filter(param -> "code".equals(param.getName()))
                .map(NameValuePair::getValue)
                .findFirst()
                .orElse(null);
    }

    private long calculateExpiryTime(long issuedTime, long expiryPeriodInSeconds) {

        return issuedTime + expiryPeriodInSeconds;
    }

    private JWTClaimsSet getJWTClaimSetFromToken(String jwtToken) throws ParseException {

        SignedJWT signedJWT = SignedJWT.parse(jwtToken);
        return signedJWT.getJWTClaimsSet();
    }
}

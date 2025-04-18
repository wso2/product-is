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
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.identity.integration.test.oauth2.dataprovider.model.ApplicationConfig;
import org.wso2.identity.integration.test.oauth2.dataprovider.model.AuthorizedAccessTokenContext;
import org.wso2.identity.integration.test.oauth2.dataprovider.model.AuthorizingUser;
import org.wso2.identity.integration.test.oauth2.dataprovider.model.TokenScopes;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.ACCESS_TOKEN_ENDPOINT;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.AUTHORIZATION_HEADER;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.INTRO_SPEC_ENDPOINT;

public class OAuth2RefreshGrantJWTTokenTestCase extends OAuth2ServiceAbstractIntegrationTest {

    private Lookup<CookieSpecProvider> cookieSpecRegistry;
    private RequestConfig requestConfig;
    private CloseableHttpClient client;

    private ApplicationConfig applicationConfig;
    private TokenScopes tokenScopes;
    private AuthorizingUser authorizingUser;
    private AuthorizedAccessTokenContext authorizedAccessTokenContext;

    private String accessToken;
    private JWTClaimsSet accessTokenClaims;

    public OAuth2RefreshGrantJWTTokenTestCase(ApplicationConfig applicationConfig, TokenScopes tokenScopes,
                                              AuthorizingUser authorizingUser,
                                              AuthorizedAccessTokenContext authorizedAccessTokenContext)
            throws Exception {

        this.applicationConfig = applicationConfig;
        this.tokenScopes = tokenScopes;
        this.authorizingUser = authorizingUser;
        this.authorizedAccessTokenContext = authorizedAccessTokenContext;

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
    }

    @Test(groups = "wso2.is", description = "Get access token from refresh token")
    public void testGetAccessTokenFromRefreshToken() throws Exception {

        List<NameValuePair> parameters = new ArrayList<>();
        parameters.add(new BasicNameValuePair("grant_type", OAuth2Constant.OAUTH2_GRANT_TYPE_REFRESH_TOKEN));
        parameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_GRANT_TYPE_REFRESH_TOKEN,
                authorizedAccessTokenContext.getRefreshToken()));

        List<Header> headers = new ArrayList<>();
        headers.add(new BasicHeader(AUTHORIZATION_HEADER,
                OAuth2Constant.BASIC_HEADER + " " + getBase64EncodedString(authorizedAccessTokenContext.getClientId(),
                        authorizedAccessTokenContext.getClientSecret())));
        headers.add(new BasicHeader("Content-Type", "application/x-www-form-urlencoded"));
        headers.add(new BasicHeader("User-Agent", OAuth2Constant.USER_AGENT));

        HttpResponse response = sendPostRequest(client, headers, parameters,
                getTenantQualifiedURL(ACCESS_TOKEN_ENDPOINT, tenantInfo.getDomain()));

        String responseString = EntityUtils.toString(response.getEntity(), "UTF-8");
        JSONObject jsonResponse = new JSONObject(responseString);

        assertTrue(jsonResponse.has("access_token"), "Access token not found in the token response.");
        assertTrue(jsonResponse.has("refresh_token"), "Refresh token not found in the token response.");
        assertTrue(jsonResponse.has("expires_in"), "Expiry time not found in the token response.");
        assertTrue(jsonResponse.has("token_type"), "Token type not found in the token response.");

        accessToken = jsonResponse.getString("access_token");
        assertNotNull(accessToken, "Access token is null.");

        String refreshToken = jsonResponse.getString("refresh_token");
        assertNotNull(refreshToken, "Refresh token is null.");

        int expiresIn = jsonResponse.getInt("expires_in");
        assertEquals(expiresIn, applicationConfig.getExpiryTime(), "Invalid expiry time for the access token.");

        String tokenType = jsonResponse.getString("token_type");
        assertEquals(tokenType, "Bearer", "Invalid token type for the access token.");

        accessTokenClaims = getJWTClaimSetFromToken(accessToken);
        assertNotNull(accessTokenClaims);
    }

    @Test(groups = "wso2.is", description = "Validate JWT token identifier", dependsOnMethods = "testGetAccessTokenFromRefreshToken")
    public void testValidateJWTID() {

        assertNotNull(accessTokenClaims.getJWTID());
    }

    @Test(groups = "wso2.is", description = "Validate issuer", dependsOnMethods = "testGetAccessTokenFromRefreshToken")
    public void testValidateIssuer() {

        assertEquals(accessTokenClaims.getIssuer(),
                getTenantQualifiedURL(ACCESS_TOKEN_ENDPOINT, tenantInfo.getDomain()));
    }

    @Test(groups = "wso2.is", description = "Validate client id", dependsOnMethods = "testGetAccessTokenFromRefreshToken")
    public void testValidateClientId() {

        assertEquals(accessTokenClaims.getClaim("client_id"), authorizedAccessTokenContext.getClientId());
    }

    @Test(groups = "wso2.is", description = "Validate audiences", dependsOnMethods = "testGetAccessTokenFromRefreshToken")
    public void testValidateAudiences() {

        List<String> audienceList = accessTokenClaims.getAudience();
        assertEquals(audienceList.get(0), authorizedAccessTokenContext.getClientId(),
                "Audience value does not include the client id.");

        List<String> expectedAudiences = applicationConfig.getAudienceList();
        for (String expectedAudience : expectedAudiences) {
            assertTrue(audienceList.contains(expectedAudience),
                    "Audience " + expectedAudience + " not found in the access token.");
        }
    }

    @Test(groups = "wso2.is", description = "Validate expiry time", dependsOnMethods = "testGetAccessTokenFromRefreshToken")
    public void testValidateExpiryTime() {

        // Convert expiry time to seconds as that is how expiry is incorporated in the JWT token claims.
        assertEquals(accessTokenClaims.getExpirationTime().getTime() / 1000,
                calculateExpiryTime(accessTokenClaims.getIssueTime().getTime() / 1000,
                        applicationConfig.getExpiryTime()),
                "Invalid expiry time for the access token.");
    }

    @Test(groups = "wso2.is", description = "Validate scopes", dependsOnMethods = "testGetAccessTokenFromRefreshToken")
    public void testValidateScopes() throws Exception {

        assertNotNull(accessTokenClaims.getStringClaim("scope"));
        List<String> authorizedScopes = Arrays.asList(accessTokenClaims.getStringClaim("scope").split(" "));
        List<String> expectedScopes = tokenScopes.getGrantedScopes();
        for (String expectedScope : expectedScopes) {
            assertTrue(authorizedScopes.contains(expectedScope),
                    "Scope " + expectedScope + " not found in the access token.");
        }
    }

    @Test(groups = "wso2.is", description = "Validate additional user claims", dependsOnMethods = "testGetAccessTokenFromRefreshToken")
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

    @Test(groups = "wso2.is", description = "Call introspect endpoint for the refresh token", dependsOnMethods = "testGetAccessTokenFromRefreshToken")
    public void testIntrospectRefreshToken() throws Exception {

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("token", authorizedAccessTokenContext.getRefreshToken()));

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
        assertEquals(jsonResponse.get("client_id"), authorizedAccessTokenContext.getClientId(),
                "Invalid client id in the refresh token introspection response");
    }

    private JWTClaimsSet getJWTClaimSetFromToken(String jwtToken) throws ParseException {

        SignedJWT signedJWT = SignedJWT.parse(jwtToken);
        return signedJWT.getJWTClaimsSet();
    }

    private long calculateExpiryTime(long issuedTime, long expiryPeriodInSeconds) {

        return issuedTime + expiryPeriodInSeconds;
    }
}

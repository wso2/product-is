/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.identity.integration.test.identityServlet;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.oauth2.sdk.AuthorizationCode;
import com.nimbusds.oauth2.sdk.AuthorizationCodeGrant;
import com.nimbusds.oauth2.sdk.AuthorizationGrant;
import com.nimbusds.oauth2.sdk.RefreshTokenGrant;
import com.nimbusds.oauth2.sdk.Scope;
import com.nimbusds.oauth2.sdk.TokenErrorResponse;
import com.nimbusds.oauth2.sdk.TokenRequest;
import com.nimbusds.oauth2.sdk.TokenResponse;
import com.nimbusds.oauth2.sdk.auth.ClientAuthentication;
import com.nimbusds.oauth2.sdk.auth.ClientSecretBasic;
import com.nimbusds.oauth2.sdk.auth.Secret;
import com.nimbusds.oauth2.sdk.http.HTTPResponse;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.token.RefreshToken;
import com.nimbusds.openid.connect.sdk.OIDCTokenResponse;
import com.nimbusds.openid.connect.sdk.OIDCTokenResponseParser;
import com.nimbusds.openid.connect.sdk.token.OIDCTokens;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.config.Lookup;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.cookie.CookieSpecProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.RFC6265CookieSpecProvider;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.identity.application.common.model.xsd.ServiceProvider;
import org.wso2.carbon.identity.oauth.stub.dto.OAuthConsumerAppDTO;
import org.wso2.identity.integration.test.oauth2.OAuth2ServiceAbstractIntegrationTest;
import org.wso2.identity.integration.test.utils.DataExtractUtil;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.servlet.http.HttpServletResponse;

import static org.wso2.identity.integration.test.utils.OAuth2Constant.ACCESS_TOKEN_ENDPOINT;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.AUTHORIZE_ENDPOINT_URL;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.OAUTH2_CLIENT_ID;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.OAUTH2_GRANT_TYPE_CODE;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.OAUTH2_REDIRECT_URI;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.OAUTH2_RESPONSE_TYPE;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.OAUTH2_SCOPE;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.OAUTH2_SCOPE_OPENID;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.OAUTH2_SCOPE_OPENID_WITH_INTERNAL_LOGIN;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.SESSION_DATA_KEY;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.AUTHORIZATION_CODE_NAME;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.SESSION_DATA_KEY_CONSENT;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.USER_AGENT;

/**
 * Test for Session Extender endpoint functionality with claims obtained via an Authorization Code Grant and Refresh
 * Grant.
 */
public class ExtendSessionEndpointAuthCodeGrantTestCase extends OAuth2ServiceAbstractIntegrationTest {

    private static final String CALLBACK_URL = "https://localhost/callback";
    private static final String IDP_SESSION_KEY_CLAIM_NAME = "isk";
    private static final String SESSION_EXTENDER_ENDPOINT_URL = "https://localhost:9853/identity/extend-session";
    private static final String SESSION_EXTENDER_ENDPOINT_GET_URL = SESSION_EXTENDER_ENDPOINT_URL + "?%s=%s";
    private static final String SESSIONS_ENDPOINT_URI = "https://localhost:9853/api/users/v1/me/sessions";

    private Lookup<CookieSpecProvider> cookieSpecRegistry;
    private RequestConfig requestConfig;
    private CloseableHttpClient firstPartyClient;
    private CloseableHttpClient thirdPartyClient;
    private String sessionDataKey;
    private String sessionDataKeyConsent;
    private AuthorizationCode authorizationCode;
    private String idToken;
    private RefreshToken refreshToken;
    private String idpSessionKey;
    private String authenticatingUserName;
    private String authenticatingCredential;
    private AutomationContext context;

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.init(TestUserMode.SUPER_TENANT_USER);
        context = isServer;
        this.authenticatingUserName = context.getContextTenant().getContextUser().getUserName();
        this.authenticatingCredential = context.getContextTenant().getContextUser().getPassword();

        cookieSpecRegistry = RegistryBuilder.<CookieSpecProvider>create()
                .register(CookieSpecs.DEFAULT, new RFC6265CookieSpecProvider())
                .build();
        requestConfig = RequestConfig.custom()
                .setCookieSpec(CookieSpecs.DEFAULT)
                .build();
        firstPartyClient = HttpClientBuilder.create()
                .disableRedirectHandling()
                .setDefaultRequestConfig(requestConfig)
                .setDefaultCookieSpecRegistry(cookieSpecRegistry)
                .build();
        thirdPartyClient = HttpClientBuilder.create()
                .disableRedirectHandling()
                .setDefaultRequestConfig(requestConfig)
                .setDefaultCookieSpecRegistry(cookieSpecRegistry)
                .build();
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {

        deleteApplication();
        removeOAuthApplicationData();

        consumerKey = null;
        consumerSecret = null;
        firstPartyClient.close();
        thirdPartyClient.close();
    }

    @Test(groups = "wso2.is", description = "Check Oauth2 application flow.")
    public void testRegisterApplication() throws Exception {

        OAuthConsumerAppDTO oAuthConsumerAppDTO = getBasicOAuthApp(CALLBACK_URL);
        ServiceProvider serviceProvider = registerServiceProviderWithOAuthInboundConfigs(oAuthConsumerAppDTO);
        Assert.assertNotNull(serviceProvider, "OAuth App creation failed.");
        Assert.assertNotNull(consumerKey, "Consumer Key is null.");
        Assert.assertNotNull(consumerSecret, "Consumer Secret is null.");
    }

    @Test(groups = "wso2.is", description = "Send authorize user request for authorization code grant type.",
            dependsOnMethods = "testRegisterApplication")
    public void testAuthCodeGrantSendAuthRequestPost() throws Exception {

        // Send a direct auth code request to IS instance.
        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair(OAUTH2_RESPONSE_TYPE, OAUTH2_GRANT_TYPE_CODE));
        urlParameters.add(new BasicNameValuePair(OAUTH2_CLIENT_ID, consumerKey));
        urlParameters.add(new BasicNameValuePair(OAUTH2_REDIRECT_URI, CALLBACK_URL));
        urlParameters.add(new BasicNameValuePair(OAUTH2_SCOPE, OAUTH2_SCOPE_OPENID_WITH_INTERNAL_LOGIN));

        HttpResponse response = sendPostRequestWithParameters(firstPartyClient, urlParameters, AUTHORIZE_ENDPOINT_URL);
        Assert.assertNotNull(response, "Authorization request failed. Authorized response is null");

        String locationValue = getLocationHeaderValue(response);
        Assert.assertTrue(locationValue.contains(SESSION_DATA_KEY),
                "sessionDataKey not found in response.");

        // Extract sessionDataKey from the location value.
        sessionDataKey = DataExtractUtil.getParamFromURIString(locationValue, SESSION_DATA_KEY);
        Assert.assertNotNull(sessionDataKey, "Session data key is null.");
        EntityUtils.consume(response.getEntity());
    }

    @Test(groups = "wso2.is", description = "Send login post request.",
            dependsOnMethods = "testAuthCodeGrantSendAuthRequestPost")
    public void testAuthCodeGrantSendLoginPost() throws Exception {

        sessionDataKeyConsent = getSessionDataKeyConsent(firstPartyClient, sessionDataKey);
        Assert.assertNotNull(sessionDataKeyConsent, "Invalid session key consent.");
    }

    @Test(groups = "wso2.is", description = "Send approval post request.",
            dependsOnMethods = "testAuthCodeGrantSendLoginPost")
    public void testAuthCodeGrantSendApprovalPost() throws Exception {

        HttpResponse response = sendApprovalPost(firstPartyClient, sessionDataKeyConsent);
        Assert.assertNotNull(response, "Approval request failed. response is invalid.");

        Header locationHeader =
                response.getFirstHeader(HTTP_RESPONSE_HEADER_LOCATION);
        Assert.assertNotNull(locationHeader, "Approval request failed. Location header is null.");

        String locationValue = getLocationHeaderValue(response);
        Assert.assertTrue(locationValue.contains(AUTHORIZATION_CODE_NAME),
                "Authorization code not found in the response.");

        // Extract authorization code from the location value.
        authorizationCode = new AuthorizationCode(DataExtractUtil.getParamFromURIString(locationValue,
                AUTHORIZATION_CODE_NAME));
        Assert.assertNotNull(authorizationCode, "Authorization code is null.");
        EntityUtils.consume(response.getEntity());
    }

    @Test(groups = "wso2.is", description = "Send get access token request.",
            dependsOnMethods = "testAuthCodeGrantSendApprovalPost")
    public void testAuthCodeGrantIdTokenClaimAvailability() throws Exception {

        URI callbackURI = new URI(CALLBACK_URL);
        AuthorizationCodeGrant authorizationCodeGrant = new AuthorizationCodeGrant(authorizationCode, callbackURI);
        OIDCTokens oidcTokens = makeTokenRequest(authorizationCodeGrant, ACCESS_TOKEN_ENDPOINT,null);

        idToken = oidcTokens.getIDTokenString();
        refreshToken = oidcTokens.getRefreshToken();
        Assert.assertNotNull(idToken, "ID token is null");
        Assert.assertNotNull(refreshToken, "Refresh token is null");

        testIdTokenClaimAvailability();
    }

    @Test(groups = "wso2.is", description = "Sends a request for session extension with a valid cookie.",
            dependsOnMethods = "testAuthCodeGrantIdTokenClaimAvailability")
    public void testSessionExtensionWithValidCookie() throws Exception {

        Long lastAccessedTimeBeforeExtension = getLastAccessedTimeOfSession();

        // A valid commonAuthIdCookie is already available in the HttpClient, which had been obtained during the
        // authorization code grant.
        HttpResponse response = sendGetRequest(firstPartyClient, SESSION_EXTENDER_ENDPOINT_URL);
        Assert.assertNotNull(response, "Session extension request failed. Response is invalid.");

        int statusCode = response.getStatusLine().getStatusCode();
        Assert.assertEquals(statusCode, HttpServletResponse.SC_OK, "Session extension failed for request with valid " +
                "cookie");
        EntityUtils.consume(response.getEntity());

        Long lastAccessedTimeAfterExtension = getLastAccessedTimeOfSession();
        Assert.assertTrue(lastAccessedTimeAfterExtension > lastAccessedTimeBeforeExtension,
                "Session has not been extended.");
    }

    @Test(groups = "wso2.is", description = "Sends a request for session extension with a valid cookie.",
            dependsOnMethods = "testSessionExtensionWithValidCookie")
    public void testSessionExtensionWithMismatchingCookieAndParameter() throws Exception {

        // A valid commonAuthIdCookie is already available in the HttpClient, which had been obtained during the
        // authorization code grant.
        String locationUrl = String.format(SESSION_EXTENDER_ENDPOINT_GET_URL, "idpSessionKey", "abcd1234");
        testSessionExtensionResponse(firstPartyClient, locationUrl, HttpServletResponse.SC_CONFLICT, "ISE-60005");
    }

    @Test(groups = "wso2.is", description = "Sends a valid request for session extension.",
            dependsOnMethods = "testSessionExtensionWithMismatchingCookieAndParameter")
    public void testSessionExtensionWithValidParameters() throws Exception {

        Long lastAccessedTimeBeforeExtension = getLastAccessedTimeOfSession();

        // Using the fresh third party client without any cookies as the firstPartyClient holds a valid cookie,
        String locationUrl = String.format(SESSION_EXTENDER_ENDPOINT_GET_URL, "idpSessionKey", idpSessionKey);
        HttpResponse response = sendGetRequest(thirdPartyClient, locationUrl);
        Assert.assertNotNull(response, "Session extension request failed. Response is invalid.");

        int statusCode = response.getStatusLine().getStatusCode();
        Assert.assertEquals(statusCode, HttpServletResponse.SC_OK, "Session extension failed for request with valid " +
                "session key parameter.");
        EntityUtils.consume(response.getEntity());

        Long lastAccessedTimeAfterExtension = getLastAccessedTimeOfSession();
        Assert.assertTrue(lastAccessedTimeAfterExtension > lastAccessedTimeBeforeExtension,
                "Session has not been extended.");
    }

    @Test(groups = "wso2.is", description = "Checks whether an access token can be obtained with internal scopes " +
            " via refresh token grant type", dependsOnMethods = "testSessionExtensionWithValidParameters")
    public void testRefreshTokenGrantWithInternalScopes() throws Exception {

        Set<String> expectedScopes = new HashSet<>();
        Collections.addAll(expectedScopes, OAUTH2_SCOPE_OPENID_WITH_INTERNAL_LOGIN.split(" "));

        RefreshTokenGrant refreshGrant = new RefreshTokenGrant(refreshToken);
        OIDCTokens oidcTokens = makeTokenRequest(refreshGrant, ACCESS_TOKEN_ENDPOINT,
                OAUTH2_SCOPE_OPENID_WITH_INTERNAL_LOGIN);
        refreshToken = oidcTokens.getRefreshToken();

        List<String> actualScopes = oidcTokens.getAccessToken().getScope().toStringList();
        actualScopes.forEach(scope -> Assert.assertTrue(expectedScopes.contains(scope), "Requested scopes does not " +
                "match the actual scopes"));

    }

    @Test(groups = "wso2.is", description = "Checks whether the IDP session key is available as a claim in the" +
            " ID token obtained from a Refresh Grant",
            dependsOnMethods = "testRefreshTokenGrantWithInternalScopes")
    public void testRefreshGrantIdTokenClaimAvailability() throws Exception {

        RefreshTokenGrant refreshGrant = new RefreshTokenGrant(refreshToken);
        OIDCTokens oidcTokens = makeTokenRequest(refreshGrant, ACCESS_TOKEN_ENDPOINT, OAUTH2_SCOPE_OPENID);

        idToken = oidcTokens.getIDTokenString();
        Assert.assertNotNull(idToken, "ID token is null");

        testIdTokenClaimAvailability();
    }

    @Test(groups = "wso2.is", description = "Sends a valid request for session extension.",
            dependsOnMethods = "testRefreshGrantIdTokenClaimAvailability")
    public void testSessionExtensionWithInValidParameters() throws Exception {

        // Using the fresh third party client without any cookies as the firstPartyClient holds a valid cookie,
        String invalidParamNameUrl = String.format(SESSION_EXTENDER_ENDPOINT_GET_URL, "idpKey", idpSessionKey);
        testSessionExtensionResponse(thirdPartyClient, invalidParamNameUrl, HttpServletResponse.SC_BAD_REQUEST,
                "ISE-60001");

        String invalidKeyValueUrl =  String.format(SESSION_EXTENDER_ENDPOINT_GET_URL, "idpSessionKey", "12345");
        testSessionExtensionResponse(thirdPartyClient, invalidKeyValueUrl, HttpServletResponse.SC_BAD_REQUEST,
                "ISE-60004");
    }

    /**
     * Checks whether the isk claim is available in the ID token.
     *
     * @throws Exception Exception.
     */
    private void testIdTokenClaimAvailability() throws Exception {

        JWTClaimsSet claims = SignedJWT.parse(idToken).getJWTClaimsSet();
        Assert.assertNotNull(claims, "ID token claim set is null");

        idpSessionKey = (String) claims.getClaim(IDP_SESSION_KEY_CLAIM_NAME);
        Assert.assertNotNull(idpSessionKey, "IDP session key not available in ID token.");
    }

    /**
     * To make a token request with specified grant.
     *
     * @param authorizationGrant    Relevant authorization grant.
     * @return                      OIDC tokens coming from request.
     * @throws Exception            Exception.
     */
    private OIDCTokens makeTokenRequest(AuthorizationGrant authorizationGrant, String uriString, String scopeString)
            throws Exception {

        ClientID clientID = new ClientID(consumerKey);
        Secret clientSecret = new Secret(consumerSecret);
        ClientAuthentication clientAuth = new ClientSecretBasic(clientID, clientSecret);
        URI uri = new URI(uriString);
        Scope scope = null;
        if (StringUtils.isNotBlank(scopeString)) {
            scope = new Scope(scopeString);
        }
        TokenRequest request = new TokenRequest(uri, clientAuth, authorizationGrant, scope);

        HTTPResponse tokenHTTPResp = request.toHTTPRequest().send();
        Assert.assertNotNull(tokenHTTPResp, "Token http response is null.");

        TokenResponse tokenResponse = OIDCTokenResponseParser.parse(tokenHTTPResp);
        Assert.assertNotNull(tokenResponse, "Token response of access token response is null.");

        Assert.assertFalse(tokenResponse instanceof TokenErrorResponse, "JWT access token response contains errors.");

        OIDCTokenResponse oidcTokenResponse = (OIDCTokenResponse) tokenResponse;
        OIDCTokens oidcTokens = oidcTokenResponse.getOIDCTokens();
        Assert.assertNotNull(oidcTokens, "OIDC Tokens object is null.");
        return oidcTokens;
    }

    /**
     * Method for testing the error responses from the API.
     *
     * @param url                   URL of the request.
     * @param expectedStatusCode    Expected status code of the response.
     * @param expectedErrorCode     Expected error code of the API response JSON.
     * @throws Exception            Error if executing the request fails.
     */
    private void testSessionExtensionResponse(CloseableHttpClient client, String url, int expectedStatusCode,
                                              String expectedErrorCode)
            throws Exception {

        HttpResponse response = sendGetRequest(client, url);
        Assert.assertNotNull(response, "Session extension request failed. Response is invalid.");

        int statusCode = response.getStatusLine().getStatusCode();
        Assert.assertEquals(statusCode, expectedStatusCode);

        HttpEntity entity = response.getEntity();
        String responseString = EntityUtils.toString(entity, "UTF-8");
        JSONObject responseJson = new JSONObject(responseString);
        Assert.assertEquals(responseJson.get("code"), expectedErrorCode);
        EntityUtils.consume(entity);
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
    private String getSessionDataKeyConsent(CloseableHttpClient client, String sessionDataKey)
            throws IOException, URISyntaxException {

        HttpResponse response = sendLoginPost(client, sessionDataKey);
        Assert.assertNotNull(response, "Login request failed. response is null.");

        Header locationHeader = response.getFirstHeader(HTTP_RESPONSE_HEADER_LOCATION);
        Assert.assertNotNull(locationHeader, "Login response header is null");
        EntityUtils.consume(response.getEntity());

        // Request will return with a 302 to the authorize end point. Doing a GET will give the sessionDataKeyConsent
        response = sendGetRequest(client, locationHeader.getValue());

        String locationValue = getLocationHeaderValue(response);
        Assert.assertTrue(locationValue.contains(SESSION_DATA_KEY_CONSENT),
                "sessionDataKeyConsent not found in response.");

        EntityUtils.consume(response.getEntity());

        // Extract sessionDataKeyConsent from the location value.
        return DataExtractUtil.getParamFromURIString(locationValue, SESSION_DATA_KEY_CONSENT);
    }

    /**
     * Retrieves the last accessed time of the existing session.
     *
     * @return              Last accessed time of the session.
     * @throws Exception    Exception.
     */
    private Long getLastAccessedTimeOfSession() throws Exception {

        String encodedCredentials =
                Base64.encodeBase64String((authenticatingUserName + ":" + authenticatingCredential).getBytes());
        BasicHeader authorizationHeader = new BasicHeader(HttpHeaders.AUTHORIZATION, "Basic " + encodedCredentials);
        List<Header> headers = new ArrayList<>();
        headers.add(authorizationHeader);
        HttpResponse response = sendGetRequestWithCustomHeaders(firstPartyClient, SESSIONS_ENDPOINT_URI, headers);
        Assert.assertNotNull(response.getEntity(), "Session extension validation request failed. Response is invalid.");

        HttpEntity entity = response.getEntity();
        String responseString = EntityUtils.toString(entity, "UTF-8");
        Assert.assertTrue(responseString.startsWith("{"), "No session information returned by the sessions endpoint.");

        JSONObject responseJson = new JSONObject(responseString);
        Assert.assertNotNull(responseJson.get("sessions"), "No session information returned by the sessions endpoint.");
        JSONArray sessionsList = (JSONArray) responseJson.get("sessions");
        JSONObject session = (JSONObject) sessionsList.get(0);
        String lastAccessTime = (String) session.get("lastAccessTime");
        Assert.assertNotNull(lastAccessTime, "No session information returned by the sessions endpoint.");
        EntityUtils.consume(entity);
        return Long.parseLong(lastAccessTime);
    }

    /**
     * Returns the response of a GET request to a REST API.
     *
     * @param client        Http client.
     * @param locationURL   URL of the request.
     * @param headerList    List of headers.
     * @return              Http response.
     * @throws Exception    Exception.
     */
    private HttpResponse sendGetRequestWithCustomHeaders(HttpClient client,
                                                         String locationURL,
                                                         List<Header> headerList)
            throws Exception {

        HttpGet getRequest = new HttpGet(locationURL);
        Header[] headers = new Header[headerList.size()];
        for (int i = 0; i < headerList.size(); i++) {
            headers[i] = headerList.get(i);
        }
        getRequest.setHeaders(headers);
        getRequest.setHeader(HttpHeaders.USER_AGENT, USER_AGENT);
        HttpResponse response = client.execute(getRequest);

        return response;
    }
}

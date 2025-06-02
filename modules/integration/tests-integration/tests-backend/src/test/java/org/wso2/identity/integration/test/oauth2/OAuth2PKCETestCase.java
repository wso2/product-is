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

import com.nimbusds.oauth2.sdk.AccessTokenResponse;
import com.nimbusds.oauth2.sdk.AuthorizationCode;
import com.nimbusds.oauth2.sdk.AuthorizationCodeGrant;
import com.nimbusds.oauth2.sdk.ErrorObject;
import com.nimbusds.oauth2.sdk.TokenErrorResponse;
import com.nimbusds.oauth2.sdk.TokenRequest;
import com.nimbusds.oauth2.sdk.TokenResponse;
import com.nimbusds.oauth2.sdk.auth.ClientSecretBasic;
import com.nimbusds.oauth2.sdk.auth.Secret;
import com.nimbusds.oauth2.sdk.http.HTTPResponse;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.pkce.CodeVerifier;
import com.nimbusds.oauth2.sdk.token.Tokens;
import com.nimbusds.openid.connect.sdk.OIDCTokenResponseParser;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Lookup;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.cookie.CookieSpecProvider;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.RFC6265CookieSpecProvider;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationPatchModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationResponseModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ClaimConfiguration;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.OAuth2PKCEConfiguration;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.OpenIDConnectConfiguration;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.SubjectConfig;
import org.wso2.identity.integration.test.utils.DataExtractUtil;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class OAuth2PKCETestCase extends OAuth2ServiceAbstractIntegrationTest {

    private static final String TEST_NONCE = "test_nonce";
    private static final String CALLBACK_URL = "https://localhost/callback";
    private static final String CODE_CHALLENGE = "code_challenge";
    private static final String CODE_CHALLENGE_METHOD = "code_challenge_method";
    private final CookieStore cookieStore = new BasicCookieStore();
    private Lookup<CookieSpecProvider> cookieSpecRegistry;
    private RequestConfig requestConfig;
    private CloseableHttpClient client;
    private String sessionDataKey;
    private String sessionDataKeyConsent;
    private AuthorizationCode authorizationCode;
    private String applicationId;
    private String pkceVerifier;

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.init(TestUserMode.SUPER_TENANT_USER);

        cookieSpecRegistry = RegistryBuilder.<CookieSpecProvider>create().register(CookieSpecs.DEFAULT,
                new RFC6265CookieSpecProvider()).build();
        requestConfig = RequestConfig.custom().setCookieSpec(CookieSpecs.DEFAULT).build();
        client = HttpClientBuilder.create()
                .setDefaultCookieStore(cookieStore)
                .setDefaultCookieSpecRegistry(cookieSpecRegistry)
                .setDefaultRequestConfig(requestConfig)
                .disableRedirectHandling()
                .build();
        pkceVerifier = getPKCECodeVerifier();
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {

        deleteApp(applicationId);

        consumerKey = null;
        consumerSecret = null;
        pkceVerifier = null;
        cookieStore.clear();
        client.close();
        restClient.closeHttpClient();
    }

    @Test(groups = "wso2.is", description = "Check Oauth2 application flow.")
    public void testRegisterApplication() throws Exception {

        ApplicationResponseModel oAuthConsumerApp = getBasicOAuthApplication(CALLBACK_URL);
        Assert.assertNotNull(oAuthConsumerApp, "OAuth App creation failed.");
        applicationId = oAuthConsumerApp.getId();

        ClaimConfiguration claimConfig = new ClaimConfiguration().subject(new SubjectConfig());
        claimConfig.getSubject().setIncludeTenantDomain(true);
        claimConfig.getSubject().setIncludeUserDomain(true);
        updateApplication(applicationId, new ApplicationPatchModel().claimConfiguration(claimConfig));

        OpenIDConnectConfiguration oidcConfig = getOIDCInboundDetailsOfApplication(applicationId);
        consumerKey = oidcConfig.getClientId();
        consumerSecret = oidcConfig.getClientSecret();
        Assert.assertNotNull(consumerKey, "Consumer Key is null.");
        Assert.assertNotNull(consumerSecret, "Consumer Secret is null.");
    }

    @Test(groups = "wso2.is", description = "Test sending authz and token request with valid PKCE.",
            dependsOnMethods = "testRegisterApplication")
    public void testDefaultAppValidPKCE() throws Exception {

        refreshHTTPClient();
        AccessTokenResponse tokenRequest = getAuthzCodeAccessToken(getPKCECodeChallenge(pkceVerifier),
                "S256", pkceVerifier);
        Tokens tokens = tokenRequest.getTokens();
        String accessToken = tokens.getAccessToken().toString();
        Assert.assertNotNull(accessToken, "Access token is null.");
    }

    @Test(groups = "wso2.is", description = "Test sending authz and token request with invalid PKCE challenge method.",
            dependsOnMethods = "testDefaultAppValidPKCE")
    public void testDefaultAppInvalidChallengeMethod() throws Exception {

        refreshHTTPClient();
        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_RESPONSE_TYPE,
                OAuth2Constant.OAUTH2_GRANT_TYPE_CODE));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_CLIENT_ID, consumerKey));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_REDIRECT_URI, CALLBACK_URL));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_SCOPE, OAuth2Constant.OAUTH2_SCOPE_OPENID_WITH_INTERNAL_LOGIN));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_NONCE, TEST_NONCE));
        urlParameters.add(new BasicNameValuePair(CODE_CHALLENGE, getPKCECodeChallenge(pkceVerifier)));
        urlParameters.add(new BasicNameValuePair(CODE_CHALLENGE_METHOD, "invalidCodeChallenge"));

        HttpResponse response =
                sendPostRequestWithParameters(client, urlParameters, OAuth2Constant.AUTHORIZE_ENDPOINT_URL);
        String locationValue = getLocationHeaderValue(response);
        Assert.assertTrue(locationValue.contains("error_description=Unsupported+PKCE+Challenge+Method"),
                "Invalid challenge method failed, unable to get required error description.");
        EntityUtils.consume(response.getEntity());
    }

    @Test(groups = "wso2.is", description = "Test sending authz and token request with invalid PKCE verifier.",
            dependsOnMethods = "testDefaultAppInvalidChallengeMethod")
    public void testDefaultAppInvalidVerifier() throws Exception {

        refreshHTTPClient();
        authCodeGrantSendAuthRequestPost(getPKCECodeChallenge(pkceVerifier), "S256");
        authCodeGrantSendLoginPost();
        authCodeGrantSendApprovalPost();

        ClientID clientID = new ClientID(consumerKey);
        Secret clientSecret = new Secret(consumerSecret);
        ClientSecretBasic clientSecretBasic = new ClientSecretBasic(clientID, clientSecret);

        URI callbackURI = new URI(CALLBACK_URL);
        AuthorizationCodeGrant authorizationCodeGrant = new AuthorizationCodeGrant(authorizationCode, callbackURI,
                    new CodeVerifier(getPKCECodeVerifier()));

        TokenRequest tokenReq = new TokenRequest(new URI(OAuth2Constant.ACCESS_TOKEN_ENDPOINT), clientSecretBasic,
                authorizationCodeGrant);

        HTTPResponse tokenHTTPResp = tokenReq.toHTTPRequest().send();
        Assert.assertNotNull(tokenHTTPResp, "Access token http response is null.");

        TokenResponse tokenResponse = OIDCTokenResponseParser.parse(tokenHTTPResp);
        Assert.assertNotNull(tokenResponse, "Access token response is null.");

        Assert.assertTrue(tokenResponse instanceof TokenErrorResponse,
                "Invalid access token response doesn't contain errors.");

        TokenErrorResponse accessTokenResponse = (TokenErrorResponse) tokenResponse;
        ErrorObject errorObject =  accessTokenResponse.getErrorObject();
        Assert.assertEquals(errorObject.getHTTPStatusCode(), HttpStatus.SC_BAD_REQUEST,
                "Invalid access token response doesn't contain bad request status code.");
        Assert.assertEquals(errorObject.getCode(), "invalid_grant",
                "Invalid access token response doesn't contain required error message.");
        Assert.assertEquals(errorObject.getDescription(), "PKCE validation failed",
                "Invalid access token response doesn't contain required error description.");
    }

    @Test(groups = "wso2.is", description = "Test sending authz and token request without PKCE verifier.",
            dependsOnMethods = "testDefaultAppInvalidVerifier")
    public void testDefaultAppWithoutVerifier() throws Exception {

        refreshHTTPClient();
        authCodeGrantSendAuthRequestPost(getPKCECodeChallenge(pkceVerifier), "S256");
        authCodeGrantSendLoginPost();
        authCodeGrantSendApprovalPost();

        ClientID clientID = new ClientID(consumerKey);
        Secret clientSecret = new Secret(consumerSecret);
        ClientSecretBasic clientSecretBasic = new ClientSecretBasic(clientID, clientSecret);

        URI callbackURI = new URI(CALLBACK_URL);
        AuthorizationCodeGrant authorizationCodeGrant = new AuthorizationCodeGrant(authorizationCode, callbackURI);

        TokenRequest tokenReq = new TokenRequest(new URI(OAuth2Constant.ACCESS_TOKEN_ENDPOINT), clientSecretBasic,
                authorizationCodeGrant);

        HTTPResponse tokenHTTPResp = tokenReq.toHTTPRequest().send();
        Assert.assertNotNull(tokenHTTPResp, "Access token http response is null.");

        TokenResponse tokenResponse = OIDCTokenResponseParser.parse(tokenHTTPResp);
        Assert.assertNotNull(tokenResponse, "Access token response is null.");

        Assert.assertTrue(tokenResponse instanceof TokenErrorResponse,
                "Invalid access token response doesn't contain errors.");

        TokenErrorResponse accessTokenResponse = (TokenErrorResponse) tokenResponse;
        ErrorObject errorObject =  accessTokenResponse.getErrorObject();
        Assert.assertEquals(errorObject.getHTTPStatusCode(), HttpStatus.SC_BAD_REQUEST,
                "Invalid access token response doesn't contain bad request status code.");
        Assert.assertEquals(errorObject.getCode(), "invalid_grant",
                "Invalid access token response doesn't contain required error message.");
        Assert.assertEquals(errorObject.getDescription(), "Empty PKCE code_verifier sent. " +
                        "This authorization code requires a PKCE verification to obtain an access token.",
                "Invalid access token response doesn't contain required error description.");
    }

    @Test(groups = "wso2.is", description = "Test sending authz and token request with plain PKCE challenge method.",
            dependsOnMethods = "testDefaultAppWithoutVerifier")
    public void testDefaultAppWithPlainChallengeMethod() throws Exception {

        refreshHTTPClient();
        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_RESPONSE_TYPE,
                OAuth2Constant.OAUTH2_GRANT_TYPE_CODE));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_CLIENT_ID, consumerKey));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_REDIRECT_URI, CALLBACK_URL));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_SCOPE,
                OAuth2Constant.OAUTH2_SCOPE_OPENID_WITH_INTERNAL_LOGIN));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_NONCE, TEST_NONCE));
        urlParameters.add(new BasicNameValuePair(CODE_CHALLENGE, getPKCECodeChallenge(pkceVerifier)));
        urlParameters.add(new BasicNameValuePair(CODE_CHALLENGE_METHOD, "plain"));

        HttpResponse response =
                sendPostRequestWithParameters(client, urlParameters, OAuth2Constant.AUTHORIZE_ENDPOINT_URL);
        String locationValue = getLocationHeaderValue(response);
        Assert.assertTrue(locationValue.contains("error=invalid_request"),
                "Invalid challenge method failed, unable to get required error message.");
        Assert.assertTrue(locationValue.contains("error_description=This+application+does+not+support+%22plain%22+" +
                "transformation+algorithm"),
                "Invalid challenge method failed, unable to get required error description.");
        EntityUtils.consume(response.getEntity());
    }

    @Test(groups = "wso2.is", description = "Update Oauth2 application for mandating PKCE.",
            dependsOnMethods = "testDefaultAppWithPlainChallengeMethod")
    public void testUpdateApplication() throws Exception {

        OpenIDConnectConfiguration oidcConfig = getOIDCInboundDetailsOfApplication(applicationId);
        oidcConfig.setPkce(new OAuth2PKCEConfiguration().mandatory(true).supportPlainTransformAlgorithm(false));
        updateApplicationInboundConfig(applicationId, oidcConfig, OIDC);

        OpenIDConnectConfiguration oidcConfigRetrieved = getOIDCInboundDetailsOfApplication(applicationId);
        Assert.assertTrue(oidcConfigRetrieved.getPkce().getMandatory(), "PKCE mandatory couldn't set to true.");
        Assert.assertFalse(oidcConfigRetrieved.getPkce().getSupportPlainTransformAlgorithm(),
                "PKCE support plain transform algorithm couldn't set to false.");
        testDefaultAppWithPlainChallengeMethod();
        testDefaultAppInvalidVerifier();
        testUpdatedAppInvalidChallengeMethod();
        testDefaultAppValidPKCE();
        testUpdatedAppWithoutVerifier();
    }

    @Test(groups = "wso2.is", description = "Test sending authz and token request without PKCE verifier.",
            dependsOnMethods = "testDefaultAppInvalidVerifier")
    public void testUpdatedAppWithoutVerifier() throws Exception {

        refreshHTTPClient();
        authCodeGrantSendAuthRequestPost(getPKCECodeChallenge(pkceVerifier), "S256");
        authCodeGrantSendLoginPost();
        authCodeGrantSendApprovalPost();

        ClientID clientID = new ClientID(consumerKey);
        Secret clientSecret = new Secret(consumerSecret);
        ClientSecretBasic clientSecretBasic = new ClientSecretBasic(clientID, clientSecret);

        URI callbackURI = new URI(CALLBACK_URL);
        AuthorizationCodeGrant authorizationCodeGrant = new AuthorizationCodeGrant(authorizationCode, callbackURI);

        TokenRequest tokenReq = new TokenRequest(new URI(OAuth2Constant.ACCESS_TOKEN_ENDPOINT), clientSecretBasic,
                authorizationCodeGrant);

        HTTPResponse tokenHTTPResp = tokenReq.toHTTPRequest().send();
        Assert.assertNotNull(tokenHTTPResp, "Access token http response is null.");

        TokenResponse tokenResponse = OIDCTokenResponseParser.parse(tokenHTTPResp);
        Assert.assertNotNull(tokenResponse, "Access token response is null.");

        Assert.assertTrue(tokenResponse instanceof TokenErrorResponse,
                "Invalid access token response doesn't contain errors.");

        TokenErrorResponse accessTokenResponse = (TokenErrorResponse) tokenResponse;
        ErrorObject errorObject =  accessTokenResponse.getErrorObject();
        Assert.assertEquals(errorObject.getHTTPStatusCode(), HttpStatus.SC_BAD_REQUEST,
                "Invalid access token response doesn't contain bad request status code.");
        Assert.assertEquals(errorObject.getCode(), "invalid_grant",
                "Invalid access token response doesn't contain required error message.");
        Assert.assertEquals(errorObject.getDescription(), "No PKCE code verifier found." +
                        "PKCE is mandatory for this oAuth 2.0 application.",
                "Invalid access token response doesn't contain required error description.");
    }

    @Test(groups = "wso2.is", description = "Test sending authz and token request with invalid PKCE challenge method.",
            dependsOnMethods = "testUpdateApplication")
    public void testUpdatedAppInvalidChallengeMethod() throws Exception {

        refreshHTTPClient();
        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_RESPONSE_TYPE,
                OAuth2Constant.OAUTH2_GRANT_TYPE_CODE));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_CLIENT_ID, consumerKey));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_REDIRECT_URI, CALLBACK_URL));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_SCOPE,
                OAuth2Constant.OAUTH2_SCOPE_OPENID_WITH_INTERNAL_LOGIN));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_NONCE, TEST_NONCE));
        urlParameters.add(new BasicNameValuePair(CODE_CHALLENGE, getPKCECodeChallenge(pkceVerifier)));
        urlParameters.add(new BasicNameValuePair(CODE_CHALLENGE_METHOD, "invalidCodeChallenge"));

        HttpResponse response =
                sendPostRequestWithParameters(client, urlParameters, OAuth2Constant.AUTHORIZE_ENDPOINT_URL);
        String locationValue = getLocationHeaderValue(response);
        Assert.assertTrue(locationValue.contains("error_description=PKCE+is+mandatory+for+this+application.+PKCE+" +
                        "Challenge+is+not+provided+or+is+not+upto+RFC+7636+specification."),
                "Invalid error description retrieved.");
        EntityUtils.consume(response.getEntity());
    }

    @Test(groups = "wso2.is", description = "Test sending authz request without challenge for PKCE mandated app.",
            dependsOnMethods = "testUpdatedAppInvalidChallengeMethod")
    public void testUpdatedAppWithoutChallenge() throws Exception {

        refreshHTTPClient();
        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_RESPONSE_TYPE,
                OAuth2Constant.OAUTH2_GRANT_TYPE_CODE));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_CLIENT_ID, consumerKey));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_REDIRECT_URI, CALLBACK_URL));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_SCOPE,
                OAuth2Constant.OAUTH2_SCOPE_OPENID_WITH_INTERNAL_LOGIN));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_NONCE, TEST_NONCE));

        HttpResponse response =
                sendPostRequestWithParameters(client, urlParameters, OAuth2Constant.AUTHORIZE_ENDPOINT_URL);
        String locationValue = getLocationHeaderValue(response);
        Assert.assertTrue(locationValue.contains("error_description=PKCE+is+mandatory+for+this+application.+PKCE+" +
                "Challenge+is+not+provided+or+is+not+upto+RFC+7636+specification."),
                "Invalid error description retrieved.");
        EntityUtils.consume(response.getEntity());
    }

    @Test(groups = "wso2.is", description = "Update Oauth2 application for mandating PKCE.",
            dependsOnMethods = "testDefaultAppWithPlainChallengeMethod")
    public void testUpdateApplicationWithPlainChallenge() throws Exception {

        OpenIDConnectConfiguration oidcConfig = getOIDCInboundDetailsOfApplication(applicationId);
        oidcConfig.setPkce(new OAuth2PKCEConfiguration().mandatory(true).supportPlainTransformAlgorithm(true));
        updateApplicationInboundConfig(applicationId, oidcConfig, OIDC);

        OpenIDConnectConfiguration oidcConfigRetrieved = getOIDCInboundDetailsOfApplication(applicationId);
        Assert.assertTrue(oidcConfigRetrieved.getPkce().getMandatory(), "PKCE mandatory couldn't set to true.");
        Assert.assertTrue(oidcConfigRetrieved.getPkce().getSupportPlainTransformAlgorithm(),
                "PKCE support plain transform algorithm couldn't set to true.");
        testDefaultAppInvalidVerifier();
        testDefaultAppValidPKCE();
        testUpdatedAppWithoutVerifier();
        testUpdatedAppInvalidChallengeMethod();
    }

    @Test(groups = "wso2.is", description = "Test sending authz and token request with valid PKCE.",
            dependsOnMethods = "testUpdateApplicationWithPlainChallenge")
    public void testUpdatedAppWithValidPlainAlg() throws Exception {

        AccessTokenResponse tokenRequest = getAuthzCodeAccessToken(pkceVerifier,
                "plain", pkceVerifier);
        Tokens tokens = tokenRequest.getTokens();
        String accessToken = tokens.getAccessToken().toString();
        Assert.assertNotNull(accessToken, "Access token is null.");
    }

    private AccessTokenResponse getAuthzCodeAccessToken(String codeChallenge, String codeChallengeMethod,
                                                        String codeChallengeVerifier) throws Exception {

        authCodeGrantSendAuthRequestPost(codeChallenge, codeChallengeMethod);
        authCodeGrantSendLoginPost();
        authCodeGrantSendApprovalPost();
        return authCodeGrantSendGetTokensPost(codeChallengeVerifier);
    }

    private void authCodeGrantSendAuthRequestPost(String codeChallenge, String codeChallengeMethod) throws Exception {

        // Send a direct auth code request to IS instance.
        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_RESPONSE_TYPE,
                OAuth2Constant.OAUTH2_GRANT_TYPE_CODE));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_CLIENT_ID, consumerKey));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_REDIRECT_URI, CALLBACK_URL));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_SCOPE,
                OAuth2Constant.OAUTH2_SCOPE_OPENID_WITH_INTERNAL_LOGIN));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_NONCE, TEST_NONCE));
        if (codeChallenge != null) {
            urlParameters.add(new BasicNameValuePair(CODE_CHALLENGE, codeChallenge));
        }
        if (codeChallengeMethod != null) {
            urlParameters.add(new BasicNameValuePair(CODE_CHALLENGE_METHOD, codeChallengeMethod));
        }

        HttpResponse response =
                sendPostRequestWithParameters(client, urlParameters
                        ,
                        OAuth2Constant.AUTHORIZE_ENDPOINT_URL);
        Assert.assertNotNull(response, "Authorization request failed. Authorized response is null");

        String locationValue = getLocationHeaderValue(response);
        Assert.assertTrue(locationValue.contains(OAuth2Constant.SESSION_DATA_KEY),
                "sessionDataKey not found in response.");

        // Extract sessionDataKey from the location value.
        sessionDataKey = DataExtractUtil.getParamFromURIString(locationValue, OAuth2Constant.SESSION_DATA_KEY);
        Assert.assertNotNull(sessionDataKey, "Session data key is null.");
        EntityUtils.consume(response.getEntity());
    }

    private void authCodeGrantSendLoginPost() throws Exception {

        sessionDataKeyConsent = getSessionDataKeyConsent(client, sessionDataKey);
        Assert.assertNotNull(sessionDataKeyConsent, "Invalid session key consent.");
    }

    private void authCodeGrantSendApprovalPost() throws Exception {

        HttpResponse response = sendApprovalPost(client, sessionDataKeyConsent);
        Assert.assertNotNull(response, "Approval request failed. response is invalid.");

        Header locationHeader =
                response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        Assert.assertNotNull(locationHeader, "Approval request failed. Location header is null.");

        String locationValue = getLocationHeaderValue(response);
        Assert.assertTrue(locationValue.contains(OAuth2Constant.AUTHORIZATION_CODE_NAME),
                "Authorization code not found in the response.");

        // Extract authorization code from the location value.
        authorizationCode = new AuthorizationCode(DataExtractUtil.getParamFromURIString(locationValue,
                OAuth2Constant.AUTHORIZATION_CODE_NAME));
        Assert.assertNotNull(authorizationCode, "Authorization code is null.");
        EntityUtils.consume(response.getEntity());
    }

    private AccessTokenResponse authCodeGrantSendGetTokensPost(String codeChallengeVerifier) throws Exception {

        ClientID clientID = new ClientID(consumerKey);
        Secret clientSecret = new Secret(consumerSecret);
        ClientSecretBasic clientSecretBasic = new ClientSecretBasic(clientID, clientSecret);

        URI callbackURI = new URI(CALLBACK_URL);
        AuthorizationCodeGrant authorizationCodeGrant = new AuthorizationCodeGrant(authorizationCode, callbackURI);
        if (codeChallengeVerifier != null) {
            authorizationCodeGrant = new AuthorizationCodeGrant(authorizationCode, callbackURI,
                    new CodeVerifier(codeChallengeVerifier));
        }

        TokenRequest tokenReq = new TokenRequest(new URI(OAuth2Constant.ACCESS_TOKEN_ENDPOINT), clientSecretBasic,
                authorizationCodeGrant);

        HTTPResponse tokenHTTPResp = tokenReq.toHTTPRequest().send();
        Assert.assertNotNull(tokenHTTPResp, "Access token http response is null.");

        TokenResponse tokenResponse = OIDCTokenResponseParser.parse(tokenHTTPResp);
        Assert.assertNotNull(tokenResponse, "Access token response is null.");

        Assert.assertFalse(tokenResponse instanceof TokenErrorResponse,
                "Access token response contains errors.");

        AccessTokenResponse accessTokenResponse = (AccessTokenResponse) tokenResponse;
        Tokens tokens = accessTokenResponse.getTokens();

        String accessToken = tokens.getAccessToken().toString();
        Assert.assertNotNull(accessToken, "Access token is null.");
        return accessTokenResponse;
    }

    private String getSessionDataKeyConsent(CloseableHttpClient client, String sessionDataKey)
            throws IOException, URISyntaxException {

        HttpResponse response = sendLoginPost(client, sessionDataKey);
        Assert.assertNotNull(response, "Login request failed. response is null.");

        Header locationHeader =
                response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        Assert.assertNotNull(locationHeader, "Login response header is null");
        EntityUtils.consume(response.getEntity());

        // Request will return with a 302 to the authorize end point. Doing a GET will give the sessionDataKeyConsent
        response = sendGetRequest(client, locationHeader.getValue());

        String locationValue = getLocationHeaderValue(response);
        Assert.assertTrue(locationValue.contains(OAuth2Constant.SESSION_DATA_KEY_CONSENT),
                "sessionDataKeyConsent not found in response.");

        EntityUtils.consume(response.getEntity());

        // Extract sessionDataKeyConsent from the location value.
        return DataExtractUtil.getParamFromURIString(locationValue, OAuth2Constant.SESSION_DATA_KEY_CONSENT);
    }

    private String getLocationHeaderValue(HttpResponse response) {

        Header location = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        Assert.assertNotNull(location);
        return location.getValue();
    }

    /**
     * Refresh the cookie store and http client.
     */
    private void refreshHTTPClient() {

        cookieStore.clear();
        client = HttpClientBuilder.create().disableRedirectHandling()
                .setDefaultCookieStore(cookieStore)
                .setDefaultCookieSpecRegistry(cookieSpecRegistry)
                .setDefaultRequestConfig(requestConfig)
                .build();
    }

    /**
     * Generates PKCE code verifier.
     *
     * @return PKCE code verifier.
     */
    private String getPKCECodeVerifier() {

        return (UUID.randomUUID() + UUID.randomUUID().toString()).replaceAll("-", "");
    }

    /**
     * Generates PKCE code challenge.
     *
     * @param codeVerifier PKCE code verifier.
     * @return Code challenge.
     * @throws NoSuchAlgorithmException No Such Algorithm Exception.
     */
    private String getPKCECodeChallenge(String codeVerifier) throws NoSuchAlgorithmException {

        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(codeVerifier.getBytes(StandardCharsets.US_ASCII));
        //Base64 encoded string is trimmed to remove trailing CR LF
        return new String(Base64.encodeBase64URLSafe(hash), StandardCharsets.UTF_8).trim();
    }
}

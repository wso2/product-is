/*
 *  Copyright (c) 2021 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.identity.integration.test.auth;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.oauth2.sdk.AuthorizationCode;
import com.nimbusds.oauth2.sdk.AuthorizationCodeGrant;
import com.nimbusds.oauth2.sdk.AuthorizationGrant;
import com.nimbusds.oauth2.sdk.Scope;
import com.nimbusds.oauth2.sdk.TokenErrorResponse;
import com.nimbusds.oauth2.sdk.TokenRequest;
import com.nimbusds.oauth2.sdk.TokenResponse;
import com.nimbusds.oauth2.sdk.auth.ClientAuthentication;
import com.nimbusds.oauth2.sdk.auth.ClientSecretBasic;
import com.nimbusds.oauth2.sdk.auth.Secret;
import com.nimbusds.oauth2.sdk.http.HTTPResponse;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.openid.connect.sdk.OIDCTokenResponse;
import com.nimbusds.openid.connect.sdk.OIDCTokenResponseParser;
import com.nimbusds.openid.connect.sdk.token.OIDCTokens;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.commons.lang.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
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
import org.wso2.carbon.identity.application.common.model.xsd.Claim;
import org.wso2.carbon.identity.application.common.model.xsd.ClaimConfig;
import org.wso2.carbon.identity.application.common.model.xsd.ClaimMapping;
import org.wso2.carbon.identity.application.common.model.xsd.ServiceProvider;
import org.wso2.carbon.integration.common.admin.client.AuthenticatorClient;
import org.wso2.identity.integration.common.clients.application.mgt.ApplicationManagementServiceClient;
import org.wso2.identity.integration.common.clients.oauth.OauthAdminClient;
import org.wso2.identity.integration.test.util.Utils;
import org.wso2.identity.integration.test.utils.DataExtractUtil;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.wso2.identity.integration.test.utils.OAuth2Constant.ACCESS_TOKEN_ENDPOINT;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.AUTHORIZATION_CODE_NAME;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.CALLBACK_URL;

/**
 * Test for temporary claim persistence with adaptive authentication scripts.
 */
public class AdaptiveScriptTemporaryClaimPersistenceTestCase extends AbstractAdaptiveAuthenticationTestCase {

    private AuthenticatorClient logManager;
    private OauthAdminClient oauthAdminClient;
    private ApplicationManagementServiceClient applicationManagementServiceClient;
    private CookieStore cookieStore = new BasicCookieStore();
    private CloseableHttpClient client;
    private Lookup<CookieSpecProvider> cookieSpecRegistry;
    private RequestConfig requestConfig;
    private HttpResponse response;
    private String idToken;
    private AuthorizationCode authorizationCode;
    private Header locationHeader;
    private List<NameValuePair> consentParameters = new ArrayList<>();
    private static final String APPLICATION_NAME = "testOauthApp";
    // Nickname, which is an already existing OIDC claim, used as the temporary claim for ease of implementation.
    private static final String TEMPORARY_CLAIM_URI = "http://wso2.org/claims/nickname";
    private static final String TEMPORARY_CLAIM_NAME = "nickname";
    private static final String TEMPORARY_CLAIM_URI_PARAM_NAME = "tempClaim";
    private static final String TEMPORARY_CLAIM_URI_PARAM_VALUE = "tempValue";

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.init();
        logManager = new AuthenticatorClient(backendURL);
        String cookie = this.logManager.login(isServer.getSuperTenant().getTenantAdmin().getUserName(),
                isServer.getSuperTenant().getTenantAdmin().getPassword(),
                isServer.getInstance().getHosts().get("default"));
        oauthAdminClient = new OauthAdminClient(backendURL, cookie);
        ConfigurationContext configContext = ConfigurationContextFactory
                .createConfigurationContextFromFileSystem(null, null);
        applicationManagementServiceClient = new ApplicationManagementServiceClient(sessionCookie, backendURL,
                configContext);

        cookieSpecRegistry = RegistryBuilder.<CookieSpecProvider>create()
                .register(CookieSpecs.DEFAULT, new RFC6265CookieSpecProvider())
                .build();
        requestConfig = RequestConfig.custom()
                .setCookieSpec(CookieSpecs.DEFAULT)
                .build();
        client = HttpClientBuilder.create()
                .setDefaultRequestConfig(requestConfig)
                .setDefaultCookieSpecRegistry(cookieSpecRegistry)
                .disableRedirectHandling()
                .setDefaultCookieStore(cookieStore)
                .build();

        String script = getConditionalAuthScript("TemporaryClaimsAdaptiveScript.js");

        createOauthApp(CALLBACK_URL, APPLICATION_NAME, oauthAdminClient);
        ServiceProvider serviceProvider = createServiceProvider(APPLICATION_NAME,
                applicationManagementServiceClient, oauthAdminClient, script);
        setClaimConfigsToServiceProvider(serviceProvider);
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {

        oauthAdminClient.removeOAuthApplicationData(consumerKey);
        applicationManagementServiceClient.deleteApplication(APPLICATION_NAME);
        client.close();

        this.logManager.logOut();
        logManager = null;
    }

    @Test(groups = "wso2.is", description = "First authentication request to register temporary claim value.")
    public void testSendTemporaryClaimWithAuthenticationRequest() throws Exception {

        Map<String, String> temporaryClaims = new HashMap<>();
        temporaryClaims.put(TEMPORARY_CLAIM_URI_PARAM_NAME, TEMPORARY_CLAIM_URI_PARAM_VALUE);
        locationHeader = initiateAuthenticationFlow(consumerKey, client, temporaryClaims);
        response = redirectToLoginPage(locationHeader.getValue());
        String sessionDataKey = extractKeyFromResponse(response, "sessionDataKey", APPLICATION_NAME);
        EntityUtils.consume(response.getEntity());

        response = sendLoginPost(client, sessionDataKey);
        EntityUtils.consume(response.getEntity());

        locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        Assert.assertNotNull(locationHeader, "Login response header is null");
        response = sendConsentGetRequest(locationHeader.getValue(), cookieStore, consentParameters);
        locationHeader = handleConsent();

        getIdToken();
        testIdTokenClaimAvailability();
    }

    @Test(groups = "wso2.is", description = "Second authentication request to check whether temporary claim persists.",
            dependsOnMethods = "testSendTemporaryClaimWithAuthenticationRequest")
    public void testTemporaryClaimWithSuccessiveAuthenticationRequest() throws Exception {

        locationHeader = initiateAuthenticationFlow(consumerKey, client, new HashMap<>());
        Assert.assertNotNull(locationHeader, "Login response header is null");

        response = sendExistingSessionConsentGetRequest(cookieStore, consentParameters);
        locationHeader = handleConsent();

        getIdToken();
        testIdTokenClaimAvailability();
    }

    private void getIdToken() throws Exception {

        Assert.assertTrue(locationHeader.getValue().contains(AUTHORIZATION_CODE_NAME),
                "Authorization code not found in the response.");
        // Extract authorization code from the location header.
        authorizationCode =
                new AuthorizationCode(DataExtractUtil.getParamFromURIString(locationHeader.getValue(),
                        AUTHORIZATION_CODE_NAME));
        Assert.assertNotNull(authorizationCode, "Authorization code is null.");
        EntityUtils.consume(response.getEntity());

        URI callbackURI = new URI(CALLBACK_URL);
        AuthorizationCodeGrant authorizationCodeGrant = new AuthorizationCodeGrant(authorizationCode, callbackURI);
        OIDCTokens oidcTokens = makeTokenRequest(authorizationCodeGrant, ACCESS_TOKEN_ENDPOINT,null);

        idToken = oidcTokens.getIDTokenString();
        Assert.assertNotNull(idToken, "ID token is null");
    }

    private void setClaimConfigsToServiceProvider(ServiceProvider serviceProvider) throws Exception {

        ClaimConfig claimConfig = new ClaimConfig();
        Claim tempClaim = new Claim();
        tempClaim.setClaimUri(TEMPORARY_CLAIM_URI);
        ClaimMapping tempClaimMapping = new ClaimMapping();
        tempClaimMapping.setRequested(true);
        tempClaimMapping.setLocalClaim(tempClaim);
        tempClaimMapping.setRemoteClaim(tempClaim);

        claimConfig.setClaimMappings(new ClaimMapping[]{tempClaimMapping});
        serviceProvider.setClaimConfig(claimConfig);
        applicationManagementServiceClient.updateApplicationData(serviceProvider);
    }

    /**
     * Checks whether the temporary claim is available in the ID token.
     *
     * @throws Exception Exception.
     */
    private void testIdTokenClaimAvailability() throws Exception {

        JWTClaimsSet claims = SignedJWT.parse(idToken).getJWTClaimsSet();
        Assert.assertNotNull(claims, "ID token claim set is null");

        String temporaryClaim = (String) claims.getClaim(TEMPORARY_CLAIM_NAME);
        Assert.assertNotNull(temporaryClaim, "Temporary claim not available in ID token.");
        Assert.assertEquals(temporaryClaim, TEMPORARY_CLAIM_URI_PARAM_VALUE);
    }

    /**
     * Initiates the authentication flow after appending the temporary claims as URL parameters.
     *
     * @param consumerKey       Client ID of application.
     * @param client            Http client used to send requests.
     * @param temporaryClaims   Temporary claims required.
     * @return                  Location header of initial authentication response.
     * @throws IOException      Exception.
     */
    private Header initiateAuthenticationFlow(String consumerKey, HttpClient client,
                                              Map<String, String> temporaryClaims)
            throws IOException {

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("response_type", OAuth2Constant.OAUTH2_GRANT_TYPE_CODE));
        urlParameters.add(new BasicNameValuePair("scope",
                OAuth2Constant.OAUTH2_SCOPE_OPENID_WITH_INTERNAL_LOGIN + " " +
                        OAuth2Constant.OAUTH2_SCOPE_PROFILE));
        urlParameters.add(new BasicNameValuePair("redirect_uri", CALLBACK_URL));
        urlParameters.add(new BasicNameValuePair("client_id", consumerKey));
        urlParameters.add(new BasicNameValuePair("acr_values", "acr1"));
        urlParameters.add(new BasicNameValuePair("accessEndpoint", OAuth2Constant.ACCESS_TOKEN_ENDPOINT));
        urlParameters.add(new BasicNameValuePair("authorize", OAuth2Constant.AUTHORIZE_PARAM));

        for (Map.Entry<String, String> entry: temporaryClaims.entrySet()) {
            urlParameters.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
        }

        response = sendPostRequestWithParameters(client, urlParameters,
                getTenantQualifiedURL( OAuth2Constant.APPROVAL_URL, tenantInfo.getDomain()));
        Assert.assertNotNull(response, "Authorization request failed. Authorized response is null.");
        EntityUtils.consume(response.getEntity());

        Header locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        Assert.assertNotNull(locationHeader, "Authorized response header is null.");
        return locationHeader;
    }

    private HttpResponse redirectToLoginPage(String locationUrl) throws IOException {

        HttpResponse response = sendGetRequest(client, locationUrl);
        Assert.assertNotNull(response, "Authorization request failed. Authorized user response is null.");
        return response;
    }

    private String extractKeyFromResponse(HttpResponse response, String keyToExtract, String appName) throws IOException {

        Map<String, Integer> keyPositionMap = new HashMap<>(1);
        keyPositionMap.put("name=\"" + keyToExtract + "\"", 1);
        List<DataExtractUtil.KeyValue> keyValues = DataExtractUtil.extractDataFromResponse(response, keyPositionMap);
        Assert.assertNotNull(keyValues, keyToExtract + "  key value is null for " + appName);

        String extractedValue = keyValues.get(0).getValue();
        Assert.assertNotNull(extractedValue, "Invalid " + keyToExtract + " for " + appName);
        return extractedValue;
    }

    private Header handleConsent() throws Exception {

        Map<String, Integer> keyPositionMap = new HashMap<>(1);
        keyPositionMap.put("name=\"sessionDataKeyConsent\"", 1);
        List<DataExtractUtil.KeyValue> keyValues =
                DataExtractUtil.extractSessionConsentDataFromResponse(response,
                        keyPositionMap);
        Assert.assertNotNull(keyValues, "SessionDataKeyConsent key value is null");
        EntityUtils.consume(response.getEntity());

        String sessionDataKeyConsent = keyValues.get(0).getValue();
        Assert.assertNotNull(sessionDataKeyConsent, "Invalid session key consent.");

        HttpResponse response = sendApprovalPostWithConsent(client, sessionDataKeyConsent, consentParameters);
        Assert.assertNotNull(response, "Approval request failed. response is invalid.");

        locationHeader =
                response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        Assert.assertNotNull(locationHeader, "Approval request failed. Location header is null.");
        EntityUtils.consume(response.getEntity());
        return locationHeader;
    }

    /**
     * Makes a token request with specified grant.
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

    private HttpResponse sendExistingSessionConsentGetRequest(CookieStore cookieStore,
                                                 List<NameValuePair> consentRequiredClaimsFromResponse)
            throws Exception {

        HttpClient httpClientWithoutAutoRedirections = HttpClientBuilder.create()
                .setDefaultCookieSpecRegistry(cookieSpecRegistry)
                .setDefaultRequestConfig(requestConfig)
                .disableRedirectHandling()
                .setDefaultCookieStore(cookieStore).build();
        consentRequiredClaimsFromResponse.addAll(Utils.getConsentRequiredClaimsFromResponse(response));
        Header locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        HttpResponse httpResponse = sendGetRequest(httpClientWithoutAutoRedirections, locationHeader.getValue());
        EntityUtils.consume(response.getEntity());
        return httpResponse;
    }
}

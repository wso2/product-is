/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
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

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Lookup;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.cookie.CookieSpecProvider;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.RFC6265CookieSpecProvider;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.AdvancedApplicationConfiguration;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationResponseModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.CIBAAuthenticationRequestConfiguration;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.InboundProtocols;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.OpenIDConnectConfiguration;
import org.wso2.identity.integration.test.util.Utils;
import org.wso2.identity.integration.test.utils.CommonConstants;
import org.wso2.identity.integration.test.utils.DataExtractUtil;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.wso2.identity.integration.test.utils.OAuth2Constant.COMMON_AUTH_URL;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.USER_AGENT;

/**
 * Integration test for CIBA (Client Initiated Backchannel Authentication) grant type.
 * Tests the happy path flow with external notification channel:
 * 1. Register application with CIBA grant type
 * 2. Send CIBA authentication request
 * 3. Complete browser-based user authentication via auth_url
 * 4. Poll token endpoint to obtain access token
 * 5. Validate the access token via introspection
 */
public class OAuth2CIBAGrantTestCase extends OAuth2ServiceAbstractIntegrationTest {

    private static final String CIBA_APP_NAME = "CIBATestApplication";
    private static final String BINDING_MESSAGE = "Please authenticate to CIBA Test App";
    private static final String SCOPE = "internal_login";

    private String applicationId;
    private String consumerKey;
    private String consumerSecret;
    private String authReqId;
    private String authUrl;
    private String sessionDataKey;
    private String sessionDataKeyConsent;
    private String accessToken;
    private long pollingInterval;

    private final CookieStore cookieStore = new BasicCookieStore();
    private Lookup<CookieSpecProvider> cookieSpecRegistry;
    private RequestConfig requestConfig;
    private CloseableHttpClient client;

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.init(TestUserMode.SUPER_TENANT_USER);

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
                .build();

        setSystemproperties();
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {

        deleteApp(applicationId);
        consumerKey = null;
        consumerSecret = null;
        applicationId = null;
        cookieStore.clear();
        client.close();
        restClient.closeHttpClient();
    }

    @Test(groups = "wso2.is", description = "Register application with CIBA grant type and external notification channel")
    public void testRegisterApplication() throws Exception {

        ApplicationResponseModel application = createCIBAApplication();
        Assert.assertNotNull(application, "CIBA application creation failed.");

        applicationId = application.getId();
        OpenIDConnectConfiguration oidcConfig = getOIDCInboundDetailsOfApplication(applicationId);

        consumerKey = oidcConfig.getClientId();
        Assert.assertNotNull(consumerKey, "Consumer key is null. Application creation failed.");

        consumerSecret = oidcConfig.getClientSecret();
        Assert.assertNotNull(consumerSecret, "Consumer secret is null. Application creation failed.");
    }

    @Test(groups = "wso2.is", description = "Send CIBA authentication request to backchannel endpoint",
            dependsOnMethods = "testRegisterApplication")
    public void testSendCIBARequest() throws Exception {

        JSONObject cibaResponse = sendCIBAAuthenticationRequest(consumerKey, consumerSecret,
                SCOPE, userInfo.getUserNameWithoutDomain(), BINDING_MESSAGE);

        authReqId = (String) cibaResponse.get(OAuth2Constant.CIBA_AUTH_REQ_ID);
        Assert.assertNotNull(authReqId, "auth_req_id is null in CIBA response.");

        authUrl = (String) cibaResponse.get(OAuth2Constant.CIBA_AUTH_URL);
        Assert.assertNotNull(authUrl,
                "auth_url is null in CIBA response. Expected for external notification channel.");
        Assert.assertTrue(authUrl.contains("ciba_authorize"),
                "auth_url does not contain expected 'ciba_authorize' path.");

        Assert.assertNotNull(cibaResponse.get(OAuth2Constant.CIBA_EXPIRES_IN),
                "expires_in is null in CIBA response.");

        Object interval = cibaResponse.get(OAuth2Constant.CIBA_INTERVAL);
        Assert.assertNotNull(interval, "interval is null in CIBA response.");
        pollingInterval = (long) interval;
    }

    @Test(groups = "wso2.is", description = "Access CIBA auth URL to get login page and extract sessionDataKey",
            dependsOnMethods = "testSendCIBARequest")
    public void testAccessAuthUrl() throws Exception {

        HttpResponse response = sendGetRequest(client, authUrl);
        Assert.assertNotNull(response, "Auth URL response is null.");

        Map<String, Integer> keyPositionMap = new HashMap<>(1);
        keyPositionMap.put("name=\"sessionDataKey\"", 1);
        List<DataExtractUtil.KeyValue> keyValues =
                DataExtractUtil.extractDataFromResponse(response, keyPositionMap);
        Assert.assertNotNull(keyValues, "sessionDataKey key value is null.");

        sessionDataKey = keyValues.get(0).getValue();
        Assert.assertNotNull(sessionDataKey, "Session data key is null.");
        EntityUtils.consume(response.getEntity());
    }

    @Test(groups = "wso2.is", description = "Send login credentials for CIBA authentication",
            dependsOnMethods = "testAccessAuthUrl")
    public void testSendLoginPost() throws Exception {

        HttpResponse response = sendLoginPost(client, sessionDataKey);
        Assert.assertNotNull(response, "Login request failed. Login response is null.");

        if (Utils.requestMissingClaims(response)) {
            Assert.assertTrue(response.getFirstHeader("Set-Cookie").getValue().contains("pastr"),
                    "pastr cookie not found in response.");
            String pastreCookie = response.getFirstHeader("Set-Cookie").getValue().split(";")[0];
            EntityUtils.consume(response.getEntity());

            response = Utils.sendPOSTConsentMessage(response, COMMON_AUTH_URL, USER_AGENT,
                    Utils.getRedirectUrl(response), client, pastreCookie);
            EntityUtils.consume(response.getEntity());
        }

        Header locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        Assert.assertNotNull(locationHeader, "Login response header is null.");
        EntityUtils.consume(response.getEntity());

        response = sendGetRequest(client, locationHeader.getValue());
        Map<String, Integer> keyPositionMap = new HashMap<>(1);
        keyPositionMap.put("name=\"" + OAuth2Constant.SESSION_DATA_KEY_CONSENT + "\"", 1);
        List<DataExtractUtil.KeyValue> keyValues =
                DataExtractUtil.extractSessionConsentDataFromResponse(response, keyPositionMap);

        if (keyValues != null && !keyValues.isEmpty()) {
            sessionDataKeyConsent = keyValues.get(0).getValue();
        }
        EntityUtils.consume(response.getEntity());
    }

    @Test(groups = "wso2.is", description = "Send consent approval post request",
            dependsOnMethods = "testSendLoginPost")
    public void testSendApprovalPost() throws Exception {

        if (sessionDataKeyConsent != null) {
            HttpResponse response = sendApprovalPost(client, sessionDataKeyConsent);
            Assert.assertNotNull(response, "Approval response is invalid.");

            Header locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
            Assert.assertNotNull(locationHeader, "Approval Location header is null.");
            EntityUtils.consume(response.getEntity());

            response = sendPostRequest(client, locationHeader.getValue());
            Assert.assertNotNull(response, "Post-approval redirect response is invalid.");
            EntityUtils.consume(response.getEntity());
        }
    }

    @Test(groups = "wso2.is", description = "Poll token endpoint with auth_req_id to obtain access token",
            dependsOnMethods = "testSendApprovalPost")
    public void testPollTokenEndpoint() throws Exception {

        // Wait for the polling interval before requesting the token.
        Thread.sleep(pollingInterval * 1000);

        JSONObject tokenResponse = sendCIBATokenRequest(consumerKey, consumerSecret, authReqId);

        Assert.assertTrue(tokenResponse.containsKey(OAuth2Constant.ACCESS_TOKEN),
                "Access token not found in the CIBA token response.");
        accessToken = (String) tokenResponse.get(OAuth2Constant.ACCESS_TOKEN);
        Assert.assertNotNull(accessToken, "Access token is null.");
    }

    @Test(groups = "wso2.is", description = "Validate CIBA access token via introspection",
            dependsOnMethods = "testPollTokenEndpoint")
    public void testValidateAccessToken() throws Exception {

        JSONObject responseObj = introspectTokenWithTenant(client, accessToken,
                OAuth2Constant.INTRO_SPEC_ENDPOINT, userInfo.getUserName(), userInfo.getPassword());
        Assert.assertNotNull(responseObj, "Introspection response is null.");
        Assert.assertEquals(responseObj.get("active"), true, "Token validation failed. Token is not active.");
    }

    @Test(groups = "wso2.is",
            description = "Poll token endpoint before user authentication - expect authorization_pending",
            dependsOnMethods = "testValidateAccessToken")
    public void testTokenPollingBeforeUserAuth() throws Exception {

        // Reset cookie store for a fresh session.
        cookieStore.clear();

        // Send a new CIBA request to get a fresh auth_req_id.
        JSONObject cibaResponse = sendCIBAAuthenticationRequest(consumerKey, consumerSecret,
                SCOPE, userInfo.getUserNameWithoutDomain(), BINDING_MESSAGE);
        String newAuthReqId = (String) cibaResponse.get(OAuth2Constant.CIBA_AUTH_REQ_ID);
        Assert.assertNotNull(newAuthReqId, "auth_req_id is null for pre-auth polling test.");

        // Wait for the polling interval.
        Object interval = cibaResponse.get(OAuth2Constant.CIBA_INTERVAL);
        if (interval != null) {
            Thread.sleep((long) interval * 1000);
        }

        // Poll token endpoint WITHOUT completing user authentication.
        JSONObject tokenResponse = sendCIBATokenRequest(consumerKey, consumerSecret, newAuthReqId);

        Assert.assertTrue(tokenResponse.containsKey("error"),
                "Expected error response when polling before user authentication.");
        Assert.assertEquals(tokenResponse.get("error"), "authorization_pending",
                "Expected 'authorization_pending' error when user has not authenticated.");
    }

    @Test(groups = "wso2.is", description = "Send CIBA request with invalid client credentials",
            dependsOnMethods = "testTokenPollingBeforeUserAuth")
    public void testCIBARequestWithInvalidClientCredentials() throws Exception {

        JSONObject cibaResponse = sendCIBAAuthenticationRequest("invalidConsumerKey", "invalidSecret",
                SCOPE, userInfo.getUserNameWithoutDomain(), BINDING_MESSAGE);

        Assert.assertTrue(cibaResponse.containsKey("error"),
                "Expected error response for invalid client credentials.");
        Assert.assertEquals(cibaResponse.get("error"), "invalid_client",
                "Expected 'invalid_client' error for invalid credentials.");
    }

    @Test(groups = "wso2.is", description = "Send CIBA request without login_hint",
            dependsOnMethods = "testCIBARequestWithInvalidClientCredentials")
    public void testCIBARequestWithMissingLoginHint() throws Exception {

        JSONObject cibaResponse = sendCIBAAuthenticationRequest(consumerKey, consumerSecret,
                SCOPE, null, BINDING_MESSAGE);

        Assert.assertTrue(cibaResponse.containsKey("error"),
                "Expected error response when login_hint is missing.");
        Assert.assertEquals(cibaResponse.get("error"), "invalid_request",
                "Expected 'invalid_request' error when login_hint is missing.");
    }

    @Test(groups = "wso2.is", description = "Poll token endpoint with invalid auth_req_id",
            dependsOnMethods = "testCIBARequestWithMissingLoginHint")
    public void testTokenPollingWithInvalidAuthReqId() throws Exception {

        JSONObject tokenResponse = sendCIBATokenRequest(consumerKey, consumerSecret,
                "invalid-auth-req-id-00000000");

        Assert.assertTrue(tokenResponse.containsKey("error"),
                "Expected error response for invalid auth_req_id.");
    }

    /**
     * Create an OAuth2 application configured with CIBA grant type and external notification channel.
     *
     * @return ApplicationResponseModel of the created application.
     * @throws Exception If an error occurred while creating the application.
     */
    private ApplicationResponseModel createCIBAApplication() throws Exception {

        ApplicationModel application = new ApplicationModel();
        application.setName(CIBA_APP_NAME);

        List<String> grantTypes = new ArrayList<>();
        Collections.addAll(grantTypes, "authorization_code", OAuth2Constant.OAUTH2_GRANT_TYPE_CIBA);

        List<String> callBackUrls = new ArrayList<>();
        Collections.addAll(callBackUrls, OAuth2Constant.CALLBACK_URL);

        OpenIDConnectConfiguration oidcConfig = new OpenIDConnectConfiguration();
        oidcConfig.setGrantTypes(grantTypes);
        oidcConfig.setCallbackURLs(callBackUrls);

        // Configure CIBA with external notification channel.
        CIBAAuthenticationRequestConfiguration cibaConfig = new CIBAAuthenticationRequestConfiguration();
        cibaConfig.setAuthReqExpiryTime(100L);
        cibaConfig.setNotificationChannels(Collections.singletonList(
                OAuth2Constant.CIBA_NOTIFICATION_CHANNEL_EXTERNAL));
        oidcConfig.setCibaAuthenticationRequest(cibaConfig);

        InboundProtocols inboundProtocolsConfig = new InboundProtocols();
        inboundProtocolsConfig.setOidc(oidcConfig);
        application.setInboundProtocolConfiguration(inboundProtocolsConfig);

        application.advancedConfigurations(new AdvancedApplicationConfiguration()
                .skipLoginConsent(true)
                .skipLogoutConsent(true));

        String appId = addApplication(application);
        return getApplication(appId);
    }

    /**
     * Send a CIBA authentication request to the backchannel endpoint.
     *
     * @param clientId       Client ID for Basic auth.
     * @param clientSecret   Client secret for Basic auth.
     * @param scope          Requested scopes.
     * @param loginHint      Username of the user to authenticate. Can be null to test missing login_hint.
     * @param bindingMessage Binding message for the authentication request.
     * @return JSONObject of the CIBA response.
     * @throws Exception If an error occurred while sending the request.
     */
    private JSONObject sendCIBAAuthenticationRequest(String clientId, String clientSecret,
                                                     String scope, String loginHint,
                                                     String bindingMessage) throws Exception {

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_SCOPE, scope));
        if (loginHint != null) {
            urlParameters.add(new BasicNameValuePair(OAuth2Constant.CIBA_LOGIN_HINT, loginHint));
        }
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.CIBA_BINDING_MESSAGE, bindingMessage));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.CIBA_NOTIFICATION_CHANNEL,
                OAuth2Constant.CIBA_NOTIFICATION_CHANNEL_EXTERNAL));

        HttpPost httpPost = new HttpPost(
                getTenantQualifiedURL(OAuth2Constant.CIBA_ENDPOINT, tenantInfo.getDomain()));
        httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
        httpPost.setHeader(OAuth2Constant.AUTHORIZATION_HEADER,
                OAuth2Constant.BASIC_HEADER + " " + getBase64EncodedString(clientId, clientSecret));
        httpPost.setEntity(new UrlEncodedFormEntity(urlParameters));

        HttpResponse response = client.execute(httpPost);
        String responseString = EntityUtils.toString(response.getEntity(), "UTF-8");
        EntityUtils.consume(response.getEntity());

        JSONParser parser = new JSONParser();
        JSONObject json = (JSONObject) parser.parse(responseString);
        Assert.assertNotNull(json, "CIBA response is null.");
        return json;
    }

    /**
     * Send a token request with the CIBA grant type.
     *
     * @param clientId     Client ID for Basic auth.
     * @param clientSecret Client secret for Basic auth.
     * @param authReqId    The auth_req_id from the CIBA authentication response.
     * @return JSONObject of the token response.
     * @throws IOException If an error occurred while sending the request.
     */
    private JSONObject sendCIBATokenRequest(String clientId, String clientSecret,
                                            String authReqId) throws IOException {

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.GRANT_TYPE_NAME,
                OAuth2Constant.OAUTH2_GRANT_TYPE_CIBA));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.CIBA_AUTH_REQ_ID, authReqId));

        HttpPost httpPost = new HttpPost(
                getTenantQualifiedURL(OAuth2Constant.ACCESS_TOKEN_ENDPOINT, tenantInfo.getDomain()));
        httpPost.setHeader(CommonConstants.USER_AGENT_HEADER, OAuth2Constant.USER_AGENT);
        httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
        httpPost.setHeader(OAuth2Constant.AUTHORIZATION_HEADER,
                OAuth2Constant.BASIC_HEADER + " " + getBase64EncodedString(clientId, clientSecret));
        httpPost.setEntity(new UrlEncodedFormEntity(urlParameters));

        HttpResponse response = client.execute(httpPost);
        try (BufferedReader responseBuffer =
                     new BufferedReader(new InputStreamReader(response.getEntity().getContent()))) {
            return (JSONObject) org.json.simple.JSONValue.parse(responseBuffer);
        }
    }
}

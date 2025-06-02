/*
 * Copyright (c) 2020, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.identity.integration.test.oauth2;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Lookup;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.cookie.CookieSpecProvider;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.RFC6265CookieSpecProvider;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationResponseModel;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.wso2.identity.integration.test.utils.OAuth2Constant.COMMON_AUTH_URL;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.SCOPE_PLAYGROUND_NAME;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.USER_AGENT;

public class OAuth2DeviceFlowTestCase extends OAuth2ServiceAbstractIntegrationTest {

    private static final String DEVICE_CODE = "device_code";
    private static final String USER_CODE = "user_code";
    private static final String ERROR = "error";
    private static final String ERROR_DESCRIPTION = "error_description";
    private static final String INTERVAL = "interval";
    private static final String EXPIRES_IN = "expires_in";
    private static final String VERIFICATION_URI = "verification_uri";
    private static final String VERIFICATION_URI_COMPLETE = "verification_uri_complete";
    private static final String CLIENT_ID_PARAM = "client_id";
    private static final String GRANT_TYPE = "urn:ietf:params:oauth:grant-type:device_code";
    private String sessionDataKeyConsent;
    private String sessionDataKey;
    private String consumerKey;
    private String consumerSecret;
    private String appId;
    private String userCode;
    private String deviceCode;
    private final CookieStore cookieStore = new BasicCookieStore();
    private Lookup<CookieSpecProvider> cookieSpecRegistry;
    private RequestConfig requestConfig;
    private CloseableHttpClient client;
    private final TestUserMode userMode;
    private final AutomationContext context;
    private final String activeTenant;
    private String deviceAuthEndpoint;
    private String deviceAuthPageEndpoint;
    private String deviceEndpoint;
    private String tokenEndpoint;


    @DataProvider(name = "configProvider")
    public static Object[][] configProvider() {

        return new Object[][]{
                {TestUserMode.SUPER_TENANT_USER, TestUserMode.SUPER_TENANT_ADMIN},
                {TestUserMode.TENANT_USER, TestUserMode.TENANT_ADMIN}
        };
    }

    @Factory(dataProvider = "configProvider")
    public OAuth2DeviceFlowTestCase(TestUserMode userMode, TestUserMode adminMode) throws Exception {

        context = new AutomationContext("IDENTITY", adminMode);
        this.userMode = userMode;
        this.activeTenant = context.getContextTenant().getDomain();
    }

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.init(userMode);

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
        setServerEndpoints();
    }

    private void setServerEndpoints() throws Exception {

        String deviceAuthEndpointBaseUrl = context.getContextUrls().getBackEndUrl()
                .replace("services/", "oauth2/device_authorize");
        deviceAuthEndpoint = getTenantQualifiedURL(deviceAuthEndpointBaseUrl, activeTenant);

        String deviceAuthPageBaseUrl = context.getContextUrls().getBackEndUrl()
                .replace("services/", "authenticationendpoint/device.do");
        deviceAuthPageEndpoint = getTenantQualifiedURL(deviceAuthPageBaseUrl, activeTenant);

        String deviceEndpointBaseUrl = context.getContextUrls().getBackEndUrl()
                .replace("services/", "oauth2/device");
        deviceEndpoint = getTenantQualifiedURL(deviceEndpointBaseUrl, activeTenant);

        String tokenEndpointBaseUrl = context.getContextUrls().getBackEndUrl()
                .replace("services/", "oauth2/token");
        tokenEndpoint = getTenantQualifiedURL(tokenEndpointBaseUrl, activeTenant);
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {

        deleteApp(appId);
        consumerKey = null;
        consumerSecret = null;
        appId = null;
        cookieStore.clear();
        client.close();
        restClient.closeHttpClient();
    }

    @Test(groups = "wso2.is", description = "Check Oauth2 application registration")
    public void testRegisterApplication() throws Exception {

        ApplicationResponseModel application = createApp();
        Assert.assertNotNull(application, "OAuth App creation failed.");

        OpenIDConnectConfiguration oidcConfig = getOIDCInboundDetailsOfApplication(application.getId());

        consumerKey = oidcConfig.getClientId();
        Assert.assertNotNull(consumerKey, "Application creation failed.");

        consumerSecret = oidcConfig.getClientSecret();
        Assert.assertNotNull(consumerSecret, "Application creation failed.");
        appId = application.getId();
    }

    @Test(groups = "wso2.is", description = "Send authorize user request without redirect_uri param", dependsOnMethods
            = "testRegisterApplication")
    public void testSendDeviceAuthorize() throws Exception {

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair(CLIENT_ID_PARAM, consumerKey));
        urlParameters.add(new BasicNameValuePair(SCOPE_PLAYGROUND_NAME, "device_01"));
        JSONObject responseObject = responseObjectNew(urlParameters, deviceAuthEndpoint);
        deviceCode = responseObject.get(DEVICE_CODE).toString();
        userCode = responseObject.get(USER_CODE).toString();
        Assert.assertNotNull(deviceCode, "device_code is null");
        Assert.assertNotNull(userCode, "user_code is null");
        Assert.assertEquals(responseObject.get(INTERVAL).toString(), "5",
                "interval period is incorrect.");
        Assert.assertEquals(responseObject.get(EXPIRES_IN).toString(), "600",
                "interval period is incorrect.");
        Assert.assertEquals(responseObject.get(VERIFICATION_URI).toString(), deviceAuthPageEndpoint,
                "verification uri is incorrect.");
        Assert.assertEquals(responseObject.get(VERIFICATION_URI_COMPLETE).toString(),
                deviceAuthPageEndpoint + "?user_code=" + userCode ,
                "complete verification uri is incorrect.");
    }

    @Test(groups = "wso2.is", description = "Send unapproved token", dependsOnMethods = "testSendDeviceAuthorize")
    public void testNonUsedDeviceTokenRequest() throws Exception {

        // Wait 5 seconds because of the token polling interval.
        Thread.sleep(5000);
        JSONObject obj = sendTokenRequest(GRANT_TYPE, consumerKey, deviceCode);
        String error = obj.get("error").toString();
        String errorDescription = obj.get("error_description").toString();
        Assert.assertNotNull(error, "error is null");
        Assert.assertEquals(error,"authorization_pending", "Error message is not valid.");
        Assert.assertEquals(errorDescription,"Precondition required",
                "Error Description is not valid.");
    }

    @Test(groups = "wso2.is", description = "Send unapproved token",
            dependsOnMethods = "testNonUsedDeviceTokenRequest")
    public void testSlowDownDeviceTokenRequest() throws Exception {

        JSONObject obj = sendTokenRequest(GRANT_TYPE, consumerKey, deviceCode);
        String error = obj.get("error").toString();
        String errorDescription = obj.get("error_description").toString();
        Assert.assertNotNull(error, "error is null");
        Assert.assertEquals(error,"slow_down", "Error message is not valid.");
        Assert.assertEquals(errorDescription,"Forbidden", "Error Description is not valid.");
    }

    @Test(groups = "wso2.is", description = "Send authorize user request",
            dependsOnMethods = "testSlowDownDeviceTokenRequest")
    public void testSendDeviceAuthorozedPost() throws Exception {

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair(USER_CODE, userCode));
        String response = responsePost(urlParameters, deviceAuthPageEndpoint);
        Assert.assertNotNull(response, "Authorized response is null");
    }
    
    @Test(groups = "wso2.is", description = "Send authorize user request",
            dependsOnMethods = "testSendDeviceAuthorozedPost")
    public void testDevicePost() throws Exception {

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair(USER_CODE, userCode));
        HttpResponse response = sendPostRequestWithParameters(client, urlParameters, deviceEndpoint);
        Header locationHeader =
                response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        Assert.assertNotNull(response);
        Assert.assertNotNull(locationHeader, "Authorized response header is null");
        EntityUtils.consume(response.getEntity());

        response = sendGetRequest(client, locationHeader.getValue());
        Assert.assertNotNull(response, "Authorized user response is null.");

        Map<String, Integer> keyPositionMap = new HashMap<>(1);
        keyPositionMap.put("name=\"sessionDataKey\"", 1);
        List<DataExtractUtil.KeyValue> keyValues =
                DataExtractUtil.extractDataFromResponse(response, keyPositionMap);
        Assert.assertNotNull(keyValues, "sessionDataKey key value is null");

        sessionDataKey = keyValues.get(0).getValue();
        Assert.assertNotNull(sessionDataKey, "Session data key is null.");
        EntityUtils.consume(response.getEntity());
    }

    @Test(groups = "wso2.is", description = "Send login post request", dependsOnMethods = "testDevicePost")
    public void testSendLoginPost() throws Exception {

        HttpResponse response = sendLoginPost(client, sessionDataKey);
        Assert.assertNotNull(response, "Login request failed. Login response is null.");

        if (Utils.requestMissingClaims(response)) {
            Assert.assertTrue(response.getFirstHeader("Set-Cookie").getValue().contains("pastr"),
                    "pastr cookie not found in response.");
            String pastreCookie = response.getFirstHeader("Set-Cookie").getValue().split(";")[0];
            EntityUtils.consume(response.getEntity());

            response = Utils.sendPOSTConsentMessage(response, COMMON_AUTH_URL, USER_AGENT, Utils.getRedirectUrl
                    (response), client, pastreCookie);
            EntityUtils.consume(response.getEntity());
        }
        Header locationHeader =
                response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        Assert.assertNotNull(locationHeader, "Login response header is null");
        EntityUtils.consume(response.getEntity());

        response = sendGetRequest(client, locationHeader.getValue());
        Map<String, Integer> keyPositionMap = new HashMap<>(1);
        keyPositionMap.put("name=\"" + OAuth2Constant.SESSION_DATA_KEY_CONSENT + "\"", 1);
        List<DataExtractUtil.KeyValue> keyValues =
                DataExtractUtil.extractSessionConsentDataFromResponse(response,
                        keyPositionMap);
        Assert.assertNotNull(keyValues, "SessionDataKeyConsent key value is null");
        sessionDataKeyConsent = keyValues.get(0).getValue();
        EntityUtils.consume(response.getEntity());

        Assert.assertNotNull(sessionDataKeyConsent, "Invalid session key consent.");
    }

    @Test(groups = "wso2.is", description = "Send approval post request", dependsOnMethods = "testSendLoginPost")
    public void testSendApprovalPost() throws Exception {

        HttpResponse response = sendApprovalPost(client, sessionDataKeyConsent);
        Assert.assertNotNull(response, "Approval response is invalid.");

        Header locationHeader =
                response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        Assert.assertNotNull(locationHeader, "Approval Location header is null.");

        EntityUtils.consume(response.getEntity());

        response = sendPostRequest(client, locationHeader.getValue());
        Assert.assertNotNull(response, "Get Activation response is invalid.");
        EntityUtils.consume(response.getEntity());
    }

    @Test(groups = "wso2.is", description = "Send token post request", dependsOnMethods = "testSendApprovalPost")
    public void testTokenRequest() throws Exception {

        // Wait 5 seconds because of the token polling interval.
        Thread.sleep(5000);
        JSONObject obj = sendTokenRequest(GRANT_TYPE, consumerKey, deviceCode);
        String accessToken = obj.get("access_token").toString();
        Assert.assertNotNull(accessToken, "Assess token is null");
    }

    @Test(groups = "wso2.is", description = "Send token post request with used code",
            dependsOnMethods = "testTokenRequest")
    public void testExpiredDeviceTokenRequest() throws Exception {

        // Wait 5 seconds because of the token polling interval.
        Thread.sleep(5000);
        JSONObject obj = sendTokenRequest(GRANT_TYPE, consumerKey, deviceCode);
        String error = obj.get("error").toString();
        Assert.assertEquals(error, "expired_token");
    }

    @Test(groups = "wso2.is", description = "Send authorize user request without client id ", dependsOnMethods
            = "testExpiredDeviceTokenRequest")
    public void testSendDeviceAuthorizeWithoutClientId() throws Exception {

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair(SCOPE_PLAYGROUND_NAME, "device_01"));
        JSONObject responseObject = responseObjectNew(urlParameters, deviceAuthEndpoint);
        String error = responseObject.get(ERROR).toString();
        String errorDescription = responseObject.get(ERROR_DESCRIPTION).toString();
        Assert.assertEquals(error, "invalid_request", "invalid error retrieved");
        Assert.assertEquals(errorDescription, "Client ID not found in the request.",
                "invalid error description received");
    }

    @Test(groups = "wso2.is", description = "Send authorize user request with invalid client  ", dependsOnMethods
            = "testSendDeviceAuthorizeWithoutClientId")
    public void testSendDeviceAuthorizeWithInvalidClientId() throws Exception {

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair(CLIENT_ID_PARAM, "invalidConsumerKey"));
        urlParameters.add(new BasicNameValuePair(SCOPE_PLAYGROUND_NAME, "device_01"));
        JSONObject responseObject = responseObjectNew(urlParameters, deviceAuthEndpoint);
        String error = responseObject.get(ERROR).toString();
        String errorDescription = responseObject.get(ERROR_DESCRIPTION).toString();
        Assert.assertEquals(error, "invalid_request", "invalid error retrieved");
        Assert.assertEquals(errorDescription, "Client credentials are invalid.",
                "invalid error description received");
    }

    @Test(groups = "wso2.is", description = "Send device user request with invalid user code  ", dependsOnMethods
            = "testSendDeviceAuthorizeWithInvalidClientId")
    public void testSendUserLoginWithInvalidUserCode() throws Exception {

        refreshHTTPClient();
        testSendDeviceAuthorize();
        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair(USER_CODE, "invalidUserCode"));
        HttpResponse response = sendPostRequestWithParameters(client, urlParameters, deviceEndpoint);
        Header locationHeader =
                response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        Assert.assertNotNull(response);
        Assert.assertNotNull(locationHeader, "Authorized response header is null");
        Assert.assertTrue(locationHeader.toString().contains("error=invalid.code"),
                "Authorized error response header doesn't contain valid error");
        EntityUtils.consume(response.getEntity());
    }

    @Test(groups = "wso2.is", description = "Send token request without grant type",
            dependsOnMethods = "testSendUserLoginWithInvalidUserCode")
    public void testTokenRequestWithoutGrantType() throws IOException {

        JSONObject obj = sendTokenRequest(null, consumerKey, deviceCode);
        String error = obj.get("error").toString();
        String errorDescription = obj.get("error_description").toString();
        Assert.assertEquals(error, "invalid_request", "Error message is not valid.");
        Assert.assertEquals(errorDescription, "Missing grant_type parameter value", "Error Description is not valid.");
    }

    @Test(groups = "wso2.is", description = "Send token request with invalid grant type",
            dependsOnMethods = "testTokenRequestWithoutGrantType")
    public void testTokenRequestWithInvalidGrantType() throws IOException {

        JSONObject obj = sendTokenRequest("invalidGrantType", consumerKey, deviceCode);
        String error = obj.get("error").toString();
        String errorDescription = obj.get("error_description").toString();
        Assert.assertEquals(error, "invalid_request", "Error message is not valid.");
        Assert.assertEquals(errorDescription, "Unsupported grant_type value",
                "Error Description is not valid.");
    }

    @Test(groups = "wso2.is", description = "Send token request without client id",
            dependsOnMethods = "testTokenRequestWithInvalidGrantType")
    public void testTokenRequestWithoutClientId() throws IOException {

        JSONObject obj = sendTokenRequest(GRANT_TYPE, null, deviceCode);
        String error = obj.get("error").toString();
        String errorDescription = obj.get("error_description").toString();
        Assert.assertEquals(error, "invalid_client", "Error message is not valid.");
        Assert.assertEquals(errorDescription, "Client ID not found in the request.",
                "Error Description is not valid.");
    }

    @Test(groups = "wso2.is", description = "Send token request with invalid client id",
            dependsOnMethods = "testTokenRequestWithoutClientId")
    public void testTokenRequestWithInvalidClientId() throws IOException {

        JSONObject obj = sendTokenRequest(GRANT_TYPE, "invalidConsumerKey", deviceCode);
        String error = obj.get("error").toString();
        String errorDescription = obj.get("error_description").toString();
        Assert.assertEquals(error, "invalid_client", "Error message is not valid.");
        Assert.assertEquals(errorDescription,
                "Client credentials are invalid.",
                "Error Description is not valid.");
    }

    @Test(groups = "wso2.is", description = "Send token request without device code",
            dependsOnMethods = "testTokenRequestWithInvalidClientId")
    public void testTokenRequestWithoutDeviceCode() throws IOException {

        JSONObject obj = sendTokenRequest(GRANT_TYPE
                , consumerKey, null);
        String error = obj.get("error").toString();
        String errorDescription = obj.get("error_description").toString();
        Assert.assertEquals(error, "invalid_request", "Error message is not valid.");
        Assert.assertEquals(errorDescription, "Missing parameters: device_code",
                "Error Description is not valid.");
    }

    @Test(groups = "wso2.is", description = "Send token request with invalid device code",
            dependsOnMethods = "testTokenRequestWithoutDeviceCode")
    public void testTokenRequestWithInvalidDeviceCode() throws IOException {

        JSONObject obj = sendTokenRequest(GRANT_TYPE, consumerKey, "invalidDeviceCode");
        String error = obj.get("error").toString();
        String errorDescription = obj.get("error_description").toString();
        Assert.assertEquals(error, "invalid_request", "Error message is not valid.");
        Assert.assertEquals(errorDescription, "The provided device code is not registered or is invalid.",
                "Error Description is not valid.");
    }

    private JSONObject sendTokenRequest(String grantType, String clientId, String deviceCode) throws IOException {

        List<NameValuePair> urlParameters = new ArrayList<>();
        if (grantType != null) {
            urlParameters.add(new BasicNameValuePair(OAuth2Constant.GRANT_TYPE_NAME, grantType));
        }
        if (clientId != null) {
            urlParameters.add(new BasicNameValuePair(CLIENT_ID_PARAM, clientId));
        }
        if (deviceCode != null) {
            urlParameters.add(new BasicNameValuePair(DEVICE_CODE, deviceCode));
        }

        HttpPost request = new HttpPost(tokenEndpoint);
        request.setHeader(CommonConstants.USER_AGENT_HEADER, OAuth2Constant.USER_AGENT);
        request.setEntity(new UrlEncodedFormEntity(urlParameters));
        HttpResponse response = client.execute(request);

        BufferedReader responseBuffer = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        return (JSONObject) JSONValue.parse(responseBuffer);
    }

    private JSONObject responseObjectNew(List<NameValuePair> postParameters, String uri) throws Exception {

        HttpPost httpPost = new HttpPost(uri);
        //generate post request
        httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
        httpPost.setEntity(new UrlEncodedFormEntity(postParameters));
        HttpResponse response = client.execute(httpPost);
        String responseString = EntityUtils.toString(response.getEntity(), "UTF-8");
        EntityUtils.consume(response.getEntity());

        JSONParser parser = new JSONParser();
        JSONObject json = (JSONObject) parser.parse(responseString);
        if (json == null) {
            throw new Exception(
                    "Error occurred while getting the response");
        }

        return json;
    }

    private String responsePost(List<NameValuePair> postParameters, String uri) throws Exception {

        HttpPost httpPost = new HttpPost(uri);
        //generate post request
        httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
        httpPost.setEntity(new UrlEncodedFormEntity(postParameters));
        HttpResponse response = client.execute(httpPost);
        String responseString = EntityUtils.toString(response.getEntity(), "UTF-8");
        EntityUtils.consume(response.getEntity());

        return responseString;
    }

    public HttpResponse sendGetRequest(HttpClient client, String locationURL) throws IOException {

        HttpGet getRequest = new HttpGet(locationURL);
        getRequest.setHeader("User-Agent", OAuth2Constant.USER_AGENT);

        return client.execute(getRequest);
    }

    /**
     * Create Application with the given app configurations.
     *
     * @return ApplicationResponseModel.
     * @throws Exception If an error occurred while creating the application.
     */
    private ApplicationResponseModel createApp() throws Exception {

        ApplicationModel application = new ApplicationModel();

        List<String> grantTypes = new ArrayList<>();
        Collections.addAll(grantTypes, "authorization_code", "implicit", "password", "client_credentials",
                "refresh_token", "urn:ietf:params:oauth:grant-type:saml2-bearer", "iwa:ntlm",
                "urn:ietf:params:oauth:grant-type:device_code");

        List<String> callBackUrls = new ArrayList<>();
        Collections.addAll(callBackUrls, OAuth2Constant.CALLBACK_URL);

        OpenIDConnectConfiguration oidcConfig = new OpenIDConnectConfiguration();
        oidcConfig.setGrantTypes(grantTypes);
        oidcConfig.setCallbackURLs(callBackUrls);
        oidcConfig.setPublicClient(true);

        InboundProtocols inboundProtocolsConfig = new InboundProtocols();
        inboundProtocolsConfig.setOidc(oidcConfig);

        application.setInboundProtocolConfiguration(inboundProtocolsConfig);
        application.setName(OAuth2Constant.OAUTH_APPLICATION_NAME);

        String appId = addApplication(application);

        return getApplication(appId);
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
}

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

package org.wso2.identity.integration.test.serviceextensions.preissueaccesstoken;

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
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.identity.integration.test.serviceextensions.common.ActionsBaseTestCase;
import org.wso2.identity.integration.test.serviceextensions.dataprovider.model.ActionResponse;
import org.wso2.identity.integration.test.serviceextensions.dataprovider.model.ExpectedTokenResponse;
import org.wso2.identity.integration.test.serviceextensions.mockservices.ServiceExtensionMockServer;
import org.wso2.identity.integration.test.rest.api.server.action.management.v1.common.model.ActionModel;
import org.wso2.identity.integration.test.rest.api.server.action.management.v1.common.model.AuthenticationType;
import org.wso2.identity.integration.test.rest.api.server.action.management.v1.common.model.Endpoint;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.AccessTokenConfiguration;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.AdvancedApplicationConfiguration;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationResponseModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.InboundProtocols;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.OpenIDConnectConfiguration;
import org.wso2.identity.integration.test.rest.api.user.common.model.Email;
import org.wso2.identity.integration.test.rest.api.user.common.model.Name;
import org.wso2.identity.integration.test.rest.api.user.common.model.UserObject;
import org.wso2.identity.integration.test.restclients.SCIM2RestClient;
import org.wso2.identity.integration.test.util.Utils;
import org.wso2.identity.integration.test.utils.CommonConstants;
import org.wso2.identity.integration.test.utils.DataExtractUtil;
import org.wso2.identity.integration.test.utils.FileUtils;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.ACCESS_TOKEN_ENDPOINT;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.AUTHORIZATION_HEADER;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.COMMON_AUTH_URL;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.USER_AGENT;

/**
 * Tests the pre-issue access token action failure scenarios with device code grant type.
 */
public class PreIssueAccessTokenActionFailureDeviceCodeGrantTestCase extends ActionsBaseTestCase {

    private static final String TEST_USER = "test_user";
    private static final String TEST_WSO2 = "Test@wso2";
    private static final String PRE_ISSUE_ACCESS_TOKEN_API_PATH = "preIssueAccessToken";
    private static final String MOCK_SERVER_ENDPOINT_RESOURCE_PATH = "/test/action";
    private static final String DEVICE_CODE_GRANT_TYPE = "urn:ietf:params:oauth:grant-type:device_code";
    private static final String CLIENT_ID_PARAM = "client_id";
    private static final String DEVICE_CODE_PARAM = "device_code";
    private static final String USER_CODE_PARAM = "user_code";

    private final CookieStore cookieStore = new BasicCookieStore();
    private CloseableHttpClient client;
    private SCIM2RestClient scim2RestClient;
    private List<String> requestedScopes;
    private String clientId;
    private String clientSecret;
    private String actionId;
    private String applicationId;
    private String userId;
    private String deviceCode;
    private String userCode;
    private String sessionDataKey;
    private final TestUserMode userMode;
    private ServiceExtensionMockServer serviceExtensionMockServer;
    private final ActionResponse actionResponse;
    private final ExpectedTokenResponse expectedTokenResponse;

    private String deviceAuthEndpoint;
    private String deviceAuthPageEndpoint;
    private String deviceEndpoint;
    private String tokenEndpoint;

    @Factory(dataProvider = "testExecutionContextProvider")
    public PreIssueAccessTokenActionFailureDeviceCodeGrantTestCase(TestUserMode testUserMode,
                                                                   ActionResponse actionResponse,
                                                                   ExpectedTokenResponse expectedTokenResponse) {

        this.userMode = testUserMode;
        this.actionResponse = actionResponse;
        this.expectedTokenResponse = expectedTokenResponse;
    }

    @DataProvider(name = "testExecutionContextProvider")
    public static Object[][] getTestExecutionContext() throws Exception {

        return new Object[][]{
                {TestUserMode.SUPER_TENANT_USER, new ActionResponse(200,
                        FileUtils.readFileInClassPathAsString("actions/response/incomplete-response.json")),
                        new ExpectedTokenResponse(500, "server_error", "Internal Server Error.")},
                {TestUserMode.SUPER_TENANT_USER, new ActionResponse(200,
                        FileUtils.readFileInClassPathAsString("actions/response/failure-response.json")),
                        new ExpectedTokenResponse(400, "Some failure reason", "Some description")},
                {TestUserMode.TENANT_USER, new ActionResponse(200,
                        FileUtils.readFileInClassPathAsString("actions/response/failure-response.json")),
                        new ExpectedTokenResponse(400, "Some failure reason", "Some description")},
                {TestUserMode.TENANT_USER, new ActionResponse(500,
                        FileUtils.readFileInClassPathAsString("actions/response/error-response.json")),
                        new ExpectedTokenResponse(500, "server_error", "Internal Server Error.")},
                {TestUserMode.TENANT_USER, new ActionResponse(401, "Unauthorized"),
                        new ExpectedTokenResponse(500, "server_error", "Internal Server Error.")},
        };
    }

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.init(userMode);

        Lookup<CookieSpecProvider> cookieSpecRegistry = RegistryBuilder.<CookieSpecProvider>create()
                .register(CookieSpecs.DEFAULT, new RFC6265CookieSpecProvider())
                .build();
        RequestConfig requestConfig = RequestConfig.custom()
                .setCookieSpec(CookieSpecs.DEFAULT)
                .build();
        client = HttpClientBuilder.create()
                .setDefaultCookieStore(cookieStore)
                .setDefaultCookieSpecRegistry(cookieSpecRegistry)
                .setDefaultRequestConfig(requestConfig)
                .build();

        scim2RestClient = new SCIM2RestClient(serverURL, tenantInfo);

        ApplicationResponseModel application = createDeviceCodeApplication();
        applicationId = application.getId();
        OpenIDConnectConfiguration oidcConfig = getOIDCInboundDetailsOfApplication(applicationId);
        clientId = oidcConfig.getClientId();
        clientSecret = oidcConfig.getClientSecret();

        actionId = createPreIssueAccessTokenAction();
        addUser();

        requestedScopes = new ArrayList<>(Arrays.asList("openid", "profile"));

        setServerEndpoints();

        serviceExtensionMockServer = new ServiceExtensionMockServer();
        serviceExtensionMockServer.startServer();
        serviceExtensionMockServer.setupStub(MOCK_SERVER_ENDPOINT_RESOURCE_PATH,
                "Basic " + getBase64EncodedString(MOCK_SERVER_AUTH_BASIC_USERNAME, MOCK_SERVER_AUTH_BASIC_PASSWORD),
                actionResponse.getResponseBody(), actionResponse.getStatusCode());
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
        cookieStore.clear();
        client.close();

        serviceExtensionMockServer = null;
    }

    @Test(groups = "wso2.is", description = "Verify token response when pre-issue access token action fails with " +
            "device code grant type.")
    public void testPreIssueAccessTokenActionFailure() throws Exception {

        sendDeviceAuthorize();
        sendDeviceAuthorizedPost();
        performDevicePost();
        performUserLogin();

        HttpResponse response = sendTokenRequestForDeviceCodeGrant();

        assertNotNull(response);
        assertEquals(response.getStatusLine().getStatusCode(), expectedTokenResponse.getStatusCode());

        String responseString = EntityUtils.toString(response.getEntity(), "UTF-8");
        JSONObject jsonResponse = new JSONObject(responseString);
        assertEquals(jsonResponse.getString("error"), expectedTokenResponse.getErrorMessage());
        assertEquals(jsonResponse.getString("error_description"), expectedTokenResponse.getErrorDescription());
    }

    /**
     * Sends a device authorize request to obtain device code and user code.
     */
    private void sendDeviceAuthorize() throws Exception {

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair(CLIENT_ID_PARAM, clientId));
        String scopes = String.join(" ", requestedScopes);
        urlParameters.add(new BasicNameValuePair("scope", scopes));

        org.json.simple.JSONObject responseObject = responseObjectNew(urlParameters, deviceAuthEndpoint);
        deviceCode = responseObject.get(DEVICE_CODE_PARAM).toString();
        userCode = responseObject.get(USER_CODE_PARAM).toString();
        assertNotNull(deviceCode, "device_code is null");
        assertNotNull(userCode, "user_code is null");
    }

    /**
     * Sends user code to device authorization page.
     */
    private void sendDeviceAuthorizedPost() throws Exception {

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair(USER_CODE_PARAM, userCode));
        String response = responsePost(urlParameters, deviceAuthPageEndpoint);
        assertNotNull(response, "Authorized response is null");
    }

    /**
     * Submits user code to device endpoint and extracts session data key.
     */
    private void performDevicePost() throws Exception {

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair(USER_CODE_PARAM, userCode));
        HttpResponse response = sendPostRequestWithParameters(client, urlParameters, deviceEndpoint);
        Header locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        assertNotNull(locationHeader, "Authorized response header is null");
        EntityUtils.consume(response.getEntity());

        response = sendGetRequest(client, locationHeader.getValue());
        assertNotNull(response, "Authorized user response is null.");

        Map<String, Integer> keyPositionMap = new HashMap<>(1);
        keyPositionMap.put("name=\"sessionDataKey\"", 1);
        List<DataExtractUtil.KeyValue> keyValues =
                DataExtractUtil.extractDataFromResponse(response, keyPositionMap);
        assertNotNull(keyValues, "sessionDataKey key value is null");

        sessionDataKey = keyValues.get(0).getValue();
        assertNotNull(sessionDataKey, "Session data key is null.");
        EntityUtils.consume(response.getEntity());
    }

    /**
     * Performs user login to complete the device authorization.
     * Since the application is configured with skipConsent, the device authorization
     * completes directly after login without requiring a consent approval step.
     */
    private void performUserLogin() throws Exception {

        HttpResponse response = sendLoginPostForCustomUsers(client, sessionDataKey, TEST_USER, TEST_WSO2);
        assertNotNull(response, "Login request failed. Login response is null.");

        if (Utils.requestMissingClaims(response)) {
            Assert.assertTrue(response.getFirstHeader("Set-Cookie").getValue().contains("pastr"),
                    "pastr cookie not found in response.");
            String pastreCookie = response.getFirstHeader("Set-Cookie").getValue().split(";")[0];
            EntityUtils.consume(response.getEntity());

            response = Utils.sendPOSTConsentMessage(response,
                    getTenantQualifiedURL(COMMON_AUTH_URL, tenantInfo.getDomain()),
                    USER_AGENT, Utils.getRedirectUrl(response), client, pastreCookie);
            EntityUtils.consume(response.getEntity());
        }
        Header locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        assertNotNull(locationHeader, "Login response header is null");
        EntityUtils.consume(response.getEntity());

        response = sendGetRequest(client, locationHeader.getValue());
        locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        EntityUtils.consume(response.getEntity());

        if (locationHeader != null) {
            response = sendGetRequest(client, locationHeader.getValue());
            EntityUtils.consume(response.getEntity());
        }
    }

    /**
     * Sends token request with device code grant type.
     *
     * @return HTTP response from the token endpoint
     */
    private HttpResponse sendTokenRequestForDeviceCodeGrant() throws Exception {

        // Wait 5 seconds because of the token polling interval.
        Thread.sleep(5000);

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.GRANT_TYPE_NAME, DEVICE_CODE_GRANT_TYPE));
        urlParameters.add(new BasicNameValuePair(CLIENT_ID_PARAM, clientId));
        urlParameters.add(new BasicNameValuePair(DEVICE_CODE_PARAM, deviceCode));

        HttpPost request = new HttpPost(tokenEndpoint);
        request.setHeader(CommonConstants.USER_AGENT_HEADER, USER_AGENT);
        request.setHeader(AUTHORIZATION_HEADER, OAuth2Constant.BASIC_HEADER + " " +
                getBase64EncodedString(clientId, clientSecret));
        request.setHeader("Content-Type", "application/x-www-form-urlencoded");
        request.setEntity(new UrlEncodedFormEntity(urlParameters));

        return client.execute(request);
    }

    /**
     * Sets the device flow server endpoints based on the tenant context.
     */
    private void setServerEndpoints() {

        deviceAuthEndpoint = getTenantQualifiedURL(
                ACCESS_TOKEN_ENDPOINT.replace("oauth2/token", "oauth2/device_authorize"),
                tenantInfo.getDomain());
        deviceAuthPageEndpoint = getTenantQualifiedURL(
                ACCESS_TOKEN_ENDPOINT.replace("oauth2/token", "authenticationendpoint/device.do"),
                tenantInfo.getDomain());
        deviceEndpoint = getTenantQualifiedURL(
                ACCESS_TOKEN_ENDPOINT.replace("oauth2/token", "oauth2/device"),
                tenantInfo.getDomain());
        tokenEndpoint = getTenantQualifiedURL(ACCESS_TOKEN_ENDPOINT, tenantInfo.getDomain());
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

    private ApplicationResponseModel createDeviceCodeApplication() throws Exception {

        ApplicationModel application = new ApplicationModel();

        List<String> grantTypes = new ArrayList<>();
        Collections.addAll(grantTypes, DEVICE_CODE_GRANT_TYPE);

        List<String> callBackUrls = new ArrayList<>();
        Collections.addAll(callBackUrls, OAuth2Constant.CALLBACK_URL);

        OpenIDConnectConfiguration oidcConfig = new OpenIDConnectConfiguration();
        oidcConfig.setGrantTypes(grantTypes);
        oidcConfig.setCallbackURLs(callBackUrls);

        AccessTokenConfiguration accessTokenConfig = new AccessTokenConfiguration().type("JWT");
        accessTokenConfig.setUserAccessTokenExpiryInSeconds(3600L);
        accessTokenConfig.setApplicationAccessTokenExpiryInSeconds(3600L);
        oidcConfig.setAccessToken(accessTokenConfig);

        InboundProtocols inboundProtocolsConfig = new InboundProtocols();
        inboundProtocolsConfig.setOidc(oidcConfig);

        application.setInboundProtocolConfiguration(inboundProtocolsConfig);
        application.setName(SERVICE_PROVIDER_NAME);
        application.setIsManagementApp(true);

        application.advancedConfigurations(
                new AdvancedApplicationConfiguration().skipLoginConsent(true).skipLogoutConsent(true));

        String appId = addApplication(application);
        return getApplication(appId);
    }

    /**
     * Sends a POST request and parses the response as a JSON object.
     *
     * @param postParameters POST parameters
     * @param uri            Target URI
     * @return JSON response object
     * @throws Exception If an error occurred while sending the request
     */
    private org.json.simple.JSONObject responseObjectNew(List<NameValuePair> postParameters, String uri)
            throws Exception {

        HttpPost httpPost = new HttpPost(uri);
        httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
        httpPost.setHeader(AUTHORIZATION_HEADER, OAuth2Constant.BASIC_HEADER + " " +
                getBase64EncodedString(clientId, clientSecret));
        httpPost.setEntity(new UrlEncodedFormEntity(postParameters));
        HttpResponse response = client.execute(httpPost);
        String responseString = EntityUtils.toString(response.getEntity(), "UTF-8");
        EntityUtils.consume(response.getEntity());

        JSONParser parser = new JSONParser();
        org.json.simple.JSONObject json = (org.json.simple.JSONObject) parser.parse(responseString);
        if (json == null) {
            throw new Exception("Error occurred while getting the response");
        }

        return json;
    }

    /**
     * Sends a POST request and returns the response body as a string.
     *
     * @param postParameters POST parameters
     * @param uri            Target URI
     * @return Response body as string
     * @throws Exception If an error occurred while sending the request
     */
    private String responsePost(List<NameValuePair> postParameters, String uri) throws Exception {

        HttpPost httpPost = new HttpPost(uri);
        httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
        httpPost.setEntity(new UrlEncodedFormEntity(postParameters));
        HttpResponse response = client.execute(httpPost);
        String responseString = EntityUtils.toString(response.getEntity(), "UTF-8");
        EntityUtils.consume(response.getEntity());

        return responseString;
    }
}

/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.identity.integration.test.rest.api.user.authorized.apps.v2;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.identity.integration.test.util.Utils;
import org.wso2.identity.integration.test.utils.CommonConstants;
import org.wso2.identity.integration.test.utils.DataExtractUtil;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.xpath.XPathExpressionException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.core.Is.is;
import static org.wso2.carbon.automation.engine.context.TestUserMode.SUPER_TENANT_ADMIN;

public class MeAuthorizedAppsScopeTest extends UserAuthorizedAppsBaseTest {

    private String clientIdApp1;
    private String appName1;
    private String appId1;
    private String clientIdApp2;
    private String appName2;
    private String appId2;
    private static final String APP_ID_PREFIX = "CLIENT_";
    private static final String APP_NAME_PREFIX = "APP_";
    private static final String APP_ID_SUFFIX_1 = "_1";
    private static final String APP_ID_SUFFIX_2 = "_2";
    private static final String CLIENT_SECRET = "TEST_CLIENT_SECRET";
    private String sessionDataKeyConsent;
    private String sessionDataKey;
    private String authorizationCode;
    private HttpClient httpClientForFirstSession;
    private HttpClient httpClientForSecondSession;

    private String accessToken;
    private List<String> accessTokes = new ArrayList<>();

    private static final String AUTHORIZED_API_ENDPOINT = "https://localhost:9853/t/carbon" +
            ".super/api/users/v2/me/authorized-apps";
    private String requestedScopes = "openid test_internal_login test_internal_user_update test_SYSTEM";

    @BeforeClass(alwaysRun = true)
    public void init() throws XPathExpressionException, RemoteException {

        super.testInit(API_VERSION, swaggerDefinition, tenant);
        initUrls("me");
        httpClientForFirstSession = HttpClientBuilder.create().setDefaultCookieStore(new BasicCookieStore()).build();
        httpClientForSecondSession = HttpClientBuilder.create().setDefaultCookieStore(new BasicCookieStore()).build();
        registerApplication(appName1, clientIdApp1, CLIENT_SECRET);
        registerApplication(appName2, clientIdApp2, CLIENT_SECRET);
    }

    @BeforeMethod(alwaysRun = true)
    public void testInit() {

        RestAssured.basePath = basePath;
    }

    @AfterMethod(alwaysRun = true)
    public void testFinish() {

        RestAssured.basePath = StringUtils.EMPTY;
    }

    public void registerApplication(String appName, String clientId, String clientSecret) {

        String body = "{\n" +
                "    \"client_name\": \"" + appName + "\",\n" +
                "    \"grant_types\": [\n" +
                "        \"authorization_code\"\n" +
                "    ],\n" +
                "    \"ext_param_client_id\": \"" + clientId + "\",\n" +
                "    \"ext_param_client_secret\": \"" + clientSecret + "\",\n" +
                " \"redirect_uris\":[\"http://localhost:8490/playground2/oauth2client\"]\n"+
                "}";
        Response response = getResponseOfJSONPost(dcrEndpointUri, body, new HashMap<>());

        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .body("client_name", equalTo(appName))
                .body("client_id", equalTo(clientId))
                .body("client_secret", equalTo(clientSecret));
    }

    @DataProvider(name = "restAPIUserConfigProvider")
    public static Object[][] restAPIUserConfigProvider() {

        return new Object[][]{
                {SUPER_TENANT_ADMIN},
        };
    }

    @Factory(dataProvider = "restAPIUserConfigProvider")
    public MeAuthorizedAppsScopeTest(TestUserMode userMode) throws Exception {

        super.init(userMode);
        this.context = isServer;
        this.authenticatingUserName = context.getContextTenant().getTenantAdmin().getUserName();
        this.authenticatingCredential = context.getContextTenant().getTenantAdmin().getPassword();
        this.tenant = context.getContextTenant().getDomain();

        this.clientIdApp1 = APP_ID_PREFIX + userMode + APP_ID_SUFFIX_1;
        this.clientIdApp2 = APP_ID_PREFIX + userMode + APP_ID_SUFFIX_2;
        this.appName1 = APP_NAME_PREFIX + userMode + APP_ID_SUFFIX_1;
        this.appName2 = APP_NAME_PREFIX + userMode + APP_ID_SUFFIX_2;
    }

    @AfterClass(alwaysRun = true)
    public void testConclude() throws Exception {

        deleteApplication(clientIdApp1);
        deleteApplication(clientIdApp2);
        super.conclude();
    }

    @Test(groups = "wso2.is", description = "Test login using OpenId connect authorization code flow")
    public void testOIDCLoginForApp1() throws Exception {

        initiateAuthorizationRequest(clientIdApp1, httpClientForFirstSession);
        authenticateUser(httpClientForFirstSession);
        performConsentApproval(httpClientForFirstSession);
        generateAuthzCodeAccessToken(clientIdApp1, httpClientForFirstSession);
        introspectActiveAccessToken(httpClientForFirstSession, accessToken);
    }

    @Test(groups = "wso2.is", dependsOnMethods = {"testOIDCLoginForApp1"}, description = "Test login using OpenId " +
            "connect authorization code flow")
    public void testOIDCLoginForApp2() throws Exception {

        initiateAuthorizationRequest(clientIdApp2, httpClientForSecondSession);
        authenticateUser(httpClientForSecondSession);
        performConsentApproval(httpClientForSecondSession);
        generateAuthzCodeAccessToken(clientIdApp2, httpClientForSecondSession);
        introspectActiveAccessToken(httpClientForSecondSession, accessToken);
    }

    @Test(groups = "wso2.is", dependsOnMethods = {"testOIDCLoginForApp2"}, description =
            "Get User authorized apps using authorized apps REST API")
    public void testGetAuthorizedApps() {

        Response response = getResponseOfGet(AUTHORIZED_API_ENDPOINT);
        response.then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .log().ifValidationFails()
                .body("size()", is(2));

        List<HashMap<String, String>> jresponse = response.jsonPath().getList("$");
        for (HashMap application : jresponse) {
            if (this.clientIdApp1.equals(application.get("clientId"))) {
                this.appId1 = (String) application.get("id");
                List<String> approvedScopesForApp1 = (ArrayList<String>) application.get("approvedScopes");
                Assert.assertEquals(approvedScopesForApp1.size(), 3,
                        "This authorized app should have allowed " + requestedScopes + "scopes");
            }
            if (this.clientIdApp2.equals(application.get("clientId"))) {
                this.appId2 = (String) application.get("id");
                List<String> approvedScopesForApp2 = (ArrayList<String>) application.get("approvedScopes");
                Assert.assertEquals(approvedScopesForApp2.size(), 3, "This authorized app should have allowed " +
                        "'test_internal_login', 'test_internal_user_update'," + " 'test_SYSTEM' scopes");
            }
        }
    }

    @Test(dependsOnMethods = {"testGetAuthorizedApps"}, description = "Get User authorized app by appId")
    public void testGetAuthorizedAppById() throws Exception {

        String[] approvedScopesForApp1 = new String[]{"test_internal_login", "test_internal_user_update", "test_SYSTEM"};
        Response response = getResponseOfGet(this.userAuthorizedAppsEndpointUri + appId1);
        response.then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("id", equalTo(appId1))
                .body("clientId", equalTo(clientIdApp1))
                .body("approvedScopes", hasItems(approvedScopesForApp1))
                .log().ifValidationFails();
    }

    @Test(dependsOnMethods = {"testGetAuthorizedAppById"}, description = "Delete all authorized apps")
    public void testDeleteAuthorizedApps() throws Exception {

        getResponseOfDelete(this.userAuthorizedAppsEndpointUri)
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_NO_CONTENT)
                .log().ifValidationFails();
    }

    @Test(groups = "wso2.is", dependsOnMethods = {"testDeleteAuthorizedApps"}, description = " The expected " +
            "behaviour is, When the authorized apps are removed, the corresponding accesstokens should be revoked")
    public void testTokenRevocationAuthorizedAppsDeleted() throws Exception {

        for (String token : accessTokes) {
            JSONObject object = testIntrospectionEndpoint(httpClientForFirstSession, token);
            Assert.assertEquals(object.get("active"), false);
        }
    }

    /**
     * Playground app will initiate authorization request to IS and obtain session data key.
     *
     * @throws IOException
     */
    private void initiateAuthorizationRequest(String clientId, HttpClient client) throws IOException {

        List<NameValuePair> urlParameters = getOIDCInitiationRequestParams(clientId);
        HttpResponse response = sendPostRequestWithParameters(client, urlParameters,
                OAuth2Constant.AUTHORIZED_USER_URL);
        Assert.assertNotNull(response, "Authorization response is null");
        Header locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        Assert.assertNotNull(locationHeader, "Authorization response header is null.");
        EntityUtils.consume(response.getEntity());
        response = sendGetRequest(client, locationHeader.getValue());
        sessionDataKey = Utils.extractDataFromResponse(response, CommonConstants.SESSION_DATA_KEY, 1);
        EntityUtils.consume(response.getEntity());
    }

    /**
     * Provide user credentials and authenticate to the system.
     *
     * @throws IOException
     */
    private void authenticateUser(HttpClient client) throws IOException {

        // Pass user credentials to commonauth endpoint and authenticate the user.
        HttpResponse response = sendLoginPost(client, sessionDataKey);
        Assert.assertNotNull(response, "OIDC login request response is null.");
        Header locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        Assert.assertNotNull(locationHeader, "OIDC login response header is null.");
        EntityUtils.consume(response.getEntity());
        // Get the sessionDatakeyConsent from the redirection after authenticating the user.
        response = sendGetRequest(client, locationHeader.getValue());
        Map<String, Integer> keyPositionMap = new HashMap<>(1);
        keyPositionMap.put("name=\"sessionDataKeyConsent\"", 1);
        List<DataExtractUtil.KeyValue> keyValues = DataExtractUtil.extractSessionConsentDataFromResponse(response,
                keyPositionMap);
        Assert.assertNotNull(keyValues, "SessionDataKeyConsent keyValues map is null.");
        sessionDataKeyConsent = keyValues.get(0).getValue();
        Assert.assertNotNull(sessionDataKeyConsent, "sessionDataKeyConsent is null.");
        EntityUtils.consume(response.getEntity());
    }

    /**
     * Approve the consent.
     *
     * @throws IOException
     */
    private void performConsentApproval(HttpClient client) throws IOException {

        HttpResponse response = sendApprovalPostWithConsent(client, sessionDataKeyConsent, null);
        Assert.assertNotNull(response, "OIDC consent approval request response is null.");
        Header locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        Assert.assertNotNull(locationHeader, "OIDC consent approval request location header is null.");
        EntityUtils.consume(response.getEntity());
        // Get authorization code flow.
        response = sendPostRequest(client, locationHeader.getValue());
        Map<String, Integer> keyPositionMap = new HashMap<>(1);
        keyPositionMap.put("Authorization Code", 1);
        List<DataExtractUtil.KeyValue> keyValues = DataExtractUtil.extractTableRowDataFromResponse(response,
                keyPositionMap);
        Assert.assertNotNull(keyValues, "Authorization code not received.");
        authorizationCode = keyValues.get(0).getValue();
        Assert.assertNotNull(authorizationCode, "Authorization code not received.");
        EntityUtils.consume(response.getEntity());
    }

    /**
     * Exchange authorization code and get accesstoken.
     *
     * @throws Exception
     */
    private void generateAuthzCodeAccessToken(String clientId, HttpClient client) throws Exception {

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.GRANT_TYPE_NAME,
                OAuth2Constant.OAUTH2_GRANT_TYPE_AUTHORIZATION_CODE));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_REDIRECT_URI, OAuth2Constant.CALLBACK_URL));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.AUTHORIZATION_CODE_NAME, authorizationCode));
        JSONObject jsonResponse = responseObject(client, OAuth2Constant.ACCESS_TOKEN_ENDPOINT, urlParameters,
                clientId, CLIENT_SECRET);
        Assert.assertNotNull(jsonResponse.get(OAuth2Constant.ACCESS_TOKEN), "Access token is null.");
        accessToken = (String) jsonResponse.get(OAuth2Constant.ACCESS_TOKEN);
        accessTokes.add(accessToken);
    }

    private List<NameValuePair> getOIDCInitiationRequestParams(String clientId) {

        List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
        urlParameters.add(new BasicNameValuePair("grantType", OAuth2Constant.OAUTH2_GRANT_TYPE_CODE));
        urlParameters.add(new BasicNameValuePair("consumerKey", clientId));
        urlParameters.add(new BasicNameValuePair("callbackurl", OAuth2Constant.CALLBACK_URL));
        urlParameters.add(new BasicNameValuePair("authorizeEndpoint", OAuth2Constant.APPROVAL_URL));
        urlParameters.add(new BasicNameValuePair("authorize", OAuth2Constant.AUTHORIZE_PARAM));
        urlParameters.add(new BasicNameValuePair("scope", requestedScopes));
        return urlParameters;
    }

    /**
     * Introspect the obtained accesstoken and it should be an active token.
     *
     * @throws Exception
     */
    private void introspectActiveAccessToken(HttpClient client, String accessToken) throws Exception {

        JSONObject object = testIntrospectionEndpoint(client, accessToken);
        Assert.assertEquals(object.get("active"), true);
    }

    /**
     * Get introspection endpoint response by callling introspection endpoint.
     *
     * @return JSONObject
     * @throws Exception
     */
    private JSONObject testIntrospectionEndpoint(HttpClient client, String accessToken) throws Exception {

        List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
        urlParameters.add(new BasicNameValuePair("token", accessToken));
        return responseObject(client, OAuth2Constant.INTRO_SPEC_ENDPOINT, urlParameters, userInfo.getUserName(),
                userInfo.getPassword());
    }

    /**
     * Build post request and return json response object.
     *
     * @param endpoint       Endpoint.
     * @param postParameters postParameters.
     * @param key            Basic authentication key.
     * @param secret         Basic authentication secret.
     * @return JSON object of the response.
     * @throws Exception
     */
    private JSONObject responseObject(HttpClient client, String endpoint, List<NameValuePair> postParameters,
                                      String key, String secret) throws Exception {

        HttpPost httpPost = new HttpPost(endpoint);
        httpPost.setHeader("Authorization", "Basic " + getBase64EncodedString(key, secret));
        httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
        httpPost.setEntity(new UrlEncodedFormEntity(postParameters));
        HttpResponse response = client.execute(httpPost);
        String responseString = EntityUtils.toString(response.getEntity(), "UTF-8");
        EntityUtils.consume(response.getEntity());
        JSONParser parser = new JSONParser();
        JSONObject json = (JSONObject) parser.parse(responseString);
        if (json == null) {
            throw new Exception("Error occurred while getting the response.");
        }
        return json;
    }

    private HttpResponse sendApprovalPostWithConsent(HttpClient client, String sessionDataKeyConsent,
                                                    List<NameValuePair> consentClaims) throws IOException {

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("consent", "approveAlways"));
        urlParameters.add(new BasicNameValuePair("scope-approval", "approveAlways"));
        urlParameters.add(new BasicNameValuePair("sessionDataKeyConsent", sessionDataKeyConsent));
        if (consentClaims != null) {
            urlParameters.addAll(consentClaims);
        }
        return sendPostRequestWithParameters(client, urlParameters, OAuth2Constant.APPROVAL_URL);
    }

    private HttpResponse sendPostRequestWithParameters(HttpClient client, List<NameValuePair> urlParameters,
                                                      String url) throws IOException {

        HttpPost request = new HttpPost(url);
        request.setHeader("User-Agent", OAuth2Constant.USER_AGENT);
        request.setEntity(new UrlEncodedFormEntity(urlParameters));
        return client.execute(request);
    }

    private HttpResponse sendGetRequest(HttpClient client, String locationURL) throws IOException {

        HttpGet getRequest = new HttpGet(locationURL);
        getRequest.setHeader("User-Agent", OAuth2Constant.USER_AGENT);
        return client.execute(getRequest);
    }

    private HttpResponse sendLoginPost(HttpClient client, String sessionDataKey) throws IOException {

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("username", userInfo.getUserName()));
        urlParameters.add(new BasicNameValuePair("password", userInfo.getPassword()));
        urlParameters.add(new BasicNameValuePair("sessionDataKey", sessionDataKey));
        log.info("Found sessionDataKey: " + sessionDataKey + " during login post.");
        return sendPostRequestWithParameters(client, urlParameters, OAuth2Constant.COMMON_AUTH_URL);
    }

    private String getBase64EncodedString(String consumerKey, String consumerSecret) {

        return new String(Base64.encodeBase64((consumerKey + ":" + consumerSecret).getBytes()));
    }

    private HttpResponse sendPostRequest(HttpClient client, String locationURL) throws IOException {

        HttpPost postRequest = new HttpPost(locationURL);
        postRequest.setHeader("User-Agent", OAuth2Constant.USER_AGENT);
        return client.execute(postRequest);
    }
}

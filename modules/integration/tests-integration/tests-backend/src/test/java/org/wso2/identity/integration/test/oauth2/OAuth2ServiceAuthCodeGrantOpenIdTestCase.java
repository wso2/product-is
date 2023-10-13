/*
 * Copyright (c) 2015, WSO2 LLC. (http://www.wso2.com).
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
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
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
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationResponseModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.OpenIDConnectConfiguration;
import org.wso2.identity.integration.test.rest.api.user.common.model.Email;
import org.wso2.identity.integration.test.rest.api.user.common.model.ListObject;
import org.wso2.identity.integration.test.rest.api.user.common.model.PatchOperationRequestObject;
import org.wso2.identity.integration.test.rest.api.user.common.model.RoleItemAddGroupobj;
import org.wso2.identity.integration.test.rest.api.user.common.model.UserObject;
import org.wso2.identity.integration.test.restclients.SCIM2RestClient;
import org.wso2.identity.integration.test.utils.DataExtractUtil;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.wso2.identity.integration.test.utils.DataExtractUtil.KeyValue;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.COMMON_AUTH_URL;

public class OAuth2ServiceAuthCodeGrantOpenIdTestCase extends OAuth2ServiceAbstractIntegrationTest {

    private String accessToken;
    private String sessionDataKeyConsent;
    private String sessionDataKey;
    private String authorizationCode;
    AutomationContext context;

    private String consumerKey;
    private String consumerSecret;

    private Lookup<CookieSpecProvider> cookieSpecRegistry;
    private RequestConfig requestConfig;
    private CloseableHttpClient client;

    private static final String USERS_PATH = "users";
    private static final String USER_EMAIL = "abc@wso2.com";
    private static final String USERNAME = "authcodegrantuser";
    private static final String PASSWORD = "pass123";

    private final List<NameValuePair> consentParameters = new ArrayList<>();
    private final CookieStore cookieStore = new BasicCookieStore();
    private final String username;
    private final String userPassword;
    private String applicationId;
    private String userId;
    private SCIM2RestClient scim2RestClient;

    @DataProvider(name = "configProvider")
    public static Object[][] configProvider() {
        return new Object[][]{{TestUserMode.SUPER_TENANT_ADMIN}, {TestUserMode.TENANT_ADMIN}};
    }

    @Factory(dataProvider = "configProvider")
    public OAuth2ServiceAuthCodeGrantOpenIdTestCase(TestUserMode userMode) throws Exception {

        super.init(userMode);
        context = new AutomationContext("IDENTITY", userMode);
        this.username = context.getContextTenant().getTenantAdmin().getUserNameWithoutDomain();
        this.userPassword = context.getContextTenant().getTenantAdmin().getPassword();
    }

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        tenantInfo = context.getContextTenant();
        scim2RestClient =  new SCIM2RestClient(serverURL, tenantInfo);
        
        cookieSpecRegistry = RegistryBuilder.<CookieSpecProvider>create()
                .register(CookieSpecs.DEFAULT, new RFC6265CookieSpecProvider())
                .build();
        requestConfig = RequestConfig.custom()
                .setCookieSpec(CookieSpecs.DEFAULT)
                .build();
        client = HttpClientBuilder.create()
                .setDefaultCookieSpecRegistry(cookieSpecRegistry)
                .setDefaultRequestConfig(requestConfig)
                .setDefaultCookieStore(cookieStore).build();
        setSystemproperties();
        addAdminUser();
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {
        deleteApp(applicationId);
        scim2RestClient.deleteUser(userId);

        restClient.closeHttpClient();
        scim2RestClient.closeHttpClient();
        client.close();

        consumerKey = null;
        accessToken = null;
    }

    @Test(groups = "wso2.is", description = "Check Oauth2 application registration")
    public void testRegisterApplication() throws Exception {
        ApplicationResponseModel application = addApplication();
        Assert.assertNotNull(application, "OAuth App creation failed.");
        applicationId = application.getId();
        OpenIDConnectConfiguration oidcConfig = getOIDCInboundDetailsOfApplication(applicationId);

        consumerKey = oidcConfig.getClientId();
        Assert.assertNotNull(consumerKey, "Application creation failed.");
        consumerSecret = oidcConfig.getClientSecret();
    }

    @Test(groups = "wso2.is", description = "Send authorize user request", dependsOnMethods = "testRegisterApplication")
    public void testSendAuthorizedPost() throws Exception {
        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("grantType", OAuth2Constant.OAUTH2_GRANT_TYPE_CODE));
        urlParameters.add(new BasicNameValuePair("consumerKey", consumerKey));
        urlParameters.add(new BasicNameValuePair("callbackurl", OAuth2Constant.CALLBACK_URL));
        urlParameters.add(new BasicNameValuePair("authorizeEndpoint",
                addTenantToURL( OAuth2Constant.APPROVAL_URL, tenantInfo.getDomain())));
        urlParameters.add(new BasicNameValuePair("authorize", OAuth2Constant.AUTHORIZE_PARAM));
        urlParameters.add(new BasicNameValuePair("scope", OAuth2Constant.OAUTH2_SCOPE_OPENID + " "
                + OAuth2Constant.OAUTH2_SCOPE_EMAIL));

        HttpResponse response =
                sendPostRequestWithParameters(client, urlParameters,
                        OAuth2Constant.AUTHORIZED_USER_URL);
        Assert.assertNotNull(response, "Authorization request failed. Authorized response is null");

        Header locationHeader =
                response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        Assert.assertNotNull(locationHeader,
                "Authorization request failed. Authorized response header is null");
        EntityUtils.consume(response.getEntity());

        response = sendGetRequest(client, locationHeader.getValue());
        Assert.assertNotNull(response,
                "Authorization request failed. Authorized user response is null.");

        Map<String, Integer> keyPositionMap = new HashMap<>(1);
        keyPositionMap.put("name=\"sessionDataKey\"", 1);
        List<KeyValue> keyValues =
                DataExtractUtil.extractDataFromResponse(response, keyPositionMap);
        Assert.assertNotNull(keyValues, "sessionDataKey key value is null");

        sessionDataKey = keyValues.get(0).getValue();
        Assert.assertNotNull(sessionDataKey, "Session data key is null.");
        EntityUtils.consume(response.getEntity());
    }

    @Test(groups = "wso2.is", description = "Send login post request", dependsOnMethods = "testSendAuthorizedPost")
    public void testSendLoginPost() throws Exception {
        HttpResponse response = sendLoginPost(client, sessionDataKey);
        Assert.assertNotNull(response, "Login request failed. response is null.");

        EntityUtils.consume(response.getEntity());

        Header locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        Assert.assertNotNull(locationHeader, "Login response header is null");
        response = sendConsentGetRequest(client, locationHeader.getValue(), cookieStore, consentParameters);
        Map<String, Integer> keyPositionMap = new HashMap<>(1);
        keyPositionMap.put("name=\"sessionDataKeyConsent\"", 1);
        List<KeyValue> keyValues =
                DataExtractUtil.extractSessionConsentDataFromResponse(response,
                        keyPositionMap);
        Assert.assertNotNull(keyValues, "SessionDataKeyConsent key value is null");

        sessionDataKeyConsent = keyValues.get(0).getValue();
        Assert.assertNotNull(sessionDataKeyConsent, "Invalid session key consent.");
        EntityUtils.consume(response.getEntity());
    }

    @Test(groups = "wso2.is", description = "Send approval post request", dependsOnMethods = "testSendLoginPost")
    public void testSendApprovalPost() throws Exception {

        HttpResponse response = sendApprovalPostWithConsent(client, sessionDataKeyConsent, consentParameters);
        Assert.assertNotNull(response, "Approval request failed. response is invalid.");

        Header locationHeader =
                response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        Assert.assertNotNull(locationHeader, "Approval request failed. Location header is null.");
        EntityUtils.consume(response.getEntity());

        response = sendPostRequest(client, locationHeader.getValue());
        Assert.assertNotNull(response, "Get Activation response is invalid.");

        Map<String, Integer> keyPositionMap = new HashMap<>(1);
        keyPositionMap.put("Authorization Code", 1);
        List<KeyValue> keyValues =
                DataExtractUtil.extractTableRowDataFromResponse(response,
                        keyPositionMap);
        Assert.assertNotNull(response, "Authorization Code key value is invalid.");

        if (keyValues != null) {
            authorizationCode = keyValues.get(0).getValue();
        }
        Assert.assertNotNull(authorizationCode, "Authorization code is null.");
        EntityUtils.consume(response.getEntity());
    }

    @Test(groups = "wso2.is", description = "Get access token", dependsOnMethods = "testSendApprovalPost")
    public void testGetAccessToken() throws Exception {
        HttpResponse response = sendGetAccessTokenPost(client, consumerSecret);
        Assert.assertNotNull(response, "Approval response is invalid.");
        EntityUtils.consume(response.getEntity());

        response = sendPostRequest(client, OAuth2Constant.AUTHORIZED_URL);

        Map<String, Integer> keyPositionMap = new HashMap<>(1);
        keyPositionMap.put("name=\"accessToken\"", 1);
        List<KeyValue> keyValues =
                DataExtractUtil.extractInputValueFromResponse(response,
                        keyPositionMap);
        Assert.assertNotNull(keyValues, "Access token Key value is null.");

        accessToken = keyValues.get(0).getValue();
        Assert.assertNotNull(accessToken, "Access token is null.");
        EntityUtils.consume(response.getEntity());

        response = sendPostRequest(client, OAuth2Constant.AUTHORIZED_URL);

        keyPositionMap = new HashMap<>(1);
        keyPositionMap.put("id=\"loggedUser\"", 1);
        keyValues = DataExtractUtil.extractLabelValueFromResponse(response, keyPositionMap);
        Assert.assertNotNull(keyValues, "Access token Key value is null.");

        String loggedUser = keyValues.get(0).getValue();
        Assert.assertNotNull(loggedUser, "Logged user is null.");
        Assert.assertNotEquals(loggedUser, "null", "Logged user is null.");
        Assert.assertNotEquals(loggedUser, "", "Logged user is null.");
        EntityUtils.consume(response.getEntity());
    }

    @Test(groups = "wso2.is", description = "Validate access token", dependsOnMethods = "testGetAccessToken")
    public void testValidateAccessToken() throws Exception {

        JSONObject responseObj = introspectToken();
        Assert.assertNotNull(responseObj, "Validate access token failed. response is invalid.");
        Assert.assertEquals(responseObj.get("active"), true, "Token Validation failed");
    }

    @Test(groups = "wso2.is", description = "Validate the user claim values", dependsOnMethods = "testGetAccessToken")
    public void testClaims() throws Exception {

        String userInfoUrl = tenantInfo.getDomain().equalsIgnoreCase("carbon.super") ?
                OAuth2Constant.USER_INFO_ENDPOINT : OAuth2Constant.TENANT_USER_INFO_ENDPOINT;
        HttpGet request = new HttpGet(userInfoUrl);

        request.setHeader("User-Agent", OAuth2Constant.USER_AGENT);
        request.setHeader("Authorization", "Bearer " + accessToken);
        request.setHeader("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");

        HttpResponse response = client.execute(request);
        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

        Object obj = JSONValue.parse(rd);
        String email = ((org.json.simple.JSONObject) obj).get("email").toString();

        EntityUtils.consume(response.getEntity());
        Assert.assertEquals(USER_EMAIL, email, "Incorrect email claim value");
    }

    @Test(groups = "wso2.is", description = "Validate Token Expiration Time",
            dependsOnMethods = "testValidateAccessToken")
    public void testValidateTokenExpirationTime() throws Exception {
        JSONObject tokenResponse = introspectToken();

        Assert.assertNotNull(tokenResponse.get("exp"), "'exp' value is not included");
        long expValue = Long.parseLong(tokenResponse.get("exp").toString());
        // ratio between these vales is normally 999, used 975 just to be in the safe side
        Assert.assertTrue(System.currentTimeMillis() / expValue > 975, "'exp time is not in milliseconds'");
    }

    @Test(groups = "wso2.is", description = "Validate Authorization Context of jwt Token", dependsOnMethods =
            "testValidateAccessToken")
    public void testValidateTokenScope() throws Exception {
        JSONObject tokenResponse = introspectToken();

        Assert.assertTrue(tokenResponse.size() > 1, "Invalid JWT token received");
        Assert.assertNotNull(tokenResponse.get("scope"), "'scope' is not included");

        String scopes = tokenResponse.get("scope").toString();
        Assert.assertTrue(scopes.contains("email"), "Invalid JWT Token scope Value");
        Assert.assertTrue(scopes.contains("openid"), "Invalid JWT Token scope Value");
    }

    public HttpResponse sendLoginPost(HttpClient client, String sessionDataKey) throws IOException {
        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("username", USERNAME));
        urlParameters.add(new BasicNameValuePair("password", PASSWORD));
        urlParameters.add(new BasicNameValuePair("sessionDataKey", sessionDataKey));

        return sendPostRequestWithParameters(client, urlParameters, getTenantQualifiedURL(COMMON_AUTH_URL, tenantInfo.getDomain()));
    }

    private JSONObject introspectToken() throws Exception {
        String introspectionUrl = tenantInfo.getDomain().equalsIgnoreCase("carbon.super") ?
                OAuth2Constant.INTRO_SPEC_ENDPOINT : OAuth2Constant.TENANT_INTRO_SPEC_ENDPOINT;
        return introspectTokenWithTenant(client, accessToken, introspectionUrl,
                username + "@" + tenantInfo.getDomain(), userPassword);
    }

    private void addAdminUser() throws Exception {
        UserObject userInfo = new UserObject();
        userInfo.setUserName(USERNAME);
        userInfo.setPassword(PASSWORD);
        userInfo.addEmail(new Email().value(USER_EMAIL));

        userId = scim2RestClient.createUser(userInfo);
        String roleId = scim2RestClient.getRoleIdByName("admin");

        RoleItemAddGroupobj patchRoleItem = new RoleItemAddGroupobj();
        patchRoleItem.setOp(RoleItemAddGroupobj.OpEnum.ADD);
        patchRoleItem.setPath(USERS_PATH);
        patchRoleItem.addValue(new ListObject().value(userId));

        scim2RestClient.updateUserRole(new PatchOperationRequestObject().addOperations(patchRoleItem), roleId);
    }
}

/*
 * Copyright (c) 2020, WSO2 LLC. (http://www.wso2.com).
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
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.RFC6265CookieSpecProvider;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationResponseModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.InboundProtocols;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.OIDCLogoutConfiguration;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.OpenIDConnectConfiguration;
import org.wso2.identity.integration.test.rest.api.user.common.model.Email;
import org.wso2.identity.integration.test.rest.api.user.common.model.ListObject;
import org.wso2.identity.integration.test.rest.api.user.common.model.PatchOperationRequestObject;
import org.wso2.identity.integration.test.rest.api.user.common.model.RoleItemAddGroupobj;
import org.wso2.identity.integration.test.rest.api.user.common.model.UserObject;
import org.wso2.identity.integration.test.restclients.SCIM2RestClient;
import org.wso2.identity.integration.test.util.Utils;
import org.wso2.identity.integration.test.utils.CommonConstants;
import org.wso2.identity.integration.test.utils.DataExtractUtil;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.wso2.identity.integration.test.utils.CommonConstants.DEFAULT_TOMCAT_PORT;

/**
 * Integration tests for OAuth2 Back-Channel Logout.
 */
public class OAuth2BackChannelLogoutTestCase extends OAuth2ServiceAbstractIntegrationTest {

    private Lookup<CookieSpecProvider> cookieSpecRegistry;
    private RequestConfig requestConfig;
    private HttpClient client;
    private final String OIDC_APP_NAME = "playground2";
    private String oidcAppClientId;
    private String sessionDataKeyConsent;
    private String sessionDataKey;
    private static final String CONSENT = "consent";
    private static final String APPROVE = "approve";
    private static final String SCOPE_APPROVAL = "scope-approval";
    private static final String USER_AGENT = "User-Agent";
    private static final String USERS_PATH = "users";

    private static final String USER_EMAIL = "abc@wso2.com";
    private static final String USERNAME = "testUser";
    private static final String PASSWORD = "pass123";
    private String applicationId;
    private SCIM2RestClient scim2RestClient;
    private String userId;

    @DataProvider(name = "configProvider")
    public static Object[][] configProvider() {
        return new Object[][]{{TestUserMode.SUPER_TENANT_ADMIN}, {TestUserMode.TENANT_ADMIN}};
    }

    @Factory(dataProvider = "configProvider")
    public OAuth2BackChannelLogoutTestCase(TestUserMode userMode) throws Exception {

        super.init(userMode);
    }

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        init();

        cookieSpecRegistry = RegistryBuilder.<CookieSpecProvider>create()
                .register(CookieSpecs.DEFAULT, new RFC6265CookieSpecProvider())
                .build();
        requestConfig = RequestConfig.custom()
                .setCookieSpec(CookieSpecs.DEFAULT)
                .build();
        client = HttpClientBuilder.create()
                .setDefaultCookieStore(new BasicCookieStore())
                .setDefaultRequestConfig(requestConfig)
                .setDefaultCookieSpecRegistry(cookieSpecRegistry)
                .build();
        scim2RestClient =  new SCIM2RestClient(serverURL, tenantInfo);
        
        createOIDCApplication();
        addAdminUser();
    }

    @AfterClass(alwaysRun = true)
    public void testCleanUp() throws Exception {

        removeApplications();
        scim2RestClient.deleteUser(userId);
    }

    @Test(groups = "wso2.is", description = "Test back channel logout for OIDC.")
    public void testOIDCLogout() throws IOException {

        // Login.
        initiateOIDCRequest(false);
        performOIDCLogin();
        performOIDCConsentApproval();

        // OIDC app logout.
        performOIDCLogout();
    }

    private void createOIDCApplication() throws Exception {
        ApplicationModel application = new ApplicationModel();

        List<String> grantTypes = new ArrayList<>();
        Collections.addAll(grantTypes, OAuth2Constant.OAUTH2_GRANT_TYPE_AUTHORIZATION_CODE);

        List<String> callBackUrls = new ArrayList<>();
        Collections.addAll(callBackUrls, OAuth2Constant.CALLBACK_URL);

        OpenIDConnectConfiguration oidcConfig = new OpenIDConnectConfiguration();
        oidcConfig.setGrantTypes(grantTypes);
        oidcConfig.setCallbackURLs(callBackUrls);
        oidcConfig.setLogout(new OIDCLogoutConfiguration().backChannelLogoutUrl("http://localhost:" +
                DEFAULT_TOMCAT_PORT + "/playground2/bclogout"));

        InboundProtocols inboundProtocolsConfig = new InboundProtocols();
        inboundProtocolsConfig.setOidc(oidcConfig);

        application.setInboundProtocolConfiguration(inboundProtocolsConfig);
        application.setName(OIDC_APP_NAME);

        String appId = addApplication(application);
        ApplicationResponseModel createdApplication = getApplication(appId);

        applicationId = createdApplication.getId();
        oidcConfig = getOIDCInboundDetailsOfApplication(applicationId);

        oidcAppClientId = oidcConfig.getClientId();
    }

    private void removeApplications() throws Exception {

        deleteApp(applicationId);
    }

    private void initiateOIDCRequest(boolean isCheckLogoutConfirmation) throws IOException {

        List<NameValuePair> urlParameters = getOIDCInitiationRequestParams();
        HttpResponse response = sendPostRequestWithParameters(urlParameters,
                "http://localhost:" + DEFAULT_TOMCAT_PORT + "/playground2/oauth2-authorize-user.jsp");
        Assert.assertNotNull(response, "Authorization response is null");
        Header locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        Assert.assertNotNull(locationHeader, "Authorization response header is null.");
        EntityUtils.consume(response.getEntity());

        response = sendGetRequest(locationHeader.getValue());
        sessionDataKey = Utils.extractDataFromResponse(response, CommonConstants.SESSION_DATA_KEY, 1);
        if (isCheckLogoutConfirmation) {
            Assert.assertNotNull(sessionDataKey, "Back channel logout failed for OIDC.");
        } else {
            Assert.assertNotNull(sessionDataKey, "SessionDataKey is null for for OIDC app: " + OIDC_APP_NAME);
        }
        EntityUtils.consume(response.getEntity());
    }

    private void performOIDCLogin() throws IOException {

        HttpResponse response = sendLoginPost(sessionDataKey);
        Assert.assertNotNull(response, "OIDC login request response is null.");
        Header locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        Assert.assertNotNull(locationHeader, "OIDC login response header is null.");
        EntityUtils.consume(response.getEntity());

        response = sendGetRequest(locationHeader.getValue());
        Map<String, Integer> keyPositionMap = new HashMap<>(1);
        keyPositionMap.put("name=\"sessionDataKeyConsent\"", 1);
        List<DataExtractUtil.KeyValue> keyValues = DataExtractUtil.extractSessionConsentDataFromResponse(
                response, keyPositionMap);
        Assert.assertNotNull(keyValues, "SessionDataKeyConsent keyValues map is null.");
        sessionDataKeyConsent = keyValues.get(0).getValue();
        Assert.assertNotNull(sessionDataKeyConsent, "sessionDataKeyConsent is null.");
        EntityUtils.consume(response.getEntity());
    }

    private void performOIDCConsentApproval() throws IOException {

        HttpResponse response = sendApprovalPostWithConsent(sessionDataKeyConsent);
        Assert.assertNotNull(response, "OIDC consent approval request response is null.");
        Header locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        Assert.assertNotNull(locationHeader, "OIDC consent approval request location header is null.");
        EntityUtils.consume(response.getEntity());

        response = sendPostRequest(locationHeader.getValue());
        Map<String, Integer> keyPositionMap = new HashMap<>(1);
        keyPositionMap.put("Authorization Code", 1);
        List<DataExtractUtil.KeyValue> keyValues =
                DataExtractUtil.extractTableRowDataFromResponse(response, keyPositionMap);
        Assert.assertNotNull(keyValues, "Authorization code not received.");
        String authorizationCode = keyValues.get(0).getValue();
        Assert.assertNotNull(authorizationCode, "Authorization code not received.");
        EntityUtils.consume(response.getEntity());
    }

    private void performOIDCLogout() throws IOException {

        String oidcLogoutUrl = identityContextUrls.getWebAppURLHttps() + "/oidc/logout";
        HttpResponse response = sendGetRequest(oidcLogoutUrl);
        EntityUtils.consume(response.getEntity());

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair(CONSENT, APPROVE));
        response = sendPostRequestWithParameters(urlParameters, oidcLogoutUrl);
        Header locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        EntityUtils.consume(response.getEntity());

        response = sendGetRequest(locationHeader.getValue());
        Assert.assertNotNull(response, "OIDC Logout failed.");
        String result = DataExtractUtil.getContentData(response);
        Assert.assertTrue(result.contains("You have successfully logged out"), "OIDC logout failed.");
        EntityUtils.consume(response.getEntity());
    }

    public HttpResponse sendGetRequest(String locationURL) throws IOException {

        HttpGet getRequest = new HttpGet(locationURL);
        getRequest.setHeader(USER_AGENT, OAuth2Constant.USER_AGENT);
        return client.execute(getRequest);
    }

    public HttpResponse sendPostRequest(String locationURL) throws IOException {

        HttpPost postRequest = new HttpPost(locationURL);
        postRequest.setHeader(USER_AGENT, OAuth2Constant.USER_AGENT);
        return client.execute(postRequest);
    }

    private HttpResponse sendApprovalPostWithConsent(String sessionDataKeyConsent) throws IOException {

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair(CONSENT, APPROVE));
        urlParameters.add(new BasicNameValuePair(SCOPE_APPROVAL, APPROVE));
        urlParameters.add(new BasicNameValuePair("sessionDataKeyConsent", sessionDataKeyConsent));
        return sendPostRequestWithParameters(urlParameters, OAuth2Constant.APPROVAL_URL);
    }

    public HttpResponse sendLoginPost(String sessionDataKey) throws IOException {

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("username", USERNAME));
        urlParameters.add(new BasicNameValuePair("password", PASSWORD));
        urlParameters.add(new BasicNameValuePair("sessionDataKey", sessionDataKey));
        return sendPostRequestWithParameters(urlParameters, OAuth2Constant.COMMON_AUTH_URL);
    }

    public HttpResponse sendPostRequestWithParameters(List<NameValuePair> urlParameters, String url) throws
            IOException {

        HttpPost request = new HttpPost(url);
        request.setHeader(USER_AGENT, OAuth2Constant.USER_AGENT);
        request.setEntity(new UrlEncodedFormEntity(urlParameters));
        return client.execute(request);
    }

    private List<NameValuePair> getOIDCInitiationRequestParams() {

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("grantType", OAuth2Constant.OAUTH2_GRANT_TYPE_CODE));
        urlParameters.add(new BasicNameValuePair("consumerKey", oidcAppClientId));
        urlParameters.add(new BasicNameValuePair("callbackurl", OAuth2Constant.CALLBACK_URL));
        urlParameters.add(new BasicNameValuePair("authorizeEndpoint", OAuth2Constant.APPROVAL_URL));
        urlParameters.add(new BasicNameValuePair("authorize", OAuth2Constant.AUTHORIZE_PARAM));
        urlParameters.add(new BasicNameValuePair("scope", OAuth2Constant.OAUTH2_SCOPE_OPENID_WITH_INTERNAL_LOGIN));
        return urlParameters;
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

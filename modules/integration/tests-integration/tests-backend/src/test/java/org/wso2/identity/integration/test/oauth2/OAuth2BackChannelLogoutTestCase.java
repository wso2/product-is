/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
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

import org.apache.commons.lang.StringUtils;
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
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.identity.application.common.model.xsd.InboundAuthenticationConfig;
import org.wso2.carbon.identity.application.common.model.xsd.InboundAuthenticationRequestConfig;
import org.wso2.carbon.identity.application.common.model.xsd.Property;
import org.wso2.carbon.identity.application.common.model.xsd.ServiceProvider;
import org.wso2.carbon.identity.oauth.stub.dto.OAuthConsumerAppDTO;
import org.wso2.carbon.um.ws.api.stub.ClaimValue;
import org.wso2.identity.integration.common.clients.application.mgt.ApplicationManagementServiceClient;
import org.wso2.identity.integration.common.clients.oauth.OauthAdminClient;
import org.wso2.identity.integration.test.util.Utils;
import org.wso2.identity.integration.test.utils.CommonConstants;
import org.wso2.identity.integration.test.utils.DataExtractUtil;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.io.IOException;
import java.util.ArrayList;
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
    private String oidcAppClientId = "";
    private String oidcAppClientSecret = "";
    private OauthAdminClient adminClient;
    private ApplicationManagementServiceClient applicationManagementServiceClient;
    private String sessionDataKeyConsent;
    private String sessionDataKey;
    private final String CONSENT = "consent";
    private final String APPROVE = "approve";
    private final String SCOPE_APPROVAL = "scope-approval";
    private final String USER_AGENT = "User-Agent";
    private final String username;
    private final String userPassword;
    private final String activeTenant;

    private static final String USER_EMAIL = "abc@wso2.com";
    private static final String USERNAME = "testUser";
    private static final String PASSWORD = "pass123";

    @DataProvider(name = "configProvider")
    public static Object[][] configProvider() {
        return new Object[][]{{TestUserMode.SUPER_TENANT_ADMIN}, {TestUserMode.TENANT_ADMIN}};
    }

    @Factory(dataProvider = "configProvider")
    public OAuth2BackChannelLogoutTestCase(TestUserMode userMode) throws Exception {

        super.init(userMode);
        AutomationContext context = new AutomationContext("IDENTITY", userMode);
        this.username = context.getContextTenant().getTenantAdmin().getUserName();
        this.userPassword = context.getContextTenant().getTenantAdmin().getPassword();
        this.activeTenant = context.getContextTenant().getDomain();
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
        createOIDCApplication();
        createServiceProvider();

        remoteUSMServiceClient.addUser(USERNAME, PASSWORD, new String[]{"admin"},
                getUserClaims(), "default", true);

    }

    @AfterClass(alwaysRun = true)
    public void testCleanUp() throws Exception {

        removeApplications();

        remoteUSMServiceClient.deleteUser(USERNAME);
    }

    protected void init() throws Exception {

        super.init();
        adminClient = new OauthAdminClient(backendURL, sessionCookie);
        applicationManagementServiceClient = new ApplicationManagementServiceClient(sessionCookie, backendURL, null);
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

        OAuthConsumerAppDTO appDTO = new OAuthConsumerAppDTO();
        appDTO.setApplicationName(OIDC_APP_NAME);
        appDTO.setCallbackUrl(OAuth2Constant.CALLBACK_URL);
        appDTO.setOAuthVersion(OAuth2Constant.OAUTH_VERSION_2);
        appDTO.setGrantTypes(OAuth2Constant.OAUTH2_GRANT_TYPE_AUTHORIZATION_CODE);
        appDTO.setBackChannelLogoutUrl("http://localhost:" + DEFAULT_TOMCAT_PORT + "/playground2/bclogout");

        adminClient.registerOAuthApplicationData(appDTO);
        OAuthConsumerAppDTO createdApp = adminClient.getOAuthAppByName(OIDC_APP_NAME);
        Assert.assertNotNull(createdApp, "Adding OIDC app failed.");
        oidcAppClientId = createdApp.getOauthConsumerKey();
        oidcAppClientSecret = createdApp.getOauthConsumerSecret();
    }

    private void removeApplications() throws Exception {

        adminClient.removeOAuthApplicationData(oidcAppClientId);
        applicationManagementServiceClient.deleteApplication(OIDC_APP_NAME);
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

        List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
        urlParameters.add(new BasicNameValuePair("grantType", OAuth2Constant.OAUTH2_GRANT_TYPE_CODE));
        urlParameters.add(new BasicNameValuePair("consumerKey", oidcAppClientId));
        urlParameters.add(new BasicNameValuePair("callbackurl", OAuth2Constant.CALLBACK_URL));
        urlParameters.add(new BasicNameValuePair("authorizeEndpoint", OAuth2Constant.APPROVAL_URL));
        urlParameters.add(new BasicNameValuePair("authorize", OAuth2Constant.AUTHORIZE_PARAM));
        urlParameters.add(new BasicNameValuePair("scope", OAuth2Constant.OAUTH2_SCOPE_OPENID_WITH_INTERNAL_LOGIN));
        return urlParameters;
    }

    private ServiceProvider createServiceProvider() throws Exception {

        ServiceProvider serviceProvider = new ServiceProvider();
        serviceProvider.setApplicationName(OIDC_APP_NAME);
        serviceProvider.setManagementApp(true);
        applicationManagementServiceClient.createApplication(serviceProvider);
        serviceProvider = applicationManagementServiceClient.getApplication(OIDC_APP_NAME);

        InboundAuthenticationRequestConfig requestConfig = new InboundAuthenticationRequestConfig();
        requestConfig.setInboundAuthKey(oidcAppClientId);
        requestConfig.setInboundAuthType("oauth2");
        if (StringUtils.isNotBlank(oidcAppClientSecret)) {
            Property property = new Property();
            property.setName("oauthConsumerSecret");
            property.setValue(oidcAppClientSecret);
            Property[] properties = {property};
            requestConfig.setProperties(properties);
        }

        InboundAuthenticationConfig inboundAuthenticationConfig = new InboundAuthenticationConfig();
        inboundAuthenticationConfig
                .setInboundAuthenticationRequestConfigs(new InboundAuthenticationRequestConfig[]{requestConfig});
        serviceProvider.setInboundAuthenticationConfig(inboundAuthenticationConfig);
        applicationManagementServiceClient.updateApplicationData(serviceProvider);
        return serviceProvider;
    }

    protected ClaimValue[] getUserClaims() {

        ClaimValue[] claimValues = new ClaimValue[1];
        ClaimValue email = new ClaimValue();
        email.setClaimURI(EMAIL_CLAIM_URI);
        email.setValue(USER_EMAIL);
        claimValues[0] = email;
        return claimValues;
    }
}

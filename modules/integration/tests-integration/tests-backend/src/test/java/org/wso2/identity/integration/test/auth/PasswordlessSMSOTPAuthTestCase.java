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

package org.wso2.identity.integration.test.auth;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Lookup;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.cookie.CookieSpecProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.impl.cookie.RFC6265CookieSpecProvider;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.identity.integration.test.base.MockApplicationServer;
import org.wso2.identity.integration.test.base.MockSMSProvider;
import org.wso2.identity.integration.test.oidc.OIDCAbstractIntegrationTest;
import org.wso2.identity.integration.test.oidc.OIDCUtilTest;
import org.wso2.identity.integration.test.oidc.bean.OIDCApplication;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.AuthenticationSequence;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.AuthenticationStep;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.Authenticator;
import org.wso2.identity.integration.test.rest.api.server.notification.sender.v1.model.Properties;
import org.wso2.identity.integration.test.rest.api.server.notification.sender.v1.model.SMSSender;
import org.wso2.identity.integration.test.rest.api.user.common.model.Name;
import org.wso2.identity.integration.test.rest.api.user.common.model.PhoneNumbers;
import org.wso2.identity.integration.test.rest.api.user.common.model.UserObject;
import org.wso2.identity.integration.test.restclients.NotificationSenderRestClient;
import org.wso2.identity.integration.test.utils.DataExtractUtil;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.ACCESS_TOKEN_ENDPOINT;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.AUTHORIZATION_HEADER;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.AUTHORIZE_ENDPOINT_URL;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.OAUTH2_GRANT_TYPE_AUTHORIZATION_CODE;

/**
 * This class includes the test cases for passwordless SMS OTP authentication.
 */
public class PasswordlessSMSOTPAuthTestCase extends OIDCAbstractIntegrationTest {

    public static final String USERNAME = "passwordlessuser";
    public static final String PASSWORD = "Oidcsessiontestuser@123";
    public static final String MOBILE = "+941111111111";
    public static final String SMS_SENDER_REQUEST_FORMAT = "{\"content\": {{body}}, \"to\": {{mobile}} }";

    private HttpClient client;

    NotificationSenderRestClient notificationSenderRestClient;

    private OIDCApplication oidcApplication;
    private UserObject userObject;
    private String sessionDataKey;
    private String authorizationCode;

    private MockSMSProvider mockSMSProvider;
    private MockApplicationServer mockApplicationServer;

    private TestUserMode userMode;

    @Factory(dataProvider = "testExecutionContextProvider")
    public PasswordlessSMSOTPAuthTestCase(TestUserMode userMode) {

        this.userMode = userMode;
    }

    @DataProvider(name = "testExecutionContextProvider")
    public static Object[][] getTestExecutionContext() throws Exception {

        return new Object[][]{
                {TestUserMode.SUPER_TENANT_USER},
                {TestUserMode.TENANT_USER},
        };
    }

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.init(userMode);
        mockSMSProvider = new MockSMSProvider();
        mockSMSProvider.start();

        mockApplicationServer = new MockApplicationServer();
        mockApplicationServer.start();

        super.init();

        Lookup<CookieSpecProvider> cookieSpecRegistry = RegistryBuilder.<CookieSpecProvider>create()
                .register(CookieSpecs.DEFAULT, new RFC6265CookieSpecProvider())
                .build();
        RequestConfig requestConfig = RequestConfig.custom()
                .setCookieSpec(CookieSpecs.DEFAULT)
                .build();
        client = HttpClientBuilder.create()
                .setDefaultRequestConfig(requestConfig)
                .setDefaultCookieSpecRegistry(cookieSpecRegistry)
                .setRedirectStrategy(new LaxRedirectStrategy())
                .build();

        backendURL = backendURL.replace("services/", "");

        oidcApplication = initOIDCApplication();
        ApplicationModel applicationModel = initApplication();
        createApplication(applicationModel, oidcApplication);

        userObject = initUser();
        createUser(userObject);

        notificationSenderRestClient = new NotificationSenderRestClient(backendURL, tenantInfo);
        SMSSender smsSender = initSMSSender();
        notificationSenderRestClient.createSMSProvider(smsSender);
    }

    private static SMSSender initSMSSender() {

        SMSSender smsSender = new SMSSender();
        smsSender.setProvider(MockSMSProvider.SMS_SENDER_PROVIDER_TYPE);
        smsSender.setProviderURL(MockSMSProvider.SMS_SENDER_URL);
        smsSender.contentType(SMSSender.ContentTypeEnum.JSON);
        ArrayList<Properties> properties = new ArrayList<>();
        properties.add(new Properties().key("body").value(SMS_SENDER_REQUEST_FORMAT));
        smsSender.setProperties(properties);
        return smsSender;
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {

        deleteApplication(oidcApplication);
        deleteUser(userObject);
        notificationSenderRestClient.deleteSMSProvider();
        notificationSenderRestClient.closeHttpClient();
        restClient.closeHttpClient();
        scim2RestClient.closeHttpClient();

        mockSMSProvider.stop();
        mockApplicationServer.stop();
    }

    @Test(groups = "wso2.is", description = "Test passwordless authentication with SMS OTP")
    public void testPasswordlessAuthentication() throws Exception {

        sendAuthorizeRequest();
        performUserLogin();
        HttpResponse response = sendTokenRequestForCodeGrant();

        assertNotNull(response);
        assertEquals(response.getStatusLine().getStatusCode(), 200);
    }

    private void sendAuthorizeRequest() throws Exception {

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("response_type", OAuth2Constant.OAUTH2_GRANT_TYPE_CODE));
        urlParameters.add(new BasicNameValuePair("client_id", oidcApplication.getClientId()));
        urlParameters.add(new BasicNameValuePair("redirect_uri", oidcApplication.getCallBackURL()));

        urlParameters.add(new BasicNameValuePair("scope", "openid"));

        HttpResponse response = sendPostRequestWithParameters(client, urlParameters,
                getTenantQualifiedURL(AUTHORIZE_ENDPOINT_URL, tenantInfo.getDomain()));

        Map<String, Integer> keyPositionMap = new HashMap<>(1);
        keyPositionMap.put("name=\"sessionDataKey\"", 1);
        List<DataExtractUtil.KeyValue> keyValues = DataExtractUtil.extractDataFromResponse(response, keyPositionMap);
        assertNotNull(keyValues, "Session data key");

        sessionDataKey = keyValues.get(0).getValue();
        assertNotNull(sessionDataKey, "Session data key");
        EntityUtils.consume(response.getEntity());
    }

    private void performUserLogin() throws Exception {

        sendLoginPostForIdentifier(client, sessionDataKey, userObject.getUserName());
        HttpResponse response = sendLoginPostForOtp(client, sessionDataKey, mockSMSProvider.getOTP());
        EntityUtils.consume(response.getEntity());

        authorizationCode = mockApplicationServer.getAuthorizationCodeForApp(oidcApplication.getApplicationName());
        assertNotNull(authorizationCode);
    }

    private void sendLoginPostForIdentifier(HttpClient client, String sessionDataKey, String username)
            throws IOException {

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("username", username));
        urlParameters.add(new BasicNameValuePair("sessionDataKey", sessionDataKey));
        sendPostRequestWithParameters(client, urlParameters,
                getTenantQualifiedURL(OAuth2Constant.COMMON_AUTH_URL, tenantInfo.getDomain()));
    }

    private HttpResponse sendLoginPostForOtp(HttpClient client, String sessionDataKey, String otp)
            throws IOException {

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("OTPcode", otp));
        urlParameters.add(new BasicNameValuePair("sessionDataKey", sessionDataKey));
        return sendPostRequestWithParameters(client, urlParameters,
                getTenantQualifiedURL(OAuth2Constant.COMMON_AUTH_URL, tenantInfo.getDomain()));
    }

    private HttpResponse sendTokenRequestForCodeGrant() throws Exception {

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("code", authorizationCode));
        urlParameters.add(new BasicNameValuePair("grant_type", OAUTH2_GRANT_TYPE_AUTHORIZATION_CODE));
        urlParameters.add(new BasicNameValuePair("redirect_uri", oidcApplication.getCallBackURL()));
        urlParameters.add(new BasicNameValuePair("client_id", oidcApplication.getClientSecret()));

        urlParameters.add(new BasicNameValuePair("scope", "openid"));

        List<Header> headers = new ArrayList<>();
        headers.add(new BasicHeader(AUTHORIZATION_HEADER,
                OAuth2Constant.BASIC_HEADER + " " + getBase64EncodedString(oidcApplication.getClientId(),
                        oidcApplication.getClientSecret())));
        headers.add(new BasicHeader("Content-Type", "application/x-www-form-urlencoded"));
        headers.add(new BasicHeader("User-Agent", OAuth2Constant.USER_AGENT));

        return sendPostRequest(client, headers, urlParameters,
                getTenantQualifiedURL(ACCESS_TOKEN_ENDPOINT, tenantInfo.getDomain()));
    }

    private OIDCApplication initOIDCApplication() {

        OIDCApplication playgroundApp = new OIDCApplication(MockApplicationServer.Constants.APP1.NAME,
                OIDCUtilTest.playgroundAppOneAppContext,
                MockApplicationServer.Constants.APP1.CALLBACK_URL);
        return playgroundApp;
    }

    private ApplicationModel initApplication() {

        ApplicationModel application = new ApplicationModel();
        AuthenticationSequence authenticationSequence = new AuthenticationSequence();
        AuthenticationStep stepsItem = new AuthenticationStep();
        stepsItem.setId(1);
        Authenticator optionsItem = new Authenticator();
        optionsItem.setAuthenticator("sms-otp-authenticator");
        optionsItem.setIdp("LOCAL");
        stepsItem.addOptionsItem(optionsItem);
        authenticationSequence.addStepsItem(stepsItem);
        authenticationSequence.setType(AuthenticationSequence.TypeEnum.USER_DEFINED);
        authenticationSequence.setSubjectStepId(1);
        application.setAuthenticationSequence(authenticationSequence);
        return application;
    }

    protected UserObject initUser() {

        UserObject user = new UserObject();
        user.setUserName(USERNAME);
        user.setPassword(PASSWORD);
        user.setName(new Name().givenName(OIDCUtilTest.firstName).familyName(OIDCUtilTest.lastName));
        user.addPhoneNumbers(new PhoneNumbers().value(MOBILE).type("mobile"));
        return user;
    }
}

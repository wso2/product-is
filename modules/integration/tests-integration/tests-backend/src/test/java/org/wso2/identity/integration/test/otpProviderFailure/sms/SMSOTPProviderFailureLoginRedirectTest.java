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

package org.wso2.identity.integration.test.otpProviderFailure.sms;

import org.apache.http.HttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.identity.integration.test.base.MockSMSProvider;
import org.wso2.identity.integration.test.otpProviderFailure.AbstractOTPProviderFailureTestBase;
import org.wso2.identity.integration.test.otpProviderFailure.Constants;
import org.wso2.identity.integration.test.restclients.NotificationSenderRestClient;
import org.wso2.identity.integration.test.restclients.OAuth2RestClient;
import org.wso2.identity.integration.test.restclients.SCIM2RestClient;

public class SMSOTPProviderFailureLoginRedirectTest extends AbstractOTPProviderFailureTestBase {

    private static final String APP_NAME = "SMSOTPProviderFailureLoginRedirectApp";

    private String appId;
    private String clientId;
    private String userId;
    private SCIM2RestClient scim2RestClient;
    private MockSMSProvider mockSMSProvider;
    private NotificationSenderRestClient notificationSenderRestClient;
    private CloseableHttpClient sharedHttpClient;
    private String otpSessionDataKey;

    @Factory(dataProvider = "testExecutionContextProvider")
    public SMSOTPProviderFailureLoginRedirectTest(TestUserMode userMode) throws Exception {

        super.init(userMode);
    }

    @DataProvider(name = "testExecutionContextProvider")
    public static Object[][] getTestExecutionContext() {

        return new Object[][]{
                {TestUserMode.SUPER_TENANT_USER},
                {TestUserMode.TENANT_USER}
        };
    }

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        restClient = new OAuth2RestClient(serverURL, tenantInfo);
        scim2RestClient = new SCIM2RestClient(serverURL, tenantInfo);

        mockSMSProvider = new MockSMSProvider();
        mockSMSProvider.start();
        notificationSenderRestClient = new NotificationSenderRestClient(serverURL, tenantInfo);
        notificationSenderRestClient.createSMSProvider(buildSMSSender());

        appId = addTwoStepOIDCApp(APP_NAME, Constants.SMS_OTP_AUTHENTICATOR);
        clientId = getOIDCInboundDetailsOfApplication(appId).getClientId();
        userId = createTestUser(scim2RestClient);
    }

    @AfterClass(alwaysRun = true)
    public void testTearDown() throws Exception {

        disableSMSOTPSendingFailureNotification();

        if (appId != null) {
            deleteApp(appId);
        }
        if (userId != null) {
            scim2RestClient.deleteUser(userId);
        }

        notificationSenderRestClient.deleteSMSProvider();
        notificationSenderRestClient.closeHttpClient();
        mockSMSProvider.stop();

        restClient.closeHttpClient();
        scim2RestClient.closeHttpClient();
    }

    @Test(groups = "wso2.is")
    public void testSuccessfulLoginWithProviderWorking() throws Exception {

        mockSMSProvider.clearSmsContent();
        try (CloseableHttpClient http = createSessionHttpClient()) {
            String sessionDataKey = initiateRedirectFlow(http, clientId);
            HttpResponse credentialsResponse = sendCredentials(http, sessionDataKey,
                    Constants.TEST_USER_NAME, Constants.TEST_USER_PASSWORD);
            String otpPageUrl = followToOtpPage(http, credentialsResponse);
            Assert.assertNotNull(otpPageUrl, "OTP page URL should not be null.");
            Assert.assertFalse(otpPageUrl.contains(Constants.AUTH_FAILURE_PARAM),
                    "OTP page URL should not contain authFailure=true when provider is working.");
            Assert.assertFalse(otpPageUrl.contains(Constants.ERROR_CODE_PARAM),
                    "OTP page URL should not contain errorCode= when provider is working.");
            String html = getPageHtml(http, otpPageUrl);
            Assert.assertFalse(html.contains(Constants.OTP_ERROR_ELEMENT_ID),
                    "Page should not contain failed-msg element when provider is working.");
            String otpSdk = extractUrlParam(otpPageUrl, "sessionDataKey");
            String otp = mockSMSProvider.getOTP();
            Assert.assertNotNull(otp, "OTP should be received from SMS provider.");
            HttpResponse otpResponse = sendOTPCode(http, otpSdk, otp);
            String callbackUrl = followRedirectsToCallback(http, otpResponse);
            Assert.assertNotNull(callbackUrl, "Callback URL should not be null.");
            Assert.assertTrue(callbackUrl.contains("code="),
                    "Callback URL should contain authorization code.");
        }
    }

    @Test(groups = "wso2.is", dependsOnMethods = "testSuccessfulLoginWithProviderWorking")
    public void testProviderFailureHiddenByDefault() throws Exception {

        mockSMSProvider.stop();
        try (CloseableHttpClient http = createSessionHttpClient()) {
            String sessionDataKey = initiateRedirectFlow(http, clientId);
            HttpResponse credentialsResponse = sendCredentials(http, sessionDataKey,
                    Constants.TEST_USER_NAME, Constants.TEST_USER_PASSWORD);
            String otpPageUrl = followToOtpPage(http, credentialsResponse);
            Assert.assertNotNull(otpPageUrl, "OTP page URL should not be null.");
            Assert.assertFalse(otpPageUrl.contains(Constants.AUTH_FAILURE_PARAM),
                    "OTP page URL should not contain authFailure=true when config is disabled.");
            Assert.assertFalse(otpPageUrl.contains(Constants.ERROR_CODE_PARAM),
                    "OTP page URL should not contain errorCode= when config is disabled.");
            String html = getPageHtml(http, otpPageUrl);
            Assert.assertFalse(html.contains(Constants.OTP_ERROR_ELEMENT_ID),
                    "Page should not contain failed-msg element when config is disabled.");
        }
    }

    @Test(groups = "wso2.is", dependsOnMethods = "testProviderFailureHiddenByDefault")
    public void testProviderFailureShownWhenConfigEnabled() throws Exception {

        enableSMSOTPSendingFailureNotification();
        sharedHttpClient = createSessionHttpClient();
        String sessionDataKey = initiateRedirectFlow(sharedHttpClient, clientId);
        HttpResponse credentialsResponse = sendCredentials(sharedHttpClient, sessionDataKey,
                Constants.TEST_USER_NAME, Constants.TEST_USER_PASSWORD);
        String otpPageUrl = followToOtpPage(sharedHttpClient, credentialsResponse);
        Assert.assertNotNull(otpPageUrl, "OTP page URL should not be null.");
        Assert.assertTrue(otpPageUrl.contains(Constants.AUTH_FAILURE_PARAM),
                "OTP page URL should contain authFailure=true when config is enabled and provider fails.");
        Assert.assertTrue(otpPageUrl.contains(Constants.ERROR_CODE_PARAM),
                "OTP page URL should contain errorCode= when config is enabled and provider fails.");
        otpSessionDataKey = extractUrlParam(otpPageUrl, "sessionDataKey");
        Assert.assertNotNull(otpSessionDataKey, "OTP sessionDataKey should be extractable from URL.");
        String html = getPageHtml(sharedHttpClient, otpPageUrl);
        Assert.assertTrue(html.contains(Constants.OTP_ERROR_ELEMENT_ID),
                "Page should contain failed-msg element when config is enabled and provider fails.");
    }

    @Test(groups = "wso2.is", dependsOnMethods = "testProviderFailureShownWhenConfigEnabled")
    public void testLoginAfterRestoringProviderSameSession() throws Exception {

        mockSMSProvider.start();
        mockSMSProvider.clearSmsContent();
        HttpResponse resendResponse = resendOTP(sharedHttpClient, otpSessionDataKey);
        String otpPageUrl = followToOtpPage(sharedHttpClient, resendResponse);
        Assert.assertNotNull(otpPageUrl, "OTP page URL should not be null after resend.");
        Assert.assertFalse(otpPageUrl.contains(Constants.ERROR_CODE_PARAM),
                "OTP page URL should not contain errorCode= after provider is restored.");
        String otpSdk = extractUrlParam(otpPageUrl, "sessionDataKey");
        String otp = mockSMSProvider.getOTP();
        Assert.assertNotNull(otp, "OTP should be received after provider is restored.");
        HttpResponse otpResponse = sendOTPCode(sharedHttpClient, otpSdk, otp);
        String callbackUrl = followRedirectsToCallback(sharedHttpClient, otpResponse);
        Assert.assertNotNull(callbackUrl, "Callback URL should not be null.");
        Assert.assertTrue(callbackUrl.contains("code="),
                "Callback URL should contain authorization code.");
        sharedHttpClient.close();
    }

    @Test(groups = "wso2.is", dependsOnMethods = "testLoginAfterRestoringProviderSameSession")
    public void testFreshLoginAfterProviderRestored() throws Exception {

        mockSMSProvider.clearSmsContent();
        try (CloseableHttpClient http = createSessionHttpClient()) {
            String sessionDataKey = initiateRedirectFlow(http, clientId);
            HttpResponse credentialsResponse = sendCredentials(http, sessionDataKey,
                    Constants.TEST_USER_NAME, Constants.TEST_USER_PASSWORD);
            String otpPageUrl = followToOtpPage(http, credentialsResponse);
            Assert.assertNotNull(otpPageUrl, "OTP page URL should not be null.");
            Assert.assertFalse(otpPageUrl.contains(Constants.AUTH_FAILURE_PARAM),
                    "OTP page URL should not contain authFailure=true when provider is working.");
            Assert.assertFalse(otpPageUrl.contains(Constants.ERROR_CODE_PARAM),
                    "OTP page URL should not contain errorCode= when provider is working.");
            String otpSdk = extractUrlParam(otpPageUrl, "sessionDataKey");
            String otp = mockSMSProvider.getOTP();
            Assert.assertNotNull(otp, "OTP should be received from SMS provider.");
            HttpResponse otpResponse = sendOTPCode(http, otpSdk, otp);
            String callbackUrl = followRedirectsToCallback(http, otpResponse);
            Assert.assertNotNull(callbackUrl, "Callback URL should not be null.");
            Assert.assertTrue(callbackUrl.contains("code="),
                    "Callback URL should contain authorization code.");
        }
    }
}

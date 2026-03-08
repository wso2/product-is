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

package org.wso2.identity.integration.test.applicationNativeAuthentication;

import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.apache.http.HttpStatus;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.OtpLimitExceededError;
import org.wso2.identity.integration.test.base.MockSMSProvider;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationResponseModel;
import org.wso2.identity.integration.test.rest.api.server.notification.sender.v1.model.Properties;
import org.wso2.identity.integration.test.rest.api.server.notification.sender.v1.model.SMSSender;
import org.wso2.identity.integration.test.restclients.NotificationSenderRestClient;

import java.util.ArrayList;

import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.AUTH_DATA_CODE;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.FAIL_INCOMPLETE;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.FLOW_ID;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.FLOW_STATUS;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.INCOMPLETE;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.SUCCESS_COMPLETED;

/**
 * Integration tests for SMS-OTP where soft resend limits are enforced via adaptive auth script.
 */
public class SMSOTPSoftResendLimitNativeAuthTestCase extends AbstractOTPLimitNativeAuthTestCase {

    private static final String APP_NAME = "it-sms-otp-soft-resend-limit";
    private static final String OTP_AUTHENTICATOR = "sms-otp-authenticator";
    private static final String SMS_SENDER_REQUEST_FORMAT =
            "{\"content\": {{body}}, \"to\": {{mobile}} }";

    private final TestUserMode userMode;
    private String appId;
    private String appConsumerKey;
    private MockSMSProvider mockSMSProvider;
    private NotificationSenderRestClient notificationSenderRestClient;

    @Factory(dataProvider = "testExecutionContextProvider")
    public SMSOTPSoftResendLimitNativeAuthTestCase(TestUserMode userMode) {

        this.userMode = userMode;
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

        super.init(userMode);
        commonInit(userMode);
        mockSMSProvider = new MockSMSProvider();
        mockSMSProvider.start();
        notificationSenderRestClient = new NotificationSenderRestClient(backendURL, tenantInfo);

        // Ensure a clean state – delete the sender if it already exists.
        try {
            notificationSenderRestClient.deleteSMSProvider();
        } catch (Throwable ignored) {
            // Sender was never registered or was properly removed.
        }
        notificationSenderRestClient.createSMSProvider(buildSMSSender());
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {

        try {
            if (appId != null) {
                deleteApp(appId);
            }
            if (notificationSenderRestClient != null) {
                try {
                    notificationSenderRestClient.deleteSMSProvider();
                } catch (Throwable ignored) {
                    // Sender was never registered or was already removed.
                }
                notificationSenderRestClient.closeHttpClient();
            }
            if (mockSMSProvider != null) {
                mockSMSProvider.stop();
            }
        } finally {
            commonTearDown();
        }
    }

    @Test(groups = "wso2.is",
          description = "Register the SMS-OTP application with terminateOnResendLimitExceeded=false "
                  + "adaptive script.")
    public void testRegisterSMSOTPSoftResendApp() throws Exception {

        ApplicationResponseModel app = createOTPAppWithSoftResendLimit(APP_NAME, OTP_AUTHENTICATOR);
        Assert.assertNotNull(app, "SMS-OTP soft-resend app creation failed.");
        Assert.assertTrue(
                app.getAdvancedConfigurations().getEnableAPIBasedAuthentication(),
                "API-based authentication must be enabled on the app.");
        appId = app.getId();
        appConsumerKey = getOIDCInboundDetailsOfApplication(appId).getClientId();
        Assert.assertNotNull(appConsumerKey, "SMS-OTP soft-resend app client-id must not be null.");
    }

    @Test(groups = "wso2.is",
          description = "SMS OTP (soft-resend) – first wrong OTP returns HTTP 200 + FAIL_INCOMPLETE.",
          dependsOnMethods = "testRegisterSMSOTPSoftResendApp")
    public void testSMSOTPSoftResendFirstWrongOTPReturnsFAIL_INCOMPLETE() throws Exception {

        initiateAuthFlow(appConsumerKey);
        ExtractableResponse<Response> resp = submitOTPCode(WRONG_OTP, HttpStatus.SC_OK);
        String status = resp.jsonPath().getString(FLOW_STATUS);
        Assert.assertEquals(status, FAIL_INCOMPLETE,
                "SMS OTP soft-resend retry #1: expected FAIL_INCOMPLETE but got: " + status);
        Assert.assertEquals(resp.jsonPath().getString(FLOW_ID), flowId,
                "SMS OTP soft-resend retry #1: flowId must remain the same after a failed attempt.");
        refreshHrefAndAuthenticatorId(resp);
    }

    @Test(groups = "wso2.is",
          description = "SMS OTP (soft-resend) – second wrong OTP returns HTTP 400 + ABA-60013.",
          dependsOnMethods = "testSMSOTPSoftResendFirstWrongOTPReturnsFAIL_INCOMPLETE")
    public void testSMSOTPSoftResendSecondWrongOTPReturnsABA60013() {

        ExtractableResponse<Response> resp = submitOTPCode(WRONG_OTP, HttpStatus.SC_BAD_REQUEST);
        assertOTPLimitExceededPayload(resp, OtpLimitExceededError.RETRY_LIMIT_EXCEEDED);
    }

    @Test(groups = "wso2.is",
          description = "SMS OTP (soft-resend) – first resend returns HTTP 200 + INCOMPLETE.",
          dependsOnMethods = "testSMSOTPSoftResendSecondWrongOTPReturnsABA60013")
    public void testSMSOTPSoftResendFirstResendReturnsINCOMPLETE() throws Exception {

        initiateAuthFlow(appConsumerKey);
        ExtractableResponse<Response> resp = triggerResend(HttpStatus.SC_OK);
        String status = resp.jsonPath().getString(FLOW_STATUS);
        Assert.assertEquals(status, INCOMPLETE,
                "SMS OTP soft-resend resend #1: expected INCOMPLETE but got: " + status);
        Assert.assertEquals(resp.jsonPath().getString(FLOW_ID), flowId,
                "SMS OTP soft-resend resend #1: flowId must remain unchanged after a resend.");
        refreshHrefAndAuthenticatorId(resp);
    }

    @Test(groups = "wso2.is",
          description = "SMS OTP (soft-resend) – second resend returns HTTP 200 + FAIL_INCOMPLETE "
                  + "with ABA-60003 in nextStep.messages.",
          dependsOnMethods = "testSMSOTPSoftResendFirstResendReturnsINCOMPLETE")
    public void testSMSOTPSoftResendSecondResendReturnsFAIL_INCOMPLETEWithABA60003() {

        ExtractableResponse<Response> resp = triggerResend(HttpStatus.SC_OK);
        assertSoftResendLimitExceededPayload(resp);
        refreshHrefAndAuthenticatorId(resp);
    }

    @Test(groups = "wso2.is",
          description = "SMS OTP (soft-resend) – correct OTP on first attempt completes "
                  + "authentication with SUCCESS_COMPLETED.",
          dependsOnMethods = "testSMSOTPSoftResendSecondResendReturnsFAIL_INCOMPLETEWithABA60003")
    public void testSMSOTPSoftResendSuccessfulAuthOnFirstAttempt() throws Exception {

        initiateAuthFlow(appConsumerKey);

        String realOtp = mockSMSProvider.getOTP();
        Assert.assertNotNull(realOtp, "MockSMSProvider did not capture an OTP – check SMS sender config.");
        ExtractableResponse<Response> resp = submitOTPCode(realOtp, HttpStatus.SC_OK);
        String status = resp.jsonPath().getString(FLOW_STATUS);
        Assert.assertEquals(status, SUCCESS_COMPLETED,
                "SMS OTP soft-resend success (1st attempt): expected SUCCESS_COMPLETED but got: " + status);
        String authCode = resp.jsonPath().getString(AUTH_DATA_CODE);
        Assert.assertNotNull(authCode,
                "SMS OTP soft-resend: authData.code must be present in SUCCESS_COMPLETED response.");
        Assert.assertFalse(authCode.trim().isEmpty(),
                "SMS OTP soft-resend: authData.code must not be blank.");
    }

    @Test(groups = "wso2.is",
          description = "SMS OTP (soft-resend) – correct OTP after one wrong attempt completes "
                  + "authentication with SUCCESS_COMPLETED.",
          dependsOnMethods = "testSMSOTPSoftResendSuccessfulAuthOnFirstAttempt")
    public void testSMSOTPSoftResendSuccessfulAuthAfterOneRetry() throws Exception {

        initiateAuthFlow(appConsumerKey);
        String realOtp = mockSMSProvider.getOTP();
        Assert.assertNotNull(realOtp, "MockSMSProvider did not capture an OTP.");
        ExtractableResponse<Response> failResp = submitOTPCode(WRONG_OTP, HttpStatus.SC_OK);
        Assert.assertEquals(failResp.jsonPath().getString(FLOW_STATUS), FAIL_INCOMPLETE,
                "SMS OTP soft-resend retry: expected FAIL_INCOMPLETE after wrong OTP.");
        refreshHrefAndAuthenticatorId(failResp);
        ExtractableResponse<Response> successResp = submitOTPCode(realOtp, HttpStatus.SC_OK);
        Assert.assertEquals(successResp.jsonPath().getString(FLOW_STATUS), SUCCESS_COMPLETED,
                "SMS OTP soft-resend retry: expected SUCCESS_COMPLETED after correct OTP.");
        String authCode = successResp.jsonPath().getString(AUTH_DATA_CODE);
        Assert.assertNotNull(authCode,
                "SMS OTP soft-resend retry: authData.code must be present.");
        Assert.assertFalse(authCode.trim().isEmpty(),
                "SMS OTP soft-resend retry: authData.code must not be blank.");
    }

    @Test(groups = "wso2.is",
          description = "SMS OTP (soft-resend) – correct OTP after a resend completes "
                  + "authentication with SUCCESS_COMPLETED.",
          dependsOnMethods = "testSMSOTPSoftResendSuccessfulAuthAfterOneRetry")
    public void testSMSOTPSoftResendSuccessfulAuthAfterResend() throws Exception {

        initiateAuthFlow(appConsumerKey);
        ExtractableResponse<Response> resendResp = triggerResend(HttpStatus.SC_OK);
        Assert.assertEquals(resendResp.jsonPath().getString(FLOW_STATUS), INCOMPLETE,
                "SMS OTP soft-resend: expected INCOMPLETE after resend.");
        refreshHrefAndAuthenticatorId(resendResp);
        String newOtp = mockSMSProvider.getOTP();
        Assert.assertNotNull(newOtp, "MockSMSProvider did not capture an OTP after resend.");
        ExtractableResponse<Response> successResp = submitOTPCode(newOtp, HttpStatus.SC_OK);
        Assert.assertEquals(successResp.jsonPath().getString(FLOW_STATUS), SUCCESS_COMPLETED,
                "SMS OTP soft-resend: expected SUCCESS_COMPLETED after submitting resent OTP.");
        String authCode = successResp.jsonPath().getString(AUTH_DATA_CODE);
        Assert.assertNotNull(authCode,
                "SMS OTP soft-resend: authData.code must be present.");
        Assert.assertFalse(authCode.trim().isEmpty(),
                "SMS OTP soft-resend: authData.code must not be blank.");
    }

    /**
     * Helper method to build the SMS sender configuration for the tests.
     */
    private SMSSender buildSMSSender() {

        SMSSender smsSender = new SMSSender();
        smsSender.setProvider(MockSMSProvider.SMS_SENDER_PROVIDER_TYPE);
        smsSender.setProviderURL(MockSMSProvider.SMS_SENDER_URL);
        smsSender.contentType(SMSSender.ContentTypeEnum.JSON);
        ArrayList<Properties> properties = new ArrayList<>();
        properties.add(new Properties().key("body").value(SMS_SENDER_REQUEST_FORMAT));
        smsSender.setProperties(properties);
        return smsSender;
    }
}


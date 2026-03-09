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
 * Integration tests that validate SMS-OTP retry and resend limit enforcement via
 * adaptive-auth runtime params, using the API-based (native) authentication flow.
 */
public class SMSOTPLimitNativeAuthTestCase extends AbstractOTPLimitNativeAuthTestCase {

    private static final String APP_NAME = "it-sms-otp-first-factor";
    private static final String OTP_AUTHENTICATOR = "sms-otp-authenticator";
    private static final String SMS_SENDER_REQUEST_FORMAT =
            "{\"content\": {{body}}, \"to\": {{mobile}} }";

    private final TestUserMode userMode;
    private String appId;
    private String appConsumerKey;
    private MockSMSProvider mockSMSProvider;
    private NotificationSenderRestClient notificationSenderRestClient;

    @Factory(dataProvider = "testExecutionContextProvider")
    public SMSOTPLimitNativeAuthTestCase(TestUserMode userMode) {

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

        // Ensure a clean state by deleting any existing SMS sender before creating a new one.
        try {
            notificationSenderRestClient.deleteSMSProvider();
        } catch (Throwable ignored) {
            // Sender not present or already deleted – nothing to clean up.
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
                    // Sender was never registered or was already removed – nothing to do.
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
          description = "Register the SMS-OTP first-factor application with retry/resend "
                  + "limit adaptive script.")
    public void testRegisterSMSOTPApplication() throws Exception {

        ApplicationResponseModel app = createOTPApp(APP_NAME, OTP_AUTHENTICATOR);
        Assert.assertNotNull(app, "SMS-OTP app creation failed.");
        Assert.assertTrue(
                app.getAdvancedConfigurations().getEnableAPIBasedAuthentication(),
                "API-based authentication must be enabled on the SMS-OTP app.");

        appId = app.getId();
        appConsumerKey = getOIDCInboundDetailsOfApplication(appId).getClientId();
        Assert.assertNotNull(appConsumerKey, "SMS-OTP app client-id must not be null.");
    }

    @Test(groups = "wso2.is",
          description = "SMS OTP – first wrong OTP submit returns HTTP 200 + FAIL_INCOMPLETE.",
          dependsOnMethods = "testRegisterSMSOTPApplication")
    public void testSMSOTPFirstWrongOTPReturnsFAIL_INCOMPLETE() throws Exception {

        initiateAuthFlow(appConsumerKey);
        ExtractableResponse<Response> resp = submitOTPCode(WRONG_OTP, HttpStatus.SC_OK);
        String status = resp.jsonPath().getString(FLOW_STATUS);
        Assert.assertEquals(status, FAIL_INCOMPLETE,
                "SMS OTP retry #1: expected FAIL_INCOMPLETE but got: " + status);
        Assert.assertEquals(resp.jsonPath().getString(FLOW_ID), flowId,
                "SMS OTP retry #1: flowId must remain the same after a failed attempt.");
        refreshHrefAndAuthenticatorId(resp);
    }

    @Test(groups = "wso2.is",
          description = "SMS OTP – second wrong OTP submit returns HTTP 400 + ABA-60013 payload.",
          dependsOnMethods = "testSMSOTPFirstWrongOTPReturnsFAIL_INCOMPLETE")
    public void testSMSOTPSecondWrongOTPReturnsABA60013() {

        ExtractableResponse<Response> resp = submitOTPCode(WRONG_OTP, HttpStatus.SC_BAD_REQUEST);
        assertOTPLimitExceededPayload(resp, OtpLimitExceededError.RETRY_LIMIT_EXCEEDED);
    }

    @Test(groups = "wso2.is",
          description = "SMS OTP – first OTP resend returns HTTP 200 + INCOMPLETE.",
          dependsOnMethods = "testSMSOTPSecondWrongOTPReturnsABA60013")
    public void testSMSOTPFirstResendReturnsINCOMPLETE() throws Exception {

        initiateAuthFlow(appConsumerKey);
        ExtractableResponse<Response> resp = triggerResend(HttpStatus.SC_OK);
        String status = resp.jsonPath().getString(FLOW_STATUS);
        Assert.assertEquals(status, INCOMPLETE,
                "SMS OTP resend #1: expected INCOMPLETE but got: " + status);
        Assert.assertEquals(resp.jsonPath().getString(FLOW_ID), flowId,
                "SMS OTP resend #1: flowId must remain unchanged after a resend.");
        refreshHrefAndAuthenticatorId(resp);
    }

    @Test(groups = "wso2.is",
          description = "SMS OTP – second OTP resend returns HTTP 400 + ABA-60014 payload.",
          dependsOnMethods = "testSMSOTPFirstResendReturnsINCOMPLETE")
    public void testSMSOTPSecondResendReturnsABA60014() {

        ExtractableResponse<Response> resp = triggerResend(HttpStatus.SC_BAD_REQUEST);
        assertOTPLimitExceededPayload(resp, OtpLimitExceededError.RESEND_LIMIT_EXCEEDED);
    }

    @Test(groups = "wso2.is",
          description = "SMS OTP – submitting the correct OTP on first attempt completes "
                  + "authentication with SUCCESS_COMPLETED.",
          dependsOnMethods = "testSMSOTPSecondResendReturnsABA60014")
    public void testSMSOTPSuccessfulAuthOnFirstAttempt() throws Exception {

        initiateAuthFlow(appConsumerKey);
        String realOtp = mockSMSProvider.getOTP();
        Assert.assertNotNull(realOtp, "MockSMSProvider did not capture an OTP – check SMS sender config.");
        ExtractableResponse<Response> resp = submitOTPCode(realOtp, HttpStatus.SC_OK);
        String status = resp.jsonPath().getString(FLOW_STATUS);
        Assert.assertEquals(status, SUCCESS_COMPLETED,
                "SMS OTP success (1st attempt): expected SUCCESS_COMPLETED but got: " + status);
        String authCode = resp.jsonPath().getString(AUTH_DATA_CODE);
        Assert.assertNotNull(authCode,
                "SMS OTP success (1st attempt): authData.code must be present in SUCCESS_COMPLETED response.");
        Assert.assertFalse(authCode.trim().isEmpty(),
                "SMS OTP success (1st attempt): authData.code must not be blank.");
    }

    @Test(groups = "wso2.is",
          description = "SMS OTP – submitting the correct OTP after one wrong attempt "
                  + "completes authentication with SUCCESS_COMPLETED.",
          dependsOnMethods = "testSMSOTPSuccessfulAuthOnFirstAttempt")
    public void testSMSOTPSuccessfulAuthAfterOneRetry() throws Exception {

        initiateAuthFlow(appConsumerKey);
        String realOtp = mockSMSProvider.getOTP();
        Assert.assertNotNull(realOtp, "MockSMSProvider did not capture an OTP.");
        ExtractableResponse<Response> failResp = submitOTPCode(WRONG_OTP, HttpStatus.SC_OK);
        String failStatus = failResp.jsonPath().getString(FLOW_STATUS);
        Assert.assertEquals(failStatus, FAIL_INCOMPLETE,
                "SMS OTP retry: expected FAIL_INCOMPLETE after wrong OTP but got: " + failStatus);
        refreshHrefAndAuthenticatorId(failResp);
        ExtractableResponse<Response> successResp = submitOTPCode(realOtp, HttpStatus.SC_OK);
        String successStatus = successResp.jsonPath().getString(FLOW_STATUS);
        Assert.assertEquals(successStatus, SUCCESS_COMPLETED,
                "SMS OTP retry: expected SUCCESS_COMPLETED after correct OTP but got: " + successStatus);
        String authCode = successResp.jsonPath().getString(AUTH_DATA_CODE);
        Assert.assertNotNull(authCode,
                "SMS OTP retry: authData.code must be present in SUCCESS_COMPLETED response.");
        Assert.assertFalse(authCode.trim().isEmpty(),
                "SMS OTP retry: authData.code must not be blank.");
    }

    @Test(groups = "wso2.is",
          description = "SMS OTP – submitting the OTP received after a resend completes "
                  + "authentication with SUCCESS_COMPLETED.",
          dependsOnMethods = "testSMSOTPSuccessfulAuthAfterOneRetry")
    public void testSMSOTPSuccessfulAuthAfterResend() throws Exception {

        initiateAuthFlow(appConsumerKey);
        ExtractableResponse<Response> resendResp = triggerResend(HttpStatus.SC_OK);
        String resendStatus = resendResp.jsonPath().getString(FLOW_STATUS);
        Assert.assertEquals(resendStatus, INCOMPLETE,
                "SMS OTP resend: expected INCOMPLETE after resend but got: " + resendStatus);
        refreshHrefAndAuthenticatorId(resendResp);
        String newOtp = mockSMSProvider.getOTP();
        Assert.assertNotNull(newOtp, "MockSMSProvider did not capture an OTP after resend.");
        ExtractableResponse<Response> successResp = submitOTPCode(newOtp, HttpStatus.SC_OK);
        String successStatus = successResp.jsonPath().getString(FLOW_STATUS);
        Assert.assertEquals(successStatus, SUCCESS_COMPLETED,
                "SMS OTP resend: expected SUCCESS_COMPLETED after submitting resent OTP but got: "
                        + successStatus);
        String authCode = successResp.jsonPath().getString(AUTH_DATA_CODE);
        Assert.assertNotNull(authCode,
                "SMS OTP resend: authData.code must be present in SUCCESS_COMPLETED response.");
        Assert.assertFalse(authCode.trim().isEmpty(),
                "SMS OTP resend: authData.code must not be blank.");
    }

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


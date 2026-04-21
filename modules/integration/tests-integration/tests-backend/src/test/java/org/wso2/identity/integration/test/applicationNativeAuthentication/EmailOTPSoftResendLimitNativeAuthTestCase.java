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

import com.icegreen.greenmail.util.GreenMailUtil;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import jakarta.mail.Message;
import org.apache.http.HttpStatus;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.OtpLimitExceededError;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationResponseModel;
import org.wso2.identity.integration.test.util.Utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.AUTH_DATA_CODE;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.FAIL_INCOMPLETE;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.FLOW_ID;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.FLOW_STATUS;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.INCOMPLETE;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.MESSAGE;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.MESSAGES;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.MESSAGE_ID;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.NEXT_STEP;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.SUCCESS_COMPLETED;

/**
 * Integration tests for Email-OTP where Soft resend limits are enforced via adaptive auth script.
 */
public class EmailOTPSoftResendLimitNativeAuthTestCase extends AbstractOTPLimitNativeAuthTestCase {

    private static final String APP_NAME = "it-email-otp-soft-resend-limit";
    private static final String OTP_AUTHENTICATOR = "email-otp-authenticator";

    private final TestUserMode userMode;
    private String appId;
    private String appConsumerKey;

    @Factory(dataProvider = "testExecutionContextProvider")
    public EmailOTPSoftResendLimitNativeAuthTestCase(TestUserMode userMode) {

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

        Utils.getMailServer().purgeEmailFromAllMailboxes();
        super.init(userMode);
        commonInit(userMode);
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {

        try {
            if (appId != null) {
                deleteApp(appId);
            }
        } finally {
            Utils.getMailServer().purgeEmailFromAllMailboxes();
            commonTearDown();
        }
    }

    @Test(groups = "wso2.is",
          description = "Register the Email-OTP application with terminateOnResendLimitExceeded=false "
                  + "adaptive script.")
    public void testRegisterEmailOTPSoftResendApp() throws Exception {

        ApplicationResponseModel app = createOTPAppWithSoftResendLimit(APP_NAME, OTP_AUTHENTICATOR);
        Assert.assertNotNull(app, "Email-OTP soft-resend app creation failed.");
        Assert.assertTrue(
                app.getAdvancedConfigurations().getEnableAPIBasedAuthentication(),
                "API-based authentication must be enabled on the app.");

        appId = app.getId();
        appConsumerKey = getOIDCInboundDetailsOfApplication(appId).getClientId();
        Assert.assertNotNull(appConsumerKey, "Email-OTP soft-resend app client-id must not be null.");
    }

    @Test(groups = "wso2.is",
          description = "Email OTP (soft-resend) – first wrong OTP returns HTTP 200 + FAIL_INCOMPLETE.",
          dependsOnMethods = "testRegisterEmailOTPSoftResendApp")
    public void testEmailOTPSoftResendFirstWrongOTPReturnsFAIL_INCOMPLETE() throws Exception {

        initiateAuthFlow(appConsumerKey);

        Utils.getMailServer().purgeEmailFromAllMailboxes();
        ExtractableResponse<Response> resp = submitOTPCode(WRONG_OTP, HttpStatus.SC_OK);
        String status = resp.jsonPath().getString(FLOW_STATUS);
        Assert.assertEquals(status, FAIL_INCOMPLETE,
                "Email OTP soft-resend retry #1: expected FAIL_INCOMPLETE but got: " + status);
        Assert.assertEquals(resp.jsonPath().getString(FLOW_ID), flowId,
                "Email OTP soft-resend retry #1: flowId must remain the same after a failed attempt.");
        refreshHrefAndAuthenticatorId(resp);
    }

    @Test(groups = "wso2.is",
          description = "Email OTP (soft-resend) – second wrong OTP returns HTTP 400 + ABA-60013.",
          dependsOnMethods = "testEmailOTPSoftResendFirstWrongOTPReturnsFAIL_INCOMPLETE")
    public void testEmailOTPSoftResendSecondWrongOTPReturnsABA60013() {

        ExtractableResponse<Response> resp = submitOTPCode(WRONG_OTP, HttpStatus.SC_BAD_REQUEST);
        assertOTPLimitExceededPayload(resp, OtpLimitExceededError.RETRY_LIMIT_EXCEEDED);
    }

    @Test(groups = "wso2.is",
          description = "Email OTP (soft-resend) – first resend returns HTTP 200 + INCOMPLETE.",
          dependsOnMethods = "testEmailOTPSoftResendSecondWrongOTPReturnsABA60013")
    public void testEmailOTPSoftResendFirstResendReturnsINCOMPLETE() throws Exception {

        initiateAuthFlow(appConsumerKey);
        Utils.getMailServer().purgeEmailFromAllMailboxes();
        ExtractableResponse<Response> resp = triggerResend(HttpStatus.SC_OK);
        String status = resp.jsonPath().getString(FLOW_STATUS);
        Assert.assertEquals(status, INCOMPLETE,
                "Email OTP soft-resend resend #1: expected INCOMPLETE but got: " + status);
        Assert.assertEquals(resp.jsonPath().getString(FLOW_ID), flowId,
                "Email OTP soft-resend resend #1: flowId must remain unchanged after a resend.");
        refreshHrefAndAuthenticatorId(resp);
    }

    @Test(groups = "wso2.is",
          description = "Email OTP (soft-resend) – second resend returns HTTP 200 + FAIL_INCOMPLETE "
                  + "with ABA-60003 in nextStep.messages.",
          dependsOnMethods = "testEmailOTPSoftResendFirstResendReturnsINCOMPLETE")
    public void testEmailOTPSoftResendSecondResendReturnsFAIL_INCOMPLETEWithABA60003() {

        ExtractableResponse<Response> resp = triggerResend(HttpStatus.SC_OK);
        assertSoftResendLimitExceededPayload(resp);
        refreshHrefAndAuthenticatorId(resp);
    }

    @Test(groups = "wso2.is",
          description = "Email OTP (soft-resend) – correct OTP on first attempt completes "
                  + "authentication with SUCCESS_COMPLETED.",
          dependsOnMethods = "testEmailOTPSoftResendSecondResendReturnsFAIL_INCOMPLETEWithABA60003")
    public void testEmailOTPSoftResendSuccessfulAuthOnFirstAttempt() throws Exception {

        // Clean the mailbox to ensure we read the correct OTP email for this test, independent of previous tests.
        Utils.getMailServer().purgeEmailFromAllMailboxes();
        initiateAuthFlow(appConsumerKey);
        String realOtp = getOTPFromEmail();
        Assert.assertNotNull(realOtp, "GreenMail did not capture an OTP – check email sender config.");
        ExtractableResponse<Response> resp = submitOTPCode(realOtp, HttpStatus.SC_OK);
        String status = resp.jsonPath().getString(FLOW_STATUS);
        Assert.assertEquals(status, SUCCESS_COMPLETED,
                "Email OTP soft-resend success (1st attempt): expected SUCCESS_COMPLETED but got: " + status);
        String authCode = resp.jsonPath().getString(AUTH_DATA_CODE);
        Assert.assertNotNull(authCode,
                "Email OTP soft-resend: authData.code must be present in SUCCESS_COMPLETED response.");
        Assert.assertFalse(authCode.trim().isEmpty(),
                "Email OTP soft-resend: authData.code must not be blank.");
        Utils.getMailServer().purgeEmailFromAllMailboxes();
    }

    @Test(groups = "wso2.is",
          description = "Email OTP (soft-resend) – correct OTP after one wrong attempt completes "
                  + "authentication with SUCCESS_COMPLETED.",
          dependsOnMethods = "testEmailOTPSoftResendSuccessfulAuthOnFirstAttempt")
    public void testEmailOTPSoftResendSuccessfulAuthAfterOneRetry() throws Exception {

        // Clean the mailbox to ensure we read the correct OTP email for this test, independent of previous tests.
        Utils.getMailServer().purgeEmailFromAllMailboxes();
        initiateAuthFlow(appConsumerKey);
        String realOtp = getOTPFromEmail();
        Assert.assertNotNull(realOtp, "GreenMail did not capture an OTP.");
        ExtractableResponse<Response> failResp = submitOTPCode(WRONG_OTP, HttpStatus.SC_OK);
        Assert.assertEquals(failResp.jsonPath().getString(FLOW_STATUS), FAIL_INCOMPLETE,
                "Email OTP soft-resend retry: expected FAIL_INCOMPLETE after wrong OTP.");
        refreshHrefAndAuthenticatorId(failResp);
        ExtractableResponse<Response> successResp = submitOTPCode(realOtp, HttpStatus.SC_OK);
        Assert.assertEquals(successResp.jsonPath().getString(FLOW_STATUS), SUCCESS_COMPLETED,
                "Email OTP soft-resend retry: expected SUCCESS_COMPLETED after correct OTP.");
        String authCode = successResp.jsonPath().getString(AUTH_DATA_CODE);
        Assert.assertNotNull(authCode,
                "Email OTP soft-resend retry: authData.code must be present.");
        Assert.assertFalse(authCode.trim().isEmpty(),
                "Email OTP soft-resend retry: authData.code must not be blank.");
        Utils.getMailServer().purgeEmailFromAllMailboxes();
    }

    @Test(groups = "wso2.is",
          description = "Email OTP (soft-resend) – correct OTP after a resend completes "
                  + "authentication with SUCCESS_COMPLETED.",
          dependsOnMethods = "testEmailOTPSoftResendSuccessfulAuthAfterOneRetry")
    public void testEmailOTPSoftResendSuccessfulAuthAfterResend() throws Exception {

        // Clean the mailbox to ensure we read the correct OTP email for this test, independent of previous tests.
        Utils.getMailServer().purgeEmailFromAllMailboxes();
        initiateAuthFlow(appConsumerKey);
        String ignoredOTP = getOTPFromEmail();
        // Clean the mailbox to get rid of the OTP email sent during initiation.
        Utils.getMailServer().purgeEmailFromAllMailboxes();
        ExtractableResponse<Response> resendResp = triggerResend(HttpStatus.SC_OK);
        Assert.assertEquals(resendResp.jsonPath().getString(FLOW_STATUS), INCOMPLETE,
                "Email OTP soft-resend: expected INCOMPLETE after resend.");
        refreshHrefAndAuthenticatorId(resendResp);
        String newOtp = getOTPFromEmail();
        Assert.assertNotNull(newOtp, "GreenMail did not capture an OTP after resend.");
        ExtractableResponse<Response> successResp = submitOTPCode(newOtp, HttpStatus.SC_OK);
        Assert.assertEquals(successResp.jsonPath().getString(FLOW_STATUS), SUCCESS_COMPLETED,
                "Email OTP soft-resend: expected SUCCESS_COMPLETED after submitting resent OTP.");
        String authCode = successResp.jsonPath().getString(AUTH_DATA_CODE);
        Assert.assertNotNull(authCode,
                "Email OTP soft-resend: authData.code must be present.");
        Assert.assertFalse(authCode.trim().isEmpty(),
                "Email OTP soft-resend: authData.code must not be blank.");
        Utils.getMailServer().purgeEmailFromAllMailboxes();
    }

    /**
     * Waits for exactly one incoming email on GreenMail (up to 10 s), then extracts
     */
    private String getOTPFromEmail() {

        Assert.assertTrue(Utils.getMailServer().waitForIncomingEmail(10000, 1),
                "Timed out waiting for OTP email from GreenMail.");
        Message[] messages = Utils.getMailServer().getReceivedMessages();
        String body = GreenMailUtil.getBody(messages[0]).replaceAll("=\r?\n", "");
        Pattern pattern = Pattern.compile("\\s*<b>(\\d+)</b>");
        Matcher matcher = pattern.matcher(body);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    @Override
    protected void assertSoftResendLimitExceededPayload(ExtractableResponse<Response> resp) {

        String status = resp.jsonPath().getString(FLOW_STATUS);
        Assert.assertEquals(status, FAIL_INCOMPLETE,
                "Soft resend limit: expected FAIL_INCOMPLETE but got: " + status);

        Constants.OtpLimitExceededMessage expected = Constants.OtpLimitExceededMessage.RESEND_LIMIT_EXCEEDED;
        String messagesPrefix = NEXT_STEP + "." + MESSAGES + "[1]";
        String actualType = resp.jsonPath().getString(messagesPrefix + ".type");
        String actualMessageId = resp.jsonPath().getString(messagesPrefix + "." + MESSAGE_ID);
        String actualMessage = resp.jsonPath().getString(messagesPrefix + "." + MESSAGE);

        Assert.assertEquals(actualType, expected.getType(),
                "Soft resend limit: expected message type " + expected.getType() + " but got: " + actualType);
        Assert.assertEquals(actualMessageId, expected.getMessageId(),
                "Soft resend limit: expected messageId " + expected.getMessageId()
                        + " but got: " + actualMessageId);
        Assert.assertEquals(actualMessage, "resend.count.exceeded",
                "Soft resend limit: expected message '" + "resend.count.exceeded"
                        + "' but got: " + actualMessage);
    }
}


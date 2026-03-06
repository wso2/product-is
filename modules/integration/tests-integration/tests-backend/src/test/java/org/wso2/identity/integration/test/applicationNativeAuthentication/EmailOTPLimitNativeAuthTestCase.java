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
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationResponseModel;
import org.wso2.identity.integration.test.util.Utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.AUTH_DATA_CODE;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.ERROR_CODE_OTP_RESEND_LIMIT_EXCEEDED;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.ERROR_CODE_OTP_RETRY_LIMIT_EXCEEDED;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.ERROR_DESC_OTP_RESEND_LIMIT_EXCEEDED;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.ERROR_DESC_OTP_RETRY_LIMIT_EXCEEDED;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.ERROR_MSG_OTP_RESEND_LIMIT_EXCEEDED;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.ERROR_MSG_OTP_RETRY_LIMIT_EXCEEDED;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.FAIL_INCOMPLETE;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.FLOW_ID;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.FLOW_STATUS;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.INCOMPLETE;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.SUCCESS_COMPLETED;

/**
 * Integration tests that validate Email-OTP retry and resend limit enforcement via
 * adaptive-auth runtime params, using the API-based (native) authentication flow.
 *
 * <h3>Test cases</h3>
 * <ol>
 *   <li>Register the Email-OTP first-factor application.</li>
 *   <li><b>Retry limit exceeded (ABA-60013)</b>:
 *       wrong OTP #1 → HTTP 200 + FAIL_INCOMPLETE;
 *       wrong OTP #2 → HTTP 400 + ABA-60013 payload.
 *   </li>
 *   <li><b>Resend limit exceeded (ABA-60014)</b>:
 *       resend #1 → HTTP 200 + INCOMPLETE;
 *       resend #2 → HTTP 400 + ABA-60014 payload.
 *   </li>
 * </ol>
 *
 * <h3>Adaptive-auth script</h3>
 * See {@link AbstractOTPLimitNativeAuthTestCase#ADAPTIVE_SCRIPT_TEMPLATE} –
 * {@code maximumAllowedFailureAttempts=2}, {@code maximumAllowedResendAttempts=1}.
 */
public class EmailOTPLimitNativeAuthTestCase extends AbstractOTPLimitNativeAuthTestCase {

    private static final String APP_NAME          = "it-email-otp-first-factor";
    private static final String OTP_AUTHENTICATOR = "email-otp-authenticator";

    private String appId;
    private String appConsumerKey;

    // ══════════════════════════════════════════════════════════════════════════
    //  Setup / teardown
    // ══════════════════════════════════════════════════════════════════════════

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        Utils.getMailServer().purgeEmailFromAllMailboxes();
        super.init(TestUserMode.SUPER_TENANT_USER);
        commonInit();
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {

        if (appId != null) {
            deleteApp(appId);
        }
        Utils.getMailServer().purgeEmailFromAllMailboxes();
        commonTearDown();
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Application registration
    // ══════════════════════════════════════════════════════════════════════════

    @Test(groups = "wso2.is",
          description = "Register the Email-OTP first-factor application with retry/resend "
                  + "limit adaptive script.")
    public void testRegisterEmailOTPApplication() throws Exception {

        ApplicationResponseModel app = createOTPApp(APP_NAME, OTP_AUTHENTICATOR);
        Assert.assertNotNull(app, "Email-OTP app creation failed.");
        Assert.assertTrue(
                app.getAdvancedConfigurations().getEnableAPIBasedAuthentication(),
                "API-based authentication must be enabled on the Email-OTP app.");

        appId = app.getId();
        appConsumerKey = getOIDCInboundDetailsOfApplication(appId).getClientId();
        Assert.assertNotNull(appConsumerKey, "Email-OTP app client-id must not be null.");
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Test A – Retry limit exceeded (ABA-60013)
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Submit a wrong OTP for the <b>first</b> time.
     *
     * <p>Expected: HTTP 200 with {@code flowStatus = FAIL_INCOMPLETE} and the
     * same {@code flowId} echoed back so the client can retry.
     */
    @Test(groups = "wso2.is",
          description = "Email OTP – first wrong OTP submit returns HTTP 200 + FAIL_INCOMPLETE.",
          dependsOnMethods = "testRegisterEmailOTPApplication")
    public void testEmailOTPFirstWrongOTPReturnsFAIL_INCOMPLETE() throws Exception {

        // Initiate a fresh flow; IDF step is completed inside – this triggers OTP delivery.
        initiateAuthFlow(appConsumerKey);
        // Purge the real OTP email; we will deliberately submit a wrong OTP below.
        Utils.getMailServer().purgeEmailFromAllMailboxes();

        ExtractableResponse<Response> resp = submitOTPCode(WRONG_OTP, HttpStatus.SC_OK);

        String status = resp.jsonPath().getString(FLOW_STATUS);
        Assert.assertEquals(status, FAIL_INCOMPLETE,
                "Email OTP retry #1: expected FAIL_INCOMPLETE but got: " + status);
        Assert.assertEquals(resp.jsonPath().getString(FLOW_ID), flowId,
                "Email OTP retry #1: flowId must remain the same after a failed attempt.");

        refreshHrefAndAuthenticatorId(resp);
    }

    /**
     * Submit a wrong OTP for the <b>second</b> time (same flow).
     *
     * <p>Expected: HTTP 400 with the exact {@code ABA-60013} error payload.
     */
    @Test(groups = "wso2.is",
          description = "Email OTP – second wrong OTP submit returns HTTP 400 + ABA-60013 payload.",
          dependsOnMethods = "testEmailOTPFirstWrongOTPReturnsFAIL_INCOMPLETE")
    public void testEmailOTPSecondWrongOTPReturnsABA60013() {

        ExtractableResponse<Response> resp = submitOTPCode(WRONG_OTP, HttpStatus.SC_BAD_REQUEST);

        assertOTPLimitExceededPayload(resp,
                ERROR_CODE_OTP_RETRY_LIMIT_EXCEEDED,
                ERROR_MSG_OTP_RETRY_LIMIT_EXCEEDED,
                ERROR_DESC_OTP_RETRY_LIMIT_EXCEEDED);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Test B – Resend limit exceeded (ABA-60014)
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Trigger an OTP resend for the <b>first</b> time.
     *
     * <p>Expected: HTTP 200 with {@code flowStatus = INCOMPLETE} and the same
     * {@code flowId} preserved so the session remains alive.
     */
    @Test(groups = "wso2.is",
          description = "Email OTP – first OTP resend returns HTTP 200 + INCOMPLETE.",
          dependsOnMethods = "testEmailOTPSecondWrongOTPReturnsABA60013")
    public void testEmailOTPFirstResendReturnsINCOMPLETE() throws Exception {

        // Fresh flow for the resend scenario (independent of retry tests above).
        initiateAuthFlow(appConsumerKey);
        // Purge the initial OTP email; the resend will deliver a new one.
        Utils.getMailServer().purgeEmailFromAllMailboxes();

        ExtractableResponse<Response> resp = triggerResend(HttpStatus.SC_OK);

        String status = resp.jsonPath().getString(FLOW_STATUS);
        Assert.assertEquals(status, INCOMPLETE,
                "Email OTP resend #1: expected INCOMPLETE but got: " + status);
        Assert.assertEquals(resp.jsonPath().getString(FLOW_ID), flowId,
                "Email OTP resend #1: flowId must remain unchanged after a resend.");

        refreshHrefAndAuthenticatorId(resp);
    }

    /**
     * Trigger an OTP resend for the <b>second</b> time (same flow).
     *
     * <p>Expected: HTTP 400 with the exact {@code ABA-60014} error payload.
     */
    @Test(groups = "wso2.is",
          description = "Email OTP – second OTP resend returns HTTP 400 + ABA-60014 payload.",
          dependsOnMethods = "testEmailOTPFirstResendReturnsINCOMPLETE")
    public void testEmailOTPSecondResendReturnsABA60014() {

        ExtractableResponse<Response> resp = triggerResend(HttpStatus.SC_BAD_REQUEST);

        assertOTPLimitExceededPayload(resp,
                ERROR_CODE_OTP_RESEND_LIMIT_EXCEEDED,
                ERROR_MSG_OTP_RESEND_LIMIT_EXCEEDED,
                ERROR_DESC_OTP_RESEND_LIMIT_EXCEEDED);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Test C – Successful authentication on first OTP attempt
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Submit the correct OTP on the <b>very first</b> attempt.
     *
     * <p>Flow:
     * <ol>
     *   <li>Initiate auth flow (IDF step completed inside – real OTP is delivered via email).</li>
     *   <li>Read the OTP from GreenMail.</li>
     *   <li>Submit the correct OTP → expect HTTP 200 + {@code SUCCESS_COMPLETED}
     *       and a non-blank {@code authData.code} (authorization code).</li>
     * </ol>
     */
    @Test(groups = "wso2.is",
          description = "Email OTP – submitting the correct OTP on first attempt completes "
                  + "authentication with SUCCESS_COMPLETED.",
          dependsOnMethods = "testEmailOTPSecondResendReturnsABA60014")
    public void testEmailOTPSuccessfulAuthOnFirstAttempt() throws Exception {

        Utils.getMailServer().purgeEmailFromAllMailboxes();
        initiateAuthFlow(appConsumerKey);

        // Read the OTP that IS delivered to GreenMail.
        String realOtp = getOTPFromEmail();
        Assert.assertNotNull(realOtp, "GreenMail did not capture an OTP – check email sender config.");

        ExtractableResponse<Response> resp = submitOTPCode(realOtp, HttpStatus.SC_OK);

        String status = resp.jsonPath().getString(FLOW_STATUS);
        Assert.assertEquals(status, SUCCESS_COMPLETED,
                "Email OTP success (1st attempt): expected SUCCESS_COMPLETED but got: " + status);

        // An authorization code must be present in authData for the token exchange.
        String authCode = resp.jsonPath().getString(AUTH_DATA_CODE);
        Assert.assertNotNull(authCode,
                "Email OTP success (1st attempt): authData.code must be present in SUCCESS_COMPLETED response.");
        Assert.assertFalse(authCode.trim().isEmpty(),
                "Email OTP success (1st attempt): authData.code must not be blank.");

        Utils.getMailServer().purgeEmailFromAllMailboxes();
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Test D – Successful authentication after one wrong OTP (retry)
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Submit a wrong OTP once (triggering FAIL_INCOMPLETE), then submit the correct OTP.
     *
     * <p>Flow:
     * <ol>
     *   <li>Initiate auth flow (IDF completed inside – real OTP delivered via email).</li>
     *   <li>Read the real OTP from GreenMail before the first (wrong) submit.</li>
     *   <li>Submit {@link #WRONG_OTP} → expect HTTP 200 + FAIL_INCOMPLETE.</li>
     *   <li>Submit the real OTP on the same flow → expect HTTP 200 + {@code SUCCESS_COMPLETED}
     *       and a non-blank {@code authData.code}.</li>
     * </ol>
     */
    @Test(groups = "wso2.is",
          description = "Email OTP – submitting the correct OTP after one wrong attempt "
                  + "completes authentication with SUCCESS_COMPLETED.",
          dependsOnMethods = "testEmailOTPSuccessfulAuthOnFirstAttempt")
    public void testEmailOTPSuccessfulAuthAfterOneRetry() throws Exception {

        Utils.getMailServer().purgeEmailFromAllMailboxes();
        initiateAuthFlow(appConsumerKey);

        // Capture the real OTP before the first (wrong) submit so we have it for the retry.
        String realOtp = getOTPFromEmail();
        Assert.assertNotNull(realOtp, "GreenMail did not capture an OTP.");

        // First attempt – deliberately wrong.
        ExtractableResponse<Response> failResp = submitOTPCode(WRONG_OTP, HttpStatus.SC_OK);
        String failStatus = failResp.jsonPath().getString(FLOW_STATUS);
        Assert.assertEquals(failStatus, FAIL_INCOMPLETE,
                "Email OTP retry: expected FAIL_INCOMPLETE after wrong OTP but got: " + failStatus);

        // Update href/authenticatorId from the FAIL_INCOMPLETE response.
        refreshHrefAndAuthenticatorId(failResp);

        // Second attempt – correct OTP on the same flow.
        ExtractableResponse<Response> successResp = submitOTPCode(realOtp, HttpStatus.SC_OK);
        String successStatus = successResp.jsonPath().getString(FLOW_STATUS);
        Assert.assertEquals(successStatus, SUCCESS_COMPLETED,
                "Email OTP retry: expected SUCCESS_COMPLETED after correct OTP but got: " + successStatus);

        String authCode = successResp.jsonPath().getString(AUTH_DATA_CODE);
        Assert.assertNotNull(authCode,
                "Email OTP retry: authData.code must be present in SUCCESS_COMPLETED response.");
        Assert.assertFalse(authCode.trim().isEmpty(),
                "Email OTP retry: authData.code must not be blank.");

        Utils.getMailServer().purgeEmailFromAllMailboxes();
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Test E – Successful authentication after OTP resend
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Trigger a resend (receiving a fresh OTP email), then submit the new OTP.
     *
     * <p>Flow:
     * <ol>
     *   <li>Initiate auth flow (IDF completed inside – initial OTP email delivered).</li>
     *   <li>Purge the initial OTP email so GreenMail is clean for the resend.</li>
     *   <li>Trigger OTP resend → expect HTTP 200 + INCOMPLETE.</li>
     *   <li>Read the <em>new</em> OTP from GreenMail (delivered by the resend).</li>
     *   <li>Submit the new OTP → expect HTTP 200 + {@code SUCCESS_COMPLETED}
     *       and a non-blank {@code authData.code}.</li>
     * </ol>
     */
    @Test(groups = "wso2.is",
          description = "Email OTP – submitting the OTP received after a resend completes "
                  + "authentication with SUCCESS_COMPLETED.",
          dependsOnMethods = "testEmailOTPSuccessfulAuthAfterOneRetry")
    public void testEmailOTPSuccessfulAuthAfterResend() throws Exception {

        Utils.getMailServer().purgeEmailFromAllMailboxes();
        initiateAuthFlow(appConsumerKey);
        // Purge the initial OTP email so the next getOTPFromEmail() picks up the resent one.
        Utils.getMailServer().purgeEmailFromAllMailboxes();

        // Trigger one resend; IS will deliver a new OTP email.
        ExtractableResponse<Response> resendResp = triggerResend(HttpStatus.SC_OK);
        String resendStatus = resendResp.jsonPath().getString(FLOW_STATUS);
        Assert.assertEquals(resendStatus, INCOMPLETE,
                "Email OTP resend: expected INCOMPLETE after resend but got: " + resendStatus);

        // Update href/authenticatorId from the INCOMPLETE response.
        refreshHrefAndAuthenticatorId(resendResp);

        // Read the freshly delivered OTP from the resend email.
        String newOtp = getOTPFromEmail();
        Assert.assertNotNull(newOtp, "GreenMail did not capture an OTP after resend.");

        // Submit the new OTP on the same flow.
        ExtractableResponse<Response> successResp = submitOTPCode(newOtp, HttpStatus.SC_OK);
        String successStatus = successResp.jsonPath().getString(FLOW_STATUS);
        Assert.assertEquals(successStatus, SUCCESS_COMPLETED,
                "Email OTP resend: expected SUCCESS_COMPLETED after submitting resent OTP but got: "
                        + successStatus);

        String authCode = successResp.jsonPath().getString(AUTH_DATA_CODE);
        Assert.assertNotNull(authCode,
                "Email OTP resend: authData.code must be present in SUCCESS_COMPLETED response.");
        Assert.assertFalse(authCode.trim().isEmpty(),
                "Email OTP resend: authData.code must not be blank.");

        Utils.getMailServer().purgeEmailFromAllMailboxes();
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  OTP extraction helper
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Waits for exactly one incoming email on GreenMail (up to 10 s), then extracts
     * the 6-digit OTP from the {@code <b>NNNNNN</b>} pattern in the email body.
     *
     * <p>Follows the same pattern as {@code ApplicationNativeAuthentication2FATestCase}.
     *
     * @return the OTP string, or {@code null} if the pattern was not found.
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
}


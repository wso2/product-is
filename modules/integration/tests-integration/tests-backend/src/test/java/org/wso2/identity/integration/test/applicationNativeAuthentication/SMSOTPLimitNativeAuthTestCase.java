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
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.identity.integration.test.base.MockSMSProvider;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationResponseModel;
import org.wso2.identity.integration.test.rest.api.server.notification.sender.v1.model.Properties;
import org.wso2.identity.integration.test.rest.api.server.notification.sender.v1.model.SMSSender;
import org.wso2.identity.integration.test.restclients.NotificationSenderRestClient;

import java.util.ArrayList;

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
 * Integration tests that validate SMS-OTP retry and resend limit enforcement via
 * adaptive-auth runtime params, using the API-based (native) authentication flow.
 *
 * <h3>Test cases</h3>
 * <ol>
 *   <li>Register the SMS-OTP first-factor application.</li>
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
 * <h3>SMS delivery</h3>
 * A {@link MockSMSProvider} (WireMock HTTPS on port 8090) intercepts the outbound
 * SMS and exposes the OTP via {@link MockSMSProvider#getOTP()}.  The SMS notification
 * sender registered with IS points at this mock.
 *
 * <h3>Adaptive-auth script</h3>
 * See {@link AbstractOTPLimitNativeAuthTestCase#ADAPTIVE_SCRIPT_TEMPLATE} –
 * {@code maximumAllowedFailureAttempts=2}, {@code maximumAllowedResendAttempts=1}.
 */
public class SMSOTPLimitNativeAuthTestCase extends AbstractOTPLimitNativeAuthTestCase {

    private static final String APP_NAME          = "it-sms-otp-first-factor";
    private static final String OTP_AUTHENTICATOR = "sms-otp-authenticator";

    /** Request body template forwarded to MockSMSProvider. */
    private static final String SMS_SENDER_REQUEST_FORMAT =
            "{\"content\": {{body}}, \"to\": {{mobile}} }";

    private String appId;
    private String appConsumerKey;

    private MockSMSProvider mockSMSProvider;
    private NotificationSenderRestClient notificationSenderRestClient;

    // ══════════════════════════════════════════════════════════════════════════
    //  Setup / teardown
    // ══════════════════════════════════════════════════════════════════════════

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.init(TestUserMode.SUPER_TENANT_USER);
        commonInit(); // strips "services/" from backendURL, creates user, resets IDP cache

        // Start the WireMock HTTPS mock that captures SMS OTPs sent by IS.
        mockSMSProvider = new MockSMSProvider();
        mockSMSProvider.start();

        // Register the mock SMS notification sender with IS.
        // Use backendURL (already stripped of "services/") – matches PasswordlessSMSOTPAuthTestCase.
        notificationSenderRestClient = new NotificationSenderRestClient(backendURL, tenantInfo);

        // Defensive cleanup: handle leftover sender from a previously crashed run.
        try {
            notificationSenderRestClient.deleteSMSProvider();
        } catch (Exception ignored) {
            // Sender not present – nothing to clean up.
        }
        notificationSenderRestClient.createSMSProvider(buildSMSSender());
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {

        if (appId != null) {
            deleteApp(appId);
        }

        notificationSenderRestClient.deleteSMSProvider();
        notificationSenderRestClient.closeHttpClient();

        if (mockSMSProvider != null) {
            mockSMSProvider.stop();
        }

        commonTearDown();
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Application registration
    // ══════════════════════════════════════════════════════════════════════════

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
          description = "SMS OTP – first wrong OTP submit returns HTTP 200 + FAIL_INCOMPLETE.",
          dependsOnMethods = "testRegisterSMSOTPApplication")
    public void testSMSOTPFirstWrongOTPReturnsFAIL_INCOMPLETE() throws Exception {

        // Initiate a fresh flow; IDF step is completed inside – this triggers OTP delivery.
        initiateAuthFlow(appConsumerKey);

        ExtractableResponse<Response> resp = submitOTPCode(WRONG_OTP, HttpStatus.SC_OK);

        String status = resp.jsonPath().getString(FLOW_STATUS);
        Assert.assertEquals(status, FAIL_INCOMPLETE,
                "SMS OTP retry #1: expected FAIL_INCOMPLETE but got: " + status);
        Assert.assertEquals(resp.jsonPath().getString(FLOW_ID), flowId,
                "SMS OTP retry #1: flowId must remain the same after a failed attempt.");

        refreshHrefAndAuthenticatorId(resp);
    }

    /**
     * Submit a wrong OTP for the <b>second</b> time (same flow).
     *
     * <p>Expected: HTTP 400 with the exact {@code ABA-60013} error payload.
     */
    @Test(groups = "wso2.is",
          description = "SMS OTP – second wrong OTP submit returns HTTP 400 + ABA-60013 payload.",
          dependsOnMethods = "testSMSOTPFirstWrongOTPReturnsFAIL_INCOMPLETE")
    public void testSMSOTPSecondWrongOTPReturnsABA60013() {

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
          description = "SMS OTP – first OTP resend returns HTTP 200 + INCOMPLETE.",
          dependsOnMethods = "testSMSOTPSecondWrongOTPReturnsABA60013")
    public void testSMSOTPFirstResendReturnsINCOMPLETE() throws Exception {

        // Fresh flow for the resend scenario (independent of retry tests above).
        initiateAuthFlow(appConsumerKey);

        ExtractableResponse<Response> resp = triggerResend(HttpStatus.SC_OK);

        String status = resp.jsonPath().getString(FLOW_STATUS);
        Assert.assertEquals(status, INCOMPLETE,
                "SMS OTP resend #1: expected INCOMPLETE but got: " + status);
        Assert.assertEquals(resp.jsonPath().getString(FLOW_ID), flowId,
                "SMS OTP resend #1: flowId must remain unchanged after a resend.");

        refreshHrefAndAuthenticatorId(resp);
    }

    /**
     * Trigger an OTP resend for the <b>second</b> time (same flow).
     *
     * <p>Expected: HTTP 400 with the exact {@code ABA-60014} error payload.
     */
    @Test(groups = "wso2.is",
          description = "SMS OTP – second OTP resend returns HTTP 400 + ABA-60014 payload.",
          dependsOnMethods = "testSMSOTPFirstResendReturnsINCOMPLETE")
    public void testSMSOTPSecondResendReturnsABA60014() {

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
     *   <li>Initiate auth flow (IDF step completed inside).</li>
     *   <li>Read the real OTP delivered to {@link MockSMSProvider}.</li>
     *   <li>Submit the correct OTP → expect HTTP 200 + {@code SUCCESS_COMPLETED}
     *       and a non-blank {@code authData.code} (authorization code).</li>
     * </ol>
     */
    @Test(groups = "wso2.is",
          description = "SMS OTP – submitting the correct OTP on first attempt completes "
                  + "authentication with SUCCESS_COMPLETED.",
          dependsOnMethods = "testSMSOTPSecondResendReturnsABA60014")
    public void testSMSOTPSuccessfulAuthOnFirstAttempt() throws Exception {

        initiateAuthFlow(appConsumerKey);

        // Read the OTP that IS delivered to the mock SMS provider.
        String realOtp = mockSMSProvider.getOTP();
        Assert.assertNotNull(realOtp, "MockSMSProvider did not capture an OTP – check SMS sender config.");

        ExtractableResponse<Response> resp = submitOTPCode(realOtp, HttpStatus.SC_OK);

        String status = resp.jsonPath().getString(FLOW_STATUS);
        Assert.assertEquals(status, SUCCESS_COMPLETED,
                "SMS OTP success (1st attempt): expected SUCCESS_COMPLETED but got: " + status);

        // An authorization code must be present in authData for the token exchange.
        String authCode = resp.jsonPath().getString(AUTH_DATA_CODE);
        Assert.assertNotNull(authCode,
                "SMS OTP success (1st attempt): authData.code must be present in SUCCESS_COMPLETED response.");
        Assert.assertFalse(authCode.trim().isEmpty(),
                "SMS OTP success (1st attempt): authData.code must not be blank.");
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Test D – Successful authentication after one wrong OTP (retry)
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Submit a wrong OTP once (triggering FAIL_INCOMPLETE), then submit the correct OTP.
     *
     * <p>Flow:
     * <ol>
     *   <li>Initiate auth flow (IDF completed inside).</li>
     *   <li>Submit {@link #WRONG_OTP} → expect HTTP 200 + FAIL_INCOMPLETE.</li>
     *   <li>Submit the real OTP (same flow) → expect HTTP 200 + {@code SUCCESS_COMPLETED}
     *       and a non-blank {@code authData.code}.</li>
     * </ol>
     *
     * <p>This validates that the retry mechanism works end-to-end: the session is kept
     * alive after the first failure and the correct OTP is accepted on the retry.
     */
    @Test(groups = "wso2.is",
          description = "SMS OTP – submitting the correct OTP after one wrong attempt "
                  + "completes authentication with SUCCESS_COMPLETED.",
          dependsOnMethods = "testSMSOTPSuccessfulAuthOnFirstAttempt")
    public void testSMSOTPSuccessfulAuthAfterOneRetry() throws Exception {

        initiateAuthFlow(appConsumerKey);

        // Capture the real OTP before the first (wrong) submit so we have it for the retry.
        String realOtp = mockSMSProvider.getOTP();
        Assert.assertNotNull(realOtp, "MockSMSProvider did not capture an OTP.");

        // First attempt – deliberately wrong.
        ExtractableResponse<Response> failResp = submitOTPCode(WRONG_OTP, HttpStatus.SC_OK);
        String failStatus = failResp.jsonPath().getString(FLOW_STATUS);
        Assert.assertEquals(failStatus, FAIL_INCOMPLETE,
                "SMS OTP retry: expected FAIL_INCOMPLETE after wrong OTP but got: " + failStatus);

        // Update href/authenticatorId from the FAIL_INCOMPLETE response.
        refreshHrefAndAuthenticatorId(failResp);

        // Second attempt – correct OTP on the same flow.
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

    // ══════════════════════════════════════════════════════════════════════════
    //  Test E – Successful authentication after OTP resend
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Trigger a resend (receiving a fresh OTP), then submit the new OTP.
     *
     * <p>Flow:
     * <ol>
     *   <li>Initiate auth flow (IDF completed inside).</li>
     *   <li>Trigger OTP resend → expect HTTP 200 + INCOMPLETE.</li>
     *   <li>Read the <em>new</em> OTP from {@link MockSMSProvider} (overwritten by the resend).</li>
     *   <li>Submit the new OTP → expect HTTP 200 + {@code SUCCESS_COMPLETED}
     *       and a non-blank {@code authData.code}.</li>
     * </ol>
     *
     * <p>This validates that the OTP delivered on a resend is valid and can complete
     * the authentication successfully.
     */
    @Test(groups = "wso2.is",
          description = "SMS OTP – submitting the OTP received after a resend completes "
                  + "authentication with SUCCESS_COMPLETED.",
          dependsOnMethods = "testSMSOTPSuccessfulAuthAfterOneRetry")
    public void testSMSOTPSuccessfulAuthAfterResend() throws Exception {

        initiateAuthFlow(appConsumerKey);

        // Trigger one resend; IS will deliver a new OTP to MockSMSProvider.
        ExtractableResponse<Response> resendResp = triggerResend(HttpStatus.SC_OK);
        String resendStatus = resendResp.jsonPath().getString(FLOW_STATUS);
        Assert.assertEquals(resendStatus, INCOMPLETE,
                "SMS OTP resend: expected INCOMPLETE after resend but got: " + resendStatus);

        // Update href/authenticatorId from the INCOMPLETE response.
        refreshHrefAndAuthenticatorId(resendResp);

        // Read the freshly delivered OTP (the resend overwrites the previous value).
        String newOtp = mockSMSProvider.getOTP();
        Assert.assertNotNull(newOtp, "MockSMSProvider did not capture an OTP after resend.");

        // Submit the new OTP on the same flow.
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

    // ══════════════════════════════════════════════════════════════════════════
    //  SMS sender helper
    // ══════════════════════════════════════════════════════════════════════════

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


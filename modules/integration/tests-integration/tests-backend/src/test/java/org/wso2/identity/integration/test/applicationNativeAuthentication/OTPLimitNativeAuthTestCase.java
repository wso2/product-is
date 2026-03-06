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

import io.restassured.http.ContentType;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;
import org.apache.commons.lang.ArrayUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Lookup;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.cookie.CookieSpecProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.RFC6265CookieSpecProvider;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.identity.application.common.model.idp.xsd.FederatedAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.idp.xsd.IdentityProvider;
import org.wso2.identity.integration.common.clients.Idp.IdentityProviderMgtServiceClient;
import org.wso2.identity.integration.test.base.MockSMSProvider;
import org.wso2.identity.integration.test.oauth2.OAuth2ServiceAbstractIntegrationTest;
import org.wso2.identity.integration.test.oidc.OIDCUtilTest;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.AdvancedApplicationConfiguration;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationResponseModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.AuthenticationSequence;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.AuthenticationStep;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.Authenticator;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.InboundProtocols;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.OpenIDConnectConfiguration;
import org.wso2.identity.integration.test.rest.api.server.notification.sender.v1.model.Properties;
import org.wso2.identity.integration.test.rest.api.server.notification.sender.v1.model.SMSSender;
import org.wso2.identity.integration.test.rest.api.user.common.model.Email;
import org.wso2.identity.integration.test.rest.api.user.common.model.Name;
import org.wso2.identity.integration.test.rest.api.user.common.model.PhoneNumbers;
import org.wso2.identity.integration.test.rest.api.user.common.model.UserObject;
import org.wso2.identity.integration.test.restclients.NotificationSenderRestClient;
import org.wso2.identity.integration.test.restclients.SCIM2RestClient;
import org.wso2.identity.integration.test.util.Utils;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.AUTHENTICATOR_ID;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.AUTHENTICATORS;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.CODE;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.CONTENT_TYPE_APPLICATION_JSON;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.DESCRIPTION;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.ERROR_CODE_OTP_RESEND_LIMIT_EXCEEDED;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.ERROR_CODE_OTP_RETRY_LIMIT_EXCEEDED;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.ERROR_DESC_OTP_RESEND_LIMIT_EXCEEDED;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.ERROR_DESC_OTP_RETRY_LIMIT_EXCEEDED;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.ERROR_MSG_OTP_RESEND_LIMIT_EXCEEDED;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.ERROR_MSG_OTP_RETRY_LIMIT_EXCEEDED;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.FAIL_INCOMPLETE;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.FLOW_ID;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.FLOW_STATUS;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.FLOW_TYPE;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.HREF;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.INCOMPLETE;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.LINKS;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.MESSAGE;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.METADATA;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.NEXT_STEP;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.PARAMS;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.PROMPT_TYPE;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.REQUIRED_PARAMS;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.RESPONSE_MODE;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.STEP_TYPE;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.TRACE_ID;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.USERNAME_PARAM;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.UTF_8;

/**
 * Integration tests that validate OTP retry and resend limit enforcement via adaptive-auth runtime
 * params for BOTH the Email-OTP and SMS-OTP authenticators used as the first authentication factor.
 *
 * <h3>Test cases</h3>
 * <ol>
 *   <li><b>Email OTP – Retry limit exceeded (ABA-60013)</b>:
 *       Wrong OTP #1 → HTTP 200 + FAIL_INCOMPLETE;
 *       Wrong OTP #2 → HTTP 400 + {@code ABA-60013} payload.
 *   </li>
 *   <li><b>Email OTP – Resend limit exceeded (ABA-60014)</b>:
 *       Resend #1 → HTTP 200 + INCOMPLETE;
 *       Resend #2 → HTTP 400 + {@code ABA-60014} payload.
 *   </li>
 *   <li><b>SMS OTP – Retry limit exceeded (ABA-60013)</b>: same flow over SMS channel.</li>
 *   <li><b>SMS OTP – Resend limit exceeded (ABA-60014)</b>: same flow over SMS channel.</li>
 * </ol>
 *
 * <h3>Adaptive-auth script params</h3>
 * <ul>
 *   <li>{@code enableRetryFromAuthenticator} = "true"</li>
 *   <li>{@code maximumAllowedFailureAttempts} = "2"  (so 1st wrong → FAIL_INCOMPLETE, 2nd → 400)</li>
 *   <li>{@code maximumAllowedResendAttempts} = "1"   (so 1st resend → INCOMPLETE, 2nd → 400)</li>
 *   <li>{@code terminateOnResendLimitExceeded} = "true"</li>
 * </ul>
 */
public class OTPLimitNativeAuthTestCase extends OAuth2ServiceAbstractIntegrationTest {

    // ── Test user ──────────────────────────────────────────────────────────────
    private static final String TEST_USER_NAME = "it_user_otp_limits";
    private static final String TEST_USER_PASSWORD = "User@123Otp!";
    private static final String TEST_USER_EMAIL = "it_user_otp_limits@example.com";
    private static final String TEST_USER_MOBILE = "+94771234567";

    // ── Application names ──────────────────────────────────────────────────────
    private static final String EMAIL_OTP_APP_NAME = "it-email-otp-first-factor";
    private static final String SMS_OTP_APP_NAME = "it-sms-otp-first-factor";

    // ── Authenticator names (as registered in IS) ──────────────────────────────
    private static final String EMAIL_OTP_AUTHENTICATOR = "email-otp-authenticator";
    private static final String SMS_OTP_AUTHENTICATOR = "sms-otp-authenticator";

    // ── SMS sender config (must point to MockSMSProvider) ─────────────────────
    private static final String SMS_SENDER_REQUEST_FORMAT = "{\"content\": {{body}}, \"to\": {{mobile}} }";

    // ── Adaptive-auth script template (one %s placeholder for authenticator key) ──
    /**
     * Adaptive script that configures OTP retry/resend limits as runtime params.
     * Params:
     *  [0] = authenticator key, e.g. "email-otp-authenticator" or "sms-otp-authenticator"
     */
    private static final String ADAPTIVE_SCRIPT_TEMPLATE =
            "var onLoginRequest = function(context) {\n" +
            "  executeStep(1, {\n" +
            "    authenticatorParams: {\n" +
            "      local: {\n" +
            "        \"%s\": {\n" +
            "          \"enableRetryFromAuthenticator\": \"true\",\n" +
            "          \"maximumAllowedFailureAttempts\": \"2\",\n" +
            "          \"maximumAllowedResendAttempts\": \"1\",\n" +
            "          \"terminateOnResendLimitExceeded\": \"true\"\n" +
            "        }\n" +
            "      }\n" +
            "    }\n" +
            "  }, {});\n" +
            "};\n";

    // ── Wrong OTP value used for retry tests ───────────────────────────────────
    private static final String WRONG_OTP = "000000";

    // ── Shared infrastructure ──────────────────────────────────────────────────
    private CloseableHttpClient client;
    private SCIM2RestClient scim2RestClient;
    private NotificationSenderRestClient notificationSenderRestClient;
    private MockSMSProvider mockSMSProvider;

    /**
     * Preserved copy of {@code backendURL} <em>before</em> stripping {@code "services/"}.
     * {@link IdentityProviderMgtServiceClient} is an Axis2 SOAP stub and requires the URL
     * to end with {@code "services/"}.  We strip the suffix from {@code backendURL} so that
     * {@link NotificationSenderRestClient} (REST) works correctly, but we must keep the
     * original value for SOAP clients.
     */
    private String soapBackendURL;

    // ── Per-suite state ────────────────────────────────────────────────────────
    private String userId;
    private String emailOtpAppId;
    private String emailOtpConsumerKey;
    private String smsOtpAppId;
    private String smsOtpConsumerKey;

    // ── Per-test flow state (reset before each test group) ────────────────────
    /** The active flowId for the current test scenario. */
    private String flowId;
    /** The authenticatorId selected for the current step. */
    private String authenticatorId;
    /** The URL to POST the next authentication step to. */
    private String href;

    // ══════════════════════════════════════════════════════════════════════════
    //  Setup / Teardown
    // ══════════════════════════════════════════════════════════════════════════

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        Utils.getMailServer().purgeEmailFromAllMailboxes();
        super.init(TestUserMode.SUPER_TENANT_USER);

        // Preserve the original Axis2/SOAP backendURL (ending with "services/") before
        // stripping the suffix.  SOAP clients (IdentityProviderMgtServiceClient etc.)
        // need the "services/" suffix; REST clients need it removed.
        soapBackendURL = backendURL;

        // Strip "services/" from backendURL – required by NotificationSenderRestClient
        // (matches the pattern used in PasswordlessSMSOTPAuthTestCase).
        backendURL = backendURL.replace("services/", "");

        Lookup<CookieSpecProvider> cookieSpecRegistry = RegistryBuilder.<CookieSpecProvider>create()
                .register(CookieSpecs.DEFAULT, new RFC6265CookieSpecProvider())
                .build();
        RequestConfig requestConfig = RequestConfig.custom()
                .setCookieSpec(CookieSpecs.DEFAULT)
                .build();
        client = HttpClientBuilder.create()
                .setDefaultCookieSpecRegistry(cookieSpecRegistry)
                .setDefaultRequestConfig(requestConfig)
                .build();

        setSystemproperties();

        // Start the mock SMS provider (WireMock HTTPS on port 8090) before registering
        // the SMS notification sender with IS.
        mockSMSProvider = new MockSMSProvider();
        mockSMSProvider.start();

        // Create the shared test user (email + mobile required for both OTP channels).
        scim2RestClient = new SCIM2RestClient(serverURL, tenantInfo);
        userId = scim2RestClient.createUser(buildTestUser());

        // Build the notification sender client against backendURL (not serverURL).
        // This matches the URL base used by the IS notification-sender API.
        notificationSenderRestClient = new NotificationSenderRestClient(backendURL, tenantInfo);

        // Defensive cleanup: if a previous test run crashed before @AfterClass could
        // delete the SMS sender, IS will already have "SMSPublisher" registered and
        // the create call will return 409.  Delete it silently first.
        try {
            notificationSenderRestClient.deleteSMSProvider();
        } catch (Exception ignored) {
            // Not present – nothing to clean up.
        }

        // Register the mock SMS notification sender.
        notificationSenderRestClient.createSMSProvider(buildSMSSender());

        // Reset IDP cache to avoid cross-test pollution from other suites.
        resetResidentIDPCache();
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {

        // Delete applications.
        if (emailOtpAppId != null) {
            deleteApp(emailOtpAppId);
        }
        if (smsOtpAppId != null) {
            deleteApp(smsOtpAppId);
        }

        // Delete test user.
        if (userId != null) {
            scim2RestClient.deleteUser(userId);
        }

        // Tear-down notification sender.
        notificationSenderRestClient.deleteSMSProvider();
        notificationSenderRestClient.closeHttpClient();

        if (mockSMSProvider != null) {
            mockSMSProvider.stop();
        }

        scim2RestClient.closeHttpClient();
        restClient.closeHttpClient();

        if (client != null) {
            client.close();
        }

        Utils.getMailServer().purgeEmailFromAllMailboxes();

        // Nullify shared state.
        consumerKey = null;
        consumerSecret = null;
        emailOtpAppId = null;
        emailOtpConsumerKey = null;
        smsOtpAppId = null;
        smsOtpConsumerKey = null;
        flowId = null;
        authenticatorId = null;
        href = null;
        userId = null;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Application registration tests
    // ══════════════════════════════════════════════════════════════════════════

    @Test(groups = "wso2.is",
          description = "Register the Email-OTP first-factor application with retry/resend limit adaptive script.")
    public void testRegisterEmailOTPApplication() throws Exception {

        ApplicationResponseModel app = createOTPApp(EMAIL_OTP_APP_NAME, EMAIL_OTP_AUTHENTICATOR);
        Assert.assertNotNull(app, "Email-OTP app creation failed.");
        Assert.assertTrue(
                app.getAdvancedConfigurations().getEnableAPIBasedAuthentication(),
                "API-based authentication must be enabled on the Email-OTP app.");

        emailOtpAppId = app.getId();
        emailOtpConsumerKey = getOIDCInboundDetailsOfApplication(emailOtpAppId).getClientId();
        Assert.assertNotNull(emailOtpConsumerKey, "Email-OTP app client-id must not be null.");
    }

    @Test(groups = "wso2.is",
          description = "Register the SMS-OTP first-factor application with retry/resend limit adaptive script.",
          dependsOnMethods = "testRegisterEmailOTPApplication")
    public void testRegisterSMSOTPApplication() throws Exception {

        ApplicationResponseModel app = createOTPApp(SMS_OTP_APP_NAME, SMS_OTP_AUTHENTICATOR);
        Assert.assertNotNull(app, "SMS-OTP app creation failed.");
        Assert.assertTrue(
                app.getAdvancedConfigurations().getEnableAPIBasedAuthentication(),
                "API-based authentication must be enabled on the SMS-OTP app.");

        smsOtpAppId = app.getId();
        smsOtpConsumerKey = getOIDCInboundDetailsOfApplication(smsOtpAppId).getClientId();
        Assert.assertNotNull(smsOtpConsumerKey, "SMS-OTP app client-id must not be null.");
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Test A – Email OTP: Retry limit exceeded (ABA-60013)
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * <b>Test A – Email OTP Retry Limit Exceeded (ABA-60013)</b>
     *
     * <p>Flow:
     * <ol>
     *   <li>Initiate auth flow for the Email-OTP app.</li>
     *   <li>Submit wrong OTP once → expect HTTP 200 with flowStatus = FAIL_INCOMPLETE.</li>
     *   <li>Submit wrong OTP second time → expect HTTP 400 with ABA-60013 error payload.</li>
     * </ol>
     */
    @Test(groups = "wso2.is",
          description = "Email OTP – first wrong OTP submit returns HTTP 200 + FAIL_INCOMPLETE.",
          dependsOnMethods = "testRegisterEmailOTPApplication")
    public void testEmailOTPFirstWrongOTPReturnsFAIL_INCOMPLETE() throws Exception {

        // Start a fresh auth flow and complete the IDF step (triggers OTP delivery).
        initiateAuthFlow(emailOtpConsumerKey);

        // Purge the real OTP email that was just delivered by the IDF step, so
        // GreenMail is clean for any later assertions. We intentionally use a wrong OTP below.
        Utils.getMailServer().purgeEmailFromAllMailboxes();

        // Submit a wrong OTP for the first time.
        ExtractableResponse<Response> resp = submitOTPCode(WRONG_OTP, HttpStatus.SC_OK);

        // Assert flowStatus is FAIL_INCOMPLETE.
        String status = resp.jsonPath().getString(FLOW_STATUS);
        Assert.assertEquals(status, FAIL_INCOMPLETE,
                "Email OTP retry #1: expected FAIL_INCOMPLETE but got: " + status);

        // The same flowId must be echoed back so the client can retry.
        Assert.assertEquals(resp.jsonPath().getString(FLOW_ID), flowId,
                "Email OTP retry #1: flowId must remain the same after a failed attempt.");

        // Update href for the next step (server may return a new continuation URL).
        refreshHrefAndAuthenticatorId(resp);
    }

    @Test(groups = "wso2.is",
          description = "Email OTP – second wrong OTP submit returns HTTP 400 + ABA-60013 payload.",
          dependsOnMethods = "testEmailOTPFirstWrongOTPReturnsFAIL_INCOMPLETE")
    public void testEmailOTPSecondWrongOTPReturnsABA60013() {

        // Submit a wrong OTP for the second time using the same flow context.
        ExtractableResponse<Response> resp = submitOTPCode(WRONG_OTP, HttpStatus.SC_BAD_REQUEST);

        // Assert the exact error payload.
        assertOTPLimitExceededPayload(resp,
                ERROR_CODE_OTP_RETRY_LIMIT_EXCEEDED,
                ERROR_MSG_OTP_RETRY_LIMIT_EXCEEDED,
                ERROR_DESC_OTP_RETRY_LIMIT_EXCEEDED);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Test B – Email OTP: Resend limit exceeded (ABA-60014)
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * <b>Test B – Email OTP Resend Limit Exceeded (ABA-60014)</b>
     *
     * <p>Flow:
     * <ol>
     *   <li>Initiate a fresh auth flow for the Email-OTP app.</li>
     *   <li>Resend OTP once → expect HTTP 200 with flowStatus = INCOMPLETE.</li>
     *   <li>Resend OTP second time → expect HTTP 400 with ABA-60014 error payload.</li>
     * </ol>
     */
    @Test(groups = "wso2.is",
          description = "Email OTP – first OTP resend returns HTTP 200 + INCOMPLETE.",
          dependsOnMethods = "testRegisterEmailOTPApplication")
    public void testEmailOTPFirstResendReturnsINCOMPLETE() throws Exception {

        // Fresh flow — independent of retry tests.
        // IDF is completed inside initiateAuthFlow; the real OTP has already been sent.
        initiateAuthFlow(emailOtpConsumerKey);
        // Purge the initial OTP email; the resend will deliver a new one.
        Utils.getMailServer().purgeEmailFromAllMailboxes();

        // Trigger first resend.
        ExtractableResponse<Response> resp = triggerResend(HttpStatus.SC_OK);

        String status = resp.jsonPath().getString(FLOW_STATUS);
        Assert.assertEquals(status, INCOMPLETE,
                "Email OTP resend #1: expected INCOMPLETE but got: " + status);

        // flowId must be preserved so the client can continue the same session.
        Assert.assertEquals(resp.jsonPath().getString(FLOW_ID), flowId,
                "Email OTP resend #1: flowId must remain unchanged after a resend.");

        refreshHrefAndAuthenticatorId(resp);
    }

    @Test(groups = "wso2.is",
          description = "Email OTP – second OTP resend returns HTTP 400 + ABA-60014 payload.",
          dependsOnMethods = "testEmailOTPFirstResendReturnsINCOMPLETE")
    public void testEmailOTPSecondResendReturnsABA60014() {

        // Trigger the second resend using the same flow context.
        ExtractableResponse<Response> resp = triggerResend(HttpStatus.SC_BAD_REQUEST);

        assertOTPLimitExceededPayload(resp,
                ERROR_CODE_OTP_RESEND_LIMIT_EXCEEDED,
                ERROR_MSG_OTP_RESEND_LIMIT_EXCEEDED,
                ERROR_DESC_OTP_RESEND_LIMIT_EXCEEDED);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Test C – SMS OTP: Retry limit exceeded (ABA-60013)
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * <b>Test C – SMS OTP Retry Limit Exceeded (ABA-60013)</b>
     *
     * <p>Flow:
     * <ol>
     *   <li>Initiate auth flow for the SMS-OTP app.</li>
     *   <li>Submit wrong OTP once → expect HTTP 200 with flowStatus = FAIL_INCOMPLETE.</li>
     *   <li>Submit wrong OTP second time → expect HTTP 400 with ABA-60013 error payload.</li>
     * </ol>
     */
    @Test(groups = "wso2.is",
          description = "SMS OTP – first wrong OTP submit returns HTTP 200 + FAIL_INCOMPLETE.",
          dependsOnMethods = "testRegisterSMSOTPApplication")
    public void testSMSOTPFirstWrongOTPReturnsFAIL_INCOMPLETE() throws Exception {

        initiateAuthFlow(smsOtpConsumerKey);

        // Submit a wrong OTP for the first time.
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

        assertOTPLimitExceededPayload(resp,
                ERROR_CODE_OTP_RETRY_LIMIT_EXCEEDED,
                ERROR_MSG_OTP_RETRY_LIMIT_EXCEEDED,
                ERROR_DESC_OTP_RETRY_LIMIT_EXCEEDED);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Test D – SMS OTP: Resend limit exceeded (ABA-60014)
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * <b>Test D – SMS OTP Resend Limit Exceeded (ABA-60014)</b>
     *
     * <p>Flow:
     * <ol>
     *   <li>Initiate a fresh auth flow for the SMS-OTP app.</li>
     *   <li>Resend OTP once → expect HTTP 200 with flowStatus = INCOMPLETE.</li>
     *   <li>Resend OTP second time → expect HTTP 400 with ABA-60014 error payload.</li>
     * </ol>
     */
    @Test(groups = "wso2.is",
          description = "SMS OTP – first OTP resend returns HTTP 200 + INCOMPLETE.",
          dependsOnMethods = "testRegisterSMSOTPApplication")
    public void testSMSOTPFirstResendReturnsINCOMPLETE() throws Exception {

        initiateAuthFlow(smsOtpConsumerKey);

        // Trigger first resend.
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

        assertOTPLimitExceededPayload(resp,
                ERROR_CODE_OTP_RESEND_LIMIT_EXCEEDED,
                ERROR_MSG_OTP_RESEND_LIMIT_EXCEEDED,
                ERROR_DESC_OTP_RESEND_LIMIT_EXCEEDED);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Core flow helpers
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Initiates a fresh API-based auth flow for the given application, then completes
     * the mandatory IDF (Identifier-First) step by submitting the test username.
     *
     * <p>After this method returns, {@code flowId}, {@code authenticatorId} and {@code href}
     * are all pointing at the <b>OTP challenge step</b>, ready for OTP submission or resend.
     *
     * <p>Flow:
     * <ol>
     *   <li>POST to {@code /oauth2/authorize} → receives IDF authenticator challenge.</li>
     *   <li>Submit {@code username} param via the IDF authenticator → OTP is triggered
     *       and the response contains the OTP-challenge step.</li>
     * </ol>
     *
     * @param consumerKey the OAuth2 client_id of the target application.
     * @throws Exception if any HTTP request or JSON parsing fails.
     */
    private void initiateAuthFlow(String consumerKey) throws Exception {

        // ── Step 1: hit /oauth2/authorize to get the IDF challenge ────────────
        HttpResponse response = sendPostRequestWithParameters(
                client,
                buildOAuth2Parameters(consumerKey),
                OAuth2Constant.AUTHORIZE_ENDPOINT_URL);
        Assert.assertNotNull(response, "Auth flow initiation returned null response.");

        String bodyStr = EntityUtils.toString(response.getEntity(), UTF_8);
        EntityUtils.consume(response.getEntity());

        JSONParser parser = new JSONParser();
        JSONObject initJson = (JSONObject) parser.parse(bodyStr);
        Assert.assertNotNull(initJson, "Auth flow init response JSON is null.");

        // Stores flowId, authenticatorId (IDF), and href from the IDF challenge.
        extractFlowState(initJson);

        // ── Step 2: submit the username through IDF → triggers OTP delivery ───
        // After this the flow state is updated to the OTP challenge step.
        submitUsernameForIDF();
    }

    /**
     * Submits the test username through the IDF (Identifier-First) authenticator step.
     *
     * <p>This is required because Email-OTP and SMS-OTP authenticators are configured as
     * <em>first factor</em>, but the IS authentication engine always presents the IDF step
     * first so it can look up the user and know which address to send the OTP to.
     *
     * <p>On success the server returns HTTP 200 with the OTP-challenge step. This method
     * updates {@code flowId}, {@code authenticatorId} and {@code href} to reflect the
     * OTP challenge, so subsequent OTP submit / resend calls use the correct values.
     *
     * @throws Exception if the IDF submission fails unexpectedly.
     */
    private void submitUsernameForIDF() throws Exception {

        String idfBody = buildIDFSubmitBody(flowId, authenticatorId, TEST_USER_NAME);

        ExtractableResponse<Response> resp = postAndAssertStatus(href, idfBody, HttpStatus.SC_OK);

        // The IDF response must advance the flow to the OTP challenge step.
        String status = resp.jsonPath().getString(FLOW_STATUS);
        Assert.assertNotEquals(status, Constants.FAIL_INCOMPLETE,
                "IDF step failed unexpectedly. The username '" + TEST_USER_NAME
                        + "' may not exist or may have been rejected.");

        // Re-extract state: flowId, authenticatorId, and href now belong to the OTP step.
        refreshFlowState(resp);
    }

    /**
     * Submits an OTP code in the current auth flow, asserts the expected HTTP status,
     * and returns the response for further assertion.
     *
     * @param otpCode        the OTP value to submit (use {@link #WRONG_OTP} to trigger failures).
     * @param expectedStatus the expected HTTP status code (200 or 400).
     * @return the extractable response for further assertion.
     */
    private ExtractableResponse<Response> submitOTPCode(String otpCode, int expectedStatus) {

        String body = buildOTPSubmitBody(flowId, authenticatorId, otpCode);
        return postAndAssertStatus(href, body, expectedStatus);
    }

    /**
     * Triggers an OTP resend in the current auth flow, asserts the expected HTTP status,
     * and returns the response for further assertion.
     *
     * @param expectedStatus the expected HTTP status code (200 or 400).
     * @return the extractable response for further assertion.
     */
    private ExtractableResponse<Response> triggerResend(int expectedStatus) {

        String body = buildResendBody(flowId, authenticatorId);
        return postAndAssertStatus(href, body, expectedStatus);
    }

    /**
     * POSTs JSON to a URL and asserts the HTTP status code.
     *
     * @param url            target URL.
     * @param requestBody    JSON body.
     * @param expectedStatus expected HTTP status (e.g. 200 or 400).
     * @return extractable response.
     */
    private ExtractableResponse<Response> postAndAssertStatus(
            String url, String requestBody, int expectedStatus) {

        Response response =
                 given()
                .contentType(ContentType.JSON)
                .headers(new HashMap<>())
                .body(requestBody)
                .when()
                .post(url);
        return response.then()
                .assertThat()
                .statusCode(expectedStatus)
                .extract();
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Assertion helpers
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Validates that an HTTP 400 error response body exactly matches the expected
     * OTP limit-exceeded payload format (code / message / description / traceId).
     *
     * <p>The {@code traceId} field is only checked for existence and non-blankness;
     * its exact value is intentionally NOT asserted (it varies per request).
     *
     * @param resp                extractable response from a 400 reply.
     * @param expectedCode        expected value of the {@code code} field.
     * @param expectedMessage     expected value of the {@code message} field.
     * @param expectedDescription expected value of the {@code description} field.
     */
    private void assertOTPLimitExceededPayload(
            ExtractableResponse<Response> resp,
            String expectedCode,
            String expectedMessage,
            String expectedDescription) {

        String actualCode = resp.jsonPath().getString(CODE);
        String actualMessage = resp.jsonPath().getString(MESSAGE);
        String actualDescription = resp.jsonPath().getString(DESCRIPTION);
        String actualTraceId = resp.jsonPath().getString(TRACE_ID);

        Assert.assertEquals(actualCode, expectedCode,
                "Error code mismatch. Expected: " + expectedCode + ", got: " + actualCode);
        Assert.assertEquals(actualMessage, expectedMessage,
                "Error message mismatch. Expected: " + expectedMessage + ", got: " + actualMessage);
        Assert.assertEquals(actualDescription, expectedDescription,
                "Error description mismatch. Expected: " + expectedDescription
                        + ", got: " + actualDescription);

        // traceId must exist and be non-blank — do NOT hardcode the value.
        Assert.assertNotNull(actualTraceId,
                "traceId must be present in the error response.");
        Assert.assertFalse(actualTraceId.trim().isEmpty(),
                "traceId must not be blank in the error response.");
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  State-extraction helpers
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Parses the JSON init-response and stores flowId, authenticatorId and href
     * into instance fields used by subsequent step methods.
     */
    private void extractFlowState(JSONObject json) {

        Assert.assertTrue(json.containsKey(FLOW_ID),
                "flowId missing from init response.");
        Assert.assertTrue(json.containsKey(FLOW_STATUS),
                "flowStatus missing from init response.");
        Assert.assertTrue(json.containsKey(FLOW_TYPE),
                "flowType missing from init response.");
        Assert.assertTrue(json.containsKey(NEXT_STEP),
                "nextStep missing from init response.");
        Assert.assertTrue(json.containsKey(LINKS),
                "links missing from init response.");

        flowId = (String) json.get(FLOW_ID);

        JSONObject nextStep = (JSONObject) json.get(NEXT_STEP);
        Assert.assertTrue(nextStep.containsKey(STEP_TYPE), "stepType missing from nextStep.");
        Assert.assertTrue(nextStep.containsKey(AUTHENTICATORS), "authenticators missing from nextStep.");

        JSONArray authenticatorsArray = (JSONArray) nextStep.get(AUTHENTICATORS);
        Assert.assertFalse(authenticatorsArray.isEmpty(), "authenticators list must not be empty.");

        JSONObject authenticator = (JSONObject) authenticatorsArray.get(0);
        Assert.assertTrue(authenticator.containsKey(AUTHENTICATOR_ID), "authenticatorId missing.");
        Assert.assertTrue(authenticator.containsKey(METADATA), "metadata missing.");
        Assert.assertTrue(authenticator.containsKey(REQUIRED_PARAMS), "requiredParams missing.");

        authenticatorId = (String) authenticator.get(AUTHENTICATOR_ID);

        JSONObject metadata = (JSONObject) authenticator.get(METADATA);
        Assert.assertTrue(metadata.containsKey(PROMPT_TYPE), "promptType missing from metadata.");
        Assert.assertTrue(metadata.containsKey(PARAMS), "params missing from metadata.");

        JSONArray linksArray = (JSONArray) json.get(LINKS);
        Assert.assertFalse(linksArray.isEmpty(), "links list must not be empty.");
        JSONObject link = (JSONObject) linksArray.get(0);
        Assert.assertTrue(link.containsKey(HREF), "href missing from links.");
        href = link.get(HREF).toString();
    }

    /**
     * After a 200 reply that keeps the flow alive (FAIL_INCOMPLETE / INCOMPLETE),
     * the server may return an updated {@code href} and/or {@code authenticatorId}.
     * This method refreshes those instance fields from the response.
     *
     * @param resp the extractable 200 response.
     */
    private void refreshHrefAndAuthenticatorId(ExtractableResponse<Response> resp) {

        // Refresh href.
        List<Map<String, Object>> links = resp.jsonPath().getList(LINKS);
        if (links != null && !links.isEmpty()) {
            Object hrefObj = links.get(0).get(HREF);
            if (hrefObj != null) {
                href = hrefObj.toString();
            }
        }

        // Refresh authenticatorId (from nextStep.authenticators[0].authenticatorId).
        String nextStepPrefix = NEXT_STEP + "." + AUTHENTICATORS;
        List<Map<String, Object>> auths = resp.jsonPath().getList(nextStepPrefix);
        if (auths != null && !auths.isEmpty()) {
            Object idObj = auths.get(0).get(AUTHENTICATOR_ID);
            if (idObj != null) {
                authenticatorId = idObj.toString();
            }
        }
    }

    /**
     * Fully refreshes {@code flowId}, {@code authenticatorId} and {@code href} from
     * an {@link ExtractableResponse} (typically the IDF step response that transitions
     * the flow into the OTP challenge step).
     *
     * <p>Unlike {@link #refreshHrefAndAuthenticatorId(ExtractableResponse)} which is
     * tolerant of missing values, this method asserts that all three fields are present,
     * because the OTP step must always supply them.
     *
     * @param resp the extractable 200 response from the IDF step.
     */
    private void refreshFlowState(ExtractableResponse<Response> resp) {

        String newFlowId = resp.jsonPath().getString(FLOW_ID);
        Assert.assertNotNull(newFlowId, "flowId must be present in the IDF step response.");
        flowId = newFlowId;

        // href from the links array.
        List<Map<String, Object>> links = resp.jsonPath().getList(LINKS);
        Assert.assertNotNull(links, "links must be present in the IDF step response.");
        Assert.assertFalse(links.isEmpty(), "links must not be empty in the IDF step response.");
        Object hrefObj = links.get(0).get(HREF);
        Assert.assertNotNull(hrefObj, "href must be present in the IDF step response links.");
        href = hrefObj.toString();

        // authenticatorId from nextStep.authenticators[0].
        String nextStepPrefix = NEXT_STEP + "." + AUTHENTICATORS;
        List<Map<String, Object>> auths = resp.jsonPath().getList(nextStepPrefix);
        Assert.assertNotNull(auths, "authenticators must be present in the IDF step nextStep.");
        Assert.assertFalse(auths.isEmpty(), "authenticators must not be empty in the IDF step nextStep.");
        Object idObj = auths.get(0).get(AUTHENTICATOR_ID);
        Assert.assertNotNull(idObj, "authenticatorId must be present in the IDF step authenticator.");
        authenticatorId = idObj.toString();
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Request body builders
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Builds the JSON payload used to submit an OTP code to the authentication endpoint.
     */
    private String buildOTPSubmitBody(String flowId, String authenticatorId, String otpCode) {

        return "{\n" +
               "  \"flowId\": \"" + flowId + "\",\n" +
               "  \"selectedAuthenticator\": {\n" +
               "    \"authenticatorId\": \"" + authenticatorId + "\",\n" +
               "    \"params\": {\n" +
               "      \"OTPCode\": \"" + otpCode + "\"\n" +
               "    }\n" +
               "  }\n" +
               "}";
    }

    /**
     * Builds the JSON payload for the IDF (Identifier-First) step.
     * This submits just the username so the server knows which user to look up
     * and which address (email / mobile) to deliver the OTP to.
     *
     * @param flowId          current flowId from the init response.
     * @param authenticatorId the IDF authenticatorId from the init response.
     * @param username        the username of the test user.
     * @return JSON body string.
     */
    private String buildIDFSubmitBody(String flowId, String authenticatorId, String username) {

        return "{\n" +
               "  \"flowId\": \"" + flowId + "\",\n" +
               "  \"selectedAuthenticator\": {\n" +
               "    \"authenticatorId\": \"" + authenticatorId + "\",\n" +
               "    \"params\": {\n" +
               "      \"" + USERNAME_PARAM + "\": \"" + username + "\"\n" +
               "    }\n" +
               "  }\n" +
               "}";
    }

    /**
     * Builds the JSON payload used to request an OTP resend.
     * The {@code RESEND} param set to {@code "true"} signals the IS authentication
     * engine to re-deliver the OTP without consuming a retry attempt.
     */
    private String buildResendBody(String flowId, String authenticatorId) {

        return "{\n" +
               "  \"flowId\": \"" + flowId + "\",\n" +
               "  \"selectedAuthenticator\": {\n" +
               "    \"authenticatorId\": \"" + authenticatorId + "\",\n" +
               "    \"params\": {\n" +
               "      \"resendCode\": \"true\"\n" +
               "    }\n" +
               "  }\n" +
               "}";
    }

    /**
     * Builds the standard OAuth2 authorization parameters for initiating an API-based auth flow.
     *
     * @param consumerKey the OAuth2 client_id.
     * @return list of name-value pairs for use in the POST to {@code /oauth2/authorize}.
     */
    private List<NameValuePair> buildOAuth2Parameters(String consumerKey) {

        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_RESPONSE_TYPE,
                OAuth2Constant.AUTHORIZATION_CODE_NAME));
        params.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_RESPONSE_MODE, RESPONSE_MODE));
        params.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_CLIENT_ID, consumerKey));
        params.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_REDIRECT_URI, OAuth2Constant.CALLBACK_URL));
        params.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_SCOPE,
                OAuth2Constant.OAUTH2_SCOPE_OPENID_WITH_INTERNAL_LOGIN));
        params.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_NONCE, UUID.randomUUID().toString()));
        return params;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Application / user / sender creation helpers
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Creates an OIDC application with:
     * <ul>
     *   <li>API-based authentication enabled.</li>
     *   <li>A single authentication step containing only {@code authenticatorName}.</li>
     *   <li>The OTP retry/resend limit adaptive-auth script attached.</li>
     * </ul>
     *
     * @param appName         display name of the application.
     * @param authenticatorName the authenticator key, e.g. {@code "email-otp-authenticator"}.
     * @return the created {@link ApplicationResponseModel}.
     * @throws Exception on any I/O or API error.
     */
    private ApplicationResponseModel createOTPApp(String appName, String authenticatorName)
            throws Exception {

        ApplicationModel application = new ApplicationModel();

        // OIDC inbound config.
        List<String> grantTypes = new ArrayList<>();
        Collections.addAll(grantTypes, OAuth2Constant.OAUTH2_GRANT_TYPE_AUTHORIZATION_CODE);

        List<String> callBackUrls = new ArrayList<>();
        Collections.addAll(callBackUrls, OAuth2Constant.CALLBACK_URL);

        OpenIDConnectConfiguration oidcConfig = new OpenIDConnectConfiguration();
        oidcConfig.setGrantTypes(grantTypes);
        oidcConfig.setCallbackURLs(callBackUrls);
        oidcConfig.setPublicClient(true);

        InboundProtocols inboundProtocols = new InboundProtocols();
        inboundProtocols.setOidc(oidcConfig);
        application.setInboundProtocolConfiguration(inboundProtocols);
        application.setName(appName);

        // Enable API-based authentication.
        application.advancedConfigurations(new AdvancedApplicationConfiguration());
        application.getAdvancedConfigurations().setEnableAPIBasedAuthentication(true);

        // Single-step auth sequence.
        AuthenticationStep step = new AuthenticationStep();
        step.setId(1);
        step.addOptionsItem(new Authenticator()
                .idp("LOCAL")
                .authenticator(authenticatorName));

        // Adaptive script with retry/resend limits.
        String adaptiveScript = String.format(ADAPTIVE_SCRIPT_TEMPLATE, authenticatorName);

        AuthenticationSequence authSequence = new AuthenticationSequence();
        authSequence.setType(AuthenticationSequence.TypeEnum.USER_DEFINED);
        authSequence.addStepsItem(step);
        authSequence.setSubjectStepId(1);
        authSequence.setScript(adaptiveScript);

        application.setAuthenticationSequence(authSequence);

        String newAppId = addApplication(application);
        return getApplication(newAppId);
    }

    /**
     * Builds the test user object with both an email address and a mobile number,
     * which are required respectively by the Email-OTP and SMS-OTP authenticators.
     */
    private UserObject buildTestUser() {

        UserObject user = new UserObject();
        user.setUserName(TEST_USER_NAME);
        user.setPassword(TEST_USER_PASSWORD);
        user.setName(new Name()
                .givenName(OIDCUtilTest.firstName)
                .familyName(OIDCUtilTest.lastName));
        // Email claim – required by email-otp-authenticator.
        user.addEmail(new Email().value(TEST_USER_EMAIL));
        // Mobile claim – required by sms-otp-authenticator.
        user.addPhoneNumbers(new PhoneNumbers().value(TEST_USER_MOBILE).type("mobile"));
        return user;
    }

    /**
     * Builds an SMS sender config pointing at the local {@link MockSMSProvider}.
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

    // ══════════════════════════════════════════════════════════════════════════
    //  Infrastructure / IDP helpers
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Resets the resident IDP federated-authenticator cache to avoid state pollution
     * from previously run test cases.
     *
     * <p><b>Important:</b> {@link IdentityProviderMgtServiceClient} is an Axis2 SOAP client
     * and requires the URL to end with {@code "services/"}.  We use {@link #soapBackendURL}
     * here (not {@code backendURL}, which has had {@code "services/"} stripped for REST calls).
     */
    private void resetResidentIDPCache() throws Exception {

        IdentityProviderMgtServiceClient superTenantIDPMgtClient =
                new IdentityProviderMgtServiceClient(sessionCookie, soapBackendURL);
        IdentityProvider residentIdp = superTenantIDPMgtClient.getResidentIdP();

        FederatedAuthenticatorConfig[] federatedAuthenticatorConfigs =
                residentIdp.getFederatedAuthenticatorConfigs();
        for (FederatedAuthenticatorConfig authenticatorConfig : federatedAuthenticatorConfigs) {
            if (!authenticatorConfig.getName().equalsIgnoreCase("samlsso")) {
                federatedAuthenticatorConfigs = (FederatedAuthenticatorConfig[])
                        ArrayUtils.removeElement(federatedAuthenticatorConfigs, authenticatorConfig);
            }
        }
        residentIdp.setFederatedAuthenticatorConfigs(federatedAuthenticatorConfigs);
        superTenantIDPMgtClient.updateResidentIdP(residentIdp);
    }
}


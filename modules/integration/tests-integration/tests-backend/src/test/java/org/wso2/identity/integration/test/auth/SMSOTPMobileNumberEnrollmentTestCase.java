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

package org.wso2.identity.integration.test.auth;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
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
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.AuthenticationStep;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.Authenticator;
import org.wso2.identity.integration.test.rest.api.server.claim.management.v1.model.LocalClaimReq;
import org.wso2.identity.integration.test.rest.api.server.claim.management.v1.model.LocalClaimRes;
import org.wso2.identity.integration.test.rest.api.user.common.model.Email;
import org.wso2.identity.integration.test.rest.api.user.common.model.Name;
import org.wso2.identity.integration.test.rest.api.user.common.model.PhoneNumbers;
import org.wso2.identity.integration.test.rest.api.user.common.model.UserObject;
import org.wso2.identity.integration.test.restclients.ClaimManagementRestClient;
import org.wso2.identity.integration.test.restclients.NotificationSenderRestClient;
import org.wso2.identity.integration.test.restclients.SCIM2RestClient;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Integration tests for enrolling a mobile number during SMS OTP authentication, for a user who does not have a
 * mobile number configured.
 */
public class SMSOTPMobileNumberEnrollmentTestCase extends AbstractOTPProviderFailureTestBase {

    private static final String IDENTIFIER_FIRST_AUTHENTICATOR = "IdentifierExecutor";
    private static final String ENROLL_USER_IN_AUTHENTICATION_FLOW = "SmsOTP.EnrollUserInAuthenticationFlow";
    private static final String MOBILE_NUMBER_REGEX = "SmsOTP.MobileNumberRegex";
    private static final String RESEND_ATTEMPTS_COUNT = "SmsOTP.ResendAttemptsCount";
    private static final String MOBILE_NUMBER_PARAM = "MOBILE_NUMBER";
    private static final String OTP_CODE_PARAM = "OTPcode";
    private static final String MOBILE_NUMBER_REQUEST_PAGE = "mobile.jsp";
    private static final String SMS_OTP_PAGE = "smsOtp.jsp";
    private static final String SMS_OTP_ERROR_PAGE = "smsOtpError.jsp";
    private static final String AUTHENTICATION_ENDPOINT = "/authenticationendpoint/";
    private static final String INVALID_MOBILE_NUMBER_ERROR = "authFailureMsg=sms.otp.mobile.number.invalid";
    private static final String MOBILE_NOT_FOUND_ERROR = "authFailureMsg=user.mobile.not.found";
    private static final String RESEND_LIMIT_EXCEEDED_ERROR = "authFailureMsg=resent.count.exceeded";
    private static final String ENROLLMENT_FAILED_ERROR = "authFailureMsg=sms.otp.mobile.number.enrollment.failed";
    private static final String ATTEMPTS_EXCEEDED_ERROR =
            "authFailureMsg=sms.otp.mobile.number.enrollment.attempts.exceeded";
    private static final String SCRIPT_OPTING_OUT_FROM_ENROLLMENT = "var onLoginRequest = function(context) {\n" +
            "    executeStep(1);\n" +
            "    executeStep(2, {authenticatorParams: {local: {'" + Constants.SMS_OTP_AUTHENTICATOR + "': " +
            "{enrollUserInAuthenticationFlow: 'false'}}}}, {});\n" +
            "};";
    private static final int MAX_ENROLLMENT_ATTEMPTS = 3;
    private static final String MOBILE_CLAIM_URI = "http://wso2.org/claims/mobile";
    private static final String WSO2_SCIM_SCHEMA = "urn:scim:wso2:schema";
    private static final String USER_PASSWORD = "User@123SmsOtp!";
    private static final int MAX_REDIRECTS = 10;

    private static final String APP_NAME = "SMSOTPMobileNumberEnrollmentApp";
    private static final String NATIVE_APP_NAME = "SMSOTPMobileNumberEnrollmentNativeApp";

    private final List<String> userIds = new ArrayList<>();
    private String appId;
    private String clientId;
    private String nativeAppId;
    private String nativeClientId;
    private SCIM2RestClient scim2RestClient;
    private MockSMSProvider mockSMSProvider;
    private NotificationSenderRestClient notificationSenderRestClient;

    @Factory(dataProvider = "testExecutionContextProvider")
    public SMSOTPMobileNumberEnrollmentTestCase(TestUserMode userMode) throws Exception {

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

        scim2RestClient = new SCIM2RestClient(serverURL, tenantInfo);

        mockSMSProvider = new MockSMSProvider();
        mockSMSProvider.start();
        notificationSenderRestClient = new NotificationSenderRestClient(serverURL, tenantInfo);
        notificationSenderRestClient.createSMSProvider(buildSMSSender());

        appId = addTwoStepOIDCApp(APP_NAME, Constants.SMS_OTP_AUTHENTICATOR);
        clientId = getOIDCInboundDetailsOfApplication(appId).getClientId();
        nativeAppId = addTwoStepNativeApp(NATIVE_APP_NAME, Constants.SMS_OTP_AUTHENTICATOR);
        nativeClientId = getOIDCInboundDetailsOfApplication(nativeAppId).getClientId();
    }

    @AfterClass(alwaysRun = true)
    public void testTearDown() throws Exception {

        try {
            revertOTPConnectorProperties(Constants.SMS_OTP_CONNECTOR_ID, ENROLL_USER_IN_AUTHENTICATION_FLOW,
                    MOBILE_NUMBER_REGEX, RESEND_ATTEMPTS_COUNT);
        } finally {
            try {
                if (appId != null) {
                    deleteApp(appId);
                }
                if (nativeAppId != null) {
                    deleteApp(nativeAppId);
                }
                for (String userId : userIds) {
                    scim2RestClient.deleteUser(userId);
                }
                if (notificationSenderRestClient != null) {
                    notificationSenderRestClient.deleteSMSProvider();
                }
            } finally {
                if (mockSMSProvider != null) {
                    mockSMSProvider.stop();
                }
                if (notificationSenderRestClient != null) {
                    notificationSenderRestClient.closeHttpClient();
                }
                restClient.closeHttpClient();
                scim2RestClient.closeHttpClient();
            }
        }
    }

    @Test(groups = "wso2.is", description = "A user without a mobile number is not prompted to enroll one by " +
            "default.")
    public void testEnrollmentIsDisabledByDefault() throws Exception {

        createUserWithoutMobileNumber("sms_otp_enroll_default");

        try (CloseableHttpClient http = createSessionHttpClient()) {
            String pageUrl = loginWithPassword(http, "sms_otp_enroll_default");
            Assert.assertTrue(pageUrl.contains(SMS_OTP_ERROR_PAGE),
                    "The error page is expected when mobile number enrollment is disabled: " + pageUrl);
            Assert.assertTrue(pageUrl.contains(MOBILE_NOT_FOUND_ERROR), "Unexpected error: " + pageUrl);
        }
    }

    @Test(groups = "wso2.is", dependsOnMethods = "testEnrollmentIsDisabledByDefault",
            description = "A user enrolls a mobile number, which is saved only after the OTP sent to it is verified.")
    public void testEnrollMobileNumber() throws Exception {

        updateOTPConnectorProperty(Constants.SMS_OTP_CONNECTOR_ID, ENROLL_USER_IN_AUTHENTICATION_FLOW, "true");
        String userId = createUserWithoutMobileNumber("sms_otp_enroll_user");
        String mobileNumber = "+94771112233";

        try (CloseableHttpClient http = createSessionHttpClient()) {
            String pageUrl = loginWithPassword(http, "sms_otp_enroll_user");
            Assert.assertTrue(pageUrl.contains(MOBILE_NUMBER_REQUEST_PAGE),
                    "The user should be requested for a mobile number: " + pageUrl);
            String sessionDataKey = extractUrlParam(pageUrl, "sessionDataKey");

            // An invalid number is rejected before any OTP is sent.
            mockSMSProvider.clearSmsContent();
            pageUrl = submitMobileNumber(http, sessionDataKey, "not-a-number");
            Assert.assertTrue(pageUrl.contains(MOBILE_NUMBER_REQUEST_PAGE), "Unexpected page: " + pageUrl);
            Assert.assertTrue(pageUrl.contains(INVALID_MOBILE_NUMBER_ERROR), "Unexpected page: " + pageUrl);
            Assert.assertNull(mockSMSProvider.getRecipient(), "No SMS should be sent to an invalid number.");

            // The OTP is sent to the submitted number, formatted characters removed.
            pageUrl = submitMobileNumber(http, sessionDataKey, "+94 77-111 2233");
            Assert.assertTrue(pageUrl.contains(SMS_OTP_PAGE), "The OTP page is expected: " + pageUrl);
            Assert.assertEquals(mockSMSProvider.getRecipient(), mobileNumber);
            String otp = mockSMSProvider.getOTP();
            String otpSessionDataKey = extractUrlParam(pageUrl, "sessionDataKey");

            // A wrong OTP does not save the number.
            pageUrl = followToPage(http, sendOTPCode(http, otpSessionDataKey, "000000"));
            Assert.assertTrue(pageUrl.contains(SMS_OTP_PAGE), "The OTP page is expected on retry: " + pageUrl);
            Assert.assertNull(getMobileNumber(userId), "The number must not be saved before it is verified.");

            String callbackUrl = followRedirectsToCallback(http, sendOTPCode(http, otpSessionDataKey, otp));
            Assert.assertNotNull(callbackUrl, "The login should complete once the OTP is verified.");
            Assert.assertTrue(callbackUrl.contains("code="), "Unexpected callback: " + callbackUrl);
        }

        Assert.assertEquals(getMobileNumber(userId), mobileNumber);
        Assert.assertTrue(isPhoneVerified(userId), "The enrolled number should be marked as verified.");
    }

    @Test(groups = "wso2.is", dependsOnMethods = "testEnrollMobileNumber",
            description = "A user who enrolled a mobile number receives the OTP to it in the next login.")
    public void testLoginAfterEnrollment() throws Exception {

        try (CloseableHttpClient http = createSessionHttpClient()) {
            mockSMSProvider.clearSmsContent();
            String pageUrl = loginWithPassword(http, "sms_otp_enroll_user");
            Assert.assertTrue(pageUrl.contains(SMS_OTP_PAGE), "The OTP page is expected: " + pageUrl);
            Assert.assertEquals(mockSMSProvider.getRecipient(), "+94771112233");

            String callbackUrl = followRedirectsToCallback(http, sendOTPCode(http,
                    extractUrlParam(pageUrl, "sessionDataKey"), mockSMSProvider.getOTP()));
            Assert.assertNotNull(callbackUrl, "The login should complete.");
        }
    }

    @Test(groups = "wso2.is", dependsOnMethods = "testEnrollMobileNumber",
            description = "An OTP sent to a number cannot be used to enroll a different number.")
    public void testOtpOfEarlierNumberCannotEnrollNewNumber() throws Exception {

        String userId = createUserWithoutMobileNumber("sms_otp_enroll_change");

        try (CloseableHttpClient http = createSessionHttpClient()) {
            String pageUrl = loginWithPassword(http, "sms_otp_enroll_change");
            String sessionDataKey = extractUrlParam(pageUrl, "sessionDataKey");

            mockSMSProvider.clearSmsContent();
            pageUrl = submitMobileNumber(http, sessionDataKey, "+94772223344");
            Assert.assertTrue(pageUrl.contains(SMS_OTP_PAGE), "The OTP page is expected: " + pageUrl);
            Assert.assertEquals(mockSMSProvider.getRecipient(), "+94772223344");
            String otpOfFirstNumber = mockSMSProvider.getOTP();

            // The user changes the number before verifying the first one.
            mockSMSProvider.clearSmsContent();
            pageUrl = submitMobileNumber(http, sessionDataKey, "+94775556677");
            Assert.assertTrue(pageUrl.contains(SMS_OTP_PAGE), "The OTP page is expected: " + pageUrl);
            Assert.assertEquals(mockSMSProvider.getRecipient(), "+94775556677");
            String otpOfSecondNumber = mockSMSProvider.getOTP();
            String otpSessionDataKey = extractUrlParam(pageUrl, "sessionDataKey");

            // Both SMS are captured above, so the OTPs are equal only if the same code is generated twice.
            if (!otpOfFirstNumber.equals(otpOfSecondNumber)) {
                pageUrl = followToPage(http, sendOTPCode(http, otpSessionDataKey, otpOfFirstNumber));
                Assert.assertTrue(pageUrl.contains(SMS_OTP_PAGE), "Unexpected page: " + pageUrl);
                Assert.assertNull(getMobileNumber(userId),
                        "The OTP of the first number must not enroll the second number.");
            }

            String callbackUrl = followRedirectsToCallback(http,
                    sendOTPCode(http, otpSessionDataKey, otpOfSecondNumber));
            Assert.assertNotNull(callbackUrl, "The login should complete once the OTP is verified.");
        }

        Assert.assertEquals(getMobileNumber(userId), "+94775556677");
    }

    @Test(groups = "wso2.is", dependsOnMethods = "testEnrollMobileNumber",
            description = "A number which does not match the regex configured for the organization is rejected.")
    public void testConfiguredMobileNumberRegexIsEnforced() throws Exception {

        updateOTPConnectorProperty(Constants.SMS_OTP_CONNECTOR_ID, MOBILE_NUMBER_REGEX, "^\\+94[0-9]{9}$");
        try {
            String userId = createUserWithoutMobileNumber("sms_otp_enroll_regex");
            try (CloseableHttpClient http = createSessionHttpClient()) {
                String pageUrl = loginWithPassword(http, "sms_otp_enroll_regex");
                String sessionDataKey = extractUrlParam(pageUrl, "sessionDataKey");

                mockSMSProvider.clearSmsContent();
                pageUrl = submitMobileNumber(http, sessionDataKey, "+14155552671");
                Assert.assertTrue(pageUrl.contains(INVALID_MOBILE_NUMBER_ERROR), "Unexpected page: " + pageUrl);
                Assert.assertNull(mockSMSProvider.getRecipient(), "No SMS should be sent to a rejected number.");
            }
            Assert.assertNull(getMobileNumber(userId));
        } finally {
            revertOTPConnectorProperties(Constants.SMS_OTP_CONNECTOR_ID, MOBILE_NUMBER_REGEX);
        }
    }

    @Test(groups = "wso2.is", dependsOnMethods = "testEnrollMobileNumber",
            description = "Submitting the same mobile number again counts as a resend, so the resend limit applies.")
    public void testResubmittingSameNumberIsLimitedByResendLimit() throws Exception {

        // One OTP is sent for the first submission, and one for each allowed resend.
        int allowedResends = 2;
        updateOTPConnectorProperty(Constants.SMS_OTP_CONNECTOR_ID, RESEND_ATTEMPTS_COUNT,
                String.valueOf(allowedResends));
        try {
            String userId = createUserWithoutMobileNumber("sms_otp_enroll_resubmit");
            String mobileNumber = "+94773334455";

            try (CloseableHttpClient http = createSessionHttpClient()) {
                String pageUrl = loginWithPassword(http, "sms_otp_enroll_resubmit");
                Assert.assertTrue(pageUrl.contains(MOBILE_NUMBER_REQUEST_PAGE), "Unexpected page: " + pageUrl);
                String sessionDataKey = extractUrlParam(pageUrl, "sessionDataKey");

                for (int submission = 1; submission <= allowedResends + 1; submission++) {
                    mockSMSProvider.clearSmsContent();
                    pageUrl = submitMobileNumber(http, sessionDataKey, mobileNumber);
                    Assert.assertTrue(pageUrl.contains(SMS_OTP_PAGE),
                            "The OTP page is expected for submission " + submission + ": " + pageUrl);
                    Assert.assertEquals(mockSMSProvider.getRecipient(), mobileNumber,
                            "An SMS is expected for submission " + submission);
                }

                // Once the resend limit is reached, submitting the same number again sends no SMS.
                mockSMSProvider.clearSmsContent();
                pageUrl = submitMobileNumber(http, sessionDataKey, mobileNumber);
                Assert.assertTrue(pageUrl.contains(SMS_OTP_ERROR_PAGE), "The error page is expected: " + pageUrl);
                Assert.assertTrue(pageUrl.contains(RESEND_LIMIT_EXCEEDED_ERROR), "Unexpected error: " + pageUrl);
                Assert.assertNull(mockSMSProvider.getRecipient(),
                        "No SMS should be sent once the resend limit is reached.");
            }
            Assert.assertNull(getMobileNumber(userId), "The number must not be saved without verifying the OTP.");
        } finally {
            revertOTPConnectorProperties(Constants.SMS_OTP_CONNECTOR_ID, RESEND_ATTEMPTS_COUNT);
        }
    }

    @Test(groups = "wso2.is", dependsOnMethods = "testEnrollMobileNumber",
            description = "The authentication fails when the verified mobile number cannot be saved to the profile.")
    public void testAuthenticationFailsWhenMobileNumberCannotBeSaved() throws Exception {

        String mobileNumber = "+94776667788";
        ClaimManagementRestClient claimClient = new ClaimManagementRestClient(serverURL, tenantInfo);
        LocalClaimRes mobileClaim = claimClient.getLocalClaimByUri(MOBILE_CLAIM_URI);
        try {
            // The number of another user cannot be saved, since the mobile number is required to be unique.
            claimClient.updateLocalClaim(mobileClaim.getId(),
                    buildLocalClaimReq(mobileClaim, LocalClaimReq.UniquenessScopeEnum.ACROSS_USERSTORES));
            createUserWithMobileNumber("sms_otp_enroll_taken_owner", mobileNumber);
            String userId = createUserWithoutMobileNumber("sms_otp_enroll_taken");

            try (CloseableHttpClient http = createSessionHttpClient()) {
                String pageUrl = loginWithPassword(http, "sms_otp_enroll_taken");
                String sessionDataKey = extractUrlParam(pageUrl, "sessionDataKey");

                mockSMSProvider.clearSmsContent();
                pageUrl = submitMobileNumber(http, sessionDataKey, mobileNumber);
                Assert.assertTrue(pageUrl.contains(SMS_OTP_PAGE), "The OTP page is expected: " + pageUrl);
                Assert.assertEquals(mockSMSProvider.getRecipient(), mobileNumber);

                // The OTP is verified, but the number cannot be saved. Hence, the authentication does not complete.
                pageUrl = followToPage(http, sendOTPCode(http, extractUrlParam(pageUrl, "sessionDataKey"),
                        mockSMSProvider.getOTP()));
                Assert.assertTrue(pageUrl.contains(MOBILE_NUMBER_REQUEST_PAGE),
                        "The mobile number request page is expected: " + pageUrl);
                Assert.assertTrue(pageUrl.contains(ENROLLMENT_FAILED_ERROR), "Unexpected error: " + pageUrl);
            }
            Assert.assertNull(getMobileNumber(userId), "The number must not be saved.");
        } finally {
            try {
                claimClient.updateLocalClaim(mobileClaim.getId(), buildLocalClaimReq(mobileClaim,
                        mobileClaim.getUniquenessScope() == null ? null :
                                LocalClaimReq.UniquenessScopeEnum.valueOf(mobileClaim.getUniquenessScope().name())));
                LocalClaimRes restoredClaim = claimClient.getLocalClaimByUri(MOBILE_CLAIM_URI);
                Assert.assertEquals(restoredClaim.getUniquenessScope(), mobileClaim.getUniquenessScope());
                Assert.assertEquals(restoredClaim.getSharedProfileValueResolvingMethod(),
                        mobileClaim.getSharedProfileValueResolvingMethod());
                Assert.assertEquals(restoredClaim.getProperties(), mobileClaim.getProperties());
            } finally {
                claimClient.closeHttpClient();
            }
        }
    }

    @Test(groups = "wso2.is", dependsOnMethods = "testEnrollMobileNumber",
            description = "A user enrolls a mobile number through app native authentication.")
    public void testEnrollMobileNumberWithAppNativeAuthentication() throws Exception {

        String userId = createUserWithoutMobileNumber("sms_otp_enroll_native");
        String mobileNumber = "+94778889900";

        AuthnFlowState state = initiateNativeFlow(nativeClientId);
        state = completeBasicAuth(state, "sms_otp_enroll_native", USER_PASSWORD);
        Assert.assertEquals(getRequiredParams(state), "[\"" + MOBILE_NUMBER_PARAM + "\"]",
                "A mobile number should be requested: " + state.response);

        mockSMSProvider.clearSmsContent();
        state = submitAuthenticatorParam(state, MOBILE_NUMBER_PARAM, mobileNumber);
        Assert.assertEquals(getRequiredParams(state), "[\"" + OTP_CODE_PARAM + "\"]",
                "The OTP should be requested: " + state.response);
        Assert.assertEquals(mockSMSProvider.getRecipient(), mobileNumber);

        state = submitOTPCode(state, mockSMSProvider.getOTP());
        Assert.assertEquals(state.response.get("flowStatus"), "SUCCESS_COMPLETED",
                "The login should complete once the OTP is verified: " + state.response);
        Assert.assertEquals(getMobileNumber(userId), mobileNumber);
        Assert.assertTrue(isPhoneVerified(userId), "The enrolled number should be marked as verified.");
    }

    @Test(groups = "wso2.is", dependsOnMethods = "testEnrollMobileNumber",
            description = "Only a limited number of mobile numbers can be tried in a login. Invalid numbers are not " +
                    "counted.")
    public void testEnrollmentAttemptsAreLimited() throws Exception {

        String userId = createUserWithoutMobileNumber("sms_otp_enroll_attempts");

        try (CloseableHttpClient http = createSessionHttpClient()) {
            String pageUrl = loginWithPassword(http, "sms_otp_enroll_attempts");
            String sessionDataKey = extractUrlParam(pageUrl, "sessionDataKey");

            pageUrl = submitMobileNumber(http, sessionDataKey, "not-a-number");
            Assert.assertTrue(pageUrl.contains(INVALID_MOBILE_NUMBER_ERROR), "Unexpected page: " + pageUrl);

            for (int attempt = 1; attempt <= MAX_ENROLLMENT_ATTEMPTS; attempt++) {
                String mobileNumber = "+9477444000" + attempt;
                mockSMSProvider.clearSmsContent();
                pageUrl = submitMobileNumber(http, sessionDataKey, mobileNumber);
                Assert.assertTrue(pageUrl.contains(SMS_OTP_PAGE),
                        "The OTP page is expected for attempt " + attempt + ": " + pageUrl);
                Assert.assertEquals(mockSMSProvider.getRecipient(), mobileNumber,
                        "An SMS is expected for attempt " + attempt);
            }

            mockSMSProvider.clearSmsContent();
            pageUrl = submitMobileNumber(http, sessionDataKey, "+94774440009");
            Assert.assertTrue(pageUrl.contains(SMS_OTP_ERROR_PAGE), "The error page is expected: " + pageUrl);
            Assert.assertTrue(pageUrl.contains(ATTEMPTS_EXCEEDED_ERROR), "Unexpected error: " + pageUrl);
            Assert.assertNull(mockSMSProvider.getRecipient(), "No SMS should be sent once the limit is reached.");
        }
        Assert.assertNull(getMobileNumber(userId), "The number must not be saved without verifying the OTP.");
    }

    @Test(groups = "wso2.is", dependsOnMethods = "testEnrollMobileNumber",
            description = "An application can turn off mobile number enrollment from the authentication script.")
    public void testNoEnrollmentWhenTurnedOffInAuthenticationScript() throws Exception {

        ApplicationModel application = buildTwoStepAppModel("SMSOTPEnrollmentScriptApp",
                Constants.SMS_OTP_AUTHENTICATOR, false);
        application.getAuthenticationSequence().script(SCRIPT_OPTING_OUT_FROM_ENROLLMENT);
        String scriptAppId = addApplication(application);
        try {
            String userId = createUserWithoutMobileNumber("sms_otp_enroll_script");
            try (CloseableHttpClient http = createSessionHttpClient()) {
                mockSMSProvider.clearSmsContent();
                String sessionDataKey =
                        initiateRedirectFlow(http, getOIDCInboundDetailsOfApplication(scriptAppId).getClientId());
                String pageUrl = followToPage(http,
                        sendCredentials(http, sessionDataKey, "sms_otp_enroll_script", USER_PASSWORD));
                assertMobileNumberNotRequested(pageUrl);
                Assert.assertTrue(pageUrl.contains(MOBILE_NOT_FOUND_ERROR), "Unexpected error: " + pageUrl);
            }
            Assert.assertNull(getMobileNumber(userId));
        } finally {
            deleteApp(scriptAppId);
        }
    }

    @Test(groups = "wso2.is", dependsOnMethods = "testEnrollMobileNumber",
            description = "A mobile number is not requested when the user is only identified, not authenticated, " +
                    "before SMS OTP.")
    public void testNoEnrollmentAfterIdentifierFirst() throws Exception {

        ApplicationModel application = buildTwoStepAppModel("SMSOTPEnrollmentIdentifierFirstApp",
                Constants.SMS_OTP_AUTHENTICATOR, false);
        application.getAuthenticationSequence().getSteps().get(0).setOptions(Collections.singletonList(
                new Authenticator().idp("LOCAL").authenticator(IDENTIFIER_FIRST_AUTHENTICATOR)));
        String identifierFirstAppId = addApplication(application);
        try {
            String userId = createUserWithoutMobileNumber("sms_otp_enroll_identifier");
            try (CloseableHttpClient http = createSessionHttpClient()) {
                mockSMSProvider.clearSmsContent();
                String sessionDataKey = initiateRedirectFlow(http,
                        getOIDCInboundDetailsOfApplication(identifierFirstAppId).getClientId());
                String pageUrl = followToPage(http, submitUsername(http, sessionDataKey, "sms_otp_enroll_identifier"));
                assertMobileNumberNotRequested(pageUrl);
                // SMS OTP shows the OTP page without revealing that the user has no mobile number.
                Assert.assertTrue(pageUrl.contains(SMS_OTP_PAGE), "The OTP page is expected: " + pageUrl);
            }
            Assert.assertNull(getMobileNumber(userId));
        } finally {
            deleteApp(identifierFirstAppId);
        }
    }

    @Test(groups = "wso2.is", dependsOnMethods = "testEnrollMobileNumber",
            description = "A mobile number is not requested when SMS OTP is the first factor.")
    public void testNoEnrollmentWhenSMSOTPIsTheFirstFactor() throws Exception {

        ApplicationModel application = buildTwoStepAppModel("SMSOTPEnrollmentFirstFactorApp",
                Constants.SMS_OTP_AUTHENTICATOR, false);
        List<AuthenticationStep> steps = application.getAuthenticationSequence().getSteps();
        steps.get(0).setOptions(Collections.singletonList(
                new Authenticator().idp("LOCAL").authenticator(Constants.SMS_OTP_AUTHENTICATOR)));
        steps.remove(1);
        String firstFactorAppId = addApplication(application);
        try {
            String userId = createUserWithoutMobileNumber("sms_otp_enroll_first_factor");
            try (CloseableHttpClient http = createSessionHttpClient()) {
                mockSMSProvider.clearSmsContent();
                String sessionDataKey = initiateRedirectFlow(http,
                        getOIDCInboundDetailsOfApplication(firstFactorAppId).getClientId());
                String pageUrl = followToPage(http, submitUsername(http, sessionDataKey,
                        "sms_otp_enroll_first_factor"));
                assertMobileNumberNotRequested(pageUrl);
                // SMS OTP shows the OTP page without revealing that the user has no mobile number.
                Assert.assertTrue(pageUrl.contains(SMS_OTP_PAGE), "The OTP page is expected: " + pageUrl);
            }
            Assert.assertNull(getMobileNumber(userId));
        } finally {
            deleteApp(firstFactorAppId);
        }
    }

    private void assertMobileNumberNotRequested(String pageUrl) {

        Assert.assertFalse(pageUrl.contains(MOBILE_NUMBER_REQUEST_PAGE),
                "A mobile number should not be requested: " + pageUrl);
        Assert.assertNull(mockSMSProvider.getRecipient(), "No SMS should be sent.");
    }

    private HttpResponse submitUsername(CloseableHttpClient http, String sessionDataKey, String username)
            throws Exception {

        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("username", username));
        params.add(new BasicNameValuePair("sessionDataKey", sessionDataKey));
        return sendPostRequestWithParameters(http, params,
                getTenantQualifiedURL(OAuth2Constant.COMMON_AUTH_URL, tenantInfo.getDomain()));
    }

    private String createUserWithoutMobileNumber(String username) throws Exception {

        UserObject user = new UserObject();
        user.setUserName(username);
        user.setPassword(USER_PASSWORD);
        user.setName(new Name().givenName("SmsOtp").familyName("Enrollment"));
        user.addEmail(new Email().value(username + "@example.com"));
        String userId = scim2RestClient.createUser(user);
        userIds.add(userId);
        return userId;
    }

    private void createUserWithMobileNumber(String username, String mobileNumber) throws Exception {

        UserObject user = new UserObject();
        user.setUserName(username);
        user.setPassword(USER_PASSWORD);
        user.setName(new Name().givenName("SmsOtp").familyName("Enrollment"));
        user.addEmail(new Email().value(username + "@example.com"));
        user.phoneNumbers(Collections.singletonList(new PhoneNumbers().type("mobile").value(mobileNumber)));
        userIds.add(scim2RestClient.createUser(user));
    }

    /**
     * Build a request that keeps every attribute of the claim, since the update replaces the whole claim.
     */
    private static LocalClaimReq buildLocalClaimReq(LocalClaimRes claim,
                                                    LocalClaimReq.UniquenessScopeEnum uniquenessScope) {

        LocalClaimReq claimReq = new LocalClaimReq();
        claimReq.setClaimURI(claim.getClaimURI());
        claimReq.setDisplayName(claim.getDisplayName());
        claimReq.setDescription(claim.getDescription());
        claimReq.setDisplayOrder(claim.getDisplayOrder());
        claimReq.setReadOnly(claim.getReadOnly());
        claimReq.setRegEx(claim.getRegEx());
        claimReq.setRequired(claim.getRequired());
        claimReq.setSupportedByDefault(claim.getSupportedByDefault());
        claimReq.setManagedInUserStore(claim.getManagedInUserStore());
        claimReq.setDataType(claim.getDataType());
        claimReq.setSubAttributes(claim.getSubAttributes());
        claimReq.setCanonicalValues(claim.getCanonicalValues());
        claimReq.setInputFormat(claim.getInputFormat());
        claimReq.setAttributeMapping(claim.getAttributeMapping());
        claimReq.setProperties(claim.getProperties());
        claimReq.setProfiles(claim.getProfiles());
        if (claim.getSharedProfileValueResolvingMethod() != null) {
            claimReq.setSharedProfileValueResolvingMethod(LocalClaimReq.SharedProfileValueResolvingMethodEnum
                    .valueOf(claim.getSharedProfileValueResolvingMethod().name()));
        }
        claimReq.setUniquenessScope(uniquenessScope);
        return claimReq;
    }

    private String loginWithPassword(CloseableHttpClient http, String username) throws Exception {

        String sessionDataKey = initiateRedirectFlow(http, clientId);
        return followToPage(http, sendCredentials(http, sessionDataKey, username, USER_PASSWORD));
    }

    private String submitMobileNumber(CloseableHttpClient http, String sessionDataKey, String mobileNumber)
            throws Exception {

        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair(MOBILE_NUMBER_PARAM, mobileNumber));
        params.add(new BasicNameValuePair("sessionDataKey", sessionDataKey));
        return followToPage(http, sendPostRequestWithParameters(http, params,
                getTenantQualifiedURL(OAuth2Constant.COMMON_AUTH_URL, tenantInfo.getDomain())));
    }

    /**
     * Follow redirects until a page of the authentication endpoint is reached.
     */
    private String followToPage(CloseableHttpClient http, HttpResponse response) throws Exception {

        for (int i = 0; i < MAX_REDIRECTS; i++) {
            Header location = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
            EntityUtils.consume(response.getEntity());
            Assert.assertNotNull(location, "A redirect to an authentication endpoint page was expected.");
            String url = location.getValue();
            if (url.contains(AUTHENTICATION_ENDPOINT)) {
                return url;
            }
            response = sendGetRequest(http, url);
        }
        Assert.fail("An authentication endpoint page was not reached.");
        return null;
    }

    private static String getRequiredParams(AuthnFlowState state) {

        JSONObject nextStep = (JSONObject) state.response.get("nextStep");
        Assert.assertNotNull(nextStep, "A next step is expected: " + state.response);
        JSONArray authenticators = (JSONArray) nextStep.get("authenticators");
        return String.valueOf(((JSONObject) authenticators.get(0)).get("requiredParams"));
    }

    private String getMobileNumber(String userId) throws Exception {

        JSONArray phoneNumbers = (JSONArray) scim2RestClient.getUser(userId, null).get("phoneNumbers");
        if (phoneNumbers == null) {
            return null;
        }
        for (Object phoneNumber : phoneNumbers) {
            JSONObject phone = (JSONObject) phoneNumber;
            if ("mobile".equals(phone.get("type"))) {
                return (String) phone.get("value");
            }
        }
        return null;
    }

    private boolean isPhoneVerified(String userId) throws Exception {

        JSONObject wso2Schema = (JSONObject) scim2RestClient.getUser(userId, null).get(WSO2_SCIM_SCHEMA);
        return wso2Schema != null && Boolean.parseBoolean(String.valueOf(wso2Schema.get("phoneVerified")));
    }
}

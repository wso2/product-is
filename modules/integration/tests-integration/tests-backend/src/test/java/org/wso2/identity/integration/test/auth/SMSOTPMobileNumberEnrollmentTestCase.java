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
import org.wso2.identity.integration.test.rest.api.server.identity.governance.v1.dto.ConnectorsPatchReq;
import org.wso2.identity.integration.test.rest.api.server.identity.governance.v1.dto.PropertyReq;
import org.wso2.identity.integration.test.rest.api.user.common.model.Email;
import org.wso2.identity.integration.test.rest.api.user.common.model.Name;
import org.wso2.identity.integration.test.rest.api.user.common.model.UserObject;
import org.wso2.identity.integration.test.restclients.IdentityGovernanceRestClient;
import org.wso2.identity.integration.test.restclients.NotificationSenderRestClient;
import org.wso2.identity.integration.test.restclients.OAuth2RestClient;
import org.wso2.identity.integration.test.restclients.SCIM2RestClient;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Integration tests for enrolling a mobile number during SMS OTP authentication, for a user who does not have a
 * mobile number configured.
 */
public class SMSOTPMobileNumberEnrollmentTestCase extends AbstractOTPProviderFailureTestBase {

    private static final String SMS_OTP_AUTHENTICATOR = "sms-otp-authenticator";
    private static final String MFA_GOVERNANCE_CATEGORY_ID = "TXVsdGkgRmFjdG9yIEF1dGhlbnRpY2F0b3Jz";
    private static final String SMS_OTP_CONNECTOR_ID = "c21zLW90cC1hdXRoZW50aWNhdG9y";
    private static final String ENROL_USER_IN_AUTHENTICATION_FLOW = "SmsOTP.EnrolUserInAuthenticationFlow";
    private static final String MOBILE_NUMBER_REGEX = "SmsOTP.MobileNumberRegex";
    private static final String MOBILE_NUMBER_PARAM = "MOBILE_NUMBER";
    private static final String OTP_CODE_PARAM = "OTPcode";
    private static final String MOBILE_NUMBER_REQUEST_PAGE = "mobile.jsp";
    private static final String SMS_OTP_PAGE = "smsOtp.jsp";
    private static final String SMS_OTP_ERROR_PAGE = "smsOtpError.jsp";
    private static final String INVALID_MOBILE_NUMBER_ERROR = "authFailureMsg=sms.otp.mobile.number.invalid";
    private static final String MOBILE_NOT_FOUND_ERROR = "authFailureMsg=user.mobile.not.found";
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

        restClient = new OAuth2RestClient(serverURL, tenantInfo);
        scim2RestClient = new SCIM2RestClient(serverURL, tenantInfo);

        mockSMSProvider = new MockSMSProvider();
        mockSMSProvider.start();
        notificationSenderRestClient = new NotificationSenderRestClient(serverURL, tenantInfo);
        notificationSenderRestClient.createSMSProvider(buildSMSSender());

        appId = addTwoStepOIDCApp(APP_NAME, SMS_OTP_AUTHENTICATOR);
        clientId = getOIDCInboundDetailsOfApplication(appId).getClientId();
        nativeAppId = addTwoStepNativeApp(NATIVE_APP_NAME, SMS_OTP_AUTHENTICATOR);
        nativeClientId = getOIDCInboundDetailsOfApplication(nativeAppId).getClientId();
    }

    @AfterClass(alwaysRun = true)
    public void testTearDown() throws Exception {

        updateSmsOtpConnectorProperty(ENROL_USER_IN_AUTHENTICATION_FLOW, "false");
        updateSmsOtpConnectorProperty(MOBILE_NUMBER_REGEX, "");

        if (appId != null) {
            deleteApp(appId);
        }
        if (nativeAppId != null) {
            deleteApp(nativeAppId);
        }
        for (String userId : userIds) {
            scim2RestClient.deleteUser(userId);
        }
        notificationSenderRestClient.deleteSMSProvider();
        notificationSenderRestClient.closeHttpClient();
        mockSMSProvider.stop();

        restClient.closeHttpClient();
        scim2RestClient.closeHttpClient();
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

        updateSmsOtpConnectorProperty(ENROL_USER_IN_AUTHENTICATION_FLOW, "true");
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

            pageUrl = submitMobileNumber(http, sessionDataKey, "+94772223344");
            Assert.assertTrue(pageUrl.contains(SMS_OTP_PAGE), "The OTP page is expected: " + pageUrl);
            String otpOfFirstNumber = mockSMSProvider.getOTP();

            // The user changes the number before verifying the first one.
            pageUrl = submitMobileNumber(http, sessionDataKey, "+94775556677");
            Assert.assertTrue(pageUrl.contains(SMS_OTP_PAGE), "The OTP page is expected: " + pageUrl);
            Assert.assertEquals(mockSMSProvider.getRecipient(), "+94775556677");
            String otpOfSecondNumber = mockSMSProvider.getOTP();
            String otpSessionDataKey = extractUrlParam(pageUrl, "sessionDataKey");

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

        updateSmsOtpConnectorProperty(MOBILE_NUMBER_REGEX, "^\\+94[0-9]{9}$");
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
            updateSmsOtpConnectorProperty(MOBILE_NUMBER_REGEX, "");
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
     * Follow redirects until a page of the SMS OTP authenticator is reached.
     */
    private String followToPage(CloseableHttpClient http, HttpResponse response) throws Exception {

        for (int i = 0; i < MAX_REDIRECTS; i++) {
            Header location = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
            EntityUtils.consume(response.getEntity());
            Assert.assertNotNull(location, "A redirect to an SMS OTP page was expected.");
            String url = location.getValue();
            if (url.contains(MOBILE_NUMBER_REQUEST_PAGE) || url.contains(SMS_OTP_PAGE)
                    || url.contains(SMS_OTP_ERROR_PAGE)) {
                return url;
            }
            response = sendGetRequest(http, url);
        }
        Assert.fail("An SMS OTP page was not reached.");
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

    private void updateSmsOtpConnectorProperty(String propertyName, String value) throws IOException {

        PropertyReq property = new PropertyReq();
        property.setName(propertyName);
        property.setValue(value);
        ConnectorsPatchReq patchReq = new ConnectorsPatchReq();
        patchReq.setOperation(ConnectorsPatchReq.OperationEnum.UPDATE);
        patchReq.addProperties(property);

        IdentityGovernanceRestClient client = new IdentityGovernanceRestClient(serverURL, tenantInfo);
        try {
            client.updateConnectors(MFA_GOVERNANCE_CATEGORY_ID, SMS_OTP_CONNECTOR_ID, patchReq);
        } finally {
            client.closeHttpClient();
        }
    }
}

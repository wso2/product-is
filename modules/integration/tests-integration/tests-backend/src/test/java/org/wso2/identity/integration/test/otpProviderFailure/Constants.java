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

package org.wso2.identity.integration.test.otpProviderFailure;

/**
 * Constants for OTP provider sending-failure notification integration tests.
 */
public final class Constants {

    private Constants() {

    }

    public static final String MFA_GOVERNANCE_CATEGORY_ID = "TXVsdGkgRmFjdG9yIEF1dGhlbnRpY2F0b3Jz";

    public static final String EMAIL_OTP_CONNECTOR_ID = "RW1haWxPVFA";

    public static final String SMS_OTP_CONNECTOR_ID = "U21zT1RQ";

    public static final String EMAIL_OTP_NOTIFY_SENDING_FAILURE_PROPERTY = "EmailOTP.NotifyEmailSendingFailure";
    public static final String SMS_OTP_NOTIFY_SENDING_FAILURE_PROPERTY = "SmsOTP.NotifySmsSendingFailure";

    public static final String PROPERTY_ENABLED = "true";
    public static final String PROPERTY_DISABLED = "false";

    public static final String EMAIL_OTP_AUTHENTICATOR = "email-otp-authenticator";
    public static final String SMS_OTP_AUTHENTICATOR = "sms-otp-authenticator";

    public static final String TEST_USER_NAME = "otp_failure_test_user";
    public static final String TEST_USER_PASSWORD = "User@123OtpFail!";
    public static final String TEST_USER_EMAIL = "otp_failure_test@example.com";
    public static final String TEST_USER_MOBILE = "+94771234568";
    public static final String TEST_USER_FIRST_NAME = "OtpFailure";
    public static final String TEST_USER_LAST_NAME = "TestUser";

    public static final String EMAIL_PROVIDER_ERROR_CODE_PREFIX = "EP-";
    public static final String SMS_PROVIDER_ERROR_CODE_PREFIX = "SP-";
    public static final String AUTH_FAILURE_PARAM = "authFailure=true";
    public static final String ERROR_CODE_PARAM = "errorCode=";
    public static final String OTP_ERROR_ELEMENT_ID = "id=\"failed-msg\"";
    public static final String FLOW_STATUS_INCOMPLETE = "INCOMPLETE";
    public static final String FLOW_STATUS_SUCCESS = "SUCCESS_COMPLETED";
}

/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.identity.scenarios.um.test.self.registration.api.util;

public class Constants {

    private Constants() {

    }

    public static class IdPConfigParameters {

        public static final String SELF_REGISTRATION_ENABLE = "SelfRegistration.Enable";

        public static final String SELF_REGISTRATION_LOCK_ON_CREATION = "SelfRegistration.LockOnCreation";

        public static final String SELF_REGISTRATION_NOTIFICATION_IM = "SelfRegistration.Notification.InternallyManage";

        public static final String SELF_REGISTRATION_RE_CAPTCHA = "SelfRegistration.ReCaptcha";

        public static final String SELF_REGISTRATION_CODE_EXPIRY_TIME = "SelfRegistration.VerificationCode.ExpiryTime";
    }

    public static class SelfRegistrationRequestElements {

        public static final String USER = "user";

        public static final String USERNAME = "username";

        public static final String REALM = "realm";

        public static final String PASSWORD = "password";

        public static final String CLAIMS = "claims";

        public static final String URI = "uri";

        public static final String VALUE = "value";

        public static final String PROPERTIES = "properties";

        public static final String CODE = "code";
    }

    public static class ClaimURIs {

        public static final String ACCOUNT_LOCK_CLAIM = "http://wso2.org/claims/identity/accountLocked";
    }

    public static class EndPoints {

        public static final String ME = "me";

        public static final String VALIDATE_CODE = "validate-code";

        public static final String RESEND_CODE = "resend-code";
    }
}

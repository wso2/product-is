/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.is.portal.user.client.api.unit.test.util;

/**
 * Constants used in OSGi tests.
 */
public class UserPotalOSGiTestConstants {
    public static final String PRIMARY_DOMAIN = "PRIMARY";
    public static final String PASSWORD_CALLBACK = "password";
    public static final String JAVA_SEC_SYSTEM_PROPERTY = "java.security.auth.login.config";
    public static final String CARBON_DIRECTORY_CONF = "conf";
    public static final String CARBON_DIRECTORY_SECURITY = "security";
    public static final String JAAS_CONFIG_FILE = "carbon-jaas.config";


    public static class ClaimURIs {

        public static final String WSO2_DIALECT_URI = "http://wso2.org/claims";
        public static final String USERNAME_CLAIM_URI = "http://wso2.org/claims/username";
        public static final String EMAIL_CLAIM_URI = "http://wso2.org/claims/email";
        public static final String FIRST_NAME_CLAIM_URI = "http://wso2.org/claims/firstName";
        public static final String GIVEN_NAME_CLAIM_URI = "http://wso2.org/claims/givenname";
        public static final String LAST_NAME_CLAIM_URI = "http://wso2.org/claims/lastName";
        public static final String ACCOUNT_DISABLED_CLAIM_URI = "http://wso2.org/claims/accountDisabled";
        public static final String ACCOUNT_LOCKED_CLAIM_URI = "http://wso2.org/claims/accountLocked";
        public static final String GROUP_NAME_CLAIM_URI = "http://wso2.org/claims/groupName";
        public static final String ORGANIZATION_CLAIM_URI = "http://wso2.org/claims/organization";
    }
}

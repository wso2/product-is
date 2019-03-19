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

package org.wso2.identity.scenarios.commons.util;

public class SSOConstants {

    public static class CommonAuthParams {

        public static final String SESSION_DATA_KEY = "sessionDataKey";

        public static final String SESSION_DATA_KEY_CONSENT = "sessionDataKeyConsent";

        public static final String USERNAME = "username";

        public static final String PASSWORD = "password";
    }

    public static class ApprovalType {

        public static final String APPROVE_ONCE = "approve";

        public static final String APPROVE_ALWAYS = "approveAlways";

        public static final String DENY = "deny";
    }
}

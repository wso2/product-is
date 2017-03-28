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
package org.wso2.is.portal.user.client.api.util;


import org.wso2.carbon.identity.policy.password.history.bean.PasswordPolicyBean;


/**
 * This is a utility class to get password policy configuration details
 */
public class PasswordPolicyConfigurationUtil {

    public static boolean isRegexValidation() {
        PasswordPolicyBean passwordPolicyBean = new PasswordPolicyBean();
        if (!passwordPolicyBean.getRegex().isEmpty()) {
            return true;
        } else {
            return false;
        }
    }

    public static int getPasswordMinLength() {
        PasswordPolicyBean passwordPolicyBean = new PasswordPolicyBean();
        return passwordPolicyBean.getMinLength();
    }

    public static boolean isIncludeNumbers() {
        PasswordPolicyBean passwordPolicyBean = new PasswordPolicyBean();
        return passwordPolicyBean.isIncludeNumbers();
    }

    public static boolean isIncludeUpperCase() {
        PasswordPolicyBean passwordPolicyBean = new PasswordPolicyBean();
        return passwordPolicyBean.isIncludeUpperCase();
    }

    public static boolean isIncludeLowerCase() {
        PasswordPolicyBean passwordPolicyBean = new PasswordPolicyBean();
        return passwordPolicyBean.isIncludeLowerCase();
    }

    public static boolean isIncludeSymbols() {
        PasswordPolicyBean passwordPolicyBean = new PasswordPolicyBean();
        return passwordPolicyBean.isIncludeSymbols();
    }
}


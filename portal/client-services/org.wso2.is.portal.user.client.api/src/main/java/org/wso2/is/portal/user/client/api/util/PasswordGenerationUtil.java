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

import org.wso2.is.portal.user.client.api.bean.PasswordPolicyBean;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * This is a utility class to generate password according to a given password policy.
 */
public class PasswordGenerationUtil {

    /**
     * This method generates a password according to the password policy condiguration.
     * @return
     */
    public static String getGeneratedPassword() {

        PasswordPolicyBean passwordPolicyBean = new PasswordPolicyBean();
        String lower = "abcdefghijklmnopqrstuvwxyz";
        String upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String digits = "0123456789";
        String punctuation = "!@#$%&*()_+-=[]|,./?><";

        int passwordlength = passwordPolicyBean.getMinLength() + new Random().nextInt(passwordPolicyBean.getMaxLength()
                - passwordPolicyBean.getMinLength() + 1);
        int count = 0;
        StringBuilder password = new StringBuilder(passwordlength);
        Random random = new Random(System.nanoTime());
        List<String> charCategories = new ArrayList<>(4);
        int position;
        if (passwordPolicyBean.isIncludeLowerCase()) {
            charCategories.add(lower);
            position = random.nextInt(lower.length());
            password.append(lower.charAt(position));
            count++;
        }
        if (passwordPolicyBean.isIncludeUpperCase()) {
            charCategories.add(upper);
            position = random.nextInt(upper.length());
            password.append(upper.charAt(position));
            count++;
        }
        if (passwordPolicyBean.isIncludeNumbers()) {
            charCategories.add(digits);
            position = random.nextInt(digits.length());
            password.append(digits.charAt(position));
            count++;
        }
        if (passwordPolicyBean.isIncludeSymbols()) {
            charCategories.add(punctuation);
            position = random.nextInt(punctuation.length());
            password.append(punctuation.charAt(position));
            count++;
        }
        String charCategory;

        for (int i = 0; i < passwordlength - count; i++) {

            charCategory = charCategories.get(random.nextInt(charCategories.size()));
            position = random.nextInt(charCategory.length());
            password.append(charCategory.charAt(position));
        }

        return new String(password);
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


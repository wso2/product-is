/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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
package org.wso2.carbon.is.migration.util;

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.core.util.CryptoException;
import org.wso2.carbon.core.util.CryptoUtil;

public class EncryptionUtil {

    public static String getNewEncryptedValue(String encryptedValue) throws CryptoException {
        if (StringUtils.isNotEmpty(encryptedValue) && !isNewlyEncrypted(encryptedValue)) {
            byte[] decryptedPassword = CryptoUtil.getDefaultCryptoUtil().base64DecodeAndDecrypt(encryptedValue, "RSA");
            return CryptoUtil.getDefaultCryptoUtil().encryptAndBase64Encode(decryptedPassword);
        }
        return null;
    }

    public static boolean isNewlyEncrypted(String encryptedValue) throws CryptoException {
        return CryptoUtil.getDefaultCryptoUtil().base64DecodeAndIsSelfContainedCipherText(encryptedValue);
    }

    public static String getNewEncryptedUserstorePassword(String encryptedValue) throws CryptoException {
        if (StringUtils.isNotEmpty(encryptedValue) && !isNewlyEncryptedUserstorePassword(encryptedValue)) {
            byte[] decryptedPassword = SecondaryUserstoreCryptoUtil.getInstance().base64DecodeAndDecrypt(encryptedValue, "RSA");
            return SecondaryUserstoreCryptoUtil.getInstance().encryptAndBase64Encode(decryptedPassword);
        }
        return null;
    }

    public static boolean isNewlyEncryptedUserstorePassword(String encryptedValue) throws CryptoException {
        return SecondaryUserstoreCryptoUtil.getInstance().base64DecodeAndIsSelfContainedCipherText(encryptedValue);
    }
}

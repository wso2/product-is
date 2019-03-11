/*
* Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.is.migration.service.v550.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.oauth.config.OAuthServerConfiguration;
import org.wso2.carbon.identity.oauth.tokenprocessor.EncryptionDecryptionPersistenceProcessor;
import org.wso2.carbon.identity.oauth.tokenprocessor.TokenPersistenceProcessor;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;

public class OAuth2Util {

    private static Log log = LogFactory.getLog(OAuth2Util.class);
    private static final String CIPHER_TRANSFORMATION_SYSTEM_PROPERTY = "org.wso2.CipherTransformation";

    public static boolean isEncryptionWithTransformationEnabled() throws IdentityOAuth2Exception {

        String cipherTransformation = System.getProperty(CIPHER_TRANSFORMATION_SYSTEM_PROPERTY);
        return cipherTransformation != null && isTokenEncryptionEnabled();
    }

    /**
     * This method will check whether token encryption is enabled via identity.xml
     * @return whether token encryption is enabled.
     * @throws IdentityOAuth2Exception
     */
    public static boolean isTokenEncryptionEnabled() throws IdentityOAuth2Exception {

        TokenPersistenceProcessor persistenceProcessor = OAuthServerConfiguration.getInstance()
                .getPersistenceProcessor();
        return (persistenceProcessor instanceof EncryptionDecryptionPersistenceProcessor);
    }
}

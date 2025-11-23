/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.identity.integration.test.rest.api.server.notification.sender.v2.util;

import com.google.gson.Gson;
import org.wso2.identity.integration.test.rest.api.server.notification.sender.v2.model.Authentication;
import org.wso2.identity.integration.test.rest.api.server.notification.sender.v2.model.SMSSender;

import java.io.IOException;

import static org.wso2.identity.integration.test.rest.api.common.RESTTestBase.readResource;

/**
 * Builder class for creating SMS sender request JSON strings
 */
public class SMSSenderRequestBuilder {

    /**
     * Creates an add SMS sender JSON request with the specified authentication type.
     *
     * @param authType the authentication type to use
     * @param testClass the test class to use for resource loading
     * @return JSON string for adding an SMS sender
     * @throws IOException if resource file cannot be read
     */
    public static SMSSender createAddSMSSenderJSON(Authentication.TypeEnum authType, Class<?> testClass)
            throws IOException {

        String basicJson = readResource("add-sms-sender-basic.json", testClass);
        SMSSender smsSender = new Gson().fromJson(basicJson, SMSSender.class);

        Authentication authentication = AuthenticationBuilder.createSampleAuth(authType);
        smsSender.setAuthentication(authentication);
        
        return smsSender;
    }

    /**
     * Creates an update SMS sender JSON request with the specified authentication type.
     *
     * @param authType the authentication type to use
     * @param testClass the test class to use for resource loading
     * @return JSON string for updating an SMS sender
     * @throws IOException if resource file cannot be read
     */
    public static SMSSender createUpdateSMSSenderJSON(Authentication.TypeEnum authType, Class<?> testClass)
            throws IOException {

        String basicJson = readResource("update-sms-sender-basic.json", testClass);
        SMSSender smsSender = new Gson().fromJson(basicJson, SMSSender.class);

        Authentication authentication = AuthenticationBuilder.createSampleAuth(authType);
        smsSender.setAuthentication(authentication);
        
        return smsSender;
    }

    /**
     * Creates an SMS sender object with the specified authentication type.
     *
     * @param authType the authentication type to use
     * @param testClass the test class to use for resource loading
     * @return SMSSender object
     * @throws IOException if resource file cannot be read
     */
    public static SMSSender createSMSSender(Authentication.TypeEnum authType, Class<?> testClass) 
            throws IOException {

        String basicJson = readResource("add-sms-sender-basic.json", testClass);
        SMSSender smsSender = new Gson().fromJson(basicJson, SMSSender.class);

        Authentication authentication = AuthenticationBuilder.createSampleAuth(authType);
        smsSender.setAuthentication(authentication);
        
        return smsSender;
    }
}

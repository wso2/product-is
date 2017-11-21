/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.identity.samples.sts;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;

import org.apache.ws.security.WSPasswordCallback;

public class PasswordCBHandler implements CallbackHandler{
    
    private String username;
    private String password;
    private String keyAlias;
    private String keyPassword;
    
    public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {

        readUsernamePasswordFromProperties();
        
        WSPasswordCallback pwcb = (WSPasswordCallback) callbacks[0];
        String id = pwcb.getIdentifier();
        int usage = pwcb.getUsage();

        if (usage == WSPasswordCallback.USERNAME_TOKEN) {

           if (username.equals(id)) {
               pwcb.setPassword(password);
           }
        } else if (usage == WSPasswordCallback.SIGNATURE || usage == WSPasswordCallback.DECRYPT) {

            if (keyAlias.equals(id)) {
                pwcb.setPassword(keyPassword);
            }
        }
    }
    
    public void readUsernamePasswordFromProperties() throws IOException{
        Properties properties = new Properties();
        FileInputStream freader = new FileInputStream(ClientConstants.PROPERTIES_FILE_PATH);
        properties.load(freader);
        username = properties.getProperty(ClientConstants.UT_USERNAME);
        password = properties.getProperty(ClientConstants.UT_PASSWORD);
        keyAlias = properties.getProperty(ClientConstants.USER_CERTIFICATE_ALIAS);
        keyPassword = properties.getProperty(ClientConstants.USER_CERTIFICATE_PASSWORD);
    }
}

/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.sample.inforecovery.client.authenticator;

import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.transport.http.HttpTransportProperties;

/**
 * Setting the trasnport authenticator for carbon.
 *
 * @author chamath
 */
public class ServiceAuthenticator {

    private static ServiceAuthenticator instance = null;
    private String accessUsername = null;
    private String accessPassword = null;

    private ServiceAuthenticator() {
    }

    public static ServiceAuthenticator getInstance() {

        if (instance != null) {
            return instance;
        } else {
            instance = new ServiceAuthenticator();
            return instance;
        }
    }

    public void authenticate(ServiceClient client) throws AuthenticationException {

        if (accessUsername != null && accessPassword != null) {
            Options option = client.getOptions();
            HttpTransportProperties.Authenticator auth = new HttpTransportProperties.Authenticator();
            auth.setUsername(accessUsername);
            auth.setPassword(accessPassword);
            auth.setPreemptiveAuthentication(true);
            option.setProperty(org.apache.axis2.transport.http.HTTPConstants.AUTHENTICATE, auth);
            option.setManageSession(true);

        } else {
            throw new AuthenticationException("Authentication username or password not set");
        }
    }

    public void setAccessUsername(String accessUsername) {
        this.accessUsername = accessUsername;
    }

    public void setAccessPassword(String accessPassword) {
        this.accessPassword = accessPassword;
    }

}

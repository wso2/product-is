/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.identity.integration.test.oauth.dcrm.bean;

import java.util.ArrayList;
import java.util.List;

public class ServiceProviderDataHolder {
    private String clientName;
    private String clientID;
    private String clientSecret;

    List<String> grantTypes = new ArrayList<>();
    List<String> redirectURIs = new ArrayList<>();

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getClientID() {
        return clientID;
    }

    public void setClientID(String clientID) {
        this.clientID = clientID;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public List getGrantTypes() {
        return grantTypes;
    }

    public void addGrantType(String grantType) {
        grantTypes.add(grantType);
    }

    public List getRedirectUris() {
        return redirectURIs;
    }

    public void addRedirectUri(String redirectURI) {
        redirectURIs.add(redirectURI);
    }
}

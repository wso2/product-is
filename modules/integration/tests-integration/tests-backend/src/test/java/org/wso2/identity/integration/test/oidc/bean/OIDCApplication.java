/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.identity.integration.test.oidc.bean;

import java.util.ArrayList;
import java.util.List;

public class OIDCApplication {

    private String applicationId;
    private String applicationName;
    private String clientId;
    private String clientSecret;
    private String callBackURL;
    private String subjectClaimURI;
    private String fclogoutURI;
    private List<String> requiredClaims = null;

    public OIDCApplication() {

    }

    public OIDCApplication(String applicationName, String callBackURL) {
        this.applicationName = applicationName;
        this.callBackURL = callBackURL;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getCallBackURL() {
        return callBackURL;
    }

    public void setCallBackURL(String callBackURL) {
        this.callBackURL = callBackURL;
    }

    public String getSubjectClaimURI() {
        return subjectClaimURI;
    }

    public void setSubjectClaimURI(String subjectClaimURI) {
        this.subjectClaimURI = subjectClaimURI;
    }

    public String getFclogoutURI() {
        return fclogoutURI;
    }

    public void setFclogoutURI(String fclogoutURI) {
        this.fclogoutURI = fclogoutURI;
    }

    public List<String> getRequiredClaims() {
        if (requiredClaims == null) {
            requiredClaims = new ArrayList<>();
        }
        return requiredClaims;
    }

    public void setRequiredClaims(List<String> requiredClaims) {
        this.requiredClaims = requiredClaims;
    }

    public void addRequiredClaim(String requiredClaimUri) {
        List<String> requiredClaims = getRequiredClaims();
        if (!requiredClaims.contains(requiredClaimUri)) {
            requiredClaims.add(requiredClaimUri);
        }
    }
}

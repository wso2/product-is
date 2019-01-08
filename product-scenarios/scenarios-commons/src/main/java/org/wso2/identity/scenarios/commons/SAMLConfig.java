/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.identity.scenarios.commons;

import org.apache.commons.lang.StringUtils;

import java.util.Map;

/**
 * Configuration class that holds SAML configurations.
 */
public class SAMLConfig extends TestConfig {

    private TestUserMode userMode;
    private String httpBinding;
    private String artifact;
    private String xmlSignatureAlgorithm;
    private String signatureAlgorithm;
    private String xmlDigestAlgorithm;
    private boolean signingEnabled;
    private Map<String, String[]> params;

    public SAMLConfig(TestUserMode userMode, User user, ClaimType claimType, String httpBinding,
                      Map<String, String[]> params, String artifact, String signatureAlgorithm,
                      String xmlSignatureAlgorithm, String xmlDigestAlgorithm, boolean signingEnabled) {

        super(userMode, user, claimType);
        this.userMode = userMode;
        this.httpBinding = httpBinding;
        this.artifact = artifact;
        this.signatureAlgorithm = signatureAlgorithm;
        this.xmlSignatureAlgorithm = xmlSignatureAlgorithm;
        this.xmlDigestAlgorithm = xmlDigestAlgorithm;
        this.signingEnabled = signingEnabled;
        this.params = params;
    }

    public Map<String, String[]> getParams() {

        return params;
    }

    public String getXmlSignatureAlgorithm() {

        return xmlSignatureAlgorithm;
    }

    public String getSignatureAlgorithm() {

        return signatureAlgorithm;
    }

    public String getXmlDigestAlgorithm() {

        return xmlDigestAlgorithm;
    }

    public boolean isSigningEnabled() {

        return signingEnabled;
    }

    public TestUserMode getUserMode() {

        return userMode;
    }

    public String getArtifact() {

        return artifact;
    }

    public String getHttpBinding() {

        return httpBinding;
    }

    @Override
    public String toString() {

        StringBuilder paramString = new StringBuilder();
        if (params != null && !params.isEmpty()) {
            for (Map.Entry<String, String[]> entry : params.entrySet()) {
                if (entry.getKey() != null && entry.getValue() != null && entry.getValue().length > 0) {
                    for (String param : entry.getValue()) {
                        paramString.append(entry.getKey()).append("=").append(param).append("&");
                    }
                }
            }
        }
        return "SAMLConfig[" +
                "  userMode=" + userMode.name() +
                ", user=" + super.getUser().getUsername() +
                ", httpBinding=" + httpBinding +
                ", claimType=" + super.getClaimType() +
                ", artifact=" + artifact +
                ", isSigningEnabled=" + isSigningEnabled() +
                ", signatureAlgorithm=" + signatureAlgorithm +
                ", xmlSignatureAlgorithm=" + xmlSignatureAlgorithm +
                ", xmlDigestAlgorithm=" + xmlDigestAlgorithm +
                ", artifact=" + artifact +
                ", params=" + StringUtils.removeEnd(paramString.toString(), "&") +
                ']';
    }
}




/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.sample.identity.oauth2;

import org.apache.oltu.oauth2.client.request.OAuthClientRequest;

/**
 * This extends the default <code>OAuthClientRequest.AuthenticationRequestBuilder</code> to support PKCE as apache
 * oltu doesn't support pkce as of now.
 */
public class OAuthPKCEAuthenticationRequestBuilder extends OAuthClientRequest.AuthenticationRequestBuilder {

    public OAuthPKCEAuthenticationRequestBuilder(String url) {
        super(url);
    }

    public OAuthPKCEAuthenticationRequestBuilder setPKCECodeChallenge(String codeChallenge, String method) {
        this.parameters.put(OAuth2Constants.OAUTH2_PKCE_CODE_CHALLENGE, codeChallenge);
        this.parameters.put(OAuth2Constants.OAUTH2_PKCE_CODE_CHALLENGE_METHOD, method);
        return this;
    }
}

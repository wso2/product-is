/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.identity.integration.test.oauth2.dataprovider.model;

import com.nimbusds.jwt.JWTClaimsSet;

public class AuthorizedAccessTokenContext {

    private String grantType;
    private String clientId;
    private String clientSecret;
    private String refreshToken;
    private String accessToken;

    private JWTClaimsSet accessTokenClaims;

    private AuthorizedAccessTokenContext(Builder builder) {

        this.grantType = builder.grantType;
        this.refreshToken = builder.refreshToken;
        this.accessToken = builder.accessToken;
        this.accessTokenClaims = builder.accessTokenClaims;
        this.clientId = builder.clientId;
        this.clientSecret = builder.clientSecret;

    }

    public String getGrantType() {

        return grantType;
    }

    public String getRefreshToken() {

        return refreshToken;
    }

    public String getAccessToken() {

        return accessToken;
    }

    public JWTClaimsSet getAccessTokenClaims() {

        return accessTokenClaims;
    }

    public String getClientId() {

        return clientId;
    }

    public String getClientSecret() {

        return clientSecret;
    }

    public static class Builder {

        private String grantType;
        private String clientId;
        private String clientSecret;
        private String refreshToken;
        private String accessToken;

        private JWTClaimsSet accessTokenClaims;

        public Builder grantType(String grantType) {

            this.grantType = grantType;
            return this;
        }

        public Builder refreshToken(String refreshToken) {

            this.refreshToken = refreshToken;
            return this;
        }

        public Builder accessToken(String accessToken) {

            this.accessToken = accessToken;
            return this;
        }

        public Builder accessTokenClaims(JWTClaimsSet accessTokenClaims) {

            this.accessTokenClaims = accessTokenClaims;
            return this;
        }

        public Builder clientId(String clientId) {

            this.clientId = clientId;
            return this;
        }

        public Builder clientSecret(String clientSecret) {

            this.clientSecret = clientSecret;
            return this;
        }

        public AuthorizedAccessTokenContext build() {

            return new AuthorizedAccessTokenContext(this);
        }
    }

}

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

package org.wso2.identity.integration.test.serviceextensions.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Objects;

/**
 * This class models the ID Token sent in the request payload to the API endpoint of Pre Issue ID Token action.
 */
public class IDToken {

    private List<String> scopes;
    private List<Claim> claims;

    public IDToken() {

    }

    public IDToken(List<String> scopes, List<Claim> claims) {

        this.scopes = scopes;
        this.claims = claims;
    }

    @JsonProperty("scopes")
    public List<String> getScopes() {

        return scopes;
    }

    public void setScopes(List<String> scopes) {

        this.scopes = scopes;
    }

    @JsonProperty("claims")
    public List<Claim> getClaims() {

        return claims;
    }

    public void setClaims(List<Claim> claims) {

        this.claims = claims;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        IDToken idToken = (IDToken) o;
        return Objects.equals(scopes, idToken.scopes) &&
                Objects.equals(claims, idToken.claims);
    }

    @Override
    public int hashCode() {

        return Objects.hash(scopes, claims);
    }

    /**
     * Enum for ID token claim names.
     */
    public enum ClaimNames {

        ISS("iss"),
        AT_HASH("at_hash"),
        C_HASH("c_hash"),
        S_HASH("s_hash"),
        SESSION_ID_CLAIM("sid"),
        EXPIRES_IN("expires_in"),
        REALM("realm"),
        TENANT("tenant"),
        USERSTORE("userstore"),
        IDP_SESSION_KEY("isk"),
        SUB("sub"),
        AUD("aud"),
        EXP("exp"),
        IAT("iat"),
        AUTH_TIME("auth_time"),
        NONCE("nonce"),
        ACR("acr"),
        AMR("amr"),
        AZP("azp"),
        JTI("jti");

        private final String name;

        ClaimNames(String name) {

            this.name = name;
        }

        public String getName() {

            return name;
        }
    }

    /**
     * This class models a claim in the ID Token.
     */
    public static class Claim {

        private String name;
        private Object value;

        public Claim() {

        }

        public Claim(String name, Object value) {

            this.name = name;
            this.value = value;
        }

        @JsonProperty("name")
        public String getName() {

            return name;
        }

        public void setName(String name) {

            this.name = name;
        }

        @JsonProperty("value")
        public Object getValue() {

            return value;
        }

        public void setValue(Object value) {

            this.value = value;
        }

        @Override
        public boolean equals(Object o) {

            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Claim claim = (Claim) o;
            return Objects.equals(name, claim.name) &&
                    Objects.equals(value, claim.value);
        }

        @Override
        public int hashCode() {

            return Objects.hash(name, value);
        }
    }
}


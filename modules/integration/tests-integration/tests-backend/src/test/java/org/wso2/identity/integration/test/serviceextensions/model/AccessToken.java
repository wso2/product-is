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

package org.wso2.identity.integration.test.serviceextensions.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * This class represents the model of the access token sent in the request payload
 * to the API endpoint of the pre issue access token action.
 */
@JsonDeserialize(builder = AccessToken.Builder.class)
public class AccessToken {

    private final String tokenType;
    List<String> scopes;
    List<Claim> claims;

    private AccessToken(Builder builder) {

        this.tokenType = builder.tokenType;
        this.scopes = builder.scopes;
        this.claims = builder.claims;
    }

    public String getTokenType() {

        return tokenType;
    }

    public List<String> getScopes() {

        return scopes;
    }

    public List<Claim> getClaims() {

        return claims;
    }

    public Claim getClaim(String name) {

        if (claims != null) {
            for (Claim claim : claims) {
                if (claim.getName().equals(name)) {
                    return claim;
                }
            }
        }

        return null;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AccessToken that = (AccessToken) o;
        return Objects.equals(tokenType, that.tokenType) && (scopes == null ? that.scopes == null :
                scopes.size() == that.scopes.size() && scopes.containsAll(that.scopes)) &&
                (claims == null ? that.claims == null :
                        claims.size() == that.claims.size() && claims.containsAll(that.claims));
    }

    @Override
    public int hashCode() {

        return Objects.hash(tokenType, scopes, claims);
    }

    /**
     * Enum for standard claim names.
     */
    public enum ClaimNames {

        SUB("sub"),
        ISS("iss"),
        AUD("aud"),
        CLIENT_ID("client_id"),
        AUTHORIZED_USER_TYPE("aut"),
        EXPIRES_IN("expires_in"),

        TOKEN_BINDING_REF("binding_ref"),
        TOKEN_BINDING_TYPE("binding_type"),
        SUBJECT_TYPE("subject_type");

        private final String name;

        ClaimNames(String name) {

            this.name = name;
        }

        public static boolean contains(String name) {

            return Arrays.stream(ClaimNames.values())
                    .anyMatch(claimName -> claimName.name.equals(name));
        }

        public String getName() {

            return name;
        }
    }

    /**
     * Model class for claims.
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

        public String getName() {

            return name;
        }

        public void setName(String name) {

            this.name = name;
        }

        public Object getValue() {

            return value;
        }

        public void setValue(Object value) {

            this.value = value;
        }

        @Override
        public boolean equals(Object o) {

            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Claim claim = (Claim) o;
            return Objects.equals(name, claim.name) && Objects.equals(value, claim.value);
        }

        @Override
        public int hashCode() {

            return Objects.hash(name, value);
        }
    }

    /**
     * Builder for AccessToken.
     */
    @JsonPOJOBuilder(withPrefix = "")
    public static class Builder {

        private String tokenType;
        private List<String> scopes = new ArrayList<>();
        private List<Claim> claims = new ArrayList<>();

        public Builder tokenType(String tokenType) {

            this.tokenType = tokenType;
            return this;
        }

        public Builder scopes(List<String> scopes) {

            this.scopes = scopes;
            return this;
        }

        public Builder claims(List<Claim> claims) {

            this.claims = claims;
            return this;
        }

        public Builder addClaim(String name, Object value) {

            this.claims.add(new Claim(name, value));
            return this;
        }

        public Builder addScope(String scope) {

            this.scopes.add(scope);
            return this;
        }

        public String getTokenType() {

            return tokenType;
        }

        public List<String> getScopes() {

            return scopes;
        }

        public List<Claim> getClaims() {

            return claims;
        }

        public Claim getClaim(String name) {

            if (claims != null) {
                for (Claim claim : claims) {
                    if (claim.getName().equals(name)) {
                        return claim;
                    }
                }
            }

            return null;
        }

        public AccessToken build() {

            return new AccessToken(this);
        }
    }
}

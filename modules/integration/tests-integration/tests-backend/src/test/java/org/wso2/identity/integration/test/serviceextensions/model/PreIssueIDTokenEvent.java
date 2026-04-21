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
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import java.util.Objects;

/**
 * This class models the Pre Issue ID Token Event sent in the request payload to the API endpoint of
 * Pre Issue ID Token action.
 */
@JsonDeserialize(builder = PreIssueIDTokenEvent.Builder.class)
public class PreIssueIDTokenEvent extends Event {

    private TokenRequest request;
    private IDToken idToken;
    private Tenant tenant;
    private Organization organization;
    private User user;
    private UserStore userStore;

    private PreIssueIDTokenEvent(Builder builder) {

        this.request = builder.request;
        this.idToken = builder.idToken;
        this.tenant = builder.tenant;
        this.organization = builder.organization;
        this.user = builder.user;
        this.userStore = builder.userStore;
    }

    public PreIssueIDTokenEvent() {

    }

    @JsonProperty("request")
    public TokenRequest getRequest() {

        return request;
    }

    @JsonProperty("idToken")
    public IDToken getIdToken() {

        return idToken;
    }

    @JsonProperty("tenant")
    public Tenant getTenant() {

        return tenant;
    }

    @JsonProperty("organization")
    public Organization getOrganization() {

        return organization;
    }

    @JsonProperty("user")
    public User getUser() {

        return user;
    }

    @JsonProperty("userStore")
    public UserStore getUserStore() {

        return userStore;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PreIssueIDTokenEvent that = (PreIssueIDTokenEvent) o;
        return Objects.equals(request, that.request) &&
                Objects.equals(idToken, that.idToken) &&
                Objects.equals(tenant, that.tenant) &&
                Objects.equals(organization, that.organization) &&
                Objects.equals(user, that.user) &&
                Objects.equals(userStore, that.userStore);
    }

    @Override
    public int hashCode() {

        return Objects.hash(request, idToken, tenant, organization, user, userStore);
    }

    /**
     * Builder class for PreIssueIDTokenEvent.
     */
    @JsonPOJOBuilder(withPrefix = "")
    public static class Builder {

        private TokenRequest request;
        private IDToken idToken;
        private Tenant tenant;
        private Organization organization;
        private User user;
        private UserStore userStore;

        @JsonProperty("request")
        public Builder request(TokenRequest request) {

            this.request = request;
            return this;
        }

        @JsonProperty("idToken")
        public Builder idToken(IDToken idToken) {

            this.idToken = idToken;
            return this;
        }

        @JsonProperty("tenant")
        public Builder tenant(Tenant tenant) {

            this.tenant = tenant;
            return this;
        }

        @JsonProperty("organization")
        public Builder organization(Organization organization) {

            this.organization = organization;
            return this;
        }

        @JsonProperty("user")
        public Builder user(User user) {

            this.user = user;
            return this;
        }

        @JsonProperty("userStore")
        public Builder userStore(UserStore userStore) {

            this.userStore = userStore;
            return this;
        }

        public PreIssueIDTokenEvent build() {

            return new PreIssueIDTokenEvent(this);
        }
    }
}


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

import java.util.Objects;

/**
 * This class models the Event sent in the request payload to the API endpoint of a pre issue access token action.
 */
@JsonDeserialize(builder = PreIssueAccessTokenEvent.Builder.class)
public class PreIssueAccessTokenEvent extends Event {

    private TokenRequest request;
    private final AccessToken accessToken;

    private PreIssueAccessTokenEvent(Builder builder) {

        this.accessToken = builder.accessToken;
        this.request = builder.request;
        this.organization = builder.organization;
        this.tenant = builder.tenant;
        this.user = builder.user;
        this.userStore = builder.userStore;
    }

    public TokenRequest getRequest() {

        return request;
    }

    public AccessToken getAccessToken() {

        return accessToken;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PreIssueAccessTokenEvent that = (PreIssueAccessTokenEvent) o;

        boolean isEqualGeneral = Objects.equals(request, that.request) &&
                Objects.equals(accessToken, that.accessToken) &&
                Objects.equals(tenant, that.tenant);

        if (!"client_credentials".equals(that.request.getGrantType())) {
            return isEqualGeneral &&
                    Objects.equals(user, that.user) &&
                    Objects.equals(userStore, that.userStore);
        }
        return isEqualGeneral;
    }

    @Override
    public int hashCode() {

        return Objects.hash(request, accessToken, tenant, user, userStore);
    }

    /**
     * Builder for the PreIssueAccessTokenEvent.
     */
    @JsonPOJOBuilder(withPrefix = "")
    public static class Builder {

        private AccessToken accessToken;
        private TokenRequest request;
        private Organization organization;
        private Tenant tenant;
        private User user;

        private UserStore userStore;

        public Builder accessToken(AccessToken accessToken) {

            this.accessToken = accessToken;
            return this;
        }

        public Builder request(TokenRequest request) {

            this.request = request;
            return this;
        }

        public Builder organization(Organization organization) {

            this.organization = organization;
            return this;
        }

        public Builder tenant(Tenant tenant) {

            this.tenant = tenant;
            return this;
        }

        public Builder user(User user) {

            this.user = user;
            return this;
        }

        public Builder userStore(UserStore userStore) {

            this.userStore = userStore;
            return this;
        }

        public PreIssueAccessTokenEvent build() {

            return new PreIssueAccessTokenEvent(this);
        }
    }
}

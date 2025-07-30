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

import java.util.Collections;
import java.util.List;

/**
 * This class models the User sent in the request payload to the API endpoint of a particular action.
 */
@JsonDeserialize(builder = User.Builder.class)
public class User {

    private String id;
    private List<? extends UserClaim> claims;
    private List<String> groups;
    private List<String> roles;
    private Organization organization;

    public User(String id) {

        this.id = id;
    }

    public User(Builder builder) {

        this.id = builder.id;
        this.claims = builder.claims;
        this.groups = builder.groups;
        this.roles = builder.roles;
        this.organization = builder.organization;
    }

    public String getId() {

        return id;
    }

    public List<UserClaim> getClaims() {

        return claims != null ? Collections.unmodifiableList(claims) : null;
    }

    public List<String> getGroups() {

        return groups != null ? Collections.unmodifiableList(groups) : null;
    }

    public List<String> getRoles() {

        return roles != null ? Collections.unmodifiableList(roles) : null;
    }

    public Organization getOrganization() {

        return organization;
    }

    /**
     * Builder for the User.
     */
    @JsonPOJOBuilder(withPrefix = "")
    public static class Builder {

        private String id;
        private List<? extends UserClaim> claims;
        private List<String> groups;
        private List<String> roles;
        private Organization organization;

        public Builder id(String id) {

            this.id = id;
            return this;
        }

        public Builder claims(List<? extends UserClaim> claims) {

            this.claims = claims;
            return this;
        }

        public Builder groups(List<String> groups) {

            this.groups = groups;
            return this;
        }

        public Builder roles(List<String> roles) {

            this.roles = roles;
            return this;
        }

        public Builder organization(Organization organization) {

            this.organization = organization;
            return this;
        }

        public User build() {

            return new User(this);
        }
    }
}

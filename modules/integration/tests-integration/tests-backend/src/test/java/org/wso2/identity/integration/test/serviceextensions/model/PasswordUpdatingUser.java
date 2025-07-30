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

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import org.wso2.carbon.identity.action.execution.api.exception.ActionExecutionRequestBuilderException;

import java.util.List;
import java.util.Objects;

@JsonDeserialize(builder = PasswordUpdatingUser.Builder.class)
public class PasswordUpdatingUser extends User {

    private final Credential updatingCredential;

    private PasswordUpdatingUser(Builder builder) {

        super(new User.Builder(builder.id)
                .claims(builder.claims)
                .groups(builder.groups)
                .organization(builder.organization));
        this.updatingCredential = builder.updatingCredential;
    }

    public Credential getUpdatingCredential() {

        return updatingCredential;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PasswordUpdatingUser that = (PasswordUpdatingUser) o;

        return Objects.equals(updatingCredential, that.updatingCredential);

    }

    @Override
    public int hashCode() {

        return Objects.hash(updatingCredential);
    }

    /**
     * Builder for TokenRequest.
     */
    @JsonPOJOBuilder(withPrefix = "")
    public static class Builder {

        private String id;
        private List<UserClaim> claims;
        private List<String> groups;
        private Organization organization;
        private Credential updatingCredential;

        public Builder id(String id) {

            this.id = id;
            return this;
        }

        public Builder claims(List<? extends UserClaim> claims) {

            this.claims.addAll(claims);
            return this;
        }

        public Builder groups(List<String> groups) {

            this.groups.addAll(groups);
            return this;
        }

        public Builder organization(Organization organization) {

            this.organization = organization;
            return this;
        }

        public Builder updatingCredential(Credential updatingCredential) {

            this.updatingCredential = updatingCredential;
            return this;
        }

        public PasswordUpdatingUser build() throws ActionExecutionRequestBuilderException {

            return new PasswordUpdatingUser(this);
        }
    }
}

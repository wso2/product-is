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

package org.wso2.identity.integration.test.serviceextensions.common.execution.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import org.wso2.carbon.identity.action.execution.api.exception.ActionExecutionRequestBuilderException;

import java.util.List;
import java.util.Objects;

/**
 * Represents the model for user updating their profile.
 */
@JsonDeserialize(builder = ProfileUpdatingUser.Builder.class)
public class ProfileUpdatingUser extends User {

    private ProfileUpdatingUser(ProfileUpdatingUser.Builder builder) {

        super(new User.Builder()
                .id(builder.id)
                .claims(builder.claims)
                .groups(builder.groups)
                .organization(builder.organization));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ProfileUpdatingUser that = (ProfileUpdatingUser) o;

        return Objects.equals(getId(), that.getId()) &&
                Objects.equals(getClaims(), that.getClaims()) &&
                Objects.equals(getGroups(), that.getGroups()) &&
                Objects.equals(getOrganization(), that.getOrganization());
    }

    /**
     * Builder for ProfileUpdatingUser.
     */
    @JsonPOJOBuilder(withPrefix = "")
    public static class Builder {

        private String id;
        private List<UpdatingUserClaim> claims;
        private List<String> groups;
        private Organization organization;

        public Builder id(String id) {

            this.id = id;
            return this;
        }

        public Builder claims(List<UpdatingUserClaim> claims) {

            this.claims = claims;
            return this;
        }

        public Builder groups(List<String> groups) {

            this.groups = groups;
            return this;
        }

        public Builder organization(Organization organization) {

            this.organization = organization;
            return this;
        }

        public ProfileUpdatingUser build() throws ActionExecutionRequestBuilderException {

            return new ProfileUpdatingUser(this);
        }
    }
}


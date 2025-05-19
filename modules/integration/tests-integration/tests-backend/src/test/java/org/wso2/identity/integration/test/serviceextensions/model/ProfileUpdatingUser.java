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

import java.util.Objects;

/**
 * Represents the model for user updating their profile.
 */
@JsonDeserialize(builder = ProfileUpdatingUser.Builder.class)
public class ProfileUpdatingUser extends User {

    private final Claim[] claims;

    private ProfileUpdatingUser(ProfileUpdatingUser.Builder builder) {

        super(builder.id);
        this.claims = builder.claims;
    }

    public Claim[] getClaims() {

        return claims;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ProfileUpdatingUser that = (ProfileUpdatingUser) o;

        return Objects.equals(claims, that.claims);

    }

    @Override
    public int hashCode() {

        return Objects.hash(claims);
    }

    /**
     * Builder for ProfileUpdatingUser.
     */
    @JsonPOJOBuilder(withPrefix = "")
    public static class Builder {

        private String id;
        private Claim[] claims;

        public ProfileUpdatingUser.Builder id(String id) {

            this.id = id;
            return this;
        }

        public ProfileUpdatingUser.Builder claims(Claim[] claims) {

            this.claims = claims;
            return this;
        }

        public ProfileUpdatingUser build() throws ActionExecutionRequestBuilderException {

            return new ProfileUpdatingUser(this);
        }
    }
}


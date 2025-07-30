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
import org.wso2.carbon.identity.action.execution.api.exception.ActionExecutionRequestBuilderException;
import org.wso2.carbon.identity.action.execution.api.model.Request;

import java.util.List;
import java.util.Objects;

/**
 * This class models the Pre Profile Update Request
 */
@JsonDeserialize(builder = PreUpdateProfileRequest.Builder.class)
public class PreUpdateProfileRequest extends Request {

    private final List<UpdatingUserClaim> claims;

    private PreUpdateProfileRequest(PreUpdateProfileRequest.Builder builder) {

        this.claims = builder.claims;
    }

    public List<UpdatingUserClaim> getClaims() {

        return claims;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PreUpdateProfileRequest that = (PreUpdateProfileRequest) o;

        return Objects.equals(claims, that.claims);

    }

    @Override
    public int hashCode() {

        return Objects.hash(claims);
    }

    /**
     * Builder for PreUpdateProfileRequest.
     */
    @JsonPOJOBuilder(withPrefix = "")
    public static class Builder {

        private List<UpdatingUserClaim> claims;

        public Builder claims(List<UpdatingUserClaim> claims) {

            this.claims = claims;
            return this;
        }

        public PreUpdateProfileRequest build() throws ActionExecutionRequestBuilderException {

            return new PreUpdateProfileRequest(this);
        }
    }
}


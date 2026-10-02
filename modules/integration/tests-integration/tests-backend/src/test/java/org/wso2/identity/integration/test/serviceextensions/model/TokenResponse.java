/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
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
import java.util.HashSet;
import java.util.List;

/**
 * This class models the token endpoint response sent in the event payload of a pre issue access token action
 * request. It carries the manifest of top level parameter names that will be present on the token endpoint's
 * response.
 */
@JsonDeserialize(builder = TokenResponse.Builder.class)
public class TokenResponse {

    private final List<String> parameters;

    private TokenResponse(Builder builder) {

        this.parameters = builder.parameters;
    }

    public List<String> getParameters() {

        return parameters;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TokenResponse that = (TokenResponse) o;
        return parameters == null ? that.parameters == null :
                parameters.size() == that.parameters.size() && parameters.containsAll(that.parameters);
    }

    @Override
    public int hashCode() {

        return parameters == null ? 0 : new HashSet<>(parameters).hashCode();
    }

    /**
     * Builder for TokenResponse.
     */
    @JsonPOJOBuilder(withPrefix = "")
    public static class Builder {

        private List<String> parameters = new ArrayList<>();

        public Builder parameters(List<String> parameters) {

            this.parameters = parameters;
            return this;
        }

        public TokenResponse build() {

            return new TokenResponse(this);
        }
    }
}

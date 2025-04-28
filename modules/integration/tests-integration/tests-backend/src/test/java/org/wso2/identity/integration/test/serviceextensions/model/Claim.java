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

import java.util.Objects;

/**
 * Represents a claim.
 */
@JsonDeserialize(builder = Claim.Builder.class)
public class Claim {

    private final String uri;
    private final String updatingValue;
    private final String value;

    private Claim(Claim.Builder builder) {

        this.uri = builder.uri;
        this.updatingValue = builder.updatingValue;
        this.value = builder.value;
    }

    public String getUri() {

        return uri;
    }

    public String getUpdatingValue() {

        return updatingValue;
    }

    public String getValue() {

        return value;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Claim that = (Claim) o;

        return Objects.equals(uri, that.uri) && Objects.equals(updatingValue, that.updatingValue) &&
                Objects.equals(value, that.value) ;
    }

    @Override
    public int hashCode() {

        return Objects.hash(uri, value);
    }


    /**
     * Builder for claims.
     */
    @JsonPOJOBuilder(withPrefix = "")
    public static class Builder {

        private String uri;
        private String updatingValue;
        private String value;

        public Claim.Builder uri(String uri) {

            this.uri = uri;
            return this;
        }

        public Claim.Builder updatingValue(String updatingValue) {

            this.updatingValue = updatingValue;
            return this;
        }

        public Claim.Builder value(String value) {

            this.value = value;
            return this;
        }

        public Claim build() {

            return new Claim(this);
        }
    }
}

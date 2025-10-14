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

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.io.IOException;

/**
 * Represents a claim.
 */
@JsonDeserialize(builder = UpdatingUserClaim.Builder.class)
public class UpdatingUserClaim extends UserClaim {

    @JsonSerialize(using = UpdatingValueSerializer.class)
    private Object updatingValue;

    public UpdatingUserClaim(String uri, String value) {

        super(uri, value);
    }

    public UpdatingUserClaim(String uri, String[] value) {

        super(uri, value);
    }

    public UpdatingUserClaim(String uri, String value, String updatingValue) {

        super(uri, value);
        this.updatingValue = updatingValue;
    }

    public UpdatingUserClaim(String uri, String[] value, String[] updatingValue) {

        super(uri, value);
        this.updatingValue = updatingValue;
    }

    public Object getUpdatingValue() {

        return updatingValue;
    }

    private static class UpdatingValueSerializer extends JsonSerializer<Object> {

        @Override
        public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers) throws IOException {

            if (value instanceof String) {
                gen.writeString((String) value);
            } else if (value instanceof String[]) {
                gen.writeArray((String[]) value, 0, ((String[]) value).length);
            } else {
                throw new IOException("Unsupported type for serialization: " + value.getClass());
            }
        }
    }

    /**
     * Builder for claims.
     */
    @JsonPOJOBuilder(withPrefix = "")
    public static class Builder {

        private String uri;
        private Object updatingValue;
        private Object value;

        public Builder uri(String uri) {

            this.uri = uri;
            return this;
        }

        public Builder updatingValue(Object updatingValue) {

            this.updatingValue = updatingValue;
            return this;
        }

        public Builder value(Object value) {

            this.value = value;
            return this;
        }

        public UpdatingUserClaim build() {

            if (updatingValue == null) {
                if (value instanceof String) {
                    return new UpdatingUserClaim(uri, (String) value);
                } else if (value instanceof String[]) {
                    return new UpdatingUserClaim(uri, (String[]) value);
                }
            } else {
                if (value instanceof String[] && updatingValue instanceof String[]) {
                    return new UpdatingUserClaim(uri, (String[]) value, (String[]) updatingValue);
                } else if (value instanceof String && updatingValue instanceof String) {
                    return new UpdatingUserClaim(uri, (String) value, (String) updatingValue);
                }
            }
            return null;
        }
    }
}

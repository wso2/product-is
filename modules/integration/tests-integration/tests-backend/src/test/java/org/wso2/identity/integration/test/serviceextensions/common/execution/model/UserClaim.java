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
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.io.IOException;

/**
 * This class models the user claims.
 * User claim is the entity that represents the user claims of the user for whom the action is triggered for.
 */
public class UserClaim {

    private final String uri;

    @JsonSerialize(using = ClaimValueSerializer.class)
    private final Object value;

    public UserClaim(String uri, String[] value) {

        this.uri = uri;
        this.value = value;
    }

    public UserClaim(String uri, String value) {

        this.uri = uri;
        this.value = value;
    }

    /**
     * Retrieve value of the user claim.
     *
     * @return Claim value.
     */
    public Object getValue() {

        return value;
    }

    /**
     * Retrieve URI of the user claim.
     *
     * @return Claim URI.
     */
    public String getUri() {

        return uri;
    }

    private static class ClaimValueSerializer extends JsonSerializer<Object> {

        @Override
        public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            if (value instanceof String) {
                // Serialize as string
                gen.writeString((String) value);
            } else if (value instanceof String[]) {
                // Serialize as array
                gen.writeArray((String[]) value, 0, ((String[]) value).length);
            } else {
                throw new IOException("Unsupported type for serialization: " + value.getClass());
            }
        }
    }
}

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

package org.wso2.identity.integration.test.actions.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import java.util.Arrays;
import java.util.Objects;

/**
 * Represents an unencrypted credential.
 */
@JsonDeserialize(builder = Credential.Builder.class)
public class Credential {

    /**
     * Type Enum.
     * Defines the type of the credential.
     */
    public enum Type {
        PASSWORD
    }

    /**
     * Format Enum.
     * Defines the format of updating the credential.
     */
    public enum Format {
        HASH,
        PLAIN_TEXT
    }

    /**
     * Algorithm Enum.
     * Defines the algorithm used to hash the credential.
     */
    public enum Algorithm {
        SHA256
    }

    private final Type type;
    private final Format format;
    private final char[] value;
    private final AdditionalData additionalData;

    private Credential(Builder builder) {
        this.type = builder.type;
        this.format = builder.format;
        this.value = builder.value;
        this.additionalData = builder.algorithm != null ? new AdditionalData(builder.algorithm) : null;
    }

    public Type getType() {
        return type;
    }

    public Format getFormat() {
        return format;
    }

    public char[] getValue() {
        return value;
    }

    public AdditionalData getAdditionalData() {
        return additionalData;
    }

    /**
     * AdditionalData class.
     */
    public static class AdditionalData {

        private final Algorithm algorithm;

        public AdditionalData(Algorithm algorithm) {

            this.algorithm = algorithm;
        }

        public Algorithm getAlgorithm() {
            return algorithm;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Credential that = (Credential) o;

        return Objects.equals(type, that.type) &&
                Objects.equals(format, that.format) &&
                Objects.equals(value, that.value) &&
                Objects.equals(additionalData, that.additionalData);

    }

    @Override
    public int hashCode() {

        return Objects.hash(type, format, Arrays.hashCode(value), additionalData);
    }


    /**
     * Builder for updatingCredential.
     */
    @JsonPOJOBuilder(withPrefix = "")
    public static class Builder {

        private Type type;
        private Format format;
        private char[] value;
        private Algorithm algorithm;

        public Builder type(Type type) {
            this.type = type;
            return this;
        }

        public Builder format(Format format) {
            this.format = format;
            return this;
        }

        public Builder value(char[] value) {
            this.value = value;
            return this;
        }

        public Builder algorithm(Algorithm algorithm) {
            this.algorithm = algorithm;
            return this;
        }

        public Credential build() {
            return new Credential(this);
        }
    }
}

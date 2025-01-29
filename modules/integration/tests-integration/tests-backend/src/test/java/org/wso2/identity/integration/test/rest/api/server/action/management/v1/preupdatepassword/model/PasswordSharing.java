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

package org.wso2.identity.integration.test.rest.api.server.action.management.v1.preupdatepassword.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

/**
 * Password Sharing.
 */
public class PasswordSharing {

    /**
     * Enum for password sharing format.
     */
    @XmlType(name = "FormatEnum")
    @XmlEnum(String.class)
    public enum FormatEnum {

        @XmlEnumValue("PLAIN_TEXT") PLAIN_TEXT(String.valueOf("PLAIN_TEXT")),
        @XmlEnumValue("SHA256_HASHED") SHA256_HASHED(String.valueOf("SHA256_HASHED"));
        
        private final String value;

        FormatEnum(String v) {
            value = v;
        }

        public String value() {
            return value;
        }

        @Override
        public String toString() {
            return String.valueOf(value);
        }

        public static FormatEnum fromValue(String value) {
            for (FormatEnum b : FormatEnum.values()) {
                if (b.value.equals(value)) {
                    return b;
                }
            }
            throw new IllegalArgumentException("Unexpected value '" + value + "'");
        }
    }

    private FormatEnum format;
    private String certificate;
    
    public PasswordSharing format(FormatEnum format) {

        this.format = format;
        return this;
    }

    @ApiModelProperty(example = "Plain Text", required = true)
    @JsonProperty("format")
    @Valid
    @NotNull(message = "Property format cannot be null.")
    public FormatEnum getFormat() {
        
        return format;
    }
    public void setFormat(FormatEnum format) {
        
        this.format = format;
    }

    public PasswordSharing certificate(String certificate) {

        this.certificate = certificate;
        return this;
    }

    @ApiModelProperty()
    @JsonProperty("certificate")
    @Valid
    public String getCertificate() {

        return certificate;
    }

    public void setCertificate(String certificate) {

        this.certificate = certificate;
    }

    @Override
    public boolean equals(java.lang.Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PasswordSharing passwordSharing = (PasswordSharing) o;
        return Objects.equals(this.format, passwordSharing.format) &&
                Objects.equals(this.certificate, passwordSharing.certificate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(format, certificate);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class PasswordSharing {\n");
        sb.append("    format: ").append(toIndentedString(format)).append("\n");
        sb.append("    certificate: ").append(toIndentedString(certificate)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(java.lang.Object o) {

        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}

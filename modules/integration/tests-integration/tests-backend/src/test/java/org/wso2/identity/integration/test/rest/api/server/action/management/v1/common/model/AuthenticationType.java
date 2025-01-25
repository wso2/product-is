/*
 * Copyright (c) 2024-2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.identity.integration.test.rest.api.server.action.management.v1.common.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

public class AuthenticationType {

    @XmlType(name="TypeEnum")
    @XmlEnum(String.class)
    public enum TypeEnum {

        @XmlEnumValue("NONE") NONE(String.valueOf("NONE")), @XmlEnumValue("BEARER") BEARER(String.valueOf("BEARER")), @XmlEnumValue("API_KEY") API_KEY(String.valueOf("API_KEY")), @XmlEnumValue("BASIC") BASIC(String.valueOf("BASIC"));

        private String value;

        TypeEnum(String v) {
            value = v;
        }

        public String value() {
            return value;
        }

        @Override
        public String toString() {
            return String.valueOf(value);
        }

        public static TypeEnum fromValue(String value) {
            for (TypeEnum b : TypeEnum.values()) {
                if (b.value.equals(value)) {
                    return b;
                }
            }
            throw new IllegalArgumentException("Unexpected value '" + value + "'");
        }
    }

    private TypeEnum type;
    private Map<String, Object> properties = null;

    /**
     **/
    public AuthenticationType type(TypeEnum type) {

        this.type = type;
        return this;
    }

    @ApiModelProperty(example = "BASIC", required = true, value = "")
    @JsonProperty("type")
    @Valid
    @NotNull(message = "Property type cannot be null.")

    public TypeEnum getType() {
        return type;
    }
    public void setType(TypeEnum type) {
        this.type = type;
    }

    /**
     **/
    public AuthenticationType properties(Map<String, Object> properties) {

        this.properties = properties;
        return this;
    }

    @ApiModelProperty(example = "{\"username\":\"auth_username\",\"password\":\"auth_username\"}", value = "")
    @JsonProperty("properties")
    @Valid
    public Map<String, Object> getProperties() {
        return properties;
    }
    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }


    public AuthenticationType putPropertiesItem(String key, Object propertiesItem) {
        if (this.properties == null) {
            this.properties = new HashMap<String, Object>();
        }
        this.properties.put(key, propertiesItem);
        return this;
    }

    @Override
    public boolean equals(java.lang.Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AuthenticationType authenticationType = (AuthenticationType) o;
        return Objects.equals(this.type, authenticationType.type) &&
                Objects.equals(this.properties, authenticationType.properties);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, properties);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class AuthenticationType {\n");
        sb.append("    type: ").append(toIndentedString(type)).append("\n");
        sb.append("    properties: ").append(toIndentedString(properties)).append("\n");
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
        return o.toString();
    }
}

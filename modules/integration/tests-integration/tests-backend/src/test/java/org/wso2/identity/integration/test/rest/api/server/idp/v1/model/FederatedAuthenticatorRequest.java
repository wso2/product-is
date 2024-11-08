/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.identity.integration.test.rest.api.server.idp.v1.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.OpenIDConnectConfiguration;

import javax.validation.Valid;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FederatedAuthenticatorRequest {
    private String defaultAuthenticatorId;
    private List<FederatedAuthenticator> authenticators;

    /**
     *
     **/
    public FederatedAuthenticatorRequest defaultAuthenticatorId(String defaultAuthenticatorId) {

        this.defaultAuthenticatorId = defaultAuthenticatorId;
        return this;
    }

    @ApiModelProperty()
    @JsonProperty("defaultAuthenticatorId")
    @Valid
    public String getDefaultAuthenticatorId() {
        return defaultAuthenticatorId;
    }

    public void setDefaultAuthenticatorId(String defaultAuthenticatorId) {
        this.defaultAuthenticatorId = defaultAuthenticatorId;
    }

    /**
     *
     **/
    public FederatedAuthenticatorRequest authenticators(List<FederatedAuthenticator> authenticators) {

        this.authenticators = authenticators;
        return this;
    }

    @ApiModelProperty()
    @JsonProperty("authenticators")
    @Valid
    public List<FederatedAuthenticator> getAuthenticators() {
        return authenticators;
    }

    public void setAuthenticators(List<FederatedAuthenticator> authenticators) {
        this.authenticators = authenticators;
    }

    public FederatedAuthenticatorRequest addAuthenticator(FederatedAuthenticator authenticator) {
        if (this.authenticators == null) {
            this.authenticators = new ArrayList<>();
        }
        this.authenticators.add(authenticator);
        return this;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FederatedAuthenticatorRequest federatedAuthenticatorRequest = (FederatedAuthenticatorRequest) o;
        return Objects.equals(this.defaultAuthenticatorId, federatedAuthenticatorRequest.defaultAuthenticatorId) &&
                Objects.equals(this.authenticators, federatedAuthenticatorRequest.authenticators);
    }

    @Override
    public int hashCode() {
        return Objects.hash(defaultAuthenticatorId, authenticators);
    }

    @Override
    public String toString() {

        return "class FederatedAuthenticatorRequest {\n" +
                "    defaultAuthenticatorId: " + toIndentedString(defaultAuthenticatorId) + "\n" +
                "    authenticators: " + toIndentedString(authenticators) + "\n" +
                "}";
    }

    @XmlType(name="DefinedByEnum")
    @XmlEnum(String.class)
    public enum DefinedByEnum {

        @XmlEnumValue("SYSTEM") SYSTEM(String.valueOf("SYSTEM")), @XmlEnumValue("USER") USER(String.valueOf("USER"));


        private String value;

        DefinedByEnum(String v) {
            value = v;
        }

        public String value() {
            return value;
        }

        @Override
        public String toString() {
            return String.valueOf(value);
        }

        public static DefinedByEnum fromValue(String value) {
            for (DefinedByEnum b : DefinedByEnum.values()) {
                if (b.value.equals(value)) {
                    return b;
                }
            }
            throw new IllegalArgumentException("Unexpected value '" + value + "'");
        }
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private static String toIndentedString(java.lang.Object o) {

        if (o == null) {
            return "null";
        }
        return o.toString();
    }

    public static class FederatedAuthenticator {
        private String authenticatorId;
        private String name;
        private Boolean isEnabled = false;
        private Boolean isDefault = false;
        private List<Property> properties = null;
        private DefinedByEnum definedBy;
        private Endpoint endpoint;

        /**
         *
         **/
        public FederatedAuthenticator authenticatorId(String authenticatorId) {

            this.authenticatorId = authenticatorId;
            return this;
        }

        @ApiModelProperty()
        @JsonProperty("authenticatorId")
        @Valid
        public String getAuthenticatorId() {
            return authenticatorId;
        }

        public void setAuthenticatorId(String authenticatorId) {
            this.authenticatorId = authenticatorId;
        }

        /**
         *
         **/
        public FederatedAuthenticator name(String name) {

            this.name = name;
            return this;
        }

        @ApiModelProperty()
        @JsonProperty("name")
        @Valid
        public String getScheme() {
            return name;
        }

        public void setScheme(String name) {
            this.name = name;
        }

        /**
         *
         **/
        public FederatedAuthenticator isEnabled(Boolean isEnabled) {

            this.isEnabled = isEnabled;
            return this;
        }

        @ApiModelProperty()
        @JsonProperty("isEnabled")
        @Valid
        public Boolean getIsEnabled() {
            return isEnabled;
        }

        public void setIsEnabled(Boolean isEnabled) {
            this.isEnabled = isEnabled;
        }

        /**
         *
         **/
        public FederatedAuthenticator isDefault(Boolean isDefault) {

            this.isDefault = isDefault;
            return this;
        }

        @ApiModelProperty()
        @JsonProperty("isDefault")
        @Valid
        public Boolean getIsDefault() {
            return isDefault;
        }

        public void setIsDefault(Boolean isDefault) {
            this.isDefault = isDefault;
        }

        /**
         *
         **/
        public FederatedAuthenticator properties(List<Property> properties) {

            this.properties = properties;
            return this;
        }

        @ApiModelProperty()
        @JsonProperty("properties")
        @Valid
        public List<Property> getProperties() {
            return properties;
        }

        public void setProperties(List<Property> properties) {
            this.properties = properties;
        }

        public FederatedAuthenticator addProperty(Property property) {
            if (this.properties == null) {
                this.properties = new ArrayList<>();
            }
            this.properties.add(property);
            return this;
        }

        /**
         *
         **/
        public FederatedAuthenticator definedBy(DefinedByEnum definedBy) {

            this.definedBy = definedBy;
            return this;
        }

        @ApiModelProperty(value = "")
        @JsonProperty("definedBy")
        @Valid
        public DefinedByEnum getDefinedBy() {
            return definedBy;
        }
        public void setDefinedBy(DefinedByEnum definedBy) {
            this.definedBy = definedBy;
        }

        /**
         **/
        public FederatedAuthenticator endpoint(Endpoint endpoint) {

            this.endpoint = endpoint;
            return this;
        }

        @ApiModelProperty(value = "")
        @JsonProperty("endpoint")
        @Valid
        public Endpoint getEndpoint() {
            return endpoint;
        }
        public void setEndpoint(Endpoint endpoint) {
            this.endpoint = endpoint;
        }

        @Override
        public String toString() {

            String classToString = "class FederatedAuthenticator {\n" +
                    "    authenticatorId: " + toIndentedString(authenticatorId) + "\n" +
                    "    name: " + toIndentedString(name) + "\n" +
                    "    isEnabled: " + toIndentedString(isEnabled) + "\n" +

                    "    isDefault: " + toIndentedString(isDefault) + "\n";
            if (properties != null) {
                classToString += "    properties: " + toIndentedString(properties) + "\n";
            }
            if (definedBy != null) {
                classToString += "    definedBy: " + toIndentedString(definedBy) + "\n";
            }
            if (endpoint != null) {
                classToString += "    endpoint: " + toIndentedString(endpoint) + "\n";
            }

            return classToString + "}";
        }
    }
}

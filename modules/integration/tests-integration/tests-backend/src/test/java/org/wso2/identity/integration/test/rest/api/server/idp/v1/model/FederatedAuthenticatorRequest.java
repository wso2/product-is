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
import javax.validation.Valid;
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

        @Override
        public String toString() {

            return "class FederatedAuthenticator {\n" +
                    "    authenticatorId: " + toIndentedString(authenticatorId) + "\n" +
                    "    name: " + toIndentedString(name) + "\n" +
                    "    isEnabled: " + toIndentedString(isEnabled) + "\n" +
                    "    isDefault: " + toIndentedString(isDefault) + "\n" +
                    "    properties: " + toIndentedString(properties) + "\n" +
                    "}";
        }
    }
}

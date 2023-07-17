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
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;
import java.util.List;
import java.util.Objects;

public class ProvisioningRequest {
    private JustInTimeProvisioning jit;
    private OutboundProvisioningRequest outboundConnectors;

    /**
     *
     **/
    public ProvisioningRequest jit(JustInTimeProvisioning jit) {

        this.jit = jit;
        return this;
    }

    @ApiModelProperty()
    @JsonProperty("jit")
    @Valid
    public JustInTimeProvisioning getJit() {
        return jit;
    }

    public void setJit(JustInTimeProvisioning jit) {
        this.jit = jit;
    }

    /**
     *
     **/
    public ProvisioningRequest outboundConnectors(OutboundProvisioningRequest outboundConnectors) {

        this.outboundConnectors = outboundConnectors;
        return this;
    }

    @ApiModelProperty()
    @JsonProperty("outboundConnectors")
    @Valid
    public OutboundProvisioningRequest getOutboundConnectors() {
        return outboundConnectors;
    }

    public void setOutboundConnectors(OutboundProvisioningRequest outboundConnectors) {
        this.outboundConnectors = outboundConnectors;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ProvisioningRequest provisioningRequest = (ProvisioningRequest) o;
        return Objects.equals(this.jit, provisioningRequest.jit) &&
                Objects.equals(this.outboundConnectors, provisioningRequest.outboundConnectors);
    }

    @Override
    public int hashCode() {
        return Objects.hash(jit, outboundConnectors);
    }

    @Override
    public String toString() {

        return "class ProvisioningRequest {\n" +
                "    jit: " + toIndentedString(jit) + "\n" +
                "    outboundConnectors: " + toIndentedString(outboundConnectors) + "\n" +
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

    public static class JustInTimeProvisioning {

        private Boolean isEnabled = false;

        @XmlType(name="SchemeEnum")
        @XmlEnum()
        public enum SchemeEnum {

            @XmlEnumValue("PROMPT_USERNAME_PASSWORD_CONSENT") PROMPT_USERNAME_PASSWORD_CONSENT("PROMPT_USERNAME_PASSWORD_CONSENT"),
            @XmlEnumValue("PROMPT_PASSWORD_CONSENT") PROMPT_PASSWORD_CONSENT("PROMPT_PASSWORD_CONSENT"),
            @XmlEnumValue("PROMPT_CONSENT") PROMPT_CONSENT("PROMPT_CONSENT"),
            @XmlEnumValue("PROVISION_SILENTLY") PROVISION_SILENTLY("PROVISION_SILENTLY");


            private final String value;

            SchemeEnum(String v) {
                value = v;
            }

            public String value() {
                return value;
            }

            @Override
            public String toString() {
                return String.valueOf(value);
            }

            public static SchemeEnum fromValue(String value) {
                for (SchemeEnum b : SchemeEnum.values()) {
                    if (b.value.equals(value)) {
                        return b;
                    }
                }
                throw new IllegalArgumentException("Unexpected value '" + value + "'");
            }
        }

        private SchemeEnum scheme = SchemeEnum.PROVISION_SILENTLY;
        private String userstore = "PRIMARY";
        private Boolean associateLocalUser;

        /**
         *
         **/
        public JustInTimeProvisioning isEnabled(Boolean isEnabled) {

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
        public JustInTimeProvisioning scheme(SchemeEnum scheme) {

            this.scheme = scheme;
            return this;
        }

        @ApiModelProperty()
        @JsonProperty("scheme")
        @Valid
        public SchemeEnum getScheme() {
            return scheme;
        }

        public void setScheme(SchemeEnum scheme) {
            this.scheme = scheme;
        }

        /**
         *
         **/
        public JustInTimeProvisioning userstore(String userstore) {

            this.userstore = userstore;
            return this;
        }

        @ApiModelProperty()
        @JsonProperty("userstore")
        @Valid
        public String getUserstore() {
            return userstore;
        }

        public void setUserstore(String userstore) {
            this.userstore = userstore;
        }

        /**
         *
         **/
        public JustInTimeProvisioning associateLocalUser(Boolean associateLocalUser) {

            this.associateLocalUser = associateLocalUser;
            return this;
        }

        @ApiModelProperty()
        @JsonProperty("associateLocalUser")
        @Valid
        public Boolean getAssociateLocalUser() {
            return associateLocalUser;
        }

        public void setAssociateLocalUser(Boolean associateLocalUser) {
            this.associateLocalUser = associateLocalUser;
        }

        @Override
        public String toString() {

            return "class JustInTimeProvisioning {\n" +
                    "    isEnabled: " + toIndentedString(isEnabled) + "\n" +
                    "    scheme: " + toIndentedString(scheme) + "\n" +
                    "    userstore: " + toIndentedString(userstore) + "\n" +
                    "    associateLocalUser: " + toIndentedString(associateLocalUser) + "\n" +
                    "}";
        }
    }

    public static class OutboundProvisioningRequest {
        private String defaultConnectorId;
        private List<OutboundConnector> connectors;

        /**
         *
         **/
        public OutboundProvisioningRequest defaultConnectorId(String defaultConnectorId) {

            this.defaultConnectorId = defaultConnectorId;
            return this;
        }

        @ApiModelProperty()
        @JsonProperty("defaultConnectorId")
        @Valid
        public String getDefaultConnectorId() {
            return defaultConnectorId;
        }

        public void setDefaultConnectorId(String defaultConnectorId) {
            this.defaultConnectorId = defaultConnectorId;
        }

        /**
         *
         **/
        public OutboundProvisioningRequest connectors(List<OutboundConnector> connectors) {

            this.connectors = connectors;
            return this;
        }

        @ApiModelProperty()
        @JsonProperty("connectors")
        @Valid
        public List<OutboundConnector> getConnectors() {
            return connectors;
        }

        public void setConnectors(List<OutboundConnector> connectors) {
            this.connectors = connectors;
        }

        @Override
        public String toString() {

            return "class OutboundProvisioningRequest {\n" +
                    "    defaultConnectorId: " + toIndentedString(defaultConnectorId) + "\n" +
                    "    connectors: " + toIndentedString(connectors) + "\n" +
                    "}";
        }

        public static class OutboundConnector {
            private String connectorId;
            private String name;
            private Boolean isEnabled = false;
            private Boolean isDefault = false;
            private Boolean blockingEnabled = false;
            private Boolean rulesEnabled = false;
            private List<Property> properties = null;

            /**
             *
             **/
            public OutboundConnector connectorId(String connectorId) {

                this.connectorId = connectorId;
                return this;
            }

            @ApiModelProperty()
            @JsonProperty("connectorId")
            @Valid
            public String getConnectorId() {
                return connectorId;
            }

            public void setConnectorId(String connectorId) {
                this.connectorId = connectorId;
            }

            /**
             *
             **/
            public OutboundConnector name(String name) {

                this.name = name;
                return this;
            }

            @ApiModelProperty()
            @JsonProperty("name")
            @Valid
            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            /**
             *
             **/
            public OutboundConnector isEnabled(Boolean isEnabled) {

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
            public OutboundConnector isDefault(Boolean isDefault) {

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
            public OutboundConnector blockingEnabled(Boolean blockingEnabled) {

                this.blockingEnabled = blockingEnabled;
                return this;
            }

            @ApiModelProperty()
            @JsonProperty("blockingEnabled")
            @Valid
            public Boolean getBlockingEnabled() {
                return blockingEnabled;
            }

            public void setBlockingEnabled(Boolean blockingEnabled) {
                this.blockingEnabled = blockingEnabled;
            }

            /**
             *
             **/
            public OutboundConnector rulesEnabled(Boolean rulesEnabled) {

                this.rulesEnabled = rulesEnabled;
                return this;
            }

            @ApiModelProperty()
            @JsonProperty("rulesEnabled")
            @Valid
            public Boolean getRulesEnabled() {
                return rulesEnabled;
            }

            public void setRulesEnabled(Boolean rulesEnabled) {
                this.rulesEnabled = rulesEnabled;
            }

            /**
             *
             **/
            public OutboundConnector properties(List<Property> properties) {

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

                return "class OutboundConnector {\n" +
                        "    connectorId: " + toIndentedString(connectorId) + "\n" +
                        "    name: " + toIndentedString(name) + "\n" +
                        "    isEnabled: " + toIndentedString(isEnabled) + "\n" +
                        "    isDefault: " + toIndentedString(isDefault) + "\n" +
                        "    blockingEnabled: " + toIndentedString(blockingEnabled) + "\n" +
                        "    rulesEnabled: " + toIndentedString(rulesEnabled) + "\n" +
                        "    properties: " + toIndentedString(properties) + "\n" +
                        "}";
            }
        }
    }
}

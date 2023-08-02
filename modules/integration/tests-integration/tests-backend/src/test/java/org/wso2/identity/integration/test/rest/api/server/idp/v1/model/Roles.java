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

public class Roles {
    private List<RoleMapping> mappings;
    private List<String> outboundProvisioningRoles;

    /**
     *
     **/
    public Roles mappings(List<RoleMapping> mappings) {

        this.mappings = mappings;
        return this;
    }

    @ApiModelProperty()
    @JsonProperty("mappings")
    @Valid
    public List<RoleMapping> getMappings() {
        return mappings;
    }

    public void setMappings(List<RoleMapping> mappings) {
        this.mappings = mappings;
    }

    /**
     *
     **/
    public Roles outboundProvisioningRoles(List<String> outboundProvisioningRoles) {

        this.outboundProvisioningRoles = outboundProvisioningRoles;
        return this;
    }

    @ApiModelProperty()
    @JsonProperty("outboundProvisioningRoles")
    @Valid
    public List<String> getOutboundProvisioningRoles() {
        return outboundProvisioningRoles;
    }

    public void setOutboundProvisioningRoles(List<String> outboundProvisioningRoles) {
        this.outboundProvisioningRoles = outboundProvisioningRoles;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Roles Roles = (Roles) o;
        return  Objects.equals(this.mappings, Roles.mappings) &&
                Objects.equals(this.outboundProvisioningRoles, Roles.outboundProvisioningRoles);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mappings, outboundProvisioningRoles);
    }

    @Override
    public String toString() {

        return "class Roles {\n" +
                "    mappings: " + toIndentedString(mappings) + "\n" +
                "    outboundProvisioningRoles: " + toIndentedString(outboundProvisioningRoles) + "\n" +
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

    public static class RoleMapping {
        private String idpRole;
        private String localRole;

        /**
         *
         **/
        public RoleMapping idpRole(String idpRole) {

            this.idpRole = idpRole;
            return this;
        }

        @ApiModelProperty()
        @JsonProperty("idpRole")
        @Valid
        public String getIdpRole() {
            return idpRole;
        }

        public void setIdpRole(String idpRole) {
            this.idpRole = idpRole;
        }

        /**
         *
         **/
        public RoleMapping localRole(String localRole) {

            this.localRole = localRole;
            return this;
        }

        @ApiModelProperty()
        @JsonProperty("localRole")
        @Valid
        public String getLocalRole() {
            return localRole;
        }

        public void setLocalRole(String localRole) {
            this.localRole = localRole;
        }

        @Override
        public boolean equals(Object o) {

            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            RoleMapping roleMapping = (RoleMapping) o;
            return Objects.equals(this.idpRole, roleMapping.idpRole) &&
                    Objects.equals(this.localRole, roleMapping.localRole);
        }

        @Override
        public int hashCode() {
            return Objects.hash(idpRole, localRole);
        }

        @Override
        public String toString() {

            return "class RoleMapping {\n" +
                    "    idpRole: " + toIndentedString(idpRole) + "\n" +
                    "    localRole: " + toIndentedString(localRole) + "\n" +
                    "}";
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
}

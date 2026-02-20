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

package org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

/**
 * Per-organization sharing configuration for selected organizations.
 */
@ApiModel(description = "Per-organization sharing configuration for selected organizations.")
public class UserOrgShareConfig {

    private String orgId;

    @XmlType(name = "PolicyEnum")
    @XmlEnum(String.class)
    public enum PolicyEnum {

        @XmlEnumValue("SELECTED_ORG_ONLY") SELECTED_ORG_ONLY(String.valueOf("SELECTED_ORG_ONLY")),
        @XmlEnumValue("SELECTED_ORG_WITH_ALL_EXISTING_CHILDREN_ONLY") 
            SELECTED_ORG_WITH_ALL_EXISTING_CHILDREN_ONLY(String.valueOf("SELECTED_ORG_WITH_ALL_EXISTING_CHILDREN_ONLY")),
        @XmlEnumValue("SELECTED_ORG_WITH_ALL_EXISTING_AND_FUTURE_CHILDREN") 
            SELECTED_ORG_WITH_ALL_EXISTING_AND_FUTURE_CHILDREN(String.valueOf("SELECTED_ORG_WITH_ALL_EXISTING_AND_FUTURE_CHILDREN")),
        @XmlEnumValue("SELECTED_ORG_WITH_EXISTING_IMMEDIATE_CHILDREN_ONLY") 
            SELECTED_ORG_WITH_EXISTING_IMMEDIATE_CHILDREN_ONLY(String.valueOf("SELECTED_ORG_WITH_EXISTING_IMMEDIATE_CHILDREN_ONLY")),
        @XmlEnumValue("SELECTED_ORG_WITH_EXISTING_IMMEDIATE_AND_FUTURE_CHILDREN") 
            SELECTED_ORG_WITH_EXISTING_IMMEDIATE_AND_FUTURE_CHILDREN(String.valueOf("SELECTED_ORG_WITH_EXISTING_IMMEDIATE_AND_FUTURE_CHILDREN"));

        private String value;

        PolicyEnum(String v) {
            value = v;
        }

        public String value() {
            return value;
        }

        @Override
        public String toString() {
            return String.valueOf(value);
        }

        public static PolicyEnum fromValue(String value) {
            for (PolicyEnum b : PolicyEnum.values()) {
                if (b.value.equals(value)) {
                    return b;
                }
            }
            throw new IllegalArgumentException("Unexpected value '" + value + "'");
        }
    }

    private PolicyEnum policy;
    private RoleAssignment roleAssignment;

    /**
     * Organization ID to share the users with.
     **/
    public UserOrgShareConfig orgId(String orgId) {
        this.orgId = orgId;
        return this;
    }

    @ApiModelProperty(required = true, value = "Organization ID to share the users with.")
    @JsonProperty("orgId")
    @Valid
    @NotNull(message = "Property orgId cannot be null.")
    public String getOrgId() {
        return orgId;
    }

    public void setOrgId(String orgId) {
        this.orgId = orgId;
    }

    /**
     * Sharing scope for this organization.
     **/
    public UserOrgShareConfig policy(PolicyEnum policy) {
        this.policy = policy;
        return this;
    }

    @ApiModelProperty(required = true, value = "Sharing scope for this organization.")
    @JsonProperty("policy")
    @Valid
    @NotNull(message = "Property policy cannot be null.")
    public PolicyEnum getPolicy() {
        return policy;
    }

    public void setPolicy(PolicyEnum policy) {
        this.policy = policy;
    }

    /**
     * Optional role assignment configuration for this organization.
     **/
    public UserOrgShareConfig roleAssignment(RoleAssignment roleAssignment) {
        this.roleAssignment = roleAssignment;
        return this;
    }

    @ApiModelProperty(value = "Optional role assignment configuration for this organization.")
    @JsonProperty("roleAssignment")
    @Valid
    public RoleAssignment getRoleAssignment() {
        return roleAssignment;
    }

    public void setRoleAssignment(RoleAssignment roleAssignment) {
        this.roleAssignment = roleAssignment;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UserOrgShareConfig userOrgShareConfig = (UserOrgShareConfig) o;
        return Objects.equals(this.orgId, userOrgShareConfig.orgId) &&
                Objects.equals(this.policy, userOrgShareConfig.policy) &&
                Objects.equals(this.roleAssignment, userOrgShareConfig.roleAssignment);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orgId, policy, roleAssignment);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class UserOrgShareConfig {\n");
        sb.append("    orgId: ").append(toIndentedString(orgId)).append("\n");
        sb.append("    policy: ").append(toIndentedString(policy)).append("\n");
        sb.append("    roleAssignment: ").append(toIndentedString(roleAssignment)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    private String toIndentedString(Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}

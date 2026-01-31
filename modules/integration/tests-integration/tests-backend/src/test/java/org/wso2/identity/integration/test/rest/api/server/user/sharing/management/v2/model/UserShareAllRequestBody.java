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
 * Request body for sharing users with all organizations controlled by the selected policy.
 */
@ApiModel(description = "Request body for sharing users with all organizations.")
public class UserShareAllRequestBody {

    private UserCriteria userCriteria;

    @XmlType(name = "PolicyEnum")
    @XmlEnum(String.class)
    public enum PolicyEnum {

        @XmlEnumValue("ALL_EXISTING_ORGS_ONLY") ALL_EXISTING_ORGS_ONLY(String.valueOf("ALL_EXISTING_ORGS_ONLY")),
        @XmlEnumValue("ALL_EXISTING_AND_FUTURE_ORGS") ALL_EXISTING_AND_FUTURE_ORGS(String.valueOf("ALL_EXISTING_AND_FUTURE_ORGS")),
        @XmlEnumValue("IMMEDIATE_EXISTING_ORGS_ONLY") IMMEDIATE_EXISTING_ORGS_ONLY(String.valueOf("IMMEDIATE_EXISTING_ORGS_ONLY")),
        @XmlEnumValue("IMMEDIATE_EXISTING_AND_FUTURE_ORGS") IMMEDIATE_EXISTING_AND_FUTURE_ORGS(String.valueOf("IMMEDIATE_EXISTING_AND_FUTURE_ORGS"));

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
     * Criteria for selecting users.
     **/
    public UserShareAllRequestBody userCriteria(UserCriteria userCriteria) {
        this.userCriteria = userCriteria;
        return this;
    }

    @ApiModelProperty(required = true, value = "Criteria for selecting users.")
    @JsonProperty("userCriteria")
    @Valid
    @NotNull(message = "Property userCriteria cannot be null.")
    public UserCriteria getUserCriteria() {
        return userCriteria;
    }

    public void setUserCriteria(UserCriteria userCriteria) {
        this.userCriteria = userCriteria;
    }

    /**
     * Global sharing policy.
     **/
    public UserShareAllRequestBody policy(PolicyEnum policy) {
        this.policy = policy;
        return this;
    }

    @ApiModelProperty(required = true, value = "Global sharing policy.")
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
     * Optional global role assignment configuration that applies to all organizations.
     **/
    public UserShareAllRequestBody roleAssignment(RoleAssignment roleAssignment) {
        this.roleAssignment = roleAssignment;
        return this;
    }

    @ApiModelProperty(value = "Optional global role assignment configuration.")
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
        UserShareAllRequestBody userShareAllRequestBody = (UserShareAllRequestBody) o;
        return Objects.equals(this.userCriteria, userShareAllRequestBody.userCriteria) &&
                Objects.equals(this.policy, userShareAllRequestBody.policy) &&
                Objects.equals(this.roleAssignment, userShareAllRequestBody.roleAssignment);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userCriteria, policy, roleAssignment);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class UserShareAllRequestBody {\n");
        sb.append("    userCriteria: ").append(toIndentedString(userCriteria)).append("\n");
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

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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * Request body for POST /users/share-with-all (V2 general share).
 * <p>
 * V2 supports only {@link PolicyEnum#ALL_EXISTING_AND_FUTURE_ORGS} as the general policy.
 * Role assignment details are encapsulated in a {@link RoleAssignments} object (mode + roles)
 * instead of a flat role list as in V1.
 * <p>
 * A new general share request always replaces the existing ResourceSharingPolicy for the user
 * (replaceExistingPolicies=true), including any previously saved selective policy.
 */
@ApiModel(description = "Request body for the V2 general user share endpoint.")
public class UserShareWithAllRequestBody {

    /**
     * V2 general sharing policies. Only one policy is currently supported.
     */
    public enum PolicyEnum {

        ALL_EXISTING_AND_FUTURE_ORGS("ALL_EXISTING_AND_FUTURE_ORGS");

        private final String value;

        PolicyEnum(String value) {
            this.value = value;
        }

        @JsonValue
        public String getValue() {
            return value;
        }

        @Override
        public String toString() {
            return value;
        }

        @JsonCreator
        public static PolicyEnum fromValue(String value) {
            for (PolicyEnum e : PolicyEnum.values()) {
                if (e.value.equalsIgnoreCase(value)) {
                    return e;
                }
            }
            throw new IllegalArgumentException("Unexpected policy value: " + value);
        }
    }

    private UserShareRequestBodyUserCriteria userCriteria;
    private PolicyEnum policy;
    private RoleAssignments roleAssignment;

    public UserShareWithAllRequestBody userCriteria(UserShareRequestBodyUserCriteria userCriteria) {

        this.userCriteria = userCriteria;
        return this;
    }

    @ApiModelProperty(required = true, value = "User criteria specifying the users to share.")
    @JsonProperty("userCriteria")
    @Valid
    @NotNull(message = "Property userCriteria cannot be null.")
    public UserShareRequestBodyUserCriteria getUserCriteria() {
        return userCriteria;
    }

    public void setUserCriteria(UserShareRequestBodyUserCriteria userCriteria) {
        this.userCriteria = userCriteria;
    }

    public UserShareWithAllRequestBody policy(PolicyEnum policy) {

        this.policy = policy;
        return this;
    }

    @ApiModelProperty(required = true, value = "The general sharing policy. Only " +
            "ALL_EXISTING_AND_FUTURE_ORGS is supported in V2.")
    @JsonProperty("policy")
    @Valid
    @NotNull(message = "Property policy cannot be null.")
    public PolicyEnum getPolicy() {
        return policy;
    }

    public void setPolicy(PolicyEnum policy) {
        this.policy = policy;
    }

    public UserShareWithAllRequestBody roleAssignment(RoleAssignments roleAssignment) {

        this.roleAssignment = roleAssignment;
        return this;
    }

    @ApiModelProperty(value = "Role assignment configuration for all organizations. Optional — if omitted, no roles are assigned.")
    @JsonProperty("roleAssignment")
    @Valid
    public RoleAssignments getRoleAssignments() {
        return roleAssignment;
    }

    public void setRoleAssignments(RoleAssignments roleAssignment) {
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
        UserShareWithAllRequestBody that = (UserShareWithAllRequestBody) o;
        return Objects.equals(this.userCriteria, that.userCriteria) &&
                Objects.equals(this.policy, that.policy) &&
                Objects.equals(this.roleAssignment, that.roleAssignment);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userCriteria, policy, roleAssignment);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class UserShareWithAllRequestBody {\n");
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

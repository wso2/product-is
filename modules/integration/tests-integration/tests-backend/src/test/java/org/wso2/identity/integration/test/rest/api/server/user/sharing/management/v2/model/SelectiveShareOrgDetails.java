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
 * Per-organization entry in a selective share request body.
 * <p>
 * V2 reduces the supported selective policies to two:
 * <ul>
 *   <li>{@link PolicyEnum#SELECTED_ORG_ONLY} – share only to the exact target org; never saved to
 *       the ResourceSharingPolicy table, so {@code sharingMode} is always absent in the GET response
 *       for orgs shared with this policy.</li>
 *   <li>{@link PolicyEnum#SELECTED_ORG_WITH_ALL_EXISTING_AND_FUTURE_CHILDREN} – share to the
 *       target org and all its recursive descendants (now and in the future); saved to the policy
 *       table so {@code sharingMode} is present on the policy-holding org in the GET response.</li>
 * </ul>
 * Only immediate children of the initiating org are accepted as valid targets
 * ({@code filterValidOrganizations} enforcement).
 */
@ApiModel(description = "Specifies a target organization and the sharing policy to apply for a selective share.")
public class SelectiveShareOrgDetails {

    /**
     * V2 selective sharing policies.
     */
    public enum PolicyEnum {

        SELECTED_ORG_ONLY("SELECTED_ORG_ONLY"),
        SELECTED_ORG_WITH_ALL_EXISTING_AND_FUTURE_CHILDREN(
                "SELECTED_ORG_WITH_ALL_EXISTING_AND_FUTURE_CHILDREN");

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

    private String orgId;
    private PolicyEnum policy;
    private RoleAssignments roleAssignment;

    public SelectiveShareOrgDetails orgId(String orgId) {

        this.orgId = orgId;
        return this;
    }

    @ApiModelProperty(required = true,
            value = "ID of the target organization. Must be an immediate child of the initiating org.")
    @JsonProperty("orgId")
    @Valid
    @NotNull(message = "Property orgId cannot be null.")
    public String getOrgId() {
        return orgId;
    }

    public void setOrgId(String orgId) {
        this.orgId = orgId;
    }

    public SelectiveShareOrgDetails policy(PolicyEnum policy) {

        this.policy = policy;
        return this;
    }

    @ApiModelProperty(required = true, value = "The selective sharing policy to apply.")
    @JsonProperty("policy")
    @Valid
    @NotNull(message = "Property policy cannot be null.")
    public PolicyEnum getPolicy() {
        return policy;
    }

    public void setPolicy(PolicyEnum policy) {
        this.policy = policy;
    }

    public SelectiveShareOrgDetails roleAssignment(RoleAssignments roleAssignment) {

        this.roleAssignment = roleAssignment;
        return this;
    }

    @ApiModelProperty(required = true, value = "Role assignment mode and roles to apply for this organization.")
    @JsonProperty("roleAssignment")
    @Valid
    @NotNull(message = "Property roleAssignment cannot be null.")
    public RoleAssignments getRoleAssignment() {
        return roleAssignment;
    }

    public void setRoleAssignment(RoleAssignments roleAssignment) {
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
        SelectiveShareOrgDetails that = (SelectiveShareOrgDetails) o;
        return Objects.equals(this.orgId, that.orgId) &&
                Objects.equals(this.policy, that.policy) &&
                Objects.equals(this.roleAssignment, that.roleAssignment);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orgId, policy, roleAssignment);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class SelectiveShareOrgDetails {\n");
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

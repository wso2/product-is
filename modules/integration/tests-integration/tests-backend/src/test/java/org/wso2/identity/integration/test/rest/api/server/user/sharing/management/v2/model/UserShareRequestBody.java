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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * Request body for POST /users/share (V2 selective share).
 * <p>
 * Each entry in {@code organizations} specifies a target org, a V2 selective policy, and a
 * {@link RoleAssignments} object. Only one effective policy per user per request is safe
 * due to HashMap-based last-write-wins policy replacement in the service layer.
 */
@ApiModel(description = "Request body for the V2 selective user share endpoint.")
public class UserShareRequestBody {

    private UserShareRequestBodyUserCriteria userCriteria;
    private List<SelectiveShareOrgDetails> organizations = new ArrayList<>();

    public UserShareRequestBody userCriteria(UserShareRequestBodyUserCriteria userCriteria) {

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

    public UserShareRequestBody organizations(List<SelectiveShareOrgDetails> organizations) {

        this.organizations = organizations;
        return this;
    }

    @ApiModelProperty(required = true, value = "List of organizations with their sharing policies and role " +
            "assignments.")
    @JsonProperty("organizations")
    @Valid
    @NotNull(message = "Property organizations cannot be null.")
    public List<SelectiveShareOrgDetails> getOrganizations() {
        return organizations;
    }

    public void setOrganizations(List<SelectiveShareOrgDetails> organizations) {
        this.organizations = organizations;
    }

    public UserShareRequestBody addOrganizationsItem(SelectiveShareOrgDetails orgDetails) {

        this.organizations.add(orgDetails);
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
        UserShareRequestBody that = (UserShareRequestBody) o;
        return Objects.equals(this.userCriteria, that.userCriteria) &&
                Objects.equals(this.organizations, that.organizations);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userCriteria, organizations);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class UserShareRequestBody {\n");
        sb.append("    userCriteria: ").append(toIndentedString(userCriteria)).append("\n");
        sb.append("    organizations: ").append(toIndentedString(organizations)).append("\n");
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

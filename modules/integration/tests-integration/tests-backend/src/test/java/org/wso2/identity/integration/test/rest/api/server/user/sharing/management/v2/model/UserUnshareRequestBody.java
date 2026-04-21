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
 * Request body for POST /users/unshare (V2 selective unshare).
 * <p>
 * Each org ID in {@code organizations} triggers a cascade removal: the target org and all its
 * recursive descendants are unshared. The ResourceSharingPolicy is deleted only when the target
 * org is the {@code policyHoldingOrgId}; a root-level general policy is NOT deleted by a
 * selective unshare — use {@link UserUnshareWithAllRequestBody} for that.
 */
@ApiModel(description = "Request body for the V2 selective user unshare endpoint.")
public class UserUnshareRequestBody {

    private UserUnshareRequestBodyUserCriteria userCriteria;
    private List<String> orgIds = new ArrayList<>();

    public UserUnshareRequestBody userCriteria(UserUnshareRequestBodyUserCriteria userCriteria) {

        this.userCriteria = userCriteria;
        return this;
    }

    @ApiModelProperty(required = true, value = "User criteria specifying the users to unshare.")
    @JsonProperty("userCriteria")
    @Valid
    @NotNull(message = "Property userCriteria cannot be null.")
    public UserUnshareRequestBodyUserCriteria getUserCriteria() {
        return userCriteria;
    }

    public void setUserCriteria(UserUnshareRequestBodyUserCriteria userCriteria) {
        this.userCriteria = userCriteria;
    }

    public UserUnshareRequestBody orgIds(List<String> orgIds) {

        this.orgIds = orgIds;
        return this;
    }

    @ApiModelProperty(required = true,
            value = "List of organization IDs from which the users should be unshared (cascade applies).")
    @JsonProperty("orgIds")
    @Valid
    @NotNull(message = "Property orgIds cannot be null.")
    public List<String> getOrgIds() {
        return orgIds;
    }

    public void setOrgIds(List<String> orgIds) {
        this.orgIds = orgIds;
    }

    public UserUnshareRequestBody addOrgIdsItem(String orgId) {

        this.orgIds.add(orgId);
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
        UserUnshareRequestBody that = (UserUnshareRequestBody) o;
        return Objects.equals(this.userCriteria, that.userCriteria) &&
                Objects.equals(this.orgIds, that.orgIds);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userCriteria, orgIds);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class UserUnshareRequestBody {\n");
        sb.append("    userCriteria: ").append(toIndentedString(userCriteria)).append("\n");
        sb.append("    orgIds: ").append(toIndentedString(orgIds)).append("\n");
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

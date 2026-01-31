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
 * Request body for unsharing users from selected organizations.
 */
@ApiModel(description = "Request body for unsharing users from selected organizations.")
public class UserUnshareSelectedRequestBody {

    private UserCriteria userCriteria;
    private List<String> orgIds = new ArrayList<>();

    /**
     * Criteria for selecting users.
     **/
    public UserUnshareSelectedRequestBody userCriteria(UserCriteria userCriteria) {
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
     * List of organization IDs from which users should be unshared.
     **/
    public UserUnshareSelectedRequestBody orgIds(List<String> orgIds) {
        this.orgIds = orgIds;
        return this;
    }

    @ApiModelProperty(required = true, value = "List of organization IDs from which users should be unshared.")
    @JsonProperty("orgIds")
    @Valid
    @NotNull(message = "Property orgIds cannot be null.")
    public List<String> getOrgIds() {
        return orgIds;
    }

    public void setOrgIds(List<String> orgIds) {
        this.orgIds = orgIds;
    }

    public UserUnshareSelectedRequestBody addOrgIdsItem(String orgIdsItem) {
        this.orgIds.add(orgIdsItem);
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
        UserUnshareSelectedRequestBody userUnshareSelectedRequestBody = (UserUnshareSelectedRequestBody) o;
        return Objects.equals(this.userCriteria, userUnshareSelectedRequestBody.userCriteria) &&
                Objects.equals(this.orgIds, userUnshareSelectedRequestBody.orgIds);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userCriteria, orgIds);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class UserUnshareSelectedRequestBody {\n");
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

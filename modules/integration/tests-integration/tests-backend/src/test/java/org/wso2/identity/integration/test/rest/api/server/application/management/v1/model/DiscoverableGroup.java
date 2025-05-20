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

package org.wso2.identity.integration.test.rest.api.server.application.management.v1.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.GroupBasicInfo;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;
import javax.validation.Valid;
import javax.xml.bind.annotation.*;

public class DiscoverableGroup  {

    private String userStore;
    private List<GroupBasicInfo> groups = new ArrayList<>();


    /**
     * The user store domain to which the groups belong.
     **/
    public DiscoverableGroup userStore(String userStore) {

        this.userStore = userStore;
        return this;
    }

    @ApiModelProperty(example = "PRIMARY", required = true, value = "The user store domain to which the groups belong.")
    @JsonProperty("userStore")
    @Valid
    @NotNull(message = "Property userStore cannot be null.")
    public String getUserStore() {

        return userStore;
    }

    public void setUserStore(String userStore) {

        this.userStore = userStore;
    }

    /**
     * List of groups configured for discoverability.
     **/
    public DiscoverableGroup groups(List<GroupBasicInfo> groups) {

        this.groups = groups;
        return this;
    }

    @ApiModelProperty(required = true, value = "List of groups configured for discoverability.")
    @JsonProperty("groups")
    @Valid
    @NotNull(message = "Property groups cannot be null.")
    @Size(min=1)
    public List<GroupBasicInfo> getGroups() {

        return groups;
    }

    public void setGroups(List<GroupBasicInfo> groups) {

        this.groups = groups;
    }

    public DiscoverableGroup addGroupsItem(GroupBasicInfo groupsItem) {

        this.groups.add(groupsItem);
        return this;
    }



    @Override
    public boolean equals(java.lang.Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DiscoverableGroup discoverableGroup = (DiscoverableGroup) o;
        return Objects.equals(this.userStore, discoverableGroup.userStore) &&
                Objects.equals(this.groups, discoverableGroup.groups);
    }

    @Override
    public int hashCode() {

        return Objects.hash(userStore, groups);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class DiscoverableGroup {\n");
        sb.append("    userStore: ").append(toIndentedString(userStore)).append("\n");
        sb.append("    groups: ").append(toIndentedString(groups)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(java.lang.Object o) {

        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n");
    }
}

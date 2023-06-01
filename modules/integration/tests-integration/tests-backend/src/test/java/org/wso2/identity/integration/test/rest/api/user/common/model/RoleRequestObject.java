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

package org.wso2.identity.integration.test.rest.api.user.common.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.Valid;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class RoleRequestObject {

    private List<String> schemas = null;
    private String displayName;
    private List<ListObject> users = null;
    private List<ListObject> groups = null;
    private List<String> permissions = null;

    /**
     *
     **/
    public RoleRequestObject schemas(List<String> schemas) {

        this.schemas = schemas;
        return this;
    }

    @ApiModelProperty()
    @JsonProperty("schemas")
    @Valid
    public List<String> getSchemas() {
        return schemas;
    }

    public void setSchemas(List<String> schemas) {
        this.schemas = schemas;
    }

    public RoleRequestObject addSchemas(String schema) {
        if (this.schemas == null) {
            this.schemas = new ArrayList<>();
        }
        this.schemas.add(schema);
        return this;
    }

    /**
     *
     **/
    public RoleRequestObject displayName(String displayName) {

        this.displayName = displayName;
        return this;
    }

    @ApiModelProperty(example = "abc")
    @JsonProperty("displayName")
    @Valid
    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     *
     **/
    public RoleRequestObject users(List<ListObject> users) {

        this.users = users;
        return this;
    }

    @ApiModelProperty()
    @JsonProperty("users")
    @Valid
    public List<ListObject> getUsers() {
        return users;
    }

    public void setUsers(List<ListObject> users) {
        this.users = users;
    }

    public RoleRequestObject addUsers(ListObject user) {
        if (this.users == null) {
            this.users = new ArrayList<>();
        }
        this.users.add(user);
        return this;
    }

    /**
     *
     **/
    public RoleRequestObject groups(List<ListObject> groups) {

        this.groups = groups;
        return this;
    }

    @ApiModelProperty()
    @JsonProperty("groups")
    @Valid
    public List<ListObject> getGroups() {
        return groups;
    }

    public void setGroups(List<ListObject> groups) {
        this.groups = groups;
    }

    public RoleRequestObject addGroups(ListObject group) {
        if (this.groups == null) {
            this.groups = new ArrayList<>();
        }
        this.groups.add(group);
        return this;
    }

    /**
     *
     **/
    public RoleRequestObject permissions(List<String> permissions) {

        this.permissions = permissions;
        return this;
    }

    @ApiModelProperty()
    @JsonProperty("permissions")
    @Valid
    public List<String> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<String> permissions) {
        this.permissions = permissions;
    }

    public RoleRequestObject addPermissions(String permission) {
        if (this.permissions == null) {
            this.permissions = new ArrayList<>();
        }
        this.permissions.add(permission);
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
        RoleRequestObject roleRequestObject = (RoleRequestObject) o;
        return Objects.equals(this.schemas, roleRequestObject.schemas) &&
                Objects.equals(this.displayName, roleRequestObject.displayName) &&
                Objects.equals(this.users, roleRequestObject.users) &&
                Objects.equals(this.groups, roleRequestObject.groups) &&
                Objects.equals(this.permissions, roleRequestObject.permissions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(schemas, displayName, users, groups, permissions);
    }

    @Override
    public String toString() {

        return "class RoleRequestObject {\n" +
                "    schemas: " + toIndentedString(schemas) + "\n" +
                "    displayName: " + toIndentedString(displayName) + "\n" +
                "    users: " + toIndentedString(users) + "\n" +
                "    groups: " + toIndentedString(groups) + "\n" +
                "    permissions: " + toIndentedString(permissions) + "\n" +
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

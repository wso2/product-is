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
 * A single PATCH operation (SCIM-like) for user sharing.
 */
@ApiModel(description = "A single PATCH operation for user sharing.")
public class UserSharingPatchOperation {

    private String op;
    private String path;
    private List<RoleShareConfig> value = null;

    /**
     * Operation type. Supported values: add, remove.
     **/
    public UserSharingPatchOperation op(String op) {
        this.op = op;
        return this;
    }

    @ApiModelProperty(required = true, value = "Operation type. Supported values: add, remove.")
    @JsonProperty("op")
    @Valid
    @NotNull(message = "Property op cannot be null.")
    public String getOp() {
        return op;
    }

    public void setOp(String op) {
        this.op = op;
    }

    /**
     * JSON-like path in the format: organizations[orgId eq \"<org-id>\"].roles
     **/
    public UserSharingPatchOperation path(String path) {
        this.path = path;
        return this;
    }

    @ApiModelProperty(required = true, value = "JSON-like path in the format: organizations[orgId eq \"<org-id>\"].roles")
    @JsonProperty("path")
    @Valid
    @NotNull(message = "Property path cannot be null.")
    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    /**
     * List of roles to add/remove under the specified path.
     **/
    public UserSharingPatchOperation value(List<RoleShareConfig> value) {
        this.value = value;
        return this;
    }

    @ApiModelProperty(value = "List of roles to add/remove under the specified path.")
    @JsonProperty("value")
    @Valid
    public List<RoleShareConfig> getValue() {
        return value;
    }

    public void setValue(List<RoleShareConfig> value) {
        this.value = value;
    }

    public UserSharingPatchOperation addValueItem(RoleShareConfig valueItem) {
        if (this.value == null) {
            this.value = new ArrayList<>();
        }
        this.value.add(valueItem);
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
        UserSharingPatchOperation userSharingPatchOperation = (UserSharingPatchOperation) o;
        return Objects.equals(this.op, userSharingPatchOperation.op) &&
                Objects.equals(this.path, userSharingPatchOperation.path) &&
                Objects.equals(this.value, userSharingPatchOperation.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(op, path, value);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class UserSharingPatchOperation {\n");
        sb.append("    op: ").append(toIndentedString(op)).append("\n");
        sb.append("    path: ").append(toIndentedString(path)).append("\n");
        sb.append("    value: ").append(toIndentedString(value)).append("\n");
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

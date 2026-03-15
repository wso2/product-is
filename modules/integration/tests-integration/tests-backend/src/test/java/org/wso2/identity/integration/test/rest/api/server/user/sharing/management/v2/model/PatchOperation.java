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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * A single PATCH operation for updating role assignments on a shared user in a specific organization.
 * <p>
 * Important behavioral notes:
 * <ul>
 *   <li>{@link OpEnum#ADD} is additive — existing role assignments in the target org are preserved.</li>
 *   <li>{@link OpEnum#REMOVE} removes only the specified roles from the target org.</li>
 *   <li>Neither operation cascades to child organizations.</li>
 *   <li>Neither operation modifies the {@code ResourceSharingPolicy} table; the
 *       {@code sharingMode.roleAssignment.roles} field in the GET response always reflects the
 *       original policy-time roles, not the current actual assignments.</li>
 * </ul>
 * The {@code path} must follow the format:
 * {@code organizations[orgId eq <uuid>].roles}
 * Use {@link org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants#PATCH_PATH_ORG_ROLES_FORMAT}
 * with {@code String.format} to build this value.
 */
@ApiModel(description = "A single role-assignment patch operation targeting a specific shared organization.")
public class PatchOperation {

    /**
     * The PATCH operation type.
     */
    public enum OpEnum {

        ADD("add"),
        REMOVE("remove");

        private final String value;

        OpEnum(String value) {
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
        public static OpEnum fromValue(String value) {
            for (OpEnum e : OpEnum.values()) {
                if (e.value.equalsIgnoreCase(value)) {
                    return e;
                }
            }
            throw new IllegalArgumentException("Unexpected op value: " + value);
        }
    }

    private OpEnum op;
    private String path;
    private List<RoleWithAudience> value = new ArrayList<>();

    public PatchOperation op(OpEnum op) {

        this.op = op;
        return this;
    }

    @ApiModelProperty(required = true, value = "The patch operation: \"add\" or \"remove\".")
    @JsonProperty("op")
    @Valid
    @NotNull(message = "Property op cannot be null.")
    public OpEnum getOp() {
        return op;
    }

    public void setOp(OpEnum op) {
        this.op = op;
    }

    public PatchOperation path(String path) {

        this.path = path;
        return this;
    }

    @ApiModelProperty(required = true,
            value = "Target path in the format \"organizations[orgId eq <uuid>].roles\".")
    @JsonProperty("path")
    @Valid
    @NotNull(message = "Property path cannot be null.")
    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public PatchOperation value(List<RoleWithAudience> value) {

        this.value = value;
        return this;
    }

    @ApiModelProperty(value = "Roles to add or remove. May be empty for a no-op remove.")
    @JsonProperty("value")
    @Valid
    public List<RoleWithAudience> getValues() {
        return value;
    }

    public void setValues(List<RoleWithAudience> value) {
        this.value = value;
    }

    public PatchOperation addValuesItem(RoleWithAudience role) {

        this.value.add(role);
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
        PatchOperation that = (PatchOperation) o;
        return Objects.equals(this.op, that.op) &&
                Objects.equals(this.path, that.path) &&
                Objects.equals(this.value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(op, path, value);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class PatchOperation {\n");
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

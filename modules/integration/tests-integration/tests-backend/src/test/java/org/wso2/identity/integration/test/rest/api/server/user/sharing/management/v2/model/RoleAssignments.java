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
 * Specifies how roles are assigned when a user is shared.
 * <p>
 * V2 replaces the flat role list of V1 with this structured object so that the sharing intent
 * ({@link ModeEnum#NONE} vs {@link ModeEnum#SELECTED}) is explicit even when no roles are provided.
 * <p>
 * Important: when {@code mode} is {@link ModeEnum#NONE} the {@code roles} list must be empty and
 * the GET response will omit the {@code roleAssignment.roles} field entirely (not return an empty
 * array). Assertions must use {@code nullValue()}, not {@code equalTo(emptyList())}.
 */
@ApiModel(description = "Defines the role assignment mode and the roles to assign when sharing a user.")
public class RoleAssignments {

    /**
     * Controls whether roles are assigned to the shared user.
     * <ul>
     *   <li>{@code SELECTED} – assign the specified roles.</li>
     *   <li>{@code NONE} – share without any role assignments; existing roles on already-shared
     *       organizations are preserved.</li>
     * </ul>
     */
    public enum ModeEnum {

        SELECTED("SELECTED"),
        NONE("NONE");

        private final String value;

        ModeEnum(String value) {
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
        public static ModeEnum fromValue(String value) {
            for (ModeEnum e : ModeEnum.values()) {
                if (e.value.equalsIgnoreCase(value)) {
                    return e;
                }
            }
            throw new IllegalArgumentException("Unexpected mode value: " + value);
        }
    }

    private ModeEnum mode;
    private List<RoleWithAudience> roles = new ArrayList<>();

    public RoleAssignments mode(ModeEnum mode) {

        this.mode = mode;
        return this;
    }

    @ApiModelProperty(required = true, value = "Role assignment mode: SELECTED or NONE.")
    @JsonProperty("mode")
    @Valid
    @NotNull(message = "Property mode cannot be null.")
    public ModeEnum getMode() {
        return mode;
    }

    public void setMode(ModeEnum mode) {
        this.mode = mode;
    }

    public RoleAssignments roles(List<RoleWithAudience> roles) {

        this.roles = roles;
        return this;
    }

    @ApiModelProperty(value = "Roles to assign. Must be empty when mode is NONE.")
    @JsonProperty("roles")
    @Valid
    public List<RoleWithAudience> getRoles() {
        return roles;
    }

    public void setRoles(List<RoleWithAudience> roles) {
        this.roles = roles;
    }

    public RoleAssignments addRolesItem(RoleWithAudience role) {

        this.roles.add(role);
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
        RoleAssignments that = (RoleAssignments) o;
        return Objects.equals(this.mode, that.mode) &&
                Objects.equals(this.roles, that.roles);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mode, roles);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class RoleAssignments {\n");
        sb.append("    mode: ").append(toIndentedString(mode)).append("\n");
        sb.append("    roles: ").append(toIndentedString(roles)).append("\n");
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

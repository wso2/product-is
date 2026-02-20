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
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

/**
 * Role assignment configuration for shared users.
 * Controls which roles are assigned to the user in the target organization.
 */
@ApiModel(description = "Role assignment configuration for shared users.")
public class RoleAssignment {

    @XmlType(name = "ModeEnum")
    @XmlEnum(String.class)
    public enum ModeEnum {

        @XmlEnumValue("NONE") NONE(String.valueOf("NONE")),
        @XmlEnumValue("SELECTED") SELECTED(String.valueOf("SELECTED"));

        private String value;

        ModeEnum(String v) {
            value = v;
        }

        public String value() {
            return value;
        }

        @Override
        public String toString() {
            return String.valueOf(value);
        }

        public static ModeEnum fromValue(String value) {
            for (ModeEnum b : ModeEnum.values()) {
                if (b.value.equals(value)) {
                    return b;
                }
            }
            throw new IllegalArgumentException("Unexpected value '" + value + "'");
        }
    }

    private ModeEnum mode = ModeEnum.SELECTED;
    private List<RoleShareConfig> roles = null;

    /**
     * Mode of role assignment.
     * NONE: Do not assign any roles via user sharing.
     * SELECTED: Assign only the roles listed under roles.
     **/
    public RoleAssignment mode(ModeEnum mode) {
        this.mode = mode;
        return this;
    }

    @ApiModelProperty(value = "Mode of role assignment.")
    @JsonProperty("mode")
    @Valid
    public ModeEnum getMode() {
        return mode;
    }

    public void setMode(ModeEnum mode) {
        this.mode = mode;
    }

    /**
     * List of roles to assign when mode = SELECTED.
     **/
    public RoleAssignment roles(List<RoleShareConfig> roles) {
        this.roles = roles;
        return this;
    }

    @ApiModelProperty(value = "List of roles to assign when mode = SELECTED.")
    @JsonProperty("roles")
    @Valid
    public List<RoleShareConfig> getRoles() {
        return roles;
    }

    public void setRoles(List<RoleShareConfig> roles) {
        this.roles = roles;
    }

    public RoleAssignment addRolesItem(RoleShareConfig rolesItem) {
        if (this.roles == null) {
            this.roles = new ArrayList<>();
        }
        this.roles.add(rolesItem);
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
        RoleAssignment roleAssignment = (RoleAssignment) o;
        return Objects.equals(this.mode, roleAssignment.mode) &&
                Objects.equals(this.roles, roleAssignment.roles);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mode, roles);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class RoleAssignment {\n");
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

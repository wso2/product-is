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

package org.wso2.identity.integration.test.rest.api.server.application.management.v1.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

public class AssociatedRolesConfig {

    @XmlType(name = "AllowedAudienceEnum")
    @XmlEnum(String.class)
    public enum AllowedAudienceEnum {

        @XmlEnumValue("ORGANIZATION") ORGANIZATION(
                String.valueOf("ORGANIZATION")), @XmlEnumValue("APPLICATION") APPLICATION(
                String.valueOf("APPLICATION"));

        private String value;

        AllowedAudienceEnum(String v) {

            value = v;
        }

        public String value() {

            return value;
        }

        @Override
        public String toString() {

            return String.valueOf(value);
        }

        public static AllowedAudienceEnum fromValue(String value) {

            for (AllowedAudienceEnum b : AllowedAudienceEnum.values()) {
                if (b.value.equals(value)) {
                    return b;
                }
            }
            throw new IllegalArgumentException("Unexpected value '" + value + "'");
        }
    }

    private AllowedAudienceEnum allowedAudience = AllowedAudienceEnum.ORGANIZATION;
    private List<Role> roles = null;

    /**
     *
     **/
    public AssociatedRolesConfig allowedAudience(AllowedAudienceEnum allowedAudience) {

        this.allowedAudience = allowedAudience;
        return this;
    }

    @ApiModelProperty(example = "ORGANIZATION", required = true, value = "")
    @JsonProperty("allowedAudience")
    @Valid
    @NotNull(message = "Property allowedAudience cannot be null.")

    public AllowedAudienceEnum getAllowedAudience() {

        return allowedAudience;
    }

    public void setAllowedAudience(AllowedAudienceEnum allowedAudience) {

        this.allowedAudience = allowedAudience;
    }

    /**
     *
     **/
    public AssociatedRolesConfig roles(List<Role> roles) {

        this.roles = roles;
        return this;
    }

    @ApiModelProperty(value = "")
    @JsonProperty("roles")
    @Valid
    public List<Role> getRoles() {

        return roles;
    }

    public void setRoles(List<Role> roles) {

        this.roles = roles;
    }

    public AssociatedRolesConfig addRolesItem(Role rolesItem) {

        if (this.roles == null) {
            this.roles = new ArrayList<>();
        }
        this.roles.add(rolesItem);
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
        AssociatedRolesConfig associatedRolesConfig = (AssociatedRolesConfig) o;
        return Objects.equals(this.allowedAudience, associatedRolesConfig.allowedAudience) &&
                Objects.equals(this.roles, associatedRolesConfig.roles);
    }

    @Override
    public int hashCode() {

        return Objects.hash(allowedAudience, roles);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class AssociatedRolesConfig {\n");

        sb.append("    allowedAudience: ").append(toIndentedString(allowedAudience)).append("\n");
        sb.append("    roles: ").append(toIndentedString(roles)).append("\n");
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

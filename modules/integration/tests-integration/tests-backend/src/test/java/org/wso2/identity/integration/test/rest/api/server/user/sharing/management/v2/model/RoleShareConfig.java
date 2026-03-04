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

import java.util.Objects;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * Role to be assigned to the shared user in the sub-organization.
 */
@ApiModel(description = "Role to be assigned to the shared user in the sub-organization.")
public class RoleShareConfig {

    private String displayName;
    private RoleAudience audience;

    /**
     * Human-readable name of the role.
     **/
    public RoleShareConfig displayName(String displayName) {
        this.displayName = displayName;
        return this;
    }

    @ApiModelProperty(required = true, value = "Human-readable name of the role.")
    @JsonProperty("displayName")
    @Valid
    @NotNull(message = "Property displayName cannot be null.")
    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Audience details for the role.
     **/
    public RoleShareConfig audience(RoleAudience audience) {
        this.audience = audience;
        return this;
    }

    @ApiModelProperty(required = true, value = "Audience details for the role.")
    @JsonProperty("audience")
    @Valid
    @NotNull(message = "Property audience cannot be null.")
    public RoleAudience getAudience() {
        return audience;
    }

    public void setAudience(RoleAudience audience) {
        this.audience = audience;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RoleShareConfig roleShareConfig = (RoleShareConfig) o;
        return Objects.equals(this.displayName, roleShareConfig.displayName) &&
                Objects.equals(this.audience, roleShareConfig.audience);
    }

    @Override
    public int hashCode() {
        return Objects.hash(displayName, audience);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class RoleShareConfig {\n");
        sb.append("    displayName: ").append(toIndentedString(displayName)).append("\n");
        sb.append("    audience: ").append(toIndentedString(audience)).append("\n");
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

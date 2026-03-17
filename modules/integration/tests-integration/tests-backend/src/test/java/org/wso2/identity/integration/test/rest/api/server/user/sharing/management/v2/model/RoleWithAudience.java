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
import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * Maps to the {@code RoleShareConfig} schema in the V2 swagger spec.
 * <p>
 * Used both in request bodies (share / patch) and in GET response validation.
 * The swagger schema uses a <em>nested</em> audience object — the same structure as V1:
 * <ul>
 *   <li>{@code displayName} — the role name (e.g. "app-role-1")</li>
 *   <li>{@code audience.display} — the application or organization name the role belongs to</li>
 *   <li>{@code audience.type} — either {@code "application"} or {@code "organization"}</li>
 * </ul>
 */
public class RoleWithAudience {

    private String displayName;
    private RoleWithAudienceAudience audience;

    public RoleWithAudience displayName(String displayName) {

        this.displayName = displayName;
        return this;
    }

    @ApiModelProperty(example = "app-role-1", required = true, value = "The role name.")
    @JsonProperty("displayName")
    @Valid
    @NotNull(message = "Property displayName cannot be null.")
    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public RoleWithAudience audience(RoleWithAudienceAudience audience) {

        this.audience = audience;
        return this;
    }

    @ApiModelProperty(required = true, value = "The audience this role belongs to.")
    @JsonProperty("audience")
    @Valid
    @NotNull(message = "Property audience cannot be null.")
    public RoleWithAudienceAudience getAudience() {
        return audience;
    }

    public void setAudience(RoleWithAudienceAudience audience) {
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
        RoleWithAudience that = (RoleWithAudience) o;
        return Objects.equals(this.displayName, that.displayName) &&
                Objects.equals(this.audience, that.audience);
    }

    @Override
    public int hashCode() {
        return Objects.hash(displayName, audience);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class RoleWithAudience {\n");
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

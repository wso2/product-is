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

package org.wso2.identity.integration.test.rest.api.server.oidc.scope.management.v1.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ScopeUpdateRequest {
    private String displayName;
    private String description;
    private List<String> claims = null;

    /**
     *
     **/
    public ScopeUpdateRequest displayName(String displayName) {

        this.displayName = displayName;
        return this;
    }

    @ApiModelProperty(example = "scopeOne")
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
    public ScopeUpdateRequest description(String description) {

        this.description = description;
        return this;
    }

    @ApiModelProperty(example = "Sample updated scope one")
    @JsonProperty("description")
    @Valid
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     *
     **/
    public ScopeUpdateRequest claims(List<String> claims) {

        this.claims = claims;
        return this;
    }

    @ApiModelProperty(example = "Sample updated scope one")
    @JsonProperty("claims")
    @Valid
    public List<String> getClaims() {
        return claims;
    }

    public void setClaims(List<String> claims) {
        this.claims = claims;
    }

    public ScopeUpdateRequest addClaims(String claim) {
        if (this.claims == null) {
            this.claims = new ArrayList<>();
        }
        this.claims.add(claim);
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
        ScopeUpdateRequest scope = (ScopeUpdateRequest) o;
        return Objects.equals(this.displayName, scope.displayName) &&
                Objects.equals(this.description, scope.description) &&
                Objects.equals(this.claims, scope.claims);
    }

    @Override
    public int hashCode() {
        return Objects.hash(displayName, description, claims);
    }

    @Override
    public String toString() {

        return "class ScopeUpdateRequest {\n" +
                "    displayName: " + toIndentedString(displayName) + "\n" +
                "    description: " + toIndentedString(description) + "\n" +
                "    claims: " + toIndentedString(claims) + "\n" +
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

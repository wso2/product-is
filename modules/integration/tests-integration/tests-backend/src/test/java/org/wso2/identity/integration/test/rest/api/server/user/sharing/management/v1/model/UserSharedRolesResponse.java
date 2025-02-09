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

package org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v1.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.validation.Valid;

@ApiModel(description = "Response showing the roles assigned to a user within a specific organization, with pagination support for large role sets. ")
public class UserSharedRolesResponse  {
  
    private List<UserSharedOrganizationsResponseLinks> links = null;

    private List<RoleWithAudience> roles = null;


    /**
    * Pagination links for navigating the result set.
    **/
    public UserSharedRolesResponse links(List<UserSharedOrganizationsResponseLinks> links) {

        this.links = links;
        return this;
    }
    
    @ApiModelProperty(value = "Pagination links for navigating the result set.")
    @JsonProperty("links")
    @Valid
    public List<UserSharedOrganizationsResponseLinks> getLinks() {
        return links;
    }
    public void setLinks(List<UserSharedOrganizationsResponseLinks> links) {
        this.links = links;
    }

    public UserSharedRolesResponse addLinksItem(UserSharedOrganizationsResponseLinks linksItem) {
        if (this.links == null) {
            this.links = new ArrayList<>();
        }
        this.links.add(linksItem);
        return this;
    }

        /**
    * A list of roles with audience details
    **/
    public UserSharedRolesResponse roles(List<RoleWithAudience> roles) {

        this.roles = roles;
        return this;
    }
    
    @ApiModelProperty(value = "A list of roles with audience details")
    @JsonProperty("roles")
    @Valid
    public List<RoleWithAudience> getRoles() {
        return roles;
    }
    public void setRoles(List<RoleWithAudience> roles) {
        this.roles = roles;
    }

    public UserSharedRolesResponse addRolesItem(RoleWithAudience rolesItem) {
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
        UserSharedRolesResponse userSharedRolesResponse = (UserSharedRolesResponse) o;
        return Objects.equals(this.links, userSharedRolesResponse.links) &&
            Objects.equals(this.roles, userSharedRolesResponse.roles);
    }

    @Override
    public int hashCode() {
        return Objects.hash(links, roles);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class UserSharedRolesResponse {\n");
        
        sb.append("    links: ").append(toIndentedString(links)).append("\n");
        sb.append("    roles: ").append(toIndentedString(roles)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
    * Convert the given object to string with each line indented by 4 spaces
    * (except the first line).
    */
    private String toIndentedString(Object o) {

        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n");
    }
}


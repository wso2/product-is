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

@ApiModel(description = "Response listing organizations where a user has shared access, including sharing policies, shared type and pagination links for navigating results. ")
public class UserSharedOrganizationsResponse  {
  
    private List<UserSharedOrganizationsResponseLinks> links = null;

    private List<UserSharedOrganizationsResponseSharedOrganizations> sharedOrganizations = null;


    /**
    * Pagination links for navigating the result set.
    **/
    public UserSharedOrganizationsResponse links(List<UserSharedOrganizationsResponseLinks> links) {

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

    public UserSharedOrganizationsResponse addLinksItem(UserSharedOrganizationsResponseLinks linksItem) {
        if (this.links == null) {
            this.links = new ArrayList<>();
        }
        this.links.add(linksItem);
        return this;
    }

        /**
    * A list of shared access details for the user across multiple organizations
    **/
    public UserSharedOrganizationsResponse sharedOrganizations(List<UserSharedOrganizationsResponseSharedOrganizations> sharedOrganizations) {

        this.sharedOrganizations = sharedOrganizations;
        return this;
    }
    
    @ApiModelProperty(value = "A list of shared access details for the user across multiple organizations")
    @JsonProperty("sharedOrganizations")
    @Valid
    public List<UserSharedOrganizationsResponseSharedOrganizations> getSharedOrganizations() {
        return sharedOrganizations;
    }
    public void setSharedOrganizations(List<UserSharedOrganizationsResponseSharedOrganizations> sharedOrganizations) {
        this.sharedOrganizations = sharedOrganizations;
    }

    public UserSharedOrganizationsResponse addSharedOrganizationsItem(UserSharedOrganizationsResponseSharedOrganizations sharedOrganizationsItem) {
        if (this.sharedOrganizations == null) {
            this.sharedOrganizations = new ArrayList<>();
        }
        this.sharedOrganizations.add(sharedOrganizationsItem);
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
        UserSharedOrganizationsResponse userSharedOrganizationsResponse = (UserSharedOrganizationsResponse) o;
        return Objects.equals(this.links, userSharedOrganizationsResponse.links) &&
            Objects.equals(this.sharedOrganizations, userSharedOrganizationsResponse.sharedOrganizations);
    }

    @Override
    public int hashCode() {
        return Objects.hash(links, sharedOrganizations);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class UserSharedOrganizationsResponse {\n");
        
        sb.append("    links: ").append(toIndentedString(links)).append("\n");
        sb.append("    sharedOrganizations: ").append(toIndentedString(sharedOrganizations)).append("\n");
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


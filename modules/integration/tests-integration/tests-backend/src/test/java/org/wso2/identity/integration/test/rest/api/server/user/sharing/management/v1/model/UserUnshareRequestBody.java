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
import javax.validation.constraints.NotNull;

@ApiModel(description = "The request body for unsharing users from multiple organizations. Includes a list of user IDs and a list of organization IDs. ")
public class UserUnshareRequestBody  {
  
    private UserUnshareRequestBodyUserCriteria userCriteria;
    private List<String> organizations = new ArrayList<>();


    /**
    **/
    public UserUnshareRequestBody userCriteria(UserUnshareRequestBodyUserCriteria userCriteria) {

        this.userCriteria = userCriteria;
        return this;
    }
    
    @ApiModelProperty(required = true, value = "")
    @JsonProperty("userCriteria")
    @Valid
    @NotNull(message = "Property userCriteria cannot be null.")

    public UserUnshareRequestBodyUserCriteria getUserCriteria() {
        return userCriteria;
    }
    public void setUserCriteria(UserUnshareRequestBodyUserCriteria userCriteria) {
        this.userCriteria = userCriteria;
    }

    /**
    * List of organization IDs from which the users should be unshared.
    **/
    public UserUnshareRequestBody organizations(List<String> organizations) {

        this.organizations = organizations;
        return this;
    }
    
    @ApiModelProperty(required = true, value = "List of organization IDs from which the users should be unshared.")
    @JsonProperty("organizations")
    @Valid
    @NotNull(message = "Property organizations cannot be null.")

    public List<String> getOrganizations() {
        return organizations;
    }
    public void setOrganizations(List<String> organizations) {
        this.organizations = organizations;
    }

    public UserUnshareRequestBody addOrganizationsItem(String organizationsItem) {
        this.organizations.add(organizationsItem);
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
        UserUnshareRequestBody userUnshareRequestBody = (UserUnshareRequestBody) o;
        return Objects.equals(this.userCriteria, userUnshareRequestBody.userCriteria) &&
            Objects.equals(this.organizations, userUnshareRequestBody.organizations);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userCriteria, organizations);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class UserUnshareRequestBody {\n");
        
        sb.append("    userCriteria: ").append(toIndentedString(userCriteria)).append("\n");
        sb.append("    organizations: ").append(toIndentedString(organizations)).append("\n");
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


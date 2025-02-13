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
import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;

import javax.validation.Valid;

public class UserSharedOrganizationsResponseSharedOrganizations  {
  
    private String orgId;
    private String orgName;
    private String sharedUserId;
    private String sharedType;
    private String rolesRef;

    /**
    * ID of the child organization
    **/
    public UserSharedOrganizationsResponseSharedOrganizations orgId(String orgId) {

        this.orgId = orgId;
        return this;
    }
    
    @ApiModelProperty(example = "b028ca17-8f89-449c-ae27-fa955e66465d", value = "ID of the child organization")
    @JsonProperty("orgId")
    @Valid
    public String getOrgId() {
        return orgId;
    }
    public void setOrgId(String orgId) {
        this.orgId = orgId;
    }

    /**
    * Name of the child organization
    **/
    public UserSharedOrganizationsResponseSharedOrganizations orgName(String orgName) {

        this.orgName = orgName;
        return this;
    }
    
    @ApiModelProperty(example = "Organization Name", value = "Name of the child organization")
    @JsonProperty("orgName")
    @Valid
    public String getOrgName() {
        return orgName;
    }
    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }

    /**
    * ID of the shared user
    **/
    public UserSharedOrganizationsResponseSharedOrganizations sharedUserId(String sharedUserId) {

        this.sharedUserId = sharedUserId;
        return this;
    }
    
    @ApiModelProperty(example = "7a1b7d63-8cfc-4dc9-9332-3f84641b72d8", value = "ID of the shared user")
    @JsonProperty("sharedUserId")
    @Valid
    public String getSharedUserId() {
        return sharedUserId;
    }
    public void setSharedUserId(String sharedUserId) {
        this.sharedUserId = sharedUserId;
    }

    /**
    * Shared type of the user (SHARED/INVITED)
    **/
    public UserSharedOrganizationsResponseSharedOrganizations sharedType(String sharedType) {

        this.sharedType = sharedType;
        return this;
    }
    
    @ApiModelProperty(example = "SHARED", value = "Shared type of the user (SHARED/INVITED)")
    @JsonProperty("sharedType")
    @Valid
    public String getSharedType() {
        return sharedType;
    }
    public void setSharedType(String sharedType) {
        this.sharedType = sharedType;
    }

    /**
    * URL reference to retrieve paginated roles for the shared user in this organization
    **/
    public UserSharedOrganizationsResponseSharedOrganizations rolesRef(String rolesRef) {

        this.rolesRef = rolesRef;
        return this;
    }
    
    @ApiModelProperty(example = "/api/server/v1/users/{userId}/shared-roles?orgId=b028ca17-8f89-449c-ae27-fa955e66465d&after=&before=&limit=2&filter=&recursive=false", value = "URL reference to retrieve paginated roles for the shared user in this organization")
    @JsonProperty("rolesRef")
    @Valid
    public String getRolesRef() {
        return rolesRef;
    }
    public void setRolesRef(String rolesRef) {
        this.rolesRef = rolesRef;
    }



    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UserSharedOrganizationsResponseSharedOrganizations userSharedOrganizationsResponseSharedOrganizations = (UserSharedOrganizationsResponseSharedOrganizations) o;
        return Objects.equals(this.orgId, userSharedOrganizationsResponseSharedOrganizations.orgId) &&
            Objects.equals(this.orgName, userSharedOrganizationsResponseSharedOrganizations.orgName) &&
            Objects.equals(this.sharedUserId, userSharedOrganizationsResponseSharedOrganizations.sharedUserId) &&
            Objects.equals(this.sharedType, userSharedOrganizationsResponseSharedOrganizations.sharedType) &&
            Objects.equals(this.rolesRef, userSharedOrganizationsResponseSharedOrganizations.rolesRef);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orgId, orgName, sharedUserId, sharedType, rolesRef);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class UserSharedOrganizationsResponseSharedOrganizations {\n");
        
        sb.append("    orgId: ").append(toIndentedString(orgId)).append("\n");
        sb.append("    orgName: ").append(toIndentedString(orgName)).append("\n");
        sb.append("    sharedUserId: ").append(toIndentedString(sharedUserId)).append("\n");
        sb.append("    sharedType: ").append(toIndentedString(sharedType)).append("\n");
        sb.append("    rolesRef: ").append(toIndentedString(rolesRef)).append("\n");
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


/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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

public class ApplicationSharePOSTRequest {
  
    private Boolean shareWithAllChildren = false;
    private List<String> sharedOrganizations = null;


    /**
    **/
    public ApplicationSharePOSTRequest shareWithAllChildren(Boolean shareWithAllChildren) {

        this.shareWithAllChildren = shareWithAllChildren;
        return this;
    }
    
    @ApiModelProperty(value = "")
    @JsonProperty("shareWithAllChildren")
    @Valid
    public Boolean getShareWithAllChildren() {
        return shareWithAllChildren;
    }
    public void setShareWithAllChildren(Boolean shareWithAllChildren) {
        this.shareWithAllChildren = shareWithAllChildren;
    }

    /**
    **/
    public ApplicationSharePOSTRequest sharedOrganizations(List<String> sharedOrganizations) {

        this.sharedOrganizations = sharedOrganizations;
        return this;
    }
    
    @ApiModelProperty(value = "")
    @JsonProperty("sharedOrganizations")
    @Valid
    public List<String> getSharedOrganizations() {
        return sharedOrganizations;
    }
    public void setSharedOrganizations(List<String> sharedOrganizations) {
        this.sharedOrganizations = sharedOrganizations;
    }

    public ApplicationSharePOSTRequest addSharedOrganizationsItem(String sharedOrganizationsItem) {
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
        ApplicationSharePOSTRequest applicationSharePOSTRequest = (ApplicationSharePOSTRequest) o;
        return Objects.equals(this.shareWithAllChildren, applicationSharePOSTRequest.shareWithAllChildren) &&
            Objects.equals(this.sharedOrganizations, applicationSharePOSTRequest.sharedOrganizations);
    }

    @Override
    public int hashCode() {
        return Objects.hash(shareWithAllChildren, sharedOrganizations);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class ApplicationSharePOSTRequest {\n");
        
        sb.append("    shareWithAllChildren: ").append(toIndentedString(shareWithAllChildren)).append("\n");
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


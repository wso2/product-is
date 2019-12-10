/*
* Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.wso2.identity.integration.test.rest.api.server.application.management.v1.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.validation.Valid;

public class RoleConfig  {
  
    private List<RoleMapping> mappings = null;

    private Boolean includeUserDomain;
    private String claimId;

    /**
    **/
    public RoleConfig mappings(List<RoleMapping> mappings) {

        this.mappings = mappings;
        return this;
    }
    
    @ApiModelProperty(value = "")
    @JsonProperty("mappings")
    @Valid
    public List<RoleMapping> getMappings() {
        return mappings;
    }
    public void setMappings(List<RoleMapping> mappings) {
        this.mappings = mappings;
    }

    public RoleConfig addMappingsItem(RoleMapping mappingsItem) {
        if (this.mappings == null) {
            this.mappings = new ArrayList<>();
        }
        this.mappings.add(mappingsItem);
        return this;
    }

        /**
    **/
    public RoleConfig includeUserDomain(Boolean includeUserDomain) {

        this.includeUserDomain = includeUserDomain;
        return this;
    }
    
    @ApiModelProperty(example = "true", value = "")
    @JsonProperty("includeUserDomain")
    @Valid
    public Boolean getIncludeUserDomain() {
        return includeUserDomain;
    }
    public void setIncludeUserDomain(Boolean includeUserDomain) {
        this.includeUserDomain = includeUserDomain;
    }

    /**
    **/
    public RoleConfig claimId(String claimId) {

        this.claimId = claimId;
        return this;
    }
    
    @ApiModelProperty(example = "http://wso2.org/claims/groups", value = "")
    @JsonProperty("claimId")
    @Valid
    public String getClaimId() {
        return claimId;
    }
    public void setClaimId(String claimId) {
        this.claimId = claimId;
    }



    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RoleConfig roleConfig = (RoleConfig) o;
        return Objects.equals(this.mappings, roleConfig.mappings) &&
            Objects.equals(this.includeUserDomain, roleConfig.includeUserDomain) &&
            Objects.equals(this.claimId, roleConfig.claimId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mappings, includeUserDomain, claimId);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class RoleConfig {\n");

        sb.append("    mappings: ").append(toIndentedString(mappings)).append("\n");
        sb.append("    includeUserDomain: ").append(toIndentedString(includeUserDomain)).append("\n");
        sb.append("    claimId: ").append(toIndentedString(claimId)).append("\n");
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


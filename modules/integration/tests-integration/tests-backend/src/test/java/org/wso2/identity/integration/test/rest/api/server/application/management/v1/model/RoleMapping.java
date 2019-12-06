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

import java.util.Objects;
import javax.validation.Valid;

public class RoleMapping {
  
    private String localRole;
    private String applicationRole;

    /**
    **/
    public RoleMapping localRole(String localRole) {

        this.localRole = localRole;
        return this;
    }
    
    @ApiModelProperty(example = "admin", value = "")
    @JsonProperty("localRole")
    @Valid
    public String getLocalRole() {
        return localRole;
    }
    public void setLocalRole(String localRole) {
        this.localRole = localRole;
    }

    /**
    **/
    public RoleMapping applicationRole(String applicationRole) {

        this.applicationRole = applicationRole;
        return this;
    }
    
    @ApiModelProperty(example = "Administrator", value = "")
    @JsonProperty("applicationRole")
    @Valid
    public String getApplicationRole() {
        return applicationRole;
    }
    public void setApplicationRole(String applicationRole) {
        this.applicationRole = applicationRole;
    }



    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RoleMapping roleMapping = (RoleMapping) o;
        return Objects.equals(this.localRole, roleMapping.localRole) &&
            Objects.equals(this.applicationRole, roleMapping.applicationRole);
    }

    @Override
    public int hashCode() {
        return Objects.hash(localRole, applicationRole);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class RoleMapping {\n");

        sb.append("    localRole: ").append(toIndentedString(localRole)).append("\n");
        sb.append("    applicationRole: ").append(toIndentedString(applicationRole)).append("\n");
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


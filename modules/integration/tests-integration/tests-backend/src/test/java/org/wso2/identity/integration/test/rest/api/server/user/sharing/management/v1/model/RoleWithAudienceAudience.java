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
import javax.validation.constraints.NotNull;

public class RoleWithAudienceAudience {
  
    private String display;
    private String type;

    /**
    * Display name of the audience
    **/
    public RoleWithAudienceAudience display(String display) {

        this.display = display;
        return this;
    }
    
    @ApiModelProperty(example = "My Org 1", required = true, value = "Display name of the audience")
    @JsonProperty("display")
    @Valid
    @NotNull(message = "Property display cannot be null.")

    public String getDisplay() {
        return display;
    }
    public void setDisplay(String display) {
        this.display = display;
    }

    /**
    * Type of the audience, e.g., &#39;organization&#39; or &#39;application&#39;
    **/
    public RoleWithAudienceAudience type(String type) {

        this.type = type;
        return this;
    }
    
    @ApiModelProperty(example = "organization", required = true, value = "Type of the audience, e.g., 'organization' or 'application'")
    @JsonProperty("type")
    @Valid
    @NotNull(message = "Property type cannot be null.")

    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }



    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RoleWithAudienceAudience roleWithAudienceAudience = (RoleWithAudienceAudience) o;
        return Objects.equals(this.display, roleWithAudienceAudience.display) &&
            Objects.equals(this.type, roleWithAudienceAudience.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(display, type);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class RoleWithAudienceAudience {\n");
        
        sb.append("    display: ").append(toIndentedString(display)).append("\n");
        sb.append("    type: ").append(toIndentedString(type)).append("\n");
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


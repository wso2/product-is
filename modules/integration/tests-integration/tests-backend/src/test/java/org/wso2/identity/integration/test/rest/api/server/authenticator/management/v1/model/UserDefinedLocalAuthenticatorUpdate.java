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

package org.wso2.carbon.identity.api.server.authenticators.v1.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.wso2.carbon.identity.api.server.authenticators.v1.model.Endpoint;
import javax.validation.constraints.*;

/**
 * TThis represents the configuration for updating user defined local authenticator.
 **/

import io.swagger.annotations.*;
import java.util.Objects;
import javax.validation.Valid;
import javax.xml.bind.annotation.*;
@ApiModel(description = "TThis represents the configuration for updating user defined local authenticator.")
public class UserDefinedLocalAuthenticatorUpdate  {
  
    private String displayName;
    private Boolean isEnabled;
    private String image;
    private String description;
    private Endpoint endpoint;

    /**
    **/
    public UserDefinedLocalAuthenticatorUpdate displayName(String displayName) {

        this.displayName = displayName;
        return this;
    }
    
    @ApiModelProperty(example = "Custom auth", required = true, value = "")
    @JsonProperty("displayName")
    @Valid
    @NotNull(message = "Property displayName cannot be null.")

    public String getDisplayName() {
        return displayName;
    }
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
    **/
    public UserDefinedLocalAuthenticatorUpdate isEnabled(Boolean isEnabled) {

        this.isEnabled = isEnabled;
        return this;
    }
    
    @ApiModelProperty(example = "true", required = true, value = "")
    @JsonProperty("isEnabled")
    @Valid
    @NotNull(message = "Property isEnabled cannot be null.")

    public Boolean getIsEnabled() {
        return isEnabled;
    }
    public void setIsEnabled(Boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

    /**
    **/
    public UserDefinedLocalAuthenticatorUpdate image(String image) {

        this.image = image;
        return this;
    }
    
    @ApiModelProperty(example = "https://custom-authenticator-logo-url", value = "")
    @JsonProperty("image")
    @Valid
    public String getImage() {
        return image;
    }
    public void setImage(String image) {
        this.image = image;
    }

    /**
    **/
    public UserDefinedLocalAuthenticatorUpdate description(String description) {

        this.description = description;
        return this;
    }
    
    @ApiModelProperty(example = "The user defined custom local authenticator.", value = "")
    @JsonProperty("description")
    @Valid
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    /**
    **/
    public UserDefinedLocalAuthenticatorUpdate endpoint(Endpoint endpoint) {

        this.endpoint = endpoint;
        return this;
    }
    
    @ApiModelProperty(required = true, value = "")
    @JsonProperty("endpoint")
    @Valid
    @NotNull(message = "Property endpoint cannot be null.")

    public Endpoint getEndpoint() {
        return endpoint;
    }
    public void setEndpoint(Endpoint endpoint) {
        this.endpoint = endpoint;
    }



    @Override
    public boolean equals(java.lang.Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UserDefinedLocalAuthenticatorUpdate userDefinedLocalAuthenticatorUpdate = (UserDefinedLocalAuthenticatorUpdate) o;
        return Objects.equals(this.displayName, userDefinedLocalAuthenticatorUpdate.displayName) &&
            Objects.equals(this.isEnabled, userDefinedLocalAuthenticatorUpdate.isEnabled) &&
            Objects.equals(this.image, userDefinedLocalAuthenticatorUpdate.image) &&
            Objects.equals(this.description, userDefinedLocalAuthenticatorUpdate.description) &&
            Objects.equals(this.endpoint, userDefinedLocalAuthenticatorUpdate.endpoint);
    }

    @Override
    public int hashCode() {
        return Objects.hash(displayName, isEnabled, image, description, endpoint);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class UserDefinedLocalAuthenticatorUpdate {\n");
        
        sb.append("    displayName: ").append(toIndentedString(displayName)).append("\n");
        sb.append("    isEnabled: ").append(toIndentedString(isEnabled)).append("\n");
        sb.append("    image: ").append(toIndentedString(image)).append("\n");
        sb.append("    description: ").append(toIndentedString(description)).append("\n");
        sb.append("    endpoint: ").append(toIndentedString(endpoint)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
    * Convert the given object to string with each line indented by 4 spaces
    * (except the first line).
    */
    private String toIndentedString(java.lang.Object o) {

        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n");
    }
}


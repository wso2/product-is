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
 * This represents the configuration for creating the user defined local authenticator.
 **/

import io.swagger.annotations.*;
import java.util.Objects;
import javax.validation.Valid;
import javax.xml.bind.annotation.*;
@ApiModel(description = "This represents the configuration for creating the user defined local authenticator.")
public class UserDefinedLocalAuthenticatorCreation  {
  
    private String name;
    private String id;
    private String displayName;
    private Boolean isEnabled;

@XmlType(name="AuthenticationTypeEnum")
@XmlEnum(String.class)
public enum AuthenticationTypeEnum {

    @XmlEnumValue("IDENTIFICATION") IDENTIFICATION(String.valueOf("IDENTIFICATION")), @XmlEnumValue("VERIFICATION") VERIFICATION(String.valueOf("VERIFICATION"));


    private String value;

    AuthenticationTypeEnum(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    public static AuthenticationTypeEnum fromValue(String value) {
        for (AuthenticationTypeEnum b : AuthenticationTypeEnum.values()) {
            if (b.value.equals(value)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
}

    private AuthenticationTypeEnum authenticationType;
    private String image;
    private String description;
    private Endpoint endpoint;

    /**
    **/
    public UserDefinedLocalAuthenticatorCreation name(String name) {

        this.name = name;
        return this;
    }
    
    @ApiModelProperty(example = "CustomAuthenticator", required = true, value = "")
    @JsonProperty("name")
    @Valid
    @NotNull(message = "Property name cannot be null.")

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    /**
    **/
    public UserDefinedLocalAuthenticatorCreation id(String id) {

        this.id = id;
        return this;
    }
    
    @ApiModelProperty(example = "Q3VzdG9tQXV0aGVudGljYXRvcg==", value = "")
    @JsonProperty("id")
    @Valid
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    /**
    **/
    public UserDefinedLocalAuthenticatorCreation displayName(String displayName) {

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
    public UserDefinedLocalAuthenticatorCreation isEnabled(Boolean isEnabled) {

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
    public UserDefinedLocalAuthenticatorCreation authenticationType(AuthenticationTypeEnum authenticationType) {

        this.authenticationType = authenticationType;
        return this;
    }
    
    @ApiModelProperty(value = "")
    @JsonProperty("authenticationType")
    @Valid
    public AuthenticationTypeEnum getAuthenticationType() {
        return authenticationType;
    }
    public void setAuthenticationType(AuthenticationTypeEnum authenticationType) {
        this.authenticationType = authenticationType;
    }

    /**
    **/
    public UserDefinedLocalAuthenticatorCreation image(String image) {

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
    public UserDefinedLocalAuthenticatorCreation description(String description) {

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
    public UserDefinedLocalAuthenticatorCreation endpoint(Endpoint endpoint) {

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
        UserDefinedLocalAuthenticatorCreation userDefinedLocalAuthenticatorCreation = (UserDefinedLocalAuthenticatorCreation) o;
        return Objects.equals(this.name, userDefinedLocalAuthenticatorCreation.name) &&
            Objects.equals(this.id, userDefinedLocalAuthenticatorCreation.id) &&
            Objects.equals(this.displayName, userDefinedLocalAuthenticatorCreation.displayName) &&
            Objects.equals(this.isEnabled, userDefinedLocalAuthenticatorCreation.isEnabled) &&
            Objects.equals(this.authenticationType, userDefinedLocalAuthenticatorCreation.authenticationType) &&
            Objects.equals(this.image, userDefinedLocalAuthenticatorCreation.image) &&
            Objects.equals(this.description, userDefinedLocalAuthenticatorCreation.description) &&
            Objects.equals(this.endpoint, userDefinedLocalAuthenticatorCreation.endpoint);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, id, displayName, isEnabled, authenticationType, image, description, endpoint);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class UserDefinedLocalAuthenticatorCreation {\n");
        
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    displayName: ").append(toIndentedString(displayName)).append("\n");
        sb.append("    isEnabled: ").append(toIndentedString(isEnabled)).append("\n");
        sb.append("    authenticationType: ").append(toIndentedString(authenticationType)).append("\n");
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


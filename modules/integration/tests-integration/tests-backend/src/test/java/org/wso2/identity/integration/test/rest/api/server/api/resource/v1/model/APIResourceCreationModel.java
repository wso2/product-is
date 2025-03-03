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

package org.wso2.identity.integration.test.rest.api.server.api.resource.v1.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class APIResourceCreationModel {
  
    private String name;
    private String identifier;
    private String description;
    private Boolean requiresAuthorization;
    private List<ScopeCreationModel> scopes = null;

    private List<AuthorizationDetailsTypesCreationModel> authorizationDetailsTypes = null;


    /**
    **/
    public APIResourceCreationModel name(String name) {

        this.name = name;
        return this;
    }
    
    @ApiModelProperty(example = "Greetings API", required = true, value = "")
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
    public APIResourceCreationModel identifier(String identifier) {

        this.identifier = identifier;
        return this;
    }
    
    @ApiModelProperty(example = "greetings_api", required = true, value = "")
    @JsonProperty("identifier")
    @Valid
    @NotNull(message = "Property identifier cannot be null.")

    public String getIdentifier() {
        return identifier;
    }
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    /**
    **/
    public APIResourceCreationModel description(String description) {

        this.description = description;
        return this;
    }
    
    @ApiModelProperty(example = "Greetings API representation", value = "")
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
    public APIResourceCreationModel requiresAuthorization(Boolean requiresAuthorization) {

        this.requiresAuthorization = requiresAuthorization;
        return this;
    }
    
    @ApiModelProperty(example = "true", value = "")
    @JsonProperty("requiresAuthorization")
    @Valid
    public Boolean getRequiresAuthorization() {
        return requiresAuthorization;
    }
    public void setRequiresAuthorization(Boolean requiresAuthorization) {
        this.requiresAuthorization = requiresAuthorization;
    }

    /**
    **/
    public APIResourceCreationModel scopes(List<ScopeCreationModel> scopes) {

        this.scopes = scopes;
        return this;
    }
    
    @ApiModelProperty(value = "")
    @JsonProperty("scopes")
    @Valid
    public List<ScopeCreationModel> getScopes() {
        return scopes;
    }
    public void setScopes(List<ScopeCreationModel> scopes) {
        this.scopes = scopes;
    }

    public APIResourceCreationModel addScopesItem(ScopeCreationModel scopesItem) {
        if (this.scopes == null) {
            this.scopes = new ArrayList<ScopeCreationModel>();
        }
        this.scopes.add(scopesItem);
        return this;
    }

        /**
    **/
    public APIResourceCreationModel authorizationDetailsTypes(List<AuthorizationDetailsTypesCreationModel> authorizationDetailsTypes) {

        this.authorizationDetailsTypes = authorizationDetailsTypes;
        return this;
    }
    
    @ApiModelProperty(value = "")
    @JsonProperty("authorizationDetailsTypes")
    @Valid
    public List<AuthorizationDetailsTypesCreationModel> getAuthorizationDetailsTypes() {
        return authorizationDetailsTypes;
    }
    public void setAuthorizationDetailsTypes(List<AuthorizationDetailsTypesCreationModel> authorizationDetailsTypes) {
        this.authorizationDetailsTypes = authorizationDetailsTypes;
    }

    public APIResourceCreationModel addAuthorizationDetailsTypesItem(AuthorizationDetailsTypesCreationModel authorizationDetailsTypesItem) {
        if (this.authorizationDetailsTypes == null) {
            this.authorizationDetailsTypes = new ArrayList<AuthorizationDetailsTypesCreationModel>();
        }
        this.authorizationDetailsTypes.add(authorizationDetailsTypesItem);
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
        APIResourceCreationModel apIResourceCreationModel = (APIResourceCreationModel) o;
        return Objects.equals(this.name, apIResourceCreationModel.name) &&
            Objects.equals(this.identifier, apIResourceCreationModel.identifier) &&
            Objects.equals(this.description, apIResourceCreationModel.description) &&
            Objects.equals(this.requiresAuthorization, apIResourceCreationModel.requiresAuthorization) &&
            Objects.equals(this.scopes, apIResourceCreationModel.scopes) &&
            Objects.equals(this.authorizationDetailsTypes, apIResourceCreationModel.authorizationDetailsTypes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, identifier, description, requiresAuthorization, scopes, authorizationDetailsTypes);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class APIResourceCreationModel {\n");
        
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    identifier: ").append(toIndentedString(identifier)).append("\n");
        sb.append("    description: ").append(toIndentedString(description)).append("\n");
        sb.append("    requiresAuthorization: ").append(toIndentedString(requiresAuthorization)).append("\n");
        sb.append("    scopes: ").append(toIndentedString(scopes)).append("\n");
        sb.append("    authorizationDetailsTypes: ").append(toIndentedString(authorizationDetailsTypes)).append("\n");
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


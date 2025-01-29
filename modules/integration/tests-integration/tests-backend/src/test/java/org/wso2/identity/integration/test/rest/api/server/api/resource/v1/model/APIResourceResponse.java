/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
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

public class APIResourceResponse {

    private String id;
    private String name;
    private String description;
    private String identifier;
    private String type;
    private Boolean requiresAuthorization;
    private List<ScopeGetModel> scopes = null;

    private List<SubscribedApplicationGetModel> subscribedApplications = null;

    private List<Property> properties = null;

    private String self;

    private List<AuthorizationDetailsType> authorizationDetailsTypes = null;

    /**
     **/
    public APIResourceResponse id(String id) {

        this.id = id;
        return this;
    }

    @ApiModelProperty(example = "gh43-jk34-vb34-df67", required = true, value = "")
    @JsonProperty("id")
    @Valid
    @NotNull(message = "Property id cannot be null.")

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    /**
     **/
    public APIResourceResponse name(String name) {

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
    public APIResourceResponse description(String description) {

        this.description = description;
        return this;
    }

    @ApiModelProperty(example = "Greeting API representation", value = "")
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
    public APIResourceResponse identifier(String identifier) {

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
    public APIResourceResponse type(String type) {

        this.type = type;
        return this;
    }

    @ApiModelProperty(example = "SYSTEM", value = "")
    @JsonProperty("type")
    @Valid
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }

    /**
     **/
    public APIResourceResponse requiresAuthorization(Boolean requiresAuthorization) {

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
    public APIResourceResponse scopes(List<ScopeGetModel> scopes) {

        this.scopes = scopes;
        return this;
    }

    @ApiModelProperty(value = "")
    @JsonProperty("scopes")
    @Valid
    public List<ScopeGetModel> getScopes() {
        return scopes;
    }
    public void setScopes(List<ScopeGetModel> scopes) {
        this.scopes = scopes;
    }

    public APIResourceResponse addScopesItem(ScopeGetModel scopesItem) {
        if (this.scopes == null) {
            this.scopes = new ArrayList<ScopeGetModel>();
        }
        this.scopes.add(scopesItem);
        return this;
    }

    /**
     **/
    public APIResourceResponse subscribedApplications(List<SubscribedApplicationGetModel> subscribedApplications) {

        this.subscribedApplications = subscribedApplications;
        return this;
    }

    @ApiModelProperty(value = "")
    @JsonProperty("subscribedApplications")
    @Valid
    public List<SubscribedApplicationGetModel> getSubscribedApplications() {
        return subscribedApplications;
    }
    public void setSubscribedApplications(List<SubscribedApplicationGetModel> subscribedApplications) {
        this.subscribedApplications = subscribedApplications;
    }

    public APIResourceResponse addSubscribedApplicationsItem(SubscribedApplicationGetModel subscribedApplicationsItem) {
        if (this.subscribedApplications == null) {
            this.subscribedApplications = new ArrayList<SubscribedApplicationGetModel>();
        }
        this.subscribedApplications.add(subscribedApplicationsItem);
        return this;
    }

    /**
     **/
    public APIResourceResponse properties(List<Property> properties) {

        this.properties = properties;
        return this;
    }

    @ApiModelProperty(value = "")
    @JsonProperty("properties")
    @Valid
    public List<Property> getProperties() {
        return properties;
    }
    public void setProperties(List<Property> properties) {
        this.properties = properties;
    }

    public APIResourceResponse addPropertiesItem(Property propertiesItem) {
        if (this.properties == null) {
            this.properties = new ArrayList<Property>();
        }
        this.properties.add(propertiesItem);
        return this;
    }

    /**
     **/
    public APIResourceResponse self(String self) {

        this.self = self;
        return this;
    }

    @ApiModelProperty(example = "/t/carbon.super/api/server/v1/api-resources/eDUwOUNlcnRpZmljYXRlQXV0aGVudGljYXRvcg", required = true, value = "")
    @JsonProperty("self")
    @Valid
    @NotNull(message = "Property self cannot be null.")

    public String getSelf() {
        return self;
    }
    public void setSelf(String self) {
        this.self = self;
    }

    public APIResourceResponse authorizationDetailsTypes(List<AuthorizationDetailsType> authorizationDetailsTypes) {

        this.authorizationDetailsTypes = authorizationDetailsTypes;
        return this;
    }

    @ApiModelProperty(value = "")
    @JsonProperty("authorizationDetailsTypes")
    @Valid
    public List<AuthorizationDetailsType> getAuthorizationDetailsTypes() {
        return authorizationDetailsTypes;
    }

    public void setAuthorizationDetailsTypes(List<AuthorizationDetailsType> authorizationDetailsTypes) {
        this.authorizationDetailsTypes = authorizationDetailsTypes;
    }

    public APIResourceResponse addAuthorizationDetailsTypesItem(AuthorizationDetailsType authorizationDetailsTypeItem) {
        if (this.authorizationDetailsTypes == null) {
            this.authorizationDetailsTypes = new ArrayList<AuthorizationDetailsType>();
        }
        this.authorizationDetailsTypes.add(authorizationDetailsTypeItem);
        return this;
    }

    @Override
    public boolean equals(java.lang.Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        APIResourceResponse apIResourceResponse = (APIResourceResponse) o;
        return Objects.equals(this.id, apIResourceResponse.id) &&
                Objects.equals(this.name, apIResourceResponse.name) &&
                Objects.equals(this.description, apIResourceResponse.description) &&
                Objects.equals(this.identifier, apIResourceResponse.identifier) &&
                Objects.equals(this.type, apIResourceResponse.type) &&
                Objects.equals(this.requiresAuthorization, apIResourceResponse.requiresAuthorization) &&
                Objects.equals(this.scopes, apIResourceResponse.scopes) &&
                Objects.equals(this.subscribedApplications, apIResourceResponse.subscribedApplications) &&
                Objects.equals(this.properties, apIResourceResponse.properties) &&
                Objects.equals(this.self, apIResourceResponse.self);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, description, identifier, type, requiresAuthorization, scopes, subscribedApplications, properties, self);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class APIResourceResponse {\n");

        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    description: ").append(toIndentedString(description)).append("\n");
        sb.append("    identifier: ").append(toIndentedString(identifier)).append("\n");
        sb.append("    type: ").append(toIndentedString(type)).append("\n");
        sb.append("    requiresAuthorization: ").append(toIndentedString(requiresAuthorization)).append("\n");
        sb.append("    scopes: ").append(toIndentedString(scopes)).append("\n");
        sb.append("    subscribedApplications: ").append(toIndentedString(subscribedApplications)).append("\n");
        sb.append("    properties: ").append(toIndentedString(properties)).append("\n");
        sb.append("    self: ").append(toIndentedString(self)).append("\n");
        sb.append("    authorizationDetailsTypes: ").append(toIndentedString(authorizationDetailsTypes)).append("\n");
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

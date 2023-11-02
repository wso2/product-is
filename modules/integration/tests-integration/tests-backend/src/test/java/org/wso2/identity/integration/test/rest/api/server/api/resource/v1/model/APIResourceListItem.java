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

public class APIResourceListItem {

    private String id;
    private String name;
    private String identifier;
    private String type;
    private Boolean requiresAuthorization;
    private List<Property> properties = null;

    private String self;

    /**
     **/
    public APIResourceListItem id(String id) {

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
    public APIResourceListItem name(String name) {

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
    public APIResourceListItem identifier(String identifier) {

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
    public APIResourceListItem type(String type) {

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
    public APIResourceListItem requiresAuthorization(Boolean requiresAuthorization) {

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
    public APIResourceListItem properties(List<Property> properties) {

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

    public APIResourceListItem addPropertiesItem(Property propertiesItem) {
        if (this.properties == null) {
            this.properties = new ArrayList<Property>();
        }
        this.properties.add(propertiesItem);
        return this;
    }

    /**
     **/
    public APIResourceListItem self(String self) {

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



    @Override
    public boolean equals(java.lang.Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        APIResourceListItem apIResourceListItem = (APIResourceListItem) o;
        return Objects.equals(this.id, apIResourceListItem.id) &&
                Objects.equals(this.name, apIResourceListItem.name) &&
                Objects.equals(this.identifier, apIResourceListItem.identifier) &&
                Objects.equals(this.type, apIResourceListItem.type) &&
                Objects.equals(this.requiresAuthorization, apIResourceListItem.requiresAuthorization) &&
                Objects.equals(this.properties, apIResourceListItem.properties) &&
                Objects.equals(this.self, apIResourceListItem.self);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, identifier, type, requiresAuthorization, properties, self);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class APIResourceListItem {\n");

        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    identifier: ").append(toIndentedString(identifier)).append("\n");
        sb.append("    type: ").append(toIndentedString(type)).append("\n");
        sb.append("    requiresAuthorization: ").append(toIndentedString(requiresAuthorization)).append("\n");
        sb.append("    properties: ").append(toIndentedString(properties)).append("\n");
        sb.append("    self: ").append(toIndentedString(self)).append("\n");
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

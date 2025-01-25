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

public class APIResourcePatchModel {

    private String name;
    private String description;
    private List<ScopeGetModel> addedScopes = null;

    private List<String> removedScopes = null;

    private List<AuthorizationDetailsType> addedAuthorizationDetailsTypes = null;

    private List<String> removedAuthorizationDetailsTypes = null;


    /**
     *
     **/
    public APIResourcePatchModel name(String name) {

        this.name = name;
        return this;
    }

    @ApiModelProperty(example = "Greetings API", value = "")
    @JsonProperty("name")
    @Valid
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     *
     **/
    public APIResourcePatchModel description(String description) {

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
     *
     **/
    public APIResourcePatchModel addedScopes(List<ScopeGetModel> addedScopes) {

        this.addedScopes = addedScopes;
        return this;
    }

    @ApiModelProperty(value = "")
    @JsonProperty("addedScopes")
    @Valid
    public List<ScopeGetModel> getAddedScopes() {
        return addedScopes;
    }

    public void setAddedScopes(List<ScopeGetModel> addedScopes) {
        this.addedScopes = addedScopes;
    }

    public APIResourcePatchModel addAddedScopesItem(ScopeGetModel addedScopesItem) {
        if (this.addedScopes == null) {
            this.addedScopes = new ArrayList<ScopeGetModel>();
        }
        this.addedScopes.add(addedScopesItem);
        return this;
    }

    /**
     * This field is not supported yet.
     **/
    public APIResourcePatchModel removedScopes(List<String> removedScopes) {

        this.removedScopes = removedScopes;
        return this;
    }

    @ApiModelProperty(value = "This field is not supported yet.")
    @JsonProperty("removedScopes")
    @Valid
    public List<String> getRemovedScopes() {
        return removedScopes;
    }

    public void setRemovedScopes(List<String> removedScopes) {
        this.removedScopes = removedScopes;
    }

    public APIResourcePatchModel addRemovedScopesItem(String removedScopesItem) {
        if (this.removedScopes == null) {
            this.removedScopes = new ArrayList<String>();
        }
        this.removedScopes.add(removedScopesItem);
        return this;
    }

    /**
     *
     **/
    public APIResourcePatchModel addedAuthorizationDetailsTypes(List<AuthorizationDetailsType> addedAuthorizationDetailsTypes) {

        this.addedAuthorizationDetailsTypes = addedAuthorizationDetailsTypes;
        return this;
    }

    @ApiModelProperty(value = "")
    @JsonProperty("addedAuthorizationDetailsTypes")
    @Valid
    public List<AuthorizationDetailsType> getAddedAuthorizationDetailsTypes() {
        return addedAuthorizationDetailsTypes;
    }

    public void setAddedAuthorizationDetailsTypes(List<AuthorizationDetailsType> addedAuthorizationDetailsTypes) {
        this.addedAuthorizationDetailsTypes = addedAuthorizationDetailsTypes;
    }

    public APIResourcePatchModel addAddedAuthorizationDetailsTypesItem(AuthorizationDetailsType addedAuthorizationDetailsTypesItem) {
        if (this.addedAuthorizationDetailsTypes == null) {
            this.addedAuthorizationDetailsTypes = new ArrayList<AuthorizationDetailsType>();
        }
        this.addedAuthorizationDetailsTypes.add(addedAuthorizationDetailsTypesItem);
        return this;
    }

    /**
     *
     **/
    public APIResourcePatchModel removedAuthorizationDetailsTypes(List<String> removedAuthorizationDetailsTypes) {

        this.removedAuthorizationDetailsTypes = removedAuthorizationDetailsTypes;
        return this;
    }

    @ApiModelProperty(value = "")
    @JsonProperty("removedAuthorizationDetailsTypes")
    @Valid
    public List<String> getRemovedAuthorizationDetailsTypes() {
        return removedAuthorizationDetailsTypes;
    }

    public void setRemovedAuthorizationDetailsTypes(List<String> removedAuthorizationDetailsTypes) {
        this.removedAuthorizationDetailsTypes = removedAuthorizationDetailsTypes;
    }

    public APIResourcePatchModel addRemovedAuthorizationDetailsTypesItem(String removedAuthorizationDetailsTypesItem) {
        if (this.removedAuthorizationDetailsTypes == null) {
            this.removedAuthorizationDetailsTypes = new ArrayList<String>();
        }
        this.removedAuthorizationDetailsTypes.add(removedAuthorizationDetailsTypesItem);
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
        APIResourcePatchModel apIResourcePatchModel = (APIResourcePatchModel) o;
        return Objects.equals(this.name, apIResourcePatchModel.name) &&
                Objects.equals(this.description, apIResourcePatchModel.description) &&
                Objects.equals(this.addedScopes, apIResourcePatchModel.addedScopes) &&
                Objects.equals(this.removedScopes, apIResourcePatchModel.removedScopes) &&
                Objects.equals(this.addedAuthorizationDetailsTypes, apIResourcePatchModel.addedAuthorizationDetailsTypes) &&
                Objects.equals(this.removedAuthorizationDetailsTypes, apIResourcePatchModel.removedAuthorizationDetailsTypes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, description, addedScopes, removedScopes, addedAuthorizationDetailsTypes, removedAuthorizationDetailsTypes);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class APIResourcePatchModel {\n");

        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    description: ").append(toIndentedString(description)).append("\n");
        sb.append("    addedScopes: ").append(toIndentedString(addedScopes)).append("\n");
        sb.append("    removedScopes: ").append(toIndentedString(removedScopes)).append("\n");
        sb.append("    addedAuthorizationDetailsTypes: ").append(toIndentedString(addedAuthorizationDetailsTypes)).append("\n");
        sb.append("    removedAuthorizationDetailsTypes: ").append(toIndentedString(removedAuthorizationDetailsTypes)).append("\n");
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

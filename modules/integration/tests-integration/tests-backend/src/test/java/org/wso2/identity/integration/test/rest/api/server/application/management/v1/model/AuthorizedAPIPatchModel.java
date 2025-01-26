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

package org.wso2.identity.integration.test.rest.api.server.application.management.v1.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.validation.Valid;

public class AuthorizedAPIPatchModel {

    private List<String> addedScopes = null;

    private List<String> removedScopes = null;

    private List<String> addedAuthorizationDetailsTypes = null;

    private List<String> removedAuthorizationDetailsTypes = null;


    /**
     *
     **/
    public AuthorizedAPIPatchModel addedScopes(List<String> addedScopes) {

        this.addedScopes = addedScopes;
        return this;
    }

    @ApiModelProperty(value = "")
    @JsonProperty("addedScopes")
    @Valid
    public List<String> getAddedScopes() {
        return addedScopes;
    }

    public void setAddedScopes(List<String> addedScopes) {
        this.addedScopes = addedScopes;
    }

    public AuthorizedAPIPatchModel addAddedScopesItem(String addedScopesItem) {
        if (this.addedScopes == null) {
            this.addedScopes = new ArrayList<>();
        }
        this.addedScopes.add(addedScopesItem);
        return this;
    }

    /**
     *
     **/
    public AuthorizedAPIPatchModel removedScopes(List<String> removedScopes) {

        this.removedScopes = removedScopes;
        return this;
    }

    @ApiModelProperty(value = "")
    @JsonProperty("removedScopes")
    @Valid
    public List<String> getRemovedScopes() {
        return removedScopes;
    }

    public void setRemovedScopes(List<String> removedScopes) {
        this.removedScopes = removedScopes;
    }

    public AuthorizedAPIPatchModel addRemovedScopesItem(String removedScopesItem) {
        if (this.removedScopes == null) {
            this.removedScopes = new ArrayList<>();
        }
        this.removedScopes.add(removedScopesItem);
        return this;
    }

    /**
     *
     **/
    public AuthorizedAPIPatchModel addedAuthorizationDetailsTypes(List<String> addedAuthorizationDetailsTypes) {

        this.addedAuthorizationDetailsTypes = addedAuthorizationDetailsTypes;
        return this;
    }

    @ApiModelProperty(value = "")
    @JsonProperty("addedAuthorizationDetailsTypes")
    @Valid
    public List<String> getAddedAuthorizationDetailsTypes() {
        return addedAuthorizationDetailsTypes;
    }

    public void setAddedAuthorizationDetailsTypes(List<String> addedAuthorizationDetailsTypes) {
        this.addedAuthorizationDetailsTypes = addedAuthorizationDetailsTypes;
    }

    public AuthorizedAPIPatchModel addAddedAuthorizationDetailsTypesItem(String addedAuthorizationDetailsTypesItem) {
        if (this.addedAuthorizationDetailsTypes == null) {
            this.addedAuthorizationDetailsTypes = new ArrayList<>();
        }
        this.addedAuthorizationDetailsTypes.add(addedAuthorizationDetailsTypesItem);
        return this;
    }

    /**
     *
     **/
    public AuthorizedAPIPatchModel removedAuthorizationDetailsTypes(List<String> removedAuthorizationDetailsTypes) {

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

    public AuthorizedAPIPatchModel addRemovedAuthorizationDetailsTypesItem(String removedAuthorizationDetailsTypesItem) {
        if (this.removedAuthorizationDetailsTypes == null) {
            this.removedAuthorizationDetailsTypes = new ArrayList<>();
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
        AuthorizedAPIPatchModel authorizedAPIPatchModel = (AuthorizedAPIPatchModel) o;
        return Objects.equals(this.addedScopes, authorizedAPIPatchModel.addedScopes) &&
                Objects.equals(this.removedScopes, authorizedAPIPatchModel.removedScopes) &&
                Objects.equals(this.addedAuthorizationDetailsTypes, authorizedAPIPatchModel.addedAuthorizationDetailsTypes) &&
                Objects.equals(this.removedAuthorizationDetailsTypes, authorizedAPIPatchModel.removedAuthorizationDetailsTypes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(addedScopes, removedScopes, addedAuthorizationDetailsTypes, removedAuthorizationDetailsTypes);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class AuthorizedAPIPatchModel {\n");

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

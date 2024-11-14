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

package org.wso2.identity.integration.test.rest.api.server.application.management.v1.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.validation.Valid;

public class AuthorizedAPICreationModel {

    private String id;
    private String policyIdentifier;
    private List<String> scopes = null;
    private List<String> authorizationDetailsTypes = null;

    /**
     **/
    public AuthorizedAPICreationModel id(String id) {

        this.id = id;
        return this;
    }

    @ApiModelProperty(example = "012df-232gf-545fg-dff23", value = "")
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
    public AuthorizedAPICreationModel policyIdentifier(String policyIdentifier) {

        this.policyIdentifier = policyIdentifier;
        return this;
    }

    @ApiModelProperty(example = "RBAC", value = "")
    @JsonProperty("policyIdentifier")
    @Valid
    public String getPolicyIdentifier() {
        return policyIdentifier;
    }
    public void setPolicyIdentifier(String policyIdentifier) {
        this.policyIdentifier = policyIdentifier;
    }

    /**
     **/
    public AuthorizedAPICreationModel scopes(List<String> scopes) {

        this.scopes = scopes;
        return this;
    }

    @ApiModelProperty(value = "")
    @JsonProperty("scopes")
    @Valid
    public List<String> getScopes() {
        return scopes;
    }
    public void setScopes(List<String> scopes) {
        this.scopes = scopes;
    }

    public AuthorizedAPICreationModel addScopesItem(String scopesItem) {
        if (this.scopes == null) {
            this.scopes = new ArrayList<>();
        }
        this.scopes.add(scopesItem);
        return this;
    }

    public AuthorizedAPICreationModel authorizationDetailsTypes(List<String> authorizationDetailsTypes) {

        this.authorizationDetailsTypes = authorizationDetailsTypes;
        return this;
    }

    @ApiModelProperty(value = "")
    @JsonProperty("authorizationDetailsTypes")
    @Valid
    public List<String> getAuthorizationDetailsTypes() {
        return authorizationDetailsTypes;
    }
    public void setAuthorizationDetailsTypes(List<String> authorizationDetailsTypes) {
        this.authorizationDetailsTypes = authorizationDetailsTypes;
    }

    public AuthorizedAPICreationModel addAuthorizationDetailsTypesItem(String authorizationDetailsTypesItem) {
        if (this.authorizationDetailsTypes == null) {
            this.authorizationDetailsTypes = new ArrayList<>();
        }
        this.authorizationDetailsTypes.add(authorizationDetailsTypesItem);
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
        AuthorizedAPICreationModel authorizedAPICreationModel = (AuthorizedAPICreationModel) o;
        return Objects.equals(this.id, authorizedAPICreationModel.id) &&
                Objects.equals(this.policyIdentifier, authorizedAPICreationModel.policyIdentifier) &&
                Objects.equals(this.scopes, authorizedAPICreationModel.scopes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, policyIdentifier, scopes);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class AuthorizedAPICreationModel {\n");

        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    policyIdentifier: ").append(toIndentedString(policyIdentifier)).append("\n");
        sb.append("    scopes: ").append(toIndentedString(scopes)).append("\n");
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

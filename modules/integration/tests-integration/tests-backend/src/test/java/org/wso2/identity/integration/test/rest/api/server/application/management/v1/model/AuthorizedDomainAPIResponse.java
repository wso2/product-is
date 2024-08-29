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

package org.wso2.identity.integration.test.rest.api.server.application.management.v1.model;

import org.wso2.identity.integration.test.rest.api.server.api.resource.v1.model.ScopeGetModel;

import java.util.List;
import java.util.Objects;

public class AuthorizedDomainAPIResponse {

    private String id;
    private String name;
    private String description;
    private String identifier;
    private String type;
    private boolean requiresAuthorization;
    private List<ScopeGetModel> scopes = null;
    private List<String> properties = null;

    public String getId() {

        return id;
    }

    public void setId(String id) {

        this.id = id;
    }

    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }

    public String getDescription() {

        return description;
    }

    public void setDescription(String description) {

        this.description = description;
    }

    public String getIdentifier() {

        return identifier;
    }

    public void setIdentifier(String identifier) {

        this.identifier = identifier;
    }

    public String getType() {

        return type;
    }

    public void setType(String type) {

        this.type = type;
    }

    public boolean isRequiresAuthorization() {

        return requiresAuthorization;
    }

    public void setRequiresAuthorization(boolean requiresAuthorization) {

        this.requiresAuthorization = requiresAuthorization;
    }

    public List<ScopeGetModel> getScopes() {

        return scopes;
    }

    public void setScopes(List<ScopeGetModel> scopes) {

        this.scopes = scopes;
    }

    public List<String> getProperties() {

        return properties;
    }

    public void setProperties(List<String> properties) {

        this.properties = properties;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AuthorizedDomainAPIResponse that = (AuthorizedDomainAPIResponse) o;
        return requiresAuthorization == that.requiresAuthorization &&
                Objects.equals(id, that.id) &&
                Objects.equals(name, that.name) &&
                Objects.equals(description, that.description) &&
                Objects.equals(identifier, that.identifier) &&
                Objects.equals(type, that.type) &&
                Objects.equals(scopes, that.scopes) &&
                Objects.equals(properties, that.properties);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, name, description, identifier, type, requiresAuthorization, scopes, properties);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class AuthorizedDomainAPIResponse {\n");

        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    description: ").append(toIndentedString(description)).append("\n");
        sb.append("    identifier: ").append(toIndentedString(identifier)).append("\n");
        sb.append("    type: ").append(toIndentedString(type)).append("\n");
        sb.append("    requiresAuthorization: ").append(toIndentedString(requiresAuthorization)).append("\n");
        sb.append("    scopes: ").append(toIndentedString(scopes)).append("\n");
        sb.append("    properties: ").append(toIndentedString(properties)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    private String toIndentedString(Object o) {

        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}

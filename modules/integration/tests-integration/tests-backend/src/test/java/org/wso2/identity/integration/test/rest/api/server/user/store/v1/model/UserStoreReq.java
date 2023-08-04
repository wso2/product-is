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

package org.wso2.identity.integration.test.rest.api.server.user.store.v1.model;


import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;

import java.util.Objects;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * Secondary user store request.
 **/
@ApiModel(description = "Secondary user store request.")
public class UserStoreReq  {

    private String typeId;
    private String description = "";
    private String name;
    private List<Property> properties = new ArrayList<>();
    private List<ClaimAttributeMapping> claimAttributeMappings = null;

    /**
     * The id of the user store manager class type.
     **/
    public UserStoreReq typeId(String typeId) {

        this.typeId = typeId;
        return this;
    }

    @ApiModelProperty(example = "VW5pcXVlSURKREJDVXNlclN0b3JlTWFuYWdlcg", required = true, value = "The id of the user store manager class type.")
    @JsonProperty("typeId")
    @Valid
    @NotNull(message = "Property typeId cannot be null.")
    public String getTypeId() {
        return typeId;
    }
    public void setTypeId(String typeId) {
        this.typeId = typeId;
    }

    /**
     * Description of the user store.
     **/
    public UserStoreReq description(String description) {

        this.description = description;
        return this;
    }

    @ApiModelProperty(example = "Some description about the user store.", required = true, value = "Description of the user store.")
    @JsonProperty("description")
    @Valid
    @NotNull(message = "Property description cannot be null.")
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * This is a unique name that identifies the user store.
     **/
    public UserStoreReq name(String name) {

        this.name = name;
        return this;
    }

    @ApiModelProperty(example = "JDBC-SECONDARY", required = true, value = "This is a unique name that identifies the user store.")
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
     * Various properties related to the user store such as connection URL, connection password etc.
     **/
    public UserStoreReq properties(List<Property> properties) {

        this.properties = properties;
        return this;
    }

    @ApiModelProperty(required = true, value = "Various properties related to the user store such as connection URL, connection password etc.")
    @JsonProperty("properties")
    @Valid
    @NotNull(message = "Property properties cannot be null.")
    public List<Property> getProperties() {
        return properties;
    }
    public void setProperties(List<Property> properties) {
        this.properties = properties;
    }

    public UserStoreReq addPropertiesItem(Property propertiesItem) {
        this.properties.add(propertiesItem);
        return this;
    }

    /**
     * Claim attribute mappings.
     **/
    public UserStoreReq claimAttributeMappings(List<ClaimAttributeMapping> claimAttributeMappings) {

        this.claimAttributeMappings = claimAttributeMappings;
        return this;
    }

    @ApiModelProperty(value = "Claim attribute mappings.")
    @JsonProperty("claimAttributeMappings")
    @Valid
    public List<ClaimAttributeMapping> getClaimAttributeMappings() {
        return claimAttributeMappings;
    }
    public void setClaimAttributeMappings(List<ClaimAttributeMapping> claimAttributeMappings) {
        this.claimAttributeMappings = claimAttributeMappings;
    }

    public UserStoreReq addClaimAttributeMappingsItem(ClaimAttributeMapping claimAttributeMappingsItem) {
        if (this.claimAttributeMappings == null) {
            this.claimAttributeMappings = new ArrayList<>();
        }
        this.claimAttributeMappings.add(claimAttributeMappingsItem);
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
        UserStoreReq userStoreReq = (UserStoreReq) o;
        return Objects.equals(this.typeId, userStoreReq.typeId) &&
                Objects.equals(this.description, userStoreReq.description) &&
                Objects.equals(this.name, userStoreReq.name) &&
                Objects.equals(this.properties, userStoreReq.properties) &&
                Objects.equals(this.claimAttributeMappings, userStoreReq.claimAttributeMappings);
    }

    @Override
    public int hashCode() {
        return Objects.hash(typeId, description, name, properties, claimAttributeMappings);
    }

    @Override
    public String toString() {

        return "class UserStoreReq {\n" +
                "    typeId: " + toIndentedString(typeId) + "\n" +
                "    description: " + toIndentedString(description) + "\n" +
                "    name: " + toIndentedString(name) + "\n" +
                "    properties: " + toIndentedString(properties) + "\n" +
                "    claimAttributeMappings: " + toIndentedString(claimAttributeMappings) + "\n" +
                "}";
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private static String toIndentedString(java.lang.Object o) {

        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n");
    }

    public static class Property  {

        private String name;
        private String value;

        /**
         **/
        public Property name(String name) {

            this.name = name;
            return this;
        }

        @ApiModelProperty(example = "some property name", required = true, value = "")
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
        public Property value(String value) {

            this.value = value;
            return this;
        }

        @ApiModelProperty(example = "some property value", required = true, value = "")
        @JsonProperty("value")
        @Valid
        @NotNull(message = "Property value cannot be null.")
        public String getValue() {
            return value;
        }
        public void setValue(String value) {
            this.value = value;
        }

        @Override
        public boolean equals(java.lang.Object o) {

            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Property property = (Property) o;
            return Objects.equals(this.name, property.name) &&
                    Objects.equals(this.value, property.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, value);
        }

        @Override
        public String toString() {

            return "class Property {\n" +
                    "    name: " + toIndentedString(name) + "\n" +
                    "    value: " + toIndentedString(value) + "\n" +
                    "}";
        }
    }

    public static class ClaimAttributeMapping  {

        private String claimURI;
        private String mappedAttribute;

        /**
         * A unique URI specific to the claim.
         **/
        public ClaimAttributeMapping claimURI(String claimURI) {

            this.claimURI = claimURI;
            return this;
        }

        @ApiModelProperty(example = "http://wso2.org/claims/username", required = true, value = "A unique URI specific to the claim.")
        @JsonProperty("claimURI")
        @Valid
        @NotNull(message = "Property claimURI cannot be null.")
        public String getClaimURI() {
            return claimURI;
        }
        public void setClaimURI(String claimURI) {
            this.claimURI = claimURI;
        }

        /**
         * Userstore attribute to be mapped to.
         **/
        public ClaimAttributeMapping mappedAttribute(String mappedAttribute) {

            this.mappedAttribute = mappedAttribute;
            return this;
        }

        @ApiModelProperty(example = "username", required = true, value = "Userstore attribute to be mapped to.")
        @JsonProperty("mappedAttribute")
        @Valid
        @NotNull(message = "Property mappedAttribute cannot be null.")
        public String getMappedAttribute() {
            return mappedAttribute;
        }
        public void setMappedAttribute(String mappedAttribute) {
            this.mappedAttribute = mappedAttribute;
        }

        @Override
        public boolean equals(java.lang.Object o) {

            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            ClaimAttributeMapping claimAttributeMapping = (ClaimAttributeMapping) o;
            return Objects.equals(this.claimURI, claimAttributeMapping.claimURI) &&
                    Objects.equals(this.mappedAttribute, claimAttributeMapping.mappedAttribute);
        }

        @Override
        public int hashCode() {
            return Objects.hash(claimURI, mappedAttribute);
        }

        @Override
        public String toString() {

            return "class ClaimAttributeMapping {\n" +
                    "    claimURI: " + toIndentedString(claimURI) + "\n" +
                    "    mappedAttribute: " + toIndentedString(mappedAttribute) + "\n" +
                    "}";
        }
    }
}

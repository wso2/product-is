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

package org.wso2.identity.integration.test.rest.api.server.claim.management.v1.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Local claim response.
 **/
public class LocalClaimRes {

    private String id = null;
    private String claimURI = null;
    private String dialectURI = null;
    private String description = null;
    private Integer displayOrder = null;
    private String displayName = null;
    private Boolean readOnly = null;
    private String regEx = null;
    private Boolean required = null;
    private Boolean supportedByDefault = null;

    public enum UniquenessScopeEnum {
        NONE, WITHIN_USERSTORE, ACROSS_USERSTORES,
    }

    private UniquenessScopeEnum uniquenessScope = null;

    public enum SharedProfileValueResolvingMethodEnum {
        FromOrigin, FromSharedProfile, FromFirstFoundInHierarchy,
    }

    private SharedProfileValueResolvingMethodEnum sharedProfileValueResolvingMethod = null;

    private List<AttributeMappingDTO> attributeMapping = new ArrayList<AttributeMappingDTO>();

    private List<PropertyDTO> properties = new ArrayList<PropertyDTO>();

    private HashMap<String, AttributeProfileDTO> profiles = null;

    /**
     * claim ID.
     **/
    @ApiModelProperty(value = "claim ID.")
    @JsonProperty("id")
    public String getId() {

        return id;
    }

    public void setId(String id) {

        this.id = id;
    }

    /**
     * A unique URI specific to the claim.
     **/
    @ApiModelProperty(value = "A unique URI specific to the claim.")
    @JsonProperty("claimURI")
    public String getClaimURI() {

        return claimURI;
    }

    public void setClaimURI(String claimURI) {

        this.claimURI = claimURI;
    }

    /**
     * URI of the claim dialect.
     **/
    @ApiModelProperty(value = "URI of the claim dialect.")
    @JsonProperty("dialectURI")
    public String getDialectURI() {

        return dialectURI;
    }

    public void setDialectURI(String dialectURI) {

        this.dialectURI = dialectURI;
    }

    /**
     * Description of the claim.
     **/
    @ApiModelProperty(value = "Description of the claim.")
    @JsonProperty("description")
    public String getDescription() {

        return description;
    }

    public void setDescription(String description) {

        this.description = description;
    }

    /**
     * The order in which the claim is displayed among other claims under the same dialect.
     **/
    @ApiModelProperty(value = "The order in which the claim is displayed among other claims under the same dialect.")
    @JsonProperty("displayOrder")
    public Integer getDisplayOrder() {

        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {

        this.displayOrder = displayOrder;
    }

    /**
     * Name of the claim to be displayed in the UI.
     **/
    @ApiModelProperty(value = "Name of the claim to be displayed in the UI.")
    @JsonProperty("displayName")
    public String getDisplayName() {

        return displayName;
    }

    public void setDisplayName(String displayName) {

        this.displayName = displayName;
    }

    /**
     * Specifies if the claim is read-only.
     **/
    @ApiModelProperty(value = "Specifies if the claim is read-only.")
    @JsonProperty("readOnly")
    public Boolean getReadOnly() {

        return readOnly;
    }

    public void setReadOnly(Boolean readOnly) {

        this.readOnly = readOnly;
    }

    /**
     * Regular expression used to validate inputs.
     **/
    @ApiModelProperty(value = "Regular expression used to validate inputs.")
    @JsonProperty("regEx")
    public String getRegEx() {

        return regEx;
    }

    public void setRegEx(String regEx) {

        this.regEx = regEx;
    }

    /**
     * Specifies if the claim is required for user registration.
     **/
    @ApiModelProperty(value = "Specifies if the claim is required for user registration.")
    @JsonProperty("required")
    public Boolean getRequired() {

        return required;
    }

    public void setRequired(Boolean required) {

        this.required = required;
    }

    /**
     * Specifies if the claim will be prompted during user registration and displayed on the user profile.
     **/
    @ApiModelProperty(value = "Specifies if the claim will be prompted during user registration and displayed on the user profile.")
    @JsonProperty("supportedByDefault")
    public Boolean getSupportedByDefault() {

        return supportedByDefault;
    }

    public void setSupportedByDefault(Boolean supportedByDefault) {

        this.supportedByDefault = supportedByDefault;
    }

    /**
     * Specifies the scope of uniqueness validation for the claim value.
     **/
    @ApiModelProperty(value = "Specifies the scope of uniqueness validation for the claim value.")
    @JsonProperty("uniquenessScope")
    public UniquenessScopeEnum getUniquenessScope() {

        return uniquenessScope;
    }

    public void setUniquenessScope(UniquenessScopeEnum uniquenessScope) {

        this.uniquenessScope = uniquenessScope;
    }

    /**
     * Specifies claim value resolving method for shared user profile.
     **/
    @ApiModelProperty(value = "Specifies claim value resolving method for shared user profile.")
    @JsonProperty("sharedProfileValueResolvingMethod")
    public SharedProfileValueResolvingMethodEnum getSharedProfileValueResolvingMethod() {

        return sharedProfileValueResolvingMethod;
    }

    public void setSharedProfileValueResolvingMethod(
            SharedProfileValueResolvingMethodEnum sharedProfileValueResolvingMethod) {

        this.sharedProfileValueResolvingMethod = sharedProfileValueResolvingMethod;
    }

    /**
     * Userstore attribute mappings.
     **/
    @ApiModelProperty(value = "Userstore attribute mappings.")
    @JsonProperty("attributeMapping")
    public List<AttributeMappingDTO> getAttributeMapping() {

        return attributeMapping;
    }

    public void setAttributeMapping(List<AttributeMappingDTO> attributeMapping) {

        this.attributeMapping = attributeMapping;
    }

    /**
     * Define any additional properties if required.
     **/
    @ApiModelProperty(value = "Define any additional properties if required.")
    @JsonProperty("properties")
    public List<PropertyDTO> getProperties() {

        return properties;
    }

    public void setProperties(List<PropertyDTO> properties) {

        this.properties = properties;
    }

    /**
     *
     **/
    @ApiModelProperty(value = "Define any attribute profiles.")
    @JsonProperty("profiles")
    public HashMap<String, AttributeProfileDTO> getProfiles() {

        return profiles;
    }

    public void setProfiles(HashMap<String, AttributeProfileDTO> profiles) {

        this.profiles = profiles;
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class LocalClaimResDTO {\n");
        sb.append("  " + super.toString()).append("\n");

        sb.append("    id: ").append(id).append("\n");
        sb.append("    claimURI: ").append(claimURI).append("\n");
        sb.append("    dialectURI: ").append(dialectURI).append("\n");
        sb.append("    description: ").append(description).append("\n");
        sb.append("    displayOrder: ").append(displayOrder).append("\n");
        sb.append("    displayName: ").append(displayName).append("\n");
        sb.append("    readOnly: ").append(readOnly).append("\n");
        sb.append("    regEx: ").append(regEx).append("\n");
        sb.append("    required: ").append(required).append("\n");
        sb.append("    supportedByDefault: ").append(supportedByDefault).append("\n");
        sb.append("    uniquenessScope: ").append(uniquenessScope).append("\n");
        sb.append("    sharedProfileValueResolvingMethod: ").append(sharedProfileValueResolvingMethod).append("\n");
        sb.append("    attributeMapping: ").append(attributeMapping).append("\n");
        sb.append("    properties: ").append(properties).append("\n");
        sb.append("    profiles: ").append(profiles).append("\n");

        sb.append("}\n");
        return sb.toString();
    }
}

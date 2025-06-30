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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.validation.Valid;

public class LocalClaimReq {

    private String claimURI = null;
    private String description = null;
    private Integer displayOrder = null;
    private String displayName = null;
    private Boolean readOnly = null;
    private String regEx = null;
    private Boolean required = null;
    private Boolean supportedByDefault = null;
    private String dataType = null;
    private String[] subAttributes = null;
    private LabelValueDTO[] canonicalValues = null;
    private HashMap<String, String> inputFormat = null;

    public enum UniquenessScopeEnum {
        NONE,  WITHIN_USERSTORE,  ACROSS_USERSTORES,
    };

    private UniquenessScopeEnum uniquenessScope = null;

    public enum SharedProfileValueResolvingMethodEnum {
        FromOrigin, FromSharedProfile, FromFirstFoundInHierarchy,
    };

    private SharedProfileValueResolvingMethodEnum sharedProfileValueResolvingMethod = null;

    private List<AttributeMappingDTO> attributeMapping = new ArrayList<AttributeMappingDTO>();

    private List<PropertyDTO> properties = new ArrayList<PropertyDTO>();

    private HashMap<String, AttributeProfileDTO> profiles = null;

    /**
     * A unique URI specific to the claim.
     **/
    @ApiModelProperty(required = true, value = "A unique URI specific to the claim.")
    @JsonProperty("claimURI")
    @Valid
    public String getClaimURI() {

        return claimURI;
    }

    public void setClaimURI(String claimURI) {

        this.claimURI = claimURI;
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
    @ApiModelProperty(required = true, value = "Name of the claim to be displayed in the UI.")
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
    @ApiModelProperty(required = true, value = "Userstore attribute mappings.")
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
     **/
    @ApiModelProperty(value = "Define any attribute profiles.")
    @JsonProperty("profiles")
    public HashMap<String, AttributeProfileDTO> getProfiles() {

        return profiles;
    }

    public void setProfiles(HashMap<String, AttributeProfileDTO> profiles) {

        this.profiles = profiles;
    }

    /**
     * Specifies the type of data which the claim holds.
     **/
    @ApiModelProperty(value = "Specifies the type of data stored in the corresponding claim value.")
    @JsonProperty("dataType")
    public String getDataType() {

        return dataType;
    }
    public void setDataType(String dataType) {

        this.dataType = dataType;
    }

    /**
     * The sub attributes of the complex attribute.
     **/
    @ApiModelProperty(value = "The sub attributes of the complex attribute.")
    @JsonProperty("subAttributes")
    public String[] getSubAttributes() {

        return subAttributes != null ? subAttributes.clone() : new String[0];
    }
    public void setSubAttributes(String[] subAttributes) {

        this.subAttributes = subAttributes != null ? subAttributes.clone() : null;
    }

    /**
     * The possible values for the attribute.
     **/
    @ApiModelProperty(value = "The possible values for the attribute.")
    @JsonProperty("canonicalValues")
    public LabelValueDTO[] getCanonicalValues() {

        return canonicalValues != null ? canonicalValues.clone() : new LabelValueDTO[0];
    }
    public void setCanonicalValues(LabelValueDTO[] canonicalValues) {

        this.canonicalValues = canonicalValues != null ? canonicalValues.clone() : null;
    }

    /**
     *
     **/
    @ApiModelProperty(value = "Input format for the claim.")
    @JsonProperty("inputFormat")
    public HashMap<String, String> getInputFormat() {

        return inputFormat;
    }

    public void setInputFormat(HashMap<String, String> inputFormat) {

        this.inputFormat = inputFormat;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LocalClaimReq localClaimReq = (LocalClaimReq) o;
        return Objects.equals(this.claimURI, localClaimReq.claimURI) &&
                Objects.equals(this.description, localClaimReq.description) &&
                Objects.equals(this.displayOrder, localClaimReq.displayOrder) &&
                Objects.equals(this.displayName, localClaimReq.displayName) &&
                Objects.equals(this.readOnly, localClaimReq.readOnly) &&
                Objects.equals(this.regEx, localClaimReq.regEx) &&
                Objects.equals(this.required, localClaimReq.required) &&
                Objects.equals(this.supportedByDefault, localClaimReq.supportedByDefault) &&
                Objects.equals(this.dataType, localClaimReq.dataType) &&
                Arrays.equals(this.subAttributes, localClaimReq.subAttributes) &&
                Arrays.equals(this.canonicalValues, localClaimReq.canonicalValues) &&
                Objects.equals(this.uniquenessScope, localClaimReq.uniquenessScope) &&
                Objects.equals(this.sharedProfileValueResolvingMethod, localClaimReq.sharedProfileValueResolvingMethod) &&
                Objects.equals(this.attributeMapping, localClaimReq.attributeMapping) &&
                Objects.equals(this.properties, localClaimReq.properties) &&
                Objects.equals(this.profiles, localClaimReq.profiles);
    }

    @Override
    public int hashCode() {

        return Objects.hash(claimURI, description, displayOrder, displayName, readOnly, regEx, required,
                supportedByDefault, uniquenessScope, sharedProfileValueResolvingMethod, attributeMapping, properties,
                profiles, dataType, subAttributes, canonicalValues);
    }

    @Override
    public String toString() {

        return "class LocalClaimReqDTO {\n" +
                "    claimURI: " + toIndentedString(claimURI) + "\n" +
                "    description: " + toIndentedString(description) + "\n" +
                "    displayOrder: " + toIndentedString(displayOrder) + "\n" +
                "    displayName: " + toIndentedString(displayName) + "\n" +
                "    readOnly: " + toIndentedString(readOnly) + "\n" +
                "    regEx: " + toIndentedString(regEx) + "\n" +
                "    required: " + toIndentedString(required) + "\n" +
                "    supportedByDefault: " + toIndentedString(supportedByDefault) + "\n" +
                "    dataType: " + toIndentedString(dataType) + "\n" +
                "    subAttributes: " + toIndentedString(subAttributes) + "\n" +
                "    canonicalValues: " + toIndentedString(canonicalValues) + "\n" +
                "    uniquenessScope: " + toIndentedString(uniquenessScope) + "\n" +
                "    sharedProfileValueResolvingMethod: " + toIndentedString(sharedProfileValueResolvingMethod) + "\n" +
                "    attributeMapping: " + toIndentedString(attributeMapping) + "\n" +
                "    properties: " + toIndentedString(properties) + "\n" +
                "    profiles: " + toIndentedString(profiles) + "\n" +
                "}\n";
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

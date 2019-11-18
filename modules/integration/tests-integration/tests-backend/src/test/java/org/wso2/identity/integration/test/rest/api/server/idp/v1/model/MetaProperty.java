/*
* Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.wso2.identity.integration.test.rest.api.server.idp.v1.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

public class MetaProperty {
  
    private String key;
    private String displayName;
    private String description;

@XmlType(name="TypeEnum")
@XmlEnum(String.class)
public enum TypeEnum {

    @XmlEnumValue("STRING") STRING(String.valueOf("STRING")), @XmlEnumValue("BOOLEAN") BOOLEAN(String.valueOf("BOOLEAN")), @XmlEnumValue("INTEGER") INTEGER(String.valueOf("INTEGER"));


    private String value;

    TypeEnum(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    public static TypeEnum fromValue(String value) {
        for (TypeEnum b : TypeEnum.values()) {
            if (b.value.equals(value)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
}

    private TypeEnum type;
    private Integer displayOrder;
    private String regex;
    private Boolean isMandatory = false;
    private Boolean isConfidential = false;
    private List<String> options = null;

    private String defaultValue;
    private List<MetaProperty> subProperties = null;


    /**
    **/
    public MetaProperty key(String key) {

        this.key = key;
        return this;
    }
    
    @ApiModelProperty(example = "httpBinding", required = true, value = "")
    @JsonProperty("key")
    @Valid
    @NotNull(message = "Property key cannot be null.")

    public String getKey() {
        return key;
    }
    public void setKey(String key) {
        this.key = key;
    }

    /**
    **/
    public MetaProperty displayName(String displayName) {

        this.displayName = displayName;
        return this;
    }
    
    @ApiModelProperty(example = "HTTP Binding", value = "")
    @JsonProperty("displayName")
    @Valid
    public String getDisplayName() {
        return displayName;
    }
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
    **/
    public MetaProperty description(String description) {

        this.description = description;
        return this;
    }
    
    @ApiModelProperty(example = "Choose the HTTP Binding or decide from incoming request", value = "")
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
    public MetaProperty type(TypeEnum type) {

        this.type = type;
        return this;
    }
    
    @ApiModelProperty(example = "STRING", value = "")
    @JsonProperty("type")
    @Valid
    public TypeEnum getType() {
        return type;
    }
    public void setType(TypeEnum type) {
        this.type = type;
    }

    /**
    **/
    public MetaProperty displayOrder(Integer displayOrder) {

        this.displayOrder = displayOrder;
        return this;
    }
    
    @ApiModelProperty(example = "10", value = "")
    @JsonProperty("displayOrder")
    @Valid
    public Integer getDisplayOrder() {
        return displayOrder;
    }
    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    /**
    **/
    public MetaProperty regex(String regex) {

        this.regex = regex;
        return this;
    }
    
    @ApiModelProperty(example = "[a-zA-Z]{3,30}", value = "")
    @JsonProperty("regex")
    @Valid
    public String getRegex() {
        return regex;
    }
    public void setRegex(String regex) {
        this.regex = regex;
    }

    /**
    **/
    public MetaProperty isMandatory(Boolean isMandatory) {

        this.isMandatory = isMandatory;
        return this;
    }
    
    @ApiModelProperty(example = "false", value = "")
    @JsonProperty("isMandatory")
    @Valid
    public Boolean getIsMandatory() {
        return isMandatory;
    }
    public void setIsMandatory(Boolean isMandatory) {
        this.isMandatory = isMandatory;
    }

    /**
    **/
    public MetaProperty isConfidential(Boolean isConfidential) {

        this.isConfidential = isConfidential;
        return this;
    }
    
    @ApiModelProperty(value = "")
    @JsonProperty("isConfidential")
    @Valid
    public Boolean getIsConfidential() {
        return isConfidential;
    }
    public void setIsConfidential(Boolean isConfidential) {
        this.isConfidential = isConfidential;
    }

    /**
    **/
    public MetaProperty options(List<String> options) {

        this.options = options;
        return this;
    }
    
    @ApiModelProperty(example = "[\"HTTP-Redirect\",\"HTTP-POST\",\"As Per Request\"]", value = "")
    @JsonProperty("options")
    @Valid
    public List<String> getOptions() {
        return options;
    }
    public void setOptions(List<String> options) {
        this.options = options;
    }

    public MetaProperty addOptionsItem(String optionsItem) {
        if (this.options == null) {
            this.options = new ArrayList<>();
        }
        this.options.add(optionsItem);
        return this;
    }

        /**
    **/
    public MetaProperty defaultValue(String defaultValue) {

        this.defaultValue = defaultValue;
        return this;
    }
    
    @ApiModelProperty(example = "HTTP-Redirect", value = "")
    @JsonProperty("defaultValue")
    @Valid
    public String getDefaultValue() {
        return defaultValue;
    }
    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    /**
    **/
    public MetaProperty subProperties(List<MetaProperty> subProperties) {

        this.subProperties = subProperties;
        return this;
    }
    
    @ApiModelProperty(value = "")
    @JsonProperty("subProperties")
    @Valid
    public List<MetaProperty> getSubProperties() {
        return subProperties;
    }
    public void setSubProperties(List<MetaProperty> subProperties) {
        this.subProperties = subProperties;
    }

    public MetaProperty addSubPropertiesItem(MetaProperty subPropertiesItem) {
        if (this.subProperties == null) {
            this.subProperties = new ArrayList<>();
        }
        this.subProperties.add(subPropertiesItem);
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
        MetaProperty metaProperty = (MetaProperty) o;
        return Objects.equals(this.key, metaProperty.key) &&
            Objects.equals(this.displayName, metaProperty.displayName) &&
            Objects.equals(this.description, metaProperty.description) &&
            Objects.equals(this.type, metaProperty.type) &&
            Objects.equals(this.displayOrder, metaProperty.displayOrder) &&
            Objects.equals(this.regex, metaProperty.regex) &&
            Objects.equals(this.isMandatory, metaProperty.isMandatory) &&
            Objects.equals(this.isConfidential, metaProperty.isConfidential) &&
            Objects.equals(this.options, metaProperty.options) &&
            Objects.equals(this.defaultValue, metaProperty.defaultValue) &&
            Objects.equals(this.subProperties, metaProperty.subProperties);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, displayName, description, type, displayOrder, regex, isMandatory, isConfidential, options, defaultValue, subProperties);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class MetaProperty {\n");

        sb.append("    key: ").append(toIndentedString(key)).append("\n");
        sb.append("    displayName: ").append(toIndentedString(displayName)).append("\n");
        sb.append("    description: ").append(toIndentedString(description)).append("\n");
        sb.append("    type: ").append(toIndentedString(type)).append("\n");
        sb.append("    displayOrder: ").append(toIndentedString(displayOrder)).append("\n");
        sb.append("    regex: ").append(toIndentedString(regex)).append("\n");
        sb.append("    isMandatory: ").append(toIndentedString(isMandatory)).append("\n");
        sb.append("    isConfidential: ").append(toIndentedString(isConfidential)).append("\n");
        sb.append("    options: ").append(toIndentedString(options)).append("\n");
        sb.append("    defaultValue: ").append(toIndentedString(defaultValue)).append("\n");
        sb.append("    subProperties: ").append(toIndentedString(subProperties)).append("\n");
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


/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.identity.integration.test.rest.api.server.identity.governance.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.validation.Valid;

/**
 * Governance connector property values.
 **/

@ApiModel(description = "Governance connector property values.")
public class PreferenceResp {
  
    private String connectorName;
    private List<PropertyReq> properties = null;


    /**
    **/
    public PreferenceResp connectorName(String connectorName) {

        this.connectorName = connectorName;
        return this;
    }
    
    @ApiModelProperty(value = "")
    @JsonProperty("connector-name")
    @Valid
    public String getConnectorName() {
        return connectorName;
    }
    public void setConnectorName(String connectorName) {
        this.connectorName = connectorName;
    }

    /**
    **/
    public PreferenceResp properties(List<PropertyReq> properties) {

        this.properties = properties;
        return this;
    }
    
    @ApiModelProperty(value = "")
    @JsonProperty("properties")
    @Valid
    public List<PropertyReq> getProperties() {
        return properties;
    }
    public void setProperties(List<PropertyReq> properties) {
        this.properties = properties;
    }

    public PreferenceResp addPropertiesItem(PropertyReq propertiesItem) {
        if (this.properties == null) {
            this.properties = new ArrayList<>();
        }
        this.properties.add(propertiesItem);
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
        PreferenceResp preferenceResp = (PreferenceResp) o;
        return Objects.equals(this.connectorName, preferenceResp.connectorName) &&
            Objects.equals(this.properties, preferenceResp.properties);
    }

    @Override
    public int hashCode() {
        return Objects.hash(connectorName, properties);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class PreferenceResp {\n");

        sb.append("    connectorName: ").append(toIndentedString(connectorName)).append("\n");
        sb.append("    properties: ").append(toIndentedString(properties)).append("\n");
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


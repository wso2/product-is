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

package org.wso2.identity.integration.test.rest.api.server.identity.governance.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.*;

import java.util.Objects;

import javax.validation.Valid;

@ApiModel(description = "Governance connector properties to delete.")
public class PropertyRevertReq {

    private List<String> properties = new ArrayList<>();

    public PropertyRevertReq properties(List<String> properties) {

        this.properties = properties;
        return this;
    }

    @ApiModelProperty(required = true, value = "")
    @JsonProperty("properties")
    @Valid
    @NotNull(message = "Property properties cannot be null.")
    @Size(min = 1)
    public List<String> getProperties() {

        return properties;
    }

    public void setProperties(List<String> properties) {

        this.properties = properties;
    }

    public PropertyRevertReq addPropertiesItem(String propertiesItem) {

        this.properties.add(propertiesItem);
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

        PropertyRevertReq propertyRevertReq = (PropertyRevertReq) o;
        return Objects.equals(this.properties, propertyRevertReq.properties);
    }

    @Override
    public int hashCode() {

        return Objects.hash(properties);
    }

    @Override
    public String toString() {

        return "class PropertyRevertReq {\n" +
                "    properties: " + toIndentedString(properties) + "\n" +
                "}";
    }

    private String toIndentedString(java.lang.Object o) {

        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n");
    }
}


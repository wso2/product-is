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

package org.wso2.identity.integration.test.rest.api.server.input.validation.v1.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.validation.constraints.*;
import javax.validation.Valid;

public class RuleModel {

    private String validator;
    private List<MappingModel> properties = new ArrayList<>();

    public RuleModel validator(String validator) {

        this.validator = validator;
        return this;
    }

    @ApiModelProperty(example = "lengthValidator", required = true, value = "")
    @JsonProperty("validator")
    @Valid
    @NotNull(message = "Property validator cannot be null.")

    public String getValidator() {

        return validator;
    }

    public void setValidator(String validator) {

        this.validator = validator;
    }

    public RuleModel properties(List<MappingModel> properties) {

        this.properties = properties;
        return this;
    }

    @ApiModelProperty(required = true, value = "")
    @JsonProperty("properties")
    @Valid
    @NotNull(message = "Property properties cannot be null.")

    public List<MappingModel> getProperties() {

        return properties;
    }

    public void setProperties(List<MappingModel> properties) {

        this.properties = properties;
    }

    public RuleModel addPropertiesItem(MappingModel propertiesItem) {

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

        RuleModel ruleModel = (RuleModel) o;
        return Objects.equals(this.validator, ruleModel.validator) &&
                Objects.equals(this.properties, ruleModel.properties);
    }

    @Override
    public int hashCode() {

        return Objects.hash(validator, properties);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class RuleModel {\n");

        sb.append("    validator: ").append(toIndentedString(validator)).append("\n");
        sb.append("    properties: ").append(toIndentedString(properties)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    private String toIndentedString(java.lang.Object o) {

        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n");
    }
}

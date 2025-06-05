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
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@ApiModel(description = "")
public class LabelValueDTO {

    @Valid 
    @NotNull(message = "Label cannot be null.")
    private String label = null;

    @Valid 
    @NotNull(message = "Value cannot be null.")
    private String value = null;

    /**
    **/
    @ApiModelProperty(required = true, value = "")
    @JsonProperty("label")
    public String getLabel() {
        return label;
    }
    public void setLabel(String label) {
        this.label = label;
    }

    /**
    **/
    @ApiModelProperty(required = true, value = "")
    @JsonProperty("value")
    public String getValue() {
        return value;
    }
    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class LabelValuePairDTO {\n");
        
        sb.append("    label: ").append(label).append("\n");
        sb.append("    value: ").append(value).append("\n");
        
        sb.append("}\n");
        return sb.toString();
    }
}

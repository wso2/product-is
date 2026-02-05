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

package org.wso2.identity.integration.test.rest.api.server.flow.execution.v1.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.validation.constraints.*;

/**
 * Flow configurations for a flow type
 **/

import io.swagger.annotations.*;
import java.util.Objects;
import javax.validation.Valid;
import javax.xml.bind.annotation.*;
@ApiModel(description = "Flow configurations for a flow type")
public class FlowConfig  {

    private String flowType;
    private Boolean isEnabled;
    private Map<String, String> flowCompletionConfigs = null;


    /**
     * Flow type
     **/
    public FlowConfig flowType(String flowType) {

        this.flowType = flowType;
        return this;
    }

    @ApiModelProperty(example = "REGISTRATION", value = "Flow type")
    @JsonProperty("flowType")
    @Valid
    public String getFlowType() {
        return flowType;
    }
    public void setFlowType(String flowType) {
        this.flowType = flowType;
    }

    /**
     * Indicate whether the orchestration is enabled for the flow
     **/
    public FlowConfig isEnabled(Boolean isEnabled) {

        this.isEnabled = isEnabled;
        return this;
    }

    @ApiModelProperty(example = "true", value = "Indicate whether the orchestration is enabled for the flow")
    @JsonProperty("isEnabled")
    @Valid
    public Boolean getIsEnabled() {
        return isEnabled;
    }
    public void setIsEnabled(Boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

    /**
     * Flow Completion Configs.
     **/
    public FlowConfig flowCompletionConfigs(Map<String, String> flowCompletionConfigs) {

        this.flowCompletionConfigs = flowCompletionConfigs;
        return this;
    }

    @ApiModelProperty(value = "Flow Completion Configs.")
    @JsonProperty("flowCompletionConfigs")
    @Valid
    public Map<String, String> getFlowCompletionConfigs() {
        return flowCompletionConfigs;
    }
    public void setFlowCompletionConfigs(Map<String, String> flowCompletionConfigs) {
        this.flowCompletionConfigs = flowCompletionConfigs;
    }


    public FlowConfig putFlowCompletionConfigsItem(String key, String flowCompletionConfigsItem) {
        if (this.flowCompletionConfigs == null) {
            this.flowCompletionConfigs = new HashMap<String, String>();
        }
        this.flowCompletionConfigs.put(key, flowCompletionConfigsItem);
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
        FlowConfig flowConfig = (FlowConfig) o;
        return Objects.equals(this.flowType, flowConfig.flowType) &&
                Objects.equals(this.isEnabled, flowConfig.isEnabled) &&
                Objects.equals(this.flowCompletionConfigs, flowConfig.flowCompletionConfigs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(flowType, isEnabled, flowCompletionConfigs);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class FlowConfig {\n");

        sb.append("    flowType: ").append(toIndentedString(flowType)).append("\n");
        sb.append("    isEnabled: ").append(toIndentedString(isEnabled)).append("\n");
        sb.append("    flowCompletionConfigs: ").append(toIndentedString(flowCompletionConfigs)).append("\n");
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


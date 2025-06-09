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
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;
import javax.validation.Valid;
import javax.xml.bind.annotation.*;

public class FlowExecutionRequest  {

    private String flowType;
    private String applicationId;
    private String flowId;
    private String actionId;
    private Object inputs;

    /**
     * Unique identifier to identify the flow type
     **/
    public FlowExecutionRequest flowType(String flowType) {

        this.flowType = flowType;
        return this;
    }

    @ApiModelProperty(example = "REGISTRATION", value = "Unique identifier to identify the flow type")
    @JsonProperty("flowType")
    @Valid
    public String getFlowType() {
        return flowType;
    }
    public void setFlowType(String flowType) {
        this.flowType = flowType;
    }

    /**
     * Unique identifier for the application
     **/
    public FlowExecutionRequest applicationId(String applicationId) {

        this.applicationId = applicationId;
        return this;
    }

    @ApiModelProperty(example = "01afc2d2-f7b8-46db-95a9-c17336e7a1c6", value = "Unique identifier for the application")
    @JsonProperty("applicationId")
    @Valid
    public String getApplicationId() {
        return applicationId;
    }
    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    /**
     * Unique identifier for the flow execution
     **/
    public FlowExecutionRequest flowId(String flowId) {

        this.flowId = flowId;
        return this;
    }

    @ApiModelProperty(example = "3fa85f64-5717-4562-b3fc-2c963f66afa6", value = "Unique identifier for the flow execution")
    @JsonProperty("flowId")
    @Valid
    public String getFlowId() {
        return flowId;
    }
    public void setFlowId(String flowId) {
        this.flowId = flowId;
    }

    /**
     * Action identifier
     **/
    public FlowExecutionRequest actionId(String actionId) {

        this.actionId = actionId;
        return this;
    }

    @ApiModelProperty(example = "dnd-component-01afc2d2-f7b8-46db-95a9-c17336e7a1c6", value = "Action identifier")
    @JsonProperty("actionId")
    @Valid
    public String getActionId() {
        return actionId;
    }
    public void setActionId(String actionId) {
        this.actionId = actionId;
    }

    /**
     * Input values for the execution step
     **/
    public FlowExecutionRequest inputs(Object inputs) {

        this.inputs = inputs;
        return this;
    }

    @ApiModelProperty(example = "{\"username\":\"johnw\"}", value = "Input values for the execution step")
    @JsonProperty("inputs")
    @Valid
    public Object getInputs() {
        return inputs;
    }
    public void setInputs(Object inputs) {
        this.inputs = inputs;
    }



    @Override
    public boolean equals(java.lang.Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FlowExecutionRequest flowExecutionRequest = (FlowExecutionRequest) o;
        return Objects.equals(this.flowType, flowExecutionRequest.flowType) &&
                Objects.equals(this.applicationId, flowExecutionRequest.applicationId) &&
                Objects.equals(this.flowId, flowExecutionRequest.flowId) &&
                Objects.equals(this.actionId, flowExecutionRequest.actionId) &&
                Objects.equals(this.inputs, flowExecutionRequest.inputs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(flowType, applicationId, flowId, actionId, inputs);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class FlowExecutionRequest {\n");

        sb.append("    flowType: ").append(toIndentedString(flowType)).append("\n");
        sb.append("    applicationId: ").append(toIndentedString(applicationId)).append("\n");
        sb.append("    flowId: ").append(toIndentedString(flowId)).append("\n");
        sb.append("    actionId: ").append(toIndentedString(actionId)).append("\n");
        sb.append("    inputs: ").append(toIndentedString(inputs)).append("\n");
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

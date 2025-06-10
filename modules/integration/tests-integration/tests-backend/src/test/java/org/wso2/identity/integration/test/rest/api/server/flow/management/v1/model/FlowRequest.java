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

package org.wso2.identity.integration.test.rest.api.server.flow.management.v1.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@ApiModel(description = "Request payload for creating or updating a flow")
public class FlowRequest {

    private String flowType;
    private List<Step> steps = new ArrayList<Step>();


    /**
     * Type of the flow being updated
     **/
    public FlowRequest flowType(String flowType) {

        this.flowType = flowType;
        return this;
    }

    @ApiModelProperty(example = "SELF_REGISTRATION", required = true, value = "Type of the flow being updated")
    @JsonProperty("flowType")
    @Valid
    @NotNull(message = "Property flowType cannot be null.")

    public String getFlowType() {
        return flowType;
    }

    public void setFlowType(String flowType) {
        this.flowType = flowType;
    }

    /**
     *
     **/
    public FlowRequest steps(List<Step> steps) {

        this.steps = steps;
        return this;
    }

    @ApiModelProperty(required = true, value = "")
    @JsonProperty("steps")
    @Valid
    @NotNull(message = "Property steps cannot be null.")

    public List<Step> getSteps() {
        return steps;
    }

    public void setSteps(List<Step> steps) {
        this.steps = steps;
    }

    public FlowRequest addStepsItem(Step stepsItem) {
        this.steps.add(stepsItem);
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
        FlowRequest flowRequest = (FlowRequest) o;
        return Objects.equals(this.flowType, flowRequest.flowType) &&
                Objects.equals(this.steps, flowRequest.steps);
    }

    @Override
    public int hashCode() {
        return Objects.hash(flowType, steps);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class FlowRequest {\n");

        sb.append("    flowType: ").append(toIndentedString(flowType)).append("\n");
        sb.append("    steps: ").append(toIndentedString(steps)).append("\n");
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

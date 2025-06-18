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

package org.wso2.identity.integration.test.rest.api.server.approval.workflow.v1.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;
import javax.validation.Valid;
import javax.xml.bind.annotation.*;

public class WorkflowTemplateBase  {

    private String name;
    private List<WorkflowTemplateParametersBase> steps = null;


    /**
     * Name of the workflow template
     **/
    public WorkflowTemplateBase name(String name) {

        this.name = name;
        return this;
    }

    @ApiModelProperty(example = "MultiStepApproval", value = "Name of the workflow template")
    @JsonProperty("name")
    @Valid
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    /**
     **/
    public WorkflowTemplateBase steps(List<WorkflowTemplateParametersBase> steps) {

        this.steps = steps;
        return this;
    }

    @ApiModelProperty(value = "")
    @JsonProperty("steps")
    @Valid @Size(min=1)
    public List<WorkflowTemplateParametersBase> getSteps() {
        return steps;
    }
    public void setSteps(List<WorkflowTemplateParametersBase> steps) {
        this.steps = steps;
    }

    public WorkflowTemplateBase addStepsItem(WorkflowTemplateParametersBase stepsItem) {
        if (this.steps == null) {
            this.steps = new ArrayList<>();
        }
        this.steps.add(stepsItem);
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
        WorkflowTemplateBase workflowTemplateBase = (WorkflowTemplateBase) o;
        return Objects.equals(this.name, workflowTemplateBase.name) &&
                Objects.equals(this.steps, workflowTemplateBase.steps);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, steps);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class WorkflowTemplateBase {\n");

        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    steps: ").append(toIndentedString(steps)).append("\n");
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



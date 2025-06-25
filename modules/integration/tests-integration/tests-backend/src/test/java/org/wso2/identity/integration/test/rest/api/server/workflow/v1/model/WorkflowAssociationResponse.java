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

package org.wso2.identity.integration.test.rest.api.server.workflow.v1.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;
import javax.validation.Valid;
import javax.xml.bind.annotation.*;

public class WorkflowAssociationResponse  {

    private String id;
    private String associationName;
    private Operation operation;
    private String workflowName;
    private Boolean isEnabled;

    /**
     * Unique id to represent a workflow association
     **/
    public WorkflowAssociationResponse id(String id) {

        this.id = id;
        return this;
    }

    @ApiModelProperty(example = "500", value = "Unique id to represent a workflow association")
    @JsonProperty("id")
    @Valid
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Name of the workflow association
     **/
    public WorkflowAssociationResponse associationName(String associationName) {

        this.associationName = associationName;
        return this;
    }

    @ApiModelProperty(example = "User Registration Workflow Association", value = "Name of the workflow association")
    @JsonProperty("associationName")
    @Valid
    public String getAssociationName() {
        return associationName;
    }
    public void setAssociationName(String associationName) {
        this.associationName = associationName;
    }

    /**
     **/
    public WorkflowAssociationResponse operation(Operation operation) {

        this.operation = operation;
        return this;
    }

    @ApiModelProperty(value = "")
    @JsonProperty("operation")
    @Valid
    public Operation getOperation() {
        return operation;
    }
    public void setOperation(Operation operation) {
        this.operation = operation;
    }

    /**
     * Assigned Workflow
     **/
    public WorkflowAssociationResponse workflowName(String workflowName) {

        this.workflowName = workflowName;
        return this;
    }

    @ApiModelProperty(example = "User Approval Workflow", value = "Assigned Workflow")
    @JsonProperty("workflowName")
    @Valid
    public String getWorkflowName() {
        return workflowName;
    }
    public void setWorkflowName(String workflowName) {
        this.workflowName = workflowName;
    }

    /**
     * Association Status
     **/
    public WorkflowAssociationResponse isEnabled(Boolean isEnabled) {

        this.isEnabled = isEnabled;
        return this;
    }

    @ApiModelProperty(example = "true", value = "Association Status")
    @JsonProperty("isEnabled")
    @Valid
    public Boolean getIsEnabled() {
        return isEnabled;
    }
    public void setIsEnabled(Boolean isEnabled) {
        this.isEnabled = isEnabled;
    }



    @Override
    public boolean equals(java.lang.Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        WorkflowAssociationResponse workflowAssociationResponse = (WorkflowAssociationResponse) o;
        return Objects.equals(this.id, workflowAssociationResponse.id) &&
                Objects.equals(this.associationName, workflowAssociationResponse.associationName) &&
                Objects.equals(this.operation, workflowAssociationResponse.operation) &&
                Objects.equals(this.workflowName, workflowAssociationResponse.workflowName) &&
                Objects.equals(this.isEnabled, workflowAssociationResponse.isEnabled);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, associationName, operation, workflowName, isEnabled);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class WorkflowAssociationResponse {\n");

        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    associationName: ").append(toIndentedString(associationName)).append("\n");
        sb.append("    operation: ").append(toIndentedString(operation)).append("\n");
        sb.append("    workflowName: ").append(toIndentedString(workflowName)).append("\n");
        sb.append("    isEnabled: ").append(toIndentedString(isEnabled)).append("\n");
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


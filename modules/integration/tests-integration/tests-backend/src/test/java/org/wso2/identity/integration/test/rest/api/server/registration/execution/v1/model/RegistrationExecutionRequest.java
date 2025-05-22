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

package org.wso2.identity.integration.test.rest.api.server.registration.execution.v1.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;
import javax.validation.Valid;

public class RegistrationExecutionRequest  {

    private String applicationId;
    private String callbackUrl;
    private String flowId;
    private String actionId;
    private Object inputs;

    /**
     * Unique identifier for the application
     **/
    public RegistrationExecutionRequest applicationId(String applicationId) {

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
     * URL to redirect during registration if required
     **/
    public RegistrationExecutionRequest callbackUrl(String callbackUrl) {

        this.callbackUrl = callbackUrl;
        return this;
    }

    @ApiModelProperty(example = "https://localhost:3000/myRegistrationPortal", value = "URL to redirect during registration if required")
    @JsonProperty("callbackUrl")
    @Valid
    public String getCallbackUrl() {
        return callbackUrl;
    }
    public void setCallbackUrl(String callbackUrl) {
        this.callbackUrl = callbackUrl;
    }

    /**
     * Unique identifier for the registration flow
     **/
    public RegistrationExecutionRequest flowId(String flowId) {

        this.flowId = flowId;
        return this;
    }

    @ApiModelProperty(example = "3fa85f64-5717-4562-b3fc-2c963f66afa6", value = "Unique identifier for the registration flow")
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
    public RegistrationExecutionRequest actionId(String actionId) {

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
     * Input values for the registration step
     **/
    public RegistrationExecutionRequest inputs(Object inputs) {

        this.inputs = inputs;
        return this;
    }

    @ApiModelProperty(example = "{\"username\":\"johnw\"}", value = "Input values for the registration step")
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
        RegistrationExecutionRequest registrationExecutionRequest = (RegistrationExecutionRequest) o;
        return Objects.equals(this.applicationId, registrationExecutionRequest.applicationId) &&
                Objects.equals(this.callbackUrl, registrationExecutionRequest.callbackUrl) &&
                Objects.equals(this.flowId, registrationExecutionRequest.flowId) &&
                Objects.equals(this.actionId, registrationExecutionRequest.actionId) &&
                Objects.equals(this.inputs, registrationExecutionRequest.inputs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(applicationId, callbackUrl, flowId, actionId, inputs);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class RegistrationExecutionRequest {\n");

        sb.append("    applicationId: ").append(toIndentedString(applicationId)).append("\n");
        sb.append("    callbackUrl: ").append(toIndentedString(callbackUrl)).append("\n");
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

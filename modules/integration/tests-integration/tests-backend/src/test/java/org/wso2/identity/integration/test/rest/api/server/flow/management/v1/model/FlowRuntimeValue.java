/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
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

import java.util.Objects;

import javax.validation.constraints.NotNull;

@ApiModel(description = "A runtime value the flow keeps")
public class FlowRuntimeValue {

    private String name;
    private String dataType;
    private Object value;
    private Boolean clientWritable = false;

    /**
     *
     **/
    public FlowRuntimeValue name(String name) {

        this.name = name;
        return this;
    }

    @ApiModelProperty(example = "plan", required = true, value = "Name of the runtime value, unique within the flow.")
    @JsonProperty("name")
    @NotNull(message = "Property name cannot be null.")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     *
     **/
    public FlowRuntimeValue dataType(String dataType) {

        this.dataType = dataType;
        return this;
    }

    @ApiModelProperty(example = "STRING", required = true, value = "Type of value the runtime value holds.")
    @JsonProperty("dataType")
    @NotNull(message = "Property dataType cannot be null.")
    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    /**
     *
     **/
    public FlowRuntimeValue value(Object value) {

        this.value = value;
        return this;
    }

    @ApiModelProperty(example = "basic", value = "Value every run of the flow starts from.")
    @JsonProperty("value")
    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    /**
     *
     **/
    public FlowRuntimeValue clientWritable(Boolean clientWritable) {

        this.clientWritable = clientWritable;
        return this;
    }

    @ApiModelProperty(example = "false", value = "Whether the app may set the runtime value when the flow starts.")
    @JsonProperty("clientWritable")
    public Boolean getClientWritable() {
        return clientWritable;
    }

    public void setClientWritable(Boolean clientWritable) {
        this.clientWritable = clientWritable;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FlowRuntimeValue flowRuntimeValue = (FlowRuntimeValue) o;
        return Objects.equals(this.name, flowRuntimeValue.name) &&
                Objects.equals(this.dataType, flowRuntimeValue.dataType) &&
                Objects.equals(this.value, flowRuntimeValue.value) &&
                Objects.equals(this.clientWritable, flowRuntimeValue.clientWritable);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, dataType, value, clientWritable);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class FlowRuntimeValue {\n");

        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    dataType: ").append(toIndentedString(dataType)).append("\n");
        sb.append("    value: ").append(toIndentedString(value)).append("\n");
        sb.append("    clientWritable: ").append(toIndentedString(clientWritable)).append("\n");
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

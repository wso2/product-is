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

import java.util.Objects;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@ApiModel(description = "Represent a single step in the registration flow process")
public class Step {

    private String id;
    private String type;
    private Data data;
    private Size size;
    private Position position;

    /**
     * Unique identifier of the step
     **/
    public Step id(String id) {

        this.id = id;
        return this;
    }

    @ApiModelProperty(example = "dnd-step-3fa85f64-5717-4562-b3fc-2c963f66afa6", required = true, value = "Unique identifier of the step")
    @JsonProperty("id")
    @Valid
    @NotNull(message = "Property id cannot be null.")

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * Type of the step
     **/
    public Step type(String type) {

        this.type = type;
        return this;
    }

    @ApiModelProperty(example = "VIEW", required = true, value = "Type of the step")
    @JsonProperty("type")
    @Valid
    @NotNull(message = "Property type cannot be null.")

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    /**
     *
     **/
    public Step data(Data data) {

        this.data = data;
        return this;
    }

    @ApiModelProperty(required = true, value = "")
    @JsonProperty("data")
    @Valid
    @NotNull(message = "Property data cannot be null.")

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    /**
     *
     **/
    public Step size(Size size) {

        this.size = size;
        return this;
    }

    @ApiModelProperty(required = true, value = "")
    @JsonProperty("size")
    @Valid
    @NotNull(message = "Property size cannot be null.")

    public Size getSize() {
        return size;
    }

    public void setSize(Size size) {
        this.size = size;
    }

    /**
     *
     **/
    public Step position(Position position) {

        this.position = position;
        return this;
    }

    @ApiModelProperty(required = true, value = "")
    @JsonProperty("position")
    @Valid
    @NotNull(message = "Property position cannot be null.")

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }


    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Step step = (Step) o;
        return Objects.equals(this.id, step.id) &&
                Objects.equals(this.type, step.type) &&
                Objects.equals(this.data, step.data) &&
                Objects.equals(this.size, step.size) &&
                Objects.equals(this.position, step.position);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, type, data, size, position);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class Step {\n");

        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    type: ").append(toIndentedString(type)).append("\n");
        sb.append("    data: ").append(toIndentedString(data)).append("\n");
        sb.append("    size: ").append(toIndentedString(size)).append("\n");
        sb.append("    position: ").append(toIndentedString(position)).append("\n");
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


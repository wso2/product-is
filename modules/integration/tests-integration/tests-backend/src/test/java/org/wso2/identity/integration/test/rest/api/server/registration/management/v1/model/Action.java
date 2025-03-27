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

package org.wso2.identity.integration.test.rest.api.server.registration.management.v1.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@ApiModel(description = "Represent an action which controls the registration flow")
public class Action {

    private String type;
    private Executor executor;
    private String next;

    /**
     * Type of action
     **/
    public Action type(String type) {

        this.type = type;
        return this;
    }

    @ApiModelProperty(example = "EXECUTOR", required = true, value = "Type of action")
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
    public Action executor(Executor executor) {

        this.executor = executor;
        return this;
    }

    @ApiModelProperty(value = "")
    @JsonProperty("executor")
    @Valid
    public Executor getExecutor() {
        return executor;
    }

    public void setExecutor(Executor executor) {
        this.executor = executor;
    }

    /**
     * ID of the next step. For the last step, value will be \&quot;COMPLETE\&quot;
     **/
    public Action next(String next) {

        this.next = next;
        return this;
    }

    @ApiModelProperty(example = "dnd-step-asd85f64-5717-4562-b3fc-2234f66afa6", required = true, value = "ID of the next step. For the last step, value will be \"COMPLETE\"")
    @JsonProperty("next")
    @Valid
    @NotNull(message = "Property next cannot be null.")

    public String getNext() {
        return next;
    }

    public void setNext(String next) {
        this.next = next;
    }


    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Action action = (Action) o;
        return Objects.equals(this.type, action.type) &&
                Objects.equals(this.executor, action.executor) &&
                Objects.equals(this.next, action.next);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, executor, next);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class Action {\n");

        sb.append("    type: ").append(toIndentedString(type)).append("\n");
        sb.append("    executor: ").append(toIndentedString(executor)).append("\n");
        sb.append("    next: ").append(toIndentedString(next)).append("\n");
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


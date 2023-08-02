/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.identity.integration.test.rest.api.server.idp.v1.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.Valid;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;
import java.util.Objects;

public class PatchRequest {

    @XmlType(name="OperationEnum")
    @XmlEnum()
    public enum OperationEnum {

        @XmlEnumValue("ADD") ADD("ADD"),
        @XmlEnumValue("REMOVE") REMOVE("REMOVE"),
        @XmlEnumValue("REPLACE") REPLACE("REPLACE");

        private String value;

        OperationEnum(String v) {
            value = v;
        }

        public String value() {
            return value;
        }

        @Override
        public String toString() {
            return String.valueOf(value);
        }

        public static OperationEnum fromValue(String value) {
            for (OperationEnum b : OperationEnum.values()) {
                if (b.value.equals(value)) {
                    return b;
                }
            }
            throw new IllegalArgumentException("Unexpected value '" + value + "'");
        }
    }

    private OperationEnum operation;
    private String path;
    private String value;

    /**
     * The operation to be performed
     **/
    public PatchRequest operation(OperationEnum operation) {

        this.operation = operation;
        return this;
    }

    @ApiModelProperty(example = "REPLACE", required = true, value = "The operation to be performed")
    @JsonProperty("operation")
    @Valid
    public OperationEnum getOperation() {
        return operation;
    }
    public void setOperation(OperationEnum operation) {
        this.operation = operation;
    }

    /**
     * A JSON-Pointer
     **/
    public PatchRequest path(String path) {

        this.path = path;
        return this;
    }

    @ApiModelProperty(example = "/idleSessionTimeoutPeriod", required = true, value = "A JSON-Pointer")
    @JsonProperty("path")
    @Valid
    public String getPath() {
        return path;
    }
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * The value to be used within the operations
     **/
    public PatchRequest value(String value) {

        this.value = value;
        return this;
    }

    @ApiModelProperty(example = "30", value = "The value to be used within the operations")
    @JsonProperty("value")
    @Valid
    public String getValue() {
        return value;
    }
    public void setValue(String value) {
        this.value = value;
    }



    @Override
    public boolean equals(java.lang.Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PatchRequest patchRequest = (PatchRequest) o;
        return Objects.equals(this.operation, patchRequest.operation) &&
                Objects.equals(this.path, patchRequest.path) &&
                Objects.equals(this.value, patchRequest.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(operation, path, value);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class PatchRequest {\n");

        sb.append("    operation: ").append(toIndentedString(operation)).append("\n");
        sb.append("    path: ").append(toIndentedString(path)).append("\n");
        sb.append("    value: ").append(toIndentedString(value)).append("\n");
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

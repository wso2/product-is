/**
 * Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.identity.integration.test.rest.api.server.identity.governance.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.Valid;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ConnectorsPatchReq {

    @XmlType(name="OperationEnum")
    @XmlEnum()
    public enum OperationEnum {

        @XmlEnumValue("UPDATE") UPDATE("UPDATE");

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

        public static ConnectorsPatchReq.OperationEnum fromValue(String value) {
            for (ConnectorsPatchReq.OperationEnum b : ConnectorsPatchReq.OperationEnum.values()) {
                if (b.value.equals(value)) {
                    return b;
                }
            }
            throw new IllegalArgumentException("Unexpected value '" + value + "'");
        }
    }

    private OperationEnum operation = OperationEnum.UPDATE;
    private List<PropertyReq> properties;

    /**
     **/
    public ConnectorsPatchReq operation(OperationEnum operation) {
        this.operation = operation;
        return this;
    }

    @ApiModelProperty()
    @JsonProperty("operation")
    @Valid
    public OperationEnum getOperation() {
        return operation;
    }
    public void setOperation(OperationEnum operation) {
        this.operation = operation;
    }

    /**
     **/
    public ConnectorsPatchReq properties(List<PropertyReq> properties) {
        this.properties = properties;
        return this;
    }

    @ApiModelProperty()
    @JsonProperty("properties")
    @Valid
    public List<PropertyReq> getProperties() { return properties; }

    public void setProperties(List<PropertyReq> properties) { this.properties = properties; }

    public ConnectorsPatchReq addProperties(PropertyReq property) {
        if (this.properties == null) {
            this.properties = new ArrayList<>();
        }
        this.properties.add(property);
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
        ConnectorsPatchReq connectorsPatchReq = (ConnectorsPatchReq) o;
        return Objects.equals(this.operation, connectorsPatchReq.operation) &&
                Objects.equals(this.properties, connectorsPatchReq.properties);
    }

    @Override
    public int hashCode() {

        return Objects.hash(operation, properties);
    }

}

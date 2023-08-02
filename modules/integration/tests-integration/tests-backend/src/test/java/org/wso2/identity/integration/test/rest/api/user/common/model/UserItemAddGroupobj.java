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

package org.wso2.identity.integration.test.rest.api.user.common.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.Valid;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;
import java.util.Objects;

public class UserItemAddGroupobj {


    @XmlType(name="OpEnum")
    @XmlEnum()
    public enum OpEnum {

        @XmlEnumValue("add") ADD("add"), @XmlEnumValue("remove") REMOVE("remove"), @XmlEnumValue("replace") REPLACE("replace");


        private String value;

        OpEnum(String val) {
            value = val;
        }

        public String value() {
            return value;
        }

        @Override
        public String toString() {
            return String.valueOf(value);
        }

        public static OpEnum fromValue(String value) {
            for (OpEnum op : OpEnum.values()) {
                if (op.value.equals(value)) {
                    return op;
                }
            }
            throw new IllegalArgumentException("Unexpected value '" + value + "'");
        }
    }

    private OpEnum op = null;
    private String path;
    private Object value = null;

    /**
     **/
    public UserItemAddGroupobj op(OpEnum op) {

        this.op = op;
        return this;
    }

    @ApiModelProperty()
    @JsonProperty("op")
    @Valid
    public OpEnum getOp() {
        return op;
    }
    public void setOp(OpEnum op) {
        this.op = op;
    }

    /**
     *
     **/
    public UserItemAddGroupobj path(String path) {

        this.path = path;
        return this;
    }

    @ApiModelProperty(example = "users")
    @JsonProperty("path")
    @Valid
    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    /**
     *
     **/
    public UserItemAddGroupobj value(Object value) {

        this.value = value;
        return this;
    }

    @ApiModelProperty()
    @JsonProperty("value")
    @Valid
    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UserItemAddGroupobj roleItemAddGroupobj = (UserItemAddGroupobj) o;
        return Objects.equals(this.op, roleItemAddGroupobj.op) &&
                Objects.equals(this.path, roleItemAddGroupobj.path) &&
                Objects.equals(this.value, roleItemAddGroupobj.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(op, path, value);
    }

    @Override
    public String toString() {

        return "class RoleItemAddGroupobj {\n" +
                "    op: " + toIndentedString(op) + "\n" +
                "    path: " + toIndentedString(path) + "\n" +
                "    value: " + toIndentedString(value) + "\n" +
                "}";
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(Object o) {

        if (o == null) {
            return "null";
        }
        return o.toString();
    }
}

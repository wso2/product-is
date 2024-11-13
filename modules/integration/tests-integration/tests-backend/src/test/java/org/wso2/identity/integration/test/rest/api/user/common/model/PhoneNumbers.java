/*
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
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
package org.wso2.identity.integration.test.rest.api.user.common.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.Valid;
import java.util.Objects;

public class PhoneNumbers {

    private String type;
    private String value;

    public PhoneNumbers type(String type) {

        this.type = type;
        return this;
    }

    @ApiModelProperty(example = "mobile")
    @JsonProperty("type")
    @Valid
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public PhoneNumbers value(String value) {

        this.value = value;
        return this;
    }

    @ApiModelProperty(example = "1111111111")
    @JsonProperty("value")
    @Valid
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
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
        PhoneNumbers email = (PhoneNumbers) o;
        return Objects.equals(this.type, email.type) &&
                Objects.equals(this.value, email.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, value);
    }

    @Override
    public String toString() {

        return "class Email {\n" +
                "    type: " + toIndentedString(type) + "\n" +
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

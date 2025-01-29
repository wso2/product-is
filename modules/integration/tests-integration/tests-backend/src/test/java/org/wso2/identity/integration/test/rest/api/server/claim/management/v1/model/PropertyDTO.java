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

package org.wso2.identity.integration.test.rest.api.server.claim.management.v1.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;

public class PropertyDTO {

    private String key = null;
    private String value = null;

    @ApiModelProperty(required = true, value = "")
    @JsonProperty("key")
    public String getKey() {

        return key;
    }
    public void setKey(String key) {

        this.key = key;
    }

    @ApiModelProperty(required = true, value = "")
    @JsonProperty("value")
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
        PropertyDTO propertyDTO = (PropertyDTO) o;
        return Objects.equals(this.key, propertyDTO.key) &&
                Objects.equals(this.value, propertyDTO.value);
    }

    @Override
    public int hashCode() {

        return Objects.hash(key, value);
    }

    @Override
    public String toString() {

        return "class PropertyDTO {\n" +
                "    key: " + toIndentedString(key) + "\n" +
                "    value: " + toIndentedString(value) + "\n" +
                "}\n";
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

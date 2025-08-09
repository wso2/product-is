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

package org.wso2.identity.integration.test.rest.api.server.input.validation.v1.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class RevertFields {

    private List<String> fields = new ArrayList<>();

    public RevertFields fields(List<String> fields) {

        this.fields = fields;
        return this;
    }

    @ApiModelProperty(required = true, value = "")
    @JsonProperty("fields")
    @Valid
    @NotNull(message = "Property fields cannot be null.")

    public List<String> getFields() {

        return fields;
    }

    public void setFields(List<String> fields) {

        this.fields = fields;
    }

    public RevertFields addFieldsItem(String fieldsItem) {

        this.fields.add(fieldsItem);
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

        RevertFields revertFields = (RevertFields) o;
        return Objects.equals(this.fields, revertFields.fields);
    }

    @Override
    public int hashCode() {

        return Objects.hash(fields);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class RevertFields {\n");

        sb.append("    fields: ").append(toIndentedString(fields)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    private String toIndentedString(java.lang.Object o) {

        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n");
    }
}

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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.validation.Valid;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

public class PatchOperationRequestObject {

    private List<String> schemas = null;
    private List<Object> Operations = null;

    /**
     *
     **/
    public PatchOperationRequestObject schemas(List<String> schemas) {

        this.schemas = schemas;
        return this;
    }

    @ApiModelProperty()
    @JsonProperty("schemas")
    @Valid
    public List<String> getSchemas() {
        return schemas;
    }

    public void setSchemas(List<String> schemas) {
        this.schemas = schemas;
    }

    /**
     *
     **/
    public PatchOperationRequestObject Operations(List<Object> Operations) {

        this.Operations = Operations;
        return this;
    }

    @ApiModelProperty()
    @JsonProperty("Operations")
    @Valid
    public List<Object> getOperations() {
        return Operations;
    }

    public void setOperations(List<Object> Operations) {
        this.Operations = Operations;
    }

    public PatchOperationRequestObject addOperations(Object Operation) {
        if (this.Operations == null) {
            this.Operations = new ArrayList<>();
        }
        this.Operations.add(Operation);
        return this;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PatchOperationRequestObject user = (PatchOperationRequestObject) o;
        return Objects.equals(this.schemas, user.schemas) &&
                Objects.equals(this.Operations, user.Operations);

    }

    @Override
    public int hashCode() {
        return Objects.hash(schemas, Operations);
    }

    @Override
    public String toString() {

        return "class PatchRoleOperationRequestObject {\n" +
                "    schemas: " + toIndentedString(schemas) + "\n" +
                "    Operations: " + toIndentedString(Operations) + "\n" +
                "}";
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(java.lang.Object o) {

        if (o == null) {
            return "null";
        }
        return o.toString();
    }
}

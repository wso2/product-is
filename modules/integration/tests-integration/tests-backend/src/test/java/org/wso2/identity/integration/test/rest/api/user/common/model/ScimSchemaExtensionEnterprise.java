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
import java.util.Objects;

public class ScimSchemaExtensionEnterprise {
    private Manager manager;
    private String employeeNumber;

    /**
     *
     **/
    public ScimSchemaExtensionEnterprise manager(Manager manager) {

        this.manager = manager;
        return this;
    }

    @ApiModelProperty()
    @JsonProperty("manager")
    @Valid
    public Manager getManager() {
        return manager;
    }

    public void setManager(Manager manager) {
        this.manager = manager;
    }

    /**
     *
     **/
    public ScimSchemaExtensionEnterprise employeeNumber(String employeeNumber) {

        this.employeeNumber = employeeNumber;
        return this;
    }

    @ApiModelProperty(example = "1234A")
    @JsonProperty("employeeNumber")
    @Valid
    public String getEmployeeNumber() {
        return employeeNumber;
    }

    public void setEmployeeNumber(String employeeNumber) {
        this.employeeNumber = employeeNumber;
    }


    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ScimSchemaExtensionEnterprise scimSchemaExtensionEnterprise = (ScimSchemaExtensionEnterprise) o;
        return Objects.equals(this.manager, scimSchemaExtensionEnterprise.manager) &&
                Objects.equals(this.employeeNumber, scimSchemaExtensionEnterprise.employeeNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(manager, employeeNumber);
    }

    @Override
    public String toString() {

        return "class ScimSchemaExtensionEnterprise {\n" +
                "    manager: " + toIndentedString(manager) + "\n" +
                "    employeeNumber: " + toIndentedString(employeeNumber) + "\n" +
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

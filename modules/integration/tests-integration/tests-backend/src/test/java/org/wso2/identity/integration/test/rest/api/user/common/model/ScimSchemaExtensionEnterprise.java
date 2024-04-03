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
    private Boolean accountLocked;
    private String country;
    private String department;
    private String stateorprovince;

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

    /**
     *
     **/
    public ScimSchemaExtensionEnterprise accountLocked(Boolean accountLocked) {

        this.accountLocked = accountLocked;
        return this;
    }

    @ApiModelProperty(example = "false")
    @JsonProperty("accountLocked")
    @Valid
    public Boolean accountLocked() {
        return accountLocked;
    }

    public void setAccountLocked(Boolean accountLocked) {
        this.accountLocked = accountLocked;
    }

    /**
     *
     **/
    public ScimSchemaExtensionEnterprise country(String country) {

        this.country = country;
        return this;
    }

    @ApiModelProperty(example = "Sri Lanka")
    @JsonProperty("country")
    @Valid
    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    @ApiModelProperty(example = "Western")
    @JsonProperty("stateorprovince")
    @Valid
    public String getStateorprovince() {
        return stateorprovince;
    }

    public void setStateorprovince(String stateorprovince) {
        this.stateorprovince = stateorprovince;
    }

    @ApiModelProperty(example = "Engineering")
    @JsonProperty("department")
    @Valid
    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
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
                Objects.equals(this.employeeNumber, scimSchemaExtensionEnterprise.employeeNumber) &&
                Objects.equals(this.accountLocked, scimSchemaExtensionEnterprise.accountLocked) &&
                Objects.equals(this.country, scimSchemaExtensionEnterprise.country) &&
                Objects.equals(this.department, scimSchemaExtensionEnterprise.department) &&
                Objects.equals(this.stateorprovince, scimSchemaExtensionEnterprise.stateorprovince);
    }

    @Override
    public int hashCode() {
        return Objects.hash(manager, employeeNumber, accountLocked, country);
    }

    @Override
    public String toString() {

        return "class ScimSchemaExtensionEnterprise {\n" +
                "    manager: " + toIndentedString(manager) + "\n" +
                "    employeeNumber: " + toIndentedString(employeeNumber) + "\n" +
                "    accountLocked: " + toIndentedString(accountLocked) + "\n" +
                "    country: " + toIndentedString(country) + "\n" +
                "    department: " + toIndentedString(department) + "\n" +
                "    stateorprovince: " + toIndentedString(stateorprovince) + "\n" +
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

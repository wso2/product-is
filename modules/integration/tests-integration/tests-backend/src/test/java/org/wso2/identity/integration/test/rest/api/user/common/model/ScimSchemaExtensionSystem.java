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

package org.wso2.identity.integration.test.rest.api.user.common.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.Valid;
import java.util.Objects;

public class ScimSchemaExtensionSystem {

    private Boolean accountLocked;
    private String country;
    private String stateorprovince;
    private Boolean askPassword;
    /**
     *
     **/
    public ScimSchemaExtensionSystem accountLocked(Boolean accountLocked) {

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

    public ScimSchemaExtensionSystem askPassword(Boolean askPassword) {

        this.askPassword = askPassword;
        return this;
    }

    @ApiModelProperty(example = "false")
    @JsonProperty("askPassword")
    @Valid
    public Boolean askPassword() {
        return askPassword;
    }

    public void setAskPassword(Boolean askPassword) {
        this.askPassword = askPassword;
    }

    /**
     *
     **/
    public ScimSchemaExtensionSystem country(String country) {

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

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ScimSchemaExtensionSystem scimSchemaExtensionSystem = (ScimSchemaExtensionSystem) o;
        return Objects.equals(this.accountLocked, scimSchemaExtensionSystem.accountLocked) &&
                Objects.equals(this.country, scimSchemaExtensionSystem.country) &&
                Objects.equals(this.stateorprovince, scimSchemaExtensionSystem.stateorprovince);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountLocked, country, stateorprovince);
    }

    @Override
    public String toString() {

        return "class ScimSchemaExtensionSystem {\n" +
                "    accountLocked: " + toIndentedString(accountLocked) + "\n" +
                "    country: " + toIndentedString(country) + "\n" +
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

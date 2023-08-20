/*
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

package org.wso2.identity.integration.test.rest.api.server.tenant.management.v1.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Owner  {

    private String username;
    private String password;
    private String email;
    private String firstname;
    private String lastname;
    private String provisioningMethod;
    private List<AdditionalClaims> additionalClaims = null;

    /**
     * Username for the tenant owner.
     **/
    public Owner username(String username) {

        this.username = username;
        return this;
    }

    @ApiModelProperty(example = "kim", required = true, value = "Username for the tenant owner.")
    @JsonProperty("username")
    @Valid
    @NotNull(message = "Property username cannot be null.")

    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Password of the owner.
     **/
    public Owner password(String password) {

        this.password = password;
        return this;
    }

    @ApiModelProperty(example = "kim123", value = "Password of the owner.")
    @JsonProperty("password")
    @Valid
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Email address of the owner.
     **/
    public Owner email(String email) {

        this.email = email;
        return this;
    }

    @ApiModelProperty(example = "kim@wso2.com", required = true, value = "Email address of the owner.")
    @JsonProperty("email")
    @Valid
    @NotNull(message = "Property email cannot be null.")

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * First name of the owner.
     **/
    public Owner firstname(String firstname) {

        this.firstname = firstname;
        return this;
    }

    @ApiModelProperty(example = "kim", value = "First name of the owner.")
    @JsonProperty("firstname")
    @Valid
    public String getFirstname() {
        return firstname;
    }
    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    /**
     * Last name of the owner.
     **/
    public Owner lastname(String lastname) {

        this.lastname = lastname;
        return this;
    }

    @ApiModelProperty(example = "kim", value = "Last name of the owner.")
    @JsonProperty("lastname")
    @Valid
    public String getLastname() {
        return lastname;
    }
    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    /**
     * If the provisioning method is inline-password then a valid password should be sent in the request body, if the provisioning method is invite-via-email then password doesn&#39;t need to be send through request body, instead an emai link will be sent to the given email address to set the password.
     **/
    public Owner provisioningMethod(String provisioningMethod) {

        this.provisioningMethod = provisioningMethod;
        return this;
    }

    @ApiModelProperty(example = "inline-password", required = true, value = "If the provisioning method is inline-password then a valid password should be sent in the request body, if the provisioning method is invite-via-email then password doesn't need to be send through request body, instead an emai link will be sent to the given email address to set the password.")
    @JsonProperty("provisioningMethod")
    @Valid
    @NotNull(message = "Property provisioningMethod cannot be null.")

    public String getProvisioningMethod() {
        return provisioningMethod;
    }
    public void setProvisioningMethod(String provisioningMethod) {
        this.provisioningMethod = provisioningMethod;
    }

    /**
     **/
    public Owner additionalClaims(List<AdditionalClaims> additionalClaims) {

        this.additionalClaims = additionalClaims;
        return this;
    }

    @ApiModelProperty(value = "")
    @JsonProperty("additionalClaims")
    @Valid
    public List<AdditionalClaims> getAdditionalClaims() {
        return additionalClaims;
    }
    public void setAdditionalClaims(List<AdditionalClaims> additionalClaims) {
        this.additionalClaims = additionalClaims;
    }

    public Owner addAdditionalClaimsItem(AdditionalClaims additionalClaimsItem) {
        if (this.additionalClaims == null) {
            this.additionalClaims = new ArrayList<>();
        }
        this.additionalClaims.add(additionalClaimsItem);
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
        Owner owner = (Owner) o;
        return Objects.equals(this.username, owner.username) &&
                Objects.equals(this.password, owner.password) &&
                Objects.equals(this.email, owner.email) &&
                Objects.equals(this.firstname, owner.firstname) &&
                Objects.equals(this.lastname, owner.lastname) &&
                Objects.equals(this.provisioningMethod, owner.provisioningMethod) &&
                Objects.equals(this.additionalClaims, owner.additionalClaims);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, password, email, firstname, lastname, provisioningMethod, additionalClaims);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class Owner {\n");
        sb.append("    username: ").append(toIndentedString(username)).append("\n");
        sb.append("    password: ").append(toIndentedString(password)).append("\n");
        sb.append("    email: ").append(toIndentedString(email)).append("\n");
        sb.append("    firstname: ").append(toIndentedString(firstname)).append("\n");
        sb.append("    lastname: ").append(toIndentedString(lastname)).append("\n");
        sb.append("    provisioningMethod: ").append(toIndentedString(provisioningMethod)).append("\n");
        sb.append("    additionalClaims: ").append(toIndentedString(additionalClaims)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private static String toIndentedString(java.lang.Object o) {

        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n");
    }

    public static class AdditionalClaims  {

        private String claim;
        private String value;

        /**
         * Claim uri.
         **/
        public AdditionalClaims claim(String claim) {

            this.claim = claim;
            return this;
        }

        @ApiModelProperty(example = "http://wso2.org/claims/telephone", value = "Claim uri.")
        @JsonProperty("claim")
        @Valid
        public String getClaim() {
            return claim;
        }
        public void setClaim(String claim) {
            this.claim = claim;
        }

        /**
         * Mobile number of the tenant owner.
         **/
        public AdditionalClaims value(String value) {

            this.value = value;
            return this;
        }

        @ApiModelProperty(example = "+94 562 8723", value = "Mobile number of the tenant owner.")
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
            AdditionalClaims additionalClaims = (AdditionalClaims) o;
            return Objects.equals(this.claim, additionalClaims.claim) &&
                    Objects.equals(this.value, additionalClaims.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(claim, value);
        }

        @Override
        public String toString() {

            StringBuilder sb = new StringBuilder();
            sb.append("class AdditionalClaims {\n");
            sb.append("    claim: ").append(toIndentedString(claim)).append("\n");
            sb.append("    value: ").append(toIndentedString(value)).append("\n");
            sb.append("}");
            return sb.toString();
        }
    }
}

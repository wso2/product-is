/*
 * Copyright (c) 2023-2025, WSO2 LLC. (https://www.wso2.com).
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

public class TenantModel  {

    private String name;
    private String domain;
    private List<Owner> owners = new ArrayList<>();

    /**
     * Name of the tenant.
     **/
    public TenantModel name(String name) {

        this.name = name;
        return this;
    }

    @ApiModelProperty(example = "ABC Builders", value = "Name of the tenant.")
    @JsonProperty("name")
    @Valid
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Tenant domain of the tenant.
     **/
    public TenantModel domain(String domain) {

        this.domain = domain;
        return this;
    }

    @ApiModelProperty(example = "abc.com", required = true, value = "Tenant domain of the tenant.")
    @JsonProperty("domain")
    @Valid
    @NotNull(message = "Property domain cannot be null.")

    public String getDomain() {
        return domain;
    }
    public void setDomain(String domain) {
        this.domain = domain;
    }

    /**
     **/
    public TenantModel owners(List<Owner> owners) {

        this.owners = owners;
        return this;
    }

    @ApiModelProperty(required = true, value = "")
    @JsonProperty("owners")
    @Valid
    @NotNull(message = "Property owners cannot be null.")

    public List<Owner> getOwners() {
        return owners;
    }
    public void setOwners(List<Owner> owners) {
        this.owners = owners;
    }

    public TenantModel addOwnersItem(Owner ownersItem) {
        this.owners.add(ownersItem);
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
        TenantModel tenantModel = (TenantModel) o;
        return Objects.equals(this.name, tenantModel.name) &&
                Objects.equals(this.domain, tenantModel.domain) &&
                Objects.equals(this.owners, tenantModel.owners);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, domain, owners);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class TenantModel {\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    domain: ").append(toIndentedString(domain)).append("\n");
        sb.append("    owners: ").append(toIndentedString(owners)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(java.lang.Object o) {

        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n");
    }
}

/*
 * Copyright (c) 2020-2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.identity.integration.test.rest.api.server.tenant.management.v1.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.validation.Valid;

public class TenantResponseModel {

    private String id;
    private String name;
    private String domain;
    private List<OwnerResponse> owners = null;

    private String createdDate;
    private LifeCycleStatus lifecycleStatus;
    private String region;

    /**
    * tenant id of the tenant owner.
    **/
    public TenantResponseModel id(String id) {

        this.id = id;
        return this;
    }

    @ApiModelProperty(example = "123e4567-e89b-12d3-a456-556642440000", value = "tenant id of the tenant owner.")
    @JsonProperty("id")
    @Valid
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Name of the tenant.
     **/
    public TenantResponseModel name(String name) {

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
    public TenantResponseModel domain(String domain) {

        this.domain = domain;
        return this;
    }

    @ApiModelProperty(example = "abc.com", value = "Tenant domain of the tenant.")
    @JsonProperty("domain")
    @Valid
    public String getDomain() {
        return domain;
    }
    public void setDomain(String domain) {
        this.domain = domain;
    }

    /**
    **/
    public TenantResponseModel owners(List<OwnerResponse> owners) {

        this.owners = owners;
        return this;
    }

    @ApiModelProperty(value = "")
    @JsonProperty("owners")
    @Valid
    public List<OwnerResponse> getOwners() {
        return owners;
    }
    public void setOwners(List<OwnerResponse> owners) {
        this.owners = owners;
    }

    public TenantResponseModel addOwnersItem(OwnerResponse ownersItem) {
        if (this.owners == null) {
            this.owners = new ArrayList<>();
        }
        this.owners.add(ownersItem);
        return this;
    }

    /**
     * Tenant created time in ISO-8601 format.
     **/
    public TenantResponseModel createdDate(String createdDate) {

        this.createdDate = createdDate;
        return this;
    }

    @ApiModelProperty(example = "2020-06-19T17:36:46.271Z", value = "Tenant created time in ISO-8601 format.")
    @JsonProperty("createdDate")
    @Valid
    public String getCreatedDate() {
        return createdDate;
    }
    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    /**
    **/
    public TenantResponseModel lifecycleStatus(LifeCycleStatus lifecycleStatus) {

        this.lifecycleStatus = lifecycleStatus;
        return this;
    }

    @ApiModelProperty(value = "")
    @JsonProperty("lifecycleStatus")
    @Valid
    public LifeCycleStatus getLifecycleStatus() {
        return lifecycleStatus;
    }
    public void setLifecycleStatus(LifeCycleStatus lifecycleStatus) {
        this.lifecycleStatus = lifecycleStatus;
    }

    /**
    * Region of the tenant.
    **/
    public TenantResponseModel region(String region) {

        this.region = region;
        return this;
    }

    @ApiModelProperty(example = "USA", value = "Region of the tenant.")
    @JsonProperty("region")
    @Valid
    public String getRegion() {
        return region;
    }
    public void setRegion(String region) {
        this.region = region;
    }



    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TenantResponseModel tenantResponseModel = (TenantResponseModel) o;
        return Objects.equals(this.id, tenantResponseModel.id) &&
            Objects.equals(this.name, tenantResponseModel.name) &&
            Objects.equals(this.domain, tenantResponseModel.domain) &&
            Objects.equals(this.owners, tenantResponseModel.owners) &&
            Objects.equals(this.createdDate, tenantResponseModel.createdDate) &&
            Objects.equals(this.lifecycleStatus, tenantResponseModel.lifecycleStatus) &&
            Objects.equals(this.region, tenantResponseModel.region);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, domain, owners, createdDate, lifecycleStatus, region);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class TenantResponseModel {\n");

        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    domain: ").append(toIndentedString(domain)).append("\n");
        sb.append("    owners: ").append(toIndentedString(owners)).append("\n");
        sb.append("    createdDate: ").append(toIndentedString(createdDate)).append("\n");
        sb.append("    lifecycleStatus: ").append(toIndentedString(lifecycleStatus)).append("\n");
        sb.append("    region: ").append(toIndentedString(region)).append("\n");
        sb.append("}");
        return sb.toString();
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


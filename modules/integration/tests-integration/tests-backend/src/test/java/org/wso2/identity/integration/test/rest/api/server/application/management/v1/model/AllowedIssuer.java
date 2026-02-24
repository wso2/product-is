/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.identity.integration.test.rest.api.server.application.management.v1.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.Valid;
import java.util.Objects;

public class AllowedIssuer {
  
    private String value;
    private String organizationId;
    private String tenantDomain;

    /**
    * Issuer detail that can be used for tokens.
    **/
    public AllowedIssuer value(String value) {

        this.value = value;
        return this;
    }
    
    @ApiModelProperty(example = "https://localhost:9443/oauth2/token", value = "Issuer detail that can be used for tokens.")
    @JsonProperty("value")
    @Valid
    public String getValue() {
        return value;
    }
    public void setValue(String value) {
        this.value = value;
    }

    /**
    * Organization ID of the allowed issuer.
    **/
    public AllowedIssuer organizationId(String organizationId) {

        this.organizationId = organizationId;
        return this;
    }
    
    @ApiModelProperty(example = "bdece142-646b-45c0-9385-a4159f1ea219", value = "Organization ID of the allowed issuer.")
    @JsonProperty("organizationId")
    @Valid
    public String getOrganizationId() {
        return organizationId;
    }
    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    /**
    * Tenant domain of the allowed issuer.
    **/
    public AllowedIssuer tenantDomain(String tenantDomain) {

        this.tenantDomain = tenantDomain;
        return this;
    }
    
    @ApiModelProperty(example = "wso2.com", value = "Tenant domain of the allowed issuer.")
    @JsonProperty("tenantDomain")
    @Valid
    public String getTenantDomain() {
        return tenantDomain;
    }
    public void setTenantDomain(String tenantDomain) {
        this.tenantDomain = tenantDomain;
    }



    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AllowedIssuer allowedIssuer = (AllowedIssuer) o;
        return Objects.equals(this.value, allowedIssuer.value) &&
            Objects.equals(this.organizationId, allowedIssuer.organizationId) &&
            Objects.equals(this.tenantDomain, allowedIssuer.tenantDomain);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, organizationId, tenantDomain);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class AllowedIssuer {\n");
        
        sb.append("    value: ").append(toIndentedString(value)).append("\n");
        sb.append("    organizationId: ").append(toIndentedString(organizationId)).append("\n");
        sb.append("    tenantDomain: ").append(toIndentedString(tenantDomain)).append("\n");
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


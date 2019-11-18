/*
* Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.wso2.identity.integration.test.rest.api.server.idp.v1.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class Claims   {
  
  private Claim userIdClaim;

  private Claim roleClaim;

  private List<ClaimMapping> mappings = null;

  private List<ProvisioningClaim> provisioningClaims = null;


  /**
   **/
  public Claims userIdClaim(Claim userIdClaim) {
    this.userIdClaim = userIdClaim;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("userIdClaim")
  @Valid
  public Claim getUserIdClaim() {
    return userIdClaim;
  }
  public void setUserIdClaim(Claim userIdClaim) {
    this.userIdClaim = userIdClaim;
  }


  /**
   **/
  public Claims roleClaim(Claim roleClaim) {
    this.roleClaim = roleClaim;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("roleClaim")
  @Valid
  public Claim getRoleClaim() {
    return roleClaim;
  }
  public void setRoleClaim(Claim roleClaim) {
    this.roleClaim = roleClaim;
  }


  /**
   **/
  public Claims mappings(List<ClaimMapping> mappings) {
    this.mappings = mappings;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("mappings")
  @Valid
  public List<ClaimMapping> getMappings() {
    return mappings;
  }
  public void setMappings(List<ClaimMapping> mappings) {
    this.mappings = mappings;
  }

  public Claims addMappingsItem(ClaimMapping mappingsItem) {
    if (this.mappings == null) {
      this.mappings = new ArrayList<>();
    }
    this.mappings.add(mappingsItem);
    return this;
  }


  /**
   **/
  public Claims provisioningClaims(List<ProvisioningClaim> provisioningClaims) {
    this.provisioningClaims = provisioningClaims;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("provisioningClaims")
  @Valid
  public List<ProvisioningClaim> getProvisioningClaims() {
    return provisioningClaims;
  }
  public void setProvisioningClaims(List<ProvisioningClaim> provisioningClaims) {
    this.provisioningClaims = provisioningClaims;
  }

  public Claims addProvisioningClaimsItem(ProvisioningClaim provisioningClaimsItem) {
    if (this.provisioningClaims == null) {
      this.provisioningClaims = new ArrayList<>();
    }
    this.provisioningClaims.add(provisioningClaimsItem);
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
    Claims claims = (Claims) o;
    return Objects.equals(this.userIdClaim, claims.userIdClaim) &&
        Objects.equals(this.roleClaim, claims.roleClaim) &&
        Objects.equals(this.mappings, claims.mappings) &&
        Objects.equals(this.provisioningClaims, claims.provisioningClaims);
  }

  @Override
  public int hashCode() {
    return Objects.hash(userIdClaim, roleClaim, mappings, provisioningClaims);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Claims {\n");

    sb.append("    userIdClaim: ").append(toIndentedString(userIdClaim)).append("\n");
    sb.append("    roleClaim: ").append(toIndentedString(roleClaim)).append("\n");
    sb.append("    mappings: ").append(toIndentedString(mappings)).append("\n");
    sb.append("    provisioningClaims: ").append(toIndentedString(provisioningClaims)).append("\n");
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
    return o.toString().replace("\n", "\n    ");
  }
}


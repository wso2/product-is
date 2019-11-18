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
import java.util.Objects;


public class ClaimMapping   {
  
  private String idpClaim;

  private Claim localClaim;


  /**
   **/
  public ClaimMapping idpClaim(String idpClaim) {
    this.idpClaim = idpClaim;
    return this;
  }

  
  @ApiModelProperty(example = "country", value = "")
  @JsonProperty("idpClaim")
  @Valid
  public String getIdpClaim() {
    return idpClaim;
  }
  public void setIdpClaim(String idpClaim) {
    this.idpClaim = idpClaim;
  }


  /**
   **/
  public ClaimMapping localClaim(Claim localClaim) {
    this.localClaim = localClaim;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("localClaim")
  @Valid
  public Claim getLocalClaim() {
    return localClaim;
  }
  public void setLocalClaim(Claim localClaim) {
    this.localClaim = localClaim;
  }



  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ClaimMapping claimMapping = (ClaimMapping) o;
    return Objects.equals(this.idpClaim, claimMapping.idpClaim) &&
        Objects.equals(this.localClaim, claimMapping.localClaim);
  }

  @Override
  public int hashCode() {
    return Objects.hash(idpClaim, localClaim);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ClaimMapping {\n");

    sb.append("    idpClaim: ").append(toIndentedString(idpClaim)).append("\n");
    sb.append("    localClaim: ").append(toIndentedString(localClaim)).append("\n");
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


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


public class Roles   {
  
  private List<RoleMapping> mappings = null;

  private List<String> outboundProvisioningRoles = null;


  /**
   **/
  public Roles mappings(List<RoleMapping> mappings) {
    this.mappings = mappings;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("mappings")
  @Valid
  public List<RoleMapping> getMappings() {
    return mappings;
  }
  public void setMappings(List<RoleMapping> mappings) {
    this.mappings = mappings;
  }

  public Roles addMappingsItem(RoleMapping mappingsItem) {
    if (this.mappings == null) {
      this.mappings = new ArrayList<>();
    }
    this.mappings.add(mappingsItem);
    return this;
  }


  /**
   **/
  public Roles outboundProvisioningRoles(List<String> outboundProvisioningRoles) {
    this.outboundProvisioningRoles = outboundProvisioningRoles;
    return this;
  }

  
  @ApiModelProperty(example = "[\"manager\",\"hr-admin\"]", value = "")
  @JsonProperty("outboundProvisioningRoles")
  @Valid
  public List<String> getOutboundProvisioningRoles() {
    return outboundProvisioningRoles;
  }
  public void setOutboundProvisioningRoles(List<String> outboundProvisioningRoles) {
    this.outboundProvisioningRoles = outboundProvisioningRoles;
  }

  public Roles addOutboundProvisioningRolesItem(String outboundProvisioningRolesItem) {
    if (this.outboundProvisioningRoles == null) {
      this.outboundProvisioningRoles = new ArrayList<>();
    }
    this.outboundProvisioningRoles.add(outboundProvisioningRolesItem);
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
    Roles roles = (Roles) o;
    return Objects.equals(this.mappings, roles.mappings) &&
        Objects.equals(this.outboundProvisioningRoles, roles.outboundProvisioningRoles);
  }

  @Override
  public int hashCode() {
    return Objects.hash(mappings, outboundProvisioningRoles);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Roles {\n");

    sb.append("    mappings: ").append(toIndentedString(mappings)).append("\n");
    sb.append("    outboundProvisioningRoles: ").append(toIndentedString(outboundProvisioningRoles)).append("\n");
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


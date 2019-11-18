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


public class ProvisioningResponse   {
  
  private JustInTimeProvisioning jit;

  private OutboundConnectorListResponse outboundConnectors;


  /**
   **/
  public ProvisioningResponse jit(JustInTimeProvisioning jit) {
    this.jit = jit;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("jit")
  @Valid
  public JustInTimeProvisioning getJit() {
    return jit;
  }
  public void setJit(JustInTimeProvisioning jit) {
    this.jit = jit;
  }


  /**
   **/
  public ProvisioningResponse outboundConnectors(OutboundConnectorListResponse outboundConnectors) {
    this.outboundConnectors = outboundConnectors;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("outboundConnectors")
  @Valid
  public OutboundConnectorListResponse getOutboundConnectors() {
    return outboundConnectors;
  }
  public void setOutboundConnectors(OutboundConnectorListResponse outboundConnectors) {
    this.outboundConnectors = outboundConnectors;
  }



  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ProvisioningResponse provisioningResponse = (ProvisioningResponse) o;
    return Objects.equals(this.jit, provisioningResponse.jit) &&
        Objects.equals(this.outboundConnectors, provisioningResponse.outboundConnectors);
  }

  @Override
  public int hashCode() {
    return Objects.hash(jit, outboundConnectors);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ProvisioningResponse {\n");

    sb.append("    jit: ").append(toIndentedString(jit)).append("\n");
    sb.append("    outboundConnectors: ").append(toIndentedString(outboundConnectors)).append("\n");
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


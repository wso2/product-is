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

package org.wso2.identity.integration.test.rest.api.server.application.management.v1.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;
import javax.validation.Valid;

public class InboundSCIMProvisioningConfiguration  {
  
    private Boolean proxyMode;
    private String provisioningUserstoreDomain;

    /**
    **/
    public InboundSCIMProvisioningConfiguration proxyMode(Boolean proxyMode) {

        this.proxyMode = proxyMode;
        return this;
    }
    
    @ApiModelProperty(example = "false", value = "")
    @JsonProperty("proxy-mode")
    @Valid
    public Boolean getProxyMode() {
        return proxyMode;
    }
    public void setProxyMode(Boolean proxyMode) {
        this.proxyMode = proxyMode;
    }

    /**
    * This property becomes only applicable if the proxy-mode config is set to false
    **/
    public InboundSCIMProvisioningConfiguration provisioningUserstoreDomain(String provisioningUserstoreDomain) {

        this.provisioningUserstoreDomain = provisioningUserstoreDomain;
        return this;
    }
    
    @ApiModelProperty(example = "PRIMARY", value = "This property becomes only applicable if the proxy-mode config is set to false")
    @JsonProperty("provisioningUserstoreDomain")
    @Valid
    public String getProvisioningUserstoreDomain() {
        return provisioningUserstoreDomain;
    }
    public void setProvisioningUserstoreDomain(String provisioningUserstoreDomain) {
        this.provisioningUserstoreDomain = provisioningUserstoreDomain;
    }



    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        InboundSCIMProvisioningConfiguration inboundSCIMProvisioningConfiguration = (InboundSCIMProvisioningConfiguration) o;
        return Objects.equals(this.proxyMode, inboundSCIMProvisioningConfiguration.proxyMode) &&
            Objects.equals(this.provisioningUserstoreDomain, inboundSCIMProvisioningConfiguration.provisioningUserstoreDomain);
    }

    @Override
    public int hashCode() {
        return Objects.hash(proxyMode, provisioningUserstoreDomain);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class InboundSCIMProvisioningConfiguration {\n");

        sb.append("    proxyMode: ").append(toIndentedString(proxyMode)).append("\n");
        sb.append("    provisioningUserstoreDomain: ").append(toIndentedString(provisioningUserstoreDomain)).append("\n");
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


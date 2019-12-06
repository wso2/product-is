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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.validation.Valid;

public class ProvisioningConfiguration  {
  
    private InboundSCIMProvisioningConfiguration inboundProvisioning;
    private List<OutboundProvisioningConfiguration> outboundProvisioningIdps = null;


    /**
    **/
    public ProvisioningConfiguration inboundProvisioning(InboundSCIMProvisioningConfiguration inboundProvisioning) {

        this.inboundProvisioning = inboundProvisioning;
        return this;
    }
    
    @ApiModelProperty(value = "")
    @JsonProperty("inboundProvisioning")
    @Valid
    public InboundSCIMProvisioningConfiguration getInboundProvisioning() {
        return inboundProvisioning;
    }
    public void setInboundProvisioning(InboundSCIMProvisioningConfiguration inboundProvisioning) {
        this.inboundProvisioning = inboundProvisioning;
    }

    /**
    **/
    public ProvisioningConfiguration outboundProvisioningIdps(List<OutboundProvisioningConfiguration> outboundProvisioningIdps) {

        this.outboundProvisioningIdps = outboundProvisioningIdps;
        return this;
    }
    
    @ApiModelProperty(value = "")
    @JsonProperty("outboundProvisioningIdps")
    @Valid
    public List<OutboundProvisioningConfiguration> getOutboundProvisioningIdps() {
        return outboundProvisioningIdps;
    }
    public void setOutboundProvisioningIdps(List<OutboundProvisioningConfiguration> outboundProvisioningIdps) {
        this.outboundProvisioningIdps = outboundProvisioningIdps;
    }

    public ProvisioningConfiguration addOutboundProvisioningIdpsItem(OutboundProvisioningConfiguration outboundProvisioningIdpsItem) {
        if (this.outboundProvisioningIdps == null) {
            this.outboundProvisioningIdps = new ArrayList<>();
        }
        this.outboundProvisioningIdps.add(outboundProvisioningIdpsItem);
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
        ProvisioningConfiguration provisioningConfiguration = (ProvisioningConfiguration) o;
        return Objects.equals(this.inboundProvisioning, provisioningConfiguration.inboundProvisioning) &&
            Objects.equals(this.outboundProvisioningIdps, provisioningConfiguration.outboundProvisioningIdps);
    }

    @Override
    public int hashCode() {
        return Objects.hash(inboundProvisioning, outboundProvisioningIdps);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class ProvisioningConfiguration {\n");

        sb.append("    inboundProvisioning: ").append(toIndentedString(inboundProvisioning)).append("\n");
        sb.append("    outboundProvisioningIdps: ").append(toIndentedString(outboundProvisioningIdps)).append("\n");
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


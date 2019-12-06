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

public class OutboundProvisioningConfiguration  {
  
    private String idp;
    private String connector;
    private Boolean blocking;
    private Boolean rules;
    private Boolean jit;

    /**
    **/
    public OutboundProvisioningConfiguration idp(String idp) {

        this.idp = idp;
        return this;
    }
    
    @ApiModelProperty(example = "Google", value = "")
    @JsonProperty("idp")
    @Valid
    public String getIdp() {
        return idp;
    }
    public void setIdp(String idp) {
        this.idp = idp;
    }

    /**
    **/
    public OutboundProvisioningConfiguration connector(String connector) {

        this.connector = connector;
        return this;
    }
    
    @ApiModelProperty(example = "googleapps", value = "")
    @JsonProperty("connector")
    @Valid
    public String getConnector() {
        return connector;
    }
    public void setConnector(String connector) {
        this.connector = connector;
    }

    /**
    **/
    public OutboundProvisioningConfiguration blocking(Boolean blocking) {

        this.blocking = blocking;
        return this;
    }
    
    @ApiModelProperty(example = "false", value = "")
    @JsonProperty("blocking")
    @Valid
    public Boolean getBlocking() {
        return blocking;
    }
    public void setBlocking(Boolean blocking) {
        this.blocking = blocking;
    }

    /**
    **/
    public OutboundProvisioningConfiguration rules(Boolean rules) {

        this.rules = rules;
        return this;
    }
    
    @ApiModelProperty(example = "false", value = "")
    @JsonProperty("rules")
    @Valid
    public Boolean getRules() {
        return rules;
    }
    public void setRules(Boolean rules) {
        this.rules = rules;
    }

    /**
    **/
    public OutboundProvisioningConfiguration jit(Boolean jit) {

        this.jit = jit;
        return this;
    }
    
    @ApiModelProperty(example = "false", value = "")
    @JsonProperty("jit")
    @Valid
    public Boolean getJit() {
        return jit;
    }
    public void setJit(Boolean jit) {
        this.jit = jit;
    }



    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        OutboundProvisioningConfiguration outboundProvisioningConfiguration = (OutboundProvisioningConfiguration) o;
        return Objects.equals(this.idp, outboundProvisioningConfiguration.idp) &&
            Objects.equals(this.connector, outboundProvisioningConfiguration.connector) &&
            Objects.equals(this.blocking, outboundProvisioningConfiguration.blocking) &&
            Objects.equals(this.rules, outboundProvisioningConfiguration.rules) &&
            Objects.equals(this.jit, outboundProvisioningConfiguration.jit);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idp, connector, blocking, rules, jit);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class OutboundProvisioningConfiguration {\n");

        sb.append("    idp: ").append(toIndentedString(idp)).append("\n");
        sb.append("    connector: ").append(toIndentedString(connector)).append("\n");
        sb.append("    blocking: ").append(toIndentedString(blocking)).append("\n");
        sb.append("    rules: ").append(toIndentedString(rules)).append("\n");
        sb.append("    jit: ").append(toIndentedString(jit)).append("\n");
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


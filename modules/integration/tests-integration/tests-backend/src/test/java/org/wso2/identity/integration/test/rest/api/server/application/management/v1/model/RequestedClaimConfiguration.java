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

public class RequestedClaimConfiguration  {
  
    private String claimUri;
    private Boolean mandatory;

    /**
    * User claims that need to be sent back to the application. If the claim mappings are local, use local claim uris. If the custom claim mappings are configured, use the mapped applicationClaimUri
    **/
    public RequestedClaimConfiguration claimUri(String claimUri) {

        this.claimUri = claimUri;
        return this;
    }
    
    @ApiModelProperty(example = "http://wso2.org/claims/givenname", value = "User claims that need to be sent back to the application. If the claim mappings are local, use local claim uris. If the custom claim mappings are configured, use the mapped applicationClaimUri")
    @JsonProperty("claimUri")
    @Valid
    public String getClaimUri() {
        return claimUri;
    }
    public void setClaimUri(String claimUri) {
        this.claimUri = claimUri;
    }

    /**
    **/
    public RequestedClaimConfiguration mandatory(Boolean mandatory) {

        this.mandatory = mandatory;
        return this;
    }
    
    @ApiModelProperty(example = "false", value = "")
    @JsonProperty("mandatory")
    @Valid
    public Boolean getMandatory() {
        return mandatory;
    }
    public void setMandatory(Boolean mandatory) {
        this.mandatory = mandatory;
    }



    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RequestedClaimConfiguration requestedClaimConfiguration = (RequestedClaimConfiguration) o;
        return Objects.equals(this.claimUri, requestedClaimConfiguration.claimUri) &&
            Objects.equals(this.mandatory, requestedClaimConfiguration.mandatory);
    }

    @Override
    public int hashCode() {
        return Objects.hash(claimUri, mandatory);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class RequestedClaimConfiguration {\n");

        sb.append("    claimUri: ").append(toIndentedString(claimUri)).append("\n");
        sb.append("    mandatory: ").append(toIndentedString(mandatory)).append("\n");
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


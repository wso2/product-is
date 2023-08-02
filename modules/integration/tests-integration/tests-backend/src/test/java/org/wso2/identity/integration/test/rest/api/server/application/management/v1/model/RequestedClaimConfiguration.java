/*
 * Copyright (c) 2019, WSO2 LLC. (http://www.wso2.com).
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

import java.util.Objects;
import javax.validation.Valid;

public class RequestedClaimConfiguration  {
  
    private Claim claim;
    private Boolean mandatory = false;

    /**
    * User claims that need to be sent back to the application. If the claim mappings are local, use local claim uris. If the custom claim mappings are configured, use the mapped applicationClaimUri
    **/
    public RequestedClaimConfiguration claim(Claim claim) {

        this.claim = claim;
        return this;
    }
    
    @ApiModelProperty(example = "http://wso2.org/claims/givenname", value = "User claims that need to be sent back to the application. If the claim mappings are local, use local claim uris. If the custom claim mappings are configured, use the mapped applicationClaimUri")
    @JsonProperty("claim")
    @Valid
    public Claim getClaim() {
        return claim;
    }
    public void setClaim(Claim claim) {
        this.claim = claim;
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
        return Objects.equals(this.claim, requestedClaimConfiguration.claim) &&
            Objects.equals(this.mandatory, requestedClaimConfiguration.mandatory);
    }

    @Override
    public int hashCode() {
        return Objects.hash(claim, mandatory);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class RequestedClaimConfiguration {\n");

        sb.append("    claim: ").append(toIndentedString(claim)).append("\n");
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


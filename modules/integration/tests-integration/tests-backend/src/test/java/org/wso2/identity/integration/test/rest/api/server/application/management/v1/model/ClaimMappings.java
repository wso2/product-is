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

public class ClaimMappings  {
  
    private String applicationClaimUri;
    private String localClaimUri;

    /**
    **/
    public ClaimMappings applicationClaimUri(String applicationClaimUri) {

        this.applicationClaimUri = applicationClaimUri;
        return this;
    }
    
    @ApiModelProperty(example = "firstname", value = "")
    @JsonProperty("applicationClaimUri")
    @Valid
    public String getApplicationClaimUri() {
        return applicationClaimUri;
    }
    public void setApplicationClaimUri(String applicationClaimUri) {
        this.applicationClaimUri = applicationClaimUri;
    }

    /**
    **/
    public ClaimMappings localClaimUri(String localClaimUri) {

        this.localClaimUri = localClaimUri;
        return this;
    }
    
    @ApiModelProperty(example = "http://wso2.org/claims/givenname", value = "")
    @JsonProperty("localClaimUri")
    @Valid
    public String getLocalClaimUri() {
        return localClaimUri;
    }
    public void setLocalClaimUri(String localClaimUri) {
        this.localClaimUri = localClaimUri;
    }



    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ClaimMappings claimMappings = (ClaimMappings) o;
        return Objects.equals(this.applicationClaimUri, claimMappings.applicationClaimUri) &&
            Objects.equals(this.localClaimUri, claimMappings.localClaimUri);
    }

    @Override
    public int hashCode() {
        return Objects.hash(applicationClaimUri, localClaimUri);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class ClaimMappings {\n");

        sb.append("    applicationClaimUri: ").append(toIndentedString(applicationClaimUri)).append("\n");
        sb.append("    localClaimUri: ").append(toIndentedString(localClaimUri)).append("\n");
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


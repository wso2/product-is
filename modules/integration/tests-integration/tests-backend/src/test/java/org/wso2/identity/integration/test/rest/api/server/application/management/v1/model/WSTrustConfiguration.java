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
import javax.validation.constraints.NotNull;

public class WSTrustConfiguration  {
  
    private String audience;
    private String certificateAlias;

    /**
    * Audience value of the trusted service
    **/
    public WSTrustConfiguration audience(String audience) {

        this.audience = audience;
        return this;
    }
    
    @ApiModelProperty(example = "https://wstrust.endpoint.com", required = true, value = "Audience value of the trusted service")
    @JsonProperty("audience")
    @Valid
    @NotNull(message = "Property audience cannot be null.")

    public String getAudience() {
        return audience;
    }
    public void setAudience(String audience) {
        this.audience = audience;
    }

    /**
    **/
    public WSTrustConfiguration certificateAlias(String certificateAlias) {

        this.certificateAlias = certificateAlias;
        return this;
    }
    
    @ApiModelProperty(example = "wso2carbon", required = true, value = "")
    @JsonProperty("certificateAlias")
    @Valid
    @NotNull(message = "Property certificateAlias cannot be null.")

    public String getCertificateAlias() {
        return certificateAlias;
    }
    public void setCertificateAlias(String certificateAlias) {
        this.certificateAlias = certificateAlias;
    }



    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        WSTrustConfiguration wsTrustConfiguration = (WSTrustConfiguration) o;
        return Objects.equals(this.audience, wsTrustConfiguration.audience) &&
            Objects.equals(this.certificateAlias, wsTrustConfiguration.certificateAlias);
    }

    @Override
    public int hashCode() {
        return Objects.hash(audience, certificateAlias);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class WSTrustConfiguration {\n");

        sb.append("    audience: ").append(toIndentedString(audience)).append("\n");
        sb.append("    certificateAlias: ").append(toIndentedString(certificateAlias)).append("\n");
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


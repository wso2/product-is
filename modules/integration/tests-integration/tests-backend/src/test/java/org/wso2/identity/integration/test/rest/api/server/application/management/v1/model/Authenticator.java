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

public class Authenticator {
  
    private String idp;
    private String authenticator;

    /**
    **/
    public Authenticator idp(String idp) {

        this.idp = idp;
        return this;
    }
    
    @ApiModelProperty(example = "LOCAL", required = true, value = "")
    @JsonProperty("idp")
    @Valid
    @NotNull(message = "Property idp cannot be null.")

    public String getIdp() {
        return idp;
    }
    public void setIdp(String idp) {
        this.idp = idp;
    }

    /**
    **/
    public Authenticator authenticator(String authenticator) {

        this.authenticator = authenticator;
        return this;
    }
    
    @ApiModelProperty(example = "basic", required = true, value = "")
    @JsonProperty("authenticator")
    @Valid
    @NotNull(message = "Property authenticator cannot be null.")

    public String getAuthenticator() {
        return authenticator;
    }
    public void setAuthenticator(String authenticator) {
        this.authenticator = authenticator;
    }



    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Authenticator authenticator = (Authenticator) o;
        return Objects.equals(this.idp, authenticator.idp) &&
            Objects.equals(this.authenticator, authenticator.authenticator);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idp, authenticator);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class Authenticator {\n");

        sb.append("    idp: ").append(toIndentedString(idp)).append("\n");
        sb.append("    authenticator: ").append(toIndentedString(authenticator)).append("\n");
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


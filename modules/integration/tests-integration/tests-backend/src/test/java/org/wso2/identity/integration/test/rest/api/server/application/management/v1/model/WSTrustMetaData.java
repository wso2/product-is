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

public class WSTrustMetaData  {
  
    private MetadataProperty certificateAlias;

    /**
    **/
    public WSTrustMetaData certificateAlias(MetadataProperty certificateAlias) {

        this.certificateAlias = certificateAlias;
        return this;
    }
    
    @ApiModelProperty(value = "")
    @JsonProperty("certificateAlias")
    @Valid
    public MetadataProperty getCertificateAlias() {
        return certificateAlias;
    }
    public void setCertificateAlias(MetadataProperty certificateAlias) {
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
        WSTrustMetaData wsTrustMetaData = (WSTrustMetaData) o;
        return Objects.equals(this.certificateAlias, wsTrustMetaData.certificateAlias);
    }

    @Override
    public int hashCode() {
        return Objects.hash(certificateAlias);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class WSTrustMetaData {\n");

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


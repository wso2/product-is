/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.identity.integration.test.rest.api.server.claim.management.v1.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.Valid;
import java.util.Objects;

public class ExternalClaimReq {

    private String claimURI;
    private String mappedLocalClaimURI;

    /**
     **/
    public ExternalClaimReq claimURI(String claimURI) {

        this.claimURI = claimURI;
        return this;
    }

    @ApiModelProperty(example = "http://updateddummy.org/claim/emailaddress")
    @JsonProperty("claimURI")
    @Valid
    public String getClaimURI() {
        return claimURI;
    }
    public void setClaimURI(String claimURI) {
        this.claimURI = claimURI;
    }

    /**
     **/
    public ExternalClaimReq mappedLocalClaimURI(String mappedLocalClaimURI) {

        this.mappedLocalClaimURI = mappedLocalClaimURI;
        return this;
    }

    @ApiModelProperty(example = "http://wso2.org/claims/emailaddress")
    @JsonProperty("mappedLocalClaimURI")
    @Valid
    public String getMappedLocalClaimURI() {
        return mappedLocalClaimURI;
    }
    public void setMappedLocalClaimURI(String mappedLocalClaimURI) {
        this.mappedLocalClaimURI = mappedLocalClaimURI;
    }



    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ExternalClaimReq externalClaimReq = (ExternalClaimReq) o;
        return Objects.equals(this.claimURI, externalClaimReq.claimURI) &&
                Objects.equals(this.mappedLocalClaimURI, externalClaimReq.mappedLocalClaimURI);
    }

    @Override
    public int hashCode() {
        return Objects.hash(claimURI, mappedLocalClaimURI);
    }

    @Override
    public String toString() {

        return "class ExternalClaimReq {\n" +
                "    claimURI: " + toIndentedString(claimURI) + "\n" +
                "    mappedLocalClaimURI: " + toIndentedString(mappedLocalClaimURI) + "\n" +
                "}";
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

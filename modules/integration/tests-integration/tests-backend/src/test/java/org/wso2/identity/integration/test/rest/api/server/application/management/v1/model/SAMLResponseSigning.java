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

package org.wso2.identity.integration.test.rest.api.server.application.management.v1.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.Valid;
import java.util.Objects;

public class SAMLResponseSigning {
    private Boolean enabled = true;
    private String signingAlgorithm;

    /**
     *
     **/
    public SAMLResponseSigning enabled(Boolean enabled) {

        this.enabled = enabled;
        return this;
    }

    @ApiModelProperty(example = "false")
    @JsonProperty("enabled")
    @Valid
    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    /**
     *
     **/
    public SAMLResponseSigning signingAlgorithm(String signingAlgorithm) {

        this.signingAlgorithm = signingAlgorithm;
        return this;
    }

    @ApiModelProperty()
    @JsonProperty("signingAlgorithm")
    @Valid
    public String getSigningAlgorithm() {
        return signingAlgorithm;
    }

    public void setSigningAlgorithm(String signingAlgorithm) {
        this.signingAlgorithm = signingAlgorithm;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SAMLResponseSigning samlResponseSigning = (SAMLResponseSigning) o;
        return Objects.equals(this.enabled, samlResponseSigning.enabled) &&
                Objects.equals(this.signingAlgorithm, samlResponseSigning.signingAlgorithm);
    }

    @Override
    public int hashCode() {
        return Objects.hash(enabled, signingAlgorithm);
    }

    @Override
    public String toString() {

        return "class SAMLResponseSigning {\n" +
                "    enabled: " + toIndentedString(enabled) + "\n" +
                "    signingAlgorithm: " + toIndentedString(signingAlgorithm) + "\n" +
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
        return o.toString();

    }
}

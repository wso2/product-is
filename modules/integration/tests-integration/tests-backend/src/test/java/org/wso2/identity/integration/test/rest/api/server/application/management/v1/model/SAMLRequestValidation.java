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

public class SAMLRequestValidation {
    private Boolean enableSignatureValidation = true;
    private String signatureValidationCertAlias;

    /**
     *
     **/
    public SAMLRequestValidation enableSignatureValidation(Boolean enableSignatureValidation) {

        this.enableSignatureValidation = enableSignatureValidation;
        return this;
    }

    @ApiModelProperty(example = "false")
    @JsonProperty("enableSignatureValidation")
    @Valid
    public Boolean getEnableSignatureValidation() {
        return enableSignatureValidation;
    }

    public void setEnableSignatureValidation(Boolean enableSignatureValidation) {
        this.enableSignatureValidation = enableSignatureValidation;
    }

    /**
     *
     **/
    public SAMLRequestValidation signatureValidationCertAlias(String signatureValidationCertAlias) {

        this.signatureValidationCertAlias = signatureValidationCertAlias;
        return this;
    }

    @ApiModelProperty()
    @JsonProperty("signatureValidationCertAlias")
    @Valid
    public String getSignatureValidationCertAlias() {
        return signatureValidationCertAlias;
    }

    public void setSignatureValidationCertAlias(String signatureValidationCertAlias) {
        this.signatureValidationCertAlias = signatureValidationCertAlias;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SAMLRequestValidation samlRequestValidation = (SAMLRequestValidation) o;
        return Objects.equals(this.enableSignatureValidation, samlRequestValidation.enableSignatureValidation) &&
                Objects.equals(this.signatureValidationCertAlias, samlRequestValidation.signatureValidationCertAlias);
    }

    @Override
    public int hashCode() {
        return Objects.hash(enableSignatureValidation, signatureValidationCertAlias);
    }

    @Override
    public String toString() {

        return "class SAMLRequestValidation {\n" +
                "    enableSignatureValidation: " + toIndentedString(enableSignatureValidation) + "\n" +
                "    signatureValidationCertAlias: " + toIndentedString(signatureValidationCertAlias) + "\n" +
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

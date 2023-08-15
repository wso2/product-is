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

package org.wso2.identity.integration.test.rest.api.server.keystore.management.v1.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Objects;

public class CertificateRequest {

    private String alias;
    private String certificate;

    /**
     **/
    public CertificateRequest alias(String alias) {

        this.alias = alias;
        return this;
    }

    @ApiModelProperty(example = "newcert", required = true)
    @JsonProperty("alias")
    @Valid
    @NotNull(message = "Property alias cannot be null.")

    public String getAlias() {
        return alias;
    }
    public void setAlias(String alias) {
        this.alias = alias;
    }

    /**
     **/
    public CertificateRequest certificate(String certificate) {

        this.certificate = certificate;
        return this;
    }

    @ApiModelProperty(required = true)
    @JsonProperty("certificate")
    @Valid
    @NotNull(message = "Property certificate cannot be null.")

    public String getCertificate() {
        return certificate;
    }
    public void setCertificate(String certificate) {
        this.certificate = certificate;
    }



    @Override
    public boolean equals(java.lang.Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CertificateRequest certificateRequest = (CertificateRequest) o;
        return Objects.equals(this.alias, certificateRequest.alias) &&
                Objects.equals(this.certificate, certificateRequest.certificate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(alias, certificate);
    }

    @Override
    public String toString() {

        return "class CertificateRequest {\n" +
                "    alias: " + toIndentedString(alias) + "\n" +
                "    certificate: " + toIndentedString(certificate) + "\n" +
                "}";
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(java.lang.Object o) {

        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n");
    }
}

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

import java.util.Objects;
import javax.validation.Valid;

public class FapiMetadata {

    private MetadataProperty allowedSignatureAlgorithms;
    private MetadataProperty allowedEncryptionAlgorithms;
    private ClientAuthenticationMethodMetadata tokenEndpointAuthMethod;

    /**
     *
     **/
    public FapiMetadata allowedSignatureAlgorithms(MetadataProperty allowedSignatureAlgorithms) {

        this.allowedSignatureAlgorithms = allowedSignatureAlgorithms;
        return this;
    }

    @ApiModelProperty(value = "")
    @JsonProperty("allowedSignatureAlgorithms")
    @Valid
    public MetadataProperty getAllowedSignatureAlgorithms() {

        return allowedSignatureAlgorithms;
    }

    public void setAllowedSignatureAlgorithms(MetadataProperty allowedSignatureAlgorithms) {

        this.allowedSignatureAlgorithms = allowedSignatureAlgorithms;
    }

    /**
     *
     **/
    public FapiMetadata allowedEncryptionAlgorithms(MetadataProperty allowedEncryptionAlgorithms) {

        this.allowedEncryptionAlgorithms = allowedEncryptionAlgorithms;
        return this;
    }

    @ApiModelProperty(value = "")
    @JsonProperty("allowedEncryptionAlgorithms")
    @Valid
    public MetadataProperty getAllowedEncryptionAlgorithms() {

        return allowedEncryptionAlgorithms;
    }

    public void setAllowedEncryptionAlgorithms(MetadataProperty allowedEncryptionAlgorithms) {

        this.allowedEncryptionAlgorithms = allowedEncryptionAlgorithms;
    }

    /**
     *
     **/
    public FapiMetadata tokenEndpointAuthMethod(ClientAuthenticationMethodMetadata tokenEndpointAuthMethod) {

        this.tokenEndpointAuthMethod = tokenEndpointAuthMethod;
        return this;
    }

    @ApiModelProperty(value = "")
    @JsonProperty("tokenEndpointAuthMethod")
    @Valid
    public ClientAuthenticationMethodMetadata getTokenEndpointAuthMethod() {

        return tokenEndpointAuthMethod;
    }

    public void setTokenEndpointAuthMethod(ClientAuthenticationMethodMetadata tokenEndpointAuthMethod) {

        this.tokenEndpointAuthMethod = tokenEndpointAuthMethod;
    }

    @Override
    public boolean equals(java.lang.Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FapiMetadata fapiMetadata = (FapiMetadata) o;
        return Objects.equals(this.allowedSignatureAlgorithms, fapiMetadata.allowedSignatureAlgorithms) &&
                Objects.equals(this.allowedEncryptionAlgorithms, fapiMetadata.allowedEncryptionAlgorithms) &&
                Objects.equals(this.tokenEndpointAuthMethod, fapiMetadata.tokenEndpointAuthMethod);
    }

    @Override
    public int hashCode() {

        return Objects.hash(allowedSignatureAlgorithms, allowedEncryptionAlgorithms, tokenEndpointAuthMethod);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class FapiMetadata {\n");

        sb.append("    allowedSignatureAlgorithms: ").append(toIndentedString(allowedSignatureAlgorithms)).append("\n");
        sb.append("    allowedEncryptionAlgorithms: ").append(toIndentedString(allowedEncryptionAlgorithms))
                .append("\n");
        sb.append("    tokenEndpointAuthMethod: ").append(toIndentedString(tokenEndpointAuthMethod)).append("\n");
        sb.append("}");
        return sb.toString();
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

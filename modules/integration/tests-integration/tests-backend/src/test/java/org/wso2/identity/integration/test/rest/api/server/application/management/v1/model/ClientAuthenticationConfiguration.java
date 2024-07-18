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

public class ClientAuthenticationConfiguration {

    private String tokenEndpointAuthMethod;
    private Boolean tokenEndpointAllowReusePvtKeyJwt;
    private String tokenEndpointAuthSigningAlg;
    private String tlsClientAuthSubjectDn;

    /**
     *
     **/
    public ClientAuthenticationConfiguration tokenEndpointAuthMethod(String tokenEndpointAuthMethod) {

        this.tokenEndpointAuthMethod = tokenEndpointAuthMethod;
        return this;
    }

    @ApiModelProperty(example = "true", value = "")
    @JsonProperty("tokenEndpointAuthMethod")
    @Valid
    public String getTokenEndpointAuthMethod() {
        return tokenEndpointAuthMethod;
    }

    public void setTokenEndpointAuthMethod(String tokenEndpointAuthMethod) {
        this.tokenEndpointAuthMethod = tokenEndpointAuthMethod;
    }

    /**
     *
     */
    public ClientAuthenticationConfiguration tokenEndpointAllowReusePvtKeyJwt(Boolean tokenEndpointAllowReusePvtKeyJwt) {

        this.tokenEndpointAllowReusePvtKeyJwt = tokenEndpointAllowReusePvtKeyJwt;
        return this;
    }

    @ApiModelProperty(example = "true", value = "")
    @JsonProperty("tokenEndpointAllowReusePvtKeyJwt")
    @Valid
    public Boolean getTokenEndpointAllowReusePvtKeyJwt() {

        return tokenEndpointAllowReusePvtKeyJwt;
    }

    /**
     * Sets the tokenEndpointAllowReusePvtKeyJwt.
     *
     * @param tokenEndpointAllowReusePvtKeyJwt the tokenEndpointAllowReusePvtKeyJwt
     */
    public void setTokenEndpointAllowReusePvtKeyJwt(Boolean tokenEndpointAllowReusePvtKeyJwt) {

        this.tokenEndpointAllowReusePvtKeyJwt = tokenEndpointAllowReusePvtKeyJwt;
    }

    /**
     *
     **/
    public ClientAuthenticationConfiguration tokenEndpointAuthSigningAlg(String tokenEndpointAuthSigningAlg) {

        this.tokenEndpointAuthSigningAlg = tokenEndpointAuthSigningAlg;
        return this;
    }

    @ApiModelProperty(example = "PS256", value = "")
    @JsonProperty("tokenEndpointAuthSigningAlg")
    @Valid
    public String getTokenEndpointAuthSigningAlg() {
        return tokenEndpointAuthSigningAlg;
    }

    public void setTokenEndpointAuthSigningAlg(String tokenEndpointAuthSigningAlg) {
        this.tokenEndpointAuthSigningAlg = tokenEndpointAuthSigningAlg;
    }

    /**
     *
     **/
    public ClientAuthenticationConfiguration tlsClientAuthSubjectDn(String tlsClientAuthSubjectDn) {

        this.tlsClientAuthSubjectDn = tlsClientAuthSubjectDn;
        return this;
    }

    @ApiModelProperty(example = "CN=John Doe,OU=OrgUnit,O=Organization,L=Colombo,ST=Western,C=LK", value = "")
    @JsonProperty("tlsClientAuthSubjectDn")
    @Valid
    public String getTlsClientAuthSubjectDn() {
        return tlsClientAuthSubjectDn;
    }

    public void setTlsClientAuthSubjectDn(String tlsClientAuthSubjectDn) {
        this.tlsClientAuthSubjectDn = tlsClientAuthSubjectDn;
    }

    @Override
    public boolean equals(java.lang.Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ClientAuthenticationConfiguration clientAuthenticationConfiguration = (ClientAuthenticationConfiguration) o;
        return Objects.equals(this.tokenEndpointAuthMethod, clientAuthenticationConfiguration.tokenEndpointAuthMethod) &&
                Objects.equals(this.tokenEndpointAuthSigningAlg, clientAuthenticationConfiguration.tokenEndpointAuthSigningAlg) &&
                Objects.equals(this.tlsClientAuthSubjectDn, clientAuthenticationConfiguration.tlsClientAuthSubjectDn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tokenEndpointAuthMethod, tokenEndpointAuthSigningAlg, tlsClientAuthSubjectDn);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class ClientAuthenticationConfiguration {\n");

        sb.append("    tokenEndpointAuthMethod: ").append(toIndentedString(tokenEndpointAuthMethod)).append("\n");
        sb.append("    tokenEndpointAuthSigningAlg: ").append(toIndentedString(tokenEndpointAuthSigningAlg)).append("\n");
        sb.append("    tlsClientAuthSubjectDn: ").append(toIndentedString(tlsClientAuthSubjectDn)).append("\n");
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

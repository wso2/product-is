/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.identity.integration.test.serviceextensions.common.management.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class Endpoint {

    private String uri;
    private AuthenticationType authentication;
    private List<String> allowedHeaders = null;
    private List<String> allowedParameters = null;

    /**
     **/
    public Endpoint uri(String uri) {

        this.uri = uri;
        return this;
    }

    @ApiModelProperty(example = "https://abc.com/token", required = true, value = "")
    @JsonProperty("uri")
    @Valid
    @NotNull(message = "Property uri cannot be null.")

    public String getUri() {
        return uri;
    }
    public void setUri(String uri) {
        this.uri = uri;
    }

    /**
     **/
    public Endpoint authentication(AuthenticationType authentication) {

        this.authentication = authentication;
        return this;
    }

    @ApiModelProperty(required = true, value = "")
    @JsonProperty("authentication")
    @Valid
    @NotNull(message = "Property authentication cannot be null.")

    public AuthenticationType getAuthentication() {
        return authentication;
    }
    public void setAuthentication(AuthenticationType authentication) {
        this.authentication = authentication;
    }

    /**
     * List of HTTP headers to forward to the extension.
     **/
    public Endpoint allowedHeaders(List<String> allowedHeaders) {

        this.allowedHeaders = allowedHeaders;
        return this;
    }

    @ApiModelProperty(example = "[\"x-geo-location\",\"host\"]", value = "List of HTTP headers to forward to the extension.")
    @JsonProperty("allowedHeaders")
    @Valid
    public List<String> getAllowedHeaders() {
        return allowedHeaders;
    }
    public void setAllowedHeaders(List<String> allowedHeaders) {
        this.allowedHeaders = allowedHeaders;
    }

    public Endpoint addAllowedHeadersItem(String allowedHeadersItem) {
        if (this.allowedHeaders == null) {
            this.allowedHeaders = new ArrayList<String>();
        }
        this.allowedHeaders.add(allowedHeadersItem);
        return this;
    }

    /**
     * List of parameters to forward to the extension.
     **/
    public Endpoint allowedParameters(List<String> allowedParameters) {

        this.allowedParameters = allowedParameters;
        return this;
    }

    @ApiModelProperty(example = "[\"device-id\"]", value = "List of parameters to forward to the extension.")
    @JsonProperty("allowedParameters")
    @Valid
    public List<String> getAllowedParameters() {
        return allowedParameters;
    }
    public void setAllowedParameters(List<String> allowedParameters) {
        this.allowedParameters = allowedParameters;
    }

    public Endpoint addAllowedParametersItem(String allowedParametersItem) {
        if (this.allowedParameters == null) {
            this.allowedParameters = new ArrayList<String>();
        }
        this.allowedParameters.add(allowedParametersItem);
        return this;
    }

    @Override
    public boolean equals(java.lang.Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Endpoint endpoint = (Endpoint) o;
        return Objects.equals(this.uri, endpoint.uri) &&
                Objects.equals(this.authentication, endpoint.authentication) &&
                Objects.equals(this.allowedHeaders, endpoint.allowedHeaders) &&
                Objects.equals(this.allowedParameters, endpoint.allowedParameters);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uri, authentication, allowedHeaders, allowedParameters);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class Endpoint {\n");
        sb.append("    uri: ").append(toIndentedString(uri)).append("\n");
        sb.append("    authentication: ").append(toIndentedString(authentication)).append("\n");
        sb.append("    allowedHeaders: ").append(toIndentedString(allowedHeaders)).append("\n");
        sb.append("    allowedParameters: ").append(toIndentedString(allowedParameters)).append("\n");
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
        return o.toString();
    }
}

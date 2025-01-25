/*
 * Copyright (c) 2024-2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.identity.integration.test.rest.api.server.action.management.v1.common.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;

import javax.validation.Valid;
import javax.validation.constraints.Pattern;

public class EndpointUpdateModel {

    private String uri;
    private AuthenticationType authentication;

    /**
     **/
    public EndpointUpdateModel uri(String uri) {

        this.uri = uri;
        return this;
    }

    @ApiModelProperty(example = "https://abc.com/token", value = "")
    @JsonProperty("uri")
    @Valid
    @Pattern(regexp="^https?://.+")
    public String getUri() {
        return uri;
    }
    public void setUri(String uri) {
        this.uri = uri;
    }

    /**
     **/
    public EndpointUpdateModel authentication(AuthenticationType authentication) {

        this.authentication = authentication;
        return this;
    }

    @ApiModelProperty(value = "")
    @JsonProperty("authentication")
    @Valid
    public AuthenticationType getAuthentication() {
        return authentication;
    }
    public void setAuthentication(AuthenticationType authentication) {
        this.authentication = authentication;
    }

    @Override
    public boolean equals(java.lang.Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        EndpointUpdateModel endpointUpdateModel = (EndpointUpdateModel) o;
        return Objects.equals(this.uri, endpointUpdateModel.uri) &&
                Objects.equals(this.authentication, endpointUpdateModel.authentication);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uri, authentication);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class EndpointUpdateModel {\n");

        sb.append("    uri: ").append(toIndentedString(uri)).append("\n");
        sb.append("    authentication: ").append(toIndentedString(authentication)).append("\n");
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

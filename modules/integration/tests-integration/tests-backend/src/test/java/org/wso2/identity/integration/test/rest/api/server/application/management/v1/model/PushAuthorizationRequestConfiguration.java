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

public class PushAuthorizationRequestConfiguration {

    private Boolean requirePushAuthorizationRequest;

    /**
     **/
    public PushAuthorizationRequestConfiguration requirePushAuthorizationRequest(Boolean requirePushAuthorizationRequest) {

        this.requirePushAuthorizationRequest = requirePushAuthorizationRequest;
        return this;
    }

    @ApiModelProperty(example = "false", value = "")
    @JsonProperty("requirePushAuthorizationRequest")
    @Valid
    public Boolean getRequirePushAuthorizationRequest() {
        return requirePushAuthorizationRequest;
    }
    public void setRequirePushAuthorizationRequest(Boolean requirePushAuthorizationRequest) {
        this.requirePushAuthorizationRequest = requirePushAuthorizationRequest;
    }

    @Override
    public boolean equals(java.lang.Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PushAuthorizationRequestConfiguration pushAuthorizationRequestConfiguration = (PushAuthorizationRequestConfiguration) o;
        return Objects.equals(this.requirePushAuthorizationRequest, pushAuthorizationRequestConfiguration.requirePushAuthorizationRequest);
    }

    @Override
    public int hashCode() {
        return Objects.hash(requirePushAuthorizationRequest);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class PushAuthorizationRequestConfiguration {\n");

        sb.append("    requirePushAuthorizationRequest: ").append(toIndentedString(requirePushAuthorizationRequest)).append("\n");
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

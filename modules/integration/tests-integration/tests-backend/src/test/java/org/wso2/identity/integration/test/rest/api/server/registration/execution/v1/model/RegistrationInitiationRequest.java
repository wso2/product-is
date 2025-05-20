/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.identity.integration.test.rest.api.server.registration.execution.v1.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;

import javax.validation.Valid;

public class RegistrationInitiationRequest {

    private String applicationId;
    private String callbackUrl;

    /**
     * Unique identifier for the application
     **/
    public RegistrationInitiationRequest applicationId(String applicationId) {

        this.applicationId = applicationId;
        return this;
    }

    @ApiModelProperty(example = "01afc2d2-f7b8-46db-95a9-c17336e7a1c6", value = "Unique identifier for the application")
    @JsonProperty("applicationId")
    @Valid
    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    /**
     * URL to redirect during registration if required
     **/
    public RegistrationInitiationRequest callbackUrl(String callbackUrl) {

        this.callbackUrl = callbackUrl;
        return this;
    }

    @ApiModelProperty(example = "https://localhost:3000/myRegistrationPortal", value = "URL to redirect during registration if required")
    @JsonProperty("callbackUrl")
    @Valid
    public String getCallbackUrl() {
        return callbackUrl;
    }
    public void setCallbackUrl(String callbackUrl) {
        this.callbackUrl = callbackUrl;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RegistrationInitiationRequest registrationInitiationRequest = (RegistrationInitiationRequest) o;
        return Objects.equals(this.applicationId, registrationInitiationRequest.applicationId) &&
                Objects.equals(this.callbackUrl, registrationInitiationRequest.callbackUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(applicationId, callbackUrl);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class RegistrationInitiationRequest {\n");

        sb.append("    applicationId: ").append(toIndentedString(applicationId)).append("\n");
        sb.append("    callbackUrl: ").append(toIndentedString(callbackUrl)).append("\n");
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


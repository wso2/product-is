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

public class ExternalConsentManagementConfiguration {

    private boolean enabled;
    private String consentUrl;

    /**
     * Decide whether external consent management is enabled.
     **/
    public ExternalConsentManagementConfiguration enabled(boolean enabled) {

        this.enabled = enabled;
        return this;
    }

    @ApiModelProperty(value = "Decide whether external consent management is enabled.")
    @JsonProperty("enabled")
    @Valid
    public boolean getEnabled() {
        return enabled;
    }
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Consent URL.
     **/
    public ExternalConsentManagementConfiguration consentUrl(String consentUrl) {

        this.consentUrl = consentUrl;
        return this;
    }

    @ApiModelProperty(value = "Consent URL.")
    @JsonProperty("consentUrl")
    @Valid
    public String getConsentUrl() {
        return consentUrl;
    }
    public void setConsentUrl(String consentUrl) {
        this.consentUrl = consentUrl;
    }

    @Override
    public boolean equals(java.lang.Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ExternalConsentManagementConfiguration externalConsentManagement = (ExternalConsentManagementConfiguration) o;
        return Objects.equals(this.enabled, externalConsentManagement.enabled) &&
                Objects.equals(this.consentUrl, externalConsentManagement.consentUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(enabled, consentUrl);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class ExternalConsentManagementConfiguration {\n");

        sb.append("    enabled: ").append(toIndentedString(enabled)).append("\n");
        sb.append("    consentUrl: ").append(toIndentedString(consentUrl)).append("\n");
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

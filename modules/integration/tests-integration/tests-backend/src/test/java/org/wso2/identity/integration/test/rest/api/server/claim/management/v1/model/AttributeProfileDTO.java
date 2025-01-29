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

package org.wso2.identity.integration.test.rest.api.server.claim.management.v1.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;

public class AttributeProfileDTO {

    private Boolean readOnly = null;
    private Boolean required = null;
    private Boolean supportedByDefault = null;

    @ApiModelProperty(value = "Specifies if the claim is read-only in given profile.")
    @JsonProperty("readOnly")
    public Boolean getReadOnly() {

        return readOnly;
    }

    public void setReadOnly(Boolean readOnly) {

        this.readOnly = readOnly;
    }

    @ApiModelProperty(value = "Specifies if the claim is required in given profile.")
    @JsonProperty("required")
    public Boolean getRequired() {

        return required;
    }

    public void setRequired(Boolean required) {

        this.required = required;
    }

    @ApiModelProperty(value = "Specifies if the claim will be displayed on the given profile.")
    @JsonProperty("supportedByDefault")
    public Boolean getSupportedByDefault() {

        return supportedByDefault;
    }

    public void setSupportedByDefault(Boolean supportedByDefault) {

        this.supportedByDefault = supportedByDefault;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AttributeProfileDTO attributeProfile = (AttributeProfileDTO) o;
        return Objects.equals(this.readOnly, attributeProfile.readOnly) &&
                Objects.equals(this.required, attributeProfile.required) &&
                Objects.equals(this.supportedByDefault, attributeProfile.supportedByDefault);
    }

    @Override
    public int hashCode() {

        return Objects.hash(readOnly, required, supportedByDefault);
    }

    @Override
    public String toString() {

        return "class AttributeProfileDTO {\n" +
                "    readOnly: " + toIndentedString(readOnly) + "\n" +
                "    required: " + toIndentedString(required) + "\n" +
                "    supportedByDefault: " + toIndentedString(supportedByDefault) + "\n" +
                "}\n";
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

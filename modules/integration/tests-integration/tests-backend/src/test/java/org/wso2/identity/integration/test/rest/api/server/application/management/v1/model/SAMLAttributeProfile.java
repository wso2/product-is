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

public class SAMLAttributeProfile {

    private Boolean enabled = false;
    private Boolean alwaysIncludeAttributesInResponse = false;
    private String nameFormat = "urn:oasis:names:tc:SAML:2.0:attrname-format:basic";

    /**
     *
     **/
    public SAMLAttributeProfile enabled(Boolean enabled) {

        this.enabled = enabled;
        return this;
    }

    @ApiModelProperty(example = "false")
    @JsonProperty("enabled")
    @Valid
    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    /**
     *
     **/
    public SAMLAttributeProfile alwaysIncludeAttributesInResponse(Boolean alwaysIncludeAttributesInResponse) {

        this.alwaysIncludeAttributesInResponse = alwaysIncludeAttributesInResponse;
        return this;
    }

    @ApiModelProperty()
    @JsonProperty("alwaysIncludeAttributesInResponse")
    @Valid
    public Boolean getAlwaysIncludeAttributesInResponse() {
        return alwaysIncludeAttributesInResponse;
    }

    public void setAlwaysIncludeAttributesInResponse(Boolean alwaysIncludeAttributesInResponse) {
        this.alwaysIncludeAttributesInResponse = alwaysIncludeAttributesInResponse;
    }

    /**
     * The name format of attributes in the SAML assertion attribute statement.
     **/
    public SAMLAttributeProfile nameFormat(String nameFormat) {

        this.nameFormat = nameFormat;
        return this;
    }

    @ApiModelProperty(value = "The name format of attributes in the SAML assertion attribute statement.")
    @JsonProperty("nameFormat")
    @Valid
    public String getNameFormat() {

        return nameFormat;
    }

    public void setNameFormat(String nameFormat) {

        this.nameFormat = nameFormat;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SAMLAttributeProfile samlAttributeProfile = (SAMLAttributeProfile) o;
        return Objects.equals(this.enabled, samlAttributeProfile.enabled) &&
                Objects.equals(this.alwaysIncludeAttributesInResponse,
                        samlAttributeProfile.alwaysIncludeAttributesInResponse) &&
                Objects.equals(this.nameFormat, samlAttributeProfile.nameFormat);
    }

    @Override
    public int hashCode() {
        return Objects.hash(enabled, alwaysIncludeAttributesInResponse, nameFormat);
    }

    @Override
    public String toString() {

        return "class SAMLAttributeProfile {\n" +
                "    enabled: " + toIndentedString(enabled) + "\n" +
                "    alwaysIncludeAttributesInResponse: " + toIndentedString(alwaysIncludeAttributesInResponse) + "\n" +
                "    nameFormat: " + toIndentedString(nameFormat) + "\n" +
                "}";
    }

    /**
     * Convert the given object to String with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(Object o) {

        if (o == null) {
            return "null";
        }
        return o.toString();

    }
}

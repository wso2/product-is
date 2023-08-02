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
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SingleSignOnProfile {

    @XmlType(name="BINDINGSEnum")
    @XmlEnum()
    public enum BINDINGSEnum {

        @XmlEnumValue("HTTP_POST") HTTP_POST("HTTP_POST"),
        @XmlEnumValue("HTTP_REDIRECT") HTTP_REDIRECT("HTTP_REDIRECT"),
        @XmlEnumValue("ARTIFACT") ARTIFACT("ARTIFACT");

        private final String value;

        BINDINGSEnum(String v) {
            value = v;
        }

        public String value() {
            return value;
        }

        @Override
        public String toString() {
            return String.valueOf(value);
        }

        public static BINDINGSEnum fromValue(String value) {
            for (BINDINGSEnum b : BINDINGSEnum.values()) {
                if (b.value.equals(value)) {
                    return b;
                }
            }
            throw new IllegalArgumentException("Unexpected value '" + value + "'");
        }
    }

    private List<BINDINGSEnum> bindings;
    private Boolean enableSignatureValidationForArtifactBinding = false;
    private String attributeConsumingServiceIndex;
    private Boolean enableIdpInitiatedSingleSignOn = false;
    private SAMLAssertionConfiguration assertion;

    /**
     **/
    public SingleSignOnProfile bindings(List<BINDINGSEnum> bindings) {

        this.bindings = bindings;
        return this;
    }

    @ApiModelProperty(value = "")
    @JsonProperty("bindings")
    @Valid
    public List<BINDINGSEnum> getBindings() {
        return bindings;
    }
    public void setBindings(List<BINDINGSEnum> bindings) {
        this.bindings = bindings;
    }

    public SingleSignOnProfile addBindingsItem(BINDINGSEnum bindingsItem) {
        if (this.bindings == null) {
            this.bindings = new ArrayList<>();
        }
        this.bindings.add(bindingsItem);
        return this;
    }

    /**
     *
     **/
    public SingleSignOnProfile enableSignatureValidationForArtifactBinding(Boolean
        enableSignatureValidationForArtifactBinding) {

        this.enableSignatureValidationForArtifactBinding = enableSignatureValidationForArtifactBinding;
        return this;
    }

    @ApiModelProperty(example = "false")
    @JsonProperty("enableSignatureValidationForArtifactBinding")
    @Valid
    public Boolean getEnableSignatureValidationForArtifactBinding() {
        return enableSignatureValidationForArtifactBinding;
    }

    public void setEnableSignatureValidationForArtifactBinding(Boolean enableSignatureValidationForArtifactBinding) {
        this.enableSignatureValidationForArtifactBinding = enableSignatureValidationForArtifactBinding;
    }

    /**
     *
     **/
    public SingleSignOnProfile attributeConsumingServiceIndex(String attributeConsumingServiceIndex) {

        this.attributeConsumingServiceIndex = attributeConsumingServiceIndex;
        return this;
    }

    @ApiModelProperty()
    @JsonProperty("attributeConsumingServiceIndex")
    @Valid
    public String getAttributeConsumingServiceIndex() {
        return attributeConsumingServiceIndex;
    }

    public void setAttributeConsumingServiceIndex(String attributeConsumingServiceIndex) {
        this.attributeConsumingServiceIndex = attributeConsumingServiceIndex;
    }

    /**
     *
     **/
    public SingleSignOnProfile enableIdpInitiatedSingleSignOn(Boolean enableIdpInitiatedSingleSignOn) {

        this.enableIdpInitiatedSingleSignOn = enableIdpInitiatedSingleSignOn;
        return this;
    }

    @ApiModelProperty(example = "false")
    @JsonProperty("enableIdpInitiatedSingleSignOn")
    @Valid
    public Boolean getEnableIdpInitiatedSingleSignOn() {
        return enableIdpInitiatedSingleSignOn;
    }

    public void setEnableIdpInitiatedSingleSignOn(Boolean enableIdpInitiatedSingleSignOn) {
        this.enableIdpInitiatedSingleSignOn = enableIdpInitiatedSingleSignOn;
    }


    /**
     *
     **/
    public SingleSignOnProfile assertion(SAMLAssertionConfiguration assertion) {

        this.assertion = assertion;
        return this;
    }

    @ApiModelProperty()
    @JsonProperty("assertion")
    @Valid
    public SAMLAssertionConfiguration getAssertion() {
        return assertion;
    }

    public void setAssertion(SAMLAssertionConfiguration assertion) {
        this.assertion = assertion;
    }


    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SingleSignOnProfile singleSignOnProfile = (SingleSignOnProfile) o;
        return Objects.equals(this.bindings, singleSignOnProfile.bindings) &&
                Objects.equals(this.enableSignatureValidationForArtifactBinding,
                        singleSignOnProfile.enableSignatureValidationForArtifactBinding) &&
                Objects.equals(this.attributeConsumingServiceIndex,
                        singleSignOnProfile.attributeConsumingServiceIndex) &&
                Objects.equals(this.enableIdpInitiatedSingleSignOn,
                        singleSignOnProfile.enableIdpInitiatedSingleSignOn) &&
                Objects.equals(this.assertion, singleSignOnProfile.assertion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bindings, enableSignatureValidationForArtifactBinding, attributeConsumingServiceIndex,
                enableIdpInitiatedSingleSignOn, assertion);
    }

    @Override
    public String toString() {

        return "class SingleSignOnProfile {\n" +
                "    bindings: " + toIndentedString(bindings) + "\n" +
                "    enableSignatureValidationForArtifactBinding: " +
                toIndentedString(enableSignatureValidationForArtifactBinding) + "\n" +
                "    attributeConsumingServiceIndex: " + toIndentedString(attributeConsumingServiceIndex) + "\n" +
                "    enableIdpInitiatedSingleSignOn: " + toIndentedString(enableIdpInitiatedSingleSignOn) + "\n" +
                "    assertion: " + toIndentedString(assertion) + "\n" +
                "}";
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(Object o) {

        if (o == null) {
            return "null";
        }
        return o.toString();

    }
}

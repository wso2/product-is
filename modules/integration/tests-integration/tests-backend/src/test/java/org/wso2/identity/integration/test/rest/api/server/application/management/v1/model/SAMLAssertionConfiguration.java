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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SAMLAssertionConfiguration {

    private String nameIdFormat = "urn:oasis:names:tc:SAML:1.1:nameid-format:emailAddress";
    private List<String> audiences = null;
    private List<String> recipients = null;
    private String digestAlgorithm;
    private AssertionEncryptionConfiguration encryption;

    /**
     *
     **/
    public SAMLAssertionConfiguration nameIdFormat(String nameIdFormat) {

        this.nameIdFormat = nameIdFormat;
        return this;
    }

    @ApiModelProperty()
    @JsonProperty("nameIdFormat")
    @Valid
    public String getNameIdFormat() {
        return nameIdFormat;
    }

    public void setNameIdFormat(String nameIdFormat) {
        this.nameIdFormat = nameIdFormat;
    }

    /**
     **/
    public SAMLAssertionConfiguration audiences(List<String> audiences) {

        this.audiences = audiences;
        return this;
    }

    @ApiModelProperty(value = "")
    @JsonProperty("audiences")
    @Valid
    public List<String> getAudiences() {
        return audiences;
    }
    public void setAudiences(List<String> audiences) {
        this.audiences = audiences;
    }

    public SAMLAssertionConfiguration addAudiencesItem(String audiencesItem) {
        if (this.audiences == null) {
            this.audiences = new ArrayList<>();
        }
        this.audiences.add(audiencesItem);
        return this;
    }

    /**
     **/
    public SAMLAssertionConfiguration recipients(List<String> recipients) {

        this.recipients = recipients;
        return this;
    }

    @ApiModelProperty(value = "")
    @JsonProperty("recipients")
    @Valid
    public List<String> getRecipients() {
        return recipients;
    }
    public void setRecipients(List<String> recipients) {
        this.recipients = recipients;
    }

    public SAMLAssertionConfiguration addRecipientsItem(String recipientsItem) {
        if (this.recipients == null) {
            this.recipients = new ArrayList<>();
        }
        this.recipients.add(recipientsItem);
        return this;
    }

    /**
     *
     **/
    public SAMLAssertionConfiguration digestAlgorithm(String digestAlgorithm) {

        this.digestAlgorithm = digestAlgorithm;
        return this;
    }

    @ApiModelProperty()
    @JsonProperty("digestAlgorithm")
    @Valid
    public String getDigestAlgorithm() {
        return digestAlgorithm;
    }

    public void setDigestAlgorithm(String digestAlgorithm) {
        this.digestAlgorithm = digestAlgorithm;
    }

    /**
     *
     **/
    public SAMLAssertionConfiguration encryption(AssertionEncryptionConfiguration encryption) {

        this.encryption = encryption;
        return this;
    }

    @ApiModelProperty()
    @JsonProperty("encryption")
    @Valid
    public AssertionEncryptionConfiguration getEncryption() {
        return encryption;
    }

    public void setEncryption(AssertionEncryptionConfiguration encryption) {
        this.encryption = encryption;
    }

    @Override
    public int hashCode() {
        return Objects.hash(encryption, nameIdFormat, digestAlgorithm);
    }


    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SAMLAssertionConfiguration SAMLAssertionConfiguration = (SAMLAssertionConfiguration) o;
        return Objects.equals(this.nameIdFormat, SAMLAssertionConfiguration.nameIdFormat) &&
                Objects.equals(this.audiences, SAMLAssertionConfiguration.audiences) &&
                Objects.equals(this.recipients, SAMLAssertionConfiguration.recipients) &&
                Objects.equals(this.digestAlgorithm, SAMLAssertionConfiguration.digestAlgorithm) &&
                Objects.equals(this.encryption, SAMLAssertionConfiguration.encryption);
    }

    @Override
    public String toString() {

        return "class SAMLAssertionConfiguration {\n" +
                "    nameIdFormat: " + toIndentedString(nameIdFormat) + "\n" +
                "    audiences: " + toIndentedString(audiences) + "\n" +
                "    recipients: " + toIndentedString(recipients) + "\n" +
                "    digestAlgorithm: " + toIndentedString(digestAlgorithm) + "\n" +
                "    encryption: " + toIndentedString(encryption) + "\n" +
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

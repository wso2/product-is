/*
* Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.wso2.identity.integration.test.rest.api.server.application.management.v1.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;
import javax.validation.Valid;

public class SAMLMetaData  {
  
    private String defaultNameIdFormat;
    private MetadataProperty certificateAlias;
    private MetadataProperty responseSigningAlgorithm;
    private MetadataProperty responseDigestAlgorithm;
    private MetadataProperty assertionEncryptionAlgorithm;
    private MetadataProperty keyEncryptionAlgorithm;

    /**
    **/
    public SAMLMetaData defaultNameIdFormat(String defaultNameIdFormat) {

        this.defaultNameIdFormat = defaultNameIdFormat;
        return this;
    }
    
    @ApiModelProperty(example = "urn:oasis:names:tc:SAML:1.1:nameid-format:emailAddress", value = "")
    @JsonProperty("defaultNameIdFormat")
    @Valid
    public String getDefaultNameIdFormat() {
        return defaultNameIdFormat;
    }
    public void setDefaultNameIdFormat(String defaultNameIdFormat) {
        this.defaultNameIdFormat = defaultNameIdFormat;
    }

    /**
    **/
    public SAMLMetaData certificateAlias(MetadataProperty certificateAlias) {

        this.certificateAlias = certificateAlias;
        return this;
    }
    
    @ApiModelProperty(value = "")
    @JsonProperty("certificateAlias")
    @Valid
    public MetadataProperty getCertificateAlias() {
        return certificateAlias;
    }
    public void setCertificateAlias(MetadataProperty certificateAlias) {
        this.certificateAlias = certificateAlias;
    }

    /**
    **/
    public SAMLMetaData responseSigningAlgorithm(MetadataProperty responseSigningAlgorithm) {

        this.responseSigningAlgorithm = responseSigningAlgorithm;
        return this;
    }
    
    @ApiModelProperty(value = "")
    @JsonProperty("responseSigningAlgorithm")
    @Valid
    public MetadataProperty getResponseSigningAlgorithm() {
        return responseSigningAlgorithm;
    }
    public void setResponseSigningAlgorithm(MetadataProperty responseSigningAlgorithm) {
        this.responseSigningAlgorithm = responseSigningAlgorithm;
    }

    /**
    **/
    public SAMLMetaData responseDigestAlgorithm(MetadataProperty responseDigestAlgorithm) {

        this.responseDigestAlgorithm = responseDigestAlgorithm;
        return this;
    }
    
    @ApiModelProperty(value = "")
    @JsonProperty("responseDigestAlgorithm")
    @Valid
    public MetadataProperty getResponseDigestAlgorithm() {
        return responseDigestAlgorithm;
    }
    public void setResponseDigestAlgorithm(MetadataProperty responseDigestAlgorithm) {
        this.responseDigestAlgorithm = responseDigestAlgorithm;
    }

    /**
    **/
    public SAMLMetaData assertionEncryptionAlgorithm(MetadataProperty assertionEncryptionAlgorithm) {

        this.assertionEncryptionAlgorithm = assertionEncryptionAlgorithm;
        return this;
    }
    
    @ApiModelProperty(value = "")
    @JsonProperty("assertionEncryptionAlgorithm")
    @Valid
    public MetadataProperty getAssertionEncryptionAlgorithm() {
        return assertionEncryptionAlgorithm;
    }
    public void setAssertionEncryptionAlgorithm(MetadataProperty assertionEncryptionAlgorithm) {
        this.assertionEncryptionAlgorithm = assertionEncryptionAlgorithm;
    }

    /**
    **/
    public SAMLMetaData keyEncryptionAlgorithm(MetadataProperty keyEncryptionAlgorithm) {

        this.keyEncryptionAlgorithm = keyEncryptionAlgorithm;
        return this;
    }
    
    @ApiModelProperty(value = "")
    @JsonProperty("keyEncryptionAlgorithm")
    @Valid
    public MetadataProperty getKeyEncryptionAlgorithm() {
        return keyEncryptionAlgorithm;
    }
    public void setKeyEncryptionAlgorithm(MetadataProperty keyEncryptionAlgorithm) {
        this.keyEncryptionAlgorithm = keyEncryptionAlgorithm;
    }



    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SAMLMetaData saMLMetaData = (SAMLMetaData) o;
        return Objects.equals(this.defaultNameIdFormat, saMLMetaData.defaultNameIdFormat) &&
            Objects.equals(this.certificateAlias, saMLMetaData.certificateAlias) &&
            Objects.equals(this.responseSigningAlgorithm, saMLMetaData.responseSigningAlgorithm) &&
            Objects.equals(this.responseDigestAlgorithm, saMLMetaData.responseDigestAlgorithm) &&
            Objects.equals(this.assertionEncryptionAlgorithm, saMLMetaData.assertionEncryptionAlgorithm) &&
            Objects.equals(this.keyEncryptionAlgorithm, saMLMetaData.keyEncryptionAlgorithm);
    }

    @Override
    public int hashCode() {
        return Objects.hash(defaultNameIdFormat, certificateAlias, responseSigningAlgorithm, responseDigestAlgorithm, assertionEncryptionAlgorithm, keyEncryptionAlgorithm);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class SAMLMetaData {\n");

        sb.append("    defaultNameIdFormat: ").append(toIndentedString(defaultNameIdFormat)).append("\n");
        sb.append("    certificateAlias: ").append(toIndentedString(certificateAlias)).append("\n");
        sb.append("    responseSigningAlgorithm: ").append(toIndentedString(responseSigningAlgorithm)).append("\n");
        sb.append("    responseDigestAlgorithm: ").append(toIndentedString(responseDigestAlgorithm)).append("\n");
        sb.append("    assertionEncryptionAlgorithm: ").append(toIndentedString(assertionEncryptionAlgorithm)).append("\n");
        sb.append("    keyEncryptionAlgorithm: ").append(toIndentedString(keyEncryptionAlgorithm)).append("\n");
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


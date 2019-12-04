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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.validation.Valid;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

public class SAML2ServiceProvider {
  
    private String issuer;
    private String serviceProviderQualifier;
    private List<String> assertionConsumerUrls = null;

    private String defaultAssertionConsumerUrl;
    private Boolean enableRequestSignatureValidation = true;
    private Boolean enableAssertionEncryption = false;
    private String assertionEncryptionAlgroithm;
    private String keyEncryptionAlgorithm;
    private String nameIdFormat = "urn:oasis:names:tc:SAML:1.1:nameid-format:emailAddress";
    private Boolean enableIdpInitiatedSingleSignOn = false;
    private Boolean enableResponseSigning = true;
    private String requestValidationCertificateAlias;
    private String responseSigningAlgorithm;
    private String responseDigestAlgorithm;
    private Boolean enableSingleLogout = true;
    private String singleLogoutResponseUrl;
    private String singleLogoutRequestUrl;

@XmlType(name="SingleLogoutMethodEnum")
@XmlEnum(String.class)
public enum SingleLogoutMethodEnum {

    @XmlEnumValue("backchannel") BACKCHANNEL(String.valueOf("backchannel")), @XmlEnumValue("frontchannel_http_redirect") FRONTCHANNEL_HTTP_REDIRECT(String.valueOf("frontchannel_http_redirect")), @XmlEnumValue("frontchannel_http_post") FRONTCHANNEL_HTTP_POST(String.valueOf("frontchannel_http_post"));


    private String value;

    SingleLogoutMethodEnum(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    public static SingleLogoutMethodEnum fromValue(String value) {
        for (SingleLogoutMethodEnum b : SingleLogoutMethodEnum.values()) {
            if (b.value.equals(value)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
}

    private SingleLogoutMethodEnum singleLogoutMethod;
    private Boolean enableIdpInitiatedSingleLogOut = false;
    private List<String> idpInitiatedLogoutReturnUrls = null;

    private Boolean enableAttributeProfile = false;
    private Boolean includedAttributeInResponseAlways = false;
    private List<String> audiences = null;

    private List<String> recipients = null;

    private Boolean enableAssertionQueryProfile = false;
    private Boolean enableSAML2ArtifactBinding = false;
    private Boolean enableSignatureValidationInArtifactBinding = false;
    private String idPEntityidAlias;

    /**
    **/
    public SAML2ServiceProvider issuer(String issuer) {

        this.issuer = issuer;
        return this;
    }
    
    @ApiModelProperty(value = "")
    @JsonProperty("issuer")
    @Valid
    public String getIssuer() {
        return issuer;
    }
    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    /**
    **/
    public SAML2ServiceProvider serviceProviderQualifier(String serviceProviderQualifier) {

        this.serviceProviderQualifier = serviceProviderQualifier;
        return this;
    }
    
    @ApiModelProperty(value = "")
    @JsonProperty("serviceProviderQualifier")
    @Valid
    public String getServiceProviderQualifier() {
        return serviceProviderQualifier;
    }
    public void setServiceProviderQualifier(String serviceProviderQualifier) {
        this.serviceProviderQualifier = serviceProviderQualifier;
    }

    /**
    **/
    public SAML2ServiceProvider assertionConsumerUrls(List<String> assertionConsumerUrls) {

        this.assertionConsumerUrls = assertionConsumerUrls;
        return this;
    }
    
    @ApiModelProperty(value = "")
    @JsonProperty("assertionConsumerUrls")
    @Valid
    public List<String> getAssertionConsumerUrls() {
        return assertionConsumerUrls;
    }
    public void setAssertionConsumerUrls(List<String> assertionConsumerUrls) {
        this.assertionConsumerUrls = assertionConsumerUrls;
    }

    public SAML2ServiceProvider addAssertionConsumerUrlsItem(String assertionConsumerUrlsItem) {
        if (this.assertionConsumerUrls == null) {
            this.assertionConsumerUrls = new ArrayList<>();
        }
        this.assertionConsumerUrls.add(assertionConsumerUrlsItem);
        return this;
    }

        /**
    **/
    public SAML2ServiceProvider defaultAssertionConsumerUrl(String defaultAssertionConsumerUrl) {

        this.defaultAssertionConsumerUrl = defaultAssertionConsumerUrl;
        return this;
    }
    
    @ApiModelProperty(value = "")
    @JsonProperty("defaultAssertionConsumerUrl")
    @Valid
    public String getDefaultAssertionConsumerUrl() {
        return defaultAssertionConsumerUrl;
    }
    public void setDefaultAssertionConsumerUrl(String defaultAssertionConsumerUrl) {
        this.defaultAssertionConsumerUrl = defaultAssertionConsumerUrl;
    }

    /**
    **/
    public SAML2ServiceProvider enableRequestSignatureValidation(Boolean enableRequestSignatureValidation) {

        this.enableRequestSignatureValidation = enableRequestSignatureValidation;
        return this;
    }
    
    @ApiModelProperty(value = "")
    @JsonProperty("enableRequestSignatureValidation")
    @Valid
    public Boolean getEnableRequestSignatureValidation() {
        return enableRequestSignatureValidation;
    }
    public void setEnableRequestSignatureValidation(Boolean enableRequestSignatureValidation) {
        this.enableRequestSignatureValidation = enableRequestSignatureValidation;
    }

    /**
    **/
    public SAML2ServiceProvider enableAssertionEncryption(Boolean enableAssertionEncryption) {

        this.enableAssertionEncryption = enableAssertionEncryption;
        return this;
    }
    
    @ApiModelProperty(value = "")
    @JsonProperty("enableAssertionEncryption")
    @Valid
    public Boolean getEnableAssertionEncryption() {
        return enableAssertionEncryption;
    }
    public void setEnableAssertionEncryption(Boolean enableAssertionEncryption) {
        this.enableAssertionEncryption = enableAssertionEncryption;
    }

    /**
    **/
    public SAML2ServiceProvider assertionEncryptionAlgroithm(String assertionEncryptionAlgroithm) {

        this.assertionEncryptionAlgroithm = assertionEncryptionAlgroithm;
        return this;
    }
    
    @ApiModelProperty(value = "")
    @JsonProperty("assertionEncryptionAlgroithm")
    @Valid
    public String getAssertionEncryptionAlgroithm() {
        return assertionEncryptionAlgroithm;
    }
    public void setAssertionEncryptionAlgroithm(String assertionEncryptionAlgroithm) {
        this.assertionEncryptionAlgroithm = assertionEncryptionAlgroithm;
    }

    /**
    **/
    public SAML2ServiceProvider keyEncryptionAlgorithm(String keyEncryptionAlgorithm) {

        this.keyEncryptionAlgorithm = keyEncryptionAlgorithm;
        return this;
    }
    
    @ApiModelProperty(value = "")
    @JsonProperty("keyEncryptionAlgorithm")
    @Valid
    public String getKeyEncryptionAlgorithm() {
        return keyEncryptionAlgorithm;
    }
    public void setKeyEncryptionAlgorithm(String keyEncryptionAlgorithm) {
        this.keyEncryptionAlgorithm = keyEncryptionAlgorithm;
    }

    /**
    **/
    public SAML2ServiceProvider nameIdFormat(String nameIdFormat) {

        this.nameIdFormat = nameIdFormat;
        return this;
    }
    
    @ApiModelProperty(example = "urn:oasis:names:tc:SAML:1.1:nameid-format:emailAddress", value = "")
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
    public SAML2ServiceProvider enableIdpInitiatedSingleSignOn(Boolean enableIdpInitiatedSingleSignOn) {

        this.enableIdpInitiatedSingleSignOn = enableIdpInitiatedSingleSignOn;
        return this;
    }
    
    @ApiModelProperty(value = "")
    @JsonProperty("enableIdpInitiatedSingleSignOn")
    @Valid
    public Boolean getEnableIdpInitiatedSingleSignOn() {
        return enableIdpInitiatedSingleSignOn;
    }
    public void setEnableIdpInitiatedSingleSignOn(Boolean enableIdpInitiatedSingleSignOn) {
        this.enableIdpInitiatedSingleSignOn = enableIdpInitiatedSingleSignOn;
    }

    /**
    **/
    public SAML2ServiceProvider enableResponseSigning(Boolean enableResponseSigning) {

        this.enableResponseSigning = enableResponseSigning;
        return this;
    }
    
    @ApiModelProperty(value = "")
    @JsonProperty("enableResponseSigning")
    @Valid
    public Boolean getEnableResponseSigning() {
        return enableResponseSigning;
    }
    public void setEnableResponseSigning(Boolean enableResponseSigning) {
        this.enableResponseSigning = enableResponseSigning;
    }

    /**
    **/
    public SAML2ServiceProvider requestValidationCertificateAlias(String requestValidationCertificateAlias) {

        this.requestValidationCertificateAlias = requestValidationCertificateAlias;
        return this;
    }
    
    @ApiModelProperty(value = "")
    @JsonProperty("requestValidationCertificateAlias")
    @Valid
    public String getRequestValidationCertificateAlias() {
        return requestValidationCertificateAlias;
    }
    public void setRequestValidationCertificateAlias(String requestValidationCertificateAlias) {
        this.requestValidationCertificateAlias = requestValidationCertificateAlias;
    }

    /**
    **/
    public SAML2ServiceProvider responseSigningAlgorithm(String responseSigningAlgorithm) {

        this.responseSigningAlgorithm = responseSigningAlgorithm;
        return this;
    }
    
    @ApiModelProperty(value = "")
    @JsonProperty("responseSigningAlgorithm")
    @Valid
    public String getResponseSigningAlgorithm() {
        return responseSigningAlgorithm;
    }
    public void setResponseSigningAlgorithm(String responseSigningAlgorithm) {
        this.responseSigningAlgorithm = responseSigningAlgorithm;
    }

    /**
    **/
    public SAML2ServiceProvider responseDigestAlgorithm(String responseDigestAlgorithm) {

        this.responseDigestAlgorithm = responseDigestAlgorithm;
        return this;
    }
    
    @ApiModelProperty(value = "")
    @JsonProperty("responseDigestAlgorithm")
    @Valid
    public String getResponseDigestAlgorithm() {
        return responseDigestAlgorithm;
    }
    public void setResponseDigestAlgorithm(String responseDigestAlgorithm) {
        this.responseDigestAlgorithm = responseDigestAlgorithm;
    }

    /**
    **/
    public SAML2ServiceProvider enableSingleLogout(Boolean enableSingleLogout) {

        this.enableSingleLogout = enableSingleLogout;
        return this;
    }
    
    @ApiModelProperty(value = "")
    @JsonProperty("enableSingleLogout")
    @Valid
    public Boolean getEnableSingleLogout() {
        return enableSingleLogout;
    }
    public void setEnableSingleLogout(Boolean enableSingleLogout) {
        this.enableSingleLogout = enableSingleLogout;
    }

    /**
    * Single logout response accepting endpoint
    **/
    public SAML2ServiceProvider singleLogoutResponseUrl(String singleLogoutResponseUrl) {

        this.singleLogoutResponseUrl = singleLogoutResponseUrl;
        return this;
    }
    
    @ApiModelProperty(value = "Single logout response accepting endpoint")
    @JsonProperty("singleLogoutResponseUrl")
    @Valid
    public String getSingleLogoutResponseUrl() {
        return singleLogoutResponseUrl;
    }
    public void setSingleLogoutResponseUrl(String singleLogoutResponseUrl) {
        this.singleLogoutResponseUrl = singleLogoutResponseUrl;
    }

    /**
    * Single logout request accepting endpoint
    **/
    public SAML2ServiceProvider singleLogoutRequestUrl(String singleLogoutRequestUrl) {

        this.singleLogoutRequestUrl = singleLogoutRequestUrl;
        return this;
    }
    
    @ApiModelProperty(value = "Single logout request accepting endpoint")
    @JsonProperty("singleLogoutRequestUrl")
    @Valid
    public String getSingleLogoutRequestUrl() {
        return singleLogoutRequestUrl;
    }
    public void setSingleLogoutRequestUrl(String singleLogoutRequestUrl) {
        this.singleLogoutRequestUrl = singleLogoutRequestUrl;
    }

    /**
    **/
    public SAML2ServiceProvider singleLogoutMethod(SingleLogoutMethodEnum singleLogoutMethod) {

        this.singleLogoutMethod = singleLogoutMethod;
        return this;
    }
    
    @ApiModelProperty(value = "")
    @JsonProperty("singleLogoutMethod")
    @Valid
    public SingleLogoutMethodEnum getSingleLogoutMethod() {
        return singleLogoutMethod;
    }
    public void setSingleLogoutMethod(SingleLogoutMethodEnum singleLogoutMethod) {
        this.singleLogoutMethod = singleLogoutMethod;
    }

    /**
    **/
    public SAML2ServiceProvider enableIdpInitiatedSingleLogOut(Boolean enableIdpInitiatedSingleLogOut) {

        this.enableIdpInitiatedSingleLogOut = enableIdpInitiatedSingleLogOut;
        return this;
    }
    
    @ApiModelProperty(value = "")
    @JsonProperty("enableIdpInitiatedSingleLogOut")
    @Valid
    public Boolean getEnableIdpInitiatedSingleLogOut() {
        return enableIdpInitiatedSingleLogOut;
    }
    public void setEnableIdpInitiatedSingleLogOut(Boolean enableIdpInitiatedSingleLogOut) {
        this.enableIdpInitiatedSingleLogOut = enableIdpInitiatedSingleLogOut;
    }

    /**
    **/
    public SAML2ServiceProvider idpInitiatedLogoutReturnUrls(List<String> idpInitiatedLogoutReturnUrls) {

        this.idpInitiatedLogoutReturnUrls = idpInitiatedLogoutReturnUrls;
        return this;
    }
    
    @ApiModelProperty(value = "")
    @JsonProperty("idpInitiatedLogoutReturnUrls")
    @Valid
    public List<String> getIdpInitiatedLogoutReturnUrls() {
        return idpInitiatedLogoutReturnUrls;
    }
    public void setIdpInitiatedLogoutReturnUrls(List<String> idpInitiatedLogoutReturnUrls) {
        this.idpInitiatedLogoutReturnUrls = idpInitiatedLogoutReturnUrls;
    }

    public SAML2ServiceProvider addIdpInitiatedLogoutReturnUrlsItem(String idpInitiatedLogoutReturnUrlsItem) {
        if (this.idpInitiatedLogoutReturnUrls == null) {
            this.idpInitiatedLogoutReturnUrls = new ArrayList<>();
        }
        this.idpInitiatedLogoutReturnUrls.add(idpInitiatedLogoutReturnUrlsItem);
        return this;
    }

        /**
    **/
    public SAML2ServiceProvider enableAttributeProfile(Boolean enableAttributeProfile) {

        this.enableAttributeProfile = enableAttributeProfile;
        return this;
    }
    
    @ApiModelProperty(value = "")
    @JsonProperty("enableAttributeProfile")
    @Valid
    public Boolean getEnableAttributeProfile() {
        return enableAttributeProfile;
    }
    public void setEnableAttributeProfile(Boolean enableAttributeProfile) {
        this.enableAttributeProfile = enableAttributeProfile;
    }

    /**
    **/
    public SAML2ServiceProvider includedAttributeInResponseAlways(Boolean includedAttributeInResponseAlways) {

        this.includedAttributeInResponseAlways = includedAttributeInResponseAlways;
        return this;
    }
    
    @ApiModelProperty(value = "")
    @JsonProperty("includedAttributeInResponseAlways")
    @Valid
    public Boolean getIncludedAttributeInResponseAlways() {
        return includedAttributeInResponseAlways;
    }
    public void setIncludedAttributeInResponseAlways(Boolean includedAttributeInResponseAlways) {
        this.includedAttributeInResponseAlways = includedAttributeInResponseAlways;
    }

    /**
    * Additional audience values to be added to the SAML Assertions
    **/
    public SAML2ServiceProvider audiences(List<String> audiences) {

        this.audiences = audiences;
        return this;
    }
    
    @ApiModelProperty(example = "[\"https://app.example.com/saml\"]", value = "Additional audience values to be added to the SAML Assertions")
    @JsonProperty("audiences")
    @Valid
    public List<String> getAudiences() {
        return audiences;
    }
    public void setAudiences(List<String> audiences) {
        this.audiences = audiences;
    }

    public SAML2ServiceProvider addAudiencesItem(String audiencesItem) {
        if (this.audiences == null) {
            this.audiences = new ArrayList<>();
        }
        this.audiences.add(audiencesItem);
        return this;
    }

        /**
    * Additional recipient values to be added to the SAML Assertions
    **/
    public SAML2ServiceProvider recipients(List<String> recipients) {

        this.recipients = recipients;
        return this;
    }
    
    @ApiModelProperty(example = "[\"https://app.example.com/saml\"]", value = "Additional recipient values to be added to the SAML Assertions")
    @JsonProperty("recipients")
    @Valid
    public List<String> getRecipients() {
        return recipients;
    }
    public void setRecipients(List<String> recipients) {
        this.recipients = recipients;
    }

    public SAML2ServiceProvider addRecipientsItem(String recipientsItem) {
        if (this.recipients == null) {
            this.recipients = new ArrayList<>();
        }
        this.recipients.add(recipientsItem);
        return this;
    }

        /**
    **/
    public SAML2ServiceProvider enableAssertionQueryProfile(Boolean enableAssertionQueryProfile) {

        this.enableAssertionQueryProfile = enableAssertionQueryProfile;
        return this;
    }
    
    @ApiModelProperty(value = "")
    @JsonProperty("enableAssertionQueryProfile")
    @Valid
    public Boolean getEnableAssertionQueryProfile() {
        return enableAssertionQueryProfile;
    }
    public void setEnableAssertionQueryProfile(Boolean enableAssertionQueryProfile) {
        this.enableAssertionQueryProfile = enableAssertionQueryProfile;
    }

    /**
    **/
    public SAML2ServiceProvider enableSAML2ArtifactBinding(Boolean enableSAML2ArtifactBinding) {

        this.enableSAML2ArtifactBinding = enableSAML2ArtifactBinding;
        return this;
    }
    
    @ApiModelProperty(value = "")
    @JsonProperty("enableSAML2ArtifactBinding")
    @Valid
    public Boolean getEnableSAML2ArtifactBinding() {
        return enableSAML2ArtifactBinding;
    }
    public void setEnableSAML2ArtifactBinding(Boolean enableSAML2ArtifactBinding) {
        this.enableSAML2ArtifactBinding = enableSAML2ArtifactBinding;
    }

    /**
    **/
    public SAML2ServiceProvider enableSignatureValidationInArtifactBinding(Boolean enableSignatureValidationInArtifactBinding) {

        this.enableSignatureValidationInArtifactBinding = enableSignatureValidationInArtifactBinding;
        return this;
    }
    
    @ApiModelProperty(value = "")
    @JsonProperty("enableSignatureValidationInArtifactBinding")
    @Valid
    public Boolean getEnableSignatureValidationInArtifactBinding() {
        return enableSignatureValidationInArtifactBinding;
    }
    public void setEnableSignatureValidationInArtifactBinding(Boolean enableSignatureValidationInArtifactBinding) {
        this.enableSignatureValidationInArtifactBinding = enableSignatureValidationInArtifactBinding;
    }

    /**
    * Default value is the IdP Entity ID value specified in Resident IdP
    **/
    public SAML2ServiceProvider idPEntityidAlias(String idPEntityidAlias) {

        this.idPEntityidAlias = idPEntityidAlias;
        return this;
    }
    
    @ApiModelProperty(value = "Default value is the IdP Entity ID value specified in Resident IdP")
    @JsonProperty("idPEntityidAlias")
    @Valid
    public String getIdPEntityidAlias() {
        return idPEntityidAlias;
    }
    public void setIdPEntityidAlias(String idPEntityidAlias) {
        this.idPEntityidAlias = idPEntityidAlias;
    }



    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SAML2ServiceProvider saML2ServiceProvider = (SAML2ServiceProvider) o;
        return Objects.equals(this.issuer, saML2ServiceProvider.issuer) &&
            Objects.equals(this.serviceProviderQualifier, saML2ServiceProvider.serviceProviderQualifier) &&
            Objects.equals(this.assertionConsumerUrls, saML2ServiceProvider.assertionConsumerUrls) &&
            Objects.equals(this.defaultAssertionConsumerUrl, saML2ServiceProvider.defaultAssertionConsumerUrl) &&
            Objects.equals(this.enableRequestSignatureValidation, saML2ServiceProvider.enableRequestSignatureValidation) &&
            Objects.equals(this.enableAssertionEncryption, saML2ServiceProvider.enableAssertionEncryption) &&
            Objects.equals(this.assertionEncryptionAlgroithm, saML2ServiceProvider.assertionEncryptionAlgroithm) &&
            Objects.equals(this.keyEncryptionAlgorithm, saML2ServiceProvider.keyEncryptionAlgorithm) &&
            Objects.equals(this.nameIdFormat, saML2ServiceProvider.nameIdFormat) &&
            Objects.equals(this.enableIdpInitiatedSingleSignOn, saML2ServiceProvider.enableIdpInitiatedSingleSignOn) &&
            Objects.equals(this.enableResponseSigning, saML2ServiceProvider.enableResponseSigning) &&
            Objects.equals(this.requestValidationCertificateAlias, saML2ServiceProvider.requestValidationCertificateAlias) &&
            Objects.equals(this.responseSigningAlgorithm, saML2ServiceProvider.responseSigningAlgorithm) &&
            Objects.equals(this.responseDigestAlgorithm, saML2ServiceProvider.responseDigestAlgorithm) &&
            Objects.equals(this.enableSingleLogout, saML2ServiceProvider.enableSingleLogout) &&
            Objects.equals(this.singleLogoutResponseUrl, saML2ServiceProvider.singleLogoutResponseUrl) &&
            Objects.equals(this.singleLogoutRequestUrl, saML2ServiceProvider.singleLogoutRequestUrl) &&
            Objects.equals(this.singleLogoutMethod, saML2ServiceProvider.singleLogoutMethod) &&
            Objects.equals(this.enableIdpInitiatedSingleLogOut, saML2ServiceProvider.enableIdpInitiatedSingleLogOut) &&
            Objects.equals(this.idpInitiatedLogoutReturnUrls, saML2ServiceProvider.idpInitiatedLogoutReturnUrls) &&
            Objects.equals(this.enableAttributeProfile, saML2ServiceProvider.enableAttributeProfile) &&
            Objects.equals(this.includedAttributeInResponseAlways, saML2ServiceProvider.includedAttributeInResponseAlways) &&
            Objects.equals(this.audiences, saML2ServiceProvider.audiences) &&
            Objects.equals(this.recipients, saML2ServiceProvider.recipients) &&
            Objects.equals(this.enableAssertionQueryProfile, saML2ServiceProvider.enableAssertionQueryProfile) &&
            Objects.equals(this.enableSAML2ArtifactBinding, saML2ServiceProvider.enableSAML2ArtifactBinding) &&
            Objects.equals(this.enableSignatureValidationInArtifactBinding, saML2ServiceProvider.enableSignatureValidationInArtifactBinding) &&
            Objects.equals(this.idPEntityidAlias, saML2ServiceProvider.idPEntityidAlias);
    }

    @Override
    public int hashCode() {
        return Objects.hash(issuer, serviceProviderQualifier, assertionConsumerUrls, defaultAssertionConsumerUrl, enableRequestSignatureValidation, enableAssertionEncryption, assertionEncryptionAlgroithm, keyEncryptionAlgorithm, nameIdFormat, enableIdpInitiatedSingleSignOn, enableResponseSigning, requestValidationCertificateAlias, responseSigningAlgorithm, responseDigestAlgorithm, enableSingleLogout, singleLogoutResponseUrl, singleLogoutRequestUrl, singleLogoutMethod, enableIdpInitiatedSingleLogOut, idpInitiatedLogoutReturnUrls, enableAttributeProfile, includedAttributeInResponseAlways, audiences, recipients, enableAssertionQueryProfile, enableSAML2ArtifactBinding, enableSignatureValidationInArtifactBinding, idPEntityidAlias);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class SAML2ServiceProvider {\n");

        sb.append("    issuer: ").append(toIndentedString(issuer)).append("\n");
        sb.append("    serviceProviderQualifier: ").append(toIndentedString(serviceProviderQualifier)).append("\n");
        sb.append("    assertionConsumerUrls: ").append(toIndentedString(assertionConsumerUrls)).append("\n");
        sb.append("    defaultAssertionConsumerUrl: ").append(toIndentedString(defaultAssertionConsumerUrl)).append("\n");
        sb.append("    enableRequestSignatureValidation: ").append(toIndentedString(enableRequestSignatureValidation)).append("\n");
        sb.append("    enableAssertionEncryption: ").append(toIndentedString(enableAssertionEncryption)).append("\n");
        sb.append("    assertionEncryptionAlgroithm: ").append(toIndentedString(assertionEncryptionAlgroithm)).append("\n");
        sb.append("    keyEncryptionAlgorithm: ").append(toIndentedString(keyEncryptionAlgorithm)).append("\n");
        sb.append("    nameIdFormat: ").append(toIndentedString(nameIdFormat)).append("\n");
        sb.append("    enableIdpInitiatedSingleSignOn: ").append(toIndentedString(enableIdpInitiatedSingleSignOn)).append("\n");
        sb.append("    enableResponseSigning: ").append(toIndentedString(enableResponseSigning)).append("\n");
        sb.append("    requestValidationCertificateAlias: ").append(toIndentedString(requestValidationCertificateAlias)).append("\n");
        sb.append("    responseSigningAlgorithm: ").append(toIndentedString(responseSigningAlgorithm)).append("\n");
        sb.append("    responseDigestAlgorithm: ").append(toIndentedString(responseDigestAlgorithm)).append("\n");
        sb.append("    enableSingleLogout: ").append(toIndentedString(enableSingleLogout)).append("\n");
        sb.append("    singleLogoutResponseUrl: ").append(toIndentedString(singleLogoutResponseUrl)).append("\n");
        sb.append("    singleLogoutRequestUrl: ").append(toIndentedString(singleLogoutRequestUrl)).append("\n");
        sb.append("    singleLogoutMethod: ").append(toIndentedString(singleLogoutMethod)).append("\n");
        sb.append("    enableIdpInitiatedSingleLogOut: ").append(toIndentedString(enableIdpInitiatedSingleLogOut)).append("\n");
        sb.append("    idpInitiatedLogoutReturnUrls: ").append(toIndentedString(idpInitiatedLogoutReturnUrls)).append("\n");
        sb.append("    enableAttributeProfile: ").append(toIndentedString(enableAttributeProfile)).append("\n");
        sb.append("    includedAttributeInResponseAlways: ").append(toIndentedString(includedAttributeInResponseAlways)).append("\n");
        sb.append("    audiences: ").append(toIndentedString(audiences)).append("\n");
        sb.append("    recipients: ").append(toIndentedString(recipients)).append("\n");
        sb.append("    enableAssertionQueryProfile: ").append(toIndentedString(enableAssertionQueryProfile)).append("\n");
        sb.append("    enableSAML2ArtifactBinding: ").append(toIndentedString(enableSAML2ArtifactBinding)).append("\n");
        sb.append("    enableSignatureValidationInArtifactBinding: ").append(toIndentedString(enableSignatureValidationInArtifactBinding)).append("\n");
        sb.append("    idPEntityidAlias: ").append(toIndentedString(idPEntityidAlias)).append("\n");
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


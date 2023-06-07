/*
 * Copyright (c) 2019, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.identity.integration.test.rest.api.server.application.management.v1.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.validation.Valid;

public class SAML2ServiceProvider {
  
    private String issuer;
    private String serviceProviderQualifier;
    private List<String> assertionConsumerUrls = null;
    private String defaultAssertionConsumerUrl;
    private SingleSignOnProfile singleSignOnProfile;
    private SAMLAttributeProfile attributeProfile;
    private SingleLogoutProfile singleLogoutProfile;
    private SAMLRequestValidation requestValidation;
    private SAMLResponseSigning responseSigning;
    private Boolean enableAssertionQueryProfile = false;

    /**
    **/
    public SAML2ServiceProvider issuer(String issuer) {

        this.issuer = issuer;
        return this;
    }

    @ApiModelProperty()
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

    @ApiModelProperty()
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

    @ApiModelProperty()
    @JsonProperty("assertionConsumerUrls")
    @Valid
    public List<String> getAssertionConsumerUrls() {
        return assertionConsumerUrls;
    }
    public void setAssertionConsumerUrls(List<String> assertionConsumerUrls) {
        this.assertionConsumerUrls = assertionConsumerUrls;
    }

    public SAML2ServiceProvider addAssertionConsumerUrl(String assertionConsumerUrl) {
        if (this.assertionConsumerUrls == null) {
            this.assertionConsumerUrls = new ArrayList<>();
        }
        this.assertionConsumerUrls.add(assertionConsumerUrl);
        return this;
    }

        /**
    **/
    public SAML2ServiceProvider defaultAssertionConsumerUrl(String defaultAssertionConsumerUrl) {

        this.defaultAssertionConsumerUrl = defaultAssertionConsumerUrl;
        return this;
    }

    @ApiModelProperty()
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
    public SAML2ServiceProvider singleSignOnProfile(SingleSignOnProfile singleSignOnProfile) {

        this.singleSignOnProfile = singleSignOnProfile;
        return this;
    }

    @ApiModelProperty()
    @JsonProperty("singleSignOnProfile")
    @Valid
    public SingleSignOnProfile getSingleSignOnProfile() {
        return singleSignOnProfile;
    }
    public void setSingleSignOnProfile(SingleSignOnProfile singleSignOnProfile) {
        this.singleSignOnProfile = singleSignOnProfile;
    }

    /**
    **/
    public SAML2ServiceProvider attributeProfile(SAMLAttributeProfile attributeProfile) {

        this.attributeProfile = attributeProfile;
        return this;
    }

    @ApiModelProperty()
    @JsonProperty("attributeProfile")
    @Valid
    public SAMLAttributeProfile getAttributeProfile() {
        return attributeProfile;
    }
    public void setAttributeProfile(SAMLAttributeProfile attributeProfile) {
        this.attributeProfile = attributeProfile;
    }

    /**
    **/
    public SAML2ServiceProvider singleLogoutProfile(SingleLogoutProfile singleLogoutProfile) {

        this.singleLogoutProfile = singleLogoutProfile;
        return this;
    }

    @ApiModelProperty()
    @JsonProperty("singleLogoutProfile")
    @Valid
    public SingleLogoutProfile getSingleLogoutProfile() {
        return singleLogoutProfile;
    }
    public void setSingleLogoutProfile(SingleLogoutProfile singleLogoutProfile) {
        this.singleLogoutProfile = singleLogoutProfile;
    }

    /**
    **/
    public SAML2ServiceProvider requestValidation(SAMLRequestValidation requestValidation) {

        this.requestValidation = requestValidation;
        return this;
    }

    @ApiModelProperty()
    @JsonProperty("requestValidation")
    @Valid
    public SAMLRequestValidation getRequestValidation() {
        return requestValidation;
    }
    public void setRequestValidation(SAMLRequestValidation requestValidation) {
        this.requestValidation = requestValidation;
    }

    /**
    **/
    public SAML2ServiceProvider responseSigning(SAMLResponseSigning responseSigning) {

        this.responseSigning = responseSigning;
        return this;
    }

    @ApiModelProperty()
    @JsonProperty("responseSigning")
    @Valid
    public SAMLResponseSigning getResponseSigning() {
        return responseSigning;
    }
    public void setResponseSigning(SAMLResponseSigning responseSigning) {
        this.responseSigning = responseSigning;
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
            Objects.equals(this.singleSignOnProfile, saML2ServiceProvider.singleSignOnProfile) &&
            Objects.equals(this.attributeProfile, saML2ServiceProvider.attributeProfile) &&
            Objects.equals(this.singleLogoutProfile, saML2ServiceProvider.singleLogoutProfile) &&
            Objects.equals(this.requestValidation, saML2ServiceProvider.requestValidation) &&
            Objects.equals(this.responseSigning, saML2ServiceProvider.responseSigning) &&
            Objects.equals(this.enableAssertionQueryProfile, saML2ServiceProvider.enableAssertionQueryProfile);
    }

    @Override
    public int hashCode() {
        return Objects.hash(issuer, serviceProviderQualifier, assertionConsumerUrls, defaultAssertionConsumerUrl,
                singleSignOnProfile, attributeProfile, singleLogoutProfile, requestValidation, responseSigning,
                enableAssertionQueryProfile);
    }

    @Override
    public String toString() {

        return "class SAML2ServiceProvider {\n" +
                "    issuer: " + toIndentedString(issuer) + "\n" +
                "    serviceProviderQualifier: " + toIndentedString(serviceProviderQualifier) + "\n" +
                "    assertionConsumerUrls: " + toIndentedString(assertionConsumerUrls) + "\n" +
                "    defaultAssertionConsumerUrl: " + toIndentedString(defaultAssertionConsumerUrl) + "\n" +
                "    singleSignOnProfile: " + toIndentedString(singleSignOnProfile) + "\n" +
                "    attributeProfile: " + toIndentedString(attributeProfile) + "\n" +
                "    singleLogoutProfile: " + toIndentedString(singleLogoutProfile) + "\n" +
                "    requestValidation: " + toIndentedString(requestValidation) + "\n" +
                "    responseSigning: " + toIndentedString(responseSigning) + "\n" +
                "    enableAssertionQueryProfile: " + toIndentedString(enableAssertionQueryProfile) + "\n" +
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


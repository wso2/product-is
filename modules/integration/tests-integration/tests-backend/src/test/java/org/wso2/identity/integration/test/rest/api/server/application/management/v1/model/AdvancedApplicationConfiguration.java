/*
 * Copyright (c) 2019, WSO2 LLC. (http://www.wso2.com).
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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.validation.Valid;

public class AdvancedApplicationConfiguration  {
  
    private Boolean saas;

    private Boolean discoverableByEndUsers;
    private List<DiscoverableGroup> discoverableGroups = null;
    private Certificate certificate;
    private Boolean skipLoginConsent;
    private Boolean skipLogoutConsent;
    private Boolean returnAuthenticatedIdpList;
    private Boolean enableAuthorization;

    private Boolean fragment;
    private List<AdditionalSpProperties> additionalSpProperties;
    private Boolean enableAPIBasedAuthentication;
    private AdvancedApplicationConfigurationAttestationMetaData attestationMetaData;
    private TrustedAppConfiguration trustedAppConfiguration;

    private Boolean useExternalConsentPage;

    /**
     * Decide whether this application is allowed to be accessed across tenants.
     **/
    public AdvancedApplicationConfiguration saas(Boolean saas) {

        this.saas = saas;
        return this;
    }

    @ApiModelProperty(example = "false", value = "Decide whether this application is allowed to be accessed across tenants.")
    @JsonProperty("saas")
    @Valid
    public Boolean getSaas() {
        return saas;
    }
    public void setSaas(Boolean saas) {
        this.saas = saas;
    }

    /**
     *
     **/
    public AdvancedApplicationConfiguration discoverableByEndUsers(Boolean discoverableByEndUsers) {

        this.discoverableByEndUsers = discoverableByEndUsers;
        return this;
    }

    @ApiModelProperty(example = "false")
    @JsonProperty("discoverableByEndUsers")
    @Valid
    public Boolean getDiscoverableByEndUsers() {
        return discoverableByEndUsers;
    }
    public void setDiscoverableByEndUsers(Boolean discoverableByEndUsers) {
        this.discoverableByEndUsers = discoverableByEndUsers;
    }

    /**
     * List of groups from user stores where users in those groups can discover the application.
     **/
    public AdvancedApplicationConfiguration discoverableGroups(List<DiscoverableGroup> discoverableGroups) {

        this.discoverableGroups = discoverableGroups;
        return this;
    }

    @ApiModelProperty(value = "List of groups from user stores where users in those groups can discover the application.")
    @JsonProperty("discoverableGroups")
    @Valid
    public List<DiscoverableGroup> getDiscoverableGroups() {

        return discoverableGroups;
    }

    public void setDiscoverableGroups(List<DiscoverableGroup> discoverableGroups) {

        this.discoverableGroups = discoverableGroups;
    }

    public AdvancedApplicationConfiguration addDiscoverableGroupsItem(DiscoverableGroup discoverableGroupsItem) {

        if (this.discoverableGroups == null) {
            this.discoverableGroups = new ArrayList<>();
        }
        this.discoverableGroups.add(discoverableGroupsItem);
        return this;
    }

    /**
    **/
    public AdvancedApplicationConfiguration certificate(Certificate certificate) {

        this.certificate = certificate;
        return this;
    }
    
    @ApiModelProperty(value = "")
    @JsonProperty("certificate")
    @Valid
    public Certificate getCertificate() {
        return certificate;
    }
    public void setCertificate(Certificate certificate) {
        this.certificate = certificate;
    }

    /**
    **/
    public AdvancedApplicationConfiguration skipLoginConsent(Boolean skipLoginConsent) {

        this.skipLoginConsent = skipLoginConsent;
        return this;
    }

    @ApiModelProperty(example = "false", value = "Decides whether user consent needs to be skipped during login flows.")
    @JsonProperty("skipLoginConsent")
    @Valid
    public Boolean getSkipLoginConsent() {
        return skipLoginConsent;
    }
    public void setSkipLoginConsent(Boolean skipLoginConsent) {
        this.skipLoginConsent = skipLoginConsent;
    }

    public AdvancedApplicationConfiguration skipLogoutConsent(Boolean skipLogoutConsent) {

        this.skipLogoutConsent = skipLogoutConsent;
        return this;
    }

    @ApiModelProperty(example = "false", value = "Decides whether user consent needs to be skipped during logout flows.")
    @JsonProperty("skipLogoutConsent")
    @Valid
    public Boolean getSkipLogoutConsent() {
        return skipLogoutConsent;
    }
    public void setSkipLogoutConsent(Boolean skipLogoutConsent) {
        this.skipLogoutConsent = skipLogoutConsent;
    }

    /**
    * Decide whether authenticated identity provider list needs to returned in the authentication response.
    **/
    public AdvancedApplicationConfiguration returnAuthenticatedIdpList(Boolean returnAuthenticatedIdpList) {

        this.returnAuthenticatedIdpList = returnAuthenticatedIdpList;
        return this;
    }
    
    @ApiModelProperty(example = "false", value = "Decide whether authenticated identity provider list needs to returned in the authentication response.")
    @JsonProperty("returnAuthenticatedIdpList")
    @Valid
    public Boolean getReturnAuthenticatedIdpList() {
        return returnAuthenticatedIdpList;
    }
    public void setReturnAuthenticatedIdpList(Boolean returnAuthenticatedIdpList) {
        this.returnAuthenticatedIdpList = returnAuthenticatedIdpList;
    }

    /**
    **/
    public AdvancedApplicationConfiguration enableAuthorization(Boolean enableAuthorization) {

        this.enableAuthorization = enableAuthorization;
        return this;
    }
    
    @ApiModelProperty(example = "true", value = "")
    @JsonProperty("enableAuthorization")
    @Valid
    public Boolean getEnableAuthorization() {
        return enableAuthorization;
    }
    public void setEnableAuthorization(Boolean enableAuthorization) {
        this.enableAuthorization = enableAuthorization;
    }

    /**
     *
     **/
    public AdvancedApplicationConfiguration fragment(Boolean fragment) {

        this.fragment = fragment;
        return this;
    }

    @ApiModelProperty(example = "false")
    @JsonProperty("fragment")
    @Valid
    public Boolean getFragment() {
        return fragment;
    }
    public void setFragment(Boolean fragment) {
        this.fragment = fragment;
    }

    /**
     * Decides whether API Based Authentication is enabled for this application.
     **/
    public AdvancedApplicationConfiguration enableAPIBasedAuthentication(Boolean enableAPIBasedAuthentication) {

        this.enableAPIBasedAuthentication = enableAPIBasedAuthentication;
        return this;
    }

    @ApiModelProperty(example = "false", value = "Decides whether API Based Authentication is enabled for this application.")
    @JsonProperty("enableAPIBasedAuthentication")
    @Valid
    public Boolean getEnableAPIBasedAuthentication() {
        return enableAPIBasedAuthentication;
    }
    public void setEnableAPIBasedAuthentication(Boolean enableAPIBasedAuthentication) {
        this.enableAPIBasedAuthentication = enableAPIBasedAuthentication;
    }

    /**
     **/
    public AdvancedApplicationConfiguration attestationMetaData(AdvancedApplicationConfigurationAttestationMetaData attestationMetaData) {

        this.attestationMetaData = attestationMetaData;
        return this;
    }

    @ApiModelProperty(value = "")
    @JsonProperty("attestationMetaData")
    @Valid
    public AdvancedApplicationConfigurationAttestationMetaData getAttestationMetaData() {
        return attestationMetaData;
    }
    public void setAttestationMetaData(AdvancedApplicationConfigurationAttestationMetaData attestationMetaData) {
        this.attestationMetaData = attestationMetaData;
    }

    /**
    **/
    public AdvancedApplicationConfiguration trustedAppConfiguration(TrustedAppConfiguration trustedAppConfiguration) {

        this.trustedAppConfiguration = trustedAppConfiguration;
        return this;
    }

    @ApiModelProperty(value = "")
    @JsonProperty("trustedAppConfiguration")
    @Valid
    public TrustedAppConfiguration getTrustedAppConfiguration() {
        return trustedAppConfiguration;
    }
    public void setTrustedAppConfiguration(TrustedAppConfiguration trustedAppConfiguration) {
        this.trustedAppConfiguration = trustedAppConfiguration;
    }

    /**
     **/
    public AdvancedApplicationConfiguration additionalSpProperties(List<AdditionalSpProperties> additionalSpProperties) {

        this.additionalSpProperties = additionalSpProperties;
        return this;
    }

    @ApiModelProperty()
    @JsonProperty("additionalSpProperties")
    @Valid
    public List<AdditionalSpProperties> getAdditionalSpProperties() {
        return additionalSpProperties;
    }
    public void setAdditionalSpProperties(List<AdditionalSpProperties> additionalSpProperties) {
        this.additionalSpProperties = additionalSpProperties;
    }

    /**
     *
     **/
    public AdvancedApplicationConfiguration useExternalConsentPage(Boolean useExternalConsentPage) {

        this.useExternalConsentPage = useExternalConsentPage;
        return this;
    }

    @ApiModelProperty(example = "false")
    @JsonProperty("useExternalConsentPage")
    @Valid
    public Boolean getUseExternalConsentPage() {
        return useExternalConsentPage;
    }
    public void setUseExternalConsentPage(Boolean useExternalConsentPage) {
        this.useExternalConsentPage = useExternalConsentPage;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AdvancedApplicationConfiguration advancedApplicationConfiguration = (AdvancedApplicationConfiguration) o;
        return Objects.equals(this.saas, advancedApplicationConfiguration.saas) &&
                Objects.equals(this.discoverableByEndUsers, advancedApplicationConfiguration.discoverableByEndUsers) &&
                Objects.equals(this.certificate, advancedApplicationConfiguration.certificate) &&
                Objects.equals(this.skipLoginConsent, advancedApplicationConfiguration.skipLoginConsent) &&
                Objects.equals(this.skipLogoutConsent, advancedApplicationConfiguration.skipLogoutConsent) &&
                Objects.equals(this.returnAuthenticatedIdpList, advancedApplicationConfiguration.returnAuthenticatedIdpList) &&
                Objects.equals(this.enableAuthorization, advancedApplicationConfiguration.enableAuthorization) &&
                Objects.equals(this.fragment, advancedApplicationConfiguration.fragment) &&
                Objects.equals(this.additionalSpProperties, advancedApplicationConfiguration.additionalSpProperties) &&
                Objects.equals(this.enableAPIBasedAuthentication, advancedApplicationConfiguration.enableAPIBasedAuthentication) &&
                Objects.equals(this.attestationMetaData, advancedApplicationConfiguration.attestationMetaData) &&
                Objects.equals(this.trustedAppConfiguration, advancedApplicationConfiguration.trustedAppConfiguration) &&
                Objects.equals(this.useExternalConsentPage, advancedApplicationConfiguration.useExternalConsentPage);
    }

    @Override
    public int hashCode() {
        return Objects.hash(saas, discoverableByEndUsers, certificate, skipLoginConsent, skipLogoutConsent, returnAuthenticatedIdpList, enableAuthorization, fragment, enableAPIBasedAuthentication, attestationMetaData, trustedAppConfiguration, additionalSpProperties, useExternalConsentPage);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class AdvancedApplicationConfiguration {\n");

        sb.append("    saas: ").append(toIndentedString(saas)).append("\n");
        sb.append("    discoverableByEndUsers: ").append(toIndentedString(discoverableByEndUsers)).append("\n");
        sb.append("    certificate: ").append(toIndentedString(certificate)).append("\n");
        sb.append("    skipLoginConsent: ").append(toIndentedString(skipLoginConsent)).append("\n");
        sb.append("    skipLogoutConsent: ").append(toIndentedString(skipLogoutConsent)).append("\n");
        sb.append("    returnAuthenticatedIdpList: ").append(toIndentedString(returnAuthenticatedIdpList)).append("\n");
        sb.append("    enableAuthorization: ").append(toIndentedString(enableAuthorization)).append("\n");
        sb.append("    fragment: ").append(toIndentedString(fragment)).append("\n");
        sb.append("    enableAPIBasedAuthentication: ").append(toIndentedString(enableAPIBasedAuthentication)).append("\n");
        sb.append("    attestationMetaData: ").append(toIndentedString(attestationMetaData)).append("\n");
        sb.append("    trustedAppConfiguration: ").append(toIndentedString(trustedAppConfiguration)).append("\n");
        sb.append("    additionalSpProperties: ").append(toIndentedString(additionalSpProperties)).append("\n");
        sb.append("    useExternalConsentPage: ").append(toIndentedString(useExternalConsentPage)).append("\n");
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


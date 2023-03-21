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

public class AdvancedApplicationConfiguration  {
  
    private Boolean saas;
    private Certificate certificate;
    private Boolean skipLoginConsent;
    private Boolean skipLogoutConsent;
    private Boolean returnAuthenticatedIdpList;
    private Boolean enableAuthorization;
    private ExternalConsentManagementConfiguration externalConsentManagement;

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
     **/
    public AdvancedApplicationConfiguration externalConsentManagement(ExternalConsentManagementConfiguration
                                                                              externalConsentManagement) {

        this.externalConsentManagement = externalConsentManagement;
        return this;
    }

    @ApiModelProperty(value = "")
    @JsonProperty("externalConsentManagement")
    @Valid
    public ExternalConsentManagementConfiguration getExternalConsentManagement() {
        return externalConsentManagement;
    }
    public void setExternalConsentManagement(ExternalConsentManagementConfiguration externalConsentManagement) {
        this.externalConsentManagement = externalConsentManagement;
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
            Objects.equals(this.certificate, advancedApplicationConfiguration.certificate) &&
                Objects.equals(this.skipLoginConsent, advancedApplicationConfiguration.skipLoginConsent) &&
                Objects.equals(this.skipLogoutConsent, advancedApplicationConfiguration.skipLogoutConsent) &&
                Objects.equals(this.externalConsentManagement, advancedApplicationConfiguration.externalConsentManagement) &&
                Objects.equals(this.returnAuthenticatedIdpList, advancedApplicationConfiguration.returnAuthenticatedIdpList) &&
            Objects.equals(this.enableAuthorization, advancedApplicationConfiguration.enableAuthorization);
    }

    @Override
    public int hashCode() {
        return Objects.hash(saas, certificate, skipLoginConsent, skipLogoutConsent, externalConsentManagement, returnAuthenticatedIdpList, enableAuthorization);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class AdvancedApplicationConfiguration {\n");

        sb.append("    saas: ").append(toIndentedString(saas)).append("\n");
        sb.append("    certificate: ").append(toIndentedString(certificate)).append("\n");
        sb.append("    skipLoginConsent: ").append(toIndentedString(skipLoginConsent)).append("\n");
        sb.append("    skipLogoutConsent: ").append(toIndentedString(skipLogoutConsent)).append("\n");
        sb.append("    externalConsentManagement: ").append(toIndentedString(externalConsentManagement)).append("\n");
        sb.append("    returnAuthenticatedIdpList: ").append(toIndentedString(returnAuthenticatedIdpList)).append("\n");
        sb.append("    enableAuthorization: ").append(toIndentedString(enableAuthorization)).append("\n");
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


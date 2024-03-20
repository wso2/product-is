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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.validation.Valid;
import javax.xml.bind.annotation.*;
import javax.validation.constraints.*;

public class ApplicationResponseModel {
  
    private String id;
    private String name;
    private String description;
    private String imageUrl;
    private String accessUrl;
    private String logoutReturnUrl;
    private String clientId;
    private String issuer;
    private String realm;
    private String templateId;
    private Boolean isManagementApp;
    private Boolean isB2BSelfServiceApp;
    private AssociatedRolesConfig associatedRoles;
    private ClaimConfiguration claimConfiguration;
    private List<InboundProtocolListItem> inboundProtocols = null;
    private AuthenticationSequence authenticationSequence;
    private AdvancedApplicationConfiguration advancedConfigurations;
    private ProvisioningConfiguration provisioningConfigurations;

    @XmlType(name="AccessEnum")
    @XmlEnum()
    public enum AccessEnum {

        @XmlEnumValue("READ") READ("READ"), @XmlEnumValue("WRITE") WRITE("WRITE");


        private final String value;

        AccessEnum(String v) {
            value = v;
        }

        public String value() {
            return value;
        }

        @Override
        public String toString() {
            return String.valueOf(value);
        }

        public static AccessEnum fromValue(String value) {
            for (AccessEnum b : AccessEnum.values()) {
                if (b.value.equals(value)) {
                    return b;
                }
            }
            throw new IllegalArgumentException("Unexpected value '" + value + "'");
        }
    }

    private AccessEnum access = AccessEnum.READ;

    /**
     **/
    public ApplicationResponseModel id(String id) {

        this.id = id;
        return this;
    }
    
    @ApiModelProperty(example = "394b8adcce24c64a8a09a0d80abf8c337bd253de")
    @JsonProperty("id")
    @Valid
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    /**
     **/
    public ApplicationResponseModel name(String name) {

        this.name = name;
        return this;
    }
    
    @ApiModelProperty(example = "pickup", required = true)
    @JsonProperty("name")
    @Valid
    @NotNull(message = "Property name cannot be null.")
    @Pattern(regexp="^[a-zA-Z0-9._-]+(?: [a-zA-Z0-9._-]+)*$")
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    /**
     **/
    public ApplicationResponseModel description(String description) {

        this.description = description;
        return this;
    }
    
    @ApiModelProperty(example = "This is the configuration for Pickup application.")
    @JsonProperty("description")
    @Valid
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     **/
    public ApplicationResponseModel imageUrl(String imageUrl) {

        this.imageUrl = imageUrl;
        return this;
    }
    
    @ApiModelProperty(example = "https://example.com/logo/my-logo.png")
    @JsonProperty("imageUrl")
    @Valid
    public String getImageUrl() {
        return imageUrl;
    }
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    /**
     **/
    public ApplicationResponseModel accessUrl(String accessUrl) {

        this.accessUrl = accessUrl;
        return this;
    }
    
    @ApiModelProperty(example = "https://example.com/login")
    @JsonProperty("accessUrl")
    @Valid
    public String getAccessUrl() {
        return accessUrl;
    }
    public void setAccessUrl(String accessUrl) {
        this.accessUrl = accessUrl;
    }

    /**
     **/
    public ApplicationResponseModel logoutReturnUrl(String logoutReturnUrl) {

        this.logoutReturnUrl = logoutReturnUrl;
        return this;
    }

    @ApiModelProperty(example = "https://example.com/app/logout", value = "")
    @JsonProperty("logoutReturnUrl")
    @Valid
    public String getLogoutReturnUrl() {
        return logoutReturnUrl;
    }
    public void setLogoutReturnUrl(String logoutReturnUrl) {
        this.logoutReturnUrl = logoutReturnUrl;
    }

    /**
     **/
    public ApplicationResponseModel clientId(String clientId) {

        this.clientId = clientId;
        return this;
    }

    @ApiModelProperty(example = "SmrrDNXRYf1lMmDlnleeHTuXx_Ea")
    @JsonProperty("clientId")
    @Valid
    public String getClientId() {
        return clientId;
    }
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    /**
     **/
    public ApplicationResponseModel issuer(String issuer) {

        this.issuer = issuer;
        return this;
    }

    @ApiModelProperty(example = "http://idp.example.com/metadata.php")
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
    public ApplicationResponseModel realm(String realm) {

        this.realm = realm;
        return this;
    }

    @ApiModelProperty(example = "PassiveSTSSampleApp")
    @JsonProperty("realm")
    @Valid
    public String getRealm() {
        return realm;
    }
    public void setRealm(String realm) {
        this.realm = realm;
    }

    /**
     **/
    public ApplicationResponseModel templateId(String templateId) {

        this.templateId = templateId;
        return this;
    }

    @ApiModelProperty(example = "980b8tester24c64a8a09a0d80abf8c337bd2555")
    @JsonProperty("templateId")
    @Valid
    public String getTemplateId() {
        return templateId;
    }
    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    /**
     * Decides whether the application used to access System APIs
     **/
    public ApplicationResponseModel isManagementApp(Boolean isManagementApp) {

        this.isManagementApp = isManagementApp;
        return this;
    }

    @ApiModelProperty(example = "false")
    @JsonProperty("isManagementApp")
    @Valid
    public Boolean getIsManagementApp() {
        return isManagementApp;
    }
    public void setIsManagementApp(Boolean isManagementApp) {
        this.isManagementApp = isManagementApp;
    }

    /**
     * Decides whether the application used to for B2B self service
     **/
    public ApplicationResponseModel isB2BSelfServiceApp(Boolean isB2BSelfServiceApp) {

        this.isB2BSelfServiceApp = isB2BSelfServiceApp;
        return this;
    }

    @ApiModelProperty(example = "false", value = "Decides whether the application used to for B2B self service")
    @JsonProperty("isB2BSelfServiceApp")
    @Valid
    public Boolean getIsB2BSelfServiceApp() {
        return isB2BSelfServiceApp;
    }
    public void setIsB2BSelfServiceApp(Boolean isB2BSelfServiceApp) {
        this.isB2BSelfServiceApp = isB2BSelfServiceApp;
    }

    /**
     **/
    public ApplicationResponseModel associatedRoles(AssociatedRolesConfig associatedRoles) {

        this.associatedRoles = associatedRoles;
        return this;
    }

    @ApiModelProperty()
    @JsonProperty("associatedRoles")
    @Valid
    public AssociatedRolesConfig getAssociatedRoles() {
        return associatedRoles;
    }
    public void setAssociatedRoles(AssociatedRolesConfig associatedRoles) {
        this.associatedRoles = associatedRoles;
    }

    /**
     **/
    public ApplicationResponseModel claimConfiguration(ClaimConfiguration claimConfiguration) {

        this.claimConfiguration = claimConfiguration;
        return this;
    }
    
    @ApiModelProperty()
    @JsonProperty("claimConfiguration")
    @Valid
    public ClaimConfiguration getClaimConfiguration() {
        return claimConfiguration;
    }
    public void setClaimConfiguration(ClaimConfiguration claimConfiguration) {
        this.claimConfiguration = claimConfiguration;
    }

    /**
     **/
    public ApplicationResponseModel inboundProtocols(List<InboundProtocolListItem> inboundProtocols) {

        this.inboundProtocols = inboundProtocols;
        return this;
    }
    
    @ApiModelProperty()
    @JsonProperty("inboundProtocols")
    @Valid
    public List<InboundProtocolListItem> getInboundProtocols() {
        return inboundProtocols;
    }
    public void setInboundProtocols(List<InboundProtocolListItem> inboundProtocols) {
        this.inboundProtocols = inboundProtocols;
    }

    /**
     **/
    public ApplicationResponseModel authenticationSequence(AuthenticationSequence authenticationSequence) {

        this.authenticationSequence = authenticationSequence;
        return this;
    }
    
    @ApiModelProperty()
    @JsonProperty("authenticationSequence")
    @Valid
    public AuthenticationSequence getAuthenticationSequence() {
        return authenticationSequence;
    }
    public void setAuthenticationSequence(AuthenticationSequence authenticationSequence) {
        this.authenticationSequence = authenticationSequence;
    }

    /**
     **/
    public ApplicationResponseModel advancedConfigurations(AdvancedApplicationConfiguration advancedConfigurations) {

        this.advancedConfigurations = advancedConfigurations;
        return this;
    }
    
    @ApiModelProperty()
    @JsonProperty("advancedConfigurations")
    @Valid
    public AdvancedApplicationConfiguration getAdvancedConfigurations() {
        return advancedConfigurations;
    }
    public void setAdvancedConfigurations(AdvancedApplicationConfiguration advancedConfigurations) {
        this.advancedConfigurations = advancedConfigurations;
    }

    /**
     **/
    public ApplicationResponseModel provisioningConfigurations(ProvisioningConfiguration provisioningConfigurations) {

        this.provisioningConfigurations = provisioningConfigurations;
        return this;
    }
    
    @ApiModelProperty()
    @JsonProperty("provisioningConfigurations")
    @Valid
    public ProvisioningConfiguration getProvisioningConfigurations() {
        return provisioningConfigurations;
    }
    public void setProvisioningConfigurations(ProvisioningConfiguration provisioningConfigurations) {
        this.provisioningConfigurations = provisioningConfigurations;
    }

    /**
     **/
    public ApplicationResponseModel access(AccessEnum access) {

        this.access = access;
        return this;
    }

    @ApiModelProperty()
    @JsonProperty("access")
    @Valid
    public AccessEnum getAccess() {
        return access;
    }
    public void setAccess(AccessEnum access) {
        this.access = access;
    }



    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ApplicationResponseModel applicationResponseModel = (ApplicationResponseModel) o;
        return Objects.equals(this.id, applicationResponseModel.id) &&
                Objects.equals(this.name, applicationResponseModel.name) &&
                Objects.equals(this.description, applicationResponseModel.description) &&
                Objects.equals(this.imageUrl, applicationResponseModel.imageUrl) &&
                Objects.equals(this.accessUrl, applicationResponseModel.accessUrl) &&
                Objects.equals(this.logoutReturnUrl, applicationResponseModel.logoutReturnUrl) &&
                Objects.equals(this.clientId, applicationResponseModel.clientId) &&
                Objects.equals(this.issuer, applicationResponseModel.issuer) &&
                Objects.equals(this.realm, applicationResponseModel.realm) &&
                Objects.equals(this.templateId, applicationResponseModel.templateId) &&
                Objects.equals(this.isManagementApp, applicationResponseModel.isManagementApp) &&
                Objects.equals(this.isB2BSelfServiceApp, applicationResponseModel.isB2BSelfServiceApp) &&
                Objects.equals(this.associatedRoles, applicationResponseModel.associatedRoles) &&
                Objects.equals(this.claimConfiguration, applicationResponseModel.claimConfiguration) &&
                Objects.equals(this.inboundProtocols, applicationResponseModel.inboundProtocols) &&
                Objects.equals(this.authenticationSequence, applicationResponseModel.authenticationSequence) &&
                Objects.equals(this.advancedConfigurations, applicationResponseModel.advancedConfigurations) &&
                Objects.equals(this.provisioningConfigurations, applicationResponseModel.provisioningConfigurations) &&
                Objects.equals(this.access, applicationResponseModel.access);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, description, imageUrl, accessUrl, logoutReturnUrl, clientId, issuer, realm, templateId, isManagementApp, isB2BSelfServiceApp, associatedRoles, claimConfiguration, inboundProtocols, authenticationSequence, advancedConfigurations, provisioningConfigurations, access);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class ApplicationResponseModel {\n");
        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    description: ").append(toIndentedString(description)).append("\n");
        sb.append("    imageUrl: ").append(toIndentedString(imageUrl)).append("\n");
        sb.append("    accessUrl: ").append(toIndentedString(accessUrl)).append("\n");
        sb.append("    logoutReturnUrl: ").append(toIndentedString(logoutReturnUrl)).append("\n");
        sb.append("    clientId: ").append(toIndentedString(clientId)).append("\n");
        sb.append("    issuer: ").append(toIndentedString(issuer)).append("\n");
        sb.append("    realm: ").append(toIndentedString(realm)).append("\n");
        sb.append("    templateId: ").append(toIndentedString(templateId)).append("\n");
        sb.append("    isManagementApp: ").append(toIndentedString(isManagementApp)).append("\n");
        sb.append("    isB2BSelfServiceApp: ").append(toIndentedString(isB2BSelfServiceApp)).append("\n");
        sb.append("    associatedRoles: ").append(toIndentedString(associatedRoles)).append("\n");
        sb.append("    claimConfiguration: ").append(toIndentedString(claimConfiguration)).append("\n");
        sb.append("    inboundProtocols: ").append(toIndentedString(inboundProtocols)).append("\n");
        sb.append("    authenticationSequence: ").append(toIndentedString(authenticationSequence)).append("\n");
        sb.append("    advancedConfigurations: ").append(toIndentedString(advancedConfigurations)).append("\n");
        sb.append("    provisioningConfigurations: ").append(toIndentedString(provisioningConfigurations)).append("\n");
        sb.append("    access: ").append(toIndentedString(access)).append("\n");
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

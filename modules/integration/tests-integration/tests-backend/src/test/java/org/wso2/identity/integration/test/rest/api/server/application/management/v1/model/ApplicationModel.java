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

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

public class ApplicationModel  {
  
    private String id;
    private String name;
    private String description;
    private String imageUrl;
    private String accessUrl;
    private Boolean isManagementApp;
    private ClaimConfiguration claimConfiguration;
    private InboundProtocols inboundProtocolConfiguration;
    private AuthenticationSequence authenticationSequence;
    private AdvancedApplicationConfiguration advancedConfigurations;
    private ProvisioningConfiguration provisioningConfigurations;

    /**
    **/
    public ApplicationModel id(String id) {

        this.id = id;
        return this;
    }
    
    @ApiModelProperty(example = "394b8adcce24c64a8a09a0d80abf8c337bd253de", value = "")
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
    public ApplicationModel name(String name) {

        this.name = name;
        return this;
    }
    
    @ApiModelProperty(example = "pickup", required = true, value = "")
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
    public ApplicationModel description(String description) {

        this.description = description;
        return this;
    }
    
    @ApiModelProperty(example = "This is the configuration for Pickup application.", value = "")
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
    public ApplicationModel imageUrl(String imageUrl) {

        this.imageUrl = imageUrl;
        return this;
    }
    
    @ApiModelProperty(example = "https://example.com/logo/my-logo.png", value = "")
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
    public ApplicationModel accessUrl(String accessUrl) {

        this.accessUrl = accessUrl;
        return this;
    }
    
    @ApiModelProperty(example = "https://example.com/home", value = "")
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
    public ApplicationModel isManagementApp(Boolean isManagementApp) {

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
    **/
    public ApplicationModel claimConfiguration(ClaimConfiguration claimConfiguration) {

        this.claimConfiguration = claimConfiguration;
        return this;
    }
    
    @ApiModelProperty(value = "")
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
    public ApplicationModel inboundProtocolConfiguration(InboundProtocols inboundProtocolConfiguration) {

        this.inboundProtocolConfiguration = inboundProtocolConfiguration;
        return this;
    }
    
    @ApiModelProperty(value = "")
    @JsonProperty("inboundProtocolConfiguration")
    @Valid
    public InboundProtocols getInboundProtocolConfiguration() {
        return inboundProtocolConfiguration;
    }
    public void setInboundProtocolConfiguration(InboundProtocols inboundProtocolConfiguration) {
        this.inboundProtocolConfiguration = inboundProtocolConfiguration;
    }

    /**
    **/
    public ApplicationModel authenticationSequence(AuthenticationSequence authenticationSequence) {

        this.authenticationSequence = authenticationSequence;
        return this;
    }
    
    @ApiModelProperty(value = "")
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
    public ApplicationModel advancedConfigurations(AdvancedApplicationConfiguration advancedConfigurations) {

        this.advancedConfigurations = advancedConfigurations;
        return this;
    }
    
    @ApiModelProperty(value = "")
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
    public ApplicationModel provisioningConfigurations(ProvisioningConfiguration provisioningConfigurations) {

        this.provisioningConfigurations = provisioningConfigurations;
        return this;
    }
    
    @ApiModelProperty(value = "")
    @JsonProperty("provisioningConfigurations")
    @Valid
    public ProvisioningConfiguration getProvisioningConfigurations() {
        return provisioningConfigurations;
    }
    public void setProvisioningConfigurations(ProvisioningConfiguration provisioningConfigurations) {
        this.provisioningConfigurations = provisioningConfigurations;
    }



    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ApplicationModel applicationModel = (ApplicationModel) o;
        return Objects.equals(this.id, applicationModel.id) &&
            Objects.equals(this.name, applicationModel.name) &&
            Objects.equals(this.description, applicationModel.description) &&
            Objects.equals(this.imageUrl, applicationModel.imageUrl) &&
            Objects.equals(this.accessUrl, applicationModel.accessUrl) &&
            Objects.equals(this.isManagementApp, applicationModel.isManagementApp) &&
            Objects.equals(this.claimConfiguration, applicationModel.claimConfiguration) &&
            Objects.equals(this.inboundProtocolConfiguration, applicationModel.inboundProtocolConfiguration) &&
            Objects.equals(this.authenticationSequence, applicationModel.authenticationSequence) &&
            Objects.equals(this.advancedConfigurations, applicationModel.advancedConfigurations) &&
            Objects.equals(this.provisioningConfigurations, applicationModel.provisioningConfigurations);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, description, imageUrl, accessUrl, isManagementApp, claimConfiguration,
                inboundProtocolConfiguration, authenticationSequence, advancedConfigurations, provisioningConfigurations);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class ApplicationModel {\n");

        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    description: ").append(toIndentedString(description)).append("\n");
        sb.append("    imageUrl: ").append(toIndentedString(imageUrl)).append("\n");
        sb.append("    loginUrl: ").append(toIndentedString(accessUrl)).append("\n");
        sb.append("    isManagementApp: ").append(toIndentedString(isManagementApp)).append("\n");
        sb.append("    claimConfiguration: ").append(toIndentedString(claimConfiguration)).append("\n");
        sb.append("    inboundProtocolConfiguration: ").append(toIndentedString(inboundProtocolConfiguration)).append("\n");
        sb.append("    authenticationSequence: ").append(toIndentedString(authenticationSequence)).append("\n");
        sb.append("    advancedConfigurations: ").append(toIndentedString(advancedConfigurations)).append("\n");
        sb.append("    provisioningConfigurations: ").append(toIndentedString(provisioningConfigurations)).append("\n");
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


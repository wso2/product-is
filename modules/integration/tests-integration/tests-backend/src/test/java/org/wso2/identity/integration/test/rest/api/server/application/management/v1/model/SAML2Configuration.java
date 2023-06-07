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

import java.util.Objects;
import javax.validation.Valid;

public class SAML2Configuration  {
  
    private String metadataFile;
    private String metadataURL;
    private SAML2ServiceProvider manualConfiguration;

    /**
    **/
    public SAML2Configuration metadataFile(String metadataFile) {

        this.metadataFile = metadataFile;
        return this;
    }
    
    @ApiModelProperty(example = "Base64 encoded metadata file content", value = "")
    @JsonProperty("metadataFile")
    @Valid
    public String getMetadataFile() {
        return metadataFile;
    }
    public void setMetadataFile(String metadataFile) {
        this.metadataFile = metadataFile;
    }

    /**
    **/
    public SAML2Configuration metadataURL(String metadataURL) {

        this.metadataURL = metadataURL;
        return this;
    }
    
    @ApiModelProperty(example = "https://example.com/samlsso/meta", value = "")
    @JsonProperty("metadataURL")
    @Valid
    public String getMetadataURL() {
        return metadataURL;
    }
    public void setMetadataURL(String metadataURL) {
        this.metadataURL = metadataURL;
    }

    /**
    **/
    public SAML2Configuration manualConfiguration(SAML2ServiceProvider serviceProvider) {

        this.manualConfiguration = serviceProvider;
        return this;
    }
    
    @ApiModelProperty(value = "")
    @JsonProperty("serviceProvider")
    @Valid
    public SAML2ServiceProvider getManualConfiguration() {
        return manualConfiguration;
    }
    public void setManualConfiguration(SAML2ServiceProvider manualConfiguration) {
        this.manualConfiguration = manualConfiguration;
    }



    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SAML2Configuration saML2Configuration = (SAML2Configuration) o;
        return Objects.equals(this.metadataFile, saML2Configuration.metadataFile) &&
            Objects.equals(this.metadataURL, saML2Configuration.metadataURL) &&
            Objects.equals(this.manualConfiguration, saML2Configuration.manualConfiguration);
    }

    @Override
    public int hashCode() {
        return Objects.hash(metadataFile, metadataURL, manualConfiguration);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class SAML2Configuration {\n");

        sb.append("    metadataFile: ").append(toIndentedString(metadataFile)).append("\n");
        sb.append("    metadataURL: ").append(toIndentedString(metadataURL)).append("\n");
        sb.append("    serviceProvider: ").append(toIndentedString(manualConfiguration)).append("\n");
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


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
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.Valid;
import java.util.Objects;

public class AdvancedApplicationConfigurationAttestationMetaData {
  
    private Boolean enableClientAttestation;
    private String androidPackageName;
    private Object androidAttestationServiceCredentials;
    private String appleAppId;

    /**
    * Decides whether client attestation enabled for this application.
    **/
    public AdvancedApplicationConfigurationAttestationMetaData enableClientAttestation(Boolean enableClientAttestation) {

        this.enableClientAttestation = enableClientAttestation;
        return this;
    }
    
    @ApiModelProperty(example = "false", value = "Decides whether client attestation enabled for this application.")
    @JsonProperty("enableClientAttestation")
    @Valid
    public Boolean getEnableClientAttestation() {
        return enableClientAttestation;
    }
    public void setEnableClientAttestation(Boolean enableClientAttestation) {
        this.enableClientAttestation = enableClientAttestation;
    }

    /**
    * Decides the android package name of the application.
    **/
    public AdvancedApplicationConfigurationAttestationMetaData androidPackageName(String androidPackageName) {

        this.androidPackageName = androidPackageName;
        return this;
    }
    
    @ApiModelProperty(example = "com.wso2.mobile.sample", value = "Decides the android package name of the application.")
    @JsonProperty("androidPackageName")
    @Valid
    public String getAndroidPackageName() {
        return androidPackageName;
    }
    public void setAndroidPackageName(String androidPackageName) {
        this.androidPackageName = androidPackageName;
    }

    /**
    * Decides the credentials for the service account to access Google Play Integrity Service.
    **/
    public AdvancedApplicationConfigurationAttestationMetaData androidAttestationServiceCredentials(Object androidAttestationServiceCredentials) {

        this.androidAttestationServiceCredentials = androidAttestationServiceCredentials;
        return this;
    }
    
    @ApiModelProperty(value = "Decides the credentials for the service account to access Google Play Integrity Service.")
    @JsonProperty("androidAttestationServiceCredentials")
    @Valid
    public Object getAndroidAttestationServiceCredentials() {
        return androidAttestationServiceCredentials;
    }
    public void setAndroidAttestationServiceCredentials(Object androidAttestationServiceCredentials) {
        this.androidAttestationServiceCredentials = androidAttestationServiceCredentials;
    }

    /**
    * Decides the apple app id which denotes {apple-teamId}.{bundleId}.
    **/
    public AdvancedApplicationConfigurationAttestationMetaData appleAppId(String appleAppId) {

        this.appleAppId = appleAppId;
        return this;
    }
    
    @ApiModelProperty(example = "APPLETEAMID.com.wso2.mobile.sample", value = "Decides the apple app id which denotes {apple-teamId}.{bundleId}.")
    @JsonProperty("appleAppId")
    @Valid
    public String getAppleAppId() {
        return appleAppId;
    }
    public void setAppleAppId(String appleAppId) {
        this.appleAppId = appleAppId;
    }



    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AdvancedApplicationConfigurationAttestationMetaData advancedApplicationConfigurationAttestationMetaData = (AdvancedApplicationConfigurationAttestationMetaData) o;
        return Objects.equals(this.enableClientAttestation, advancedApplicationConfigurationAttestationMetaData.enableClientAttestation) &&
            Objects.equals(this.androidPackageName, advancedApplicationConfigurationAttestationMetaData.androidPackageName) &&
            Objects.equals(this.androidAttestationServiceCredentials, advancedApplicationConfigurationAttestationMetaData.androidAttestationServiceCredentials) &&
            Objects.equals(this.appleAppId, advancedApplicationConfigurationAttestationMetaData.appleAppId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(enableClientAttestation, androidPackageName, androidAttestationServiceCredentials, appleAppId);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class AdvancedApplicationConfigurationAttestationMetaData {\n");
        
        sb.append("    enableClientAttestation: ").append(toIndentedString(enableClientAttestation)).append("\n");
        sb.append("    androidPackageName: ").append(toIndentedString(androidPackageName)).append("\n");
        sb.append("    androidAttestationServiceCredentials: ").append(toIndentedString(androidAttestationServiceCredentials)).append("\n");
        sb.append("    appleAppId: ").append(toIndentedString(appleAppId)).append("\n");
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


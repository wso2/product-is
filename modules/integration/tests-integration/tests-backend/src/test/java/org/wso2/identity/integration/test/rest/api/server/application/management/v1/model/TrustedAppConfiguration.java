/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@ApiModel(description = "Decides the trusted app configurations for the application.")
public class TrustedAppConfiguration {
  
    private Boolean isFIDOTrustedApp;
    private Boolean isConsentGranted;
    private String androidPackageName;
    private List<String> androidThumbprints = null;

    private String appleAppId;

    /**
    * Decides whether the application is a FIDO trusted app.
    **/
    public TrustedAppConfiguration isFIDOTrustedApp(Boolean isFIDOTrustedApp) {

        this.isFIDOTrustedApp = isFIDOTrustedApp;
        return this;
    }
    
    @ApiModelProperty(example = "false", value = "Decides whether the application is a FIDO trusted app.")
    @JsonProperty("isFIDOTrustedApp")
    @Valid
    public Boolean getIsFIDOTrustedApp() {
        return isFIDOTrustedApp;
    }
    public void setIsFIDOTrustedApp(Boolean isFIDOTrustedApp) {
        this.isFIDOTrustedApp = isFIDOTrustedApp;
    }

    /**
    * Decides whether consent is granted for the trusted app.
    **/
    public TrustedAppConfiguration isConsentGranted(Boolean isConsentGranted) {

        this.isConsentGranted = isConsentGranted;
        return this;
    }
    
    @ApiModelProperty(example = "false", value = "Decides whether consent is granted for the trusted app.")
    @JsonProperty("isConsentGranted")
    @Valid
    public Boolean getIsConsentGranted() {
        return isConsentGranted;
    }
    public void setIsConsentGranted(Boolean isConsentGranted) {
        this.isConsentGranted = isConsentGranted;
    }

    /**
    * Decides the android package name for the application.
    **/
    public TrustedAppConfiguration androidPackageName(String androidPackageName) {

        this.androidPackageName = androidPackageName;
        return this;
    }
    
    @ApiModelProperty(example = "com.wso2.mobile.sample", value = "Decides the android package name for the application.")
    @JsonProperty("androidPackageName")
    @Valid
    public String getAndroidPackageName() {
        return androidPackageName;
    }
    public void setAndroidPackageName(String androidPackageName) {
        this.androidPackageName = androidPackageName;
    }

    /**
    * Decides the android thumbprints for the application.
    **/
    public TrustedAppConfiguration androidThumbprints(List<String> androidThumbprints) {

        this.androidThumbprints = androidThumbprints;
        return this;
    }
    
    @ApiModelProperty(value = "Decides the android thumbprints for the application.")
    @JsonProperty("androidThumbprints")
    @Valid
    public List<String> getAndroidThumbprints() {
        return androidThumbprints;
    }
    public void setAndroidThumbprints(List<String> androidThumbprints) {
        this.androidThumbprints = androidThumbprints;
    }

    public TrustedAppConfiguration addAndroidThumbprintsItem(String androidThumbprintsItem) {
        if (this.androidThumbprints == null) {
            this.androidThumbprints = new ArrayList<>();
        }
        this.androidThumbprints.add(androidThumbprintsItem);
        return this;
    }

        /**
    * Decides the apple app id for the application.
    **/
    public TrustedAppConfiguration appleAppId(String appleAppId) {

        this.appleAppId = appleAppId;
        return this;
    }
    
    @ApiModelProperty(example = "APPLETEAMID.com.org.mobile.sample", value = "Decides the apple app id for the application.")
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
        TrustedAppConfiguration trustedAppConfiguration = (TrustedAppConfiguration) o;
        return Objects.equals(this.isFIDOTrustedApp, trustedAppConfiguration.isFIDOTrustedApp) &&
            Objects.equals(this.isConsentGranted, trustedAppConfiguration.isConsentGranted) &&
            Objects.equals(this.androidPackageName, trustedAppConfiguration.androidPackageName) &&
            Objects.equals(this.androidThumbprints, trustedAppConfiguration.androidThumbprints) &&
            Objects.equals(this.appleAppId, trustedAppConfiguration.appleAppId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(isFIDOTrustedApp, isConsentGranted, androidPackageName, androidThumbprints, appleAppId);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class TrustedAppConfiguration {\n");
        
        sb.append("    isFIDOTrustedApp: ").append(toIndentedString(isFIDOTrustedApp)).append("\n");
        sb.append("    isConsentGranted: ").append(toIndentedString(isConsentGranted)).append("\n");
        sb.append("    androidPackageName: ").append(toIndentedString(androidPackageName)).append("\n");
        sb.append("    androidThumbprints: ").append(toIndentedString(androidThumbprints)).append("\n");
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


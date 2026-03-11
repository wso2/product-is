/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
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

public class CIBAMetadata {
  
    private List<CIBANotificationChannel> supportedNotificationChannels = null;


    /**
    **/
    public CIBAMetadata supportedNotificationChannels(List<CIBANotificationChannel> supportedNotificationChannels) {

        this.supportedNotificationChannels = supportedNotificationChannels;
        return this;
    }
    
    @ApiModelProperty(value = "")
    @JsonProperty("supportedNotificationChannels")
    @Valid
    public List<CIBANotificationChannel> getSupportedNotificationChannels() {
        return supportedNotificationChannels;
    }
    public void setSupportedNotificationChannels(List<CIBANotificationChannel> supportedNotificationChannels) {
        this.supportedNotificationChannels = supportedNotificationChannels;
    }

    public CIBAMetadata addSupportedNotificationChannelsItem(CIBANotificationChannel supportedNotificationChannelsItem) {
        if (this.supportedNotificationChannels == null) {
            this.supportedNotificationChannels = new ArrayList<>();
        }
        this.supportedNotificationChannels.add(supportedNotificationChannelsItem);
        return this;
    }

    

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CIBAMetadata ciBAMetadata = (CIBAMetadata) o;
        return Objects.equals(this.supportedNotificationChannels, ciBAMetadata.supportedNotificationChannels);
    }

    @Override
    public int hashCode() {
        return Objects.hash(supportedNotificationChannels);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class CIBAMetadata {\n");
        
        sb.append("    supportedNotificationChannels: ").append(toIndentedString(supportedNotificationChannels)).append("\n");
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


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
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;
import javax.validation.Valid;
import javax.xml.bind.annotation.*;

public class CIBAAuthenticationRequestConfiguration  {

    private Long authReqExpiryTime;
    private List<String> notificationChannels = null;

    private Boolean skipUserValidation = false;
    private Boolean allowFederatedUsers = false;

    /**
     * CIBA authentication request expiry time in seconds.
     **/
    public CIBAAuthenticationRequestConfiguration authReqExpiryTime(Long authReqExpiryTime) {

        this.authReqExpiryTime = authReqExpiryTime;
        return this;
    }

    @ApiModelProperty(example = "600", value = "CIBA authentication request expiry time in seconds.")
    @JsonProperty("authReqExpiryTime")
    @Valid
    public Long getAuthReqExpiryTime() {
        return authReqExpiryTime;
    }
    public void setAuthReqExpiryTime(Long authReqExpiryTime) {
        this.authReqExpiryTime = authReqExpiryTime;
    }

    /**
     * List of allowed notification channels.
     **/
    public CIBAAuthenticationRequestConfiguration notificationChannels(List<String> notificationChannels) {

        this.notificationChannels = notificationChannels;
        return this;
    }

    @ApiModelProperty(example = "[\"email\",\"sms\"]", value = "List of allowed notification channels.")
    @JsonProperty("notificationChannels")
    @Valid
    public List<String> getNotificationChannels() {
        return notificationChannels;
    }
    public void setNotificationChannels(List<String> notificationChannels) {
        this.notificationChannels = notificationChannels;
    }

    public CIBAAuthenticationRequestConfiguration addNotificationChannelsItem(String notificationChannelsItem) {
        if (this.notificationChannels == null) {
            this.notificationChannels = new ArrayList<>();
        }
        this.notificationChannels.add(notificationChannelsItem);
        return this;
    }

    /**
     * Skip validation that the authenticated user matches the resolved user from login_hint during token issuance.
     **/
    public CIBAAuthenticationRequestConfiguration skipUserValidation(Boolean skipUserValidation) {

        this.skipUserValidation = skipUserValidation;
        return this;
    }

    @ApiModelProperty(example = "false", value = "Skip validation that the authenticated user matches the resolved user from login_hint during token issuance.")
    @JsonProperty("skipUserValidation")
    @Valid
    public Boolean getSkipUserValidation() {
        return skipUserValidation;
    }
    public void setSkipUserValidation(Boolean skipUserValidation) {
        this.skipUserValidation = skipUserValidation;
    }

    /**
     * Allow sending CIBA notifications to users not found in the local user store. When enabled, the notification is sent directly to the login_hint value using the notification_channel to determine if it is an email or phone number. Requires skipUserValidation to be enabled.
     **/
    public CIBAAuthenticationRequestConfiguration allowFederatedUsers(Boolean allowFederatedUsers) {

        this.allowFederatedUsers = allowFederatedUsers;
        return this;
    }

    @ApiModelProperty(example = "false", value = "Allow sending CIBA notifications to users not found in the local user store. When enabled, the notification is sent directly to the login_hint value using the notification_channel to determine if it is an email or phone number. Requires skipUserValidation to be enabled.")
    @JsonProperty("allowFederatedUsers")
    @Valid
    public Boolean getAllowFederatedUsers() {
        return allowFederatedUsers;
    }
    public void setAllowFederatedUsers(Boolean allowFederatedUsers) {
        this.allowFederatedUsers = allowFederatedUsers;
    }



    @Override
    public boolean equals(java.lang.Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CIBAAuthenticationRequestConfiguration ciBAAuthenticationRequestConfiguration = (CIBAAuthenticationRequestConfiguration) o;
        return Objects.equals(this.authReqExpiryTime, ciBAAuthenticationRequestConfiguration.authReqExpiryTime) &&
                Objects.equals(this.notificationChannels, ciBAAuthenticationRequestConfiguration.notificationChannels) &&
                Objects.equals(this.skipUserValidation, ciBAAuthenticationRequestConfiguration.skipUserValidation) &&
                Objects.equals(this.allowFederatedUsers, ciBAAuthenticationRequestConfiguration.allowFederatedUsers);
    }

    @Override
    public int hashCode() {
        return Objects.hash(authReqExpiryTime, notificationChannels, skipUserValidation, allowFederatedUsers);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class CIBAAuthenticationRequestConfiguration {\n");

        sb.append("    authReqExpiryTime: ").append(toIndentedString(authReqExpiryTime)).append("\n");
        sb.append("    notificationChannels: ").append(toIndentedString(notificationChannels)).append("\n");
        sb.append("    skipUserValidation: ").append(toIndentedString(skipUserValidation)).append("\n");
        sb.append("    allowFederatedUsers: ").append(toIndentedString(allowFederatedUsers)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(java.lang.Object o) {

        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n");
    }
}


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

public class RefreshTokenConfiguration  {

    private Long expiryInSeconds;
    private Boolean extendRenewedRefreshTokenExpiryTime;
    private Boolean renewRefreshToken;

    /**
     **/
    public RefreshTokenConfiguration expiryInSeconds(Long expiryInSeconds) {

        this.expiryInSeconds = expiryInSeconds;
        return this;
    }

    @ApiModelProperty(example = "86400", value = "")
    @JsonProperty("expiryInSeconds")
    @Valid
    public Long getExpiryInSeconds() {
        return expiryInSeconds;
    }
    public void setExpiryInSeconds(Long expiryInSeconds) {
        this.expiryInSeconds = expiryInSeconds;
    }

    /**
     * Decides whether the expiry time of the renewed refresh token needs to be extended based on the refresh token expiry time configured.
     **/
    public RefreshTokenConfiguration extendRenewedRefreshTokenExpiryTime(Boolean extendRenewedRefreshTokenExpiryTime) {

        this.extendRenewedRefreshTokenExpiryTime = extendRenewedRefreshTokenExpiryTime;
        return this;
    }

    @ApiModelProperty(example = "false", value = "Decides whether the expiry time of the renewed refresh token needs to be extended based on the refresh token expiry time configured.")
    @JsonProperty("extendRenewedRefreshTokenExpiryTime")
    @Valid
    public Boolean getExtendRenewedRefreshTokenExpiryTime() {
        return extendRenewedRefreshTokenExpiryTime;
    }
    public void setExtendRenewedRefreshTokenExpiryTime(Boolean extendRenewedRefreshTokenExpiryTime) {
        this.extendRenewedRefreshTokenExpiryTime = extendRenewedRefreshTokenExpiryTime;
    }

    /**
     * Decides whether the refresh token needs to be renewed during refresh grant flow.
     **/
    public RefreshTokenConfiguration renewRefreshToken(Boolean renewRefreshToken) {

        this.renewRefreshToken = renewRefreshToken;
        return this;
    }

    @ApiModelProperty(example = "true", value = "Decides whether the refresh token needs to be renewed during refresh grant flow.")
    @JsonProperty("renewRefreshToken")
    @Valid
    public Boolean getRenewRefreshToken() {
        return renewRefreshToken;
    }
    public void setRenewRefreshToken(Boolean renewRefreshToken) {
        this.renewRefreshToken = renewRefreshToken;
    }



    @Override
    public boolean equals(java.lang.Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RefreshTokenConfiguration refreshTokenConfiguration = (RefreshTokenConfiguration) o;
        return Objects.equals(this.expiryInSeconds, refreshTokenConfiguration.expiryInSeconds) &&
                Objects.equals(this.extendRenewedRefreshTokenExpiryTime, refreshTokenConfiguration.extendRenewedRefreshTokenExpiryTime) &&
                Objects.equals(this.renewRefreshToken, refreshTokenConfiguration.renewRefreshToken);
    }

    @Override
    public int hashCode() {
        return Objects.hash(expiryInSeconds, extendRenewedRefreshTokenExpiryTime, renewRefreshToken);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class RefreshTokenConfiguration {\n");

        sb.append("    expiryInSeconds: ").append(toIndentedString(expiryInSeconds)).append("\n");
        sb.append("    extendRenewedRefreshTokenExpiryTime: ").append(toIndentedString(extendRenewedRefreshTokenExpiryTime)).append("\n");
        sb.append("    renewRefreshToken: ").append(toIndentedString(renewRefreshToken)).append("\n");
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


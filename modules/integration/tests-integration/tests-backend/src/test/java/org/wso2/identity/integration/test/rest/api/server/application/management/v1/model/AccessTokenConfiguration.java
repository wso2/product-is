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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.validation.Valid;

public class AccessTokenConfiguration  {

    private String type;
    private Long userAccessTokenExpiryInSeconds;
    private Long applicationAccessTokenExpiryInSeconds;
    private String bindingType = "None";
    private Boolean revokeTokensWhenIDPSessionTerminated;
    private Boolean validateTokenBinding;
    private List<String> accessTokenAttributes = null;


    /**
     **/
    public AccessTokenConfiguration type(String type) {

        this.type = type;
        return this;
    }

    @ApiModelProperty(example = "JWT", value = "")
    @JsonProperty("type")
    @Valid
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }

    /**
     **/
    public AccessTokenConfiguration userAccessTokenExpiryInSeconds(Long userAccessTokenExpiryInSeconds) {

        this.userAccessTokenExpiryInSeconds = userAccessTokenExpiryInSeconds;
        return this;
    }

    @ApiModelProperty(example = "3600", value = "")
    @JsonProperty("userAccessTokenExpiryInSeconds")
    @Valid
    public Long getUserAccessTokenExpiryInSeconds() {
        return userAccessTokenExpiryInSeconds;
    }
    public void setUserAccessTokenExpiryInSeconds(Long userAccessTokenExpiryInSeconds) {
        this.userAccessTokenExpiryInSeconds = userAccessTokenExpiryInSeconds;
    }

    /**
     **/
    public AccessTokenConfiguration applicationAccessTokenExpiryInSeconds(Long applicationAccessTokenExpiryInSeconds) {

        this.applicationAccessTokenExpiryInSeconds = applicationAccessTokenExpiryInSeconds;
        return this;
    }

    @ApiModelProperty(example = "3600", value = "")
    @JsonProperty("applicationAccessTokenExpiryInSeconds")
    @Valid
    public Long getApplicationAccessTokenExpiryInSeconds() {
        return applicationAccessTokenExpiryInSeconds;
    }
    public void setApplicationAccessTokenExpiryInSeconds(Long applicationAccessTokenExpiryInSeconds) {
        this.applicationAccessTokenExpiryInSeconds = applicationAccessTokenExpiryInSeconds;
    }

    /**
     * OAuth2 access token and refresh token can be bound to an external attribute during the token generation so that it can be optionally validated during the API invocation.
     **/
    public AccessTokenConfiguration bindingType(String bindingType) {

        this.bindingType = bindingType;
        return this;
    }

    @ApiModelProperty(example = "cookie", value = "OAuth2 access token and refresh token can be bound to an external attribute during the token generation so that it can be optionally validated during the API invocation.")
    @JsonProperty("bindingType")
    @Valid
    public String getBindingType() {
        return bindingType;
    }
    public void setBindingType(String bindingType) {
        this.bindingType = bindingType;
    }

    /**
     * If enabled, when the IDP session is terminated, all the access tokens bound to the session will get revoked.
     **/
    public AccessTokenConfiguration revokeTokensWhenIDPSessionTerminated(Boolean revokeTokensWhenIDPSessionTerminated) {

        this.revokeTokensWhenIDPSessionTerminated = revokeTokensWhenIDPSessionTerminated;
        return this;
    }

    @ApiModelProperty(value = "If enabled, when the IDP session is terminated, all the access tokens bound to the session will get revoked.")
    @JsonProperty("revokeTokensWhenIDPSessionTerminated")
    @Valid
    public Boolean getRevokeTokensWhenIDPSessionTerminated() {
        return revokeTokensWhenIDPSessionTerminated;
    }
    public void setRevokeTokensWhenIDPSessionTerminated(Boolean revokeTokensWhenIDPSessionTerminated) {
        this.revokeTokensWhenIDPSessionTerminated = revokeTokensWhenIDPSessionTerminated;
    }

    /**
     * If enabled, both access token and the token binding needs to be present for a successful API invocation.
     **/
    public AccessTokenConfiguration validateTokenBinding(Boolean validateTokenBinding) {

        this.validateTokenBinding = validateTokenBinding;
        return this;
    }

    @ApiModelProperty(value = "If enabled, both access token and the token binding needs to be present for a successful API invocation.")
    @JsonProperty("validateTokenBinding")
    @Valid
    public Boolean getValidateTokenBinding() {
        return validateTokenBinding;
    }
    public void setValidateTokenBinding(Boolean validateTokenBinding) {
        this.validateTokenBinding = validateTokenBinding;
    }

    /**
     **/
    public AccessTokenConfiguration accessTokenAttributes(List<String> accessTokenAttributes) {

        this.accessTokenAttributes = accessTokenAttributes;
        return this;
    }

    @ApiModelProperty(value = "")
    @JsonProperty("accessTokenAttributes")
    @Valid
    public List<String> getAccessTokenAttributes() {
        return accessTokenAttributes;
    }
    public void setAccessTokenAttributes(List<String> accessTokenAttributes) {
        this.accessTokenAttributes = accessTokenAttributes;
    }

    public AccessTokenConfiguration addAccessTokenAttributesItem(String accessTokenAttributesItem) {
        if (this.accessTokenAttributes == null) {
            this.accessTokenAttributes = new ArrayList<>();
        }
        this.accessTokenAttributes.add(accessTokenAttributesItem);
        return this;
    }



    @Override
    public boolean equals(java.lang.Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AccessTokenConfiguration accessTokenConfiguration = (AccessTokenConfiguration) o;
        return Objects.equals(this.type, accessTokenConfiguration.type) &&
                Objects.equals(this.userAccessTokenExpiryInSeconds, accessTokenConfiguration.userAccessTokenExpiryInSeconds) &&
                Objects.equals(this.applicationAccessTokenExpiryInSeconds, accessTokenConfiguration.applicationAccessTokenExpiryInSeconds) &&
                Objects.equals(this.bindingType, accessTokenConfiguration.bindingType) &&
                Objects.equals(this.revokeTokensWhenIDPSessionTerminated, accessTokenConfiguration.revokeTokensWhenIDPSessionTerminated) &&
                Objects.equals(this.validateTokenBinding, accessTokenConfiguration.validateTokenBinding) &&
                Objects.equals(this.accessTokenAttributes, accessTokenConfiguration.accessTokenAttributes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, userAccessTokenExpiryInSeconds, applicationAccessTokenExpiryInSeconds, bindingType, revokeTokensWhenIDPSessionTerminated, validateTokenBinding, accessTokenAttributes);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class AccessTokenConfiguration {\n");

        sb.append("    type: ").append(toIndentedString(type)).append("\n");
        sb.append("    userAccessTokenExpiryInSeconds: ").append(toIndentedString(userAccessTokenExpiryInSeconds)).append("\n");
        sb.append("    applicationAccessTokenExpiryInSeconds: ").append(toIndentedString(applicationAccessTokenExpiryInSeconds)).append("\n");
        sb.append("    bindingType: ").append(toIndentedString(bindingType)).append("\n");
        sb.append("    revokeTokensWhenIDPSessionTerminated: ").append(toIndentedString(revokeTokensWhenIDPSessionTerminated)).append("\n");
        sb.append("    validateTokenBinding: ").append(toIndentedString(validateTokenBinding)).append("\n");
        sb.append("    accessTokenAttributes: ").append(toIndentedString(accessTokenAttributes)).append("\n");
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


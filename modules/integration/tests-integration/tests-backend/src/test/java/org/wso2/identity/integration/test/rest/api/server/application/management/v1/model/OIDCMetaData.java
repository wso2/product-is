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

public class OIDCMetaData  {

    private GrantTypeMetaData allowedGrantTypes;
    private String defaultUserAccessTokenExpiryTime;
    private String defaultApplicationAccessTokenExpiryTime;
    private String defaultRefreshTokenExpiryTime;
    private String defaultIdTokenExpiryTime;
    private MetadataProperty idTokenEncryptionAlgorithm;
    private MetadataProperty idTokenEncryptionMethod;
    private MetadataProperty scopeValidators;
    private MetadataProperty accessTokenType;
    private MetadataProperty accessTokenBindingType;

    /**
     **/
    public OIDCMetaData allowedGrantTypes(GrantTypeMetaData allowedGrantTypes) {

        this.allowedGrantTypes = allowedGrantTypes;
        return this;
    }

    @ApiModelProperty(value = "")
    @JsonProperty("allowedGrantTypes")
    @Valid
    public GrantTypeMetaData getAllowedGrantTypes() {
        return allowedGrantTypes;
    }
    public void setAllowedGrantTypes(GrantTypeMetaData allowedGrantTypes) {
        this.allowedGrantTypes = allowedGrantTypes;
    }

    /**
     **/
    public OIDCMetaData defaultUserAccessTokenExpiryTime(String defaultUserAccessTokenExpiryTime) {

        this.defaultUserAccessTokenExpiryTime = defaultUserAccessTokenExpiryTime;
        return this;
    }

    @ApiModelProperty(example = "3600", value = "")
    @JsonProperty("defaultUserAccessTokenExpiryTime")
    @Valid
    public String getDefaultUserAccessTokenExpiryTime() {
        return defaultUserAccessTokenExpiryTime;
    }
    public void setDefaultUserAccessTokenExpiryTime(String defaultUserAccessTokenExpiryTime) {
        this.defaultUserAccessTokenExpiryTime = defaultUserAccessTokenExpiryTime;
    }

    /**
     **/
    public OIDCMetaData defaultApplicationAccessTokenExpiryTime(String defaultApplicationAccessTokenExpiryTime) {

        this.defaultApplicationAccessTokenExpiryTime = defaultApplicationAccessTokenExpiryTime;
        return this;
    }

    @ApiModelProperty(example = "3600", value = "")
    @JsonProperty("defaultApplicationAccessTokenExpiryTime")
    @Valid
    public String getDefaultApplicationAccessTokenExpiryTime() {
        return defaultApplicationAccessTokenExpiryTime;
    }
    public void setDefaultApplicationAccessTokenExpiryTime(String defaultApplicationAccessTokenExpiryTime) {
        this.defaultApplicationAccessTokenExpiryTime = defaultApplicationAccessTokenExpiryTime;
    }

    /**
     **/
    public OIDCMetaData defaultRefreshTokenExpiryTime(String defaultRefreshTokenExpiryTime) {

        this.defaultRefreshTokenExpiryTime = defaultRefreshTokenExpiryTime;
        return this;
    }

    @ApiModelProperty(example = "86400", value = "")
    @JsonProperty("defaultRefreshTokenExpiryTime")
    @Valid
    public String getDefaultRefreshTokenExpiryTime() {
        return defaultRefreshTokenExpiryTime;
    }
    public void setDefaultRefreshTokenExpiryTime(String defaultRefreshTokenExpiryTime) {
        this.defaultRefreshTokenExpiryTime = defaultRefreshTokenExpiryTime;
    }

    /**
     **/
    public OIDCMetaData defaultIdTokenExpiryTime(String defaultIdTokenExpiryTime) {

        this.defaultIdTokenExpiryTime = defaultIdTokenExpiryTime;
        return this;
    }

    @ApiModelProperty(example = "3600", value = "")
    @JsonProperty("defaultIdTokenExpiryTime")
    @Valid
    public String getDefaultIdTokenExpiryTime() {
        return defaultIdTokenExpiryTime;
    }
    public void setDefaultIdTokenExpiryTime(String defaultIdTokenExpiryTime) {
        this.defaultIdTokenExpiryTime = defaultIdTokenExpiryTime;
    }

    /**
     **/
    public OIDCMetaData idTokenEncryptionAlgorithm(MetadataProperty idTokenEncryptionAlgorithm) {

        this.idTokenEncryptionAlgorithm = idTokenEncryptionAlgorithm;
        return this;
    }

    @ApiModelProperty(value = "")
    @JsonProperty("idTokenEncryptionAlgorithm")
    @Valid
    public MetadataProperty getIdTokenEncryptionAlgorithm() {
        return idTokenEncryptionAlgorithm;
    }
    public void setIdTokenEncryptionAlgorithm(MetadataProperty idTokenEncryptionAlgorithm) {
        this.idTokenEncryptionAlgorithm = idTokenEncryptionAlgorithm;
    }

    /**
     **/
    public OIDCMetaData idTokenEncryptionMethod(MetadataProperty idTokenEncryptionMethod) {

        this.idTokenEncryptionMethod = idTokenEncryptionMethod;
        return this;
    }

    @ApiModelProperty(value = "")
    @JsonProperty("idTokenEncryptionMethod")
    @Valid
    public MetadataProperty getIdTokenEncryptionMethod() {
        return idTokenEncryptionMethod;
    }
    public void setIdTokenEncryptionMethod(MetadataProperty idTokenEncryptionMethod) {
        this.idTokenEncryptionMethod = idTokenEncryptionMethod;
    }

    /**
     **/
    public OIDCMetaData scopeValidators(MetadataProperty scopeValidators) {

        this.scopeValidators = scopeValidators;
        return this;
    }

    @ApiModelProperty(value = "")
    @JsonProperty("scopeValidators")
    @Valid
    public MetadataProperty getScopeValidators() {
        return scopeValidators;
    }
    public void setScopeValidators(MetadataProperty scopeValidators) {
        this.scopeValidators = scopeValidators;
    }

    /**
     **/
    public OIDCMetaData accessTokenType(MetadataProperty accessTokenType) {

        this.accessTokenType = accessTokenType;
        return this;
    }

    @ApiModelProperty(value = "")
    @JsonProperty("accessTokenType")
    @Valid
    public MetadataProperty getAccessTokenType() {
        return accessTokenType;
    }
    public void setAccessTokenType(MetadataProperty accessTokenType) {
        this.accessTokenType = accessTokenType;
    }

    /**
     **/
    public OIDCMetaData accessTokenBindingType(MetadataProperty accessTokenBindingType) {

        this.accessTokenBindingType = accessTokenBindingType;
        return this;
    }

    @ApiModelProperty(value = "")
    @JsonProperty("accessTokenBindingType")
    @Valid
    public MetadataProperty getAccessTokenBindingType() {
        return accessTokenBindingType;
    }
    public void setAccessTokenBindingType(MetadataProperty accessTokenBindingType) {
        this.accessTokenBindingType = accessTokenBindingType;
    }

    @Override
    public boolean equals(java.lang.Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        OIDCMetaData oiDCMetaData = (OIDCMetaData) o;
        return Objects.equals(this.allowedGrantTypes, oiDCMetaData.allowedGrantTypes) &&
                Objects.equals(this.defaultUserAccessTokenExpiryTime, oiDCMetaData.defaultUserAccessTokenExpiryTime) &&
                Objects.equals(this.defaultApplicationAccessTokenExpiryTime, oiDCMetaData.defaultApplicationAccessTokenExpiryTime) &&
                Objects.equals(this.defaultRefreshTokenExpiryTime, oiDCMetaData.defaultRefreshTokenExpiryTime) &&
                Objects.equals(this.defaultIdTokenExpiryTime, oiDCMetaData.defaultIdTokenExpiryTime) &&
                Objects.equals(this.idTokenEncryptionAlgorithm, oiDCMetaData.idTokenEncryptionAlgorithm) &&
                Objects.equals(this.idTokenEncryptionMethod, oiDCMetaData.idTokenEncryptionMethod) &&
                Objects.equals(this.scopeValidators, oiDCMetaData.scopeValidators) &&
                Objects.equals(this.accessTokenType, oiDCMetaData.accessTokenType) &&
                Objects.equals(this.accessTokenBindingType, oiDCMetaData.accessTokenBindingType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(allowedGrantTypes, defaultUserAccessTokenExpiryTime, defaultApplicationAccessTokenExpiryTime, defaultRefreshTokenExpiryTime, defaultIdTokenExpiryTime, idTokenEncryptionAlgorithm, idTokenEncryptionMethod, scopeValidators, accessTokenType, accessTokenBindingType);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class OIDCMetaData {\n");

        sb.append("    allowedGrantTypes: ").append(toIndentedString(allowedGrantTypes)).append("\n");
        sb.append("    defaultUserAccessTokenExpiryTime: ").append(toIndentedString(defaultUserAccessTokenExpiryTime)).append("\n");
        sb.append("    defaultApplicationAccessTokenExpiryTime: ").append(toIndentedString(defaultApplicationAccessTokenExpiryTime)).append("\n");
        sb.append("    defaultRefreshTokenExpiryTime: ").append(toIndentedString(defaultRefreshTokenExpiryTime)).append("\n");
        sb.append("    defaultIdTokenExpiryTime: ").append(toIndentedString(defaultIdTokenExpiryTime)).append("\n");
        sb.append("    idTokenEncryptionAlgorithm: ").append(toIndentedString(idTokenEncryptionAlgorithm)).append("\n");
        sb.append("    idTokenEncryptionMethod: ").append(toIndentedString(idTokenEncryptionMethod)).append("\n");
        sb.append("    scopeValidators: ").append(toIndentedString(scopeValidators)).append("\n");
        sb.append("    accessTokenType: ").append(toIndentedString(accessTokenType)).append("\n");
        sb.append("    accessTokenBindingType: ").append(toIndentedString(accessTokenBindingType)).append("\n");
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

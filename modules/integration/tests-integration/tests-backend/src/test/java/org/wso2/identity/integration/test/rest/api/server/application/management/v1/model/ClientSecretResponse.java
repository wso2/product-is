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

import javax.validation.Valid;
import javax.xml.bind.annotation.XmlEnumValue;
import java.util.Objects;

public class ClientSecretResponse {

    private String secretId;
    private String secretValue;
    private Long expiresAt;
    private Long createdAt;

    public enum StatusEnum {

        @XmlEnumValue("ACTIVE") ACTIVE(String.valueOf("ACTIVE")),
        @XmlEnumValue("EXPIRED") EXPIRED(String.valueOf("EXPIRED"));


        private String value;

        StatusEnum(String v) {
            value = v;
        }

        public String value() {
            return value;
        }

        @Override
        public String toString() {
            return String.valueOf(value);
        }

        public static StatusEnum fromValue(String value) {
            for (StatusEnum b : StatusEnum.values()) {
                if (b.value.equals(value)) {
                    return b;
                }
            }
            throw new IllegalArgumentException("Unexpected value '" + value + "'");
        }
    }

    private StatusEnum status;
    private Boolean latest;

    /**
    * Identifier of the secret.
    **/
    public ClientSecretResponse secretId(String secretId) {

        this.secretId = secretId;
        return this;
    }

    @ApiModelProperty(example = "4a7f9b23-c1e0-4d86-b5fa-28c93a71b4ef", value = "Identifier of the secret.")
    @JsonProperty("secretId")
    @Valid
    public String getSecretId() {
        return secretId;
    }
    public void setSecretId(String secretId) {
        this.secretId = secretId;
    }

    /**
    * The client secret value, as maintained in the configured client secret persistence mode.
    **/
    public ClientSecretResponse secretValue(String secretValue) {

        this.secretValue = secretValue;
        return this;
    }

    @ApiModelProperty(example = "AbCd1234EfGh5678IjKlMnOp", value = "The client secret value, as maintained in the " +
            "configured client secret persistence mode.")
    @JsonProperty("secretValue")
    @Valid
    public String getSecretValue() {
        return secretValue;
    }
    public void setSecretValue(String secretValue) {
        this.secretValue = secretValue;
    }

    /**
    * Expiry time as Unix epoch seconds; 0 if the secret never expires.
    **/
    public ClientSecretResponse expiresAt(Long expiresAt) {

        this.expiresAt = expiresAt;
        return this;
    }

    @ApiModelProperty(example = "1761568483", value = "Expiry time as Unix epoch seconds; 0 if the secret never " +
            "expires.")
    @JsonProperty("expiresAt")
    @Valid
    public Long getExpiresAt() {
        return expiresAt;
    }
    public void setExpiresAt(Long expiresAt) {
        this.expiresAt = expiresAt;
    }

    /**
    * Creation time as Unix epoch seconds.
    **/
    public ClientSecretResponse createdAt(Long createdAt) {

        this.createdAt = createdAt;
        return this;
    }

    @ApiModelProperty(example = "1761568483", value = "Creation time as Unix epoch seconds.")
    @JsonProperty("createdAt")
    @Valid
    public Long getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

    /**
    * Status of the secret.
    **/
    public ClientSecretResponse status(StatusEnum status) {

        this.status = status;
        return this;
    }

    @ApiModelProperty(example = "ACTIVE", value = "Status of the secret.")
    @JsonProperty("status")
    @Valid
    public StatusEnum getStatus() {
        return status;
    }
    public void setStatus(StatusEnum status) {
        this.status = status;
    }

    /**
    * Whether this is the latest (active) secret of the application. The latest secret cannot be deleted.
    **/
    public ClientSecretResponse latest(Boolean latest) {

        this.latest = latest;
        return this;
    }

    @ApiModelProperty(example = "true", value = "Whether this is the latest (active) secret of the application. " +
            "The latest secret cannot be deleted.")
    @JsonProperty("latest")
    @Valid
    public Boolean getLatest() {
        return latest;
    }
    public void setLatest(Boolean latest) {
        this.latest = latest;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ClientSecretResponse clientSecretResponse = (ClientSecretResponse) o;
        return Objects.equals(this.secretId, clientSecretResponse.secretId) &&
            Objects.equals(this.secretValue, clientSecretResponse.secretValue) &&
            Objects.equals(this.expiresAt, clientSecretResponse.expiresAt) &&
            Objects.equals(this.createdAt, clientSecretResponse.createdAt) &&
            Objects.equals(this.status, clientSecretResponse.status) &&
            Objects.equals(this.latest, clientSecretResponse.latest);
    }

    @Override
    public int hashCode() {
        return Objects.hash(secretId, secretValue, expiresAt, createdAt, status, latest);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class ClientSecretResponse {\n");

        sb.append("    secretId: ").append(toIndentedString(secretId)).append("\n");
        sb.append("    secretValue: ").append(toIndentedString(secretValue)).append("\n");
        sb.append("    expiresAt: ").append(toIndentedString(expiresAt)).append("\n");
        sb.append("    createdAt: ").append(toIndentedString(createdAt)).append("\n");
        sb.append("    status: ").append(toIndentedString(status)).append("\n");
        sb.append("    latest: ").append(toIndentedString(latest)).append("\n");
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

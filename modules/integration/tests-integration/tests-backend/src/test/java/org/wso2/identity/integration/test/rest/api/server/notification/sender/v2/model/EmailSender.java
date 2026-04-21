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

package org.wso2.identity.integration.test.rest.api.server.notification.sender.v2.model;

import com.google.gson.annotations.SerializedName;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Email Sender model for V2 API.
 */
public class EmailSender {

    @SerializedName("name")
    private String name = null;

    @SerializedName("provider")
    private String provider = null;

    @SerializedName("providerURL")
    private String providerURL = null;

    @SerializedName("fromAddress")
    private String fromAddress = null;

    @SerializedName("smtpServerHost")
    private String smtpServerHost = null;

    @SerializedName("smtpPort")
    private Integer smtpPort = null;

    @SerializedName("authType")
    private String authType = null;

    @SerializedName("properties")
    private List<Properties> properties = null;

    public EmailSender name(String name) {
        this.name = name;
        return this;
    }

    @Schema(example = "EmailPublisher", required = true, description = "")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public EmailSender provider(String provider) {
        this.provider = provider;
        return this;
    }

    @Schema(example = "HTTP", description = "")
    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public EmailSender providerURL(String providerURL) {
        this.providerURL = providerURL;
        return this;
    }

    @Schema(example = "https://webhook.site/example", description = "")
    public String getProviderURL() {
        return providerURL;
    }

    public void setProviderURL(String providerURL) {
        this.providerURL = providerURL;
    }

    public EmailSender fromAddress(String fromAddress) {
        this.fromAddress = fromAddress;
        return this;
    }

    @Schema(example = "iam@gmail.com", description = "")
    public String getFromAddress() {
        return fromAddress;
    }

    public void setFromAddress(String fromAddress) {
        this.fromAddress = fromAddress;
    }

    public EmailSender smtpServerHost(String smtpServerHost) {
        this.smtpServerHost = smtpServerHost;
        return this;
    }

    @Schema(example = "smtp.gmail.com", description = "")
    public String getSmtpServerHost() {
        return smtpServerHost;
    }

    public void setSmtpServerHost(String smtpServerHost) {
        this.smtpServerHost = smtpServerHost;
    }

    public EmailSender smtpPort(Integer smtpPort) {
        this.smtpPort = smtpPort;
        return this;
    }

    @Schema(example = "587", description = "")
    public Integer getSmtpPort() {
        return smtpPort;
    }

    public void setSmtpPort(Integer smtpPort) {
        this.smtpPort = smtpPort;
    }

    public EmailSender authType(String authType) {
        this.authType = authType;
        return this;
    }

    @Schema(example = "BASIC", description = "")
    public String getAuthType() {
        return authType;
    }

    public void setAuthType(String authType) {
        this.authType = authType;
    }

    public EmailSender properties(List<Properties> properties) {
        this.properties = properties;
        return this;
    }

    public EmailSender addPropertiesItem(Properties propertiesItem) {
        if (this.properties == null) {
            this.properties = new ArrayList<>();
        }
        this.properties.add(propertiesItem);
        return this;
    }

    @Schema(description = "")
    public List<Properties> getProperties() {
        return properties;
    }

    public void setProperties(List<Properties> properties) {
        this.properties = properties;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        EmailSender emailSender = (EmailSender) o;
        return Objects.equals(this.name, emailSender.name) &&
                Objects.equals(this.provider, emailSender.provider) &&
                Objects.equals(this.providerURL, emailSender.providerURL) &&
                Objects.equals(this.fromAddress, emailSender.fromAddress) &&
                Objects.equals(this.smtpServerHost, emailSender.smtpServerHost) &&
                Objects.equals(this.smtpPort, emailSender.smtpPort) &&
                Objects.equals(this.authType, emailSender.authType) &&
                Objects.equals(this.properties, emailSender.properties);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, provider, providerURL, fromAddress, smtpServerHost, smtpPort, authType,
                properties);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class EmailSender {\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    provider: ").append(toIndentedString(provider)).append("\n");
        sb.append("    providerURL: ").append(toIndentedString(providerURL)).append("\n");
        sb.append("    fromAddress: ").append(toIndentedString(fromAddress)).append("\n");
        sb.append("    smtpServerHost: ").append(toIndentedString(smtpServerHost)).append("\n");
        sb.append("    smtpPort: ").append(toIndentedString(smtpPort)).append("\n");
        sb.append("    authType: ").append(toIndentedString(authType)).append("\n");
        sb.append("    properties: ").append(toIndentedString(properties)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    private String toIndentedString(Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}

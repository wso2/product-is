/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.identity.integration.test.rest.api.server.webhook.management.v1.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

public class WebhookRequest  {
  
    private String endpoint;
    private WebhookRequestEventProfile eventProfile;
    private String name;
    private String secret;
    private List<String> channelsSubscribed = new ArrayList<String>();


@XmlType(name="StatusEnum")
@XmlEnum(String.class)
public enum StatusEnum {

    @XmlEnumValue("ACTIVE") ACTIVE(String.valueOf("ACTIVE")), @XmlEnumValue("INACTIVE") INACTIVE(String.valueOf("INACTIVE"));


    private String value;

    StatusEnum(String v) {
        value = v;
    }

    @JsonValue
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

    /**
    * Webhook URL.
    **/
    public WebhookRequest endpoint(String endpoint) {

        this.endpoint = endpoint;
        return this;
    }
    
    @ApiModelProperty(example = "https://example.com/webhook", required = true, value = "Webhook URL.")
    @JsonProperty("endpoint")
    @Valid
    @NotNull(message = "Property endpoint cannot be null.")

    public String getEndpoint() {
        return endpoint;
    }
    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    /**
    **/
    public WebhookRequest eventProfile(WebhookRequestEventProfile eventProfile) {

        this.eventProfile = eventProfile;
        return this;
    }
    
    @ApiModelProperty(required = true, value = "")
    @JsonProperty("eventProfile")
    @Valid
    @NotNull(message = "Property eventProfile cannot be null.")

    public WebhookRequestEventProfile getEventProfile() {
        return eventProfile;
    }
    public void setEventProfile(WebhookRequestEventProfile eventProfile) {
        this.eventProfile = eventProfile;
    }

    /**
    * Webhook name.
    **/
    public WebhookRequest name(String name) {

        this.name = name;
        return this;
    }
    
    @ApiModelProperty(example = "Login Webhook.", required = true, value = "Webhook name.")
    @JsonProperty("name")
    @Valid
    @NotNull(message = "Property name cannot be null.")

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    /**
    * Secret for validating webhook payloads.
    **/
    public WebhookRequest secret(String secret) {

        this.secret = secret;
        return this;
    }
    
    @ApiModelProperty(example = "my-secret", value = "Secret for validating webhook payloads.")
    @JsonProperty("secret")
    @Valid
    public String getSecret() {
        return secret;
    }
    public void setSecret(String secret) {
        this.secret = secret;
    }

    /**
    * List of channels to subscribe to.
    **/
    public WebhookRequest channelsSubscribed(List<String> channelsSubscribed) {

        this.channelsSubscribed = channelsSubscribed;
        return this;
    }
    
    @ApiModelProperty(example = "[\"https://schemas.identity.wso2.org/events/login\",\"https://schemas.identity.wso2.org/events/registration\"]", required = true, value = "List of channels to subscribe to.")
    @JsonProperty("channelsSubscribed")
    @Valid
    @NotNull(message = "Property channelsSubscribed cannot be null.")

    public List<String> getChannelsSubscribed() {
        return channelsSubscribed;
    }
    public void setChannelsSubscribed(List<String> channelsSubscribed) {
        this.channelsSubscribed = channelsSubscribed;
    }

    public WebhookRequest addChannelsSubscribedItem(String channelsSubscribedItem) {
        this.channelsSubscribed.add(channelsSubscribedItem);
        return this;
    }

        /**
    * Webhook Status.
    **/
    public WebhookRequest status(StatusEnum status) {

        this.status = status;
        return this;
    }
    
    @ApiModelProperty(example = "ACTIVE", value = "Webhook Status.")
    @JsonProperty("status")
    @Valid
    public StatusEnum getStatus() {
        return status;
    }
    public void setStatus(StatusEnum status) {
        this.status = status;
    }



    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        WebhookRequest webhookRequest = (WebhookRequest) o;
        return Objects.equals(this.endpoint, webhookRequest.endpoint) &&
            Objects.equals(this.eventProfile, webhookRequest.eventProfile) &&
            Objects.equals(this.name, webhookRequest.name) &&
            Objects.equals(this.secret, webhookRequest.secret) &&
            Objects.equals(this.channelsSubscribed, webhookRequest.channelsSubscribed) &&
            Objects.equals(this.status, webhookRequest.status);
    }

    @Override
    public int hashCode() {
        return Objects.hash(endpoint, eventProfile, name, secret, channelsSubscribed, status);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class WebhookRequest {\n");
        
        sb.append("    endpoint: ").append(toIndentedString(endpoint)).append("\n");
        sb.append("    eventProfile: ").append(toIndentedString(eventProfile)).append("\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    secret: ").append(toIndentedString(secret)).append("\n");
        sb.append("    channelsSubscribed: ").append(toIndentedString(channelsSubscribed)).append("\n");
        sb.append("    status: ").append(toIndentedString(status)).append("\n");
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


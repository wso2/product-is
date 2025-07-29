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
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

public class WebhookResponse  {
  
    private String id;
    private String createdAt;
    private String updatedAt;
    private String endpoint;
    private WebhookRequestEventProfile eventProfile;
    private String name;
    private List<WebhookSubscription> channelsSubscribed = null;


@XmlType(name="StatusEnum")
@XmlEnum(String.class)
public enum StatusEnum {

    @XmlEnumValue("ACTIVE") ACTIVE(String.valueOf("ACTIVE")), @XmlEnumValue("PARTIALLY_ACTIVE") PARTIALLY_ACTIVE(String.valueOf("PARTIALLY_ACTIVE")), @XmlEnumValue("INACTIVE") INACTIVE(String.valueOf("INACTIVE")), @XmlEnumValue("PARTIALLY_INACTIVE") PARTIALLY_INACTIVE(String.valueOf("PARTIALLY_INACTIVE"));


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
    **/
    public WebhookResponse id(String id) {

        this.id = id;
        return this;
    }
    
    @ApiModelProperty(example = "eeb8c1a2-3f4d-4e5b-8c6f-7d8e9f0a1b2c", value = "")
    @JsonProperty("id")
    @Valid
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    /**
    **/
    public WebhookResponse createdAt(String createdAt) {

        this.createdAt = createdAt;
        return this;
    }
    
    @ApiModelProperty(example = "2024-05-01T12:00:00Z", value = "")
    @JsonProperty("createdAt")
    @Valid
    public String getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    /**
    **/
    public WebhookResponse updatedAt(String updatedAt) {

        this.updatedAt = updatedAt;
        return this;
    }
    
    @ApiModelProperty(example = "2024-05-02T12:00:00Z", value = "")
    @JsonProperty("updatedAt")
    @Valid
    public String getUpdatedAt() {
        return updatedAt;
    }
    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
    * Webhook URL.
    **/
    public WebhookResponse endpoint(String endpoint) {

        this.endpoint = endpoint;
        return this;
    }
    
    @ApiModelProperty(example = "https://example.com/webhook", value = "Webhook URL.")
    @JsonProperty("endpoint")
    @Valid
    public String getEndpoint() {
        return endpoint;
    }
    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    /**
    **/
    public WebhookResponse eventProfile(WebhookRequestEventProfile eventProfile) {

        this.eventProfile = eventProfile;
        return this;
    }
    
    @ApiModelProperty(value = "")
    @JsonProperty("eventProfile")
    @Valid
    public WebhookRequestEventProfile getEventProfile() {
        return eventProfile;
    }
    public void setEventProfile(WebhookRequestEventProfile eventProfile) {
        this.eventProfile = eventProfile;
    }

    /**
    * Webhook name.
    **/
    public WebhookResponse name(String name) {

        this.name = name;
        return this;
    }
    
    @ApiModelProperty(example = "Login webhook.", value = "Webhook name.")
    @JsonProperty("name")
    @Valid
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    /**
    * List of channel subscriptions with status.
    **/
    public WebhookResponse channelsSubscribed(List<WebhookSubscription> channelsSubscribed) {

        this.channelsSubscribed = channelsSubscribed;
        return this;
    }
    
    @ApiModelProperty(example = "[{\"channelUri\":\"https://schemas.identity.wso2.org/events/login\",\"status\":\"SUBSCRIPTION_ACCEPTED\"},{\"channelUri\":\"https://schemas.identity.wso2.org/events/registration\",\"status\":\"SUBSCRIPTION_ERROR\"}]", value = "List of channel subscriptions with status.")
    @JsonProperty("channelsSubscribed")
    @Valid
    public List<WebhookSubscription> getChannelsSubscribed() {
        return channelsSubscribed;
    }
    public void setChannelsSubscribed(List<WebhookSubscription> channelsSubscribed) {
        this.channelsSubscribed = channelsSubscribed;
    }

    public WebhookResponse addChannelsSubscribedItem(WebhookSubscription channelsSubscribedItem) {
        if (this.channelsSubscribed == null) {
            this.channelsSubscribed = new ArrayList<WebhookSubscription>();
        }
        this.channelsSubscribed.add(channelsSubscribedItem);
        return this;
    }

        /**
    * Webhook Status.
    **/
    public WebhookResponse status(StatusEnum status) {

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
        WebhookResponse webhookResponse = (WebhookResponse) o;
        return Objects.equals(this.id, webhookResponse.id) &&
            Objects.equals(this.createdAt, webhookResponse.createdAt) &&
            Objects.equals(this.updatedAt, webhookResponse.updatedAt) &&
            Objects.equals(this.endpoint, webhookResponse.endpoint) &&
            Objects.equals(this.eventProfile, webhookResponse.eventProfile) &&
            Objects.equals(this.name, webhookResponse.name) &&
            Objects.equals(this.channelsSubscribed, webhookResponse.channelsSubscribed) &&
            Objects.equals(this.status, webhookResponse.status);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, createdAt, updatedAt, endpoint, eventProfile, name, channelsSubscribed, status);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class WebhookResponse {\n");
        
        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    createdAt: ").append(toIndentedString(createdAt)).append("\n");
        sb.append("    updatedAt: ").append(toIndentedString(updatedAt)).append("\n");
        sb.append("    endpoint: ").append(toIndentedString(endpoint)).append("\n");
        sb.append("    eventProfile: ").append(toIndentedString(eventProfile)).append("\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
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


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

package org.wso2.identity.integration.test.rest.api.server.webhook.management.v2.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Request body to create a webhook through the webhook management v2 API.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WebhookCreateRequest {

    /**
     * Desired status of the webhook on create.
     */
    public enum StatusEnum {
        ACTIVE, INACTIVE
    }

    private String endpoint;
    private EventProfile eventProfile;
    private String name;
    private String secret;
    private List<WebhookSubscription> subscribedChannels = new ArrayList<>();
    private StatusEnum status;

    public WebhookCreateRequest endpoint(String endpoint) {

        this.endpoint = endpoint;
        return this;
    }

    @JsonProperty("endpoint")
    public String getEndpoint() {

        return endpoint;
    }

    public void setEndpoint(String endpoint) {

        this.endpoint = endpoint;
    }

    public WebhookCreateRequest eventProfile(EventProfile eventProfile) {

        this.eventProfile = eventProfile;
        return this;
    }

    @JsonProperty("eventProfile")
    public EventProfile getEventProfile() {

        return eventProfile;
    }

    public void setEventProfile(EventProfile eventProfile) {

        this.eventProfile = eventProfile;
    }

    public WebhookCreateRequest name(String name) {

        this.name = name;
        return this;
    }

    @JsonProperty("name")
    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }

    public WebhookCreateRequest secret(String secret) {

        this.secret = secret;
        return this;
    }

    @JsonProperty("secret")
    public String getSecret() {

        return secret;
    }

    public void setSecret(String secret) {

        this.secret = secret;
    }

    public WebhookCreateRequest subscribedChannels(List<WebhookSubscription> subscribedChannels) {

        this.subscribedChannels = subscribedChannels;
        return this;
    }

    public WebhookCreateRequest addSubscribedChannelsItem(WebhookSubscription subscribedChannelsItem) {

        this.subscribedChannels.add(subscribedChannelsItem);
        return this;
    }

    @JsonProperty("subscribedChannels")
    public List<WebhookSubscription> getSubscribedChannels() {

        return subscribedChannels;
    }

    public void setSubscribedChannels(List<WebhookSubscription> subscribedChannels) {

        this.subscribedChannels = subscribedChannels;
    }

    public WebhookCreateRequest status(StatusEnum status) {

        this.status = status;
        return this;
    }

    @JsonProperty("status")
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
        WebhookCreateRequest webhookCreateRequest = (WebhookCreateRequest) o;
        return Objects.equals(this.endpoint, webhookCreateRequest.endpoint) &&
                Objects.equals(this.eventProfile, webhookCreateRequest.eventProfile) &&
                Objects.equals(this.name, webhookCreateRequest.name) &&
                Objects.equals(this.secret, webhookCreateRequest.secret) &&
                Objects.equals(this.subscribedChannels, webhookCreateRequest.subscribedChannels) &&
                Objects.equals(this.status, webhookCreateRequest.status);
    }

    @Override
    public int hashCode() {

        return Objects.hash(endpoint, eventProfile, name, secret, subscribedChannels, status);
    }

    @Override
    public String toString() {

        return "class WebhookCreateRequest {\n" +
                "    endpoint: " + endpoint + "\n" +
                "    eventProfile: " + eventProfile + "\n" +
                "    name: " + name + "\n" +
                "    subscribedChannels: " + subscribedChannels + "\n" +
                "    status: " + status + "\n" +
                "}";
    }
}

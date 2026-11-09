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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Objects;

/**
 * Webhook as returned by the webhook management v2 API.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class WebhookResponse {

    private String id;
    private String endpoint;
    private EventProfile eventProfile;
    private String name;
    private List<WebhookSubscription> subscribedChannels;
    private String status;

    @JsonProperty("id")
    public String getId() {

        return id;
    }

    public void setId(String id) {

        this.id = id;
    }

    @JsonProperty("endpoint")
    public String getEndpoint() {

        return endpoint;
    }

    public void setEndpoint(String endpoint) {

        this.endpoint = endpoint;
    }

    @JsonProperty("eventProfile")
    public EventProfile getEventProfile() {

        return eventProfile;
    }

    public void setEventProfile(EventProfile eventProfile) {

        this.eventProfile = eventProfile;
    }

    @JsonProperty("name")
    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }

    @JsonProperty("subscribedChannels")
    public List<WebhookSubscription> getSubscribedChannels() {

        return subscribedChannels;
    }

    public void setSubscribedChannels(List<WebhookSubscription> subscribedChannels) {

        this.subscribedChannels = subscribedChannels;
    }

    /**
     * Aggregate webhook status derived from the channel subscription statuses. One of ACTIVE, PARTIALLY_ACTIVE,
     * INACTIVE or PARTIALLY_INACTIVE.
     */
    @JsonProperty("status")
    public String getStatus() {

        return status;
    }

    public void setStatus(String status) {

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
                Objects.equals(this.endpoint, webhookResponse.endpoint) &&
                Objects.equals(this.eventProfile, webhookResponse.eventProfile) &&
                Objects.equals(this.name, webhookResponse.name) &&
                Objects.equals(this.subscribedChannels, webhookResponse.subscribedChannels) &&
                Objects.equals(this.status, webhookResponse.status);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, endpoint, eventProfile, name, subscribedChannels, status);
    }

    @Override
    public String toString() {

        return "class WebhookResponse {\n" +
                "    id: " + id + "\n" +
                "    endpoint: " + endpoint + "\n" +
                "    eventProfile: " + eventProfile + "\n" +
                "    name: " + name + "\n" +
                "    subscribedChannels: " + subscribedChannels + "\n" +
                "    status: " + status + "\n" +
                "}";
    }
}

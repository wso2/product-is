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
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * A subscribed channel of a webhook, with its child organization fanout configuration.
 * The channel ID and status are read-only and populated only in responses.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WebhookSubscription {

    private String channelUri;
    private ChildOrgSubscription childOrgSubscription;
    private String channelId;
    private String status;

    public WebhookSubscription channelUri(String channelUri) {

        this.channelUri = channelUri;
        return this;
    }

    @JsonProperty("channelUri")
    public String getChannelUri() {

        return channelUri;
    }

    public void setChannelUri(String channelUri) {

        this.channelUri = channelUri;
    }

    public WebhookSubscription childOrgSubscription(ChildOrgSubscription childOrgSubscription) {

        this.childOrgSubscription = childOrgSubscription;
        return this;
    }

    @JsonProperty("childOrgSubscription")
    public ChildOrgSubscription getChildOrgSubscription() {

        return childOrgSubscription;
    }

    public void setChildOrgSubscription(ChildOrgSubscription childOrgSubscription) {

        this.childOrgSubscription = childOrgSubscription;
    }

    @JsonProperty("channelId")
    public String getChannelId() {

        return channelId;
    }

    public void setChannelId(String channelId) {

        this.channelId = channelId;
    }

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
        WebhookSubscription webhookSubscription = (WebhookSubscription) o;
        return Objects.equals(this.channelUri, webhookSubscription.channelUri) &&
                Objects.equals(this.childOrgSubscription, webhookSubscription.childOrgSubscription) &&
                Objects.equals(this.channelId, webhookSubscription.channelId) &&
                Objects.equals(this.status, webhookSubscription.status);
    }

    @Override
    public int hashCode() {

        return Objects.hash(channelUri, childOrgSubscription, channelId, status);
    }

    @Override
    public String toString() {

        return "class WebhookSubscription {\n" +
                "    channelUri: " + channelUri + "\n" +
                "    childOrgSubscription: " + childOrgSubscription + "\n" +
                "    channelId: " + channelId + "\n" +
                "    status: " + status + "\n" +
                "}";
    }
}

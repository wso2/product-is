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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Fanout configuration controlling which child organizations receive the events of a subscribed channel.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChildOrgSubscription {

    /**
     * Organization level fanout policy of a channel.
     */
    public enum SubscriptionPolicyEnum {
        NONE, ALL_EXISTING_AND_FUTURE_ORGS, SELECTED_ORGS_ONLY
    }

    private SubscriptionPolicyEnum subscriptionPolicy;
    private List<Organization> organizations;

    public ChildOrgSubscription subscriptionPolicy(SubscriptionPolicyEnum subscriptionPolicy) {

        this.subscriptionPolicy = subscriptionPolicy;
        return this;
    }

    @JsonProperty("subscriptionPolicy")
    public SubscriptionPolicyEnum getSubscriptionPolicy() {

        return subscriptionPolicy;
    }

    public void setSubscriptionPolicy(SubscriptionPolicyEnum subscriptionPolicy) {

        this.subscriptionPolicy = subscriptionPolicy;
    }

    public ChildOrgSubscription organizations(List<Organization> organizations) {

        this.organizations = organizations;
        return this;
    }

    public ChildOrgSubscription addOrganizationsItem(Organization organizationsItem) {

        if (this.organizations == null) {
            this.organizations = new ArrayList<>();
        }
        this.organizations.add(organizationsItem);
        return this;
    }

    @JsonProperty("organizations")
    public List<Organization> getOrganizations() {

        return organizations;
    }

    public void setOrganizations(List<Organization> organizations) {

        this.organizations = organizations;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChildOrgSubscription childOrgSubscription = (ChildOrgSubscription) o;
        return Objects.equals(this.subscriptionPolicy, childOrgSubscription.subscriptionPolicy) &&
                Objects.equals(this.organizations, childOrgSubscription.organizations);
    }

    @Override
    public int hashCode() {

        return Objects.hash(subscriptionPolicy, organizations);
    }

    @Override
    public String toString() {

        return "class ChildOrgSubscription {\n" +
                "    subscriptionPolicy: " + subscriptionPolicy + "\n" +
                "    organizations: " + organizations + "\n" +
                "}";
    }
}

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

import java.util.Objects;

/**
 * A child organization selected to receive the events of a subscribed channel.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Organization {

    private String orgId;

    public Organization orgId(String orgId) {

        this.orgId = orgId;
        return this;
    }

    @JsonProperty("orgId")
    public String getOrgId() {

        return orgId;
    }

    public void setOrgId(String orgId) {

        this.orgId = orgId;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        return Objects.equals(this.orgId, ((Organization) o).orgId);
    }

    @Override
    public int hashCode() {

        return Objects.hash(orgId);
    }

    @Override
    public String toString() {

        return "class Organization {\n" +
                "    orgId: " + orgId + "\n" +
                "}";
    }
}

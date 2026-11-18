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
 * Event profile of a webhook in the webhook management v2 API.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class EventProfile {

    private String name;
    private String uri;

    public EventProfile name(String name) {

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

    public EventProfile uri(String uri) {

        this.uri = uri;
        return this;
    }

    @JsonProperty("uri")
    public String getUri() {

        return uri;
    }

    public void setUri(String uri) {

        this.uri = uri;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        EventProfile eventProfile = (EventProfile) o;
        return Objects.equals(this.name, eventProfile.name) &&
                Objects.equals(this.uri, eventProfile.uri);
    }

    @Override
    public int hashCode() {

        return Objects.hash(name, uri);
    }

    @Override
    public String toString() {

        return "class EventProfile {\n" +
                "    name: " + name + "\n" +
                "    uri: " + uri + "\n" +
                "}";
    }
}

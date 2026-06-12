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

package org.wso2.identity.integration.test.serviceextensions.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import java.util.Objects;

/**
 * This class models the Session object sent in the request payload to the API endpoint of a pre issue access token
 * action.
 */
@JsonDeserialize(builder = Session.Builder.class)
public class Session {

    private final String sessionDataKeyConsent;

    private Session(Builder builder) {

        this.sessionDataKeyConsent = builder.sessionDataKeyConsent;
    }

    public String getSessionDataKeyConsent() {

        return sessionDataKeyConsent;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Session session = (Session) o;
        return Objects.equals(sessionDataKeyConsent, session.sessionDataKeyConsent);
    }

    @Override
    public int hashCode() {

        return Objects.hash(sessionDataKeyConsent);
    }

    /**
     * Builder for the Session.
     */
    @JsonPOJOBuilder(withPrefix = "")
    public static class Builder {

        private String sessionDataKeyConsent;

        @JsonProperty("sessionDataKeyConsent")
        public Builder sessionDataKeyConsent(String sessionDataKeyConsent) {

            this.sessionDataKeyConsent = sessionDataKeyConsent;
            return this;
        }

        public Session build() {

            return new Session(this);
        }
    }
}

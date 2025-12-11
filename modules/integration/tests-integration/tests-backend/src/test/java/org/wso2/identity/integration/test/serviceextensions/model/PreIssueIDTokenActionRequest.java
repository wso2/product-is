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

import java.util.List;
import java.util.Objects;

/**
 * This class models the Pre Issue ID Token Action Request sent in the request payload to the API endpoint of
 * Pre Issue ID Token action.
 */
@JsonDeserialize(builder = PreIssueIDTokenActionRequest.Builder.class)
public class PreIssueIDTokenActionRequest extends Request {

    private String requestId;
    private ActionType actionType;
    private PreIssueIDTokenEvent event;
    private List<AllowedOperation> allowedOperations;

    private PreIssueIDTokenActionRequest() {

    }

    private PreIssueIDTokenActionRequest(Builder builder) {

        this.requestId = builder.requestId;
        this.actionType = builder.actionType;
        this.event = builder.event;
        this.allowedOperations = builder.allowedOperations;
    }

    public static Builder builder() {

        return new Builder();
    }

    @JsonProperty("actionType")
    public ActionType getActionType() {

        return actionType;
    }

    public void setActionType(ActionType actionType) {

        this.actionType = actionType;
    }

    @JsonProperty("event")
    public PreIssueIDTokenEvent getEvent() {

        return event;
    }

    public void setEvent(PreIssueIDTokenEvent event) {

        this.event = event;
    }

    @JsonProperty("allowedOperations")
    public List<AllowedOperation> getAllowedOperations() {

        return allowedOperations;
    }

    public void setAllowedOperations(List<AllowedOperation> allowedOperations) {

        this.allowedOperations = allowedOperations;
    }

    /**
     * Builder for PreIssueIDTokenActionRequest.
     */
    public static class Builder {

        private String requestId;
        private ActionType actionType;
        private PreIssueIDTokenEvent event;
        private List<AllowedOperation> allowedOperations;

        public Builder requestId(String requestId) {

            this.requestId = requestId;
            return this;
        }

        public Builder actionType(ActionType actionType) {

            this.actionType = actionType;
            return this;
        }

        public Builder event(PreIssueIDTokenEvent event) {

            this.event = event;
            return this;
        }

        public Builder allowedOperations(List<AllowedOperation> allowedOperations) {

            this.allowedOperations = allowedOperations;
            return this;
        }

        public PreIssueIDTokenActionRequest build() {

            return new PreIssueIDTokenActionRequest(this);
        }
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PreIssueIDTokenActionRequest that = (PreIssueIDTokenActionRequest) o;
        return Objects.equals(actionType, that.actionType) &&
                Objects.equals(event, that.event) &&
                Objects.equals(allowedOperations, that.allowedOperations);
    }

    @Override
    public int hashCode() {

        return Objects.hash(actionType, event, allowedOperations);
    }
}


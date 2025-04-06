/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.identity.integration.test.extensions.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import java.util.List;
import java.util.Objects;

/**
 * This class represents the model of the pre issue access token action request sent in the request payload
 * to the API endpoint of the pre issue access token action.
 */
@JsonDeserialize(builder = PreIssueAccessTokenActionRequest.Builder.class)
public class PreIssueAccessTokenActionRequest {

    private final ActionType actionType;
    private final String flowId;
    private final String requestId;
    private final PreIssueAccessTokenEvent event;
    private final List<AllowedOperation> allowedOperations;

    public PreIssueAccessTokenActionRequest(Builder builder) {

        this.actionType = builder.actionType;
        this.flowId = builder.flowId;
        this.requestId = builder.requestId;
        this.event = builder.event;
        this.allowedOperations = builder.allowedOperations;
    }

    public ActionType getActionType() {

        return actionType;
    }

    public String getFlowId() {

        return flowId;
    }

    public String getRequestId() {

        return requestId;
    }

    public PreIssueAccessTokenEvent getEvent() {

        return event;
    }

    public List<AllowedOperation> getAllowedOperations() {

        return allowedOperations;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PreIssueAccessTokenActionRequest that = (PreIssueAccessTokenActionRequest) o;
        return actionType == that.actionType &&
                Objects.equals(event, that.event) &&
                Objects.equals(allowedOperations, that.allowedOperations);
    }

    @Override
    public int hashCode() {

        return Objects.hash(actionType, event, allowedOperations);
    }

    /**
     * Builder for the {@link PreIssueAccessTokenActionRequest}.
     */
    @JsonPOJOBuilder(withPrefix = "")
    public static class Builder {

        private ActionType actionType;
        private String flowId;
        private String requestId;
        private PreIssueAccessTokenEvent event;
        private List<AllowedOperation> allowedOperations;

        public Builder actionType(ActionType actionType) {

            this.actionType = actionType;
            return this;
        }

        public Builder flowId(String flowId) {

            this.flowId = flowId;
            return this;
        }

        public Builder requestId(String requestId) {

            this.requestId = requestId;
            return this;
        }

        public Builder event(PreIssueAccessTokenEvent event) {

            this.event = event;
            return this;
        }

        public Builder allowedOperations(List<AllowedOperation> allowedOperations) {

            this.allowedOperations = allowedOperations;
            return this;
        }

        public PreIssueAccessTokenActionRequest build() {

            return new PreIssueAccessTokenActionRequest(this);
        }
    }
}


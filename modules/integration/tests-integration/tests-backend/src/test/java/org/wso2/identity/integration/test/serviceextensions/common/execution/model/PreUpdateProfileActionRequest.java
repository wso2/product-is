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

package org.wso2.identity.integration.test.serviceextensions.common.execution.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import java.util.Objects;

/**
 * This class represents the model of the pre update profile action request sent in the request payload
 * to the API endpoint of the pre update profile action.
 */
@JsonDeserialize(builder = PreUpdateProfileActionRequest.Builder.class)
public class PreUpdateProfileActionRequest {

    private final ActionType actionType;
    private final String flowId;
    private final String requestId;
    private final PreUpdateProfileEvent event;

    public PreUpdateProfileActionRequest(Builder builder) {

        this.actionType = builder.actionType;
        this.flowId = builder.flowId;
        this.requestId = builder.requestId;
        this.event = builder.event;
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

    public PreUpdateProfileEvent getEvent() {

        return event;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PreUpdateProfileActionRequest that = (PreUpdateProfileActionRequest) o;
        return actionType == that.actionType &&
                Objects.equals(event, that.event);
    }

    @Override
    public int hashCode() {

        return Objects.hash(actionType, event);
    }

    /**
     * Builder for the {@link PreUpdateProfileActionRequest}.
     */
    @JsonPOJOBuilder(withPrefix = "")
    public static class Builder {

        private ActionType actionType;
        private String flowId;
        private String requestId;
        private PreUpdateProfileEvent event;

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

        public Builder event(PreUpdateProfileEvent event) {

            this.event = event;
            return this;
        }

        public PreUpdateProfileActionRequest build() {

            return new PreUpdateProfileActionRequest(this);
        }
    }
}


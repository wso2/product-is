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
 * This class models the event at a pre update profile trigger.
 * ProfileEvent is the entity that represents the event that is sent to the Action
 * over {@link org.wso2.carbon.identity.action.execution.model.ActionExecutionRequest}.
 */
@JsonDeserialize(builder = PreUpdateProfileEvent.Builder.class)
public class PreUpdateProfileEvent extends Event {

    /**
     * FlowInitiator Enum.
     * Defines the initiator type for the profile update flow.
     */
    public enum FlowInitiatorType {
        USER,
        ADMIN,
        APPLICATION
    }

    /**
     * Action Enum.
     * Defines the mode of updating the profile.
     */
    public enum Action {
        UPDATE
    }

    private final PreUpdateProfileEvent.FlowInitiatorType initiatorType;
    private final PreUpdateProfileEvent.Action action;
    private final PreUpdateProfileRequest request;
    private final ProfileUpdatingUser user;

    private PreUpdateProfileEvent(PreUpdateProfileEvent.Builder builder) {

        this.initiatorType = builder.initiatorType;
        this.action = builder.action;
        this.request = builder.request;
        this.organization = builder.organization;
        this.tenant = builder.tenant;
        this.user = builder.user;
        this.userStore = builder.userStore;
    }

    public PreUpdateProfileEvent.FlowInitiatorType getInitiatorType() {

        return initiatorType;
    }

    public PreUpdateProfileEvent.Action getAction() {

        return action;
    }

    public PreUpdateProfileRequest getRequest() {

        return request;
    }

    public ProfileUpdatingUser getProfileUpdatingUser() {

        return user;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PreUpdateProfileEvent that = (PreUpdateProfileEvent) o;

        return Objects.equals(request, that.request) &&
                Objects.equals(initiatorType, that.initiatorType) &&
                Objects.equals(action, that.action) &&
                Objects.equals(user, that.user) &&
                Objects.equals(userStore, that.userStore) &&
                Objects.equals(tenant, that.tenant);
    }

    @Override
    public int hashCode() {

        return Objects.hash(request, initiatorType, action, user, userStore, tenant);
    }

    /**
     * Builder for PasswordEvent.
     */
    @JsonPOJOBuilder(withPrefix = "")
    public static class Builder {

        private PreUpdateProfileRequest request;
        private Tenant tenant;
        private Organization organization;
        private ProfileUpdatingUser user;
        private UserStore userStore;
        private PreUpdateProfileEvent.FlowInitiatorType initiatorType;
        private PreUpdateProfileEvent.Action action;

        public PreUpdateProfileEvent.Builder request(PreUpdateProfileRequest request) {

            this.request = request;
            return this;
        }

        public PreUpdateProfileEvent.Builder tenant(Tenant tenant) {

            this.tenant = tenant;
            return this;
        }

        public PreUpdateProfileEvent.Builder organization(Organization organization) {

            this.organization = organization;
            return this;
        }

        public PreUpdateProfileEvent.Builder user(ProfileUpdatingUser user) {

            this.user = user;
            return this;
        }

        public PreUpdateProfileEvent.Builder userStore(UserStore userStore) {

            this.userStore = userStore;
            return this;
        }

        public PreUpdateProfileEvent.Builder initiatorType(PreUpdateProfileEvent.FlowInitiatorType initiatorType) {

            this.initiatorType = initiatorType;
            return this;
        }

        public PreUpdateProfileEvent.Builder action(PreUpdateProfileEvent.Action action) {

            this.action = action;
            return this;
        }

        public PreUpdateProfileEvent build() {

            return new PreUpdateProfileEvent(this);
        }
    }
}


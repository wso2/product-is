/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
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

package org.wso2.identity.integration.test.rest.api.server.application.management.v1.model;

import java.util.List;

/**
 * This class represents the role sharing configuration.
 */
public class ApplicationShareRolePolicy {

    private final List<RoleWithAudienceDO> roleWithAudienceDOList;
    private final Mode mode;

    private ApplicationShareRolePolicy(Mode mode, List<RoleWithAudienceDO> roleWithAudienceDOList) {

        this.mode = mode;
        this.roleWithAudienceDOList = roleWithAudienceDOList;
    }

    public List<RoleWithAudienceDO> getRoleWithAudienceDOList() {

        return roleWithAudienceDOList;
    }

    public Mode getMode() {

        return mode;
    }

    /**
     * This enum represents the role sharing mode.
     */
    public enum Mode {
        ALL,
        NONE,
        SELECTED;

        public static Mode fromValue(String text) {

            for (Mode b : Mode.values()) {
                if (String.valueOf(b).equals(text)) {
                    return b;
                }
            }
            return null;
        }
    }

    /**
     * This class is used to build the RoleSharingConfig object.
     */
    public static class Builder {

        private Mode mode;
        private List<RoleWithAudienceDO> roleWithAudienceDOList;

        public Builder mode(Mode mode) {
            this.mode = mode;
            return this;
        }

        public Builder roleWithAudienceDOList(List<RoleWithAudienceDO> roleWithAudienceDOList) {

            this.roleWithAudienceDOList = roleWithAudienceDOList;
            return this;
        }

        public ApplicationShareRolePolicy build() {

            if (mode == null) {
                throw new IllegalStateException("Role sharing mode must be set.");
            }
            return new ApplicationShareRolePolicy(mode, roleWithAudienceDOList);
        }
    }
}

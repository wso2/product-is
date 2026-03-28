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

/**
 * This class represents the Role with audience.
 */
public class RoleWithAudienceDO {

    private final String roleName;
    private final String audienceName;
    private final AudienceType audienceType;

    public RoleWithAudienceDO(String roleName, String audienceName, AudienceType audienceType) {

        this.roleName = roleName;
        this.audienceName = audienceName;
        this.audienceType = audienceType;
    }

    public String getRoleName() {

        return roleName;
    }

    public String getAudienceName() {

        return audienceName;
    }

    public AudienceType getAudienceType() {

        return audienceType;
    }

    /**
     * This enum represents the audience type.
     */
    public enum AudienceType {

        ORGANIZATION("organization"),
        APPLICATION("application");

        private final String value;

        AudienceType(String value) {

            this.value = value;
        }

        public static AudienceType fromValue(String text) {

            for (AudienceType b : AudienceType.values()) {
                if (String.valueOf(b.value).equals(text)) {
                    return b;
                }
            }
            return null;
        }

        @Override
        public String toString() {

            return value;
        }
    }
}

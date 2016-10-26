/*
 * Copyright (c) 2016 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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

package org.wso2.carbon.identity.provider.common.model;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Role configuration within an Identity Provider including the mappings.
 */
public class RoleConfig implements Serializable {

    private static final long serialVersionUID = 8637428653822260106L;

    private Map<String, String> roleMapping = new HashMap<String, String>();

    private RoleConfig(RoleConfigBuilder builder) {
        this.roleMapping = builder.roleMapping;
    }

    public Map<String, String> getRoleMapping() {
        return MapUtils.unmodifiableMap(roleMapping);
    }

    /**
     * Builds the role configuration with the given mappings.
     */
    public static class RoleConfigBuilder {

        private Map<String, String> roleMapping = new HashMap<String, String>();

        public RoleConfig build() {
            return new RoleConfig(this);
        }

        public RoleConfig.RoleConfigBuilder setRoleMappings(Map<String, String> roleMap) {
            if (!roleMap.isEmpty()) {
                this.roleMapping.clear();
                this.roleMapping.putAll(roleMap);
            }
            return this;
        }

        public RoleConfig.RoleConfigBuilder addRoleMapping(String role1, String role2) {
            if (StringUtils.isNotBlank(role1) && StringUtils.isNotBlank(role2)) {
                this.roleMapping.put(role1, role2);
            }
            return this;
        }

        public RoleConfig.RoleConfigBuilder addRoleMappings(Map<String, String> roleMap) {
            if (!roleMap.isEmpty()) {
                this.roleMapping.putAll(roleMap);
            }
            return this;
        }
    }
}

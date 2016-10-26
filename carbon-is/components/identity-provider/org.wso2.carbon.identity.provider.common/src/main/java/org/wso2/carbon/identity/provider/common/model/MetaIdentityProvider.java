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
import java.util.Map;

/**
 * Representation of meta information of an Identity Provider.
 */
public class MetaIdentityProvider implements Serializable {

    private static final long serialVersionUID = -4977395047974321120L;

    private int identityProviderId;
    private String name;
    private String displayName;
    private String description;

    // idp:cert -> 1..n
    // certs must be managed in keystore component
    private Map<String, String> certMap;

    // Do we need to have ClaimConfig object? Or can we directly store the claimDialectURI? UserId and RoleID will be
    // defined in ClaimMgt
    private ClaimConfig claimConfig;

    private RoleConfig roleConfig;

    private MetaIdentityProvider(MetaIdentityProviderBuilder builder) {
        this.identityProviderId = builder.identityProviderId;
        this.name = builder.name;
        this.displayName = builder.displayName;
        this.description = builder.description;
        this.certMap = builder.certMap;
        this.claimConfig = builder.claimConfigBuilder.build();
        this.roleConfig = builder.roleConfigBuilder.build();
    }

    public int getIdentityProviderId() {
        return identityProviderId;
    }

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public Map<String, String> getCertMap() {
        return MapUtils.unmodifiableMap(certMap);
    }

    public ClaimConfig getClaimConfig() {
        return claimConfig;
    }

    public RoleConfig getRoleConfig() {
        return roleConfig;
    }

    /**
     * Builder class for meta representation of an identity provider.
     */
    public class MetaIdentityProviderBuilder {

        private int identityProviderId;
        private String name;
        private String displayName;
        private String description;
        private Map<String, String> certMap;
        private ClaimConfig.ClaimConfigBuilder claimConfigBuilder;
        private RoleConfig.RoleConfigBuilder roleConfigBuilder = new RoleConfig.RoleConfigBuilder();

        public MetaIdentityProviderBuilder(int identityProviderId, String name) {
            this.identityProviderId = identityProviderId;
            if (StringUtils.isNoneBlank(name)) {
                this.name = name;
            } else {
                throw new IllegalArgumentException("Invalid Identity Provider name: " + name);
            }
        }

        public MetaIdentityProviderBuilder setDisplayName(String displayName) {
            this.displayName = displayName;
            return this;
        }

        public MetaIdentityProviderBuilder setDescription(String description) {
            this.description = description;
            return this;
        }

        public MetaIdentityProviderBuilder setCerts(Map<String, String> certMap) {
            if (!certMap.isEmpty()) {
                this.certMap.clear();
                this.certMap.putAll(certMap);
            }
            return this;
        }

        public MetaIdentityProviderBuilder addCert(String alias, String thumbPrint) {
            if (StringUtils.isNotBlank(alias) && StringUtils.isNotBlank(thumbPrint)) {
                this.certMap.put(alias, thumbPrint);
            }
            return this;
        }

        public MetaIdentityProviderBuilder addCerts(Map<String, String> certMap) {
            if (!certMap.isEmpty()) {
                this.certMap.putAll(certMap);
            }
            return this;
        }

        public MetaIdentityProviderBuilder setDialect(String dialect) {
            this.claimConfigBuilder = new ClaimConfig.ClaimConfigBuilder(dialect);
            return this;
        }

        public MetaIdentityProviderBuilder setRoleMappings(Map<String, String> roleMap) {
            this.roleConfigBuilder.setRoleMappings(roleMap);
            return this;
        }

        public MetaIdentityProviderBuilder addRoleMapping(String role1, String role2) {
            this.roleConfigBuilder.addRoleMapping(role1, role2);
            return this;
        }

        public MetaIdentityProviderBuilder addRoleMappings(Map<String, String> roleMap) {
            this.roleConfigBuilder.addRoleMappings(roleMap);
            return this;
        }

        public MetaIdentityProvider build() {
            return new MetaIdentityProvider(this);
        }
    }

}

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

import org.apache.commons.collections.CollectionUtils;

import java.util.Collection;
import java.util.HashSet;

/**
 * Do we need to remove this class and move all the attributes to IDP level?
 */
public class ProvisioningConfig {

    private String provisioningRole;
    private Collection<ProvisioningClaim> provisioningClaims = new HashSet<ProvisioningClaim>();

    // Is it OK to have identitystore as a attribute of IdentityProvider?
    private JITProvisioningConfig jitProvisioningConfig;
    private Collection<ProvisionerConfig> provisioners = new HashSet<ProvisionerConfig>();

    private ProvisioningConfig(ProvisioningConfigBuilder builder) {
        this.provisioningRole = builder.selectiveProvisioningRole;
        this.provisioningClaims = builder.provisioningClaims;
        this.jitProvisioningConfig = builder.jitProvisioningConfigBuilder.build();
        this.provisioners = builder.provisioners;
    }

    public String getProvisioningRole() {
        return provisioningRole;
    }

    public Collection<ProvisioningClaim> getProvisioningClaims() {
        return provisioningClaims;
    }

    public JITProvisioningConfig getJitProvisioningConfig() {
        return jitProvisioningConfig;
    }

    public Collection<ProvisionerConfig> getProvisioners() {
        return CollectionUtils.unmodifiableCollection(provisioners);
    }

    /**
     * Builds the configurations available for provisioning.
     */
    public static class ProvisioningConfigBuilder {

        private String selectiveProvisioningRole;
        private Collection<ProvisioningClaim> provisioningClaims = new HashSet<ProvisioningClaim>();
        private JITProvisioningConfig.JITProvisioningConfigBuilder jitProvisioningConfigBuilder =
                new JITProvisioningConfig.JITProvisioningConfigBuilder();
        private Collection<ProvisionerConfig> provisioners = new HashSet<ProvisionerConfig>();

        public ProvisioningConfigBuilder() {

        }

        public ProvisioningConfig.ProvisioningConfigBuilder setSelectiveProvisioningRole(
                String selectiveProvisioningRole) {
            this.selectiveProvisioningRole = selectiveProvisioningRole;
            return this;
        }

        public ProvisioningConfig.ProvisioningConfigBuilder setProvisioningClaims(
                Collection<ProvisioningClaim> provisioningClaims) {
            this.provisioningClaims.clear();
            this.provisioningClaims.addAll(provisioningClaims);
            return this;
        }

        public ProvisioningConfig.ProvisioningConfigBuilder addProvisionClaim(ProvisioningClaim provisioningClaim) {
            this.provisioningClaims.add(provisioningClaim);
            return this;
        }

        public ProvisioningConfig.ProvisioningConfigBuilder addProvisioningClaims(
                Collection<ProvisioningClaim> provisioningClaims) {
            this.provisioningClaims.addAll(provisioningClaims);
            return this;
        }

        public ProvisioningConfig.ProvisioningConfigBuilder setProvisioningIdP(Collection<String> provisioningIdPs) {
            this.jitProvisioningConfigBuilder.setProvisioningIdPs(provisioningIdPs);
            return this;
        }

        public ProvisioningConfig.ProvisioningConfigBuilder addProvisioningIdP(String provisioningIdP) {
            this.jitProvisioningConfigBuilder.addProvisioningIdP(provisioningIdP);
            return this;
        }

        public ProvisioningConfig.ProvisioningConfigBuilder addProvisioningIdPs(Collection<String> provisioningIdPs) {
            this.jitProvisioningConfigBuilder.addProvisioningIdPs(provisioningIdPs);
            return this;
        }

        public ProvisioningConfig.ProvisioningConfigBuilder setProvisioners(
                Collection<ProvisionerConfig> provisioners) {
            if (!provisioners.isEmpty()) {
                this.provisioners.clear();
                this.provisioners.addAll(provisioners);
            }
            return this;
        }

        public ProvisioningConfig.ProvisioningConfigBuilder addProvisioner(ProvisionerConfig provisioner) {
            if (provisioner != null) {
                this.provisioners.add(provisioner);
            }
            return this;
        }

        public ProvisioningConfig.ProvisioningConfigBuilder addProvisioners(
                Collection<ProvisionerConfig> provisioners) {
            if (!provisioners.isEmpty()) {
                this.provisioners.addAll(provisioners);
            }
            return this;
        }

        public ProvisioningConfig build() {
            return new ProvisioningConfig(this);
        }
    }
}

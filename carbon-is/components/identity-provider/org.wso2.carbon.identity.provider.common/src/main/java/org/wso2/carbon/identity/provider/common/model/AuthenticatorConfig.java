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

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class AuthenticatorConfig implements Serializable {

    private static final long serialVersionUID = -4569973060498183209L;

    protected String name;
    protected boolean isEnabled;
    protected Collection<String> requestedClaims = new HashSet<String>();
    protected Set<IdentityConnectorProperty> properties = new HashSet<IdentityConnectorProperty>();

    private AuthenticatorConfig(AuthenticatorConfigBuilder builder) {
        this.name = builder.name;
        this.properties = builder.properties;
    }

    public String getName() {
        return name;
    }

    public Collection<String> getRequestedClaims() {
        return Collections.unmodifiableCollection(requestedClaims);
    }

    public Collection<IdentityConnectorProperty> getProperties() {
        return Collections.unmodifiableCollection(properties);
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public class AuthenticatorConfigBuilder {

        protected String name;
        protected boolean isEnabled;
        protected Set<IdentityConnectorProperty> properties = new HashSet<IdentityConnectorProperty>();

        public AuthenticatorConfigBuilder(String name) {
            this.name = name;
        }

        public AuthenticatorConfigBuilder setEnabled(boolean isEnabled) {
            this.isEnabled = isEnabled;
            return this;
        }

        public AuthenticatorConfigBuilder setProperties(Collection<IdentityConnectorProperty> properties) {
            if(CollectionUtils.isNotEmpty(properties)) {
                this.properties = new HashSet<IdentityConnectorProperty>(properties);
            }
            return this;
        }

        public AuthenticatorConfigBuilder addProperty(IdentityConnectorProperty property) {
            if (property != null) {
                this.properties.add(property);
            }
            return this;
        }

        public AuthenticatorConfigBuilder addProperties(Collection<IdentityConnectorProperty> properties) {
            if (CollectionUtils.isNotEmpty(properties)) {
                this.properties.addAll(properties);
            }
            return this;
        }

        public AuthenticatorConfig build() {
            return new AuthenticatorConfig(this);
        }
    }

}

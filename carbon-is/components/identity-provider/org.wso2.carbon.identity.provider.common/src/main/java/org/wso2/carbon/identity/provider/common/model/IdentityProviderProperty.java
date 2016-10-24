/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

/**
 * Basic representation of an IDP.
 */
public class IdentityProviderProperty extends IdentityProperty {

    private static final long serialVersionUID = 9110101982938078646L;

    private String identityProviderName;

    protected IdentityProviderProperty(IdentityProviderPropertyBuilder builder) {
        super(builder);
        this.identityProviderName = builder.identityProviderName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof IdentityProviderProperty)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        IdentityProviderProperty that = (IdentityProviderProperty) o;

        return identityProviderName.equals(that.identityProviderName);

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + identityProviderName.hashCode();
        return result;
    }

    /**
     * Builds the basic representation of an IDP.
     */
    public class IdentityProviderPropertyBuilder extends IdentityPropertyBuilder {

        private String identityProviderName;

        public IdentityProviderPropertyBuilder(String identityProviderName, String propertyName, String dataType) {
            super(propertyName, dataType);
            this.identityProviderName = identityProviderName;
        }

        public IdentityProperty build() {
            return new IdentityProviderProperty(this);
        }
    }
}

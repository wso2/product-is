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
 * Representation of a property used by the identity connectors.
 */
public class IdentityConnectorProperty extends IdentityProperty {

    private static final long serialVersionUID = -3072914320353287472L;

    // Is it OK to remove this attribute? Do properties always live within a connector only?
    // What if we want to update one property in a connector? Then we need to give the IdentityConnectorProperty
    // object as input parameter which should say if this is a authenticator property or provisioner property
    private ConnectorType connectorType;
    private String connectorName;

    protected IdentityConnectorProperty(IdentityConnectorPropertyBuilder builder) {
        super(builder);
        this.connectorType = builder.connectorType;
        this.connectorName = builder.connectorName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof IdentityConnectorProperty)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        IdentityConnectorProperty that = (IdentityConnectorProperty) o;

        if (connectorType != that.connectorType) {
            return false;
        }
        return connectorName.equals(that.connectorName);

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + connectorType.hashCode();
        result = 31 * result + connectorName.hashCode();
        return result;
    }

    /**
     * Builds the identity connector property.
     */
    public class IdentityConnectorPropertyBuilder extends IdentityPropertyBuilder {

        private ConnectorType connectorType;
        private String connectorName;

        public IdentityConnectorPropertyBuilder(ConnectorType connectorType, String connectorName, String propertyName,
                String dataType) {
            super(propertyName, dataType);
            this.connectorType = connectorType;
            this.connectorName = connectorName;
        }

        public IdentityProperty build() {
            return new IdentityConnectorProperty(this);
        }
    }
}

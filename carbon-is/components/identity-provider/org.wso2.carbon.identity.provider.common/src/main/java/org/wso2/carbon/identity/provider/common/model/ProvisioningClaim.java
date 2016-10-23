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

public class ProvisioningClaim {

    private int claimId;
    private DataType dataType;
    private Object defaultValue;

    public int getClaimId() {
        return claimId;
    }

    public DataType getDataType() {
        return dataType;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    private ProvisioningClaim(ProvisioningClaimBuilder builder) {
        this.claimId = builder.claim;
        this.dataType = builder.dataType;
    }

    protected static class ProvisioningClaimBuilder {

        private int claim;
        private DataType dataType;
        private Object defaultValue;

        public ProvisioningClaimBuilder(int claim, DataType dataType) {
            this.claim = claim;
            this.dataType = dataType;
        }

        public ProvisioningClaimBuilder setDefaultValue(Object defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }

        public ProvisioningClaim build() {
            return new ProvisioningClaim(this);
        }
    }
}

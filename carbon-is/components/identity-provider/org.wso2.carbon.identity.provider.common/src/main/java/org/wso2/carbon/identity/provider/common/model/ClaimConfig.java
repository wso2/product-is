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

import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;

/**
 * Representation of a user claim configuration.
 */
public class ClaimConfig implements Serializable {

    private static final long serialVersionUID = -3886962663799281628L;

    // We have removed UserID claim URI and role claim URI because it should come from ClaimMgt component
    private String dialect;

    private ClaimConfig(ClaimConfigBuilder builder) {
        this.dialect = builder.dialect;
    }

    public String getDialect() {
        return dialect;
    }

    /**
     * Builds the claim configuration to be used by the IDPs.
     */
    public static class ClaimConfigBuilder {

        private String dialect;

        public ClaimConfigBuilder(String dialect) {
            if (StringUtils.isNoneBlank(dialect)) {
                this.dialect = dialect;
            } else {
                throw new IllegalArgumentException("Invalid claim dialect: " + dialect);
            }
        }

        public ClaimConfig build() {
            return new ClaimConfig(this);
        }
    }

}

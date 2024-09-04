/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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

package org.wso2.identity.integration.test.oauth2.dataprovider.model;

public class UserClaimConfig {

    private String localClaimUri;
    private String oidcClaimUri;

    public UserClaimConfig(Builder builder) {

        this.localClaimUri = builder.localClaimUri;
        this.oidcClaimUri = builder.oidcClaimUri;
    }

    public String getLocalClaimUri() {

        return localClaimUri;
    }

    public String getOidcClaimUri() {

        return oidcClaimUri;
    }

    public static class Builder {

        private String localClaimUri;
        private String oidcClaimUri;

        public Builder localClaimUri(String localClaimUri) {

            this.localClaimUri = localClaimUri;
            return this;
        }

        public Builder oidcClaimUri(String oidcClaimUri) {

            this.oidcClaimUri = oidcClaimUri;
            return this;
        }

        public UserClaimConfig build() {

            return new UserClaimConfig(this);
        }
    }
}

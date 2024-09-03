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

import java.util.List;

public class TokenScopes {

    private List<String> requestedScopes;
    private List<String> grantedScopes;

    private TokenScopes(Builder builder) {

        this.requestedScopes = builder.requestedScopes;
        this.grantedScopes = builder.grantedScopes;
    }

    public List<String> getRequestedScopes() {

        return requestedScopes;
    }

    public List<String> getGrantedScopes() {

        return grantedScopes;
    }

    public static class Builder {

        private List<String> requestedScopes;
        private List<String> grantedScopes;

        public Builder requestedScopes(List<String> requestedScopes) {

            this.requestedScopes = requestedScopes;
            return this;
        }

        public Builder grantedScopes(List<String> grantedScopes) {

            this.grantedScopes = grantedScopes;
            return this;
        }

        public TokenScopes build() {

            return new TokenScopes(this);
        }
    }
}

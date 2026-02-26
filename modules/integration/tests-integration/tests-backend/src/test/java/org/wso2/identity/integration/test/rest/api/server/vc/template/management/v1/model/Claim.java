/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.identity.integration.test.rest.api.server.vc.template.management.v1.model;

/**
 * VC template claim model.
 */
public class Claim {

    private String name;
    private String type;
    private String claimUri;

    public String getName() {

        return name;
    }

    public Claim name(String name) {

        this.name = name;
        return this;
    }

    public String getType() {

        return type;
    }

    public Claim type(String type) {

        this.type = type;
        return this;
    }

    public String getClaimUri() {

        return claimUri;
    }

    public Claim claimUri(String claimUri) {

        this.claimUri = claimUri;
        return this;
    }
}

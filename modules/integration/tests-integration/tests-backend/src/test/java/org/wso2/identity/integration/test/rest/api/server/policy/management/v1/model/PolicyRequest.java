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

package org.wso2.identity.integration.test.rest.api.server.policy.management.v1.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Policy creation request model.
 */
public class PolicyRequest {

    private String name;
    private List<PolicyResourceRequest> resources;

    public PolicyRequest name(String name) {

        this.name = name;
        return this;
    }

    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }

    public PolicyRequest resources(List<PolicyResourceRequest> resources) {

        this.resources = resources;
        return this;
    }

    public PolicyRequest addResourceItem(PolicyResourceRequest resourceItem) {

        if (this.resources == null) {
            this.resources = new ArrayList<>();
        }
        this.resources.add(resourceItem);
        return this;
    }

    public List<PolicyResourceRequest> getResources() {

        return resources;
    }

    public void setResources(List<PolicyResourceRequest> resources) {

        this.resources = resources;
    }
}

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

/**
 * Policy resource request model.
 */
public class PolicyResourceRequest {

    private String target;
    private String resourceType;
    private RuleRequest rule;

    public PolicyResourceRequest target(String target) {

        this.target = target;
        return this;
    }

    public String getTarget() {

        return target;
    }

    public void setTarget(String target) {

        this.target = target;
    }

    public PolicyResourceRequest resourceType(String resourceType) {

        this.resourceType = resourceType;
        return this;
    }

    public String getResourceType() {

        return resourceType;
    }

    public void setResourceType(String resourceType) {

        this.resourceType = resourceType;
    }

    public PolicyResourceRequest rule(RuleRequest rule) {

        this.rule = rule;
        return this;
    }

    public RuleRequest getRule() {

        return rule;
    }

    public void setRule(RuleRequest rule) {

        this.rule = rule;
    }
}

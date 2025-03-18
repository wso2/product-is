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

package org.wso2.identity.integration.test.rest.api.server.rules.metadata.v1;

import org.testng.Assert;
import org.wso2.identity.integration.test.rest.api.server.common.RESTAPIServerTestBase;

import java.io.IOException;

public class RulesMetadataTestBase extends RESTAPIServerTestBase {

    private static final String API_DEFINITION_NAME = "rule-metadata.yaml";
    private static final String API_VERSION = "v1";

    private static String swaggerDefinition;

    static {
        String API_PACKAGE_NAME = "org.wso2.carbon.identity.api.server.rule.metadata.v1";
        try {
            swaggerDefinition = getAPISwaggerDefinition(API_PACKAGE_NAME, API_DEFINITION_NAME);
        } catch (IOException e) {
            Assert.fail(String.format("Unable to read the swagger definition %s from %s", API_DEFINITION_NAME,
                    API_PACKAGE_NAME), e);
        }
    }

    protected void initTestClass(String tenantDomain) throws IOException {

        super.testInit(API_VERSION, swaggerDefinition, tenantDomain);
    }

    protected String getAPIRequestForValidFlow(String flow) {

        validateFlow(flow);
        return getAPIEndpoint() + "?flow=" + flow;
    }

    protected String getAPIRequestForFlow(String flow) {

        return getAPIEndpoint() + "?flow=" + flow;
    }

    protected String getAPIEndpoint() {

        return "/rules/metadata";
    }

    private void validateFlow(String flow) {

        if (!"preIssueAccessToken".equals(flow)) {
            throw new IllegalArgumentException("Invalid flow: " + flow);
        }
    }
}

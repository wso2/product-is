/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.identity.integration.test.rest.api.user.common;

import org.apache.axis2.AxisFault;
import org.wso2.identity.integration.test.rest.api.common.RESTTestBase;

import javax.xml.xpath.XPathExpressionException;

/**
 * Base Test Class for user based REST API test cases
 * ex: /t/{tenant-domain}/api/users/{version}
 */
public class RESTAPIUserTestBase extends RESTTestBase {

    protected static final String API_USERS_BASE_PATH = "/api/users/%s";
    protected static final String API_USERS_BASE_PATH_WITH_TENANT_CONTEXT = TENANT_CONTEXT_IN_URL + API_USERS_BASE_PATH;

    protected void testInit(String apiVersion, String apiDefinition, String tenantDomain)
            throws XPathExpressionException, AxisFault {

        String basePathInSwagger = String.format(API_USERS_BASE_PATH, apiVersion);
        String basePath = String.format(API_USERS_BASE_PATH_WITH_TENANT_CONTEXT,
                tenantDomain, apiVersion);
        super.init(apiDefinition, basePathInSwagger, basePath);
    }
}

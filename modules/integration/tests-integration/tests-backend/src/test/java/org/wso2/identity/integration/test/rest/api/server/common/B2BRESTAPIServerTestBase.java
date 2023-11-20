/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.identity.integration.test.rest.api.server.common;

import org.wso2.identity.integration.test.rest.api.common.B2BRESTTestBase;

import java.rmi.RemoteException;

/**
 * Base Test Class for server based B2B REST API test cases.
 * ex: /o/{organization-domain}/api/server/{version}
 */
public class B2BRESTAPIServerTestBase extends B2BRESTTestBase {

    protected static final String API_SERVER_BASE_PATH = "/api/server/%s";
    protected static final String API_SERVER_BASE_PATH_IN_SWAGGER = "/t/\\{tenant-domain\\}" +
            API_SERVER_BASE_PATH;
    protected static final String API_SERVER_BASE_PATH_WITH_TENANT_CONTEXT =
            TENANT_CONTEXT_IN_ORG_URL + API_SERVER_BASE_PATH;

    protected void testInit(String apiVersion, String apiDefinition, String tenantDomain) throws RemoteException {

        String basePathInSwagger = String.format(API_SERVER_BASE_PATH_IN_SWAGGER, apiVersion);
        String basePath = String.format(API_SERVER_BASE_PATH_WITH_TENANT_CONTEXT,
                tenantDomain, apiVersion);
        super.init(apiDefinition, basePathInSwagger, basePath);
    }

    protected void testInitWithoutTenantQualifiedPath(String apiVersion, String apiDefinition) throws RemoteException {

        String basePathInSwagger = String.format(API_SERVER_BASE_PATH, apiVersion);
        super.init(apiDefinition, basePathInSwagger, basePathInSwagger);
    }
}

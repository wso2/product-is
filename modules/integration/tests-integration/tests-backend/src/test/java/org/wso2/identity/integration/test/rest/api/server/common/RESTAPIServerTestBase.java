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

package org.wso2.identity.integration.test.rest.api.server.common;

import org.apache.axis2.AxisFault;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;
import org.wso2.identity.integration.test.rest.api.common.RESTTestBase;

import java.rmi.RemoteException;

/**
 * Base Test Class for server based REST API test cases.
 * ex: /t/{tenant-domain}/api/server/{version}
 */
public class RESTAPIServerTestBase extends RESTTestBase {

    protected static final String API_SERVER_BASE_PATH = "/api/server/%s";
    protected static final String API_SERVER_BASE_PATH_IN_SWAGGER = "/t/\\{tenant-domain\\}" + API_SERVER_BASE_PATH;

    protected void testInit(String apiVersion, String apiDefinition, String tenantDomain)
            throws RemoteException {

        String basePathInSwagger = String.format(API_SERVER_BASE_PATH_IN_SWAGGER, apiVersion);
        String basePath = ISIntegrationTest.getTenantedRelativePath(String.format(API_SERVER_BASE_PATH, apiVersion),
                tenantDomain);
        super.init(apiDefinition, basePathInSwagger, basePath);
    }

    protected void testInitWithoutTenantQualifiedPath(String apiVersion, String apiDefinition)
            throws RemoteException {

        String basePathInSwagger = String.format(API_SERVER_BASE_PATH, apiVersion);
        super.init(apiDefinition, basePathInSwagger, basePathInSwagger);
    }
}

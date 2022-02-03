/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.identity.integration.test.scim2.rest.api;

import io.restassured.RestAssured;
import org.apache.axis2.AxisFault;
import org.apache.commons.lang.StringUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.wso2.identity.integration.test.rest.api.common.RESTTestBase;

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;

import static org.wso2.identity.integration.test.scim2.SCIM2BaseTestCase.SCIM2_ENDPOINT;

public class SCIM2BaseTest extends RESTTestBase {

    private static final String API_DEFINITION_FILE_NAME = "scim2.yaml";

    protected static String swaggerDefinition;
    protected static final String SCIM2_BASE_PATH_IN_SWAGGER = "/t/\\{tenant-domain\\}" + SCIM2_ENDPOINT;
    protected static final String SCIM2_BASE_PATH_WITH_TENANT_CONTEXT = TENANT_CONTEXT_IN_URL + SCIM2_ENDPOINT;
    public static final String FILE_BASE_PATH = (System.getProperty("basedir", "."))
            + File.separator + "src" + File.separator + "test"
            + File.separator + "resources" + File.separator;

    static {
        try {
            String swaggerFilePath = "apiSwaggerFiles" + File.separator + API_DEFINITION_FILE_NAME;
            swaggerDefinition = getAPISwaggerDefinition(FILE_BASE_PATH + swaggerFilePath);
        } catch (IOException e) {
            Assert.fail(String.format("Unable to read the swagger definition from %s", API_DEFINITION_FILE_NAME), e);
        }
    }

    public void testInit(String apiDefinition, String tenantDomain)
            throws RemoteException {

        final String basePath;
        if ("carbon.super".equals(tenantDomain)) {
            basePath = String.format(SCIM2_ENDPOINT);
        } else {
            basePath = String.format(SCIM2_BASE_PATH_WITH_TENANT_CONTEXT,
                    tenantDomain);
        }
        super.init(apiDefinition, SCIM2_BASE_PATH_IN_SWAGGER, basePath);
    }

    @AfterClass(alwaysRun = true)
    public void testConclude() throws Exception {

        super.conclude();
    }

    @BeforeMethod(alwaysRun = true)
    public void testInit() {

        RestAssured.basePath = basePath;
    }

    @AfterMethod(alwaysRun = true)
    public void testFinish() throws Exception {

        RestAssured.basePath = StringUtils.EMPTY;
    }
}

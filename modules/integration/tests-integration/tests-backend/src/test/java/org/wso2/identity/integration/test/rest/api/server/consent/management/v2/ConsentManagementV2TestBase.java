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

package org.wso2.identity.integration.test.rest.api.server.consent.management.v2;

import io.restassured.RestAssured;
import org.apache.commons.lang.StringUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;
import org.wso2.identity.integration.test.rest.api.common.RESTTestBase;

import java.io.IOException;
import java.rmi.RemoteException;

/**
 * Base test class for Consent Management API v2 REST API tests.
 * <p>
 * The consent management v2 API base path: /api/identity/consent-mgt/v2.0
 */
public class ConsentManagementV2TestBase extends RESTTestBase {

    private static final String API_DEFINITION_NAME = "carbon-consent-management-v2.yaml";
    private static final String API_PACKAGE_NAME = "org.wso2.carbon.api.server.consent.mgt.v2";

    protected static final String API_VERSION = "v2.0";

    /**
     * Base path for the consent management v2 API.
     * Pattern: /api/identity/consent-mgt/v2.0
     */
    protected static final String API_BASE_PATH = "/api/identity/consent-mgt/" + API_VERSION;

    /**
     * Pattern used in the swagger definition: /t/{tenant-domain}/api/identity/consent-mgt/v2.0
     */
    protected static final String API_BASE_PATH_IN_SWAGGER =
            "/t/\\{tenant-domain\\}/api/identity/consent-mgt/" + API_VERSION;

    // Endpoint paths.
    protected static final String PURPOSES_ENDPOINT = "/purposes";
    protected static final String ELEMENTS_ENDPOINT = "/elements";
    protected static final String CONSENTS_ENDPOINT = "/consents";
    protected static final String VERSIONS_ENDPOINT = "/versions";

    protected static String swaggerDefinition;

    static {
        try {
            swaggerDefinition = getAPISwaggerDefinition(API_PACKAGE_NAME, API_DEFINITION_NAME);
        } catch (IOException e) {
            Assert.fail(String.format("Unable to read the swagger definition %s from %s", API_DEFINITION_NAME,
                    API_PACKAGE_NAME), e);
        }
    }

    /**
     * Initialize the test with the API version, swagger definition, and tenant domain.
     *
     * @param tenantDomain Tenant domain.
     * @throws RemoteException If an error occurs initializing.
     */
    protected void testInit(String tenantDomain) throws RemoteException {

        String basePathInSwagger = API_BASE_PATH_IN_SWAGGER;
        String basePath = ISIntegrationTest.getTenantedRelativePath(API_BASE_PATH, tenantDomain);
        super.init(swaggerDefinition, basePathInSwagger, basePath);
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
    public void testFinish() {

        RestAssured.basePath = StringUtils.EMPTY;
    }

    @DataProvider(name = "restAPIUserConfigProvider")
    public static Object[][] restAPIUserConfigProvider() {

        return new Object[][]{
                {TestUserMode.SUPER_TENANT_ADMIN},
                {TestUserMode.TENANT_ADMIN}
        };
    }
}

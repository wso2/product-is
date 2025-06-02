/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.identity.integration.test.rest.api.server.flow.management.v1;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.identity.integration.test.rest.api.server.flow.management.v1.model.RegistrationFlowRequest;
import org.wso2.identity.integration.test.restclients.RegistrationManagementClient;

import java.io.IOException;

/**
 * This class contains the test cases for Registration Management API.
 */
public class RegistrationManagementNegativeTest extends RegistrationManagementTestBase {

    private RegistrationManagementClient registrationManagementClient;
    private static String registrationFlowRequestJson;

    @DataProvider
    public static Object[][] restAPIUserConfigProvider() {

        return new Object[][]{
                {TestUserMode.SUPER_TENANT_ADMIN},
                {TestUserMode.TENANT_ADMIN}
        };
    }

    @Factory(dataProvider = "restAPIUserConfigProvider")
    public RegistrationManagementNegativeTest(TestUserMode userMode) throws Exception {

        super.init(userMode);
        this.context = isServer;
        this.authenticatingUserName = context.getContextTenant().getTenantAdmin().getUserName();
        this.authenticatingCredential = context.getContextTenant().getTenantAdmin().getPassword();
        this.tenant = context.getContextTenant().getDomain();
    }

    @BeforeClass(alwaysRun = true)
    public void init() throws IOException {

        super.testInit(API_VERSION, swaggerDefinition, tenantInfo.getDomain());
        registrationManagementClient = new RegistrationManagementClient(serverURL, tenantInfo);
        registrationFlowRequestJson = readResource(REGISTRATION_FLOW);
    }

    @AfterClass
    public void cleanup() throws Exception {

        registrationManagementClient.closeHttpClient();
        super.testConclude();
    }

    @Test(description = "Test invalid registration flow request")
    public void testInvalidRegistrationFlowRequest() throws Exception {

        ObjectMapper objectMapper = new ObjectMapper(new JsonFactory());
        RegistrationFlowRequest registrationFlowRequest = objectMapper.readValue(registrationFlowRequestJson,
                RegistrationFlowRequest.class);
        registrationFlowRequest.getSteps().get(0).setType("INVALID");
        try {
            registrationManagementClient.putRegistrationFlow(registrationFlowRequest);
        } catch (Exception e) {
            Assert.assertNotNull(e.getMessage());
            Assert.assertTrue(e.getMessage().contains("Error code 400"));
        }
    }
}

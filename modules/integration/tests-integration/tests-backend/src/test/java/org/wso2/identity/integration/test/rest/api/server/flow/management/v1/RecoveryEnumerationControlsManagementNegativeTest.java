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
import org.wso2.identity.integration.test.rest.api.server.flow.management.v1.model.FlowRequest;
import org.wso2.identity.integration.test.restclients.FlowManagementClient;

import java.io.IOException;

/**
 * Negative tests for managing the password recovery flow with the user enumeration and account status controls
 * present on the {@code UserResolveExecutor}. A structurally invalid flow definition must be rejected with
 * HTTP 400 even when the controls are configured.
 */
public class RecoveryEnumerationControlsManagementNegativeTest extends FlowManagementTestBase {

    private static final String INVALID_STEP_TYPE = "INVALID";
    private static final String BAD_REQUEST_ERROR = "Error code 400";

    private FlowManagementClient flowManagementClient;
    private static String passwordRecoveryFlowRequestJson;

    @DataProvider(name = "restAPIUserConfigProvider")
    public static Object[][] restAPIUserConfigProvider() {

        return new Object[][]{
                {TestUserMode.SUPER_TENANT_ADMIN},
                {TestUserMode.TENANT_ADMIN}
        };
    }

    @Factory(dataProvider = "restAPIUserConfigProvider")
    public RecoveryEnumerationControlsManagementNegativeTest(TestUserMode userMode) throws Exception {

        super.init(userMode);
        this.context = isServer;
        this.authenticatingUserName = context.getContextTenant().getTenantAdmin().getUserName();
        this.authenticatingCredential = context.getContextTenant().getTenantAdmin().getPassword();
        this.tenant = context.getContextTenant().getDomain();
    }

    @BeforeClass(alwaysRun = true)
    public void init() throws IOException {

        super.testInit(API_VERSION, swaggerDefinition, tenantInfo.getDomain());
        flowManagementClient = new FlowManagementClient(serverURL, tenantInfo);
        passwordRecoveryFlowRequestJson = readResource(PASSWORD_RECOVERY_FLOW);
    }

    @AfterClass(alwaysRun = true)
    public void testCleanup() throws Exception {

        flowManagementClient.closeHttpClient();
        super.testConclude();
    }

    @Test(description = "Updating a structurally invalid password recovery flow is rejected with HTTP 400.")
    public void testUpdateInvalidPasswordRecoveryFlowIsRejected() throws Exception {

        FlowRequest invalidFlowRequest = new ObjectMapper(new JsonFactory())
                .readValue(passwordRecoveryFlowRequestJson, FlowRequest.class);
        // Keep the controls-enabled UserResolveExecutor but corrupt the step type to make the definition invalid.
        invalidFlowRequest.getSteps().get(0).setType(INVALID_STEP_TYPE);
        try {
            flowManagementClient.putFlow(invalidFlowRequest);
            Assert.fail("Invalid password recovery flow should be rejected with HTTP 400.");
        } catch (Exception e) {
            Assert.assertTrue(e.getMessage().contains(BAD_REQUEST_ERROR),
                    "Expected a 400 error but got: " + e.getMessage());
        }
    }
}

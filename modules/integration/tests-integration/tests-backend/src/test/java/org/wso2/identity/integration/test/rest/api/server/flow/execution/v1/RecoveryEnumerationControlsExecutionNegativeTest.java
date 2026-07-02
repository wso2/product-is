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

package org.wso2.identity.integration.test.rest.api.server.flow.execution.v1;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.identity.integration.test.rest.api.server.flow.execution.v1.model.FlowExecutionRequest;
import org.wso2.identity.integration.test.rest.api.server.flow.execution.v1.model.FlowExecutionResponse;
import org.wso2.identity.integration.test.rest.api.server.flow.management.v1.model.Error;
import org.wso2.identity.integration.test.restclients.FlowExecutionClient;
import org.wso2.identity.integration.test.restclients.FlowManagementClient;

import java.util.HashMap;
import java.util.Map;

import static org.wso2.identity.integration.test.rest.api.server.flow.execution.v1.FlowExecutionTestBase.FlowTypes.PASSWORD_RECOVERY;

/**
 * Negative tests for the password recovery flow execution with the user enumeration and account status controls.
 */
public class RecoveryEnumerationControlsExecutionNegativeTest extends FlowExecutionTestBase {

    private static final String USERNAME_CLAIM = "http://wso2.org/claims/username";
    private static final String USER_RESOLVE_ACTION_ID = "button_1ov4";
    private static final String TEST_IDENTIFIER = "PwdRecNegTestUser";

    private static final String FLOW_NOT_ENABLED_ON_INITIATE = "FE-60101";
    private static final String FLOW_NOT_ENABLED_OR_INVALID_FLOW_ID = "FE-60001";
    private static final String INVALID_INPUTS = "FE-60008";

    private FlowExecutionClient flowExecutionClient;
    private FlowManagementClient flowManagementClient;
    private static String flowId;

    @DataProvider(name = "restAPIUserConfigProvider")
    public static Object[][] restAPIUserConfigProvider() {

        return new Object[][]{
                {TestUserMode.SUPER_TENANT_ADMIN},
                {TestUserMode.TENANT_ADMIN}
        };
    }

    @Factory(dataProvider = "restAPIUserConfigProvider")
    public RecoveryEnumerationControlsExecutionNegativeTest(TestUserMode userMode) throws Exception {

        super.init(userMode);
        this.context = isServer;
        this.authenticatingUserName = context.getContextTenant().getTenantAdmin().getUserName();
        this.authenticatingCredential = context.getContextTenant().getTenantAdmin().getPassword();
        this.tenant = context.getContextTenant().getDomain();
    }

    @BeforeClass(alwaysRun = true)
    public void setupClass() throws Exception {

        super.testInit(API_VERSION, swaggerDefinition, tenantInfo.getDomain());
        flowExecutionClient = new FlowExecutionClient(serverURL, tenantInfo);
        flowManagementClient = new FlowManagementClient(serverURL, tenantInfo);
        addPasswordRecoveryFlow(flowManagementClient);
    }

    @AfterClass(alwaysRun = true)
    public void tearDownClass() throws Exception {

        disablePasswordRecoveryFlow(flowManagementClient);
        flowExecutionClient.closeHttpClient();
        flowManagementClient.closeHttpClient();
    }

    @Test(description = "Initiating the password recovery flow before it is enabled returns FE-60101.")
    public void testInitiateFlowWithoutEnable() throws Exception {

        Object responseObj = flowExecutionClient.initiateFlowExecution(PASSWORD_RECOVERY);
        Assert.assertTrue(responseObj instanceof Error);
        Assert.assertEquals(((Error) responseObj).getCode(), FLOW_NOT_ENABLED_ON_INITIATE);
    }

    @Test(description = "Executing the password recovery flow before it is enabled returns FE-60001.",
            dependsOnMethods = "testInitiateFlowWithoutEnable")
    public void testExecuteFlowWithoutEnable() throws Exception {

        Object responseObj = flowExecutionClient.executeFlow(getRequest("INVALID_FLOW_ID", defaultInputs()));
        Assert.assertTrue(responseObj instanceof Error);
        Assert.assertEquals(((Error) responseObj).getCode(), FLOW_NOT_ENABLED_OR_INVALID_FLOW_ID);
    }

    @Test(description = "Enable the flow and initiate it to obtain a valid flow id.",
            dependsOnMethods = "testExecuteFlowWithoutEnable")
    public void testInitiateFlow() throws Exception {

        enablePasswordRecoveryFlow(flowManagementClient);
        Object responseObj = flowExecutionClient.initiateFlowExecution(PASSWORD_RECOVERY);
        Assert.assertTrue(responseObj instanceof FlowExecutionResponse);
        FlowExecutionResponse response = (FlowExecutionResponse) responseObj;
        Assert.assertNotNull(response.getFlowId());
        Assert.assertEquals(response.getFlowStatus(), STATUS_INCOMPLETE);
        Assert.assertEquals(response.getType().toString(), TYPE_VIEW);
        flowId = response.getFlowId();
    }

    @Test(description = "Executing with an invalid flow id returns FE-60001.",
            dependsOnMethods = "testInitiateFlow")
    public void testExecuteFlowWithInvalidFlowId() throws Exception {

        Object responseObj = flowExecutionClient.executeFlow(getRequest("INVALID_FLOW_ID", defaultInputs()));
        Assert.assertTrue(responseObj instanceof Error);
        Assert.assertEquals(((Error) responseObj).getCode(), FLOW_NOT_ENABLED_OR_INVALID_FLOW_ID);
    }

    @Test(description = "Executing with empty inputs returns FE-60008.",
            dependsOnMethods = "testExecuteFlowWithInvalidFlowId")
    public void testExecuteFlowWithEmptyInputs() throws Exception {

        Object responseObj = flowExecutionClient.executeFlow(getRequest(flowId, new HashMap<>()));
        Assert.assertTrue(responseObj instanceof Error);
        Assert.assertEquals(((Error) responseObj).getCode(), INVALID_INPUTS);
    }

    private static Map<String, String> defaultInputs() {

        Map<String, String> inputs = new HashMap<>();
        inputs.put(USERNAME_CLAIM, TEST_IDENTIFIER);
        return inputs;
    }

    private static FlowExecutionRequest getRequest(String flowId, Map<String, String> inputs) {

        FlowExecutionRequest request = new FlowExecutionRequest();
        request.setFlowType(PASSWORD_RECOVERY);
        request.setFlowId(flowId);
        request.setActionId(USER_RESOLVE_ACTION_ID);
        request.setInputs(inputs);
        return request;
    }
}

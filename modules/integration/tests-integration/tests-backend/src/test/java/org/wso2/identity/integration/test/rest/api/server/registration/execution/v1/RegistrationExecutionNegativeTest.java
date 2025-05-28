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

package org.wso2.identity.integration.test.rest.api.server.registration.execution.v1;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.identity.integration.test.rest.api.server.registration.execution.v1.model.FlowExecutionRequest;
import org.wso2.identity.integration.test.rest.api.server.registration.execution.v1.model.FlowExecutionResponse;
import org.wso2.identity.integration.test.rest.api.server.registration.management.v1.model.Error;
import org.wso2.identity.integration.test.restclients.IdentityGovernanceRestClient;
import org.wso2.identity.integration.test.restclients.RegistrationExecutionClient;
import org.wso2.identity.integration.test.restclients.RegistrationManagementClient;

import java.util.HashMap;
import java.util.Map;

/**
 * Test class for Registration Execution API.
 */
public class RegistrationExecutionNegativeTest extends RegistrationExecutionTestBase {

    private static final String ACTION_ID = "button_5zqc";
    public static final String USER = "RegExecNegTestUser";
    private RegistrationExecutionClient registrationExecutionClient;
    private RegistrationManagementClient registrationManagementClient;
    private IdentityGovernanceRestClient identityGovernanceRestClient;
    private static String flowId;

    @DataProvider(name = "restAPIUserConfigProvider")
    public static Object[][] restAPIUserConfigProvider() {

        return new Object[][]{
                {TestUserMode.SUPER_TENANT_ADMIN},
                {TestUserMode.TENANT_ADMIN}
        };
    }

    @Factory(dataProvider = "restAPIUserConfigProvider")
    public RegistrationExecutionNegativeTest(TestUserMode userMode) throws Exception {

        super.init(userMode);
        this.context = isServer;
        this.authenticatingUserName = context.getContextTenant().getTenantAdmin().getUserName();
        this.authenticatingCredential = context.getContextTenant().getTenantAdmin().getPassword();
        this.tenant = context.getContextTenant().getDomain();
    }

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {

        super.testInit(API_VERSION, swaggerDefinition, tenantInfo.getDomain());
        registrationExecutionClient = new RegistrationExecutionClient(serverURL, tenantInfo);
        registrationManagementClient = new RegistrationManagementClient(serverURL, tenantInfo);
        identityGovernanceRestClient = new IdentityGovernanceRestClient(serverURL, tenantInfo);
        addRegistrationFlow(registrationManagementClient);
    }

    @AfterClass(alwaysRun = true)
    public void testConclude() throws Exception {

        super.conclude();
        disableNewRegistrationFlow(identityGovernanceRestClient);
        identityGovernanceRestClient.closeHttpClient();
        registrationManagementClient.closeHttpClient();
        registrationExecutionClient.closeHttpClient();
    }

    @Test
    public void initiateRegistrationFlowWithoutEnable() throws Exception {

        Object responseObj = registrationExecutionClient.initiateRegistrationExecution();
        Assert.assertTrue(responseObj instanceof Error);
        Error error = (Error) responseObj;
        Assert.assertNotNull(error);
        Assert.assertNotNull(error.getCode());
        Assert.assertEquals(error.getCode(), "RFM-60101");
    }

    @Test(dependsOnMethods = "initiateRegistrationFlowWithoutEnable")
    public void testSubmitRegistrationFlowWithoutEnable() throws Exception {

        Object responseObj = registrationExecutionClient.submitRegistration(getRegistrationExecutionRequest(flowId));
        Assert.assertTrue(responseObj instanceof Error);
        Error error = (Error) responseObj;
        Assert.assertNotNull(error);
        Assert.assertNotNull(error.getCode());
        Assert.assertEquals(error.getCode(), "RFM-60101");
    }

    @Test(dependsOnMethods = "testSubmitRegistrationFlowWithoutEnable")
    public void testInitiateRegistrationFlow() throws Exception {

        enableNewRegistrationFlow(identityGovernanceRestClient);
        Object responseObj = registrationExecutionClient.initiateRegistrationExecution();
        Assert.assertTrue(responseObj instanceof FlowExecutionResponse);
        FlowExecutionResponse response = (FlowExecutionResponse) responseObj;
        Assert.assertNotNull(response);
        Assert.assertNotNull(response.getFlowId());
        flowId = response.getFlowId();
        Assert.assertEquals(response.getFlowStatus(), STATUS_INCOMPLETE);
        Assert.assertEquals(response.getType().toString(), TYPE_VIEW);
        Assert.assertNotNull(response.getData());
        Assert.assertNotNull(response.getData().getComponents());
        Assert.assertEquals(response.getData().getComponents().size(), 2);
    }

    @Test(dependsOnMethods = "testInitiateRegistrationFlow")
    public void testSubmitRegistrationFlowWithInvalidFlowId() throws Exception {

        Object responseObj = registrationExecutionClient.submitRegistration(getRegistrationExecutionRequest(
                "INVALID_FLOW_ID"));
        Assert.assertTrue(responseObj instanceof Error);
        Error error = (Error) responseObj;
        Assert.assertNotNull(error);
        Assert.assertNotNull(error.getCode());
        Assert.assertEquals(error.getCode(), "RFE-60001");
    }

    @Test(dependsOnMethods = "testSubmitRegistrationFlowWithInvalidFlowId")
    public void testSubmitRegistrationFlowWithEmptyInputs() throws Exception {

        Map<String, String> inputs = new HashMap<>();
        Object responseObj = registrationExecutionClient
                .submitRegistration(getRegistrationExecutionRequest(flowId, inputs));
    }

    @Test(dependsOnMethods = "testSubmitRegistrationFlowWithEmptyInputs")
    public void testSubmitRegistrationFlowWithInvalidInputs() throws Exception {

        Map<String, String> inputs = new HashMap<>();
        inputs.put("http://wso2.org/claims/username", "");
        inputs.put("password", "test@test.com");
        inputs.put("http://wso2.org/claims/emailaddress", "test@test.com");
        inputs.put("http://wso2.org/claims/givenname", "John");
        inputs.put("http://wso2.org/claims/lastname", "Doe");
        Object responseObj = registrationExecutionClient
                .submitRegistration(getRegistrationExecutionRequest(flowId, inputs));
        Assert.assertTrue(responseObj instanceof Error);
        Error error = (Error) responseObj;
        Assert.assertNotNull(error);
        Assert.assertNotNull(error.getCode());
        Assert.assertEquals(error.getCode(), "RFE-60002");
    }

    private static FlowExecutionRequest getRegistrationExecutionRequest(String flowId,
                                                                        Map<String, String> inputs) {

        FlowExecutionRequest registrationExecutionRequest = new FlowExecutionRequest();
        registrationExecutionRequest.setFlowId(flowId != null ? flowId : "FLOW_ID");
        registrationExecutionRequest.setActionId(RegistrationExecutionNegativeTest.ACTION_ID);
        registrationExecutionRequest.setInputs(inputs);
        return registrationExecutionRequest;
    }

    private static FlowExecutionRequest getRegistrationExecutionRequest(String flowId) {

        Map<String, String> inputs = new HashMap<>();
        inputs.put("http://wso2.org/claims/username", USER);
        inputs.put("password", "Wso2@test");
        inputs.put("http://wso2.org/claims/emailaddress", "test@wso2.com");
        inputs.put("http://wso2.org/claims/givenname", "RegExecNegJohn");
        inputs.put("http://wso2.org/claims/lastname", "RegExecNegDoe");
        return getRegistrationExecutionRequest(flowId, inputs);
    }
}

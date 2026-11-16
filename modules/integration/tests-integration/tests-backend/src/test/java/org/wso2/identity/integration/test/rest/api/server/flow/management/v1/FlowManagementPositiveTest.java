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
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.identity.integration.test.rest.api.server.flow.management.v1.model.FlowRequest;
import org.wso2.identity.integration.test.rest.api.server.flow.management.v1.model.FlowResponse;
import org.wso2.identity.integration.test.restclients.FlowManagementClient;

import java.io.IOException;

import static org.wso2.identity.integration.test.rest.api.server.flow.management.v1.FlowManagementTestBase.FlowTypes.PROFILE_UPDATE;
import static org.wso2.identity.integration.test.rest.api.server.flow.management.v1.FlowManagementTestBase.FlowTypes.REGISTRATION;

/**
 * This class contains the test cases for Flow Management API.
 */
public class FlowManagementPositiveTest extends FlowManagementTestBase {

    private FlowManagementClient flowManagementClient;
    private static String registrationFlowRequestJson;
    private static String profileUpdateFlowRequestJson;

    @DataProvider(name = "restAPIUserConfigProvider")
    public static Object[][] restAPIUserConfigProvider() {

        return new Object[][]{
                {TestUserMode.SUPER_TENANT_ADMIN},
                {TestUserMode.TENANT_ADMIN}
        };
    }

    @Factory(dataProvider = "restAPIUserConfigProvider")
    public FlowManagementPositiveTest(TestUserMode userMode) throws Exception {

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
        registrationFlowRequestJson = readResource(REGISTRATION_FLOW);
        profileUpdateFlowRequestJson = readResource(PROFILE_UPDATE_FLOW);
    }

    @AfterClass(alwaysRun = true)
    public void testCleanup() throws Exception {

        flowManagementClient.closeHttpClient();
        super.testConclude();
    }

    @Test(description = "Test update registration flow")
    public void testUpdateRegistrationFlow() throws Exception {

        ObjectMapper jsonReader = new ObjectMapper(new JsonFactory());
        FlowRequest registrationFlowRequest = getFlowRequest(jsonReader);
        flowManagementClient.putFlow(registrationFlowRequest);
    }

    @Test(description = "Test get registration flow", dependsOnMethods = "testUpdateRegistrationFlow")
    public void testGetRegistrationFlow() throws Exception {

        ObjectMapper jsonReader = new ObjectMapper(new JsonFactory());
        FlowRequest expectedRegistrationFlowRequest = getFlowRequest(jsonReader);
        FlowResponse registrationFlowResponse = flowManagementClient.getFlow(REGISTRATION);
        assert registrationFlowResponse.getSteps().equals(expectedRegistrationFlowRequest.getSteps())
                : "Registration flow mismatch";
    }

    @Test(description = "Test update profile update flow")
    public void testUpdateProfileUpdateFlow() throws Exception {

        ObjectMapper jsonReader = new ObjectMapper(new JsonFactory());
        FlowRequest profileUpdateFlowRequest = jsonReader.readValue(profileUpdateFlowRequestJson,
                FlowRequest.class);
        flowManagementClient.putFlow(profileUpdateFlowRequest);
    }

    @Test(description = "Test get profile update flow", dependsOnMethods = "testUpdateProfileUpdateFlow")
    public void testGetProfileUpdateFlow() throws Exception {

        ObjectMapper jsonReader = new ObjectMapper(new JsonFactory());
        FlowRequest expectedProfileUpdateFlowRequest = jsonReader.readValue(profileUpdateFlowRequestJson,
                FlowRequest.class);
        FlowResponse profileUpdateFlowResponse = flowManagementClient.getFlow(PROFILE_UPDATE);
        assert profileUpdateFlowResponse.getSteps().equals(expectedProfileUpdateFlowRequest.getSteps())
                : "Profile update flow mismatch";
    }

    private static FlowRequest getFlowRequest(ObjectMapper jsonReader)
            throws JsonProcessingException {

        return jsonReader.readValue(registrationFlowRequestJson, FlowRequest.class);
    }
}

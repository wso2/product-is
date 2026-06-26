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
import org.wso2.identity.integration.test.rest.api.server.flow.management.v1.model.Action;
import org.wso2.identity.integration.test.rest.api.server.flow.management.v1.model.Component;
import org.wso2.identity.integration.test.rest.api.server.flow.management.v1.model.Executor;
import org.wso2.identity.integration.test.rest.api.server.flow.management.v1.model.FlowRequest;
import org.wso2.identity.integration.test.rest.api.server.flow.management.v1.model.FlowResponse;
import org.wso2.identity.integration.test.rest.api.server.flow.management.v1.model.Step;
import org.wso2.identity.integration.test.restclients.FlowManagementClient;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.wso2.identity.integration.test.rest.api.server.flow.management.v1.FlowManagementTestBase.FlowTypes.PASSWORD_RECOVERY;

/**
 * Test class for the password recovery flow management API, focused on round-tripping the
 * {@code UserResolveExecutor} metadata flags {@code notifyUserExistence} and {@code notifyUserAccountStatus}.
 */
public class PasswordRecoveryFlowManagementTest extends FlowManagementTestBase {

    private static final String USER_RESOLVE_EXECUTOR = "UserResolveExecutor";
    private static final String NOTIFY_USER_EXISTENCE = "notifyUserExistence";
    private static final String NOTIFY_USER_ACCOUNT_STATUS = "notifyUserAccountStatus";

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
    public PasswordRecoveryFlowManagementTest(TestUserMode userMode) throws Exception {

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

    @Test(description = "Update the password recovery flow with notify flags set on the user resolve executor.")
    public void testUpdatePasswordRecoveryFlow() throws Exception {

        FlowRequest passwordRecoveryFlowRequest = getFlowRequest();
        flowManagementClient.putFlow(passwordRecoveryFlowRequest);
    }

    @Test(description = "Get the password recovery flow and verify the notify flags round-trip on the executor meta.",
            dependsOnMethods = "testUpdatePasswordRecoveryFlow")
    public void testGetPasswordRecoveryFlow() throws Exception {

        FlowResponse passwordRecoveryFlowResponse = flowManagementClient.getFlow(PASSWORD_RECOVERY);
        Assert.assertNotNull(passwordRecoveryFlowResponse, "Password recovery flow response should not be null.");
        Assert.assertNotNull(passwordRecoveryFlowResponse.getSteps(), "Password recovery flow steps should not be null.");

        Object meta = findUserResolveExecutorMetaInSteps(passwordRecoveryFlowResponse.getSteps());
        Assert.assertNotNull(meta, "Could not locate the UserResolveExecutor meta in the retrieved flow.");
        Assert.assertTrue(meta instanceof Map,
                "Expected the UserResolveExecutor meta to be a map but got: " + meta.getClass().getName());

        Map<?, ?> metaMap = (Map<?, ?>) meta;
        Assert.assertEquals(String.valueOf(metaMap.get(NOTIFY_USER_EXISTENCE)), "true",
                "Expected notifyUserExistence to round-trip as true on the UserResolveExecutor meta.");
        Assert.assertEquals(String.valueOf(metaMap.get(NOTIFY_USER_ACCOUNT_STATUS)), "true",
                "Expected notifyUserAccountStatus to round-trip as true on the UserResolveExecutor meta.");
    }

    private FlowRequest getFlowRequest() throws IOException {

        return new ObjectMapper(new JsonFactory()).readValue(passwordRecoveryFlowRequestJson, FlowRequest.class);
    }

    /**
     * Walk the flow steps and locate the {@code meta} object of the {@code UserResolveExecutor} action.
     *
     * @param steps Steps of the retrieved flow.
     * @return The executor meta object, or {@code null} if the executor is not found.
     */
    private Object findUserResolveExecutorMetaInSteps(List<Step> steps) {

        for (Step step : steps) {
            if (step.getData() == null) {
                continue;
            }
            Object meta = findUserResolveExecutorMeta(step.getData().getComponents());
            if (meta != null) {
                return meta;
            }
        }
        return null;
    }

    private Object findUserResolveExecutorMeta(List<Component> components) {

        if (components == null) {
            return null;
        }
        for (Component component : components) {
            Action action = component.getAction();
            if (action != null && action.getExecutor() != null) {
                Executor executor = action.getExecutor();
                if (USER_RESOLVE_EXECUTOR.equals(executor.getName())) {
                    return executor.getMeta();
                }
            }
            Object nestedMeta = findUserResolveExecutorMeta(component.getComponents());
            if (nestedMeta != null) {
                return nestedMeta;
            }
        }
        return null;
    }
}

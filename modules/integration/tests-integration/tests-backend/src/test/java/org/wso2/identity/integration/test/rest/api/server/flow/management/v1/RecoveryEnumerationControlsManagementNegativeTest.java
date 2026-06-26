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
import org.wso2.identity.integration.test.rest.api.server.flow.execution.v1.model.FlowConfig;
import org.wso2.identity.integration.test.rest.api.server.flow.management.v1.model.Action;
import org.wso2.identity.integration.test.rest.api.server.flow.management.v1.model.Component;
import org.wso2.identity.integration.test.rest.api.server.flow.management.v1.model.Executor;
import org.wso2.identity.integration.test.rest.api.server.flow.management.v1.model.FlowRequest;
import org.wso2.identity.integration.test.rest.api.server.flow.management.v1.model.FlowResponse;
import org.wso2.identity.integration.test.restclients.FlowManagementClient;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.wso2.identity.integration.test.rest.api.server.flow.management.v1.FlowManagementTestBase.FlowTypes.PASSWORD_RECOVERY;

/**
 * Negative tests for managing the password recovery flow's user enumeration and account status controls.
 */
public class RecoveryEnumerationControlsManagementNegativeTest extends FlowManagementTestBase {

    private static final String USER_RESOLVE_EXECUTOR = "UserResolveExecutor";
    private static final String NOTIFY_USER_EXISTENCE = "notifyUserExistence";
    private static final String NOTIFY_USER_ACCOUNT_STATUS = "notifyUserAccountStatus";
    private static final String CONTROLS_DISABLED = "false";

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
    public void init() throws Exception {

        super.testInit(API_VERSION, swaggerDefinition, tenantInfo.getDomain());
        flowManagementClient = new FlowManagementClient(serverURL, tenantInfo);
        passwordRecoveryFlowRequestJson = readResource(PASSWORD_RECOVERY_FLOW);
        updatePasswordRecoveryFlowStatus(true);
    }

    @AfterClass(alwaysRun = true)
    public void testCleanup() throws Exception {

        updatePasswordRecoveryFlowStatus(false);
        flowManagementClient.closeHttpClient();
        super.testConclude();
    }

    @Test(description = "Update the password recovery flow with the enumeration controls disabled on the user " +
            "resolve executor.")
    public void testUpdateFlowWithControlsDisabled() throws Exception {

        FlowRequest passwordRecoveryFlowRequest = getFlowRequest();
        setNotifyFlags(passwordRecoveryFlowRequest, CONTROLS_DISABLED);
        flowManagementClient.putFlow(passwordRecoveryFlowRequest);
    }

    @Test(description = "Get the password recovery flow and verify the notify flags round-trip as disabled on the " +
            "executor meta.", dependsOnMethods = "testUpdateFlowWithControlsDisabled")
    public void testGetFlowReflectsControlsDisabled() throws Exception {

        FlowResponse passwordRecoveryFlowResponse = flowManagementClient.getFlow(PASSWORD_RECOVERY);
        Object meta = findUserResolveExecutorMeta(
                passwordRecoveryFlowResponse.getSteps().getFirst().getData().getComponents());
        Assert.assertTrue(meta instanceof Map, "UserResolveExecutor meta not found in the retrieved flow.");

        Map<?, ?> metaMap = (Map<?, ?>) meta;
        Assert.assertEquals(String.valueOf(metaMap.get(NOTIFY_USER_EXISTENCE)), CONTROLS_DISABLED);
        Assert.assertEquals(String.valueOf(metaMap.get(NOTIFY_USER_ACCOUNT_STATUS)), CONTROLS_DISABLED);
    }

    private FlowRequest getFlowRequest() throws IOException {

        return new ObjectMapper(new JsonFactory()).readValue(passwordRecoveryFlowRequestJson, FlowRequest.class);
    }

    /**
     * Set both enumeration-control flags on the {@code UserResolveExecutor} meta to the given value.
     */
    @SuppressWarnings("unchecked")
    private void setNotifyFlags(FlowRequest flowRequest, String value) {

        Object meta = findUserResolveExecutorMeta(flowRequest.getSteps().get(0).getData().getComponents());
        Assert.assertTrue(meta instanceof Map, "UserResolveExecutor meta not found in the flow definition.");
        Map<String, Object> metaMap = (Map<String, Object>) meta;
        metaMap.put(NOTIFY_USER_EXISTENCE, value);
        metaMap.put(NOTIFY_USER_ACCOUNT_STATUS, value);
    }

    private void updatePasswordRecoveryFlowStatus(boolean enable) throws Exception {

        FlowConfig flowConfig = new FlowConfig();
        flowConfig.setFlowType(PASSWORD_RECOVERY);
        flowConfig.setIsEnabled(enable);
        flowManagementClient.updateFlowConfig(flowConfig);
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

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
import org.wso2.identity.integration.test.rest.api.server.flow.execution.v1.model.Component;
import org.wso2.identity.integration.test.rest.api.server.flow.execution.v1.model.FlowExecutionRequest;
import org.wso2.identity.integration.test.rest.api.server.flow.execution.v1.model.FlowExecutionResponse;
import org.wso2.identity.integration.test.rest.api.server.flow.execution.v1.model.Message;
import org.wso2.identity.integration.test.rest.api.server.identity.governance.v1.dto.ConnectorsPatchReq;
import org.wso2.identity.integration.test.rest.api.server.identity.governance.v1.dto.PropertyReq;
import org.wso2.identity.integration.test.rest.api.user.common.model.Email;
import org.wso2.identity.integration.test.rest.api.user.common.model.Name;
import org.wso2.identity.integration.test.rest.api.user.common.model.PatchOperationRequestObject;
import org.wso2.identity.integration.test.rest.api.user.common.model.UserItemAddGroupobj;
import org.wso2.identity.integration.test.rest.api.user.common.model.UserObject;
import org.wso2.identity.integration.test.restclients.FlowExecutionClient;
import org.wso2.identity.integration.test.restclients.FlowManagementClient;
import org.wso2.identity.integration.test.restclients.IdentityGovernanceRestClient;
import org.wso2.identity.integration.test.restclients.SCIM2RestClient;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.wso2.identity.integration.test.rest.api.server.flow.execution.v1.FlowExecutionTestBase.FlowTypes.PASSWORD_RECOVERY;

/**
 * Tests the user enumeration and account status controls of the password recovery flow execution — the
 * {@code UserResolveExecutor}'s {@code notifyUserExistence} and {@code notifyUserAccountStatus} flags.
 *
 * <p>With both flags enabled, the executor must surface user-facing error messages (instead of silently
 * advancing the flow to prevent user enumeration) when the submitted identifier does not resolve to a user,
 * or resolves to a user whose account is locked or disabled.</p>
 */
public class RecoveryEnumerationControlsExecutionPositiveTest extends FlowExecutionTestBase {

    private static final String USERNAME_CLAIM = "http://wso2.org/claims/username";
    private static final String USER_RESOLVE_ACTION_ID = "button_1ov4";

    private static final String USER_SYSTEM_SCHEMA_ATTRIBUTE = "urn:scim:wso2:schema";
    private static final String ACCOUNT_LOCKED_ATTRIBUTE = "accountLocked";
    private static final String ACCOUNT_DISABLED_ATTRIBUTE = "accountDisabled";

    private static final String LOCKED_USER = "PwdRecLockedUser";
    private static final String LOCKED_USER_EMAIL = "pwd.rec.locked@example.com";
    private static final String DISABLED_USER = "PwdRecDisabledUser";
    private static final String DISABLED_USER_EMAIL = "pwd.rec.disabled@example.com";
    private static final String NON_EXISTENT_USER = "PwdRecNonExistentUser";
    private static final String ACTIVE_USER = "PwdRecActiveUser";
    private static final String ACTIVE_USER_EMAIL = "pwd.rec.active@example.com";
    private static final String TEST_USER_PASSWORD = "Wso2@Test123";

    private static final String USER_NOT_FOUND_I18N_KEY = "user.not.found";
    private static final String ACCOUNT_LOCKED_I18N_KEY_PREFIX = "account.locked";
    private static final String ACCOUNT_DISABLED_I18N_KEY = "account.disabled";

    private FlowExecutionClient flowExecutionClient;
    private FlowManagementClient flowManagementClient;
    private IdentityGovernanceRestClient identityGovernanceRestClient;
    private SCIM2RestClient scim2RestClient;

    private String lockedUserId;
    private String disabledUserId;
    private String activeUserId;

    @DataProvider(name = "restAPIUserConfigProvider")
    public static Object[][] restAPIUserConfigProvider() {

        return new Object[][]{
                {TestUserMode.SUPER_TENANT_ADMIN},
                {TestUserMode.TENANT_ADMIN}
        };
    }

    @Factory(dataProvider = "restAPIUserConfigProvider")
    public RecoveryEnumerationControlsExecutionPositiveTest(TestUserMode userMode) throws Exception {

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
        identityGovernanceRestClient = new IdentityGovernanceRestClient(serverURL, tenantInfo);
        scim2RestClient = new SCIM2RestClient(serverURL, tenantInfo);

        updatePasswordRecoveryFeatureStatus(true);
        // The account disable handler is off by default; it must be enabled for the accountDisabled claim to be
        // honoured by the UserResolveExecutor's account status check.
        setAccountDisableHandlerStatus(true);
        addPasswordRecoveryFlow(flowManagementClient);
        enablePasswordRecoveryFlow(flowManagementClient);

        lockedUserId = createUser(LOCKED_USER, LOCKED_USER_EMAIL);
        setAccountState(lockedUserId, ACCOUNT_LOCKED_ATTRIBUTE, true);

        disabledUserId = createUser(DISABLED_USER, DISABLED_USER_EMAIL);
        setAccountState(disabledUserId, ACCOUNT_DISABLED_ATTRIBUTE, true);

        activeUserId = createUser(ACTIVE_USER, ACTIVE_USER_EMAIL);
    }

    @AfterClass(alwaysRun = true)
    public void tearDownClass() throws Exception {

        if (lockedUserId != null) {
            scim2RestClient.deleteUser(lockedUserId);
        }
        if (disabledUserId != null) {
            scim2RestClient.deleteUser(disabledUserId);
        }
        if (activeUserId != null) {
            scim2RestClient.deleteUser(activeUserId);
        }
        disablePasswordRecoveryFlow(flowManagementClient);
        updatePasswordRecoveryFeatureStatus(false);
        setAccountDisableHandlerStatus(false);
        identityGovernanceRestClient.closeHttpClient();
        flowExecutionClient.closeHttpClient();
        flowManagementClient.closeHttpClient();
        scim2RestClient.closeHttpClient();
    }

    @Test(description = "Initiate the password recovery flow and verify the username collection step is rendered.")
    public void testInitiatePasswordRecoveryFlow() throws Exception {

        Object responseObj = flowExecutionClient.initiateFlowExecution(PASSWORD_RECOVERY);
        Assert.assertTrue(responseObj instanceof FlowExecutionResponse);
        FlowExecutionResponse response = (FlowExecutionResponse) responseObj;
        Assert.assertNotNull(response.getFlowId());
        Assert.assertEquals(response.getFlowStatus(), STATUS_INCOMPLETE);
        Assert.assertEquals(response.getType().toString(), TYPE_VIEW);
    }

    @Test(description = "Submit a non-existent identifier with notifyUserExistence enabled and expect an error.",
            dependsOnMethods = "testInitiatePasswordRecoveryFlow")
    public void testNonExistentIdentifierIsNotified() throws Exception {

        FlowExecutionResponse response = submitIdentifier(NON_EXISTENT_USER);
        assertErrorMessage(response, USER_NOT_FOUND_I18N_KEY);
    }

    @Test(description = "Submit a locked user with notifyUserAccountStatus enabled and expect an error.",
            dependsOnMethods = "testNonExistentIdentifierIsNotified")
    public void testLockedAccountIsNotified() throws Exception {

        FlowExecutionResponse response = submitIdentifier(LOCKED_USER);
        assertErrorMessage(response, ACCOUNT_LOCKED_I18N_KEY_PREFIX);
    }

    @Test(description = "Submit a disabled user with notifyUserAccountStatus enabled and expect an error.",
            dependsOnMethods = "testLockedAccountIsNotified")
    public void testDisabledAccountIsNotified() throws Exception {

        FlowExecutionResponse response = submitIdentifier(DISABLED_USER);
        assertErrorMessage(response, ACCOUNT_DISABLED_I18N_KEY);
    }

    @Test(description = "Submit a valid active user with the controls enabled and expect the flow to advance " +
            "past user resolution without any enumeration or account-status error.",
            dependsOnMethods = "testDisabledAccountIsNotified")
    public void testActiveUserResolvesAndAdvances() throws Exception {

        FlowExecutionResponse response = submitIdentifier(ACTIVE_USER);

        // Flow continues as a VIEW step (the next step is rendered), not terminated.
        Assert.assertEquals(response.getFlowStatus(), STATUS_INCOMPLETE);
        Assert.assertEquals(response.getType().toString(), TYPE_VIEW);

        // No enumeration / account-status ERROR was surfaced for a valid active user.
        Assert.assertFalse(hasErrorMessage(response),
                "Unexpected ERROR message for a valid active user: " + response.getData().getMessages());

        // The flow advanced to the OTP verification step (proves it moved past user resolution).
        Assert.assertTrue(containsOtpComponent(response.getData().getComponents()),
                "Expected the OTP verification step to be rendered after resolving a valid user.");
    }

    /**
     * Initiate a fresh password recovery flow and submit the given username at the user resolution step.
     *
     * @param username Username submitted to the {@code UserResolveExecutor}.
     * @return The flow execution response of the user resolution step.
     * @throws Exception If the flow could not be initiated or executed.
     */
    private FlowExecutionResponse submitIdentifier(String username) throws Exception {

        Object initiationObj = flowExecutionClient.initiateFlowExecution(PASSWORD_RECOVERY);
        Assert.assertTrue(initiationObj instanceof FlowExecutionResponse);
        String flowId = ((FlowExecutionResponse) initiationObj).getFlowId();

        FlowExecutionRequest request = new FlowExecutionRequest();
        request.setFlowType(PASSWORD_RECOVERY);
        request.setFlowId(flowId);
        request.setActionId(USER_RESOLVE_ACTION_ID);
        Map<String, String> inputs = new HashMap<>();
        inputs.put(USERNAME_CLAIM, username);
        request.setInputs(inputs);

        Object responseObj = flowExecutionClient.executeFlow(request);
        Assert.assertTrue(responseObj instanceof FlowExecutionResponse);
        return (FlowExecutionResponse) responseObj;
    }

    /**
     * Assert that the response re-prompts the same step and carries an ERROR message with the expected i18n key.
     * The i18n key is matched as a prefix to tolerate reason-specific variants (e.g. account locked reasons).
     */
    private void assertErrorMessage(FlowExecutionResponse response, String expectedI18nKeyPrefix) {

        Assert.assertEquals(response.getFlowStatus(), STATUS_INCOMPLETE);
        Assert.assertEquals(response.getType().toString(), TYPE_VIEW);

        List<Message> messages = response.getData().getMessages();
        boolean hasExpectedError = messages != null && messages.stream().anyMatch(message ->
                message.getType() == Message.TypeEnum.ERROR
                        && message.getI18nKey() != null
                        && message.getI18nKey().startsWith(expectedI18nKeyPrefix));
        Assert.assertTrue(hasExpectedError,
                "No ERROR message with i18n key '" + expectedI18nKeyPrefix + "' in: " + messages);
    }

    /**
     * Whether the response carries any ERROR message (e.g. an enumeration or account-status notification).
     */
    private boolean hasErrorMessage(FlowExecutionResponse response) {

        List<Message> messages = response.getData().getMessages();
        return messages != null && messages.stream()
                .anyMatch(message -> message.getType() == Message.TypeEnum.ERROR);
    }

    /**
     * Recursively walk the rendered components to detect the OTP verification step, identified by an
     * INPUT component with the {@code OTP} variant.
     */
    private boolean containsOtpComponent(List<Component> components) {

        if (components == null) {
            return false;
        }
        for (Component component : components) {
            if ("OTP".equals(component.getVariant())) {
                return true;
            }
            if (containsOtpComponent(component.getComponents())) {
                return true;
            }
        }
        return false;
    }

    private String createUser(String username, String email) throws Exception {

        UserObject user = new UserObject()
                .userName(username)
                .password(TEST_USER_PASSWORD)
                .name(new Name().givenName(username).familyName("User"))
                .addEmail(new Email().value(email));
        return scim2RestClient.createUser(user);
    }

    private void setAccountState(String userId, String attribute, boolean value) throws IOException {

        UserItemAddGroupobj patchOp = new UserItemAddGroupobj().op(UserItemAddGroupobj.OpEnum.REPLACE);
        patchOp.setPath(USER_SYSTEM_SCHEMA_ATTRIBUTE + ":" + attribute);
        patchOp.setValue(value);
        scim2RestClient.updateUser(new PatchOperationRequestObject().addOperations(patchOp), userId);
    }

    private void updatePasswordRecoveryFeatureStatus(boolean enable) throws IOException {

        ConnectorsPatchReq connectorsPatchReq = new ConnectorsPatchReq();
        connectorsPatchReq.setOperation(ConnectorsPatchReq.OperationEnum.UPDATE);
        PropertyReq propertyReq = new PropertyReq();
        propertyReq.setName("Recovery.Notification.Password.emailLink.Enable");
        propertyReq.setValue(enable ? "true" : "false");
        connectorsPatchReq.addProperties(propertyReq);
        identityGovernanceRestClient.updateConnectors("QWNjb3VudCBNYW5hZ2VtZW50",
                "YWNjb3VudC1yZWNvdmVyeQ", connectorsPatchReq);
    }

    private void setAccountDisableHandlerStatus(boolean enable) throws IOException {

        ConnectorsPatchReq connectorsPatchReq = new ConnectorsPatchReq();
        connectorsPatchReq.setOperation(ConnectorsPatchReq.OperationEnum.UPDATE);
        PropertyReq propertyReq = new PropertyReq();
        propertyReq.setName("account.disable.handler.enable");
        propertyReq.setValue(enable ? "true" : "false");
        connectorsPatchReq.addProperties(propertyReq);
        // Category "Account Management", connector "account.disable.handler".
        identityGovernanceRestClient.updateConnectors("QWNjb3VudCBNYW5hZ2VtZW50",
                "YWNjb3VudC5kaXNhYmxlLmhhbmRsZXI", connectorsPatchReq);
    }
}

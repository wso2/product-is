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

package org.wso2.identity.integration.test.serviceextensions.preupdatepassword.v1.execution;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.StringUtils;
import org.testng.annotations.*;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.identity.integration.test.base.MockSMSProvider;
import org.wso2.identity.integration.test.recovery.model.v2.ConfirmModel;
import org.wso2.identity.integration.test.recovery.model.v2.InitModel;
import org.wso2.identity.integration.test.recovery.model.v2.RecoverModel;
import org.wso2.identity.integration.test.recovery.model.v2.ResetModel;
import org.wso2.identity.integration.test.recovery.model.v2.UserClaim;
import org.wso2.identity.integration.test.rest.api.server.identity.governance.v1.dto.ConnectorsPatchReq;
import org.wso2.identity.integration.test.rest.api.server.identity.governance.v1.dto.PropertyReq;
import org.wso2.identity.integration.test.rest.api.server.notification.sender.v1.model.Properties;
import org.wso2.identity.integration.test.rest.api.server.notification.sender.v1.model.SMSSender;
import org.wso2.identity.integration.test.restclients.NotificationSenderRestClient;
import org.wso2.identity.integration.test.restclients.PasswordRecoveryV2RestClient;
import org.wso2.identity.integration.test.serviceextensions.common.execution.dataprovider.model.ActionResponse;
import org.wso2.identity.integration.test.serviceextensions.common.execution.dataprovider.model.ExpectedPasswordUpdateResponse;
import org.wso2.identity.integration.test.serviceextensions.common.execution.mockservices.ServiceExtensionMockServer;
import org.wso2.identity.integration.test.serviceextensions.common.execution.model.*;
import org.wso2.identity.integration.test.rest.api.server.notification.sender.v1.model.Error;
import org.wso2.identity.integration.test.rest.api.user.common.model.*;
import org.wso2.identity.integration.test.restclients.SCIM2RestClient;
import org.wso2.identity.integration.test.utils.FileUtils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * Integration test class for testing the pre update password action failure.
 * This test case extends {@link PreUpdatePasswordActionBaseTestCase} and focuses on failed scenario
 * on SMS OTP based password recovery/reset via V2 recovery API.
 */
public class PreUpdatePasswordActionSmsOtpFailureTestCase extends PreUpdatePasswordActionBaseTestCase {

    private static final String MOBILE = "+941111111111";
    private static final String SMS_SENDER_REQUEST_FORMAT = "{\"content\": {{body}}, \"to\": {{mobile}} }";

    private final String tenantId;
    private final TestUserMode userMode;

    private SCIM2RestClient scim2RestClient;
    private NotificationSenderRestClient notificationSenderRestClient;
    private PasswordRecoveryV2RestClient passwordRecoveryV2RestClient;

    private String actionId;
    private String userId;
    private UserObject userInfo;

    private ServiceExtensionMockServer serviceExtensionMockServer;
    private MockSMSProvider mockSMSProvider;

    private final ActionResponse actionResponse;
    private final ExpectedPasswordUpdateResponse expectedPasswordUpdateResponse;

    @Factory(dataProvider = "testExecutionContextProvider")
    public PreUpdatePasswordActionSmsOtpFailureTestCase(TestUserMode testUserMode, ActionResponse actionResponse,
                                                        ExpectedPasswordUpdateResponse expectedPasswordUpdateResponse) {

        this.userMode = testUserMode;
        this.tenantId = testUserMode == TestUserMode.SUPER_TENANT_USER ? "-1234" : "1";
        this.actionResponse = actionResponse;
        this.expectedPasswordUpdateResponse = expectedPasswordUpdateResponse;
    }

    @DataProvider(name = "testExecutionContextProvider")
    public static Object[][] getTestExecutionContext() throws IOException, URISyntaxException {

        return new Object[][]{
                {TestUserMode.SUPER_TENANT_USER, new ActionResponse(HttpServletResponse.SC_OK,
                        FileUtils.readFileInClassPathAsString("actions/response/failure-response.json")),
                        new ExpectedPasswordUpdateResponse(HttpServletResponse.SC_BAD_REQUEST,
                                "Some failure reason",
                                "Some description")},
                {TestUserMode.TENANT_USER, new ActionResponse(HttpServletResponse.SC_OK,
                        FileUtils.readFileInClassPathAsString("actions/response/failure-response.json")),
                        new ExpectedPasswordUpdateResponse(HttpServletResponse.SC_BAD_REQUEST,
                                "Some failure reason",
                                "Some description")}
        };
    }

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.init(userMode);

        scim2RestClient = new SCIM2RestClient(serverURL, tenantInfo);
        passwordRecoveryV2RestClient = new PasswordRecoveryV2RestClient(serverURL, tenantInfo);

        userInfo = new UserObject()
                .userName(TEST_USER1_USERNAME)
                .password(TEST_USER_PASSWORD)
                .addPhoneNumbers(new PhoneNumbers().type("mobile").value(MOBILE))
                .name(new Name().givenName(TEST_USER_GIVEN_NAME).familyName(TEST_USER_LASTNAME))
                .addEmail(new Email().value(TEST_USER_EMAIL));
        userId = scim2RestClient.createUser(userInfo);

        updatePasswordBasedRecoveryFeatureStatus(true);

        actionId = createPreUpdatePasswordAction(ACTION_NAME, ACTION_DESCRIPTION);

        mockSMSProvider = new MockSMSProvider();
        mockSMSProvider.start();
        String backendServicesUrl = backendURL.replace("services/", "");
        notificationSenderRestClient = new NotificationSenderRestClient(backendServicesUrl, tenantInfo);
        SMSSender smsSender = initSMSSender();
        notificationSenderRestClient.createSMSProvider(smsSender);

        serviceExtensionMockServer = new ServiceExtensionMockServer();
        serviceExtensionMockServer.startServer();
        serviceExtensionMockServer.setupStub(MOCK_SERVER_ENDPOINT_RESOURCE_PATH,
                "Basic " + getBase64EncodedString(MOCK_SERVER_AUTH_BASIC_USERNAME,
                        MOCK_SERVER_AUTH_BASIC_PASSWORD),
                actionResponse.getResponseBody(), actionResponse.getStatusCode());
    }

    @BeforeMethod
    public void setUp() {

        serviceExtensionMockServer.resetRequests();
    }

    @Test
    public void testPasswordResetFailsForSmsOtpWhenPrePasswordUpdateActionFails() throws Exception {

        // Step 1: Build user claim.
        UserClaim usernameClaim = new UserClaim()
                .uri("http://wso2.org/claims/username")
                .value(userInfo.getUserName());

        InitModel initModel = new InitModel().claims(List.of(usernameClaim));

        // Step 2: Initiate password recovery via SMS.
        RecoverModel recoverModel = passwordRecoveryV2RestClient.init(initModel, "SMS");
        assertNotNull(recoverModel, "Recovery model should not be null");
        assertTrue(StringUtils.isNotBlank(recoverModel.getChannelId()), "Channel ID should not be blank");
        assertTrue(StringUtils.isNotBlank(recoverModel.getRecoveryCode()), "Recovery code should not be blank");

        // Step 3: Trigger recovery to get flow confirmation code.
        String flowConfirmationCode = passwordRecoveryV2RestClient.recover(recoverModel);
        assertTrue(StringUtils.isNotBlank(flowConfirmationCode), "Flow confirmation code should not be blank");

        // Step 4: Extract OTP and confirm.
        String otp = extractOtp(mockSMSProvider.getSmsContent());
        assertTrue(StringUtils.isNotBlank(otp), "OTP should not be blank");

        ConfirmModel confirmModel = new ConfirmModel()
                .confirmationCode(flowConfirmationCode)
                .otp(otp);

        String resetCode = passwordRecoveryV2RestClient.confirm(confirmModel);
        assertTrue(StringUtils.isNotBlank(resetCode), "Reset code should not be blank");

        // Step 5: Attempt password reset.
        ResetModel resetModel = new ResetModel()
                .resetCode(resetCode)
                .flowConfirmationCode(flowConfirmationCode)
                .password(TEST_USER_UPDATED_PASSWORD);

        Error errorResponse = passwordRecoveryV2RestClient.reset(resetModel);

        // Step 6: Validate error response (e.g. due to password policy violation).
        assertNotNull(errorResponse, "Expected error response on reset attempt");
        assertEquals(errorResponse.getCode(), "PWR-20067");
        assertEquals(errorResponse.getMessage(), expectedPasswordUpdateResponse.getErrorMessage());
        assertEquals(errorResponse.getDescription(), expectedPasswordUpdateResponse.getErrorDetail());
        assertTrue(StringUtils.isNotBlank(errorResponse.getTraceId()), "Trace ID should not be blank");

        // Step 7: Assert the pre password update action.
        assertActionRequestPayload(
                userId,
                TEST_USER_UPDATED_PASSWORD,
                PreUpdatePasswordEvent.FlowInitiatorType.USER,
                PreUpdatePasswordEvent.Action.RESET
                                  );
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {

        updatePasswordBasedRecoveryFeatureStatus(false);
        deleteAction(PRE_UPDATE_PASSWORD_API_PATH, actionId);
        scim2RestClient.deleteUser(userId);
        notificationSenderRestClient.deleteSMSProvider();
        identityGovernanceRestClient.closeHttpClient();
        scim2RestClient.closeHttpClient();
        actionsRestClient.closeHttpClient();
        passwordRecoveryV2RestClient.closeHttpClient();
        client.close();
        serviceExtensionMockServer.stopServer();
        serviceExtensionMockServer = null;
        mockSMSProvider.stop();
    }

    private void assertActionRequestPayload(String userId, String updatedPassword,
                                            PreUpdatePasswordEvent.FlowInitiatorType initiatorType,
                                            PreUpdatePasswordEvent.Action action) throws JsonProcessingException {

        String actualRequestPayload =
                serviceExtensionMockServer.getReceivedRequestPayload(MOCK_SERVER_ENDPOINT_RESOURCE_PATH);
        PreUpdatePasswordActionRequest actionRequest = new ObjectMapper()
                .readValue(actualRequestPayload, PreUpdatePasswordActionRequest.class);

        assertEquals(actionRequest.getActionType(), ActionType.PRE_UPDATE_PASSWORD);
        assertEquals(actionRequest.getEvent().getTenant().getName(), tenantInfo.getDomain());
        assertEquals(actionRequest.getEvent().getTenant().getId(), tenantId);
        assertEquals(actionRequest.getEvent().getUserStore().getName(), PRIMARY_USER_STORE_NAME);
        assertEquals(actionRequest.getEvent().getUserStore().getId(), PRIMARY_USER_STORE_ID);

        PasswordUpdatingUser user = actionRequest.getEvent().getPasswordUpdatingUser();

        assertEquals(user.getId(), userId);
        assertEquals(user.getUpdatingCredential().getType(), Credential.Type.PASSWORD);
        assertEquals(user.getUpdatingCredential().getFormat(), Credential.Format.PLAIN_TEXT);
        assertEquals(user.getUpdatingCredential().getValue(), updatedPassword.toCharArray());
        assertEquals(actionRequest.getEvent().getInitiatorType(), initiatorType);
        assertEquals(actionRequest.getEvent().getAction(), action);
    }

    private SMSSender initSMSSender() {

        SMSSender smsSender = new SMSSender();
        smsSender.setProvider(MockSMSProvider.SMS_SENDER_PROVIDER_TYPE);
        smsSender.setProviderURL(MockSMSProvider.SMS_SENDER_URL);
        smsSender.contentType(SMSSender.ContentTypeEnum.JSON);
        ArrayList<Properties> properties = new ArrayList<>();
        properties.add(new Properties().key("body").value(SMS_SENDER_REQUEST_FORMAT));
        smsSender.setProperties(properties);
        return smsSender;
    }

    private void updatePasswordBasedRecoveryFeatureStatus(boolean enable) throws IOException {

        ConnectorsPatchReq connectorsPatchReq = new ConnectorsPatchReq();
        connectorsPatchReq.setOperation(ConnectorsPatchReq.OperationEnum.UPDATE);

        PropertyReq enableSMSOtpRecovery = new PropertyReq()
                .name("Recovery.Notification.Password.smsOtp.Enable")
                .value(enable ? "true" : "false");

        PropertyReq enablePasswordRecovery = new PropertyReq()
                .name("Recovery.Notification.Password.Enable")
                .value(enable ? "true" : "false");

        connectorsPatchReq.addProperties(enablePasswordRecovery);
        connectorsPatchReq.addProperties(enableSMSOtpRecovery);

        identityGovernanceRestClient.updateConnectors("QWNjb3VudCBNYW5hZ2VtZW50",
                "YWNjb3VudC1yZWNvdmVyeQ", connectorsPatchReq);
    }

    private String extractOtp(String message) {

        String prefix = "Your One-Time Password : ";
        if (message != null && message.contains(prefix)) {
            return message.substring(message.indexOf(prefix) + prefix.length()).trim();
        }
        return null;
    }
}

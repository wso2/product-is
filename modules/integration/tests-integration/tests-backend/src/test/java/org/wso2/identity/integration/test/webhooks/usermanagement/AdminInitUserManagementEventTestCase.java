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

package org.wso2.identity.integration.test.webhooks.usermanagement;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;
import org.wso2.identity.integration.test.rest.api.server.identity.governance.v1.dto.ConnectorsPatchReq;
import org.wso2.identity.integration.test.rest.api.server.identity.governance.v1.dto.PropertyReq;
import org.wso2.identity.integration.test.rest.api.user.common.model.Email;
import org.wso2.identity.integration.test.rest.api.user.common.model.Name;
import org.wso2.identity.integration.test.rest.api.user.common.model.PatchOperationRequestObject;
import org.wso2.identity.integration.test.rest.api.user.common.model.UserItemAddGroupobj;
import org.wso2.identity.integration.test.rest.api.user.common.model.UserObject;
import org.wso2.identity.integration.test.restclients.IdentityGovernanceRestClient;
import org.wso2.identity.integration.test.restclients.SCIM2RestClient;
import org.wso2.identity.integration.test.webhooks.usermanagement.eventpayloadbuilder.AdminInitUserManagementEventTestExpectedEventPayloadBuilder;
import org.wso2.identity.integration.test.webhooks.util.WebhookEventTestManager;

import java.util.Arrays;

import static org.testng.Assert.assertNotNull;

/**
 * This class tests the user management events triggered by the admin user via SCIM2 User API.
 */
public class AdminInitUserManagementEventTestCase extends ISIntegrationTest {

    public static final String GOVERNANCE_CATEGORY_ACCOUNT_MANAGEMENT_ID = "QWNjb3VudCBNYW5hZ2VtZW50";
    public static final String ACCOUNT_DISABLE_CONNECTOR_ID = "YWNjb3VudC5kaXNhYmxlLmhhbmRsZXI";
    private String userId;
    private final AutomationContext automationContext;
    private final TestUserMode userMode;
    private WebhookEventTestManager webhookEventTestManager;
    private SCIM2RestClient scim2RestClient;
    private IdentityGovernanceRestClient identityGovernanceRestClient;

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.init(userMode);

        scim2RestClient = new SCIM2RestClient(serverURL, tenantInfo);
        identityGovernanceRestClient = new IdentityGovernanceRestClient(serverURL, tenantInfo);

        enableUserAccountDisablingFeature();

        webhookEventTestManager = new WebhookEventTestManager("/scim2/webhook", "WSO2",
                Arrays.asList("https://schemas.identity.wso2.org/events/user",
                        "https://schemas.identity.wso2.org/events/registration"),
                "AdminInitUserManagementEventTestCase",
                automationContext);

    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {

        disableUserAccountDisablingFeature();
        scim2RestClient.closeHttpClient();
        identityGovernanceRestClient.closeHttpClient();
        webhookEventTestManager.teardown();
    }

    @Factory(dataProvider = "testExecutionContextProvider")
    public AdminInitUserManagementEventTestCase(TestUserMode userMode) throws Exception {

        this.userMode = userMode;
        automationContext = new AutomationContext("IDENTITY", userMode);
    }

    @DataProvider(name = "testExecutionContextProvider")
    public static Object[][] getTestExecutionContext() {

        return new Object[][]{
                {TestUserMode.SUPER_TENANT_USER},
                {TestUserMode.TENANT_USER}
        };
    }

    @Test
    public void testCreateUser() throws Exception {

        UserObject userInfo = new UserObject()
                .userName("test-user")
                .password("TestPassword@123")
                .name(new Name().givenName("test-user-given-name").familyName("test-user-last-name"))
                .addEmail(new Email().value("test-user@test.com"));
        userId = scim2RestClient.createUser(userInfo);
        assertNotNull(userId);

        webhookEventTestManager.stackExpectedEventPayload(
                "https://schemas.identity.wso2.org/events/registration/event-type/registrationSuccess",
                AdminInitUserManagementEventTestExpectedEventPayloadBuilder.buildExpectedRegistrationSuccessEventPayloadForTestCreateUser(
                        userId, automationContext.getContextTenant().getDomain()));
        webhookEventTestManager.stackExpectedEventPayload(
                "https://schemas.identity.wso2.org/events/user/event-type/userCreated",
                AdminInitUserManagementEventTestExpectedEventPayloadBuilder.buildExpectedUserCreatedEventPayloadForTestCreateUser(
                        userId, automationContext.getContextTenant().getDomain()));
        webhookEventTestManager.validateStackedEventPayloads();
    }

    @Test(dependsOnMethods = "testCreateUser")
    public void testCreateUserFailure() throws Exception {

        UserObject userInfo = new UserObject()
                .userName("password-incompatible-test-user")
                .password("a");
        scim2RestClient.createUserWithInvalidRequest(userInfo);

        webhookEventTestManager.stackExpectedEventPayload(
                "https://schemas.identity.wso2.org/events/registration/event-type/registrationFailed",
                AdminInitUserManagementEventTestExpectedEventPayloadBuilder.buildExpectedRegistrationFailedEventPayload(
                        automationContext.getContextTenant().getDomain(),
                        "20035 - The minimum length of password should be 8."));
        webhookEventTestManager.validateStackedEventPayloads();
    }

    @Test(dependsOnMethods = "testCreateUser")
    public void testUserProfileUpdateWithoutAnyAccountStateManagementClaims() throws Exception {

        PatchOperationRequestObject patchRequest = new PatchOperationRequestObject();
        UserItemAddGroupobj addNickName = new UserItemAddGroupobj().op(UserItemAddGroupobj.OpEnum.ADD);
        addNickName.setPath("nickName");
        addNickName.setValue("test-user-nickname");
        patchRequest.addOperations(addNickName);

        UserItemAddGroupobj replaceCountry = new UserItemAddGroupobj().op(UserItemAddGroupobj.OpEnum.REPLACE);
        replaceCountry.setPath("urn:scim:wso2:schema:country");
        replaceCountry.setValue("United States");
        patchRequest.addOperations(replaceCountry);

        UserItemAddGroupobj replaceEmails = new UserItemAddGroupobj().op(UserItemAddGroupobj.OpEnum.REPLACE);
        replaceEmails.setPath("urn:scim:wso2:schema:emailAddresses");
        replaceEmails.setValue(Arrays.asList("test-user-personal@test.com", "test-user-work@test.com"));
        patchRequest.addOperations(replaceEmails);

        UserItemAddGroupobj removeGivenName = new UserItemAddGroupobj().op(UserItemAddGroupobj.OpEnum.REMOVE);
        removeGivenName.setPath("name:givenName");
        patchRequest.addOperations(removeGivenName);

        scim2RestClient.updateUser(patchRequest, userId);

        webhookEventTestManager.stackExpectedEventPayload(
                "https://schemas.identity.wso2.org/events/user/event-type/userProfileUpdated",
                AdminInitUserManagementEventTestExpectedEventPayloadBuilder.buildExpectedUserProfileUpdatedEventPayloadWithoutAnyAccountStateManagementClaims(
                        userId, automationContext.getContextTenant().getDomain()));
        webhookEventTestManager.validateStackedEventPayloads();
    }

//     @Test(dependsOnMethods = "testUserProfileUpdateWithoutAnyAccountStateManagementClaims")
//     public void testUserProfileUpdateWithAccountLock() throws Exception {

//         PatchOperationRequestObject patchRequest = new PatchOperationRequestObject();
//         UserItemAddGroupobj addGivenName = new UserItemAddGroupobj().op(UserItemAddGroupobj.OpEnum.REPLACE);
//         addGivenName.setPath("name:givenName");
//         addGivenName.setValue("test-user-given-name");
//         patchRequest.addOperations(addGivenName);

//         UserItemAddGroupobj setAccountLock = new UserItemAddGroupobj().op(UserItemAddGroupobj.OpEnum.REPLACE);
//         setAccountLock.setPath("urn:scim:wso2:schema:accountLocked");
//         setAccountLock.setValue(true);
//         patchRequest.addOperations(setAccountLock);

//         scim2RestClient.updateUser(patchRequest, userId);

//         webhookEventTestManager.stackExpectedEventPayload(
//                 "https://schemas.identity.wso2.org/events/user/event-type/userProfileUpdated",
//                 AdminInitUserManagementEventTestExpectedEventPayloadBuilder.buildExpectedUserProfileUpdatedEventPayloadWithAccountLockClaim(
//                         userId, automationContext.getContextTenant().getDomain()));
//         webhookEventTestManager.stackExpectedEventPayload(
//                 "https://schemas.identity.wso2.org/events/user/event-type/userAccountLocked",
//                 AdminInitUserManagementEventTestExpectedEventPayloadBuilder.buildExpectedAccountLockedEventPayload(
//                         userId, automationContext.getContextTenant().getDomain()));
//         webhookEventTestManager.validateStackedEventPayloads();
//     }

//     @Test(dependsOnMethods = "testUserProfileUpdateWithAccountLock")
//     public void testAccountUnlock() throws Exception {

//         PatchOperationRequestObject patchRequest = new PatchOperationRequestObject();
//         UserItemAddGroupobj setAccountLock = new UserItemAddGroupobj().op(UserItemAddGroupobj.OpEnum.REPLACE);
//         setAccountLock.setPath("urn:scim:wso2:schema:accountLocked");
//         setAccountLock.setValue(false);
//         patchRequest.addOperations(setAccountLock);

//         scim2RestClient.updateUser(patchRequest, userId);

//         webhookEventTestManager.stackExpectedEventPayload(
//                 "https://schemas.identity.wso2.org/events/user/event-type/userAccountUnlocked",
//                 AdminInitUserManagementEventTestExpectedEventPayloadBuilder.buildExpectedAccountUnlockedEventPayload(
//                         userId, automationContext.getContextTenant().getDomain()));
//         webhookEventTestManager.validateStackedEventPayloads();
//     }

    @Test(dependsOnMethods = "testUserProfileUpdateWithoutAnyAccountStateManagementClaims")
    public void testUserProfileUpdateWithAccountDisable() throws Exception {

        PatchOperationRequestObject patchRequest = new PatchOperationRequestObject();
        UserItemAddGroupobj addGivenName = new UserItemAddGroupobj().op(UserItemAddGroupobj.OpEnum.REPLACE);
        addGivenName.setPath("name:givenName");
        addGivenName.setValue("test-user-updated-given-name");
        patchRequest.addOperations(addGivenName);

        UserItemAddGroupobj setAccountDisable = new UserItemAddGroupobj().op(UserItemAddGroupobj.OpEnum.REPLACE);
        setAccountDisable.setPath("urn:scim:wso2:schema:accountDisabled");
        setAccountDisable.setValue(true);
        patchRequest.addOperations(setAccountDisable);

        scim2RestClient.updateUser(patchRequest, userId);

        webhookEventTestManager.stackExpectedEventPayload(
                "https://schemas.identity.wso2.org/events/user/event-type/userProfileUpdated",
                AdminInitUserManagementEventTestExpectedEventPayloadBuilder.buildExpectedUserProfileUpdatedEventPayloadWithAccountDisableClaim(
                        userId, automationContext.getContextTenant().getDomain()));
        webhookEventTestManager.stackExpectedEventPayload(
                "https://schemas.identity.wso2.org/events/user/event-type/userDisabled",
                AdminInitUserManagementEventTestExpectedEventPayloadBuilder.buildExpectedAccountDisabledEventPayload(
                        userId, automationContext.getContextTenant().getDomain()));
        webhookEventTestManager.validateStackedEventPayloads();
    }

    @Test(dependsOnMethods = "testUserProfileUpdateWithAccountDisable")
    public void testAccountEnable() throws Exception {

        PatchOperationRequestObject patchRequest = new PatchOperationRequestObject();
        UserItemAddGroupobj setAccountDisable = new UserItemAddGroupobj().op(UserItemAddGroupobj.OpEnum.REPLACE);
        setAccountDisable.setPath("urn:scim:wso2:schema:accountDisabled");
        setAccountDisable.setValue(false);
        patchRequest.addOperations(setAccountDisable);

        scim2RestClient.updateUser(patchRequest, userId);

        webhookEventTestManager.stackExpectedEventPayload(
                "https://schemas.identity.wso2.org/events/user/event-type/userEnabled",
                AdminInitUserManagementEventTestExpectedEventPayloadBuilder.buildExpectedAccountEnabledEventPayload(
                        userId, automationContext.getContextTenant().getDomain()));
        webhookEventTestManager.validateStackedEventPayloads();
    }

    @Test(dependsOnMethods = {"testCreateUser", "testUserProfileUpdateWithoutAnyAccountStateManagementClaims",
            "testUserProfileUpdateWithAccountDisable", "testAccountEnable"})
    public void testDeleteUser() throws Exception {

        scim2RestClient.deleteUser(userId);

        webhookEventTestManager.stackExpectedEventPayload(
                "https://schemas.identity.wso2.org/events/user/event-type/userDeleted",
                AdminInitUserManagementEventTestExpectedEventPayloadBuilder.buildExpectedUserDeletedEventPayload(
                        userId, automationContext.getContextTenant().getDomain()));
        webhookEventTestManager.validateStackedEventPayloads();
    }

    private void enableUserAccountDisablingFeature() throws Exception {

        PropertyReq property = new PropertyReq();
        property.setName("account.disable.handler.enable");
        property.setValue("true");

        ConnectorsPatchReq connectorPatchRequest = new ConnectorsPatchReq();
        connectorPatchRequest.setOperation(ConnectorsPatchReq.OperationEnum.UPDATE);
        connectorPatchRequest.addProperties(property);

        identityGovernanceRestClient.updateConnectors(GOVERNANCE_CATEGORY_ACCOUNT_MANAGEMENT_ID,
                ACCOUNT_DISABLE_CONNECTOR_ID, connectorPatchRequest);
    }

    private void disableUserAccountDisablingFeature() throws Exception {

        PropertyReq property = new PropertyReq();
        property.setName("account.disable.handler.enable");
        property.setValue("false");

        ConnectorsPatchReq connectorPatchRequest = new ConnectorsPatchReq();
        connectorPatchRequest.setOperation(ConnectorsPatchReq.OperationEnum.UPDATE);
        connectorPatchRequest.addProperties(property);

        identityGovernanceRestClient.updateConnectors(GOVERNANCE_CATEGORY_ACCOUNT_MANAGEMENT_ID,
                ACCOUNT_DISABLE_CONNECTOR_ID, connectorPatchRequest);
    }
}

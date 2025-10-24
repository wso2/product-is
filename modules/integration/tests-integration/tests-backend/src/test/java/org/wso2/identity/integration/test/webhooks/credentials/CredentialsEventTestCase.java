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

package org.wso2.identity.integration.test.webhooks.credentials;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;
import org.wso2.identity.integration.test.rest.api.user.common.model.Email;
import org.wso2.identity.integration.test.rest.api.user.common.model.Name;
import org.wso2.identity.integration.test.rest.api.user.common.model.PatchOperationRequestObject;
import org.wso2.identity.integration.test.rest.api.user.common.model.UserItemAddGroupobj;
import org.wso2.identity.integration.test.rest.api.user.common.model.UserObject;
import org.wso2.identity.integration.test.restclients.IdentityGovernanceRestClient;
import org.wso2.identity.integration.test.restclients.SCIM2RestClient;
import org.wso2.identity.integration.test.webhooks.usermanagement.eventpayloadbuilder.AdminInitCredentialEventTestExpectedEventPayloadBuilder;
import org.wso2.identity.integration.test.webhooks.util.WebhookEventTestManager;

import java.util.Arrays;

import static org.testng.Assert.assertNotNull;

/**
 * This class tests the credential related events in the webhook.
 */
public class CredentialsEventTestCase extends ISIntegrationTest {

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

        UserObject userInfo = new UserObject()
                .userName("test-user")
                .password("TestPassword@123")
                .name(new Name().givenName("test-user-given-name").familyName("test-user-last-name"))
                .addEmail(new Email().value("test-user@test.com"));
        userId = scim2RestClient.createUser(userInfo);

        webhookEventTestManager = new WebhookEventTestManager("/scim2/webhook", "WSO2",
                Arrays.asList("https://schemas.identity.wso2.org/events/user",
                        "https://schemas.identity.wso2.org/events/credential"),
                "CredentialsEventTestCase",
                automationContext);

    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {

        scim2RestClient.deleteUser(userId);
        scim2RestClient.closeHttpClient();
        identityGovernanceRestClient.closeHttpClient();
        webhookEventTestManager.teardown();
    }

    @Factory(dataProvider = "testExecutionContextProvider")
    public CredentialsEventTestCase(TestUserMode userMode) throws Exception {

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
    public void testUserProfileUpdateWithoutAnyAccountStateManagementClaims() throws Exception {

        assertNotNull(userId);

        PatchOperationRequestObject patchRequest = new PatchOperationRequestObject();

        UserItemAddGroupobj newPassword = new UserItemAddGroupobj().op(UserItemAddGroupobj.OpEnum.REPLACE);
        newPassword.setPath("password");
        newPassword.setValue("NewSecurePassword1234!");
        patchRequest.addOperations(newPassword);

        scim2RestClient.updateUser(patchRequest, userId);

        webhookEventTestManager.stackExpectedEventPayload(
                "https://schemas.identity.wso2.org/events/credential/event-type/credentialUpdated",
                AdminInitCredentialEventTestExpectedEventPayloadBuilder.buildExpectedUserCredentialUpdatedEventPayloadWithoutAnyOtherClaims(
                        userId, automationContext.getContextTenant().getDomain()));
        webhookEventTestManager.validateStackedEventPayloads();
    }
}

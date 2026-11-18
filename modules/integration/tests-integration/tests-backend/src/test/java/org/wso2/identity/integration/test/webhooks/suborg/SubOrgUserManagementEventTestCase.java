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

package org.wso2.identity.integration.test.webhooks.suborg;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.identity.integration.test.rest.api.user.common.model.Email;
import org.wso2.identity.integration.test.rest.api.user.common.model.Name;
import org.wso2.identity.integration.test.rest.api.user.common.model.PatchOperationRequestObject;
import org.wso2.identity.integration.test.rest.api.user.common.model.UserItemAddGroupobj;
import org.wso2.identity.integration.test.rest.api.user.common.model.UserObject;

import static org.testng.Assert.assertNotNull;
import static org.wso2.identity.integration.test.webhooks.suborg.eventpayloadbuilder.SubOrgUserManagementEventTestExpectedEventPayloadBuilder.buildExpectedUserAccountLockedEventPayload;
import static org.wso2.identity.integration.test.webhooks.suborg.eventpayloadbuilder.SubOrgUserManagementEventTestExpectedEventPayloadBuilder.buildExpectedUserAccountUnlockedEventPayload;
import static org.wso2.identity.integration.test.webhooks.suborg.eventpayloadbuilder.SubOrgUserManagementEventTestExpectedEventPayloadBuilder.buildExpectedUserCreatedEventPayload;
import static org.wso2.identity.integration.test.webhooks.suborg.eventpayloadbuilder.SubOrgUserManagementEventTestExpectedEventPayloadBuilder.buildExpectedUserDeletedEventPayload;
import static org.wso2.identity.integration.test.webhooks.suborg.eventpayloadbuilder.SubOrgUserManagementEventTestExpectedEventPayloadBuilder.buildExpectedUserProfileUpdatedEventPayload;

/**
 * Tests the user management events triggered when an administrator manages the users of the sub organizations, as
 * received by a root organization webhook subscribed to the user events of all the sub organizations.
 */
public class SubOrgUserManagementEventTestCase extends SubOrgEventTestBase {

    private static final String TEST_NAME = "SubOrgUserManagementEventTestCase";
    private static final String USER_EVENT_CHANNEL = "https://schemas.identity.wso2.org/events/user";
    private static final String USER_CREATED_EVENT_URI = USER_EVENT_CHANNEL + "/event-type/userCreated";
    private static final String USER_PROFILE_UPDATED_EVENT_URI = USER_EVENT_CHANNEL +
            "/event-type/userProfileUpdated";
    private static final String USER_ACCOUNT_LOCKED_EVENT_URI = USER_EVENT_CHANNEL + "/event-type/userAccountLocked";
    private static final String USER_ACCOUNT_UNLOCKED_EVENT_URI = USER_EVENT_CHANNEL +
            "/event-type/userAccountUnlocked";
    private static final String USER_DELETED_EVENT_URI = USER_EVENT_CHANNEL + "/event-type/userDeleted";
    private static final String ACCOUNT_LOCKED_ATTRIBUTE = "urn:scim:wso2:schema:accountLocked";
    private static final String GIVEN_NAME_ATTRIBUTE = "name:givenName";
    private static final String GIVEN_NAME = "sub-org-user-given-name";
    private static final String UPDATED_GIVEN_NAME = "sub-org-user-updated-given-name";
    private static final String LAST_NAME = "sub-org-user-last-name";

    @Factory(dataProvider = "testExecutionContextProvider")
    public SubOrgUserManagementEventTestCase(TestUserMode userMode, String[] subOrgNames) {

        super(userMode, subOrgNames, TEST_NAME);
    }

    @DataProvider(name = "testExecutionContextProvider")
    public static Object[][] getTestExecutionContext() {

        return new Object[][]{
                {TestUserMode.SUPER_TENANT_ADMIN, new String[]{"webhook_user_sub_org_1", "webhook_user_sub_org_2"}},
                {TestUserMode.TENANT_ADMIN, new String[]{"webhook_user_t_sub_org_1", "webhook_user_t_sub_org_2"}}
        };
    }

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        setUpSubOrgs();
        subscribeWebhook(USER_EVENT_CHANNEL);
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {

        tearDownSubOrgs();
    }

    @Test(dataProvider = "subOrgProvider", description = "Administrator creates a user in the sub organization.")
    public void testSubOrgUserCreation(SubOrg subOrg) throws Exception {

        subOrg.username = TEST_NAME + "-user-" + (subOrgs.indexOf(subOrg) + 1);
        subOrg.email = subOrg.username + "@test.com";
        subOrg.userId = scim2RestClient.createSubOrgUser(new UserObject()
                .userName(subOrg.username)
                .password(PASSWORD)
                .name(new Name().givenName(GIVEN_NAME).familyName(LAST_NAME))
                .addEmail(new Email().value(subOrg.email)), subOrg.switchedToken);
        assertNotNull(subOrg.userId, "Failed to create user in sub organization: " + subOrg.name);

        webhookEventTestManager.stackExpectedEventPayload(USER_CREATED_EVENT_URI,
                buildExpectedUserCreatedEventPayload(subOrg.userId, subOrg.username, subOrg.email, GIVEN_NAME,
                        LAST_NAME, rootTenantDomain, subOrg.toOrganizationObject()));
        webhookEventTestManager.validateStackedEventPayloads();
    }

    @Test(dataProvider = "subOrgProvider", dependsOnMethods = "testSubOrgUserCreation",
            description = "Administrator updates the profile of a sub organization user.")
    public void testSubOrgUserProfileUpdate(SubOrg subOrg) throws Exception {

        updateSubOrgUserAttribute(subOrg, GIVEN_NAME_ATTRIBUTE, UPDATED_GIVEN_NAME);

        webhookEventTestManager.stackExpectedEventPayload(USER_PROFILE_UPDATED_EVENT_URI,
                buildExpectedUserProfileUpdatedEventPayload(subOrg.userId, UPDATED_GIVEN_NAME, rootTenantDomain,
                        subOrg.toOrganizationObject()));
        webhookEventTestManager.validateStackedEventPayloads();
    }

    @Test(dataProvider = "subOrgProvider", dependsOnMethods = "testSubOrgUserProfileUpdate",
            description = "Administrator locks the account of a sub organization user.")
    public void testSubOrgUserAccountLock(SubOrg subOrg) throws Exception {

        updateSubOrgUserAttribute(subOrg, ACCOUNT_LOCKED_ATTRIBUTE, true);

        webhookEventTestManager.stackExpectedEventPayload(USER_ACCOUNT_LOCKED_EVENT_URI,
                buildExpectedUserAccountLockedEventPayload(subOrg.userId, subOrg.email, rootTenantDomain,
                        subOrg.toOrganizationObject()));
        webhookEventTestManager.validateStackedEventPayloads();
    }

    @Test(dataProvider = "subOrgProvider", dependsOnMethods = "testSubOrgUserAccountLock",
            description = "Administrator unlocks the account of a sub organization user.")
    public void testSubOrgUserAccountUnlock(SubOrg subOrg) throws Exception {

        updateSubOrgUserAttribute(subOrg, ACCOUNT_LOCKED_ATTRIBUTE, false);

        webhookEventTestManager.stackExpectedEventPayload(USER_ACCOUNT_UNLOCKED_EVENT_URI,
                buildExpectedUserAccountUnlockedEventPayload(subOrg.userId, subOrg.email, rootTenantDomain,
                        subOrg.toOrganizationObject()));
        webhookEventTestManager.validateStackedEventPayloads();
    }

    @Test(dataProvider = "subOrgProvider", dependsOnMethods = "testSubOrgUserAccountUnlock",
            description = "Administrator deletes a sub organization user.")
    public void testSubOrgUserDeletion(SubOrg subOrg) throws Exception {

        String userId = subOrg.userId;
        scim2RestClient.deleteSubOrgUser(userId, subOrg.switchedToken);
        // The user is deleted, hence it is not deleted again when cleaning up the test data.
        subOrg.userId = null;

        webhookEventTestManager.stackExpectedEventPayload(USER_DELETED_EVENT_URI,
                buildExpectedUserDeletedEventPayload(userId, subOrg.username, rootTenantDomain,
                        subOrg.toOrganizationObject()));
        webhookEventTestManager.validateStackedEventPayloads();
    }

    private void updateSubOrgUserAttribute(SubOrg subOrg, String attributePath, Object value) throws Exception {

        UserItemAddGroupobj replaceOperation = new UserItemAddGroupobj().op(UserItemAddGroupobj.OpEnum.REPLACE);
        replaceOperation.setPath(attributePath);
        replaceOperation.setValue(value);
        PatchOperationRequestObject patchRequest = new PatchOperationRequestObject();
        patchRequest.addOperations(replaceOperation);
        scim2RestClient.updateSubOrgUser(patchRequest, subOrg.userId, subOrg.switchedToken);
    }
}

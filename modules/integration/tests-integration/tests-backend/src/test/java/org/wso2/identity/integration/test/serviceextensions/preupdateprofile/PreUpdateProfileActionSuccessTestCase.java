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

package org.wso2.identity.integration.test.serviceextensions.preupdateprofile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.testng.annotations.*;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.identity.integration.test.serviceextensions.common.ActionsBaseTestCase;
import org.wso2.identity.integration.test.serviceextensions.mockservices.ServiceExtensionMockServer;
import org.wso2.identity.integration.test.serviceextensions.model.*;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationResponseModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.OpenIDConnectConfiguration;
import org.wso2.identity.integration.test.rest.api.user.common.model.*;
import org.wso2.identity.integration.test.restclients.SCIM2RestClient;
import org.wso2.identity.integration.test.restclients.UsersRestClient;
import org.wso2.identity.integration.test.utils.FileUtils;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;


/**
 * Integration test class for testing the pre update profile action execution.
 * This test case extends {@link ActionsBaseTestCase} and focuses on success scenarios related
 * to profile update flows.
 */
public class PreUpdateProfileActionSuccessTestCase extends PreUpdateProfileActionBaseTestCase {

    private final String tenantId;
    private final TestUserMode userMode;

    private SCIM2RestClient scim2RestClient;
    private UsersRestClient usersRestClient;

    private String clientId;
    private String clientSecret;
    private String actionId;
    private String userId;
    private String currentClaimValue;
    private ApplicationResponseModel application;
    private ServiceExtensionMockServer serviceExtensionMockServer;

    @Factory(dataProvider = "testExecutionContextProvider")
    public PreUpdateProfileActionSuccessTestCase(TestUserMode testUserMode) {

        userMode = testUserMode;
        this.tenantId = testUserMode == TestUserMode.SUPER_TENANT_USER ? "-1234" : "1";
    }

    @DataProvider(name = "testExecutionContextProvider")
    public static Object[][] getTestExecutionContext() {

        return new Object[][]{
                {TestUserMode.SUPER_TENANT_USER},
                {TestUserMode.TENANT_USER}
        };
    }

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.init(userMode);

        scim2RestClient = new SCIM2RestClient(serverURL, tenantInfo);
        usersRestClient = new UsersRestClient(serverURL, tenantInfo);

        application = addApplicationWithGrantType(CLIENT_CREDENTIALS_GRANT_TYPE);
        OpenIDConnectConfiguration oidcConfig = getOIDCInboundDetailsOfApplication(application.getId());
        clientId = oidcConfig.getClientId();
        clientSecret = oidcConfig.getClientSecret();

        UserObject userInfo = new UserObject()
                .userName(TEST_USER1_USERNAME)
                .password(TEST_USER_PASSWORD)
                .name(new Name().givenName(TEST_USER_GIVEN_NAME).familyName(TEST_USER_LASTNAME))
                .addEmail(new Email().value(TEST_USER_EMAIL));
        userId = scim2RestClient.createUser(userInfo);
        currentClaimValue = TEST_USER_GIVEN_NAME;
        actionId = createPreUpdateProfileAction(ACTION_NAME, ACTION_DESCRIPTION);

        serviceExtensionMockServer = new ServiceExtensionMockServer();
        serviceExtensionMockServer.startServer();
        serviceExtensionMockServer.setupStub(MOCK_SERVER_ENDPOINT_RESOURCE_PATH,
                "Basic " + getBase64EncodedString(MOCK_SERVER_AUTH_BASIC_USERNAME,
                        MOCK_SERVER_AUTH_BASIC_PASSWORD),
                FileUtils.readFileInClassPathAsString("actions/response/pre-update-profile-response.json"));
    }

    @BeforeMethod
    public void setUp() throws Exception {

        serviceExtensionMockServer.resetRequests();
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {

        deleteAction(PRE_UPDATE_PROFILE_API_PATH, actionId);
        deleteApp(application.getId());
        scim2RestClient.deleteUser(userId);
        restClient.closeHttpClient();
        identityGovernanceRestClient.closeHttpClient();
        scim2RestClient.closeHttpClient();
        actionsRestClient.closeHttpClient();
        client.close();
        serviceExtensionMockServer.stopServer();
        serviceExtensionMockServer = null;
    }

    @Test(description = "Verify the profile update in self service portal with pre update profile action for add operation")
    public void testUserUpdateProfileWithAddOperation() throws Exception {

        UserItemAddGroupobj userPatchOp = new UserItemAddGroupobj().op(UserItemAddGroupobj.OpEnum.ADD);
        userPatchOp.setPath("nickName");
        userPatchOp.setValue(TEST_USER_CLAIM_VALUE);
        PatchOperationRequestObject patchUserInfo = new PatchOperationRequestObject()
                .addOperations(userPatchOp);
        org.json.simple.JSONObject response = scim2RestClient.updateUserMe(patchUserInfo,
                TEST_USER1_USERNAME + "@" + tenantInfo.getDomain(), TEST_USER_PASSWORD);

        assertNotNull(response);
        assertActionRequestPayload(UserItemAddGroupobj.OpEnum.ADD, userId, TEST_USER_CLAIM_VALUE, TEST_USER_CLAIM_VALUE,
                PreUpdateProfileEvent.FlowInitiatorType.USER, PreUpdateProfileEvent.Action.UPDATE);
        currentClaimValue = TEST_USER_CLAIM_VALUE;
    }

    @Test(dependsOnMethods = "testUserUpdateProfileWithAddOperation" ,
            description = "Verify the profile update in self service portal with pre update profile action for replace operation")
    public void testUserUpdateProfileWithReplaceOperation() throws Exception {

        UserItemAddGroupobj userPatchOp = new UserItemAddGroupobj().op(UserItemAddGroupobj.OpEnum.REPLACE);
        userPatchOp.setPath("nickName");
        userPatchOp.setValue(TEST_USER_UPDATED_CLAIM_VALUE);
        PatchOperationRequestObject patchUserInfo = new PatchOperationRequestObject()
                .addOperations(userPatchOp);
        org.json.simple.JSONObject response = scim2RestClient.updateUserMe(patchUserInfo,
                TEST_USER1_USERNAME + "@" + tenantInfo.getDomain(), TEST_USER_PASSWORD);

        assertNotNull(response);
        assertActionRequestPayload(UserItemAddGroupobj.OpEnum.REPLACE, userId, currentClaimValue, TEST_USER_UPDATED_CLAIM_VALUE,
                PreUpdateProfileEvent.FlowInitiatorType.USER, PreUpdateProfileEvent.Action.UPDATE);
        currentClaimValue = TEST_USER_UPDATED_CLAIM_VALUE;
    }

    @Test(dependsOnMethods = "testUserUpdateProfileWithReplaceOperation" ,
            description = "Verify the profile update in self service portal with pre update profile action for remove operation")
    public void testUserUpdateProfileWithRemoveOperation() throws Exception {

        UserItemAddGroupobj userPatchOp = new UserItemAddGroupobj().op(UserItemAddGroupobj.OpEnum.REMOVE);
        userPatchOp.setPath("nickName");
        PatchOperationRequestObject patchUserInfo = new PatchOperationRequestObject()
                .addOperations(userPatchOp);
        org.json.simple.JSONObject response = scim2RestClient.updateUserMe(patchUserInfo,
                TEST_USER1_USERNAME + "@" + tenantInfo.getDomain(), TEST_USER_PASSWORD);

        assertNotNull(response);
        assertActionRequestPayload(UserItemAddGroupobj.OpEnum.REMOVE, userId, currentClaimValue, "",
                PreUpdateProfileEvent.FlowInitiatorType.USER, PreUpdateProfileEvent.Action.UPDATE);
        currentClaimValue = TEST_USER_CLAIM_VALUE;
    }

    @Test(dependsOnMethods = "testUserUpdateProfileWithRemoveOperation" ,
            description = "Verify the admin update profile with pre update profile action for add operation")
    public void testAdminUpdateProfileWithAddOperation() throws Exception {

        UserItemAddGroupobj userPatchOp = new UserItemAddGroupobj().op(UserItemAddGroupobj.OpEnum.ADD);
        userPatchOp.setPath("nickName");
        userPatchOp.setValue(TEST_USER_CLAIM_VALUE);
        PatchOperationRequestObject patchUserInfo = new PatchOperationRequestObject()
                .addOperations(userPatchOp);
        scim2RestClient.updateUser(patchUserInfo, userId);

        assertActionRequestPayload(UserItemAddGroupobj.OpEnum.ADD, userId, currentClaimValue, TEST_USER_CLAIM_VALUE,
                PreUpdateProfileEvent.FlowInitiatorType.ADMIN, PreUpdateProfileEvent.Action.UPDATE);
        currentClaimValue = TEST_USER_CLAIM_VALUE;
    }

    @Test(dependsOnMethods = "testAdminUpdateProfileWithAddOperation" ,
            description = "Verify the admin update profile with pre update profile action for replace operation")
    public void testAdminUpdateProfileWithReplaceOperation() throws Exception {

        UserItemAddGroupobj userPatchOp = new UserItemAddGroupobj().op(UserItemAddGroupobj.OpEnum.REPLACE);
        userPatchOp.setPath("nickName");
        userPatchOp.setValue(TEST_USER_UPDATED_CLAIM_VALUE);
        PatchOperationRequestObject patchUserInfo = new PatchOperationRequestObject()
                .addOperations(userPatchOp);
        scim2RestClient.updateUser(patchUserInfo, userId);

        assertActionRequestPayload(UserItemAddGroupobj.OpEnum.REPLACE, userId, currentClaimValue, TEST_USER_UPDATED_CLAIM_VALUE,
                PreUpdateProfileEvent.FlowInitiatorType.ADMIN, PreUpdateProfileEvent.Action.UPDATE);
        currentClaimValue = TEST_USER_UPDATED_CLAIM_VALUE;
    }

    @Test(dependsOnMethods = "testAdminUpdateProfileWithReplaceOperation" ,
            description = "Verify the admin update profile with pre update profile action for remove operation")
    public void testAdminUpdateProfileWithRemoveOperation() throws Exception {

        UserItemAddGroupobj userPatchOp = new UserItemAddGroupobj().op(UserItemAddGroupobj.OpEnum.REMOVE);
        userPatchOp.setPath("nickName");
        //userPatchOp.setValue(TEST_USER_CLAIM_VALUE);
        PatchOperationRequestObject patchUserInfo = new PatchOperationRequestObject()
                .addOperations(userPatchOp);
        scim2RestClient.updateUser(patchUserInfo, userId);

        assertActionRequestPayload(UserItemAddGroupobj.OpEnum.REMOVE, userId, currentClaimValue, "",
                PreUpdateProfileEvent.FlowInitiatorType.ADMIN, PreUpdateProfileEvent.Action.UPDATE);
        currentClaimValue = TEST_USER_CLAIM_VALUE;
    }

    @Test(dependsOnMethods = "testAdminUpdateProfileWithRemoveOperation",
            description = "Verify the application update profile with pre update profile action for add operation")
    public void testApplicationUpdateProfileWithAddOperation() throws Exception {

        String token = getTokenWithClientCredentialsGrant(application.getId(), clientId, clientSecret);
        UserItemAddGroupobj userPatchOp = new UserItemAddGroupobj().op(UserItemAddGroupobj.OpEnum.ADD);
        userPatchOp.setPath("nickName");
        userPatchOp.setValue(TEST_USER_CLAIM_VALUE);
        PatchOperationRequestObject patchUserInfo = new PatchOperationRequestObject()
                .addOperations(userPatchOp);
        org.json.simple.JSONObject response = scim2RestClient.updateUserWithBearerToken(patchUserInfo, userId, token);

        assertNotNull(response);
        assertActionRequestPayload(UserItemAddGroupobj.OpEnum.ADD, userId, currentClaimValue, TEST_USER_CLAIM_VALUE,
                PreUpdateProfileEvent.FlowInitiatorType.APPLICATION, PreUpdateProfileEvent.Action.UPDATE);
        currentClaimValue = TEST_USER_CLAIM_VALUE;
    }

    @Test(dependsOnMethods = "testApplicationUpdateProfileWithAddOperation",
            description = "Verify the application update profile with pre update profile action for replace operation")
    public void testApplicationUpdateProfileWithReplaceOperation() throws Exception {

        String token = getTokenWithClientCredentialsGrant(application.getId(), clientId, clientSecret);
        UserItemAddGroupobj userPatchOp = new UserItemAddGroupobj().op(UserItemAddGroupobj.OpEnum.ADD);
        userPatchOp.setPath("nickName");
        userPatchOp.setValue(TEST_USER_UPDATED_CLAIM_VALUE);
        PatchOperationRequestObject patchUserInfo = new PatchOperationRequestObject()
                .addOperations(userPatchOp);
        org.json.simple.JSONObject response = scim2RestClient.updateUserWithBearerToken(patchUserInfo, userId, token);

        assertNotNull(response);
        assertActionRequestPayload(UserItemAddGroupobj.OpEnum.REPLACE, userId, currentClaimValue, TEST_USER_UPDATED_CLAIM_VALUE,
                PreUpdateProfileEvent.FlowInitiatorType.APPLICATION, PreUpdateProfileEvent.Action.UPDATE);
        currentClaimValue = TEST_USER_UPDATED_CLAIM_VALUE;
    }

    @Test(dependsOnMethods = "testApplicationUpdateProfileWithReplaceOperation",
            description = "Verify the application update profile with pre update profile action for remove operation")
    public void testApplicationUpdateProfileWithRemoveOperation() throws Exception {

        String token = getTokenWithClientCredentialsGrant(application.getId(), clientId, clientSecret);
        UserItemAddGroupobj userPatchOp = new UserItemAddGroupobj().op(UserItemAddGroupobj.OpEnum.REMOVE);
        userPatchOp.setPath("nickName");
        PatchOperationRequestObject patchUserInfo = new PatchOperationRequestObject()
                .addOperations(userPatchOp);
        org.json.simple.JSONObject response = scim2RestClient.updateUserWithBearerToken(patchUserInfo, userId, token);

        assertNotNull(response);
        assertActionRequestPayload(UserItemAddGroupobj.OpEnum.REMOVE, userId, currentClaimValue, "",
                PreUpdateProfileEvent.FlowInitiatorType.APPLICATION, PreUpdateProfileEvent.Action.UPDATE);
    }

    private void assertActionRequestPayload(UserItemAddGroupobj.OpEnum operation,String userId, String currentClaimValue,
                                            String updateClaimValue, PreUpdateProfileEvent.FlowInitiatorType initiatorType,
                                            PreUpdateProfileEvent.Action action) throws JsonProcessingException {

        String actualRequestPayload = serviceExtensionMockServer.getReceivedRequestPayload(MOCK_SERVER_ENDPOINT_RESOURCE_PATH);
        PreUpdateProfileActionRequest actionRequest = new ObjectMapper()
                .readValue(actualRequestPayload, PreUpdateProfileActionRequest.class);

        assertEquals(actionRequest.getActionType(), ActionType.PRE_UPDATE_PROFILE);
        assertEquals(actionRequest.getEvent().getTenant().getName(), tenantInfo.getDomain());
        assertEquals(actionRequest.getEvent().getTenant().getId(), tenantId);
        assertEquals(actionRequest.getEvent().getUserStore().getName(), PRIMARY_USER_STORE_NAME);
        assertEquals(actionRequest.getEvent().getUserStore().getId(), PRIMARY_USER_STORE_ID);
        assertEquals(actionRequest.getEvent().getInitiatorType(), initiatorType);
        assertEquals(actionRequest.getEvent().getAction(), action);

        ProfileUpdatingUser user = actionRequest.getEvent().getProfileUpdatingUser();

        assertEquals(user.getId(), userId);
        if (operation == UserItemAddGroupobj.OpEnum.ADD) {
            assertNull(user.getClaims());
        } else if (operation == UserItemAddGroupobj.OpEnum.REPLACE) {
            assertEquals(user.getClaims()[0].getUri(), NICK_NAME_CLAIM_URI);
            assertEquals(user.getClaims()[0].getUpdatingValue(), updateClaimValue);
            assertEquals(user.getClaims()[0].getValue(), currentClaimValue);
        } else if (operation == UserItemAddGroupobj.OpEnum.REMOVE) {
            assertEquals(user.getClaims()[0].getUri(), NICK_NAME_CLAIM_URI);
            assertEquals(user.getClaims()[0].getValue(), currentClaimValue);
        }

        PreUpdateProfileRequest request = actionRequest.getEvent().getRequest();

        assertEquals(request.getClaims()[0].getUri(), NICK_NAME_CLAIM_URI);
        assertNull(request.getClaims()[0].getUpdatingValue());
        assertEquals(request.getClaims()[0].getValue(), updateClaimValue);
    }

}

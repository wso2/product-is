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
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationResponseModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.OpenIDConnectConfiguration;
import org.wso2.identity.integration.test.rest.api.user.common.model.*;
import org.wso2.identity.integration.test.restclients.SCIM2RestClient;
import org.wso2.identity.integration.test.restclients.UsersRestClient;
import org.wso2.identity.integration.test.serviceextensions.dataprovider.model.ActionResponse;
import org.wso2.identity.integration.test.serviceextensions.dataprovider.model.ExpectedProfileUpdateResponse;
import org.wso2.identity.integration.test.serviceextensions.mockservices.ServiceExtensionMockServer;
import org.wso2.identity.integration.test.serviceextensions.model.*;
import org.wso2.identity.integration.test.util.Utils;
import org.wso2.identity.integration.test.utils.FileUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URISyntaxException;

import static org.testng.Assert.*;

public class PreUpdateProfileActionFailureTestCase extends PreUpdateProfileActionBaseTestCase {

    private final String tenantId;
    private final TestUserMode userMode;

    private SCIM2RestClient scim2RestClient;
    private UsersRestClient usersRestClient;

    private String clientId;
    private String clientSecret;
    private String actionId;
    private String userId;
    private ApplicationResponseModel application;
    private ServiceExtensionMockServer serviceExtensionMockServer;
    private final ActionResponse actionResponse;
    private final ExpectedProfileUpdateResponse expectedProfileUpdateResponse;

    @Factory(dataProvider = "testExecutionContextProvider")
    public PreUpdateProfileActionFailureTestCase(TestUserMode testUserMode, ActionResponse actionResponse,
                                                 ExpectedProfileUpdateResponse expectedProfileUpdateResponse) {

        this.userMode = testUserMode;
        this.tenantId = testUserMode == TestUserMode.SUPER_TENANT_USER ? "-1234" : "1";
        this.actionResponse = actionResponse;
        this.expectedProfileUpdateResponse = expectedProfileUpdateResponse;
    }

    @DataProvider(name = "testExecutionContextProvider")
    public static Object[][] getTestExecutionContext() throws IOException, URISyntaxException {

        return new Object[][]{
                {TestUserMode.SUPER_TENANT_USER, new ActionResponse(HttpServletResponse.SC_OK,
                        FileUtils.readFileInClassPathAsString("actions/response/failure-response.json")),
                        new ExpectedProfileUpdateResponse(HttpServletResponse.SC_BAD_REQUEST,
                                "Some failure reason",
                                "Some description")},
                {TestUserMode.TENANT_USER, new ActionResponse(HttpServletResponse.SC_OK,
                        FileUtils.readFileInClassPathAsString("actions/response/failure-response.json")),
                        new ExpectedProfileUpdateResponse(HttpServletResponse.SC_BAD_REQUEST,
                                "Some failure reason",
                                "Some description")},
                {TestUserMode.SUPER_TENANT_USER, new ActionResponse(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        FileUtils.readFileInClassPathAsString("actions/response/error-response.json")),
                        new ExpectedProfileUpdateResponse(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                                "Invalid value",
                                "Error while updating attributes of user")},
                {TestUserMode.TENANT_USER, new ActionResponse(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        FileUtils.readFileInClassPathAsString("actions/response/error-response.json")),
                        new ExpectedProfileUpdateResponse(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                                "Invalid value",
                                "Error while updating attributes of user")},
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

        actionId = createPreUpdateProfileAction(ACTION_NAME, ACTION_DESCRIPTION);

        serviceExtensionMockServer = new ServiceExtensionMockServer();
        serviceExtensionMockServer.startServer();
        serviceExtensionMockServer.setupStub(MOCK_SERVER_ENDPOINT_RESOURCE_PATH,
                "Basic " + getBase64EncodedString(MOCK_SERVER_AUTH_BASIC_USERNAME,
                        MOCK_SERVER_AUTH_BASIC_PASSWORD),
                actionResponse.getResponseBody(), actionResponse.getStatusCode());
    }

    @BeforeMethod
    public void setUp() throws Exception {

        serviceExtensionMockServer.resetRequests();
        Utils.getMailServer().purgeEmailFromAllMailboxes();
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
        Utils.getMailServer().purgeEmailFromAllMailboxes();
        serviceExtensionMockServer.stopServer();
        serviceExtensionMockServer = null;
    }

    @Test(description = "Verify the profile update in self service portal with pre update profile action")
    public void testUserUpdateProfile() throws Exception {

        UserItemAddGroupobj userPatchOp = new UserItemAddGroupobj().op(UserItemAddGroupobj.OpEnum.REPLACE);
        userPatchOp.setPath("name.givenName");
        userPatchOp.setValue(TEST_USER_UPDATED_CLAIM_VALUE);
        PatchOperationRequestObject patchUserInfo = new PatchOperationRequestObject()
                .addOperations(userPatchOp);
        org.json.simple.JSONObject response = scim2RestClient.updateUserMe(patchUserInfo,
                TEST_USER1_USERNAME + "@" + tenantInfo.getDomain(), TEST_USER_PASSWORD);

        assertNotNull(response);
        String status = response.get("status").toString();
        assertEquals(status, String.valueOf(expectedProfileUpdateResponse.getStatusCode()));
        if (status.equals(String.valueOf(HttpServletResponse.SC_OK))) {
            assertEquals(response.get("scimType"), String.valueOf(expectedProfileUpdateResponse.getErrorMessage()));
        }
        assertTrue(response.get("detail").toString().contains(expectedProfileUpdateResponse.getErrorDetail()));
        assertActionRequestPayload(userId, TEST_USER_GIVEN_NAME, TEST_USER_UPDATED_CLAIM_VALUE, PreUpdateProfileEvent.FlowInitiatorType.USER,
                PreUpdateProfileEvent.Action.UPDATE);
    }

    @Test(dependsOnMethods = "testUserUpdateProfile" ,
            description = "Verify the admin update profile with pre update profile action")
    public void testAdminUpdateProfile() throws Exception {

        UserItemAddGroupobj userPatchOp = new UserItemAddGroupobj().op(UserItemAddGroupobj.OpEnum.REPLACE);
        userPatchOp.setPath("name.givenName");
        userPatchOp.setValue(TEST_USER_UPDATED_CLAIM_VALUE);
        PatchOperationRequestObject patchUserInfo = new PatchOperationRequestObject()
                .addOperations(userPatchOp);
        org.json.simple.JSONObject response = scim2RestClient.updateUserAndReturnResponse(patchUserInfo, userId);

        assertNotNull(response);
        String status = response.get("status").toString();
        assertEquals(status, String.valueOf(expectedProfileUpdateResponse.getStatusCode()));
        if (status.equals(String.valueOf(HttpServletResponse.SC_OK))) {
            assertEquals(response.get("scimType"), String.valueOf(expectedProfileUpdateResponse.getErrorMessage()));
        }
        assertTrue(response.get("detail").toString().contains(expectedProfileUpdateResponse.getErrorDetail()));
        assertActionRequestPayload(userId, TEST_USER_GIVEN_NAME, TEST_USER_UPDATED_CLAIM_VALUE, PreUpdateProfileEvent.FlowInitiatorType.ADMIN,
                PreUpdateProfileEvent.Action.UPDATE);
    }

    @Test(dependsOnMethods = "testAdminUpdateProfile",
            description = "Verify the application update profile with pre update profile action")
    public void testApplicationUpdateProfile() throws Exception {

        String token = getTokenWithClientCredentialsGrant(application.getId(), clientId, clientSecret);
        UserItemAddGroupobj userPatchOp = new UserItemAddGroupobj().op(UserItemAddGroupobj.OpEnum.REPLACE);
        userPatchOp.setPath("name.givenName");
        userPatchOp.setValue(TEST_USER_UPDATED_CLAIM_VALUE);
        PatchOperationRequestObject patchUserInfo = new PatchOperationRequestObject()
                .addOperations(userPatchOp);
        org.json.simple.JSONObject response = scim2RestClient.updateUserWithBearerToken(patchUserInfo, userId, token);

        assertNotNull(response);
        String status = response.get("status").toString();
        assertEquals(status, String.valueOf(expectedProfileUpdateResponse.getStatusCode()));
        if (status.equals(String.valueOf(HttpServletResponse.SC_OK))) {
            assertEquals(response.get("scimType"), String.valueOf(expectedProfileUpdateResponse.getErrorMessage()));
        }
        assertTrue(response.get("detail").toString().contains(expectedProfileUpdateResponse.getErrorDetail()));
        assertActionRequestPayload(userId, TEST_USER_GIVEN_NAME, TEST_USER_UPDATED_CLAIM_VALUE, PreUpdateProfileEvent.FlowInitiatorType.APPLICATION,
                PreUpdateProfileEvent.Action.UPDATE);
    }

    private void assertActionRequestPayload(String userId, String currentClaimValue, String updateClaimValue,
                                            PreUpdateProfileEvent.FlowInitiatorType initiatorType,
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
        assertEquals(user.getClaims()[0].getUri(), NICK_NAME_CLAIM_URI);
        assertEquals(user.getClaims()[0].getUpdatingValue(), updateClaimValue);
        assertEquals(user.getClaims()[0].getValue(), currentClaimValue);

        PreUpdateProfileRequest request = actionRequest.getEvent().getRequest();

        assertEquals(request.getClaims()[0].getUri(), NICK_NAME_CLAIM_URI);
        assertNull(request.getClaims()[0].getUpdatingValue());
        assertEquals(request.getClaims()[0].getValue(), updateClaimValue);
    }
}

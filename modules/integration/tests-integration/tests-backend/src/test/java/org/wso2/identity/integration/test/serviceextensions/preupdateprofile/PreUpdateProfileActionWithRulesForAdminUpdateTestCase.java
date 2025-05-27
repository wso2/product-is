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
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.identity.integration.test.rest.api.server.action.management.v1.common.model.ANDRule;
import org.wso2.identity.integration.test.rest.api.server.action.management.v1.common.model.ActionUpdateModel;
import org.wso2.identity.integration.test.rest.api.server.action.management.v1.common.model.Expression;
import org.wso2.identity.integration.test.rest.api.server.action.management.v1.common.model.ORRule;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationResponseModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.OpenIDConnectConfiguration;
import org.wso2.identity.integration.test.rest.api.user.common.model.Email;
import org.wso2.identity.integration.test.rest.api.user.common.model.Name;
import org.wso2.identity.integration.test.rest.api.user.common.model.PatchOperationRequestObject;
import org.wso2.identity.integration.test.rest.api.user.common.model.UserItemAddGroupobj;
import org.wso2.identity.integration.test.rest.api.user.common.model.UserObject;
import org.wso2.identity.integration.test.restclients.SCIM2RestClient;
import org.wso2.identity.integration.test.serviceextensions.mockservices.ServiceExtensionMockServer;
import org.wso2.identity.integration.test.serviceextensions.model.PreUpdateProfileActionRequest;
import org.wso2.identity.integration.test.serviceextensions.model.ActionType;
import org.wso2.identity.integration.test.serviceextensions.model.PreUpdateProfileEvent;
import org.wso2.identity.integration.test.serviceextensions.model.PreUpdateProfileRequest;
import org.wso2.identity.integration.test.serviceextensions.model.ProfileUpdatingUser;
import org.wso2.identity.integration.test.utils.FileUtils;

import java.io.IOException;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

/**
 * Test class to cover the scenarios of pre update profile action execution with rules for admin profile update.
 */
public class PreUpdateProfileActionWithRulesForAdminUpdateTestCase extends PreUpdateProfileActionBaseTestCase {

    private final String tenantId;
    private final TestUserMode userMode;

    private SCIM2RestClient scim2RestClient;

    private String clientId;
    private String clientSecret;
    private String actionId;
    private String userId;
    private String currentClaimValue = TEST_USER_CLAIM_VALUE;
    private ApplicationResponseModel application;
    private ServiceExtensionMockServer serviceExtensionMockServer;

    @Factory(dataProvider = "testExecutionContextProvider")
    public PreUpdateProfileActionWithRulesForAdminUpdateTestCase(TestUserMode testUserMode) {

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
        scim2RestClient.closeHttpClient();
        actionsRestClient.closeHttpClient();
        client.close();
        serviceExtensionMockServer.stopServer();
        serviceExtensionMockServer = null;
    }

    @DataProvider(name = "rulesProvider")
    private Object[][] rulesProvider() {

        return new Object[][]{
                {new ORRule()
                        .condition(ORRule.ConditionEnum.OR)
                        .addRulesItem(new ANDRule().condition(ANDRule.ConditionEnum.AND)
                        .addExpressionsItem(new Expression().field("flow").operator("equals")
                                .value("adminInitiatedProfileUpdate"))), TEST_USER_CLAIM_VALUE, UserItemAddGroupobj.OpEnum.ADD, true},
                {new ORRule()
                        .condition(ORRule.ConditionEnum.OR)
                        .addRulesItem(new ANDRule().condition(ANDRule.ConditionEnum.AND)
                        .addExpressionsItem(new Expression().field("flow").operator("equals")
                                .value("adminInitiatedProfileUpdate"))), TEST_USER_UPDATED_CLAIM_VALUE, UserItemAddGroupobj.OpEnum.REPLACE, true},
                {new ORRule()
                        .condition(ORRule.ConditionEnum.OR)
                        .addRulesItem(new ANDRule().condition(ANDRule.ConditionEnum.AND)
                        .addExpressionsItem(new Expression().field("flow").operator("equals")
                                .value("adminInitiatedProfileUpdate"))), EMPTY_STRING, UserItemAddGroupobj.OpEnum.REMOVE, true},
                {new ORRule()
                        .condition(ORRule.ConditionEnum.OR)
                        .addRulesItem(new ANDRule().condition(ANDRule.ConditionEnum.AND)
                        .addExpressionsItem(new Expression().field("claim").operator("equals")
                                .value(NICK_NAME_CLAIM_URI))), TEST_USER_CLAIM_VALUE, UserItemAddGroupobj.OpEnum.ADD, true},
                {new ORRule()
                        .condition(ORRule.ConditionEnum.OR)
                        .addRulesItem(new ANDRule().condition(ANDRule.ConditionEnum.AND)
                        .addExpressionsItem(new Expression().field("claim").operator("equals")
                                .value(NICK_NAME_CLAIM_URI))), TEST_USER_UPDATED_CLAIM_VALUE, UserItemAddGroupobj.OpEnum.REPLACE, true},
                {new ORRule()
                        .condition(ORRule.ConditionEnum.OR)
                        .addRulesItem(new ANDRule().condition(ANDRule.ConditionEnum.AND)
                        .addExpressionsItem(new Expression().field("claim").operator("equals")
                                .value(NICK_NAME_CLAIM_URI))), EMPTY_STRING, UserItemAddGroupobj.OpEnum.REMOVE, true},
                {new ORRule()
                        .condition(ORRule.ConditionEnum.OR)
                        .addRulesItem(new ANDRule().condition(ANDRule.ConditionEnum.AND)
                        .addExpressionsItem(new Expression().field("flow").operator("equals")
                                .value("adminInitiatedProfileUpdate"))
                        .addExpressionsItem(new Expression().field("claim").operator("equals")
                                .value(NICK_NAME_CLAIM_URI))), TEST_USER_CLAIM_VALUE, UserItemAddGroupobj.OpEnum.ADD, true},
                {new ORRule()
                        .condition(ORRule.ConditionEnum.OR)
                        .addRulesItem(new ANDRule().condition(ANDRule.ConditionEnum.AND)
                        .addExpressionsItem(new Expression().field("flow").operator("equals")
                                .value("adminInitiatedProfileUpdate"))
                        .addExpressionsItem(new Expression().field("claim").operator("equals")
                                .value(NICK_NAME_CLAIM_URI))), TEST_USER_UPDATED_CLAIM_VALUE, UserItemAddGroupobj.OpEnum.REPLACE, true},
                {new ORRule()
                        .condition(ORRule.ConditionEnum.OR)
                        .addRulesItem(new ANDRule().condition(ANDRule.ConditionEnum.AND)
                        .addExpressionsItem(new Expression().field("flow").operator("equals")
                                .value("adminInitiatedProfileUpdate"))
                        .addExpressionsItem(new Expression().field("claim").operator("equals")
                                .value(NICK_NAME_CLAIM_URI))), EMPTY_STRING, UserItemAddGroupobj.OpEnum.REMOVE, true},
                {new ORRule()
                        .condition(ORRule.ConditionEnum.OR)
                        .addRulesItem(new ANDRule().condition(ANDRule.ConditionEnum.AND)
                                .addExpressionsItem(new Expression().field("flow").operator("equals")
                                        .value("adminInitiatedProfileUpdate")))
                        .addRulesItem(new ANDRule().condition(ANDRule.ConditionEnum.AND)
                        .addExpressionsItem(new Expression().field("claim").operator("equals")
                                .value(NICK_NAME_CLAIM_URI))),  TEST_USER_CLAIM_VALUE, UserItemAddGroupobj.OpEnum.ADD, true},
                {new ORRule()
                        .condition(ORRule.ConditionEnum.OR)
                        .addRulesItem(new ANDRule().condition(ANDRule.ConditionEnum.AND)
                                .addExpressionsItem(new Expression().field("flow").operator("equals")
                                        .value("adminInitiatedProfileUpdate")))
                        .addRulesItem(new ANDRule().condition(ANDRule.ConditionEnum.AND)
                        .addExpressionsItem(new Expression().field("claim").operator("equals")
                                .value(NICK_NAME_CLAIM_URI))),  TEST_USER_UPDATED_CLAIM_VALUE, UserItemAddGroupobj.OpEnum.REPLACE, true},
                {new ORRule()
                        .condition(ORRule.ConditionEnum.OR)
                        .addRulesItem(new ANDRule().condition(ANDRule.ConditionEnum.AND)
                                .addExpressionsItem(new Expression().field("flow").operator("equals")
                                        .value("adminInitiatedProfileUpdate")))
                        .addRulesItem(new ANDRule().condition(ANDRule.ConditionEnum.AND)
                        .addExpressionsItem(new Expression().field("claim").operator("equals")
                                .value(NICK_NAME_CLAIM_URI))),  EMPTY_STRING, UserItemAddGroupobj.OpEnum.REMOVE, true},
                {new ORRule()
                        .condition(ORRule.ConditionEnum.OR)
                        .addRulesItem(new ANDRule().condition(ANDRule.ConditionEnum.AND)
                        .addExpressionsItem(new Expression().field("flow").operator("notEquals")
                                .value("adminInitiatedProfileUpdate"))), TEST_USER_CLAIM_VALUE, UserItemAddGroupobj.OpEnum.ADD, false},
                {new ORRule()
                        .condition(ORRule.ConditionEnum.OR)
                        .addRulesItem(new ANDRule().condition(ANDRule.ConditionEnum.AND)
                        .addExpressionsItem(new Expression().field("claim").operator("notEquals")
                                .value(NICK_NAME_CLAIM_URI))), TEST_USER_UPDATED_CLAIM_VALUE, UserItemAddGroupobj.OpEnum.REPLACE, false},
                {new ORRule()
                        .condition(ORRule.ConditionEnum.OR)
                        .addRulesItem(new ANDRule().condition(ANDRule.ConditionEnum.AND)
                        .addExpressionsItem(new Expression().field("flow").operator("notEquals")
                                .value("adminInitiatedProfileUpdate"))
                        .addExpressionsItem(new Expression().field("claim").operator("notEquals")
                                .value(NICK_NAME_CLAIM_URI))), TEST_USER_CLAIM_VALUE, UserItemAddGroupobj.OpEnum.REPLACE, false},
                {new ORRule()
                        .condition(ORRule.ConditionEnum.OR)
                        .addRulesItem(new ANDRule().condition(ANDRule.ConditionEnum.AND)
                                .addExpressionsItem(new Expression().field("flow").operator("equals")
                                        .value("userInitiatedProfileUpdate")))
                        .addRulesItem(new ANDRule().condition(ANDRule.ConditionEnum.AND)
                        .addExpressionsItem(new Expression().field("claim").operator("equals")
                                .value(GIVEN_NAME_CLAIM_URI))), TEST_USER_UPDATED_CLAIM_VALUE, UserItemAddGroupobj.OpEnum.REPLACE, false},
        };
    }

    @Test(dataProvider = "rulesProvider", groups = "wso2.is", description = "Update profile")
    public void testPreUpdateProfileActionInvocationForRules(ORRule rule, String value, UserItemAddGroupobj.OpEnum operation, boolean shouldActionExecute)
            throws Exception {

        assertTrue(updatePreUpdateProfileActionRule(rule),
                "Updating the pre update profile action rule returned an error.");

        if (shouldActionExecute) {
            UserItemAddGroupobj userPatchOp = new UserItemAddGroupobj().op(operation);
            userPatchOp.setPath(NICK_NAME_USER_SCHEMA_NAME);
            if (operation != UserItemAddGroupobj.OpEnum.REMOVE) {
                userPatchOp.setValue(value);
            }
            PatchOperationRequestObject patchUserInfo = new PatchOperationRequestObject()
                    .addOperations(userPatchOp);
            scim2RestClient.updateUser(patchUserInfo, userId);
            if (operation != UserItemAddGroupobj.OpEnum.REMOVE) {
                assertUpdatingClaimValue(value);
            } else {
                assertRemovingClaimValue();
            }
            assertActionRequestPayload(operation, userId, currentClaimValue, value,
                    PreUpdateProfileEvent.FlowInitiatorType.ADMIN, PreUpdateProfileEvent.Action.UPDATE);
            currentClaimValue = value;
        } else {
            UserItemAddGroupobj userPatchOp = new UserItemAddGroupobj().op(operation);
            userPatchOp.setPath(NICK_NAME_USER_SCHEMA_NAME);
            userPatchOp.setValue(value);
            PatchOperationRequestObject patchUserInfo = new PatchOperationRequestObject()
                    .addOperations(userPatchOp);
            scim2RestClient.updateUser(patchUserInfo, userId);
            assertEquals(serviceExtensionMockServer.getReceivedRequestPayload(MOCK_SERVER_ENDPOINT_RESOURCE_PATH), EMPTY_STRING);
        }
    }

    private boolean updatePreUpdateProfileActionRule(ORRule rule) throws IOException {

        ActionUpdateModel actionModel = new ActionUpdateModel().rule(rule);
        return updateAction(PRE_UPDATE_PROFILE_API_PATH, actionId, actionModel);
    }

    private void assertUpdatingClaimValue(String claimValue) throws Exception {

        org.json.simple.JSONObject userObj = scim2RestClient.getUser(userId, null);
        String value = userObj.get(NICK_NAME_USER_SCHEMA_NAME).toString();
        Assert.assertEquals(value, claimValue);
    }

    private void assertRemovingClaimValue() throws Exception {

        org.json.simple.JSONObject userObj = scim2RestClient.getUser(userId, null);
        Assert.assertNull(userObj.get(NICK_NAME_USER_SCHEMA_NAME));
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

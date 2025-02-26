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

package org.wso2.identity.integration.test.identity.mgt;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.identity.governance.stub.bean.Property;
import org.wso2.carbon.integration.common.admin.client.AuthenticatorClient;
import org.wso2.identity.integration.common.clients.mgt.IdentityGovernanceServiceClient;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;
import org.wso2.identity.integration.test.restclients.SCIM2RestClient;
import org.wso2.identity.integration.test.rest.api.user.common.model.Email;
import org.wso2.identity.integration.test.rest.api.user.common.model.GroupRequestObject;
import org.wso2.identity.integration.test.rest.api.user.common.model.GroupRequestObject.MemberItem;
import org.wso2.identity.integration.test.rest.api.user.common.model.ListObject;
import org.wso2.identity.integration.test.rest.api.user.common.model.Name;
import org.wso2.identity.integration.test.rest.api.user.common.model.PatchOperationRequestObject;
import org.wso2.identity.integration.test.rest.api.user.common.model.RoleItemAddGroupobj;
import org.wso2.identity.integration.test.rest.api.user.common.model.UserObject;
import org.wso2.identity.integration.test.rest.api.server.roles.v2.model.RoleV2;
import org.wso2.identity.integration.test.restclients.IdentityGovernanceRestClient;
import org.wso2.identity.integration.test.rest.api.server.identity.governance.v1.dto.ConnectorsPatchReq;
import org.wso2.identity.integration.test.rest.api.server.identity.governance.v1.dto.PropertyReq;

import java.io.IOException;

import java.util.Collections;

public class PasswordExpirationTestCase extends ISIntegrationTest {

    private static final Log log = LogFactory.getLog(PasswordExpirationTestCase.class);
    private static final String TEST_USER1_USERNAME = "pwdExpiryTestUser1";
    private static final String TEST_USER2_USERNAME = "pwdExpiryTestUser2";
    private static final String TEST_USER3_USERNAME = "pwdExpiryTestUser3";
    private static final String TEST_USER4_USERNAME = "pwdExpiryTestUser4";
    private static final String TEST_USER_PASSWORD = "Test@123";
    private static final String TEST_USER_NEW_PASSWORD = "NewTest@123";
    private static final String TEST_ROLE1 = "pwdExpiryTestRole1";
    private static final String TEST_ROLE2 = "pwdExpiryTestRole2";
    private static final String TEST_GROUP1 = "pwdExpiryTestGroup1";
    private static final String TEST_GROUP2 = "pwdExpiryTestGroup2";

    private static final String USERS_PATH = "users";
    private static final String PASSWORD_EXPIRY_CATEGORY_ID = "UGFzc3dvcmQgUG9saWNpZXM";
    private static final String PASSWORD_EXPIRY_CONNECTOR_ID = "cGFzc3dvcmRFeHBpcnk";
    private static final String PASSWORD_EXPIRY_ENABLED = "passwordExpiry.enablePasswordExpiry";
    private static final String PASSWORD_EXPIRY_TIME = "passwordExpiry.passwordExpiryInDays";
    private static final String PASSWORD_EXPIRY_SKIP_IF_NO_APPLICABLE_RULES = "passwordExpiry.skipIfNoApplicableRules";
    private static final String PASSWORD_EXPIRY_RULE1 = "passwordExpiry.rule1";
    private static final String PASSWORD_EXPIRY_RULE2 = "passwordExpiry.rule1";
    private static final String PASSWORD_EXPIRY_RULE3 = "passwordExpiry.rule1";
    private static final String PASSWORD_EXPIRY_RULE4 = "passwordExpiry.rule1";

    private AuthenticatorClient loginClient;
    private SCIM2RestClient scim2RestClient;
    private IdentityGovernanceRestClient identityGovernanceRestClient;
    
    private String user1Id;
    private String user2Id;
    private String user3Id;
    private String user4Id;
    private String role1Id;
    private String role2Id;
    private String group1Id;
    private String group2Id;

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.init(TestUserMode.SUPER_TENANT_ADMIN);

        loginClient = new AuthenticatorClient(backendURL);
        scim2RestClient = new SCIM2RestClient(serverURL, tenantInfo);
        identityGovernanceRestClient = new IdentityGovernanceRestClient(serverURL, tenantInfo);

        /*
        * The following steps are performed in this method:
        * 1. Add users - user1, user2, user3, user4.
        * 2. Create roles - role1, role2.
        * 3. Assign users to roles - user1 -> role1, user2 -> role1, role2.
        * 4. Create groups - group1, group2.
        * 5. Assign users to groups - user3 -> group1, user4 -> group1, group2.
         */
        addUsers();
        configureRoles();
        configureGroups();
    }

    @AfterClass(alwaysRun = true)
    public void cleanUp() throws Exception {

        // Reset password expiration policy.
        setPasswordExpirationPolicy(false, false, true);

        // Delete users
        if (user1Id != null) scim2RestClient.deleteUser(user1Id);
        if (user2Id != null) scim2RestClient.deleteUser(user2Id);
        if (user3Id != null) scim2RestClient.deleteUser(user3Id);
        if (user4Id != null) scim2RestClient.deleteUser(user4Id);

        // Delete roles.
        if (role1Id != null) scim2RestClient.deleteV2Role(role1Id);
        if (role2Id != null) scim2RestClient.deleteV2Role(role2Id);

        // Delete groups.
        if (group1Id != null) scim2RestClient.deleteGroup(group1Id);
        if (group2Id != null) scim2RestClient.deleteGroup(group2Id);

        // Close the clients.
        if (scim2RestClient != null) scim2RestClient.closeHttpClient();
        if (identityGovernanceRestClient != null) identityGovernanceRestClient.closeHttpClient();
    }
    
    @Test(groups = "wso2.is", description = "Test enabling password expiration")
    public void testEnablePasswordExpiration() throws Exception {
        // Configure password expiration (1 day)
        setPasswordExpirationPolicy(true, false, true);

        
    }
    
//    @Test(groups = "wso2.is", description = "Test authentication with non-expired password for user with role",
//            dependsOnMethods = "testEnablePasswordExpiration")
//    public void testAuthenticationWithNonExpiredPasswordForRoleUser() throws Exception {
//        // Login with valid credentials for user1 (assigned to role1)
//        String sessionCookie = loginClient.login(TEST_USER1_USERNAME, TEST_USER_PASSWORD, isServer.getContextUrls().getBackEndUrl());
//        Assert.assertNotNull(sessionCookie, "Authentication with non-expired password should succeed for role user");
//    }

    private void addUsers() throws Exception {

        user1Id = addUser(TEST_USER1_USERNAME);
        Assert.assertNotNull(user1Id, "Failed to create user1");

        user2Id = addUser(TEST_USER2_USERNAME);
        Assert.assertNotNull(user2Id, "Failed to create user2");

        user3Id = addUser(TEST_USER3_USERNAME);
        Assert.assertNotNull(user3Id, "Failed to create user3");

        user4Id = addUser(TEST_USER4_USERNAME);
        Assert.assertNotNull(user4Id, "Failed to create user4");
    }

    private String addUser(String userName) throws Exception {

        UserObject user = new UserObject();
        user.setUserName(userName);
        user.setPassword(TEST_USER_PASSWORD);
        user.setName(new Name().givenName(userName));
        user.addEmail(new Email().value(userName + "@example.com"));
        return scim2RestClient.createUser(user);
    }

    private void configureRoles() throws IOException {

        // Create roles.
        RoleV2 role1 = new RoleV2(null, TEST_ROLE1, Collections.emptyList(), Collections.emptyList());
        role1Id = scim2RestClient.addV2Role(role1);
        Assert.assertNotNull(role1Id, "Failed to create role1");

        RoleV2 role2 = new RoleV2(null, TEST_ROLE2, Collections.emptyList(), Collections.emptyList());
        role2Id = scim2RestClient.addV2Role(role2);
        Assert.assertNotNull(role2Id, "Failed to create role2");

        // Assign user1 & user2 -> role1.
        RoleItemAddGroupobj role1PatchReqObject = new RoleItemAddGroupobj();
        role1PatchReqObject.setOp(RoleItemAddGroupobj.OpEnum.ADD);
        role1PatchReqObject.setPath(USERS_PATH);
        ListObject role1Users = new ListObject();
        role1Users.setValue(user1Id);
        role1Users.setValue(user2Id);
        role1PatchReqObject.addValue(role1Users);
        scim2RestClient.updateUsersOfRoleV2(role1Id,
                new PatchOperationRequestObject().addOperations(role1PatchReqObject));

        // Assign user2 -> role2.
        RoleItemAddGroupobj role2PatchReqObject = new RoleItemAddGroupobj();
        role2PatchReqObject.setOp(RoleItemAddGroupobj.OpEnum.ADD);
        role2PatchReqObject.setPath(USERS_PATH);
        role2PatchReqObject.addValue(new ListObject().value(user2Id));
        scim2RestClient.updateUsersOfRoleV2(role1Id,
                new PatchOperationRequestObject().addOperations(role2PatchReqObject));
    }

    private void configureGroups() throws Exception {

        // Create group1.
        group1Id = scim2RestClient.createGroup(
                new GroupRequestObject()
                        .displayName(TEST_GROUP1)
                        .addMember(new MemberItem().value(user3Id))
                        .addMember(new MemberItem().value(user4Id)));
        Assert.assertNotNull(group1Id, "Failed to create group1");

        // Create group2.
        group2Id = scim2RestClient.createGroup(
                new GroupRequestObject()
                        .displayName(TEST_GROUP2)
                        .addMember(new MemberItem().value(user4Id)));
        Assert.assertNotNull(group2Id, "Failed to create group2");
    }


    /**
     * Update password expiration policy with rules.
     *
     * @param enabled Whether password expiration is enabled.
     * @param skipIfNoApplicableRules Whether to skip expiration if no rules apply.
     * @throws Exception If an error occurs.
     */
    private void setPasswordExpirationPolicy(boolean enabled, boolean skipIfNoApplicableRules, boolean noRules)
            throws Exception {

        ConnectorsPatchReq patchReq = new ConnectorsPatchReq();
        patchReq.setOperation(ConnectorsPatchReq.OperationEnum.UPDATE);
        
        // Enable/disable password expiration
        PropertyReq enableProperty = new PropertyReq();
        enableProperty.setName(PASSWORD_EXPIRY_ENABLED);
        enableProperty.setValue(String.valueOf(enabled));
        patchReq.addProperties(enableProperty);
        
        // Set default expiry time in days.
        PropertyReq expiryDaysProperty = new PropertyReq();
        expiryDaysProperty.setName(PASSWORD_EXPIRY_TIME);
        expiryDaysProperty.setValue("30");
        patchReq.addProperties(expiryDaysProperty);
        
        // Set skip if no applicable rules.
        PropertyReq skipProperty = new PropertyReq();
        skipProperty.setName(PASSWORD_EXPIRY_SKIP_IF_NO_APPLICABLE_RULES);
        skipProperty.setValue(noRules ? StringUtils.EMPTY : String.valueOf(skipIfNoApplicableRules));
        patchReq.addProperties(skipProperty);
        
        // Rule 1: Skip password expiration for users in role1 and role2.
        PropertyReq rule1Property = new PropertyReq();
        rule1Property.setName(PASSWORD_EXPIRY_RULE1);
        rule1Property.setValue(noRules ? StringUtils.EMPTY : String.format("1,0,roles,ne,%s,%s", role1Id, role2Id));
        patchReq.addProperties(rule1Property);
        
        // Rule 2: Apply password expiration for 20 days for users in role1.
        PropertyReq rule2Property = new PropertyReq();
        rule2Property.setName(PASSWORD_EXPIRY_RULE2);
        rule2Property.setValue(noRules ? StringUtils.EMPTY : String.format("2,20,roles,eq,%s", role1Id));
        patchReq.addProperties(rule2Property);
        
        // Rule 3: Skip password expiration for users in group1 and group2.
        PropertyReq rule3Property = new PropertyReq();
        rule3Property.setName(PASSWORD_EXPIRY_RULE3);
        rule3Property.setValue(noRules ? StringUtils.EMPTY : String.format("3,0,groups,ne,%s,%s", group1Id, group2Id));
        patchReq.addProperties(rule3Property);
        
        // Rule 4: Apply password expiration for 10 days for users in group1.
        PropertyReq rule4Property = new PropertyReq();
        rule4Property.setName(PASSWORD_EXPIRY_RULE4);
        rule4Property.setValue(noRules ? StringUtils.EMPTY : String.format("4,10,groups,eq,%s", group1Id));
        patchReq.addProperties(rule4Property);

        identityGovernanceRestClient.updateConnectors(PASSWORD_EXPIRY_CATEGORY_ID, PASSWORD_EXPIRY_CONNECTOR_ID,
                patchReq);
        Thread.sleep(5000);
    }

}

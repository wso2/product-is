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

package org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v1;

import io.restassured.response.Response;
import org.apache.http.HttpStatus;
import org.json.JSONObject;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationPatchModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.AssociatedRolesConfig;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.AssociatedRolesConfig.AllowedAudienceEnum;
import org.wso2.identity.integration.test.rest.api.server.roles.v2.model.Audience;
import org.wso2.identity.integration.test.rest.api.server.roles.v2.model.RoleV2;
import org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v1.model.RoleWithAudience;
import org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v1.model.RoleWithAudienceAudience;
import org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v1.model.UserShareRequestBody;
import org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v1.model.UserShareRequestBodyOrganizations;
import org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v1.model.UserShareRequestBodyUserCriteria;
import org.wso2.identity.integration.test.rest.api.user.common.model.Email;
import org.wso2.identity.integration.test.rest.api.user.common.model.Name;
import org.wso2.identity.integration.test.rest.api.user.common.model.UserObject;
import org.wso2.identity.integration.test.restclients.OAuth2RestClient;
import org.wso2.identity.integration.test.restclients.OrgMgtRestClient;
import org.wso2.identity.integration.test.restclients.SCIM2RestClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;

/**
 * Tests for successful cases of the User Sharing REST APIs.
 */
public class UserSharingSuccessTest extends UserSharingBaseTest {

    private String rootOrgUserId;
    private String l1Org1UserId;

    private String l1Org1Id;
    private String l1Org2Id;
    private String l2Org1Id;
    private String l2Org2Id;
    private String l2Org3Id;
    private String l3Org1Id;

    private String l1Org1SwitchToken;

    private String appId1;
    private String appId2;

    private String appRole1Id;
    private String orgRole1Id;

    @Factory(dataProvider = "restAPIUserConfigProvider")
    public UserSharingSuccessTest(TestUserMode userMode) throws Exception {

        super.init(userMode);
        this.context = isServer;
        this.authenticatingUserName = context.getContextTenant().getTenantAdmin().getUserName();
        this.authenticatingCredential = context.getContextTenant().getTenantAdmin().getPassword();
        this.tenant = context.getContextTenant().getDomain();
    }

    @Override
    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {

        super.testInit(API_VERSION, swaggerDefinition, tenant);

        oAuth2RestClient = new OAuth2RestClient(serverURL, tenantInfo);
        scim2RestClient = new SCIM2RestClient(serverURL, tenantInfo);
        orgMgtRestClient = new OrgMgtRestClient(context, tenantInfo, serverURL,
                new JSONObject(readResource(AUTHORIZED_APIS_JSON)));

        setupOrganizations();
        setupTokens();
        setupApplicationsAndRoles();
        shareApplications();
        setupUsers();
    }

    @Override
    @AfterClass(alwaysRun = true)
    public void testConclude() throws Exception {

        // Cleanup users
        deleteUserIfExists(rootOrgUserId);
        deleteSubOrgUserIfExists(l1Org1UserId);

        // Cleanup roles
        deleteRoleIfExists(appRole1Id);
        deleteRoleIfExists(orgRole1Id);

        // Cleanup applications
        deleteApplicationIfExists(appId1);
        deleteApplicationIfExists(appId2);

        // Cleanup organizations
        deleteSubOrganizationIfExists(l3Org1Id, l2Org1Id);
        deleteSubOrganizationIfExists(l2Org3Id, l1Org2Id);
        deleteSubOrganizationIfExists(l2Org2Id, l1Org1Id);
        deleteSubOrganizationIfExists(l2Org1Id, l1Org1Id);
        deleteOrganizationIfExists(l1Org2Id);
        deleteOrganizationIfExists(l1Org1Id);

        // Close REST clients
        closeRestClients();
    }

    @DataProvider(name = "restAPIUserConfigProvider")
    public static Object[][] restAPIUserConfigProvider() {

        return new Object[][]{
                {TestUserMode.SUPER_TENANT_ADMIN},
                {TestUserMode.TENANT_ADMIN}
        };
    }

    @Test
    public void testShareUsersWithOrganizations() {

        UserShareRequestBody requestBody = new UserShareRequestBody()
                .userCriteria(getUserCriteria())
                .organizations(getOrganizations());

        Response response = getResponseOfPost(USER_SHARING_API_BASE_PATH + SHARE_PATH, toJSONString(requestBody));

        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_ACCEPTED)
                .body("status", equalTo("Processing"))
                .body("details", equalTo("User sharing process triggered successfully."));
    }

    private UserShareRequestBodyUserCriteria getUserCriteria() {

        UserShareRequestBodyUserCriteria criteria = new UserShareRequestBodyUserCriteria();
        criteria.setUserIds(Collections.singletonList(rootOrgUserId));
        return criteria;
    }

    private List<UserShareRequestBodyOrganizations> getOrganizations() {

        UserShareRequestBodyOrganizations organizationWithRoles = new UserShareRequestBodyOrganizations();
        organizationWithRoles.setOrgId(l1Org1Id);
        organizationWithRoles.setPolicy(
                UserShareRequestBodyOrganizations.PolicyEnum.SELECTED_ORG_WITH_EXISTING_IMMEDIATE_AND_FUTURE_CHILDREN);
        organizationWithRoles.setRoles(Arrays.asList(createRoleWithAudience(APP_ROLE_1, APP_1, APPLICATION_AUDIENCE),
                createRoleWithAudience(ORG_ROLE_1, SUPER_ORG, ORGANIZATION_AUDIENCE)));

        UserShareRequestBodyOrganizations organizationWithoutRoles = new UserShareRequestBodyOrganizations();
        organizationWithoutRoles.setOrgId(l1Org2Id);
        organizationWithoutRoles.setPolicy(UserShareRequestBodyOrganizations.PolicyEnum.SELECTED_ORG_ONLY);

        return Arrays.asList(organizationWithRoles, organizationWithoutRoles);
    }

    private RoleWithAudience createRoleWithAudience(String roleName, String display, String type) {

        RoleWithAudienceAudience audience = new RoleWithAudienceAudience();
        audience.setDisplay(display);
        audience.setType(type);

        RoleWithAudience roleWithAudience = new RoleWithAudience();
        roleWithAudience.setDisplayName(roleName);
        roleWithAudience.setAudience(audience);

        return roleWithAudience;
    }

    private void setupOrganizations() throws Exception {

        l1Org1Id = orgMgtRestClient.addOrganization(L1_ORG_1_NAME);
        l1Org2Id = orgMgtRestClient.addOrganization(L1_ORG_2_NAME);
        l2Org1Id = orgMgtRestClient.addSubOrganization(L2_ORG_1_NAME, l1Org1Id);
        l2Org2Id = orgMgtRestClient.addSubOrganization(L2_ORG_2_NAME, l1Org1Id);
        l2Org3Id = orgMgtRestClient.addSubOrganization(L2_ORG_3_NAME, l1Org2Id);
        l3Org1Id = orgMgtRestClient.addSubOrganization(L3_ORG_1_NAME, l2Org1Id);
    }

    private void setupTokens() throws Exception {

        l1Org1SwitchToken = orgMgtRestClient.switchM2MToken(l1Org1Id);
    }

    private void setupApplicationsAndRoles() throws Exception {

        appId1 = addApplication(APP_1);
        appId2 = addApplication(APP_2);

        Audience roleAudience1 = new Audience(APPLICATION_AUDIENCE, appId1);
        RoleV2 appRole1 = new RoleV2(roleAudience1, APP_ROLE_1, Collections.emptyList(), getRoleV2Schema());
        appRole1Id = oAuth2RestClient.createV2Roles(appRole1);

        RoleV2 orgRole1 = new RoleV2(null, ORG_ROLE_1, getPermissions(), getRoleV2Schema());
        orgRole1Id = oAuth2RestClient.createV2Roles(orgRole1);

        switchApplicationAudience(appId2, AllowedAudienceEnum.ORGANIZATION);
    }

    private void switchApplicationAudience(String appId, AllowedAudienceEnum newAudience) throws Exception {

        AssociatedRolesConfig associatedRolesConfigApp2 = new AssociatedRolesConfig();
        associatedRolesConfigApp2.setAllowedAudience(newAudience);

        ApplicationPatchModel patchModelApp2 = new ApplicationPatchModel();
        patchModelApp2.setAssociatedRoles(associatedRolesConfigApp2);

        oAuth2RestClient.updateApplication(appId, patchModelApp2);
    }

    private void shareApplications() throws Exception {

        shareApplicationWithAllOrgs(appId1);
        shareApplicationWithAllOrgs(appId2);
    }

    private void setupUsers() throws Exception {

        UserObject rootOrgUser = createUserObject(ROOT_ORG_USERNAME, ROOT_ORG_NAME);
        rootOrgUserId = scim2RestClient.createUser(rootOrgUser);

        UserObject l1Org1User = createUserObject(L1_ORG_1_USERNAME, L1_ORG_1_NAME);
        l1Org1UserId = scim2RestClient.createSubOrgUser(l1Org1User, l1Org1SwitchToken);
    }

    private static UserObject createUserObject(String userName, String orgName) {

        UserObject user = new UserObject()
                .userName( "PRIMARY/" + userName)
                .password("Admin123")
                .name(new Name().givenName(userName).familyName(orgName))
                .emails(new ArrayList<>());

        Email email = new Email();
        email.setValue(userName + "@gmail.com");
        email.setPrimary(true);
        user.getEmails().add(email);

        List<String> schemas = new ArrayList<>();
        schemas.add("urn:ietf:params:scim:schemas:core:2.0:User");
        user.setSchemas(schemas);

        return user;
    }

    private void deleteUserIfExists(String userId) throws Exception {

        if (userId != null) {
            scim2RestClient.deleteUser(userId);
        }
    }

    private void deleteSubOrgUserIfExists(String userId) throws Exception {

        if (userId != null) {
            scim2RestClient.deleteSubOrgUser(userId, l1Org1SwitchToken);
        }
    }

    private void deleteRoleIfExists(String roleId) throws Exception {

        if (roleId != null) {
            oAuth2RestClient.deleteV2Role(roleId);
        }
    }

    private void deleteApplicationIfExists(String appId) throws Exception {

        if (appId != null) {
            oAuth2RestClient.deleteApplication(appId);
        }
    }

    private void deleteSubOrganizationIfExists(String orgId, String parentId) throws Exception {

        if (orgId != null) {
            orgMgtRestClient.deleteSubOrganization(orgId, parentId);
        }
    }

    private void deleteOrganizationIfExists(String orgId) throws Exception {

        if (orgId != null) {
            orgMgtRestClient.deleteOrganization(orgId);
        }
    }

    private void closeRestClients() throws IOException {

        oAuth2RestClient.closeHttpClient();
        scim2RestClient.closeHttpClient();
        orgMgtRestClient.closeHttpClient();
    }
}

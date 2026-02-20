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

package org.wso2.identity.integration.test.provisioning;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.test.utils.dbutils.H2DataBaseManager;
import org.wso2.identity.integration.common.clients.application.mgt.ApplicationManagementServiceClient;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;
import org.wso2.identity.integration.test.base.TestDataHolder;
import org.wso2.identity.integration.test.rest.api.server.idp.v1.model.Roles;
import org.wso2.identity.integration.test.rest.api.server.user.store.v1.model.UserStoreReq;
import org.wso2.identity.integration.test.restclients.IdpMgtRestClient;
import org.wso2.identity.integration.test.restclients.SCIM2RestClient;
import org.wso2.identity.integration.test.restclients.UserStoreMgtRestClient;
import org.wso2.identity.integration.test.util.Utils;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;

import static org.wso2.identity.integration.test.provisioning.OutboundProvisioningTestUtils.*;

/**
 * Tests group-based outbound provisioning via SCIM2.
 */
public class GroupBasedSCIM2OutboundProvisioningTest extends ISIntegrationTest {

    private static final String IDP_NAME = "group-based-outbound-provisioning-connection";
    private static final String IDP_DESCRIPTION = "Group-based SCIM outbound provisioning connection";
    private static final String IDP_IMAGE = "assets/images/logos/outbound-provisioning.svg";

    private static final String OUTBOUND_PROVISIONING_GROUP = "outboundProvisioningGroup";

    private static final String TEST_USER1_NAME = "grp.user1";
    private static final String TEST_USER1_PASSWORD = "Wso2@test";
    private static final String TEST_USER1_GIVEN_NAME = "GroupUser";
    private static final String TEST_USER1_FAMILY_NAME = "One";
    private static final String TEST_USER1_EMAIL = "grp.user1@example.com";

    private static final String TEST_USER2_NAME = "grp.user2";
    private static final String TEST_USER2_PASSWORD = "Wso2@test";
    private static final String TEST_USER2_GIVEN_NAME = "GroupUser";
    private static final String TEST_USER2_FAMILY_NAME = "Two";
    private static final String TEST_USER2_EMAIL = "grp.user2@example.com";

    private static final String REGULAR_GROUP = "regularGroup";

    // Secondary user store constants.
    private static final String SECONDARY_DOMAIN = "SECONDARY";
    private static final String USER_STORE_DB_NAME = "OUTBOUND_PROV_SECONDARY_DB";
    private static final String DB_USER_NAME = "wso2automation";
    private static final String DB_USER_PASSWORD = "wso2automation";
    private static final String USER_STORE_TYPE = "VW5pcXVlSURKREJDVXNlclN0b3JlTWFuYWdlcg";

    // Secondary domain outbound provisioning group and user constants.
    private static final String SEC_US_GROUP_NAME = "outboundProvisioningGroup2";
    private static final String SEC_US_USER_NAME = "sec.grp.user1";
    private static final String SEC_US_DOMAIN_QUALIFIED_GROUP =
            SECONDARY_DOMAIN + "/" + SEC_US_GROUP_NAME;
    private static final String SEC_US_DOMAIN_QUALIFIED_USER = SECONDARY_DOMAIN + "/" + SEC_US_USER_NAME;
    private static final String SEC_US_USER_PASSWORD = "Wso2@test";
    private static final String SEC_US_USER_GIVEN_NAME = "SecGroupUser";
    private static final String SEC_US_USER_FAMILY_NAME = "One";
    private static final String SEC_US_USER_EMAIL = "sec.grp.user1@example.com";

    private IdpMgtRestClient idpMgtRestClient;
    private ApplicationManagementServiceClient appMgtClient;
    private SCIM2RestClient primaryScim2RestClient;
    private SCIM2RestClient secondaryScim2RestClient;
    private UserStoreMgtRestClient userStoreMgtRestClient;
    private TestDataHolder testDataHolder;

    private String idpId;
    private String outboundProvisioningGroupId;
    private String primaryUser1Id;
    private String primaryUser2Id;
    private String nonProvisioningGroupId;
    private String userStoreId;
    private String secondaryOutboundProvisioningGroupId;
    private String secondaryDomainUserId;

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.init();

        testDataHolder = TestDataHolder.getInstance();

        idpMgtRestClient = new IdpMgtRestClient(serverURL, tenantInfo);
        primaryScim2RestClient = new SCIM2RestClient(serverURL, tenantInfo);
        secondaryScim2RestClient = new SCIM2RestClient(getSecondaryISURI(), tenantInfo);
        userStoreMgtRestClient = new UserStoreMgtRestClient(serverURL, tenantInfo);

        ConfigurationContext configContext = ConfigurationContextFactory
                .createConfigurationContextFromFileSystem(null, null);
        appMgtClient = new ApplicationManagementServiceClient(sessionCookie, backendURL, configContext);
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {

        clearResidentAppOutboundProvisioning(appMgtClient);

        // Clean up users on primary IS.
        if (primaryUser1Id != null) {
            primaryScim2RestClient.deleteUser(primaryUser1Id);
        }
        if (primaryUser2Id != null) {
            primaryScim2RestClient.deleteUser(primaryUser2Id);
        }
        if (secondaryDomainUserId != null) {
            primaryScim2RestClient.deleteUser(secondaryDomainUserId);
        }

        // Clean up groups on primary IS.
        if (outboundProvisioningGroupId != null) {
            primaryScim2RestClient.deleteGroup(outboundProvisioningGroupId);
        }
        if (nonProvisioningGroupId != null) {
            primaryScim2RestClient.deleteGroup(nonProvisioningGroupId);
        }
        if (secondaryOutboundProvisioningGroupId != null) {
            primaryScim2RestClient.deleteGroup(secondaryOutboundProvisioningGroupId);
        }

        // Clean up the secondary user store.
        if (userStoreId != null) {
            userStoreMgtRestClient.deleteUserStore(userStoreId);
            userStoreMgtRestClient.waitForUserStoreUnDeployment(userStoreId);
        }

        if (idpId != null) {
            idpMgtRestClient.deleteIdp(idpId);
        }
        if (idpMgtRestClient != null) {
            idpMgtRestClient.closeHttpClient();
        }
        if (userStoreMgtRestClient != null) {
            userStoreMgtRestClient.closeHttpClient();
        }
    }

    @Test(alwaysRun = true,
            description = "Create the outbound provisioning group that will be used as the provisioning trigger")
    public void testCreateOutboundProvisioningGroup() throws Exception {

        String groupPayload = buildGroupPayloadNoMembers(OUTBOUND_PROVISIONING_GROUP);
        outboundProvisioningGroupId = primaryScim2RestClient.createGroupWithRawJSON(groupPayload);
        Assert.assertNotNull(outboundProvisioningGroupId,
                "Outbound provisioning group creation on primary IS failed");
    }

    @Test(alwaysRun = true, dependsOnMethods = "testCreateOutboundProvisioningGroup",
            description = "Create IdP with outbound provisioning role set to the provisioning group")
    public void testCreateIdPWithOutboundProvisioningGroup() throws Exception {

        idpId = createOutboundProvisioningIdP(idpMgtRestClient, IDP_NAME, IDP_DESCRIPTION, IDP_IMAGE,
                testDataHolder.getAutomationContext().getSuperTenant().getTenantAdmin(),
                Collections.singletonList(OUTBOUND_PROVISIONING_GROUP));
        Assert.assertNotNull(idpId, "Identity Provider creation failed - returned null ID");

        JSONObject idpResponse = idpMgtRestClient.getIdentityProvider(idpId);
        Assert.assertNotNull(idpResponse, "Failed to retrieve created Identity Provider");

        JSONObject roles = (JSONObject) idpResponse.get("roles");
        Assert.assertNotNull(roles, "Roles configuration not found on IdP");
        JSONArray outboundProvisioningRoles = (JSONArray) roles.get("outboundProvisioningRoles");
        Assert.assertNotNull(outboundProvisioningRoles, "outboundProvisioningRoles not found");
        Assert.assertEquals(outboundProvisioningRoles.size(), 1,
                "Expected exactly 1 outbound provisioning role");
        Assert.assertEquals(outboundProvisioningRoles.get(0), OUTBOUND_PROVISIONING_GROUP,
                "Outbound provisioning role name mismatch");
    }

    @Test(alwaysRun = true, dependsOnMethods = "testCreateIdPWithOutboundProvisioningGroup",
            description = "Enable outbound provisioning on the resident application")
    public void testEnableOutboundProvisioningOnResidentApp() throws Exception {

        enableResidentAppOutboundProvisioning(appMgtClient, IDP_NAME);
    }

    @Test(alwaysRun = true, dependsOnMethods = "testEnableOutboundProvisioningOnResidentApp",
            description = "Create users and a Regular group - users should NOT be provisioned, group should be provisioned")
    public void testUsersNotProvisionedWithoutGroupAssignment() throws Exception {

        // Create two users.
        primaryUser1Id = primaryScim2RestClient.createUserWithRawJSON(
                buildUserPayload(TEST_USER1_NAME, TEST_USER1_PASSWORD, TEST_USER1_GIVEN_NAME,
                        TEST_USER1_FAMILY_NAME, TEST_USER1_EMAIL));
        Assert.assertNotNull(primaryUser1Id, "User1 creation on primary IS failed");

        primaryUser2Id = primaryScim2RestClient.createUserWithRawJSON(
                buildUserPayload(TEST_USER2_NAME, TEST_USER2_PASSWORD, TEST_USER2_GIVEN_NAME,
                        TEST_USER2_FAMILY_NAME, TEST_USER2_EMAIL));
        Assert.assertNotNull(primaryUser2Id, "User2 creation on primary IS failed");

        String regularGroupPayload = buildGroupPayloadNoMembers(REGULAR_GROUP);
        nonProvisioningGroupId = primaryScim2RestClient.createGroupWithRawJSON(regularGroupPayload);
        Assert.assertNotNull(nonProvisioningGroupId, "Regular group creation on primary IS failed");

        // Verify users were NOT provisioned to the secondary IS.
        String secondaryUser1Id = getProvisionedUserId(secondaryScim2RestClient, TEST_USER1_NAME);
        Assert.assertNull(secondaryUser1Id,
                "User1 should NOT be provisioned to secondary IS without group assignment");

        String secondaryUser2Id = getProvisionedUserId(secondaryScim2RestClient, TEST_USER2_NAME);
        Assert.assertNull(secondaryUser2Id,
                "User2 should NOT be provisioned to secondary IS without group assignment");

        // Note: All groups are provisioned regardless of outboundProvisioningRoles.
        JSONObject secondaryGroups = secondaryScim2RestClient.filterGroups(
                buildEncodedDisplayNameFilter(REGULAR_GROUP));
        long totalResults = (long) secondaryGroups.get("totalResults");
        Assert.assertEquals(totalResults, 1L,
                "Regular group should be provisioned to secondary IS.");
    }

    @Test(alwaysRun = true, dependsOnMethods = "testUsersNotProvisionedWithoutGroupAssignment",
            description = "Assign users to the outbound provisioning group - users should be provisioned")
    public void testUsersProvisionedOnGroupAssignment() throws Exception {

        // Add user1 to the outbound provisioning group.
        String patchPayload1 = buildGroupPatchAddMemberPayload(primaryUser1Id, TEST_USER1_NAME);
        primaryScim2RestClient.patchGroupWithRawJSON(patchPayload1, outboundProvisioningGroupId);

        // Add user2 to the outbound provisioning group.
        String patchPayload2 = buildGroupPatchAddMemberPayload(primaryUser2Id, TEST_USER2_NAME);
        primaryScim2RestClient.patchGroupWithRawJSON(patchPayload2, outboundProvisioningGroupId);

        // Verify user1 was provisioned to the secondary IS.
        String secondaryUser1Id = getProvisionedUserId(secondaryScim2RestClient, TEST_USER1_NAME);
        Assert.assertNotNull(secondaryUser1Id,
                "User1 should be provisioned to secondary IS after being assigned to the provisioning group");

        // Verify user2 was provisioned to the secondary IS.
        String secondaryUser2Id = getProvisionedUserId(secondaryScim2RestClient, TEST_USER2_NAME);
        Assert.assertNotNull(secondaryUser2Id,
                "User2 should be provisioned to secondary IS after being assigned to the provisioning group");
    }

    @Test(alwaysRun = true, dependsOnMethods = "testUsersProvisionedOnGroupAssignment",
            description = "Unassign a user from the outbound provisioning group - user should be de-provisioned")
    public void testUserDeProvisionedOnGroupUnassignment() throws Exception {

        // Remove user1 from the outbound provisioning group.
        String patchPayload = buildGroupPatchRemoveMemberPayload(primaryUser1Id);
        primaryScim2RestClient.patchGroupWithRawJSON(patchPayload, outboundProvisioningGroupId);

        // Verify user1 was de-provisioned from the secondary IS.
        String secondaryUser1Id = getProvisionedUserId(secondaryScim2RestClient, TEST_USER1_NAME);
        Assert.assertNull(secondaryUser1Id,
                "User1 should be de-provisioned from secondary IS after being removed from the provisioning group");

        // Verify user2 is still provisioned on the secondary IS.
        String secondaryUser2Id = getProvisionedUserId(secondaryScim2RestClient, TEST_USER2_NAME);
        Assert.assertNotNull(secondaryUser2Id,
                "User2 should still be provisioned on secondary IS (not removed from group)");
    }

    @Test(alwaysRun = true, dependsOnMethods = "testUserDeProvisionedOnGroupUnassignment",
            description = "Create a secondary JDBC user store on the primary IS")
    public void testCreateSecondaryUserStore() throws Exception {

        // Create the H2 database for the secondary user store.
        // Use Utils.getResidentCarbonHome() (not FrameworkPathUtil.getCarbonHome()) to get the PRIMARY IS
        // carbon home. The secondary IS startup overwrites System.property("carbon.home") to the secondary
        // IS path, so FrameworkPathUtil.getCarbonHome() would return the wrong directory.
        // Utils.getResidentCarbonHome() caches the primary IS path on first call (before secondary IS starts).
        String carbonHome = Utils.getResidentCarbonHome();
        H2DataBaseManager dbManager = new H2DataBaseManager(
                "jdbc:h2:" + carbonHome + "/repository/database/" + USER_STORE_DB_NAME,
                DB_USER_NAME, DB_USER_PASSWORD);
        dbManager.executeUpdate(new File(carbonHome + "/dbscripts/h2.sql"));
        dbManager.disconnect();

        // Register the secondary user store.
        UserStoreReq userStore = new UserStoreReq()
                .typeId(USER_STORE_TYPE)
                .name(SECONDARY_DOMAIN)
                .addPropertiesItem(new UserStoreReq.Property().name("driverName").value("org.h2.Driver"))
                .addPropertiesItem(new UserStoreReq.Property().name("url")
                        .value("jdbc:h2:./repository/database/" + USER_STORE_DB_NAME))
                .addPropertiesItem(new UserStoreReq.Property().name("userName").value(DB_USER_NAME))
                .addPropertiesItem(new UserStoreReq.Property().name("password").value(DB_USER_PASSWORD))
                .addPropertiesItem(new UserStoreReq.Property().name("PasswordJavaRegEx")
                        .value("^[\\S]{5,30}$"))
                .addPropertiesItem(new UserStoreReq.Property().name("UsernameJavaRegEx")
                        .value("^[\\S]{5,30}$"))
                .addPropertiesItem(new UserStoreReq.Property().name("Disabled").value("false"))
                .addPropertiesItem(new UserStoreReq.Property().name("PasswordDigest").value("SHA-256"))
                .addPropertiesItem(new UserStoreReq.Property().name("StoreSaltedPassword").value("true"))
                .addPropertiesItem(new UserStoreReq.Property().name("SCIMEnabled").value("true"))
                .addPropertiesItem(new UserStoreReq.Property().name("UserIDEnabled").value("true"))
                .addPropertiesItem(new UserStoreReq.Property().name("GroupIDEnabled").value("true"));

        userStoreId = userStoreMgtRestClient.addUserStore(userStore);
        Thread.sleep(5000);
        boolean isDeployed = userStoreMgtRestClient.waitForUserStoreDeployment(SECONDARY_DOMAIN);
        Assert.assertTrue(isDeployed, "Secondary user store deployment failed");
    }

    @Test(alwaysRun = true, dependsOnMethods = "testCreateSecondaryUserStore",
            description = "Create a provisioning group in the secondary user store domain - " +
                    "group should be provisioned to PRIMARY user store on secondary IS")
    public void testCreateSecondaryDomainProvisioningGroup() throws Exception {

        // With scim2-user-store-domain=PRIMARY on the connector, this group should be provisioned 
        // to the PRIMARY user store on the secondary IS.
        String groupPayload = buildGroupPayloadNoMembers(SEC_US_DOMAIN_QUALIFIED_GROUP);
        secondaryOutboundProvisioningGroupId = primaryScim2RestClient.createGroupWithRawJSON(groupPayload);
        Assert.assertNotNull(secondaryOutboundProvisioningGroupId,
                "Secondary domain provisioning group creation on primary IS failed");

        JSONObject secondaryGroups = secondaryScim2RestClient.filterGroups(
                buildEncodedDisplayNameFilter(SEC_US_GROUP_NAME));
        long totalResults = (long) secondaryGroups.get("totalResults");
        Assert.assertEquals(totalResults, 1L,
                "Secondary domain provisioning group should be provisioned to secondary IS ");
    }

    @Test(alwaysRun = true, dependsOnMethods = "testCreateSecondaryDomainProvisioningGroup",
            description = "Update IdP outbound provisioning roles to include the secondary domain group")
    public void testUpdateIdPWithSecondaryOutboundProvisioningGroup() throws Exception {

        // Update the IdP to include both primary and secondary domain provisioning groups.
        Roles roles = new Roles();
        roles.setOutboundProvisioningRoles(
                Arrays.asList(OUTBOUND_PROVISIONING_GROUP, SEC_US_DOMAIN_QUALIFIED_GROUP));
        idpMgtRestClient.updateIdpRoles(idpId, roles);

        // Verify the IdP now has both outbound provisioning roles.
        JSONObject idpResponse = idpMgtRestClient.getIdentityProvider(idpId);
        JSONObject rolesJson = (JSONObject) idpResponse.get("roles");
        Assert.assertNotNull(rolesJson, "Roles configuration not found on IdP");
        JSONArray outboundProvisioningRoles = (JSONArray) rolesJson.get("outboundProvisioningRoles");
        Assert.assertNotNull(outboundProvisioningRoles, "outboundProvisioningRoles not found");
        Assert.assertEquals(outboundProvisioningRoles.size(), 2,
                "Expected exactly 2 outbound provisioning roles after update");
        Assert.assertTrue(outboundProvisioningRoles.contains(OUTBOUND_PROVISIONING_GROUP),
                "Primary domain provisioning group should be present");
        Assert.assertTrue(outboundProvisioningRoles.contains(SEC_US_DOMAIN_QUALIFIED_GROUP),
                "Secondary domain provisioning group should be present");
    }

    @Test(alwaysRun = true, dependsOnMethods = "testUpdateIdPWithSecondaryOutboundProvisioningGroup",
            description = "Create a user in the secondary US - user should NOT be provisioned without group assignment")
    public void testSecondaryUserNotProvisionedWithoutGroupAssignment() throws Exception {

        secondaryDomainUserId = primaryScim2RestClient.createUserWithRawJSON(
                buildUserPayload(SEC_US_DOMAIN_QUALIFIED_USER, SEC_US_USER_PASSWORD,
                        SEC_US_USER_GIVEN_NAME, SEC_US_USER_FAMILY_NAME,
                        SEC_US_USER_EMAIL));
        Assert.assertNotNull(secondaryDomainUserId, "Secondary domain user creation on primary IS failed");

        String provisionedUserId = getProvisionedUserId(secondaryScim2RestClient,
                SEC_US_USER_NAME);
        Assert.assertNull(provisionedUserId,
                "Secondary domain user should NOT be provisioned to secondary IS without group assignment");
    }

    @Test(alwaysRun = true, dependsOnMethods = "testSecondaryUserNotProvisionedWithoutGroupAssignment",
            description = "Assign secondary US user to the secondary provisioning group - user should be provisioned")
    public void testSecondaryUserProvisionedOnGroupAssignment() throws Exception {

        String patchPayload = buildGroupPatchAddMemberPayload(secondaryDomainUserId, SEC_US_DOMAIN_QUALIFIED_USER);
        primaryScim2RestClient.patchGroupWithRawJSON(patchPayload, secondaryOutboundProvisioningGroupId);

        String provisionedUserId = getProvisionedUserId(secondaryScim2RestClient,
                SEC_US_USER_NAME);
        Assert.assertNotNull(provisionedUserId,
                "Secondary domain user should be provisioned to secondary IS after being assigned to " +
                        "the secondary provisioning group");
    }

    @Test(alwaysRun = true, dependsOnMethods = "testSecondaryUserProvisionedOnGroupAssignment",
            description = "Unassign secondary US user from the secondary provisioning group " +
                    "- user should be de-provisioned")
    public void testSecondaryUserDeProvisionedOnGroupUnassignment() throws Exception {

        String patchPayload = buildGroupPatchRemoveMemberPayload(secondaryDomainUserId);
        primaryScim2RestClient.patchGroupWithRawJSON(patchPayload, secondaryOutboundProvisioningGroupId);

        String provisionedUserId = getProvisionedUserId(secondaryScim2RestClient,
                SEC_US_USER_NAME);
        Assert.assertNull(provisionedUserId,
                "Secondary domain user should be de-provisioned from secondary IS after being removed " +
                        "from the secondary provisioning group");
    }
}

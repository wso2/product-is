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
import org.wso2.identity.integration.common.clients.application.mgt.ApplicationManagementServiceClient;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;
import org.wso2.identity.integration.test.base.TestDataHolder;
import org.wso2.identity.integration.test.restclients.IdpMgtRestClient;
import org.wso2.identity.integration.test.restclients.SCIM2RestClient;

import java.util.Collections;

import static org.wso2.identity.integration.test.provisioning.OutboundProvisioningTestUtils.*;

/**
 * Tests group-based outbound provisioning via SCIM2.
 *
 * When an outbound provisioning group (configured via outboundProvisioningRoles in the IdP) is specified,
 * only users who are members of that group should be provisioned to the secondary IS. Users created outside
 * the group, and groups not matching the provisioning group, should not be provisioned.
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

    private static final String NON_PROVISIONING_GROUP = "regularGroup";

    private IdpMgtRestClient idpMgtRestClient;
    private ApplicationManagementServiceClient appMgtClient;
    private SCIM2RestClient primaryScim2RestClient;
    private SCIM2RestClient secondaryScim2RestClient;
    private TestDataHolder testDataHolder;

    private String idpId;
    private String outboundProvisioningGroupId;
    private String primaryUser1Id;
    private String primaryUser2Id;
    private String nonProvisioningGroupId;

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.init();

        testDataHolder = TestDataHolder.getInstance();

        idpMgtRestClient = new IdpMgtRestClient(serverURL, tenantInfo);
        primaryScim2RestClient = new SCIM2RestClient(serverURL, tenantInfo);
        secondaryScim2RestClient = new SCIM2RestClient(getSecondaryISURI(), tenantInfo);

        ConfigurationContext configContext = ConfigurationContextFactory
                .createConfigurationContextFromFileSystem(null, null);
        appMgtClient = new ApplicationManagementServiceClient(sessionCookie, backendURL, configContext);
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {

        // Clean up users on primary IS.
        if (primaryUser1Id != null) {
            primaryScim2RestClient.deleteUser(primaryUser1Id);
        }
        if (primaryUser2Id != null) {
            primaryScim2RestClient.deleteUser(primaryUser2Id);
        }

        // Clean up groups on primary IS.
        if (outboundProvisioningGroupId != null) {
            primaryScim2RestClient.deleteGroup(outboundProvisioningGroupId);
        }
        if (nonProvisioningGroupId != null) {
            primaryScim2RestClient.deleteGroup(nonProvisioningGroupId);
        }

        // Remove outbound provisioning from resident application first to prevent provisioning
        // events when deleting groups/users.
        clearResidentAppOutboundProvisioning(appMgtClient);

        if (idpId != null) {
            idpMgtRestClient.deleteIdp(idpId);
        }
        if (idpMgtRestClient != null) {
            idpMgtRestClient.closeHttpClient();
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

        // Verify the IdP was created with the correct outbound provisioning roles.
        JSONObject idpResponse = idpMgtRestClient.getIdentityProvider(idpId);
        Assert.assertNotNull(idpResponse, "Failed to retrieve created Identity Provider");
        Assert.assertEquals(idpResponse.get("name"), IDP_NAME, "IdP name mismatch");

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
            description = "Create users and a non-provisioning group - users should NOT be provisioned, group should be provisioned")
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

        // Create a non-provisioning group (different from the outbound provisioning group).
        String regularGroupPayload = buildGroupPayloadNoMembers(NON_PROVISIONING_GROUP);
        nonProvisioningGroupId = primaryScim2RestClient.createGroupWithRawJSON(regularGroupPayload);
        Assert.assertNotNull(nonProvisioningGroupId, "Non-provisioning group creation on primary IS failed");

        // Verify users were NOT provisioned to the secondary IS.
        String secondaryUser1Id = getProvisionedUserId(secondaryScim2RestClient, TEST_USER1_NAME);
        Assert.assertNull(secondaryUser1Id,
                "User1 should NOT be provisioned to secondary IS without group assignment");

        String secondaryUser2Id = getProvisionedUserId(secondaryScim2RestClient, TEST_USER2_NAME);
        Assert.assertNull(secondaryUser2Id,
                "User2 should NOT be provisioned to secondary IS without group assignment");

        // Verify the non-provisioning group WAS provisioned to the secondary IS.
        // Note: All groups are provisioned regardless of outboundProvisioningRoles.
        // Only user provisioning is filtered based on group membership.
        JSONObject secondaryGroups = secondaryScim2RestClient.filterGroups(
                buildEncodedDisplayNameFilter(NON_PROVISIONING_GROUP));
        long totalResults = (long) secondaryGroups.get("totalResults");
        Assert.assertEquals(totalResults, 1L,
                "Non-provisioning group should be provisioned to secondary IS (groups are always provisioned)");
    }

    @Test(alwaysRun = true, dependsOnMethods = "testUsersNotProvisionedWithoutGroupAssignment",
            description = "Assign users to the outbound provisioning group - users should be provisioned")
    public void testUsersProvisionedOnGroupAssignment() throws Exception {

        // Debug: Verify IdP configuration before patching
        JSONObject idpConfig = idpMgtRestClient.getIdentityProvider(idpId);
        JSONObject roles = (JSONObject) idpConfig.get("roles");
        System.out.println("DEBUG: IdP outboundProvisioningRoles: " +
                (roles != null ? roles.get("outboundProvisioningRoles") : "null"));

        // Add user1 to the outbound provisioning group.
        System.out.println("DEBUG: Adding user1 (ID: " + primaryUser1Id + ") to provisioning group (ID: " +
                outboundProvisioningGroupId + ")");
        String patchPayload1 = buildGroupPatchAddMemberPayload(primaryUser1Id, TEST_USER1_NAME);
        System.out.println("DEBUG: Patch payload for user1: " + patchPayload1);
        primaryScim2RestClient.patchGroupWithRawJSON(patchPayload1, outboundProvisioningGroupId);

        // Add user2 to the outbound provisioning group.
        System.out.println("DEBUG: Adding user2 (ID: " + primaryUser2Id + ") to provisioning group");
        String patchPayload2 = buildGroupPatchAddMemberPayload(primaryUser2Id, TEST_USER2_NAME);
        primaryScim2RestClient.patchGroupWithRawJSON(patchPayload2, outboundProvisioningGroupId);

        // Debug: Check all groups on secondary IS to see what was provisioned
        JSONObject secondaryGroups = secondaryScim2RestClient.getGroups();
        System.out.println("DEBUG: All groups on secondary IS (total=" +
                secondaryGroups.get("totalResults") + "): " + secondaryGroups);

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
}

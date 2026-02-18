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
import org.wso2.carbon.automation.engine.context.beans.User;
import org.wso2.carbon.identity.application.common.model.xsd.InboundProvisioningConfig;
import org.wso2.carbon.identity.application.common.model.xsd.JustInTimeProvisioningConfig;
import org.wso2.carbon.identity.application.common.model.xsd.OutboundProvisioningConfig;
import org.wso2.carbon.identity.application.common.model.xsd.ProvisioningConnectorConfig;
import org.wso2.carbon.identity.application.common.model.xsd.ServiceProvider;
import org.wso2.identity.integration.common.clients.application.mgt.ApplicationManagementServiceClient;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;
import org.wso2.identity.integration.test.base.TestDataHolder;
import org.wso2.identity.integration.test.rest.api.server.idp.v1.model.IdentityProviderPOSTRequest;
import org.wso2.identity.integration.test.rest.api.server.idp.v1.model.Property;
import org.wso2.identity.integration.test.rest.api.server.idp.v1.model.ProvisioningRequest;
import org.wso2.identity.integration.test.restclients.IdpMgtRestClient;
import org.wso2.identity.integration.test.restclients.SCIM2RestClient;
import org.wso2.identity.integration.test.utils.CommonConstants;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.xpath.XPathExpressionException;

public class OutboundProvisioningSCIM2TestCase extends ISIntegrationTest {

    private static final String IDP_NAME = "outbound-provisioning-connection";
    private static final String IDP_DESCRIPTION = "SCIM outbound provisioning connection";
    private static final String IDP_IMAGE = "assets/images/logos/outbound-provisioning.svg";
    private static final String SCIM2_CONNECTOR_ID = "U0NJTTI";
    private static final String SCIM2_CONNECTOR_NAME = "SCIM2";
    private static final String RESIDENT_SP_NAME = "wso2carbon-local-sp";

    private static final int DEFAULT_PORT = CommonConstants.IS_DEFAULT_HTTPS_PORT;
    private static final int PORT_OFFSET_1 = 1;
    private static final String HTTPS_LOCALHOST_SERVICES = "https://localhost:%s/";
    private static final String SCIM2_USER_ENDPOINT_PATH = "scim2/Users";
    private static final String SCIM2_GROUP_ENDPOINT_PATH = "scim2/Groups";

    private static final String TEST_USER_NAME = "john.doe";
    private static final String TEST_USER_PASSWORD = "Wso2@test";
    private static final String TEST_USER_GIVEN_NAME = "John";
    private static final String TEST_USER_FAMILY_NAME = "Doe";
    private static final String TEST_USER_PRIMARY_EMAIL = "john.doe@example.com";
    private static final String TEST_USER_WORK_EMAIL = "john.doe@workplace.com";
    private static final String TEST_USER_MOBILE = "0222222222";
    private static final String TEST_USER_DEPARTMENT = "Engineering";
    private static final String TEST_USER_MANAGER_DISPLAY_NAME = "Jane Smith";

    private static final String PATCHED_USER_GIVEN_NAME = "Jonathan";
    private static final String PATCHED_USER_MOBILE = "0333333333";
    private static final String PATCHED_USER_DEPARTMENT = "Product Development";
    private static final String PATCHED_USER_MANAGER_DISPLAY_NAME = "Robert Johnson";

    private static final String TEST_USER2_NAME = "alice.smith";
    private static final String TEST_USER2_PASSWORD = "Wso2@test";
    private static final String TEST_USER2_GIVEN_NAME = "Alice";
    private static final String TEST_USER2_FAMILY_NAME = "Smith";
    private static final String TEST_USER2_EMAIL = "alice.smith@example.com";

    private static final String TEST_USER3_NAME = "bob.jones";
    private static final String TEST_USER3_PASSWORD = "Wso2@test";
    private static final String TEST_USER3_GIVEN_NAME = "Bob";
    private static final String TEST_USER3_FAMILY_NAME = "Jones";
    private static final String TEST_USER3_EMAIL = "bob.jones@example.com";

    private static final String TEST_GROUP_DISPLAY_NAME = "EngineeringTeam";
    private static final String PATCHED_GROUP_DISPLAY_NAME = "ProductEngineeringTeam";

    private IdpMgtRestClient idpMgtRestClient;
    private ApplicationManagementServiceClient appMgtClient;
    private SCIM2RestClient primaryScim2RestClient;
    private SCIM2RestClient secondaryScim2RestClient;
    private TestDataHolder testDataHolder;

    private String idpId;
    private String primaryUserId;
    private String secondaryUserId;
    private String primaryUser2Id;
    private String secondaryUser2Id;
    private String primaryUser3Id;
    private String secondaryUser3Id;
    private String primaryGroupId;
    private String secondaryGroupId;

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

        // Clean up the created group on the primary IS.
        if (primaryGroupId != null) {
            primaryScim2RestClient.deleteGroup(primaryGroupId);
        }

        // Clean up the created users on the primary IS.
        if (primaryUserId != null) {
            primaryScim2RestClient.deleteUser(primaryUserId);
        }
        if (primaryUser2Id != null) {
            primaryScim2RestClient.deleteUser(primaryUser2Id);
        }
        if (primaryUser3Id != null) {
            primaryScim2RestClient.deleteUser(primaryUser3Id);
        }

        // Remove outbound provisioning from resident application before deleting the IdP.
        clearResidentAppOutboundProvisioning();

        if (idpId != null) {
            idpMgtRestClient.deleteIdp(idpId);
        }
        if (idpMgtRestClient != null) {
            idpMgtRestClient.closeHttpClient();
        }
        if (primaryScim2RestClient != null) {
            primaryScim2RestClient.closeHttpClient();
        }
        if (secondaryScim2RestClient != null) {
            secondaryScim2RestClient.closeHttpClient();
        }
    }

    @Test(alwaysRun = true, description = "Verify outbound provisioning IdP was created correctly")
    public void testVerifyIdPCreation() throws Exception {

        // Create the outbound provisioning IdP connection
        idpId = createOutboundProvisioningIdP();
        Assert.assertNotNull(idpId, "Identity Provider creation failed - returned null ID");

        JSONObject idpResponse = idpMgtRestClient.getIdentityProvider(idpId);
        Assert.assertNotNull(idpResponse, "Failed to retrieve created Identity Provider");

        // Verify basic IdP properties
        Assert.assertEquals(idpResponse.get("name"), IDP_NAME,
                "IdP name mismatch");
        Assert.assertEquals(idpResponse.get("description"), IDP_DESCRIPTION,
                "IdP description mismatch");
        Assert.assertEquals(idpResponse.get("image"), IDP_IMAGE,
                "IdP image mismatch");
        Assert.assertEquals(idpResponse.get("isPrimary"), false,
                "IdP isPrimary should be false");
        Assert.assertEquals(idpResponse.get("isFederationHub"), false,
                "IdP isFederationHub should be false");

        // Verify provisioning configuration
        JSONObject provisioning = (JSONObject) idpResponse.get("provisioning");
        Assert.assertNotNull(provisioning, "Provisioning configuration not found");

        JSONObject outboundConnectors = (JSONObject) provisioning.get("outboundConnectors");
        Assert.assertNotNull(outboundConnectors, "Outbound connectors not found");

        Assert.assertEquals(outboundConnectors.get("defaultConnectorId"), SCIM2_CONNECTOR_ID,
                "Default connector ID mismatch");

        JSONArray connectors = (JSONArray) outboundConnectors.get("connectors");
        Assert.assertNotNull(connectors, "Connectors array not found");
        Assert.assertEquals(connectors.size(), 1, "Expected exactly 1 connector");

        JSONObject connectorSummary = (JSONObject) connectors.get(0);
        Assert.assertEquals(connectorSummary.get("connectorId"), SCIM2_CONNECTOR_ID,
                "SCIM2 connector ID mismatch");
        Assert.assertEquals(connectorSummary.get("name"), SCIM2_CONNECTOR_NAME,
                "SCIM2 connector name mismatch");
        Assert.assertEquals(connectorSummary.get("isEnabled"), true,
                "SCIM2 connector should be enabled");

        // Fetch full connector details (properties require a separate API call)
        JSONObject scim2Connector = idpMgtRestClient.getIdpOutboundConnector(idpId, SCIM2_CONNECTOR_ID);
        Assert.assertNotNull(scim2Connector, "Failed to retrieve SCIM2 outbound connector details");

        Assert.assertEquals(scim2Connector.get("connectorId"), SCIM2_CONNECTOR_ID,
                "Connector detail - connectorId mismatch");
        Assert.assertEquals(scim2Connector.get("isEnabled"), true,
                "Connector detail - should be enabled");

        // Verify SCIM2 connector properties (skip secrets like password as they may be masked)
        JSONArray properties = (JSONArray) scim2Connector.get("properties");
        Assert.assertNotNull(properties, "SCIM2 connector properties not found");

        Map<String, String> propertyMap = new HashMap<>();
        for (Object prop : properties) {
            JSONObject property = (JSONObject) prop;
            propertyMap.put((String) property.get("key"), (String) property.get("value"));
        }

        Assert.assertEquals(propertyMap.get("scim2-user-ep"), getTargetScim2UserEndpoint(),
                "User endpoint mismatch");
        Assert.assertEquals(propertyMap.get("scim2-group-ep"), getTargetScim2GroupEndpoint(),
                "Group endpoint mismatch");
        Assert.assertEquals(propertyMap.get("scim2-user-store-domain"), "PRIMARY",
                "User store domain mismatch");
        Assert.assertEquals(propertyMap.get("scim2-enable-pwd-provisioning"), "true",
                "Password provisioning mismatch");
        Assert.assertEquals(propertyMap.get("scim2-authentication-mode"), "basic",
                "Authentication mode mismatch");
    }

    @Test(alwaysRun = true, dependsOnMethods = "testVerifyIdPCreation",
            description = "Enable outbound provisioning on the resident application")
    public void testEnableOutboundProvisioningOnResidentApp() throws Exception {

        enableResidentAppOutboundProvisioning();
    }

    @Test(alwaysRun = true, dependsOnMethods = "testEnableOutboundProvisioningOnResidentApp",
            description = "Test outbound provisioning of user creation via SCIM2")
    public void testOutboundProvisioningUserCreation() throws Exception {

        String userPayload = buildTestUserPayload();
        primaryUserId = primaryScim2RestClient.createUserWithRawJSON(userPayload);
        Assert.assertNotNull(primaryUserId, "User creation on primary IS failed");

        // Verify user was created on the primary IS.
        JSONObject primaryUser = primaryScim2RestClient.getUser(primaryUserId, null);
        Assert.assertNotNull(primaryUser, "Failed to retrieve created user from primary IS");
        Assert.assertEquals(primaryUser.get("userName"), TEST_USER_NAME,
                "Username mismatch on primary IS");

        // Verify the user was provisioned to the secondary IS via outbound provisioning.
        JSONObject secondaryUsers = secondaryScim2RestClient.filterUsers(buildEncodedUserNameFilter(TEST_USER_NAME));
        Assert.assertNotNull(secondaryUsers, "Failed to query users on secondary IS");

        long totalResults = (long) secondaryUsers.get("totalResults");
        Assert.assertEquals(totalResults, 1L,
                "Expected exactly 1 provisioned user on secondary IS, found: " + totalResults);

        JSONArray resources = (JSONArray) secondaryUsers.get("Resources");
        Assert.assertNotNull(resources, "Resources array not found in secondary IS response");
        JSONObject provisionedUser = (JSONObject) resources.get(0);

        secondaryUserId = (String) provisionedUser.get("id");
        Assert.assertNotNull(secondaryUserId, "Provisioned user ID is null on secondary IS");

        Assert.assertEquals(provisionedUser.get("userName"), TEST_USER_NAME,
                "Username mismatch on secondary IS");

        // Verify name attributes were provisioned.
        JSONObject name = (JSONObject) provisionedUser.get("name");
        Assert.assertNotNull(name, "Name not found on provisioned user");
        Assert.assertEquals(name.get("givenName"), TEST_USER_GIVEN_NAME,
                "Given name mismatch on secondary IS");
        Assert.assertEquals(name.get("familyName"), TEST_USER_FAMILY_NAME,
                "Family name mismatch on secondary IS");

        // Verify emails were provisioned.
        JSONArray emails = (JSONArray) provisionedUser.get("emails");
        Assert.assertNotNull(emails, "Emails not found on provisioned user");
        Assert.assertTrue(emails.size() >= 1, "Expected at least 1 email on provisioned user");
    }

    @Test(alwaysRun = true, dependsOnMethods = "testOutboundProvisioningUserCreation",
            description = "Test outbound provisioning of user PATCH via SCIM2")
    public void testOutboundProvisioningUserPatch() throws Exception {

        String patchPayload = buildPatchUserPayload();
        primaryScim2RestClient.patchUserWithRawJSON(patchPayload, primaryUserId);

        // Verify the patch was applied on the primary IS.
        JSONObject primaryUser = primaryScim2RestClient.getUser(primaryUserId, null);
        Assert.assertNotNull(primaryUser, "Failed to retrieve patched user from primary IS");

        // Verify patched attributes on primary IS.
        JSONObject primaryName = (JSONObject) primaryUser.get("name");
        Assert.assertEquals(primaryName.get("givenName"), PATCHED_USER_GIVEN_NAME,
                "Given name not patched on primary IS");
        // Verify familyName remains unchanged.
        Assert.assertEquals(primaryName.get("familyName"), TEST_USER_FAMILY_NAME,
                "Family name should remain unchanged on primary IS after patch");

        // Verify the patch was provisioned to the secondary IS.
        JSONObject secondaryUser = secondaryScim2RestClient.getUser(secondaryUserId, null);
        Assert.assertNotNull(secondaryUser, "Failed to retrieve user from secondary IS after patch");

        // Verify patched attributes on secondary IS.
        JSONObject secondaryName = (JSONObject) secondaryUser.get("name");
        Assert.assertNotNull(secondaryName, "Name not found on secondary IS after patch");
        Assert.assertEquals(secondaryName.get("givenName"), PATCHED_USER_GIVEN_NAME,
                "Given name not patched on secondary IS");
        Assert.assertEquals(secondaryName.get("familyName"), TEST_USER_FAMILY_NAME,
                "Family name should remain unchanged on secondary IS after patch");

        // Verify patched phone number on secondary IS.
        JSONArray secondaryPhones = (JSONArray) secondaryUser.get("phoneNumbers");
        Assert.assertNotNull(secondaryPhones, "Phone numbers not found on secondary IS after patch");
        String mobileValue = getAttributeValueByType(secondaryPhones, "mobile");
        Assert.assertEquals(mobileValue, PATCHED_USER_MOBILE,
                "Mobile number not patched on secondary IS");

        // Verify patched enterprise extension attributes on secondary IS.
        JSONObject secondaryEnterprise = (JSONObject) secondaryUser.get(
                "urn:ietf:params:scim:schemas:extension:enterprise:2.0:User");
        Assert.assertNotNull(secondaryEnterprise,
                "Enterprise extension not found on secondary IS after patch");
        Assert.assertEquals(secondaryEnterprise.get("department"), PATCHED_USER_DEPARTMENT,
                "Department not patched on secondary IS");
        JSONObject secondaryManager = (JSONObject) secondaryEnterprise.get("manager");
        Assert.assertNotNull(secondaryManager, "Manager not found on secondary IS after patch");
        Assert.assertEquals(secondaryManager.get("displayName"), PATCHED_USER_MANAGER_DISPLAY_NAME,
                "Manager displayName not patched on secondary IS");

        // Verify unchanged attributes on secondary IS.
        Assert.assertEquals(secondaryUser.get("userName"), TEST_USER_NAME,
                "Username should remain unchanged on secondary IS after patch");

        JSONArray secondaryEmails = (JSONArray) secondaryUser.get("emails");
        Assert.assertNotNull(secondaryEmails, "Emails should remain on secondary IS after patch");
        Assert.assertTrue(secondaryEmails.size() >= 1,
                "Expected at least 1 email on secondary IS after patch");
    }

    @Test(alwaysRun = true, dependsOnMethods = "testOutboundProvisioningUserPatch",
            description = "Test outbound provisioning of user DELETE via SCIM2")
    public void testOutboundProvisioningUserDeletion() throws Exception {

        // Delete the user on the primary IS.
        primaryScim2RestClient.deleteUser(primaryUserId);

        // Verify the user is deleted from the primary IS.
        JSONObject primaryUser = primaryScim2RestClient.getUser(primaryUserId, null);
        // A GET on a deleted user should return an error response with a non-null detail or status.
        Assert.assertNotNull(primaryUser, "Expected a response when querying deleted user on primary IS");
        // The response should not contain the userName of the deleted user (indicating it's gone).
        Assert.assertNotEquals(primaryUser.get("userName"), TEST_USER_NAME,
                "User should no longer exist on primary IS after deletion");

        // Verify the user was de-provisioned from the secondary IS.
        JSONObject secondaryUsers = secondaryScim2RestClient.filterUsers(
                buildEncodedUserNameFilter(TEST_USER_NAME));
        Assert.assertNotNull(secondaryUsers, "Failed to query users on secondary IS after deletion");

        long totalResults = (long) secondaryUsers.get("totalResults");
        Assert.assertEquals(totalResults, 0L,
                "User should be de-provisioned from secondary IS after deletion, but found: " + totalResults);

        // Set to null so cleanup in @AfterClass doesn't try to delete again.
        primaryUserId = null;
    }

    @Test(alwaysRun = true, dependsOnMethods = "testOutboundProvisioningUserDeletion",
            description = "Test outbound provisioning of group creation via SCIM2")
    public void testOutboundProvisioningGroupCreation() throws Exception {

        // Create two users that will be members of the group.
        primaryUser2Id = primaryScim2RestClient.createUserWithRawJSON(buildUserPayload(
                TEST_USER2_NAME, TEST_USER2_PASSWORD, TEST_USER2_GIVEN_NAME, TEST_USER2_FAMILY_NAME,
                TEST_USER2_EMAIL));
        Assert.assertNotNull(primaryUser2Id, "User2 creation on primary IS failed");

        primaryUser3Id = primaryScim2RestClient.createUserWithRawJSON(buildUserPayload(
                TEST_USER3_NAME, TEST_USER3_PASSWORD, TEST_USER3_GIVEN_NAME, TEST_USER3_FAMILY_NAME,
                TEST_USER3_EMAIL));
        Assert.assertNotNull(primaryUser3Id, "User3 creation on primary IS failed");

        // Look up the provisioned user IDs on the secondary IS.
        secondaryUser2Id = getProvisionedUserId(TEST_USER2_NAME);
        Assert.assertNotNull(secondaryUser2Id, "User2 was not provisioned to secondary IS");

        secondaryUser3Id = getProvisionedUserId(TEST_USER3_NAME);
        Assert.assertNotNull(secondaryUser3Id, "User3 was not provisioned to secondary IS");

        // Create the group with user2 as the initial member.
        String groupPayload = buildGroupPayload(TEST_GROUP_DISPLAY_NAME, primaryUser2Id, TEST_USER2_NAME);
        primaryGroupId = primaryScim2RestClient.createGroupWithRawJSON(groupPayload);
        Assert.assertNotNull(primaryGroupId, "Group creation on primary IS failed");

        // Verify the group was provisioned to the secondary IS.
        JSONObject secondaryGroups = secondaryScim2RestClient.filterGroups(
                buildEncodedDisplayNameFilter(TEST_GROUP_DISPLAY_NAME));
        Assert.assertNotNull(secondaryGroups, "Failed to query groups on secondary IS");

        long totalResults = (long) secondaryGroups.get("totalResults");
        Assert.assertEquals(totalResults, 1L,
                "Expected exactly 1 provisioned group on secondary IS, found: " + totalResults);

        JSONArray resources = (JSONArray) secondaryGroups.get("Resources");
        Assert.assertNotNull(resources, "Resources array not found in secondary IS group response");
        JSONObject provisionedGroup = (JSONObject) resources.get(0);

        secondaryGroupId = (String) provisionedGroup.get("id");
        Assert.assertNotNull(secondaryGroupId, "Provisioned group ID is null on secondary IS");

        // Verify group displayName.
        String actualDisplayName = (String) provisionedGroup.get("displayName");
        Assert.assertEquals(actualDisplayName, TEST_GROUP_DISPLAY_NAME,
                "Group displayName mismatch on secondary IS");

        // Verify member was provisioned.
        JSONArray members = (JSONArray) provisionedGroup.get("members");
        Assert.assertNotNull(members, "Members not found on provisioned group");
        Assert.assertEquals(members.size(), 1, "Expected 1 member in provisioned group, found: " + members.size());

        List<String> memberDisplayNames = extractMemberDisplayNames(members);
        Assert.assertTrue(memberDisplayNames.contains(TEST_USER2_NAME),
                "Member '" + TEST_USER2_NAME + "' not found in provisioned group. Members: " + memberDisplayNames);
    }

    @Test(alwaysRun = true, dependsOnMethods = "testOutboundProvisioningGroupCreation",
            description = "Test outbound provisioning of group PATCH (add member) via SCIM2")
    public void testOutboundProvisioningGroupPatchAddMember() throws Exception {

        String patchPayload = buildGroupPatchAddMemberPayload(primaryUser3Id, TEST_USER3_NAME);
        primaryScim2RestClient.patchGroupWithRawJSON(patchPayload, primaryGroupId);

        // Verify the member addition was provisioned to the secondary IS.
        JSONObject secondaryGroups = secondaryScim2RestClient.filterGroups(
                buildEncodedDisplayNameFilter(TEST_GROUP_DISPLAY_NAME));
        Assert.assertNotNull(secondaryGroups, "Failed to query groups on secondary IS after adding member");

        long totalResults = (long) secondaryGroups.get("totalResults");
        Assert.assertEquals(totalResults, 1L,
                "Group should exist on secondary IS after adding member");

        JSONArray resources = (JSONArray) secondaryGroups.get("Resources");
        JSONObject patchedGroup = (JSONObject) resources.get(0);

        // Verify both members are now present.
        JSONArray members = (JSONArray) patchedGroup.get("members");
        Assert.assertNotNull(members, "Members not found after adding member");
        Assert.assertEquals(members.size(), 2,
                "Expected 2 members after adding a member, found: " + members.size());

        List<String> memberDisplayNames = extractMemberDisplayNames(members);
        Assert.assertTrue(memberDisplayNames.contains(TEST_USER2_NAME),
                "Original member '" + TEST_USER2_NAME + "' not found. Members: " + memberDisplayNames);
        Assert.assertTrue(memberDisplayNames.contains(TEST_USER3_NAME),
                "Added member '" + TEST_USER3_NAME + "' not found. Members: " + memberDisplayNames);
    }

    @Test(alwaysRun = true, dependsOnMethods = "testOutboundProvisioningGroupPatchAddMember",
            description = "Test outbound provisioning of group PATCH (remove member) via SCIM2")
    public void testOutboundProvisioningGroupPatchRemoveMember() throws Exception {

        String patchPayload = buildGroupPatchRemoveMemberPayload(primaryUser3Id);
        primaryScim2RestClient.patchGroupWithRawJSON(patchPayload, primaryGroupId);

        // Verify the member removal was provisioned to the secondary IS.
        JSONObject secondaryGroups = secondaryScim2RestClient.filterGroups(
                buildEncodedDisplayNameFilter(TEST_GROUP_DISPLAY_NAME));
        Assert.assertNotNull(secondaryGroups, "Failed to query groups on secondary IS after member removal");

        long totalResults = (long) secondaryGroups.get("totalResults");
        Assert.assertEquals(totalResults, 1L,
                "Group should still exist on secondary IS after member removal");

        JSONArray resources = (JSONArray) secondaryGroups.get("Resources");
        JSONObject patchedGroup = (JSONObject) resources.get(0);

        // Verify only one member remains.
        JSONArray members = (JSONArray) patchedGroup.get("members");
        Assert.assertNotNull(members, "Members should not be null after removing one member");
        Assert.assertEquals(members.size(), 1,
                "Expected 1 member after removing a member, found: " + members.size());

        List<String> memberDisplayNames = extractMemberDisplayNames(members);
        Assert.assertTrue(memberDisplayNames.contains(TEST_USER2_NAME),
                "Remaining member should be '" + TEST_USER2_NAME + "'. Members: " + memberDisplayNames);
        Assert.assertFalse(memberDisplayNames.contains(TEST_USER3_NAME),
                "Removed member '" + TEST_USER3_NAME + "' should not be in group. Members: " + memberDisplayNames);
    }

    @Test(alwaysRun = true, dependsOnMethods = "testOutboundProvisioningGroupPatchRemoveMember",
            description = "Test outbound provisioning of group PATCH (display name change) via SCIM2")
    public void testOutboundProvisioningGroupPatchDisplayName() throws Exception {

        String patchPayload = buildGroupPatchDisplayNamePayload(PATCHED_GROUP_DISPLAY_NAME);
        primaryScim2RestClient.patchGroupWithRawJSON(patchPayload, primaryGroupId);

        // Verify the patch was provisioned to the secondary IS.
        // The old display name should no longer match.
        JSONObject oldNameResult = secondaryScim2RestClient.filterGroups(
                buildEncodedDisplayNameFilter(TEST_GROUP_DISPLAY_NAME));
        long oldCount = (long) oldNameResult.get("totalResults");
        Assert.assertEquals(oldCount, 0L,
                "Old group name should not exist on secondary IS after display name patch");

        // The new display name should exist.
        JSONObject newNameResult = secondaryScim2RestClient.filterGroups(
                buildEncodedDisplayNameFilter(PATCHED_GROUP_DISPLAY_NAME));
        long newCount = (long) newNameResult.get("totalResults");
        Assert.assertEquals(newCount, 1L,
                "Patched group not found on secondary IS with new display name");

        JSONArray resources = (JSONArray) newNameResult.get("Resources");
        JSONObject patchedGroup = (JSONObject) resources.get(0);

        // Update the secondary group ID since the group may have been re-created.
        secondaryGroupId = (String) patchedGroup.get("id");

        // Verify members are still present after display name change.
        JSONArray members = (JSONArray) patchedGroup.get("members");
        Assert.assertNotNull(members, "Members lost after group display name patch");
        Assert.assertEquals(members.size(), 1,
                "Expected 1 member after display name patch, found: " + members.size());
    }

    @Test(alwaysRun = true, dependsOnMethods = "testOutboundProvisioningGroupPatchDisplayName",
            description = "Test outbound provisioning of group DELETE via SCIM2")
    public void testOutboundProvisioningGroupDeletion() throws Exception {

        // Delete the group on the primary IS.
        primaryScim2RestClient.deleteGroup(primaryGroupId);

        // Verify the group was de-provisioned from the secondary IS.
        JSONObject secondaryGroups = secondaryScim2RestClient.filterGroups(
                buildEncodedDisplayNameFilter(PATCHED_GROUP_DISPLAY_NAME));
        Assert.assertNotNull(secondaryGroups, "Failed to query groups on secondary IS after deletion");

        long totalResults = (long) secondaryGroups.get("totalResults");
        Assert.assertEquals(totalResults, 0L,
                "Group should be de-provisioned from secondary IS after deletion, but found: " + totalResults);

        // Set to null so cleanup in @AfterClass doesn't try to delete again.
        primaryGroupId = null;
    }

    private String buildPatchUserPayload() {

        JSONObject payload = new JSONObject();

        JSONArray schemas = new JSONArray();
        schemas.add("urn:ietf:params:scim:api:messages:2.0:PatchOp");
        payload.put("schemas", schemas);

        JSONArray operations = new JSONArray();

        // Operation 1: Replace givenName and mobile phone.
        JSONObject op1 = new JSONObject();
        op1.put("op", "replace");

        JSONObject op1Value = new JSONObject();

        JSONObject nameUpdate = new JSONObject();
        nameUpdate.put("givenName", PATCHED_USER_GIVEN_NAME);
        op1Value.put("name", nameUpdate);

        JSONObject mobilePhone = new JSONObject();
        mobilePhone.put("type", "mobile");
        mobilePhone.put("value", PATCHED_USER_MOBILE);
        JSONArray phoneNumbers = new JSONArray();
        phoneNumbers.add(mobilePhone);
        op1Value.put("phoneNumbers", phoneNumbers);

        op1.put("value", op1Value);
        operations.add(op1);

        // Operation 2: Replace enterprise extension attributes.
        JSONObject op2 = new JSONObject();
        op2.put("op", "replace");

        JSONObject op2Value = new JSONObject();
        JSONObject enterpriseExtension = new JSONObject();
        enterpriseExtension.put("department", PATCHED_USER_DEPARTMENT);

        JSONObject manager = new JSONObject();
        manager.put("displayName", PATCHED_USER_MANAGER_DISPLAY_NAME);
        enterpriseExtension.put("manager", manager);

        op2Value.put("urn:ietf:params:scim:schemas:extension:enterprise:2.0:User", enterpriseExtension);

        op2.put("value", op2Value);
        operations.add(op2);

        payload.put("Operations", operations);

        return payload.toJSONString();
    }

    /**
     * Get the value of a multi-valued attribute entry by its type.
     *
     * @param multiValuedAttr JSON array of multi-valued attribute entries.
     * @param type            the type to look for (e.g., "mobile", "work").
     * @return the value string, or null if not found.
     */
    private String getAttributeValueByType(JSONArray multiValuedAttr, String type) {

        for (Object item : multiValuedAttr) {
            JSONObject entry = (JSONObject) item;
            if (type.equals(entry.get("type"))) {
                return (String) entry.get("value");
            }
        }
        return null;
    }

    private String buildTestUserPayload() {

        JSONObject payload = new JSONObject();

        // Name
        JSONObject name = new JSONObject();
        name.put("familyName", TEST_USER_FAMILY_NAME);
        name.put("givenName", TEST_USER_GIVEN_NAME);
        payload.put("name", name);

        payload.put("userName", TEST_USER_NAME);
        payload.put("password", TEST_USER_PASSWORD);

        // Emails (primary + work)
        JSONObject primaryEmail = new JSONObject();
        primaryEmail.put("value", TEST_USER_PRIMARY_EMAIL);
        primaryEmail.put("primary", true);

        JSONObject workEmail = new JSONObject();
        workEmail.put("type", "work");
        workEmail.put("value", TEST_USER_WORK_EMAIL);

        JSONArray emails = new JSONArray();
        emails.add(primaryEmail);
        emails.add(workEmail);
        payload.put("emails", emails);

        // Phone numbers
        JSONObject mobilePhone = new JSONObject();
        mobilePhone.put("type", "mobile");
        mobilePhone.put("value", TEST_USER_MOBILE);

        JSONArray phoneNumbers = new JSONArray();
        phoneNumbers.add(mobilePhone);
        payload.put("phoneNumbers", phoneNumbers);

        // Enterprise extension
        JSONObject manager = new JSONObject();
        manager.put("displayName", TEST_USER_MANAGER_DISPLAY_NAME);

        JSONObject enterpriseExtension = new JSONObject();
        enterpriseExtension.put("department", TEST_USER_DEPARTMENT);
        enterpriseExtension.put("manager", manager);
        payload.put("urn:ietf:params:scim:schemas:extension:enterprise:2.0:User", enterpriseExtension);

        return payload.toJSONString();
    }

    private void enableResidentAppOutboundProvisioning() throws Exception {

        ServiceProvider serviceProvider = appMgtClient.getApplication(RESIDENT_SP_NAME);
        if (serviceProvider == null) {
            serviceProvider = new ServiceProvider();
            serviceProvider.setApplicationName(RESIDENT_SP_NAME);
            try {
                appMgtClient.createApplication(serviceProvider);
                serviceProvider = appMgtClient.getApplication(RESIDENT_SP_NAME);
            } catch (Exception error) {
                throw new Exception("Error occurred during obtaining applicationManagementServiceClients", error);
            }

        }
        Assert.assertNotNull(serviceProvider, "Resident service provider not found");

        InboundProvisioningConfig inboundProConfig = new InboundProvisioningConfig();
        inboundProConfig.setProvisioningUserStore("");
        serviceProvider.setInboundProvisioningConfig(inboundProConfig);

        org.wso2.carbon.identity.application.common.model.xsd.IdentityProvider proIdp =
                new org.wso2.carbon.identity.application.common.model.xsd.IdentityProvider();
        proIdp.setIdentityProviderName(IDP_NAME);

        JustInTimeProvisioningConfig jitConfig = new JustInTimeProvisioningConfig();
        jitConfig.setProvisioningEnabled(true);
        proIdp.setJustInTimeProvisioningConfig(jitConfig);

        ProvisioningConnectorConfig proCon = new ProvisioningConnectorConfig();
        proCon.setName(SCIM2_CONNECTOR_NAME);
        proCon.setBlocking(true);
        proIdp.setDefaultProvisioningConnectorConfig(proCon);

        OutboundProvisioningConfig outboundProConfig = new OutboundProvisioningConfig();
        outboundProConfig.setProvisioningIdentityProviders(
                new org.wso2.carbon.identity.application.common.model.xsd.IdentityProvider[]{proIdp});
        serviceProvider.setOutboundProvisioningConfig(outboundProConfig);

        appMgtClient.updateApplicationData(serviceProvider);
    }

    private void clearResidentAppOutboundProvisioning() throws Exception {

        ServiceProvider serviceProvider = appMgtClient.getApplication(RESIDENT_SP_NAME);
        if (serviceProvider != null) {
            serviceProvider.setOutboundProvisioningConfig(new OutboundProvisioningConfig());
            appMgtClient.updateApplicationData(serviceProvider);
        }
    }

    private String createOutboundProvisioningIdP() throws Exception {

        IdentityProviderPOSTRequest idpRequest = new IdentityProviderPOSTRequest();
        idpRequest.setName(IDP_NAME);
        idpRequest.setDescription(IDP_DESCRIPTION);
        idpRequest.setImage(IDP_IMAGE);
        idpRequest.setIsPrimary(false);
        idpRequest.setIsFederationHub(false);
        idpRequest.setHomeRealmIdentifier("");
        idpRequest.setIdpIssuerName("");
        idpRequest.setAlias("");

        // Build outbound provisioning connector properties
        List<Property> connectorProperties = new ArrayList<>();
        connectorProperties.add(createProperty("scim2-authentication-mode", "basic"));
        connectorProperties.add(createProperty("scim2-username", getSecondaryISAdmin().getUserName()));
        connectorProperties.add(createProperty("scim2-password", getSecondaryISAdmin().getPassword()));
        connectorProperties.add(createProperty("scim2-user-ep", getTargetScim2UserEndpoint()));
        connectorProperties.add(createProperty("scim2-group-ep", getTargetScim2GroupEndpoint()));
        connectorProperties.add(createProperty("scim2-user-store-domain", "PRIMARY"));
        connectorProperties.add(createProperty("scim2-enable-pwd-provisioning", "true"));

        // Build outbound connector
        ProvisioningRequest.OutboundProvisioningRequest.OutboundConnector scim2Connector =
                new ProvisioningRequest.OutboundProvisioningRequest.OutboundConnector();
        scim2Connector.setConnectorId(SCIM2_CONNECTOR_ID);
        scim2Connector.setName(SCIM2_CONNECTOR_NAME);
        scim2Connector.setIsEnabled(true);
        scim2Connector.setProperties(connectorProperties);

        List<ProvisioningRequest.OutboundProvisioningRequest.OutboundConnector> connectorList = new ArrayList<>();
        connectorList.add(scim2Connector);

        // Build outbound provisioning request
        ProvisioningRequest.OutboundProvisioningRequest outboundRequest =
                new ProvisioningRequest.OutboundProvisioningRequest();
        outboundRequest.setDefaultConnectorId(SCIM2_CONNECTOR_ID);
        outboundRequest.setConnectors(connectorList);

        // Build provisioning request
        ProvisioningRequest provisioningRequest = new ProvisioningRequest();
        provisioningRequest.setOutboundConnectors(outboundRequest);

        idpRequest.setProvisioning(provisioningRequest);

        return idpMgtRestClient.createIdentityProvider(idpRequest);
    }

    private Property createProperty(String key, String value) {

        Property property = new Property();
        property.setKey(key);
        property.setValue(value);
        return property;
    }

    /**
     * Builds SCIM filter query parameter value for filtering by username and encodes it for use in a URL.
     * @param username the username to filter by.
     * @return the encoded filter query parameter value.
     * @throws UnsupportedEncodingException if UTF-8 encoding is not supported.
     */
    private String buildEncodedUserNameFilter(String username) throws UnsupportedEncodingException {

        String filter = String.format("userName eq \"%s\"", username);
        return URLEncoder.encode(filter, StandardCharsets.UTF_8.toString());
    }

    private String buildEncodedDisplayNameFilter(String displayName) throws UnsupportedEncodingException {

        String filter = String.format("displayName eq \"%s\"", displayName);
        return URLEncoder.encode(filter, StandardCharsets.UTF_8.toString());
    }

    private String buildUserPayload(String userName, String password, String givenName, String familyName,
                                    String email) {

        JSONObject payload = new JSONObject();

        JSONObject name = new JSONObject();
        name.put("familyName", familyName);
        name.put("givenName", givenName);
        payload.put("name", name);

        payload.put("userName", userName);
        payload.put("password", password);

        JSONObject primaryEmail = new JSONObject();
        primaryEmail.put("value", email);
        primaryEmail.put("primary", true);

        JSONArray emails = new JSONArray();
        emails.add(primaryEmail);
        payload.put("emails", emails);

        return payload.toJSONString();
    }

    private String buildGroupPayload(String displayName, String memberId, String memberDisplay) {

        JSONObject payload = new JSONObject();

        JSONArray schemas = new JSONArray();
        schemas.add("urn:ietf:params:scim:schemas:core:2.0:Group");
        payload.put("schemas", schemas);

        payload.put("displayName", displayName);

        JSONArray members = new JSONArray();

        JSONObject member = new JSONObject();
        member.put("value", memberId);
        member.put("display", memberDisplay);
        members.add(member);

        payload.put("members", members);

        return payload.toJSONString();
    }

    private String buildGroupPatchDisplayNamePayload(String newDisplayName) {

        JSONObject payload = new JSONObject();

        JSONArray schemas = new JSONArray();
        schemas.add("urn:ietf:params:scim:api:messages:2.0:PatchOp");
        payload.put("schemas", schemas);

        JSONArray operations = new JSONArray();

        JSONObject op = new JSONObject();
        op.put("op", "replace");
        op.put("path", "displayName");
        op.put("value", newDisplayName);
        operations.add(op);

        payload.put("Operations", operations);

        return payload.toJSONString();
    }

    private String buildGroupPatchAddMemberPayload(String memberId, String memberDisplay) {

        JSONObject payload = new JSONObject();

        JSONArray schemas = new JSONArray();
        schemas.add("urn:ietf:params:scim:api:messages:2.0:PatchOp");
        payload.put("schemas", schemas);

        JSONArray operations = new JSONArray();

        JSONObject op = new JSONObject();
        op.put("op", "add");

        JSONObject opValue = new JSONObject();
        JSONArray members = new JSONArray();
        JSONObject member = new JSONObject();
        member.put("display", memberDisplay);
        member.put("value", memberId);
        members.add(member);
        opValue.put("members", members);

        op.put("value", opValue);
        operations.add(op);

        payload.put("Operations", operations);

        return payload.toJSONString();
    }

    private String buildGroupPatchRemoveMemberPayload(String memberIdToRemove) {

        JSONObject payload = new JSONObject();

        JSONArray schemas = new JSONArray();
        schemas.add("urn:ietf:params:scim:api:messages:2.0:PatchOp");
        payload.put("schemas", schemas);

        JSONArray operations = new JSONArray();

        JSONObject op = new JSONObject();
        op.put("op", "remove");
        op.put("path", "members[value eq \"" + memberIdToRemove + "\"]");
        operations.add(op);

        payload.put("Operations", operations);

        return payload.toJSONString();
    }

    private String getProvisionedUserId(String userName) throws Exception {

        JSONObject secondaryUsers = secondaryScim2RestClient.filterUsers(buildEncodedUserNameFilter(userName));
        if (secondaryUsers == null) {
            return null;
        }

        long totalResults = (long) secondaryUsers.get("totalResults");
        if (totalResults == 0) {
            return null;
        }

        JSONArray resources = (JSONArray) secondaryUsers.get("Resources");
        if (resources == null || resources.size() == 0) {
            return null;
        }

        JSONObject user = (JSONObject) resources.get(0);
        return (String) user.get("id");
    }

    private List<String> extractMemberDisplayNames(JSONArray members) {

        List<String> displayNames = new ArrayList<>();
        for (Object item : members) {
            JSONObject member = (JSONObject) item;
            String display = (String) member.get("display");
            if (display != null) {
                displayNames.add(display);
            }
        }
        return displayNames;
    }

    /**
     * Get the base URI of the target server for provisioning.
     */
    private String getSecondaryISURI() {

        return String.format(HTTPS_LOCALHOST_SERVICES, DEFAULT_PORT + PORT_OFFSET_1);
    }

    /**
     * Get the admin username of the target server for provisioning.
     */
    private User getSecondaryISAdmin() throws XPathExpressionException {

        return testDataHolder.getAutomationContext().getSuperTenant().getTenantAdmin();
    }

    /**
     * Get the SCIM2 Users endpoint of the target server.
     */
    private String getTargetScim2UserEndpoint() {

        return getSecondaryISURI() + SCIM2_USER_ENDPOINT_PATH;
    }

    /**
     * Get the SCIM2 Groups endpoint of the target server.
     */
    private String getTargetScim2GroupEndpoint() {

        return getSecondaryISURI() + SCIM2_GROUP_ENDPOINT_PATH;
    }

}

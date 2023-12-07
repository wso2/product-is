/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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

package org.wso2.identity.integration.test.workflow.mgt;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.commons.lang.StringUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterGroups;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeGroups;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.extensions.servers.carbonserver.MultipleServersManager;
import org.wso2.carbon.identity.workflow.impl.stub.bean.BPSProfile;
import org.wso2.carbon.identity.workflow.mgt.stub.WorkflowAdminServiceWorkflowException;
import org.wso2.carbon.identity.workflow.mgt.stub.bean.Parameter;
import org.wso2.carbon.identity.workflow.mgt.stub.metadata.Association;
import org.wso2.carbon.identity.workflow.mgt.stub.metadata.Template;
import org.wso2.carbon.identity.workflow.mgt.stub.metadata.WorkflowImpl;
import org.wso2.carbon.identity.workflow.mgt.stub.metadata.WorkflowWizard;
import org.wso2.carbon.integration.common.admin.client.AuthenticatorClient;
import org.wso2.carbon.um.ws.api.stub.ClaimValue;
import org.wso2.carbon.um.ws.api.stub.PermissionDTO;
import org.wso2.identity.integration.common.clients.usermgt.remote.RemoteUserStoreManagerServiceClient;
import org.wso2.identity.integration.common.clients.workflow.mgt.WorkflowAdminClient;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;
import org.wso2.identity.integration.test.utils.WorkflowConstants;

import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

public class WorkflowManagementTestCase extends ISIntegrationTest {

    private static final int ITERATIONS = 20;
    private static final int DEFAULT_RESULTS_PER_PAGE = 15;
    private static final String DEFAULT_FILTER = "*";
    private WorkflowAdminClient client;
    private RemoteUserStoreManagerServiceClient usmClient;
    public MultipleServersManager manager = new MultipleServersManager();
    private String workflowId = null;
    private String associationId = null;
    private String[] rolesToAdd = {"wfRole1", "wfRole2", "wfRole3"};
    private String sessionCookie2;
    private String servicesUrl = "https://localhost:9854/services/";
    private String templateId = "MultiStepApprovalTemplate";
    private String workflowImplId = "ApprovalWorkflow";
    private final String WORKFLOW_NAME = "TestWorkflowAddUser";
    private final String ASSOCIATION_WORKFLOW_NAME = "TestAssociationWorkflow";
    private final String WORKFLOW_DESCRIPTION = "TestWorkflowDescription";
    private final String ASSOCIATION_NAME = "TestAddUserAssociation";
    private final String ASSOCIATION_CONDITION = "//*[local-name()='parameter'][@name='Roles']/*[local-name()='value']/*[local-name()" +
            "='itemValue']/text()='wfRole1'";
    private WorkflowWizard[] workflowsForListTesting = new WorkflowWizard[ITERATIONS];
    private String[] associationNamesForListTesting = new String[ITERATIONS];
    private int index = 0;

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.init();
        ConfigurationContext configContext = ConfigurationContextFactory
                .createConfigurationContextFromFileSystem(null
                        , null);
        startOtherCarbonServers();
        client = new WorkflowAdminClient(sessionCookie2, servicesUrl, configContext);
        usmClient = new RemoteUserStoreManagerServiceClient(servicesUrl, sessionCookie2);
        for (String role : rolesToAdd) {
            usmClient.addRole(role, new String[0], new PermissionDTO[0]);
        }
    }

    /**
     * Start additional carbon servers
     *
     * @throws Exception
     */
    private void startOtherCarbonServers() throws Exception {

        AuthenticatorClient authenticatorClient = new AuthenticatorClient(servicesUrl);
        sessionCookie2 = authenticatorClient.login("admin", "admin", "localhost");
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {

        try {
            for (String role : rolesToAdd) {
                usmClient.deleteRole(role);
            }
            client = null;
            usmClient = null;
        } catch (Exception e) {
            log.error("Failure occured due to :" + e.getMessage(), e);
            throw e;
        }
    }

    @Test(alwaysRun = true, description = "Testing adding a BPS Profile.")
    public void testAddBPSProfile() {

        String profileName = "TestBPSProfile";
        String host = "https://localhost:9844/services";
        String user = "admin";
        String userPassword = "admin";
        String[] passwordAsArray = new String[userPassword.length()];
        for (int i = 0; i < userPassword.length(); i++) {
            passwordAsArray[i] = userPassword.charAt(i) + "";
        }
        try {
            BPSProfile bpsProfileDTO = new BPSProfile();
            bpsProfileDTO.setProfileName(profileName);
            bpsProfileDTO.setManagerHostURL(host);
            bpsProfileDTO.setWorkerHostURL(host);
            bpsProfileDTO.setUsername(user);
            bpsProfileDTO.setPassword(passwordAsArray);
            client.addBPSProfile(bpsProfileDTO);
            BPSProfile[] bpsProfiles = client.listBPSProfiles();
            if (bpsProfiles == null || bpsProfiles.length == 0) {
                Assert.fail("BPS Profile list is empty, profile adding has been failed.");
            }
            boolean added = false;
            for (BPSProfile bpsProfile : bpsProfiles) {
                if (profileName.equals(bpsProfile.getProfileName())) {
                    added = true;
                    break;
                }
            }
            Assert.assertTrue(added, "Failed to add profile.");
        } catch (Exception e) {
            Assert.fail("Error while adding the BPS profile.", e);
        }
    }

    @Test(alwaysRun = true, description = "Testing adding a BPS Profile with existing name.", dependsOnMethods =
            "testAddBPSProfile", expectedExceptions = Exception.class)
    public void testAddDuplicateBPSProfile() throws Exception {

        String profileName = "TestBPSProfile";
        String host = "https://localhost:9845/services";
        String user = "testUser";
        String userPassword = "testPassword";
        String[] passwordAsArray = new String[userPassword.length()];
        for (int i = 0; i < userPassword.length(); i++) {
            passwordAsArray[i] = userPassword.charAt(i) + "";
        }
        BPSProfile bpsProfileDTO = new BPSProfile();
        bpsProfileDTO.setProfileName(profileName);
        bpsProfileDTO.setManagerHostURL(host);
        bpsProfileDTO.setWorkerHostURL(host);
        bpsProfileDTO.setUsername(user);
        bpsProfileDTO.setPassword(passwordAsArray);
        client.addBPSProfile(bpsProfileDTO);
    }

    @Test(alwaysRun = true, description = "Testing adding a new Workflow.", dependsOnMethods = "testAddBPSProfile")
    public void testAddWorkflow() {

        String workflowDescription = "TestWorkflowDescription";
        try {
            WorkflowWizard workflowDTO = getWorkflowDTO(WORKFLOW_NAME, WORKFLOW_DESCRIPTION);
            client.addWorkflow(workflowDTO);

            WorkflowWizard[] workflows = client.listWorkflows();
            if (workflows == null || workflows.length == 0) {
                Assert.fail("Workflow list is empty, new workflow is not added.");
            }
            boolean added = false;
            for (WorkflowWizard workflow : workflows) {
                if (WORKFLOW_NAME.equals(workflow.getWorkflowName()) &&
                        WORKFLOW_DESCRIPTION.equals(workflow
                                .getWorkflowDescription())) {
                    added = true;
                    workflowId = workflow.getWorkflowId();  //setting for future tests
                    break;
                }
            }
            Assert.assertTrue(added, "Failed to add workflow.");
        } catch (Exception e) {
            Assert.fail("Error while adding the workflow.", e);
        }
    }

    @Test(alwaysRun = true, description = "Testing adding a new association.", dependsOnMethods = "testAddWorkflow")
    public void testAddAssociation() {

        if (workflowId == null) {
            Assert.fail("Workflow has not been added in the previous test.");
        }
        try {
            client.addAssociation(workflowId, ASSOCIATION_NAME, WorkflowConstants.ADD_USER_EVENT, ASSOCIATION_CONDITION);
            Association[] associations = client.listAssociations(workflowId);
            if (associations == null || associations.length == 0) {
                Assert.fail("Association list is empty, new association is not added.");
            }
            boolean added = false;
            for (Association association : associations) {
                if (ASSOCIATION_NAME.equals(association.getAssociationName())) {
                    added = true;
                    associationId = association.getAssociationId();
                    break;
                }
            }
            Assert.assertTrue(added, "Failed to add association.");
        } catch (Exception e) {
            Assert.fail("Error while adding the association.", e);
        }
    }

    @Test(alwaysRun = true, description = "Testing a added association where request matches condition.",
            dependsOnMethods = "testAddAssociation")
    public void testAssociationForMatch() {

        String userName1 = "TestUser1ForSuccess";
        String userName2 = "TestUser2ForSuccess";

        try {

            usmClient.addUser(userName1, "Test@12345", new String[]{"wfRole1"}, new ClaimValue[0], null, false);
            Assert.assertFalse(usmClient.isExistingUser(userName1),
                    "User has been added, workflow has not been engaged.");

            usmClient.addUser(userName2, "Test@12345", new String[]{"wfRole1", "wfRole2"}, new ClaimValue[0], null,
                    false);
            Assert.assertFalse(usmClient.isExistingUser(userName2),
                    "User has been added, workflow has not been engaged.");
        } catch (Exception e) {
            log.error("Error occurred when adding test user, therefore ignoring testAssociation.", e);
        } finally {
            try {
                if (usmClient.isExistingUser(userName1)) {
                    usmClient.deleteUser(userName1);
                }
                if (usmClient.isExistingUser(userName2)) {
                    usmClient.deleteUser(userName2);
                }
            } catch (Exception e) {
                log.error("Error while removing added test users.", e);
            }
        }
    }

    @Test(alwaysRun = true, description = "Testing a added association where the request doesn't match the condition.",
            dependsOnMethods = "testAddAssociation")
    public void testAssociationForNonMatch() {
        String userName3 = "TestUser3ForAddUserWorkflow";
        String userName4 = "TestUser4ForAddUserWorkflow";
        String userName5 = "TestUser5ForAddUserWorkflow";
        try {
            //test when multiple roles present
            usmClient.addUser(userName3, "Test@12345", new String[]{"wfRole2", "wfRole3"}, new ClaimValue[0], null,
                    false);
            Assert.assertTrue(usmClient.isExistingUser(userName3),
                    "User has not been added, workflow has been engaged where it should have not.");

            //test when single role is present
            usmClient.addUser(userName4, "Test@12345", new String[]{"wfRole3"}, new ClaimValue[0], null,
                    false);
            Assert.assertTrue(usmClient.isExistingUser(userName4),
                    "User has not been added, workflow has been engaged where it should have not.");

            //test when no roles present
            usmClient.addUser(userName5, "Test@12345", new String[0], new ClaimValue[0], null,
                    false);
            Assert.assertTrue(usmClient.isExistingUser(userName5),
                    "User has not been added, workflow has been engaged where it should have not.");
        } catch (Exception e) {
            log.error("Error occurred when adding test user, therefore ignoring testAssociation.", e);
        } finally {
            try {
                if (usmClient.isExistingUser(userName3)) {
                    usmClient.deleteUser(userName3);
                }
                if (usmClient.isExistingUser(userName4)) {
                    usmClient.deleteUser(userName4);
                }
                if (usmClient.isExistingUser(userName5)) {
                    usmClient.deleteUser(userName5);
                }
            } catch (Exception e) {
                log.error("Error while removing added test users.", e);
            }
        }
    }

    @Test(alwaysRun = true, description = "Testing add user operation when workflows associated with it.",
            dependsOnMethods = "testAddAssociation")
    public void testAddUserOperation() {
        String userName6 = "TestUser6ForAddUserWorkflow";
        String userName7 = "TestUser7ForAddUserWorkflow";
        String roleName1 = "TestRole1ForAddUserWorkflow";
        try {

            usmClient.addUser(userName6, "Test@12345", new String[]{"wfRole1"}, new ClaimValue[0], null,
                    false);
        } catch (Exception e) {
            log.error("Error occurred when adding test user, therefore ignoring testAssociation.", e);
        }

        try {
            client.addAssociation(workflowId, "TestDeleteRoleAssociation", WorkflowConstants.DELETE_ROLE_EVENT,
                    "boolean(1)");
        } catch (Exception e) {
            Assert.fail("failed to add deleteRole workflow.");
        }
        try {
            usmClient.addRole(roleName1, new String[0], new PermissionDTO[0]);
            usmClient.deleteRole(roleName1);
            Assert.assertTrue(usmClient.isExistingRole(roleName1), "Role should still exist in user store since " +
                    "workflow has not approved yet.");
        } catch (Exception e) {
            Assert.fail("Failed at triggering delete role workflow.");
        }
        try {
            Association[] associations = client.listAssociations(workflowId);
            for (Association association : associations) {
                if ("TestDeleteRoleAssociation".equals(association.getAssociationName())) {
                    associationId = association.getAssociationId();
                    client.deleteAssociation(associationId);
                    break;
                }
            }
        } catch (Exception e) {
            log.error("Error while deleting deleteRole association at testAddUserOperation.", e);
        }
        try {
            usmClient.deleteRole(roleName1);
            Assert.assertFalse(usmClient.isExistingRole(roleName1), "Role should have been deleted since association is " +
                    "removed.");
        } catch (Exception e) {
            Assert.fail("Error while deleting role where no associations of DELETE_ROLE exist.");
        }

    }

    @Test(alwaysRun = true, description = "Testing delete user operation when workflows associated with it.",
            dependsOnMethods = "testAddUserOperation")
    public void testDeleteUserOperation() {
        String userName1 = "TestUser1DeleteUserWorkflow";
        String userName2 = "TestUser2DeleteUserWorkflow";
        String userName3 = "TestUser3DeleteUserWorkflow";
        String userName4 = "TestUser4DeleteUserWorkflow";
        String userName5 = "TestUser5DeleteUserWorkflow";
        String roleName1 = "TestRole1DeleteUserWorkflow";
        String roleName2 = "TestRole2DeleteUserWorkflow";

        try {
            client.addAssociation(workflowId, "TestDeleteUserAssociation", WorkflowConstants.DELETE_USER_EVENT,
                    "boolean(1)");
        } catch (Exception e) {
            Assert.fail("Failed to add deleteUser workflow.");
        }
        try {
            usmClient.addUser(userName1, "Test@12345", new String[0], new ClaimValue[0], null, false);
            usmClient.addUser(userName2, "Test@12345", new String[0], new ClaimValue[0], null, false);
            usmClient.addUser(userName3, "Test@12345", new String[0], new ClaimValue[0], null, false);
            usmClient.addUser(userName4, "Test@12345", new String[0], new ClaimValue[0], null, false);
            usmClient.addUser(userName5, "Test@12345", new String[0], new ClaimValue[0], null, false);

        } catch (Exception e) {
            log.error("Error occurred when adding test user.", e);
        }
        try {
            usmClient.deleteUser(userName1);
            Assert.assertTrue(usmClient.isExistingUser(userName1), "User should be still in the user store since " +
                    "workflow has not approved yet.");
        } catch (Exception e) {
            log.error("Error occurred when deleting test user, therefore ignoring testAssociation.", e);
        }

        try {
            client.addAssociation(workflowId, "TestAddRoleAssociation", WorkflowConstants.ADD_ROLE_EVENT,
                    "boolean(1)");
        } catch (Exception e) {
            Assert.fail("Failed to add addRole workflow.");
        }
        try {
            usmClient.addRole(roleName1, new String[]{userName2}, new PermissionDTO[]{});
        } catch (Exception e) {
            Assert.fail("Failed to add role with user.");
        }
        try {
            Association[] associations = client.listAssociations(workflowId);
            for (Association association : associations) {
                if ("TestAddRoleAssociation".equals(association.getAssociationName())) {
                    associationId = association.getAssociationId();
                    client.deleteAssociation(associationId);
                    break;
                }
            }
        } catch (Exception e) {
            log.error("Error while deleting addRole association at testAddUserOperation.", e);
        }

        try {
            usmClient.addRole(roleName2, new String[]{userName2}, new PermissionDTO[]{});
            Assert.assertTrue(usmClient.isExistingRole(roleName2), "Role should be available in the user store.");
        } catch (Exception e) {
            Assert.fail("Failed to add role.");
        }
        try {
            client.addAssociation(workflowId, "TestUpdateUserListOfRoleAssociation", WorkflowConstants
                    .UPDATE_ROLE_USERS_EVENT, "boolean(1)");
        } catch (Exception e) {
            Assert.fail("Failed to add updateUserListOfRole workflow.");
        }
        try {
            usmClient.updateUserListOfRole(roleName2, new String[]{userName3}, new String[]{});
        } catch (Exception e) {
            Assert.fail("Failed to update user list of role.");
        }

        try {
            Association[] associations = client.listAssociations(workflowId);
            for (Association association : associations) {
                if ("TestUpdateUserListOfRoleAssociation".equals(association.getAssociationName())) {
                    associationId = association.getAssociationId();
                    client.deleteAssociation(associationId);
                    break;
                }
            }
        } catch (Exception e) {
            log.error("Error while deleting updateUserListOfRole association at testAddUserOperation.", e);
        }

        try {
            client.addAssociation(workflowId, "TestUpdateRoleListOfUserAssociation", WorkflowConstants
                    .UPDATE_USER_ROLES_EVENT, "boolean(1)");
        } catch (Exception e) {
            Assert.fail("Failed to add updateRoleListOfUser workflow.");
        }
        try {
            usmClient.updateRoleListOfUser(userName4, new String[]{roleName2}, new String[]{});
        } catch (Exception e) {
            Assert.fail("Failed to update user list of role.");
        }

        try {
            Association[] associations = client.listAssociations(workflowId);
            for (Association association : associations) {
                if ("TestUpdateRoleListOfUserAssociation".equals(association.getAssociationName())) {
                    associationId = association.getAssociationId();
                    client.deleteAssociation(associationId);
                    break;
                }
            }
        } catch (Exception e) {
            log.error("Error while deleting updateRoleListOfUser association at testAddUserOperation.", e);
        }

        try {
            client.addAssociation(workflowId, "TestDeleteClaimOfUserAssociation", WorkflowConstants
                    .DELETE_USER_CLAIM_EVENT, "boolean(1)");
        } catch (Exception e) {
            Assert.fail("Failed to add deleteClaimOfUser workflow.");
        }
        try {
            usmClient.deleteUserClaimValue(userName5, "wso2.org/testClaim", null);
        } catch (Exception e) {
            Assert.fail("Failed to delete claim of user.");
        }

        try {
            Association[] associations = client.listAssociations(workflowId);
            for (Association association : associations) {
                if ("TestDeleteUserAssociation".equals(association.getAssociationName())) {
                    associationId = association.getAssociationId();
                    client.deleteAssociation(associationId);
                } else if ("TestDeleteClaimOfUserAssociation".equals(association.getAssociationName())) {
                    associationId = association.getAssociationId();
                    client.deleteAssociation(associationId);
                }
            }
            usmClient.deleteUser(userName1);
            usmClient.deleteUser(userName2);
            usmClient.deleteUser(userName3);
            usmClient.deleteUser(userName4);
            usmClient.deleteUser(userName5);
        } catch (Exception e) {
            log.error("Error while cleaning up testDeleteUserOperation.", e);
        }

    }

    @Test(alwaysRun = true, description = "Testing add role operation when workflows associated with it.",
            dependsOnMethods = "testDeleteUserOperation")
    public void testAddRoleOperation() {

        String userName1 = "TestUser11ForAddRoleWorkflow";
        String roleName1 = "TestRole1ForAddRoleWorkflow";
        String roleName2 = "TestRole2ForAddRoleWorkflow";
        String roleName3 = "TestRole3ForAddRoleWorkflow";
        String roleName4 = "TestRole4ForAddRoleWorkflow";

        try {
            usmClient.addUser(userName1, "Test@12345", new String[0], new ClaimValue[0], null, false);
            usmClient.addRole(roleName2, new String[0], new PermissionDTO[0]);
        } catch (Exception e) {
            log.error("Error occurred when adding test role.", e);
        }
        try {
            client.addAssociation(workflowId, "TestAddRoleAssociation", WorkflowConstants.ADD_ROLE_EVENT,
                    "boolean(1)");
        } catch (Exception e) {
            Assert.fail("Failed to add addRole workflow.");
        }
        try {

            usmClient.addRole(roleName1, new String[]{}, new PermissionDTO[]{});
            Assert.assertFalse(usmClient.isExistingRole(roleName1), "Role should not exist in user store since " +
                    "workflow has not been approved yet.");
        } catch (Exception e) {
            log.error("Error occurred while deleting test user, therefore ignoring testAssociation.", e);
        }

        try {
            client.addAssociation(workflowId, "TestUpdateRoleNameAssociation", WorkflowConstants.UPDATE_ROLE_NAME_EVENT,
                    "boolean(1)");
        } catch (Exception e) {
            Assert.fail("Failed to add addRole workflow.");
        }
        try {
            usmClient.updateRoleName(roleName2, roleName3);
        } catch (Exception e) {
            log.error("Error occurred while renaming the role.", e);
        }
        try {
            Association[] associations = client.listAssociations(workflowId);
            for (Association association : associations) {
                if ("TestUpdateRoleNameAssociation".equals(association.getAssociationName())) {
                    associationId = association.getAssociationId();
                    client.deleteAssociation(associationId);
                    break;
                }
            }
        } catch (Exception e) {
            log.error("Error while clean up testDeleteUserOperation.", e);
        }

        try {
            client.addAssociation(workflowId, "TestDeleteRoleAssociation", WorkflowConstants.DELETE_USER_EVENT,
                    "boolean(1)");
        } catch (Exception e) {
            Assert.fail("Failed to add deleteRole workflow.");
        }
        try {
            usmClient.deleteUser(userName1);
        } catch (Exception e) {

        }
        try {
            Association[] associations = client.listAssociations(workflowId);
            for (Association association : associations) {
                if ("TestAddRoleAssociation".equals(association.getAssociationName())) {
                    associationId = association.getAssociationId();
                    client.deleteAssociation(associationId);
                    break;
                }
            }
        } catch (Exception e) {
            log.error("Error while cleaning up testDeleteUserOperation.", e);
        }

        try {
            Association[] associations = client.listAssociations(workflowId);
            for (Association association : associations) {
                if ("TestDeleteRoleAssociation".equals(association.getAssociationName())) {
                    associationId = association.getAssociationId();
                    client.deleteAssociation(associationId);
                    break;
                }
            }
            usmClient.deleteUser(userName1);
        } catch (Exception e) {
            log.error("Error while cleaning up testDeleteUserOperation.", e);
        }
    }

    @Test(alwaysRun = true, description = "Testing delete role operation when workflows associated with it.",
            dependsOnMethods = "testAddRoleOperation")
    public void testDeleteRoleOperation() {

        String userName1 = "TestUser1DeleteRoleWorkflow";
        String userName2 = "TestUser2DeleteRoleWorkflow";
        String roleName1 = "TestRole1DeleteRoleWorkflow";
        String roleName2 = "TestRole2DeleteRoleWorkflow";
        String roleName3 = "TestRole3DeleteRoleWorkflow";
        String roleName4 = "TestRole4DeleteRoleWorkflow";
        String roleName5 = "TestRole5DeleteRoleWorkflow";
        try {

            usmClient.addUser(userName1, "Test@12345", new String[0], new ClaimValue[0], null, false);
            usmClient.addUser(userName2, "Test@12345", new String[0], new ClaimValue[0], null, false);
            usmClient.addRole(roleName1, new String[]{}, new PermissionDTO[]{});
            usmClient.addRole(roleName2, new String[]{}, new PermissionDTO[]{});
            usmClient.addRole(roleName3, new String[]{}, new PermissionDTO[]{});
            usmClient.addRole(roleName4, new String[]{}, new PermissionDTO[]{});
        } catch (Exception e) {
            log.error("Error occurred when adding test role.", e);
        }
        try {
            client.addAssociation(workflowId, "TestDeleteRoleAssociation", WorkflowConstants.DELETE_ROLE_EVENT,
                    "boolean(1)");
        } catch (Exception e) {
            Assert.fail("Failed to add deleteRole workflow.");
        }

        try {
            client.addAssociation(workflowId, "TestUpdateUserListOfRoleAssociation", WorkflowConstants
                            .UPDATE_ROLE_USERS_EVENT,
                    "boolean(1)");
        } catch (Exception e) {
            Assert.fail("failed to add deleteRole workflow.");
        }
        try {
            usmClient.updateUserListOfRole(roleName2, new String[]{userName1}, new String[]{});
        } catch (Exception e) {
            //test pass
        }
        try {
            Association[] associations = client.listAssociations(workflowId);
            for (Association association : associations) {
                if ("TestUpdateUserListOfRoleAssociation".equals(association.getAssociationName())) {
                    associationId = association.getAssociationId();
                    client.deleteAssociation(associationId);
                    break;
                }
            }
        } catch (Exception e) {
            log.error("Error while cleaning up testDeleteUserOperation.", e);
        }

        try {
            client.addAssociation(workflowId, "TestUpdateRoleListOfUserAssociation", WorkflowConstants
                    .UPDATE_USER_ROLES_EVENT, "boolean(1)");
        } catch (Exception e) {
            Assert.fail("Failed to add deleteRole workflow.");
        }
        try {
            usmClient.updateRoleListOfUser(userName2, new String[]{roleName3}, new String[]{});
        } catch (Exception e) {
            //test pass
        }
        try {
            Association[] associations = client.listAssociations(workflowId);
            for (Association association : associations) {
                if ("TestUpdateRoleListOfUserAssociation".equals(association.getAssociationName())) {
                    associationId = association.getAssociationId();
                    client.deleteAssociation(associationId);
                    break;
                }
            }
        } catch (Exception e) {
            log.error("Error while cleaning up testDeleteUserOperation.", e);
        }

        try {
            client.addAssociation(workflowId, "TestUpdateRoleNameAssociation", WorkflowConstants.UPDATE_ROLE_NAME_EVENT,
                    "boolean(1)");
        } catch (Exception e) {
            Assert.fail("Failed to add deleteRole workflow.");
        }
        try {
            usmClient.updateRoleName(roleName4, roleName5);
        } catch (Exception e) {
            //test pass
        }
        try {
            Association[] associations = client.listAssociations(workflowId);
            for (Association association : associations) {
                if ("TestUpdateRoleNameAssociation".equals(association.getAssociationName())) {
                    associationId = association.getAssociationId();
                    client.deleteAssociation(associationId);
                    break;
                }
            }
        } catch (Exception e) {
            log.error("Error while cleaning up testDeleteUserOperation.", e);
        }

        try {
            Association[] associations = client.listAssociations(workflowId);
            for (Association association : associations) {
                if ("TestDeleteRoleAssociation".equals(association.getAssociationName())) {
                    associationId = association.getAssociationId();
                    client.deleteAssociation(associationId);
                    break;
                }
            }
            usmClient.deleteUser(userName1);
            usmClient.deleteUser(userName2);
            usmClient.deleteRole(roleName1);
            usmClient.deleteRole(roleName2);
            usmClient.deleteRole(roleName3);
            usmClient.deleteRole(roleName4);
        } catch (Exception e) {
            log.error("Error while cleaning up testDeleteUserOperation.", e);
        }
    }

    @Test(alwaysRun = true, description = "Testing update user list of role operation when workflows associated with " +
            " it.", dependsOnMethods = "testDeleteRoleOperation")
    public void testUpdateUserListOfRoleOperation() {

        String userName1 = "TestUser1UpdateUserList";
        String userName2 = "TestUser2UpdateUserList";
        String roleName1 = "TestRole1UpdateUserList";
        String roleName2 = "TestRole2UpdateUserList";
        String roleName3 = "TestRole3UpdateUserList";
        try {

            usmClient.addRole(roleName1, new String[]{}, new PermissionDTO[]{});
            usmClient.addRole(roleName2, new String[]{}, new PermissionDTO[]{});
            usmClient.addUser(userName1, "Test@12345", new String[0], new ClaimValue[0], null, false);
            usmClient.addUser(userName2, "Test@12345", new String[0], new ClaimValue[0], null, false);
        } catch (Exception e) {
            log.error("Error occurred when adding test user and role.", e);
        }
        try {
            client.addAssociation(workflowId, "TestUpdateUserListOfRoleAssociation", WorkflowConstants
                    .UPDATE_ROLE_USERS_EVENT, "boolean(1)");
        } catch (Exception e) {
            Assert.fail("Failed to add updateUserListOfRole workflow.");
        }

        try {
            client.addAssociation(workflowId, "TestDeleteUserAssociation", WorkflowConstants.DELETE_USER_EVENT,
                    "boolean(1)");
            usmClient.deleteUser(userName1);
        } catch (Exception e) {
            Assert.fail("Failed to add deleteUser workflow.");
        }
        try {
            Association[] associations = client.listAssociations(workflowId);
            for (Association association : associations) {
                if ("TestDeleteUserAssociation".equals(association.getAssociationName())) {
                    associationId = association.getAssociationId();
                    client.deleteAssociation(associationId);
                    break;
                }
            }
        } catch (Exception e) {
            log.error("Error while cleaning up testDeleteUserOperation.", e);
        }

        try {
            client.addAssociation(workflowId, "TestDeleteRoleAssociation", WorkflowConstants.DELETE_ROLE_EVENT,
                    "boolean(1)");
            usmClient.deleteRole(roleName1);
        } catch (Exception e) {
            Assert.fail("Failed to add deleteRole workflow.");
        }
        try {
            Association[] associations = client.listAssociations(workflowId);
            for (Association association : associations) {
                if ("TestDeleteRoleAssociation".equals(association.getAssociationName())) {
                    associationId = association.getAssociationId();
                    client.deleteAssociation(associationId);
                    break;
                }
            }
        } catch (Exception e) {
            log.error("Error while cleaning up testDeleteRoleOperation.", e);
        }

        try {
            client.addAssociation(workflowId, "TestRenameRoleAssociation", WorkflowConstants.UPDATE_ROLE_NAME_EVENT,
                    "boolean(1)");
            usmClient.updateRoleName(roleName2, roleName3);
        } catch (Exception e) {
            Assert.fail("Failed to add renameRole workflow.");
        }
        try {
            Association[] associations = client.listAssociations(workflowId);
            for (Association association : associations) {
                if ("TestRenameRoleAssociation".equals(association.getAssociationName())) {
                    associationId = association.getAssociationId();
                    client.deleteAssociation(associationId);
                    break;
                }
            }
        } catch (Exception e) {
            log.error("Error while cleaning up testRenameRoleOperation.WorkflowManagementTestCase.", e);
        }

        try {
            Association[] associations = client.listAssociations(workflowId);
            for (Association association : associations) {
                if ("TestUpdateUserListOfRoleAssociation".equals(association.getAssociationName())) {
                    associationId = association.getAssociationId();
                    client.deleteAssociation(associationId);
                    break;
                }
            }
            usmClient.deleteRole(roleName1);
            usmClient.deleteUser(userName1);
        } catch (Exception e) {
            log.error("Error while cleaning up testUpdateUserListOfRole.", e);
        }
    }

    @Test(alwaysRun = true, description = "Testing update role list of user operation when workflows associated with " +
            "it.", dependsOnMethods = "testUpdateUserListOfRoleOperation")
    public void testUpdateRoleListOfUserOperation() {

        String userName1 = "TestUser1ForUpdateRoleList";
        String userName2 = "TestUser2ForUpdateRoleList";
        String roleName1 = "TestRole1ForUpdateRoleList";
        String roleName2 = "TestRole2ForUpdateRoleList";
        String roleName3 = "TestRole3ForUpdateRoleList";
        try {

            usmClient.addRole(roleName1, new String[]{}, new PermissionDTO[]{});
            usmClient.addRole(roleName2, new String[]{}, new PermissionDTO[]{});
            usmClient.addUser(userName1, "Test@12345", new String[0], new ClaimValue[0], null, false);
            usmClient.addUser(userName2, "Test@12345", new String[0], new ClaimValue[0], null, false);
        } catch (Exception e) {
            log.error("Error occurred when adding test user and role.", e);
        }
        try {
            client.addAssociation(workflowId, "TestUpdateRoleListOfUserAssociation", WorkflowConstants
                    .UPDATE_USER_ROLES_EVENT, "boolean(1)");
        } catch (Exception e) {
            Assert.fail("Failed to add updateUserListOfRole workflow.");
        }

        try {
            client.addAssociation(workflowId, "TestDeleteUserAssociation", WorkflowConstants.DELETE_USER_EVENT,
                    "boolean(1)");
            usmClient.deleteUser(userName1);
        } catch (Exception e) {
            Assert.fail("Failed to add deleteUser workflow.");
        }
        try {
            Association[] associations = client.listAssociations(workflowId);
            for (Association association : associations) {
                if ("TestDeleteUserAssociation".equals(association.getAssociationName())) {
                    associationId = association.getAssociationId();
                    client.deleteAssociation(associationId);
                    break;
                }
            }
        } catch (Exception e) {
            log.error("Error while cleaning up testDeleteUserOperation.", e);
        }

        try {
            client.addAssociation(workflowId, "TestDeleteRoleAssociation", WorkflowConstants.DELETE_ROLE_EVENT,
                    "boolean(1)");
            usmClient.deleteRole(roleName1);
        } catch (Exception e) {
            Assert.fail("Failed to add deleteUser workflow.");
        }
        try {
            Association[] associations = client.listAssociations(workflowId);
            for (Association association : associations) {
                if ("TestDeleteRoleAssociation".equals(association.getAssociationName())) {
                    associationId = association.getAssociationId();
                    client.deleteAssociation(associationId);
                    break;
                }
            }
        } catch (Exception e) {
            log.error("Error while cleaning up testDeleteUserOperation.", e);
        }

        try {
            client.addAssociation(workflowId, "TestRenameRoleAssociation", WorkflowConstants.UPDATE_ROLE_NAME_EVENT,
                    "boolean(1)");
            usmClient.updateRoleName(roleName2, roleName3);
        } catch (Exception e) {
            Assert.fail("Failed to add renameRole workflow.");
        }
        try {
            Association[] associations = client.listAssociations(workflowId);
            for (Association association : associations) {
                if ("TestRenameRoleAssociation".equals(association.getAssociationName())) {
                    associationId = association.getAssociationId();
                    client.deleteAssociation(associationId);
                    break;
                }
            }
        } catch (Exception e) {
            log.error("Error while cleaning up testRenameRoleOperation.", e);
        }

        try {
            Association[] associations = client.listAssociations(workflowId);
            for (Association association : associations) {
                if ("TestUpdateRoleListOfUserAssociation".equals(association.getAssociationName())) {
                    associationId = association.getAssociationId();
                    client.deleteAssociation(associationId);
                    break;
                }
            }
            usmClient.deleteRole(roleName1);
            usmClient.deleteUser(userName1);
        } catch (Exception e) {
            log.error("Error while cleaning up testUpdateRoleListOfUser.", e);
        }
    }

    @Test(alwaysRun = true, description = "Testing update role name operation when workflows associated with it.",
            dependsOnMethods = "testUpdateRoleListOfUserOperation")
    public void testUpdateRoleNameOperation() {

        String userName1 = "TestUser1ForRenameRole";
        String userName2 = "TestUser2ForRenameRole";
        String roleName1 = "TestRole1ForRenameRole";
        String roleName2 = "TestRole2ForRenameRole";
        String roleName3 = "TestRole3ForRenameRole";
        String roleName4 = "TestRole4ForRenameRole";
        String roleName5 = "TestRole5ForRenameRole";
        String roleName6 = "TestRole6ForRenameRole";
        String roleName7 = "TestRole7ForRenameRole";
        try {
            usmClient.addRole(roleName1, new String[]{}, new PermissionDTO[]{});
            usmClient.addRole(roleName3, new String[]{}, new PermissionDTO[]{});
            usmClient.addRole(roleName4, new String[]{}, new PermissionDTO[]{});
            usmClient.addRole(roleName5, new String[]{}, new PermissionDTO[]{});
            usmClient.addRole(roleName6, new String[]{}, new PermissionDTO[]{});
            usmClient.addUser(userName2, "passwd123", new String[]{}, new ClaimValue[0], null, false);
        } catch (Exception e) {

        }

        try {
            client.addAssociation(workflowId, "TestUpdateRoleNameAssociation", WorkflowConstants
                    .UPDATE_ROLE_NAME_EVENT, "boolean(1)");
            client.addAssociation(workflowId, "TestAddUserAssociationForRoleRename", WorkflowConstants
                    .ADD_USER_EVENT, "boolean(1)");
            usmClient.addUser(userName1, "passwd123", new String[]{roleName1}, new ClaimValue[0], null, false);
        } catch (Exception e) {
            Assert.fail("Failed to add updateRoleName workflow.");
        }

        try {
            Association[] associations = client.listAssociations(workflowId);
            for (Association association : associations) {
                if ("TestAddUserAssociationForRoleRename".equals(association.getAssociationName())) {
                    associationId = association.getAssociationId();
                    client.deleteAssociation(associationId);
                    break;
                }
            }
        } catch (Exception e) {
            log.error("Error while cleaning up testUpdateRoleListOfUser.", e);
        }

        try {
            client.addAssociation(workflowId, "TestDeleteRoleAssociation", WorkflowConstants
                    .DELETE_ROLE_EVENT, "boolean(1)");
            usmClient.deleteRole(roleName3);
        } catch (Exception e) {
            Assert.fail("Failed to add updateRoleName workflow.");
        }

        try {
            Association[] associations = client.listAssociations(workflowId);
            for (Association association : associations) {
                if ("TestDeleteRoleAssociation".equals(association.getAssociationName())) {
                    associationId = association.getAssociationId();
                    client.deleteAssociation(associationId);
                    break;
                }
            }
        } catch (Exception e) {
            log.error("Error while cleaning up testUpdateRoleListOfUser.", e);
        }

        try {
            client.addAssociation(workflowId, "TestUpdateUserListAssociationForRename", WorkflowConstants
                    .UPDATE_ROLE_USERS_EVENT, "boolean(1)");
            usmClient.updateUserListOfRole(roleName4, new String[0], new String[]{userName2});
        } catch (Exception e) {
            Assert.fail("Failed to add updateRoleName workflow.");
        }

        try {
            Association[] associations = client.listAssociations(workflowId);
            for (Association association : associations) {
                if ("TestUpdateUserListAssociationForRename".equals(association.getAssociationName())) {
                    associationId = association.getAssociationId();
                    client.deleteAssociation(associationId);
                    break;
                }
            }
        } catch (Exception e) {
            log.error("Error while cleaning up testUpdateRoleListOfUser.", e);
        }

        try {
            client.addAssociation(workflowId, "TestUpdateRoleListAssociationForRename", WorkflowConstants
                    .UPDATE_USER_ROLES_EVENT, "boolean(1)");
            usmClient.updateRoleListOfUser(userName2, new String[0], new String[]{roleName5});
        } catch (Exception e) {
            Assert.fail("Failed to add updateRoleName workflow.");
        }

        try {
            Association[] associations = client.listAssociations(workflowId);
            for (Association association : associations) {
                if ("TestUpdateRoleListAssociationForRename".equals(association.getAssociationName())) {
                    associationId = association.getAssociationId();
                    client.deleteAssociation(associationId);
                    break;
                }
            }
        } catch (Exception e) {
            log.error("Error while cleaning up testUpdateRoleListOfUser.", e);
        }

        try {
            usmClient.updateRoleName(roleName6, roleName2);
        } catch (Exception e) {
            Assert.fail("Failed to add updateRoleName workflow.");
        }

        try {
            Association[] associations = client.listAssociations(workflowId);
            for (Association association : associations) {
                if ("TestUpdateRoleNameAssociation".equals(association.getAssociationName())) {
                    associationId = association.getAssociationId();
                    client.deleteAssociation(associationId);
                    break;
                }
            }
            usmClient.deleteRole(roleName1);
            usmClient.deleteRole(roleName3);
            usmClient.deleteRole(roleName4);
            usmClient.deleteRole(roleName5);
            usmClient.deleteRole(roleName6);
            usmClient.deleteUser(userName2);
        } catch (Exception e) {
            log.error("Error while cleaning up testUpdateRoleName.", e);
        }
    }

    @Test(alwaysRun = true, description = "Testing update user claim operation when workflows associated with it.",
            dependsOnMethods = "testUpdateRoleNameOperation")
    public void testUpdateUserClaimOperation() {

        String userName1 = "TestUser1DeleteClaimWorkflow";
        try {
            usmClient.addUser(userName1, "passwd123", new String[0], new ClaimValue[0], null,
                    false);
        } catch (Exception e) {

        }
        try {
            client.addAssociation(workflowId, "TestDeleteUserClaimValueAssociation", WorkflowConstants
                    .DELETE_USER_CLAIM_EVENT, "boolean(1)");
            client.addAssociation(workflowId, "TestDeleteUserAssociation", WorkflowConstants
                    .DELETE_USER_EVENT, "boolean(1)");
            usmClient.deleteUser(userName1);
        } catch (Exception e) {
            Assert.fail("Failed to add updateRoleName workflow.");
        }

        try {
            Association[] associations = client.listAssociations(workflowId);
            for (Association association : associations) {
                if ("TestDeleteUserAssociation".equals(association.getAssociationName())) {
                    associationId = association.getAssociationId();
                    client.deleteAssociation(associationId);
                    break;
                }
            }
        } catch (Exception e) {
            log.error("Error while cleaning up testUpdateRoleListOfUser.", e);
        }
        try {
            Association[] associations = client.listAssociations(workflowId);
            for (Association association : associations) {
                if ("TestDeleteUserClaimValueAssociation".equals(association.getAssociationName())) {
                    associationId = association.getAssociationId();
                    client.deleteAssociation(associationId);
                    break;
                }
            }
            usmClient.deleteUser(userName1);
        } catch (Exception e) {
            log.error("Error while cleaning up testUpdateUserClaimOperation.", e);
        }
    }


    @Test(alwaysRun = true, description = "Testing removing an association.", dependsOnMethods =
            {"testAssociationForMatch", "testAssociationForNonMatch", "testUpdateUserClaimOperation"})
    public void testRemoveAssociation() {

        if (associationId == null) {
            Assert.fail("Association has not been added in the previous test.");
        }
        try {
            client.deleteAssociation(associationId);
            Association[] associations = client.listAssociations(workflowId);
            if (associations != null) {
                for (Association association : associations) {
                    if (associationId.equals(association.getAssociationId())) {
                        Assert.fail("Association " + associationId + " is not deleted, It still exists.");
                        break;
                    }
                }
            }
        } catch (Exception e) {
            Assert.fail("Error while deleting the association.", e);
        }
    }

    @Test(alwaysRun = true, description = "Testing removing a workflow.", dependsOnMethods =
            {"testRemoveAssociation", "testDeleteUserOperation"})
    public void testRemoveWorkflow() {

        if (workflowId == null) {
            Assert.fail("Workflow has not been added in the previous test.");
        }

        try {
            if (isWorkflowPresent(workflowId)) {
                Thread.sleep(20000);
                client.deleteWorkflow(workflowId);
                if (isWorkflowPresent(workflowId)) {
                    Assert.fail("Workflow " + workflowId + " is not deleted, It still exists.");
                }
            } else {
                Assert.fail("Workflow with id " + workflowId + " does not exist to be deleted.");
            }
        } catch (Exception e) {
            Assert.fail("Error while deleting the workflow.", e);
        }
    }

    @Test(alwaysRun = true, description = "Testing deleting a BPS Profile.", dependsOnMethods =
            {"testAddDuplicateBPSProfile", "testRemoveWorkflow"})
    public void testRemoveBPSProfile() {

        String profileName = "TestBPSProfile";
        try {
            client.deleteBPSProfile(profileName);
            BPSProfile[] bpsProfiles = client.listBPSProfiles();
            if (bpsProfiles != null) {
                for (BPSProfile bpsProfile : bpsProfiles) {
                    if (profileName.equals(bpsProfile.getProfileName())) {
                        Assert.fail("BPS Profile " + profileName + " is not deleted.");
                        break;
                    }
                }
            }
        } catch (Exception e) {
            Assert.fail("Error while deleting the BPS profile.", e);
        }
    }

    @BeforeGroups("ListWorkflowsTests")
    public void createWorkflowsForListing() throws RemoteException, WorkflowAdminServiceWorkflowException {

        if (StringUtils.isNotEmpty(workflowId) && isWorkflowPresent(workflowId)) {
            client.deleteWorkflow(workflowId);
        }

        for (index = 0; index < ITERATIONS; index++) {
            String workflowName = WORKFLOW_NAME + index;
            String workflowDescription = WORKFLOW_DESCRIPTION + index;

            WorkflowWizard workflowDTO = getWorkflowDTO(workflowName, workflowDescription);
            client.addWorkflow(workflowDTO);

            workflowsForListTesting[index] = workflowDTO;
        }
        index = 0;
    }

    @AfterGroups("ListWorkflowsTests")
    public void deleteWorkflowsForListing() throws RemoteException, WorkflowAdminServiceWorkflowException {

        WorkflowWizard[] workflows = client.listWorkflows();
        try {
            Thread.sleep(20000);
        } catch (InterruptedException e) {
            Assert.fail("Error while deleting the workflows created for listing.", e);
        }
        for (index = 0; index < ITERATIONS; index++) {
            String workflowName = WORKFLOW_NAME + index;
            String workflowDescription = WORKFLOW_DESCRIPTION + index;

            Optional<WorkflowWizard> workflow = Arrays.stream(workflows)
                    .filter(w -> w.getWorkflowName().equals(workflowName))
                    .collect(Collectors.toList())
                    .stream()
                    .findFirst();

            if (workflow.isPresent()) {
                client.deleteWorkflow(workflow.get().getWorkflowId());
            }
        }
        workflowsForListTesting = new WorkflowWizard[ITERATIONS];
    }

    @Test(alwaysRun = true, description = "Testing listing workflows with filter.", groups = {
            "ListWorkflowsTests"}, dependsOnMethods = {"testRemoveWorkflow", "deleteAssociationsForListing"})
    public void testListPaginatedWorkflowsWithFilter() throws RemoteException, WorkflowAdminServiceWorkflowException {

        String workflowName = WORKFLOW_NAME + index;
        WorkflowWizard[] workflows = client.listPaginatedWorkflows(ITERATIONS * 2, 0, workflowName);
        workflows = removeAssociationWorkflow(workflows);
        Assert.assertEquals(workflows.length, 1, "Results are not filtered");

        Assert.assertEquals(workflows[0].getWorkflowName(), workflowName,
                "Results are not filtered");
    }

    @Test(alwaysRun = true, description = "Testing listing workflows with wildcard.", groups = {
            "ListWorkflowsTests"}, dependsOnMethods = {"testRemoveWorkflow", "deleteAssociationsForListing"})
    public void testListPaginatedWorkflowsWithWildcard() throws RemoteException, WorkflowAdminServiceWorkflowException {

        WorkflowWizard[] workflows = client.listPaginatedWorkflows(ITERATIONS * 2, 0, DEFAULT_FILTER);
        workflows = removeAssociationWorkflow(workflows);
        Assert.assertTrue(compareWorkflowArrays(workflows, workflowsForListTesting),
                "Received workflows array is different");
    }

    @Test(alwaysRun = true, description = "Testing listing workflows with non existing filter.", groups = {
            "ListWorkflowsTests"}, dependsOnMethods = {"testRemoveWorkflow", "deleteAssociationsForListing"})
    public void testListPaginatedWorkflowsWithNonExisting()
            throws RemoteException, WorkflowAdminServiceWorkflowException {

        WorkflowWizard[] workflows = client.listPaginatedWorkflows(ITERATIONS * 2, 0, "NonExisting");
        Assert.assertEquals(workflows.length, 0, "Results are received for non existing filter");
    }

    @Test(alwaysRun = true, description = "Testing listing all workflows.", groups = {
            "ListWorkflowsTests"}, dependsOnMethods = {"testRemoveWorkflow", "deleteAssociationsForListing"})
    public void testListWorkflows() throws RemoteException, WorkflowAdminServiceWorkflowException {

        WorkflowWizard[] workflows = client.listWorkflows();
        workflows = removeAssociationWorkflow(workflows);
        WorkflowWizard[] orderedWorkflowsForListTesting = Arrays.stream(workflowsForListTesting)
                .sorted((o1, o2) -> o1.getWorkflowName().compareTo(o2.getWorkflowName()))
                .toArray(WorkflowWizard[]::new);

        Assert.assertTrue(compareWorkflowArrays(workflows, orderedWorkflowsForListTesting),
                "Received workflows array is different");

    }

    private WorkflowWizard[] removeAssociationWorkflow(WorkflowWizard[] workflows) {

        return Arrays.stream(workflows)
                .filter(e -> !e.getWorkflowName().equals(ASSOCIATION_WORKFLOW_NAME)).toArray(WorkflowWizard[]::new);
    }

    @Test(alwaysRun = true, description = "Testing listing all paginated workflows for first page.", groups = {
            "ListWorkflowsTests"}, dependsOnMethods = {"testRemoveWorkflow", "deleteAssociationsForListing"})
    public void testListPaginatedWorkflowsFirstPage() throws RemoteException, WorkflowAdminServiceWorkflowException {

        WorkflowWizard[] workflows =
                client.listPaginatedWorkflows(DEFAULT_RESULTS_PER_PAGE, 0, WORKFLOW_NAME + DEFAULT_FILTER);
        Assert.assertEquals(workflows.length, DEFAULT_RESULTS_PER_PAGE, "Results are not paginated");

        WorkflowWizard[] page1Workflows = Arrays.copyOf(workflowsForListTesting, DEFAULT_RESULTS_PER_PAGE);
        Assert.assertTrue(compareWorkflowArrays(workflows, page1Workflows),
                "Received workflows array is different");

    }

    @Test(alwaysRun = true, description = "Testing listing all paginated workflows for second page.", groups = {
            "ListWorkflowsTests"}, dependsOnMethods = {"testRemoveWorkflow", "deleteAssociationsForListing"})
    public void testListPaginatedWorkflowsSecondPage()
            throws RemoteException, WorkflowAdminServiceWorkflowException {

        WorkflowWizard[] workflows = client.listPaginatedWorkflows(DEFAULT_RESULTS_PER_PAGE, DEFAULT_RESULTS_PER_PAGE,
                WORKFLOW_NAME + DEFAULT_FILTER);
        Assert.assertEquals(workflows.length, workflowsForListTesting.length - DEFAULT_RESULTS_PER_PAGE,
                "Results are not paginated");

        WorkflowWizard[] page2Workflows =
                Arrays.copyOfRange(workflowsForListTesting, DEFAULT_RESULTS_PER_PAGE,
                        workflowsForListTesting.length);
        Assert.assertTrue(compareWorkflowArrays(workflows, page2Workflows),
                "Received workflows array is different");

    }

    @Test(alwaysRun = true, description = "Testing getting count of all workflows.", groups = {
            "ListWorkflowsTests"}, dependsOnMethods = {"testRemoveWorkflow", "deleteAssociationsForListing"})
    public void testGetWorkflowsCountWithWildcard() throws RemoteException, WorkflowAdminServiceWorkflowException {

        int workflowsCount = client.getWorkflowsCount(WORKFLOW_NAME + DEFAULT_FILTER);
        Assert.assertEquals(workflowsCount, ITERATIONS, "Count is incorrect");

    }

    @Test(alwaysRun = true, description = "Testing getting count of workflows.", groups = {
            "ListWorkflowsTests"}, dependsOnMethods = {"testRemoveWorkflow", "deleteAssociationsForListing"})
    public void testGetWorkflowsCountWithFilter() throws RemoteException, WorkflowAdminServiceWorkflowException {

        String workflowName = WORKFLOW_NAME + index;
        int workflowsCount = client.getWorkflowsCount(workflowName);
        Assert.assertEquals(workflowsCount, 1, "Count is incorrect");

    }

    private boolean compareWorkflowArrays(WorkflowWizard[] actual, WorkflowWizard[] expected) {

        if (actual.length != expected.length) {
            return false;
        }

        for (int i = 0; i < actual.length; i++) {
            if (!isWorkflowsEqual(actual[i], expected[i])) {
                return false;
            }
        }

        return true;
    }

    private boolean isWorkflowsEqual(WorkflowWizard actual, WorkflowWizard expected) {

        return (actual.getWorkflowName().equals(expected.getWorkflowName()) &&
                actual.getWorkflowDescription().equals(expected
                        .getWorkflowDescription()));
    }

    private WorkflowWizard getWorkflowDTO(String workflowName, String workflowDescription)
            throws RemoteException, WorkflowAdminServiceWorkflowException {

        WorkflowWizard workflowDTO = new WorkflowWizard();
        workflowDTO.setWorkflowName(workflowName);
        workflowDTO.setWorkflowDescription(workflowDescription);
        workflowDTO.setTemplateId(templateId);
        workflowDTO.setWorkflowImplId(workflowImplId);
        Template template = client.getTemplate(templateId);
        workflowDTO.setTemplate(template);
        WorkflowImpl workflowImpl = client.getWorkflowImpl(templateId, workflowImplId);
        workflowDTO.setWorkflowImpl(workflowImpl);

        Parameter[] parametersImpl = new Parameter[2];

        Parameter parameter = new Parameter();
        parameter.setParamName("BPSProfile");
        parameter.setParamValue("embeded_bps");
        parameter.setHolder("WorkflowImpl");
        parameter.setQName("BPSProfile");

        parametersImpl[0] = parameter;

        parameter = new Parameter();
        parameter.setParamName("HTSubject");
        parameter.setParamValue("sample ht");
        parameter.setHolder("WorkflowImpl");
        parameter.setQName("HTSubject");

        parametersImpl[1] = parameter;

        Parameter[] parametersTmp = new Parameter[1];

        parameter = new Parameter();
        parameter.setParamName("UserAndRole");
        parameter.setParamValue("admin");
        parameter.setHolder("Template");
        parameter.setQName("UserAndRole-step-1-roles");

        parametersTmp[0] = parameter;

        workflowDTO.setTemplateParameters(parametersTmp);
        workflowDTO.setWorkflowImplParameters(parametersImpl);

        return workflowDTO;
    }

    private boolean isWorkflowPresent(String workflowIdentity) throws RemoteException, WorkflowAdminServiceWorkflowException {

        WorkflowWizard[] workflows = client.listWorkflows();

        if (workflows != null) {
            for (WorkflowWizard workflow : workflows) {
                if (workflowIdentity.equals(workflow.getWorkflowId())) {
                    return true;
                }
            }
        }

        return false;
    }

    @BeforeGroups("ListAssociationsTests")
    public void createAssociationsForListing() throws RemoteException, WorkflowAdminServiceWorkflowException {

        if (StringUtils.isNotEmpty(associationId)) {
            client.deleteAssociation(associationId);
        }

        String workflowName = ASSOCIATION_WORKFLOW_NAME;
        String workflowDescription = WORKFLOW_DESCRIPTION + ITERATIONS;

        WorkflowWizard workflowDTO = getWorkflowDTO(workflowName, workflowDescription);
        client.addWorkflow(workflowDTO);
        WorkflowWizard[] workflows = client.listPaginatedWorkflows(ITERATIONS * 2, 0, workflowName);

        if (Arrays.stream(workflows).findFirst().isPresent()) {
            String testWorkflowId = Arrays.stream(workflows).findFirst().get().getWorkflowId();

            for (index = 0; index < ITERATIONS; index++) {
                String associationName = ASSOCIATION_NAME + index;

                client.addAssociation(testWorkflowId, associationName, WorkflowConstants.ADD_USER_EVENT,
                        ASSOCIATION_CONDITION);
                associationNamesForListTesting[index] = associationName;
            }
            index = 0;
        }
    }

    @AfterGroups("ListAssociationsTests")
    public void deleteAssociationsForListing() throws RemoteException, WorkflowAdminServiceWorkflowException {

        Association[] associations = client.listAllAssociations();
        String testWorkflowId = null;

        for (index = 0; index < ITERATIONS; index++) {
            String associationName = ASSOCIATION_NAME + index;

            Optional<Association> association = Arrays.stream(associations)
                    .filter(w -> w.getAssociationName().equals(associationName))
                    .collect(Collectors.toList())
                    .stream()
                    .findFirst();

            if (association.isPresent()) {
                if (testWorkflowId == null) {
                    testWorkflowId = association.get().getWorkflowId();
                }
                client.deleteAssociation(association.get().getAssociationId());
            }
        }

        if (StringUtils.isNotEmpty(testWorkflowId)) {
            client.deleteWorkflow(testWorkflowId);
        }

        associationNamesForListTesting = new String[ITERATIONS];
        index = 0;
    }

    @Test(alwaysRun = true, description = "Testing listing associations.", groups = {
            "ListAssociationsTests"}, dependsOnMethods = {"testRemoveWorkflow", "testRemoveAssociation"})
    public void testListPaginatedAssociationsWithFilter()
            throws RemoteException, WorkflowAdminServiceWorkflowException {

        String associationName = ASSOCIATION_NAME + index;
        Association[] associations = client.listPaginatedAssociations(ITERATIONS * 2, 0, associationName);
        Assert.assertEquals(associations.length, 1, "Results are not filtered");

        Assert.assertEquals(associations[0].getAssociationName(), associationName,
                "Results are not filtered");
    }

    @Test(alwaysRun = true, description = "Testing listing associations with wildcard.", groups = {
            "ListAssociationsTests"}, dependsOnMethods = {"testRemoveWorkflow", "testRemoveAssociation"})
    public void testListPaginatedAssociationsWithWildcard()
            throws RemoteException, WorkflowAdminServiceWorkflowException {

        Association[] associations = client.listPaginatedAssociations(ITERATIONS * 2, 0, DEFAULT_FILTER);
        Assert.assertTrue(compareAssociationArrays(associations, associationNamesForListTesting),
                "Received associations array is different");
    }

    @Test(alwaysRun = true, description = "Testing listing associations with non existing filter.", groups = {
            "ListAssociationsTests"}, dependsOnMethods = {"testRemoveWorkflow", "testRemoveAssociation"})
    public void testListPaginatedAssociationsWithNonExisting()
            throws RemoteException, WorkflowAdminServiceWorkflowException {

        Association[] associations = client.listPaginatedAssociations(ITERATIONS * 2, 0, "NonExisting");
        Assert.assertEquals(associations.length, 0, "Results are received for non existing filter");
    }

    @Test(alwaysRun = true, description = "Testing listing all associations for workflow.", groups = {
            "ListAssociationsTests"}, dependsOnMethods = {"testRemoveWorkflow", "testRemoveAssociation"})
    public void testListAssociations() throws RemoteException, WorkflowAdminServiceWorkflowException {

        WorkflowWizard[] workflows = client.listPaginatedWorkflows(ITERATIONS * 2, 0, ASSOCIATION_WORKFLOW_NAME);
        if (Arrays.stream(workflows).findFirst().isPresent()) {
            String workflowId = Arrays.stream(workflows).findFirst().get().getWorkflowId();
            Association[] associations = client.listAssociations(workflowId);
            Assert.assertTrue(compareAssociationArrays(associations, associationNamesForListTesting),
                    "Received associations array is different");
        }
    }

    @Test(alwaysRun = true, description = "Testing listing all associations.", groups = {
            "ListAssociationsTests"}, dependsOnMethods = {"testRemoveWorkflow", "testRemoveAssociation"})
    public void testListAllAssociations() throws RemoteException, WorkflowAdminServiceWorkflowException {

        Association[] associations = client.listAllAssociations();
        Assert.assertTrue(compareAssociationArrays(associations, associationNamesForListTesting),
                "Received associations array is different");

    }

    @Test(alwaysRun = true, description = "Testing listing all paginated associations for first page.", groups = {
            "ListAssociationsTests"}, dependsOnMethods = {"testRemoveWorkflow", "testRemoveAssociation"})
    public void testListPaginatedAssociationsFirstPage()
            throws RemoteException, WorkflowAdminServiceWorkflowException {

        Association[] associations = client.listPaginatedAssociations(DEFAULT_RESULTS_PER_PAGE, 0, DEFAULT_FILTER);
        Assert.assertEquals(associations.length, DEFAULT_RESULTS_PER_PAGE, "Results are not paginated");

        String[] page1Associations = Arrays.copyOf(associationNamesForListTesting, DEFAULT_RESULTS_PER_PAGE);
        Assert.assertTrue(compareAssociationArrays(associations, page1Associations),
                "Received associations array is different");

    }

    @Test(alwaysRun = true, description = "Testing listing all paginated associations for second page.", groups = {
            "ListAssociationsTests"}, dependsOnMethods = {"testRemoveWorkflow", "testRemoveAssociation"})
    public void testListPaginatedAssociationsSecondPage()
            throws RemoteException, WorkflowAdminServiceWorkflowException {

        Association[] associations =
                client.listPaginatedAssociations(DEFAULT_RESULTS_PER_PAGE, DEFAULT_RESULTS_PER_PAGE, DEFAULT_FILTER);
        Assert.assertEquals(associations.length, associationNamesForListTesting.length - DEFAULT_RESULTS_PER_PAGE,
                "Results are not paginated");

        String[] page2Associations = Arrays.copyOfRange(associationNamesForListTesting,
                DEFAULT_RESULTS_PER_PAGE, associationNamesForListTesting.length);
        Assert.assertTrue(compareAssociationArrays(associations, page2Associations),
                "Received associations array is different");

    }

    @Test(alwaysRun = true, description = "Testing getting count of all associations.", groups = {
            "ListAssociationsTests"}, dependsOnMethods = {"testRemoveWorkflow", "testRemoveAssociation"})
    public void testGetAssociationsCountWithWildcard() throws RemoteException, WorkflowAdminServiceWorkflowException {

        int associationsCount = client.getAssociationsCount(DEFAULT_FILTER);
        Assert.assertEquals(associationsCount, ITERATIONS, "Count is incorrect");

    }

    @Test(alwaysRun = true, description = "Testing getting count of associations.", groups = {
            "ListAssociationsTests"}, dependsOnMethods = {"testRemoveWorkflow", "testRemoveAssociation"})
    public void testGetAssociationsCountWithFilter() throws RemoteException, WorkflowAdminServiceWorkflowException {

        String associationName = ASSOCIATION_NAME + index;
        int associationsCount = client.getAssociationsCount(associationName);
        Assert.assertEquals(associationsCount, 1, "Count is incorrect");

    }

    private boolean compareAssociationArrays(Association[] actual, String[] expected) {

        if (actual.length != expected.length) {
            return false;
        }

        for (int i = 0; i < actual.length; i++) {
            if (!actual[i].getAssociationName().equals(expected[i])) {
                return false;
            }
        }

        return true;
    }
}

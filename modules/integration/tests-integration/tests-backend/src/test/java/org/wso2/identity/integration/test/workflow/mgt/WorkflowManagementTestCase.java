/*
 * Copyright (c) 2005-2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.identity.workflow.mgt.stub.bean.AssociationDTO;
import org.wso2.carbon.identity.workflow.mgt.stub.bean.BPSProfileDTO;
import org.wso2.carbon.identity.workflow.mgt.stub.bean.ParameterDTO;
import org.wso2.carbon.identity.workflow.mgt.stub.bean.TemplateImplDTO;
import org.wso2.carbon.identity.workflow.mgt.stub.bean.WorkflowDTO;
import org.wso2.carbon.um.ws.api.stub.ClaimValue;
import org.wso2.carbon.um.ws.api.stub.PermissionDTO;
import org.wso2.identity.integration.common.clients.usermgt.remote.RemoteUserStoreManagerServiceClient;
import org.wso2.identity.integration.common.clients.workflow.mgt.WorkflowAdminClient;
import org.wso2.identity.integration.common.utils.CarbonTestServerManager;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;
import org.wso2.identity.integration.test.utils.WorkflowConstants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WorkflowManagementTestCase extends ISIntegrationTest {

    private WorkflowAdminClient client;
    private RemoteUserStoreManagerServiceClient usmClient;

    private String workflowName = "TestWorkflow";
    private String workflowId = null;
    private String associationId = null;
    private String[] rolesToAdd = {"wfRole1", "wfRole2", "wfRole3"};

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.init();
        ConfigurationContext configContext = ConfigurationContextFactory
                .createConfigurationContextFromFileSystem(null
                        , null);
        client = new WorkflowAdminClient(sessionCookie, backendURL, configContext);
        usmClient = new RemoteUserStoreManagerServiceClient(backendURL, sessionCookie);
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

        Map<String, String> startupParameterMap1 = new HashMap<String, String>();
        startupParameterMap1.put("", "");

        AutomationContext context1 = new AutomationContext("IDENTITY", "identity002", TestUserMode.SUPER_TENANT_ADMIN);
        automationContextMap.put(PORT_OFFSET_1, context1);

        CarbonTestServerManager server1 = new CarbonTestServerManager(context1, System.getProperty("carbon.zip"),
                startupParameterMap1);

        Map<String, String> startupParameterMap2 = new HashMap<String, String>();
        startupParameterMap2.put(PORT_OFFSET_PARAM, String.valueOf(PORT_OFFSET_2));

        AutomationContext context2 = new AutomationContext("IDENTITY", "identity003", TestUserMode.SUPER_TENANT_ADMIN);
        automationContextMap.put(PORT_OFFSET_2, context2);

        CarbonTestServerManager server2 = new CarbonTestServerManager(context2, System.getProperty("carbon.zip"),
                startupParameterMap2);

        manager.startServers(server1, server2);
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {

        for (String role : rolesToAdd) {
            usmClient.deleteRole(role);
        }
        client = null;
        usmClient = null;
    }

    @Test(alwaysRun = true, description = "Testing adding a BPS Profile")
    public void testAddBPSProfile() {

        String profileName = "TestBPSProfile";
        String host = "https://localhost:9444";
        String user = "admin";
        String userPassword = "admin";
        try {
            BPSProfileDTO bpsProfileDTO = new BPSProfileDTO();
            bpsProfileDTO.setProfileName(profileName);
            bpsProfileDTO.setHost(host);
            bpsProfileDTO.setUsername(user);
            bpsProfileDTO.setPassword(userPassword);
            bpsProfileDTO.setCallbackUser(user);
            bpsProfileDTO.setCallbackPassword(userPassword);
            client.addBPSProfile(bpsProfileDTO);
            BPSProfileDTO[] bpsProfiles = client.listBPSProfiles();
            if (bpsProfiles == null || bpsProfiles.length == 0) {
                Assert.fail("BPS Profile list is empty, profile adding has been failed");
            }
            boolean added = false;
            for (BPSProfileDTO bpsProfile : bpsProfiles) {
                if (profileName.equals(bpsProfile.getProfileName())) {
                    added = true;
                    break;
                }
            }
            Assert.assertTrue(added, "Failed to add profile");
        } catch (Exception e) {
            Assert.fail("Error while adding the BPS profile", e);
        }
    }

    @Test(alwaysRun = true, description = "Testing adding a BPS Profile with existing name", dependsOnMethods =
            "testAddBPSProfile", expectedExceptions = AxisFault.class)
    public void testAddDuplicateBPSProfile() throws Exception {

        String profileName = "TestBPSProfile";
        String host = "https://localhost:9445";
        String user = "testUser";
        String userPassword = "testPassword";BPSProfileDTO bpsProfileDTO = new BPSProfileDTO();
        bpsProfileDTO.setProfileName(profileName);
        bpsProfileDTO.setHost(host);
        bpsProfileDTO.setUsername(user);
        bpsProfileDTO.setPassword(userPassword);
        bpsProfileDTO.setCallbackUser(user);
        bpsProfileDTO.setCallbackPassword(userPassword);
        client.addBPSProfile(bpsProfileDTO);
    }

    @Test(alwaysRun = true, description = "Testing adding a new Workflow", dependsOnMethods = "testAddBPSProfile")
    public void testAddWorkflow() {

        String workflowDescription = "TestWorkflowDescription";
        try {
            WorkflowDTO workflowDTO = new WorkflowDTO();
            workflowDTO.setWorkflowName(workflowName);
            workflowDTO.setWorkflowDescription(workflowDescription);
            workflowDTO.setTemplateName("SimpleApproval");
            workflowDTO.setImplementationName("BPEL");

            List<ParameterDTO> templateImplParams = new ArrayList<>();
            ParameterDTO bpelProfile = new ParameterDTO();
            bpelProfile.setParamName("BPELEngineProfile");
            bpelProfile.setParamValue("TestBPSProfile");
            templateImplParams.add(bpelProfile);
            ParameterDTO HTSubject = new ParameterDTO();
            HTSubject.setParamName("HTSubject");
            HTSubject.setParamValue("");
            templateImplParams.add(HTSubject);
            ParameterDTO HTDescription = new ParameterDTO();
            HTDescription.setParamName("HTDescription");
            HTDescription.setParamValue("");
            templateImplParams.add(HTDescription);

            client.addWorkflow(workflowDTO, Collections.EMPTY_LIST, templateImplParams);

            WorkflowDTO[] workflows = client.listWorkflows();
            if (workflows == null || workflows.length == 0) {
                Assert.fail("Workflow list is empty, new workflow is not added");
            }
            boolean added = false;
            for (WorkflowDTO workflow : workflows) {
                if (workflowName.equals(workflow.getWorkflowName()) && workflowDescription.equals(workflow
                        .getWorkflowDescription())) {
                    added = true;
                    workflowId = workflow.getWorkflowId();  //setting for future tests
                    break;
                }
            }
            Assert.assertTrue(added, "Failed to add workflow");
        } catch (Exception e) {
            Assert.fail("Error while adding the workflow", e);
        }
    }

    @Test(alwaysRun = true, description = "Testing adding a new association", dependsOnMethods = "testAddWorkflow")
    public void testAddAssociation() {

        String associationName = "TestAddUserAssociation";
        //the following xpath is valid for user with the role "wfRole1"
        String condition = "//*[local-name()='parameter'][@name='Roles']/*[local-name()='value']/*[local-name()" +
                "='itemValue']/text()='wfRole1'";
        if (workflowId == null) {
            Assert.fail("Workflow has not been added in the previous test");
        }
        try {
            client.addAssociation(workflowId, associationName, WorkflowConstants.ADD_USER_EVENT, condition);
            AssociationDTO[] associations = client.listAssociationsForWorkflow(workflowId);
            if (associations == null || associations.length == 0) {
                Assert.fail("Association list is empty, new association is not added");
            }
            boolean added = false;
            for (AssociationDTO association : associations) {
                if (associationName.equals(association.getAssociationName())) {
                    added = true;
                    associationId = association.getAssociationId();
                    break;
                }
            }
            Assert.assertTrue(added, "Failed to add association");
        } catch (Exception e) {
            Assert.fail("Error while adding the association", e);
        }
    }

    @Test(alwaysRun = true, description = "Testing a added association where request matches condition",
            dependsOnMethods = "testAddAssociation")
    public void testAssociationForMatch() {
        //by this time workflow to reject every user addition is enabled,
        String userName1 = "TestUser1ForSuccessAddUserWorkflow";
        String userName2 = "TestUser2ForSuccessAddUserWorkflow";
        try {

            usmClient.addUser(userName1, "test12345", new String[]{"wfRole1"}, new ClaimValue[0], null, false);
            Assert.assertFalse(usmClient.isExistingUser(userName1),
                    "User has been added, workflow has not been engaged");

            usmClient.addUser(userName2, "test12345", new String[]{"wfRole1", "wfRole2"}, new ClaimValue[0], null,
                    false);
            Assert.assertFalse(usmClient.isExistingUser(userName2),
                    "User has been added, workflow has not been engaged");
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
                log.error("Error while removing added test users");
            }
        }
    }

    @Test(alwaysRun = true, description = "Testing a added association where the request doesn't match the condition",
            dependsOnMethods = "testAddAssociation")
    public void testAssociationForNonMatch() {
        //by this time workflow to reject every user addition is enabled,
        String userName3 = "TestUser3ForAddUserWorkflow";
        String userName4 = "TestUser4ForAddUserWorkflow";
        String userName5 = "TestUser5ForAddUserWorkflow";
        try {
            //test when multiple roles present
            usmClient.addUser(userName3, "test12345", new String[]{"wfRole2", "wfRole3"}, new ClaimValue[0], null,
                    false);
            Assert.assertTrue(usmClient.isExistingUser(userName3),
                    "User has not been added, workflow has been engaged where it should have not");

            //test when single role is present
            usmClient.addUser(userName4, "test12345", new String[]{"wfRole3"}, new ClaimValue[0], null,
                    false);
            Assert.assertTrue(usmClient.isExistingUser(userName4),
                    "User has not been added, workflow has been engaged where it should have not");

            //test when no roles present
            usmClient.addUser(userName5, "test12345", new String[0], new ClaimValue[0], null,
                    false);
            Assert.assertTrue(usmClient.isExistingUser(userName5),
                    "User has not been added, workflow has been engaged where it should have not");
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
                log.error("Error while removing added test users");
            }
        }
    }

    @Test(alwaysRun = true, description = "Testing removing an association", dependsOnMethods =
            {"testAssociationForMatch", "testAssociationForNonMatch"})
    public void testRemoveAssociation() {

        if (associationId == null) {
            Assert.fail("Association has not been added in the previous test");
        }
        try {
            client.deleteAssociation(associationId);
            AssociationDTO[] associations = client.listAssociationsForWorkflow(workflowId);
            if (associations != null) {
                for (AssociationDTO association : associations) {
                    if (associationId.equals(association.getAssociationId())) {
                        Assert.fail("Association " + associationId + " is not deleted, It still exists");
                        break;
                    }
                }
            }
        } catch (Exception e) {
            Assert.fail("Error while deleting the association", e);
        }
    }

    @Test(alwaysRun = true, description = "Testing removing an association", dependsOnMethods = "testRemoveAssociation")
    public void testRemoveWorkflow() {

        if (workflowId == null) {
            Assert.fail("Workflow has not been added in the previous test");
        }
        try {
            client.deleteWorkflow(workflowId);
            WorkflowDTO[] workflows = client.listWorkflows();
            if (workflows != null) {
                for (WorkflowDTO workflow : workflows) {
                    if (workflowId.equals(workflow.getWorkflowId())) {
                        Assert.fail("Workflow " + workflowId + " is not deleted, It still exists");
                        break;
                    }
                }
            }
        } catch (Exception e) {
            Assert.fail("Error while deleting the workflow", e);
        }
    }

    @Test(alwaysRun = true, description = "Testing deleting a BPS Profile", dependsOnMethods =
            {"testAddDuplicateBPSProfile","testAddWorkflow"})
    public void testRemoveBPSProfile() {

        String profileName = "TestBPSProfile";
        try {
            client.deleteBPSProfile(profileName);
            BPSProfileDTO[] bpsProfiles = client.listBPSProfiles();
            if (bpsProfiles != null) {
                for (BPSProfileDTO bpsProfile : bpsProfiles) {
                    if (profileName.equals(bpsProfile.getProfileName())) {
                        Assert.fail("BPS Profile " + profileName + " is not deleted");
                        break;
                    }
                }
            }
        } catch (Exception e) {
            Assert.fail("Error while deleting the BPS profile", e);
        }
    }

}
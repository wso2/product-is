/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.identity.integration.test.rest.api.user.approval.common;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.openqa.selenium.remote.internal.HttpClientFactory;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.extensions.servers.carbonserver.MultipleServersManager;
import org.wso2.carbon.identity.workflow.mgt.stub.WorkflowAdminServiceWorkflowException;
import org.wso2.carbon.identity.workflow.mgt.stub.bean.Parameter;
import org.wso2.carbon.identity.workflow.mgt.stub.metadata.Association;
import org.wso2.carbon.identity.workflow.mgt.stub.metadata.Template;
import org.wso2.carbon.identity.workflow.mgt.stub.metadata.WorkflowImpl;
import org.wso2.carbon.identity.workflow.mgt.stub.metadata.WorkflowWizard;
import org.wso2.carbon.um.ws.api.stub.ClaimValue;
import org.wso2.carbon.um.ws.api.stub.PermissionDTO;
import org.wso2.identity.integration.common.clients.usermgt.remote.RemoteUserStoreManagerServiceClient;
import org.wso2.identity.integration.common.clients.workflow.mgt.WorkflowAdminClient;
import org.wso2.identity.integration.test.rest.api.user.common.RESTAPIUserTestBase;
import org.wso2.identity.integration.test.utils.WorkflowConstants;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.rmi.RemoteException;

public class UserApprovalTestBase extends RESTAPIUserTestBase {

    private static final Log log = LogFactory.getLog(UserApprovalTestBase.class);

    protected static String templateId = "MultiStepApprovalTemplate";
    protected static String workflowImplId = "ApprovalWorkflow";
    protected static String addUserWorkflowName = "TestWorkflowAddUserForRest";
    protected WorkflowAdminClient client;
    protected RemoteUserStoreManagerServiceClient usmClient;
    protected MultipleServersManager manager = new MultipleServersManager();
    protected String[] rolesToAdd = {"wfRestRole1", "wfRestRole2", "wfRestRole3"};
    protected String[] userToAdd = {"TestUser1ForSuccessRest", "TestUser2ForSuccessRest", "TestUser3ForSuccessRest"};
    protected String workflowId = null;
    protected String associationId = null;

    /**
     * Enum for pending task's status.
     */
    public enum STATE {

        COMPLETED,
        RESERVED,
        READY
    }

    public enum APPROVAL_STATE {

        PENDING,
        APPROVED,
        REJECTED
    }

    public enum APPROVAL_ACTION {

        CLAIM,
        RELEASE,
        APPROVE,
        REJECT
    }

    public UserApprovalTestBase(TestUserMode userMode) throws Exception {

        super.init(userMode);
        this.context = isServer;
        this.authenticatingUserName = tenantInfo.getContextUser().getUserName();
        this.authenticatingCredential = tenantInfo.getContextUser().getPassword();
        this.tenant = context.getContextTenant().getDomain();

        ConfigurationContext configContext = ConfigurationContextFactory
                .createConfigurationContextFromFileSystem(null
                        , null);
        this.client = new WorkflowAdminClient(this.sessionCookie, backendURL, configContext);
        this.usmClient = new RemoteUserStoreManagerServiceClient(backendURL, this.sessionCookie);
    }

    private void addWorkflow() throws Exception {

        String workflowDescription = "TestWorkflowDescription";
        WorkflowWizard workflowDTO = getWorkflowDTO(addUserWorkflowName, workflowDescription);

        log.info("Adding workflow " + addUserWorkflowName + " to tenant:" + this.tenant);
        this.client.addWorkflow(workflowDTO);

        WorkflowWizard[] workflows = this.client.listWorkflows();
        for (WorkflowWizard workflow : workflows) {
            if (this.addUserWorkflowName.equals(workflow.getWorkflowName()) && workflowDescription.equals(workflow
                    .getWorkflowDescription())) {
                this.workflowId = workflow.getWorkflowId();  //setting for future tests
                break;
            }
        }
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
        parameter.setParamValue("sample approval task");
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

    private void addWorkFlowAssociation() throws Exception {

        String associationName = "TestAddUserAssociationForRest";
        //the following xpath is valid for user with the role "wfRestRole1"
        String condition = "//*[local-name()='parameter'][@name='Roles']/*[local-name()='value']/*[local-name()" +
                "='itemValue']/text()='wfRestRole1'";

        log.info("Adding Association " + associationName + " to tenant:" + this.tenant);
        this.client.addAssociation(this.workflowId, associationName, WorkflowConstants.ADD_USER_EVENT, condition);
        Association[] associations = this.client.listAssociationsForWorkflow(this.workflowId);

        for (Association association : associations) {
            if (associationName.equals(association.getAssociationName())) {
                this.associationId = association.getAssociationId();
                break;
            }
        }
    }

    protected void addAssociationForMatch() throws Exception {

        String userName1 = userToAdd[0];
        String userName2 = userToAdd[1];
        String userName3 = userToAdd[2];

        log.info("Adding users matching the workflow engagement " + addUserWorkflowName + " to tenant " + this.tenant);

        this.usmClient.addUser(userName1, "test12345", new String[]{"wfRestRole1"}, new ClaimValue[0], null,
                false);
        this.usmClient.addUser(userName2, "test12345", new String[]{"wfRestRole1", "wfRestRole2"},
                new ClaimValue[0], null, false);
        this.usmClient.addUser(userName3, "test12345", new String[]{"wfRestRole1", "wfRestRole2",
                "wfRestRole3"}, new ClaimValue[0], null, false);
    }

    protected void waitForWorkflowToDeploy() throws Exception {

        // We have to check whether the service is up and running by calling the generated endpoint.
        String url = super.getBackendURL() + addUserWorkflowName + "Service";
        HttpClient client = new HttpClientFactory().getHttpClient();
        HttpGet request = new HttpGet(url);

        boolean runLoop;
        int count = 1;

        do {
            runLoop = false;
            HttpResponse httpResponse = client.execute(request);

            // If the server response is 500 or it contains "service not available" text or "Operation not found" text,
            // then we have to assume that the service is still not available. So we have to recheck after
            // brief time period.
            if (httpResponse.getStatusLine().getStatusCode() == 500) {
                runLoop = true;
            } else {
                BufferedReader bufferedReader =
                        new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent()));
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    if (line.contains("The service cannot be found for the endpoint reference") ||
                            line.contains("The endpoint reference (EPR) for the Operation not found")) {
                        runLoop = true;
                        break;
                    }
                }
            }

            // Wait 20 times for 5 seconds intervals until the workflow is properly deployed. This is for server based
            // test runners like Jenkins.
            if (count < 20) {
                log.info("Still no luck :(. So going to wait 5 seconds and try " + (20 - count) + " more time(s).");
                Thread.sleep(5000);
            }

            // Give up after 100 seconds or this will be a forever loop.
            if (count >= 20) {
                log.info("No luck. Going to give up. Test will most probably fail.");
                runLoop = false;
            }
            count++;
        } while (runLoop);
    }

    protected void setUpWorkFlowAssociation() throws Exception {

        addWorkflow();
        addWorkFlowAssociation();
        for (String role : rolesToAdd) {
            this.usmClient.addRole(role, new String[0], new PermissionDTO[0]);
        }
    }

    protected void cleanUPWorkFlows() throws Exception {

        this.client.deleteAssociation(associationId);
        this.client.deleteWorkflow(workflowId);
        cleanUpUsersAndRoles();
    }

    private void cleanUpUsersAndRoles() throws Exception {

        try {
            // Remove Users
            for (String user : userToAdd) {
                if (usmClient.isExistingUser(user)) {
                    usmClient.deleteUser(user);
                }
            }

            //Remove Roles
            for (String role : rolesToAdd) {
                usmClient.deleteRole(role);
            }

            client = null;
            usmClient = null;
        } catch (Exception e) {
            log.error("Failure occurred due to :" + e.getMessage(), e);
            throw e;
        }
    }

}

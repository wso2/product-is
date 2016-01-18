/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.sp.mgt.workflow.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.mgt.ApplicationManagementService;
import org.wso2.carbon.identity.workflow.mgt.bean.Entity;
import org.wso2.carbon.identity.workflow.mgt.exception.WorkflowException;
import org.wso2.carbon.identity.workflow.mgt.extension.AbstractWorkflowRequestHandler;
import org.wso2.carbon.identity.workflow.mgt.util.WorkflowDataType;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.workflow.mgt.util.WorkflowRequestStatus;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class SPCreateHandler extends AbstractWorkflowRequestHandler {

    private static final Map<String, String> PARAM_DEFINITION;
    private static Log log = LogFactory.getLog(SPCreateHandler.class);

    static {
        PARAM_DEFINITION = new LinkedHashMap<>();
        PARAM_DEFINITION.put("Application ID", WorkflowDataType.INTEGER_TYPE);
        PARAM_DEFINITION.put("Application Name", WorkflowDataType.STRING_TYPE);
        PARAM_DEFINITION.put("Application Description", WorkflowDataType.STRING_TYPE);
        PARAM_DEFINITION.put("Tenant Domain", WorkflowDataType.STRING_TYPE);
        PARAM_DEFINITION.put("Username", WorkflowDataType.STRING_TYPE);
    }

    @Override
    public void onWorkflowCompletion(String status, Map<String, Object> requestParams, Map<String, Object>
            responseAdditionalParams, int tenantId) throws WorkflowException {

        String applicationName = (String)requestParams.get("Application Name");
        String applicationDescription = (String)requestParams.get("Application Description");
        String tenantDoamin = (String)requestParams.get("Tenant Domain");
        String username = (String)requestParams.get("Username");

        if (WorkflowRequestStatus.APPROVED.toString().equals(status) ||
                WorkflowRequestStatus.SKIPPED.toString().equals(status)) {
            try {

                ApplicationManagementService applicationMgtService = ApplicationManagementService.getInstance();
                ServiceProvider serviceProvider = new ServiceProvider();
                //serviceProvider.setApplicationID(applicationID);
                serviceProvider.setApplicationName(applicationName);
                serviceProvider.setDescription(applicationDescription);
                applicationMgtService.createApplication(serviceProvider, tenantDoamin, username);
            } catch (Exception e) {
                throw new WorkflowException(e.getMessage(), e);
            }
        } else {
            if (retryNeedAtCallback()) {
                unsetWorkFlowCompleted();
            }
            if (log.isDebugEnabled()) {
                log.debug("Adding user is aborted for SP '" + applicationName + "', Reason: Workflow response was " +
                                status);
            }
        }
    }

    @Override
    public boolean retryNeedAtCallback() {
        return true;
    }

    public String getEventId() {
        return "ADD_SP";
    }

    public Map<String, String> getParamDefinitions() {
        return PARAM_DEFINITION;
    }

    public String getFriendlyName() {
        return "Add SP";
    }

    public String getDescription() {
        return "";
    }

    public String getCategory() {
        return "SP Operations";
    }

    public boolean startSPCreateWorkflow (ServiceProvider serviceProvider, String tenantDomain, String userName)
            throws WorkflowException{
        Map<String, Object> wfParams = new HashMap<>();
        Map<String, Object> nonWfParams = new HashMap<>();
        wfParams.put("Application ID",serviceProvider.getApplicationID());
        wfParams.put("Application Name",serviceProvider.getApplicationName());
        wfParams.put("Application Description",serviceProvider.getDescription());
        wfParams.put("Tenant Domain",tenantDomain);
        wfParams.put("Username",userName);
        String uuid = UUID.randomUUID().toString();
        Entity[] entities = new Entity[1];
        entities[0] = new Entity(serviceProvider.getApplicationName(), "SP", -1234);
        if (!Boolean.TRUE.equals(getWorkFlowCompleted()) && !isValidOperation(entities)) {
            throw new WorkflowException("Operation is not valid.");
        }
        boolean state = startWorkFlow(wfParams, nonWfParams, uuid).getExecutorResultState().state();
        return state;
    }

    @Override
    public boolean isValidOperation(Entity[] entities) throws WorkflowException {
        //Check if the operation is valid, eg:- Is there a SP already added and not approved with the same name as
        // this SP.
        return true;
    }
}

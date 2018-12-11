/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.identity.integration.common.clients.template.mgt;

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.template.mgt.TemplateManager;
import org.wso2.carbon.identity.template.mgt.TemplateMgtConstants;
import org.wso2.carbon.identity.template.mgt.exception.TemplateManagementException;
import org.wso2.carbon.identity.template.mgt.model.Template;
import org.wso2.carbon.identity.template.mgt.model.TemplateInfo;
import org.wso2.carbon.identity.template.mgt.ui.dto.TemplateRequestDTO;
import org.wso2.carbon.identity.template.mgt.ui.dto.UpdateTemplateRequestDTO;
import org.wso2.carbon.identity.template.mgt.ui.internal.TemplateManagementUIServiceDataHolder;
import org.wso2.carbon.user.api.AuthorizationManager;
import org.wso2.carbon.user.api.UserStoreException;

import java.util.List;

public class TemplateManagementServiceClient {

    private String loggedInUser;

    public TemplateManagementServiceClient(String loggedInUser) {

        this.loggedInUser = loggedInUser;
    }

    public Template addTemplate(TemplateRequestDTO templateRequestDTO) throws TemplateManagementException {

        handleLoggedInUserAuthorization(TemplateMgtConstants.PERMISSION_TEMPLATE_MGT_ADD);
        Template template = new Template(templateRequestDTO.getTenantId(), templateRequestDTO.getTemplateName(),
                templateRequestDTO.getDescription(), templateRequestDTO.getTemplateScript());
        return getTemplateManager().addTemplate(template);
    }

    public Template getTemplateByName(String templateName) throws TemplateManagementException {

        handleLoggedInUserAuthorization(TemplateMgtConstants.PERMISSION_TEMPLATE_MGT_VIEW);
        return getTemplateManager().getTemplateByName(templateName);
    }

    public Template updateTemplate(String templateName, UpdateTemplateRequestDTO updateTemplateRequestDTO)
            throws TemplateManagementException {

        handleLoggedInUserAuthorization(TemplateMgtConstants.PERMISSION_TEMPLATE_MGT_UPDATE);
        Template updateTemplateRequest = new Template(updateTemplateRequestDTO.getTemplateName(),
                updateTemplateRequestDTO.getDescription(), updateTemplateRequestDTO.getTemplateScript());
        return getTemplateManager().updateTemplate(templateName, updateTemplateRequest);
    }

    public void deleteTemplate(String templateName) throws TemplateManagementException {

        handleLoggedInUserAuthorization(TemplateMgtConstants.PERMISSION_TEMPLATE_MGT_DELETE);
        getTemplateManager().deleteTemplate(templateName);
    }

    public List<TemplateInfo> listTemplates(Integer limit, Integer offset) throws TemplateManagementException {

        handleLoggedInUserAuthorization(TemplateMgtConstants.PERMISSION_TEMPLATE_MGT_LIST);
        return getTemplateManager().listTemplates(limit, offset);
    }

    private TemplateManager getTemplateManager() {

        return TemplateManagementUIServiceDataHolder.getInstance().getTemplateManager();
    }

    private void handleLoggedInUserAuthorization(String permission) throws TemplateManagementException {

        try {
            int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();

            if (StringUtils.isBlank(loggedInUser)) {
                throw new TemplateManagementException(TemplateMgtConstants.ErrorMessages.
                        ERROR_CODE_NO_AUTH_USER_FOUND.getMessage(),
                        TemplateMgtConstants.ErrorMessages.ERROR_CODE_NO_AUTH_USER_FOUND.getCode());
            }
            AuthorizationManager authorizationManager = TemplateManagementUIServiceDataHolder
                    .getInstance().getRealmService()
                    .getTenantUserRealm(tenantId)
                    .getAuthorizationManager();
            if (!authorizationManager.isUserAuthorized(loggedInUser, permission, CarbonConstants.UI_PERMISSION_ACTION)) {
                throw new TemplateManagementException(TemplateMgtConstants.
                        ErrorMessages.ERROR_CODE_USER_NOT_AUTHORIZED.getMessage(),
                        TemplateMgtConstants.ErrorMessages
                                .ERROR_CODE_USER_NOT_AUTHORIZED.getCode());
            }
        } catch (UserStoreException e) {
            throw new TemplateManagementException(TemplateMgtConstants.ErrorMessages.ERROR_CODE_UNEXPECTED.getMessage(),
                    TemplateMgtConstants.ErrorMessages.ERROR_CODE_UNEXPECTED.getCode());
        }
    }
}

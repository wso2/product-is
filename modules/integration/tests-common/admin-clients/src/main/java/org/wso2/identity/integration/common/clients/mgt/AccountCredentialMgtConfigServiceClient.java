/*
* Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.identity.integration.common.clients.mgt;

import org.apache.axis2.AxisFault;
import org.wso2.carbon.identity.mgt.stub.AccountCredentialMgtConfigServiceCallbackHandler;
import org.wso2.carbon.identity.mgt.stub.AccountCredentialMgtConfigServiceIdentityMgtServiceExceptionException;
import org.wso2.carbon.identity.mgt.stub.AccountCredentialMgtConfigServiceStub;
import org.wso2.carbon.identity.mgt.stub.dto.EmailTemplateDTO;
import org.wso2.identity.integration.common.clients.AuthenticateStub;

import java.rmi.RemoteException;

/**
 * Service client for AccountCredentialMgtConfigService
 */
public class AccountCredentialMgtConfigServiceClient {

    private AccountCredentialMgtConfigServiceStub accCredentialMgtConfigStub;
    private final String serviceName = "AccountCredentialMgtConfigService";

    public AccountCredentialMgtConfigServiceClient(String backEndUrl, String sessionCookie)
            throws AxisFault {
        String endPoint = backEndUrl + serviceName;
        accCredentialMgtConfigStub = new AccountCredentialMgtConfigServiceStub(endPoint);
        AuthenticateStub.authenticateStub(sessionCookie, accCredentialMgtConfigStub);
    }

    public AccountCredentialMgtConfigServiceClient(String backEndUrl, String userName,
                                                   String password) throws AxisFault {
        String endPoint = backEndUrl + serviceName;
        accCredentialMgtConfigStub = new AccountCredentialMgtConfigServiceStub(endPoint);
        AuthenticateStub.authenticateStub(userName, password, accCredentialMgtConfigStub);
    }

    /**
     * load the tenant specific Email template configurations
     *
     * @return an array of templates
     * @throws java.rmi.RemoteException
     * @throws AccountCredentialMgtConfigServiceIdentityMgtServiceExceptionException
     */
    public EmailTemplateDTO[] getEmailConfig() throws RemoteException,
                                                      AccountCredentialMgtConfigServiceIdentityMgtServiceExceptionException {
        return accCredentialMgtConfigStub.getEmailConfig();
    }

    /**
     * save the Email template configurations which is specific to tenant
     *
     * @param emailTemplates Email templates to be saved.
     * @throws java.rmi.RemoteException
     * @throws AccountCredentialMgtConfigServiceIdentityMgtServiceExceptionException
     */
    public void saveEmailConfigs(EmailTemplateDTO[] emailTemplates) throws RemoteException,
                                                                           AccountCredentialMgtConfigServiceIdentityMgtServiceExceptionException {
        accCredentialMgtConfigStub.saveEmailConfig(emailTemplates);
    }

    public void startGetEmailConfig(
            AccountCredentialMgtConfigServiceCallbackHandler callbackHandler)
            throws RemoteException {
        accCredentialMgtConfigStub.startgetEmailConfig(callbackHandler);
    }
}

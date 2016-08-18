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

package org.wso2.identity.integration.common.clients.mgt;

import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.governance.stub.bean.Property;
import org.wso2.carbon.identity.governance.stub.IdentityGovernanceAdminServiceIdentityGovernanceExceptionException;
import org.wso2.carbon.identity.governance.stub.IdentityGovernanceAdminServiceStub;
import org.wso2.carbon.identity.governance.stub.bean.ConnectorConfig;

import java.rmi.RemoteException;

public class IdentityGovernanceServiceClient {

    protected IdentityGovernanceAdminServiceStub stub = null;

    protected static Log log = LogFactory.getLog(IdentityGovernanceServiceClient.class);

    public IdentityGovernanceServiceClient(String cookie, String backendServerURL)
            throws Exception {
        try {
            stub = new IdentityGovernanceAdminServiceStub(backendServerURL + "IdentityGovernanceAdminService");
            ServiceClient client = stub._getServiceClient();
            Options option = client.getOptions();
            option.setManageSession(true);
            option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
        } catch (Exception e) {
            throw new Exception("Error occurred while creating TenantIdentityMgtClient Object", e);
        }
    }

    public ConnectorConfig[] getConnectorList() throws RemoteException, IdentityGovernanceAdminServiceIdentityGovernanceExceptionException {
        return stub.getConnectorList();
    }

    public void updateConfigurations (Property[] properties) throws RemoteException, IdentityGovernanceAdminServiceIdentityGovernanceExceptionException {
        stub.updateConfigurations(properties);
    }

}

/*
*Copyright (c) 2005-2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/

package org.wso2.identity.integration.common.clients.user.account.connector;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.user.account.connector.stub.UserAccountConnectorServiceStub;
import org.wso2.carbon.user.mgt.stub.UserAdminStub;

public class UserAccountConnectorServiceClient {

    private UserAccountConnectorServiceStub serviceStub;
    private UserAdminStub userAdminStub;

    private Log log = LogFactory.getLog(UserAccountConnectorServiceClient.class);

    public UserAccountConnectorServiceClient(String cookie, String backendServerURL, ConfigurationContext configCtx)
            throws AxisFault {

        String serviceURL = backendServerURL + "UserAccountConnectorService";
        serviceStub = new UserAccountConnectorServiceStub(configCtx, serviceURL);
        ServiceClient client = serviceStub._getServiceClient();
        Options option = client.getOptions();
        option.setManageSession(true);
        option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);

    }

    /**
     * Create new user account association
     *
     * @param userName
     * @param password
     * @throws Exception
     */
    public void connectUserAccount(String userName, String password) throws Exception {
        serviceStub.connectUserAccount(userName, password);
    }

    /**
     * Delete an existing user account association
     *
     * @param userName
     * @throws Exception
     */
    public void deleteUserAccountConnection(String userName) throws Exception {
        serviceStub.deleteUserAccountConnection(userName);
    }

    /**
     * Get all associated accounts of the logged in user
     *
     * @return
     * @throws Exception
     */
    public String[] getConnectedAccountsOfUser() throws Exception {
        return serviceStub.getConnectedAccountsOfUser();
    }

    /**
     * Switch logged in user account to the required associated user account
     *
     * @param userName
     * @return
     * @throws Exception
     */
    public boolean switchLoggedInUser(String userName) throws Exception {
        return serviceStub.switchLoggedInUser(userName);
    }


}

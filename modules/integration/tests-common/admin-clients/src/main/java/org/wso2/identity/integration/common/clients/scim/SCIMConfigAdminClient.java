/*
*Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.identity.integration.common.clients.scim;

import org.apache.axis2.AxisFault;
import org.wso2.carbon.identity.scim.common.stub.SCIMConfigAdminServiceIdentitySCIMExceptionException;
import org.wso2.carbon.identity.scim.common.stub.SCIMConfigAdminServiceStub;
import org.wso2.carbon.identity.scim.common.stub.config.SCIMProviderDTO;
import org.wso2.identity.integration.common.clients.AuthenticateStub;

import java.rmi.RemoteException;

/**
 * API Class for implentation of the  scimConfigAdminServiceStub which will handle all the scim operations
 */
public class SCIMConfigAdminClient {
    private final String serviceName = "SCIMConfigAdminService";
    private SCIMConfigAdminServiceStub scimConfigAdminServiceStub;

    public SCIMConfigAdminClient(String backEndUrl, String sessionCookie) throws AxisFault {
        String endPoint = backEndUrl + serviceName;
        scimConfigAdminServiceStub = new SCIMConfigAdminServiceStub(endPoint);
        AuthenticateStub.authenticateStub(sessionCookie, scimConfigAdminServiceStub);
    }

    public SCIMConfigAdminClient(String backEndUrl, String userName, String password)
            throws AxisFault {
        String endPoint = backEndUrl + serviceName;
        scimConfigAdminServiceStub = new SCIMConfigAdminServiceStub(endPoint);
        AuthenticateStub.authenticateStub(userName, password, scimConfigAdminServiceStub);
    }

    /**
     * Adds a User provider SCIM
     *
     * @param consumerId
     * @param providerId
     * @param userName
     * @param password
     * @param userEPUrl
     * @param groupEPUrl
     * @throws org.wso2.carbon.identity.scim.common.stub.SCIMConfigAdminServiceIdentitySCIMExceptionException
     *
     * @throws java.rmi.RemoteException
     */
    public void addUserProvider(String consumerId, String providerId, String userName, String
            password, String userEPUrl, String groupEPUrl)
            throws SCIMConfigAdminServiceIdentitySCIMExceptionException, RemoteException {
        SCIMProviderDTO scimProviderDTO = new SCIMProviderDTO();
        scimProviderDTO.setUserName(userName);
        scimProviderDTO.setPassword(password);
        scimProviderDTO.setGroupEPURL(groupEPUrl);
        scimProviderDTO.setProviderId(providerId);
        scimProviderDTO.setUserEPURL(userEPUrl);
        scimConfigAdminServiceStub.addUserProvider(consumerId, scimProviderDTO);
    }

    /**
     * Add a Global provider SCIM
     *
     * @param consumerId
     * @param providerId
     * @param userName
     * @param password
     * @param userEPUrl
     * @param groupEPUrl
     * @throws org.wso2.carbon.identity.scim.common.stub.SCIMConfigAdminServiceIdentitySCIMExceptionException
     *
     * @throws java.rmi.RemoteException
     */
    public void addGlobalProvider(String consumerId, String providerId, String userName, String
            password, String userEPUrl, String groupEPUrl)
            throws SCIMConfigAdminServiceIdentitySCIMExceptionException, RemoteException {
        SCIMProviderDTO scimProviderDTO = new SCIMProviderDTO();
        scimProviderDTO.setUserName(userName);
        scimProviderDTO.setPassword(password);
        scimProviderDTO.setGroupEPURL(groupEPUrl);
        scimProviderDTO.setProviderId(providerId);
        scimProviderDTO.setUserEPURL(userEPUrl);
        scimConfigAdminServiceStub.addGlobalProvider(consumerId, scimProviderDTO);
    }

    /**
     * Delete a User provider
     *
     * @param consumerId
     * @param providerId
     * @throws org.wso2.carbon.identity.scim.common.stub.SCIMConfigAdminServiceIdentitySCIMExceptionException
     *
     * @throws java.rmi.RemoteException
     */
    public void deleteUserProvider(String consumerId, String providerId)
            throws SCIMConfigAdminServiceIdentitySCIMExceptionException, RemoteException {
        scimConfigAdminServiceStub.deleteUserProvider(consumerId, providerId);
    }

    /**
     * Deletes a global Provider
     *
     * @param consumerId
     * @param providerId
     * @throws org.wso2.carbon.identity.scim.common.stub.SCIMConfigAdminServiceIdentitySCIMExceptionException
     *
     * @throws java.rmi.RemoteException
     */
    public void deleteGlobalProvider(String consumerId, String providerId)
            throws SCIMConfigAdminServiceIdentitySCIMExceptionException, RemoteException {
        scimConfigAdminServiceStub.deleteGlobalProvider(consumerId, providerId);
    }

    /**
     * Lista all user Providers
     *
     * @param consumerId
     * @param providerId
     * @return
     * @throws org.wso2.carbon.identity.scim.common.stub.SCIMConfigAdminServiceIdentitySCIMExceptionException
     *
     * @throws java.rmi.RemoteException
     */
    public SCIMProviderDTO[] listUserProviders(String consumerId, String providerId)
            throws SCIMConfigAdminServiceIdentitySCIMExceptionException, RemoteException {
        return scimConfigAdminServiceStub.getAllUserProviders(consumerId);
    }

    /**
     * Lists all Global Providers
     *
     * @param consumerId
     * @param providerId
     * @return
     * @throws org.wso2.carbon.identity.scim.common.stub.SCIMConfigAdminServiceIdentitySCIMExceptionException
     *
     * @throws java.rmi.RemoteException
     */
    public SCIMProviderDTO[] listGlobalProviders(String consumerId, String providerId)
            throws SCIMConfigAdminServiceIdentitySCIMExceptionException, RemoteException {
        return scimConfigAdminServiceStub.getAllGlobalProviders(consumerId);
    }
}

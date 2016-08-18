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

package org.wso2.identity.integration.common.clients.user.store.config;

import org.apache.axis2.AxisFault;
import org.wso2.carbon.identity.user.store.configuration.stub.UserStoreConfigAdminServiceStub;
import org.wso2.carbon.identity.user.store.configuration.stub.api.Properties;
import org.wso2.carbon.identity.user.store.configuration.stub.dto.PropertyDTO;
import org.wso2.carbon.identity.user.store.configuration.stub.dto.UserStoreDTO;
import org.wso2.carbon.integration.common.admin.client.utils.AuthenticateStubUtil;


public class UserStoreConfigAdminServiceClient {

    private UserStoreConfigAdminServiceStub stub;

    /**
     * Constructor UserStoreConfigAdminServiceClient
     *
     * @param sessionCookie    - session cookie
     * @param backendServerURL - backend server URL
     */
    public UserStoreConfigAdminServiceClient(String backendServerURL, String sessionCookie) throws AxisFault {
        String serviceURL = backendServerURL + "UserStoreConfigAdminService";
        stub = new UserStoreConfigAdminServiceStub(serviceURL);
        AuthenticateStubUtil.authenticateStub(sessionCookie, stub);

    }

    /**
     * Constructor UserStoreConfigAdminServiceClient
     *
     * @param userName         - user name
     * @param backendServerURL - backend server URL
     */
    public UserStoreConfigAdminServiceClient(String backendServerURL, String userName, String password) throws AxisFault {
        String serviceURL = backendServerURL + "UserStoreConfigAdminService";
        stub = new UserStoreConfigAdminServiceStub(serviceURL);
        AuthenticateStubUtil.authenticateStub(userName, password, stub);

    }


    /**
     * Get all the configured domains
     *
     * @throws Exception
     * @return: active domains
     */
    public UserStoreDTO[] getActiveDomains() throws Exception {
        return stub.getSecondaryRealmConfigurations();
    }

    /**
     * Get available user store implementations
     *
     * @return : available user store managers
     * @throws Exception
     */
    public String[] getAvailableUserStoreClasses() throws Exception {
        return stub.getAvailableUserStoreClasses();

    }

    /**
     * Get properties required for the given user store
     *
     * @param className : list of properties required by each user store manager
     * @return : list of properties(mandatory+optional)
     * @throws Exception
     */
    public Properties getUserStoreProperties(String className) throws Exception {
        return stub.getUserStoreManagerProperties(className);

    }


    /**
     * Save configuration to file system
     *
     * @param userStoreDTO : representation of new user store to be persisted
     * @throws Exception
     */
    public void addUserStore(UserStoreDTO userStoreDTO) throws Exception {
        stub.addUserStore(userStoreDTO);
    }

    /**
     * Deletes a given list of user stores
     *
     * @param userStores : domain names of user stores to deleted
     * @throws Exception
     */
    public void deleteUserStoresSet(String[] userStores) throws Exception {
        stub.deleteUserStoresSet(userStores);
    }

    /**
     * Deletes a given user store
     *
     * @param userStore : domain name of the user store to deleted
     * @throws Exception
     */
    public void deleteUserStore(String userStore) throws Exception {
        stub.deleteUserStore(userStore);
    }

    /**
     * Toggle user store state (enable/disable)
     *
     * @param domain     : domain name of the user store to enable/dissable
     * @param isDisabled : set true to disable user store
     * @throws Exception
     */
    public void changeUserStoreState(String domain, boolean isDisabled) throws Exception {
        stub.changeUserStoreState(domain, isDisabled);
    }

    /**
     * Rename user store including property changes
     *
     * @param previousDomain Previous domain name of the user store
     * @param userStoreDTO   New properties of the user store
     * @throws Exception
     */
    public void updateUserStoreWithDomainName(String previousDomain, UserStoreDTO userStoreDTO) throws Exception {
        if (previousDomain != null && !"".equals(previousDomain) && !previousDomain.equalsIgnoreCase(userStoreDTO.getDomainId())) {
            stub.editUserStoreWithDomainName(previousDomain, userStoreDTO);
        } else {
            this.updateUserStore(userStoreDTO);
        }
    }

    /**
     * Update user store without changing the domain name
     *
     * @param userStoreDTO New properties of the user store
     * @throws Exception
     */
    public void updateUserStore(UserStoreDTO userStoreDTO) throws Exception {
        stub.editUserStore(userStoreDTO);
    }

    /**
     * Create UserDTO to simulate UI call
     *
     * @param className
     * @param domainId
     * @param properties
     * @return
     */
    public UserStoreDTO createUserStoreDTO(String className, String domainId, PropertyDTO[] properties) {
        UserStoreDTO userStoreDTO = new UserStoreDTO();
        userStoreDTO.setClassName(className);
        userStoreDTO.setDomainId(domainId);
        userStoreDTO.setProperties(properties);
        return userStoreDTO;
    }
}

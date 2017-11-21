/*
*  Copyright (c) 2014 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.identity.integration.common.utils;

import org.wso2.carbon.identity.user.store.configuration.stub.dto.UserStoreDTO;
import org.wso2.identity.integration.common.clients.user.store.config.UserStoreConfigAdminServiceClient;

/**
 * The Util class which carries common functionality required by the user store configuration scenarios
 */
public class UserStoreConfigUtils {

    public boolean waitForUserStoreDeployment(UserStoreConfigAdminServiceClient userStoreConfigAdminServiceClient,
                                              String domain) throws Exception {

        long waitTime = System.currentTimeMillis() + 30000; //wait for 45 seconds
        while (System.currentTimeMillis() < waitTime) {
            UserStoreDTO[] userStoreDTOs = userStoreConfigAdminServiceClient.getActiveDomains();
            for (UserStoreDTO userStoreDTO : userStoreDTOs) {
                if (userStoreDTO != null) {
                    if (userStoreDTO.getDomainId().equalsIgnoreCase(domain)) {
                        return true;
                    }
                }
            }
            Thread.sleep(500);
        }
        return false;
    }

    public boolean waitForUserStoreUnDeployment(UserStoreConfigAdminServiceClient userStoreConfigAdminServiceClient,
                                                String domain) throws Exception {

        long waitTime = System.currentTimeMillis() + 20000; //wait for 15 seconds
        while (System.currentTimeMillis() < waitTime) {
            UserStoreDTO[] userStoreDTOs = userStoreConfigAdminServiceClient.getActiveDomains();
            userStoreConfigAdminServiceClient.getActiveDomains();
            for (UserStoreDTO userStoreDTO : userStoreDTOs) {
                if (userStoreDTO != null) {
                    if (userStoreDTO.getDomainId().equalsIgnoreCase(domain)) {
                        Thread.sleep(500);
                    }
                }
            }
        }
        return true;
    }
}

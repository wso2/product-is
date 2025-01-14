/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.user.core.common;

import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.common.AbstractUserStoreManager;

import java.util.HashMap;
import java.util.Map;

/**
 * This class is used to manage unique user ids.
 */
public class UserUniqueIDManger {

    /**
     * Add unique id to the user.
     * @param username Username in the user store.
     * @return User object with unique user id.
     */
    public User addUniqueIdToUser(String username, String profileName, AbstractUserStoreManager userStoreManager)
            throws UserStoreException {

        Map<String, String> claims = new HashMap<>();
        String uniqueId = generateUniqueId();

        claims.put("http://wso2.org/claims/userid", uniqueId);
        userStoreManager.setUserClaimValues(username, claims, profileName);

        return new User(username, uniqueId, userStoreManager.getRealmConfiguration().getUserStoreProperty("DomainName"));
    }

    /**
     * Add new user and create a unique user id for that user.
     * @param username Username in the user store.
     * @return User object with unique user id.
     */
    @Deprecated
    public User addUser(String username, String profileName, AbstractUserStoreManager userStoreManager)
            throws UserStoreException {

        return addUniqueIdToUser(username, profileName, userStoreManager);
    }

    private String generateUniqueId() {

        return java.util.UUID.randomUUID().toString();
    }
}

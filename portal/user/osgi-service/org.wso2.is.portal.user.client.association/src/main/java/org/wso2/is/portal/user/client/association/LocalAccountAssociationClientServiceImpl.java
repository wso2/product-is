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

package org.wso2.is.portal.user.client.association;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.security.caas.user.core.bean.User;

import java.util.Collections;
import java.util.Map;

/**
 * Implementation of User association service.
 */
public class LocalAccountAssociationClientServiceImpl implements LocalAccountAssociationClientService {

    private static final Logger logger = LoggerFactory.getLogger(LocalAccountAssociationClientServiceImpl.class);

    @Override
    public Map<String, User> listUserAssociations(User primaryUser) {

        return Collections.emptyMap();
    }

    @Override
    public User addUserAssociation(User primaryUser, User associatedUser) throws UserAccountAssociationException {
        if (primaryUser == null) {
            throw new UserAccountAssociationException("The primary user is null");
        }
        if (associatedUser == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("The supplied association to the user " + primaryUser.getUserId() + " is null.");
            }
            return null;
        }
        if (primaryUser.getUserId().equals(associatedUser.getUserId())) {
            throw new UserAccountAssociationException(
                    "The user is associated to itself. Offending user ID: " + primaryUser.getUserId());
        }

        return associatedUser;
    }

    @Override
    public User removeUserAssociation(User primaryUser, User associatedUser) throws UserAccountAssociationException {
        if (primaryUser == null) {
            throw new UserAccountAssociationException("The primary user is null");
        }
        if (associatedUser == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("The supplied association to the user " + primaryUser.getUserId() + " is null.");
            }
            return null;
        }

        return associatedUser;
    }

    @Override
    public User getAssociationCandidateUser(String userName, String password, String domain) {
        return new User.UserBuilder().setUserId(userName).build();
    }
}
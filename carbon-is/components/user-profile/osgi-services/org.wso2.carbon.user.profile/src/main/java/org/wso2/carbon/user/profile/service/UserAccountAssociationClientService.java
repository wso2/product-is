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

package org.wso2.carbon.user.profile.service;

import org.wso2.carbon.security.caas.user.core.bean.User;

import java.util.Map;

/**
 * Service to manage associations of other accounts which is associated with a user.
 * The service allows associate another account which a User owns, so that he can choose
 * between different accounts when logging into a service provider or switch between different
 * accounts after logging into a service provider.
 * This allows viewing the existing associations and delete unwanted associations for a User.
 */
public interface UserAccountAssociationClientService {

    /**
     * Lists the current user associations available for the given user.
     *
     * @param primaryUser
     * @return Map of "user-id" vs User of any user associations. Returns an empty map is no associations
     *  found.
     */
    Map<String, User> listUserAssociations(User primaryUser);

    /**
     * Adds the new user association to the given Primary User.
     * @param primaryUser   The user which the user association happens.
     * @param associatedUser  The associated User.
     * @return the Added associated user.
     * @throws UserProfileManagementException when there is any error in adding the association.
     */
    User addUserAssociation(User primaryUser, User associatedUser) throws UserProfileManagementException;

    /**
     * Removes the given user association from the primary User.
     * @param primaryUser
     * @param associatedUser
     * @return
     * @throws UserProfileManagementException when there is any error in removing the user association.
     */
    User removeUserAssociation(User primaryUser, User associatedUser) throws UserProfileManagementException;

    /**
     * Authenticate the associated user with the given UserName and password with the domain provided.
     * Then returns the User bean so that it can be associated to a primary user.
     *
     * @param userName
     * @param password
     * @param domain
     * @return
     */
    User getAssociationCandidateUser(String userName, String password, String domain);
}

/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.identity.user.mgt.store.connector;

import org.wso2.carbon.security.caas.user.core.bean.Attribute;
import org.wso2.carbon.security.caas.user.core.exception.IdentityStoreException;
import org.wso2.carbon.security.caas.user.core.store.connector.IdentityStoreConnector;

import java.util.List;

/**
 * Extended Identity Store Connector which provides write capability.
 *
 * @since 1.0.0
 */
public interface PrivilegedIdentityStoreConnector extends IdentityStoreConnector {

    /**
     * Adds a new user.
     *
     * @param attributes Attributes of the user.
     * @throws IdentityStoreException Identity store exception.
     */
    void addUser(List<Attribute> attributes) throws IdentityStoreException;

    /**
     * Adds new users.
     *
     * @param attributes Attributes of the users.
     * @throws IdentityStoreException Identity store exception.
     */
    void addUsers(List<List<Attribute>> attributes) throws IdentityStoreException;

    /**
     * Update all attributes of a user.
     *
     * @param userIdentifier User identifier.
     * @param attributes Attribute values to update.
     * @throws IdentityStoreException Identity store exception.
     */
    void updateUserAttributes(String userIdentifier, List<Attribute> attributes) throws IdentityStoreException;

    /**
     * Update selected attributes of a user.
     *
     * @param userIdentifier User identifier.
     * @param attributesToAdd Attribute values to add.
     * @param attributesToRemove Attribute values to remove.
     * @throws IdentityStoreException Identity store exception.
     */
    void updateUserAttributes(String userIdentifier, List<Attribute> attributesToAdd, List<Attribute>
            attributesToRemove) throws IdentityStoreException;

    /**
     * Delete a user.
     *
     * @param userIdentifier User identifier.
     * @throws IdentityStoreException Identity store exception.
     */
    void deleteUser(String userIdentifier) throws IdentityStoreException;

    /**
     * Update group list of user.
     *
     * @param userIdentifier User identifier.
     * @param groupIds Group identifiers.
     * @throws IdentityStoreException Identity store exception.
     */
    void updateGroupsOfUser(String userIdentifier, List<String> groupIds) throws IdentityStoreException;

    /**
     * Update selected group list of user.
     *
     * @param userIdentifier User identifier.
     * @param groupIdsToAdd Group identifier list to update.
     * @param groupIdsToRemove Group identifier list to remove.
     * @throws IdentityStoreException Identity store exception.
     */
    void updateGroupsOfUser(String userIdentifier, List<String> groupIdsToAdd, List<String> groupIdsToRemove) throws
            IdentityStoreException;

    /**
     * Adds a new group.
     *
     * @param attributes Attributes of the group.
     * @throws IdentityStoreException Identity store exception.
     */
    void addGroup(List<Attribute> attributes) throws IdentityStoreException;

    /**
     * Adds new groups.
     *
     * @param attributes Attributes of the groups.
     * @throws IdentityStoreException Identity store exception.
     */
    void addGroups(List<List<Attribute>> attributes) throws IdentityStoreException;

    /**
     * Update all attributes of a group.
     *
     * @param groupIdentifier Group identifier.
     * @param attributes Attribute values to update.
     * @throws IdentityStoreException Identity store exception.
     */
    void updateGroupAttributes(String groupIdentifier, List<Attribute> attributes) throws IdentityStoreException;

    /**
     * Update selected attributes of a group.
     *
     * @param groupIdentifier Group identifier.
     * @param attributesToAdd Attribute values to update.
     * @param attributesToRemove Attribute values to remove.
     * @throws IdentityStoreException Identity store exception.
     */
    void updateGroupAttributes(String groupIdentifier, List<Attribute> attributesToAdd, List<Attribute>
            attributesToRemove) throws IdentityStoreException;

    /**
     * Delete a group.
     *
     * @param groupIdentifier Group identifier.
     * @throws IdentityStoreException Identity store exception.
     */
    void deleteGroup(String groupIdentifier) throws IdentityStoreException;

    /**
     * Update user list of a group.
     *
     * @param groupIdentifier Group identifier.
     * @param userIds User identifier list.
     * @throws IdentityStoreException Identity store exception.
     */
    void updateUsersOfGroup(String groupIdentifier, List<String> userIds) throws IdentityStoreException;

    /**
     *  Update selected user list of a group.
     *
     * @param groupIdentifier Group identifier.
     * @param userIdsToAdd User identifier list to add.
     * @param userIdsToRemove User identifier list to remove.
     * @throws IdentityStoreException Identity store exception.
     */
    void updateUsersOfGroup(String groupIdentifier, List<String> userIdsToAdd, List<String> userIdsToRemove) throws
            IdentityStoreException;
}

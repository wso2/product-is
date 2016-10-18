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

package org.wso2.carbon.identity.user.mgt.store;

import org.wso2.carbon.identity.user.mgt.model.GroupModel;
import org.wso2.carbon.identity.user.mgt.model.UserModel;
import org.wso2.carbon.security.caas.user.core.bean.Group;
import org.wso2.carbon.security.caas.user.core.bean.User;
import org.wso2.carbon.security.caas.user.core.claim.Claim;
import org.wso2.carbon.security.caas.user.core.exception.IdentityStoreException;
import org.wso2.carbon.security.caas.user.core.store.IdentityStore;

import java.util.List;

/**
 * PrivilegedIdentityStore which provides write capability.
 *
 * @since 1.0.0
 */
public interface PrivilegedIdentityStore extends IdentityStore {

    /**
     * Add new user to the default domain.
     *
     * @param user User model.
     * @return Created user.
     * @throws IdentityStoreException Identity store exception.
     */
    User addUser(UserModel user) throws IdentityStoreException;

    /**
     * Add new user to a specific domain.
     *
     * @param user  User model.
     * @param domain User domain.
     * @return Created user.
     * @throws IdentityStoreException Identity store exception.
     */
    User addUser(UserModel user, String domain) throws IdentityStoreException;

    /**
     * Add new users to the default domain.
     *
     * @param users User models.
     * @return Created users.
     * @throws IdentityStoreException Identity store exception.
     */
    List<User> addUsers(List<UserModel> users) throws IdentityStoreException;

    /**
     * Add new users to a specific domain.
     *
     * @param users User models.
     * @param domain User domain.
     * @return Created users.
     * @throws IdentityStoreException Identity store exception.
     */
    List<User> addUsers(List<UserModel> users, String domain) throws IdentityStoreException;

    /**
     * Update user claims by user id.
     *
     * @param userId User uuid.
     * @param userClaims User claims.
     * @throws IdentityStoreException Identity store exception.
     */
    void updateUserClaims(String userId, List<Claim> userClaims) throws IdentityStoreException;

    /**
     * Update selected user claims by user id.
     *
     * @param userId User uuid.
     * @param userClaimsToAdd user claims to update.
     * @param userClaimsToRemove user claims to remove.
     * @throws IdentityStoreException Identity store exception.
     */
    void updateUserClaims(String userId, List<Claim> userClaimsToAdd, List<Claim> userClaimsToRemove) throws
            IdentityStoreException;

    /**
     * Delete a user by user id.
     *
     * @param userId User uuid.
     * @throws IdentityStoreException Identity store exception.
     */
    void deleteUser(String userId) throws IdentityStoreException;

    /**
     * Update groups of a user by user id.
     *
     * @param userId User uuid.
     * @param groupIds Group uuid list.
     * @throws IdentityStoreException Identity store exception.
     */
    void updateGroupsOfUser(String userId, List<String> groupIds) throws IdentityStoreException;

    /**
     * Update selected groups of a user by user id.
     *
     * @param userId User uuid.
     * @param groupIdsToAdd Group ids to add.
     * @param groupIdsToRemove Group ids to remove.
     * @throws IdentityStoreException Identity store exception.
     */
    void updateGroupsOfUser(String userId, List<String> groupIdsToAdd, List<String> groupIdsToRemove) throws
            IdentityStoreException;

    /**
     * Add new group to the default domain.
     *
     * @param groupModel Group model.
     * @return Created group.
     * @throws IdentityStoreException Identity store exception.
     */
    Group addGroup(GroupModel groupModel) throws IdentityStoreException;

    /**
     * Add new group to the specific domain.
     *
     * @param groupModel Group model.
     * @param domain Group damian.
     * @return Created group.
     * @throws IdentityStoreException Identity store exception.
     */
    Group addGroup(GroupModel groupModel, String domain) throws IdentityStoreException;

    /**
     * Add new groups to the default domain.
     *
     * @param groups Group models.
     * @return Created groups.
     * @throws IdentityStoreException Identity store exception.
     */
    List<Group> addGroups(List<GroupModel> groups) throws IdentityStoreException;

    /**
     * Add new groups to the specific domain.
     *
     * @param groups Group models.
     * @param domain Group domain.
     * @return Created groups.
     * @throws IdentityStoreException Identity store exception.
     */
    List<Group> addGroups(List<GroupModel> groups, String domain) throws IdentityStoreException;

    /**
     * Update group claims by group id.
     *
     * @param groupId Group uuid.
     * @param groupClaims Group claims.
     * @throws IdentityStoreException Identity store exception.
     */
    void updateGroupClaims(String groupId, List<Claim> groupClaims) throws IdentityStoreException;

    /**
     *  Update selected group claims by group id.
     *
     * @param groupId Group uuid.
     * @param groupClaimsToAdd Group ids to add.
     * @param groupClaimsToRemove Group ids to remove.
     * @throws IdentityStoreException Identity store exception.
     */
    void updateGroupClaims(String groupId, List<Claim> groupClaimsToAdd, List<Claim> groupClaimsToRemove) throws
            IdentityStoreException;

    /**
     * Deleate a group by group id.
     *
     * @param groupId Group uuid.
     * @throws IdentityStoreException Identity store exception.
     */
    void deleteGroup(String groupId) throws IdentityStoreException;

    /**
     * Update users of a group by group id.
     *
     * @param groupId Group uuid.
     * @param userIds User uuid list.
     * @throws IdentityStoreException Identity store exception.
     */
    void updateUsersOfGroup(String groupId, List<String> userIds) throws IdentityStoreException;

    /**
     * Update selected users of a group by group id.
     *
     * @param groupId Group uuid.
     * @param userIdsToAdd User uuid list to add.
     * @param userIdsToRemove User uuid list to remove.
     * @throws IdentityStoreException Identity store exception.
     */
    void updateUsersOfGroup(String groupId, List<String> userIdsToAdd, List<String> userIdsToRemove) throws
            IdentityStoreException;
}


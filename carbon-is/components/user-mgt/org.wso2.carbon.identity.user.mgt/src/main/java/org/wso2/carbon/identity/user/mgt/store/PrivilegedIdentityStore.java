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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
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

    User addUser(UserModel users) throws IdentityStoreException;

    User addUser(UserModel users, String domain) throws IdentityStoreException;

    List<User> addUsers(List<UserModel> users) throws IdentityStoreException;

    List<User> addUsers(List<UserModel> users, String domain) throws IdentityStoreException;

    void updateUserClaims(String userId, List<Claim> userClaims) throws IdentityStoreException;

    void updateUserClaims(String userId, List<Claim> userClaims, String domain) throws IdentityStoreException;

    void updateUserClaims(String userId, List<Claim> userClaimsToUpdate, List<Claim> userClaimsToRemove) throws
            IdentityStoreException;

    void updateUserClaims(String userId, List<Claim> userClaimsToUpdate, List<Claim> userClaimsToRemove, String
            domain) throws IdentityStoreException;

    void deleteUser(String userId) throws IdentityStoreException;

    void deleteUser(String userId, String domain) throws IdentityStoreException;

    void updateGroupsOfUser(String userId, List<GroupModel> groups) throws IdentityStoreException;

    void updateGroupsOfUser(String userId, List<GroupModel> groups, String domain) throws IdentityStoreException;

    void updateGroupsOfUser(String userId, List<GroupModel> groupsToUpdate, List<GroupModel> groupsToRemove) throws
            IdentityStoreException;

    void updateGroupsOfUser(String userId, List<GroupModel> groupsToUpdate, List<GroupModel> groupsToRemove, String
            domain) throws IdentityStoreException;


    Group addGroup(GroupModel groupModel) throws IdentityStoreException;

    Group addGroup(GroupModel groupModel, String domain) throws IdentityStoreException;

    List<Group> addGroups(List<GroupModel> groups) throws IdentityStoreException;

    List<Group> addGroups(List<GroupModel> groups, String domain) throws IdentityStoreException;

    Group updateGroupClaims(String groupId, List<Claim> groupClaims) throws IdentityStoreException;

    Group updateGroupClaims(String groupId, List<Claim> groupClaims, String domain) throws IdentityStoreException;

    Group updateGroupClaims(String groupId, List<Claim> groupClaimsToUpdate, List<Claim> groupClaimsToRemove) throws
            IdentityStoreException;

    Group updateGroupClaims(String groupId, List<Claim> groupClaimsToUpdate, List<Claim> groupClaimsToRemove, String
            domain) throws IdentityStoreException;

    void deleteGroup(String groupId) throws IdentityStoreException;

    void deleteGroup(String groupId, String domain) throws IdentityStoreException;

    void updateUsersOfGroup(String groupId, List<UserModel> users) throws IdentityStoreException;

    void updateUsersOfGroup(String groupId, List<UserModel> users, String domain) throws IdentityStoreException;

    void updateUsersOfGroup(String groupId, List<UserModel> usersToUpdate, List<UserModel> usersToRemove) throws
            IdentityStoreException;

    void updateUsersOfGroup(String groupId, List<UserModel> usersToUpdate, List<UserModel> usersToRemove, String
            domain) throws IdentityStoreException;

}


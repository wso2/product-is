/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.is.portal.user.client.api;

import org.wso2.carbon.identity.claim.mapping.profile.ClaimConfigEntry;
import org.wso2.carbon.identity.mgt.bean.GroupBean;
import org.wso2.carbon.identity.mgt.claim.Claim;
import org.wso2.carbon.identity.mgt.claim.MetaClaim;
import org.wso2.carbon.identity.mgt.exception.UserNotFoundException;
import org.wso2.is.portal.user.client.api.bean.UUFGroup;
import org.wso2.is.portal.user.client.api.bean.UUFUser;
import org.wso2.is.portal.user.client.api.bean.UserListBean;
import org.wso2.is.portal.user.client.api.exception.UserPortalUIException;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Perform operations related to password handling.
 */
public interface IdentityStoreClientService {

    /**
     * Authenticate.
     *
     * @param username username
     * @param password password
     * @return authentication context
     * @throws UserPortalUIException Authentication Failure
     */
    UUFUser authenticate(String username, char[] password, String domain)
            throws UserPortalUIException;

    /**
     * Update user password.
     *
     * @param username    username
     * @param oldPassword old password
     * @param newPassword new password
     * @throws UserNotFoundException User Not Found Exception
     */
    void updatePassword(String username, char[] oldPassword, char[] newPassword, String domain)
            throws UserNotFoundException, UserPortalUIException;

    /**
     * Add new user to the default domain.
     *
     * @param userClaims  claims of the user
     * @param credentials credentials of the user
     * @return Created user.
     * @throws UserPortalUIException User Not Found Exception
     */
    UUFUser addUser(Map<String, String> userClaims, Map<String, String> credentials) throws UserPortalUIException;

    /**
     * Add new user to the defined domain.
     *
     * @param userClaims  claims of the user
     * @param credentials credentials of the user
     * @param domainName  domain name of the user
     * @return Created user
     * @throws UserPortalUIException
     */
    UUFUser addUser(Map<String, String> userClaims, Map<String, String> credentials, String domainName)
            throws UserPortalUIException;

    /**
     * Check user existence in a specific domain.
     *
     * @param userClaims claims of the user
     * @param domain     domain name of the user
     * @return True if user exists
     * @throws UserPortalUIException
     */
    boolean isUserExist(Map<String, String> userClaims, String domain) throws UserPortalUIException;

    /**
     * Check user existence across domains.
     *
     * @param userClaims claims of the user
     * @return meta data of user existence check
     * @throws UserPortalUIException
     */
    List<String> isUserExist(Map<String, String> userClaims) throws UserPortalUIException;

    /**
     * Update user claims by user id.
     *
     * @param uniqueUserId     User unique id
     * @param updatedClaimsMap Updated user claims map
     * @throws UserPortalUIException User portal ui exception
     */
    void updateUserProfile(String uniqueUserId, Map<String, String> updatedClaimsMap) throws UserPortalUIException;

    void updateGroupProfile(String uniqueGroupId, Map<String, String> updatedClaimsMap) throws
            UserPortalUIException;

    /**
     * Get list of user claims by user id.
     *
     * @param uniqueUserId User unique id
     * @param metaClaims   Meta claim map
     * @return List of user claims
     * @throws UserPortalUIException
     */
    List<Claim> getClaimsOfUser(String uniqueUserId, List<MetaClaim> metaClaims) throws UserPortalUIException;

    List<Claim> getClaimsOfGroup(String uniqueGroupId, List<MetaClaim> metaClaims) throws UserPortalUIException;

    /**
     * Get list of domain names
     *
     * @return Domain name List
     * @throws UserPortalUIException
     */
    Set<String> getDomainNames() throws UserPortalUIException;

    /**
     * Get primary domain name
     *
     * @return Primary domain name
     * @throws UserPortalUIException
     */
    String getPrimaryDomainName() throws UserPortalUIException;

    /**
     * List users by claim
     * @param claimUri claim uri for filtering
     * @param claimValue claim value
     * @param offset starting point of user list
     * @param length number of users to be returned
     * @param domainName domain name
     * @return list of users
     * @throws UserPortalUIException
     */
    List<UUFUser> listUsers(String claimUri, String claimValue, int offset, int length,
                            String domainName) throws UserPortalUIException;

    List<UserListBean> getFilteredList(int offset, int length, String claimURI,
                                       String claimValue, String domainName,
                                       List<ClaimConfigEntry> requestedClaims) throws UserPortalUIException;

    List<UserListBean> getUserList(int offset, int length, String domainName,
            List<ClaimConfigEntry> requestedClaims) throws UserPortalUIException;

    /**
     * Add a group
     *
     * @param group      object with claims of the group
     * @param domainName domain where the group should be added
     * @return uniquegrouid and other attributes of the created group
     * @throws UserPortalUIException
     */
    UUFGroup addGroup(GroupBean group, String domainName) throws UserPortalUIException;

    /**
     * Add a group to the given domain
     *
     * @param groupClaims claims of the group
     * @param domainName  domain where the group should be added
     * @return uniquegrouid and other attributes of the created group
     * @throws UserPortalUIException
     */
    UUFGroup addGroup(Map<String, String> groupClaims, String domainName) throws UserPortalUIException;

    /**
     * Check whether a group exist with given claims.
     *
     * @param groupClaims claim for existence check
     * @param domain      domain to be checked
     * @return True if group exists
     * @throws UserPortalUIException
     */
    boolean isGroupExist(Map<String, String> groupClaims, String domain) throws UserPortalUIException;

    /**
     * Add a set of users to a group.
     *
     * @param groupId unique ID of the group
     * @param userIds unique IDs of the users to be added to the group
     * @throws UserPortalUIException
     */
    void addUsersToGroup(String groupId, List<String> userIds) throws UserPortalUIException;

    /**
     * Update a group with combination of addition and removal.
     *
     * @param groupId       unique ID of the group to be updated
     * @param addingUsers   List of user IDs to be added to group
     * @param removingUsers List of user IDs to be removed from group
     * @throws UserPortalUIException
     */
    void updateUsersInGroup(String groupId, List<String> addingUsers, List<String> removingUsers)
            throws UserPortalUIException;

    /**
     * Check whether a user is in a group.
     *
     * @param userId  unique ID of the user
     * @param groupId unique ID of the group
     * @return True if user belongs to the group
     * @throws UserPortalUIException
     */
    boolean isUserInGroup(String userId, String groupId) throws UserPortalUIException;

    /**
     * Update the group with the given unique group id
     *
     * @param uniqueGroupId    unique ID of the group to be updated
     * @param updatedClaimsMap claims with values to be updated.
     * @throws UserPortalUIException
     */
    void updateGroup(String uniqueGroupId, Map<String, String> updatedClaimsMap)
            throws UserPortalUIException;
}


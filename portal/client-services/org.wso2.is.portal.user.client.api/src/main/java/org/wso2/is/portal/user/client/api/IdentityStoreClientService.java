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

import org.wso2.carbon.identity.mgt.claim.Claim;
import org.wso2.carbon.identity.mgt.claim.MetaClaim;
import org.wso2.carbon.identity.mgt.exception.UserNotFoundException;
import org.wso2.is.portal.user.client.api.bean.UUFUser;
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
    Map<String, String> isUserExist(Map<String, String> userClaims) throws UserPortalUIException;

    /**
     * Update user claims by user id.
     *
     * @param uniqueUserId     User unique id
     * @param updatedClaimsMap Updated user claims map
     * @throws UserPortalUIException User portal ui exception
     */
    void updateUserProfile(String uniqueUserId, Map<String, String> updatedClaimsMap) throws UserPortalUIException;

    /**
     * Get list of user claims by user id.
     *
     * @param uniqueUserId User unique id
     * @param metaClaims   Meta claim map
     * @return List of user claims
     * @throws UserPortalUIException
     */
    List<Claim> getClaimsOfUser(String uniqueUserId, List<MetaClaim> metaClaims) throws UserPortalUIException;

    /**
     * Get list of domain names.
     *
     * @return Domain name List
     * @throws UserPortalUIException
     */
    Set<String> getDomainNames() throws UserPortalUIException;

    /**
     * Get primary domain name.
     *
     * @return Primary domain name
     * @throws UserPortalUIException
     */
    String getPrimaryDomainName() throws UserPortalUIException;
}

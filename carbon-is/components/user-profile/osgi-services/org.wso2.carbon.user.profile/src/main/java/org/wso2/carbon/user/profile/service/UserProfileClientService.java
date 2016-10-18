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

package org.wso2.carbon.user.profile.service;

import org.wso2.carbon.security.caas.user.core.bean.User;
import org.wso2.carbon.security.caas.user.core.claim.Claim;
import org.wso2.carbon.security.caas.user.core.claim.MetaClaim;

import java.util.Collection;
import java.util.List;

/**
 * Interface for the user profile related functionality
 */
public interface UserProfileClientService {

    /**
     * Authenticates the user.
     * @param username
     * @param password
     * @return authenticated User
     */
    User authenticate(String username, String password);

    /**
     * Get the profile details of the given user.
     * @param profileId
     * @param userid
     * @return
     */
    Collection<Claim> getProfile(String profileId, String userid);

    /**
     * Get the default profile of the given user
     * @param userid
     * @return
     */
    Collection<Claim> getProfile(String userid);

    /**
     *
     * @param dialect
     * @param claims
     * @return
     */
    Collection<Claim> getMetaClaims(String dialect, Collection<String> claims);

    /**
     * Get the claim values of the user for default profile.
     * @param user
     * @return
     */
    Collection<Claim> getClaims(User user);

    /**
     * Create a new profile.
     * @param profileId
     */
    void createProfile(String profileId, Collection<Claim> claims);

    /**
     * Create a new profile using a given template.
     * @param templateId
     * @param profileId
     */
    void createProfile(String templateId, String profileId, Collection<Claim> claims);

    /**
     * Update the profile with the given ID using the sent in claims.
     * @param profileId
     */
    void updateProfile(String profileId, Collection<Claim> claims);

    /**
     * Update the profile name.
     * @param profileId
     * @param newName
     */
    void updateProfileName(String profileId, String newName);

    /**
     * Delete a profile by ID.
     * @param profileId
     */
    void deleteProfile(String profileId);

    /**
     * Get the template of a requested profile.
     * @param profileTempleteId
     * @return
     */
    Collection<MetaClaim> getProfileTemplate (String profileTempleteId);

    /**
     * The the list of available templates.
     * @return
     */
    List<Integer> getProfileTemplates ();



}

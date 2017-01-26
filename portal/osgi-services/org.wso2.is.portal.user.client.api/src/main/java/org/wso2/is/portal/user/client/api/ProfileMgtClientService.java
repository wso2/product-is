/*
<<<<<<< HEAD
<<<<<<< HEAD
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
=======
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
>>>>>>> c9a9414... Adding ProfileMgtClientService
=======
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
>>>>>>> 1b5ffcb... Changed year to 2017
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

<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
import org.wso2.carbon.identity.claim.mapping.profile.ProfileEntry;
import org.wso2.is.portal.user.client.api.bean.ProfileUIEntry;
import org.wso2.is.portal.user.client.api.exception.UserPortalUIException;

import java.util.List;
import java.util.Set;

=======
>>>>>>> c9a9414... Adding ProfileMgtClientService
=======
import org.wso2.is.portal.user.client.api.exception.ProfileMgtClientException;
=======
import org.wso2.carbon.identity.meta.claim.mgt.mapping.profile.ProfileEntry;
=======
import org.wso2.carbon.identity.claim.mapping.profile.ProfileEntry;
>>>>>>> 7303600... Adapt package name changing in meta.claim.mgt package.
import org.wso2.is.portal.user.client.api.bean.ProfileUIEntry;
import org.wso2.is.portal.user.client.api.exception.UserPortalUIException;
>>>>>>> 951ade6... Adding profile loading js

import java.util.List;
import java.util.Set;

>>>>>>> c2d029b... Adding profile view dynamically.
=======
import org.wso2.carbon.identity.meta.claim.mgt.exception.ProfileMgtServiceException;
import org.wso2.carbon.identity.meta.claim.mgt.mapping.profile.ProfileEntry;

<<<<<<< HEAD
import java.util.List;
import java.util.Map;

>>>>>>> d702044...  Added backend services for user self sign-up
=======
>>>>>>> b1950fd... Removed unused functions in profile mgt service
/**
 * Profile Mgt Client Service.
 */
public interface ProfileMgtClientService {

<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
    /**
     * Get the names of available profiles
     *
     * @return a set with all the available profile names
     * @throws UserPortalUIException User portal ui exception
     */
    Set<String> getProfileNames() throws UserPortalUIException;

    /**
     * Get the claims set of a profile
     *
     * @param profileName Uniquely identifying name of the profile
     * @return ProfileEntry with the set of claims and their properties
     * @throws UserPortalUIException User portal ui exception
     */
    ProfileEntry getProfile(String profileName) throws UserPortalUIException;

    /**
     * Get the claims set of a profile
     *
     * @param profileName  Uniquely identifying name of the profile
     * @param uniqueUserId Unique user id
     * @return ProfileEntry with the set of claims and their properties
     * @throws UserPortalUIException User portal ui exception
     */
    List<ProfileUIEntry> getProfileEntries(String profileName, String uniqueUserId) throws UserPortalUIException;
=======
>>>>>>> c9a9414... Adding ProfileMgtClientService
=======
    /**
     * Get the names of available profiles
     *
     * @return a set with all the available profile names
     * @throws UserPortalUIException User portal ui exception
     */
<<<<<<< HEAD
    Set<String> getProfileNames() throws ProfileMgtClientException;
>>>>>>> c2d029b... Adding profile view dynamically.
=======
    /**
     * Get the claims set of a profile.
     *
     * @param profileName : Uniquely identifying name of the profile.
     * @return ProfileEntry with the set of claims and their properties.
     * @throws ProfileMgtServiceException : Error in getting the profile.
     */
    ProfileEntry getProfile(String profileName) throws ProfileMgtServiceException;
<<<<<<< HEAD

    /**
     * Get the properties of a particular claim of a profile.
     *
     * @param profileName : Uniquely identifying name of the profile.
     * @param claim       : Root claim URI for the properties to be retrieved.
     * @return Map(Property Key : Property Value)
     * @throws ProfileMgtServiceException : Error in getting the properties of a claim.
     */
    ClaimConfigEntry getClaimAttributes(String profileName, String claim) throws ProfileMgtServiceException;
=======
    Set<String> getProfileNames() throws UserPortalUIException;

    /**
     * Get the claims set of a profile
     *
     * @param profileName Uniquely identifying name of the profile
     * @return ProfileEntry with the set of claims and their properties
     * @throws UserPortalUIException User portal ui exception
     */
    ProfileEntry getProfile(String profileName) throws UserPortalUIException;
>>>>>>> 951ade6... Adding profile loading js

    /**
     * Get the claims marked as required for a particular profile.
     *
     * @param profileName : Uniquely identifying name of the profile.
     * @return List(Root claim URIs)
     * @throws ProfileMgtServiceException : Error in getting the claims with required property.
     */
    List<String> getRequiredClaims(String profileName) throws ProfileMgtServiceException;
>>>>>>> d702044...  Added backend services for user self sign-up

    /**
     * Get the claims marked as read-only for a particular profile.
     *
     * @param profileName : Uniquely identifying name of the profile.
     * @return List(Root claim URIs)
     * @throws ProfileMgtServiceException : Error in getting the claims with read-only property.
     */
    List<String> getReadOnlyClaims(String profileName) throws ProfileMgtServiceException;

    /**
     * Get the claims marked as unique for a particular profile.
     *
     * @param profileName : Uniquely identifying name of the profile.
     * @return List(Root claim URIs)
     * @throws ProfileMgtServiceException : Error in getting the claims with unique property.
     */
    List<String> getUniqueClaims(String profileName) throws ProfileMgtServiceException;

    /**
     * Get the claims marked as verify for a particular profile.
     *
     * @param profileName : Uniquely identifying name of the profile.
     * @return List(Root claim URIs)
     * @throws ProfileMgtServiceException : Error in getting the claims with 'verify' property.
     */
    List<String> getVerifyingClaims(String profileName) throws ProfileMgtServiceException;

    /**
<<<<<<< HEAD
     * Get the claims marked as verify with the verifying mechanism for a particular profile.
     *
     * @param profileName : Uniquely identifying name of the profile.
     * @return Map(Claim : Verifying mechanism)
     * @throws ProfileMgtServiceException : Error in getting the claims with verifying mechanism.
     */
    List<String> getValidatingClaims(String profileName) throws ProfileMgtServiceException;

    /**
     * Get the claims marked for regex validations.
     *
     * @param profileName : Uniquely identifying name of the profile.
     * @return Map(Claim : Regex)
     * @throws ProfileMgtServiceException : Error in getting the claims with regex validations.
     */
    List<String> getTransformingClaims(String profileName) throws ProfileMgtServiceException;
=======
>>>>>>> b1950fd... Removed unused functions in profile mgt service
=======
     * Get the claims set of a profile
     *
     * @param profileName  Uniquely identifying name of the profile
     * @param uniqueUserId Unique user id
     * @return ProfileEntry with the set of claims and their properties
     * @throws UserPortalUIException User portal ui exception
     */
    List<ProfileUIEntry> getProfileEntries(String profileName, String uniqueUserId) throws UserPortalUIException;

>>>>>>> 6c5f9c0... Fixing login issue.
}


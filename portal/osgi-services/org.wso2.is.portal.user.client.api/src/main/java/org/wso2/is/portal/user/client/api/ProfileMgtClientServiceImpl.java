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


package org.wso2.is.portal.user.client.api;

import org.wso2.carbon.identity.meta.claim.mgt.exception.ProfileMgtServiceException;
import org.wso2.carbon.identity.meta.claim.mgt.mapping.profile.ClaimConfigEntry;
import org.wso2.carbon.identity.meta.claim.mgt.mapping.profile.ProfileEntry;
import org.wso2.is.portal.user.client.api.internal.UserPortalClientApiDataHolder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Profile Mgt Client Service Implementation.
 */
public class ProfileMgtClientServiceImpl implements ProfileMgtClientService {

    /**
     * Get the claims set of profiles.
     *
     * @return Map(profileName,(Map(claim, Map(Property Key: Property Value))) with the set of claims and their
     * properties.
     * @throws ProfileMgtServiceException : Error in getting the profile.
     */
    @Override
    public Map<String, ProfileEntry> getProfiles() throws ProfileMgtServiceException {
        return new HashMap<String, ProfileEntry>();
    }

    /**
     * Get the claims set of a profile.
     *
     * @param profileName : Uniquely identifying name of the profile.
     * @return Map(claim, Map(Property Key: Property Value)) with the set of claims and their properties.
     * @throws ProfileMgtServiceException : Error in getting the profile.
     */
    @Override
    public ProfileEntry getProfile(String profileName) throws ProfileMgtServiceException {
        return UserPortalClientApiDataHolder.getInstance().getProfileMgtService().getProfile(profileName);
    }

    /**
     * Get the properties of a particular claim of a profile.
     *
     * @param profileName : Uniquely identifying name of the profile.
     * @param claimURI    : Root claim URI for the properties to be retrieved.
     * @return Map(Property Key : Property Value)
     * @throws ProfileMgtServiceException : Error in getting the properties of a claim.
     */
    @Override
    public ClaimConfigEntry getClaimAttributes(String profileName, String claimURI) throws ProfileMgtServiceException {
        return new ClaimConfigEntry();
    }

    /**
     * Get the claims marked as required for a particular profile.
     *
     * @param profileName : Uniquely identifying name of the profile.
     * @return List(Root claim URIs)
     * @throws ProfileMgtServiceException : Error in getting the claims with required property.
     */
    @Override
    public List<String> getRequiredClaims(String profileName) throws ProfileMgtServiceException {
        return new ArrayList<String>();
    }

    /**
     * Get the claims marked as read-only for a particular profile.
     *
     * @param profileName : Uniquely identifying name of the profile.
     * @return List(Root claim URIs)
     * @throws ProfileMgtServiceException : Error in getting the claims with read-only property.
     */
    @Override
    public List<String> getReadOnlyClaims(String profileName) throws ProfileMgtServiceException {
        return new ArrayList<String>();
    }

    /**
     * Get the claims marked as unique for a particular profile.
     *
     * @param profileName : Uniquely identifying name of the profile.
     * @return List(Root claim URIs)
     * @throws ProfileMgtServiceException : Error in getting the claims with unique property.
     */
    @Override
    public List<String> getUniqueClaims(String profileName) throws ProfileMgtServiceException {
        return new ArrayList<String>();
    }

    /**
     * Get the claims marked as verify for a particular profile.
     *
     * @param profileName : Uniquely identifying name of the profile.
     * @return List(Root claim URIs)
     * @throws ProfileMgtServiceException : Error in getting the claims with 'verify' property.
     */
    @Override
    public List<String> getVerifyingClaims(String profileName) throws ProfileMgtServiceException {
        return new ArrayList<String>();
    }

    /**
     * Get the claims marked as verify with the verifying mechanism for a particular profile.
     *
     * @param profileName : Uniquely identifying name of the profile.
     * @return Map(Claim : Verifying mechanism)
     * @throws ProfileMgtServiceException : Error in getting the claims with verifying mechanism.
     */
    @Override
    public List<String> getValidatingClaims(String profileName) throws ProfileMgtServiceException {
        return new ArrayList<String>();
    }

    /**
     * Get the claims marked for regex validations.
     *
     * @param profileName : Uniquely identifying name of the profile.
     * @return Map(Claim : Regex)
     * @throws ProfileMgtServiceException : Error in getting the claims with regex validations.
     */
    @Override
    public List<String> getTransformingClaims(String profileName) throws ProfileMgtServiceException {
        return new ArrayList<String>();
    }
}

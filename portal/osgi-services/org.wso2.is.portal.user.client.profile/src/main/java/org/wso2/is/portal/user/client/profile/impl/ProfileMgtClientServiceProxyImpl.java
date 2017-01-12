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

package org.wso2.is.portal.user.client.profile.impl;

import org.wso2.carbon.identity.meta.claim.mgt.exception.ProfileMgtServiceException;
import org.wso2.carbon.identity.meta.claim.mgt.exception.ProfileReaderException;
import org.wso2.carbon.identity.meta.claim.mgt.mapping.profile.ClaimConfigEntry;
import org.wso2.carbon.identity.meta.claim.mgt.mapping.profile.ProfileEntry;
import org.wso2.carbon.identity.meta.claim.mgt.mapping.profile.ProfileMappingReader;
import org.wso2.carbon.identity.meta.claim.mgt.service.ProfileMgtService;
import org.wso2.is.portal.user.client.profile.ProfileMgtClientService;
import org.wso2.carbon.kernel.utils.StringUtils;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.identity.mgt.AuthenticationContext;
import org.wso2.carbon.identity.mgt.RealmService;
import org.wso2.carbon.identity.mgt.claim.Claim;
import org.wso2.carbon.identity.mgt.exception.AuthenticationFailure;
import org.wso2.carbon.identity.mgt.exception.IdentityStoreException;
import org.wso2.carbon.identity.mgt.impl.util.IdentityMgtConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Default implementation of User Profile Service
 */
@Component(name = "org.wso2.is.portal.user.client.profile.impl.ProfileMgtClientServiceProxyImpl",
        service =  ProfileMgtClientService.class,
        immediate = true)
public class ProfileMgtClientServiceProxyImpl implements ProfileMgtClientService {

    private ProfileMgtService profileMgtService = null;

    @Activate
    protected void start(final BundleContext bundleContext) {
        // nothing to do
    }

    @Reference(
            name = "profileMgtService",
            service = ProfileMgtService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetProfileMgtService")
    protected void setProfileMgtService(ProfileMgtService profileMgtService) {
        this.profileMgtService = profileMgtService;
    }

    protected void unsetProfileMgtService(ProfileMgtService profileMgtService) {
        this.profileMgtService = null;
    }


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
        return profileMgtService.getProfile(profileName);
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

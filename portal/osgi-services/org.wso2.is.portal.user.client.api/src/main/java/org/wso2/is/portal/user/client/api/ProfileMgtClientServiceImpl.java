/*
<<<<<<< HEAD
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
=======
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
>>>>>>> c9a9414... Adding ProfileMgtClientService
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

<<<<<<< HEAD
package org.wso2.is.portal.user.client.api;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.identity.claim.exception.ProfileMgtServiceException;
import org.wso2.carbon.identity.claim.mapping.profile.ProfileEntry;
import org.wso2.carbon.identity.claim.service.ProfileMgtService;
import org.wso2.carbon.identity.mgt.RealmService;
import org.wso2.carbon.identity.mgt.claim.Claim;
import org.wso2.carbon.identity.mgt.claim.MetaClaim;
import org.wso2.carbon.identity.mgt.exception.IdentityStoreException;
import org.wso2.carbon.identity.mgt.exception.UserNotFoundException;
import org.wso2.carbon.identity.mgt.impl.util.IdentityMgtConstants;
import org.wso2.is.portal.user.client.api.bean.ProfileUIEntry;
import org.wso2.is.portal.user.client.api.exception.UserPortalUIException;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
=======

package org.wso2.is.portal.user.client.api;
>>>>>>> c9a9414... Adding ProfileMgtClientService

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.identity.claim.exception.ProfileMgtServiceException;
import org.wso2.carbon.identity.claim.mapping.profile.ProfileEntry;
import org.wso2.carbon.identity.claim.service.ProfileMgtService;
import org.wso2.carbon.identity.mgt.RealmService;
import org.wso2.carbon.identity.mgt.claim.Claim;
import org.wso2.carbon.identity.mgt.claim.MetaClaim;
import org.wso2.carbon.identity.mgt.exception.IdentityStoreException;
import org.wso2.carbon.identity.mgt.exception.UserNotFoundException;
import org.wso2.carbon.identity.mgt.impl.util.IdentityMgtConstants;
import org.wso2.is.portal.user.client.api.bean.ProfileUIEntry;
import org.wso2.is.portal.user.client.api.exception.UserPortalUIException;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.wso2.carbon.identity.meta.claim.mgt.exception.ProfileMgtServiceException;
import org.wso2.carbon.identity.meta.claim.mgt.mapping.profile.ProfileEntry;
import org.wso2.is.portal.user.client.api.internal.UserPortalClientApiDataHolder;

/**
 * Profile Mgt Client Service Implementation.
 */
<<<<<<< HEAD
<<<<<<< HEAD
=======
>>>>>>> cf181b2... Removing DataHolder from the component.
@Component(
        name = "org.wso2.is.portal.user.client.api.ProfileMgtClientServiceImpl",
        service = ProfileMgtClientService.class,
        immediate = true)
<<<<<<< HEAD
public class ProfileMgtClientServiceImpl implements ProfileMgtClientService {

<<<<<<< HEAD
    private static final Logger LOGGER = LoggerFactory.getLogger(ProfileMgtClientServiceImpl.class);

    private RealmService realmService;

    private ProfileMgtService profileMgtService;

    @Activate
    protected void start(final BundleContext bundleContext) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("ProfileMgtClientService activated successfully.");
        }
    }

    @Reference(
            name = "realmService",
            service = RealmService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetRealmService")
    protected void setRealmService(RealmService realmService) {

        this.realmService = realmService;
    }

    protected void unsetRealmService(RealmService realmService) {

        this.realmService = null;
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

    @Override
    public Set<String> getProfileNames() throws UserPortalUIException {

        Set<String> profileNames;
        try {
            profileNames = getProfileMgtService().getProfiles().entrySet().stream()
                                                 .filter(profile -> !profile.getValue().isAdminProfile())
                                                 .map(Map.Entry::getKey)
                                                 .collect(Collectors.toSet());
        } catch (ProfileMgtServiceException e) {
            String error = "Failed to retrieve profile names.";
            LOGGER.error(error, e);
            throw new UserPortalUIException(error);
        }

        return profileNames;
    }

    @Override
    public ProfileEntry getProfile(String profileName) throws UserPortalUIException {

        try {
            ProfileEntry profileEntry = getProfileMgtService().getProfile(profileName);
            if (profileEntry == null) {
                String error = String.format("Invalid profile - %s", profileName);
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug(error);
                }
                throw new UserPortalUIException(error);
            }
            return profileEntry;
        } catch (ProfileMgtServiceException e) {
            String error = "Failed to retrieve the claim profile.";
            LOGGER.error(error, e);
            throw new UserPortalUIException(error);
        }
    }

    @Override
    public List<ProfileUIEntry> getProfileEntries(String profileName, String uniqueUserId)
            throws UserPortalUIException {

        ProfileEntry profileEntry = getProfile(profileName);

        if (profileEntry.getClaims().isEmpty()) {
            return Collections.emptyList();
        }

        List<MetaClaim> metaClaims = profileEntry.getClaims().stream()
                .map(claimConfigEntry -> new MetaClaim(IdentityMgtConstants.CLAIM_ROOT_DIALECT,
                        claimConfigEntry.getClaimURI()))
                .collect(Collectors.toList());

        List<Claim> claims;
        try {
            claims = getRealmService().getIdentityStore().getClaimsOfUser(uniqueUserId, metaClaims);
        } catch (IdentityStoreException e) {
            LOGGER.error(String.format("Failed to get the user claims for user - %s", uniqueUserId), e);
            throw new UserPortalUIException(String.format("Failed to get the user claims for the profile - %s",
                    profileName));
        } catch (UserNotFoundException e) {
            String error = String.format("Invalid user - %s", uniqueUserId);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(error, e);
            }
            throw new UserPortalUIException(error);
        }

        if (claims.isEmpty()) {
            return profileEntry.getClaims().stream()
                    .map(claimConfigEntry -> new ProfileUIEntry(claimConfigEntry, null))
                    .collect(Collectors.toList());
        }

        return profileEntry.getClaims().stream()
                .map(claimConfigEntry -> {
                    Optional<Claim> optional = claims.stream()
                            .filter(claim -> claim.getClaimUri().equals(claimConfigEntry.getClaimURI()))
                            .findAny();
                    if (optional.isPresent()) {
                        return new ProfileUIEntry(claimConfigEntry, optional.get().getValue());
                    }
                    return new ProfileUIEntry(claimConfigEntry, null);
                }).collect(Collectors.toList());
    }

    private RealmService getRealmService() {
        if (this.realmService == null) {
            throw new IllegalStateException("Realm Service is null.");
        }
        return this.realmService;
    }

    private ProfileMgtService getProfileMgtService() {
        if (profileMgtService == null) {
            throw new IllegalStateException("Profile Mgt Service is null.");
        }
        return profileMgtService;
    }
=======
public class ProfileMgtClientServiceImpl implements ProfileMgtClientService {

<<<<<<< HEAD
<<<<<<< HEAD
>>>>>>> c9a9414... Adding ProfileMgtClientService
=======
=======
    private static final Logger log = LoggerFactory.getLogger(ProfileMgtClientServiceImpl.class);

<<<<<<< HEAD
>>>>>>> 951ade6... Adding profile loading js
=======
    private static final String CLAIM_ROOT_DIALECT = "http://wso2.org/claims";
=======
public class ProfileMgtClientServiceImpl implements ProfileMgtClientService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProfileMgtClientServiceImpl.class);

    private RealmService realmService;

    private ProfileMgtService profileMgtService;

    @Activate
    protected void start(final BundleContext bundleContext) {

    }

    @Reference(
            name = "realmService",
            service = RealmService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetRealmService")
    protected void setRealmService(RealmService realmService) {

        this.realmService = realmService;
    }

    protected void unsetRealmService(RealmService realmService) {

        this.realmService = null;
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
>>>>>>> cf181b2... Removing DataHolder from the component.

>>>>>>> 6c5f9c0... Fixing login issue.
    @Override
    public Set<String> getProfileNames() throws UserPortalUIException {

        Set<String> profileNames;
        try {
            profileNames = getProfileMgtService().getProfiles().entrySet().stream()
                                                 .filter(profile -> !profile.getValue().isAdminProfile())
                                                 .map(Map.Entry::getKey)
                                                 .collect(Collectors.toSet());
        } catch (ProfileMgtServiceException e) {
            String error = "Failed to retrieve profile names.";
            LOGGER.error(error, e);
            throw new UserPortalUIException(error);
        }

        return profileNames;
    }
<<<<<<< HEAD
>>>>>>> c2d029b... Adding profile view dynamically.
=======
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
<<<<<<< HEAD

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
>>>>>>> d702044...  Added backend services for user self sign-up
=======
>>>>>>> b1950fd... Removed unused functions in profile mgt service
=======

    @Override
    public ProfileEntry getProfile(String profileName) throws UserPortalUIException {

        try {
            ProfileEntry profileEntry = getProfileMgtService().getProfile(profileName);
            if (profileEntry == null) {
                String error = String.format("Invalid profile - %s", profileName);
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug(error);
                }
                throw new UserPortalUIException(error);
            }
            return profileEntry;
        } catch (ProfileMgtServiceException e) {
            String error = "Failed to retrieve the claim profile.";
            LOGGER.error(error, e);
            throw new UserPortalUIException(error);
        }
    }
<<<<<<< HEAD
>>>>>>> 951ade6... Adding profile loading js
=======

    @Override
    public List<ProfileUIEntry> getProfileEntries(String profileName, String uniqueUserId)
            throws UserPortalUIException {

        ProfileEntry profileEntry = getProfile(profileName);

        if (profileEntry.getClaims().isEmpty()) {
            return Collections.emptyList();
        }

        List<MetaClaim> metaClaims = profileEntry.getClaims().stream()
                .map(claimConfigEntry -> new MetaClaim(IdentityMgtConstants.CLAIM_ROOT_DIALECT,
                        claimConfigEntry.getClaimURI()))
                .collect(Collectors.toList());

        List<Claim> claims;
        try {
            claims = getRealmService().getIdentityStore().getClaimsOfUser(uniqueUserId, metaClaims);
        } catch (IdentityStoreException e) {
            LOGGER.error(String.format("Failed to get the user claims for user - %s", uniqueUserId), e);
            throw new UserPortalUIException(String.format("Failed to get the user claims for the profile - %s",
                    profileName));
        } catch (UserNotFoundException e) {
            String error = String.format("Invalid user - %s", uniqueUserId);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(error, e);
            }
            throw new UserPortalUIException(error);
        }

        if (claims.isEmpty()) {
            return profileEntry.getClaims().stream()
                    .map(claimConfigEntry -> new ProfileUIEntry(claimConfigEntry, null))
                    .collect(Collectors.toList());
        }

        return profileEntry.getClaims().stream()
                .map(claimConfigEntry -> {
                    Optional<Claim> optional = claims.stream()
                            .filter(claim -> claim.getClaimUri().equals(claimConfigEntry.getClaimURI()))
                            .findAny();
                    if (optional.isPresent()) {
                        return new ProfileUIEntry(claimConfigEntry, optional.get().getValue());
                    }
                    return new ProfileUIEntry(claimConfigEntry, null);
                }).collect(Collectors.toList());
    }

    private RealmService getRealmService() {
        if (this.realmService == null) {
            throw new IllegalStateException("Realm Service is null.");
        }
        return this.realmService;
    }

<<<<<<< HEAD
>>>>>>> 6c5f9c0... Fixing login issue.
=======
    private ProfileMgtService getProfileMgtService() {
        if (profileMgtService == null) {
            throw new IllegalStateException("Profile Mgt Service is null.");
        }
        return profileMgtService;
    }
>>>>>>> cf181b2... Removing DataHolder from the component.
}

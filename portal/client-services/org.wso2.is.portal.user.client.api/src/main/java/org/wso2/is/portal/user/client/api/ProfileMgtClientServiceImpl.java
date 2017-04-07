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
import org.wso2.carbon.identity.mgt.exception.GroupNotFoundException;
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

/**
 * Profile Mgt Client Service Implementation.
 */
@Component(
        name = "org.wso2.is.portal.user.client.api.ProfileMgtClientServiceImpl",
        service = ProfileMgtClientService.class,
        immediate = true)
public class ProfileMgtClientServiceImpl implements ProfileMgtClientService {

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
                    .filter(profile -> !profile.getValue().isAdminProfile()).map(Map.Entry::getKey)
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
                        claimConfigEntry.getClaimURI())).collect(Collectors.toList());

        List<Claim> claims;
        try {
            claims = getRealmService().getIdentityStore().getClaimsOfUser(uniqueUserId, metaClaims);
        } catch (IdentityStoreException e) {
            LOGGER.error(String.format("Failed to get the user claims for user - %s", uniqueUserId), e);
            throw new UserPortalUIException(
                    String.format("Failed to get the user claims for the profile - %s", profileName));
        } catch (UserNotFoundException e) {
            String error = String.format("Invalid user - %s", uniqueUserId);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(error, e);
            }
            throw new UserPortalUIException(error);
        }

        if (claims.isEmpty()) {
            return profileEntry.getClaims().stream().map(claimConfigEntry -> new ProfileUIEntry(claimConfigEntry, null))
                    .collect(Collectors.toList());
        }

        return profileEntry.getClaims().stream().map(claimConfigEntry -> {
            Optional<Claim> optional = claims.stream()
                    .filter(claim -> claim.getClaimUri().equals(claimConfigEntry.getClaimURI())).findAny();
            if (optional.isPresent()) {
                return new ProfileUIEntry(claimConfigEntry, optional.get().getValue());
            }
            return new ProfileUIEntry(claimConfigEntry, null);
        }).collect(Collectors.toList());
    }

    @Override
    public List<ProfileUIEntry> getGroupClaimEntries(String uniqueGroupId)
            throws UserPortalUIException {

        //get all the claims of the group profile as some of the claims may not have been filled yet.
        ProfileEntry profileEntry = getProfile("group");

        if (profileEntry.getClaims().isEmpty()) {
            return Collections.emptyList();
        }

        List<MetaClaim> metaClaims = profileEntry.getClaims().stream()
                .map(claimConfigEntry -> new MetaClaim(IdentityMgtConstants.CLAIM_ROOT_DIALECT,
                        claimConfigEntry.getClaimURI())).collect(Collectors.toList());

        List<Claim> claims;
        try {
            claims = getRealmService().getIdentityStore().getClaimsOfGroup(uniqueGroupId, metaClaims);
        } catch (IdentityStoreException e) {
            LOGGER.error(String.format("Failed to get the user claims for group - %s", uniqueGroupId), e);
            throw new UserPortalUIException(
                    String.format("Failed to get the group claims for the profile - group"));
        } catch (GroupNotFoundException e) {
            String error = String.format("Invalid group - %s", uniqueGroupId);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(error, e);
            }
            throw new UserPortalUIException(error);
        }

        if (claims.isEmpty()) {
            return profileEntry.getClaims().stream().map(claimConfigEntry -> new ProfileUIEntry(claimConfigEntry, null))
                    .collect(Collectors.toList());
        }

        return profileEntry.getClaims().stream().map(claimConfigEntry -> {
            Optional<Claim> optional = claims.stream()
                    .filter(claim -> claim.getClaimUri().equals(claimConfigEntry.getClaimURI())).findAny();
            if (optional.isPresent()) {
                return new ProfileUIEntry(claimConfigEntry, optional.get().getValue());
            }
            return new ProfileUIEntry(claimConfigEntry, null);
        }).collect(Collectors.toList());
    }

    @Override
    public List<ProfileUIEntry> getProfileEntries(String profileName)
            throws UserPortalUIException {
        ProfileEntry profileEntry = getProfile(profileName);
        if (profileEntry.getClaims().isEmpty()) {
            return Collections.emptyList();
        }
        return profileEntry.getClaims().stream()
                .map(claimConfigEntry -> new ProfileUIEntry(claimConfigEntry, null))
                .collect(Collectors.toList());
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
}

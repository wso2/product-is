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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.identity.meta.claim.mgt.exception.ProfileMgtServiceException;
import org.wso2.carbon.identity.meta.claim.mgt.mapping.profile.ProfileEntry;
import org.wso2.carbon.identity.mgt.claim.Claim;
import org.wso2.carbon.identity.mgt.claim.MetaClaim;
import org.wso2.carbon.identity.mgt.exception.IdentityStoreException;
import org.wso2.carbon.identity.mgt.exception.UserNotFoundException;
import org.wso2.is.portal.user.client.api.bean.ProfileUIEntry;
import org.wso2.is.portal.user.client.api.exception.UserPortalUIException;
import org.wso2.is.portal.user.client.api.internal.UserPortalClientApiDataHolder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Profile Mgt Client Service Implementation.
 */
public class ProfileMgtClientServiceImpl implements ProfileMgtClientService {

    private static final Logger log = LoggerFactory.getLogger(ProfileMgtClientServiceImpl.class);

    private static final String CLAIM_ROOT_DIALECT = "http://wso2.org/claims";

    @Override
    public Set<String> getProfileNames() throws UserPortalUIException {

        Set<String> profileNames;
        try {
            profileNames = UserPortalClientApiDataHolder.getInstance().getProfileMgtService().getProfileNames();
        } catch (ProfileMgtServiceException e) {
            String error = "Failed to retrieve profile names.";
            log.error(error, e);
            throw new UserPortalUIException(error);
        }

        return profileNames;
    }

    @Override
    public ProfileEntry getProfile(String profileName) throws UserPortalUIException {

        try {
            return UserPortalClientApiDataHolder.getInstance().getProfileMgtService().getProfile(profileName);
        } catch (ProfileMgtServiceException e) {
            String error = String.format("Failed to retrieve the claim profile.");
            log.error(error, e);
            throw new UserPortalUIException(error);
        }
    }

    @Override
    public List<ProfileUIEntry> getProfileEntries(String profileName, String uniqueUserId)
            throws UserPortalUIException {

        ProfileEntry profileEntry = getProfile(profileName);

        if (profileEntry == null) {
            String error = String.format("Invalid profile - %s", profileName);
            if (log.isDebugEnabled()) {
                log.debug(error);
            }
            throw new UserPortalUIException(error);
        }

        if (profileEntry.getClaims().isEmpty()) {
            return Collections.emptyList();
        }

        List<MetaClaim> metaClaims = profileEntry.getClaims().stream()
                .map(claimConfigEntry -> new MetaClaim(CLAIM_ROOT_DIALECT, claimConfigEntry.getClaimURI()))
                .collect(Collectors.toList());

        List<Claim> claims;
        try {
            //TODO remove test users
            claims = UserPortalClientApiDataHolder.getInstance().getRealmService().getIdentityStore()
                    .getClaimsOfUser(uniqueUserId, metaClaims);
        } catch (IdentityStoreException e) {
            log.error(String.format("Failed to get the user claims for user - %s", uniqueUserId), e);
            throw new UserPortalUIException(String.format("Failed to get the user claims for the profile - %s",
                    profileName));
        } catch (UserNotFoundException e) {
            String error = String.format("Invalid user - %s", uniqueUserId);
            if (log.isDebugEnabled()) {
                log.debug(error);
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


}

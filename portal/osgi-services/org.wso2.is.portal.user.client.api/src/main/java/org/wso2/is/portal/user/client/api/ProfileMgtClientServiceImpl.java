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
import org.wso2.is.portal.user.client.api.exception.UserPortalUIException;
import org.wso2.is.portal.user.client.api.internal.UserPortalClientApiDataHolder;

import java.util.Set;

/**
 * Profile Mgt Client Service Implementation.
 */
public class ProfileMgtClientServiceImpl implements ProfileMgtClientService {

    private static final Logger log = LoggerFactory.getLogger(ProfileMgtClientServiceImpl.class);

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

        ProfileEntry profileEntry;
        try {
            profileEntry = UserPortalClientApiDataHolder.getInstance().getProfileMgtService().getProfile(profileName);
        } catch (ProfileMgtServiceException e) {
            String error = String.format("Failed to retrieve profile - %s", profileName);
            log.error(error, e);
            throw new UserPortalUIException(error);
        }

        return profileEntry;
    }
}

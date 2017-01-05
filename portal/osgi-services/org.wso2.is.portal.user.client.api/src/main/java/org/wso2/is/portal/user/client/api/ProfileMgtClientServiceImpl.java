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
import org.wso2.carbon.identity.meta.claim.mgt.mapping.profile.ProfileEntry;
import org.wso2.is.portal.user.client.api.internal.UserPortalClientApiDataHolder;

/**
 * Profile Mgt Client Service Implementation.
 */
public class ProfileMgtClientServiceImpl implements ProfileMgtClientService {

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
}

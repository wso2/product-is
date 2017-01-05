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

package org.wso2.is.portal.user.client.api;

<<<<<<< HEAD
import org.wso2.carbon.identity.claim.mapping.profile.ProfileEntry;
import org.wso2.is.portal.user.client.api.bean.ProfileUIEntry;
import org.wso2.is.portal.user.client.api.exception.UserPortalUIException;

import java.util.List;
import java.util.Set;

=======
>>>>>>> c9a9414... Adding ProfileMgtClientService
/**
 * Profile Mgt Client Service.
 */
public interface ProfileMgtClientService {

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

}

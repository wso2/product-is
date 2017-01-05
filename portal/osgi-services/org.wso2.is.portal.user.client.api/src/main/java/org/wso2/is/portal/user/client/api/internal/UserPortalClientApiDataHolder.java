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

package org.wso2.is.portal.user.client.api.internal;

import org.wso2.carbon.identity.meta.claim.mgt.service.ProfileMgtService;
import org.wso2.carbon.identity.mgt.RealmService;

import java.util.List;

/**
 * Data Holder.
 */
public class UserPortalClientApiDataHolder {

    private static UserPortalClientApiDataHolder instance = new UserPortalClientApiDataHolder();

    private RealmService realmService;
    private ProfileMgtService profileMgtService;

    //TODO remove
    private List<String> tempUsers;

    private UserPortalClientApiDataHolder() {

    }

    public static UserPortalClientApiDataHolder getInstance() {
        return instance;
    }

    public RealmService getRealmService() {
        if(realmService == null) {
            throw new IllegalStateException("Realm Service is null.");
        }
        return realmService;
    }

    public void setRealmService(RealmService realmService) {
        this.realmService = realmService;
    }

    public void setProfileMgtService(ProfileMgtService profileMgtService) {
        this.profileMgtService = profileMgtService;
    }

    public ProfileMgtService getProfileMgtService() {
        if (profileMgtService == null) {
            throw new IllegalStateException("Profile Mgt Service is null.");
        }
        return profileMgtService;
    }

    public static void setInstance(UserPortalClientApiDataHolder instance) {
        UserPortalClientApiDataHolder.instance = instance;
    }

    public List<String> getTempUsers() {
        return tempUsers;
    }

    public void setTempUsers(List<String> tempUsers) {
        this.tempUsers = tempUsers;
    }
}

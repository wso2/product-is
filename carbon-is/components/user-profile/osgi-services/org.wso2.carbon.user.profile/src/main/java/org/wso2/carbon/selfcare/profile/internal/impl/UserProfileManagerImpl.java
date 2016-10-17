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

package org.wso2.carbon.selfcare.profile.internal.impl;

import org.wso2.carbon.security.caas.user.core.bean.User;
import org.wso2.carbon.security.caas.user.core.claim.Claim;
import org.wso2.carbon.security.caas.user.core.claim.MetaClaim;
import org.wso2.carbon.selfcare.profile.service.UserProfileService;

import java.util.Collection;
import java.util.List;

/**
 * Defalt implementation of User Profile Service
 */
public class UserProfileManagerImpl implements UserProfileService {

    @Override
    public User authenticate(String username, String password) {
        return null;
    }

    @Override
    public Collection<Claim> getProfile(String profileId, String userid) {
        return null;
    }

    @Override
    public Collection<Claim> getProfile(String userid) {
        return null;
    }

    @Override
    public Collection<Claim> getMetaClaims(String dialect, Collection<String> claims) {
        return null;
    }

    @Override
    public Collection<Claim> getClaims(User user) {
        return null;
    }

    @Override
    public void createProfile(String profileId, Collection<Claim> claims) {

    }

    @Override
    public void createProfile(String templateId, String profileId, Collection<Claim> claims) {

    }

    @Override
    public void updateProfile(String profileId, Collection<Claim> claims) {

    }

    @Override
    public void updateProfileName(String profileId, String newName) {

    }

    @Override
    public void deleteProfile(String profileId) {

    }

    @Override
    public Collection<MetaClaim> getProfileTemplate(String profileTempleteId) {
        return null;
    }

    @Override
    public List<Integer> getProfileTemplates() {
        return null;
    }
}

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
import org.wso2.carbon.identity.mgt.AuthenticationContext;
import org.wso2.carbon.identity.mgt.User;
import org.wso2.carbon.identity.mgt.bean.UserBean;
import org.wso2.carbon.identity.mgt.claim.Claim;
import org.wso2.carbon.identity.mgt.exception.AuthenticationFailure;
import org.wso2.carbon.identity.mgt.exception.IdentityStoreException;
import org.wso2.carbon.identity.mgt.exception.UserNotFoundException;
import org.wso2.carbon.identity.mgt.impl.util.IdentityMgtConstants;
import org.wso2.carbon.kernel.utils.StringUtils;
import org.wso2.is.portal.user.client.api.bean.UUFUser;
import org.wso2.is.portal.user.client.api.exception.UserPortalUIException;
import org.wso2.is.portal.user.client.api.internal.UserPortalClientApiDataHolder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.PasswordCallback;

/**
 * Identity store client service implementation.
 */
public class IdentityStoreClientServiceImpl implements IdentityStoreClientService {

    public IdentityStoreClientServiceImpl() {
        addTestUsers();
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(IdentityStoreClientServiceImpl.class);

    @Override
    public UUFUser authenticate(String username, char[] password) throws UserPortalUIException {

        try {
            //TODO if different claim is used, need identify that claim.
            Claim usernameClaim = new Claim(IdentityMgtConstants.CLAIM_ROOT_DIALECT, IdentityMgtConstants.USERNAME_CLAIM,
                    username);
            PasswordCallback passwordCallback = new PasswordCallback("password", false);
            passwordCallback.setPassword(password);
            //todo
            AuthenticationContext authenticationContext = UserPortalClientApiDataHolder.getInstance().getRealmService().getIdentityStore().authenticate(usernameClaim,
                    new Callback[]{passwordCallback}, null);
            User identityUser = authenticationContext.getUser();

            //TODO if another claim used, need to load username claim

            return new UUFUser(username, identityUser.getUniqueUserId(), identityUser.getDomainName());
        } catch (AuthenticationFailure | IdentityStoreException e) {
            //todo
            e.printStackTrace();
            throw new UserPortalUIException(e.getMessage());
        }
    }

    @Override
    public void updatePassword(String username, char[] oldPassword, char[] newPassword)
            throws UserNotFoundException, UserPortalUIException {

        try {
            //validate the old password
            UUFUser uufUser = authenticate(username, oldPassword);

            PasswordCallback passwordCallback = new PasswordCallback("password", false);
            passwordCallback.setPassword(newPassword);

            UserPortalClientApiDataHolder.getInstance().getRealmService().getIdentityStore().updateUserCredentials(uufUser.getUserId(), Collections
                    .singletonList(passwordCallback));
        } catch (IdentityStoreException e) {
            throw new UserPortalUIException(e.getMessage());
        }

    }

    @Override
    public User addUser(Map<String, String> userClaims) throws IdentityStoreException {
        UserBean userBean = new UserBean();
        List<Claim> claimsList = new ArrayList();

        for (Map.Entry<String, String> entry : userClaims.entrySet()) {
            Claim claim = new Claim();
            claim.setClaimUri(entry.getKey());
            claim.setValue(entry.getValue());
            claimsList.add(claim);
        }

        userBean.setClaims(claimsList);

        if (UserPortalClientApiDataHolder.getInstance().getRealmService() != null) {
            UserPortalClientApiDataHolder.getInstance().getRealmService().getIdentityStore().addUser(userBean);
        } else {
            throw new RuntimeException("RealmService is not available");
        }
        return null;
    }

    @Override
    public void updateUserProfile(String uniqueUserId, Map<String, String> updatedClaimsMap) throws
            UserPortalUIException {

        if (updatedClaimsMap == null || updatedClaimsMap.isEmpty()) {
            return;
        }

        List<Claim> updatedClaims = updatedClaimsMap.entrySet().stream()
                .filter(entry -> !StringUtils.isNullOrEmpty(entry.getKey()))
                .map(entry -> new Claim(IdentityMgtConstants.CLAIM_ROOT_DIALECT, entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());

        try {
            UserPortalClientApiDataHolder.getInstance().getRealmService().getIdentityStore().updateUserClaims
                    (uniqueUserId, updatedClaims, null);
        } catch (IdentityStoreException | UserNotFoundException e) {
            String error = "Failed to updated user profile.";
            LOGGER.error(error, e);
            throw new UserPortalUIException(error);
        }
    }

    private void addTestUsers() {

        try {
            User testUser = UserPortalClientApiDataHolder.getInstance().getRealmService().getIdentityStore().getUser(new Claim
                    ("http://wso2.org/claims", "http://wso2.org/claims/username", "lucifer"));
            if (testUser != null) {
                return;
            }
        } catch (IdentityStoreException | UserNotFoundException e) {

        }

        UserBean userBean1 = new UserBean();
        List<Claim> claims1 = Arrays
                .asList(new Claim("http://wso2.org/claims", "http://wso2.org/claims/username", "jon"),
                        new Claim("http://wso2.org/claims", "http://wso2.org/claims/givenname", "Jon"),
                        new Claim("http://wso2.org/claims", "http://wso2.org/claims/lastname", "Snow"),
                        new Claim("http://wso2.org/claims", "http://wso2.org/claims/email", "jon@wso2.com"),
                        new Claim("http://wso2.org/claims", "http://wso2.org/claims/telephone", "+94715979891"));
        userBean1.setClaims(claims1);

        PasswordCallback passwordCallback1 = new PasswordCallback("password", false);
        passwordCallback1.setPassword("admin".toCharArray());
        userBean1.setCredentials(Collections.singletonList(passwordCallback1));

        UserBean userBean2 = new UserBean();
        List<Claim> claims2 = Arrays
                .asList(new Claim("http://wso2.org/claims", "http://wso2.org/claims/username", "sansa"),
                        new Claim("http://wso2.org/claims", "http://wso2.org/claims/givenname", "Sansa"),
                        new Claim("http://wso2.org/claims", "http://wso2.org/claims/lastname", "Stark"),
                        new Claim("http://wso2.org/claims", "http://wso2.org/claims/email", "sansa@wso2.com"),
                        new Claim("http://wso2.org/claims", "http://wso2.org/claims/telephone", "+94715979891"));
        userBean2.setClaims(claims2);

        PasswordCallback passwordCallback2 = new PasswordCallback("password", false);
        passwordCallback2.setPassword("admin".toCharArray());
        userBean2.setCredentials(Collections.singletonList(passwordCallback2));

        try {

            User user1 = UserPortalClientApiDataHolder.getInstance().getRealmService().getIdentityStore().addUser
                    (userBean1);
            User user2 = UserPortalClientApiDataHolder.getInstance().getRealmService().getIdentityStore().addUser
                    (userBean2);
            List<User> users = Arrays.asList(user1, user2);

            UserPortalClientApiDataHolder.getInstance().setTempUsers(users.stream().map(User::getUniqueUserId)
                    .collect(Collectors.toList()));
        } catch (IdentityStoreException e) {
            throw new RuntimeException("Failed to add test users.", e);
        }
    }
}

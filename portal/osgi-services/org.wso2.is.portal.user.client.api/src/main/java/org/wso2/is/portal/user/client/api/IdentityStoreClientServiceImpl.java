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
import org.wso2.carbon.identity.mgt.User;
import org.wso2.carbon.identity.mgt.bean.UserBean;
import org.wso2.carbon.identity.mgt.claim.Claim;
import org.wso2.carbon.identity.mgt.claim.MetaClaim;
import org.wso2.carbon.identity.mgt.exception.AuthenticationFailure;
import org.wso2.carbon.identity.mgt.exception.IdentityStoreException;
import org.wso2.carbon.identity.mgt.exception.UserNotFoundException;
import org.wso2.carbon.identity.mgt.impl.util.IdentityMgtConstants;
import org.wso2.carbon.kernel.utils.StringUtils;
import org.wso2.is.portal.user.client.api.bean.UUFUser;
import org.wso2.is.portal.user.client.api.exception.UserPortalUIException;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.PasswordCallback;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Identity store client service implementation.
 */
@Component(
        name = "org.wso2.is.portal.user.client.api.IdentityStoreClientServiceImpl",
        service = IdentityStoreClientService.class,
        immediate = true)
public class IdentityStoreClientServiceImpl implements IdentityStoreClientService {

    private static final Logger LOGGER = LoggerFactory.getLogger(IdentityStoreClientServiceImpl.class);

    private RealmService realmService;

    @Activate
    protected void start(final BundleContext bundleContext) {
        LOGGER.info("Registered service implementation" + IdentityStoreClientServiceImpl.class); //todo
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

    @Override
    public UUFUser authenticate(String username, char[] password) throws UserPortalUIException {

        try {
            //TODO if different claim is used, need identify that claim.
            Claim usernameClaim = new Claim(IdentityMgtConstants.CLAIM_ROOT_DIALECT,
                    IdentityMgtConstants.USERNAME_CLAIM, username);
            PasswordCallback passwordCallback = new PasswordCallback("password", false);
            passwordCallback.setPassword(password);
            AuthenticationContext authenticationContext = getRealmService().getIdentityStore()
                    .authenticate(usernameClaim, new Callback[]{passwordCallback}, null);
            User identityUser = authenticationContext.getUser();

            //TODO if another claim used, need to load username claim

            return new UUFUser(username, identityUser.getUniqueUserId(), identityUser.getDomainName());
        } catch (AuthenticationFailure e) {
            String error = "Invalid credentials.";
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(error, e);
            }
            throw new UserPortalUIException("Invalid credentials.");
        } catch (IdentityStoreException e) {
            String error = "Failed to authenticate user.";
            LOGGER.error(error, e);
            throw new UserPortalUIException(error);
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

            getRealmService().getIdentityStore().updateUserCredentials(uufUser.getUserId(),
                    Collections.singletonList(passwordCallback));
        } catch (IdentityStoreException e) {
            String error = "Failed to update user password.";
            LOGGER.error(error, e);
            throw new UserPortalUIException(error);
        }

    }

    @Override
    public UUFUser addUser(Map<String, String> userClaims, Map<String, String> credentials) throws
            UserPortalUIException {

        UserBean userBean = new UserBean();
        List<Claim> claimsList = new ArrayList<>();
        List<Callback> credentialsList = new ArrayList<>();
        User identityUser;

        for (Map.Entry<String, String> credential : credentials.entrySet()) {
            PasswordCallback passwordCallback = new PasswordCallback("password", false);
            passwordCallback.setPassword(credential.getValue().toCharArray());
            credentialsList.add(passwordCallback);
        }

        for (Map.Entry<String, String> entry : userClaims.entrySet()) {
            Claim claim = new Claim();
            claim.setClaimUri(entry.getKey());
            claim.setValue(entry.getValue());
            claimsList.add(claim);
        }

        userBean.setClaims(claimsList);
        userBean.setCredentials(credentialsList);

        try {
            identityUser = getRealmService().getIdentityStore().addUser(userBean);
        } catch (IdentityStoreException e) {
            String error = "Error while adding user.";
            LOGGER.error(error, e);
            throw new UserPortalUIException(error);
        }
        return new UUFUser(null, identityUser.getUniqueUserId(), identityUser.getDomainName());
    }

    @Override
    public UUFUser addUser(Map<String, String> userClaims, Map<String, String> credentials, String domainName)
            throws UserPortalUIException {

        UserBean userBean = new UserBean();
        List<Claim> claimsList = new ArrayList<>();
        List<Callback> credentialsList = new ArrayList<>();
        User identityUser;

        for (Map.Entry<String, String> credential : credentials.entrySet()) {
            PasswordCallback passwordCallback = new PasswordCallback("password", false);
            passwordCallback.setPassword(credential.getValue().toCharArray());
            credentialsList.add(passwordCallback);
        }

        for (Map.Entry<String, String> entry : userClaims.entrySet()) {
            Claim claim = new Claim();
            claim.setClaimUri(entry.getKey());
            claim.setValue(entry.getValue());
            claimsList.add(claim);
        }

        userBean.setClaims(claimsList);
        userBean.setCredentials(credentialsList);

        try {
            identityUser = getRealmService().getIdentityStore().addUser(userBean, domainName);
        } catch (IdentityStoreException e) {
            String error = "Error while adding user.";
            LOGGER.error(error, e);
            throw new UserPortalUIException(error);
        }
        return new UUFUser(null, identityUser.getUniqueUserId(), identityUser.getDomainName());
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
            getRealmService().getIdentityStore().updateUserClaims(uniqueUserId, updatedClaims, null);
        } catch (IdentityStoreException | UserNotFoundException e) {
            String error = "Failed to updated user profile.";
            LOGGER.error(error, e);
            throw new UserPortalUIException(error);
        }
    }

    public List<Claim> getClaimsOfUser(String uniqueUserId, List<MetaClaim> metaClaims) throws UserPortalUIException {
        List<Claim> claimList = null;

        if(StringUtils.isNullOrEmpty(uniqueUserId)) {
            throw new UserPortalUIException("Invalid unique user id.");
        }
        if(metaClaims != null && !metaClaims.isEmpty()) {
            try {
                claimList = getRealmService().getIdentityStore().getClaimsOfUser(uniqueUserId, metaClaims);
            } catch (IdentityStoreException | UserNotFoundException e) {
                String error = "Failed to get claims of the user.";
                LOGGER.error(error, e);
                throw new UserPortalUIException(error);
            }
        } else {
            claimList = Collections.emptyList();
        }
        return claimList;
    }

    private RealmService getRealmService() {
        if (this.realmService == null) {
            throw new IllegalStateException("Realm Service is null.");
        }
        return this.realmService;
    }
}

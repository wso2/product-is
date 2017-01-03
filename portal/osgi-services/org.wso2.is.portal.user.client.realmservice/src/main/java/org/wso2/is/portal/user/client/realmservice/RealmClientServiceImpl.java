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

package org.wso2.is.portal.user.client.realmservice;

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
import org.wso2.carbon.identity.mgt.claim.Claim;
import org.wso2.carbon.identity.mgt.exception.AuthenticationFailure;
import org.wso2.carbon.identity.mgt.exception.IdentityStoreException;
import org.wso2.carbon.identity.mgt.exception.UserNotFoundException;
import org.wso2.carbon.identity.mgt.impl.util.IdentityMgtConstants;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.PasswordCallback;
import java.util.Collections;

/**
 * Default implementation of Realm Client Service.
 * todo move to internal
 */
@Component(
        name = "org.wso2.is.portal.user.client.realmservice.RealmClientServiceImpl",
        service = RealmClientService.class,
        immediate = true)
public class RealmClientServiceImpl implements RealmClientService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RealmClientServiceImpl.class);
    private RealmService realmService = null;

    @Activate
    protected void start(final BundleContext bundleContext) {
        // nothing to do
    }

    @Reference(
            name = "realmService",
            service = RealmService.class,
            cardinality = ReferenceCardinality.OPTIONAL,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetRealmService")
    protected void setRealmService(RealmService realmService) {
        this.realmService = realmService;
    }

    protected void unsetRealmService(RealmService realmService) {
        this.realmService = null;
    }

    @Override
    public AuthenticationContext authenticate(String username, char[] password)
            throws IdentityStoreException, AuthenticationFailure {
        Claim usernameClaim = new Claim(IdentityMgtConstants.CLAIM_ROOT_DIALECT, IdentityMgtConstants.USERNAME_CLAIM,
                username);
        PasswordCallback passwordCallback = new PasswordCallback("password", false);
        passwordCallback.setPassword(password);

        if (realmService != null) {
            return realmService.getIdentityStore()
                    .authenticate(usernameClaim, new Callback[] { passwordCallback }, null);
        } else {
            throw new RuntimeException("RealmService is not available");
        }

    }

    @Override
    public void updatePassword(String username, char[] oldPassword, char[] newPassword)
            throws UserNotFoundException, AuthenticationFailure, IdentityStoreException {

        //validate the old password
        authenticate(username, oldPassword);

        PasswordCallback passwordCallback = new PasswordCallback("password", false);
        passwordCallback.setPassword(newPassword);

        if (realmService != null) {
            realmService.getIdentityStore()
                    .updateUserCredentials(username, Collections.singletonList(passwordCallback));
        } else {
            throw new RuntimeException("RealmService is not available");
        }

    }
}

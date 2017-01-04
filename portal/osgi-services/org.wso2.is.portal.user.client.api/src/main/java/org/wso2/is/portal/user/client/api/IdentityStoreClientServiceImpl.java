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
import org.wso2.carbon.identity.mgt.claim.Claim;
import org.wso2.carbon.identity.mgt.exception.AuthenticationFailure;
import org.wso2.carbon.identity.mgt.exception.IdentityStoreException;
import org.wso2.carbon.identity.mgt.exception.UserNotFoundException;
import org.wso2.carbon.identity.mgt.impl.util.IdentityMgtConstants;
import org.wso2.is.portal.user.client.api.internal.DataHolder;

import java.util.Collections;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.PasswordCallback;

/**
 * Identity store client service implementation.
 */
public class IdentityStoreClientServiceImpl implements IdentityStoreClientService {

    private static final Logger LOGGER = LoggerFactory.getLogger(IdentityStoreClientServiceImpl.class);

    @Override
    public AuthenticationContext authenticate(String username, char[] password) throws IdentityStoreException,
            AuthenticationFailure {

        Claim usernameClaim = new Claim(IdentityMgtConstants.CLAIM_ROOT_DIALECT, IdentityMgtConstants.USERNAME_CLAIM,
                username);
        PasswordCallback passwordCallback = new PasswordCallback("password", false);
        passwordCallback.setPassword(password);

        return DataHolder.getInstance().getRealmService().getIdentityStore().authenticate(usernameClaim,
                new Callback[]{passwordCallback}, null);

    }

    @Override
    public void updatePassword(String username, char[] oldPassword, char[] newPassword) throws UserNotFoundException,
            AuthenticationFailure, IdentityStoreException {

        //validate the old password
        authenticate(username, oldPassword);

        PasswordCallback passwordCallback = new PasswordCallback("password", false);
        passwordCallback.setPassword(newPassword);

        DataHolder.getInstance().getRealmService().getIdentityStore().updateUserCredentials(username, Collections
                .singletonList(passwordCallback));

    }
}

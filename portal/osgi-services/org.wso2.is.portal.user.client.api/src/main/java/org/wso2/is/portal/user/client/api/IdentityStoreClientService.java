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

import org.wso2.carbon.identity.mgt.AuthenticationContext;
import org.wso2.carbon.identity.mgt.exception.AuthenticationFailure;
import org.wso2.carbon.identity.mgt.exception.IdentityStoreException;
import org.wso2.carbon.identity.mgt.exception.UserNotFoundException;

/**
 * Perform operations related to password handling
 */
public interface IdentityStoreClientService {


    /**
     * Authenticate.
     *
     * @param username username
     * @param password password
     * @return authentication context
     * @throws IdentityStoreException Identity Store Exception
     * @throws AuthenticationFailure Authentication Failure
     */
    AuthenticationContext authenticate(String username, char[] password)
            throws IdentityStoreException, AuthenticationFailure;

    /**
     * Update user password.
     *
     * @param username username
     * @param oldPassword old password
     * @param newPassword new password
     * @throws IdentityStoreException Identity Store Exception
     * @throws AuthenticationFailure Authentication Failure
     * @throws UserNotFoundException User Not Found Exception
     */
    void updatePassword(String username, char[] oldPassword, char[] newPassword)
            throws IdentityStoreException, AuthenticationFailure, UserNotFoundException;

}

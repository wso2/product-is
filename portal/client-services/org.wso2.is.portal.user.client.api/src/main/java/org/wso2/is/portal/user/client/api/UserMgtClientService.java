/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.wso2.carbon.identity.mgt.User;
import org.wso2.carbon.identity.mgt.claim.Claim;
import org.wso2.is.portal.user.client.api.bean.UserUIEntry;
import org.wso2.is.portal.user.client.api.exception.UserPortalUIException;

import java.util.List;

/**
 * User management client service
 */
public interface UserMgtClientService {

    List<User> listUsers(int offset, int length, String domainName) throws UserPortalUIException;

    List<User> listUsers(Claim claim, int offset, int length, String domainName) throws UserPortalUIException;

    List<Claim> getClaimsOfUser(String uniqueUserId) throws UserPortalUIException;

//    List<UserUIEntry> getUsersForList(int offset, int length, String domainName)
//            throws UserPortalUIException;

    List<UserUIEntry> getUsersForList(int offset, int length, String domainName, String usernameClaim)
            throws UserPortalUIException;
}

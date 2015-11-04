/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.identity.integration.common.clients.mgt;

import org.apache.axis2.AxisFault;
import org.wso2.carbon.identity.mgt.stub.UserIdentityManagementAdminServiceIdentityMgtServiceExceptionException;
import org.wso2.carbon.identity.mgt.stub.UserIdentityManagementAdminServiceStub;
import org.wso2.carbon.identity.mgt.stub.dto.UserChallengesDTO;
import org.wso2.identity.integration.common.clients.AuthenticateStub;

import java.rmi.RemoteException;

public class UserIdentityManagementAdminServiceClient {

    private UserIdentityManagementAdminServiceStub userIdentityManagementAdminServicestub;
    private final String serviceName = "UserIdentityManagementAdminService";

    public UserIdentityManagementAdminServiceClient(String backEndUrl, String sessionCookie)
            throws AxisFault {
        String endPoint = backEndUrl + serviceName;
        userIdentityManagementAdminServicestub = new UserIdentityManagementAdminServiceStub(endPoint);
        AuthenticateStub.authenticateStub(sessionCookie, userIdentityManagementAdminServicestub);
    }

    public UserChallengesDTO[] getChallengeQuestionsOfUser(String userName) throws UserIdentityManagementAdminServiceIdentityMgtServiceExceptionException, RemoteException {
        return userIdentityManagementAdminServicestub.getChallengeQuestionsOfUser(userName);
    }

    public void setChallengeQuestionsOfUser(String userName, UserChallengesDTO[] challengesDTOs) throws
            UserIdentityManagementAdminServiceIdentityMgtServiceExceptionException, RemoteException {
        userIdentityManagementAdminServicestub.setChallengeQuestionsOfUser(userName, challengesDTOs);
    }
}
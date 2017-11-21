/*
*Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/
package org.wso2.identity.integration.common.clients.authorization.mgt;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.um.ws.api.stub.RemoteAuthorizationManagerServiceCallbackHandler;
import org.wso2.carbon.um.ws.api.stub.RemoteAuthorizationManagerServiceStub;
import org.wso2.carbon.um.ws.api.stub.UserStoreExceptionException;
import org.wso2.identity.integration.common.clients.AuthenticateStub;

import java.rmi.RemoteException;

public class RemoteAuthorizationManagerServiceClient {
    private static final Log log = LogFactory.getLog(RemoteAuthorizationManagerServiceClient.class);
    private final String serviceName = "RemoteAuthorizationManagerService";
    private RemoteAuthorizationManagerServiceStub remoteAuthorizationManagerServiceStub;

    private String endPoint;

    public RemoteAuthorizationManagerServiceClient(String backEndUrl, String sessionCookie) throws AxisFault {
        this.endPoint = backEndUrl + serviceName;
        remoteAuthorizationManagerServiceStub = new RemoteAuthorizationManagerServiceStub(endPoint);
        AuthenticateStub.authenticateStub(sessionCookie, remoteAuthorizationManagerServiceStub);
    }

    public boolean isRoleAuthorized(String roleName, String resourceId, String action) throws RemoteException,
            UserStoreExceptionException {
        return remoteAuthorizationManagerServiceStub.isRoleAuthorized(roleName, resourceId, action);
    }

    public void clearResourceAuthorizations(String resourceId) throws RemoteException, UserStoreExceptionException {
        remoteAuthorizationManagerServiceStub.clearResourceAuthorizations(resourceId);
    }

    public void clearAllUserAuthorization(String username) throws RemoteException, UserStoreExceptionException {
        remoteAuthorizationManagerServiceStub.clearAllUserAuthorization(username);
    }

    public boolean isUserAuthorized(String username, String resourceId, String action) throws RemoteException,
            UserStoreExceptionException {
        return remoteAuthorizationManagerServiceStub.isUserAuthorized(username, resourceId, action);
    }

    public void clearRoleActionOnAllResources(String roleName, String action) throws RemoteException,
            UserStoreExceptionException {
        remoteAuthorizationManagerServiceStub.clearRoleActionOnAllResources(roleName, action);
    }

    public void clearRoleAuthorization(String roleName, String resourceId, String action) throws RemoteException,
            UserStoreExceptionException {
        remoteAuthorizationManagerServiceStub.clearRoleAuthorization(roleName, resourceId, action);
    }

    public void clearAllRoleAuthorization(String roleName) throws RemoteException, UserStoreExceptionException {
        remoteAuthorizationManagerServiceStub.clearAllRoleAuthorization(roleName);
    }

    public String[] getExplicitlyDeniedUsersForResource(String resourceId, String action) throws RemoteException,
            UserStoreExceptionException {
        return remoteAuthorizationManagerServiceStub.getExplicitlyDeniedUsersForResource(resourceId, action);
    }

    public void authorizeUser(String username, String resourceId, String action) throws RemoteException,
            UserStoreExceptionException {
        remoteAuthorizationManagerServiceStub.authorizeUser(username, resourceId, action);
    }

    public String[] getAllowedRolesForResource(String resourceId, String action) throws RemoteException,
            UserStoreExceptionException {
        return remoteAuthorizationManagerServiceStub.getAllowedRolesForResource(resourceId, action);
    }

    public void clearUserAuthorization(String username, String resourceId, String action) throws RemoteException,
            UserStoreExceptionException {
        remoteAuthorizationManagerServiceStub.clearUserAuthorization(username, resourceId, action);
    }

    public String[] getDeniedRolesForResource(String resourceId, String action) throws RemoteException,
            UserStoreExceptionException {
        return remoteAuthorizationManagerServiceStub.getDeniedRolesForResource(resourceId, action);
    }

    public String[] getAllowedUIResourcesForUser(String username, String permissionRootPath) throws RemoteException,
            UserStoreExceptionException {
        return remoteAuthorizationManagerServiceStub.getAllowedUIResourcesForUser(username, permissionRootPath);
    }

    public void resetPermissionOnUpdateRole(String roleName, String newRoleName) throws RemoteException,
            UserStoreExceptionException {
        remoteAuthorizationManagerServiceStub.resetPermissionOnUpdateRole(roleName, newRoleName);
    }

    public void denyRole(String roleName, String resourceId, String action) throws RemoteException,
            UserStoreExceptionException {
        remoteAuthorizationManagerServiceStub.denyRole(roleName, resourceId, action);
    }

    public String[] getExplicitlyAllowedUsersForResource(String resourceId, String action) throws RemoteException,
            UserStoreExceptionException {
        return remoteAuthorizationManagerServiceStub.getExplicitlyAllowedUsersForResource(resourceId, action);
    }

    public void denyUser(String username, String resourceId, String action) throws RemoteException,
            UserStoreExceptionException {
        remoteAuthorizationManagerServiceStub.denyUser(username, resourceId, action);
    }

    public void authorizeRole(String roleName, String resourceId, String action) throws RemoteException,
            UserStoreExceptionException {
        remoteAuthorizationManagerServiceStub.authorizeRole(roleName, resourceId, action);
    }
}
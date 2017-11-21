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

package org.wso2.sample.inforecovery.client;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.user.mgt.stub.UserAdminStub;
import org.wso2.carbon.user.mgt.stub.UserAdminUserAdminException;
import org.wso2.carbon.user.mgt.stub.types.carbon.ClaimValue;
import org.wso2.carbon.user.mgt.stub.types.carbon.FlaggedName;
import org.wso2.carbon.user.mgt.stub.types.carbon.UIPermissionNode;
import org.wso2.carbon.user.mgt.stub.types.carbon.UserRealmInfo;
import org.wso2.sample.inforecovery.client.authenticator.AuthenticationException;
import org.wso2.sample.inforecovery.client.authenticator.ServiceAuthenticator;

import javax.activation.DataHandler;


public class UserAdminClient {


    protected static Log log = LogFactory.getLog(UserAdminClient.class);
    protected UserAdminStub stub = null;

    public UserAdminClient(String cookie, String url, String serviceName,
                           ConfigurationContext configContext) throws Exception {
        try {
            stub = new UserAdminStub(configContext, url + serviceName);
            ServiceAuthenticator authenticator = ServiceAuthenticator.getInstance();

            ServiceClient client = stub._getServiceClient();
            try {
                authenticator.authenticate(client);
            } catch (AuthenticationException e) {
                e.printStackTrace();
            }
            Options option = client.getOptions();
            option.setManageSession(true);


            option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
        } catch (Exception e) {
            handleException(e);
        }
    }

    public UserAdminClient(String cookie, String url, ConfigurationContext configContext)
            throws Exception {
        try {

            stub = new UserAdminStub(configContext, url + "UserAdmin");
            ServiceClient client = stub._getServiceClient();
            ServiceAuthenticator authenticator = ServiceAuthenticator.getInstance();

            try {
                authenticator.authenticate(client);
            } catch (AuthenticationException e) {
                e.printStackTrace();
            }
            Options option = client.getOptions();
            option.setManageSession(true);
            option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
        } catch (Exception e) {
            handleException(e);
        }
    }

    public void addRole(String roleName, String[] userList, String[] permissions,
                        boolean isSharedRole) throws AxisFault {
        try {
            stub.addRole(roleName, userList, permissions, isSharedRole);
        } catch (Exception e) {
            handleException(e);
        }
    }

    public void addInternalRole(String roleName, String[] userList, String[] permissions)
            throws AxisFault {
        try {
            stub.addInternalRole(roleName, userList, permissions);
        } catch (Exception e) {
            handleException(e);
        }
    }

    public void addUser(String userName, String password, String[] roles, ClaimValue[] claims,
                        String profileName) throws AxisFault {
        try {
            stub.addUser(userName, password, roles, claims, profileName);
        } catch (Exception e) {
            handleException(e);
        }
    }

    public void changePassword(String userName, String newPassword) throws AxisFault {
        try {
            stub.changePassword(userName, newPassword);
        } catch (Exception e) {
            handleException(e);
        }

    }

    public void deleteRole(String roleName) throws AxisFault {
        try {
            stub.deleteRole(roleName);
        } catch (Exception e) {
            handleException(e);
        }

    }

    public void updateRoleName(String roleName, String newRoleName) throws AxisFault {
        try {
            stub.updateRoleName(roleName, newRoleName);
        } catch (Exception e) {
            handleException(e);
        }
    }

    public void deleteUser(String userName) throws AxisFault {
        try {
            stub.deleteUser(userName);
        } catch (Exception e) {
            handleException(e);
        }
    }

    public FlaggedName[] getAllRolesNames(String filter, int limit) throws AxisFault {
        try {
            return stub.getAllRolesNames(filter, limit);
        } catch (Exception e) {
            handleException(e);
        }
        return (new FlaggedName[0]);
    }

    public FlaggedName[] getRolesOfUser(String userName, String filter, int limit)
            throws AxisFault {
        try {
            return stub.getRolesOfUser(userName, filter, limit);
        } catch (Exception e) {
            handleException(e);
        }
        return (new FlaggedName[0]);
    }

    public FlaggedName[] getUsersOfRole(String roleName, String filter, int limit)
            throws AxisFault {
        try {
            return stub.getUsersOfRole(roleName, filter, limit);
        } catch (Exception e) {
            handleException(e);
        }
        return new FlaggedName[0];
    }

    public UserRealmInfo getUserRealmInfo() throws AxisFault {
        UserRealmInfo info = null;
        ;
        try {
            info = stub.getUserRealmInfo();
        } catch (Exception e) {
            handleException(e);
        }
        return info;
    }

    public String[] listUsers(String filter, int limit) throws AxisFault {
        try {
            return stub.listUsers(filter, limit);
        } catch (Exception e) {
            handleException(e);
        }
        return new String[0];
    }

    public FlaggedName[] listAllUsers(String filter, int limit) throws AxisFault {
        try {
            return stub.listAllUsers(filter, limit);
        } catch (Exception e) {
            handleException(e);
        }
        return new FlaggedName[0];
    }

    public UIPermissionNode getAllUIPermissions() throws AxisFault {
        try {
            return stub.getAllUIPermissions();
        } catch (Exception e) {
            handleException(e);
        }
        return null;
    }

    public UIPermissionNode getRolePermissions(String roleName) throws AxisFault {
        try {
            return stub.getRolePermissions(roleName);
        } catch (Exception e) {
            handleException(e);
        }
        return null;
    }

    public void setRoleUIPermission(String roleName, String[] rawResources) throws AxisFault {
        try {
            stub.setRoleUIPermission(roleName, rawResources);
        } catch (Exception e) {
            handleException(e);
        }
    }

    public void bulkImportUsers(String userStoreDomain, String fileName, DataHandler handler, String defaultPassword)
            throws AxisFault {
        try {
            stub.bulkImportUsers(userStoreDomain, fileName, handler, defaultPassword);
        } catch (Exception e) {
            handleException(e);
        }
    }

    public void changePasswordByUser(String userName, String oldPassword, String newPassword) throws AxisFault {
        try {
            stub.changePasswordByUser(userName, oldPassword, newPassword);
        } catch (Exception e) {
            handleException(e);
        }
    }


    public void addRemoveRolesOfUser(String userName, String[] newRoles, String[] deletedRoles)
            throws AxisFault {
        try {
            stub.addRemoveRolesOfUser(userName, newRoles, deletedRoles);
        } catch (Exception e) {
            handleException(e);
        }
    }

    public void addRemoveUsersOfRole(String roleName, String[] newUsers, String[] deletedUsers)
            throws AxisFault {
        try {
            stub.addRemoveUsersOfRole(roleName, newUsers, deletedUsers);
        } catch (Exception e) {
            handleException(e);
        }
    }

    public FlaggedName[] listUserByClaim(ClaimValue claimValue, String filter, int limit)
            throws AxisFault {
        try {
            org.wso2.carbon.user.mgt.stub.types.carbon.ClaimValue abcdClaimValue = new
                    org.wso2.carbon.user.mgt.stub.types.carbon.ClaimValue();
            abcdClaimValue.setClaimURI(claimValue.getClaimURI());
            abcdClaimValue.setValue(claimValue.getValue());
            return stub.listUserByClaim(abcdClaimValue, filter, limit);
        } catch (Exception e) {
            handleException(e);
        }

        return new FlaggedName[0];
    }

    public boolean hasMultipleUserStores() throws AxisFault {
        try {
            return stub.hasMultipleUserStores();
        } catch (Exception e) {
            handleException(e);
        }
        return false;
    }

    protected String[] handleException(Exception e) throws AxisFault {

        String errorMessage = "Unknown";

        if (e instanceof UserAdminUserAdminException) {
            UserAdminUserAdminException adminException = (UserAdminUserAdminException) e;
            if (adminException.getFaultMessage().getUserAdminException() != null) {
                errorMessage = adminException.getFaultMessage().getUserAdminException()
                        .getMessage();
            }
        } else {
            errorMessage = e.getMessage();
        }

        log.error(errorMessage, e);
        throw new AxisFault(errorMessage, e);

    }

    public boolean isSharedRolesEnabled() throws AxisFault {
        try {
            return stub.isSharedRolesEnabled();
        } catch (Exception e) {
            handleException(e);
        }
        return false;
    }
}

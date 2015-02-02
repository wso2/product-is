/*
 * Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.identity.integration.test.um.ws.api;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.integration.common.admin.client.UserManagementClient;
import org.wso2.identity.integration.common.clients.authorization.mgt.RemoteAuthorizationManagerServiceClient;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;

import java.util.Arrays;
import java.util.List;

public class RemoteAuthorizationManagerServiceTestCase extends ISIntegrationTest {

    private static final String UI_PERMISSION_ACTION = "ui.execute";

    private UserManagementClient userMgtClient;
    private RemoteAuthorizationManagerServiceClient remoteAuthorizationManagerServiceClient;

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {
        super.init();

        userMgtClient = new UserManagementClient(backendURL, sessionCookie);
        remoteAuthorizationManagerServiceClient = new RemoteAuthorizationManagerServiceClient(backendURL, sessionCookie);

        // Create users
        userMgtClient.addUser("user1", "passWord1@", null, "default");
        userMgtClient.addUser("user2", "passWord1@", null, "default");
        userMgtClient.addUser("user3", "passWord1@", null, "default");
        userMgtClient.addUser("user4", "passWord1@", null, "default");

        // Create roles
        userMgtClient.addRole("role1", null, null);
        userMgtClient.addRole("role2", null, null);
        userMgtClient.addRole("role3", null, null);
        userMgtClient.addRole("role4", null, null);
        userMgtClient.addRole("role5", null, null);
        userMgtClient.addRole("role6", null, null);
    }

    @AfterClass(alwaysRun = true)
    public void testClear() throws Exception {
        // Delete users
        userMgtClient.deleteUser("user1");
        userMgtClient.deleteUser("user2");
        userMgtClient.deleteUser("user3");
        userMgtClient.deleteUser("user4");

        // Delete roles
        userMgtClient.deleteRole("role1");
        userMgtClient.deleteRole("role2");
        userMgtClient.deleteRole("role3");
        userMgtClient.deleteRole("role4");
        userMgtClient.deleteRole("role5");
        userMgtClient.deleteRole("role6");

        userMgtClient = null;
        remoteAuthorizationManagerServiceClient = null;
    }

    @Test(groups = "wso2.is", description = "Authorize role")
    public void testAuthorizeRole() throws Exception {
        remoteAuthorizationManagerServiceClient.authorizeRole("role1", "/permission/admin/login",
                UI_PERMISSION_ACTION);

        String[] allowedRolesForResource = remoteAuthorizationManagerServiceClient.getAllowedRolesForResource
                ("/permission/admin/login", UI_PERMISSION_ACTION);

        boolean isAuthorized = false;
        for (String role: allowedRolesForResource){
            if (role.equalsIgnoreCase("role1")){
                isAuthorized = true;
            }
        }

        Assert.assertTrue(isAuthorized, "Failed to authorize role1 for resource /permission/admin/login");
    }

    @Test(groups = "wso2.is", description = "Authorize user")
    public void testAuthorizeUser() throws Exception {
        remoteAuthorizationManagerServiceClient.authorizeUser("user1", "/permission/admin/login", UI_PERMISSION_ACTION);

        String[] allowedResourcesForUser = remoteAuthorizationManagerServiceClient.getAllowedUIResourcesForUser
                ("user1", null);

        boolean isAuthorized = false;
        for (String resource: allowedResourcesForUser){
            if (resource.equalsIgnoreCase("/permission/admin/login")){
                isAuthorized = true;
            }
        }

        Assert.assertTrue(isAuthorized, "Failed to authorize user1 for resource /permission/admin/login");
    }

    @Test(groups = "wso2.is", description = "Check whether role is authorized",
            dependsOnMethods = "testAuthorizeRole")
    public void testIsRoleAuthorized() throws Exception{
        Assert.assertTrue(remoteAuthorizationManagerServiceClient.isRoleAuthorized("role1",
                "/permission/admin/login", UI_PERMISSION_ACTION), "Authorized role check failed for role1");
    }

    @Test(groups = "wso2.is", description = "Check whether user is authorized",
            dependsOnMethods = "testAuthorizeUser")
    public void testIsUserAuthorized() throws Exception{
        Assert.assertTrue(remoteAuthorizationManagerServiceClient.isUserAuthorized("user1",
                "/permission/admin/login", UI_PERMISSION_ACTION), "Authorized user check failed for user1");
    }

    @Test(groups = "wso2.is", description = "Clear resource authorizations", dependsOnMethods = "testAuthorizeRole")
    public void testClearResourceAuthorizations() throws Exception{
        remoteAuthorizationManagerServiceClient.authorizeRole("role2", "/testpermission/login", UI_PERMISSION_ACTION);

        remoteAuthorizationManagerServiceClient.clearResourceAuthorizations("/testpermission/login");

        String[] allowedRolesForResource = remoteAuthorizationManagerServiceClient.getAllowedRolesForResource
                ("/testpermission/login", UI_PERMISSION_ACTION);

        Assert.assertNull(allowedRolesForResource, "Failed to clear authorizations for resource /testpermission/login");
    }

    @Test(groups = "wso2.is", description = "Clear role action on all resources of a role",
            dependsOnMethods = "testAuthorizeRole")
    public void testClearRoleActionOnAllResources() throws Exception{
        remoteAuthorizationManagerServiceClient.authorizeRole("role3", "/testpermission/manage", UI_PERMISSION_ACTION);

        remoteAuthorizationManagerServiceClient.clearRoleActionOnAllResources("role3", UI_PERMISSION_ACTION);

        String[] allowedRolesForResource = remoteAuthorizationManagerServiceClient.getAllowedRolesForResource
                ("/testpermission/manage", UI_PERMISSION_ACTION);

        boolean isRoleAuthorized = false;
        if (allowedRolesForResource != null){
            for (String role: allowedRolesForResource){
                if (role.equalsIgnoreCase("role3")){
                    isRoleAuthorized = true;
                }
            }
        }

        Assert.assertTrue(!isRoleAuthorized, "Failed to clear role action on all resources of role role3");
    }

    @Test(groups = "wso2.is", description = "Clear a resource authorization of a role",
            dependsOnMethods = "testAuthorizeRole")
    public void testClearRoleAuthorization() throws Exception{
        remoteAuthorizationManagerServiceClient.authorizeRole("role4", "/testpermission/monitor", UI_PERMISSION_ACTION);

        remoteAuthorizationManagerServiceClient.clearRoleAuthorization("role4", "/testpermission/monitor",
                UI_PERMISSION_ACTION);
        String[] allowedRolesForResource = remoteAuthorizationManagerServiceClient.getAllowedRolesForResource
                ("/testpermission/monitor", UI_PERMISSION_ACTION);

        boolean isRoleAuthorized = false;
        if (allowedRolesForResource != null){
            for (String role: allowedRolesForResource){
                if (role.equalsIgnoreCase("role4")){
                    isRoleAuthorized = true;
                }
            }
        }

        Assert.assertFalse(isRoleAuthorized, "Failed to clear resource /testpermission/monitor authorization for role4");
    }

    @Test(groups = "wso2.is", description = "Clear all resource authorizations of a role",
            dependsOnMethods = "testAuthorizeRole")
    public void testClearAllRoleAuthorization() throws Exception{
        remoteAuthorizationManagerServiceClient.authorizeRole("role5", "/testpermission/manage", UI_PERMISSION_ACTION);
        remoteAuthorizationManagerServiceClient.authorizeRole("role5", "/testpermission/monitor", UI_PERMISSION_ACTION);

        remoteAuthorizationManagerServiceClient.clearAllRoleAuthorization("role5");
        String[] allowedRolesForResource = remoteAuthorizationManagerServiceClient.getAllowedRolesForResource
                ("/testpermission/manage", UI_PERMISSION_ACTION);

        boolean isRoleAuthorizedToManage = false;
        boolean isRoleAuthorizedToMonitor = false;

        if (allowedRolesForResource != null){
            for (String role: allowedRolesForResource){
                if (role.equalsIgnoreCase("role5")){
                    isRoleAuthorizedToManage = true;
                }
            }
        }

        allowedRolesForResource = remoteAuthorizationManagerServiceClient.getAllowedRolesForResource
                ("/testpermission/monitor", UI_PERMISSION_ACTION);
        if (allowedRolesForResource != null){
            for (String role: allowedRolesForResource){
                if (role.equalsIgnoreCase("role5")){
                    isRoleAuthorizedToMonitor = true;
                }
            }
        }

        Assert.assertTrue(!isRoleAuthorizedToManage && !isRoleAuthorizedToMonitor,
                "Failed to clear all resource authorizations for role role5");
    }

    @Test(groups = "wso2.is", description = "Clear a resource authorization of a user",
            dependsOnMethods = "testAuthorizeUser")
    public void testClearUserAuthorization() throws Exception{
        remoteAuthorizationManagerServiceClient.authorizeUser("user2","/permission/admin/login", UI_PERMISSION_ACTION);

        remoteAuthorizationManagerServiceClient.clearUserAuthorization("user2", "/permission/admin/login",
                UI_PERMISSION_ACTION);
        String[] allowedResourcesForUser = remoteAuthorizationManagerServiceClient.getAllowedUIResourcesForUser
                ("user2", null);

        boolean isUserAuthorized = false;
        if (allowedResourcesForUser != null){
            for (String resource: allowedResourcesForUser){
                if (resource.equalsIgnoreCase("/permission/admin/login")){
                    isUserAuthorized = true;
                }
            }
        }

        Assert.assertFalse(isUserAuthorized, "Failed to clear resource /permission/admin/login authorization for user2");
    }

    @Test(groups = "wso2.is", description = "Clear all resource authorizations of a user",
            dependsOnMethods = "testAuthorizeUser")
    public void testClearAllUserAuthorization() throws Exception{
        remoteAuthorizationManagerServiceClient.authorizeUser("user3", "/permission/admin/login", UI_PERMISSION_ACTION);

        remoteAuthorizationManagerServiceClient.clearAllUserAuthorization("user3");

        String[] allowedResourcesForUser = remoteAuthorizationManagerServiceClient.getAllowedUIResourcesForUser
                ("user3", null);

        Assert.assertNull(allowedResourcesForUser, "Failed to clear all resource authorizations for user3");
    }

    @Test(groups = "wso2.is", description = "Deny a resource authorization of a role",
            dependsOnMethods = "testAuthorizeRole")
    public void testDenyRole() throws Exception{
        remoteAuthorizationManagerServiceClient.authorizeRole("role6", "/permission/admin/login", UI_PERMISSION_ACTION);

        remoteAuthorizationManagerServiceClient.denyRole("role6", "/permission/admin/login", UI_PERMISSION_ACTION);

        String[] deniedRolesForResource = remoteAuthorizationManagerServiceClient.getDeniedRolesForResource
                ("/permission/admin/login", UI_PERMISSION_ACTION);

        boolean isRoleDenied = false;
        if (deniedRolesForResource != null){
            for (String role: deniedRolesForResource){
                if (role.equalsIgnoreCase("role6")){
                    isRoleDenied = true;
                }
            }
        }

        Assert.assertTrue(isRoleDenied, "Failed to deny role6 for resource /permission/admin/login authorization");
    }

    @Test(groups = "wso2.is", description = "Deny a resource authorization for a user",
            dependsOnMethods = "testAuthorizeUser")
    public void testDenyUser() throws Exception{
        remoteAuthorizationManagerServiceClient.authorizeUser("user4", "/permission/admin/login", UI_PERMISSION_ACTION);

        remoteAuthorizationManagerServiceClient.denyUser("user4", "/permission/admin/login", UI_PERMISSION_ACTION);

        String[] deniedUsersForResource = remoteAuthorizationManagerServiceClient.getExplicitlyDeniedUsersForResource
                ("/permission/admin/login", UI_PERMISSION_ACTION);

        boolean isUserDenied = false;
        if (deniedUsersForResource != null){
            for (String user: deniedUsersForResource){
                if (user.equalsIgnoreCase("user4")){
                    isUserDenied = true;
                }
            }
        }

        Assert.assertTrue(isUserDenied, "Failed to deny user4 for resource /permission/admin/login authorization");
    }

    @Test(groups = "wso2.is", description = "Get allowed roles for a resource",
            dependsOnMethods = "testAuthorizeRole")
    public void testGetAllowedRolesForResource() throws Exception{
        remoteAuthorizationManagerServiceClient.authorizeRole("role1", "/testpermission/login", UI_PERMISSION_ACTION);
        remoteAuthorizationManagerServiceClient.authorizeRole("role2", "/testpermission/login", UI_PERMISSION_ACTION);
        remoteAuthorizationManagerServiceClient.authorizeRole("role3", "/testpermission/login", UI_PERMISSION_ACTION);

        String[] allowedRolesForResource = remoteAuthorizationManagerServiceClient.getAllowedRolesForResource
                ("/testpermission/login", UI_PERMISSION_ACTION);

        if (allowedRolesForResource != null && allowedRolesForResource.length == 3){
            List<String> allowedRolesList = Arrays.asList(allowedRolesForResource);
            Assert.assertTrue(allowedRolesList.contains("role1"), "Failed to get role1 authorized for resource " +
                    "/testpermission/login");
            Assert.assertTrue(allowedRolesList.contains("role2"), "Failed to get role2 authorized for resource " +
                    "/testpermission/login");
            Assert.assertTrue(allowedRolesList.contains("role3"), "Failed to get role3 authorized for resource " +
                    "/testpermission/login");
        }else{
            Assert.fail("Failed to get authorized roles for resource /testpermission/login");
        }

    }

    @Test(groups = "wso2.is", description = "Get allowed roles for a resource",
            dependsOnMethods = "testGetAllowedRolesForResource")
    public void testGetDeniedRolesForResource() throws Exception{
        remoteAuthorizationManagerServiceClient.denyRole("role1", "/testpermission/login", UI_PERMISSION_ACTION);
        remoteAuthorizationManagerServiceClient.denyRole("role2", "/testpermission/login", UI_PERMISSION_ACTION);
        remoteAuthorizationManagerServiceClient.denyRole("role3", "/testpermission/login", UI_PERMISSION_ACTION);

        String[] deniedRolesForResource = remoteAuthorizationManagerServiceClient.getDeniedRolesForResource
                ("/testpermission/login", UI_PERMISSION_ACTION);

        if (deniedRolesForResource != null && deniedRolesForResource.length == 3){
            List<String> deniedRoleList = Arrays.asList(deniedRolesForResource);
            Assert.assertTrue( deniedRoleList.contains("role1"), "Failed to get role1 denied for resource " +
                    "/testpermission/login");
            Assert.assertTrue( deniedRoleList.contains("role2"), "Failed to get role2 denied for resource " +
                    "/testpermission/login");
            Assert.assertTrue( deniedRoleList.contains("role3"), "Failed to get role3 denied for resource " +
                    "/testpermission/login");
        }else{
            Assert.fail("Failed to get denied roles for resource /testpermission/login");
        }
    }

    @Test(groups = "wso2.is", description = "Get allowed users for a resource",
            dependsOnMethods = "testAuthorizeUser")
    public void testGetExplicitlyAllowedUsersForResource() throws Exception{
        remoteAuthorizationManagerServiceClient.authorizeUser("user1", "/testpermission/login", UI_PERMISSION_ACTION);
        remoteAuthorizationManagerServiceClient.authorizeUser("user2", "/testpermission/login", UI_PERMISSION_ACTION);
        remoteAuthorizationManagerServiceClient.authorizeUser("user3", "/testpermission/login", UI_PERMISSION_ACTION);

        String[] allowedUsersForResource = remoteAuthorizationManagerServiceClient.getExplicitlyAllowedUsersForResource
                ("/testpermission/login", UI_PERMISSION_ACTION);

        if (allowedUsersForResource != null && allowedUsersForResource.length == 3){
            List<String> allowedUsersList = Arrays.asList(allowedUsersForResource);
            Assert.assertTrue(allowedUsersList.contains("user1"), "Failed to get user1 authorized for resource " +
                    "/testpermission/login");
            Assert.assertTrue(allowedUsersList.contains("user2"), "Failed to get user2 authorized for resource " +
                    "/testpermission/login");
            Assert.assertTrue(allowedUsersList.contains("user3"), "Failed to get user3 authorized for resource " +
                    "/testpermission/login");
        }else{
            Assert.fail("Failed to get authorized users for resource /testpermission/login");
        }
    }

    @Test(groups = "wso2.is", description = "Get allowed users for a resource",
            dependsOnMethods = "testGetExplicitlyAllowedUsersForResource")
    public void testGetExplicitlyDeniedUsersForResource() throws Exception{
        remoteAuthorizationManagerServiceClient.denyUser("user1", "/testpermission/login", UI_PERMISSION_ACTION);
        remoteAuthorizationManagerServiceClient.denyUser("user2", "/testpermission/login", UI_PERMISSION_ACTION);
        remoteAuthorizationManagerServiceClient.denyUser("user3", "/testpermission/login", UI_PERMISSION_ACTION);

        String[] deniedUsersForResource = remoteAuthorizationManagerServiceClient.getExplicitlyDeniedUsersForResource
                ("/testpermission/login", UI_PERMISSION_ACTION);

        if (deniedUsersForResource != null && deniedUsersForResource.length == 3){
            List<String> deniedUserList = Arrays.asList(deniedUsersForResource);
            Assert.assertTrue( deniedUserList.contains("user1"), "Failed to get user1 denied for resource " +
                    "/testpermission/login");
            Assert.assertTrue( deniedUserList.contains("user2"), "Failed to get user2 denied for resource " +
                    "/testpermission/login");
            Assert.assertTrue( deniedUserList.contains("user3"), "Failed to get user3 denied for resource " +
                    "/testpermission/login");
        }else{
            Assert.fail("Failed to get denied roles for resource /testpermission/login");
        }
    }

    @Test(groups = "wso2.is", description = "Get allowed resources for user")
    public void testGetAllowedUIResourcesForUser() throws Exception{
        remoteAuthorizationManagerServiceClient.authorizeUser("user1","/permission/admin/login", UI_PERMISSION_ACTION);
        remoteAuthorizationManagerServiceClient.authorizeUser("user1","/permission/admin/manage", UI_PERMISSION_ACTION);
        remoteAuthorizationManagerServiceClient.authorizeUser("user1","/permission/admin/monitor",
                UI_PERMISSION_ACTION);

        String[] allowedResourcesForUser = remoteAuthorizationManagerServiceClient.getAllowedUIResourcesForUser
                ("user1", null);
        if (allowedResourcesForUser != null && allowedResourcesForUser.length == 3){
            List<String> allowedResourceList = Arrays.asList(allowedResourcesForUser);
            Assert.assertTrue( allowedResourceList.contains("/permission/admin/login"),
                    "Failed to get /permission/admin/login allowed for user1");
            Assert.assertTrue( allowedResourceList.contains("/permission/admin/manage"),
                    "Failed to get /permission/admin/manage allowed for user1");
            Assert.assertTrue( allowedResourceList.contains("/permission/admin/monitor"),
                    "Failed to get /permission/admin/monitor allowed for user1");
        }else{
            Assert.fail("Failed to get allowed resources for user1");
        }
    }

    @Test(groups = "wso2.is", description = "Reset permission")
    public void testResetPermissionOnUpdateRole() throws Exception{
        remoteAuthorizationManagerServiceClient.authorizeRole("role2", "/permission/admin/login",
                UI_PERMISSION_ACTION);
        remoteAuthorizationManagerServiceClient.authorizeRole("role2", "/permission/admin/manage",
                UI_PERMISSION_ACTION);
        remoteAuthorizationManagerServiceClient.authorizeRole("role2", "/permission/admin/monitor",
                UI_PERMISSION_ACTION);

        remoteAuthorizationManagerServiceClient.resetPermissionOnUpdateRole("role2", "role3");

        String[] rolesForResource = remoteAuthorizationManagerServiceClient.getAllowedRolesForResource
                ("/permission/admin/login", UI_PERMISSION_ACTION);
        if (rolesForResource != null){
            List<String> rolesList = Arrays.asList(rolesForResource);
            Assert.assertTrue(rolesList.contains("role3") && !rolesList.contains("role2"),
                    "Failed to reset permission /permission/admin/login for role2");
        }else{
            Assert.fail("Failed to reset permissions of role2");
        }
    }

}



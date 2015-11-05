package org.wso2.identity.integration.common.clients;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Stub;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.integration.common.admin.client.utils.AuthenticateStubUtil;
import org.wso2.carbon.user.mgt.stub.UserAdminStub;
import org.wso2.carbon.user.mgt.stub.UserAdminUserAdminException;
import org.wso2.carbon.user.mgt.stub.types.carbon.ClaimValue;
import org.wso2.carbon.user.mgt.stub.types.carbon.FlaggedName;
import org.wso2.carbon.user.mgt.stub.types.carbon.UIPermissionNode;
import org.wso2.carbon.user.mgt.stub.types.carbon.UserRealmInfo;

import javax.activation.DataHandler;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class UserManagementClient {
    private static final int LIMIT = 100;
    private final Log log = LogFactory.getLog(UserManagementClient.class);
    private final String serviceName = "UserAdmin";
    private UserAdminStub userAdminStub;

    public UserManagementClient(String backendURL, String sessionCookie) throws AxisFault {
        String endPoint = backendURL + serviceName;
        userAdminStub = new UserAdminStub(endPoint);
        AuthenticateStubUtil.authenticateStub(sessionCookie, userAdminStub);
    }

    public UserManagementClient(String backendURL, String userName, String password)
            throws AxisFault {
        String endPoint = backendURL + serviceName;
        userAdminStub = new UserAdminStub(endPoint);
        AuthenticateStubUtil.authenticateStub(userName, password, userAdminStub);
    }

    public Stub getServiceStub() {
        return this.userAdminStub;
    }

    public static ClaimValue[] toADBClaimValues(
            ClaimValue[] claimValues) {
        if (claimValues == null) {
            return new ClaimValue[0];
        }
        ClaimValue[] values = new ClaimValue[claimValues.length];
        for (ClaimValue cvalue : claimValues) {
            ClaimValue value = new ClaimValue();
            value.setClaimURI(cvalue.getClaimURI());
            value.setValue(cvalue.getValue());
        }
        return values;
    }

    public void addRole(String roleName, String[] userList, String[] permissions) throws
            RemoteException,
            UserAdminUserAdminException {
        userAdminStub.addRole(roleName, userList, permissions, false);
    }

    public void addRole(String roleName, String[] userList, String[] permissions,
                        boolean isSharedRole) throws RemoteException, UserAdminUserAdminException {
        userAdminStub.addRole(roleName, userList, permissions, isSharedRole);
    }

    public void addUser(String userName, String password, String[] roles,
                        String profileName) throws RemoteException, UserAdminUserAdminException {
        userAdminStub.addUser(userName, password, roles, null, profileName);
    }

    public void deleteRole(String roleName) throws RemoteException, UserAdminUserAdminException {
        userAdminStub.deleteRole(roleName);
    }

    public void deleteUser(String userName) throws RemoteException, UserAdminUserAdminException {
        userAdminStub.deleteUser(userName);
    }

    private void addRoleWithUser(String roleName, String userName, String[] permission)
            throws RemoteException, UserAdminUserAdminException {
        userAdminStub.addRole(roleName, new String[]{userName}, null, false);
        FlaggedName[] roles = userAdminStub.getAllRolesNames(roleName, 100);
        for (FlaggedName role : roles) {
            if (!role.getItemName().equals(roleName)) {
                continue;
            } else {
                assert (role.getItemName().equals(roleName));
            }
            assert false : "Role: " + roleName + " was not added properly.";
        }
    }

    private void addRoleWithUser(String roleName, String userName, boolean isSharedRole)
            throws RemoteException, UserAdminUserAdminException {
        userAdminStub.addRole(roleName, new String[]{userName}, null, isSharedRole);
        FlaggedName[] roles = userAdminStub.getAllRolesNames(roleName, 100);
        for (FlaggedName role : roles) {
            if (!role.getItemName().equals(roleName)) {
                continue;
            } else {
                assert (role.getItemName().equals(roleName));
            }
            assert false : "Role: " + roleName + " was not added properly.";
        }
    }

    public void updateUserListOfRole(String roleName, String[] addingUsers,
                                     String[] deletingUsers)
            throws UserAdminUserAdminException, RemoteException {
        List<FlaggedName> updatedUserList = new ArrayList<FlaggedName>();
        if (addingUsers != null) {
            for (String addUser : addingUsers) {
                FlaggedName fName = new FlaggedName();
                fName.setItemName(addUser);
                fName.setSelected(true);
                updatedUserList.add(fName);
            }
        }
        //add deleted users to the list
        if (deletingUsers != null) {
            for (String deletedUser : deletingUsers) {
                FlaggedName fName = new FlaggedName();
                fName.setItemName(deletedUser);
                fName.setSelected(false);
                updatedUserList.add(fName);
            }
        }
        //call userAdminStub to update user list of role

        userAdminStub.updateUsersOfRole(roleName, updatedUserList.toArray(
                new FlaggedName[updatedUserList.size()]));
        //if delete users in retrieved list, fail
        if (deletingUsers != null) {
            for (String deletedUser : deletingUsers) {
                FlaggedName[] verifyingList;
                verifyingList = userAdminStub.getUsersOfRole(roleName, deletedUser, LIMIT);
                assert (!verifyingList[0].getSelected());
            }
        }
        if (addingUsers != null) {
            //if all added users are not in list fail
            for (String addingUser : addingUsers) {
                FlaggedName[] verifyingList = userAdminStub.getUsersOfRole(roleName, addingUser, LIMIT);
                assert (verifyingList[0].getSelected());
            }
        }

    }

    public boolean roleNameExists(String roleName)
            throws RemoteException, UserAdminUserAdminException {
        FlaggedName[] roles;
        roles = userAdminStub.getAllRolesNames(roleName, LIMIT);
        for (FlaggedName role : roles) {
            if (role.getItemName().equals(roleName)) {
                log.info("Role name " + roleName + " already exists");
                return true;
            }
        }
        return false;
    }

    /**
     * Lists all roles caught by wither with in limit
     */
    public FlaggedName[] listRoles(String filter, int limit)
            throws RemoteException, UserAdminUserAdminException {
        return userAdminStub.getAllRolesNames(filter, limit);
    }

    /**
     * Lists all users with in filter and limit
     */
    public String[] listUsers(String filter, int limit)
            throws RemoteException, UserAdminUserAdminException {
        return userAdminStub.listUsers(filter, limit);
    }

    public FlaggedName[] listAllUsers(String filter, int limit) throws RemoteException,
            UserAdminUserAdminException {
        return userAdminStub.listAllUsers(filter, limit);
    }

    public boolean userNameExists(String roleName, String userName)
            throws RemoteException, UserAdminUserAdminException {

        FlaggedName[] users;
        users = userAdminStub.getUsersOfRole(roleName, "*", LIMIT);

        for (FlaggedName user : users) {
            if (user.getItemName().equals(userName)) {
                log.info("User name " + userName + " already exists");
                return true;
            }
        }
        return false;
    }

    public Boolean hasMultipleUserStores() throws RemoteException, UserAdminUserAdminException {
        return userAdminStub.hasMultipleUserStores();
    }

    public void addInternalRole(String roleName, String[] userList, String[] permissions)
            throws RemoteException, UserAdminUserAdminException {
        userAdminStub.addInternalRole(roleName, userList, permissions);
    }

    public FlaggedName[] getAllRolesNames(String filter, int limit)
            throws RemoteException, UserAdminUserAdminException {
        return userAdminStub.getAllRolesNames(filter, limit);
    }

    public void addRemoveUsersOfRole(String roleName, String[] newUsers, String[] deletedUsers)
            throws RemoteException, UserAdminUserAdminException {
        userAdminStub.addRemoveUsersOfRole(roleName, newUsers, deletedUsers);
    }

    public void addRemoveRolesOfUser(String userName, String[] newRoles, String[] deletedRoles)
            throws RemoteException, UserAdminUserAdminException {
        userAdminStub.addRemoveRolesOfUser(userName, newRoles, deletedRoles);
    }

    public FlaggedName[] getUsersOfRole(String roleName, String filter, int limit)
            throws RemoteException, UserAdminUserAdminException {
        return userAdminStub.getUsersOfRole(roleName, filter, limit);
    }

    public FlaggedName[] getRolesOfUser(String userName, String filter, int limit)
            throws RemoteException, UserAdminUserAdminException {
        return userAdminStub.getRolesOfUser(userName, filter, limit);
    }

    public void updateUsersOfRole(String roleName, FlaggedName[] userList)
            throws RemoteException, UserAdminUserAdminException {
        userAdminStub.updateUsersOfRole(roleName, userList);
    }

    public void updateRolesOfUser(String userName, String[] newUserList)
            throws RemoteException, UserAdminUserAdminException {
        userAdminStub.updateRolesOfUser(userName, newUserList);
    }

    public void changePassword(String userName, String newPassword)
            throws RemoteException, UserAdminUserAdminException {
        userAdminStub.changePassword(userName, newPassword);
    }

    public void updateRoleName(String roleName, String newRoleName)
            throws RemoteException, UserAdminUserAdminException {
        userAdminStub.updateRoleName(roleName, newRoleName);
    }

    public void changePasswordByUser(String userName, String oldPassword, String newPassword)
            throws RemoteException, UserAdminUserAdminException {
        userAdminStub.changePasswordByUser(userName, oldPassword, newPassword);
    }

    public FlaggedName[] getAllSharedRoleNames(String filter, int limit)
            throws RemoteException, UserAdminUserAdminException {
        return userAdminStub.getAllSharedRoleNames(filter, limit);
    }

    public UIPermissionNode getAllUIPermissions()
            throws RemoteException, UserAdminUserAdminException {
        return userAdminStub.getAllUIPermissions();
    }

    public void setRoleUIPermission(String roleName, String[] rawResources)
            throws RemoteException, UserAdminUserAdminException {
        userAdminStub.setRoleUIPermission(roleName, rawResources);
    }

    public UIPermissionNode getRolePermissions(String roleName)
            throws RemoteException, UserAdminUserAdminException {
        return userAdminStub.getRolePermissions(roleName);
    }

    public FlaggedName[] getRolesOfCurrentUser()
            throws RemoteException, UserAdminUserAdminException {
        return userAdminStub.getRolesOfCurrentUser();
    }

    public FlaggedName[] listUserByClaim(ClaimValue claimValue, String filter, int maxLimit)
            throws RemoteException, UserAdminUserAdminException {
        return userAdminStub.listUserByClaim(claimValue, filter, maxLimit);
    }

    public UserRealmInfo getUserRealmInfo()
            throws RemoteException, UserAdminUserAdminException {
        return userAdminStub.getUserRealmInfo();
    }

    public boolean isSharedRolesEnabled()
            throws RemoteException, UserAdminUserAdminException {
        return userAdminStub.isSharedRolesEnabled();
    }

    public void bulkImportUsers(String userStoreDomain, String filename, DataHandler handler, String defaultPassword)
            throws RemoteException, UserAdminUserAdminException {
        userAdminStub.bulkImportUsers(userStoreDomain, filename, handler, defaultPassword);
    }

    public HashSet<String> getUserList() throws RemoteException, UserAdminUserAdminException {
        return new HashSet<String>(Arrays.asList(userAdminStub.listUsers("*", LIMIT)));
    }
}
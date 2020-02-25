/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.identity.integration.common.clients.usermgt.uuid;

import org.apache.axis2.AxisFault;
import org.wso2.carbon.identity.test.integration.service.stub.AuthenticationResultDTO;
import org.wso2.carbon.identity.test.integration.service.stub.ClaimDTO;
import org.wso2.carbon.identity.test.integration.service.stub.ClaimValue;
import org.wso2.carbon.identity.test.integration.service.stub.ConditionDTO;
import org.wso2.carbon.identity.test.integration.service.stub.LoginIdentifierDTO;
import org.wso2.carbon.identity.test.integration.service.stub.PermissionDTO;
import org.wso2.carbon.identity.test.integration.service.stub.UUIDUserStoreManagerServiceStub;
import org.wso2.carbon.identity.test.integration.service.stub.UUIDUserStoreManagerServiceUserStoreExceptionException;
import org.wso2.carbon.identity.test.integration.service.stub.UniqueIDUserClaimSearchEntryDAO;
import org.wso2.carbon.identity.test.integration.service.stub.UserDTO;
import org.wso2.carbon.identity.test.integration.service.stub.UserRoleListDTO;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.identity.integration.common.clients.AuthenticateStub;

import java.rmi.RemoteException;
import java.util.Date;
import java.util.List;

/**
 * Service client for the UUID User Store service.
 */
public class UUIDUserStoreManagerServiceClient {

    private String serviceName = "UUIDUserStoreManagerService";
    private UUIDUserStoreManagerServiceStub uuidUserStoreManagerServiceStub;

    public UUIDUserStoreManagerServiceClient(String backEndUrl, String sessionCookie) throws AxisFault {

        String endPoint = backEndUrl + serviceName;
        uuidUserStoreManagerServiceStub = new UUIDUserStoreManagerServiceStub(endPoint);
        AuthenticateStub.authenticateStub(sessionCookie, uuidUserStoreManagerServiceStub);
    }

    public UUIDUserStoreManagerServiceClient(String backEndUrl, String username, String password) throws AxisFault {

        String endPoint = backEndUrl + serviceName;
        uuidUserStoreManagerServiceStub = new UUIDUserStoreManagerServiceStub(endPoint);
        AuthenticateStub.authenticateStub(username, password, uuidUserStoreManagerServiceStub);
    }

    public UserDTO addUserWithID(String userName, String credential, String[] roleList, ClaimValue[] claims,
                                 String profileName) throws UserStoreException, RemoteException,
            UUIDUserStoreManagerServiceUserStoreExceptionException {

        return uuidUserStoreManagerServiceStub.addUserWithID(userName, credential, roleList, claims, profileName);
    }

    public AuthenticationResultDTO authenticateWithIDLoginIdentifier(LoginIdentifierDTO[] loginIdentifiers,
                                                                     String domain, String credential)
            throws UserStoreException, RemoteException, UUIDUserStoreManagerServiceUserStoreExceptionException {

        return uuidUserStoreManagerServiceStub.authenticateWithIDLoginIdentifier(loginIdentifiers, domain, credential);
    }

    public AuthenticationResultDTO authenticateWithIDUserId(String userID, String domain, String credential)
            throws UserStoreException, RemoteException, UUIDUserStoreManagerServiceUserStoreExceptionException {

        return uuidUserStoreManagerServiceStub.authenticateWithIDUserId(userID, credential);
    }

    public AuthenticationResultDTO authenticateWithIDUsernameClaim(String preferredUserNameClaim,
                                                                   String preferredUserNameValue, String credential,
                                                                   String profileName) throws UserStoreException,
            RemoteException, UUIDUserStoreManagerServiceUserStoreExceptionException {

        return uuidUserStoreManagerServiceStub.authenticateWithIDUsernameClaim(preferredUserNameClaim,
                preferredUserNameValue, credential, profileName);
    }

    public void deleteUserClaimValuesWithID(String userID, String[] claims, String profileName)
            throws UserStoreException, RemoteException, UUIDUserStoreManagerServiceUserStoreExceptionException {

        uuidUserStoreManagerServiceStub.deleteUserClaimValuesWithID(userID, claims, profileName);
    }

    public Date getPasswordExpirationTimeWithID(String userId) throws UserStoreException, RemoteException,
            UUIDUserStoreManagerServiceUserStoreExceptionException {

        return uuidUserStoreManagerServiceStub.getPasswordExpirationTimeWithID(userId);
    }

    public boolean isUserInRoleWithID(String userID, String roleName) throws UserStoreException, RemoteException,
            UUIDUserStoreManagerServiceUserStoreExceptionException {

        return uuidUserStoreManagerServiceStub.isUserInRoleWithID(userID, roleName);
    }

    public UserDTO[] listUsersWithID(String filter, int limit, int offset) throws UserStoreException, RemoteException,
            UUIDUserStoreManagerServiceUserStoreExceptionException {

        return uuidUserStoreManagerServiceStub.listUsersWithOffsetWithID(filter, limit, offset);
    }

    public UserDTO[] getUserListWithID(String claim, String claimValue, String profileName, int limit, int offset)
            throws UserStoreException, RemoteException, UUIDUserStoreManagerServiceUserStoreExceptionException {

        return uuidUserStoreManagerServiceStub.getUserListWithOffsetWithID(claim, claimValue, profileName, limit,
                offset);
    }

    public UserDTO[] getUserListWithID(ConditionDTO conditionDTO, String domain, String profileName, int limit,
                                       int offset, String sortBy, String sortOrder) throws UserStoreException,
            RemoteException, UUIDUserStoreManagerServiceUserStoreExceptionException {

        return uuidUserStoreManagerServiceStub.getUserListWithIDCondition(conditionDTO, domain, profileName, limit,
                offset, sortBy, sortOrder);
    }

    public UniqueIDUserClaimSearchEntryDAO[] getUsersClaimValuesWithID(List<String> userIDs, List<String> claims,
                                                                       String profileName) throws UserStoreException,
            RemoteException, UUIDUserStoreManagerServiceUserStoreExceptionException {

        return uuidUserStoreManagerServiceStub.getUsersClaimValuesWithID(userIDs.toArray(new String[0]),
                claims.toArray(new String[0]), profileName);
    }

    public UserRoleListDTO[] getRoleListOfUsersWithID(String[] userIDs) throws UserStoreException, RemoteException,
            UUIDUserStoreManagerServiceUserStoreExceptionException {

        return uuidUserStoreManagerServiceStub.getRoleListOfUsersWithID(userIDs);
    }

    public void deleteUserClaimValueWithID(String userID, String claimURI, String profileName)
            throws UserStoreException {

    }

    public UserDTO[] listUsersWithID(String filter, int maxItemLimit) throws UserStoreException, RemoteException,
            UUIDUserStoreManagerServiceUserStoreExceptionException {

        return uuidUserStoreManagerServiceStub.listUsersWithID(filter, maxItemLimit);
    }

    public UserDTO getUserWithID(String userID, String[] requestedClaims, String profileName)
            throws UserStoreException, RemoteException, UUIDUserStoreManagerServiceUserStoreExceptionException {

        return uuidUserStoreManagerServiceStub.getUserWithID(userID, requestedClaims, profileName);
    }

    public boolean isExistingUserWithID(String userID) throws UserStoreException, RemoteException,
            UUIDUserStoreManagerServiceUserStoreExceptionException {

        return uuidUserStoreManagerServiceStub.isExistingUserWithID(userID);
    }

    public String[] getProfileNamesWithID(String userID) throws UserStoreException, RemoteException,
            UUIDUserStoreManagerServiceUserStoreExceptionException {

        return uuidUserStoreManagerServiceStub.getProfileNamesWithID(userID);
    }

    public String[] getRoleListOfUserWithID(String userID) throws UserStoreException, RemoteException,
            UUIDUserStoreManagerServiceUserStoreExceptionException {

        return uuidUserStoreManagerServiceStub.getRoleListOfUserWithID(userID);
    }

    public UserDTO[] getUserListOfRoleWithID(String roleName) throws UserStoreException, RemoteException,
            UUIDUserStoreManagerServiceUserStoreExceptionException {

        return uuidUserStoreManagerServiceStub.getUserListOfRoleWithID(roleName);
    }

    public UserDTO[] getUserListOfRoleWithID(String roleName, String filter, int maxItemLimit)
            throws UserStoreException, RemoteException, UUIDUserStoreManagerServiceUserStoreExceptionException {

        return uuidUserStoreManagerServiceStub.getUserListOfRoleFilteredWithID(roleName, filter, maxItemLimit);
    }

    public String getUserClaimValueWithID(String userID, String claim, String profileName) throws UserStoreException,
            RemoteException, UUIDUserStoreManagerServiceUserStoreExceptionException {

        return uuidUserStoreManagerServiceStub.getUserClaimValueWithID(userID, claim, profileName);
    }

    public ClaimValue[] getUserClaimValuesWithID(String userID, String[] claims, String profileName)
            throws UserStoreException, RemoteException, UUIDUserStoreManagerServiceUserStoreExceptionException {

        return uuidUserStoreManagerServiceStub.getUserClaimValuesForGivenClaimsWithID(userID, claims, profileName);
    }

    public ClaimDTO[] getUserClaimValuesWithID(String userID, String profileName) throws UserStoreException,
            RemoteException, UUIDUserStoreManagerServiceUserStoreExceptionException {

        return uuidUserStoreManagerServiceStub.getUserClaimValuesWithID(userID, profileName);
    }

    public void deleteUserWithID(String userID) throws UserStoreException, RemoteException,
            UUIDUserStoreManagerServiceUserStoreExceptionException {

        uuidUserStoreManagerServiceStub.deleteUserWithID(userID);
    }

    public void setUserClaimValueWithID(String userID, String claimURI, String claimValue, String profileName)
            throws UserStoreException {

    }

    public void setUserClaimValuesWithID(String userID, ClaimValue[] claims, String profileName)
            throws UserStoreException, RemoteException, UUIDUserStoreManagerServiceUserStoreExceptionException {

        uuidUserStoreManagerServiceStub.setUserClaimValuesWithID(userID, claims, profileName);
    }

    public UserDTO[] getUserListWithID(String claim, String claimValue, String profileName) throws UserStoreException,
            RemoteException, UUIDUserStoreManagerServiceUserStoreExceptionException {

        return uuidUserStoreManagerServiceStub.getUserListWithID(claim, claimValue, profileName);
    }

    public void updateCredentialWithID(String userID, String newCredential, String oldCredential)
            throws UserStoreException, RemoteException, UUIDUserStoreManagerServiceUserStoreExceptionException {

        uuidUserStoreManagerServiceStub.updateCredentialWithID(userID, newCredential, oldCredential);
    }

    public void updateCredentialByAdminWithID(String userID, String newCredential) throws UserStoreException,
            RemoteException, UUIDUserStoreManagerServiceUserStoreExceptionException {

        uuidUserStoreManagerServiceStub.updateCredentialByAdminWithID(userID, newCredential);
    }

    public void addRoleWithID(String roleName, String[] userIDList, PermissionDTO[] permissionsDTOs,
                              boolean isSharedRole) throws UserStoreException, RemoteException,
            UUIDUserStoreManagerServiceUserStoreExceptionException {

        uuidUserStoreManagerServiceStub.addRoleWithID(roleName, userIDList, permissionsDTOs, isSharedRole);
    }

    public void updateUserListOfRoleWithID(String roleName, String[] deletedUserIDs, String[] newUserIDs)
            throws UserStoreException, RemoteException, UUIDUserStoreManagerServiceUserStoreExceptionException {

        uuidUserStoreManagerServiceStub.updateUserListOfRoleWithID(roleName, deletedUserIDs, newUserIDs);
    }

    public void updateRoleListOfUserWithID(String userID, String[] deletedRoles, String[] newRoles)
            throws UserStoreException, RemoteException, UUIDUserStoreManagerServiceUserStoreExceptionException {

        uuidUserStoreManagerServiceStub.updateRoleListOfUserWithID(userID, deletedRoles, newRoles);
    }
}

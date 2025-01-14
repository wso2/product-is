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

package org.wso2.carbon.identity.test.integration.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.identity.test.integration.service.dao.Attribute;
import org.wso2.carbon.identity.test.integration.service.dao.AuthenticationResultDTO;
import org.wso2.carbon.identity.test.integration.service.dao.ClaimDTO;
import org.wso2.carbon.identity.test.integration.service.dao.ClaimValue;
import org.wso2.carbon.identity.test.integration.service.dao.ConditionDTO;
import org.wso2.carbon.identity.test.integration.service.dao.FailureReasonDTO;
import org.wso2.carbon.identity.test.integration.service.dao.LoginIdentifierDTO;
import org.wso2.carbon.identity.test.integration.service.dao.PermissionDTO;
import org.wso2.carbon.identity.test.integration.service.dao.UniqueIDUserClaimSearchEntryDAO;
import org.wso2.carbon.identity.test.integration.service.dao.UserClaimSearchEntryDAO;
import org.wso2.carbon.identity.test.integration.service.dao.UserDTO;
import org.wso2.carbon.identity.test.integration.service.dao.UserRoleListDTO;
import org.wso2.carbon.identity.test.integration.service.dao.UserStoreException;
import org.wso2.carbon.user.core.Permission;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.claim.Claim;
import org.wso2.carbon.user.core.common.AbstractUserStoreManager;
import org.wso2.carbon.user.core.common.AuthenticationResult;
import org.wso2.carbon.user.core.common.FailureReason;
import org.wso2.carbon.user.core.common.LoginIdentifier;
import org.wso2.carbon.user.core.common.User;
import org.wso2.carbon.user.core.model.Condition;
import org.wso2.carbon.user.core.model.UniqueIDUserClaimSearchEntry;
import org.wso2.carbon.user.core.model.UserClaimSearchEntry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Admin service to call methods using unique user id. This service currently only used for integration tests.
 */
public class UUIDUserStoreManagerService {

    private static final Log log = LogFactory.getLog(UUIDUserStoreManagerService.class);
    private static final String NULL_REALM_MESSAGE = "UserRealm is null";

    public UserDTO addUserWithID(String userName, String credential, String[] roleList, ClaimValue[] claims,
                                 String profileName) throws UserStoreException {

        try {
            return getUserDTO(getUserStoreManager().addUniqueIdToUser(userName, credential, roleList,
                    convertClaimValueToMap(claims), profileName));
        } catch (org.wso2.carbon.user.core.UserStoreException e) {
            log.error("Error while calling the service method.", e);
            throw new UserStoreException(e.getMessage(), e.getErrorCode(), e.getCause());
        }
    }

    public AuthenticationResultDTO authenticateWithIDLoginIdentifier(LoginIdentifierDTO[] loginIdentifiers,
                                                                     String domain, String credential)
            throws UserStoreException {

        List<LoginIdentifier> identifierList = Arrays
                .asList(getLoginIdentifierListFromLoginIdentifierList(loginIdentifiers));
        try {
            return getAuthenticationResultDTOFromAuthenticationResult(getUserStoreManager()
                    .authenticateWithID(identifierList, domain, credential));
        } catch (org.wso2.carbon.user.core.UserStoreException e) {
            log.error("Error while calling the service method.", e);
            throw new UserStoreException(e.getMessage(), e.getErrorCode(), e.getCause());
        }
    }

    public AuthenticationResultDTO authenticateWithIDUserId(String userID, String credential)
            throws UserStoreException {

        try {
            return getAuthenticationResultDTOFromAuthenticationResult(getUserStoreManager()
                    .authenticateWithID(userID, credential));
        } catch (org.wso2.carbon.user.core.UserStoreException e) {
            log.error("Error while calling the service method.", e);
            throw new UserStoreException(e.getMessage(), e.getErrorCode(), e.getCause());
        }
    }

    public AuthenticationResultDTO authenticateWithIDUsernameClaim(String preferredUserNameClaim,
                                                                   String preferredUserNameValue, String credential,
                                                                   String profileName)
            throws UserStoreException {

        try {
            return getAuthenticationResultDTOFromAuthenticationResult(getUserStoreManager()
                    .authenticateWithID(preferredUserNameClaim, preferredUserNameValue, credential, profileName));
        } catch (org.wso2.carbon.user.core.UserStoreException e) {
            log.error("Error while calling the service method.", e);
            throw new UserStoreException(e.getMessage(), e.getErrorCode(), e.getCause());
        }
    }

    public void deleteUserClaimValuesWithID(String userID, String[] claims, String profileName)
            throws UserStoreException {

        try {
            getUserStoreManager().deleteUserClaimValuesWithID(userID, claims, profileName);
        } catch (org.wso2.carbon.user.core.UserStoreException e) {
            log.error("Error while calling the service method.", e);
            throw new UserStoreException(e.getMessage(), e.getErrorCode(), e.getCause());
        }
    }

    public Date getPasswordExpirationTimeWithID(String userId) throws UserStoreException {

        try {
            return getUserStoreManager().getPasswordExpirationTimeWithID(userId);
        } catch (org.wso2.carbon.user.core.UserStoreException e) {
            log.error("Error while calling the service method.", e);
            throw new UserStoreException(e.getMessage(), e.getErrorCode(), e.getCause());
        }
    }

    public boolean isUserInRoleWithID(String userID, String roleName) throws UserStoreException {

        try {
            return getUserStoreManager().isUserInRoleWithID(userID, roleName);
        } catch (org.wso2.carbon.user.core.UserStoreException e) {
            log.error("Error while calling the service method.", e);
            throw new UserStoreException(e.getMessage(), e.getErrorCode(), e.getCause());
        }
    }

    public UserDTO[] listUsersWithOffsetWithID(String filter, int limit, int offset) throws UserStoreException {

        try {
            return getUserDTOListFromUser(getUserStoreManager().listUsersWithID(filter, limit, offset));
        } catch (org.wso2.carbon.user.core.UserStoreException e) {
            log.error("Error while calling the service method.", e);
            throw new UserStoreException(e.getMessage(), e.getErrorCode(), e.getCause());
        }
    }

    public UserDTO[] getUserListWithOffsetWithID(String claim, String claimValue, String profileName, int limit,
                                                 int offset)
            throws UserStoreException {

        try {
            return getUserDTOListFromUser(getUserStoreManager().getUserListWithID(claim, claimValue, profileName, limit,
                    offset));
        } catch (org.wso2.carbon.user.core.UserStoreException e) {
            log.error("Error while calling the service method.", e);
            throw new UserStoreException(e.getMessage(), e.getErrorCode(), e.getCause());
        }
    }

    public UserDTO[] getUserListWithIDCondition(ConditionDTO conditionDTO, String domain, String profileName, int limit,
                                                int offset, String sortBy, String sortOrder)
            throws UserStoreException {

        org.wso2.carbon.user.core.model.Condition condition = getConditionFromConditionDTO(conditionDTO);
        try {
            return getUserDTOListFromUser(getUserStoreManager().getUserListWithID(condition, domain, profileName, limit,
                    offset, sortBy, sortOrder));
        } catch (org.wso2.carbon.user.core.UserStoreException e) {
            log.error("Error while calling the service method.", e);
            throw new UserStoreException(e.getMessage(), e.getErrorCode(), e.getCause());
        }
    }

    public UniqueIDUserClaimSearchEntryDAO[] getUsersClaimValuesWithID(String [] userIDs, String [] claims,
                                                                       String profileName)
            throws UserStoreException {

        try {
            return getClaimSearchEntryDAOFromClaimSearchEntry(getUserStoreManager()
                    .getUsersClaimValuesWithID(Arrays.asList(userIDs), Arrays.asList(claims), profileName)
                    .toArray(new UniqueIDUserClaimSearchEntry[0]));
        } catch (org.wso2.carbon.user.core.UserStoreException e) {
            log.error("Error while calling the service method.", e);
            throw new UserStoreException(e.getMessage(), e.getErrorCode(), e.getCause());
        }
    }

    public UserRoleListDTO[] getRoleListOfUsersWithID(String[] userIDs) throws UserStoreException {

        try {
            return getUserRoleListDTOs(getUserStoreManager().getRoleListOfUsersWithID(Arrays.asList(userIDs)));
        } catch (org.wso2.carbon.user.core.UserStoreException e) {
            log.error("Error while calling the service method.", e);
            throw new UserStoreException(e.getMessage(), e.getErrorCode(), e.getCause());
        }
    }

    public void deleteUserClaimValueWithID(String userID, String claimURI, String profileName)
            throws UserStoreException {

        try {
            getUserStoreManager().deleteUserClaimValueWithID(userID, claimURI, profileName);
        } catch (org.wso2.carbon.user.core.UserStoreException e) {
            log.error("Error while calling the service method.", e);
            throw new UserStoreException(e.getMessage(), e.getErrorCode(), e.getCause());
        }
    }

    public UserDTO[] listUsersWithID(String filter, int maxItemLimit) throws UserStoreException {

        try {
            return getUserDTOListFromUser(getUserStoreManager().listUsersWithID(filter, maxItemLimit));
        } catch (org.wso2.carbon.user.core.UserStoreException e) {
            log.error("Error while calling the service method.", e);
            throw new UserStoreException(e.getMessage(), e.getErrorCode(), e.getCause());
        }
    }

    public UserDTO getUserWithID(String userID, String[] requestedClaims, String profileName)
            throws UserStoreException {

        try {
            return getUserDTO(getUserStoreManager().getUserWithID(userID, requestedClaims, profileName));
        } catch (org.wso2.carbon.user.core.UserStoreException e) {
            log.error("Error while calling the service method.", e);
            throw new UserStoreException(e.getMessage(), e.getErrorCode(), e.getCause());
        }
    }

    public boolean isExistingUserWithID(String userID) throws UserStoreException {

        try {
            return getUserStoreManager().isExistingUserWithID(userID);
        } catch (org.wso2.carbon.user.core.UserStoreException e) {
            log.error("Error while calling the service method.", e);
            throw new UserStoreException(e.getMessage(), e.getErrorCode(), e.getCause());
        }
    }

    public String[] getProfileNamesWithID(String userID) throws UserStoreException {

        try {
            return getUserStoreManager().getProfileNamesWithID(userID);
        } catch (org.wso2.carbon.user.core.UserStoreException e) {
            log.error("Error while calling the service method.", e);
            throw new UserStoreException(e.getMessage(), e.getErrorCode(), e.getCause());
        }
    }

    public String[] getRoleListOfUserWithID(String userID) throws UserStoreException {

        try {
            return getUserStoreManager().getRoleListOfUserWithID(userID).toArray(new String[0]);
        } catch (org.wso2.carbon.user.core.UserStoreException e) {
            log.error("Error while calling the service method.", e);
            throw new UserStoreException(e.getMessage(), e.getErrorCode(), e.getCause());
        }
    }

    public UserDTO[] getUserListOfRoleWithID(String roleName) throws UserStoreException {

        try {
            return getUserDTOListFromUser(getUserStoreManager().getUserListOfRoleWithID(roleName));
        } catch (org.wso2.carbon.user.core.UserStoreException e) {
            log.error("Error while calling the service method.", e);
            throw new UserStoreException(e.getMessage(), e.getErrorCode(), e.getCause());
        }
    }

    public UserDTO[] getUserListOfRoleFilteredWithID(String roleName, String filter, int maxItemLimit)
            throws UserStoreException {

        try {
            return getUserDTOListFromUser(getUserStoreManager().getUserListOfRoleWithID(roleName, filter,
                    maxItemLimit));
        } catch (org.wso2.carbon.user.core.UserStoreException e) {
            log.error("Error while calling the service method.", e);
            throw new UserStoreException(e.getMessage(), e.getErrorCode(), e.getCause());
        }
    }

    public String getUserClaimValueWithID(String userID, String claim, String profileName) throws UserStoreException {

        try {
            return getUserStoreManager().getUserClaimValueWithID(userID, claim, profileName);
        } catch (org.wso2.carbon.user.core.UserStoreException e) {
            log.error("Error while calling the service method.", e);
            throw new UserStoreException(e.getMessage(), e.getErrorCode(), e.getCause());
        }
    }

    public ClaimValue[] getUserClaimValuesForGivenClaimsWithID(String userID, String[] claims, String profileName)
            throws UserStoreException {

        try {
            return convertMapToClaimValue(getUserStoreManager().getUserClaimValuesWithID(userID, claims, profileName));
        } catch (org.wso2.carbon.user.core.UserStoreException e) {
            log.error("Error while calling the service method.", e);
            throw new UserStoreException(e.getMessage(), e.getErrorCode(), e.getCause());
        }
    }

    public ClaimDTO[] getUserClaimValuesWithID(String userID, String profileName) throws UserStoreException {

        try {
            return convertClaimToClaimDTO(getUserStoreManager().getUserClaimValuesWithID(userID, profileName));
        } catch (org.wso2.carbon.user.core.UserStoreException e) {
            log.error("Error while calling the service method.", e);
            throw new UserStoreException(e.getMessage(), e.getErrorCode(), e.getCause());
        }
    }

    public void deleteUserWithID(String userID) throws UserStoreException {

        try {
            getUserStoreManager().deleteUserWithID(userID);
        } catch (org.wso2.carbon.user.core.UserStoreException e) {
            log.error("Error while calling the service method.", e);
            throw new UserStoreException(e.getMessage(), e.getErrorCode(), e.getCause());
        }
    }

    public void setUserClaimValueWithID(String userID, String claimURI, String claimValue, String profileName)
            throws UserStoreException {

        try {
            getUserStoreManager().setUserClaimValueWithID(userID, claimURI, claimValue, profileName);
        } catch (org.wso2.carbon.user.core.UserStoreException e) {
            log.error("Error while calling the service method.", e);
            throw new UserStoreException(e.getMessage(), e.getErrorCode(), e.getCause());
        }
    }

    public void setUserClaimValuesWithID(String userID, ClaimValue[] claims, String profileName)
            throws UserStoreException {

        try {
            getUserStoreManager().setUserClaimValuesWithID(userID, convertClaimValueToMap(claims), profileName);
        } catch (org.wso2.carbon.user.core.UserStoreException e) {
            log.error("Error while calling the service method.", e);
            throw new UserStoreException(e.getMessage(), e.getErrorCode(), e.getCause());
        }
    }

    public UserDTO[] getUserListWithID(String claim, String claimValue, String profileName) throws UserStoreException {

        try {
            return getUserDTOListFromUser(getUserStoreManager().getUserListWithID(claim, claimValue, profileName));
        } catch (org.wso2.carbon.user.core.UserStoreException e) {
            log.error("Error while calling the service method.", e);
            throw new UserStoreException(e.getMessage(), e.getErrorCode(), e.getCause());
        }
    }

    public void updateCredentialWithID(String userID, String newCredential, String oldCredential)
            throws UserStoreException {

        try {
            getUserStoreManager().updateCredentialWithID(userID, newCredential, oldCredential);
        } catch (org.wso2.carbon.user.core.UserStoreException e) {
            log.error("Error while calling the service method.", e);
            throw new UserStoreException(e.getMessage(), e.getErrorCode(), e.getCause());
        }
    }

    public void updateCredentialByAdminWithID(String userID, String newCredential) throws UserStoreException {

        try {
            getUserStoreManager().updateCredentialByAdminWithID(userID, newCredential);
        } catch (org.wso2.carbon.user.core.UserStoreException e) {
            log.error("Error while calling the service method.", e);
            throw new UserStoreException(e.getMessage(), e.getErrorCode(), e.getCause());
        }
    }

    public void addRoleWithID(String roleName, String[] userIDList, PermissionDTO[] permissionsDTOs,
                              boolean isSharedRole)
            throws UserStoreException {

        org.wso2.carbon.user.core.Permission[] permissions =
                (org.wso2.carbon.user.core.Permission[]) convertDTOToPermission(permissionsDTOs);
        try {
            getUserStoreManager().addRoleWithID(roleName, userIDList, permissions, isSharedRole);
        } catch (org.wso2.carbon.user.core.UserStoreException e) {
            log.error("Error while calling the service method.", e);
            throw new UserStoreException(e.getMessage(), e.getErrorCode(), e.getCause());
        }
    }

    public void updateUserListOfRoleWithID(String roleName, String[] deletedUserIDs, String[] newUserIDs)
            throws UserStoreException {

        try {
            getUserStoreManager().updateUserListOfRoleWithID(roleName, deletedUserIDs, newUserIDs);
        } catch (org.wso2.carbon.user.core.UserStoreException e) {
            log.error("Error while calling the service method.", e);
            throw new UserStoreException(e.getMessage(), e.getErrorCode(), e.getCause());
        }
    }

    public void updateRoleListOfUserWithID(String userID, String[] deletedRoles, String[] newRoles)
            throws UserStoreException {

        try {
            getUserStoreManager().updateRoleListOfUserWithID(userID, deletedRoles, newRoles);
        } catch (org.wso2.carbon.user.core.UserStoreException e) {
            log.error("Error while calling the service method.", e);
            throw new UserStoreException(e.getMessage(), e.getErrorCode(), e.getCause());
        }
    }

    private UserDTO getUserDTO(User user) {

        UserDTO userDTO = new UserDTO();
        userDTO.setAttributes(getAttributesFromMap(user.getAttributes()));
        userDTO.setDisplayName(user.getDisplayName());
        userDTO.setPreferredUsername(user.getPreferredUsername());
        userDTO.setTenantDomain(user.getTenantDomain());
        userDTO.setUserID(user.getUserID());
        userDTO.setUsername(user.getUsername());
        userDTO.setUserStoreDomain(user.getUserStoreDomain());

        return userDTO;
    }

    private Attribute[] getAttributesFromMap(Map<String, String> attributes) {

        if (attributes == null) {
            return new Attribute[0];
        }

        Attribute[] attributesArray = new Attribute[attributes.size()];
        int i = 0;
        for (Map.Entry<String, String> entry : attributes.entrySet()) {
            Attribute attribute = new Attribute();
            attribute.setAttributeName(entry.getKey());
            attribute.setAttributeValue(entry.getValue());
            attributesArray[i] = attribute;
            i++;
        }
        return attributesArray;
    }

    private UserDTO[] getUserDTOListFromUser(List<User> userListWithID) {

        UserDTO[] userDTOS = new UserDTO[userListWithID.size()];
        int i = 0;
        for (User user : userListWithID) {
            userDTOS[i] = getUserDTO(user);
            i++;
        }
        return userDTOS;
    }

    private Condition getConditionFromConditionDTO(ConditionDTO conditionDTO) {

        return conditionDTO;
    }

    private AuthenticationResultDTO getAuthenticationResultDTOFromAuthenticationResult(
            AuthenticationResult authenticationResult) {

        AuthenticationResultDTO authenticationResultDTO = new AuthenticationResultDTO();
        authenticationResultDTO.setAuthenticatedSubjectIdentifier(authenticationResult
                .getAuthenticatedSubjectIdentifier());
        authenticationResultDTO.setAuthenticatedUser(getUserDTO(authenticationResult.getAuthenticatedUser()
                .orElse(new User())));
        authenticationResultDTO.setAuthenticationStatus(getAuthenticationStatusDTOFromAuthenticationStatus(
                authenticationResult.getAuthenticationStatus()));
        authenticationResultDTO.setFailureReason(getFailureReasonDTOFromFailureReason(authenticationResult
                .getFailureReason()
                .orElse(new FailureReason())));

        return authenticationResultDTO;
    }

    private LoginIdentifier[] getLoginIdentifierListFromLoginIdentifierList(LoginIdentifierDTO[] loginIdentifiers) {

        List<LoginIdentifier> loginIdentifiersList = new ArrayList<>();
        for (LoginIdentifierDTO loginIdentifierDTO : loginIdentifiers) {
            LoginIdentifier loginIdentifier = new LoginIdentifier(loginIdentifierDTO.getLoginKey(),
                    loginIdentifierDTO.getLoginValue(), loginIdentifierDTO.getProfileName(),
                    getLoginIdentifierFromDTO(loginIdentifierDTO.getLoginIdentifierType()));
            loginIdentifiersList.add(loginIdentifier);
        }
        return loginIdentifiersList.toArray(new LoginIdentifier[0]);
    }

    private LoginIdentifier.LoginIdentifierType getLoginIdentifierFromDTO(String loginIdentifierType) {

        switch (loginIdentifierType) {
            case "CLAIM_URI":
                return LoginIdentifier.LoginIdentifierType.CLAIM_URI;
            case "ATTRIBUTE":
                return LoginIdentifier.LoginIdentifierType.ATTRIBUTE;
            default:
                return null;
        }
    }

    private UserRoleListDTO[] getUserRoleListDTOs(Map<String, List<String>> roleListOfUsersWithID) {

        UserRoleListDTO[] userRoleListDTOs = new UserRoleListDTO[roleListOfUsersWithID.size()];
        int i = 0;
        for (Map.Entry<String, List<String>> entry : roleListOfUsersWithID.entrySet()) {
            UserRoleListDTO userRoleListDTO = new UserRoleListDTO();
            userRoleListDTO.setUserId(entry.getKey());
            userRoleListDTO.setRoleNames(entry.getValue().toArray(new String[0]));
            userRoleListDTOs[i] = userRoleListDTO;
            i++;
        }
        return userRoleListDTOs;
    }

    private String getAuthenticationStatusDTOFromAuthenticationStatus(
            AuthenticationResult.AuthenticationStatus authenticationStatus) {

        switch (authenticationStatus) {
            case SUCCESS:
                return "SUCCESS";
            case FAIL:
                return "FAIL";
            default:
                return null;
        }
    }

    private FailureReasonDTO getFailureReasonDTOFromFailureReason(FailureReason failureReason) {

        FailureReasonDTO failureReasonDTO = new FailureReasonDTO();
        failureReasonDTO.setFailureReason(failureReason.getFailureReason());
        failureReasonDTO.setId(failureReason.getId());

        return failureReasonDTO;
    }

    private AbstractUserStoreManager getUserStoreManager() throws UserStoreException {

        try {
            UserRealm realm = (UserRealm) CarbonContext.getThreadLocalCarbonContext().getUserRealm();
            if (realm == null) {
                throw new UserStoreException(NULL_REALM_MESSAGE);
            }
            return (AbstractUserStoreManager) realm.getUserStoreManager();
        } catch (Exception e) {
            throw new UserStoreException(e);
        }
    }

    private Permission[] convertDTOToPermission(PermissionDTO[] permissionDTOs) {

        List<Permission> permissions = new ArrayList<>();
        for (PermissionDTO permissionDTO : permissionDTOs) {
            Permission permission = new Permission(permissionDTO.getResourceId(),
                    permissionDTO.getAction());
            permissions.add(permission);
        }
        return permissions.toArray(new Permission[0]);
    }

    private ClaimValue[] convertMapToClaimValue(Map<String, String> map) {

        ClaimValue[] claims = new ClaimValue[map.size()];
        Iterator<Map.Entry<String, String>> ite = map.entrySet().iterator();
        int i = 0;
        while (ite.hasNext()) {
            Map.Entry<String, String> entry = ite.next();
            claims[i] = new ClaimValue(entry.getKey(), entry.getValue());
            i++;
        }
        return claims;
    }

    private Map<String, String> convertClaimValueToMap(ClaimValue[] values) {

        Map<String, String> map = new HashMap<>();
        for (ClaimValue claimValue : values) {
            map.put(claimValue.getClaimUri(), claimValue.getClaimValue());
        }
        return map;
    }

    private ClaimDTO[] convertClaimToClaimDTO(List<Claim> claims) {

        List<ClaimDTO> ClaimDTOs = new ArrayList<>();
        for (Claim claim : claims) {
            ClaimDTO claimDTO = new ClaimDTO();
            claimDTO.setClaimUri(claim.getClaimUri());
            claimDTO.setValue(claim.getValue());
            claimDTO.setDescription(claim.getDescription());
            claimDTO.setDialectURI(claim.getDialectURI());
            claimDTO.setDisplayOrder(claim.getDisplayOrder());
            claimDTO.setRegEx(claim.getRegEx());
            claimDTO.setSupportedByDefault(claim.isSupportedByDefault());
            claimDTO.setRequired(claim.isRequired());
            ClaimDTOs.add(claimDTO);
        }
        return ClaimDTOs.toArray(new ClaimDTO[0]);
    }

    private UniqueIDUserClaimSearchEntryDAO [] getClaimSearchEntryDAOFromClaimSearchEntry(
            UniqueIDUserClaimSearchEntry[] claimSearchEntries) {

        UniqueIDUserClaimSearchEntryDAO [] claimSearchEntryDAOS =
                new UniqueIDUserClaimSearchEntryDAO[claimSearchEntries.length];
        for(int i = 0; i < claimSearchEntries.length; i++) {
            claimSearchEntryDAOS[i] = getClaimSearchEntryDAOFromClaimSearchEntry(claimSearchEntries[i]);
        }

        return claimSearchEntryDAOS;
    }

    private UniqueIDUserClaimSearchEntryDAO getClaimSearchEntryDAOFromClaimSearchEntry(UniqueIDUserClaimSearchEntry
                                                                                               claimSearchEntry) {

        UniqueIDUserClaimSearchEntryDAO uniqueIDUserClaimSearchEntryDAO = new UniqueIDUserClaimSearchEntryDAO();

        ClaimValue[] claimValues = convertMapToClaimValue(claimSearchEntry.getClaims());

        uniqueIDUserClaimSearchEntryDAO.setClaims(claimValues);
        uniqueIDUserClaimSearchEntryDAO.setUser(getUserDTO(claimSearchEntry.getUser()));
        uniqueIDUserClaimSearchEntryDAO.setUserClaimSearchEntry(getClaimEntryDAOFromClaimEntry(claimSearchEntry
                .getUserClaimSearchEntry()));
        return uniqueIDUserClaimSearchEntryDAO;
    }

    private UserClaimSearchEntryDAO getClaimEntryDAOFromClaimEntry(UserClaimSearchEntry userClaimSearchEntry) {

        UserClaimSearchEntryDAO userClaimSearchEntryDAO = new UserClaimSearchEntryDAO();
        userClaimSearchEntry.setUserName(userClaimSearchEntry.getUserName());

        ClaimValue[] claimValues = convertMapToClaimValue(userClaimSearchEntry.getClaims());

        userClaimSearchEntryDAO.setClaims(claimValues);
        return userClaimSearchEntryDAO;
    }
}

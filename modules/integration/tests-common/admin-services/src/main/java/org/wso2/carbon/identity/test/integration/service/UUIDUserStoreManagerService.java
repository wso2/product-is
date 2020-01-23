package org.wso2.carbon.identity.test.integration.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.identity.test.integration.service.dao.Attribute;
import org.wso2.carbon.identity.test.integration.service.dao.AuthenticationResultDTO;
import org.wso2.carbon.identity.test.integration.service.dao.ConditionDTO;
import org.wso2.carbon.identity.test.integration.service.dao.FailureReasonDTO;
import org.wso2.carbon.identity.test.integration.service.dao.LoginIdentifierDTO;
import org.wso2.carbon.identity.test.integration.service.dao.PermissionDTO;
import org.wso2.carbon.identity.test.integration.service.dao.UserDTO;
import org.wso2.carbon.identity.test.integration.service.dao.UserRoleListDTO;
import org.wso2.carbon.user.api.Permission;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.claim.Claim;
import org.wso2.carbon.user.core.common.AbstractUserStoreManager;
import org.wso2.carbon.user.core.common.AuthenticationResult;
import org.wso2.carbon.user.core.common.FailureReason;
import org.wso2.carbon.user.core.common.LoginIdentifier;
import org.wso2.carbon.user.core.common.User;
import org.wso2.carbon.user.core.model.Condition;
import org.wso2.carbon.user.core.model.UniqueIDUserClaimSearchEntry;
import org.wso2.carbon.user.mgt.common.ClaimValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.jws.WebService;

@WebService
public class UUIDUserStoreManagerService {

    private static final Log log = LogFactory.getLog(UUIDUserStoreManagerService.class);
    private static final String NULL_REALM_MESSAGE = "UserRealm is null";

    public UserDTO addUserWithID(String userName, Object credential, String[] roleList, ClaimValue[] claims,
                                 String profileName) throws UserStoreException {

        return getUserDTO(getUserStoreManager().addUserWithID(userName, credential, roleList,
                convertClaimValueToMap(claims), profileName));
    }

    public AuthenticationResultDTO authenticateWithIDLoginIdentifier(LoginIdentifierDTO[] loginIdentifiers,
                                                                     String domain, Object credential)
            throws UserStoreException {

        List<LoginIdentifier> identifierList = getLoginIdentifierListFromLoginIdentifierList(loginIdentifiers);
        return getAuthenticationResultDTOFromAuthenticationResult(getUserStoreManager()
                .authenticateWithID(identifierList, domain, credential));
    }

    public AuthenticationResultDTO authenticateWithIDUserId(String userID, String domain, Object credential)
            throws UserStoreException {

        return getAuthenticationResultDTOFromAuthenticationResult(getUserStoreManager()
                .authenticateWithID(userID, domain, credential));
    }

    public AuthenticationResultDTO authenticateWithIDUsernameClaim(String preferredUserNameClaim,
                                                                   String preferredUserNameValue, Object credential,
                                                                   String profileName)
            throws UserStoreException {

        return getAuthenticationResultDTOFromAuthenticationResult(getUserStoreManager()
                .authenticateWithID(preferredUserNameClaim, preferredUserNameValue, credential, profileName));
    }

    public void deleteUserClaimValuesWithID(String userID, String[] claims, String profileName)
            throws UserStoreException {

        getUserStoreManager().deleteUserClaimValuesWithID(userID, claims, profileName);
    }

    public Date getPasswordExpirationTimeWithID(String userId) throws UserStoreException {

        return getUserStoreManager().getPasswordExpirationTimeWithID(userId);
    }

    public boolean isUserInRoleWithID(String userID, String roleName) throws UserStoreException {

        return getUserStoreManager().isUserInRoleWithID(userID, roleName);
    }

    public List<User> listUsersWithID(String filter, int limit, int offset) throws UserStoreException {

        return getUserStoreManager().listUsersWithID(filter, limit, offset);
    }

    public List<User> getUserListWithID(String claim, String claimValue, String profileName, int limit, int offset)
            throws UserStoreException {

        return getUserStoreManager().getUserListWithID(claim, claimValue, profileName, limit, offset);
    }

    public List<UserDTO> getUserListWithID(ConditionDTO conditionDTO, String domain, String profileName, int limit,
                                           int offset, String sortBy, String sortOrder)
            throws UserStoreException {

        org.wso2.carbon.user.core.model.Condition condition = getConditionFromConditionDTO(conditionDTO);
        return getUserDTOListFromUser(getUserStoreManager().getUserListWithID(condition, domain, profileName, limit,
                offset, sortBy, sortOrder));
    }


    public List<UniqueIDUserClaimSearchEntry> getUsersClaimValuesWithID(List<String> userIDs, List<String> claims,
                                                                        String profileName)
            throws UserStoreException {

        return getUserStoreManager().getUsersClaimValuesWithID(userIDs, claims, profileName);
    }

    public UserRoleListDTO[] getRoleListOfUsersWithID(String [] userIDs) throws UserStoreException {

        return getUserRoleListDTOs(getUserStoreManager().getRoleListOfUsersWithID(Arrays.asList(userIDs)));
    }

    public void deleteUserClaimValueWithID(String userID, String claimURI, String profileName)
            throws UserStoreException {

        getUserStoreManager().deleteUserClaimValueWithID(userID, claimURI, profileName);
    }

    public List<User> listUsersWithID(String filter, int maxItemLimit) throws UserStoreException {

        return getUserStoreManager().listUsersWithID(filter, maxItemLimit);
    }

    public UserDTO getUserWithID(String userID, String[] requestedClaims, String profileName)
            throws UserStoreException {

        return getUserDTO(getUserStoreManager().getUserWithID(userID, requestedClaims, profileName));
    }

    public boolean isExistingUserWithID(String userID) throws UserStoreException {

        return getUserStoreManager().isExistingUserWithID(userID);
    }

    public String[] getProfileNamesWithID(String userID) throws UserStoreException {

        return getUserStoreManager().getProfileNamesWithID(userID);
    }

    public List<String> getRoleListOfUserWithID(String userID) throws UserStoreException {

        return getUserStoreManager().getRoleListOfUserWithID(userID);
    }

    public List<User> getUserListOfRoleWithID(String roleName) throws UserStoreException {

        return getUserStoreManager().getUserListOfRoleWithID(roleName);
    }

    public List<User> getUserListOfRoleWithID(String roleName, String filter, int maxItemLimit)
            throws UserStoreException {

        return getUserStoreManager().getUserListOfRoleWithID(roleName, filter, maxItemLimit);
    }

    public String getUserClaimValueWithID(String userID, String claim, String profileName) throws UserStoreException {

        return getUserStoreManager().getUserClaimValueWithID(userID, claim, profileName);
    }

    public ClaimValue[] getUserClaimValuesWithID(String userID, String[] claims, String profileName)
            throws UserStoreException {

        return convertMapToClaimValue(getUserStoreManager().getUserClaimValuesWithID(userID, claims, profileName));
    }

    public List<Claim> getUserClaimValuesWithID(String userID, String profileName) throws UserStoreException {

        return getUserStoreManager().getUserClaimValuesWithID(userID, profileName);
    }

    public void deleteUserWithID(String userID) throws UserStoreException {

        getUserStoreManager().deleteUserWithID(userID);
    }

    public void setUserClaimValueWithID(String userID, String claimURI, String claimValue, String profileName)
            throws UserStoreException {

        getUserStoreManager().setUserClaimValueWithID(userID, claimURI, claimValue, profileName);
    }

    public void setUserClaimValuesWithID(String userID, ClaimValue [] claims, String profileName)
            throws UserStoreException {

        getUserStoreManager().setUserClaimValuesWithID(userID, convertClaimValueToMap(claims), profileName);
    }

    public List<User> getUserListWithID(String claim, String claimValue, String profileName) throws UserStoreException {

        return getUserStoreManager().getUserListWithID(claim, claimValue, profileName);
    }

    public void updateCredentialWithID(String userID, Object newCredential, Object oldCredential)
            throws UserStoreException {

        getUserStoreManager().updateCredentialWithID(userID, newCredential, oldCredential);
    }

    public void updateCredentialByAdminWithID(String userID, Object newCredential) throws UserStoreException {

        getUserStoreManager().updateCredentialByAdminWithID(userID, newCredential);
    }

    public void addRoleWithID(String roleName, String[] userIDList, PermissionDTO[] permissionsDTOs,
                              boolean isSharedRole)
            throws UserStoreException {

        org.wso2.carbon.user.core.Permission [] permissions =
                (org.wso2.carbon.user.core.Permission[]) convertDTOToPermission(permissionsDTOs);
        getUserStoreManager().addRoleWithID(roleName, userIDList, permissions, isSharedRole);
    }

    public void updateUserListOfRoleWithID(String roleName, String[] deletedUserIDs, String[] newUserIDs)
            throws UserStoreException {

        getUserStoreManager().updateUserListOfRoleWithID(roleName, deletedUserIDs, newUserIDs);
    }

    public void updateRoleListOfUserWithID(String userID, String[] deletedRoles, String[] newRoles)
            throws UserStoreException {

        getUserStoreManager().updateRoleListOfUserWithID(userID, deletedRoles, newRoles);
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

        Attribute [] attributesArray = new Attribute[attributes.size()];
        int i = 0;
        for(Map.Entry<String, String> entry : attributes.entrySet()) {
            Attribute attribute = new Attribute();
            attribute.setAttributeName(entry.getKey());
            attribute.setAttributeValue(entry.getValue());
            attributesArray[i] = attribute;
            i++;
        }
        return attributesArray;
    }

    private List<UserDTO> getUserDTOListFromUser(List<User> userListWithID) {

        return userListWithID
                .stream()
                .map(this::getUserDTO)
                .collect(Collectors.toList());
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
                .getFailureReason().orElse(new FailureReason())));

        return authenticationResultDTO;
    }

    private List<LoginIdentifier> getLoginIdentifierListFromLoginIdentifierList(LoginIdentifierDTO[] loginIdentifiers) {

        List<LoginIdentifier> loginIdentifiersList = new ArrayList<>();
        for (LoginIdentifierDTO loginIdentifierDTO : loginIdentifiers) {
            LoginIdentifier loginIdentifier = new LoginIdentifier(loginIdentifierDTO.getLoginKey(),
                    loginIdentifierDTO.getLoginValue(), loginIdentifierDTO.getProfileName(),
                    getLoginIdentifierFromDTO(loginIdentifierDTO.getLoginIdentifierType()));
            loginIdentifiersList.add(loginIdentifier);
        }
        return loginIdentifiersList;
    }

    private LoginIdentifier.LoginIdentifierType getLoginIdentifierFromDTO(
            LoginIdentifierDTO.LoginIdentifierType loginIdentifierType) {

        switch (loginIdentifierType) {
            case CLAIM_URI:
                return LoginIdentifier.LoginIdentifierType.CLAIM_URI;
            case ATTRIBUTE:
                return LoginIdentifier.LoginIdentifierType.ATTRIBUTE;
            default:
                return null;
        }
    }

    private UserRoleListDTO[] getUserRoleListDTOs(Map<String, List<String>> roleListOfUsersWithID) {

        UserRoleListDTO [] userRoleListDTOs = new UserRoleListDTO[roleListOfUsersWithID.size()];
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

    private AuthenticationResultDTO.AuthenticationStatus getAuthenticationStatusDTOFromAuthenticationStatus(
            AuthenticationResult.AuthenticationStatus authenticationStatus) {

        switch (authenticationStatus) {
            case SUCCESS:
                return AuthenticationResultDTO.AuthenticationStatus.SUCCESS;
            case FAIL:
                return AuthenticationResultDTO.AuthenticationStatus.FAIL;
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
            claims[i] = new ClaimValue();
            claims[i].setClaimURI(entry.getKey());
            claims[i].setValue(entry.getValue());
            i++;
        }
        return claims;
    }

    private Map<String, String> convertClaimValueToMap(ClaimValue[] values) {

        Map<String, String> map = new HashMap<>();
        for (ClaimValue claimValue : values) {
            map.put(claimValue.getClaimURI(), claimValue.getValue());
        }
        return map;
    }
}

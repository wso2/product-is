package org.wso2.sample.user.store.manager;

import org.wso2.carbon.user.api.*;

import java.util.Date;
import java.util.Map;


/**
 * Sample User Store Manager Class
 */
public class CustomUserStoreManager implements org.wso2.carbon.user.api.UserStoreManager {

    public CustomUserStoreManager() {

    }

    @Override
    public boolean authenticate(String s, Object o) throws UserStoreException {
        return false;
    }

    @Override
    public String[] listUsers(String s, int i) throws UserStoreException {
        return new String[0];
    }

    @Override
    public boolean isExistingUser(String s) throws UserStoreException {
        return false;
    }

    @Override
    public boolean isExistingRole(String s, boolean b) throws UserStoreException {
        return false;
    }

    @Override
    public boolean isExistingRole(String s) throws UserStoreException {
        return false;
    }

    @Override
    public String[] getRoleNames() throws UserStoreException {
        return new String[0];
    }

    @Override
    public String[] getProfileNames(String s) throws UserStoreException {
        return new String[0];
    }

    @Override
    public String[] getRoleListOfUser(String s) throws UserStoreException {
        return new String[0];
    }

    @Override
    public String[] getUserListOfRole(String s) throws UserStoreException {
        return new String[0];
    }

    @Override
    public String getUserClaimValue(String s, String s2, String s3) throws UserStoreException {
        return null;
    }

    @Override
    public Map<String, String> getUserClaimValues(String s, String[] strings, String s2) throws UserStoreException {
        return null;
    }

    @Override
    public Claim[] getUserClaimValues(String s, String s2) throws UserStoreException {
        return new Claim[0];
    }

    @Override
    public String[] getAllProfileNames() throws UserStoreException {
        return new String[0];
    }

    @Override
    public boolean isReadOnly() throws UserStoreException {
        return false;
    }

    @Override
    public void addUser(String s, Object o, String[] strings, Map<String, String> stringStringMap, String s2) throws UserStoreException {
    }

    @Override
    public void addUser(String s, Object o, String[] strings, Map<String, String> stringStringMap, String s2, boolean b) throws UserStoreException {
    }

    @Override
    public void updateCredential(String s, Object o, Object o2) throws UserStoreException {
    }

    @Override
    public void updateCredentialByAdmin(String s, Object o) throws UserStoreException {
    }

    @Override
    public void deleteUser(String s) throws UserStoreException {
    }

    @Override
    public void addRole(String s, String[] strings, Permission[] permissions, boolean b) throws UserStoreException {

    }

    @Override
    public void addRole(String s, String[] strings, Permission[] permissions) throws UserStoreException {
    }

    @Override
    public void deleteRole(String s) throws UserStoreException {
    }

    @Override
    public void updateUserListOfRole(String s, String[] strings, String[] strings2) throws UserStoreException {
    }

    @Override
    public void updateRoleListOfUser(String s, String[] strings, String[] strings2) throws UserStoreException {
    }

    @Override
    public void setUserClaimValue(String s, String s2, String s3, String s4) throws UserStoreException {
    }

    @Override
    public void setUserClaimValues(String s, Map<String, String> stringStringMap, String s2) throws UserStoreException {
    }

    @Override
    public void deleteUserClaimValue(String s, String s2, String s3) throws UserStoreException {
    }

    @Override
    public void deleteUserClaimValues(String s, String[] strings, String s2) throws UserStoreException {
    }

    @Override
    public String[] getHybridRoles() throws UserStoreException {
        return new String[0];
    }

    @Override
    public Date getPasswordExpirationTime(String s) throws UserStoreException {
        return null;
    }

    @Override
    public int getUserId(String s) throws UserStoreException {
        return 0;
    }

    @Override
    public int getTenantId(String s) throws UserStoreException {
        return 0;
    }

    @Override
    public int getTenantId() throws UserStoreException {
        return 0;
    }

    @Override
    public Map<String, String> getProperties(Tenant tenant) throws UserStoreException {
        return null;
    }

    @Override
    public void updateRoleName(String s, String s2) throws UserStoreException {

    }

    @Override
    public boolean isMultipleProfilesAllowed() {
        return false;
    }

    @Override
    public void addRememberMe(String s, String s2) throws UserStoreException {

    }

    @Override
    public boolean isValidRememberMeToken(String s, String s2) throws UserStoreException {
        return false;
    }

    @Override
    public ClaimManager getClaimManager() throws UserStoreException {
        return null;
    }

    @Override
    public boolean isSCIMEnabled() throws UserStoreException {
        return false;
    }

    public Properties getDefaultUserStoreProperties() {
            Properties properties = new Properties();
            properties.setMandatoryProperties(CustomUserStoreManagerConstants.CUSTOM_USERSTORE_PROPERTIES.toArray
                    (new Property[CustomUserStoreManagerConstants.CUSTOM_USERSTORE_PROPERTIES.size()]));
            properties.setOptionalProperties(CustomUserStoreManagerConstants.OPTIONAL_CUSTOM_USERSTORE_PROPERTIES.toArray
                    (new Property[CustomUserStoreManagerConstants.OPTIONAL_CUSTOM_USERSTORE_PROPERTIES.size()]));
            return properties;
    }


}

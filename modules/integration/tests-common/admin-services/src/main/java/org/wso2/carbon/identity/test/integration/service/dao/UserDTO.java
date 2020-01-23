package org.wso2.carbon.identity.test.integration.service.dao;

import org.wso2.carbon.user.core.util.UserCoreUtil;

public class UserDTO {

    private String userID;
    private String username;
    private String preferredUsername;
    private String displayName;
    private String tenantDomain;
    private String userStoreDomain;
    private Attribute [] attributes;

    public UserDTO() {

        super();
    }

    public UserDTO(String userID) {

        this.userID = userID;
    }

    public UserDTO(String userID, String username, String preferredUsername) {

        this.userID = userID;
        this.username = username;
        this.preferredUsername = preferredUsername;
    }

    public UserDTO(String userID, String username, String preferredUsername, String displayName, String tenantDomain,
                   String userStoreDomain, Attribute [] attributes) {

        this.userID = userID;
        this.username = username;
        this.preferredUsername = preferredUsername;
        this.displayName = displayName;
        this.tenantDomain = tenantDomain;
        this.userStoreDomain = userStoreDomain;
        this.attributes = attributes;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDomainQualifiedUsername() {

        return UserCoreUtil.addDomainToName(username, userStoreDomain);
    }

    public String getFullQualifiedUsername() {

        String domainQualifiedUsername = getDomainQualifiedUsername();
        return UserCoreUtil.addTenantDomainToEntry(domainQualifiedUsername, tenantDomain);
    }

    public String getPreferredUsername() {

        return preferredUsername;
    }

    public void setPreferredUsername(String preferredUsername) {

        this.preferredUsername = preferredUsername;
    }

    public void setDisplayName(String displayName) {

        this.displayName = displayName;
    }

    public String getDisplayName() {

        return displayName;
    }

    public String getTenantDomain() {

        return tenantDomain;
    }

    public void setTenantDomain(String tenantDomain) {

        this.tenantDomain = tenantDomain;
    }

    public String getUserStoreDomain() {

        return userStoreDomain;
    }

    public void setUserStoreDomain(String userStoreDomain) {

        this.userStoreDomain = userStoreDomain;
    }

    public Attribute[] getAttributes() {

        return attributes;
    }

    public void setAttributes(Attribute [] attributes) {

        this.attributes = attributes;
    }

    @Override
    public boolean equals(Object obj) {

        if (obj instanceof UserDTO) {
            return this.getFullQualifiedUsername().equals(((UserDTO) obj).getFullQualifiedUsername());
        }

        return false;
    }

    @Override
    public int hashCode() {

        return this.getFullQualifiedUsername().hashCode();
    }
}

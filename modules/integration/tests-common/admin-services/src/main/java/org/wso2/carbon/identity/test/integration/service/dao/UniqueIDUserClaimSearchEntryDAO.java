package org.wso2.carbon.identity.test.integration.service.dao;

public class UniqueIDUserClaimSearchEntryDAO {

    private UserDTO user;
    private ClaimValue [] claims;
    private UserClaimSearchEntryDAO userClaimSearchEntry;

    public UniqueIDUserClaimSearchEntryDAO() {
    }

    public UserDTO getUser() {
        return this.user;
    }

    public void setUser(UserDTO user) {
        this.user = user;
    }

    public ClaimValue[] getClaims() {
        return this.claims;
    }

    public void setClaims(ClaimValue [] claims) {
        this.claims = claims;
    }

    public UserClaimSearchEntryDAO getUserClaimSearchEntry() {
        return this.userClaimSearchEntry;
    }

    public void setUserClaimSearchEntry(UserClaimSearchEntryDAO userClaimSearchEntry) {
        this.userClaimSearchEntry = userClaimSearchEntry;
    }
}

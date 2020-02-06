package org.wso2.carbon.identity.test.integration.service.dao;

public class UserClaimSearchEntryDAO {

    private String userName;
    private ClaimValue [] claims;

    public UserClaimSearchEntryDAO() {
    }

    public String getUserName() {
        return this.userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public ClaimValue [] getClaims() {
        return this.claims;
    }

    public void setClaims(ClaimValue [] claims) {
        this.claims = claims;
    }

}

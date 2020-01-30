package org.wso2.carbon.identity.test.integration.service.dao;

import java.io.Serializable;

public class AuthenticationResultDTO implements Serializable {

    private String authenticationStatus;
    private UserDTO authenticatedUser;
    private String authenticatedSubjectIdentifier;
    private FailureReasonDTO failureReason;

    public AuthenticationResultDTO() {
        super();
    }

    public AuthenticationResultDTO(String authenticationStatus) {

        this.authenticationStatus = authenticationStatus;
    }

    public UserDTO getAuthenticatedUser() {

        return authenticatedUser;
    }

    public void setAuthenticatedUser(UserDTO authenticatedUser) {

        this.authenticatedUser = authenticatedUser;
    }

    public String getAuthenticatedSubjectIdentifier() {

        return authenticatedSubjectIdentifier;
    }

    public void setAuthenticatedSubjectIdentifier(String authenticatedSubjectIdentifier) {

        this.authenticatedSubjectIdentifier = authenticatedSubjectIdentifier;
    }

    public String getAuthenticationStatus() {

        return authenticationStatus;
    }

    public void setAuthenticationStatus(String authenticationStatus) {

        this.authenticationStatus = authenticationStatus;
    }

    public FailureReasonDTO getFailureReason() {

        return failureReason;
    }

    public void setFailureReason(FailureReasonDTO failureReason) {

        this.failureReason = failureReason;
    }
}

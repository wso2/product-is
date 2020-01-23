package org.wso2.carbon.identity.test.integration.service.dao;

import java.io.Serializable;
import java.util.Optional;

public class AuthenticationResultDTO implements Serializable {

    private AuthenticationResultDTO.AuthenticationStatus authenticationStatus;
    private UserDTO authenticatedUser;
    private String authenticatedSubjectIdentifier;
    private FailureReasonDTO failureReason;

    public AuthenticationResultDTO() {
        super();
    }

    public AuthenticationResultDTO(AuthenticationResultDTO.AuthenticationStatus authenticationStatus) {

        this.authenticationStatus = authenticationStatus;
    }

    public Optional<UserDTO> getAuthenticatedUser() {

        return Optional.of(authenticatedUser);
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

    public AuthenticationResultDTO.AuthenticationStatus getAuthenticationStatus() {

        return authenticationStatus;
    }

    public void setAuthenticationStatus(AuthenticationResultDTO.AuthenticationStatus authenticationStatus) {

        this.authenticationStatus = authenticationStatus;
    }

    public Optional<FailureReasonDTO> getFailureReason() {

        return Optional.of(failureReason);
    }

    public void setFailureReason(FailureReasonDTO failureReason) {

        this.failureReason = failureReason;
    }

    public enum AuthenticationStatus {
        SUCCESS, FAIL
    }
}

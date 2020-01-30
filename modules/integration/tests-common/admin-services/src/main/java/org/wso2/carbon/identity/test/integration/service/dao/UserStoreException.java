package org.wso2.carbon.identity.test.integration.service.dao;

public class UserStoreException extends Exception {

    private String errorCode;

    public UserStoreException() {
    }

    public UserStoreException(String message, Throwable cause) {
        super(message, cause);
    }

    public UserStoreException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public UserStoreException(String message, boolean convertMessage) {
        super(message);
    }

    public UserStoreException(String message) {
        super(message);
    }

    public UserStoreException(Throwable cause) {
        super(cause);
    }

    public String getErrorCode() {
        return this.errorCode;
    }
}

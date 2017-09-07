package org.wso2.carbon.is.migration;

import org.wso2.carbon.identity.base.IdentityException;

public class MigrationClientException extends IdentityException{
    public MigrationClientException(String message) {
        super(message);
    }

    public MigrationClientException(String errorCode, String message) {
        super(errorCode, message);
    }

    public MigrationClientException(String message, Throwable cause) {
        super(message, cause);
    }

    public MigrationClientException(String errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}

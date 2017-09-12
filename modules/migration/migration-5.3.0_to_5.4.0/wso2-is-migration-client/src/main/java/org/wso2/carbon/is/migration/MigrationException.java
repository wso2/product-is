package org.wso2.carbon.is.migration;

import org.wso2.carbon.identity.base.IdentityException;

public class MigrationException extends IdentityException{

    public MigrationException(String message) {
        super(message);
    }

    public MigrationException(String errorCode, String message) {
        super(errorCode, message);
    }

    public MigrationException(String message, Throwable cause) {
        super(message, cause);
    }

    public MigrationException(String errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}

/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.is.portal.user.client.api.exception;

/**
 * User portal ui exception.
 */
public class UserPortalUIException extends Exception {

    private int errorCode = 0;

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public UserPortalUIException() {
        super();
    }

    public UserPortalUIException(String message) {
        super(message);
    }

    public UserPortalUIException(String message, int errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public UserPortalUIException(String message, Throwable cause) {
        super(message, cause);
    }
}


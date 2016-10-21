/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.user.profile.service;

/**
 * Custom exception for tjhe user profile management errors.
 *
 */
public class UserProfileManagementException extends Exception {

    public UserProfileManagementException() {
    }

    public UserProfileManagementException(String message) {
        super(message);
    }

    public UserProfileManagementException(String message, Throwable cause) {
        super(message, cause);
    }

    public UserProfileManagementException(Throwable cause) {
        super(cause);
    }

    public UserProfileManagementException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}

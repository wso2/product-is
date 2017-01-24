/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

/**
 * User related reusable utility methods are defined here
 */

var userManager = {};

(function (userManager) {

    /**
     * Check whether a unique user can be found with given username
     * @param username username
     * @param domainSeparator app specific domain separator
     * @returns {success: true, userdomain: "*", username: "*", uniqueUserId: "*"} or
     *          {success: false, message: "*"}
     */
    function isUserExists(username, domainSeparator) {
        try {
            var uufUsers = callOSGiService("org.wso2.is.portal.user.client.api.IdentityStoreClientService",
                "listUsers", ["http://wso2.org/claims/username", extractUsername(username, domainSeparator) , 0, 2,
                    extractDomain(username, domainSeparator)]);

            Log.debug("Check whether a unique user is found with username: " + username);

            if (uufUsers.length == 1) {
                Log.debug("A unique user is found with username: " + extractUsername(username, domainSeparator)
                    + "in domain:" + extractDomain(username, domainSeparator));
                return {success: true, userdomain: uufUsers[0].getDomainName(), username: extractUsername(username,
                    domainSeparator), uniqueUserId: uufUsers[0].getUserId()};
            } else if (uufUsers.length > 1) {
                Log.debug("Multiple users are found with username: " + username);
                return {success: false, message: "user-portal.user.not.found.for.username"}
            } else {
                Log.debug("No user found in the system with username: " + username);
                return {success: false, message: "user-portal.user.not.found.for.username"}
            }

        } catch (e) {
            var message = e.message;
            var cause = e.getCause();
            if (cause != null) {
                //the exceptions thrown by the actual osgi service method is wrapped inside a InvocationTargetException.
                if (cause instanceof java.lang.reflect.InvocationTargetException) {
                    message = cause.getTargetException().message;
                }
            }
            LOG.error(message);
            return {success: false, message: "user-portal.user.something.wrong.error."};
        }
    }

    /**
     * Extract domain from username if username is domain aware
     * @param username
     * @param domainSeparator
     * @returns "***" or null if domain is not available in username
     */
    function extractDomain(username, domainSeparator) {
        if (username.indexOf(domainSeparator) > -1) {
            return username.substr(0, username.indexOf(domainSeparator));
        } else {
            return null;
        }
    }

    /**
     * Extract username from if provided username is domain aware
     * @param username
     * @param domainSeparator
     * @returns "***" or username
     */
    function extractUsername(username, domainSeparator) {
        if (username.indexOf(domainSeparator) > -1) {
            return username.substr(username.indexOf(domainSeparator) + 1, username.length);
        } else {
            return username;
        }
    }

    /**
     * Public function to check whether a unique user exists
     * @param username
     * @param domainSeparator
     * @returns result of private isUserExists function
     */
    userManager.isUserExists = function (username, domainSeparator) {
        Log.debug("Check whether a unique user is found with username: " + username);
        return isUserExists(username, domainSeparator);
    }

})(userManager);
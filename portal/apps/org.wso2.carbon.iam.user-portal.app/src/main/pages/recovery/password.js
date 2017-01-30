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
module("recovery-manager");
module("user-manager");

function onRequest(env) {
    var domainSeparator = env.config['domainSeparator'];
    if (env.request.method == "POST") {
        var username = env.request.formParams['username'];

        //check whether user exists
        var result = userManager.isUserExists(username, domainSeparator);
        if (result.success) {
            Log.debug("An unique user found in the system with username: " + username);
            //configure recovery-options redirect uri
            sendRedirect(env.contextPath + '/recovery/password-options?username=' + result.username
                + "&domain=" + result.userdomain + "&userId=" + result.uniqueUserId);
        } else {
            return {errorMessage: result.message, username: username, isPasswordRecoveryEnabled: true};
        }
    }

    if (env.request.method == "GET") {
        //check whether password recovery options are enabled
        var result = recoveryManager.isPasswordRecoveryEnabled();
        if (result.success) {
            return {isPasswordRecoveryEnabled: result.isEnabled}
        } else {
            sendError(505, "user-portal.user.something.wrong.error");
        }
    }
}

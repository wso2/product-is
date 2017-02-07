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

function updatePassword(username, oldPassword, newPassword, domain) {
    try {
        var oldPasswordChar = Java.to(oldPassword.split(''), 'char[]');
        var newPasswordChar = Java.to(newPassword.split(''), 'char[]');
        callOSGiService("org.wso2.is.portal.user.client.api.IdentityStoreClientService",
            "updatePassword", [username, oldPasswordChar, newPasswordChar, domain]);

        return {success: true, message: "You have successfully updated the password"};
    } catch (e) {
        var message = e.message;
        return {success: false, message: message};
    }
}

function onPost(env) {
    var session = getSession();
    var username = session.getUser().getUsername();
    var domain = session.getUser().getDomainName();
    if ("reset-password" == env.params.actionId) {
        var oldPassword = env.request.formParams['oldPassword'];
        var newPassword = env.request.formParams['newPassword'];
        var result = updatePassword(username, oldPassword, newPassword, domain);
        if (result.success) {
            return {success: true, message: result.message};
        } else {
            return {success: false, message: result.message};
        }
    }
}

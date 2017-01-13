/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

function onRequest(env) {

    var session = getSession();
    var username = session.getUser().getUsername();

    if (env.request.method == "POST" && "reset-password" == env.params.actionId) {
        var oldPassword = env.request.formParams['oldPassword'];
        var newPassword = env.request.formParams['newPassword'];
        var result = updatePassword(username, oldPassword, newPassword);
        if (result.success) {
            return {success: true, message: result.message}
        } else {
            return {success: false, message: result.message};
        }
    }
}

function updatePassword(username, oldPassword, newPassword) {
    try {
        var oldPasswordChar = Java.to(oldPassword.split(''), 'char[]');
        var newPasswordChar = Java.to(newPassword.split(''), 'char[]');
        callOSGiService("org.wso2.is.portal.user.client.api.IdentityStoreClientService",
            "updatePassword", [username, oldPasswordChar, newPasswordChar]);

        return {success: true, message: "You have successfully updated the password"}
    } catch (e) {
        var message = e.message;
        var cause = e.getCause();
        if (cause != null) {
            //the exceptions thrown by the actual osgi service method is wrapped inside a InvocationTargetException.
            if (cause instanceof java.lang.reflect.InvocationTargetException) {
                message = cause.getTargetException().message;
            }
        }

        return {success: false, message: message};
    }
}

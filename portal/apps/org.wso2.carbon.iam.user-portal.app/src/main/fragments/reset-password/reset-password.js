/*
<<<<<<< HEAD
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
=======
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

<<<<<<< HEAD
function onRequest(env) {

    var session = getSession();
    var username = session.getUser().getUsername();
    var domain = session.getUser().getDomainName();

    if (env.request.method == "POST" && "reset-password" == env.params.actionId) {
        var oldPassword = env.request.formParams['oldPassword'];
        var newPassword = env.request.formParams['newPassword'];
        var result = updatePassword(username, oldPassword, newPassword, domain);
        if (result.success) {
            return {success: true, message: result.message}
        } else {
            return {success: false, message: result.message};
        }
    }
}

<<<<<<< HEAD
function updatePassword(username, oldPassword, newPassword) {
>>>>>>> 0413463... update user portal update password feature
=======
=======
>>>>>>> 8aeb445... Fixing jshint issues
function updatePassword(username, oldPassword, newPassword, domain) {
>>>>>>> 759b7c7... add user store domain support in login and update password
    try {
        var oldPasswordChar = Java.to(oldPassword.split(''), 'char[]');
        var newPasswordChar = Java.to(newPassword.split(''), 'char[]');
        callOSGiService("org.wso2.is.portal.user.client.api.IdentityStoreClientService",
<<<<<<< HEAD
<<<<<<< HEAD
            "updatePassword", [username, oldPasswordChar, newPasswordChar, domain]);

        return {success: true, message: "You have successfully updated the password"};
    } catch (e) {
        var message = e.message;
        var cause = e.getCause();
        if (cause !== null) {
=======
            "updatePassword", [username, oldPasswordChar, newPasswordChar]);
=======
            "updatePassword", [username, oldPasswordChar, newPasswordChar, domain]);
>>>>>>> 759b7c7... add user store domain support in login and update password

        return {success: true, message: "You have successfully updated the password"};
    } catch (e) {
        var message = e.message;
        var cause = e.getCause();
<<<<<<< HEAD
        if (cause != null) {
>>>>>>> 0413463... update user portal update password feature
=======
        if (cause !== null) {
>>>>>>> 8aeb445... Fixing jshint issues
            //the exceptions thrown by the actual osgi service method is wrapped inside a InvocationTargetException.
            if (cause instanceof java.lang.reflect.InvocationTargetException) {
                message = cause.getTargetException().message;
            }
        }

        return {success: false, message: message};
    }
}
<<<<<<< HEAD
<<<<<<< HEAD
=======
>>>>>>> 8aeb445... Fixing jshint issues

function onRequest(env) {

    var session = getSession();
    var username = session.getUser().getUsername();
    var domain = session.getUser().getDomainName();

    if (env.request.method == "POST" && "reset-password" == env.params.actionId) {
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
<<<<<<< HEAD
=======
>>>>>>> 0413463... update user portal update password feature
=======
>>>>>>> 8aeb445... Fixing jshint issues

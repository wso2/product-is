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
    if (session) {
        sendRedirect(env.contextPath + env.config['loginRedirectUri']);
    }

    if (env.request.method == "POST") {
        var username = env.request.formParams['username'];
        var password = env.request.formParams['password'];
        var result = authenticate(username, password);
        if (result.success) {
            //configure login redirect uri
            sendRedirect(env.contextPath + env.config['loginRedirectUri']);
        } else {
            return {errorMessage: result.message};
        }
    }
}

function authenticate(username, password) {
    try {
        var passwordChar = Java.to(password.split(''), 'char[]');
        var authenticationContext = callOSGiService("org.wso2.is.portal.user.client.api.RealmClientService",
            "authenticate", [username, passwordChar]);

        createSession(authenticationContext.user);
        return {success: true, message: "success"}
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
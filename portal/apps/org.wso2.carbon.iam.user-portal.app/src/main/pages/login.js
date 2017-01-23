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

function getDomainNames(env) {
    var domainNames;
    if (env.config.isDomainInLogin) {
        try {
            domainNames = callOSGiService("org.wso2.is.portal.user.client.api.IdentityStoreClientService",
                "getDomainNames", []);
        } catch (e) {
            return {errorMessage: 'signup.error.retrieve.domain'};
        }
    }
    return {
        "domainNames": domainNames
    };
}

function authenticate(username, password, domain) {
    try {
        var passwordChar = Java.to(password.split(''), 'char[]');
        var uufUser = callOSGiService("org.wso2.is.portal.user.client.api.IdentityStoreClientService",
            "authenticate", [username, passwordChar, domain]);

        createSession(uufUser);
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

        return {success: false, message: 'login.error.authentication'};
    }
}

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

>>>>>>> 7fa08ef... adding the login page to user portal
function onRequest(env) {
    var session = getSession();
    if (session) {
        sendRedirect(env.contextPath + env.config['loginRedirectUri']);
    }

<<<<<<< HEAD
    if (env.request.method == "GET") {
        return getDomainNames(env);
    }

    if (env.request.method == "POST") {
        var domain = env.request.formParams['domain'];
        var username = env.request.formParams['username'];
        var password = env.request.formParams['password'];
<<<<<<< HEAD

        if (!env.config.isDomainInLogin) {
            if (username.indexOf("/") != -1) {
                var splitedValue = username.split("/");
                if (splitedValue.length == 2) {
                    domain = splitedValue[0];
                    username = splitedValue[1];
                } else {
                    return {errorMessage: 'login.error.invalid.username'};
                }
            }
        }
        var result = authenticate(username, password, domain);
=======
    if (env.request.method == "POST") {
        var username = env.request.formParams['username'];
        var password = env.request.formParams['password'];
<<<<<<< HEAD
        var result = authenticate(username, password);
>>>>>>> 7fa08ef... adding the login page to user portal
=======
        var result = authenticate(username, password, null);
>>>>>>> 4f4c7d8... Added domain in UI for self sign-up
=======
        var domain = null;
        var usernameWithoutDomain = username;
        if(username.indexOf("/") != -1) {
            var splitedValue = username.split("/");
            if(splitedValue.length == 2) {
                domain = splitedValue[0];
                usernameWithoutDomain = splitedValue[1];
            } else {
                return {errorMessage: "You have provided an invalid username."};
            }
        }
        var result = authenticate(usernameWithoutDomain, password, domain);
>>>>>>> 759b7c7... add user store domain support in login and update password
        if (result.success) {
            //configure login redirect uri
            sendRedirect(env.contextPath + env.config['loginRedirectUri']);
        } else {
            return {errorMessage: result.message};
        }
    }
}

<<<<<<< HEAD
<<<<<<< HEAD
=======
function authenticate(username, password) {
=======
function authenticate(username, password, domain) {
>>>>>>> 4f4c7d8... Added domain in UI for self sign-up
    try {
        var passwordChar = Java.to(password.split(''), 'char[]');
        var uufUser = callOSGiService("org.wso2.is.portal.user.client.api.IdentityStoreClientService",
            "authenticate", [username, passwordChar, domain]);

        createSession(uufUser);
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
>>>>>>> 7fa08ef... adding the login page to user portal

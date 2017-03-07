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
module("recovery-manager");

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
    return domainNames;
}

function getRecoveryConfigInfo(){
    var recoveryConfig = recoveryManager.getRecoveryConfigs();
    return {
        passwordRecoveryUrl: recoveryConfig.getPassword().getUrl() || env.contextPath + '/recovery/password',
        usernameRecoveryUrl: recoveryConfig.getUsername().getUrl() || env.contextPath + '/recovery/username'
    };
}

function getPrimaryDomainName(env) {
    var primaryDomainName;
    if (env.config.isDomainInLogin) {
        try {
            primaryDomainName = callOSGiService("org.wso2.is.portal.user.client.api.IdentityStoreClientService",
                "getPrimaryDomainName", []);
        } catch (e) {
            return {errorMessage: 'signup.error.retrieve.domain'};
        }
    }
    return primaryDomainName;
}


function authenticate(username, password, domain) {
    try {
        if (!(username && password)) {
            return {success: false, message: 'login.error.empty.authentication'};
        }
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

function onGet(env) {
    var session = getSession();
    if (session) {
        sendRedirect(env.contextPath + env.config['loginRedirectUri']);
    }
    var domainNames = getDomainNames(env);
    var primaryDomainName = getPrimaryDomainName(env);
    var recoverynfo = getRecoveryConfigInfo();
    return { domainNames:domainNames, primaryDomainName:primaryDomainName, recoveryInfo: recoverynfo };
}


function onPost(env) {
    var session = getSession();
    if (session) {
        sendRedirect(env.contextPath + env.config['loginRedirectUri']);
    }

    var domain = env.request.formParams['domain'];
    var username = env.request.formParams['username'];
    var password = env.request.formParams['password'];

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
    if (result.success) {
        //configure login redirect uri
        sendRedirect(env.contextPath + env.config['loginRedirectUri']);
    } else {
        var domainNames = getDomainNames(env);
        var primaryDomainName = getPrimaryDomainName(env);
        var recoverynfo = getRecoveryConfigInfo();
        return { errorMessage: result.message, domainNames: domainNames, primaryDomainName:primaryDomainName,
            recoveryInfo: recoverynfo };
    }
}


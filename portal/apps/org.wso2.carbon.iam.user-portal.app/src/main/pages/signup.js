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
        var formParams = {};
        var claimMap = {};
        var credentialMap = {};
        var domain;
        formParams = env.request.formParams;
        for (var i in formParams) {
            if (i == "password") {
                credentialMap["password"] = formParams[i];
            } else if (i == "domainValue") {
                domain = formParams[i];
            }
            else {
                claimMap[i] = formParams[i];
            }
        }

        var registrationResult = userRegistration(claimMap, credentialMap, domain);
        if (registrationResult.errorMessage) {
            return {errorMessage: registrationResult.message};
        }
        else if (registrationResult.userRegistration && registrationResult.userRegistration.userId) {
            var authenticationResult = authenticate(claimMap["http://wso2.org/claims/username"], credentialMap["password"], domain);
            if (authenticationResult.success) {
                sendRedirect(env.contextPath + env.config['loginRedirectUri']);
            } else {
                sendRedirect(env.contextPath + env.config['loginPageUri']);
            }
        }
    }

    if (env.request.method == "GET") {
        return getProfile();
    }
}

function getProfile() {

    /**
     * Get the Claim Profile
     */

    var claimProfile;
    try {
        claimProfile = callOSGiService("org.wso2.is.portal.user.client.api.ProfileMgtClientService",
            "getProfile", ["self-signUp"]);
    } catch (e) {
        return {errorMessage: 'user-portal.user.signup.error.retrieve.claim'};
    }
    var claimForProfile = claimProfile.claims;

    var claimProfileArray = [];
    var userName = [];

    for (var i = 0; i < claimForProfile.length; i++) {
        if (claimForProfile[i].claimURI == "http://wso2.org/claims/username") {
            var usernameClaim = {};
            usernameClaim["displayName"] = claimForProfile[i].getDisplayName();
            usernameClaim["claimURI"] = claimForProfile[i].getClaimURI();
            if (claimForProfile[i].getDefaultValue()) {
                usernameClaim["defaultValue"] = claimForProfile[i].getDefaultValue();
            }
            usernameClaim["claimLabel"] = claimForProfile[i].getClaimURI().replace("http://wso2.org/claims/", "");
            usernameClaim["required"] = claimForProfile[i].getRequired();
            usernameClaim["regex"] = claimForProfile[i].getRegex();
            usernameClaim["readonly"] = claimForProfile[i].getReadonly();
            usernameClaim["dataType"] = claimForProfile[i].getDataType();
            userName[i] = usernameClaim;
        } else {
            var claimProfileMap = {};
            claimProfileMap["displayName"] = claimForProfile[i].getDisplayName();
            claimProfileMap["claimURI"] = claimForProfile[i].getClaimURI();
            if (claimForProfile[i].getDefaultValue()) {
                claimProfileMap["defaultValue"] = claimForProfile[i].getDefaultValue();
            }
            claimProfileMap["claimLabel"] = claimForProfile[i].getClaimURI().replace("http://wso2.org/claims/", "");
            claimProfileMap["required"] = claimForProfile[i].getRequired();
            claimProfileMap["regex"] = claimForProfile[i].getRegex();
            claimProfileMap["readonly"] = claimForProfile[i].getReadonly();
            claimProfileMap["dataType"] = claimForProfile[i].getDataType();
            claimProfileArray[i] = claimProfileMap;
        }
    }


    /**
     * Get the set of domain names
     */

    var domainNames;
    try {
        domainNames = callOSGiService("org.wso2.is.portal.user.client.api.IdentityStoreClientService",
            "getDomainNames", []);
    } catch (e) {
        return {errorMessage: 'user-portal.user.signup.error.retrieve.domain'};
    }

    sendToClient("signupClaims", claimProfileArray);
    return {
        "usernameClaim": userName,
        "signupClaims": claimProfileArray,
        "domainNames": domainNames
    };
}

function userRegistration(claimMap, credentialMap, domain) {
    try {
        var userRegistration = callOSGiService("org.wso2.is.portal.user.client.api.IdentityStoreClientService",
            "addUser", [claimMap, credentialMap, domain]);
        return {userRegistration: userRegistration};
    } catch (e) {
        var message = e.message;
        var cause = e.getCause();
        if (cause != null) {
            //the exceptions thrown by the actual osgi service method is wrapped inside a InvocationTargetException.
            if (cause instanceof java.lang.reflect.InvocationTargetException) {
                message = cause.getTargetException().message;
            }
        }
        return {errorMessage: 'user-portal.user.signup.error.registration'};
    }
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

        return {success: false, message: 'user-portal.user.login.error.authentication'};
    }
}
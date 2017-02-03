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

function getProfile() {

    /**
     * Get the Claim Profile
     */

    var claimProfile;
    try {
        claimProfile = callOSGiService("org.wso2.is.portal.user.client.api.ProfileMgtClientService",
            "getProfile", ["self-signUp"]);
    } catch (e) {
        return {errorMessage: 'signup.error.retrieve.claim'};
    }
    var claimForProfileEntry = claimProfile.claims;

    var claimProfileArray = [];
    var userName = [];

    for (var i = 0; i < claimForProfileEntry.length; i++) {
        if (claimForProfileEntry[i].claimURI == "http://wso2.org/claims/username") {
            userName[i] = generateClaimProfileMap(claimForProfileEntry[i]);
        } else {
            claimProfileArray[i] = generateClaimProfileMap(claimForProfileEntry[i]);
        }
    }


    /**
     * Get the set of domain names
     */

    var domainNames;
    var domainNamesList;
    try {
        domainNamesList = callOSGiService("org.wso2.is.portal.user.client.api.IdentityStoreClientService",
            "getDomainNames", []);
        if (domainNamesList.size() != 1) {
            domainNames = domainNamesList;
        }

    } catch (e) {
        return {errorMessage: 'signup.error.retrieve.domain'};
    }

    var primaryDomainName;
    try {
        primaryDomainName = callOSGiService("org.wso2.is.portal.user.client.api.IdentityStoreClientService",
            "getPrimaryDomainName", []);
    } catch (e) {
        return {errorMessage: 'signup.error.retrieve.domain'};
    }

    sendToClient("signupClaims", claimProfileArray);
    return {
        "usernameClaim": userName,
        "signupClaims": claimProfileArray,
        "domainNames": domainNames,
        "primaryDomainName": primaryDomainName
    };
}

function generateClaimProfileMap(claimProfileEntry) {
    var claimProfileMap = {};
    claimProfileMap["displayName"] = claimProfileEntry.getDisplayName();
    claimProfileMap["claimURI"] = claimProfileEntry.getClaimURI();
    if (claimProfileEntry.getDefaultValue()) {
        claimProfileMap["defaultValue"] = claimProfileEntry.getDefaultValue();
    }
    claimProfileMap["claimLabel"] = claimProfileEntry.getClaimURI().replace("http://wso2.org/claims/", "");
    claimProfileMap["required"] = claimProfileEntry.getRequired();
    claimProfileMap["regex"] = claimProfileEntry.getRegex();
    claimProfileMap["readonly"] = claimProfileEntry.getReadonly();
    claimProfileMap["dataType"] = claimProfileEntry.getDataType();
    return claimProfileMap;
}

function userRegistration(claimMap, credentialMap, domain) {
    try {
        var userRegistrationResult = callOSGiService("org.wso2.is.portal.user.client.api.IdentityStoreClientService",
            "addUser", [claimMap, credentialMap, domain]);
        return {userRegistration: userRegistrationResult};
    } catch (e) {
        var message = e.message;
        var cause = e.getCause();
        if (cause !== null) {
            //the exceptions thrown by the actual osgi service method is wrapped inside a InvocationTargetException.
            if (cause instanceof java.lang.reflect.InvocationTargetException) {
                message = cause.getTargetException().message;
            }
        }
        return {errorMessage: 'signup.error.registration'};
    }
}

function authenticate(username, password, domain) {
    try {
        var passwordChar = Java.to(password.split(''), 'char[]');
        var uufUser = callOSGiService("org.wso2.is.portal.user.client.api.IdentityStoreClientService",
            "authenticate", [username, passwordChar, domain]);

        createSession(uufUser);
        return {success: true, message: "success"};
    } catch (e) {
        var message = e.message;
        var cause = e.getCause();
        if (cause !== null) {
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
    return getProfile();
}


function onPost(env) {
    var session = getSession();
    if (session) {
        sendRedirect(env.contextPath + env.config['loginRedirectUri']);
    }
    var formParams = {};
    var claimMap = {};
    var credentialMap = {};
    var domain = null;
    formParams = env.request.formParams;
    for (var i in formParams) {
        if (i == "password") {
            credentialMap["password"] = formParams[i];
        } else if (i == "domain") {
            domain = formParams[i];
        }
        else {
            claimMap[i] = formParams[i];
        }
    }

    var registrationResult = userRegistration(claimMap, credentialMap, domain);
    if (registrationResult.errorMessage) {
        return {errorMessage: registrationResult.errorMessage};
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


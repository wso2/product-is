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

    if (env.request.method == "POST") {
        var formParams = {};
        var claimMap = {};
        var credentialMap = {};
        formParams = env.request.formParams;
        for (var i in formParams) {
            if (i == "password") {
                credentialMap["password"] = formParams[i];
            } else {
                claimMap[i] = formParams[i];
            }
        }

        var registrationResult = userRegistration(claimMap, credentialMap);
        if (registrationResult.errorMessage) {
            return {errorMessage: registrationResult.message};
        }
        else if (registrationResult.userRegistration && registrationResult.userRegistration.userId) {
            var authenticationResult = authenticate(claimMap["http://wso2.org/claims/username"], credentialMap["password"]);
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
    try {
        // Get Claim Profile
        var claimProfile = callOSGiService("org.wso2.is.portal.user.client.api.ProfileMgtClientService",
            "getProfile", ["self-signUp"]);
        if (claimProfile == null) {
            return {errorMessage: "Failed to retrieve the claim profile."};
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
                usernameClaim["displayLabel"] = claimForProfile[i].getClaimURI().replace("http://wso2.org/claims/", "");
                usernameClaim["required"] = Boolean.toString(claimForProfile[i].getRequired());
                usernameClaim["regex"] = claimForProfile[i].getRegex();
                usernameClaim["readonly"] = Boolean.toString(claimForProfile[i].getReadonly());
                usernameClaim["dataType"] = claimForProfile[i].getDataType();
                userName[i] = usernameClaim;
            } else {
                var claimProfileMap = {};
                claimProfileMap["displayName"] = claimForProfile[i].getDisplayName();
                claimProfileMap["claimURI"] = claimForProfile[i].getClaimURI();
                if (claimForProfile[i].getDefaultValue()) {
                    claimProfileMap["defaultValue"] = claimForProfile[i].getDefaultValue();
                }
                claimProfileMap["displayLabel"] = claimForProfile[i].getClaimURI().replace("http://wso2.org/claims/", "");
                claimProfileMap["required"] = Boolean.toString(claimForProfile[i].getRequired());
                claimProfileMap["regex"] = claimForProfile[i].getRegex();
                claimProfileMap["readonly"] = Boolean.toString(claimForProfile[i].getReadonly());
                claimProfileMap["dataType"] = claimForProfile[i].getDataType();
                claimProfileArray[i] = claimProfileMap;
            }
        }
        sendToClient("signupClaims", claimProfileArray);

        // Get Challenge Questions
        var challengeQuestions;
        /*callOSGiService("org.wso2.is.portal.user.client.api.ChallengeQuestionManagerService",
            "getChallengeQuestionList", null);*/
        return {
            "usernameClaim": userName,
            "signupClaims": claimProfileArray,
            "challengeQuestions": challengeQuestions
        };
    }
    catch
        (e) {
        return {errorMessage: e.instMessage};
    }
}

function userRegistration(claimMap, credentialMap) {
    try {
        var userRegistration = callOSGiService("org.wso2.is.portal.user.client.api.IdentityStoreClientService",
            "addUser", [claimMap, credentialMap]);
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
        return {errorMessage: message};
    }
}

function authenticate(username, password) {
    try {
        var passwordChar = Java.to(password.split(''), 'char[]');
        var uufUser = callOSGiService("org.wso2.is.portal.user.client.api.IdentityStoreClientService",
            "authenticate", [username, passwordChar]);

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
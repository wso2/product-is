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

function onGet(env) {
    var session = getSession();
    var domainNames = getDomainNames(env);
    var primaryDomainName = getPrimaryDomainName(env);
    return {domainNames: domainNames, primaryDomainName: primaryDomainName};
}


function onPost(env) {
    var confirmationCode = env.request.queryParams['confirmation'];
    var password = env.request.formParams['input-password'];

    if (confirmationCode && password) {
        recoveryManager.updatePassword(confirmationCode, password);
    }
}

function getGroupProfile() {
    /**
     * Get the 'group' Profile
     */
    var claimProfile;
    try {
        claimProfile = callOSGiService("org.wso2.is.portal.user.client.api.ProfileMgtClientService",
            "getProfile", ["group"]);
    } catch (e) {
        return {errorMessage: profile + '.error.retrieve.claim'};
    }
    var claimForProfileEntry = claimProfile.claims;
    var claimProfileArray = [];

    for (var i = 0; i < claimForProfileEntry.length; i++) {
        claimProfileArray[i] = generateClaimProfileMap(claimForProfileEntry[i]);
    }

    return {
        "groupClaims": claimProfileArray
    };
}

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

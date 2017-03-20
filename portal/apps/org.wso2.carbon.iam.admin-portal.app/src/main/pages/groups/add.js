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

// function onGet(env) {
//     var session = getSession();
//     var domainNames = getDomainNames(env);
//     var primaryDomainName = getPrimaryDomainName(env);
//     return {domainNames: domainNames, primaryDomainName: primaryDomainName};
// }
//
//
// function onPost(env) {
//     var groupName = env.request.formParams['input-groupName'];
//     var groupDescription = env.request.formParams['input-groupDescription'];
//     domain = env.request.formParams['domain'];
//
//     callOSGiService("org.wso2.is.portal.user.client.api.IdentityStoreClientService",
//         "addGroup", [domain, groupName, groupDescription]);
//
//     return {success: true, message: "Group Added Successfully"};
// }

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

function onGet(env) {
    var domainNames = getDomainNames(env);
    var primaryDomainName = getPrimaryDomainName(env);

    return {domainNames: domainNames, primaryDomainName: primaryDomainName};
}

function onPost(env) {
    var claimMap = {};
    var domain = null;
    try {
        claimMap["http://wso2.org/claims/groupname"] = env.request.formParams['input-groupname'];
        claimMap["http://wso2.org/claims/groupdescription"] = env.request.formParams['input-description'];
        domain = env.request.formParams['domain'];
        var addGroupResult = addGroup(claimMap, domain);
        var domainNames = getDomainNames(env);
        var primaryDomainName = getPrimaryDomainName(env);

        return {addGroupResult: addGroupResult, domainNames: domainNames, primaryDomainName: primaryDomainName};
    } catch (e) {
        return {errorMessage: 'group.add.error'};
    }
    
    
}

function addGroup(claimMap, domain) {
    try {
        var userRegistrationResult = callOSGiService("org.wso2.is.portal.user.client.api.IdentityStoreClientService",
            "addGroup", [claimMap, domain]);
        return {userRegistration: userRegistrationResult};
    } catch (e) {
        return {errorMessage: 'group.add.error'};
    }
}

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
    var usernameClaim = getUsernameClaimFromProfile();
//    var userList = getUsersForList(0, 100, "", usernameClaim);
    var userList = getFilteredList(0, 100, "", "", "");
    var claimProfile = getUserListProfile();
    var domains = getDomainNames(env);
    var primaryDomainName = getPrimaryDomainName(env);
    buildJSONArray(userList);
    return {claimProfile: claimProfile, domains: domains, primaryDomainName: primaryDomainName};
}

function onPost(env) {
    var session, action, claimUri, claimValue, domainName;
    session = getSession();
    action = env.request.formParams["action"];
    Log.info("--------------------------");
    Log.info(action);
    if (action === "filter-list") {
        claimUri = env.request.formParams["claim-uri"];
        claimValue = env.request.formParams["claim-filter"];
        domainName = env.request.formParams["domain-name"];
        var userList = getFilteredList(0, 100, claimUri, claimValue, domainName);
        var claimProfile = getUserListProfile();
        var domains = getDomainNames(env);
        var primaryDomainName = getPrimaryDomainName(env);
        buildJSONArray(userList);
        return {claimProfile: claimProfile, domains: domains, primaryDomainName: primaryDomainName};
    }
}

function getUsersForList(offset, length, domainName, usernameClaim) {
    var userList;
    try {
        userList = callOSGiService("org.wso2.is.portal.user.client.api.UserMgtClientService",
            "getUsersForList", [offset, length, domainName, usernameClaim]);
    } catch (e) {
        return {errorMessage: 'signup.error.retrieve.domain'};
    }
    return userList;
}

function getFilteredList(offset, length, claimURI, claimValue, domainName){
    var userList;
    try {
        userList = callOSGiService("org.wso2.is.portal.user.client.api.UserMgtClientService",
            "getFilteredList", [offset, length, claimURI, claimValue, domainName]);
    } catch (e) {
        return {errorMessage: 'signup.error.retrieve.domain'};
    }
    return userList;
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

function getUsernameClaimFromProfile() {
    /**
     * Get the unique claim profile
     */
    var claimProfile;
    try {
        claimProfile = callOSGiService("org.wso2.is.portal.user.client.api.ProfileMgtClientService",
            "getProfile", ["user-list"]);
    } catch (e) {
        return {errorMessage: profile + '.error.retrieve.claim'};
    }
    var claimForProfileEntry = claimProfile.claims;
    var usernameClaim;

    if (claimForProfileEntry.length > 0) {
        for (var i = 0; i < claimForProfileEntry.length; i++) {
            if (claimForProfileEntry[i].getProperties().get("claimId") == "Username") {
                usernameClaim = claimForProfileEntry[i].getClaimURI();
            }
        }
        if (!usernameClaim) {
            usernameClaim = claimForProfileEntry[0].getClaimURI();
        }
    } else {
        usernameClaim = "http://wso2.org/claims/username";
    }

    return usernameClaim;
}

function getUserListProfile () {
    var claimProfile;
    try {
        claimProfile = callOSGiService("org.wso2.is.portal.user.client.api.ProfileMgtClientService",
            "getProfile", ["user-list"]);
    } catch (e) {
        return {errorMessage: profile + '.error.retrieve.claim'};
    }
    var claimForProfileEntry = claimProfile.claims;

    return claimForProfileEntry;
}

function buildJSONArray(userList) {
    var userArray = [];
    var claimArray = [];
    for (var i in userList) {
        var item = userList[i];
        userArray.push({
            "Username" : item.getUserId(),
            "Status" : item.getState(),
            "Groups" : "",
            "Roles" : "",
            "UniqueId" : item.getUserUniqueId(),
            "Domain" : item.getDomainName(),
        });
    }
    sendToClient("users", userArray);
}
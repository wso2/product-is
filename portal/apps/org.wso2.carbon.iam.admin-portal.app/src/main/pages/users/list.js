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
    var userList = getFilteredList(0, -1, "", "", "");
    var claimProfile = getUserListProfile();
    var domains = getDomainNames(env);
    var primaryDomainName = getPrimaryDomainName(env);
    return {claimProfile: claimProfile, domains: domains};
}

function onPost(env) {
    var session, action, claimUri, claimValue, domainName, recordLimit;
    session = getSession();
    action = env.request.formParams['action'];
    if (action === "filter-list") {
        claimUri = env.request.formParams["claim-uri"];
        claimValue = env.request.formParams["claim-filter"];
        domainName = env.request.formParams["domain-name"];
        recordLimit = env.request.formParams["record-limit"];
        sendToClient("selectedClaim", claimUri);
        sendToClient("selectedDomain", domainName);
        if(!recordLimit) {
            recordLimit = -1
        }
        var userList = getFilteredList(0, recordLimit, claimUri, claimValue, domainName);
        var claimProfile = getUserListProfile();
        var domains = getDomainNames(env);
        var primaryDomainName = getPrimaryDomainName(env);
        return {claimProfile: claimProfile, domains: domains, claimValue: claimValue};
    }
}

function getFilteredList(offset, length, claimURI, claimValue, domainName){
    var userList;
    try {
        userList = callOSGiService("org.wso2.is.portal.user.client.api.UserMgtClientService",
            "getFilteredList", [offset, length, claimURI, claimValue, domainName]);
    } catch (e) {
        return {errorMessage: 'signup.error.retrieve.domain'};
    }
    buildJSONArray(userList);
    return userList;
}

function getDomainNames(env) {
    var domainNames;
    try {
        domainNames = callOSGiService("org.wso2.is.portal.user.client.api.IdentityStoreClientService",
            "getDomainNames", []);
    } catch (e) {
        return {errorMessage: 'signup.error.retrieve.domain'};
    }
    return domainNames;
}

function getPrimaryDomainName(env) {
    var primaryDomainName;
    try {
        primaryDomainName = callOSGiService("org.wso2.is.portal.user.client.api.IdentityStoreClientService",
            "getPrimaryDomainName", []);
    } catch (e) {
        return {errorMessage: 'signup.error.retrieve.domain'};
    }
    return primaryDomainName;
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

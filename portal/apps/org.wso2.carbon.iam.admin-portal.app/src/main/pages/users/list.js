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
    var claimProfile = getUserListProfile();
    var domains = getDomainNames(env);
    var primaryDomainName = getPrimaryDomainName(env);
    getUserList(0, -1, "");
    return {claimProfile: claimProfile, domains: domains};
}

function onPost(env) {
    var session, action, claimUri, claimValue, domainName, recordLimit, offset;
    session = getSession();
    action = env.request.formParams['action'];
    claimUri = env.request.formParams["claim-uri"];
    claimValue = env.request.formParams["claim-filter"];
    domainName = env.request.formParams["domain-name"];
    recordLimit = parseInt(env.request.formParams["length-value"]);
    offset = parseInt(env.request.formParams["offset-value"]);
    sendToClient("selectedClaim", claimUri);
    sendToClient("selectedDomain", domainName);
    sendToClient("action", action);
    sendToClient("offset", offset);
    sendToClient("recordLimit", recordLimit);
    if(!recordLimit) {
        recordLimit = -1
    }
    var userList = getFilteredList(offset, recordLimit, claimUri, claimValue, domainName);
    var claimProfile = getUserListProfile();
    var domains = getDomainNames(env);
    return {claimProfile: claimProfile, domains: domains, claimValue: claimValue};
}

function getFilteredList(offset, length, claimURI, claimValue, domainName){
    var userList;
    try {
        userList = callOSGiService("org.wso2.is.portal.user.client.api.IdentityStoreClientService",
            "getFilteredList", [offset, length, claimURI, claimValue, domainName]);
    } catch (e) {
        return {errorMessage: 'list.error.retrieve.users'};
    }
    buildJSONArray(userList);
    return userList;
}

function getUserList(offset, length, domainName){
    var userList;
    try {
        userList = callOSGiService("org.wso2.is.portal.user.client.api.IdentityStoreClientService",
            "getUserList", [offset, length, domainName]);
    } catch (e) {
        return {errorMessage: 'list.error.retrieve.users'};
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
            "getProfile", ["user-list-filter"]);
    } catch (e) {
        return {errorMessage: profile + '.error.retrieve.claim'};
    }
    var claimForProfileEntry = claimProfile.claims;

    return claimForProfileEntry;
}

function buildJSONArray(userList) {
    var userArray = [];
    var groups = [];
    for (var i in userList) {
        var item = userList[i];
        groups = item.getGroups();
        userArray.push({
            "Username" : item.getUserId(),
            "Status" : item.getState(),
            "Groups" : groups,
            "Roles" : "",
            "UniqueId" : item.getUserUniqueId(),
            "Domain" : item.getDomainName(),
        });
    }
    sendToClient("users", userArray);
}

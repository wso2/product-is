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
    var claimProfile = getClaimsInProfile("user-list-filter");
    var requestedClaims = getClaimsInProfile("user-list-columns");
    var domains = getDomainNames(env);
    var userList = getUserList(0, -1, "", requestedClaims);
    var columns = buildJSONArray(userList);
    sendToClient("columnList", columns);
    return {claimProfile: claimProfile, domains: domains, columns: columns};
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
    var claimProfile = getClaimsInProfile("user-list-filter");
    var requestedClaims = getClaimsInProfile("user-list-columns");
    if(!recordLimit) {
        recordLimit = -1
    }
    var userList = getFilteredList(offset, recordLimit, claimUri, claimValue, domainName, requestedClaims);
    var columns = buildJSONArray(userList);
    sendToClient("columnList", columns);

    var domains = getDomainNames(env);
    return {claimProfile: claimProfile, domains: domains, claimValue: claimValue, columns: columns};
}

function getFilteredList(offset, length, claimURI, claimValue, domainName, requestedClaims){
    var userList;
    try {
        userList = callOSGiService("org.wso2.is.portal.user.client.api.IdentityStoreClientService",
            "getFilteredList", [offset, length, claimURI, claimValue, domainName, requestedClaims]);
    } catch (e) {
        return {errorMessage: 'list.error.retrieve.users'};
    }

    return userList;
}

function getUserList(offset, length, domainName, requestedClaims){
    var userList;
    try {
        userList = callOSGiService("org.wso2.is.portal.user.client.api.IdentityStoreClientService",
            "getUserList", [offset, length, domainName, requestedClaims]);
    } catch (e) {
        return {errorMessage: 'list.error.retrieve.users'};
    }

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

function getClaimsInProfile (profileName) {
    var claimProfile;
    try {
        claimProfile = callOSGiService("org.wso2.is.portal.user.client.api.ProfileMgtClientService",
            "getProfile", [profileName]);
    } catch (e) {
        return {errorMessage: profile + '.error.retrieve.claim'};
    }
    var claimForProfileEntry = claimProfile.claims;

    return claimForProfileEntry;
}

function buildJSONArray(userList) {
    var userArray = [];
    var groups = [];
    var roles = [];
    var columnsArray = {};
    var claimsMap = {};
    var claims = {};

    claimsMap = userList[0].getClaims();
    var index = 0;
    for (var key in claimsMap) {
        columnsArray[index] = key;
        index++;
    }

    for (var i in userList) {
        var item = userList[i];
        groups = item.getGroups();
        claims = item.getClaims();
        //roles = item.getRoles();
        for (var key in claims) {
            if (key === "Groups") {
                claims[key] = groups;
            } else if (key === "Roles") {
                //claims[key] = roles;
            }
        }
        userArray.push(claims);
    }
    sendToClient("users", userArray);
    return columnsArray;
}
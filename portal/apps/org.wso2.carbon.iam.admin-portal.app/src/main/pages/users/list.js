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

function getFilteredList(offset, length, claimURI, claimValue, domainName, requestedClaims) {
     var userList;
     try {
         userList = callOSGiService("org.wso2.is.portal.user.client.api.IdentityStoreClientService",
             "listUsersWithFilter", [offset, length, claimURI, claimValue, domainName, requestedClaims]);
     } catch (e) {
         return {errorMessage: 'list.error.retrieve.users'};
     }

     return userList;
}

function getUserList(offset, length, domainName, requestedClaims) {
     var userList;
     try {
         userList = callOSGiService("org.wso2.is.portal.user.client.api.IdentityStoreClientService",
             "listUsers", [offset, length, domainName, requestedClaims]);
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
         sendError(500, "list.error.retrieve.domain");
     }
     return domainNames;
}

function getPrimaryDomainName(env) {
     var primaryDomainName;
     try {
         primaryDomainName = callOSGiService("org.wso2.is.portal.user.client.api.IdentityStoreClientService",
             "getPrimaryDomainName", []);
     } catch (e) {
         sendError(500, "list.error.retrieve.domain");
     }
     return primaryDomainName;
}

function getClaimsInProfile (profileName) {
     var claimProfile;
     try {
         claimProfile = callOSGiService("org.wso2.is.portal.user.client.api.ProfileMgtClientService",
             "getProfile", [profileName]);
     } catch (e) {
         sendError(500, "list.error.retrieve.profile");
     }
     var claimForProfileEntry = claimProfile.claims;

     return claimForProfileEntry;
}

function buildJSONArray(userList) {
    var userArray = [];
    var groups = [];
    var roles = [];
    var columnsArray = [];
    var claimsMap = {};
    var claims = {};

    if (userList.length > 0) {
        claimsMap = userList[0].getClaims();
        for (var key in claimsMap) {
            columnsArray.push(key);
        }

        for (var i in userList) {
            var item = userList[i];
            groups = item.getGroups();
            claims = item.getClaims();
            roles = item.getRoles();
            for (var colName in claims) {
                if (colName === "Groups") {
                    claims[colName] = groups;
                } else if (colName === "Roles") {
                    claims[colName] = roles;
                }
            }
            userArray.push(claims);
        }
    }

    sendToClient("users", userArray);
    return columnsArray;
}

function onGet(env) {
    var session = getSession();

    var claimProfile = getClaimsInProfile("user-list-filter");
    var requestedClaims = getClaimsInProfile("user-list-columns");
    var domains = getDomainNames(env);

    sendToClient("selectedClaim", null);
    sendToClient("selectedDomain", null);
    sendToClient("action", null);
    sendToClient("offset", null);
    sendToClient("recordLimit", null);

    var userList = getUserList(0, -1, "", requestedClaims);
    if (userList.errorMessage) {
        sendToClient("columnList", null);
        return {claimProfile: claimProfile, domains: domains, errorMessage: userList.errorMessage};
    } else {
        var columns = buildJSONArray(userList);
        sendToClient("columnList", columns);
        var sortRowListCount = Object.keys(columns).length - 2;
        var sortRowList = [];
        for (var i=0; i<sortRowListCount; i++) {
            var noSort = '';
            if (columns[i] === "Groups" || columns[i] === "Roles") {
                noSort = 'no-sort';
            }
            sortRowList.push({
                "name" : columns[i],
                "noSort" : noSort
            });
        }

        return {claimProfile: claimProfile, domains: domains, sortRowList: sortRowList};
    }

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
        recordLimit = -1;
    }

    var claimProfile = getClaimsInProfile("user-list-filter");
    var requestedClaims = getClaimsInProfile("user-list-columns");

    var columns = null;

    var userList = getFilteredList(offset, recordLimit, claimUri, claimValue, domainName, requestedClaims);
    if (userList.errorMessage) {
        sendToClient("columnList", columns);
        return {claimProfile: claimProfile, domains: domains,
                claimValue: claimValue, errorMessage: userList.errorMessage};
    } else if (userList.length == 0) {
        sendToClient("columnList", columns);
        return {claimProfile: claimProfile, domains: domains,
                claimValue: claimValue, noRecordsMessage: 'zero.results.list'};
    } else {
        var columns = buildJSONArray(userList);
        sendToClient("columnList", columns);

        var sortRowListCount = Object.keys(columns).length - 2;
        var sortRowList = [];
        for (var i=0; i<sortRowListCount; i++) {
            var noSort = '';
            if (columns[i] === "Groups" || columns[i] === "Roles") {
                noSort = 'no-sort';
            }
            sortRowList.push({
                "name" : columns[i],
                "noSort" : noSort
            });
        }

        var domains = getDomainNames(env);
        return {claimProfile: claimProfile, domains: domains,
                claimValue: claimValue, sortRowList: sortRowList};
    }

}

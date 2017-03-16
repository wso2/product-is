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
    var userList = getUsersForList(0, 100, "", usernameClaim);
    buildJSONArray(userList);
    return {userList: userList};
}


function onPost(env) {

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
            "getProfile", ["user-identifier"]);
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

function buildJSONArray(userList) {
    var userArray = [];
    var claimArray = [];
    for (var i in userList) {
        var item = userList[i];
//        var claims = item.getClaims();
//        for (var j in claims) {
//            var claim = claims[j];
//            claimArray.push({
//            "DialectURI" : claims.getDialectUri(),
//            "ClaimURI" : claim.getClaimURI(),
//            "Value" : claim.getValue()
//            });
//        }
        userArray.push({
            "Username" : item.getUserId(),
            "Status" : item.getState(),
            "Groups" : "",
            "Roles" : "",
            "UniqueId" : item.getUserUniqueId(),
            "Domain" : item.getDomainName(),
            "Claims" : claimArray
        });
    }
    sendToClient("users", userArray);
}
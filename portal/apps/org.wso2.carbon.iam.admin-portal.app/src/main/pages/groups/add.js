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
     * Get the 'group' Profile
     */
    var claimProfile;
    try {
        claimProfile = callOSGiService("org.wso2.is.portal.user.client.api.ProfileMgtClientService",
            "getProfile", ["group"]);
    } catch (e) {
        return {errorMessage: 'group.error.retrieve.claim'};
    }
    var claimForProfileEntry = claimProfile.claims;
    var claimProfileArray = [];

    for (var i = 0; i < claimForProfileEntry.length; i++) {
        claimProfileArray[i] = generateClaimProfileMap(claimForProfileEntry[i]);
    }

    sendToClient("groupClaims", claimProfileArray);
    return {
        "groupClaims": claimProfileArray
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

function getDomainNames(env) {
    var domainNames;
    if (env.config.isDomainInLogin) {
        try {
            domainNames = callOSGiService("org.wso2.is.portal.user.client.api.IdentityStoreClientService",
                "getDomainNames", []);
        } catch (e) {
            return {errorMessage: 'group.error.retrieve.domain'};
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
            return {errorMessage: 'group.error.retrieve.domain'};
        }
    }
    return primaryDomainName;
}

function onGet(env) {
    try {

        var primaryDomainName = getPrimaryDomainName(env);

        // userList
        var requestedClaims = getClaimsInProfile("group-assign-users");
        var domainNames = getDomainNames(env);

        var userList = getUserList(0, -1, "", requestedClaims);
        var columns = buildJSONArray(userList);

        sendToClient("columnList", columns);
        sendToClient("selectedClaim", null);
        sendToClient("selectedDomain", null);
        sendToClient("action", null);
        sendToClient("offset", null);
        sendToClient("recordLimit", null);

        var sortRowList = [];

        return {
            primaryDomainName: primaryDomainName,
            domainNames: domainNames,
            profile: getProfile(),
            sortRowList: sortRowList
        };
    } catch (e) {
        return {errorMessage: 'group.add.error'};
    }

}

function onPost(env) {
    var claimMap = {};
    var domain = null;
    var formParams = null;
    var ArrayList = Java.type('java.util.ArrayList');
    var usersArray = new ArrayList();
    try {
        domain = env.request.formParams['domain'];
        formParams = env.request.formParams;
        for (var i in formParams) {
            if (i.indexOf("http://wso2.org/claims") !== -1) {
                claimMap[i] = formParams[i];
            }
            else if(i.indexOf("userUniqueId") !== -1) {
                usersArray.add(formParams[i]);
            }
        }

        var addGroupResult = addGroup(claimMap, domain);
        var assignUsersToGroup = addUsersToGroup(addGroupResult.addGroupResult.groupId, usersArray);
        var primaryDomainName = getPrimaryDomainName(env);

        // userList
        var requestedClaims = getClaimsInProfile("group-assign-users");
        var domainNames = getDomainNames(env);

        var userList = getUserList(0, -1, "", requestedClaims);
        var columns = buildJSONArray(userList);

        sendToClient("columnList", columns);
        sendToClient("selectedClaim", null);
        sendToClient("selectedDomain", null);
        sendToClient("action", null);
        sendToClient("offset", null);
        sendToClient("recordLimit", null);

        var sortRowList = [];

        return {
            primaryDomainName: primaryDomainName,
            domainNames: domainNames,
            profile: getProfile(),
            sortRowList: sortRowList,
            addGroupResult: addGroupResult
        };
    } catch (e) {
        return {errorMessage: 'group.add.error'};
    }


}

function addGroup(claimMap, domain) {
    try {
        var addGroupResult = callOSGiService("org.wso2.is.portal.user.client.api.IdentityStoreClientService",
            "addGroup", [claimMap, domain]);
        return {addGroupResult: addGroupResult};
    } catch (e) {
        return {errorMessage: 'group.add.error'};
    }
}


function addUsersToGroup(groupId, userIdsList) {
    try {
        var assignUsersResult = callOSGiService("org.wso2.is.portal.user.client.api.IdentityStoreClientService",
            "addUsersToGroup", [groupId, userIdsList]);
        return {assignUsersResult: assignUsersResult};
    } catch (e) {
        return {errorMessage: 'group.add.error'};
    }
}

function updateGroupProfile(groupId, updatedClaims) {

    try {
        var profileUIEntries = callOSGiService("org.wso2.is.portal.user.client.api.IdentityStoreClientService",
            "updateGroupProfile", [groupId, updatedClaims]);
        return {success: true, message: "Group profile is updated."};
    } catch (e) {
        var message = e.message;
        var cause = e.getCause();
        if (cause !== null) {
            //the exceptions thrown by the actual osgi service method is wrapped inside a InvocationTargetException.
            if (cause instanceof java.lang.reflect.InvocationTargetException) {
                message = cause.getTargetException().message;
            }
        }
    }
    return {success: false, message: message};
}

function isUserInGroup(userId, groupId) {
    try {
        var isUserInGroup = callOSGiService("org.wso2.is.portal.user.client.api.IdentityStoreClientService",
            "isUserInGroup", [userId, groupId]);
        return {success: true, isUserInGroup: isUserInGroup, message: "User is in group."};
    } catch (e) {
        var message = e.message;
        var cause = e.getCause();
        if (cause !== null) {
            //the exceptions thrown by the actual osgi service method is wrapped inside a InvocationTargetException.
            if (cause instanceof java.lang.reflect.InvocationTargetException) {
                message = cause.getTargetException().message;
            }
        }
    }
    return {success: false, message: message};
}

function updateUsersInGroup(groupId, addUserIds, removeUserIds) {

    try {
        callOSGiService("org.wso2.is.portal.user.client.api.IdentityStoreClientService",
            "updateUsersInGroup", [groupId, addUserIds, removeUserIds]);
        return {success: true, message: "Users are updated to group."};
    } catch (e) {
        var message = e.message;
        var cause = e.getCause();
        if (cause !== null) {
            //the exceptions thrown by the actual osgi service method is wrapped inside a InvocationTargetException.
            if (cause instanceof java.lang.reflect.InvocationTargetException) {
                message = cause.getTargetException().message;
            }
        }
    }
    return {success: false, message: message};
}


// UserList
function getFilteredList(offset, length, claimURI, claimValue, domainName, requestedClaims) {
    var userList;
    try {
        userList = callOSGiService("org.wso2.is.portal.user.client.api.IdentityStoreClientService",
            "getFilteredList", [offset, length, claimURI, claimValue, domainName, requestedClaims]);
    } catch (e) {
        return {errorMessage: 'list.error.retrieve.users'};
    }

    return userList;
}

function getUserList(offset, length, domainName, requestedClaims) {
    var userList;
    try {
        userList = callOSGiService("org.wso2.is.portal.user.client.api.IdentityStoreClientService",
            "getUserListForAssignment", [offset, length, domainName, requestedClaims]);
    } catch (e) {
        return {errorMessage: 'list.error.retrieve.users'};
    }

    return userList;
}

function getClaimsInProfile(profileName) {
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
    var columnsArray = [];
    // columnsArray.push("profilepic", "username", "uniqueId");

    sendToClient("users", userList);
    return columnsArray;
}



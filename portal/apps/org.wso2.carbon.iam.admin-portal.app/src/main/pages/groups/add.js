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

    // sendToClient("signupClaims", claimProfileArray); ToDo why?
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
    try {
        var domainNames = getDomainNames(env);
        var primaryDomainName = getPrimaryDomainName(env);

        return {domainNames: domainNames, primaryDomainName: primaryDomainName, profile: getProfile()};
    } catch (e) {
        return {errorMessage: 'group.add.error'};
    }

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

function buildUIEntries(profileUIEntries) {

    var uiEntries = [];
    if (profileUIEntries) {
        for (var i = 0; i < profileUIEntries.length > 0; i++) {
            var entry = {
                claimURI: profileUIEntries[i].claimConfigEntry.claimURI,
                claimLabel: profileUIEntries[i].claimConfigEntry.claimURI.replace("http://wso2.org/claims/", ""),
                displayName: profileUIEntries[i].claimConfigEntry.displayName,
                value: (profileUIEntries[i].value ? profileUIEntries[i].value : ""),
                readonly: ((profileUIEntries[i].claimConfigEntry.readonly &&
                profileUIEntries[i].claimConfigEntry.readonly) ? "readonly" : ""),
                required: ((profileUIEntries[i].claimConfigEntry.required &&
                profileUIEntries[i].claimConfigEntry.required) ? "required" : ""),
                requiredIcon: ((profileUIEntries[i].claimConfigEntry.required &&
                profileUIEntries[i].claimConfigEntry.required) ? "*" : ""),
                dataType: (profileUIEntries[i].claimConfigEntry.dataType ?
                    profileUIEntries[i].claimConfigEntry.dataType : "text"),
                regex: (profileUIEntries[i].claimConfigEntry.regex ?
                    profileUIEntries[i].claimConfigEntry.regex : ".*")
            };
            uiEntries.push(entry);
        }
    }
    return uiEntries;
}

// function getGroupProfileUIEntries() {
//
//     try {
//         var profileUIEntries = callOSGiService("org.wso2.is.portal.user.client.api.ProfileMgtClientService",
//             "getProfile", ["group"]);
//         return {success: true, profileUIEntries: profileUIEntries};
//     } catch (e) {
//         var message = e.message;
//         var cause = e.getCause();
//         if (cause !== null) {
//             //the exceptions thrown by the actual osgi service method is wrapped inside a InvocationTargetException.
//             if (cause instanceof java.lang.reflect.InvocationTargetException) {
//                 message = cause.getTargetException().message;
//             }
//         }
//     }
//     return {success: false, message: message};
// }

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

function onPost(env) {
    var success = false;
    var message = "";
    var updatedClaims = env.request.formParams;
    var groupId = env.params.groupId;
    var result = updateGroupProfile(groupId, updatedClaims);
    success = result.success;
    message = result.message;

    if (env.params.profileName) {

        var uiEntries = [];
        var result = getGroupProfileUIEntries(env.params.profileName, session.getUser().getUserId());
        if (result.success) {
            if (env.request.method != "POST") {
                success = true;
            }
            uiEntries = buildUIEntries(result.profileUIEntries);
        } else {
            success = false;
            message = result.message;
        }

        return {
            success: success, uiEntries: uiEntries,
            message: message,
        };
    }

    return {success: false, message: "Invalid profile name."};
}


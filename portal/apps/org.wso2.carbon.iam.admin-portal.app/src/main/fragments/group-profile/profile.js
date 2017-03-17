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

function getProfileUIEntries(profileName, groupId) {

    try {
        var profileUIEntries = callOSGiService("org.wso2.is.portal.user.client.api.ProfileMgtClientService",
            "getProfileEntries", [profileName, groupId]);
        return {success: true, profileUIEntries: profileUIEntries};
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

function updateGroupProfile(groupId, updatedClaims) {

    try {
        var profileUIEntries = callOSGiService("org.wso2.is.portal.user.client.api.IdentityStoreClientService",
            "updateGroupProfile", [groupId, updatedClaims]);
        return {success: true, message: "Group is updated."};
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

function onGet(env) {
    var session = getSession();
    var success = false;
    var message = "";
    if (env.params.profileName) {

        var uiEntries = [];
        var result = getProfileUIEntries(env.params.profileName, session.getUser().getUserId());
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
            success: success, profile: env.params.profileName, uiEntries: uiEntries,
            message: message
        };
    }

    return {success: false, message: "Invalid profile name."};
}


function onPost(env) {
    var session = getSession();
    var success = false;
    var message = "";

    if (env.params.profileName) {

        var updatedClaims = env.request.formParams;
        var result = updateGroupProfile(env.params.groupId, updatedClaims);
        success = result.success;
        message = result.message;


        if (env.params.profileName) {

            var uiEntries = [];
            var result = getProfileUIEntries(env.params.profileName, env.params.groupId);
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
                success: success, profile: env.params.profileName, uiEntries: uiEntries,
                message: message,
            };
        }

        return {success: false, message: "Invalid profile name."};
    }
}

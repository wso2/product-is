function onRequest(env) {

    var session = getSession();
    var success = false;
    var message = "";
    
    if (env.request.method == "POST" && env.params.profileName && env.params.profileName == env.params.actionId) {
        
        var updatedClaims = env.request.formParams;
        var result = updateUserProfile(session.getUser().getUserId(), updatedClaims);
        success = result.success;
        message = result.message;
    }

    if (env.params.profileName) {
        
        var uiEntries = [];
        var result = getProfileUIEntries(env.params.profileName, session.getUser().getUserId());
        if (result.success) {
            success = true;
            uiEntries = buildUIEntries(result.profileUIEntries);
        } else {
            success = false;
            message = result.message;
        }

        return {success: success, profile: env.params.profileName, uiEntries: uiEntries, message: message};
    }

    return {success: false, message: "Invalid profile name."};
}

function buildUIEntries(profileUIEntries) {

    var uiEntries = [];
    if (profileUIEntries) {
        for (var i = 0; i < profileUIEntries.length > 0; i++) {
            var entry = {
                claimURI: profileUIEntries[i].claimConfigEntry.claimURI,
                displayName: profileUIEntries[i].claimConfigEntry.displayName,
                value: (profileUIEntries[i].value ? profileUIEntries[i].value : ""),
                readonly: ((profileUIEntries[i].claimConfigEntry.readonly
                && profileUIEntries[i].claimConfigEntry.readonly == true) ? "readonly" : ""),
                required: ((profileUIEntries[i].claimConfigEntry.required
                && profileUIEntries[i].claimConfigEntry.required == true) ? "required" : ""),
                requiredIcon: ((profileUIEntries[i].claimConfigEntry.required
                && profileUIEntries[i].claimConfigEntry.required == true) ? "*" : ""),
                dataType: (profileUIEntries[i].claimConfigEntry.dataType ?
                    profileUIEntries[i].claimConfigEntry.dataType : "text")
            };
            uiEntries.push(entry);
        }
    }
    return uiEntries;
}

function getProfileUIEntries(profileName, userId) {

    try {
        var profileUIEntries = callOSGiService("org.wso2.is.portal.user.client.api.ProfileMgtClientService",
            "getProfileEntries", [profileName, userId]);
        return {success: true, profileUIEntries: profileUIEntries}
    } catch (e) {
        var message = e.message;
        var cause = e.getCause();
        if (cause != null) {
            //the exceptions thrown by the actual osgi service method is wrapped inside a InvocationTargetException.
            if (cause instanceof java.lang.reflect.InvocationTargetException) {
                message = cause.getTargetException().message;
            }
        }
    }
    return {success: false, message: message};
}

function updateUserProfile(userId, updatedClaims) {

    try {
        var profileUIEntries = callOSGiService("org.wso2.is.portal.user.client.api.IdentityStoreClientService",
            "updateUserProfile", [userId, updatedClaims]);
        return {success: true}
    } catch (e) {
        var message = e.message;
        var cause = e.getCause();
        if (cause != null) {
            //the exceptions thrown by the actual osgi service method is wrapped inside a InvocationTargetException.
            if (cause instanceof java.lang.reflect.InvocationTargetException) {
                message = cause.getTargetException().message;
            }
        }
    }
    return {success: false, message: message};
}
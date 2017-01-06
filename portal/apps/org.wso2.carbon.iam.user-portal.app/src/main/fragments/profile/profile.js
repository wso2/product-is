function onRequest(env) {

    if (env.params.profileName) {
        var result = getProfileUIEntries(env.params.profileName, "test-user-id");
        if (result.success) {
            var uiEntries = [];
            if (result.profileUIEntries) {
                var profileUIEntries = result.profileUIEntries;
                for (var i = 0; i < profileUIEntries.length > 0; i++) {
                    var entry = {
                        "claimURI": profileUIEntries[i].claimConfigEntry.claimURI,
                        "displayName": profileUIEntries[i].claimConfigEntry.displayName,
                        "value": (profileUIEntries[i].value ? profileUIEntries[i].value : "")
                    };
                    uiEntries.push(entry);
                    Log.info(entry);
                }
            }
            Log.info(uiEntries);
            return {"uiEntries": uiEntries}
        } else {
            return {errorMessage: result.message};
        }
    }

    return {errorMessage: result.message};
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
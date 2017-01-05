function onRequest(env) {

    if (env.params.profileName) {
        var result = getProfile(env.params.profileName);
        if (result.success) {
            if (result.profileEntry && result.profileEntry.claims) {
                var claims = result.profileEntry.claims;
                for(var i = 0; i < claims.length; i++) {
                    Log.info(claims[i].claimURI);
                }
            }
            return {"profileEntry": result.profileEntry}
        } else {
            return {errorMessage: result.message};
        }
    }

    return {errorMessage: result.message};
}

function getProfile(profileName) {

    try {
        var profileEntry = callOSGiService("org.wso2.is.portal.user.client.api.ProfileMgtClientService", "getProfile",
            [profileName]);
        return {success: true, profileEntry: profileEntry}
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
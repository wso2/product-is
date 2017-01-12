function onRequest(env) {

    var session = getSession();
    if (!session || !session.getUser()) {
        sendRedirect(env.contextPath + env.config['loginPageUri']);
    }

    var formId = "";
    if (env.request.method == "POST") {
        formId = env.request.queryString;
    }

    var result = getProfileNames();

    if (result.success) {
        return {profiles: result.profiles, actionId: formId}
    } else {
        return {errorMessage: result.message};
    }
}

function getProfileNames() {

    try {
        var profiles = callOSGiService("org.wso2.is.portal.user.client.api.ProfileMgtClientService",
            "getProfileNames", []);
        return {success: true, profiles: profiles}
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
function getPasscode(env) {
    try {
        var passcode = callOSGiService("org.wso2.is.portal.user.client.api.RecoveryMgtService", "persistPasscode",
            ["user1"]);;
    } catch (e) {
        return {errorMessage: 'Error while persisting the passcode'};
    }
    return {sucess: true, code: passcode};
}

function onPost(env) {
    var passcode = getPasscode(env);
    if (passcode.errorMessage) {
        return {success: false, message: 'error.while.persisting'};
    } else {
        sendToClient("passcode", passcode.code);
        return {success: true, message: 'successfully.persisted'};
    }
}


function getPasscode(env) {
    try {
        var passcode = callOSGiService("org.wso2.is.portal.user.client.api.RecoveryMgtService", "persistPasscode",
            ["user1"]);
    } catch (e) {
        return {success: false, errorMessage: 'Error while persisting the passcode'};
    }
    return passcode;
}

function onPost(env) {
    try {
        var passcode = getPasscode(env);
        sendToClient("passcode", passcode);
        return {success: true, message: 'Succefully persisted the passcode.'};

    } catch (e) {
        return {success: false, errorMessage: 'Error while persisting the passcode'};
    }

}




function getPasscode(env) {
    try {
        var passcode = callOSGiService("org.wso2.is.portal.user.client.api.RecoveryMgtService", "persistPassCode",
            ["user1"]);
    } catch (e) {
        return {errorMessage: 'Error while generating the passcode'};
    }
    return passcode;
}

function onPost(env){
    var passcode = getPasscode(env);
    sendToClient("passCode", passcode);

}


function onPost(env) {
    var newPassword = env.request.formParams['newPassword'];
    //todo uniqueUserId should be obtained from the userlist and remove this hardcoded value
    var uniqueUserId = "user1";
    var recoveryMgtService = callOSGiService("org.wso2.is.portal.user.client.api.RecoveryMgtService", "persistOTP", [uniqueUserId, newPassword]);
}
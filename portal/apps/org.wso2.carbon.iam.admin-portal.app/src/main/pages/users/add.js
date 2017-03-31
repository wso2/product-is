function onGet(env) {
    var session = getSession();
    var domainNames = getDomainNames(env);
    var primaryDomainName = getPrimaryDomainName(env);
    var PasswordGenerationUtil = Java.type('org.wso2.is.portal.user.client.api.util.PasswordGenerationUtil');
    var passwordMinLength = PasswordGenerationUtil.getPasswordMinLength();
    var isNumbersIncluded = PasswordGenerationUtil.isIncludeNumbers();
    var isUpperCaseNeeded = PasswordGenerationUtil.isIncludeUpperCase();
    var isLowerCaseNeeded = PasswordGenerationUtil.isIncludeLowerCase();
    var isSpecialCharacterNeeded = PasswordGenerationUtil.isIncludeSymbols();
    sendToClient("result", {
        passwordMinLength: passwordMinLength, isNumbersIncluded: isNumbersIncluded,
        isUpperCaseNeeded: isUpperCaseNeeded, isLowerCaseNeeded: isLowerCaseNeeded,
        isSpecialCharacterNeeded: isSpecialCharacterNeeded
    });

    return {domainNames: domainNames, primaryDomainName: primaryDomainName};
}

function onPost(env) {

    var domainNames = getDomainNames(env);
    var primaryDomainName = getPrimaryDomainName(env);
    var PasswordGenerationUtil = Java.type('org.wso2.is.portal.user.client.api.util.PasswordGenerationUtil');
    var passwordMinLength = PasswordGenerationUtil.getPasswordMinLength();
    var isNumbersIncluded = PasswordGenerationUtil.isIncludeNumbers();
    var isUpperCaseNeeded = PasswordGenerationUtil.isIncludeUpperCase();
    var isLowerCaseNeeded = PasswordGenerationUtil.isIncludeLowerCase();
    var isSpecialCharacterNeeded = PasswordGenerationUtil.isIncludeSymbols();
    sendToClient("result", {
        passwordMinLength: passwordMinLength, isNumbersIncluded: isNumbersIncluded,
        isUpperCaseNeeded: isUpperCaseNeeded, isLowerCaseNeeded: isLowerCaseNeeded,
        isSpecialCharacterNeeded: isSpecialCharacterNeeded
    });


    var optionSelector = env.request.formParams['verificationSelector'];

    if (optionSelector === "with_password") {
        var credentialMap = {};
        var claimMap = {};
        var domain = null;
        Log.info("the selected option is with password");
        credentialMap["password"] = env.request.formParams['newPassword'];
        claimMap["http://wso2.org/claims/username"] = env.request.formParams['inputUsername'];
        domain = env.request.formParams['domain'];
        var registrationResult = userRegistration(claimMap, credentialMap, domain);
        if (registrationResult.errorMessage) {
            return {
                domainNames: domainNames, primaryDomainName: primaryDomainName,
                errorMessage: registrationResult.errorMessage
            };
        }
        else {
            return {
                domainNames: domainNames,
                primaryDomainName: primaryDomainName,
                message: registrationResult.message
            };
            //TODO:do a redirect to listing page once user added.
            //sendRedirect(env.contextPath + '/users/list');
        }
    }
    else if (optionSelector === "ask_password") {
        var credentialMapAP = {};
        var claimMapAP = {};
        var domainAP = null;
        var emailMapAP = {};
        claimMapAP["http://wso2.org/claims/username"] = env.request.formParams['inputUsername'];
        claimMapAP["http://wso2.org/claims/email"] = env.request.formParams['askPwdEmail'];
        credentialMapAP["password"] = generatePassword(8);
        Log.info("the generated password :"+ credentialMapAP["password"]);
        domainAP = env.request.formParams['domain'];
        emailMapAP["email"]  = env.request.formParams['askPwdEmail'];
        Log.info("the given email is :"+ emailMapAP["email"]);
        var enableAskPasswordUsingEmail = true;
        var addUserAskPasswordResult = userRegistrationWithAskPassword(claimMapAP, credentialMapAP, domainAP,
            enableAskPasswordUsingEmail);
        return {
            domainNames: domainNames, primaryDomainName: primaryDomainName
        };
    }
    else if (optionSelector === "otp") {

        Log.info("the selected option is using OTP");
        return {
            domainNames: domainNames, primaryDomainName: primaryDomainName
        };
    }
    else if (optionSelector === "email_or_phone") {

        Log.info("the selected option is using email");
        return {
            domainNames: domainNames, primaryDomainName: primaryDomainName
        };
    }

}

function getDomainNames(env) {
    var domainNames;
    if (env.config.isDomainInLogin) {
        try {
            domainNames = callOSGiService("org.wso2.is.portal.user.client.api.IdentityStoreClientService",
                "getDomainNames", []);
        } catch (e) {
            sendError(500, "user.add.error.retrieve.domain");
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
            sendError(500, "user.add.error.retrieve.primary.domain");
        }
    }
    return primaryDomainName;
}

function userRegistration(claimMap, credentialMap, domain) {
    try {
        var userRegistrationResult = callOSGiService("org.wso2.is.portal.user.client.api.IdentityStoreClientService",
            "addUser", [claimMap, credentialMap, domain]);
        return {userRegistration: userRegistrationResult, message: 'user.add.success.message'};
    } catch (e) {
        var message = "Error occurred while adding the user.";
        Log.error(message, e);
        return {
            errorMessage: message
        };
    }
}

function userRegistrationWithAskPassword(claimMap, credentialMap, domain, enableAskPasswordUsingEmail){
    Log.info("Succcessfully added");
    try {
        var userRegistrationResult = callOSGiService("org.wso2.is.portal.user.client.api.IdentityStoreClientService",
                "addUser", [claimMap, credentialMap, domain, enableAskPasswordUsingEmail]);
            return {userRegistration: userRegistrationResult, message: 'user.add.success.message'};

    } catch (e){
        var message = "Error occurred while adding the user.";
        Log.error(message, e);
        return {
            errorMessage: message
        };
    }


}

function generatePassword(length) {
    var length = length,
        charset = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789",
        retVal = "";
    for (var i = 0, n = charset.length; i < length; ++i) {
        retVal += charset.charAt(Math.floor(Math.random() * n));
    }
    return retVal;
}

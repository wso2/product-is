function onGet(env) {
    var session = getSession();
    var domainNames = getDomainNames(env);
    var primaryDomainName = getPrimaryDomainName(env);
    sendPasswordStrengthParameters();
    return {domainNames: domainNames, primaryDomainName: primaryDomainName};
}

function onPost(env) {
    var domainNames = getDomainNames(env);
    var primaryDomainName = getPrimaryDomainName(env);
    sendPasswordStrengthParameters();
    var claimMap = {};
    var domain = null;
    var credentialMap;
    var registrationResult;
    var optionSelector = env.request.formParams['verificationSelector'];
    claimMap["http://wso2.org/claims/username"] = env.request.formParams['inputUsername'];
    domain = env.request.formParams['domain'];
    switch (optionSelector) {
        case "with_password" :
            credentialMap = {};
            credentialMap["password"] = env.request.formParams['newPassword'];
            registrationResult = userRegistration(claimMap, credentialMap, domain);
            break;
        case "ask_password" :
            credentialMap = {};
            claimMap["http://wso2.org/claims/email"] = env.request.formParams['askPwdEmail'];
            /*The password is going to store in recovery table and not exposed to customer. This will
             be replaced after updating password.
             */
            credentialMap["password"] = generatePassword(8);
            registrationResult = userRegistration(claimMap, credentialMap, domain);
            break;
        case "otp" :
            break;
        case "email_or_phone" :
            break;
    }
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
    }
}

function sendPasswordStrengthParameters() {
    var PasswordPolicyConfigurationUtil = Java
        .type('org.wso2.is.portal.user.client.api.util.PasswordPolicyConfigurationUtil');
    var isRegexValidation = PasswordPolicyConfigurationUtil.isRegexValidation();
    if (isRegexValidation === false) {
        var passwordMinLength = PasswordPolicyConfigurationUtil.getPasswordMinLength();
        var isNumbersIncluded = PasswordPolicyConfigurationUtil.isIncludeNumbers();
        var isUpperCaseNeeded = PasswordPolicyConfigurationUtil.isIncludeUpperCase();
        var isLowerCaseNeeded = PasswordPolicyConfigurationUtil.isIncludeLowerCase();
        var isSpecialCharacterNeeded = PasswordPolicyConfigurationUtil.isIncludeSymbols();
        sendToClient("result", {
            passwordMinLength: passwordMinLength, isNumbersIncluded: isNumbersIncluded,
            isUpperCaseNeeded: isUpperCaseNeeded, isLowerCaseNeeded: isLowerCaseNeeded,
            isSpecialCharacterNeeded: isSpecialCharacterNeeded, regexvalidation: false
        });
    } else {
        sendToClient("result", {
            regexvalidation: true
        });
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

// A random temporary password generation which will not expose to the user nor admin.
function generatePassword(length) {
    var length = length,
        charset = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789",
        retVal = "";
    for (var i = 0, n = charset.length; i < length; ++i) {
        retVal += charset.charAt(Math.floor(Math.random() * n));
    }
    return retVal;
}

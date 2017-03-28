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
    var credentialMap = {};
    var domain = null;
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

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
    var claimMap = {};
    var credentialMap = {};
    var domain = null;
    credentialMap["password"] = env.request.formParams['newPassword'];
    claimMap["http://wso2.org/claims/username"] = env.request.formParams['inputUsername'];
    domain = env.request.formParams['domain'];
    var registrationResult = userRegistration(claimMap, credentialMap, domain);
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

function getDomainNames(env) {
    var domainNames;
    if (env.config.isDomainInLogin) {
        try {
            domainNames = callOSGiService("org.wso2.is.portal.user.client.api.IdentityStoreClientService",
                "getDomainNames", []);
        } catch (e) {
            return {errorMessage: 'signup.error.retrieve.domain'};
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
            return {errorMessage: 'signup.error.retrieve.domain'};
        }
    }
    return primaryDomainName;
}

function userRegistration(claimMap, credentialMap, domain) {
    try {
        var userRegistrationResult = callOSGiService("org.wso2.is.portal.user.client.api.IdentityStoreClientService",
            "addUser", [claimMap, credentialMap, domain]);
        return {userRegistration: userRegistrationResult};
    } catch (e) {
        return {errorMessage: 'user.add.error'};
    }
}

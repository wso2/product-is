/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

/**
 * Recovery Related utility methods are defined here
 *
 */

var recoveryManager = {};

(function (recoveryManager) {

    /**
     * Check whether username recovery enabled.
     * @returns {*}
     */

    function isUsernameRecoveryPortalEnabled() {
        return getRecoveryConfigs().getUsername().isEnablePortal();
    }

    /**
     * Check whether the question password recovery enabled
     *
     * @param method define osgi service method to be called
     * @returns {*}
     */

    function isQuestionBasedPasswordRecoveryEnabled() {
        var checkMethod = "isQuestionBasedPwdRecoveryEnabled";
        return callOSGiService("org.wso2.is.portal.user.client.api.ChallengeQuestionManagerClientService",
            checkMethod, []);
    }

    /**
     * Check whether the notification based password recovery enabled
     *
     * @param method define osgi service method to be called
     * @returns {*}
     */

    function isNotificationBasedPasswordRecoveryEnabled() {
        var checkMethod = "isNotificationBasedPasswordRecoveryEnabled";
        return callOSGiService("org.wso2.is.portal.user.client.api.RecoveryMgtService",
            checkMethod, []);

    }

    /**
     * Check whether the password recovery enabled.
     *
     * @returns {*}
     */

    function isPasswordRecoveryEnabled() {
        return getRecoveryConfigs().getPassword().isEnablePortal();
    }

    /**
     * Get recovery configs of the system
     * @param
     * @returns {*}
     */

    function getRecoveryConfigs() {
        var checkMethod = "getRecoveryConfigs";
        return callOSGiService("org.wso2.is.portal.user.client.api.RecoveryMgtService",
            checkMethod, []);
    }

    /**
     * Check whether multiple password recovery options enabled
     * @param method define osgi service method to be called
     * @returns {*}
     */

    function hasMultiplePasswordRecoveryEnabled() {
        var config = getRecoveryConfigs().getPassword();
        var emailLink = config.getNotificationBased().getRecoveryLink().isEnablePortal();
        var questionBased = config.getSecurityQuestion().isEnablePortal();
        var external = config.getExternal().isEnablePortal();
        return emailLink ? (questionBased || external) : (questionBased && external);
    }


    /**
     * private method to return security questions of the user
     * @param userUniqueId
     * @returns {{}}
     */
    function getUserQuestions(userUniqueId) {

        var result = {};
        result.success = true;
        result.message = "";
        try {
            var challengeQuestions = callOSGiService("org.wso2.is.portal.user.client.api.ChallengeQuestionManagerClientService",
                "getAllChallengeQuestionsForUser", [userUniqueId]);
        } catch (e) {
            Log.error(e.message);
            result.success = false;
            result.message = "contact.system.admin";
            return result;
            // TODO Backend throws error when user doesn't exist with useId, has to distinguish no-user exists
        }
        result.data = challengeQuestions;
        return result;
    }

    /**
     * private method to return security questions of the user
     * @param userUniqueId
     * @returns {{}}
     */
    function getUserAnsweredQuestions(userUniqueId) {

        var result = {};
        result.success = true;
        result.message = "";
        try {
            var challengeQuestionsResponse = callOSGiService("org.wso2.is.portal.user.client.api.RecoveryMgtService",
                "getUserChallengeQuestionAtOnce", [userUniqueId]);
        } catch (e) {
            Log.error(e.message);
            result.success = false;
            result.message = "contact.system.admin";
            return result;
            // TODO Backend throws error when user doesn't exist with useId, has to distinguish no-user exists
        }
        // TODO handle account locked(17003) and disabled(17004) status
        result.code = challengeQuestionsResponse.getCode();
        result.data = challengeQuestionsResponse.getQuestions();
        result.status = challengeQuestionsResponse.getStatus();
        return result;
    }

    /**
     * private method to validate answers for security questions of the user
     * @param answers
     * @returns {{}}
     */
    function verifyUserChallengeAnswers(answers) {

        var result = {};
        result.success = true;
        result.message = "";
        try {
            var challengeQuestionsResponse = callOSGiService("org.wso2.is.portal.user.client.api.RecoveryMgtService",
                "verifyUserChallengeAnswers", ['',answers]);
        } catch (e) {
            Log.error(e.message);
            result.success = false;
            result.message = "contact.system.admin";
            return result;
            // TODO Backend throws error when user doesn't exist with useId, has to distinguish no-user exists
        }
        result.code = challengeQuestionsResponse.getCode();
        result.data = challengeQuestionsResponse.getQuestions();
        result.status = challengeQuestionsResponse.getStatus();
        return result;
    }

    /**
     * Returns whether username recovery portal enabled.
     * @returns {success: true/flase, isEnabled: true/false}
     */
    recoveryManager.isUsernameRecoveryPortalEnabled = function () {
        return isUsernameRecoveryPortalEnabled();
    };

    /**
     * Returns whether password recovery options are enabled
     * @returns {success: true/flase, isEnabled: true/false}
     */
    recoveryManager.isPasswordRecoveryEnabled = function () {
        return isPasswordRecoveryEnabled();
    };

    /**
     * Returns whether password recovery options are enabled
     * @returns {success: true/flase, isEnabled: true/false}
     */
    recoveryManager.hasMultiplePasswordRecoveryEnabled = function () {
        return hasMultiplePasswordRecoveryEnabled();
    };

    /**
     * Returns whether requested password recovery option is enabled.
     * @param option recovery option
     * @returns {success: true/false, isEnabled: true/false}
     */
    recoveryManager.isPasswordRecoveryOptionEnabled = function (option) {
        if (option == "notification-based") {
            return isNotificationBasedPasswordRecoveryEnabled();
        } else if (option == "security-question-based") {
            return isQuestionBasedPasswordRecoveryEnabled();
        } else {
            return false;
        }
    };

    /**
     * Returns security questions of the user
     * @param uniqueUserId
     * @returns {*}
     */
    recoveryManager.getUserQuestions = function (uniqueUserId) {
        if (uniqueUserId) {
            return getUserQuestions(uniqueUserId);
        } else {
            return { success: false };
        }
    };

    /**
     * Returns security questions of the user
     * @param uniqueUserId
     * @returns {*}
     */
    recoveryManager.getUserAnsweredQuestions = function (uniqueUserId) {
        if (uniqueUserId) {
            return getUserAnsweredQuestions(uniqueUserId);
        } else {
            return { success: false };
        }
    };

    /**
     * Returns security questions of the user
     * @param answers
     * @returns {*}
     */
    recoveryManager.verifyUserChallengeAnswers = function (answers) {
        if (answers) {
            return verifyUserChallengeAnswers(answers);
        } else {
            return { success: false };
        }
    };

    /**
     * Returns security questions of the user
     * @param answers
     * @returns {*}
     */
    recoveryManager.recoverPasswordViaUserChallengeAnswers = function (env) {
        var formParams = env.request.formParams;
        var result = verifyUserChallengeAnswers(formParams);

        if (!result.success) {
            sendError(500, result.message);
        }
        var error;
        if(result.status === "COMPLETE"){
            sendRedirect(env.contextPath + '/recovery/password-reset?confirmation=' + result.code);
        } else if (result.status === "INCOMPLETE") {
            sendToClient("result", { status: result.status, option : "security-question-recovery" } );
        } else if (result.status === "20008") {
            sendToClient("result", { status: result.status, option : "security-question-recovery" } );
            error = "error." + result.status;
        } else if (result.status) {
            sendRedirect(env.contextPath + '/recovery/failure?code=' + result.code + '&status=' + result.status);
        }
        var pwdRecoveryConfig = getRecoveryConfigs().getPassword();
        var hasMultiple = hasMultiplePasswordRecoveryEnabled();

        return {
            hasMultipleOptions: hasMultiple,
            hasUserQuestions: result.data.length > 0,
            userQuestions: result.data,
            externalOption : pwdRecoveryConfig.getExternal().getUrl(),
            recoveryCode : result.code,
            errorMessage : error
        };
    };

    /**
     * Returns security questions of the user
     * @param
     * @returns {*}
     */
    recoveryManager.getRecoveryConfigs = function () {
        return getRecoveryConfigs();
    };

    recoveryManager.sendRecoveryNotification = function (uniqueUserId) {
        if (uniqueUserId) {
            try {
                var recoveryMgtService = callOSGiService("org.wso2.is.portal.user.client.api.RecoveryMgtService", "setPasswordRecoveryNotification", [uniqueUserId]);
            } catch (e) {
                //todo need show error message in UI
                Log.error(e.getMessage());
            }
        }
    };

    recoveryManager.updatePassword = function (code, password) {
        try {
            var passwordChar = Java.to(password.split(''), 'char[]');
            var recoveryMgtService = callOSGiService("org.wso2.is.portal.user.client.api.RecoveryMgtService", "updatePassword", [code, passwordChar]);
        } catch (e) {
            //todo need show error message in UI
            Log.error(e.getMessage());
        }

    };

})(recoveryManager);
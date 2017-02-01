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
     * Check whether the password recovery enabled
     * @param method define osgi service method to be called
     * @returns {*}
     */

    function isPasswordRecoveryEnabled() {
        var questionBased = isQuestionBasedPasswordRecoveryEnabled();
        var notificationBased = isNotificationBasedPasswordRecoveryEnabled();
        return notificationBased || questionBased;
    }

    /**
     * Check whether multiple password recovery options enabled
     * @param method define osgi service method to be called
     * @returns {*}
     */

    function hasMultiplePasswordRecoveryEnabled() {
        var questionBased = isQuestionBasedPasswordRecoveryEnabled();
        var notificationBased = isNotificationBasedPasswordRecoveryEnabled();
        return notificationBased && questionBased;
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
            result.message = "something.wrong.error";
            return result;
        }
        result.data = challengeQuestions;
        return result;
    }

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
     * Returns whether requested password recovery option is enabled
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

})(recoveryManager);
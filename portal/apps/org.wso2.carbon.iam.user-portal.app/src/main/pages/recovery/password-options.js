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

module("recovery-manager");

function onGet(env) {
    var username = env.request.queryParams['username'];
    var domain = env.request.queryParams['domain'];
    var userId = env.request.queryParams['userId'];

    //if username is not available redirected to password recovery init page
    if (!username) {
        sendRedirect(env.contextPath + '/recovery/password');
    }

    Log.debug("Check whether Notification Based Password Recovery is Enabled.");
    var hasMultiple = recoveryManager.hasMultiplePasswordRecoveryEnabled();

    if (hasMultiple.success) {
        if (!hasMultiple.isEnabled) {//when multiple recovery options are not enabled
            Log.debug("Multiple Password Recovery options are not Enabled.");
            if (recoveryManager.isPasswordRecoveryOptionEnabled("notification-based").isEnabled) {
                Log.debug("Notification Based Password Recovery flow started.");
                //TODO invoke password recovery via email
                sendRedirect(env.contextPath + '/recovery/password-complete?username=' + username);
            }
            if (recoveryManager.isPasswordRecoveryOptionEnabled("security-question-based").isEnabled) {
                Log.debug("Security Question Based Password Recovery flow started.");
                sendRedirect(env.contextPath + '/recovery/security-questions?username=' + username);
            }
            //TODO decide what, when non of the options are enabled
        } else {
            var questions = recoveryManager.getUserQuestions(userId);
            if (questions.data.length > 0) {
                return {
                    hasMultipleOptions: hasMultiple.isEnabled,
                    hasUserQuestions: true,
                    userQuestions: questions.data
                };
            } else {
                return {hasMultipleOptions: hasMultiple.isEnabled};
            }
        }
    } else {
        Log.error("Error while checking whether multiple recovery options are enabled.");
        return {errorMessage: "something.wrong.error"};
    }
}


function onPost(env) {
    //TODO pasword recover option handle

//        Log.info(env.request.formParams);
//        var isEmailBased = env.request.formParams['recover-option-email'];
//        if(isEmailBased){
//            //TODO invoke password recovery via email
//            sendRedirect(env.contextPath + '/recovery/password-complete');
//        }
//        var isQuestionBased = env.request.formParams['recover-option-question'];
//        if(isQuestionBased){
//            //TODO invoke password recovery via questions
//            sendRedirect(env.contextPath + '/recovery/password-complete');
//        }
//        //TODO else
}
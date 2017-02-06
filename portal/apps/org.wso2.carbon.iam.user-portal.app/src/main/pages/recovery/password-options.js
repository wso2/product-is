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
    if (!username || !domain || !userId) {
        sendRedirect(env.contextPath + '/recovery/password');
    }

    //TODO wrap with try after fixing error in senRedirect inside try
    var hasMultiple = recoveryManager.hasMultiplePasswordRecoveryEnabled();
    if (!hasMultiple) {//when multiple recovery options are not enabled
        if (recoveryManager.isPasswordRecoveryOptionEnabled("notification-based")) {
            Log.debug("Notification Based Password Recovery flow started for user: " + userId);
            //TODO invoke password recovery via email
            sendRedirect(env.contextPath + '/recovery/complete?password=true');

        } else if (recoveryManager.isPasswordRecoveryOptionEnabled("security-question-based")) {
            Log.debug("Security Question Based Password Recovery flow for user: " + userId);
            sendRedirect(env.contextPath + '/recovery/security-questions?username=' + username + "&userId="+ userId);

        }
        //TODO decide what, when non of the options are enabled
    } else {
        var questions = recoveryManager.getUserQuestions(userId);
        if (!questions.success) {
            sendError(500, questions.message);
            //TODO
        }
        return {
            hasMultipleOptions: hasMultiple,
            hasUserQuestions: questions.data.length > 0,
            userQuestions: questions.data
        };
    }
}

function onPost(env) {
    //TODO pasword recover option handle

//        Log.info(env.request.formParams);
//        var isEmailBased = env.request.formParams['recover-option-email'];
//        if(isEmailBased){
//            //TODO invoke password recovery via email
//            sendRedirect(env.contextPath + '/recovery/complete?username=true');
//        }
//        var isQuestionBased = env.request.formParams['recover-option-question'];
//        if(isQuestionBased){
//            //TODO invoke password recovery via questions
//            sendRedirect(env.contextPath + '/recovery/complete?username=true');
//        }
//        //TODO else
}
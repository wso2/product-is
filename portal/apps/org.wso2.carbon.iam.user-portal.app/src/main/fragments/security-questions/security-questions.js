/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

function onGet(env) {
    var data = {};

    data.passwordform=true;
    return data;
}

function onPost(env) {

    var data = {};
    var session, userUniqueId, action, oldPassword, username, domain, authenticationResult, getChallengeQResult
        , getChallengeQuestionsResult, ids, newAnswer, questionId, questionSetId, updateChallengeQResult
        , deleteChallengeQResult;
    data.success = true;
    data.minQuestions =getMinimumNoOfQuestionsToAnswer().data;
    session = getSession();
    userUniqueId = session.getUser().getUserId();
    action = env.request.formParams["action"];
    if (action === "check-password") {
        oldPassword = env.request.formParams["old-password"];
        username = session.getUser().getUsername();
        domain = session.getUser().getDomainName();
        authenticationResult = authenticate(username, oldPassword, domain);
        if (authenticationResult.success) {
            getChallengeQResult = getUserQuestions(userUniqueId);
            if (getChallengeQResult.data.length === 0) {
                data.isUserAuthenticated = true;
                getChallengeQuestionsResult = getChallengeQuestions(userUniqueId);
                data.questionList = getChallengeQuestionsResult.data;
                data.passwordform = false;
            } else {
                data.isUserHasQuestions = true;
                data.passwordform = false;
                data.userQuestions = getChallengeQResult.data;
            }
        } else {
            data.passwordform = true;
            data.success = authenticationResult.success;
            data.message = authenticationResult.message;
        }
        return data;
    } else if (action === "add-question") {
        // Add question flow.
        ids = env.request.formParams["question_list"];
        var i;
        if (ids !== null) {
            for (i = 0; i < ids.length; i++) {
                var idsArray = ids[i].split(":");
                questionSetId = idsArray[0];
                questionId = idsArray[1];
                var answerId = "question-answer-" + questionSetId;
                var answer = env.request.formParams[answerId];
                if(answer) {
                    var addChallengeQResult = setChallengeAnswer(userUniqueId, answer, questionSetId, questionId,
                        "challengeQAdd");
                    if (!addChallengeQResult.success) {
                        getChallengeQuestionsResult = getChallengeQuestions(userUniqueId);
                        data.questionList = getChallengeQuestionsResult.data;
                        data.isUserAuthenticated = true;
                        data.success = addChallengeQResult.success;
                        data.message = addChallengeQResult.message;
                        break;
                    } else {
                        data.isUserHasQuestions = true;
                        data.success = addChallengeQResult.success;
                        data.message = addChallengeQResult.message;
                        getChallengeQResult = getUserQuestions(userUniqueId);
                        if (getChallengeQResult.data !== null) {
                            if (getChallengeQResult.data.length === 0) {
                                data.isUserHasQuestions = false;
                            } else {
                                data.isUserHasQuestions = true;
                                data.userQuestions = getChallengeQResult.data;
                            }
                        }
                    }
                }
            }
        }
        return data;
    } else if (action === "update-question") {

        // Update question answer flow.
        newAnswer = env.request.formParams["new-answer"];
        questionId = env.request.formParams["question-id"];
        questionSetId = env.request.formParams["question-set-id"];
        updateChallengeQResult = setChallengeAnswer(userUniqueId, newAnswer, questionSetId, questionId,
            "challengeQUpdate");
        data.success = updateChallengeQResult.success;
        data.message = updateChallengeQResult.message;
    } else if (action === "delete-question") {

        // Delete question flow.
        questionId = env.request.formParams["question-id"];
        questionSetId = env.request.formParams["question-set-id"];
        deleteChallengeQResult = deleteQuestion(userUniqueId, questionId, questionSetId);
        data.message = deleteChallengeQResult.message;
        data.success = deleteChallengeQResult.success;
    }
    getChallengeQResult = getUserQuestions(userUniqueId);
    if (getChallengeQResult.data !== null) {
        if (getChallengeQResult.data.length === 0) {
            data.isUserHasQuestions = false;
        } else {
            data.isUserHasQuestions = true;
            data.userQuestions = getChallengeQResult.data;
        }
    }
    return data;
}

function getUserQuestions(userUniqueId) {
    try {
        var challengeQuestions = callOSGiService("org.wso2.is.portal.user.client.api.ChallengeQuestionManagerClientService",
            "getAllChallengeQuestionsForUser", [userUniqueId]);
        return {success: true, message: "", data : challengeQuestions};
    } catch (e) {
        return {success: false, message: 'security.question.error.getChallengeQuestionsForUser'};
    }
}

function getChallengeQuestions(userUniqueId) {
    try {
        var challengeQuestions = callOSGiService("org.wso2.is.portal.user.client.api.ChallengeQuestionManagerClientService",
            "getChallengeQuestionList", [userUniqueId]);
        return {success: true, message: "", data : challengeQuestions};
    } catch (e) {
        return {success: false, message: 'security.question.error.getChallengeQuestionList'};
    }
}

function setChallengeAnswer(userUniqueId, answer, questionSetId, questionId, actionId) {
    try {
        callOSGiService("org.wso2.is.portal.user.client.api.ChallengeQuestionManagerClientService",
            "setChallengeQuestionForUser", [userUniqueId, questionId, questionSetId, answer, actionId]);
        return {success: true, message: 'security.question.success.setChallengeAnswer'};
    } catch (e) {
        return {success: false, message: 'security.question.error.setChallengeAnswer'};
    }
}

function deleteQuestion(userUniqueId, questionId, questionSetId) {
    try {
        callOSGiService("org.wso2.is.portal.user.client.api.ChallengeQuestionManagerClientService",
            "deleteChallengeQuestionForUser", [userUniqueId, questionId, questionSetId]);
        return {success: true, message: 'security.question.success.deleteQuestion'};
    } catch (e) {
        return {success: false, message: 'security.question.error.deleteQuestion'};
    }
}

function authenticate(username, password, domain) {
    try {
        var passwordChar = Java.to(password.split(''), 'char[]');
        var uufUser = callOSGiService("org.wso2.is.portal.user.client.api.IdentityStoreClientService",
            "authenticate", [username, passwordChar, domain]);
        return {success: true, message: "success"};
    } catch (e) {
        return {success: false, message: 'security.question.error.invalidPassword'};
    }
}

function getMinimumNoOfQuestionsToAnswer(){
    try{
    var minNumOfQuestions = callOSGiService("org.wso2.is.portal.user.client.api.ChallengeQuestionManagerClientService",
        "getMinimumNoOfChallengeQuestionsToAnswer", []);
        return {success: true, data: minNumOfQuestions};
    } catch (e) {
        return {success: false, message: 'security.question.error.getMinimumNoOfChallengeQuestionsToAnswer'};
    }
}
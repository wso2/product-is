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
    data.success = true;
    var session = getSession();
    var userUniqueId = session.getUser().getUserId();
    var action = env.request.formParams["action"];

    var getUserQuestionsResult = getUserQuestions(userUniqueId);

    if(!getUserQuestionsResult.success) {
        data.success = getUserQuestionsResult.success;
        data.message = getUserQuestionsResult.message;
    }

    if (getUserQuestionsResult.data.length === 0) {
        data.isUserHasQuestions = false;
    } else {
        data.isUserHasQuestions = true;
        data.userQuestions = getUserQuestionsResult.data;
    }

    var getChallengeQuestionsResult = getChallengeQuestions(userUniqueId);
    if(!getChallengeQuestionsResult.success) {
        data.success = getChallengeQuestionsResult.success;
        data.message = getChallengeQuestionsResult.message;
    }
    data.questionList = getChallengeQuestionsResult.data;
    return data;
}


function onPost(env) {

    var data = {};
    data.success = true;
    var session = getSession();
    var userUniqueId = session.getUser().getUserId();
    var action = env.request.formParams["action"];

    if (action == "add-question") {

        // Add question flow.
        var answer = env.request.formParams["question-answer"];
        var ids = env.request.formParams["question_list"];
        var idsArray = ids.split(":");
        var questionSetId = idsArray[0];
        var questionId = idsArray[1];
        var addChallengeQResult = setChallengeAnswer(userUniqueId, answer, questionSetId, questionId);
        data.success = addChallengeQResult.success;
        data.message = addChallengeQResult.message;
    } else if (action == "update-question") {

        // Update question answer flow.
        var oldPassword = env.request.formParams["old-password"];
        var newAnswer = env.request.formParams["new-answer"];
        questionId = env.request.formParams["question-id"];
        questionSetId = env.request.formParams["question-set-id"];
        var username = session.getUser().getUsername();
        var domain = session.getUser().getDomainName();
        var authenticationResult = authenticate(username, oldPassword, domain);
        if (authenticationResult.success) {
            var updateChallengeQResult = setChallengeAnswer(userUniqueId, newAnswer, questionSetId, questionId);
            data.success = updateChallengeQResult.success;
            data.message = updateChallengeQResult.message;
        } else {
            data.success = authenticationResult.success;
            data.message = authenticationResult.message;
        }
    } else if (action == "delete-question") {

        // Delete question flow.
        questionId = env.request.formParams["question-id"];
        questionSetId = env.request.formParams["question-set-id"];
        var deleteChallengeQResult = deleteQuestion(userUniqueId, questionId, questionSetId);
        data.message = deleteChallengeQResult.message;
        data.success = deleteChallengeQResult.success;
    }

    var getChallengeQResult = getUserQuestions(userUniqueId);

    if (getChallengeQResult.data.length === 0) {
        data.isUserHasQuestions = false;
    } else {
        data.isUserHasQuestions = true;
        data.userQuestions = getChallengeQResult.data;
    }

    var getChallengeQuestionsResult = getChallengeQuestions(userUniqueId);
    data.questionList = getChallengeQuestionsResult.data;
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

function setChallengeAnswer(userUniqueId, answer, questionSetId, questionId) {
    try {
        callOSGiService("org.wso2.is.portal.user.client.api.ChallengeQuestionManagerClientService",
            "setChallengeQuestionForUser", [userUniqueId, questionId, questionSetId, answer]);
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
        createSession(uufUser);
        return {success: true, message: "success"};
    } catch (e) {
        return {success: false, message: 'security.question.error.invalidPassword'};
    }
}
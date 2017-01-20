<<<<<<< HEAD
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

function onRequest(env) {

    var data = {};
    data.success = true;
    var session = getSession();
    var userUniqueId = session.getUser().getUserId();
    var requestMethod = env.request.method;
    var action = env.request.formParams["action"];

    if (requestMethod == "POST" && action == "add-question") {

        // Add question flow.
        var answer = env.request.formParams["question-answer"];
        var ids = env.request.formParams["question_list"];
        var idsArray = ids.split(":");
        var questionSetId = idsArray[0];
        var questionId = idsArray[1];
        setChallengeAnswer(userUniqueId, answer, questionSetId, questionId);
    } else if (requestMethod== "POST" && action == "update-question") {

        // Update question answer flow.
        var oldPassword = env.request.formParams["old-password"];
        var newAnswer = env.request.formParams["new-answer"];
        questionId = env.request.formParams["question-id"];
        questionSetId = env.request.formParams["question-set-id"];
        var username = session.getUser().getUsername();
        var domain = session.getUser().getDomainName();
        var result = authenticate(username, oldPassword, domain);
        if (result.success) {
            setChallengeAnswer(userUniqueId, newAnswer, questionSetId, questionId);
        } else {
            data.success = result.success;
            data.message = result.message;
        }
    } else if (requestMethod == "POST" && action == "delete-question") {

        // Delete question flow.
        questionId = env.request.formParams["question-id"];
        questionSetId = env.request.formParams["question-set-id"];

        result = deleteQuestion(userUniqueId, questionId, questionSetId);

        data.message = result.message;
        data.success = result.success;
    }

    result = getUserQuestions(userUniqueId);

    if (result.data.length === 0) {
        data.isUserHasQuestions = false;
    } else {
        data.isUserHasQuestions = true;
        data.userQuestions = result.data;
    }

    data.questionList = getChallengeQuestions(userUniqueId).data;

    return data;
}

function getUserQuestions(userUniqueId) {

    var result = {};
    result.success = true;
    result.message = "";

    try {
        var challengeQuestions = callOSGiService("org.wso2.is.portal.user.client.api.ChallengeQuestionManagerClientService",
            "getAllChallengeQuestionsForUser", [userUniqueId]);
    } catch (e) {
        result.success = false;
        result.message = e.message;
        return result;
    }

    result.data = challengeQuestions;
    return result;
}

function getChallengeQuestions(userUniqueId) {

    var result = {};
    result.success = true;
    result.message = "";

    try {
        var challengeQuestions = callOSGiService("org.wso2.is.portal.user.client.api.ChallengeQuestionManagerClientService",
            "getChallengeQuestionList", [userUniqueId]);
    } catch (e) {
        result.success = false;
        result.message = e.message;
    }

    result.data = challengeQuestions;
    return result;
}

function setChallengeAnswer(userUniqueId, answer, questionSetId, questionId) {

    var result = {};
    result.success = true;
    result.message = "";

    try {
        callOSGiService("org.wso2.is.portal.user.client.api.ChallengeQuestionManagerClientService",
            "setChallengeQuestionForUser", [userUniqueId, questionId, questionSetId, answer]);
    } catch (e) {
        result.message = e.message;
        result.success = false;
    }

    return result;
}

function deleteQuestion(userUniqueId, questionId, questionSetId) {

    var result = {};
    result.success = true;
    result.message = "";

    try {
        callOSGiService("org.wso2.is.portal.user.client.api.ChallengeQuestionManagerClientService",
            "deleteChallengeQuestionForUser", [userUniqueId, questionId, questionSetId]);
    } catch (e) {
        result.message = e.message;
        result.success = false;
    }

    return result;
}

function authenticate(username, password, domain) {
    try {
        var passwordChar = Java.to(password.split(''), 'char[]');
        callOSGiService("org.wso2.is.portal.user.client.api.IdentityStoreClientService",
            "authenticate", [username, passwordChar, domain]);
        return {success: true, message: ""};
    } catch (e) {
        var message = e.message;
        var cause = e.getCause();
        if (cause != null) {
            // The exceptions thrown by the actual osgi service method is wrapped inside a InvocationTargetException.
            if (cause instanceof java.lang.reflect.InvocationTargetException) {
                message = cause.getTargetException().message;
            }
        }
        return {success: false, message: message};
    }
=======
function onRequest(env) {

    var data = {};

    var session = getSession();
    var userUniqueId = session.getUser().getUserId();

    var requestMethod = env.request.method;
    var action = env.params.action;

    if (requestMethod == "POST" && action == "add-question") {
        // Add question flow.
        var answer = env.params.answer;
        var questionId = env.params.questionId;
        var question = env.params.question;

        var locale = env.params.locale;

        setChallengeAnswer(userUniqueId, answer, questionSetId, questionId, question, locale)
    } else if (requestMethod== "POST" && action == "update-question") {
        // Update question answer flow.
    }

    data.questionList = getChallengeQuestions();
    data.userQuestions= getChallengeQuestions(); // getUserQuestions(userUniqueId)

    return data;
}

function getUserQuestions(userUniqueId) {

    var challengeQuestions = callOSGiService("org.wso2.is.portal.user.client.api.ChallengeQuestionManagerClientService",
        "getAllChallengeQuestionsForUser", [userUniqueId]);

    if (challengeQuestions.length == 0) {
        // TODO: Handle any errors here.
    }

    return challengeQuestions;
}

function getChallengeQuestions() {

    var challengeQuestions = callOSGiService("org.wso2.is.portal.user.client.api.ChallengeQuestionManagerClientService",
        "getChallengeQuestionList", []);

    if (challengeQuestions.length == 0) {
        // TODO: Handle any errors here.
    }

    return challengeQuestions;
<<<<<<< HEAD
>>>>>>> 966974f... Added the front end functionality for the security questions page.
=======
}

function setChallengeAnswer(userUniqueId, answer, questionSetId, questionId, question, locale) {

    var ChallengeQuestion = Java.type("org.wso2.carbon.identity.recovery.model.ChallengeQuestion");
    var challengeQuestion = new ChallengeQuestion(questionSetId, questionId, question, locale);

    callOSGiService("org.wso2.is.portal.user.client.api.ChallengeQuestionManagerClientService",
        "setChallengeQuestionForUser", [userUniqueId, challengeQuestion, answer]);
}

function authenticate(username, password) {
    try {
        var passwordChar = Java.to(password.split(''), 'char[]');
        var uufUser = callOSGiService("org.wso2.is.portal.user.client.api.IdentityStoreClientService",
            "authenticate", [username, passwordChar]);

        createSession(uufUser);
        return {success: true, message: "success"}
    } catch (e) {
        var message = e.message;
        var cause = e.getCause();
        if (cause != null) {
            //the exceptions thrown by the actual osgi service method is wrapped inside a InvocationTargetException.
            if (cause instanceof java.lang.reflect.InvocationTargetException) {
                message = cause.getTargetException().message;
            }
        }

        return {success: false, message: message};
    }
>>>>>>> 005c948... Added the challenge question related dependencies and osgi test dependencies.
}
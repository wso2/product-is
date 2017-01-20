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

$(document).ready(function () {

    if ($("#list-questions").length === 0) {
        $("#add-question").show();
    }
});

function editQuestion(questionId, questionSetId, questionText) {
=======
$(document).ready(function () {
});

<<<<<<< HEAD
function editQuestion() {
>>>>>>> 966974f... Added the front end functionality for the security questions page.
=======
function editQuestion(question) {
>>>>>>> 005c948... Added the challenge question related dependencies and osgi test dependencies.

    $("#add-question").hide();
    $("#list-questions").hide();
    $("#edit-question").show();
<<<<<<< HEAD
<<<<<<< HEAD

    $("#question-id").val(questionId);
    $("#question-set-id").val(questionSetId);

    $("#current-question").html(questionText);
=======
>>>>>>> 966974f... Added the front end functionality for the security questions page.
=======

    $("#current-question").html(question.question_text);
    $("#answer").html(question.answer);
>>>>>>> 005c948... Added the challenge question related dependencies and osgi test dependencies.
}

function updateQuestion() {
    $("#add-question").hide();
    $("#list-questions").show();
    $("#edit-question").hide();
}

<<<<<<< HEAD
function deleteQuestion(questionId, questionSetId) {

    var deleteModal = $('#deleteModal');

    deleteModal.modal();
    deleteModal.find('.btn-primary').click(function postDeleteData() {

        var formElement = document.createElement("form");
        formElement.setAttribute("method", "post");
        formElement.setAttribute("action", window.location.href);

        var actionElement = document.createElement("input");
        actionElement.setAttribute("type", "hidden");
        actionElement.setAttribute("name", "action");
        actionElement.setAttribute("value", "delete-question");

        var questionIdElement = document.createElement("input");
        questionIdElement.setAttribute("type", "hidden");
        questionIdElement.setAttribute("name", "question-id");
        questionIdElement.setAttribute("value", questionId);

        var questionSetIdElement = document.createElement("input");
        questionSetIdElement.setAttribute("type", "hidden");
        questionSetIdElement.setAttribute("name", "question-set-id");
        questionSetIdElement.setAttribute("value", questionSetId);

        formElement.appendChild(actionElement);
        formElement.appendChild(questionIdElement);
        formElement.appendChild(questionSetIdElement);

        document.body.appendChild(formElement);
        formElement.submit();
    });
=======
function deleteQuestion() {
    // TODO:
>>>>>>> 966974f... Added the front end functionality for the security questions page.
}

function addQuestion() {
    $("#add-question").show();
    $("#create-question").hide();
<<<<<<< HEAD
}

<<<<<<< HEAD
function goBack() {

    if ($("#list-questions").length === 0) {
        $("#add-question").show();
    } else {
        $("#list-questions").show();
    }

    $("#edit-question").hide();
=======
function saveQuestion() {
    $("#add-question").hide();
    $("#list-questions").show();
    $("#edit-question").hide();
    $("#create-question").show();
>>>>>>> 966974f... Added the front end functionality for the security questions page.
=======
>>>>>>> 005c948... Added the challenge question related dependencies and osgi test dependencies.
}
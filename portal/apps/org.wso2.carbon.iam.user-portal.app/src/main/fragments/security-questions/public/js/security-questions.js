$(document).ready(function () {
});

function editQuestion(question) {

    $("#add-question").hide();
    $("#list-questions").hide();
    $("#edit-question").show();

    $("#current-question").html(question.question_text);
    $("#answer").html(question.answer);
}

function updateQuestion() {
    $("#add-question").hide();
    $("#list-questions").show();
    $("#edit-question").hide();
}

function deleteQuestion() {
    // TODO:
}

function addQuestion() {
    $("#add-question").show();
    $("#create-question").hide();
}
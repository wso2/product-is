$(document).ready(function () {
});

function editQuestion() {

    $("#add-question").hide();
    $("#list-questions").hide();
    $("#edit-question").show();
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

function saveQuestion() {
    $("#add-question").hide();
    $("#list-questions").show();
    $("#edit-question").hide();
    $("#create-question").show();
}
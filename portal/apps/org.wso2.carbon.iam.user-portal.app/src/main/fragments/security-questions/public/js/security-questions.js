$(document).ready(function () {
    if ($("#list-questions").length === 0) {
        $("#add-question").show();
    }
});

function editQuestion(questionId, questionSetId, questionText) {

    $("#add-question").hide();
    $("#list-questions").hide();
    $("#edit-question").show();

    $("#question-id").val(questionId);
    $("#question-set-id").val(questionSetId);

    $("#current-question").html(questionText);
}

function updateQuestion() {
    $("#add-question").hide();
    $("#list-questions").show();
    $("#edit-question").hide();
}

function deleteQuestion(questionId) {

    var data = {};

    data.action = "delete-question";
    data.questionId = questionId;

    $.ajax({
        type: "POST",
        url: window.location.href,
        data: data,
        success: function () {

        }
    });
}

function addQuestion() {
    $("#add-question").show();
    $("#create-question").hide();
}
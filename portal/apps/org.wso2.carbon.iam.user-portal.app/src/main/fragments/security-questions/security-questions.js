function onRequest(env) {

    return {
        "user_questions": [
            {"question_text": "Security question one ?"},
            {"question_text": "Security question two ?"}
        ],

        "question_list": [
            {"question_text": "Security question one ?", "question_value": "q1"},
            {"question_text": "Security question two ?", "question_value": "q1"}
        ]
    };
}

function getChallangeQuestions() {

    var challengeQuestions = callOSGiService("org.wso2.is.portal.user.client.api.ChallengeQuestionManagerClientService",
        "getChallengeQuestionList", []);

    if (challengeQuestions.length == 0) {
        // TODO: Handle any errors here.
    }

    return challengeQuestions;
}
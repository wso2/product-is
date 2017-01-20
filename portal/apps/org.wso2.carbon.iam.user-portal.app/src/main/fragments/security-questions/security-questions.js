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
}
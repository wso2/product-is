package org.wso2.is.portal.user.client.association.bean;

/**
 * User's challenge questions
 */
public class ChallengeQuestion {
    private int questionId;
    private String answer;

    public int getQuestionId() {
        return questionId;
    }

    public void setQuestionId(int questionId) {
        this.questionId = questionId;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }
}

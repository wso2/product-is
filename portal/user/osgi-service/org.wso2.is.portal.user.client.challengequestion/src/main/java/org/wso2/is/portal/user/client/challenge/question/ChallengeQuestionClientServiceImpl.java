package org.wso2.is.portal.user.client.challenge.question;

import org.wso2.is.portal.user.client.challenge.question.bean.ChallengeQuestion;

import java.util.Collection;

/**
 *Default implementation of Challenge Question Client Service.
 */
public class ChallengeQuestionClientServiceImpl implements ChallengeQuestionClientService{
    @Override
    public Collection<String> getChallengeQuestions() {
        return null;
    }

    @Override
    public void updateChallengeQuestion(ChallengeQuestion question) {

    }

    @Override
    public void updateChallengeQuestions(Collection<ChallengeQuestion> questions) {

    }
}

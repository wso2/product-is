package org.wso2.is.portal.user.client.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.identity.recovery.ChallengeQuestionManager;
import org.wso2.carbon.identity.recovery.IdentityRecoveryException;
import org.wso2.carbon.identity.recovery.IdentityRecoveryServerException;
import org.wso2.carbon.identity.recovery.model.ChallengeQuestion;
import org.wso2.is.portal.user.client.api.exception.UserPortalUIException;

import java.util.List;

/**
 * Service implementation of the challenge question manager.
 */
//TODO uncomment
//@Component(
//        name = "org.wso2.is.portal.user.client.api.ChallengeQuestionManagerClientServiceImpl",
//        service = ChallengeQuestionManagerClientService.class,
//        immediate = true)
public class ChallengeQuestionManagerClientServiceImpl implements ChallengeQuestionManagerClientService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChallengeQuestionManagerClientServiceImpl.class);

    private ChallengeQuestionManager challengeQuestionManager;

//TODO uncomment
//    @Reference(
//            name = "challengeQuestionManager",
//            service = ChallengeQuestionManager.class,
//            cardinality = ReferenceCardinality.OPTIONAL,
//            policy = ReferencePolicy.DYNAMIC,
//            unbind = "unSetChallangeQuestionManager")
//    protected void setChallangeQuestionManager(ChallengeQuestionManager challangeQuestionManager) {
//
//        UserPortalClientApiDataHolder.getInstance().setChallengeQuestionManager(challangeQuestionManager);
//    }
//
//    protected void unSetChallangeQuestionManager(ChallengeQuestionManager challangeQuestionManager) {
//    }

    @Override
    public List<ChallengeQuestion> getChallengeQuestionList() throws UserPortalUIException {

        List<ChallengeQuestion> challengeQuestions = null;
        try {
            challengeQuestions = challengeQuestionManager.getAllChallengeQuestions(null);
        } catch (IdentityRecoveryServerException e) {
            String error = "Failed to get the challenge question list.";
            LOGGER.error(error, e);
            throw new UserPortalUIException(error);
        }
        return challengeQuestions;
    }

    @Override
    public List<ChallengeQuestion> getAllChallengeQuestionsForUser(String userUniqueId)
            throws UserPortalUIException {

        List<ChallengeQuestion> challengeQuestionsForUser = null;
        try {
            challengeQuestionsForUser = challengeQuestionManager.getAllChallengeQuestionsForUser(null, null);
        } catch (IdentityRecoveryException e) {
            String error = "Failed to get the challenge questions for the user.";
            LOGGER.error(error, e);
            throw new UserPortalUIException(error);
        }
        return challengeQuestionsForUser;
    }
}


package org.wso2.is.portal.user.client.api;

import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.identity.mgt.exception.IdentityStoreException;
import org.wso2.carbon.identity.mgt.exception.UserNotFoundException;
import org.wso2.carbon.identity.recovery.ChallengeQuestionManager;
import org.wso2.carbon.identity.recovery.IdentityRecoveryException;
import org.wso2.carbon.identity.recovery.IdentityRecoveryServerException;
import org.wso2.carbon.identity.recovery.model.ChallengeQuestion;

import java.util.List;

/**
 * Service implementation of the challenge question manager.
 */
//TODO uncomment
//@Component(
//        name = "org.wso2.is.portal.user.client.api.ChallengeQuestionManagerServiceImpl",
//        service = ChallengeQuestionManagerService.class,
//        immediate = true)
public class ChallengeQuestionManagerServiceImpl implements ChallengeQuestionManagerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChallengeQuestionManagerServiceImpl.class);

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
    public List<ChallengeQuestion> getChallengeQuestionList() throws IdentityRecoveryServerException {

        return challengeQuestionManager.getAllChallengeQuestions(null);
    }

    @Override
    public List<ChallengeQuestion> getAllChallengeQuestionsForUser(String userUniqueId)
            throws IdentityStoreException, UserNotFoundException, IdentityRecoveryException {

        return challengeQuestionManager.getAllChallengeQuestionsForUser(null, null);
    }
}


package org.wso2.is.portal.user.client.api;

import org.wso2.carbon.identity.mgt.User;
import org.wso2.carbon.identity.mgt.exception.IdentityStoreException;
import org.wso2.carbon.identity.mgt.exception.UserNotFoundException;
import org.wso2.carbon.identity.recovery.ChallengeQuestionManager;
import org.wso2.carbon.identity.recovery.IdentityRecoveryException;
import org.wso2.carbon.identity.recovery.IdentityRecoveryServerException;
import org.wso2.carbon.identity.recovery.model.ChallengeQuestion;
import org.wso2.is.portal.user.client.api.internal.UserPortalClientApiDataHolder;

import java.util.List;

/**
 * Service implementation of the challenge question manager.
 */
public class ChallengeQuestionManagerServiceImpl implements ChallengeQuestionManagerService {

    private ChallengeQuestionManager challengeQuestionManager;

    public ChallengeQuestionManagerServiceImpl() {
        challengeQuestionManager = UserPortalClientApiDataHolder.getInstance().getChallengeQuestionManager();
    }

    @Override
    public List<ChallengeQuestion> getChallengeQuestionList() throws IdentityRecoveryServerException {
        return challengeQuestionManager.getAllChallengeQuestions();
    }

    @Override
    public List<ChallengeQuestion> getAllChallengeQuestionsForUser(String userUniqueId)
            throws IdentityStoreException, UserNotFoundException, IdentityRecoveryException {

        User user = UserPortalClientApiDataHolder.getInstance().getRealmService().getIdentityStore()
                .getUser(userUniqueId);
        return challengeQuestionManager.getAllChallengeQuestionsForUser(user);
    }

}

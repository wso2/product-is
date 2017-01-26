package org.wso2.is.portal.user.client.api;

import org.wso2.carbon.identity.mgt.exception.IdentityStoreException;
import org.wso2.carbon.identity.mgt.exception.UserNotFoundException;
import org.wso2.carbon.identity.recovery.IdentityRecoveryException;
import org.wso2.carbon.identity.recovery.model.ChallengeQuestion;

import java.util.List;

/**
 * Service with operations related to challenge question management.
 */
public interface ChallengeQuestionManagerClientService {

    List<ChallengeQuestion> getChallengeQuestionList() throws IdentityRecoveryException;

    List<ChallengeQuestion> getAllChallengeQuestionsForUser(String userUniqueId)
            throws IdentityStoreException, UserNotFoundException, IdentityRecoveryException;

    void setChallengeQuestionForUser(String userUniqueId, String questionId, String questionSetId, String answer)
            throws IdentityStoreException, UserNotFoundException, IdentityRecoveryException;

    void deleteChallengeQuestionForUser(String userUniqueId, String questionId)
            throws IdentityRecoveryException, IdentityStoreException, UserNotFoundException;
}

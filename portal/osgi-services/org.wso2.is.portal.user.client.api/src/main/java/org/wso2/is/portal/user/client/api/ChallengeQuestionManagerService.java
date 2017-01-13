package org.wso2.is.portal.user.client.api;

import org.wso2.carbon.identity.mgt.exception.IdentityStoreException;
import org.wso2.carbon.identity.mgt.exception.UserNotFoundException;
import org.wso2.carbon.identity.recovery.IdentityRecoveryException;
import org.wso2.carbon.identity.recovery.IdentityRecoveryServerException;
import org.wso2.carbon.identity.recovery.model.ChallengeQuestion;

import java.util.List;

/**
 * Service with operations related to challenge question management.
 */
public interface ChallengeQuestionManagerService {

    List<ChallengeQuestion> getChallengeQuestionList() throws IdentityRecoveryServerException, IdentityRecoveryException;

    List<ChallengeQuestion> getAllChallengeQuestionsForUser(String userUniqueId)
            throws IdentityStoreException, UserNotFoundException, IdentityRecoveryException;
}

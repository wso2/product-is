/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.is.portal.user.client.api;

import org.wso2.carbon.identity.mgt.exception.IdentityStoreException;
import org.wso2.carbon.identity.mgt.exception.UserNotFoundException;
import org.wso2.carbon.identity.recovery.IdentityRecoveryException;
import org.wso2.carbon.identity.recovery.model.ChallengeQuestion;
import org.wso2.carbon.identity.recovery.model.UserChallengeAnswer;

import java.util.List;

/**
 * Service with operations related to challenge question management.
 */
public interface ChallengeQuestionManagerClientService {

    /**
     * Get all registered challenge questions
     * @return registered challenge questions
     * @throws IdentityRecoveryException
     */
    List<ChallengeQuestion> getAllChallengeQuestions() throws IdentityRecoveryException;

    /**
     * Get all of the available challenge questions for user.
     * @param userUniqueId User's unique ID.
     * @return List of challenge questions.
     * @throws IdentityRecoveryException Exception in recovery component.
     * @throws IdentityStoreException Exception in identity management component.
     * @throws UserNotFoundException User not found.
     */
    List<ChallengeQuestion> getChallengeQuestionList(String userUniqueId) throws IdentityRecoveryException,
            IdentityStoreException, UserNotFoundException;

    /**
     * Get all challenge questions answered by the specific user.
     * @param userUniqueId User's unique ID.
     * @return List of challenge questions.
     * @throws IdentityStoreException Exception in identity management component.
     * @throws UserNotFoundException User not found.
     * @throws IdentityRecoveryException Exception in recovery component.
     */
    List<ChallengeQuestion> getAllChallengeQuestionsForUser(String userUniqueId)
            throws IdentityStoreException, UserNotFoundException, IdentityRecoveryException;

    /**
     * Set challenge question for user.
     * @param userUniqueId User's unique ID.
     * @param questionId Question id.
     * @param questionSetId Question set ID.
     * @param answer User's answer.
     * @throws IdentityStoreException Exception in identity management component.
     * @throws UserNotFoundException User not found.
     * @throws IdentityRecoveryException Exception in recovery component.
     */
    void setChallengeQuestionForUser(String userUniqueId, String questionId, String questionSetId, String answer)
            throws IdentityStoreException, UserNotFoundException, IdentityRecoveryException;

    /**
     * Delete the specific question that user answered.
     * @param userUniqueId User's unique id.
     * @param questionId Question id.
     * @param questionSetId Question set id.
     * @throws IdentityRecoveryException Exception in recovery component.
     * @throws IdentityStoreException Exception in identity management component.
     * @throws UserNotFoundException User not found.
     */
    void deleteChallengeQuestionForUser(String userUniqueId, String questionId, String questionSetId)
            throws IdentityRecoveryException, IdentityStoreException, UserNotFoundException;

    List<UserChallengeAnswer> getChallengeAnswersOfUser(String userUniqueId) throws IdentityRecoveryException,
            IdentityStoreException, UserNotFoundException;
}

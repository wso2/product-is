/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.is.portal.user.client.api;


import org.wso2.carbon.identity.recovery.IdentityRecoveryException;
import org.wso2.carbon.identity.recovery.bean.ChallengeQuestionsResponse;
import org.wso2.carbon.identity.recovery.mapping.RecoveryConfig;
import org.wso2.is.portal.user.client.api.exception.UserPortalUIException;

import java.util.Map;

/**
 * Represent Recovery management service
 * A placeholder service for now
 */
public interface RecoveryMgtService {

    /**
     * check whether password recovery via notification method is enabled
     *
     * @return
     * @throws UserPortalUIException
     */
    boolean isNotificationBasedPasswordRecoveryEnabled() throws UserPortalUIException;

    /**
     * send notification to the user with a url to recover password
     *
     * @throws UserPortalUIException
     */
    void setPasswordRecoveryNotification(String userUniqueId) throws UserPortalUIException;

    /**
     *
     * @param code
     * @param password
     * @throws UserPortalUIException
     */
    String updatePassword(String code, char[] password) throws UserPortalUIException;

    /**
     * get recovery config bean object
     *
     * @return
     * @throws UserPortalUIException
     */
    RecoveryConfig getRecoveryConfigs() throws UserPortalUIException;

    /**
     * get recovery config bean object
     *
     * @return
     * @throws UserPortalUIException
     */
    ChallengeQuestionsResponse getUserChallengeQuestionAtOnce(String userUniqueId) throws UserPortalUIException;

    /**
     * get recovery config bean object
     *
     * @return
     * @throws UserPortalUIException
     */
    ChallengeQuestionsResponse verifyUserChallengeAnswers(String code,
                                                          Map<String, String> answers) throws UserPortalUIException;

    /**
     *  Check whether a username is available bu given set of user claims.
     *
     * @param userClaims user claims.
     * @return
     * @throws IdentityRecoveryException IdentityRecoveryExceptions
     */
    boolean verifyUsername(Map<String, String> userClaims) throws IdentityRecoveryException;

}

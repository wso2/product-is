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


import org.apache.commons.lang3.StringUtils;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.identity.mgt.RealmService;
import org.wso2.carbon.identity.mgt.User;
import org.wso2.carbon.identity.mgt.claim.Claim;
import org.wso2.carbon.identity.mgt.exception.IdentityStoreException;
import org.wso2.carbon.identity.mgt.exception.UserNotFoundException;
import org.wso2.carbon.identity.mgt.impl.util.IdentityMgtConstants;
import org.wso2.carbon.identity.recovery.IdentityRecoveryClientException;
import org.wso2.carbon.identity.recovery.IdentityRecoveryConstants;
import org.wso2.carbon.identity.recovery.IdentityRecoveryException;
import org.wso2.carbon.identity.recovery.bean.ChallengeQuestionsResponse;
import org.wso2.carbon.identity.recovery.mapping.RecoveryConfig;
import org.wso2.carbon.identity.recovery.model.ChallengeQuestion;
import org.wso2.carbon.identity.recovery.model.UserChallengeAnswer;
import org.wso2.carbon.identity.recovery.password.NotificationPasswordRecoveryManager;
import org.wso2.carbon.identity.recovery.password.SecurityQuestionPasswordRecoveryManager;
import org.wso2.carbon.identity.recovery.username.NotificationUsernameRecoveryManager;
import org.wso2.is.portal.user.client.api.exception.UserPortalUIException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Represent Recovery management service implementation
 */
@Component(
        name = "org.wso2.is.portal.user.client.api.RecoveryMgtService",
        service = RecoveryMgtService.class,
        immediate = true)
public class RecoveryMgtServiceImpl implements RecoveryMgtService {

    private static final Logger log = LoggerFactory.getLogger(RecoveryMgtService.class);
    private RecoveryConfig recoveryConfig;
    private RealmService realmService;
    private NotificationPasswordRecoveryManager notificationPasswordRecoveryManager;
    private SecurityQuestionPasswordRecoveryManager securityQuestionPasswordRecoveryManager;
    private NotificationUsernameRecoveryManager notificationUsernameRecoveryManager;


    @Activate
    protected void start(final BundleContext bundleContext) {
        log.info("Registered service implementation" + RecoveryMgtService.class); //todo
        recoveryConfig = new RecoveryConfig();
    }

    @Reference(
            name = "realmService",
            service = RealmService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetRealmService")
    protected void setRealmService(RealmService realmService) {

        this.realmService = realmService;
    }

    protected void unsetRealmService(RealmService realmService) {

        this.realmService = null;
    }

    @Reference(
            name = "securityQuestionPasswordRecoveryManager",
            service = SecurityQuestionPasswordRecoveryManager.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetSecurityQuestionPasswordRecoveryManager")
    protected void setRealmService(SecurityQuestionPasswordRecoveryManager securityQuestionPasswordRecoveryManager) {

        this.securityQuestionPasswordRecoveryManager = securityQuestionPasswordRecoveryManager;
    }

    protected void unsetSecurityQuestionPasswordRecoveryManager(SecurityQuestionPasswordRecoveryManager
                                                                        securityQuestionPasswordRecoveryManager) {

        this.securityQuestionPasswordRecoveryManager = null;
    }

    @Reference(
            name = "NotificationPasswordRecoveryManager",
            service = NotificationPasswordRecoveryManager.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetNotificationPasswordRecoveryManagerService")
    protected void setNotificationPasswordRecoveryManagerService(
            NotificationPasswordRecoveryManager notificationPasswordRecoveryManager) {

        this.notificationPasswordRecoveryManager = notificationPasswordRecoveryManager;
    }

    protected void unsetNotificationPasswordRecoveryManagerService(
            NotificationPasswordRecoveryManager notificationPasswordRecoveryManager) {
        this.notificationPasswordRecoveryManager = null;
    }

    @Override
    public boolean isNotificationBasedPasswordRecoveryEnabled() throws UserPortalUIException {
        return true;
    }

    @Override
    public RecoveryConfig getRecoveryConfigs() {
        return recoveryConfig;
    }

    @Override
    public ChallengeQuestionsResponse getUserChallengeQuestionAtOnce(String userUniqueId) throws UserPortalUIException {
        if (securityQuestionPasswordRecoveryManager == null || realmService == null || recoveryConfig == null) {
            throw new UserPortalUIException("Challenge question recovery manager or Realm service or recovery " +
                    "configuration is not available.");
        }
        User user;
        try {
            user = realmService.getIdentityStore().getUser(userUniqueId);
            if (recoveryConfig.getPassword().getSecurityQuestion().isValidateOneByOne()) {
                return securityQuestionPasswordRecoveryManager.initiateUserChallengeQuestion(user);
            } else {
                return securityQuestionPasswordRecoveryManager.initiateUserChallengeQuestionAtOnce(user);
            }
        } catch (IdentityStoreException e) {
            log.error("Error While retrieving user for userID: " + userUniqueId, e);
            throw new UserPortalUIException("Error While retrieving user for userID: " + userUniqueId);
        } catch (UserNotFoundException e) {
            log.error("UserNotFoundFor userID: " + userUniqueId, e);
            throw new UserPortalUIException("UserNotFoundFor userID: " + userUniqueId);
        } catch (IdentityRecoveryException e) {
            log.error("Error while starting challenge question based recovery for userID: " + userUniqueId, e);
            String errorCode = !StringUtils.isEmpty(e.getErrorCode()) ? e.getErrorCode() : IdentityRecoveryConstants
                    .ErrorMessages.ERROR_CODE_UNEXPECTED.getCode();
            ChallengeQuestionsResponse challengeQuestionResponse = new ChallengeQuestionsResponse(Collections
                    .EMPTY_LIST);
            challengeQuestionResponse.setStatus(errorCode);
            return challengeQuestionResponse;
        }
    }

    @Override
    public ChallengeQuestionsResponse verifyUserChallengeAnswers(String code, Map<String,
            String> answers) throws UserPortalUIException {
        if (securityQuestionPasswordRecoveryManager == null || realmService == null) {
            throw new UserPortalUIException("Challenge question recovery manager or Realm service is not available.");
        }

        List<UserChallengeAnswer> userChallengeAnswer = new ArrayList<>();

        for (Map.Entry<String, String> answer : answers.entrySet()) {
            String answerVal = answer.getValue();
            String questionSetID = answer.getKey();
            switch (questionSetID) {
                case "question-recovery-code":
                    code = answerVal;
                    break;
                case "recover-option":
                    break;
                default:
                    ChallengeQuestion question = new ChallengeQuestion(questionSetID, "");
                    userChallengeAnswer.add(new UserChallengeAnswer(question, answerVal));
                    break;
            }
        }
        try {
            return securityQuestionPasswordRecoveryManager.validateUserChallengeQuestions(userChallengeAnswer, code);
        } catch (IdentityRecoveryException e) {
            log.error("Error while validating recovery question answers for code " + code, e);
            throw new UserPortalUIException("Error while validating recovery question answers for code " + code);
        }
//        User user;
//        try {
//            user = realmService.getIdentityStore().getUser(userUniqueId);
//            return securityQuestionPasswordRecoveryManager.initiateUserChallengeQuestionAtOnce(user);
//        } catch (IdentityStoreException e) {
//            log.error("Error While retrieving user for userID: " + userUniqueId, e);
//            throw new UserPortalUIException("Error While retrieving user for userID: " + userUniqueId);
//        } catch (UserNotFoundException e) {
//            log.error("UserNotFoundFor userID: " + userUniqueId, e);
//            throw new UserPortalUIException("UserNotFoundFor userID: " + userUniqueId);
//        } catch (IdentityRecoveryException e) {
//            log.error("Error while getting recovery questions for userID: " + userUniqueId, e);
//            throw new UserPortalUIException("Error while getting recovery questions for userID: " + userUniqueId);
//        }
    }

    @Override
    public void setPasswordRecoveryNotification(String userUniqueId) throws UserPortalUIException {
        try {
            getNotificationPasswordRecoveryManager().sendRecoveryNotification(userUniqueId, true);
        } catch (IdentityRecoveryException e) {
            String msg = "Error while sending password recovery notification";
            log.error(msg, e);
            throw new UserPortalUIException(msg);
        }
    }

    @Override
    public String updatePassword(String code, char[] password) throws UserPortalUIException {
        try {
            getNotificationPasswordRecoveryManager().updatePassword(code, password);
        } catch (IdentityRecoveryClientException e) {
            return e.getErrorCode();
        } catch (IdentityRecoveryException e) {
            log.error("Error while updating password", e);
            throw new UserPortalUIException("Error while updating password", e);
        }

        return "UPDATED";
    }

    private NotificationPasswordRecoveryManager getNotificationPasswordRecoveryManager() {
        if (this.notificationPasswordRecoveryManager == null) {
            throw new IllegalStateException("Notification password recovery manager is null.");
        }
        return this.notificationPasswordRecoveryManager;
    }

    @Reference(
            name = "notificationUsernameRecoveryManager",
            service = NotificationUsernameRecoveryManager.class,
            cardinality = ReferenceCardinality.OPTIONAL,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unSetNotificationUsernameRecoveryManager")
    protected void setNotificationUsernameRecoveryManager(NotificationUsernameRecoveryManager
                                                                  notificationUsernameRecoveryManager) {
        this.notificationUsernameRecoveryManager = notificationUsernameRecoveryManager;
    }

    protected void unSetNotificationUsernameRecoveryManager(NotificationUsernameRecoveryManager
                                                                    notificationUsernameRecoveryManager) {
        this.notificationUsernameRecoveryManager = null;
    }

    @Override
    public boolean verifyUsername(Map<String, String> userClaims) throws IdentityRecoveryException {

        List<Claim> claims = new ArrayList<>();
        for (Map.Entry<String, String> entry : userClaims.entrySet()) {
            // Check whether claim value is empty or not.
            if (entry.getValue().isEmpty()) {
                continue;
            } else {
                Claim claim = new Claim(IdentityMgtConstants.CLAIM_ROOT_DIALECT, entry.getKey(), entry.getValue());
                claims.add(claim);
            }
        }

        return getNotificationUsernameRecoveryManager().verifyUsername(claims);
    }

    public NotificationUsernameRecoveryManager getNotificationUsernameRecoveryManager() {
        return notificationUsernameRecoveryManager;
    }
}

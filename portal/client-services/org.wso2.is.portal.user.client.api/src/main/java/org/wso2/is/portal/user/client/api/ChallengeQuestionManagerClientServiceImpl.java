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
import org.wso2.carbon.identity.mgt.exception.IdentityStoreException;
import org.wso2.carbon.identity.mgt.exception.UserNotFoundException;
import org.wso2.carbon.identity.recovery.ChallengeQuestionManager;
import org.wso2.carbon.identity.recovery.IdentityRecoveryException;
import org.wso2.carbon.identity.recovery.model.ChallengeQuestion;
import org.wso2.carbon.identity.recovery.model.UserChallengeAnswer;
import org.wso2.is.portal.user.client.api.bean.ChallengeQuestionSetEntry;
import org.wso2.is.portal.user.client.api.exception.UserPortalUIException;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service implementation of the challenge question manager.
 */
@Component(
        name = "org.wso2.is.portal.user.client.api.ChallengeQuestionManagerServiceImpl",
        service = ChallengeQuestionManagerClientService.class,
        immediate = true)
public class ChallengeQuestionManagerClientServiceImpl implements ChallengeQuestionManagerClientService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChallengeQuestionManagerClientService.class);

    private ChallengeQuestionManager challengeQuestionManager;
    private RealmService realmService;

    @Activate
    protected void start(BundleContext bundleContext) {

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("ChallengeQuestionManagerClientService activated successfully.");
        }
    }

    @Reference(
            name = "challengeQuestionManager",
            service = ChallengeQuestionManager.class,
            cardinality = ReferenceCardinality.OPTIONAL,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unSetChallengeQuestionManager")
    protected void setChallangeQuestionManager(ChallengeQuestionManager challangeQuestionManager) {
        this.challengeQuestionManager = challangeQuestionManager;
    }

    protected void unSetChallengeQuestionManager(ChallengeQuestionManager challangeQuestionManager) {
        this.challengeQuestionManager = null;
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

    @Override
    public List<ChallengeQuestion> getAllChallengeQuestions() throws IdentityRecoveryException {
        if (challengeQuestionManager == null) {
            throw new IdentityRecoveryException("Challenge question manager is not available.");
        }
        return challengeQuestionManager.getAllChallengeQuestions();
    }

    @Override
    public List<ChallengeQuestionSetEntry> getChallengeQuestionList(String userUniqueId) throws
            IdentityRecoveryException,
            IdentityStoreException, UserNotFoundException {

        List<ChallengeQuestionSetEntry> challengeQuestionSetEntryList = new ArrayList<ChallengeQuestionSetEntry>();
        if (challengeQuestionManager == null || realmService == null) {
            throw new IdentityRecoveryException("Challenge question manager or Realm service is not available.");
        }
        User user = realmService.getIdentityStore().getUser(userUniqueId);
        List<ChallengeQuestion> challengeQuestions = challengeQuestionManager.getAllChallengeQuestionsForUser(user);
        Map<String, List<ChallengeQuestion>> groupedChallengeQuestionMap = challengeQuestions.stream()
                .collect(Collectors
                .groupingBy(ChallengeQuestion::getQuestionSetId));
        for (Map.Entry<String, List<ChallengeQuestion>> entry : groupedChallengeQuestionMap.entrySet()) {
            ChallengeQuestionSetEntry challengeQuestionSetEntry = new ChallengeQuestionSetEntry();
            challengeQuestionSetEntry.setChallengeQuestionSetId(encodeChallengeQuestionSetId(entry.getKey()));
            List<ChallengeQuestion> encodedSetIdChallengeQuestionsList = entry.getValue().stream().
                    map(challengeQuestion -> {
                        challengeQuestion.setQuestionSetId(encodeChallengeQuestionSetId(challengeQuestion
                                .getQuestionSetId()));
                        return challengeQuestion;
                    }).collect(Collectors.toList());
            challengeQuestionSetEntry.setChallengeQuestionList(encodedSetIdChallengeQuestionsList);
            challengeQuestionSetEntryList.add(challengeQuestionSetEntry);
        }
        return challengeQuestionSetEntryList;
    }

    @Override
    public List<ChallengeQuestion> getAllChallengeQuestionsForUser(String userUniqueId)
            throws IdentityStoreException, UserNotFoundException, IdentityRecoveryException {

        if (challengeQuestionManager == null || realmService == null) {
            throw new IdentityRecoveryException("Challenge question manager or Realm service is not available.");
        }

        List<UserChallengeAnswer> userChallengeAnswers = challengeQuestionManager
                .getChallengeAnswersOfUser(userUniqueId);

        return userChallengeAnswers
                .stream()
                .map(UserChallengeAnswer::getQuestion).map(challengeQuestion -> {
                    challengeQuestion.setQuestionSetId(new String(Base64.getEncoder().encode(challengeQuestion
                            .getQuestionSetId().getBytes(Charset.forName("UTF-8"))), Charset.forName("UTF-8")));
                    return challengeQuestion;
                })
                .collect(Collectors.toList());
    }

    @Override
    public void setChallengeQuestionForUser(String userUniqueId, String questionId, String questionSetId, String answer,
                                            String actionId)
            throws IdentityStoreException, UserNotFoundException, IdentityRecoveryException {

        if (challengeQuestionManager == null || realmService == null) {
            throw new IdentityRecoveryException("Challenge question manager or Realm service is not available.");
        }

        User user = realmService.getIdentityStore().getUser(userUniqueId);

        List<UserChallengeAnswer> existingAnswers = challengeQuestionManager.getChallengeAnswersOfUser(userUniqueId);

        if (StringUtils.equals(actionId, "challengeQUpdate")) {
            Iterator<UserChallengeAnswer> existingAnswersIterator = existingAnswers.iterator();
            while (existingAnswersIterator.hasNext()) {
                ChallengeQuestion challengeQuestion = existingAnswersIterator.next().getQuestion();
                if (StringUtils.equals(challengeQuestion.getQuestionSetId(),
                        new String(Base64.getDecoder().decode(questionSetId.getBytes(Charset.forName("UTF-8"))),
                                Charset.forName("UTF-8"))) &&
                        StringUtils.equals(challengeQuestion.getQuestionId(), questionId)) {
                    existingAnswersIterator.remove();
                    break;
                }
            }
        }

        List<ChallengeQuestion> challengeQuestions = challengeQuestionManager.getAllChallengeQuestionsForUser(user);
        ChallengeQuestion challengeQuestion = challengeQuestions.stream()
                .filter(question -> StringUtils.equals(question.getQuestionId(), questionId) &&
                        StringUtils.equals(question.getQuestionSetId(),
                                new String(Base64.getDecoder().decode(questionSetId.getBytes(Charset.forName("UTF-8"))),
                                        Charset.forName("UTF-8"))))
                .findFirst()
                .get();

        UserChallengeAnswer userChallengeAnswer = new UserChallengeAnswer(challengeQuestion, answer);
        existingAnswers.add(userChallengeAnswer);

        challengeQuestionManager.setChallengesOfUser(user, existingAnswers);
    }

    @Override
    public void deleteChallengeQuestionForUser(String userUniqueId, String questionId, String questionSetId)
            throws IdentityRecoveryException, IdentityStoreException, UserNotFoundException, UserPortalUIException {

        if (challengeQuestionManager == null || realmService == null) {
            throw new IdentityRecoveryException("Challenge question manager or Realm service is not available.");
        }
        int minNumOfSecurityQuestions = challengeQuestionManager.getMinimumNoOfChallengeQuestionsToAnswer();
        User user = realmService.getIdentityStore().getUser(userUniqueId);

        List<UserChallengeAnswer> existingAnswers = challengeQuestionManager.getChallengeAnswersOfUser(userUniqueId);
        if (minNumOfSecurityQuestions < existingAnswers.size()) {
            existingAnswers.removeIf(answer -> StringUtils.equals(answer.getQuestion().getQuestionId(), questionId) &&
                    StringUtils.equals(answer.getQuestion().getQuestionSetId(),
                            new String(Base64.getDecoder().decode(questionSetId.getBytes(StandardCharsets.UTF_8)),
                                    StandardCharsets.UTF_8)));
            challengeQuestionManager.setChallengesOfUser(user, existingAnswers);
        } else {
            String error = "Cannot delete security question. You need to have at least" +
                    minNumOfSecurityQuestions + "security questions";
            throw new UserPortalUIException(error);
        }
    }

    @Override
    public List<UserChallengeAnswer> getChallengeAnswersOfUser(String userUniqueId) throws IdentityRecoveryException,
            IdentityStoreException, UserNotFoundException {
        if (challengeQuestionManager == null || realmService == null) {
            throw new IdentityRecoveryException("Challenge question manager or Realm service is not available.");
        }

        return challengeQuestionManager.getChallengeAnswersOfUser
                (userUniqueId);
    }

    public int getMinimumNoOfChallengeQuestionsToAnswer() throws IdentityRecoveryException {
        if (challengeQuestionManager == null || realmService == null) {
            throw new IdentityRecoveryException("Challenge question manager or Realm service is not available.");
        }
        return challengeQuestionManager.getMinimumNoOfChallengeQuestionsToAnswer();
    }

    private String encodeChallengeQuestionSetId(String questionSetId) {
        return new String(Base64.getEncoder().encode(questionSetId.
                getBytes(Charset.forName("UTF-8"))), Charset.forName("UTF-8"));
    }

    @Override
    public boolean isQuestionBasedPwdRecoveryEnabled() throws IdentityRecoveryException,
            IdentityStoreException, UserNotFoundException {
        if (challengeQuestionManager == null || realmService == null) {
            throw new IdentityRecoveryException("Challenge question manager or Realm service is not available.");
        }
        return challengeQuestionManager.isQuestionBasedPwdRecoveryEnabledInPortal();
    }
}


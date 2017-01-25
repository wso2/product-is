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

import java.util.Arrays;
import java.util.List;
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

        LOGGER.info("Challenge question manager service activated successfully.");
    }

    @Reference(
            name = "challengeQuestionManager",
            service = ChallengeQuestionManager.class,
            cardinality = ReferenceCardinality.OPTIONAL,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unSetChallangeQuestionManager")
    protected void setChallangeQuestionManager(ChallengeQuestionManager challangeQuestionManager) {
        this.challengeQuestionManager = challangeQuestionManager;
    }

    protected void unSetChallangeQuestionManager(ChallengeQuestionManager challangeQuestionManager) {
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
    public List<ChallengeQuestion> getChallengeQuestionList() throws IdentityRecoveryException {

        if (challengeQuestionManager == null) {
            throw new IdentityRecoveryException("Challenge question manager is not available.");
        }

        return challengeQuestionManager.getAllChallengeQuestions();
    }

    @Override
    public List<ChallengeQuestion> getAllChallengeQuestionsForUser(String userUniqueId)
            throws IdentityStoreException, UserNotFoundException, IdentityRecoveryException {

        if (challengeQuestionManager == null || realmService == null) {
            throw new IdentityRecoveryException("Challenge question manager or Realm service is not available.");
        }

        UserChallengeAnswer [] userChallengeAnswers = challengeQuestionManager
                .getChallengeAnswersOfUser(realmService.getIdentityStore().getUser(userUniqueId));

        return Arrays.stream(userChallengeAnswers)
                .map(answer -> answer.getQuestion())
                .collect(Collectors.toList());
    }

    @Override
    public void setChallengeQuestionForUser(String userUniqueId, String questionId, String questionSetId, String answer)
            throws IdentityStoreException, UserNotFoundException, IdentityRecoveryException {

        if (challengeQuestionManager == null || realmService == null) {
            throw new IdentityRecoveryException("Challenge question manager or Realm service is not available.");
        }

        User user = realmService.getIdentityStore().getUser(userUniqueId);

        List<ChallengeQuestion> challengeQuestions = challengeQuestionManager.getAllChallengeQuestionsForUser(user);
        ChallengeQuestion challengeQuestion = challengeQuestions.stream()
                .filter(question -> StringUtils.equals(question.getQuestionId(), questionId) &&
                        StringUtils.equals(question.getQuestionSetId(), questionSetId))
                .findFirst()
                .get();

        UserChallengeAnswer userChallengeAnswer = new UserChallengeAnswer();

        userChallengeAnswer.setAnswer(answer);
        userChallengeAnswer.setQuestion(challengeQuestion);

        challengeQuestionManager.setChallengesOfUser(user, new UserChallengeAnswer[] { userChallengeAnswer });
    }

    @Override
    public void deleteChallengeQuestionForUser(String userUniqueId, String questionId)
            throws IdentityRecoveryException, IdentityStoreException, UserNotFoundException {

        if (challengeQuestionManager == null || realmService == null) {
            throw new IdentityRecoveryException("Challenge question manager or Realm service is not available.");
        }

        User user = realmService.getIdentityStore().getUser(userUniqueId);

        ChallengeQuestion challengeQuestion = challengeQuestionManager.getAllChallengeQuestionsForUser(user).stream()
                .filter(question -> StringUtils.equals(question.getQuestionId(), questionId))
                .findFirst()
                .get();

        challengeQuestionManager.deleteChallengeQuestions(new ChallengeQuestion[]{challengeQuestion});
    }

    @Override
    public UserChallengeAnswer[] getChallengeAnswersOfUser(String userUniqueId) throws IdentityRecoveryException,
            IdentityStoreException, UserNotFoundException {
        if (challengeQuestionManager == null || realmService == null) {
            throw new IdentityRecoveryException("Challenge question manager or Realm service is not available.");
        }

        return challengeQuestionManager.getChallengeAnswersOfUser
                (realmService.getIdentityStore().getUser(userUniqueId));
    }
}


package org.wso2.is.portal.user.client.api.unit.test;

import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerSuite;
import org.ops4j.pax.exam.testng.listener.PaxExam;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.mgt.exception.IdentityStoreException;
import org.wso2.carbon.identity.mgt.exception.UserNotFoundException;
import org.wso2.carbon.identity.recovery.IdentityRecoveryException;
import org.wso2.carbon.identity.recovery.bean.ChallengeQuestionsResponse;
import org.wso2.carbon.identity.recovery.mapping.RecoveryConfig;
import org.wso2.carbon.identity.recovery.model.ChallengeQuestion;
import org.wso2.carbon.kernel.utils.CarbonServerInfo;
import org.wso2.is.portal.user.client.api.ChallengeQuestionManagerClientService;
import org.wso2.is.portal.user.client.api.IdentityStoreClientService;
import org.wso2.is.portal.user.client.api.RecoveryMgtService;
import org.wso2.is.portal.user.client.api.bean.UUFUser;
import org.wso2.is.portal.user.client.api.exception.UserPortalUIException;
import org.wso2.is.portal.user.client.api.unit.test.util.UserPortalOSGiTestUtils;
import org.wso2.is.portal.user.client.api.unit.test.util.UserPotalOSGiTestConstants;

import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;

import static org.ops4j.pax.exam.CoreOptions.systemProperty;

@Listeners(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
public class RecoveryMgtServiceTest {

    private static List<UUFUser> users = new ArrayList<>();
    private static List<ChallengeQuestion> challengeQuestions = new ArrayList<>();
    private static final Logger LOGGER = LoggerFactory.getLogger(RecoveryMgtServiceTest.class);
    private static ChallengeQuestionsResponse challengeQuestionsResponse;
    private static String answer1 = "Answer1";

    @Inject
    private BundleContext bundleContext;

    @Inject
    private CarbonServerInfo carbonServerInfo;

    @Inject
    private RecoveryMgtService recoveryMgtService;

//    @Inject
//    private IdentityStoreClientService identityStoreClientService;

    @Configuration
    public Option[] createConfiguration() {

        List<Option> optionList = UserPortalOSGiTestUtils.getDefaultSecurityPAXOptions();

        optionList.add(systemProperty("java.security.auth.login.config")
                .value(Paths.get(UserPortalOSGiTestUtils.getCarbonHome(), "conf", "security", "carbon-jaas.config")
                        .toString()));

        return optionList.toArray(new Option[optionList.size()]);
    }

    @Test(groups = "getRecoveryConfig")
    public void testGetRecoveryConfig() throws UserPortalUIException {
        RecoveryConfig config = recoveryMgtService.getRecoveryConfigs();
        Assert.assertEquals(config.getPassword().isEnablePortal(), true, "Failed to retrieve default value for " +
                "password recovery enabling in Portal");
        Assert.assertEquals(config.getPassword().getSecurityQuestion().isEnablePortal(), true,
                "Failed to retrieve default value for password recovery based on security question enabling in Portal");
    }

    @Test(groups = "getRecoveryQuestions", dependsOnGroups = {"getRecoveryConfig"})
    public void getUserChallengeQuestionAtOnce() throws UserPortalUIException, IdentityRecoveryException {
        Map<String, String> userClaims = new HashMap<>();
        Map<String, String> credentials = new HashMap<>();
        userClaims.put(UserPotalOSGiTestConstants.ClaimURIs.USERNAME_CLAIM_URI, "Ayesha");
        userClaims.put(UserPotalOSGiTestConstants.ClaimURIs.FIRST_NAME_CLAIM_URI, "Ayesha");
        userClaims.put(UserPotalOSGiTestConstants.ClaimURIs.LAST_NAME_CLAIM_URI, "Dissanayaka");
        userClaims.put(UserPotalOSGiTestConstants.ClaimURIs.EMAIL_CLAIM_URI, "ayesha@wso2.com");

        credentials.put(UserPotalOSGiTestConstants.PASSWORD_CALLBACK, "password");
        addUser(userClaims, credentials);
        getAllChallengeQuestionsForUser();
        addChallengeQuestionForUser();
        challengeQuestionsResponse = recoveryMgtService.getUserChallengeQuestionAtOnce(users.get(0).getUserId());
        Assert.assertNotNull(challengeQuestionsResponse, "Failed to start challenge question based password recovery " +
                "for the user");
        Assert.assertNotNull(challengeQuestionsResponse.getCode(), "ChallengeQuestions Response does not have a " +
                "confirmation code");
        Assert.assertTrue(challengeQuestionsResponse.getQuestions().size() > 0, "ChallengeQuestions Response does not" +
                " contain questions to be answered");
        Assert.assertEquals(challengeQuestionsResponse.getQuestions().get(0).getQuestion(),
                challengeQuestions.get(0).getQuestion(), "Asked question is not from answered question");

    }

    @Test(groups = "answerRecoveryQuestions", dependsOnGroups = {"getRecoveryQuestions"})
    public void answerChallengeQuestion() throws UserPortalUIException, IdentityRecoveryException {

        challengeQuestionsResponse = startQuestionBasedPasswordRecovery();
        Map<String, String> answers = new HashMap<>();
        answers.put(challengeQuestionsResponse.getQuestions().get(0).getQuestionSetId(), answer1);

        challengeQuestionsResponse = recoveryMgtService.verifyUserChallengeAnswers(challengeQuestionsResponse.getCode
                (), answers);
        Assert.assertNotNull(challengeQuestionsResponse, "Failed to answer challenge question for password recovery");
        Assert.assertNotNull(challengeQuestionsResponse.getCode(), "ChallengeQuestions Response does not have a " +
                "confirmation code");
        Assert.assertNotNull(challengeQuestionsResponse.getStatus(), "ChallengeQuestions Response does not" +
                " contain status");
        Assert.assertEquals(challengeQuestionsResponse.getStatus(), "COMPLETE", "Challenge Question answer not " +
                "validated");

    }

    @Test(groups = "answerRecoveryQuestions", dependsOnGroups = {"getRecoveryQuestions"})
    public void answerChallengeQuestionWrongAnswer() throws UserPortalUIException, IdentityRecoveryException {

        ChallengeQuestionsResponse challengeQuestionsResponse1 = startQuestionBasedPasswordRecovery();
        Map<String, String> answers = new HashMap<>();
        answers.put(challengeQuestionsResponse1.getQuestions().get(0).getQuestionSetId(), answer1 + "AB12");

        challengeQuestionsResponse = recoveryMgtService.verifyUserChallengeAnswers(challengeQuestionsResponse1.getCode
                (), answers);
        Assert.assertNotNull(challengeQuestionsResponse, "Failed to answer challenge question for password recovery");
        Assert.assertNotNull(challengeQuestionsResponse.getCode(), "ChallengeQuestions Response does not have a " +
                "confirmation code");
        Assert.assertNotNull(challengeQuestionsResponse.getStatus(), "ChallengeQuestions Response does not" +
                " contain status");
        Assert.assertEquals(challengeQuestionsResponse.getStatus(), "20008", "Challenge Question answer hasn't been " +
                "invalid");
        Assert.assertEquals(challengeQuestionsResponse1.getQuestions().get(0).getQuestion(),
                challengeQuestionsResponse.getQuestions().get(0).getQuestion(), "Question after failed attempt " +
                        "is not same as the question in previous attempt ");
        Assert.assertEquals(challengeQuestionsResponse1.getCode(),
                challengeQuestionsResponse.getCode(), "Recovery code after failed attempt " +
                        "is not same as the code in previous attempt ");

    }

    @Test(groups = "usernameRecovery")
    public void verifyUsernameWithNoClaims() throws UserPortalUIException, IdentityRecoveryException {
        Map<String, String> testUserClaims = new HashMap<>();
        boolean result = recoveryMgtService.verifyUsername(testUserClaims);
        Assert.assertEquals(result, false, "There should not be any user recovered.");
    }

/*    @Test(groups = "usernameRecovery")
    public void verifyUsernameWithOneClaims() throws UserPortalUIException, IdentityRecoveryException {

        Map<String, String> userClaims = new HashMap<>();
        Map<String, String> credentials = new HashMap<>();
        userClaims.put(UserPotalOSGiTestConstants.ClaimURIs.USERNAME_CLAIM_URI, "dinali1234");
        userClaims.put(UserPotalOSGiTestConstants.ClaimURIs.FIRST_NAME_CLAIM_URI, "dinali");
        userClaims.put(UserPotalOSGiTestConstants.ClaimURIs.LAST_NAME_CLAIM_URI, "dabarera");
        userClaims.put(UserPotalOSGiTestConstants.ClaimURIs.EMAIL_CLAIM_URI, "dinali@wso2.com");
        credentials.put("password", "password");
        addUser(userClaims, credentials);

        Map<String, String> testUserClaims = new HashMap<>();
        testUserClaims.put(UserPotalOSGiTestConstants.ClaimURIs.LAST_NAME_CLAIM_URI, "dabarera");
        boolean result = recoveryMgtService.verifyUsername(testUserClaims);
        Assert.assertEquals(result, true, "There user should be recovered.");
    }*/

    @Test(groups = "usernameRecovery")
    public void verifyUsernameWithWrongClaims() throws UserPortalUIException, IdentityRecoveryException {

        Map<String, String> userClaims = new HashMap<>();
        Map<String, String> credentials = new HashMap<>();
        userClaims.put(UserPotalOSGiTestConstants.ClaimURIs.USERNAME_CLAIM_URI, "dinali1234");
        userClaims.put(UserPotalOSGiTestConstants.ClaimURIs.FIRST_NAME_CLAIM_URI, "dinali");
        userClaims.put(UserPotalOSGiTestConstants.ClaimURIs.LAST_NAME_CLAIM_URI, "dabarera");
        userClaims.put(UserPotalOSGiTestConstants.ClaimURIs.EMAIL_CLAIM_URI, "dinali@wso2.com");
        credentials.put(UserPotalOSGiTestConstants.PASSWORD_CALLBACK, "password");
        addUser(userClaims, credentials);

        Map<String, String> testUserClaims = new HashMap<>();
        testUserClaims.put(UserPotalOSGiTestConstants.ClaimURIs.FIRST_NAME_CLAIM_URI, "mala");
        boolean result = recoveryMgtService.verifyUsername(testUserClaims);
        Assert.assertEquals(result, false, "There should not be any user recovered.");
    }

    @Test(groups = "usernameRecovery", dependsOnMethods = "verifyUsernameWithWrongClaims")
    public void verifyUsernameWithMultipleClaims() throws UserPortalUIException, IdentityRecoveryException {

        Map<String, String> userClaims2 = new HashMap<>();
        Map<String, String> credentials2 = new HashMap<>();
        userClaims2.put(UserPotalOSGiTestConstants.ClaimURIs.USERNAME_CLAIM_URI, "dinaliDuplicate");
        userClaims2.put(UserPotalOSGiTestConstants.ClaimURIs.FIRST_NAME_CLAIM_URI, "dinali");
        userClaims2.put(UserPotalOSGiTestConstants.ClaimURIs.LAST_NAME_CLAIM_URI, "silva");
        userClaims2.put(UserPotalOSGiTestConstants.ClaimURIs.EMAIL_CLAIM_URI, "dinali@wso2.com");
        credentials2.put(UserPotalOSGiTestConstants.PASSWORD_CALLBACK, "password");
        addUser(userClaims2, credentials2);

        Map<String, String> testUserClaims = new HashMap<>();
        testUserClaims.put(UserPotalOSGiTestConstants.ClaimURIs.FIRST_NAME_CLAIM_URI, "dinali");
        boolean result = recoveryMgtService.verifyUsername(testUserClaims);
        Assert.assertEquals(result, false, "There should not be any user recovered.");
    }

    private void addUser(Map<String, String> userClaims, Map<String, String> credentials) throws UserPortalUIException {
        IdentityStoreClientService identityStoreClientService =
                bundleContext.getService(bundleContext.getServiceReference(IdentityStoreClientService.class));
        Assert.assertNotNull(identityStoreClientService, "Failed to get IdentityStoreClientService instance");
        UUFUser user = identityStoreClientService.addUser(userClaims, credentials);
        users.add(user);
    }

    private void getAllChallengeQuestionsForUser() throws IdentityRecoveryException {
        ChallengeQuestionManagerClientService challengeQuestionManagerClientService =
                bundleContext.getService(bundleContext.getServiceReference
                        (ChallengeQuestionManagerClientService.class));
        Assert.assertNotNull(challengeQuestionManagerClientService,
                "Failed to get ChallengeQuestionManagerClientService instance");

        List<ChallengeQuestion> challengeQuestions = challengeQuestionManagerClientService.getAllChallengeQuestions();
        Assert.assertNotNull(challengeQuestions, "Failed to retrieve the challenge question list.");

        this.challengeQuestions = challengeQuestions;
    }

    private void addChallengeQuestionForUser() throws UserPortalUIException {
        ChallengeQuestionManagerClientService challengeQuestionManagerClientService =
                bundleContext.getService(bundleContext.getServiceReference
                        (ChallengeQuestionManagerClientService.class));
        Assert.assertNotNull(challengeQuestionManagerClientService,
                "Failed to get ChallengeQuestionManagerClientService instance");
        try {
            challengeQuestionManagerClientService.setChallengeQuestionForUser(users.get(0).getUserId(),
                    challengeQuestions.get(0).getQuestionId(), new String(Base64.getEncoder().encode
                            (challengeQuestions.get(0).getQuestionSetId().getBytes(StandardCharsets.UTF_8)),
                            StandardCharsets.UTF_8), answer1, "challengeQAdd");
        } catch (IdentityStoreException | UserNotFoundException | IdentityRecoveryException e) {
            throw new UserPortalUIException("Test Failure. Error when setting challenge questions for the user.");
        }
    }

    private ChallengeQuestionsResponse startQuestionBasedPasswordRecovery() throws UserPortalUIException {
        return recoveryMgtService.getUserChallengeQuestionAtOnce(users.get(0).getUserId());
    }
}

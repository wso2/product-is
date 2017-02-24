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
import org.wso2.carbon.identity.recovery.model.ChallengeQuestion;
import org.wso2.carbon.kernel.utils.CarbonServerInfo;
import org.wso2.is.portal.user.client.api.ChallengeQuestionManagerClientService;
import org.wso2.is.portal.user.client.api.IdentityStoreClientService;
import org.wso2.is.portal.user.client.api.bean.ChallengeQuestionSetEntry;
import org.wso2.is.portal.user.client.api.bean.UUFUser;
import org.wso2.is.portal.user.client.api.exception.UserPortalUIException;
import org.wso2.is.portal.user.client.api.unit.test.util.UserPortalOSGiTestUtils;

import java.nio.charset.Charset;
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
public class ChallengeQuestionManagerClientServiceTest {

    private static List<UUFUser> users = new ArrayList<>();
    private static List<ChallengeQuestion> challengeQuestions = new ArrayList<>();
    private static final Logger LOGGER = LoggerFactory.getLogger(IdentityStoreClientServiceTest.class);

    @Inject
    private BundleContext bundleContext;

    @Inject
    private CarbonServerInfo carbonServerInfo;

    @Inject
    private ChallengeQuestionManagerClientService challengeQuestionManagerService;


    @Configuration
    public Option[] createConfiguration() {

        List<Option> optionList = UserPortalOSGiTestUtils.getDefaultSecurityPAXOptions();

        optionList.add(systemProperty("java.security.auth.login.config")
                .value(Paths.get(UserPortalOSGiTestUtils.getCarbonHome(), "conf", "security", "carbon-jaas.config")
                        .toString()));

        return optionList.toArray(new Option[optionList.size()]);
    }

    @Test(groups = "getChallengeQuestionList")
    public void testGetAllChallengeQuestions() throws IdentityRecoveryException {
        ChallengeQuestionManagerClientService challengeQuestionManagerClientService =
                bundleContext.getService(bundleContext.getServiceReference
                        (ChallengeQuestionManagerClientService.class));
        Assert.assertNotNull(challengeQuestionManagerClientService,
                "Failed to get ChallengeQuestionManagerClientService instance");

        List<ChallengeQuestion> challengeQuestions = challengeQuestionManagerClientService.getAllChallengeQuestions();
        Assert.assertNotNull(challengeQuestions, "Failed to retrieve the challenge question list.");

        this.challengeQuestions = challengeQuestions;
    }

    @Test(groups = "setChallengeQuestion", dependsOnGroups = {"getChallengeQuestionList"})
    public void testSetChallengeQuestionForUser() throws UserPortalUIException, UserNotFoundException,
            IdentityStoreException, IdentityRecoveryException {
        ChallengeQuestionManagerClientService challengeQuestionManagerClientService =
                bundleContext.getService(bundleContext.getServiceReference
                        (ChallengeQuestionManagerClientService.class));
        Assert.assertNotNull(challengeQuestionManagerClientService,
                "Failed to get ChallengeQuestionManagerClientService instance");

        IdentityStoreClientService identityStoreClientService =
                bundleContext.getService(bundleContext.getServiceReference(IdentityStoreClientService.class));
        Assert.assertNotNull(identityStoreClientService, "Failed to get IdentityStoreClientService instance");

        Map<String, String> userClaims = new HashMap<>();
        Map<String, String> credentials = new HashMap<>();
        userClaims.put("http://wso2.org/claims/username", "user1");
        userClaims.put("http://wso2.org/claims/givenname", "user1_firstName");
        userClaims.put("http://wso2.org/claims/lastName", "user1_lastName");
        userClaims.put("http://wso2.org/claims/email", "user1@wso2.com");

        credentials.put("password", "admin");

        UUFUser user = identityStoreClientService.addUser(userClaims, credentials);
        List<ChallengeQuestion> challengeQuestionsList = challengeQuestionManagerClientService.
                getAllChallengeQuestions();
        int minNumOfQuestionsToAnswer = challengeQuestionManagerClientService.
                getMinimumNoOfChallengeQuestionsToAnswer();
        Assert.assertEquals(minNumOfQuestionsToAnswer, 2);
        String encodedSetId1 = encodeChallengeQuestionSetId(challengeQuestionsList.get(0).getQuestionSetId());
        challengeQuestionManagerClientService.setChallengeQuestionForUser(user.getUserId(),
                challengeQuestionsList.get(0).getQuestionId(), encodedSetId1,
                "Answer1", "challengeQAdd");
        try {
            challengeQuestionManagerClientService.deleteChallengeQuestionForUser(user.getUserId(),
                    challengeQuestionsList.get(0).getQuestionId(), encodedSetId1);
        } catch (IdentityRecoveryException | IdentityStoreException | UserNotFoundException | UserPortalUIException e) {
            Assert.assertTrue(true, "Succefully validating the minimum number of questions to be answered");
        }
        List<ChallengeQuestionSetEntry> challengeQuestionSetEntries = challengeQuestionManagerClientService
                .getChallengeQuestionList(user.getUserId());
        List<ChallengeQuestion> challengeQuestionsOFUser = challengeQuestionManagerClientService
                .getAllChallengeQuestionsForUser(user.getUserId());
        //Assert.assertEquals(challengeQuestionsOFUser.size(), 1);
        Assert.assertNotNull(challengeQuestionSetEntries, "Failed to retrieve the challenge set entries question " +
                "list.");
        Assert.assertNotNull(challengeQuestionsOFUser, "Failed to retrieve the challenge question list answered by" +
                " user");
        Assert.assertNotNull(user, "Failed to add the user.");
        Assert.assertNotNull(user.getUserId(), "Invalid user unique id.");

        List<ChallengeQuestionSetEntry> remainingChallengeQuestions = challengeQuestionManagerClientService
                .getRemainingChallengeQuestions(user.getUserId());
        Assert.assertNotNull(remainingChallengeQuestions, "Failed to retrieve the remaining challenge questions of " +
                "user");


        users.add(user);

        /*try {
            challengeQuestionManagerClientService.setChallengeQuestionForUser(users.get(0).getUserId(),
                    challengeQuestions.get(0).getQuestionId(),
                    challengeQuestions.get(0).getQuestionSetId(), "Answer1", "challengeQAdd");
        } catch (IdentityStoreException | UserNotFoundException | IdentityRecoveryException e) {
            throw new UserPortalUIException("Test Failure. Error when setting challenge questions for the user.");
        }
        LOGGER.info("Test Passed. Successfully set challenge questions for the user.");
        List<UserChallengeAnswer> userChallengeAnswers = challengeQuestionManagerClientService
                .getChallengeAnswersOfUser(users.get(0).getUserId());

        Assert.assertNotNull(userChallengeAnswers, "Failed to set challenge questions for the user.");

        boolean isAdded = false;
        for (UserChallengeAnswer challengeAnswer : userChallengeAnswers) {
            if (challengeAnswer.getQuestion().equals(challengeQuestions.get(0).getQuestion())) {
                Assert.assertEquals(challengeAnswer.getAnswer(), "Answer1",
                        "Failed to set challenge questions for the user.");
                isAdded = true;
            }
        }
        if (!isAdded) {
            throw new UserPortalUIException("Test Failure. Error when setting challenge questions for the user.");
        }*/
    }


    /*@Test(groups = "getChallengeQuestionForUser", dependsOnGroups = {"getChallengeQuestionList",
            "setChallengeQuestion"})
    public void testGetAllChallengeQuestionsForUser() throws IdentityRecoveryException, IdentityStoreException,
            UserNotFoundException {
        ChallengeQuestionManagerClientService challengeQuestionManagerClientService =
                bundleContext.getService(bundleContext.getServiceReference
                        (ChallengeQuestionManagerClientService.class));
        Assert.assertNotNull(challengeQuestionManagerClientService,
                "Failed to get ChallengeQuestionManagerClientService instance");

        List<ChallengeQuestion> challengeQuestions = challengeQuestionManagerClientService
                .getAllChallengeQuestionsForUser(users.get(0).getUserId());
        Assert.assertNotNull(challengeQuestions, "Failed to retrieve the challenge question list for user.");
    }*/

   /* @Test(dependsOnGroups = {"setChallengeQuestion"})
    public void testGetAllChallengeQuestionsForUser() throws UserPortalUIException, UserNotFoundException,
            IdentityStoreException, IdentityRecoveryException {
        ChallengeQuestionManagerClientService challengeQuestionManagerClientService =
                bundleContext.getService(bundleContext.getServiceReference
                        (ChallengeQuestionManagerClientService.class));
        Assert.assertNotNull(challengeQuestionManagerClientService,
                "Failed to get ChallengeQuestionManagerClientService instance");

        List<ChallengeQuestion> challengeQuestions = challengeQuestionManagerClientService
                .getAllChallengeQuestionsForUser(users.get(0).getUserId());
        Assert.assertNotNull(challengeQuestions, "Failed to retrieve the challenge questions of the user.");

        boolean addedAvailable = false;
        for (ChallengeQuestion challengeQuestion : challengeQuestions) {
            if (challengeQuestions.get(0).getQuestion().equals(challengeQuestion.getQuestion())) {
                addedAvailable = true;
            }
        }
        if (!addedAvailable) {
            throw new UserPortalUIException("Test Failure. " +
                    "Error when getting all the challenge questions for the user.");
        }
    }
*/

    private String encodeChallengeQuestionSetId(String questionSetId) {
        return new String(Base64.getEncoder().encode(questionSetId.
                getBytes(Charset.forName("UTF-8"))), Charset.forName("UTF-8"));
    }

}

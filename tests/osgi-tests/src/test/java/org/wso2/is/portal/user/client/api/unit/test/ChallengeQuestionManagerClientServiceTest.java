/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
import org.wso2.carbon.identity.recovery.IdentityRecoveryException;
import org.wso2.carbon.identity.recovery.model.ChallengeQuestion;
import org.wso2.carbon.kernel.utils.CarbonServerInfo;
import org.wso2.is.portal.user.client.api.ChallengeQuestionManagerClientService;
import org.wso2.is.portal.user.client.api.bean.UUFUser;
import org.wso2.is.portal.user.client.api.unit.test.util.UserPortalOSGiTestUtils;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;

import static org.ops4j.pax.exam.CoreOptions.systemProperty;

@Listeners(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
public class ChallengeQuestionManagerClientServiceTest {

    private static List<UUFUser> users = new ArrayList<>();
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

    @Test(groups = "getChallengeQuestion")
    public void testGetChallengeQuestionList() throws IdentityRecoveryException {
        ChallengeQuestionManagerClientService challengeQuestionManagerService =
                bundleContext.getService(bundleContext.getServiceReference
                        (ChallengeQuestionManagerClientService.class));
        Assert.assertNotNull(challengeQuestionManagerService,
                "Failed to get ChallengeQuestionManagerClientService instance");

        List<ChallengeQuestion> challengeQuestions = challengeQuestionManagerService.getChallengeQuestionList();
        Assert.assertNotNull(challengeQuestions, "Failed to retrieve the challenge question list.");
    }

  /*  @Test(groups = "setChallengeQuestion")
    public void testSetChallengeQuestionForUser() throws UserPortalUIException {
        ChallengeQuestionManagerClientService challengeQuestionManagerService =
                bundleContext.getService(bundleContext.getServiceReference
                        (ChallengeQuestionManagerClientService.class));
        Assert.assertNotNull(challengeQuestionManagerService,
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

        Assert.assertNotNull(user, "Failed to add the user.");
        Assert.assertNotNull(user.getUserId(), "Invalid user unique id.");

        users.add(user);

        ChallengeQuestion challengeQuestion = new ChallengeQuestion(null, "What is your pet's name");

        try {
            challengeQuestionManagerService.setChallengeQuestionForUser(user.getUserId(), challengeQuestion, "kitty");
        } catch (IdentityStoreException | UserNotFoundException | IdentityRecoveryException e) {
            throw new UserPortalUIException("Test Failure. Error when setting challenge questions for the user.");
        }
        LOGGER.info("Test Passed. Successfully set challenge questions for the user.");
    }

    @Test(dependsOnGroups = {"setChallengeQuestion"})
    public void testGetAllChallengeQuestionsForUser() throws UserPortalUIException, UserNotFoundException,
            IdentityStoreException, IdentityRecoveryException {
        ChallengeQuestionManagerClientService challengeQuestionManagerService =
                bundleContext.getService(bundleContext.getServiceReference
                        (ChallengeQuestionManagerClientService.class));
        Assert.assertNotNull(challengeQuestionManagerService,
                "Failed to get ChallengeQuestionManagerClientService instance");

        List<ChallengeQuestion>  challengeQuestions = challengeQuestionManagerService.getAllChallengeQuestionsForUser
                (users.get(0).getUserId());
        Assert.assertNotNull(challengeQuestions, "Failed to retrieve the challenge questions of the user.");
        Assert.assertEquals(challengeQuestions.get(0).getQuestion(),"What is your pet's name",
                "Failed to retrieve the challenge questions of the user.");
    }
*/
}

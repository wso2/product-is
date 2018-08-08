/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.identity.integration.test.challenge.questions.mgt;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.common.model.xsd.User;
import org.wso2.carbon.identity.recovery.stub.model.ChallengeQuestion;
import org.wso2.carbon.integration.common.admin.client.AuthenticatorClient;
import org.wso2.identity.integration.common.clients.UserManagementClient;
import org.wso2.identity.integration.common.clients.challenge.questions.mgt.ChallengeQuestionMgtAdminClient;
import org.wso2.identity.integration.common.clients.usermgt.remote.RemoteUserStoreManagerServiceClient;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;

import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

public class ChallengeQuestionManagementAdminServiceTestCase extends ISIntegrationTest {

    private UserManagementClient userMgtClient;
    private ChallengeQuestionMgtAdminClient challengeQuestionsAdminClient;
    private ChallengeQuestionMgtAdminClient challengeQuestionsBobClient;
    private AuthenticatorClient loginManger;
    private RemoteUserStoreManagerServiceClient remoteUSMServiceClient;
    ChallengeQuestion challengeQuestionSet1;

    private static final String PROFILE_NAME = "default";

    private static final String BOB_USERNAME = "Bob";
    private static final String BOB_PASSWORD = "Bob@123";
    private static final String BOB_LOCALE = "xx_YY";
    private static final String LOGIN_ROLE = "loginRole";


    private static final String LOGIN_PERMISSION = "/permission/admin/login";
    private static final String SET_CHALLENGE_QUESTIONS_PERMISSION = "/permission/admin/configure/security/questions";

    private static final String SUPER_TENANT = "carbon.super";
    private static final String WSO2_TENANT = "wso2.com";

    private static final String LOCALITY_CLAIM_URI = "http://wso2.org/claims/locality";

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {
        super.init();

        loginManger = new AuthenticatorClient(backendURL);

        // login as super tenant admin
        loginManger.login(isServer.getSuperTenant().getTenantAdmin().getUserName(),
                isServer.getSuperTenant().getTenantAdmin().getPassword(),
                isServer.getInstance().getHosts().get("default"));

        userMgtClient = new UserManagementClient(backendURL, sessionCookie);
        remoteUSMServiceClient = new RemoteUserStoreManagerServiceClient(backendURL, sessionCookie);
        challengeQuestionsAdminClient = new ChallengeQuestionMgtAdminClient(backendURL, sessionCookie);
        challengeQuestionsBobClient = new ChallengeQuestionMgtAdminClient(backendURL, BOB_USERNAME, BOB_PASSWORD);

        createUsersAndRoles();
        setSystemproperties();
    }

    @Test(groups = "wso2.is", description = "Getting challenge questions of a user", priority = 1, sequential = true)
    public void testDefaultChallengeQuestions() throws Exception {
        ChallengeQuestion[] defaultQuestions = challengeQuestionsBobClient.getChallengeQuestionsForTenant(SUPER_TENANT);
        assertTrue(defaultQuestions != null && defaultQuestions.length > 0,
                "Default Challenge questions not found for " + SUPER_TENANT + " tenantDomain.");

    }


    @Test(groups = "wso2.is", description = "Getting challenge questions of a user", priority = 2, sequential = true)
    public void addChallengeQuestionByTenant() throws Exception {

        int countBefore = challengeQuestionsBobClient.getChallengeQuestionsForTenant(SUPER_TENANT).length;

        challengeQuestionSet1 = new ChallengeQuestion();
        challengeQuestionSet1.setQuestionSetId("newSet1");
        challengeQuestionSet1.setQuestionId("q1");
        challengeQuestionSet1.setQuestion("This is a new Challenge Question????");
        challengeQuestionSet1.setLocale("en_US");
        challengeQuestionsBobClient.setChallengeQuestions(new ChallengeQuestion[]{challengeQuestionSet1},
                SUPER_TENANT);

        int countAfter = challengeQuestionsBobClient.getChallengeQuestionsForTenant(SUPER_TENANT).length;

        assertTrue(countBefore + 1 == countAfter, "Adding a new challenge question failed in " + SUPER_TENANT);
    }

    @Test(groups = "wso2.is", description = "Getting challenge questions of a user", priority = 3, sequential = true)
    public void addChallengeQuestionByLocale() throws Exception {
        challengeQuestionsAdminClient = new ChallengeQuestionMgtAdminClient(backendURL, BOB_USERNAME, BOB_PASSWORD);

        ChallengeQuestion[] challengeQuestions =
                challengeQuestionsAdminClient.getChallengeQuestionsForLocale(SUPER_TENANT, BOB_LOCALE);

        int countBefore = challengeQuestions == null ? 0 : challengeQuestions.length;

        ChallengeQuestion challengeQuestion = new ChallengeQuestion();
        challengeQuestion.setQuestionSetId("newSet1");
        challengeQuestion.setQuestionId("q2");
        challengeQuestion.setQuestion("Challenge Question in xx_YY ????");
        challengeQuestion.setLocale(BOB_LOCALE);

        challengeQuestionsAdminClient.setChallengeQuestions(new ChallengeQuestion[]{challengeQuestion}, SUPER_TENANT);
        int countAfter = challengeQuestionsAdminClient.getChallengeQuestionsForLocale(SUPER_TENANT, BOB_LOCALE)
                .length;
        assertTrue(countBefore + 1 == countAfter, "Adding a new challenge question for locale " + BOB_LOCALE +
                " failed in " + SUPER_TENANT);
    }

    @Test(groups = "wso2.is", description = "Getting challenge questions of a user", priority = 4, sequential = true)
    public void getChallengeQuestionsByLocale() throws Exception {
        challengeQuestionsAdminClient = new ChallengeQuestionMgtAdminClient(backendURL, BOB_USERNAME, BOB_PASSWORD);

        try {
            ChallengeQuestion[] questionsForLocale =
                    challengeQuestionsAdminClient.getChallengeQuestionsForLocale(SUPER_TENANT, BOB_LOCALE);

            for (ChallengeQuestion challengeQuestion : questionsForLocale) {
                assertTrue(BOB_LOCALE.equalsIgnoreCase(challengeQuestion.getLocale()));
            }
        } catch (Exception e) {
            fail("Exception when retrieving questions of locale : " + BOB_LOCALE);
        }

    }

    @Test(groups = "wso2.is", description = "Getting challenge questions of a user", priority = 5, sequential = true)
    public void getChallengeQuestionByUser() throws Exception {
        challengeQuestionsAdminClient = new ChallengeQuestionMgtAdminClient(backendURL, BOB_USERNAME, BOB_PASSWORD);

        User bob = new User();
        bob.setUserName(BOB_USERNAME);
        bob.setTenantDomain(SUPER_TENANT);

        int count = challengeQuestionsAdminClient.getChallengeQuestionsForUser(bob).length;
        assertTrue(count == 1, "Challenge Questions not retrieved successfully for user : " + bob.toString());
    }

    @Test(groups = "wso2.is", description = "Deleting challenge question in a locale", priority = 6, sequential = true)
    public void deleteChallengeQuestionsByLocale() throws Exception {
        challengeQuestionsAdminClient = new ChallengeQuestionMgtAdminClient(backendURL,
                isServer.getSuperTenant().getTenantAdmin().getUserName(),
                isServer.getSuperTenant().getTenantAdmin().getUserName());

        // get challenge challenge questions of xx_YY locale
        ChallengeQuestion[] challengeQuestions =
                challengeQuestionsAdminClient.getChallengeQuestionsForLocale(SUPER_TENANT, BOB_LOCALE);

        // we should have challenge questions in the locale by now.
        assertTrue(challengeQuestions != null && challengeQuestions.length > 0);

        // delete all the challenge questions in the xx_YY locale
        challengeQuestionsAdminClient.deleteChallengeQuestions(challengeQuestions, SUPER_TENANT);

        // delete all added challenge questions in the en_US locale
        challengeQuestionsAdminClient.deleteChallengeQuestions(new ChallengeQuestion[]{challengeQuestionSet1},
                SUPER_TENANT);

        // retrieve challenge questions once again.
        challengeQuestions =
                challengeQuestionsAdminClient.getChallengeQuestionsForLocale(SUPER_TENANT, BOB_LOCALE);

        int numberOfQuestions = challengeQuestions == null ? 0 : challengeQuestions.length;
        // now we shouldn't have challenge questions.
        assertTrue(numberOfQuestions == 0);

    }

    @Test(groups = "wso2.is", description = "Get default questions if the locale doesn't have any questions set.",
            priority = 7, sequential = true)
    public void getDefaultChallengeQuestionsForUser() throws Exception {
        challengeQuestionsAdminClient = new ChallengeQuestionMgtAdminClient(backendURL, BOB_USERNAME, BOB_PASSWORD);

        User bob = new User();
        bob.setUserName(BOB_USERNAME);
        bob.setTenantDomain(SUPER_TENANT);

        // we need to confirm there are no challenge questions in Bob's locale
        ChallengeQuestion[] localeQuestions =
                challengeQuestionsAdminClient.getChallengeQuestionsForLocale(SUPER_TENANT, BOB_LOCALE);
        int bobLocale = localeQuestions == null ? 0 : localeQuestions.length;
        assertTrue(bobLocale == 0, "Cannot have challenge questions in " + BOB_LOCALE);

        // this should return us with default questions since Bob's locale doesn't have any challenge questions.
        int count = challengeQuestionsAdminClient.getChallengeQuestionsForUser(bob).length;
        assertTrue(count > 1, "Default Challenge Questions not retrieved for user : " + bob.toString());
    }


    @Test(groups = "wso2.is", description = "Testing cross tenant access.")
    public void checkSaaSAccess() throws Exception {
        challengeQuestionsAdminClient = new ChallengeQuestionMgtAdminClient(backendURL, BOB_USERNAME, BOB_PASSWORD);
        try {
            challengeQuestionsAdminClient.getChallengeQuestionsForTenant(WSO2_TENANT);
            fail(SUPER_TENANT + " user was able to access challenge questions in " + WSO2_TENANT);
        } catch (Exception ex) {
            assertTrue(ex.getMessage().contains("Unauthorized Access"));
        }
    }


    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {
        challengeQuestionsAdminClient = null;
        loginManger.logOut();

    }


    private void createUsersAndRoles() throws Exception {

        try {
            log.info("Creating user : " + BOB_USERNAME);
            remoteUSMServiceClient.addUser(BOB_USERNAME, BOB_PASSWORD, null, null, PROFILE_NAME, false);
        } catch (Exception ex) {
            fail("Exception when creating users.", ex);
        }

        try {
            remoteUSMServiceClient.setUserClaimValue(BOB_USERNAME, LOCALITY_CLAIM_URI, BOB_LOCALE, null);
        } catch (Exception ex) {
            fail("Exception when setting locale claim for users.");
        }

        try {
            // assign them login permissions via testRole
            log.info("Creating " + LOGIN_ROLE + " role and assigning to created users.");
            userMgtClient.addRole(LOGIN_ROLE, new String[]{BOB_USERNAME}, new String[]{LOGIN_PERMISSION,
                    SET_CHALLENGE_QUESTIONS_PERMISSION}, false);

        } catch (Exception ex) {
            fail("Exception when  assigning roles with login permission to users.");
        }
    }

    private void deleteUsersAndRoles() {
        try {
            log.info("Deleting user : " + BOB_USERNAME);
            userMgtClient.deleteUser(BOB_USERNAME);
        } catch (Exception e) {
            fail("Exception when deleting users", e);
        }

        try {
            log.info("Deleting Role : " + LOGIN_ROLE);
            userMgtClient.deleteRole(LOGIN_ROLE);
        } catch (Exception e) {
            fail("Exception when deleting " + LOGIN_ROLE + " role.", e);
        }

    }


}

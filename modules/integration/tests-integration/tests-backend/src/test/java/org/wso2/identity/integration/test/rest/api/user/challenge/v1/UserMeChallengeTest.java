/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.identity.integration.test.rest.api.user.challenge.v1;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.identity.integration.test.rest.api.user.common.RESTAPIUserTestBase;

import java.io.IOException;

import javax.xml.xpath.XPathExpressionException;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.core.Is.is;

/**
 * Test REST API for managing logged in user's challenge question answers
 */
public class UserMeChallengeTest extends RESTAPIUserTestBase {

    static final String API_DEFINITION_NAME = "challenge.yaml";
    static final String API_VERSION = "v1";
    static String API_PACKAGE_NAME = "org.wso2.carbon.identity.rest.api.user.challenge.v1";

    public static final String CHALLENGES_ENDPOINT_URI = "/%s/challenges";
    public static final String CHALLENGE_ANSWERS_ENDPOINT_URI = "/%s/challenge-answers";
    public static final String CHALLENGE_ANSWER_ENDPOINT_URI = CHALLENGE_ANSWERS_ENDPOINT_URI + "/%s";

    protected String adminUsername;
    protected String adminPassword;
    protected String tenant;

    private String userChallengesEndpointURI;
    private String userChallengeAnswerEndpointURI;
    private String userChallengeAnswersEndpointURI;

    @Factory(dataProvider = "restAPIUserConfigProvider")
    public UserMeChallengeTest(TestUserMode userMode) throws Exception {

        context = new AutomationContext("IDENTITY", userMode);
        this.adminUsername = context.getContextTenant().getTenantAdmin().getUserName();
        this.adminPassword = context.getContextTenant().getTenantAdmin().getPassword();
        this.tenant = context.getContextTenant().getDomain();
    }

    @BeforeClass(alwaysRun = true)
    public void init() throws IOException, XPathExpressionException {

        super.testInit(API_VERSION, API_PACKAGE_NAME, API_DEFINITION_NAME, tenant);
        initUrls("me");
    }

    void initUrls(String pathParam) {
        this.userChallengesEndpointURI = String.format(CHALLENGES_ENDPOINT_URI, pathParam);
        this.userChallengeAnswersEndpointURI = String.format(CHALLENGE_ANSWERS_ENDPOINT_URI, pathParam);
        this.userChallengeAnswerEndpointURI = this.userChallengeAnswersEndpointURI + "/%s";
    }

    @AfterClass(alwaysRun = true)
    public void testConclude() {

        super.conclude();
    }

    @BeforeMethod(alwaysRun = true)
    public void testInit() {

        RestAssured.basePath = basePath;
    }

    @AfterMethod(alwaysRun = true)
    public void testFinish() {

        RestAssured.basePath = StringUtils.EMPTY;
    }

    @DataProvider(name = "restAPIUserConfigProvider")
    public static Object[][] restAPIUserConfigProvider() {
        return new Object[][]{
                {TestUserMode.SUPER_TENANT_ADMIN},
                {TestUserMode.TENANT_ADMIN}
        };
    }

    @Test
    public void testGetAvailableChallenges() {

        getResponseOfGet(this.userChallengesEndpointURI)
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .log().ifValidationFails();
    }

    @Test
    public void testAnswerChallenges() throws IOException {

        String body = readResource("challenge-answers-array-correct-1.json");
        getResponseOfPost(this.userChallengeAnswersEndpointURI, body)
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .log().ifValidationFails();
    }

    @Test(dependsOnMethods = {"testAnswerChallenges"})
    public void testGetAnsweredChallenges() {

        getResponseOfGet(this.userChallengeAnswersEndpointURI)
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .log().ifValidationFails()
                .body("size()", is(1))
                .body("questionSetId", hasItems("challengeQuestion1"))
                .body("question", hasItems("City where you were born ?"));
    }

    @Test(dependsOnMethods = {"testGetAnsweredChallenges"})
    public void testUpdateChallengeAnswerOfASet() throws IOException {

        String endpointURI = String.format(this.userChallengeAnswerEndpointURI, "challengeQuestion1");
        String body = readResource("challenge-answer-set1-q4-correct.json");
        getResponseOfPut(endpointURI, body)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);

        getResponseOfGet(this.userChallengeAnswersEndpointURI)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("size()", is(1))
                .body("questionSetId", hasItems("challengeQuestion1"));
    }

    @Test(dependsOnMethods = {"testUpdateChallengeAnswerOfASet"})
    public void testAnswerSpecificChallenge() throws IOException {

        String endpointURI = String.format(this.userChallengeAnswerEndpointURI, "challengeQuestion2");
        String body = readResource("challenge-answer-set2-q4-correct.json");
        getResponseOfPost(endpointURI, body)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED);
    }

    @Test(dependsOnMethods = {"testAnswerSpecificChallenge"})
    public void testGetAllChallengeAnswers() {

        getResponseOfGet(this.userChallengeAnswersEndpointURI)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("size()", is(2))
                .body("questionSetId", hasItems("challengeQuestion1", "challengeQuestion2"))
                .body("question", hasItems("Favorite vacation location ?", "Favorite sport ?"))
                .body("find{ it.questionSetId == 'challengeQuestion1' }.question", is("Favorite vacation location ?"))
                .body("find{ it.questionSetId == 'challengeQuestion2' }.question", is("Favorite sport ?"));
    }

    @Test(dependsOnMethods = {"testGetAllChallengeAnswers"})
    public void testUpdateAllChallenges() throws IOException {

        String body = readResource("challenge-answers-array-correct-2.json");
        getResponseOfPut(this.userChallengeAnswersEndpointURI, body)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);

        getResponseOfGet(this.userChallengeAnswersEndpointURI)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("size()", is(2))
                .body("questionSetId", hasItems("challengeQuestion1", "challengeQuestion2"))
                .body("question", hasItems("Father's middle name ?", "Name of your first pet ?"))
                .body("find{ it.questionSetId == 'challengeQuestion1' }.question", is("Father's middle name ?"))
                .body("find{ it.questionSetId == 'challengeQuestion2' }.question", is("Name of your first pet ?"));
    }

    @Test(dependsOnMethods = {"testUpdateAllChallenges"})
    public void testRemoveSpecificChallengeAnswer() {

        String endpointURI = String.format(this.userChallengeAnswerEndpointURI, "challengeQuestion2");
        getResponseOfDelete(endpointURI)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NO_CONTENT);

        getResponseOfGet(this.userChallengeAnswersEndpointURI)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("size()", is(1))
                .body("questionSetId", hasItems("challengeQuestion1"))
                .body("question", hasItems("Father's middle name ?"));
    }

    @Test(dependsOnMethods = {"testRemoveSpecificChallengeAnswer"})
    public void testRemoveChallengeAnswers() {

        getResponseOfDelete(this.userChallengeAnswersEndpointURI)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NO_CONTENT);

        getResponseOfGet(this.userChallengeAnswersEndpointURI)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("size()", is(0));
    }

    private Response getResponseOfGet(String endpointUri) {
        return given().auth().preemptive().basic(adminUsername, adminPassword)
                .contentType(ContentType.JSON)
                .header(HttpHeaders.ACCEPT, ContentType.JSON)
                .log().ifValidationFails()
                .filter(validationFilter)
                .when()
                .get(endpointUri);
    }

    private Response getResponseOfPost(String endpointUri, String body) {

        return given().auth().preemptive().basic(adminUsername, adminPassword)
                .contentType(ContentType.JSON)
                .header(HttpHeaders.ACCEPT, ContentType.JSON)
                .log().ifValidationFails()
                .body(body)
                .filter(validationFilter)
                .when()
                .post(endpointUri);
    }

    private Response getResponseOfPut(String endpointURI, String body) {

        return given().auth().preemptive().basic(adminUsername, adminPassword)
                .contentType(ContentType.JSON)
                .header(HttpHeaders.ACCEPT, ContentType.JSON)
                .log().ifValidationFails()
                .body(body)
                .filter(validationFilter)
                .when()
                .put(endpointURI);
    }

    private Response getResponseOfDelete(String endpointURI) {

        return given().auth().preemptive().basic(adminUsername, adminPassword)
                .contentType(ContentType.JSON)
                .header(HttpHeaders.ACCEPT, ContentType.JSON)
                .log().ifValidationFails()
                .filter(validationFilter)
                .when()
                .delete(endpointURI);
    }

}

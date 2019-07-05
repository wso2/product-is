/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 */

package org.wso2.identity.integration.test.rest.api.challenge.questions;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;
import org.wso2.identity.integration.test.rest.api.challenge.questions.model.ServerChallengeModel;

import java.util.ArrayList;

import java.util.List;

import static io.restassured.RestAssured.given;

/**
 * This class is for ServerChallengeQuestion rest api test cases.
 */
public class ServerChallengeTestCase extends ISIntegrationTest {

    private final String questionId = "question7";
    private final String questionSetId = "challengeQuestion7";
    private final String username = "admin";
    private final String password = "admin";
    private static final String serverUrl = "https://localhost:9853";
    private static final String resourcePath = "/api/server/v1/challenges";

    /**
     * RestAssured.baseURI, which sets the base URI statically.
     */
    @BeforeTest(alwaysRun = true)
    public void testinit() throws Exception {

        super.init();
        RestAssured.baseURI = serverUrl;

    }

    /**
     * The method is used to add new challenge question set.
     *
     * @throws JSONException thrown to indicate a problem with the JSON.
     */
    @Test
    public void addsNewChallengeQuestionset() throws JSONException {

        String locale = "en_US";
        String challengeQuestion = "What is your best friend name?";
        ServerChallengeModel.Questions question = new ServerChallengeModel.Questions(locale,
                challengeQuestion, questionId);
        List<ServerChallengeModel.Questions> questions = new ArrayList<>();
        questions.add(question);

        List<ServerChallengeModel> serverChallenges = new ArrayList<>();
        ServerChallengeModel serverChallengeRequest = new ServerChallengeModel(questionSetId, questions);
        serverChallenges.add(serverChallengeRequest);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String jsonRequrst = gson.toJson(serverChallenges);

        given()
                .auth().preemptive().basic(username, password)
                .contentType(ContentType.JSON)
                .header("Accept", ContentType.JSON)
                .body(jsonRequrst)
                .log().all()
                .when()
                .post(resourcePath).
                then().assertThat().statusCode(201);

        getsChallengeQuestiontest(questionSetId, challengeQuestion, questionId);
    }

    /**
     * This test method validate update challenge question api response.
     *
     * @throws JSONException thrown to indicate a problem with the JSON.
     */
    @Test(dependsOnMethods = { "addsNewChallengeQuestionset" })
    public void updateChallengeQuestionviaPUTtest() throws JSONException {

        String locale = "en_UK";
        String questionID = "question6";
        String challengeQuestion = "What is the name of your first pet?";
        ServerChallengeModel.Questions question = new ServerChallengeModel.Questions(locale,
                challengeQuestion, questionID);
        List<ServerChallengeModel.Questions> questions = new ArrayList<>();
        questions.add(question);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String request = gson.toJson(questions);

        given().auth().preemptive().basic(username, password).contentType(ContentType.JSON)
                .header("Accept", ContentType.JSON)
                .body(request)
                .log().all()
                .when()
                .put(resourcePath + "/{challenge-set-id}", questionSetId)
                .then()
                .log().all()
                .assertThat()
                .statusCode(200);
        getsChallengeQuestiontest(questionSetId, challengeQuestion, questionID);
    }

    /**
     * This test method validate update challenge question api response.
     *
     * @throws JSONException thrown to indicate a problem with the JSON.
     */
    @Test(dependsOnMethods = { "addsNewChallengeQuestionset" })
    public void updateChallengeQuestionviaPATCHtest() throws JSONException {
        String operation = "add";
        String locale = "en_US";
        String questionID = "question6";
        String challengeQuestion = "What is the name of your first school?";
        ServerChallengeModel.Questions question = new ServerChallengeModel.Questions(locale,
                challengeQuestion, questionID);
        ServerChallengeModel.ChallengeQuestionOperation challengeQuestionOperation = new ServerChallengeModel
                .ChallengeQuestionOperation(question, operation);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(challengeQuestionOperation);

        given().auth().preemptive().basic(username, password).contentType(ContentType.JSON)
                .header("Accept", ContentType.JSON)
                .body(json)
                .log().all()
                .when()
                .patch(resourcePath + "/{challenge-set-id}", questionSetId)
                .then()
                .log().all()
                .assertThat()
                .statusCode(201);

        getsChallengeQuestiontest(questionSetId, challengeQuestion, questionID);

    }

    /**
     * The test method validate the api which delete challenge question by questionSetId.
     *
     * @throws JSONException thrown to indicate a problem with the JSON.
     */
    @Test(priority = 1)
    public void removesChallengeQuestionSet() throws JSONException {

        addsNewChallengeQuestionset();

        given().auth().preemptive().basic(username, password).contentType(ContentType.JSON)
                .header("Accept", ContentType.JSON)
                .log().all()
                .when()
                .delete(resourcePath + "/{challenge-set-id}", questionSetId)
                .then()
                .log().all()
                .assertThat()
                .statusCode(204)
                .extract().response();

    }

    /**
     * The test method validate the api which delete challenge question by questionSetId and questionSetId.
     */
    @Test(dependsOnMethods = {
            "addsNewChallengeQuestionset", "updateChallengeQuestionviaPUTtest", "updateChallengeQuestionviaPATCHtest"
    })
    public void removesChallengeQuestion() {

        given().auth().preemptive().basic(username, password).contentType(ContentType.JSON)
                .header("Accept", ContentType.JSON)
                .log().all()
                .when()
                .delete(resourcePath + "/{challenge-set-id}/questions/{question-id}", questionSetId, questionId)
                .then()
                .log().all()
                .assertThat()
                .statusCode(204)
                .extract().response();

    }

    /**
     * This test method validate retrieve challenge question api response.
     *
     * @param questionSetId string example: challengeQuestion1
     * @param question      string, represent challenge question
     * @param questionId    string, challenge question id
     * @throws JSONException thrown to indicate a problem with the JSON.
     */
    private void getsChallengeQuestiontest(String questionSetId, String question, String questionId)
            throws JSONException {

        String questionSetIdAttribute = "questionSetId";
        String questionIdAttribute = "questionId";
        String questionAttribute = "question";
        String questioObjectAttribute = "questions";
        Response response = given().auth().preemptive().basic(username, password).contentType(ContentType.JSON)
                .header("Accept", ContentType.JSON).log().all().when().get(resourcePath).then()
                .log().all()
                .assertThat()
                .statusCode(200)
                .and()
                .contentType(ContentType.JSON)
                .extract().response();

        List<String> actualQuestionSetId = getValuesByKeyInJSONArray(response.asString(), questionSetIdAttribute);
        Assert.assertTrue(actualQuestionSetId.contains(questionSetId));

        List<String> actualQuestionId = getValueKeyInNestedArray(response.asString(), questioObjectAttribute,
                questionIdAttribute);
        Assert.assertTrue(actualQuestionId.contains(questionId));

        List<String> actualQuestion = getValueKeyInNestedArray(response.asString(), questioObjectAttribute,
                questionAttribute);
        Assert.assertTrue(actualQuestion.contains(question));

    }

    /**
     * @param jsonArrayStr represent JSON Array.
     * @param key          will give you the value of a particular key of the JSON Array.
     * @return will return the corresponding values.
     * @throws JSONException thrown to indicate a problem with the JSON.
     */
    private List<String> getValuesByKeyInJSONArray(String jsonArrayStr, String key) throws JSONException {
        List<String> values = new ArrayList<>();
        JSONArray jsonArray = new JSONArray(jsonArrayStr);
        for (int idx = 0; idx < jsonArray.length(); idx++) {
            JSONObject jsonObj = jsonArray.getJSONObject(idx);
            values.add(jsonObj.optString(key));
        }
        return values;
    }

    /**
     * @param jsonArrayStr   represent JSON Array.
     * @param key            will give you the value of a particular key of the JSON Array.
     * @param nestedArrayKey represent Nested JSON Array.
     * @return will return the corresponding values.
     * @throws JSONException thrown to indicate a problem with the JSON.
     */
    private List<String> getValueKeyInNestedArray(String jsonArrayStr, String key, String nestedArrayKey) throws
            JSONException {

        List<String> values = new ArrayList<>();
        JSONArray jsonArray = new JSONArray(jsonArrayStr);
        for (int idx = 0; idx < jsonArray.length(); idx++) {
            JSONObject jsonObj = jsonArray.getJSONObject(idx);
            String questionObject = jsonObj.getString(key);
            JSONArray nestedArray = new JSONArray(questionObject);

            for (int k = 0; k < nestedArray.length(); k++) {
                JSONObject nestedobj = nestedArray.getJSONObject(k);

                values.add(nestedobj.optString(nestedArrayKey));

            }

        }
        return values;
    }

}

/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.identity.integration.test.rest.api.server.challenge.v1;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.wso2.identity.integration.test.rest.api.server.challenge.v1.model.ServerChallengeModel;

import java.util.ArrayList;
import java.util.List;

import static io.restassured.RestAssured.given;

/**
 * This class is for ServerChallengeQuestion rest api test cases.
 */
public class ServerChallengeTestCase {

    private final String USER_NAME = "admin";
    private final String PASSWORD = "admin";
    private static final String CHALLENGE_QUESTION = "What is your best friend name?";
    private static final String LOCALE = "en_US";
    private static final String LOCALE_ATTRIBUTE = "locale";
    private static final String QUESTION_ATTRIBUTE = "question";
    private static final String QUESTION_ID = "question7";
    private static final String QUESTION_ID_ATTRIBUTE = "questionId";
    private static final String QUESTION_OBJECT_ATTRIBUTE = "questions";
    private final static String QUESTION_SET_ID = "challengeQuestion7";
    private static final String QUESTION_SET_ID_ATTRIBUTE = "questionSetId";
    private static final String RESOURCE_PATH = "/api/server/v1/challenges";
    private static final String SERVER_URL = "https://localhost:9853";

    /**
     * RestAssured.baseURI, which sets the base URI statically.
     */
    @BeforeTest(alwaysRun = true)
    public void testinit() {

        RestAssured.baseURI = SERVER_URL;
    }

    /**
     * The method is used to add new challenge question set.
     *
     * @throws JSONException thrown to indicate a problem with the JSON.
     */
    @Test
    public void addsNewChallengeQuestionSetTest() throws JSONException {

        ServerChallengeModel.Questions question = new ServerChallengeModel.Questions(LOCALE,
                CHALLENGE_QUESTION, QUESTION_ID);
        List<ServerChallengeModel.Questions> questions = new ArrayList<>();
        questions.add(question);

        List<ServerChallengeModel> serverChallenges = new ArrayList<>();
        ServerChallengeModel serverChallengeRequest = new ServerChallengeModel(QUESTION_SET_ID, questions);
        serverChallenges.add(serverChallengeRequest);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String jsonRequrst = gson.toJson(serverChallenges);

        given()
                .auth().preemptive().basic(USER_NAME, PASSWORD)
                .contentType(ContentType.JSON)
                .header(HttpHeaders.ACCEPT, ContentType.JSON)
                .body(jsonRequrst)
                .log().ifValidationFails()
                .when()
                .post(RESOURCE_PATH).
                then().assertThat().statusCode(HttpStatus.SC_CREATED).log().ifValidationFails();

        getsChallengeQuestionTest(QUESTION_SET_ID, CHALLENGE_QUESTION, QUESTION_ID);
    }

    /**
     * This test method validate update challenge question api response.
     *
     * @throws JSONException thrown to indicate a problem with the JSON.
     */
    @Test(dependsOnMethods = { "addsNewChallengeQuestionSetTest" })
    public void updateChallengeQuestionPUTTest() throws JSONException {

        String locale = "en_UK";
        String questionID = "question6";
        String challengeQuestion = "What is the name of your first pet?";
        ServerChallengeModel.Questions question = new ServerChallengeModel.Questions(locale,
                challengeQuestion, questionID);
        List<ServerChallengeModel.Questions> questions = new ArrayList<>();
        questions.add(question);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String request = gson.toJson(questions);

        given().auth().preemptive().basic(USER_NAME, PASSWORD).contentType(ContentType.JSON)
                .header(HttpHeaders.ACCEPT, ContentType.JSON)
                .body(request)
                .log().ifValidationFails()
                .when()
                .put(RESOURCE_PATH + "/{challenge-set-id}", QUESTION_SET_ID)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK).log().ifValidationFails();
        getsChallengeQuestionTest(QUESTION_SET_ID, challengeQuestion, questionID);
    }

    /**
     * This test method validate update challenge question api response.
     *
     * @throws JSONException thrown to indicate a problem with the JSON.
     */
    @Test(dependsOnMethods = { "addsNewChallengeQuestionSetTest" })
    public void updateChallengeQuestionPATCHTest() throws JSONException {

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

        given().auth().preemptive().basic(USER_NAME, PASSWORD).contentType(ContentType.JSON)
                .header(HttpHeaders.ACCEPT, ContentType.JSON)
                .body(json)
                .log().ifValidationFails()
                .when()
                .patch(RESOURCE_PATH + "/{challenge-set-id}", QUESTION_SET_ID)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED).log().ifValidationFails();

        getsChallengeQuestionTest(QUESTION_SET_ID, challengeQuestion, questionID);
    }

    /**
     * The test method validate the api which delete challenge question by QUESTION_SET_ID.
     */
    @Test(dependsOnMethods = { "addsNewChallengeQuestionSetTest", "updateChallengeQuestionPUTTest",
                               "updateChallengeQuestionPATCHTest" })
    public void removesChallengeQuestionSetTest() {

        given().auth().preemptive().basic(USER_NAME, PASSWORD).contentType(ContentType.JSON)
                .header(HttpHeaders.ACCEPT, ContentType.JSON)
                .log().ifValidationFails()
                .when()
                .delete(RESOURCE_PATH + "/{challenge-set-id}", QUESTION_SET_ID)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NO_CONTENT)
                .log().ifValidationFails()
                .extract().response();
    }

    /**
     * The test method validate the api which delete challenge question by QUESTION_SET_ID and QUESTION_SET_ID.
     *
     * @throws JSONException thrown to indicate a problem with the JSON.
     */
    @Test(priority = 1)
    public void removesChallengeQuestionTest() throws JSONException {

        addsNewChallengeQuestionSetTest();
        given().auth().preemptive().basic(USER_NAME, PASSWORD).contentType(ContentType.JSON)
                .header(HttpHeaders.ACCEPT, ContentType.JSON)
                .log().ifValidationFails()
                .when()
                .delete(RESOURCE_PATH + "/{challenge-set-id}/questions/{question-id}", QUESTION_SET_ID, QUESTION_ID)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NO_CONTENT)
                .log().ifValidationFails()
                .extract().response();
    }

    /**
     * This test method validate api which retrieve challenge question object by QUESTION_SET_ID.
     *
     * @throws JSONException thrown to indicate a problem with the JSON.
     */
    @Test(dependsOnMethods = { "addsNewChallengeQuestionSetTest" })
    public void getsChallengeQuestionByIdTest() throws JSONException {

        Response response = given().auth().preemptive().basic(USER_NAME, PASSWORD).contentType(ContentType.JSON)
                .header(HttpHeaders.ACCEPT, ContentType.JSON).log().ifValidationFails().when().get(RESOURCE_PATH
                        + "/{challenge-set-id}", QUESTION_SET_ID).then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .and()
                .contentType(ContentType.JSON)
                .log().ifValidationFails()
                .extract().response();

        getKeyValueInJSONObject(response.asString(), QUESTION_SET_ID, QUESTION_ID, CHALLENGE_QUESTION, LOCALE);
    }

    /**
     * This test method validate retrieve challenge question api response.
     *
     * @param questionSetId string example: challengeQuestion1
     * @param question      string, represent challenge question
     * @param questionId    string, challenge question id
     * @throws JSONException thrown to indicate a problem with the JSON.
     */
    private void getsChallengeQuestionTest(String questionSetId, String question, String questionId)
            throws JSONException {

        Response response = given().auth().preemptive().basic(USER_NAME, PASSWORD).contentType(ContentType.JSON)
                .header(HttpHeaders.ACCEPT, ContentType.JSON).log().ifValidationFails().when().get(RESOURCE_PATH).then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .and()
                .contentType(ContentType.JSON)
                .extract().response();

        List<String> actualQuestionSetId = getValuesByKeyInJSONArray(response.asString(), QUESTION_SET_ID_ATTRIBUTE);
        Assert.assertTrue(actualQuestionSetId.contains(questionSetId), "Provided question setId value does not exist");

        List<String> actualQuestionId = getValueByKeyInNestedArray(response.asString(), QUESTION_OBJECT_ATTRIBUTE,
                QUESTION_ID_ATTRIBUTE);
        Assert.assertTrue(actualQuestionId.contains(questionId), "Provided QUESTION_ID value does not exist");

        List<String> actualQuestion = getValueByKeyInNestedArray(response.asString(), QUESTION_OBJECT_ATTRIBUTE,
                QUESTION_ATTRIBUTE);
        Assert.assertTrue(actualQuestion.contains(question), "Provided question value does not exist");
    }

    /**
     * The method is used to validate the response which as JSON Array.
     *
     * @param jsonArrayStr represent JSON Array.
     * @param key will give you the value of a particular key of the JSON Array.
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
     * The method is used to validate the response which has JSON nested Array.
     *
     * @param jsonArrayStr   represent JSON Array.
     * @param key will give you the value of a particular key of the JSON Array.
     * @param nestedArrayKey represent Nested JSON Array.
     * @return will return the corresponding values.
     * @throws JSONException thrown to indicate a problem with the JSON.
     */
    private List<String> getValueByKeyInNestedArray(String jsonArrayStr, String key,
            String nestedArrayKey) throws JSONException {

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

    /**
     * The method is used to validate the response which as JSON object.
     *
     * @param responseasString string, the response which will return as Json Object
     * @param setIdKey         string, challenge question setId
     * @param questionIdKey    string, challenge question id
     * @param questionKey      String, challenge question
     * @param localekey        String, challenge question LOCALE
     * @throws JSONException thrown to indicate a problem with the JSON.
     */
    private void getKeyValueInJSONObject(String responseasString, String setIdKey, String questionIdKey,
            String questionKey, String localekey) throws JSONException {

        JSONObject obj = new JSONObject(responseasString);
        String actualQuestionSetId = (String) obj.get(QUESTION_SET_ID_ATTRIBUTE);
        Assert.assertTrue(actualQuestionSetId.contains(setIdKey), "Provided question setId value does not exist");
        JSONArray questions = obj.getJSONArray(QUESTION_OBJECT_ATTRIBUTE);

        for (int i = 0; i < questions.length(); i++) {
            JSONObject jsonObject = questions.getJSONObject(i);
            if (jsonObject.getString(QUESTION_ID_ATTRIBUTE).equals(questionIdKey)) {

                String question = jsonObject.getString(QUESTION_ATTRIBUTE);
                Assert.assertTrue(question.contains(questionKey), "Provided question value does not exist");

                String locale = jsonObject.getString(LOCALE_ATTRIBUTE);
                Assert.assertTrue(locale.contains(localekey), "Provided LOCALE value does not exist");
            }
        }
    }
}

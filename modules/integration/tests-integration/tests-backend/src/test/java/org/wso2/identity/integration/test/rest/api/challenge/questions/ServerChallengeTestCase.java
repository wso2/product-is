/*
 *Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *WSO2 Inc. licenses this file to you under the Apache License,
 *Version 2.0 (the "License"); you may not use this file except
 *in compliance with the License.
 *You may obtain a copy of the License at
 *
 *http://www.apache.org/licenses/LICENSE-2.0
 *
 *Unless required by applicable law or agreed to in writing,
 *software distributed under the License is distributed on an
 *"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *KIND, either express or implied.  See the License for the
 *specific language governing permissions and limitations
 *under the License.
 */
package org.wso2.identity.integration.test.rest.api.challenge.questions;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.wso2.identity.integration.test.rest.api.challenge.questions.model.ServerChallengeModel;

import java.util.ArrayList;
import java.util.List;

import static io.restassured.RestAssured.given;

/**
 * This class is for ServerChallengeQuestion rest api test cases.
 */
public class ServerChallengeTestCase {


    private final String username = "admin";
    private final String password = "admin";
    private static final String serverUrl = "https://localhost:9853";
    private static final String resourcePath = "/api/server/v1/challenges";
    private static final String questionId = "question7";
    private final static String questionSetId = "challengeQuestion7";
    private static final String locale = "en_US";
    private static final String challengeQuestion = "What is your best friend name?";
    private static final String questionSetIdAttribute = "questionSetId";
    private static final String questionIdAttribute = "questionId";
    private static final String questionAttribute = "question";
    private static final String questionObjectAttribute = "questions";
    private static final String localeAttribute = "locale";

    /**
     * RestAssured.baseURI, which sets the base URI statically.
     */
    @BeforeTest(alwaysRun = true)
    public void testinit() {

        RestAssured.baseURI = serverUrl;

    }

    /**
     * The method is used to add new challenge question set.
     *
     * @throws JSONException thrown to indicate a problem with the JSON.
     */
    @Test
    public void addsNewChallengeQuestionSetTest() throws JSONException {

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
                then().assertThat().statusCode(201).log().ifValidationFails();

        getsChallengeQuestionTest(questionSetId, challengeQuestion, questionId);
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

        given().auth().preemptive().basic(username, password).contentType(ContentType.JSON)
                .header("Accept", ContentType.JSON)
                .body(request)
                .log().all()
                .when()
                .put(resourcePath + "/{challenge-set-id}", questionSetId)
                .then()
                .log().all()
                .assertThat()
                .statusCode(200).log().ifValidationFails();
        getsChallengeQuestionTest(questionSetId, challengeQuestion, questionID);
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

        given().auth().preemptive().basic(username, password).contentType(ContentType.JSON)
                .header("Accept", ContentType.JSON)
                .body(json)
                .log().all()
                .when()
                .patch(resourcePath + "/{challenge-set-id}", questionSetId)
                .then()
                .log().all()
                .assertThat()
                .statusCode(201).log().ifValidationFails();

        getsChallengeQuestionTest(questionSetId, challengeQuestion, questionID);

    }

    /**
     * The test method validate the api which delete challenge question by questionSetId.
     */
    @Test(
          dependsOnMethods = {
                  "addsNewChallengeQuestionSetTest", "updateChallengeQuestionPUTTest",
                  "updateChallengeQuestionPATCHTest"
          })
    public void removesChallengeQuestionSetTest() {

        given().auth().preemptive().basic(username, password).contentType(ContentType.JSON)
                .header("Accept", ContentType.JSON)
                .log().all()
                .when()
                .delete(resourcePath + "/{challenge-set-id}", questionSetId)
                .then()
                .log().all()
                .assertThat()
                .statusCode(204)
                .log().ifValidationFails()
                .extract().response();

    }

    /**
     * The test method validate the api which delete challenge question by questionSetId and questionSetId.
     *
     * @throws JSONException thrown to indicate a problem with the JSON.
     */
    @Test(priority = 1)
    public void removesChallengeQuestionTest() throws JSONException {
        addsNewChallengeQuestionSetTest();
        given().auth().preemptive().basic(username, password).contentType(ContentType.JSON)
                .header("Accept", ContentType.JSON)
                .log().all()
                .when()
                .delete(resourcePath + "/{challenge-set-id}/questions/{question-id}", questionSetId, questionId)
                .then()
                .log().all()
                .assertThat()
                .statusCode(204)
                .log().ifValidationFails()
                .extract().response();

    }

    /**
     * This test method validate api which retrieve challenge question object by questionSetId.
     * @throws JSONException thrown to indicate a problem with the JSON.
     */
    @Test(dependsOnMethods = {"addsNewChallengeQuestionSetTest"})
    public void getsChallengeQuestionByIdTest() throws JSONException {

        Response response = given().auth().preemptive().basic(username, password).contentType(ContentType.JSON)
                .header("Accept", ContentType.JSON).log().all().when().get(resourcePath
                        + "/{challenge-set-id}", questionSetId).then()
                .log().all()
                .assertThat()
                .statusCode(200)
                .and()
                .contentType(ContentType.JSON)
                .log().ifValidationFails()
                .extract().response();

        recurseKeys(response.asString(), questionSetId, questionId, challengeQuestion, locale);
    }

    /**
     * This test method validate retrieve challenge question api response.
     *
     * @param questionSetId string example: challengeQuestion1
     * @param question string, represent challenge question
     * @param questionId  string, challenge question id
     * @throws JSONException thrown to indicate a problem with the JSON.
     */
    @Test
    private void getsChallengeQuestionTest(String questionSetId, String question, String questionId)
            throws JSONException {

        Response response = given().auth().preemptive().basic(username, password).contentType(ContentType.JSON)
                .header("Accept", ContentType.JSON).log().all().when().get(resourcePath).then()
                .log().all()
                .assertThat()
                .statusCode(200)
                .and()
                .contentType(ContentType.JSON)
                .extract().response();

        List<String> actualQuestionSetId = getValuesByKeyInJSONArray(response.asString(), questionSetIdAttribute);
        Assert.assertTrue(actualQuestionSetId.contains(questionSetId), "Provided question setId value does not exist");

        List<String> actualQuestionId = getValueKeyInNestedArray(response.asString(), questionObjectAttribute,
                questionIdAttribute);
        Assert.assertTrue(actualQuestionId.contains(questionId), "Provided questionId value does not exist");

        List<String> actualQuestion = getValueKeyInNestedArray(response.asString(), questionObjectAttribute,
                questionAttribute);
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
     * @param jsonArrayStr represent JSON Array.
     * @param key will give you the value of a particular key of the JSON Array.
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

    /**
     *The method is used to validate the response which as JSON object.
     *
     * @param responseasString string, the response which will return as Json Object
     * @param setIdKey string, challenge question setId
     * @param questionIdKey string, challenge question id
     * @param questionKey String, challenge question
     * @param localekey String, challenge question locale
     * @throws JSONException thrown to indicate a problem with the JSON.
     */
    private void recurseKeys(String responseasString, String setIdKey, String questionIdKey, String questionKey,
            String localekey)
            throws
            JSONException {
        JSONObject obj = new JSONObject(responseasString);
        String actualQuestionSetId = (String) obj.get(questionSetIdAttribute);
        Assert.assertTrue(actualQuestionSetId.contains(setIdKey), "Provided question setId value does not exist");
        JSONArray contacts = obj.getJSONArray(questionObjectAttribute);

        for (int i = 0; i < contacts.length(); i++) {
            JSONObject c = contacts.getJSONObject(i);
            if (c.getString(questionIdAttribute).equals(questionIdKey)) {

                String question = c.getString(questionAttribute);
                Assert.assertTrue(question.contains(questionKey), "Provided question value does not exist");

                String locale = c.getString(localeAttribute);
                Assert.assertTrue(locale.contains(localekey), "Provided locale value does not exist");
            }
        }
    }
}

package org.wso2.identity.scenarios.test.scim2;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.testng.annotations.*;
import org.wso2.identity.scenarios.commons.SCIM2CommonClient;
import org.wso2.identity.scenarios.commons.ScenarioTestBase;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.wso2.identity.scenarios.commons.util.Constants.IS_HTTPS_URL;
import static org.wso2.identity.scenarios.commons.util.IdentityScenarioUtil.getJSONFromResponse;
import static org.wso2.identity.scenarios.test.scim2.SCIMConstants.*;

public class UserRetrieveWithoutFilterParameterTestCase extends ScenarioTestBase {

    private JSONObject userJSON_1;
    private JSONObject userJSON_2;
    private JSONObject userJSON_3;
    private JSONObject userJSON_4;
    private JSONObject userJSON_5;
    private String username;
    private String password;
    private String tenantDomain;
    private JSONObject requests;
    List<JSONObject> userList;
    List<String> userIdList;

    private CloseableHttpClient client;
    private SCIM2CommonClient scim2Client;
    private String userId;

    private static final String SCIM2_USERS_LOCATION = "scim2.users.location";
    private static final String SCIM2_REQUESTS_LOCATION = "scim2.requests.location";
    private static JSONParser parser = new JSONParser();

    @Factory(dataProvider = "scim2userConfig")
    public UserRetrieveWithoutFilterParameterTestCase(JSONObject userJSON_1, JSONObject userJSON_2, JSONObject userJSON_3,
                                                      JSONObject userJSON_4, JSONObject userJSON_5, JSONObject requests, String username,
                                                      String password, String tenantDomain){
        this.userJSON_1 = userJSON_1;
        this.userJSON_2 = userJSON_2;
        this.userJSON_3 = userJSON_3;
        this.userJSON_4 = userJSON_4;
        this.userJSON_5 = userJSON_5;
        this.username = username;
        this.password = password;
        this.tenantDomain = tenantDomain;
        this.requests = requests;
    }

    @DataProvider(name = "scim2userConfig")
    public static Object[][] scim2userConfig() throws Exception {
        return new Object[][] {
                {
                        getUserJSON("scim2user1.json"), getUserJSON("scim2user2.json"),
                        getUserJSON("scim2user3.json"), getUserJSON("scim2user4.json"),
                        getUserJSON("scim2user5.json"), getRequestJSON("request1.json"),ADMIN_USERNAME,
                        ADMIN_PASSWORD, SUPER_TENANT_DOMAIN
                }
        };
    }

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.init();
        scim2Client = new SCIM2CommonClient(getDeploymentProperty(IS_HTTPS_URL));
        client = HttpClients.createDefault();

        userIdList = new ArrayList<>();
        userList = new ArrayList<>();
        userList.add(userJSON_1);
        userList.add(userJSON_2);
        userList.add(userJSON_3);
        userList.add(userJSON_4);
        userList.add(userJSON_5);

        for (JSONObject userJSON : userList) {

            // create users
            HttpResponse response = scim2Client.provisionUser(client, userJSON, username, password);
            assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_CREATED,
                    "User has not been created successfully");
            JSONObject returnedUserJSON = getJSONFromResponse(response);
            userId = returnedUserJSON.get(SCIMConstants.ID_ATTRIBUTE).toString();

            // add user id to userIdList array
            userIdList.add(userId);

            // validate user creation
            assertNotNull(userId, "SCIM2 user id not available in the response.");
        }
    }

    @AfterClass(alwaysRun = true)
    public void testClear() throws Exception {

        for (String userId : userIdList) {

            // delete created users from database
            HttpResponse deleteResponse = scim2Client.deleteUser(client, userId, ADMIN_USERNAME, ADMIN_PASSWORD);

            // validate user deletion
            assertEquals(deleteResponse.getStatusLine().getStatusCode(), HttpStatus.SC_NO_CONTENT, "Failed to delete the user");
        }
        client.close();
    }

    @Test(description = "1.2.1.1")
    public void testSCIM2GetUser() throws Exception {

        Iterator<String> keys = requests.keySet().iterator();

        while(keys.hasNext()) {

            String key = keys.next();
            JSONObject requestJson = (JSONObject)requests.get(key);
            JSONObject paramsJson = (JSONObject)requestJson.get(QUERY_PARAMS_ATTRIBUTE);
            JSONObject resultsJson = (JSONObject)requestJson.get(RESULTS_ATTRIBUTE);

            String startIndex = paramsJson.get(SCIM2_QUERY_PARAM_START_INDEX_ATTRIBUTE).toString();
            String count = paramsJson.get(SCIM2_QUERY_PARAM_COUNT_ATTRIBUTE).toString();
            String domain = null;
            if (paramsJson.get(SCIM2_QUERY_PARAM_DOMAIN_ATTRIBUTE) != null) {
                domain = paramsJson.get(SCIM2_QUERY_PARAM_DOMAIN_ATTRIBUTE).toString();
            }

            HttpResponse response = scim2Client.getUser(client, startIndex, count, domain, ADMIN_USERNAME, ADMIN_PASSWORD);
            // validate user retrieve
            assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_OK, "Failed to retrieve the user");

            // get JSON from response
            JSONObject applicationJSON = getJSONFromResponse(response);
            // validate response further
            validateResponse(paramsJson, resultsJson, applicationJSON);
        }
    }

    /**
     * Get user json object from a file.
     *
     * @param fileName File name.
     * @return User json object.
     * @throws Exception Exception.
     */

    private static JSONObject getUserJSON(String fileName) throws Exception {

        return (JSONObject) parser.parse(new FileReader(getFilePath(SCIM2_USERS_LOCATION, fileName)));
    }

    /**
     * Get query params json object from a file.
     *
     * @param fileName File name.
     * @return query params json object.
     * @throws Exception Exception.
     */
    private static JSONObject getRequestJSON(String fileName) throws Exception {

        return (JSONObject) parser.parse(new FileReader(getFilePath(SCIM2_REQUESTS_LOCATION, fileName)));
    }

    /**
     * Get file path.
     *
     * @param folderPath Folder path.
     * @param fileName   File name.
     * @return File path.
     * @throws Exception Exception.
     */
    private static String getFilePath(String folderPath, String fileName) throws Exception {

        Path path = Paths.get(System.getProperty(folderPath) + fileName);
        if (!Files.exists(path)) {
            throw new FileNotFoundException("Failed to find file: " + path.toString());
        }
        return path.toString();
    }

    /**
     * Validate response.
     *
     * @param paramsJson      Query Parameters.
     * @param resultsJSON     Expected Results.
     * @param applicationJSON Results of Response.
     * @throws Exception      Exception.
     */
    private void validateResponse(JSONObject paramsJson, JSONObject resultsJSON, JSONObject applicationJSON) throws IOException {

        // validate users count
        assertEquals(applicationJSON.get(SCIM2_RESPONCE_TOTAL_RESULTS_ATTRIBUTE).toString(),
                paramsJson.get(SCIM2_QUERY_PARAM_COUNT_ATTRIBUTE).toString(), "Received users count is invalid in " +
                        "UserRetrieveWithoutFilterParameterTestCase. Requested users count: "
                        + paramsJson.get(SCIM2_QUERY_PARAM_COUNT_ATTRIBUTE).toString() + ", Received user count: "
                        + applicationJSON.get(SCIM2_RESPONCE_TOTAL_RESULTS_ATTRIBUTE).toString() );

        // validate users startIndex
        assertEquals(applicationJSON.get(SCIM2_RESPONCE_START_INDEX_ATTRIBUTE).toString(),
                paramsJson.get(SCIM2_QUERY_PARAM_START_INDEX_ATTRIBUTE).toString(), "StartIndex is invalid in " +
                        "UserRetrieveWithoutFilterParameterTestCase. Requested startIndex: "
                        + paramsJson.get(SCIM2_QUERY_PARAM_START_INDEX_ATTRIBUTE).toString()
                        + ", Received startIndex: " + applicationJSON.get(SCIM2_RESPONCE_START_INDEX_ATTRIBUTE).toString() );

        // validate all users id and user names in first item page
        int itemsPerPage = Integer.parseInt(applicationJSON.get(SCIM2_RESPONCE_ITEMS_PER_PAGE_ATTRIBUTE).toString());

        JSONArray resource = (JSONArray)applicationJSON.get(SCIM2_RESPONCE_RESOURCES_ATTRIBUTE);
        JSONArray userNamesJson = (JSONArray)resultsJSON.get(USER_NAMES_ATTRIBUTE);
        for (int i = 0; i < itemsPerPage; i++){

            JSONObject resourceJson = (JSONObject)resource.get(i);
            // validate id
            String userId = resourceJson.get(SCIM2_RESPONCE_USER_ID_ATTRIBUTE).toString();
            assertNotNull(userId, "SCIM2 user id not available in the response.");

            // validate user name
            JSONObject user = (JSONObject)userNamesJson.get(i);
            assertEquals(resourceJson.get(SCIM2_RESPONCE_USER_NAME_ATTRIBUTE).toString(), user.get(NAME_ATTRIBUTE).toString(),
                    "Received user name is invalid in UserRetrieveWithoutFilterParameterTestCase. " +
                            "Requested user name: " + user.get(NAME_ATTRIBUTE).toString() + ", Received user name: "
                            + resourceJson.get(SCIM2_RESPONCE_USER_NAME_ATTRIBUTE).toString() );
        }
    }
}

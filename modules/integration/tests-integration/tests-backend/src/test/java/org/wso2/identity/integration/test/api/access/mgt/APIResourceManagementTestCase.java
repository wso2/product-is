package org.wso2.identity.integration.test.api.access.mgt;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.identity.application.common.model.APIResource;
import org.wso2.carbon.identity.application.common.model.Scope;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class APIResourceManagementTestCase extends APIAccessManagementBaseTestCase {

    private static final Log LOG = LogFactory.getLog(APIResourceManagementTestCase.class);
    private static final String API_1_NAME = "Booking API";
    private static final String API_1_EDITED_NAME = "Bookings API Edited";
    private static final String API_1_DESCRIPTION = "This is a test API created by an integration test";
    private static final String API_1_DESCRIPTION_EDITED = "This is a test API created by an integration test";
    private static final String API_1_IDENTIFIER = "/bookings";
    private static final String API_2_NAME = "Flight API";
    private static final String API_2_EDITED_NAME = "Flight API Edited";
    private static final String API_2_DESCRIPTION = "This is a test API created by an integration test";
    private static final String API_2_DESCRIPTION_EDITED = "This is a test API created by an integration test";
    private static final String API_2_IDENTIFIER = "/flight";
    private static final boolean API_REQUIRES_AUTHORIZATION = true;
    private static final String SCOPE_1_NAME = "read_bookings";
    private static final String SCOPE_1_DISPLAY_NAME = "Read Bookings";
    private static final String SCOPE_1_DESCRIPTION = "Read all the bookings in the system";
    private static final String SCOPE_2_NAME = "write_bookings";
    private static final String SCOPE_2_DISPLAY_NAME = "Write Bookings";
    private static final String SCOPE_2_DESCRIPTION = "Write bookings to the system";
    private static final String SCOPE_3_NAME = "delete_bookings";
    private static final String SCOPE_3_DISPLAY_NAME = "Delete Bookings";
    private static final String SCOPE_3_DESCRIPTION = "Delete bookings from the system";
    private static final String SCOPE_4_NAME = "read_flights";
    private static final String SCOPE_4_DISPLAY_NAME = "Read Flights";
    private static final String SCOPE_4_DESCRIPTION = "Read flights from the system";

    private String apiResourceId;
    private String scope1Id;
    private String scope2Id;

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {

        super.init();
    }

    @AfterClass(alwaysRun = true)
    public void testConclude() {

        super.conclude();
    }

    @DataProvider(name = "APIResourceMgtConfigProvider")
    public static Object[][] APIResourceMgtConfigProvider() {

        return new Object[][]{
                {TestUserMode.SUPER_TENANT_ADMIN}
        };
    }

    @Factory(dataProvider = "APIResourceMgtConfigProvider")
    public APIResourceManagementTestCase(TestUserMode userMode) throws Exception {

        super(userMode);
    }

    @Test
    public void testAddAPIResource() throws Exception {

        List<Scope> scopeList = new ArrayList<>();

        Scope scope1 = new Scope.ScopeBuilder()
                .name(SCOPE_1_NAME)
                .displayName(SCOPE_1_DISPLAY_NAME)
                .description(SCOPE_1_DESCRIPTION)
                .build();

        Scope scope2 = new Scope.ScopeBuilder()
                .name(SCOPE_2_NAME)
                .displayName(SCOPE_2_DISPLAY_NAME)
                .description(SCOPE_2_DESCRIPTION)
                .build();

        scopeList.add(scope1);
        scopeList.add(scope2);

        APIResource apiResource = new APIResource.APIResourceBuilder()
                .name(API_1_NAME)
                .identifier(API_1_IDENTIFIER)
                .description(API_1_DESCRIPTION)
                .requiresAuthorization(API_REQUIRES_AUTHORIZATION)
                .scopes(scopeList)
                .build();

        HttpResponse response = createAPIResource(apiResource);
        assertNotNull(response, "API resource creation failed");
        assertEquals(response.getStatusLine().getStatusCode(), 201, "API resource creation failed");
        JSONObject responseObj = new JSONObject(EntityUtils.toString(response.getEntity()));
        EntityUtils.consume(response.getEntity());
        apiResourceId = responseObj.getString(API_ID_ATTRIBUTE);
        JSONArray scopeArray = responseObj.getJSONArray(API_SCOPE_ATTRIBUTE);
        for (int i = 0; i < scopeArray.length(); i++) {
            JSONObject scopeObj = (JSONObject) scopeArray.get(i);
            if (scopeObj.getString(API_SCOPE_NAME_ATTRIBUTE).equals(SCOPE_1_NAME)) {
                scope1Id = scopeObj.getString(API_SCOPE_ID_ATTRIBUTE);
            } else if (scopeObj.getString(API_SCOPE_NAME_ATTRIBUTE).equals(SCOPE_2_NAME)) {
                scope2Id = scopeObj.getString(API_SCOPE_ID_ATTRIBUTE);
            }
        }
        HttpResponse getResponse = getAPIResource(apiResourceId);
        assertNotNull(getResponse, "API resource retrieval failed");
        assertEquals(getResponse.getStatusLine().getStatusCode(), 200, "API resource retrieval failed");
        JSONObject getResponseObj = new JSONObject(EntityUtils.toString(getResponse.getEntity()));
        EntityUtils.consume(getResponse.getEntity());
        assertEquals(getResponseObj.getString(API_ID_ATTRIBUTE), apiResourceId, "API resource retrieval failed");
    }

    @Test(dependsOnMethods = "testAddAPIResource")
    public void testAddDuplicateAPIResource() throws JSONException, IOException {

        APIResource apiResource = new APIResource.APIResourceBuilder()
                .name(API_1_NAME)
                .identifier(API_1_IDENTIFIER)
                .description(API_1_DESCRIPTION)
                .requiresAuthorization(API_REQUIRES_AUTHORIZATION)
                .scopes(new ArrayList<>())
                .build();

        HttpResponse response = createAPIResource(apiResource);
        assertNotNull(response, "API resource creation request failed");
        assertEquals(response.getStatusLine().getStatusCode(), 409, "Expected status code not received");
    }

    @Test(dependsOnMethods = "testAddDuplicateAPIResource")
    public void testAddAPIWithDuplicateScope() throws JSONException, IOException {

        List<Scope> scopeList = new ArrayList<>();

        Scope scope1 = new Scope.ScopeBuilder()
                .name(SCOPE_1_NAME)
                .displayName(SCOPE_1_DISPLAY_NAME)
                .description(SCOPE_1_DESCRIPTION)
                .build();

        scopeList.add(scope1);

        APIResource apiResource = new APIResource.APIResourceBuilder()
                .name(API_1_NAME)
                .identifier(API_1_IDENTIFIER)
                .description(API_1_DESCRIPTION)
                .requiresAuthorization(API_REQUIRES_AUTHORIZATION)
                .scopes(scopeList)
                .build();

        HttpResponse response = createAPIResource(apiResource);
        assertNotNull(response, "API resource creation request failed");
        assertEquals(response.getStatusLine().getStatusCode(), 409, "Expected status code not received");
    }

    @Test(dependsOnMethods = "testAddAPIResource")
    public void testGetAPIResources() throws JSONException, IOException {

        HttpGet request = new HttpGet(SERVER_URL + API_RESOURCE_ENDPOINT + BUSINESS_API_FILTER_QUERY);
        request.setHeader("Content-Type", "application/json");
        request.setHeader("Authorization", getAuthzHeader());
        HttpResponse response = client.execute(request);
        assertNotNull(response, "API resource retrieval failed");
        assertEquals(response.getStatusLine().getStatusCode(), 200, "API resource retrieval failed");
        JSONObject responseObj = new JSONObject(EntityUtils.toString(response.getEntity()));
        EntityUtils.consume(response.getEntity());
        JSONArray apiResourceArray = responseObj.getJSONArray("apiResources");
        assertEquals(apiResourceArray.length(), 1, "API resource count expected to be 1");
        JSONObject apiResource = apiResourceArray.getJSONObject(0);
        assertEquals(apiResource.getString(API_ID_ATTRIBUTE), apiResourceId, "API resource retrieval failed");
    }

    @Test(dependsOnMethods = "testGetAPIResources")
    public void testGetAPIScopes() throws IOException, JSONException {

        HttpGet request = new HttpGet(SERVER_URL + API_RESOURCE_ENDPOINT + "/" + apiResourceId + SCOPE_PATH);
        request.setHeader("Content-Type", "application/json");
        request.setHeader("Authorization", getAuthzHeader());
        HttpResponse response = null;
        response = client.execute(request);
        assertNotNull(response, "API scope retrieval failed");
        assertEquals(response.getStatusLine().getStatusCode(), 200, "API scope retrieval failed");
        JSONArray scopeArray = new JSONArray(EntityUtils.toString(response.getEntity()));
        EntityUtils.consume(response.getEntity());
        assertEquals(scopeArray.length(), 2, "API scope count expected to be 2");
        for (int i = 0; i < scopeArray.length(); i++) {
            JSONObject scopeObj = (JSONObject) scopeArray.get(i);
            if (scopeObj.getString(API_SCOPE_NAME_ATTRIBUTE).equals(SCOPE_1_NAME)) {
                assertEquals(scopeObj.getString(API_SCOPE_ID_ATTRIBUTE), scope1Id, "API scope retrieval failed");
            } else if (scopeObj.getString(API_SCOPE_NAME_ATTRIBUTE).equals(SCOPE_2_NAME)) {
                assertEquals(scopeObj.getString(API_SCOPE_ID_ATTRIBUTE), scope2Id, "API scope retrieval failed");
            }
        }
    }

    @Test(dependsOnMethods = "testAddAPIResource")
    public void testPatchAPIResource() throws JSONException, IOException {

        HttpPatch request = new HttpPatch(SERVER_URL + API_RESOURCE_ENDPOINT + "/" + apiResourceId);
        request.setHeader("Content-Type", "application/json");
        request.setHeader("Authorization", getAuthzHeader());
        JSONObject rootObject = new JSONObject();
        rootObject.put(API_NAME_ATTRIBUTE, API_1_EDITED_NAME);
        rootObject.put(API_DESCRIPTION_ATTRIBUTE, API_1_DESCRIPTION_EDITED);
        JSONArray scopeArray = new JSONArray();
        JSONObject scopeObject = new JSONObject();
        scopeObject.put(API_SCOPE_NAME_ATTRIBUTE, SCOPE_3_NAME);
        scopeObject.put(API_SCOPE_DISPLAY_NAME_ATTRIBUTE, SCOPE_3_DISPLAY_NAME);
        scopeObject.put(API_SCOPE_DESCRIPTION_ATTRIBUTE, SCOPE_3_DESCRIPTION);
        scopeArray.put(scopeObject);
        rootObject.put(ADDED_SCOPE_ATTRIBUTE, scopeArray);
        StringEntity entity = new StringEntity(rootObject.toString());
        request.setEntity(entity);
        HttpResponse response = client.execute(request);
        assertNotNull(response, "API resource update failed");
        assertEquals(response.getStatusLine().getStatusCode(), 204, "API resource update failed");
        // Check whether the API resource is updated.
        HttpResponse getResponse = getAPIResource(apiResourceId);
        JSONObject responseObj = new JSONObject(EntityUtils.toString(getResponse.getEntity()));
        EntityUtils.consume(response.getEntity());
        assertEquals(responseObj.getString(API_ID_ATTRIBUTE), apiResourceId, "API resource update failed");
        assertEquals(responseObj.getString(API_NAME_ATTRIBUTE), API_1_EDITED_NAME, "API resource update failed");
        assertEquals(responseObj.getString(API_DESCRIPTION_ATTRIBUTE), API_1_DESCRIPTION_EDITED,
                "API resource update failed");
        JSONArray scopeResponseArray = responseObj.getJSONArray(API_SCOPE_ATTRIBUTE);
        assertEquals(scopeResponseArray.length(), 3, "API scope count expected to be 3");
        for (int i = 0; i < scopeResponseArray.length(); i++) {
            JSONObject scopeObj = (JSONObject) scopeResponseArray.get(i);
            if (scopeObj.getString(API_SCOPE_NAME_ATTRIBUTE).equals(SCOPE_1_NAME)) {
                assertEquals(scopeObj.getString(API_SCOPE_ID_ATTRIBUTE), scope1Id, "API scope retrieval failed");
            } else if (scopeObj.getString(API_SCOPE_NAME_ATTRIBUTE).equals(SCOPE_2_NAME)) {
                assertEquals(scopeObj.getString(API_SCOPE_ID_ATTRIBUTE), scope2Id, "API scope retrieval failed");
            } else if (scopeObj.getString(API_SCOPE_NAME_ATTRIBUTE).equals(SCOPE_3_NAME)) {
                assertEquals(scopeObj.getString(API_SCOPE_DISPLAY_NAME_ATTRIBUTE), SCOPE_3_DISPLAY_NAME,
                        "API scope retrieval failed");
            }
        }
    }

    @Test(dependsOnMethods = "testPatchAPIResource")
    public void testPutScopes() throws IOException, JSONException {

        HttpPut request = new HttpPut(SERVER_URL + API_RESOURCE_ENDPOINT + "/" + apiResourceId + SCOPE_PATH);
        request.setHeader("Content-Type", "application/json");
        request.setHeader("Authorization", getAuthzHeader());
        JSONArray scopeArray = new JSONArray();

        // Remove existing scopes from the API resource.
        StringEntity entity = new StringEntity(scopeArray.toString());
        request.setEntity(entity);
        HttpResponse response = client.execute(request);
        assertNotNull(response, "API scope update failed");
        assertEquals(response.getStatusLine().getStatusCode(), 204, "API scope update failed");

        // Put scopes to the API resource.
        JSONObject scopeObject = new JSONObject();
        scopeObject.put(API_SCOPE_NAME_ATTRIBUTE, SCOPE_1_NAME);
        scopeObject.put(API_SCOPE_DISPLAY_NAME_ATTRIBUTE, SCOPE_1_DISPLAY_NAME);
        scopeObject.put(API_SCOPE_DESCRIPTION_ATTRIBUTE, SCOPE_1_DESCRIPTION);
        scopeArray.put(scopeObject);
        scopeObject = new JSONObject();
        scopeObject.put(API_SCOPE_NAME_ATTRIBUTE, SCOPE_2_NAME);
        scopeObject.put(API_SCOPE_DISPLAY_NAME_ATTRIBUTE, SCOPE_2_DISPLAY_NAME);
        scopeObject.put(API_SCOPE_DESCRIPTION_ATTRIBUTE, SCOPE_2_DESCRIPTION);
        scopeArray.put(scopeObject);

        entity = new StringEntity(scopeArray.toString());
        request.setEntity(entity);
        response = client.execute(request);
        assertNotNull(response, "API scope update failed");
        assertEquals(response.getStatusLine().getStatusCode(), 204, "API scope update failed");

        // Get API Scopes.
        HttpResponse getResponse = getAPIScopes(apiResourceId);
        JSONArray scopeResponseArray = new JSONArray(EntityUtils.toString(getResponse.getEntity()));
        EntityUtils.consume(response.getEntity());
        assertEquals(scopeResponseArray.length(), 2, "API scope count expected to be 2");
        for (int i = 0; i < scopeResponseArray.length(); i++) {
            JSONObject scopeObj = (JSONObject) scopeResponseArray.get(i);
            if (scopeObj.getString(API_SCOPE_NAME_ATTRIBUTE).equals(SCOPE_1_NAME)) {
                assertEquals(scopeObj.getString(API_SCOPE_DISPLAY_NAME_ATTRIBUTE), SCOPE_1_DISPLAY_NAME, "API scope retrieval failed");
            } else if (scopeObj.getString(API_SCOPE_NAME_ATTRIBUTE).equals(SCOPE_2_NAME)) {
                assertEquals(scopeObj.getString(API_SCOPE_DISPLAY_NAME_ATTRIBUTE), SCOPE_2_DISPLAY_NAME, "API scope retrieval failed");
            }
        }
    }

    @Test(dependsOnMethods = "testPutScopes")
    public void testPutDuplicateScopes() throws IOException, JSONException {

        Scope scope = new Scope.ScopeBuilder()
                .name(SCOPE_4_NAME)
                .displayName(SCOPE_4_DISPLAY_NAME)
                .description(SCOPE_4_DESCRIPTION)
                .build();
        HttpResponse createResponse = createAPIResource(new APIResource.APIResourceBuilder()
                .name(API_2_NAME)
                .identifier(API_2_IDENTIFIER)
                .description(API_2_DESCRIPTION)
                .requiresAuthorization(API_REQUIRES_AUTHORIZATION)
                .scopes(Collections.singletonList(scope))
                .build());

        assertNotNull(createResponse, "API resource creation failed");
        assertEquals(createResponse.getStatusLine().getStatusCode(), 201, "API resource creation failed");

        HttpPut request = new HttpPut(SERVER_URL + API_RESOURCE_ENDPOINT + "/" + apiResourceId + SCOPE_PATH);
        request.setHeader("Content-Type", "application/json");
        request.setHeader("Authorization", getAuthzHeader());
        JSONArray scopeArray = new JSONArray();
        JSONObject scopeObject = new JSONObject();
        scopeObject.put(API_SCOPE_NAME_ATTRIBUTE, SCOPE_4_NAME);
        scopeObject.put(API_SCOPE_DISPLAY_NAME_ATTRIBUTE, SCOPE_4_DISPLAY_NAME);
        scopeObject.put(API_SCOPE_DESCRIPTION_ATTRIBUTE, SCOPE_4_DESCRIPTION);
        scopeArray.put(scopeObject);

        StringEntity entity = new StringEntity(scopeArray.toString());
        request.setEntity(entity);
        HttpResponse response = client.execute(request);
        assertNotNull(response, "API scope update failed");
        assertEquals(response.getStatusLine().getStatusCode(), 409, "API scope update failed");
    }

    @Test(dependsOnMethods = "testPutScopes")
    public void testDeleteScope() throws IOException, JSONException {

        HttpDelete request = new HttpDelete(SERVER_URL + API_RESOURCE_ENDPOINT + "/" + apiResourceId + SCOPE_PATH + "/" + SCOPE_1_NAME);
        request.setHeader("Content-Type", "application/json");
        request.setHeader("Authorization", getAuthzHeader());
        HttpResponse response = client.execute(request);
        assertNotNull(response, "API scope deletion failed");
        assertEquals(response.getStatusLine().getStatusCode(), 204, "API scope deletion failed");

        // Get API Scopes.
        HttpResponse getResponse = getAPIScopes(apiResourceId);
        JSONArray scopeResponseArray = new JSONArray(EntityUtils.toString(getResponse.getEntity()));
        EntityUtils.consume(response.getEntity());
        assertEquals(scopeResponseArray.length(), 1, "API scope count expected to be 1");
    }
}

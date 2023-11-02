package org.wso2.identity.integration.test.api.access.mgt;

import io.netty.util.internal.StringUtil;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
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
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class APIResourceManagementFailureTestCase extends APIAccessManagementBaseTestCase {

    private static final String API_1_NAME = "Files API";
    private static final String API_1_DESCRIPTION = "This is a test API created by an integration test";
    private static final String API_1_IDENTIFIER = "/files";
    private static final boolean API_REQUIRES_AUTHORIZATION = true;
    private static final String SCOPE_1_DISPLAY_NAME = "Read Files";
    private static final String SCOPE_1_DESCRIPTION = "Read all the files in the system";

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
    public APIResourceManagementFailureTestCase(TestUserMode userMode) throws Exception {

        super(userMode);
    }

    @Test(priority = 0)
    public void testAddAPIWithInvalidScope() throws JSONException, IOException {

        List<Scope> scopeList = new ArrayList<>();

        Scope scope1 = new Scope.ScopeBuilder()
                .name(null)
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
        assertEquals(response.getStatusLine().getStatusCode(), 400, "Expected status code not received");
    }

    @Test(priority = 1)
    public void testAddInvalidAPIResource() throws JSONException, IOException {

        APIResource apiResource = new APIResource.APIResourceBuilder()
                .name(API_1_NAME)
                .identifier(null)
                .description(API_1_DESCRIPTION)
                .requiresAuthorization(API_REQUIRES_AUTHORIZATION)
                .scopes(new ArrayList<>())
                .build();

        HttpResponse response = createAPIResource(apiResource);
        assertNotNull(response, "API resource creation request failed");
        assertEquals(response.getStatusLine().getStatusCode(), 400, "Expected status code not received");
    }

    @Test(priority = 2)
    public void testPutInvalidScope() throws JSONException, IOException {

        APIResource apiResource = new APIResource.APIResourceBuilder()
                .name(API_1_NAME)
                .identifier(API_1_IDENTIFIER)
                .description(API_1_DESCRIPTION)
                .requiresAuthorization(API_REQUIRES_AUTHORIZATION)
                .scopes(new ArrayList<>())
                .build();
        HttpResponse response = createAPIResource(apiResource);
        assertNotNull(response, "API resource creation request failed");
        assertEquals(response.getStatusLine().getStatusCode(), 201, "Expected status code not received");
        JSONObject responseObj = new JSONObject(EntityUtils.toString(response.getEntity()));
        EntityUtils.consume(response.getEntity());
        String apiResourceId = responseObj.getString(API_ID_ATTRIBUTE);

        HttpPut request = getHttpPut(apiResourceId);
        response = client.execute(request);
        assertNotNull(response, "API resource update request failed");
        assertEquals(response.getStatusLine().getStatusCode(), 400, "Expected status code not received");
    }

    private HttpPut getHttpPut(String apiResourceId) throws JSONException, UnsupportedEncodingException {
        HttpPut request = new HttpPut(SERVER_URL + API_RESOURCE_ENDPOINT + "/" + apiResourceId + "/" + SCOPE_PATH);
        request.setHeader("Content-Type", "application/json");
        request.setHeader("Authorization", getAuthzHeader());
        JSONObject scopeObject = new JSONObject();
        scopeObject.put(API_SCOPE_NAME_ATTRIBUTE, StringUtil.EMPTY_STRING);
        scopeObject.put(API_SCOPE_DISPLAY_NAME_ATTRIBUTE, StringUtil.EMPTY_STRING);
        scopeObject.put(API_SCOPE_DESCRIPTION_ATTRIBUTE, StringUtil.EMPTY_STRING);

        StringEntity entity = new StringEntity(scopeObject.toString());
        request.setEntity(entity);
        return request;
    }
}

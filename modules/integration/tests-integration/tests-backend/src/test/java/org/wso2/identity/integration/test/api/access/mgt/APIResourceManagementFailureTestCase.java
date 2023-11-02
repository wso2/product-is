package org.wso2.identity.integration.test.api.access.mgt;

import org.apache.http.HttpResponse;
import org.json.JSONException;
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
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class APIResourceManagementFailureTestCase extends APIAccessManagementBaseTestCase {

    private static final String API_1_NAME = "Booking API";
    private static final String API_1_DESCRIPTION = "This is a test API created by an integration test";
    private static final String API_1_IDENTIFIER = "/bookings";
    private static final boolean API_REQUIRES_AUTHORIZATION = true;
    private static final String SCOPE_1_DISPLAY_NAME = "Read Bookings";
    private static final String SCOPE_1_DESCRIPTION = "Read all the bookings in the system";

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
    public void addAPIWithInvalidScope() throws JSONException, IOException {

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
    public void addInvalidAPIResource() throws JSONException, IOException {

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
}

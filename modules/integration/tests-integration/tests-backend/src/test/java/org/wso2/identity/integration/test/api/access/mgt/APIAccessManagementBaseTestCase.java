package org.wso2.identity.integration.test.api.access.mgt;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.identity.application.common.model.APIResource;
import org.wso2.carbon.identity.application.common.model.Scope;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.identity.integration.test.rest.api.common.RESTTestBase;

import java.io.IOException;

import javax.xml.xpath.XPathExpressionException;

public class APIAccessManagementBaseTestCase extends RESTTestBase {

    public static final String SERVER_URL = "https://localhost:9853";
    public static final String API_RESOURCE_ENDPOINT = "/api/server/v1/api-resources";
    public static final String API_RESOURCE_SCOPE_ENDPOINT = "/api/server/v1/scopes";
    public static final String APPLICATION_ENDPOINT = "/api/server/v1/applications";
    public static final String BUSINESS_API_FILTER_QUERY = "?filter=type+eq+BUSINESS";
    public static final String API_ID_ATTRIBUTE = "id";
    public static final String API_NAME_ATTRIBUTE = "name";
    public static final String API_DESCRIPTION_ATTRIBUTE = "description";
    public static final String API_IDENTIFIER_ATTRIBUTE = "identifier";
    public static final String API_IS_REQUIRED_AUTHORIZATION_ATTRIBUTE = "requiresAuthorization";
    public static final String API_SCOPE_ATTRIBUTE = "scopes";
    public static final String API_SCOPE_ID_ATTRIBUTE = "id";
    public static final String API_SCOPE_NAME_ATTRIBUTE = "name";
    public static final String API_SCOPE_DISPLAY_NAME_ATTRIBUTE = "displayName";
    public static final String API_SCOPE_DESCRIPTION_ATTRIBUTE = "description";

    protected CloseableHttpClient client;
    protected String adminUsername;
    protected String password;
    protected String tenant;

    private ServerConfigurationManager serverConfigurationManager;

    public APIAccessManagementBaseTestCase(TestUserMode userMode) throws XPathExpressionException {

        AutomationContext context = new AutomationContext("IDENTITY", userMode);
        this.adminUsername = context.getContextTenant().getTenantAdmin().getUserName();
        this.password = context.getContextTenant().getTenantAdmin().getPassword();
        this.tenant = context.getContextTenant().getDomain();
    }

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {

        super.init();
        client = HttpClients.createDefault();
    }

    @AfterClass(alwaysRun = true)
    public void testConclude() throws Exception {

        super.conclude();
    }

    protected HttpResponse createAPIResource(APIResource apiResource) throws JSONException, IOException {

        HttpPost request = new HttpPost(SERVER_URL + API_RESOURCE_ENDPOINT);
        request.setHeader("Content-Type", "application/json");
        request.setHeader("Authorization", getAuthzHeader());
        JSONObject rootObject = new JSONObject();
        rootObject.put(API_NAME_ATTRIBUTE, apiResource.getName());
        rootObject.put(API_IDENTIFIER_ATTRIBUTE, apiResource.getIdentifier());
        rootObject.put(API_DESCRIPTION_ATTRIBUTE, apiResource.getDescription());
        rootObject.put(API_IS_REQUIRED_AUTHORIZATION_ATTRIBUTE, apiResource.isAuthorizationRequired());
        JSONArray scopeArray = new JSONArray();
        for (Scope scope : apiResource.getScopes()) {
            JSONObject scopeObject = new JSONObject();
            scopeObject.put(API_SCOPE_NAME_ATTRIBUTE, scope.getName());
            scopeObject.put(API_SCOPE_DISPLAY_NAME_ATTRIBUTE, scope.getDisplayName());
            scopeObject.put(API_SCOPE_DESCRIPTION_ATTRIBUTE, scope.getDescription());
            scopeArray.put(scopeObject);
        }
        rootObject.put(API_SCOPE_ATTRIBUTE, scopeArray);
        StringEntity entity = new StringEntity(rootObject.toString());
        request.setEntity(entity);
        return client.execute(request);
    }

    protected HttpResponse getAPIResource(String apiId) throws IOException {

        HttpGet request = new HttpGet(SERVER_URL + API_RESOURCE_ENDPOINT + "/" + apiId);
        request.setHeader("Content-Type", "application/json");
        request.setHeader("Authorization", getAuthzHeader());
        return client.execute(request);
    }

    protected String getAuthzHeader() {

        return "Basic " + Base64.encodeBase64String((adminUsername + ":" + password).getBytes()).trim();
    }
}

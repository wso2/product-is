/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.identity.integration.test.actions;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.identity.integration.test.actions.dataprovider.model.ActionResponse;
import org.wso2.identity.integration.test.actions.dataprovider.model.ExpectedTokenResponse;
import org.wso2.identity.integration.test.actions.mockserver.ActionsMockServer;
import org.wso2.identity.integration.test.rest.api.server.action.management.v1.common.model.ActionModel;
import org.wso2.identity.integration.test.rest.api.server.action.management.v1.common.model.AuthenticationType;
import org.wso2.identity.integration.test.rest.api.server.action.management.v1.common.model.Endpoint;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationResponseModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.OpenIDConnectConfiguration;
import org.wso2.identity.integration.test.utils.FileUtils;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.ACCESS_TOKEN_ENDPOINT;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.AUTHORIZATION_HEADER;

/**
 * Tests the pre-issue access token action failure scenarios with password grant type.
 */
public class PreIssueAccessTokenActionFailureClientCredentialsGrantTestCase extends ActionsBaseTestCase {

    private static final String PRE_ISSUE_ACCESS_TOKEN_API_PATH = "preIssueAccessToken";
    private static final String MOCK_SERVER_ENDPOINT_RESOURCE_PATH = "/test/action";
    private static final String CLIENT_CREDENTIALS_GRANT_TYPE = "client_credentials";
    private CloseableHttpClient client;
    private List<String> requestedScopes;
    private String clientId;
    private String clientSecret;
    private String actionId;
    private String applicationId;
    private final TestUserMode userMode;
    private ActionsMockServer actionsMockServer;
    private final ActionResponse actionResponse;
    private final ExpectedTokenResponse expectedTokenResponse;

    @Factory(dataProvider = "testExecutionContextProvider")
    public PreIssueAccessTokenActionFailureClientCredentialsGrantTestCase(TestUserMode testUserMode,
                                                                          ActionResponse actionResponse,
                                                                          ExpectedTokenResponse expectedTokenResponse) {

        this.userMode = testUserMode;
        this.actionResponse = actionResponse;
        this.expectedTokenResponse = expectedTokenResponse;
    }

    @DataProvider(name = "testExecutionContextProvider")
    public static Object[][] getTestExecutionContext() throws Exception {

        return new Object[][]{
                {TestUserMode.SUPER_TENANT_USER, new ActionResponse(200,
                        FileUtils.readFileInClassPathAsString("actions/response/incomplete-response.json")),
                        new ExpectedTokenResponse(500, "server_error", "Internal Server Error.")},
                {TestUserMode.SUPER_TENANT_USER, new ActionResponse(200,
                        FileUtils.readFileInClassPathAsString("actions/response/failure-response.json")),
                        new ExpectedTokenResponse(400, "Some failure reason", "Some description")},
                {TestUserMode.TENANT_USER, new ActionResponse(200,
                        FileUtils.readFileInClassPathAsString("actions/response/failure-response.json")),
                        new ExpectedTokenResponse(400, "Some failure reason", "Some description")},
                {TestUserMode.TENANT_USER, new ActionResponse(500,
                        FileUtils.readFileInClassPathAsString("actions/response/error-response.json")),
                        new ExpectedTokenResponse(500, "server_error", "Internal Server Error.")},
                {TestUserMode.TENANT_USER, new ActionResponse(401, "Unauthorized"),
                        new ExpectedTokenResponse(500, "server_error", "Internal Server Error.")},
        };
    }

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.init(userMode);
        client = HttpClientBuilder.create().build();

        ApplicationResponseModel application = addApplicationWithGrantType(CLIENT_CREDENTIALS_GRANT_TYPE);
        applicationId = application.getId();
        OpenIDConnectConfiguration oidcConfig = getOIDCInboundDetailsOfApplication(applicationId);
        clientId = oidcConfig.getClientId();
        clientSecret = oidcConfig.getClientSecret();
        actionId = createPreIssueAccessTokenAction();

        requestedScopes = new ArrayList<>(Arrays.asList("scope_1", "scope_2"));

        actionsMockServer = new ActionsMockServer();
        actionsMockServer.startServer();
        actionsMockServer.setupStub(MOCK_SERVER_ENDPOINT_RESOURCE_PATH,
                "Basic " + getBase64EncodedString(MOCK_SERVER_AUTH_BASIC_USERNAME, MOCK_SERVER_AUTH_BASIC_PASSWORD),
                actionResponse.getResponseBody(), actionResponse.getStatusCode());
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {

        actionsMockServer.stopServer();

        deleteAction(PRE_ISSUE_ACCESS_TOKEN_API_PATH, actionId);
        deleteApp(applicationId);

        restClient.closeHttpClient();
        actionsRestClient.closeHttpClient();
        client.close();

        actionsMockServer = null;
    }

    @Test(groups = "wso2.is", description = "Verify token response when pre-issue access token action fails with " +
            "client credentials grant type.")
    public void testPreIssueAccessTokenActionFailure() throws Exception {

        HttpResponse response = sendTokenRequestForClientCredentialsGrant();

        assertNotNull(response);
        assertEquals(response.getStatusLine().getStatusCode(), expectedTokenResponse.getStatusCode());

        String responseString = EntityUtils.toString(response.getEntity(), "UTF-8");
        JSONObject jsonResponse = new JSONObject(responseString);
        assertEquals(jsonResponse.getString("error"), expectedTokenResponse.getErrorMessage());
        assertEquals(jsonResponse.getString("error_description"), expectedTokenResponse.getErrorDescription());
    }

    public HttpResponse sendTokenRequestForClientCredentialsGrant() throws Exception {

        List<NameValuePair> parameters = new ArrayList<>();
        parameters.add(new BasicNameValuePair("grant_type", OAuth2Constant.OAUTH2_GRANT_TYPE_CLIENT_CREDENTIALS));

        String scopes = String.join(" ", requestedScopes);
        parameters.add(new BasicNameValuePair("scope", scopes));

        List<Header> headers = new ArrayList<>();
        headers.add(new BasicHeader(AUTHORIZATION_HEADER, OAuth2Constant.BASIC_HEADER + " " +
                getBase64EncodedString(clientId, clientSecret)));
        headers.add(new BasicHeader("Content-Type", "application/x-www-form-urlencoded"));
        headers.add(new BasicHeader("User-Agent", OAuth2Constant.USER_AGENT));

        return sendPostRequest(client, headers, parameters,
                getTenantQualifiedURL(ACCESS_TOKEN_ENDPOINT, tenantInfo.getDomain()));
    }

    private String createPreIssueAccessTokenAction() throws IOException {

        AuthenticationType authenticationType = new AuthenticationType();
        authenticationType.setType(AuthenticationType.TypeEnum.BASIC);
        Map<String, Object> authProperties = new HashMap<>();
        authProperties.put(USERNAME_PROPERTY, MOCK_SERVER_AUTH_BASIC_USERNAME);
        authProperties.put(PASSWORD_PROPERTY, MOCK_SERVER_AUTH_BASIC_PASSWORD);
        authenticationType.setProperties(authProperties);

        Endpoint endpoint = new Endpoint();
        endpoint.setUri(EXTERNAL_SERVICE_URI);
        endpoint.setAuthentication(authenticationType);

        ActionModel actionModel = new ActionModel();
        actionModel.setName("Access Token Pre Issue");
        actionModel.setDescription("This is a test pre issue access token type");
        actionModel.setEndpoint(endpoint);

        return createAction(PRE_ISSUE_ACCESS_TOKEN_API_PATH, actionModel);
    }
}

/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.identity.integration.test.rest.api.user.password.v1;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.identity.integration.test.rest.api.server.action.management.v1.common.model.AuthenticationType;
import org.wso2.identity.integration.test.rest.api.server.action.management.v1.common.model.Endpoint;
import org.wso2.identity.integration.test.rest.api.server.action.management.v1.preupdatepassword.model.PasswordSharing;
import org.wso2.identity.integration.test.rest.api.server.action.management.v1.preupdatepassword.model.PreUpdatePasswordActionModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationResponseModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.OpenIDConnectConfiguration;
import org.wso2.identity.integration.test.restclients.ActionsRestClient;
import org.wso2.identity.integration.test.utils.FileUtils;
import org.wso2.identity.integration.test.serviceextensions.mockservices.ServiceExtensionMockServer;
import org.wso2.identity.integration.test.serviceextensions.model.ActionType;
import org.wso2.identity.integration.test.serviceextensions.model.Credential;
import org.wso2.identity.integration.test.serviceextensions.model.PasswordUpdatingUser;
import org.wso2.identity.integration.test.serviceextensions.model.PreUpdatePasswordActionRequest;
import org.wso2.identity.integration.test.serviceextensions.model.PreUpdatePasswordEvent;

import java.io.IOException;
import java.net.URISyntaxException;

import javax.servlet.http.HttpServletResponse;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * Integration tests for verifying Pre-Update Password Action integration with the Password Update API.
 */
public class PasswordUpdateActionTest extends PasswordUpdateTestBase {

    private static final String ACTION_USER = "actionTestUser";
    private static final String ACTION_USER_PASSWORD = "ActionTest@123";
    private static final String ACTION_NEW_PASSWORD = "ActionNew@123";
    private static final String ACTION_NAME = "PasswordUpdatePreAction";
    private static final String ACTION_DESCRIPTION = "Pre update password action for password update API test";
    private static final String PRE_UPDATE_PASSWORD_API_PATH = "preUpdatePassword";
    private static final String MOCK_SERVER_ENDPOINT_RESOURCE_PATH = "/test/action";
    private static final String EXTERNAL_SERVICE_URI = "http://localhost:8587/test/action";
    private static final String MOCK_SERVER_AUTH_BASIC_USERNAME = "test";
    private static final String MOCK_SERVER_AUTH_BASIC_PASSWORD = "test";

    private String userId;
    private String appId;
    private String clientId;
    private String clientSecret;
    private String accessToken;
    private String actionId;
    private ActionsRestClient actionsRestClient;
    private ServiceExtensionMockServer serviceExtensionMockServer;

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        initBase(TestUserMode.TENANT_ADMIN);

        actionsRestClient = new ActionsRestClient(serverURL, tenantInfo);

        ApplicationResponseModel application = createApp("PasswordUpdateActionTestApp", false);
        appId = application.getId();
        authorizePasswordUpdateScope(appId);

        OpenIDConnectConfiguration oidcConfig = getOIDCInboundDetailsOfApplication(appId);
        clientId = oidcConfig.getClientId();
        clientSecret = oidcConfig.getClientSecret();

        // Enable session preservation so token remains valid after password change.
        setPreserveSessionConfig(true);

        // Create test user.
        userId = createTestUser(ACTION_USER, ACTION_USER_PASSWORD);

        // Get access token once for reuse across test methods.
        accessToken = getUserAccessToken(clientId, clientSecret, ACTION_USER, ACTION_USER_PASSWORD,
                PASSWORD_UPDATE_SCOPE);

        // Start WireMock server and create action.
        serviceExtensionMockServer = new ServiceExtensionMockServer();
        serviceExtensionMockServer.startServer();

        actionId = createPreUpdatePasswordAction();
    }

    @AfterClass(alwaysRun = true)
    public void testCleanup() throws Exception {

        try {
            // Deactivate and delete action.
            safeCleanup(() -> {
                if (actionId != null) {
                    actionsRestClient.deactivateAction(PRE_UPDATE_PASSWORD_API_PATH, actionId);
                    actionsRestClient.deleteActionType(PRE_UPDATE_PASSWORD_API_PATH, actionId);
                }
            });

            safeCleanup(() -> {
                if (serviceExtensionMockServer != null) {
                    serviceExtensionMockServer.stopServer();
                    serviceExtensionMockServer = null;
                }
            });

            safeCleanup(() -> {
                if (userId != null) {
                    scim2RestClient.deleteUser(userId);
                }
            });
            safeCleanup(() -> {
                if (appId != null) {
                    deleteApp(appId);
                }
            });
            safeCleanup(() -> {
                if (actionsRestClient != null) {
                    actionsRestClient.closeHttpClient();
                }
            });
        } finally {
            setPreserveSessionConfig(false);
            cleanupBase();
        }
    }

    @Test(priority = 1, description = "Verify password update fails when pre-update action returns FAILED")
    public void testPreUpdateAction_Failure() throws Exception {

        serviceExtensionMockServer.setupStub(MOCK_SERVER_ENDPOINT_RESOURCE_PATH,
                "Basic " + getBase64EncodedString(MOCK_SERVER_AUTH_BASIC_USERNAME, MOCK_SERVER_AUTH_BASIC_PASSWORD),
                buildFailureActionResponse());

        try (CloseableHttpResponse response = changePassword(accessToken, ACTION_USER_PASSWORD, ACTION_NEW_PASSWORD)) {
            assertEquals(response.getStatusLine().getStatusCode(), HttpServletResponse.SC_BAD_REQUEST,
                    "Expected 400 when pre-update action returns FAILED.");
            String responseBody = EntityUtils.toString(response.getEntity());
            JSONObject json = new JSONObject(responseBody);
            assertEquals(json.getString("code"), "PWD-10004",
                    "Expected error code PWD-10004 for action failure.");
        }
    }

    @Test(priority = 2, description = "Verify password update succeeds when pre-update action returns SUCCESS")
    public void testPreUpdateAction_Success() throws Exception {

        serviceExtensionMockServer.setupStub(MOCK_SERVER_ENDPOINT_RESOURCE_PATH,
                "Basic " + getBase64EncodedString(MOCK_SERVER_AUTH_BASIC_USERNAME, MOCK_SERVER_AUTH_BASIC_PASSWORD),
                buildSuccessActionResponse());

        try (CloseableHttpResponse response = changePassword(accessToken, ACTION_USER_PASSWORD, ACTION_NEW_PASSWORD)) {
            assertEquals(response.getStatusLine().getStatusCode(), HttpServletResponse.SC_NO_CONTENT,
                    "Expected 204 when pre-update action returns SUCCESS.");
        }

        // Verify the action request payload.
        String actualPayload = serviceExtensionMockServer.getReceivedRequestPayload(
                MOCK_SERVER_ENDPOINT_RESOURCE_PATH);
        assertNotNull(actualPayload, "Action request payload should not be null.");

        PreUpdatePasswordActionRequest actionRequest = new ObjectMapper()
                .readValue(actualPayload, PreUpdatePasswordActionRequest.class);

        assertEquals(actionRequest.getActionType(), ActionType.PRE_UPDATE_PASSWORD,
                "Action type should be PRE_UPDATE_PASSWORD.");
        assertEquals(actionRequest.getEvent().getInitiatorType(), PreUpdatePasswordEvent.FlowInitiatorType.USER,
                "Initiator type should be USER.");
        assertEquals(actionRequest.getEvent().getAction(), PreUpdatePasswordEvent.Action.UPDATE,
                "Action should be UPDATE.");

        PasswordUpdatingUser user = actionRequest.getEvent().getPasswordUpdatingUser();
        assertNotNull(user, "Password updating user should not be null.");
        assertEquals(user.getUpdatingCredential().getType(), Credential.Type.PASSWORD,
                "Credential type should be PASSWORD.");
        assertEquals(new String(user.getUpdatingCredential().getValue()), ACTION_NEW_PASSWORD,
                "Credential value should match the new password.");
    }

    /**
     * Creates and activates a pre-update password action backed by the WireMock server,
     * configured with BASIC authentication and plain-text password sharing.
     *
     * @return the ID of the created action.
     * @throws IOException if the REST call fails.
     */
    private String createPreUpdatePasswordAction() throws IOException {

        AuthenticationType authentication = new AuthenticationType()
                .type(AuthenticationType.TypeEnum.BASIC)
                .putPropertiesItem("username", MOCK_SERVER_AUTH_BASIC_USERNAME)
                .putPropertiesItem("password", MOCK_SERVER_AUTH_BASIC_PASSWORD);

        Endpoint endpoint = new Endpoint()
                .uri(EXTERNAL_SERVICE_URI)
                .authentication(authentication);

        PreUpdatePasswordActionModel actionModel = new PreUpdatePasswordActionModel();
        actionModel.setName(ACTION_NAME);
        actionModel.setDescription(ACTION_DESCRIPTION);
        actionModel.setEndpoint(endpoint);
        actionModel.setPasswordSharing(new PasswordSharing().format(PasswordSharing.FormatEnum.PLAIN_TEXT));

        String createdActionId = actionsRestClient.createActionType(actionModel, PRE_UPDATE_PASSWORD_API_PATH);
        actionsRestClient.activateAction(PRE_UPDATE_PASSWORD_API_PATH, createdActionId);
        return createdActionId;
    }

    /**
     * Builds the success action response.
     *
     * @return the success action response.
     * @throws IOException if the file read fails.
     * @throws URISyntaxException if the URI syntax is invalid.
     */
    private String buildSuccessActionResponse() throws IOException, URISyntaxException {

        return FileUtils.readFileInClassPathAsString("actions/response/pre-update-password-response.json");
    }

    /**
     * Builds the failure action response.
     *
     * @return the failure action response.
     * @throws IOException if the file read fails.
     * @throws URISyntaxException if the URI syntax is invalid.
     */
    private String buildFailureActionResponse() throws IOException, URISyntaxException {

        return FileUtils.readFileInClassPathAsString("actions/response/failure-response.json");
    }
}

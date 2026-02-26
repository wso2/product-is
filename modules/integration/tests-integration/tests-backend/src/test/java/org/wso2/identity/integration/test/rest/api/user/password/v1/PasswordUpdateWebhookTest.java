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

import org.apache.http.client.methods.CloseableHttpResponse;
import org.json.JSONObject;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationResponseModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.OpenIDConnectConfiguration;
import org.wso2.identity.integration.test.webhooks.util.EventPayloadUtils;
import org.wso2.identity.integration.test.webhooks.util.WebhookEventTestManager;

import java.util.Arrays;

import javax.servlet.http.HttpServletResponse;

import static org.testng.Assert.assertEquals;

/**
 * Integration tests for verifying POST_UPDATE_CREDENTIAL webhook fires after successful password change
 * via the Password Update API.
 */
public class PasswordUpdateWebhookTest extends PasswordUpdateTestBase {

    private static final String WEBHOOK_USER = "webhookTestUser";
    private static final String WEBHOOK_USER_PASSWORD = "WebhookTest@123";
    private static final String WEBHOOK_NEW_PASSWORD = "WebhookNew@123";
    private static final String CREDENTIAL_EVENT_CHANNEL =
            "https://schemas.identity.wso2.org/events/credential";
    private static final String CREDENTIAL_UPDATED_EVENT_URI =
            "https://schemas.identity.wso2.org/events/credential/event-type/credentialUpdated";
    private static final String SCIM2_USERS_PATH = "scim2/Users/";

    private String userId;
    private String appId;
    private String clientId;
    private String clientSecret;
    private String accessToken;
    private WebhookEventTestManager webhookEventTestManager;

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        initBase(TestUserMode.TENANT_ADMIN);

        ApplicationResponseModel application = createApp("PasswordUpdateWebhookTestApp", false);
        appId = application.getId();
        authorizePasswordUpdateScope(appId);

        OpenIDConnectConfiguration oidcConfig = getOIDCInboundDetailsOfApplication(appId);
        clientId = oidcConfig.getClientId();
        clientSecret = oidcConfig.getClientSecret();

        // Enable session preservation so token remains valid.
        setPreserveSessionConfig(true);

        // Create test user.
        userId = createTestUser(WEBHOOK_USER, WEBHOOK_USER_PASSWORD);

        // Get access token once for reuse.
        accessToken = getUserAccessToken(clientId, clientSecret, WEBHOOK_USER, WEBHOOK_USER_PASSWORD, PASSWORD_UPDATE_SCOPE);

        // Set up webhook subscription for credential events.
        AutomationContext automationContext = new AutomationContext("IDENTITY", TestUserMode.TENANT_ADMIN);
        webhookEventTestManager = new WebhookEventTestManager(
                "/password-update/webhook",
                "WSO2",
                Arrays.asList(CREDENTIAL_EVENT_CHANNEL),
                "PasswordUpdateWebhookTest",
                automationContext);
    }

    @AfterClass(alwaysRun = true)
    public void testCleanup() throws Exception {

        if (webhookEventTestManager != null) {
            webhookEventTestManager.teardown();
        }
        if (userId != null) {
            scim2RestClient.deleteUser(userId);
        }
        if (appId != null) {
            deleteApp(appId);
        }
        setPreserveSessionConfig(false);
        cleanupBase();
    }

    @Test(description = "Verify POST_UPDATE_CREDENTIAL webhook fires after successful password change")
    public void testPostUpdateCredentialWebhook() throws Exception {

        // Change password — this should trigger the credential updated webhook.
        try (CloseableHttpResponse response = changePassword(accessToken, WEBHOOK_USER_PASSWORD,
                WEBHOOK_NEW_PASSWORD)) {
            assertEquals(response.getStatusLine().getStatusCode(), HttpServletResponse.SC_NO_CONTENT,
                    "Expected 204 for successful password change.");
        }

        JSONObject expectedPayload = buildExpectedCredentialUpdatedPayload(userId);
        webhookEventTestManager.stackExpectedEventPayload(CREDENTIAL_UPDATED_EVENT_URI, expectedPayload);
        webhookEventTestManager.validateStackedEventPayloads();
    }

    /**
     * Builds the expected payload for a user-initiated credential updated event.
     *
     * @param userId ID of the user whose credential was updated.
     * @return JSONObject representing the expected event payload.
     * @throws Exception if an error occurs while building the payload.
     */
    private JSONObject buildExpectedCredentialUpdatedPayload(String userId) throws Exception {

        String tenantDomain = tenantInfo.getDomain();

        JSONObject payload = new JSONObject();
        payload.put("initiatorType", "USER");
        payload.put("action", "CREDENTIAL_UPDATE");
        payload.put("credentialType", "PASSWORD");
        payload.put("user", buildUserObject(userId, tenantDomain));
        payload.put("tenant", EventPayloadUtils.createTenantObject(tenantDomain));
        payload.put("organization", EventPayloadUtils.createOrganizationObject(tenantDomain));
        payload.put("userStore", EventPayloadUtils.createUserStoreObject());

        return payload;
    }

    /**
     * Builds the user object for the event payload, including the SCIM2 ref URL
     * and the organization context, both correctly qualified for the tenant domain.
     *
     * @param userId       ID of the user.
     * @param tenantDomain Tenant domain of the user.
     * @return JSONObject representing the user field in the event payload.
     * @throws Exception if an error occurs while building the user object.
     */
    private JSONObject buildUserObject(String userId, String tenantDomain) throws Exception {

        JSONObject user = new JSONObject();
        user.put("id", userId);
        user.put("organization", EventPayloadUtils.createOrganizationObject(tenantDomain));
        user.put("ref", getTenantQualifiedURL(serverURL + SCIM2_USERS_PATH + userId, tenantDomain));
        return user;
    }
}

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
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationResponseModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.OpenIDConnectConfiguration;
import org.wso2.identity.integration.test.restclients.OrgMgtRestClient;

import javax.servlet.http.HttpServletResponse;

import static org.testng.Assert.assertEquals;

/**
 * Integration tests for verifying session/token behavior after password change based on the
 * preserveCurrentSessionAtPasswordUpdate configuration.
 */
public class PasswordUpdateSessionPreservationTest extends PasswordUpdateTestBase {

    private final TestUserMode userMode;

    @Factory(dataProvider = "restAPIUserConfigProvider")
    public PasswordUpdateSessionPreservationTest(TestUserMode userMode) {
        this.userMode = userMode;
    }

    @DataProvider(name = "restAPIUserConfigProvider")
    public static Object[][] restAPIUserConfigProvider() {
        return new Object[][]{{TestUserMode.SUPER_TENANT_ADMIN}, {TestUserMode.TENANT_ADMIN}};
    }

    private static final String SUPER_ORG_USER = "sessionTestUser";
    private static final String SUPER_ORG_USER_PASSWORD = "SessionTest@123";
    private static final String NEW_PASSWORD_1 = "NewPassword1@123";
    private static final String NEW_PASSWORD_2 = "NewPassword2@123";

    private static final String SUB_ORG_NAME = "sessionpreservationsuborg";
    private static final String SUB_ORG_USER = "subOrgSessionUser";
    private static final String SUB_ORG_USER_PASSWORD = "SubOrgSession@123";
    private static final String SUB_ORG_NEW_PASSWORD_1 = "SubOrgNew1@123";
    private static final String SUB_ORG_NEW_PASSWORD_2 = "SubOrgNew2@123";
    private static final String SUB_ORG_NEW_PASSWORD_3 = "SubOrgNew3@123";
    private static final String SUB_ORG_NEW_PASSWORD_4 = "SubOrgNew4@123";

    private String userId;
    private String appId;
    private String clientId;
    private String clientSecret;

    private OrgMgtRestClient orgMgtRestClient;
    private String organizationId;
    private String subOrgUserId;
    private String switchedM2MToken;

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        initBase(userMode);
        setPasswordHistoryEnabled(false);

        ApplicationResponseModel application = createApp("PasswordUpdateSessionTestApp", true);
        appId = application.getId();
        authorizePasswordUpdateScope(appId);

        OpenIDConnectConfiguration oidcConfig = getOIDCInboundDetailsOfApplication(appId);
        clientId = oidcConfig.getClientId();
        clientSecret = oidcConfig.getClientSecret();

        // Create super-org test user.
        userId = createTestUser(SUPER_ORG_USER, SUPER_ORG_USER_PASSWORD);

        // Set up sub-org infrastructure.
        orgMgtRestClient = createOrgMgtRestClient();
        organizationId = orgMgtRestClient.addOrganization(SUB_ORG_NAME);
        switchedM2MToken = orgMgtRestClient.switchM2MToken(organizationId);

        // Create sub-org test user.
        subOrgUserId = createSubOrgUser(SUB_ORG_USER, SUB_ORG_USER_PASSWORD, switchedM2MToken);
    }

    @AfterClass(alwaysRun = true)
    public void testCleanup() throws Exception {

        try {
            safeCleanup(() -> {
                if (subOrgUserId != null) {
                    scim2RestClient.deleteSubOrgUser(subOrgUserId, switchedM2MToken);
                }
            });
            safeCleanup(() -> {
                if (organizationId != null) {
                    orgMgtRestClient.deleteOrganization(organizationId);
                }
            });
            safeCleanup(() -> {
                if (orgMgtRestClient != null) {
                    orgMgtRestClient.closeHttpClient();
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
        } finally {
            setPreserveSessionConfig(false);
            cleanupBase();
        }
    }

    @Test(priority = 1,
            description = "Verify session preservation enabled: token remains valid after password change")
    public void testSessionPreservation_Enabled_SuperOrg() throws Exception {

        adminResetSuperOrgUserPassword(userId, SUPER_ORG_USER_PASSWORD);
        setPreserveSessionConfig(true);

        String accessToken = getUserAccessToken(clientId, clientSecret, SUPER_ORG_USER,
                SUPER_ORG_USER_PASSWORD, PASSWORD_UPDATE_SCOPE);

        // First password change — must succeed.
        try (CloseableHttpResponse response = changePassword(accessToken, SUPER_ORG_USER_PASSWORD, NEW_PASSWORD_1)) {
            assertEquals(response.getStatusLine().getStatusCode(), HttpServletResponse.SC_NO_CONTENT,
                    "Expected 204 for first super-org password change with session preservation enabled.");
        }

        // Second password change — token is still valid; current password is now NEW_PASSWORD_1.
        try (CloseableHttpResponse response = changePassword(accessToken, NEW_PASSWORD_1, NEW_PASSWORD_2)) {
            assertEquals(response.getStatusLine().getStatusCode(), HttpServletResponse.SC_NO_CONTENT,
                    "Expected 204 for second super-org password change — token should remain valid.");
        }
    }

    @Test(priority = 2,
            description = "Verify session preservation enabled for sub-org: token remains valid after password change")
    public void testSessionPreservation_Enabled_SubOrg() throws Exception {

        adminResetSubOrgUserPassword(subOrgUserId, SUB_ORG_USER_PASSWORD, switchedM2MToken);
        setPreserveSessionConfig(true);

        String accessToken = getSubOrgUserAccessToken(clientId, clientSecret, SUB_ORG_USER,
                SUB_ORG_USER_PASSWORD, ORG_PASSWORD_UPDATE_SCOPE,
                SUB_ORG_NAME, organizationId);

        // First password change — must succeed.
        try (CloseableHttpResponse response = changePasswordInSubOrg(accessToken, SUB_ORG_USER_PASSWORD,
                SUB_ORG_NEW_PASSWORD_1)) {
            assertEquals(response.getStatusLine().getStatusCode(), HttpServletResponse.SC_NO_CONTENT,
                    "Expected 204 for sub-org password change with session preservation enabled.");
        }

        // Second password change — token is still valid; current password is now SUB_ORG_NEW_PASSWORD_1.
        try (CloseableHttpResponse response = changePasswordInSubOrg(accessToken, SUB_ORG_NEW_PASSWORD_1,
                SUB_ORG_NEW_PASSWORD_2)) {
            assertEquals(response.getStatusLine().getStatusCode(), HttpServletResponse.SC_NO_CONTENT,
                    "Expected 204 for second sub-org password change — token should remain valid.");
        }
    }

    @Test(priority = 3,
            description = "Verify session preservation disabled: token should be revoked after " +
                    "password change for sub-org user")
    public void testSessionPreservation_Disabled_SubOrg() throws Exception {

        adminResetSubOrgUserPassword(subOrgUserId, SUB_ORG_USER_PASSWORD, switchedM2MToken);
        setPreserveSessionConfig(false);

        String accessToken = getSubOrgUserAccessToken(clientId, clientSecret, SUB_ORG_USER,
                SUB_ORG_USER_PASSWORD, ORG_PASSWORD_UPDATE_SCOPE,
                SUB_ORG_NAME, organizationId);

        // First password change — must succeed; the server revokes the token as a side-effect.
        try (CloseableHttpResponse response = changePasswordInSubOrg(accessToken, SUB_ORG_USER_PASSWORD,
                SUB_ORG_NEW_PASSWORD_3)) {
            assertEquals(response.getStatusLine().getStatusCode(), HttpServletResponse.SC_NO_CONTENT,
                    "Expected 204 for sub-org password change with session preservation disabled.");
        }

        // Second attempt with the now-revoked token — must be rejected; password is NOT changed.
        try (CloseableHttpResponse response = changePasswordInSubOrg(accessToken, SUB_ORG_NEW_PASSWORD_3,
                SUB_ORG_NEW_PASSWORD_4)) {
            assertEquals(response.getStatusLine().getStatusCode(), HttpServletResponse.SC_UNAUTHORIZED,
                    "Expected 401 — sub-org token should be revoked after password change.");
        }
    }
}

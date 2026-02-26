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
import org.wso2.carbon.identity.user.store.configuration.stub.dto.PropertyDTO;
import org.wso2.identity.integration.common.utils.UserStoreConfigUtils;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationResponseModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.OpenIDConnectConfiguration;
import org.wso2.identity.integration.test.rest.api.server.user.store.v1.model.UserStoreReq;
import org.wso2.identity.integration.test.rest.api.server.user.store.v1.model.UserStoreReq.Property;
import org.wso2.identity.integration.test.restclients.OrgMgtRestClient;
import org.wso2.identity.integration.test.restclients.UserStoreMgtRestClient;

import javax.servlet.http.HttpServletResponse;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Integration tests for verifying password change across different app types, organizations,
 * and userstore combinations via the Password Update API.
 */
public class PasswordUpdateOrgAndUserStoreTest extends PasswordUpdateTestBase {

    private final TestUserMode userMode;

    @Factory(dataProvider = "restAPIUserConfigProvider")
    public PasswordUpdateOrgAndUserStoreTest(TestUserMode userMode) {
        this.userMode = userMode;
    }

    @DataProvider(name = "restAPIUserConfigProvider")
    public static Object[][] restAPIUserConfigProvider() {
        return new Object[][]{{TestUserMode.SUPER_TENANT_ADMIN}, {TestUserMode.TENANT_ADMIN}};
    }

    private static final String ROOT_APP_NAME = "PasswordUpdateRootApp";
    private static final String SHARED_APP_NAME = "PasswordUpdateSharedApp";

    private static final String PRIMARY_USER = "orgPrimaryUser";
    private static final String PRIMARY_USER_PASSWORD = "PrimaryUser@123";
    private static final String PRIMARY_USER_NEW_PWD = "PrimaryNew@123";

    private static final String SECONDARY_DOMAIN = "SECONDARY";
    private static final String SUPER_TENANT_SECONDARY_USER_STORE_DB = "SUPER_TENANT_SECONDARY_USER_STORE_DB_PASSWORD_UPDATE_TEST";
    private static final String TENANT_SECONDARY_USER_STORE_DB = "TENANT_SECONDARY_USER_STORE_DB_PASSWORD_UPDATE_TEST";
    private static final String USER_STORE_TYPE = "VW5pcXVlSURKREJDVXNlclN0b3JlTWFuYWdlcg";
    private static final String SECONDARY_USER = SECONDARY_DOMAIN + "/orgSecondaryUser";
    private static final String SECONDARY_USER_PASSWORD = "SecondaryUser@123";
    private static final String SECONDARY_USER_NEW_PWD = "SecondaryNew@123";

    private static final String SUB_ORG_NAME = "pwdupdateSubOrg";
    private static final String SUB_ORG_USER = "subOrgPwdUser";
    private static final String SUB_ORG_USER_PASSWORD = "SubOrgPwd@123";
    private static final String SUB_ORG_USER_NEW_PWD = "SubOrgNew@123";

    private static final String SUB_ORG_SECONDARY_DOMAIN = "SUBORGJDBC";
    private static final String SUB_ORG_SECONDARY_USER = SUB_ORG_SECONDARY_DOMAIN + "/subOrgSecUser";
    private static final String SUB_ORG_SECONDARY_USER_PASSWORD = "SubOrgSecStore@123";
    private static final String SUB_ORG_SECONDARY_USER_NEW_PWD = "SubOrgSecNew@123";

    private String primaryUserId;
    private String secondaryUserId;

    private String rootAppId;
    private String rootClientId;
    private String rootClientSecret;

    private String sharedAppId;
    private String sharedAppClientId;
    private String sharedAppClientSecret;

    // Secondary userstore infrastructure.
    private UserStoreMgtRestClient userStoreMgtRestClient;
    private String secondaryUserStoreDomainId;
    private final UserStoreConfigUtils userStoreConfigUtils = new UserStoreConfigUtils();

    // Sub-org infrastructure.
    private OrgMgtRestClient orgMgtRestClient;
    private String organizationId;
    private String subOrgUserId;
    private String subOrgSecondaryUserId;
    private String switchedM2MToken;

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        initBase(userMode);

        // Create root app (non-shared) for super-org-only tests.
        ApplicationResponseModel rootApp = createApp(ROOT_APP_NAME, false);
        rootAppId = rootApp.getId();
        authorizePasswordUpdateScope(rootAppId);

        OpenIDConnectConfiguration rootOidcConfig = getOIDCInboundDetailsOfApplication(rootAppId);
        rootClientId = rootOidcConfig.getClientId();
        rootClientSecret = rootOidcConfig.getClientSecret();

        // Create shared app for both super-org and sub-org tests (via Organization SSO).
        ApplicationResponseModel sharedApp = createApp(SHARED_APP_NAME, true);
        sharedAppId = sharedApp.getId();
        authorizePasswordUpdateScope(sharedAppId);

        OpenIDConnectConfiguration sharedAppOidcConfig = getOIDCInboundDetailsOfApplication(sharedAppId);
        sharedAppClientId = sharedAppOidcConfig.getClientId();
        sharedAppClientSecret = sharedAppOidcConfig.getClientSecret();

        // Enable session preservation so tokens remain valid after password change.
        setPreserveSessionConfig(true);

        // Create primary user.
        primaryUserId = createTestUser(PRIMARY_USER, PRIMARY_USER_PASSWORD);

        // Set up secondary JDBC userstore.
        userStoreMgtRestClient = new UserStoreMgtRestClient(serverURL, tenantInfo);
        PropertyDTO[] userStoreProperties = getSecondaryUserStoreProperties();
        UserStoreReq userStoreReq = new UserStoreReq().typeId(USER_STORE_TYPE).name(SECONDARY_DOMAIN);
        for (PropertyDTO propertyDTO : userStoreProperties) {
            userStoreReq.addPropertiesItem(new Property().name(propertyDTO.getName()).value(propertyDTO.getValue()));
        }
        secondaryUserStoreDomainId = userStoreMgtRestClient.addUserStore(userStoreReq);
        assertTrue(userStoreMgtRestClient.waitForUserStoreDeployment(SECONDARY_DOMAIN),
                "Secondary JDBC user store is not deployed.");

        // Create user in secondary userstore.
        secondaryUserId = createTestUser(SECONDARY_USER, SECONDARY_USER_PASSWORD);

        // Set up sub-organization infrastructure.
        orgMgtRestClient = createOrgMgtRestClient();
        organizationId = orgMgtRestClient.addOrganization(SUB_ORG_NAME);
        switchedM2MToken = orgMgtRestClient.switchM2MToken(organizationId);

        // Create a user in the sub-organization.
        subOrgUserId = createSubOrgUser(SUB_ORG_USER, SUB_ORG_USER_PASSWORD, switchedM2MToken);

        // Set up secondary JDBC userstore in the sub-organization (reuse same JDBC properties).
        UserStoreReq subOrgUserStoreReq = new UserStoreReq().typeId(USER_STORE_TYPE).name(SUB_ORG_SECONDARY_DOMAIN);
        for (PropertyDTO propertyDTO : userStoreProperties) {
            subOrgUserStoreReq.addPropertiesItem(
                    new Property().name(propertyDTO.getName()).value(propertyDTO.getValue()));
        }
        userStoreMgtRestClient.addSubOrgUserStore(subOrgUserStoreReq, switchedM2MToken);
        assertTrue(userStoreMgtRestClient.waitForSubOrgUserStoreDeployment(SUB_ORG_SECONDARY_DOMAIN, switchedM2MToken),
                "Sub-org secondary JDBC user store is not deployed.");

        // Create a user in the sub-org secondary userstore.
        subOrgSecondaryUserId = createSubOrgUser(
                SUB_ORG_SECONDARY_USER, SUB_ORG_SECONDARY_USER_PASSWORD, switchedM2MToken);
    }

    @AfterClass(alwaysRun = true)
    public void testCleanup() throws Exception {

        // Clean up sub-org resources (users first, then userstore).
        if (subOrgSecondaryUserId != null) {
            scim2RestClient.deleteSubOrgUser(subOrgSecondaryUserId, switchedM2MToken);
        }
        if (switchedM2MToken != null && userStoreMgtRestClient != null) {
            userStoreMgtRestClient.deleteSubOrgUserStore(SUB_ORG_SECONDARY_DOMAIN, switchedM2MToken);
        }
        if (subOrgUserId != null) {
            scim2RestClient.deleteSubOrgUser(subOrgUserId, switchedM2MToken);
        }

        // Delete root-level apps.
        if (rootAppId != null) {
            deleteApp(rootAppId);
        }
        if (sharedAppId != null) {
            deleteApp(sharedAppId);
        }

        // Delete sub-organization.
        if (organizationId != null) {
            orgMgtRestClient.deleteOrganization(organizationId);
        }

        // Clean up super-org resources (users first, then userstore).
        if (secondaryUserId != null) {
            scim2RestClient.deleteUser(secondaryUserId);
        }
        if (primaryUserId != null) {
            scim2RestClient.deleteUser(primaryUserId);
        }
        if (secondaryUserStoreDomainId != null && userStoreMgtRestClient != null) {
            userStoreMgtRestClient.deleteUserStore(secondaryUserStoreDomainId);
            userStoreMgtRestClient.waitForUserStoreUnDeployment(secondaryUserStoreDomainId);
        }

        // Close all HTTP clients.
        if (orgMgtRestClient != null) {
            orgMgtRestClient.closeHttpClient();
        }
        if (userStoreMgtRestClient != null) {
            userStoreMgtRestClient.closeHttpClient();
        }
        setPreserveSessionConfig(false);
        cleanupBase();
    }

    // -----------------------------------------------------------------------
    // 1. rootApp (non-shared) -> super-org users
    // -----------------------------------------------------------------------

    @Test(description = "Verify password change for super org user in PRIMARY userstore via root app")
    public void testRootApp_PrimaryUserStore() throws Exception {

        String accessToken = getUserAccessToken(rootClientId, rootClientSecret, PRIMARY_USER,
                PRIMARY_USER_PASSWORD, PASSWORD_UPDATE_SCOPE);

        try (CloseableHttpResponse response = changePassword(accessToken, PRIMARY_USER_PASSWORD,
                PRIMARY_USER_NEW_PWD)) {
            assertEquals(response.getStatusLine().getStatusCode(), HttpServletResponse.SC_NO_CONTENT,
                    "Expected 204 for primary userstore password change via root app.");
        }

        adminResetSuperOrgUserPassword(primaryUserId, PRIMARY_USER_PASSWORD);
    }

    @Test(description = "Verify password change for super org user in SECONDARY userstore via root app")
    public void testRootApp_SecondaryUserStore() throws Exception {

        String accessToken = getUserAccessToken(rootClientId, rootClientSecret, SECONDARY_USER,
                SECONDARY_USER_PASSWORD, PASSWORD_UPDATE_SCOPE);

        try (CloseableHttpResponse response = changePassword(accessToken, SECONDARY_USER_PASSWORD,
                SECONDARY_USER_NEW_PWD)) {
            assertEquals(response.getStatusLine().getStatusCode(), HttpServletResponse.SC_NO_CONTENT,
                    "Expected 204 for secondary userstore password change via root app.");
        }

        adminResetSuperOrgUserPassword(secondaryUserId, SECONDARY_USER_PASSWORD);
    }

    // -----------------------------------------------------------------------
    // 2. sharedApp (shared) -> super-org users
    // -----------------------------------------------------------------------

    @Test(description = "Verify password change for super org user in PRIMARY userstore via shared app")
    public void testSharedApp_PrimaryUserStore() throws Exception {

        String accessToken = getUserAccessToken(sharedAppClientId, sharedAppClientSecret, PRIMARY_USER,
                PRIMARY_USER_PASSWORD, PASSWORD_UPDATE_SCOPE);

        try (CloseableHttpResponse response = changePassword(accessToken, PRIMARY_USER_PASSWORD,
                PRIMARY_USER_NEW_PWD)) {
            assertEquals(response.getStatusLine().getStatusCode(), HttpServletResponse.SC_NO_CONTENT,
                    "Expected 204 for primary userstore password change via shared app.");
        }

        adminResetSuperOrgUserPassword(primaryUserId, PRIMARY_USER_PASSWORD);
    }

    @Test(description = "Verify password change for super org user in SECONDARY userstore via shared app")
    public void testSharedApp_SecondaryUserStore() throws Exception {

        String accessToken = getUserAccessToken(sharedAppClientId, sharedAppClientSecret, SECONDARY_USER,
                SECONDARY_USER_PASSWORD, PASSWORD_UPDATE_SCOPE);

        try (CloseableHttpResponse response = changePassword(accessToken, SECONDARY_USER_PASSWORD,
                SECONDARY_USER_NEW_PWD)) {
            assertEquals(response.getStatusLine().getStatusCode(), HttpServletResponse.SC_NO_CONTENT,
                    "Expected 204 for secondary userstore password change via shared app.");
        }

        adminResetSuperOrgUserPassword(secondaryUserId, SECONDARY_USER_PASSWORD);
    }

    // -----------------------------------------------------------------------
    // 3. sharedApp (shared) -> sub-org users (Organization SSO flow)
    // -----------------------------------------------------------------------

    @Test(description = "Verify password change for sub-org user in PRIMARY userstore via shared app")
    public void testSharedApp_SubOrgPrimaryUserStore() throws Exception {

        String accessToken = getSubOrgUserAccessToken(sharedAppClientId, sharedAppClientSecret, SUB_ORG_USER,
                SUB_ORG_USER_PASSWORD, ORG_PASSWORD_UPDATE_SCOPE, SUB_ORG_NAME, organizationId);

        try (CloseableHttpResponse response = changePasswordInSubOrg(accessToken, SUB_ORG_USER_PASSWORD,
                SUB_ORG_USER_NEW_PWD)) {
            assertEquals(response.getStatusLine().getStatusCode(), HttpServletResponse.SC_NO_CONTENT,
                    "Expected 204 for sub-org primary userstore password change via shared app.");
        }

        adminResetSubOrgUserPassword(subOrgUserId, SUB_ORG_USER_PASSWORD, switchedM2MToken);
    }

    @Test(description = "Verify password change for sub-org user in SECONDARY userstore via shared app")
    public void testSharedApp_SubOrgSecondaryUserStore() throws Exception {

        String accessToken = getSubOrgUserAccessToken(sharedAppClientId, sharedAppClientSecret, SUB_ORG_SECONDARY_USER,
                SUB_ORG_SECONDARY_USER_PASSWORD, ORG_PASSWORD_UPDATE_SCOPE, SUB_ORG_NAME, organizationId);

        try (CloseableHttpResponse response = changePasswordInSubOrg(accessToken,
                SUB_ORG_SECONDARY_USER_PASSWORD, SUB_ORG_SECONDARY_USER_NEW_PWD)) {
            assertEquals(response.getStatusLine().getStatusCode(), HttpServletResponse.SC_NO_CONTENT,
                    "Expected 204 for sub-org secondary userstore password change via shared app.");
        }

        adminResetSubOrgUserPassword(subOrgSecondaryUserId, SUB_ORG_SECONDARY_USER_PASSWORD, switchedM2MToken);
    }

    /**
     * Helper method to retrieve userstore properties based on the current test user mode.
     * @return
     * @throws Exception
     */
    private PropertyDTO[] getSecondaryUserStoreProperties() throws Exception {

        if (userMode == TestUserMode.SUPER_TENANT_ADMIN) {
            return userStoreConfigUtils.getJDBCUserStoreProperties(SUPER_TENANT_SECONDARY_USER_STORE_DB);
        }
        return userStoreConfigUtils.getJDBCUserStoreProperties(TENANT_SECONDARY_USER_STORE_DB);
    }
}

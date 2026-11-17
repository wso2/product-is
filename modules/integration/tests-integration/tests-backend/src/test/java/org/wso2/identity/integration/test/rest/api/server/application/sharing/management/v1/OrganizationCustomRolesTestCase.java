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

package org.wso2.identity.integration.test.rest.api.server.application.sharing.management.v1;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.config.Lookup;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.cookie.CookieSpecProvider;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.RFC6265CookieSpecProvider;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.annotations.AfterClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.identity.integration.test.oauth2.OAuth2ServiceAbstractIntegrationTest;
import org.wso2.identity.integration.test.rest.api.common.RESTTestBase;
import org.wso2.identity.integration.test.rest.api.server.api.resource.v1.model.APIResourceCreationModel;
import org.wso2.identity.integration.test.rest.api.server.api.resource.v1.model.APIResourceListItem;
import org.wso2.identity.integration.test.rest.api.server.api.resource.v1.model.ScopeCreationModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.AdvancedApplicationConfiguration;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationPatchModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationResponseModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.AssociatedRolesConfig;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.AuthorizedAPICreationModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.InboundProtocols;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.OpenIDConnectConfiguration;
import org.wso2.identity.integration.test.rest.api.server.roles.v2.model.Audience;
import org.wso2.identity.integration.test.rest.api.server.roles.v2.model.Permission;
import org.wso2.identity.integration.test.rest.api.server.roles.v2.model.RoleV2;
import org.wso2.identity.integration.test.rest.api.user.common.model.Email;
import org.wso2.identity.integration.test.rest.api.user.common.model.GroupRequestObject;
import org.wso2.identity.integration.test.rest.api.user.common.model.UserObject;
import org.wso2.identity.integration.test.restclients.APIResourceManagementClient;
import org.wso2.identity.integration.test.restclients.OAuth2RestClient;
import org.wso2.identity.integration.test.restclients.OrgMgtRestClient;
import org.wso2.identity.integration.test.restclients.SCIM2RestClient;
import org.wso2.identity.integration.test.utils.DataExtractUtil;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.ACCESS_TOKEN_ENDPOINT;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.AUTHORIZE_ENDPOINT_URL;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.CALLBACK_URL;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.COMMON_AUTH_URL;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.OAUTH2_GRANT_TYPE_AUTHORIZATION_CODE;

/**
 * Integration tests for organization custom roles (for B2B SaaS apps), which allow organizations to create custom
 * roles for the applications shared with them. The organization custom roles feature is expected to be enabled in the
 * server.
 * <p>
 * Tokens are obtained through the enhanced organization login direct path (/t/{tenant}/o/{org-id}) with the client
 * of the root application, and the permissions of the user are verified from the scope of the introspected token.
 */
public class OrganizationCustomRolesTestCase extends OAuth2ServiceAbstractIntegrationTest {

    private static final String MGT_APP_AUTHORIZED_API_RESOURCES =
            "organization-custom-roles-mgt-app-authorized-apis.json";

    private static final String API_SERVER_PATH = "api/server/v1";
    private static final String APPLICATIONS_PATH = API_SERVER_PATH + "/applications";
    private static final String ROLES_V2_PATH = "scim2/v2/Roles";
    private static final String PATCH_OP_SCHEMA = "urn:ietf:params:scim:api:messages:2.0:PatchOp";

    private static final String AUDIENCE_APPLICATION = "APPLICATION";
    private static final String AUDIENCE_ORGANIZATION = "ORGANIZATION";
    private static final String ORGANIZATION_CUSTOM_ROLES_ENABLED = "organizationCustomRolesEnabled";
    private static final String ENHANCED_ORG_AUTHENTICATION_ENABLED = "enhancedOrgAuthenticationEnabled";
    private static final String SELECTED_ORG_ONLY = "SELECTED_ORG_ONLY";
    private static final String SELECTED_ORG_WITH_ALL_CHILDREN = "SELECTED_ORG_WITH_ALL_EXISTING_AND_FUTURE_CHILDREN";
    private static final String ROLE_SHARING_MODE_ALL = "ALL";
    private static final String ROLE_SHARING_MODE_SELECTED = "SELECTED";
    private static final String ROLE_SHARING_MODE_NONE = "NONE";
    private static final String RBAC_POLICY = "RBAC";
    // SCIM2 error responses do not have an error code, so the error is identified by its description.
    private static final String SHARED_APP_ROLE_CREATION_NOT_ALLOWED_ERROR = "not allowed at organization level";
    // Application validation errors share a generic error code, so the description is verified as well.
    private static final String INVALID_APPLICATION_CONFIGURATION_ERROR_CODE = "APP-60001";
    private static final String ENHANCED_ORG_AUTHENTICATION_REQUIRED_ERROR = "enhanced organization authentication";

    private static final String MGT_USER_SCIM_API = "/scim2/Users";
    private static final String ORG_USER_SCIM_API = "/o/scim2/Users";
    private static final String MGT_GROUP_SCIM_API = "/scim2/Groups";
    private static final String ORG_GROUP_SCIM_API = "/o/scim2/Groups";
    private static final String MGT_USER_VIEW_SCOPE = "internal_user_mgt_view";
    private static final String ORG_USER_VIEW_SCOPE = "internal_org_user_mgt_view";
    private static final String MGT_GROUP_VIEW_SCOPE = "internal_group_mgt_view";
    private static final String ORG_GROUP_VIEW_SCOPE = "internal_org_group_mgt_view";

    private static final String USER_PASSWORD = "Ocr@User123";
    private static final long WAIT_TIMEOUT_SECONDS = 60;
    private static final long WAIT_INTERVAL_SECONDS = 1;

    private final TestUserMode userMode;
    private final String suffix;

    // Business API resource scopes. Read is shared from the root organization, write is used by organization roles.
    private final String readScope;
    private final String writeScope;

    private CloseableHttpClient apiClient;
    private SCIM2RestClient scim2RestClient;
    private OrgMgtRestClient orgMgtRestClient;
    private OAuth2RestClient oAuth2RestClient;
    private APIResourceManagementClient apiResourceManagementClient;

    private String rootBasePath;
    private String businessApiId;

    // Applications created in the root organization, keyed by application name.
    private final Map<String, TestApplication> applications = new HashMap<>();
    // Organizations, keyed by a logical name.
    private final Map<String, String> organizationIds = new HashMap<>();
    // Switched tokens for the organizations, keyed by the logical name.
    private final Map<String, String> organizationTokens = new HashMap<>();
    // Users created in the organizations, keyed by username.
    private final Map<String, String> userIds = new HashMap<>();
    private final List<String> rootRoleIds = new ArrayList<>();

    private String appAud;
    private String appOff;
    private String appLegacy;
    private String appOrgAud;

    @DataProvider(name = "configProvider")
    public static Object[][] configProvider() {

        return new Object[][]{
                {TestUserMode.SUPER_TENANT_ADMIN, "st"},
                {TestUserMode.TENANT_ADMIN, "tn"}};
    }

    @Factory(dataProvider = "configProvider")
    public OrganizationCustomRolesTestCase(TestUserMode userMode, String suffix) {

        this.userMode = userMode;
        this.suffix = suffix;
        this.readScope = "ocr_" + suffix + ":read";
        this.writeScope = "ocr_" + suffix + ":write";
    }

    // ------------------------------------------------------------------------------------------------------------
    // Setup.
    // ------------------------------------------------------------------------------------------------------------

    @Test(priority = 1, description = "Initialize the clients.")
    public void testInit() throws Exception {

        super.init(userMode);
        apiClient = HttpClientBuilder.create().build();
        scim2RestClient = new SCIM2RestClient(serverURL, tenantInfo);
        oAuth2RestClient = new OAuth2RestClient(serverURL, tenantInfo);
        apiResourceManagementClient = new APIResourceManagementClient(serverURL, tenantInfo);
        orgMgtRestClient = new OrgMgtRestClient(isServer, tenantInfo, serverURL,
                new JSONObject(RESTTestBase.readResource(MGT_APP_AUTHORIZED_API_RESOURCES, getClass())));
        rootBasePath = isSuperTenant() ? serverURL : serverURL + "t/" + tenantInfo.getDomain() + "/";
    }

    @Test(priority = 2, dependsOnMethods = "testInit",
            description = "Create the API resource, the applications and the roles of the root organization.")
    public void testCreateApplications() throws Exception {

        businessApiId = createBusinessApiResource();
        assertNotNull(businessApiId, "API resource ID should not be null.");

        appAud = "OCR-APP-AUD-" + suffix;
        appOff = "OCR-APP-OFF-" + suffix;
        appLegacy = "OCR-LEGACY-" + suffix;
        appOrgAud = "OCR-ORG-AUD-" + suffix;
        createApplication(appAud, AUDIENCE_APPLICATION, true);
        createApplication(appOff, AUDIENCE_APPLICATION, true);
        createApplication(appLegacy, AUDIENCE_APPLICATION, false);
        createApplication(appOrgAud, AUDIENCE_ORGANIZATION, true);

        // Roles of APP-AUD that are shared with the organizations.
        rootRoleIds.add(createRootApplicationRole(appAud, "R-App", readScope));
        rootRoleIds.add(createRootApplicationRole(appAud, "R-Mgmt", MGT_USER_VIEW_SCOPE));
    }

    @Test(priority = 3, dependsOnMethods = "testCreateApplications",
            description = "Create the organizations.")
    public void testCreateSubOrganizations() throws Exception {

        String m2mToken = orgMgtRestClient.getM2MAccessToken();
        organizationIds.put("orgA", orgMgtRestClient.addOrganizationWithToken("ocr-orgA-" + suffix,
                "ocr-orga-" + suffix, m2mToken));
        organizationIds.put("orgB", orgMgtRestClient.addOrganizationWithToken("ocr-orgB-" + suffix,
                "ocr-orgb-" + suffix, m2mToken));
        organizationIds.put("orgA1", orgMgtRestClient.addSubOrganization("ocr-orgA1-" + suffix,
                organizationIds.get("orgA")));
        for (String org : organizationIds.keySet()) {
            assertNotNull(organizationIds.get(org), "Organization ID of " + org + " should not be null.");
            organizationTokens.put(org, orgMgtRestClient.switchM2MToken(organizationIds.get(org)));
        }
    }

    @Test(priority = 4, dependsOnMethods = "testCreateSubOrganizations",
            description = "Share the applications with the organizations.")
    public void testShareApplicationsToSubOrgs() throws Exception {

        // APP-AUD is shared with orgA (and its children) and orgB. The other applications are shared with orgA.
        shareWithOrganizations(appAud, new String[][]{
                {"orgA", SELECTED_ORG_WITH_ALL_CHILDREN}, {"orgB", SELECTED_ORG_ONLY}});
        for (String app : Arrays.asList(appOff, appLegacy, appOrgAud)) {
            shareWithOrganizations(app, new String[][]{{"orgA", SELECTED_ORG_ONLY}});
        }
        for (String org : Arrays.asList("orgA", "orgB", "orgA1")) {
            waitForSharedApplication(appAud, org);
        }
        for (String app : Arrays.asList(appOff, appLegacy, appOrgAud)) {
            waitForSharedApplication(app, "orgA");
        }
        waitForRoles("R-App should be shared with orgA.", "orgA", appAud, true, "R-App", "R-Mgmt");
        waitForRoles("R-App should be shared with orgB.", "orgB", appAud, true, "R-App");
    }

    @Test(priority = 5, dependsOnMethods = "testShareApplicationsToSubOrgs",
            description = "Create the users of the organizations.")
    public void testCreateSubOrgUsers() throws Exception {

        userIds.put("alice", createOrganizationUser("orgA", "ocr_alice_" + suffix));
        userIds.put("bob", createOrganizationUser("orgA", "ocr_bob_" + suffix));
        userIds.put("carol", createOrganizationUser("orgB", "ocr_carol_" + suffix));
        userIds.put("dave", createOrganizationUser("orgA1", "ocr_dave_" + suffix));
        for (String user : userIds.keySet()) {
            assertNotNull(userIds.get(user), "User ID of " + user + " should not be null.");
        }
    }

    // ------------------------------------------------------------------------------------------------------------
    // Application configuration.
    // ------------------------------------------------------------------------------------------------------------

    @Test(priority = 6, dependsOnMethods = "testCreateSubOrgUsers",
            description = "Allow organizations to create custom roles for an application audience application.")
    public void testEnableOrganizationCustomRoles() throws Exception {

        enableOrganizationCustomRoles(appAud);
        assertEquals(getRootApplication(appAud).getOrganizationCustomRolesEnabled(), Boolean.TRUE,
                "organizationCustomRolesEnabled should be true after enabling it.");
        assertEquals(getRootApplication(appOff).getOrganizationCustomRolesEnabled(), Boolean.FALSE,
                "organizationCustomRolesEnabled should be false by default.");
    }

    @Test(priority = 7, dependsOnMethods = "testEnableOrganizationCustomRoles",
            description = "Organization custom roles cannot be enabled without enhanced organization login.")
    public void testEnableRejectedWithoutEnhancedOrgAuthentication() throws Exception {

        ApiResponse response = patchApplication(appLegacy,
                new JSONObject().put(ORGANIZATION_CUSTOM_ROLES_ENABLED, true));
        assertEquals(response.status, 400, "Enabling custom roles without enhanced login should fail.");
        assertEnhancedOrgAuthenticationRequiredError(response);
        assertEquals(getRootApplication(appLegacy).getOrganizationCustomRolesEnabled(), Boolean.FALSE,
                "organizationCustomRolesEnabled should stay false after the rejected update.");

        ApplicationModel invalidApplication = buildApplication("OCR-INVALID-" + suffix, AUDIENCE_APPLICATION, false)
                .organizationCustomRolesEnabled(true);
        response = send("POST", rootBasePath + APPLICATIONS_PATH, toJson(invalidApplication), null);
        if (response.status == 201) {
            // Clean up the application if it is unexpectedly created.
            applications.put("OCR-INVALID-" + suffix, new TestApplication(getIdFromLocation(response), null, null));
        }
        assertEquals(response.status, 400, "Creating an application with custom roles and without enhanced login "
                + "should fail. " + response.body);
        assertEnhancedOrgAuthenticationRequiredError(response);
    }

    @Test(priority = 8, dependsOnMethods = "testEnableRejectedWithoutEnhancedOrgAuthentication",
            description = "An application can be created with organization custom roles enabled.")
    public void testCreateApplicationWithOrganizationCustomRoles() throws Exception {

        String name = "OCR-CREATE-" + suffix;
        String id = oAuth2RestClient.createApplication(buildApplication(name, AUDIENCE_APPLICATION, true)
                .organizationCustomRolesEnabled(true));
        applications.put(name, new TestApplication(id, null, null));
        assertEquals(getRootApplication(name).getOrganizationCustomRolesEnabled(), Boolean.TRUE,
                "The application should be created with organization custom roles enabled.");
    }

    @Test(priority = 9, dependsOnMethods = "testCreateApplicationWithOrganizationCustomRoles",
            description = "Enhanced organization login cannot be turned off alone while custom roles are enabled.")
    public void testDisableEnhancedOrgAuthenticationAloneRejected() throws Exception {

        ApiResponse response = patchApplication(appAud,
                new JSONObject().put(ENHANCED_ORG_AUTHENTICATION_ENABLED, false));
        assertEquals(response.status, 400, "Turning off enhanced login alone should fail. " + response.body);
        ApplicationResponseModel application = getRootApplication(appAud);
        assertEquals(application.getEnhancedOrgAuthenticationEnabled(), Boolean.TRUE,
                "Enhanced organization login should stay enabled after the rejected update.");
        assertEquals(application.getOrganizationCustomRolesEnabled(), Boolean.TRUE,
                "Organization custom roles should stay enabled after the rejected update.");
    }

    @Test(priority = 10, dependsOnMethods = "testDisableEnhancedOrgAuthenticationAloneRejected",
            description = "The configuration is returned when listing applications with the attribute.")
    public void testListApplicationsWithAttribute() throws Exception {

        Map<String, Boolean> values = listOrganizationCustomRolesValues(rootBasePath, null);
        assertEquals(values.get(appAud), Boolean.TRUE, "Listed values: " + values);
        assertEquals(values.get(appOff), Boolean.FALSE, "Listed values: " + values);
        assertEquals(values.get(appLegacy), Boolean.FALSE, "Listed values: " + values);
    }

    @Test(priority = 11, dependsOnMethods = "testListApplicationsWithAttribute",
            description = "The configuration of a shared application is resolved from the main application when "
                    + "listing applications in an organization with the attribute.")
    public void testListSharedApplicationsWithAttribute() throws Exception {

        Map<String, Boolean> values = listOrganizationCustomRolesValues(orgBasePath(), organizationTokens.get("orgA"));
        assertEquals(values.get(appAud), Boolean.TRUE, "The shared application should resolve the configuration "
                + "of the main application. Listed values: " + values);
        assertEquals(values.get(appOff), Boolean.FALSE, "Listed values: " + values);
        assertEquals(values.get(appLegacy), Boolean.FALSE, "Listed values: " + values);
    }

    @Test(priority = 12, dependsOnMethods = "testListSharedApplicationsWithAttribute",
            description = "The configuration of a shared application is resolved from the main application and a "
                    + "value sent in the organization is ignored.")
    public void testSharedApplicationConfigurationIsReadOnly() throws Exception {

        String sharedAppId = getSharedApplicationId(appAud, "orgA");
        String sharedAppPath = orgBasePath() + APPLICATIONS_PATH + "/" + sharedAppId;
        ApiResponse response = send("GET", sharedAppPath, null, organizationTokens.get("orgA"));
        assertEquals(response.status, 200, response.body);
        assertTrue(new JSONObject(response.body).getBoolean(ORGANIZATION_CUSTOM_ROLES_ENABLED),
                "The shared application should resolve the configuration of the main application.");

        ApiResponse offResponse = send("GET", orgBasePath() + APPLICATIONS_PATH + "/"
                + getSharedApplicationId(appOff, "orgA"), null, organizationTokens.get("orgA"));
        assertEquals(offResponse.status, 200, offResponse.body);
        assertFalse(new JSONObject(offResponse.body).getBoolean(ORGANIZATION_CUSTOM_ROLES_ENABLED),
                "A shared application of a main application without custom roles should resolve false. "
                        + offResponse.body);

        response = send("PATCH", sharedAppPath, new JSONObject().put(ORGANIZATION_CUSTOM_ROLES_ENABLED, false)
                .toString(), organizationTokens.get("orgA"));
        assertEquals(response.status, 200, "A value sent for a shared application should be ignored. "
                + response.body);
        response = send("GET", sharedAppPath, null, organizationTokens.get("orgA"));
        assertTrue(new JSONObject(response.body).getBoolean(ORGANIZATION_CUSTOM_ROLES_ENABLED),
                "The shared application should still resolve the configuration of the main application.");

        response = send("PATCH", sharedAppPath, new JSONObject().put(ORGANIZATION_CUSTOM_ROLES_ENABLED, true)
                .toString(), organizationTokens.get("orgA"));
        assertEquals(response.status, 200, "An unchanged value should be ignored. " + response.body);
    }

    @Test(priority = 13, dependsOnMethods = "testSharedApplicationConfigurationIsReadOnly",
            description = "The configuration is kept when the application is exported and imported.")
    public void testExportAndImportKeepsConfiguration() throws Exception {

        ApiResponse export = send("GET", rootBasePath + APPLICATIONS_PATH + "/" + applications.get(appAud).id
                + "/exportFile?exportSecrets=false", null, null, "application/xml");
        assertEquals(export.status, 200, export.body);
        assertTrue(export.body.contains("<IsOrganizationCustomRolesEnabled>true</IsOrganizationCustomRolesEnabled>"),
                "The exported application should contain the organization custom roles configuration.");

        String importedName = "OCR-IMPORTED-" + suffix;
        String xml = export.body
                .replaceAll("(?s)<AssociatedRoles>.*?</AssociatedRoles>", "")
                .replace(">" + appAud + "<", ">" + importedName + "<")
                .replace(applications.get(appAud).clientId, "ocr_imported_" + suffix);
        ApiResponse imported = importApplication(xml);
        assertEquals(imported.status, 201, "Import failed. " + imported.body);
        applications.put(importedName, new TestApplication(getIdFromLocation(imported), null, null));
        assertEquals(getRootApplication(importedName).getOrganizationCustomRolesEnabled(), Boolean.TRUE,
                "The imported application should keep the organization custom roles configuration.");

        String invalidXml = xml.replace(">" + importedName + "<", ">OCR-IMPORTED-INVALID-" + suffix + "<")
                .replace("ocr_imported_" + suffix, "ocr_imported_invalid_" + suffix)
                .replace("<IsEnhancedOrganizationAuthenticationEnabled>true",
                        "<IsEnhancedOrganizationAuthenticationEnabled>false");
        ApiResponse invalid = importApplication(invalidXml);
        if (invalid.status == 201) {
            // Clean up the application if it is unexpectedly imported.
            applications.put("OCR-IMPORTED-INVALID-" + suffix,
                    new TestApplication(getIdFromLocation(invalid), null, null));
        }
        assertEquals(invalid.status, 400, "Importing custom roles without enhanced login should fail. "
                + invalid.body);
    }

    @Test(priority = 14, dependsOnMethods = "testExportAndImportKeepsConfiguration",
            description = "Enhanced organization login and custom roles can be turned on together.")
    public void testEnableEnhancedOrgAuthenticationWithCustomRoles() throws Exception {

        String app = "OCR-ENABLE-BOTH-" + suffix;
        createApplication(app, AUDIENCE_APPLICATION, false);
        updateRootApplication(app, new ApplicationPatchModel().enhancedOrgAuthenticationEnabled(true)
                .organizationCustomRolesEnabled(true));
        ApplicationResponseModel application = getRootApplication(app);
        assertEquals(application.getEnhancedOrgAuthenticationEnabled(), Boolean.TRUE,
                "Enhanced organization login should be enabled.");
        assertEquals(application.getOrganizationCustomRolesEnabled(), Boolean.TRUE,
                "Organization custom roles should be enabled.");
    }

    // ------------------------------------------------------------------------------------------------------------
    // Creating roles in an organization.
    // ------------------------------------------------------------------------------------------------------------

    @Test(priority = 15, dependsOnMethods = "testEnableEnhancedOrgAuthenticationWithCustomRoles",
            description = "An organization can create an application role for the shared application.")
    public void testCreateOrganizationRoleForSharedApplication() throws Exception {

        createOrganizationRoleId("orgA", "Sub-App-1", appAud, readScope, writeScope);
        Map<String, String> roles = getOrganizationRoles("orgA", appAud);
        assertTrue(roles.containsKey("Sub-App-1"), "The created role should be listed for the shared app.");
        assertTrue(roles.containsKey("R-App"), "The inherited role should still be listed for the shared app.");
    }

    @Test(priority = 16, dependsOnMethods = "testCreateOrganizationRoleForSharedApplication",
            description = "Only the permissions authorized for the shared application are accepted.")
    public void testOnlyAuthorizedPermissionsAccepted() throws Exception {

        assertEquals(createOrganizationRole("orgA", "Sub-Mgmt", appAud, MGT_USER_VIEW_SCOPE).status, 400,
                "A management API permission should be rejected.");
        assertEquals(createOrganizationRole("orgA", "Sub-Unknown", appAud, "ocr_" + suffix + ":unknown").status,
                400, "A permission that is not authorized for the application should be rejected.");
        assertEquals(createOrganizationRole("orgA", "Sub-Mixed", appAud, readScope, MGT_USER_VIEW_SCOPE).status,
                400, "A request with a mix of valid and invalid permissions should be rejected.");
        // An organization API permission authorized for the application is accepted.
        createOrganizationRoleId("orgA", "Sub-Org-Api", appAud, ORG_USER_VIEW_SCOPE);

        Map<String, String> roles = getOrganizationRoles("orgA", appAud);
        assertFalse(roles.containsKey("Sub-Mgmt") || roles.containsKey("Sub-Unknown")
                || roles.containsKey("Sub-Mixed"), "Rejected roles should not be created.");

        String roleId = createOrganizationRoleId("orgA", "Sub-Scim", appAud, readScope);
        String rolePath = orgBasePath() + ROLES_V2_PATH + "/" + roleId;
        ApiResponse patch = send("PATCH", rolePath, patchOperation("add", "permissions",
                new JSONArray().put(new JSONObject().put("value", MGT_USER_VIEW_SCOPE))),
                organizationTokens.get("orgA"));
        assertEquals(patch.status, 400, "Adding a management API permission should be rejected.");
        patch = send("PATCH", rolePath, patchOperation("add", "permissions",
                new JSONArray().put(new JSONObject().put("value", writeScope))), organizationTokens.get("orgA"));
        assertEquals(patch.status, 200, "Adding an authorized permission should succeed. " + patch.body);
    }

    @Test(priority = 17, dependsOnMethods = "testOnlyAuthorizedPermissionsAccepted",
            description = "Users of another organization cannot be assigned to an organization role.")
    public void testAssigningUserOfAnotherOrganizationRejected() throws Exception {

        JSONObject role = new JSONObject(toJson(roleRequest("Sub-Foreign-User", AUDIENCE_APPLICATION,
                getSharedApplicationId(appAud, "orgA"), readScope)))
                .put("users", new JSONArray().put(new JSONObject().put("value", userIds.get("carol"))));
        ApiResponse response = send("POST", orgBasePath() + ROLES_V2_PATH, role.toString(),
                organizationTokens.get("orgA"));
        assertEquals(response.status, 400, "Assigning a user of another organization should fail. " + response.body);
    }

    @Test(priority = 18, dependsOnMethods = "testAssigningUserOfAnotherOrganizationRejected",
            description = "Role names used by the roles of the shared application are rejected.")
    public void testDuplicateRoleNamesRejected() throws Exception {

        assertEquals(createOrganizationRole("orgA", "R-App", appAud, readScope).status, 409,
                "A role with the name of an inherited role should be rejected.");
        assertEquals(createOrganizationRole("orgA", "Sub-App-1", appAud, readScope).status, 409,
                "A role with the name of an existing organization role should be rejected.");

        // The same name is allowed in the organization audience.
        String organizationRoleId = createOwnOrganizationRole("orgA", "Sub-App-1", readScope);
        deleteQuietly(orgBasePath() + ROLES_V2_PATH + "/" + organizationRoleId, organizationTokens.get("orgA"));
    }

    @Test(priority = 19, dependsOnMethods = "testDuplicateRoleNamesRejected",
            description = "Roles cannot be created for shared applications that do not allow custom roles.")
    public void testRoleCreationRejectedForUnsupportedApplications() throws Exception {

        for (String app : Arrays.asList(appOff, appLegacy, appOrgAud)) {
            ApiResponse response = createOrganizationRole("orgA", "Sub-Unsupported", app, readScope);
            assertEquals(response.status, 400, "Role creation should fail for " + app + ". " + response.body);
            assertTrue(response.body.contains(SHARED_APP_ROLE_CREATION_NOT_ALLOWED_ERROR), response.body);
        }
        RoleV2 role = roleRequest("Sub-Unknown-App", AUDIENCE_APPLICATION, UUID.randomUUID().toString(), readScope);
        ApiResponse response = send("POST", orgBasePath() + ROLES_V2_PATH, toJson(role),
                organizationTokens.get("orgA"));
        assertEquals(response.status, 400, "Role creation should fail for an unknown application. "
                + response.body);
    }

    @Test(priority = 20, dependsOnMethods = "testRoleCreationRejectedForUnsupportedApplications",
            description = "A child organization can create roles for the shared application, isolated from its "
                    + "parent.")
    public void testChildOrganizationCanCreateRoles() throws Exception {

        createOrganizationRoleId("orgA1", "A1-App", appAud, writeScope);
        assertTrue(getOrganizationRoles("orgA1", appAud).containsKey("A1-App"),
                "The role should be listed in the child organization.");
        assertFalse(getOrganizationRoles("orgA", appAud).containsKey("A1-App"),
                "The role of the child organization should not be listed in the parent organization.");
        assertFalse(getOrganizationRoles("orgB", appAud).containsKey("Sub-App-1"),
                "The role of orgA should not be listed in orgB.");
    }

    // ------------------------------------------------------------------------------------------------------------
    // Tokens.
    // ------------------------------------------------------------------------------------------------------------

    @Test(priority = 21, dependsOnMethods = "testChildOrganizationCanCreateRoles",
            description = "A user assigned to an organization role receives its permissions.")
    public void testTokenIncludesOrganizationRolePermissions() throws Exception {

        assignToRole("orgA", getOrganizationRoles("orgA", appAud).get("Sub-App-1"), "users", userIds.get("alice"));
        List<String> scopes = getScopes(appAud, "orgA", "ocr_alice_" + suffix);
        assertTrue(scopes.contains(readScope) && scopes.contains(writeScope),
                "The token should carry the permissions of the organization role. Scopes: " + scopes);
    }

    @Test(priority = 22, dependsOnMethods = "testTokenIncludesOrganizationRolePermissions",
            description = "A user receives inherited roles and organization roles assigned through a group.")
    public void testTokenIncludesGroupAssignedOrganizationRole() throws Exception {

        String bobId = userIds.get("bob");
        assignToRole("orgA", getOrganizationRoles("orgA", appAud).get("R-App"), "users", bobId);
        String subGroupRoleId = createOrganizationRoleId("orgA", "Sub-Group", appAud, writeScope);

        // The group is removed when orgA is deleted in the clean up.
        String groupId = scim2RestClient.createSubOrgGroup(new GroupRequestObject().displayName("ocr-team-a")
                .addMember(new GroupRequestObject.MemberItem().value(bobId)), organizationTokens.get("orgA"));
        assignToRole("orgA", subGroupRoleId, "groups", groupId);

        List<String> scopes = getScopes(appAud, "orgA", "ocr_bob_" + suffix);
        assertTrue(scopes.contains(readScope), "The inherited role should apply. Scopes: " + scopes);
        assertTrue(scopes.contains(writeScope), "The group assigned organization role should apply. Scopes: "
                + scopes);
    }

    @Test(priority = 23, dependsOnMethods = "testTokenIncludesGroupAssignedOrganizationRole",
            description = "Management API permissions are never issued for a shared application.")
    public void testManagementScopeNotIssued() throws Exception {

        assignToRole("orgA", getOrganizationRoles("orgA", appAud).get("R-Mgmt"), "users", userIds.get("bob"));
        List<String> scopes = getScopes(appAud, "orgA", "ocr_bob_" + suffix);
        assertFalse(scopes.contains(MGT_USER_VIEW_SCOPE),
                "A management API permission should not be issued. Scopes: " + scopes);
    }

    @Test(priority = 24, dependsOnMethods = "testManagementScopeNotIssued",
            description = "Organization roles apply only to the users of the organization that created them.")
    public void testOrganizationRolesIsolated() throws Exception {

        assignToRole("orgB", getOrganizationRoles("orgB", appAud).get("R-App"), "users", userIds.get("carol"));
        List<String> carolScopes = getScopes(appAud, "orgB", "ocr_carol_" + suffix);
        assertTrue(carolScopes.contains(readScope), "Scopes: " + carolScopes);
        assertFalse(carolScopes.contains(writeScope), "Scopes: " + carolScopes);

        assignToRole("orgA1", getOrganizationRoles("orgA1", appAud).get("A1-App"), "users", userIds.get("dave"));
        List<String> daveScopes = getScopes(appAud, "orgA1", "ocr_dave_" + suffix);
        assertTrue(daveScopes.contains(writeScope), "The role of the child organization should apply. Scopes: "
                + daveScopes);
    }

    @Test(priority = 25, dependsOnMethods = "testOrganizationRolesIsolated",
            description = "Changes to organization roles are reflected in the next token.")
    public void testRoleChangesReflectedInNextToken() throws Exception {

        String aliceId = userIds.get("alice");
        String roleId = getOrganizationRoles("orgA", appAud).get("Sub-App-1");
        ApiResponse response = send("PATCH", orgBasePath() + ROLES_V2_PATH + "/" + roleId,
                patchOperation("remove", "users[value eq " + aliceId + "]", null), organizationTokens.get("orgA"));
        assertEquals(response.status, 200, response.body);

        List<String> scopes = getScopes(appAud, "orgA", "ocr_alice_" + suffix);
        assertFalse(scopes.contains(writeScope), "The unassigned role should not apply. Scopes: " + scopes);
    }

    @Test(priority = 26, dependsOnMethods = "testRoleChangesReflectedInNextToken",
            description = "Organization roles apply to an organization audience application only after custom "
                    + "roles are allowed, limited to the permissions authorized for the application.")
    public void testOrganizationRolesForOrganizationAudienceApplication() throws Exception {

        // The organization audience role is removed when orgA is deleted in the clean up.
        String roleId = createOwnOrganizationRole("orgA", "Sub-Org-1", readScope, writeScope);
        assignToRole("orgA", roleId, "users", userIds.get("alice"));

        List<String> scopes = getScopes(appOrgAud, "orgA", "ocr_alice_" + suffix);
        assertFalse(scopes.contains(readScope), "Organization roles should not apply before custom roles are "
                + "allowed. Scopes: " + scopes);

        enableOrganizationCustomRoles(appOrgAud);
        scopes = getScopes(appOrgAud, "orgA", "ocr_alice_" + suffix);
        assertTrue(scopes.contains(readScope), "The organization role should apply. Scopes: " + scopes);
        assertFalse(scopes.contains(writeScope), "Only the permissions authorized for the application should be "
                + "issued. Scopes: " + scopes);
    }

    // ------------------------------------------------------------------------------------------------------------
    // Changes in the root organization.
    // ------------------------------------------------------------------------------------------------------------

    @Test(priority = 27, dependsOnMethods = "testOrganizationRolesForOrganizationAudienceApplication",
            description = "An organization keeps its own role when the root organization shares a role with the same "
                    + "name.")
    public void testRootRoleWithSameNameKeepsOrganizationRole() throws Exception {

        String ownRoleId = createOrganizationRoleId("orgA", "Clash-1", appAud, writeScope);

        rootRoleIds.add(createRootApplicationRole(appAud, "Clash-1", readScope));
        waitForRoles("The root role should be shared with orgB.", "orgB", appAud, true, "Clash-1");
        assertEquals(getOrganizationRoles("orgA", appAud).get("Clash-1"), ownRoleId,
                "orgA should keep its own role instead of receiving the shared role.");
    }

    @Test(priority = 28, dependsOnMethods = "testRootRoleWithSameNameKeepsOrganizationRole",
            description = "Renaming a root role to a name used by an organization keeps the organization role.")
    public void testRootRoleRenameKeepsOrganizationRole() throws Exception {

        String rootRoleId = createRootApplicationRole(appAud, "Ren-Src", readScope);
        rootRoleIds.add(rootRoleId);
        waitForRoles("The root role should be shared with orgA.", "orgA", appAud, true, "Ren-Src");
        waitForRoles("The root role should be shared with orgB.", "orgB", appAud, true, "Ren-Src");
        String ownRoleId = createOrganizationRoleId("orgA", "Clash-2", appAud, writeScope);

        ApiResponse rename = send("PATCH", rootBasePath + ROLES_V2_PATH + "/" + rootRoleId,
                patchOperation("replace", "displayName", "Clash-2"), null);
        assertEquals(rename.status, 200, rename.body);
        waitForRoles("The shared role should be renamed in orgB.", "orgB", appAud, true, "Clash-2");

        Map<String, String> orgARoles = getOrganizationRoles("orgA", appAud);
        assertEquals(orgARoles.get("Clash-2"), ownRoleId, "orgA should keep its own role.");
        assertTrue(orgARoles.containsKey("Ren-Src"), "The shared copy in orgA should keep its previous name.");
    }

    @Test(priority = 29, dependsOnMethods = "testRootRoleRenameKeepsOrganizationRole",
            description = "Changing the role sharing policy does not affect organization roles.")
    public void testRoleSharingPolicyKeepsOrganizationRoles() throws Exception {

        shareWithOrganization(appAud, "orgA", SELECTED_ORG_WITH_ALL_CHILDREN,
                new JSONObject().put("mode", ROLE_SHARING_MODE_SELECTED)
                        .put("roles", new JSONArray().put(new JSONObject().put("displayName", "R-App")
                                .put("audience", new JSONObject().put("display", appAud).put("type", "application")))));
        waitForRoles("Only R-App should be shared with orgA.", "orgA", appAud, false, "R-Mgmt");
        assertTrue(getOrganizationRoles("orgA", appAud).keySet().containsAll(Arrays.asList("R-App", "Sub-App-1")),
                "The selected role and the organization role should remain.");

        shareWithOrganization(appAud, "orgA", SELECTED_ORG_WITH_ALL_CHILDREN,
                new JSONObject().put("mode", ROLE_SHARING_MODE_NONE));
        waitForRoles("No roles should be shared with orgA.", "orgA", appAud, false, "R-App");
        assertTrue(getOrganizationRoles("orgA", appAud).containsKey("Sub-App-1"),
                "Organization roles should not be affected by the role sharing policy.");

        shareWithOrganization(appAud, "orgA", SELECTED_ORG_WITH_ALL_CHILDREN,
                new JSONObject().put("mode", ROLE_SHARING_MODE_ALL));
        waitForRoles("All roles should be shared with orgA again.", "orgA", appAud, true, "R-App", "R-Mgmt");
        assertTrue(getOrganizationRoles("orgA", appAud).containsKey("Sub-App-1"),
                "The organization role should remain after sharing all roles again.");
    }

    @Test(priority = 30, dependsOnMethods = "testRoleSharingPolicyKeepsOrganizationRoles",
            description = "Deleting a root role removes only its shared copies.")
    public void testRootRoleDeletionKeepsOrganizationRoles() throws Exception {

        String rootRoleId = createRootApplicationRole(appAud, "R-Del", readScope);
        waitForRoles("The root role should be shared with orgA.", "orgA", appAud, true, "R-Del");
        ApiResponse delete = send("DELETE", rootBasePath + ROLES_V2_PATH + "/" + rootRoleId, null, null);
        assertEquals(delete.status, 204, delete.body);
        waitForRoles("The shared copy should be deleted.", "orgA", appAud, false, "R-Del");
        assertTrue(getOrganizationRoles("orgA", appAud).containsKey("Sub-App-1"),
                "The organization role should not be affected by deleting a root role.");
    }

    @Test(priority = 31, dependsOnMethods = "testRootRoleDeletionKeepsOrganizationRoles",
            description = "A new child organization receives the application and can create roles for it.")
    public void testNewOrganizationCanCreateRoles() throws Exception {

        organizationIds.put("orgA2", orgMgtRestClient.addSubOrganization("ocr-orgA2-" + suffix,
                organizationIds.get("orgA")));
        organizationTokens.put("orgA2", orgMgtRestClient.switchM2MToken(organizationIds.get("orgA2")));
        waitForSharedApplication(appAud, "orgA2");
        createOrganizationRoleId("orgA2", "A2-App", appAud, readScope);
    }

    @Test(priority = 32, dependsOnMethods = "testNewOrganizationCanCreateRoles",
            description = "Only APIs authorized later that are not management APIs can be used by organizations.")
    public void testApisAuthorizedLater() throws Exception {

        authorizeApi(appAud, getApiResourceId(MGT_GROUP_SCIM_API), MGT_GROUP_VIEW_SCOPE);
        assertEquals(createOrganizationRole("orgA", "Sub-Groups-Mgmt", appAud, MGT_GROUP_VIEW_SCOPE).status, 400,
                "A management API authorized later should not be usable.");

        authorizeApi(appAud, getApiResourceId(ORG_GROUP_SCIM_API), ORG_GROUP_VIEW_SCOPE);
        // An organization API authorized later is usable.
        createOrganizationRoleId("orgA", "Sub-Groups-Org", appAud, ORG_GROUP_VIEW_SCOPE);
    }

    @Test(priority = 33, dependsOnMethods = "testApisAuthorizedLater",
            description = "Unrelated updates to the application do not affect organization roles.")
    public void testUnrelatedUpdateKeepsOrganizationRoles() throws Exception {

        updateRootApplication(appAud, new ApplicationPatchModel().description("Updated description"));
        assertTrue(getOrganizationRoles("orgA", appAud).containsKey("Sub-App-1"),
                "The organization role should not be affected by an unrelated update.");
    }

    // ------------------------------------------------------------------------------------------------------------
    // Actions that delete organization roles.
    // ------------------------------------------------------------------------------------------------------------

    @Test(priority = 34, dependsOnMethods = "testUnrelatedUpdateKeepsOrganizationRoles",
            description = "An organization that has its own roles can be deleted.")
    public void testDeleteOrganizationWithOwnRoles() throws Exception {

        // Create a role in this test, so that the organization has its own role regardless of the other tests.
        createOrganizationRoleId("orgA2", "A2-Own", appAud, readScope);
        assertTrue(getOrganizationRoles("orgA2", appAud).containsKey("A2-Own"),
                "The role should be listed in orgA2 before the organization is deleted.");

        orgMgtRestClient.deleteSubOrganization(organizationIds.get("orgA2"), organizationIds.get("orgA"));
        organizationIds.remove("orgA2");
    }

    @Test(priority = 35, dependsOnMethods = "testDeleteOrganizationWithOwnRoles",
            description = "An organization can delete its own role.")
    public void testOrganizationCanDeleteOwnRole() throws Exception {

        String roleId = getOrganizationRoles("orgA", appAud).get("Sub-Scim");
        assertNotNull(roleId, "The Sub-Scim role created earlier is not found.");
        ApiResponse response = send("DELETE", orgBasePath() + ROLES_V2_PATH + "/" + roleId, null,
                organizationTokens.get("orgA"));
        assertEquals(response.status, 204, response.body);
        assertFalse(getOrganizationRoles("orgA", appAud).containsKey("Sub-Scim"),
                "The deleted role should not be listed.");
    }

    @Test(priority = 36, dependsOnMethods = "testOrganizationCanDeleteOwnRole",
            description = "Unsharing the application from an organization deletes the roles of that organization, "
                    + "and sharing it again does not restore them.")
    public void testUnshareDeletesOrganizationRoles() throws Exception {

        createOrganizationRoleId("orgB", "B-App", appAud, writeScope);

        ApiResponse unshare = send("POST", rootBasePath + APPLICATIONS_PATH + "/unshare", new JSONObject()
                .put("applicationId", applications.get(appAud).id)
                .put("orgIds", new JSONArray().put(organizationIds.get("orgB"))).toString(), null);
        assertTrue(unshare.status == 202 || unshare.status == 200, unshare.body);
        waitUntil("The application should be unshared from orgB.",
                () -> StringUtils.isBlank(findSharedApplicationId(appAud, "orgB")));
        assertTrue(getOrganizationRoles("orgA", appAud).containsKey("Sub-App-1"),
                "The roles of other organizations should not be affected.");

        shareWithOrganization(appAud, "orgB", SELECTED_ORG_ONLY, new JSONObject().put("mode", ROLE_SHARING_MODE_ALL));
        waitForSharedApplication(appAud, "orgB");
        waitForRoles("The inherited roles should be shared with orgB again.", "orgB", appAud, true, "R-App");
        assertFalse(getOrganizationRoles("orgB", appAud).containsKey("B-App"),
                "The deleted organization role should not be restored.");
    }

    @Test(priority = 37, dependsOnMethods = "testUnshareDeletesOrganizationRoles",
            description = "Disallowing custom roles deletes the application roles created by organizations, and "
                    + "allowing them again does not restore them.")
    public void testDisableDeletesOrganizationRoles() throws Exception {

        // The roles expected to be deleted must exist first, so that their absence is not trivially true.
        assertTrue(getOrganizationRoles("orgA", appAud).keySet().containsAll(Arrays.asList("Sub-App-1", "Clash-1")),
                "The organization roles of orgA should exist before custom roles are disallowed.");
        assertTrue(getOrganizationRoles("orgA1", appAud).containsKey("A1-App"),
                "The organization role of orgA1 should exist before custom roles are disallowed.");

        updateRootApplication(appAud, new ApplicationPatchModel().organizationCustomRolesEnabled(false));
        waitForRoles("The organization roles should be deleted.", "orgA", appAud, false, "Sub-App-1", "Clash-1");
        waitForRoles("The organization roles of the child organization should be deleted.", "orgA1", appAud, false,
                "A1-App");
        assertTrue(getOrganizationRoles("orgA", appAud).containsKey("R-App"),
                "The inherited roles should not be affected.");
        ApiResponse create = createOrganizationRole("orgA", "Sub-While-Off", appAud, readScope);
        assertEquals(create.status, 400, "Role creation should fail while custom roles are disallowed. "
                + create.body);

        enableOrganizationCustomRoles(appAud);
        Map<String, String> roles = getOrganizationRoles("orgA", appAud);
        assertFalse(roles.containsKey("Sub-App-1"), "The deleted roles should not be restored.");
        // Roles can be created again once custom roles are allowed.
        createOrganizationRoleId("orgA", "After-On", appAud, readScope);
    }

    @Test(priority = 38, dependsOnMethods = "testDisableDeletesOrganizationRoles",
            description = "Changing the role audience of the application to organization deletes the application "
                    + "roles created by organizations.")
    public void testAudienceSwitchDeletesOrganizationRoles() throws Exception {

        String app = createCustomRolesApplicationWithOrganizationRole("OCR-AUDIENCE-" + suffix, "Aud-A");
        updateRootApplication(app, new ApplicationPatchModel().associatedRoles(new AssociatedRolesConfig()
                .allowedAudience(AssociatedRolesConfig.AllowedAudienceEnum.ORGANIZATION).roles(new ArrayList<>())));
        waitForRoles("The organization role should be deleted.", "orgA", app, false, "Aud-A");
    }

    @Test(priority = 39, dependsOnMethods = "testAudienceSwitchDeletesOrganizationRoles",
            description = "Turning off enhanced organization login together with custom roles deletes the "
                    + "application roles created by organizations.")
    public void testDisableEnhancedOrgAuthenticationWithCustomRoles() throws Exception {

        String app = createCustomRolesApplicationWithOrganizationRole("OCR-ENHANCED-" + suffix, "Enh-A");
        updateRootApplication(app, new ApplicationPatchModel().enhancedOrgAuthenticationEnabled(false)
                .organizationCustomRolesEnabled(false));
        ApplicationResponseModel application = getRootApplication(app);
        assertEquals(application.getEnhancedOrgAuthenticationEnabled(), Boolean.FALSE,
                "Enhanced organization login should be disabled.");
        assertEquals(application.getOrganizationCustomRolesEnabled(), Boolean.FALSE,
                "Organization custom roles should be disabled.");
        waitForRoles("The organization role should be deleted.", "orgA", app, false, "Enh-A");
    }

    @Test(priority = 40, dependsOnMethods = "testDisableEnhancedOrgAuthenticationWithCustomRoles",
            description = "Deleting the application deletes its roles in every organization.")
    public void testDeleteApplicationDeletesOrganizationRoles() throws Exception {

        String app = createCustomRolesApplicationWithOrganizationRole("OCR-DELETE-" + suffix, "Del-A");
        String roleId = getOrganizationRoles("orgA", app).get("Del-A");
        ApiResponse response = send("DELETE", rootBasePath + APPLICATIONS_PATH + "/" + applications.get(app).id,
                null, null);
        assertEquals(response.status, 204, response.body);
        applications.remove(app);
        waitUntil("The organization role should be deleted with the application.",
                () -> send("GET", orgBasePath() + ROLES_V2_PATH + "/" + roleId, null,
                        organizationTokens.get("orgA")).status == 404);
    }

    @Test(priority = 41, dependsOnMethods = "testDeleteApplicationDeletesOrganizationRoles",
            description = "Sharing an organization audience application with an organization that has its own "
                    + "organization role with the name of a root organization role keeps that role and shares the "
                    + "application with the other roles.")
    public void testShareOrganizationAudienceApplicationWithConflict() throws Exception {

        String appName = "OCR-ORG-SHARE-" + suffix;
        createOrganizationAudienceApplicationWithCustomRoles(appName);
        String conflictingRole = "Share-Clash-" + suffix;
        String sharedRole = "Share-Ok-" + suffix;
        // The organization creates its role before the root organization creates a role with the same name.
        String ownRoleId = createOwnOrganizationRole("orgB", conflictingRole, readScope);
        createRootOrganizationRole(conflictingRole, readScope);
        createRootOrganizationRole(sharedRole, readScope);

        shareWithOrganizations(appName, new String[][]{{"orgB", SELECTED_ORG_ONLY}});
        waitForSharedApplication(appName, "orgB");
        waitUntil("The role without a conflict should be shared with orgB.",
                () -> getRolesByAudience("orgB", organizationIds.get("orgB")).containsKey(sharedRole));
        assertEquals(getRolesByAudience("orgB", organizationIds.get("orgB")).get(conflictingRole), ownRoleId,
                "orgB should keep its own role instead of receiving the shared role.");
    }

    @AfterClass(alwaysRun = true)
    public void cleanupTest() throws Exception {

        for (TestApplication application : applications.values()) {
            deleteQuietly(rootBasePath + APPLICATIONS_PATH + "/" + application.id);
        }
        for (String roleId : rootRoleIds) {
            deleteQuietly(rootBasePath + ROLES_V2_PATH + "/" + roleId);
        }
        if (apiResourceManagementClient != null) {
            if (businessApiId != null) {
                try {
                    apiResourceManagementClient.deleteAPIResource(businessApiId);
                } catch (Throwable e) {
                    log.error("Failed to delete the API resource: " + businessApiId, e);
                }
            }
            apiResourceManagementClient.closeHttpClient();
        }
        if (orgMgtRestClient != null) {
            for (String child : Arrays.asList("orgA2", "orgA1")) {
                if (organizationIds.containsKey(child)) {
                    try {
                        orgMgtRestClient.deleteSubOrganization(organizationIds.get(child), organizationIds.get("orgA"));
                    } catch (Throwable e) {
                        log.error("Failed to delete the organization: " + child, e);
                    }
                }
            }
            for (String org : Arrays.asList("orgA", "orgB")) {
                if (organizationIds.containsKey(org)) {
                    try {
                        orgMgtRestClient.deleteOrganization(organizationIds.get(org));
                    } catch (Throwable e) {
                        log.error("Failed to delete the organization: " + org, e);
                    }
                }
            }
            orgMgtRestClient.closeHttpClient();
        }
        if (scim2RestClient != null) {
            scim2RestClient.closeHttpClient();
        }
        if (oAuth2RestClient != null) {
            oAuth2RestClient.closeHttpClient();
        }
        if (apiClient != null) {
            apiClient.close();
        }
    }

    // ------------------------------------------------------------------------------------------------------------
    // Helpers.
    // ------------------------------------------------------------------------------------------------------------

    private boolean isSuperTenant() {

        return MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantInfo.getDomain());
    }

    private String orgBasePath() {

        return rootBasePath + "o/";
    }

    private String createBusinessApiResource() throws Exception {

        APIResourceCreationModel apiResource = new APIResourceCreationModel();
        apiResource.setIdentifier("ocr_orders_" + suffix);
        apiResource.setName("OCR Orders " + suffix);
        apiResource.setRequiresAuthorization(true);
        for (String scopeName : Arrays.asList(readScope, writeScope)) {
            ScopeCreationModel scope = new ScopeCreationModel();
            scope.setName(scopeName);
            scope.setDisplayName(scopeName);
            apiResource.addScopesItem(scope);
        }
        return apiResourceManagementClient.createAPIResource(apiResource);
    }

    private String getApiResourceId(String identifier) throws Exception {

        List<APIResourceListItem> apiResources =
                apiResourceManagementClient.getAPIResourcesWithFiltering("identifier+eq+" + identifier);
        assertTrue(apiResources != null && !apiResources.isEmpty(), "API resource not found: " + identifier);
        return apiResources.get(0).getId();
    }

    private ApplicationModel buildApplication(String name, String audience, boolean enhancedOrgAuthentication) {

        OpenIDConnectConfiguration oidc = new OpenIDConnectConfiguration();
        oidc.setGrantTypes(Collections.singletonList(OAUTH2_GRANT_TYPE_AUTHORIZATION_CODE));
        oidc.setCallbackURLs(Collections.singletonList(CALLBACK_URL));
        InboundProtocols inboundProtocols = new InboundProtocols();
        inboundProtocols.setOidc(oidc);

        ApplicationModel application = new ApplicationModel();
        application.setName(name);
        application.setInboundProtocolConfiguration(inboundProtocols);
        return application
                .advancedConfigurations(new AdvancedApplicationConfiguration().skipLoginConsent(true)
                        .skipLogoutConsent(true))
                .associatedRoles(new AssociatedRolesConfig()
                        .allowedAudience(AssociatedRolesConfig.AllowedAudienceEnum.valueOf(audience))
                        .roles(new ArrayList<>()))
                .enhancedOrgAuthenticationEnabled(enhancedOrgAuthentication);
    }

    /**
     * Create an application authorized for the business API, and the management and organization SCIM2 Users APIs.
     * An organization audience application is authorized only for the read scope of the business API.
     */
    private void createApplication(String name, String audience, boolean enhancedOrgAuthentication)
            throws Exception {

        String id = oAuth2RestClient.createApplication(buildApplication(name, audience, enhancedOrgAuthentication));
        OpenIDConnectConfiguration oidc = oAuth2RestClient.getOIDCInboundDetails(id);
        applications.put(name, new TestApplication(id, oidc.getClientId(), oidc.getClientSecret()));

        if (AUDIENCE_ORGANIZATION.equals(audience)) {
            authorizeApi(name, businessApiId, readScope);
            return;
        }
        authorizeApi(name, businessApiId, readScope, writeScope);
        authorizeApi(name, getApiResourceId(MGT_USER_SCIM_API), MGT_USER_VIEW_SCOPE);
        authorizeApi(name, getApiResourceId(ORG_USER_SCIM_API), ORG_USER_VIEW_SCOPE);
    }

    private void authorizeApi(String appName, String apiId, String... scopes) throws Exception {

        int status = oAuth2RestClient.addAPIAuthorizationToApplication(applications.get(appName).id,
                new AuthorizedAPICreationModel().id(apiId).policyIdentifier(RBAC_POLICY)
                        .scopes(Arrays.asList(scopes)));
        assertTrue(status == 200 || status == 201, "API authorization failed for " + appName + ". Status: " + status);
    }

    private void enableOrganizationCustomRoles(String appName) throws Exception {

        updateRootApplication(appName, new ApplicationPatchModel().organizationCustomRolesEnabled(true));
    }

    private void updateRootApplication(String appName, ApplicationPatchModel patch) throws Exception {

        oAuth2RestClient.updateApplication(applications.get(appName).id, patch);
    }

    private void assertEnhancedOrgAuthenticationRequiredError(ApiResponse response) throws Exception {

        JSONObject error = new JSONObject(response.body);
        assertEquals(error.optString("code"), INVALID_APPLICATION_CONFIGURATION_ERROR_CODE, response.body);
        assertTrue(error.optString("description").contains(ENHANCED_ORG_AUTHENTICATION_REQUIRED_ERROR),
                response.body);
    }

    private ApplicationResponseModel getRootApplication(String appName) throws Exception {

        return oAuth2RestClient.getApplication(applications.get(appName).id);
    }

    /**
     * Create an application with custom roles allowed, share it with orgA and create an organization role for it.
     */
    private String createCustomRolesApplicationWithOrganizationRole(String name, String roleName) throws Exception {

        createApplication(name, AUDIENCE_APPLICATION, true);
        enableOrganizationCustomRoles(name);
        shareWithOrganizations(name, new String[][]{{"orgA", SELECTED_ORG_ONLY}});
        waitForSharedApplication(name, "orgA");
        createOrganizationRoleId("orgA", roleName, name, readScope);
        return name;
    }

    /**
     * Create an organization audience application with custom roles allowed.
     */
    private void createOrganizationAudienceApplicationWithCustomRoles(String name) throws Exception {

        createApplication(name, AUDIENCE_ORGANIZATION, true);
        enableOrganizationCustomRoles(name);
    }

    private String createRootApplicationRole(String appName, String roleName, String... permissions)
            throws Exception {

        return scim2RestClient.addV2Role(roleRequest(roleName, AUDIENCE_APPLICATION, applications.get(appName).id,
                permissions));
    }

    /**
     * Create an organization audience role in the root organization.
     */
    private String createRootOrganizationRole(String roleName, String... permissions) throws Exception {

        String roleId = scim2RestClient.addV2Role(new RoleV2(roleName, toPermissions(permissions),
                Collections.emptyList()));
        rootRoleIds.add(roleId);
        return roleId;
    }

    /**
     * Create an organization audience role owned by the organization.
     */
    private String createOwnOrganizationRole(String org, String roleName, String... permissions) throws Exception {

        return scim2RestClient.addOrganizationV2Roles(roleRequest(roleName, AUDIENCE_ORGANIZATION,
                organizationIds.get(org), permissions), organizationTokens.get(org));
    }

    /**
     * Create an application role for the shared application in the organization, and return the role ID.
     */
    private String createOrganizationRoleId(String org, String roleName, String appName, String... permissions)
            throws Exception {

        return scim2RestClient.addOrganizationV2Roles(roleRequest(roleName, AUDIENCE_APPLICATION,
                getSharedApplicationId(appName, org), permissions), organizationTokens.get(org));
    }

    /**
     * Send a request to create an application role for the shared application in the organization, to verify the
     * status of the response.
     */
    private ApiResponse createOrganizationRole(String org, String roleName, String appName, String... permissions)
            throws Exception {

        RoleV2 role = roleRequest(roleName, AUDIENCE_APPLICATION, getSharedApplicationId(appName, org), permissions);
        return send("POST", orgBasePath() + ROLES_V2_PATH, toJson(role), organizationTokens.get(org));
    }

    private RoleV2 roleRequest(String roleName, String audienceType, String audienceValue, String... permissions) {

        return new RoleV2(new Audience(audienceType, audienceValue), roleName, toPermissions(permissions),
                Collections.emptyList());
    }

    private List<Permission> toPermissions(String... permissions) {

        List<Permission> permissionList = new ArrayList<>();
        for (String permission : permissions) {
            permissionList.add(new Permission(permission));
        }
        return permissionList;
    }

    /**
     * Get the roles of a shared application in an organization, keyed by the role name.
     */
    private Map<String, String> getOrganizationRoles(String org, String appName) throws Exception {

        return getRolesByAudience(org, getSharedApplicationId(appName, org));
    }

    /**
     * Get the roles of an organization with the given audience, keyed by the role name. The audience is a shared
     * application for application roles, and the organization for organization roles.
     */
    private Map<String, String> getRolesByAudience(String org, String audienceValue) throws Exception {

        ApiResponse response = send("GET", orgBasePath() + ROLES_V2_PATH + "?count=200&filter="
                + encode("audience.value eq " + audienceValue), null, organizationTokens.get(org));
        assertEquals(response.status, 200, response.body);
        Map<String, String> roles = new HashMap<>();
        JSONArray resources = new JSONObject(response.body).optJSONArray("Resources");
        if (resources != null) {
            for (int i = 0; i < resources.length(); i++) {
                JSONObject role = resources.getJSONObject(i);
                roles.put(role.getString("displayName"), role.getString("id"));
            }
        }
        return roles;
    }

    /**
     * List the applications of the test, with the organization custom roles attribute, keyed by the application name.
     */
    private Map<String, Boolean> listOrganizationCustomRolesValues(String basePath, String bearerToken)
            throws Exception {

        ApiResponse response = send("GET", basePath + APPLICATIONS_PATH + "?limit=100&attributes="
                + ORGANIZATION_CUSTOM_ROLES_ENABLED + "&filter=" + encode("name sw OCR-"), null, bearerToken);
        assertEquals(response.status, 200, response.body);
        Map<String, Boolean> values = new HashMap<>();
        JSONArray list = new JSONObject(response.body).getJSONArray("applications");
        for (int i = 0; i < list.length(); i++) {
            JSONObject item = list.getJSONObject(i);
            values.put(item.getString("name"), item.optBoolean(ORGANIZATION_CUSTOM_ROLES_ENABLED));
        }
        return values;
    }

    private void assignToRole(String org, String roleId, String path, String memberId) throws Exception {

        assertNotNull(roleId, "Role to assign is not found.");
        ApiResponse response = send("PATCH", orgBasePath() + ROLES_V2_PATH + "/" + roleId,
                patchOperation("add", path, new JSONArray().put(new JSONObject().put("value", memberId))),
                organizationTokens.get(org));
        assertEquals(response.status, 200, "Role assignment failed. " + response.body);
    }

    private String patchOperation(String op, String path, Object value) throws Exception {

        JSONObject operation = new JSONObject().put("op", op).put("path", path);
        if (value != null) {
            operation.put("value", value);
        }
        return new JSONObject().put("schemas", new JSONArray().put(PATCH_OP_SCHEMA))
                .put("Operations", new JSONArray().put(operation)).toString();
    }

    private ApiResponse patchApplication(String appName, JSONObject patch) throws Exception {

        return send("PATCH", rootBasePath + APPLICATIONS_PATH + "/" + applications.get(appName).id,
                patch.toString(), null);
    }

    private void shareWithOrganizations(String appName, String[][] orgPolicies) throws Exception {

        JSONArray organizations = new JSONArray();
        for (String[] orgPolicy : orgPolicies) {
            organizations.put(new JSONObject().put("orgId", organizationIds.get(orgPolicy[0]))
                    .put("policy", orgPolicy[1])
                    .put("roleSharing", new JSONObject().put("mode", ROLE_SHARING_MODE_ALL)));
        }
        share(appName, organizations);
    }

    private void shareWithOrganization(String appName, String org, String policy, JSONObject roleSharing)
            throws Exception {

        share(appName, new JSONArray().put(new JSONObject().put("orgId", organizationIds.get(org))
                .put("policy", policy).put("roleSharing", roleSharing)));
    }

    private void share(String appName, JSONArray organizations) throws Exception {

        JSONObject body = new JSONObject().put("applicationId", applications.get(appName).id)
                .put("organizations", organizations);
        ApiResponse response = send("POST", rootBasePath + APPLICATIONS_PATH + "/share", body.toString(), null);
        assertTrue(response.status == 202 || response.status == 200, "Sharing failed. " + response.body);
    }

    private String findSharedApplicationId(String appName, String org) throws Exception {

        return oAuth2RestClient.getAppIdUsingAppNameInOrganization(appName, organizationTokens.get(org));
    }

    private String getSharedApplicationId(String appName, String org) throws Exception {

        String sharedAppId = findSharedApplicationId(appName, org);
        assertTrue(StringUtils.isNotBlank(sharedAppId), appName + " is not shared with " + org);
        return sharedAppId;
    }

    private void waitForSharedApplication(String appName, String org) throws Exception {

        waitUntil(appName + " should be shared with " + org,
                () -> StringUtils.isNotBlank(findSharedApplicationId(appName, org)));
    }

    /**
     * Wait until all the given roles are present (or absent) for the shared application in the organization.
     */
    private void waitForRoles(String message, String org, String appName, boolean present, String... roleNames)
            throws Exception {

        waitUntil(message, () -> {
            Map<String, String> roles = getOrganizationRoles(org, appName);
            return Arrays.stream(roleNames).allMatch(name -> roles.containsKey(name) == present);
        });
    }

    private void waitUntil(String message, Callable<Boolean> condition) {

        await(message).atMost(WAIT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .pollInterval(WAIT_INTERVAL_SECONDS, TimeUnit.SECONDS)
                .until(condition);
    }

    private String createOrganizationUser(String org, String username) throws Exception {

        UserObject user = new UserObject();
        user.setUserName(username);
        user.setPassword(USER_PASSWORD);
        user.addEmail(new Email().value(username + "@wso2.com"));
        return scim2RestClient.createSubOrgUser(user, organizationTokens.get(org));
    }

    private ApiResponse importApplication(String xml) throws Exception {

        String boundary = "----ocr" + UUID.randomUUID().toString().replace("-", "");
        String body = "--" + boundary + "\r\n"
                + "Content-Disposition: form-data; name=\"file\"; filename=\"application.xml\"\r\n"
                + "Content-Type: application/xml\r\n\r\n" + xml + "\r\n--" + boundary + "--\r\n";
        RequestBuilder request = RequestBuilder.post(rootBasePath + APPLICATIONS_PATH + "/import")
                .setEntity(new StringEntity(body, ContentType.create("multipart/form-data",
                        new BasicNameValuePair("boundary", boundary))))
                .addHeader("Authorization", "Basic " + getBase64EncodedString(
                        tenantInfo.getContextUser().getUserName(), tenantInfo.getContextUser().getPassword()));
        try (CloseableHttpResponse response = apiClient.execute(request.build())) {
            return toApiResponse(response);
        }
    }

    // ------------------------------------------------------------------------------------------------------------
    // Login.
    // ------------------------------------------------------------------------------------------------------------

    /**
     * Log in to the application through the enhanced organization login direct path, and return the scopes of the
     * introspected access token.
     */
    private List<String> getScopes(String appName, String org, String username) throws Exception {

        TestApplication application = applications.get(appName);
        String orgId = organizationIds.get(org);
        String tenantDomain = tenantInfo.getDomain();
        String scope = String.join(" ", "openid", readScope, writeScope, MGT_USER_VIEW_SCOPE);

        try (CloseableHttpClient client = createBrowserClient()) {
            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("response_type", "code"));
            params.add(new BasicNameValuePair("client_id", application.clientId));
            params.add(new BasicNameValuePair("redirect_uri", CALLBACK_URL));
            params.add(new BasicNameValuePair("scope", scope));
            HttpResponse response = sendPostRequestWithParameters(client, params,
                    getRootTenantQualifiedOrgURL(AUTHORIZE_ENDPOINT_URL, tenantDomain, orgId));
            Header location = response.getFirstHeader(HTTP_RESPONSE_HEADER_LOCATION);
            assertNotNull(location, "Authorize request did not redirect to the login page.");
            EntityUtils.consume(response.getEntity());

            response = sendGetRequest(client, location.getValue());
            Map<String, Integer> keyPositionMap = new HashMap<>(1);
            keyPositionMap.put("name=\"sessionDataKey\"", 1);
            List<DataExtractUtil.KeyValue> keyValues = DataExtractUtil.extractDataFromResponse(response,
                    keyPositionMap);
            assertTrue(keyValues != null && !keyValues.isEmpty(), "sessionDataKey is not found on the login page.");
            EntityUtils.consume(response.getEntity());

            List<NameValuePair> login = new ArrayList<>();
            login.add(new BasicNameValuePair("username", username));
            login.add(new BasicNameValuePair("password", USER_PASSWORD));
            login.add(new BasicNameValuePair("sessionDataKey", keyValues.get(0).getValue()));
            response = sendPostRequestWithParameters(client, login,
                    getRootTenantQualifiedOrgURL(COMMON_AUTH_URL, tenantDomain, orgId));
            location = response.getFirstHeader(HTTP_RESPONSE_HEADER_LOCATION);
            assertNotNull(location, "Login did not redirect to the authorize endpoint.");
            EntityUtils.consume(response.getEntity());

            response = sendGetRequest(client, location.getValue());
            location = response.getFirstHeader(HTTP_RESPONSE_HEADER_LOCATION);
            assertNotNull(location, "Authorize endpoint did not redirect to the callback URL.");
            EntityUtils.consume(response.getEntity());
            String code = URLEncodedUtils.parse(URI.create(location.getValue()), StandardCharsets.UTF_8).stream()
                    .filter(param -> "code".equals(param.getName()))
                    .map(NameValuePair::getValue)
                    .findFirst()
                    .orElse(null);
            assertNotNull(code, "Authorization code is not returned for " + username);

            List<NameValuePair> tokenParams = new ArrayList<>();
            tokenParams.add(new BasicNameValuePair("code", code));
            tokenParams.add(new BasicNameValuePair("grant_type", OAUTH2_GRANT_TYPE_AUTHORIZATION_CODE));
            tokenParams.add(new BasicNameValuePair("redirect_uri", CALLBACK_URL));
            List<Header> headers = new ArrayList<>();
            headers.add(new BasicHeader("Authorization", "Basic "
                    + getBase64EncodedString(application.clientId, application.clientSecret)));
            headers.add(new BasicHeader("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8"));
            headers.add(new BasicHeader("User-Agent", OAuth2Constant.USER_AGENT));
            response = sendPostRequest(client, headers, tokenParams,
                    getRootTenantQualifiedOrgURL(ACCESS_TOKEN_ENDPOINT, tenantDomain, orgId));
            JSONObject token = new JSONObject(EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8));
            assertTrue(token.has("access_token"), "Access token is not returned. " + token);

            org.json.simple.JSONObject introspection = introspectTokenWithTenant(client,
                    token.getString("access_token"),
                    getTenantQualifiedURL(OAuth2Constant.INTRO_SPEC_ENDPOINT, tenantDomain),
                    tenantInfo.getTenantAdmin().getUserName(), tenantInfo.getTenantAdmin().getPassword());
            Object scopes = introspection.get("scope");
            return scopes == null ? new ArrayList<>() : Arrays.asList(scopes.toString().split(" "));
        }
    }

    private CloseableHttpClient createBrowserClient() {

        Lookup<CookieSpecProvider> cookieSpecRegistry = RegistryBuilder.<CookieSpecProvider>create()
                .register(CookieSpecs.DEFAULT, new RFC6265CookieSpecProvider())
                .build();
        return HttpClientBuilder.create()
                .setDefaultCookieStore(new BasicCookieStore())
                .setDefaultRequestConfig(RequestConfig.custom().setCookieSpec(CookieSpecs.DEFAULT).build())
                .setDefaultCookieSpecRegistry(cookieSpecRegistry)
                .setRedirectStrategy(new DefaultRedirectStrategy() {
                    @Override
                    protected boolean isRedirectable(String method) {

                        return false;
                    }
                })
                .build();
    }

    // ------------------------------------------------------------------------------------------------------------
    // HTTP.
    // ------------------------------------------------------------------------------------------------------------

    private ApiResponse send(String method, String url, String body, String bearerToken) throws Exception {

        return send(method, url, body, bearerToken, "application/json");
    }

    /**
     * Send a request with the basic credentials of the tenant admin, or the given bearer token.
     */
    private ApiResponse send(String method, String url, String body, String bearerToken, String accept)
            throws Exception {

        RequestBuilder request = RequestBuilder.create(method).setUri(url).addHeader("Accept", accept);
        if (bearerToken != null) {
            request.addHeader("Authorization", "Bearer " + bearerToken);
        } else {
            request.addHeader("Authorization", "Basic " + getBase64EncodedString(
                    tenantInfo.getContextUser().getUserName(), tenantInfo.getContextUser().getPassword()));
        }
        if (body != null) {
            request.setEntity(new StringEntity(body, ContentType.APPLICATION_JSON));
        }
        try (CloseableHttpResponse response = apiClient.execute(request.build())) {
            return toApiResponse(response);
        }
    }

    private String toJson(Object model) {

        return oAuth2RestClient.toJSONString(model);
    }

    private ApiResponse toApiResponse(CloseableHttpResponse response) throws Exception {

        String responseBody = response.getEntity() == null ? "" : EntityUtils.toString(response.getEntity());
        Header location = response.getFirstHeader("Location");
        return new ApiResponse(response.getStatusLine().getStatusCode(), responseBody,
                location == null ? null : location.getValue());
    }

    private void deleteQuietly(String url) {

        deleteQuietly(url, null);
    }

    private void deleteQuietly(String url, String bearerToken) {

        try {
            send("DELETE", url, null, bearerToken);
        } catch (Throwable e) {
            log.error("Failed to delete the resource: " + url, e);
        }
    }

    private String getIdFromLocation(ApiResponse response) {

        assertNotNull(response.location, "Location header is missing. " + response.body);
        return response.location.substring(response.location.lastIndexOf('/') + 1);
    }

    private String encode(String value) {

        return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
    }

    private static final class ApiResponse {

        private final int status;
        private final String body;
        private final String location;

        private ApiResponse(int status, String body, String location) {

            this.status = status;
            this.body = body;
            this.location = location;
        }
    }

    private static final class TestApplication {

        private final String id;
        private final String clientId;
        private final String clientSecret;

        private TestApplication(String id, String clientId, String clientSecret) {

            this.id = id;
            this.clientId = clientId;
            this.clientSecret = clientSecret;
        }
    }
}

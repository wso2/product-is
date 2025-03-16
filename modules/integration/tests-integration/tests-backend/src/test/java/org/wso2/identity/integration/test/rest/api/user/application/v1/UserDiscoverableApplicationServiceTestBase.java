/*
 * Copyright (c) 2019-2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.identity.integration.test.rest.api.user.application.v1;

import io.restassured.RestAssured;

import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.AccessTokenConfiguration;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.AdvancedApplicationConfiguration;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationPatchModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationSharePOSTRequest;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.DiscoverableGroup;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.GroupBasicInfo;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.OAuth2PKCEConfiguration;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.OpenIDConnectConfiguration;
import org.wso2.identity.integration.test.rest.api.user.common.RESTAPIUserTestBase;
import org.wso2.identity.integration.test.rest.api.user.common.model.GroupRequestObject;
import org.wso2.identity.integration.test.rest.api.user.common.model.UserObject;
import org.wso2.identity.integration.test.restclients.OAuth2RestClient;
import org.wso2.identity.integration.test.restclients.OrgMgtRestClient;
import org.wso2.identity.integration.test.restclients.SCIM2RestClient;

public class UserDiscoverableApplicationServiceTestBase extends RESTAPIUserTestBase {

    public static final String API_DEFINITION_NAME = "application.yaml";
    public static final String API_VERSION = "v1";
    public static final String USER_APPLICATION_ENDPOINT_URI = "/me/applications";
    protected static String swaggerDefinition;
    protected static String API_PACKAGE_NAME = "org.wso2.carbon.identity.rest.api.user.application.v1";
    private static final int TOTAL_DISCOVERABLE_APP_COUNT = 19;
    private static final int TOTAL_NON_DISCOVERABLE_APP_COUNT = 2;
    private static final int TOTAL_GROUP_COUNT = 3;
    private static final String[] GROUP_IDS = new String[TOTAL_GROUP_COUNT];
    private static final int TOTAL_USER_COUNT  = 3;
    private static final String[] USER_IDS = new String[TOTAL_USER_COUNT];
    protected static final String[] USER_TOKENS = new String[TOTAL_USER_COUNT];
    private static final int TOTAL_SUB_ORG_GROUP_COUNT = 3;
    private static final int TOTAL_SUB_ORG_USER_COUNT  = 3;
    private static final String[] SUB_ORG_USER_IDS = new String[TOTAL_SUB_ORG_USER_COUNT];
    private static final String[] SUB_ORG_GROUP_IDS = new String[TOTAL_SUB_ORG_GROUP_COUNT];
    protected static final String[] SUB_ORG_USER_TOKENS = new String[TOTAL_SUB_ORG_USER_COUNT];
    private static final String USER_NAME_PREFIX = "user-";
    private static final String USER_PASSWORD = "Wso2@test";
    private static final String GROUP_NAME_PREFIX = "GROUP_";
    protected static final String APP_NAME_PREFIX = "APP_";
    protected static final String APP_DESC_PREFIX = "This is APP_";
    protected static final String APP_IMAGE_URL = "https://dummy-image-url.com";
    protected static final String APP_ACCESS_URL = "https://dummy-access-url.com";
    private static final String SUB_ORG_NAME = "sub-org";
    private static final String PRIMARY_USER_STORE = "PRIMARY";
    private static final String MY_ACCOUNT_APP_NAME = "My Account";
    private static final String MY_ACCOUNT_APP_PATH = "myaccount";
    protected static final String[][] USER_DISCOVERABLE_APPS = new String[][]{
            { "19", "18", "17", "16", "15", "14", "13", "8", "7", "6", "5", "4", "3", "2", "1" },
            { "19", "18", "17", "16", "15", "14", "13", "12", "11", "10", "9", "8", "7", "6", "5", "4", "3"},
            { "19", "18", "17", "16", "15", "14", "13" }
    };
    protected static final String[] USER_NON_DISCOVERABLE_APPS = new String[]{ "21", "20" };
    protected static final String APP_NAME_WITH_SPACES = "APP_SPACES IN NAME ";
    protected static final int APP_NAME_WITH_SPACES_APP_NUM = 2;
    protected static final int APP_NAME_WITH_SPACES_APP_NUM_WITHOUT_GROUPS = 18;
    protected static final String[] DISCOVERABLE_APP_IDS = new String[TOTAL_DISCOVERABLE_APP_COUNT];
    protected static final String[] SUB_ORG_DISCOVERABLE_APP_IDS = new String[TOTAL_DISCOVERABLE_APP_COUNT];

    private String subOrgID;
    private String subOrgToken;
    private String rootMyAccountAppId;

    static {
        try {
            swaggerDefinition = getAPISwaggerDefinition(API_PACKAGE_NAME, API_DEFINITION_NAME);
        } catch (IOException e) {
            Assert.fail(String.format("Unable to read the swagger definition %s from %s", API_DEFINITION_NAME,
                    API_PACKAGE_NAME), e);
        }
    }

    protected OAuth2RestClient oAuth2RestClient;
    protected SCIM2RestClient scim2RestClient;
    protected OrgMgtRestClient orgMgtRestClient;

    @BeforeClass(alwaysRun = true)
    public void testStart() throws Exception {

        oAuth2RestClient = new OAuth2RestClient(serverURL, context.getContextTenant());
        scim2RestClient = new SCIM2RestClient(serverURL, context.getContextTenant());
        orgMgtRestClient =
                new OrgMgtRestClient(context, context.getContextTenant(), serverURL, getAuthorizedAPIList());
        subOrgID = orgMgtRestClient.addOrganization(SUB_ORG_NAME);
        subOrgToken = orgMgtRestClient.switchM2MToken(subOrgID);

        createUsers();
        createGroup();
        createSubOrgUsers();
        createSubOrgGroups();
        createApplications();
        changeMyAccountConfiguration();
        getTokenForUsers();
    }

    @AfterClass(alwaysRun = true)
    public void testEnd() throws Exception {

        super.conclude();
        deleteUsers();
        deleteGroups();
        deleteSubOrgUsers();
        deleteSubOrgGroups();
        deleteApplications();
        revertMyAccountConfiguration();
        orgMgtRestClient.deleteOrganization(subOrgID);
        oAuth2RestClient.deleteApplication(oAuth2RestClient.getAppIdUsingAppName("b2b-app"));
        oAuth2RestClient.closeHttpClient();
        scim2RestClient.closeHttpClient();
        orgMgtRestClient.closeHttpClient();
    }

    @BeforeMethod(alwaysRun = true)
    public void testMethodStart() {

        RestAssured.basePath = basePath;
    }

    @AfterMethod(alwaysRun = true)
    public void testMethodEnd() {

        RestAssured.basePath = StringUtils.EMPTY;
    }

    /**
     * Create root tenant users for the test.
     *
     * @throws Exception If an error occurred while creating users.
     */
    private void createUsers() throws Exception {

        for (int i = 1; i <= TOTAL_USER_COUNT; i++) {
            UserObject userObject = new UserObject();
            userObject.setUserName(USER_NAME_PREFIX + i);
            userObject.setPassword(USER_PASSWORD);
            USER_IDS[i - 1] = scim2RestClient.createUser(userObject);
        }
    }

    /**
     * Delete root tenant users created for the test.
     *
     * @throws Exception If an error occurred while deleting users.
     */
    private void deleteUsers() throws Exception {

        for (int i = 1; i <= TOTAL_USER_COUNT; i++) {
            scim2RestClient.deleteUser(USER_IDS[i - 1]);
        }
    }

    /**
     * Create root tenant groups for the test.
     *
     * @throws Exception If an error occurred while creating groups.
     */
    private void createGroup() throws Exception {

        for (int i = 1; i <= TOTAL_GROUP_COUNT; i++) {
            GroupRequestObject groupRequestObject = new GroupRequestObject();
            groupRequestObject.displayName(GROUP_NAME_PREFIX + i);
            assignMembersToGroup(groupRequestObject, i, USER_IDS);
            GROUP_IDS[i - 1] = scim2RestClient.createGroup(groupRequestObject);
        }
    }

    /**
     * Delete root tenant groups created for the test.
     *
     * @throws Exception If an error occurred while deleting groups.
     */
    private void deleteGroups() throws Exception {

        for (int i = 1; i <= TOTAL_GROUP_COUNT; i++) {
            scim2RestClient.deleteGroup(GROUP_IDS[i - 1]);
        }
    }

    /**
     * Create sub organization users for the test.
     *
     * @throws Exception If an error occurred while creating users.
     */
    private void createSubOrgUsers() throws Exception {

        for (int i = 1; i <= TOTAL_SUB_ORG_USER_COUNT; i++) {
            UserObject userObject = new UserObject();
            userObject.setUserName(USER_NAME_PREFIX + i);
            userObject.setPassword(USER_PASSWORD);
            SUB_ORG_USER_IDS[i - 1] = scim2RestClient.createSubOrgUser(userObject, subOrgToken);
        }
    }

    /**
     * Delete sub organization users created for the test.
     *
     * @throws Exception If an error occurred while deleting users.
     */
    private void deleteSubOrgUsers() throws Exception {

        for (int i = 1; i <= TOTAL_SUB_ORG_USER_COUNT; i++) {
            scim2RestClient.deleteSubOrgUser(SUB_ORG_USER_IDS[i - 1], subOrgToken);
        }
    }

    /**
     * Create sub organization groups for the test.
     *
     * @throws Exception If an error occurred while creating groups.
     */
    private void createSubOrgGroups() throws Exception {

        for (int i = 1; i <= TOTAL_SUB_ORG_GROUP_COUNT; i++) {
            GroupRequestObject groupRequestObject = new GroupRequestObject();
            groupRequestObject.displayName(GROUP_NAME_PREFIX + i);
            assignMembersToGroup(groupRequestObject, i, SUB_ORG_USER_IDS);
            SUB_ORG_GROUP_IDS[i - 1] = scim2RestClient.createSubOrgGroup(groupRequestObject, subOrgToken);
        }
    }

    /**
     * Delete sub organization groups created for the test.
     *
     * @throws Exception If an error occurred while deleting groups.
     */
    private void deleteSubOrgGroups() throws Exception {

        for (int i = 1; i <= TOTAL_SUB_ORG_GROUP_COUNT; i++) {
            scim2RestClient.deleteSubOrgGroup(SUB_ORG_GROUP_IDS[i - 1], subOrgToken);
        }
    }

    /**
     * Assign members to the group.
     * The first user will be assigned to the first group.
     * The second user will be assigned to the second and third groups.
     * The third user will not be assigned to any group.
     *
     * @param groupRequestObject Group request object.
     * @param groupNum           Group number.
     * @param userIDs            User IDs.
     */
    private void assignMembersToGroup(GroupRequestObject groupRequestObject, int groupNum, String[] userIDs) {

        GroupRequestObject.MemberItem member = new GroupRequestObject.MemberItem();
        if (groupNum == 1) {
            member.value(userIDs[0]);
            groupRequestObject.addMember(member);
        } else if (groupNum == 2) {
            member.value(userIDs[1]);
            groupRequestObject.addMember(member);
        } else if (groupNum == 3) {
            member.value(userIDs[1]);
            groupRequestObject.addMember(member);
        }
    }

    /**
     * Get the list of sub APIs that need to be authorized for the B2B application.
     *
     * @return A JSON object containing the API and scopes list.
     * @throws JSONException If an error occurs while creating the JSON object.
     */
    private JSONObject getAuthorizedAPIList() throws JSONException {

        JSONObject jsonObject = new JSONObject();
        // SCIM2 Users.
        jsonObject.put("/o/scim2/Users",
                new String[] {"internal_org_user_mgt_create", "internal_org_user_mgt_delete"});
        // SCIM2 Groups.
        jsonObject.put("/o/scim2/Groups",
                new String[] {"internal_org_group_mgt_create", "internal_org_group_mgt_delete"});
        // Application management.
        jsonObject.put("/o/api/server/v1/applications",
                new String[] {"internal_org_application_mgt_view", "internal_org_application_mgt_create",
                        "internal_org_application_mgt_update"});
        jsonObject.put("/api/server/v1/applications",
                new String[] {"internal_application_mgt_view", "internal_application_mgt_delete"});
        // Organization management.
        jsonObject.put("/api/server/v1/organizations",
                new String[] {"internal_organization_create", "internal_organization_delete"});

        return jsonObject;
    }

    /**
     * Create applications for the test.
     *
     * @throws Exception If an error occurred while creating applications.
     */
    private void createApplications() throws Exception {

        for (int i = 1; i <= TOTAL_DISCOVERABLE_APP_COUNT; i++) {
            ApplicationModel application = new ApplicationModel();
            application.setName(getApplicationName(String.valueOf(i)));
            application.setDescription(APP_DESC_PREFIX + i);
            application.setImageUrl(APP_IMAGE_URL);
            AdvancedApplicationConfiguration advancedApplicationConfiguration = new AdvancedApplicationConfiguration();
            advancedApplicationConfiguration.setDiscoverableByEndUsers(true);
            assignDiscoverableGroups(advancedApplicationConfiguration, i, GROUP_IDS);
            application.setAdvancedConfigurations(advancedApplicationConfiguration);
            application.setAccessUrl(APP_ACCESS_URL);
            String appId = oAuth2RestClient.createApplication(application);
            DISCOVERABLE_APP_IDS[i - 1] = appId;
            oAuth2RestClient.shareApplication(appId, new ApplicationSharePOSTRequest().shareWithAllChildren(true));
            String sharedAppId = null;
            do {
                if (sharedAppId != null) {
                    Thread.sleep(1000);
                }
                sharedAppId = oAuth2RestClient.getAppIdUsingAppNameInOrganization(getApplicationName(
                        String.valueOf(i)), subOrgToken);
            } while (StringUtils.isEmpty(sharedAppId));
            SUB_ORG_DISCOVERABLE_APP_IDS[i - 1] = sharedAppId;
            ApplicationPatchModel sharedAppPatch = new ApplicationPatchModel();
            AdvancedApplicationConfiguration sharedAppAdvancedConfig = new AdvancedApplicationConfiguration();
            assignDiscoverableGroups(sharedAppAdvancedConfig, i, SUB_ORG_GROUP_IDS);
            sharedAppPatch.advancedConfigurations(sharedAppAdvancedConfig);
            oAuth2RestClient.updateSubOrgApplication(sharedAppId, sharedAppPatch, subOrgToken);
        }
        for (int i = 1; i <= TOTAL_NON_DISCOVERABLE_APP_COUNT; i++) {
            ApplicationModel application = new ApplicationModel();
            application.setName(getApplicationName(String.valueOf(i + TOTAL_DISCOVERABLE_APP_COUNT)));
            application.setDescription(APP_DESC_PREFIX + (i + TOTAL_DISCOVERABLE_APP_COUNT));
            application.setImageUrl(APP_IMAGE_URL);
            oAuth2RestClient.createApplication(application);
        }
    }

    /**
     * Delete applications created for the test.
     *
     * @throws Exception If an error occurred while deleting applications.
     */
    private void deleteApplications() throws Exception {

        for (int i = 1; i <= TOTAL_DISCOVERABLE_APP_COUNT; i++) {
            oAuth2RestClient.deleteApplication(DISCOVERABLE_APP_IDS[i - 1]);
        }
        for (int i = 1; i <= TOTAL_NON_DISCOVERABLE_APP_COUNT; i++) {
            oAuth2RestClient.deleteApplication(oAuth2RestClient.getAppIdUsingAppName(getApplicationName(
                    String.valueOf(i + TOTAL_DISCOVERABLE_APP_COUNT))));
        }
    }

    /**
     * Assign discoverable groups to the application.
     *
     * @param advancedApplicationConfiguration Advanced application configuration.
     * @param applicationNum                   Application number.
     * @param groupIDs                         Group IDs.
     */
    private void assignDiscoverableGroups(AdvancedApplicationConfiguration advancedApplicationConfiguration,
                                          int applicationNum, String[] groupIDs) {

        DiscoverableGroup discoverableGroup = new DiscoverableGroup();
        discoverableGroup.setUserStore(PRIMARY_USER_STORE);
        if (applicationNum >= 1 && applicationNum <= 8) {
            GroupBasicInfo group = new GroupBasicInfo();
            group.setId(groupIDs[0]);
            discoverableGroup.addGroupsItem(group);
        }
        if (applicationNum >= 3 && applicationNum <= 10) {
            GroupBasicInfo group = new GroupBasicInfo();
            group.setId(groupIDs[1]);
            discoverableGroup.addGroupsItem(group);
        }
        if (applicationNum >= 5 && applicationNum <= 12) {
            GroupBasicInfo group = new GroupBasicInfo();
            group.setId(groupIDs[2]);
            discoverableGroup.addGroupsItem(group);
        }
        if (applicationNum < 13) {
            advancedApplicationConfiguration.addDiscoverableGroupsItem(discoverableGroup);
        }
    }

    /**
     * Make My Account application a confidential client.
     *
     * @throws Exception If an error occurred while making the application a confidential client.
     */
    private void changeMyAccountConfiguration() throws Exception {

        rootMyAccountAppId = oAuth2RestClient.getAppIdUsingAppName(MY_ACCOUNT_APP_NAME);
        OpenIDConnectConfiguration rootMyAccountAppOIDC = oAuth2RestClient.getOIDCInboundDetails(rootMyAccountAppId);
        OAuth2PKCEConfiguration oAuth2PKCEConfiguration = rootMyAccountAppOIDC.getPkce();
        oAuth2PKCEConfiguration.setMandatory(false);
        rootMyAccountAppOIDC.setPublicClient(false);
        AccessTokenConfiguration accessTokenConfiguration = rootMyAccountAppOIDC.getAccessToken();
        accessTokenConfiguration.setBindingType(null);
        accessTokenConfiguration.setValidateTokenBinding(false);
        oAuth2RestClient.updateInboundDetailsOfApplication(rootMyAccountAppId, rootMyAccountAppOIDC, "oidc");
        oAuth2RestClient.shareApplication(
                rootMyAccountAppId, new ApplicationSharePOSTRequest().shareWithAllChildren(true));
    }

    /**
     * Revert the configuration of My Account application.
     *
     * @throws Exception If an error occurred while reverting the configuration.
     */
    private void revertMyAccountConfiguration() throws Exception {

        OpenIDConnectConfiguration rootMyAccountAppOIDC = oAuth2RestClient.getOIDCInboundDetails(rootMyAccountAppId);
        OAuth2PKCEConfiguration oAuth2PKCEConfiguration = rootMyAccountAppOIDC.getPkce();
        oAuth2PKCEConfiguration.setMandatory(true);
        rootMyAccountAppOIDC.setPublicClient(true);
        AccessTokenConfiguration accessTokenConfiguration = rootMyAccountAppOIDC.getAccessToken();
        accessTokenConfiguration.setBindingType("cookie");
        accessTokenConfiguration.setValidateTokenBinding(true);
        oAuth2RestClient.updateInboundDetailsOfApplication(rootMyAccountAppId, rootMyAccountAppOIDC, "oidc");
        oAuth2RestClient.unshareApplication(rootMyAccountAppId);
    }

    /**
     * Get the access tokens for the users.
     *
     * @throws Exception If an error occurred while getting the access tokens.
     */
    private void getTokenForUsers() throws Exception {

        for (int i = 1; i <= TOTAL_USER_COUNT; i++) {
            USER_TOKENS[i - 1] =
                    oAuth2RestClient.getAccessTokenUsingCodeGrantForRootUser(rootMyAccountAppId, USER_NAME_PREFIX + i,
                            USER_PASSWORD, "SYSTEM", getMyAccountRedirectUrl());
        }
        for (int i = 1; i <= TOTAL_SUB_ORG_USER_COUNT; i++) {
            SUB_ORG_USER_TOKENS[i - 1] =
                    oAuth2RestClient.getAccessTokenUsingCodeGrantForSubOrgUser(rootMyAccountAppId, SUB_ORG_NAME,
                            subOrgID, USER_NAME_PREFIX + i, USER_PASSWORD, "SYSTEM", getMyAccountRedirectUrl());
        }
    }

    /**
     * Get my account redirect URL.
     *
     * @return My account redirect URL.
     */
    private String getMyAccountRedirectUrl() {

        if (StringUtils.equals(tenantInfo.getDomain(), MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
            return serverURL + MY_ACCOUNT_APP_PATH;
        } else {
            return serverURL + TENANTED_URL_PATH_SPECIFIER.replace(URL_SEPARATOR, StringUtils.EMPTY) + URL_SEPARATOR +
                    tenantInfo.getDomain() + URL_SEPARATOR + MY_ACCOUNT_APP_PATH;
        }
    }

    /**
     * Get the expected application name based on name prefix and application number.
     *
     * @param applicationNum Application number.
     * @return Expected application name.
     */
    protected String getApplicationName(String applicationNum) {

        if (Integer.parseInt(applicationNum) == APP_NAME_WITH_SPACES_APP_NUM ||
                Integer.parseInt(applicationNum) == APP_NAME_WITH_SPACES_APP_NUM_WITHOUT_GROUPS) {
            return APP_NAME_WITH_SPACES + applicationNum;
        } else {
            return APP_NAME_PREFIX + applicationNum;
        }
    }
}

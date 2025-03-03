/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v1;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.restassured.RestAssured;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationPatchModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationResponseModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationSharePOSTRequest;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.AssociatedRolesConfig;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ClaimConfiguration;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ClaimMappings;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.InboundProtocols;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.OpenIDConnectConfiguration;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.RequestedClaimConfiguration;
import org.wso2.identity.integration.test.rest.api.server.common.RESTAPIServerTestBase;
import org.wso2.identity.integration.test.rest.api.server.roles.v2.model.Audience;
import org.wso2.identity.integration.test.rest.api.server.roles.v2.model.RoleV2;
import org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v1.model.RoleWithAudience;
import org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v1.model.RoleWithAudienceAudience;
import org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v1.model.UserShareRequestBodyOrganizations;
import org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v1.model.UserShareRequestBodyUserCriteria;
import org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v1.model.UserShareWithAllRequestBody;
import org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v1.model.UserUnshareRequestBodyUserCriteria;
import org.wso2.identity.integration.test.rest.api.user.common.model.Email;
import org.wso2.identity.integration.test.rest.api.user.common.model.Name;
import org.wso2.identity.integration.test.rest.api.user.common.model.UserObject;
import org.wso2.identity.integration.test.restclients.OAuth2RestClient;
import org.wso2.identity.integration.test.restclients.OrgMgtRestClient;
import org.wso2.identity.integration.test.restclients.SCIM2RestClient;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.wso2.identity.integration.test.restclients.RestBaseClient.USER_AGENT_ATTRIBUTE;

/**
 * Base test class for the User Sharing REST APIs.
 */
public class UserSharingBaseTest extends RESTAPIServerTestBase {

    protected static String swaggerDefinition;

    protected OAuth2RestClient oAuth2RestClient;
    protected SCIM2RestClient scim2RestClient;
    protected OrgMgtRestClient orgMgtRestClient;

    protected Map<String, Map<String, Object>> userDetails;
    protected Map<String, Map<String, Object>> orgDetails;
    protected Map<String, Map<String, Object>> appDetails;
    protected Map<String, Map<String, Object>> roleDetails;

    private static final String API_DEFINITION_NAME = "organization-user-share.yaml";
    protected static final String AUTHORIZED_APIS_JSON = "user-sharing-apis.json";
    static final String API_VERSION = "v1";
    private static final String API_PACKAGE_NAME = "org.wso2.carbon.identity.api.server.organization.user.sharing.management.v1";

    static final String USER_SHARING_API_BASE_PATH = "/users";
    static final String SHARE_PATH = "/share";
    static final String SHARE_WITH_ALL_PATH = "/share-with-all";
    static final String UNSHARE_PATH = "/unshare";
    static final String UNSHARE_WITH_ALL_PATH = "/unshare-with-all";
    static final String SHARED_ORGANIZATIONS_PATH = "/shared-organizations";
    static final String SHARED_ROLES_PATH = "/shared-roles";

    protected static final String PATH_PARAM_USER_ID = "userId";
    protected static final String QUERY_PARAM_ORG_ID = "orgId";
    protected static final String QUERY_PARAM_LIMIT = "limit";
    protected static final String QUERY_PARAM_AFTER = "after";
    protected static final String QUERY_PARAM_BEFORE = "before";
    protected static final String QUERY_PARAM_FILTER = "filter";
    protected static final String QUERY_PARAM_RECURSIVE = "recursive";

    protected static final String ERROR_CODE_BAD_REQUEST = "UE-10000";
    protected static final String ERROR_CODE_INVALID_PAGINATION_CURSOR = "ORG-60026";
    protected static final String ERROR_CODE_SERVER_ERROR = "SE-50000";

    protected static final String ROOT_ORG_NAME = "Super";
    protected static final String L1_ORG_1_NAME = "L1 - Organization 1";
    protected static final String L1_ORG_2_NAME = "L1 - Organization 2";
    protected static final String L1_ORG_3_NAME = "L1 - Organization 3";
    protected static final String L2_ORG_1_NAME = "L2 - Organization 1";
    protected static final String L2_ORG_2_NAME = "L2 - Organization 2";
    protected static final String L2_ORG_3_NAME = "L2 - Organization 3";
    protected static final String L3_ORG_1_NAME = "L3 - Organization 1";

    protected static final String ROOT_ORG_ID = "10084a8d-113f-4211-a0d5-efe36b082211";

    protected static final String APP_1_NAME = "App 1";
    protected static final String APP_2_NAME = "App 2";

    protected static final String APPLICATION_AUDIENCE = "application";
    protected static final String ORGANIZATION_AUDIENCE = "organization";

    protected static final String APP_ROLE_1 = "app-role-1";
    protected static final String APP_ROLE_2 = "app-role-2";
    protected static final String APP_ROLE_3 = "app-role-3";
    protected static final String ORG_ROLE_1 = "org-role-1";
    protected static final String ORG_ROLE_2 = "org-role-2";
    protected static final String ORG_ROLE_3 = "org-role-3";

    protected static final String USER_DOMAIN_PRIMARY = "PRIMARY";

    protected static final String ROOT_ORG_USER_1_USERNAME = "rootUser1";
    protected static final String ROOT_ORG_USER_2_USERNAME = "rootUser2";
    protected static final String ROOT_ORG_USER_3_USERNAME = "rootUser3";
    protected static final String L1_ORG_1_USER_1_USERNAME = "l1Org1User1";
    protected static final String L1_ORG_1_USER_2_USERNAME = "l1Org1User2";
    protected static final String L1_ORG_1_USER_3_USERNAME = "l1Org1User3";

    protected static final String API_SCOPE_INTERNAL_USER_SHARE = "internal_user_share";
    protected static final String API_SCOPE_INTERNAL_USER_UNSHARE = "internal_user_unshare";
    protected static final String API_SCOPE_INTERNAL_USER_SHARED_ACCESS_VIEW = "internal_user_shared_access_view";
    protected static final String API_SCOPE_INTERNAL_ORG_USER_SHARE = "internal_org_user_share";
    protected static final String API_SCOPE_INTERNAL_ORG_USER_UNSHARE = "internal_org_user_unshare";
    protected static final String API_SCOPE_INTERNAL_ORG_USER_SHARED_ACCESS_VIEW = "internal_org_user_shared_access_view";

    protected static final String EMAIL_CLAIM_URI = "http://wso2.org/claims/emailaddress";
    protected static final String COUNTRY_CLAIM_URI = "http://wso2.org/claims/country";
    protected static final String ROLES_CLAIM_URI = "http://wso2.org/claims/roles";
    protected static final String GROUPS_CLAIM_URI = "http://wso2.org/claims/groups";

    protected static final String MAP_KEY_SELECTIVE_ORG_ID = "orgId";
    protected static final String MAP_KEY_SELECTIVE_ORG_NAME = "orgName";
    protected static final String MAP_KEY_SELECTIVE_POLICY = "selectivePolicy";
    protected static final String MAP_KEY_SELECTIVE_ROLES = "selectiveRoles";

    protected static final String MAP_KEY_GENERAL_POLICY = "generalPolicy";
    protected static final String MAP_KEY_GENERAL_ROLES = "generalRoles";

    protected static final String MAP_KEY_EXPECTED_ORG_COUNT = "expectedOrgCount";
    protected static final String MAP_KEY_EXPECTED_ORG_IDS = "expectedOrgIds";
    protected static final String MAP_KEY_EXPECTED_ORG_NAMES = "expectedOrgNames";
    protected static final String MAP_KEY_EXPECTED_ROLES_PER_EXPECTED_ORG = "expectedRolesPerExpectedOrg";

    static {
        try {
            swaggerDefinition = getAPISwaggerDefinition(API_PACKAGE_NAME, API_DEFINITION_NAME);
        } catch (IOException e) {
            Assert.fail(String.format("Unable to read the swagger definition %s from %s", API_DEFINITION_NAME, API_PACKAGE_NAME), e);
        }
    }

    @AfterClass(alwaysRun = true)
    public void testConclude() throws Exception {

        super.conclude();
    }

    @BeforeMethod(alwaysRun = true)
    public void testInit() {

        RestAssured.basePath = basePath;
    }

    @AfterMethod(alwaysRun = true)
    public void testFinish() {

        RestAssured.basePath = StringUtils.EMPTY;
    }

    // Request Sending Methods.

    protected HttpResponse sendGetRequest(String endpointURL, HttpClient client) throws IOException {

        HttpGet request = new HttpGet(endpointURL);
        request.setHeader(USER_AGENT_ATTRIBUTE, OAuth2Constant.USER_AGENT);
        return client.execute(request);
    }

    protected HttpResponse sendPostRequest(String endpointURL, List<NameValuePair> urlParameters, HttpClient client) throws IOException {

        HttpPost request = new HttpPost(endpointURL);
        request.setHeader(USER_AGENT_ATTRIBUTE, OAuth2Constant.USER_AGENT);
        request.setEntity(new UrlEncodedFormEntity(urlParameters));
        return client.execute(request);
    }

    // Methods to add organizations and sub organizations for testing purposes.

    protected String addOrganization(String orgName) throws Exception {

        String orgId = orgMgtRestClient.addOrganization(orgName);
        setOrgDetails(orgName, orgId, ROOT_ORG_ID, 1);
        return orgId;
    }

    protected String addSubOrganization(String orgName, String parentId, int orgLevel) throws Exception {

        String orgId = orgMgtRestClient.addSubOrganization(orgName, parentId);
        setOrgDetails(orgName, orgId, parentId, orgLevel);
        return orgId;
    }

    protected String getOrgId(String orgName) {

        return orgDetails.get(orgName).get("orgId").toString();
    }

    protected void setOrgDetails(String orgName, String orgId, String parentId, int orgLevel) throws Exception {

        Map<String, Object> orgDetail = new HashMap<>();
        orgDetail.put("orgName", orgName);
        orgDetail.put("orgId", orgId);
        orgDetail.put("parentOrgId", parentId);
        orgDetail.put("orgSwitchToken", orgMgtRestClient.switchM2MToken(orgId));
        orgDetail.put("orgLevel", orgLevel);
        orgDetails.put(orgName, orgDetail);
    }

    // Methods to add applications and roles for testing purposes.

    protected Map<String, Object> createApplication(String appName, String audience, List<String> roleNames) throws Exception{

        Map<String, Object> createdAppDetails = new HashMap<>();
        String rootOrgAppName = appName + "/" + ROOT_ORG_NAME;

        ApplicationResponseModel application = addApplication(appName);
        String appId = application.getId();
        OpenIDConnectConfiguration oidcConfig = oAuth2RestClient.getOIDCInboundDetails(appId);
        String clientId = oidcConfig.getClientId();
        String clientSecret = oidcConfig.getClientSecret();
        Map<String, String> roleIdsByName = new HashMap<>();

        if (StringUtils.equalsIgnoreCase(APPLICATION_AUDIENCE, audience)){

            Audience appRoleAudience = new Audience(APPLICATION_AUDIENCE, appId);
            for (String roleName : roleNames) {
                RoleV2 appRole = new RoleV2(appRoleAudience, roleName, Collections.emptyList(), Collections.emptyList());
                String roleId = scim2RestClient.addV2Role(appRole);
                roleIdsByName.put(roleName, roleId);
            }
            storeRoleDetails(APPLICATION_AUDIENCE, rootOrgAppName, roleIdsByName);
            createdAppDetails.put("appAudience", APPLICATION_AUDIENCE);

        } else {

            switchApplicationAudience(appId, AssociatedRolesConfig.AllowedAudienceEnum.ORGANIZATION);

            for (String roleName: roleNames){
                String roleId = scim2RestClient.getRoleIdByName(roleName);
                roleIdsByName.put(roleName, roleId);
            }
            createdAppDetails.put("appAudience", ORGANIZATION_AUDIENCE);
        }

        // Mark roles and groups as requested claims for the app 2.
        updateRequestedClaimsOfApp(appId, getClaimConfigurationsWithRolesAndGroups());
        shareApplication(appId);

        // Get sub org details of Applications.
        Map<String, Object> appDetailsOfSubOrgs = new HashMap<>();
        for (Map.Entry<String, Map<String, Object>> entry : orgDetails.entrySet()) {
            String orgName = entry.getKey();
            Map<String, Object> orgDetail = entry.getValue();

            Map<String, Object> appDetailsOfSubOrg = getAppDetailsOfSubOrg(appName, audience, roleNames, orgDetail);
            appDetailsOfSubOrgs.put(orgName, appDetailsOfSubOrg);
        }

        createdAppDetails.put("appName", appName);
        createdAppDetails.put("appId", appId);
        createdAppDetails.put("clientId", clientId);
        createdAppDetails.put("clientSecret", clientSecret);
        createdAppDetails.put("roleNames", roleNames);
        createdAppDetails.put("roleIdsByName", roleIdsByName);
        createdAppDetails.put("appDetailsOfSubOrgs", appDetailsOfSubOrgs);

        appDetails.put(appName, createdAppDetails);
        return createdAppDetails;
    }

    protected Map<String, Object> getAppDetailsOfSubOrg(String appName, String audience, List<String> roleNames, Map<String, Object> orgDetail) throws Exception {

        Map<String, Object> subOrgAppDetails = new HashMap<>();

        String subOrgName = (String) orgDetail.get("orgName");
        String subOrgId = (String) orgDetail.get("orgId");
        String subOrgSwitchToken = (String) orgDetail.get("orgSwitchToken");
        String subOrgAppName = appName + "/" + subOrgName;

        String subOrgAppId = oAuth2RestClient.getAppIdUsingAppNameInOrganization(appName, subOrgSwitchToken);

        Map<String, String> subOrgRoleIdsByName = StringUtils.equalsIgnoreCase(APPLICATION_AUDIENCE, audience) ?
                getSubOrgRoleIdsByName(roleNames, APPLICATION_AUDIENCE, subOrgAppName, subOrgAppId, subOrgSwitchToken) :
                getSubOrgRoleIdsByName(roleNames,ORGANIZATION_AUDIENCE, subOrgName, subOrgId, subOrgSwitchToken);

        subOrgAppDetails.put("subOrgName", subOrgName);
        subOrgAppDetails.put("appName", appName);
        subOrgAppDetails.put("appId", subOrgAppId);
        subOrgAppDetails.put("roleNames", roleNames);
        subOrgAppDetails.put("roleIdsByName", subOrgRoleIdsByName);
        subOrgAppDetails.put("appAudience", audience);

        return subOrgAppDetails;
    }

    protected Map<String, String> getSubOrgRoleIdsByName(List<String> roleNames, String audienceType, String audienceName, String audienceValue, String subOrgSwitchToken) throws Exception {

        Map<String, String> roleIdsByName = new HashMap<>();
        for (String roleName : roleNames) {
            String sharedAppRoleId =
                    scim2RestClient.getRoleIdByNameAndAudienceInSubOrg(roleName, audienceValue, subOrgSwitchToken);
            roleIdsByName.put(roleName, sharedAppRoleId);
        }

        if (StringUtils.equalsIgnoreCase(APPLICATION_AUDIENCE, audienceType)) {
            storeRoleDetails(APPLICATION_AUDIENCE, audienceName, roleIdsByName);
        } else {
            storeRoleDetails(ORGANIZATION_AUDIENCE, audienceName, roleIdsByName);
        }

        return roleIdsByName;
    }

    protected Map<String, String> createOrganizationRoles(String orgName, List<String> orgRoleNames) throws IOException {

        Map<String, String> orgRoleIdsByName = new HashMap<>();
        for (String orgRoleName : orgRoleNames) {
            RoleV2 orgRole = new RoleV2(null, orgRoleName, Collections.emptyList(), Collections.emptyList());
            String orgRoleId = scim2RestClient.addV2Role(orgRole);
            orgRoleIdsByName.put(orgRoleName, orgRoleId);
        }

        storeRoleDetails(ORGANIZATION_AUDIENCE, orgName, orgRoleIdsByName);

        return orgRoleIdsByName;
    }

    protected RoleWithAudience createRoleWithAudience(String roleName, String display, String type) {

        RoleWithAudienceAudience audience = new RoleWithAudienceAudience();
        audience.setDisplay(display);
        audience.setType(type);

        RoleWithAudience roleWithAudience = new RoleWithAudience();
        roleWithAudience.setDisplayName(roleName);
        roleWithAudience.setAudience(audience);

        return roleWithAudience;
    }

    protected String getSharedOrgsRolesRef(String userId, String orgId) {

        return "/api/server/v1" + USER_SHARING_API_BASE_PATH + "/" + userId + SHARED_ROLES_PATH + "?orgId=" + orgId;
    }

    protected void storeRoleDetails(String audienceType, String audienceName, Map<String, String> rolesOfAudience) {

        String key = StringUtils.equalsIgnoreCase(APPLICATION_AUDIENCE, audienceType)
                ? APPLICATION_AUDIENCE
                : ORGANIZATION_AUDIENCE;

        Map<String, Object> rolesMapOfAudienceType = new HashMap<>();
        rolesMapOfAudienceType.put(audienceName, rolesOfAudience);

        roleDetails.computeIfAbsent(key, k -> new HashMap<>()).putAll(rolesMapOfAudienceType);
    }

    private ApplicationResponseModel addApplication(String appName) throws Exception {

        ApplicationModel application = new ApplicationModel();

        List<String> grantTypes = new ArrayList<>();
        Collections.addAll(grantTypes, "authorization_code", "implicit", "password", "client_credentials", "refresh_token", "organization_switch");

        List<String> callBackUrls = new ArrayList<>();
        Collections.addAll(callBackUrls, OAuth2Constant.CALLBACK_URL);

        OpenIDConnectConfiguration oidcConfig = new OpenIDConnectConfiguration();
        oidcConfig.setGrantTypes(grantTypes);
        oidcConfig.setCallbackURLs(callBackUrls);

        InboundProtocols inboundProtocolsConfig = new InboundProtocols();
        inboundProtocolsConfig.setOidc(oidcConfig);

        application.setInboundProtocolConfiguration(inboundProtocolsConfig);
        application.setName(appName);
        application.setIsManagementApp(true);

        application.setClaimConfiguration(setApplicationClaimConfig());
        String appId = oAuth2RestClient.createApplication(application);

        return oAuth2RestClient.getApplication(appId);
    }

    private ClaimConfiguration setApplicationClaimConfig() {

        ClaimMappings emailClaim = new ClaimMappings().applicationClaim(EMAIL_CLAIM_URI);
        emailClaim.setLocalClaim(new org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.Claim().uri(EMAIL_CLAIM_URI));
        ClaimMappings countryClaim = new ClaimMappings().applicationClaim(COUNTRY_CLAIM_URI);
        countryClaim.setLocalClaim(new org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.Claim().uri(COUNTRY_CLAIM_URI));

        RequestedClaimConfiguration emailRequestedClaim = new RequestedClaimConfiguration();
        emailRequestedClaim.setClaim(new org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.Claim().uri(EMAIL_CLAIM_URI));
        RequestedClaimConfiguration countryRequestedClaim = new RequestedClaimConfiguration();
        countryRequestedClaim.setClaim(new org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.Claim().uri(COUNTRY_CLAIM_URI));

        ClaimConfiguration claimConfiguration = new ClaimConfiguration().dialect(ClaimConfiguration.DialectEnum.CUSTOM);
        claimConfiguration.addClaimMappingsItem(emailClaim);
        claimConfiguration.addClaimMappingsItem(countryClaim);
        claimConfiguration.addRequestedClaimsItem(emailRequestedClaim);
        claimConfiguration.addRequestedClaimsItem(countryRequestedClaim);

        return claimConfiguration;
    }

    private ClaimConfiguration getClaimConfigurationsWithRolesAndGroups() {

        ClaimConfiguration claimConfiguration = new ClaimConfiguration();
        claimConfiguration.addRequestedClaimsItem(getRequestedClaim(ROLES_CLAIM_URI));
        claimConfiguration.addRequestedClaimsItem(getRequestedClaim(GROUPS_CLAIM_URI));
        return claimConfiguration;
    }

    private RequestedClaimConfiguration getRequestedClaim(String claimUri) {

        RequestedClaimConfiguration requestedClaim = new RequestedClaimConfiguration();
        requestedClaim.setClaim(new org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.Claim().uri(claimUri));
        return requestedClaim;
    }

    private void updateRequestedClaimsOfApp(String applicationId, ClaimConfiguration claimConfigurationsForApp) throws IOException {

        ApplicationPatchModel applicationPatch = new ApplicationPatchModel();
        applicationPatch.setClaimConfiguration(claimConfigurationsForApp);
        oAuth2RestClient.updateApplication(applicationId, applicationPatch);
    }

    private void switchApplicationAudience(String appId, AssociatedRolesConfig.AllowedAudienceEnum newAudience) throws Exception {

        AssociatedRolesConfig associatedRolesConfigApp2 = new AssociatedRolesConfig();
        associatedRolesConfigApp2.setAllowedAudience(newAudience);

        ApplicationPatchModel patchModelApp2 = new ApplicationPatchModel();
        patchModelApp2.setAssociatedRoles(associatedRolesConfigApp2);

        oAuth2RestClient.updateApplication(appId, patchModelApp2);
    }

    private void shareApplication(String applicationId) throws Exception {

        ApplicationSharePOSTRequest applicationSharePOSTRequest = new ApplicationSharePOSTRequest();
        applicationSharePOSTRequest.setShareWithAllChildren(true);
        oAuth2RestClient.shareApplication(applicationId, applicationSharePOSTRequest);

        // Since application sharing is an async operation, wait for some time for it to finish.
        Thread.sleep(5000);
    }

    // Methods to add users in organizations and sub organizations for testing purposes.

    protected UserObject createUserObject(String userDomain, String userName, String orgName) {

        String domainQualifiedUserName = userDomain + "/" + userName;
        UserObject user = new UserObject()
                .userName(domainQualifiedUserName)
                .password("Admin123")
                .name(new Name().givenName(userName).familyName(orgName))
                .emails(new ArrayList<>());

        Email email = new Email();
        email.setValue(userName + "@gmail.com");
        email.setPrimary(true);
        user.getEmails().add(email);

        List<String> schemas = new ArrayList<>();
        schemas.add("urn:ietf:params:scim:schemas:core:2.0:User");
        user.setSchemas(schemas);

        return user;
    }

    protected String createUser(UserObject user) throws Exception{

        String userId = scim2RestClient.createUser(user);

        Map<String, Object> userDetail = new HashMap<>();
        userDetail.put("username", user.getUserName());
        userDetail.put("userId", userId);
        userDetail.put("isRootOrgUser", true);
        userDetail.put("orgName", ROOT_ORG_NAME);
        userDetail.put("orgId", ROOT_ORG_ID);
        userDetail.put("orgLevel", 0);

        userDetails.put(user.getUserName(), userDetail);
        return  userId;
    }

    protected String createSuborgUser(UserObject user, String suborg) throws Exception{

        String userId = scim2RestClient.createSubOrgUser(user, (String) orgDetails.get(suborg).get("orgSwitchToken"));

        Map<String, Object> userDetail = new HashMap<>();
        userDetail.put("username", user.getUserName());
        userDetail.put("userId", userId);
        userDetail.put("isRootOrgUser", false);
        userDetail.put("orgName", suborg);
        userDetail.put("orgId", orgDetails.get(suborg).get("orgId"));
        userDetail.put("orgLevel", orgDetails.get(suborg).get("orgLevel"));

        userDetails.put(user.getUserName(), userDetail);
        return  userId;
    }

    protected String getUserId(String userName, String userDomain) {

        String domainQualifiedUserName = userDomain + "/" + userName;
        return userDetails.get(domainQualifiedUserName).get("userId").toString();
    }

    // Methods to clean up the resources created for testing purposes.

    /**
     * Clean up users by deleting them if they exist.
     *
     * @throws Exception If an error occurs while deleting the users.
     */
    protected void cleanUpUsers() throws Exception {

        for (Map.Entry<String, Map<String, Object>> entry : userDetails.entrySet()) {
            String userId = (String) entry.getValue().get("userId");
            String orgName = (String) entry.getValue().get("orgName");
            int orgLevel = (int) entry.getValue().get("orgLevel");

            if(orgLevel==0) {
                deleteUserIfExists(userId);
            } else {
                deleteSubOrgUserIfExists(userId, (String) orgDetails.get(orgName).get("orgSwitchToken"));
            }
        }
    }

    /**
     * Cleans up roles for the specified audiences if exists.
     * Audiences will always be either ORGANIZATION_AUDIENCE or APPLICATION_AUDIENCE or both.
     *
     * <p>
     * The `@SuppressWarnings("unchecked")` annotation is used in this method because the values being cast are
     * predefined in the test data providers.
     * </p>
     * @param audiences The audiences for which roles need to be cleaned up.
     * @throws Exception If an error occurs during the cleanup process.
     */
    @SuppressWarnings("unchecked")
    protected void cleanUpRoles(String... audiences) throws Exception {

        for(String audience : audiences) {
            Map<String, Object> orgWiseRolesOfAudience = roleDetails.get(audience);
            for (Map.Entry<String, Object> entry : orgWiseRolesOfAudience.entrySet()) {
                String audienceName = entry.getKey();
                Map<String, String> roles = (Map<String, String>) entry.getValue();
                for (Map.Entry<String, String> role : roles.entrySet()) {
                    String roleId = role.getValue();
                    if(audienceName.contains(ROOT_ORG_NAME)) {
                        deleteRoleIfExists(roleId);
                    }
                }
            }
        }
    }

    /**
     * Cleans up applications by deleting them if they exist.
     *
     * @throws Exception If an error occurs while deleting the applications.
     */
    protected void cleanUpApplications() throws Exception {

        for (Map.Entry<String, Map<String, Object>> entry : appDetails.entrySet()) {
            Map<String, Object> details = entry.getValue();
            deleteApplicationIfExists(details.get("appId").toString());
        }
    }

    /**
     * Cleans up organizations by deleting them from the deepest level to the root level.
     *
     * @throws Exception If an error occurs while deleting the organizations.
     */
    protected void cleanUpOrganizations() throws Exception {
        // Determine the deepest organization level in the hierarchy
        int maxDepth = orgDetails.values().stream()
                .mapToInt(details -> (int) details.get("orgLevel"))
                .max()
                .orElse(1);

        // Delete organizations starting from the deepest level down to the root level
        for (int level = maxDepth; level >= 1; level--) {
            for (Map.Entry<String, Map<String, Object>> entry : orgDetails.entrySet()) {
                if ((int) entry.getValue().get("orgLevel") == level) {
                    deleteOrganization(entry.getKey(), entry.getValue());
                }
            }
        }
    }

    /**
     * Cleans up the detail maps by clearing all entries.
     */
    protected void cleanUpDetailMaps() {

        userDetails.clear();
        orgDetails.clear();
        appDetails.clear();
        roleDetails.clear();
    }

    /**
     * Close the HTTP clients for OAuth2, SCIM2, and Organization Management.
     *
     * @throws IOException If an error occurred while closing the HTTP clients.
     */
    protected void closeRestClients() throws IOException {

        oAuth2RestClient.closeHttpClient();
        scim2RestClient.closeHttpClient();
        orgMgtRestClient.closeHttpClient();
    }

    private void deleteOrganization(String orgName, Map<String, Object> details) throws Exception {
        String orgId = getOrgId(orgName);
        String parentOrgId = (String) details.get("parentOrgId");

        if ((int) details.get("orgLevel") > 1) {
            deleteSubOrganizationIfExists(orgId, parentOrgId);
        } else {
            deleteOrganizationIfExists(orgId);
        }
    }

    private void deleteUserIfExists(String userId) throws Exception {

        if (userId != null) {
            scim2RestClient.deleteUser(userId);
        }
    }

    private void deleteSubOrgUserIfExists(String userId, String organizationSwitchToken) throws Exception {

        if (userId != null) {
            scim2RestClient.deleteSubOrgUser(userId, organizationSwitchToken);
        }
    }

    private void deleteRoleIfExists(String roleId) throws Exception {

        if (roleId != null) {
            scim2RestClient.deleteV2Role(roleId);
        }
    }

    private void deleteApplicationIfExists(String appId) throws Exception {

        if (appId != null) {
            oAuth2RestClient.deleteApplication(appId);
        }
    }

    private void deleteSubOrganizationIfExists(String orgId, String parentId) throws Exception {

        if (orgId != null) {
            orgMgtRestClient.deleteSubOrganization(orgId, parentId);
        }
    }

    private void deleteOrganizationIfExists(String orgId) throws Exception {

        if (orgId != null) {
            orgMgtRestClient.deleteOrganization(orgId);
        }
    }

    // Methods to create request bodies for user sharing and unsharing.

    /**
     * Creates a `UserShareRequestBodyUserCriteria` object with the given user IDs.
     *
     * @param userIds The list of user IDs to be included in the criteria.
     * @return A `UserShareRequestBodyUserCriteria` object containing the specified user IDs.
     */
    protected UserShareRequestBodyUserCriteria getUserCriteriaForBaseUserSharing(List<String> userIds) {

        UserShareRequestBodyUserCriteria criteria = new UserShareRequestBodyUserCriteria();
        criteria.setUserIds(userIds);
        return criteria;
    }

    /**
     * Creates a `UserUnshareRequestBodyUserCriteria` object with the given user IDs.
     *
     * @param userIds The list of user IDs to be included in the criteria.
     * @return A `UserUnshareRequestBodyUserCriteria` object containing the specified user IDs.
     */
    protected UserUnshareRequestBodyUserCriteria getUserCriteriaForBaseUserUnsharing(List<String> userIds) {

        UserUnshareRequestBodyUserCriteria criteria = new UserUnshareRequestBodyUserCriteria();
        criteria.setUserIds(userIds);
        return criteria;
    }

    /**
     * Converts a map of organization details into a list of `UserShareRequestBodyOrganizations` objects.
     *
     * @param organizations A map where the key is the organization name and the value is a map of organization details.
     * @return A list of `UserShareRequestBodyOrganizations` objects.
     * <p>
     * The `@SuppressWarnings("unchecked")` annotation is used in this method because the values being cast are
     * predefined in the test data providers.
     * </p>
     */
    @SuppressWarnings("unchecked")
    protected List<UserShareRequestBodyOrganizations> getOrganizationsForSelectiveUserSharing(Map<String, Map<String, Object>> organizations) {

        List<UserShareRequestBodyOrganizations> orgs = new ArrayList<>();

        for (Map.Entry<String, Map<String, Object>> entry : organizations.entrySet()) {

            Map<String, Object> orgDetails = entry.getValue();

            UserShareRequestBodyOrganizations org = new UserShareRequestBodyOrganizations();
            org.setOrgId((String) orgDetails.get(MAP_KEY_SELECTIVE_ORG_ID));
            org.setPolicy((UserShareRequestBodyOrganizations.PolicyEnum) orgDetails.get(MAP_KEY_SELECTIVE_POLICY));
            org.setRoles((List<RoleWithAudience>) orgDetails.get(MAP_KEY_SELECTIVE_ROLES));

            orgs.add(org);
        }
        return orgs;
    }

    /**
     * Retrieves the policy enum for general user sharing from the provided map.
     *
     * @param policyWithRoles A map containing the policy and roles for general user sharing.
     * @return The policy enum for general user sharing.
     */
    protected UserShareWithAllRequestBody.PolicyEnum getPolicyEnumForGeneralUserSharing(Map<String, Object> policyWithRoles) {

        return (UserShareWithAllRequestBody.PolicyEnum)policyWithRoles.get(MAP_KEY_GENERAL_POLICY) ;
    }

    /**
     * Retrieves the roles for general user sharing from the provided map.
     *
     * @param policyWithRoles A map containing the policy and roles for general user sharing.
     * @return A list of `RoleWithAudience` objects representing the roles for general user sharing.
     * <p>
     * The `@SuppressWarnings("unchecked")` annotation is used in this method because the values being cast are
     * predefined in the test data providers.
     * </p>
     */
    @SuppressWarnings("unchecked")
    protected List<RoleWithAudience> getRolesForGeneralUserSharing(Map<String, Object> policyWithRoles) {

        return (List<RoleWithAudience>) policyWithRoles.get(MAP_KEY_GENERAL_ROLES);
    }

    /**
     * Retrieves the list of organization IDs from which the users are being selectively unshared.
     *
     * @param removingOrgIds The list of organization IDs to be removed.
     * @return A list of organization IDs as strings.
     */
    protected List<String> getOrganizationsForSelectiveUserUnsharing(List<String> removingOrgIds) {

        return removingOrgIds;
    }

    // Helper methods.

    protected String toJSONString(java.lang.Object object) {

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(object);
    }
}

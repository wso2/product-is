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
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.apache.commons.lang.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
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
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.everyItem;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.wso2.identity.integration.test.restclients.RestBaseClient.TENANT_PATH;
import static org.wso2.identity.integration.test.restclients.RestBaseClient.USER_AGENT_ATTRIBUTE;
import static org.awaitility.Awaitility.await;

/**
 * Base test class for the User Sharing REST APIs.
 */
public class UserSharingBaseTest extends RESTAPIServerTestBase {

    protected static String swaggerDefinition;

    protected OAuth2RestClient oAuth2RestClient;
    protected SCIM2RestClient scim2RestClient;
    protected OrgMgtRestClient orgMgtRestClient;
    protected HttpClient httpClient;

    protected Map<String, Map<String, Object>> userDetails;
    protected Map<String, Map<String, Object>> orgDetails;
    protected Map<String, Map<String, Object>> appDetails;
    protected Map<String, Map<String, Object>> roleDetails;

    protected static final String API_DEFINITION_NAME = "organization-user-share.yaml";
    protected static final String AUTHORIZED_APIS_JSON = "user-sharing-apis.json";
    protected static final String API_VERSION = "v1";
    private static final String API_PACKAGE_NAME =
            "org.wso2.carbon.identity.api.server.organization.user.sharing.management.v1";

    protected static final String API_SERVER_BASE_PATH = "/api/server/v1";
    protected static final String ORGANIZATION_API_PATH = "/o";
    protected static final String USER_SHARING_API_BASE_PATH = "/users";
    protected static final String SHARE_PATH = "/share";
    protected static final String SHARE_WITH_ALL_PATH = "/share-with-all";
    protected static final String UNSHARE_PATH = "/unshare";
    protected static final String UNSHARE_WITH_ALL_PATH = "/unshare-with-all";
    protected static final String SHARED_ORGANIZATIONS_PATH = "/shared-organizations";
    protected static final String SHARED_ROLES_PATH = "/shared-roles";

    protected static final String PATH_SEPARATOR = "/";
    protected static final String QUERY_PARAM_SEPARATOR = "?";
    protected static final String QUERY_PARAM_VALUE_SEPARATOR = "=";

    protected static final String HEADER_AUTHORIZATION = "Authorization";
    protected static final String HEADER_AUTHORIZATION_VALUE_BEARER = "Bearer ";
    protected static final String HEADER_CONTENT_TYPE = "Content-Type";

    protected static final String SHARED_TYPE_SHARED = "SHARED";
    protected static final String SHARED_TYPE_OWNER = "OWNER";
    protected static final String SHARED_TYPE_INVITED = "INVITED";

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
    protected static final String ROOT_ORG_USER_DUPLICATED_USERNAME = "rootUserDuplicated";

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

    protected static final String MAP_ORG_DETAILS_KEY_ORG_NAME = "orgName";
    protected static final String MAP_ORG_DETAILS_KEY_ORG_ID = "orgId";
    protected static final String MAP_ORG_DETAILS_KEY_PARENT_ORG_ID = "parentOrgId";
    protected static final String MAP_ORG_DETAILS_KEY_ORG_SWITCH_TOKEN = "orgSwitchToken";
    protected static final String MAP_ORG_DETAILS_KEY_ORG_LEVEL = "orgLevel";

    protected static final String MAP_APP_DETAILS_KEY_APP_NAME = "appName";
    protected static final String MAP_APP_DETAILS_KEY_APP_ID = "appId";
    protected static final String MAP_APP_DETAILS_KEY_APP_AUDIENCE = "appAudience";
    protected static final String MAP_APP_DETAILS_KEY_CLIENT_ID = "clientId";
    protected static final String MAP_APP_DETAILS_KEY_CLIENT_SECRET = "clientSecret";
    protected static final String MAP_APP_DETAILS_KEY_ROLE_NAMES = "roleNames";
    protected static final String MAP_APP_DETAILS_KEY_ROLE_IDS_BY_NAME = "roleIdsByName";
    protected static final String MAP_APP_DETAILS_KEY_APP_DETAILS_OF_SUB_ORGS = "appDetailsOfSubOrgs";
    protected static final String MAP_APP_DETAILS_KEY_APP_SUB_ORG_NAME = "subOrgName";

    protected static final String MAP_USER_DETAILS_KEY_DOMAIN_QUALIFIED_USER_NAME = "domainQualifiedUserName";
    protected static final String MAP_USER_DETAILS_KEY_USER_NAME = "userName";
    protected static final String MAP_USER_DETAILS_KEY_USER_ID = "userId";
    protected static final String MAP_USER_DETAILS_KEY_USER_DOMAIN = "userDomain";
    protected static final String MAP_USER_DETAILS_KEY_USER_ORG_NAME = "userOrgName";
    protected static final String MAP_USER_DETAILS_KEY_USER_ORG_ID = "userOrgId";
    protected static final String MAP_USER_DETAILS_KEY_USER_ORG_LEVEL = "userOrgLevel";
    protected static final String MAP_USER_DETAILS_KEY_IS_ROOT_ORG_USER = "isRootOrgUser";

    protected static final String MAP_USER_DOMAIN_QUALIFIED_USER_NAME_USER_NAME = "userName";
    protected static final String MAP_USER_DOMAIN_QUALIFIED_USER_NAME_USER_DOMAIN = "userDomain";

    protected static final String SCOPE_INTERNAL_USER_SHARE = "internal_user_share";
    protected static final String SCOPE_INTERNAL_USER_UNSHARE = "internal_user_unshare";
    protected static final String SCOPE_INTERNAL_USER_SHARED_ACCESS_VIEW = "internal_user_shared_access_view";
    protected static final String SCOPE_INTERNAL_ORG_USER_SHARE = "internal_org_user_share";
    protected static final String SCOPE_INTERNAL_ORG_USER_UNSHARE = "internal_org_user_unshare";
    protected static final String SCOPE_INTERNAL_ORG_USER_SHARED_ACCESS_VIEW = "internal_org_user_shared_access_view";

    protected static final String GRANT_AUTHORIZATION_CODE = "authorization_code";
    protected static final String GRANT_IMPLICIT = "implicit";
    protected static final String GRANT_PASSWORD = "password";
    protected static final String GRANT_CLIENT_CREDENTIALS = "client_credentials";
    protected static final String GRANT_REFRESH_TOKEN = "refresh_token";
    protected static final String GRANT_ORGANIZATION_SWITCH = "organization_switch";

    protected static final String CLAIM_EMAIL_URI = "http://wso2.org/claims/emailaddress";
    protected static final String CLAIM_COUNTRY_URI = "http://wso2.org/claims/country";
    protected static final String CLAIM_ROLES_URI = "http://wso2.org/claims/roles";
    protected static final String CLAIM_GROUPS_URI = "http://wso2.org/claims/groups";

    protected static final String ATTRIBUTE_USER_PASSWORD = "Admin123";
    protected static final String ATTRIBUTE_USER_EMAIL_DOMAIN = "@gmail.com";
    protected static final String ATTRIBUTE_USER_SCHEMA_SCIM2_USER = "urn:ietf:params:scim:schemas:core:2.0:User";

    protected static final String RESPONSE_STATUS = "status";
    protected static final String RESPONSE_DETAILS = "details";
    protected static final String RESPONSE_STATUS_VALUE = "Processing";
    protected static final String RESPONSE_DETAIL_VALUE_SHARING = "User sharing process triggered successfully.";
    protected static final String RESPONSE_DETAIL_VALUE_UNSHARING = "User unsharing process triggered successfully.";

    protected static final String RESPONSE_LINKS_SIZE = "links.size()";
    protected static final String RESPONSE_LINKS_EMPTY = "links[0].isEmpty()";
    protected static final String RESPONSE_LINKS_SHARED_ORGS = "sharedOrganizations";
    protected static final String RESPONSE_LINKS_SHARED_ORGS_SIZE = "sharedOrganizations.size()";
    protected static final String RESPONSE_LINKS_SHARED_ORGS_ID = "sharedOrganizations.orgId";
    protected static final String RESPONSE_LINKS_SHARED_ORGS_NAME = "sharedOrganizations.orgName";
    protected static final String RESPONSE_LINKS_SHARED_ORGS_SHARED_USER_ID = "sharedOrganizations.sharedUserId";
    protected static final String RESPONSE_LINKS_SHARED_ORGS_SHARED_TYPE = "sharedOrganizations.sharedType";
    protected static final String RESPONSE_LINKS_SHARED_ORGS_ROLES_REF = "sharedOrganizations.rolesRef";
    protected static final String RESPONSE_LINKS_SHARED_ORGS_ROLES = "roles";
    protected static final String RESPONSE_LINKS_SHARED_ORGS_ROLES_SIZE = "roles.size()";
    protected static final String RESPONSE_LINKS_SHARED_ORGS_ROLES_NAME = "roles.displayName";
    protected static final String RESPONSE_LINKS_SHARED_ORGS_ROLES_AUDIENCE_NAME = "roles.audience.display";
    protected static final String RESPONSE_LINKS_SHARED_ORGS_ROLES_AUDIENCE_TYPE = "roles.audience.type";

    protected static final String ERROR_SETUP_SWAGGER_DEFINITION = "Unable to read the swagger definition %s from %s";
    protected static final String SHARED_USER_ID_JSON_PATH =
            "sharedOrganizations.find { it.orgName == '%s' }.sharedUserId";

    static {
        try {
            swaggerDefinition = getAPISwaggerDefinition(API_PACKAGE_NAME, API_DEFINITION_NAME);
        } catch (IOException e) {
            Assert.fail(String.format(ERROR_SETUP_SWAGGER_DEFINITION, API_DEFINITION_NAME, API_PACKAGE_NAME), e);
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

    protected HttpResponse getResponseOfPostToSubOrg(String path, String body, String token) throws Exception {

        HttpPost request =
                new HttpPost(serverURL + TENANT_PATH + tenant + ORGANIZATION_API_PATH + API_SERVER_BASE_PATH + path);
        request.setHeaders(getHeaders(token));
        request.setEntity(new StringEntity(body));
        return httpClient.execute(request);
    }

    protected HttpResponse sendGetRequest(String endpointURL, HttpClient client) throws Exception {

        HttpGet request = new HttpGet(endpointURL);
        request.setHeader(USER_AGENT_ATTRIBUTE, OAuth2Constant.USER_AGENT);
        return client.execute(request);
    }

    protected HttpResponse sendPostRequest(String endpointURL, List<NameValuePair> urlParameters, HttpClient client)
            throws Exception {

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

        return orgDetails.get(orgName).get(MAP_ORG_DETAILS_KEY_ORG_ID).toString();
    }

    protected void setOrgDetails(String orgName, String orgId, String parentId, int orgLevel) throws Exception {

        Map<String, Object> orgDetail = new HashMap<>();
        orgDetail.put(MAP_ORG_DETAILS_KEY_ORG_NAME, orgName);
        orgDetail.put(MAP_ORG_DETAILS_KEY_ORG_ID, orgId);
        orgDetail.put(MAP_ORG_DETAILS_KEY_PARENT_ORG_ID, parentId);
        orgDetail.put(MAP_ORG_DETAILS_KEY_ORG_SWITCH_TOKEN, orgMgtRestClient.switchM2MToken(orgId));
        orgDetail.put(MAP_ORG_DETAILS_KEY_ORG_LEVEL, orgLevel);
        orgDetails.put(orgName, orgDetail);
    }

    // Methods to add applications and roles for testing purposes.

    protected Map<String, Object> createApplication(String appName, String audience, List<String> roleNames)
            throws Exception {

        Map<String, Object> createdAppDetails = new HashMap<>();
        String rootOrgAppName = appName + PATH_SEPARATOR + ROOT_ORG_NAME;

        ApplicationResponseModel application = addApplication(appName);
        String appId = application.getId();
        OpenIDConnectConfiguration oidcConfig = oAuth2RestClient.getOIDCInboundDetails(appId);
        String clientId = oidcConfig.getClientId();
        String clientSecret = oidcConfig.getClientSecret();
        Map<String, String> roleIdsByName = new HashMap<>();

        if (StringUtils.equalsIgnoreCase(APPLICATION_AUDIENCE, audience)) {

            Audience appRoleAudience = new Audience(APPLICATION_AUDIENCE, appId);
            for (String roleName : roleNames) {
                RoleV2 appRole =
                        new RoleV2(appRoleAudience, roleName, Collections.emptyList(), Collections.emptyList());
                String roleId = scim2RestClient.addV2Role(appRole);
                roleIdsByName.put(roleName, roleId);
            }
            storeRoleDetails(APPLICATION_AUDIENCE, rootOrgAppName, roleIdsByName);
            createdAppDetails.put(MAP_APP_DETAILS_KEY_APP_AUDIENCE, APPLICATION_AUDIENCE);

        } else {

            switchApplicationAudience(appId, AssociatedRolesConfig.AllowedAudienceEnum.ORGANIZATION);

            for (String roleName : roleNames) {
                String roleId = scim2RestClient.getRoleIdByName(roleName);
                roleIdsByName.put(roleName, roleId);
            }
            createdAppDetails.put(MAP_APP_DETAILS_KEY_APP_AUDIENCE, ORGANIZATION_AUDIENCE);
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

        createdAppDetails.put(MAP_APP_DETAILS_KEY_APP_NAME, appName);
        createdAppDetails.put(MAP_APP_DETAILS_KEY_APP_ID, appId);
        createdAppDetails.put(MAP_APP_DETAILS_KEY_CLIENT_ID, clientId);
        createdAppDetails.put(MAP_APP_DETAILS_KEY_CLIENT_SECRET, clientSecret);
        createdAppDetails.put(MAP_APP_DETAILS_KEY_ROLE_NAMES, roleNames);
        createdAppDetails.put(MAP_APP_DETAILS_KEY_ROLE_IDS_BY_NAME, roleIdsByName);
        createdAppDetails.put(MAP_APP_DETAILS_KEY_APP_DETAILS_OF_SUB_ORGS, appDetailsOfSubOrgs);

        appDetails.put(appName, createdAppDetails);
        return createdAppDetails;
    }

    protected Map<String, Object> getAppDetailsOfSubOrg(String appName, String audience, List<String> roleNames,
                                                        Map<String, Object> orgDetail) throws Exception {

        Map<String, Object> subOrgAppDetails = new HashMap<>();

        String subOrgName = (String) orgDetail.get(MAP_ORG_DETAILS_KEY_ORG_NAME);
        String subOrgId = (String) orgDetail.get(MAP_ORG_DETAILS_KEY_ORG_ID);
        String subOrgSwitchToken = (String) orgDetail.get(MAP_ORG_DETAILS_KEY_ORG_SWITCH_TOKEN);
        String subOrgAppName = appName + PATH_SEPARATOR + subOrgName;

        String subOrgAppId = oAuth2RestClient.getAppIdUsingAppNameInOrganization(appName, subOrgSwitchToken);

        Map<String, String> subOrgRoleIdsByName = StringUtils.equalsIgnoreCase(APPLICATION_AUDIENCE, audience) ?
                getSubOrgRoleIdsByName(roleNames, APPLICATION_AUDIENCE, subOrgAppName, subOrgAppId, subOrgSwitchToken) :
                getSubOrgRoleIdsByName(roleNames, ORGANIZATION_AUDIENCE, subOrgName, subOrgId, subOrgSwitchToken);

        subOrgAppDetails.put(MAP_APP_DETAILS_KEY_APP_SUB_ORG_NAME, subOrgName);
        subOrgAppDetails.put(MAP_APP_DETAILS_KEY_APP_NAME, appName);
        subOrgAppDetails.put(MAP_APP_DETAILS_KEY_APP_ID, subOrgAppId);
        subOrgAppDetails.put(MAP_APP_DETAILS_KEY_ROLE_NAMES, roleNames);
        subOrgAppDetails.put(MAP_APP_DETAILS_KEY_ROLE_IDS_BY_NAME, subOrgRoleIdsByName);
        subOrgAppDetails.put(MAP_APP_DETAILS_KEY_APP_AUDIENCE, audience);

        return subOrgAppDetails;
    }

    protected Map<String, String> getSubOrgRoleIdsByName(List<String> roleNames, String audienceType,
                                                         String audienceName, String audienceValue,
                                                         String subOrgSwitchToken) throws Exception {

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

    protected Map<String, String> createOrganizationRoles(String orgName, List<String> orgRoleNames)
            throws Exception {

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

        return API_SERVER_BASE_PATH + USER_SHARING_API_BASE_PATH + PATH_SEPARATOR + userId + SHARED_ROLES_PATH +
                QUERY_PARAM_SEPARATOR + QUERY_PARAM_ORG_ID + QUERY_PARAM_VALUE_SEPARATOR + orgId;
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
        Collections.addAll(grantTypes, GRANT_AUTHORIZATION_CODE, GRANT_IMPLICIT, GRANT_PASSWORD,
                GRANT_CLIENT_CREDENTIALS, GRANT_REFRESH_TOKEN, GRANT_ORGANIZATION_SWITCH);

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

        ClaimMappings emailClaim = new ClaimMappings().applicationClaim(CLAIM_EMAIL_URI);
        emailClaim.setLocalClaim(
                new org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.Claim().uri(
                        CLAIM_EMAIL_URI));
        ClaimMappings countryClaim = new ClaimMappings().applicationClaim(CLAIM_COUNTRY_URI);
        countryClaim.setLocalClaim(
                new org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.Claim().uri(
                        CLAIM_COUNTRY_URI));

        RequestedClaimConfiguration emailRequestedClaim = new RequestedClaimConfiguration();
        emailRequestedClaim.setClaim(
                new org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.Claim().uri(
                        CLAIM_EMAIL_URI));
        RequestedClaimConfiguration countryRequestedClaim = new RequestedClaimConfiguration();
        countryRequestedClaim.setClaim(
                new org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.Claim().uri(
                        CLAIM_COUNTRY_URI));

        ClaimConfiguration claimConfiguration = new ClaimConfiguration().dialect(ClaimConfiguration.DialectEnum.CUSTOM);
        claimConfiguration.addClaimMappingsItem(emailClaim);
        claimConfiguration.addClaimMappingsItem(countryClaim);
        claimConfiguration.addRequestedClaimsItem(emailRequestedClaim);
        claimConfiguration.addRequestedClaimsItem(countryRequestedClaim);

        return claimConfiguration;
    }

    private ClaimConfiguration getClaimConfigurationsWithRolesAndGroups() {

        ClaimConfiguration claimConfiguration = new ClaimConfiguration();
        claimConfiguration.addRequestedClaimsItem(getRequestedClaim(CLAIM_ROLES_URI));
        claimConfiguration.addRequestedClaimsItem(getRequestedClaim(CLAIM_GROUPS_URI));
        return claimConfiguration;
    }

    private RequestedClaimConfiguration getRequestedClaim(String claimUri) {

        RequestedClaimConfiguration requestedClaim = new RequestedClaimConfiguration();
        requestedClaim.setClaim(
                new org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.Claim().uri(
                        claimUri));
        return requestedClaim;
    }

    private void updateRequestedClaimsOfApp(String applicationId, ClaimConfiguration claimConfigurationsForApp)
            throws Exception {

        ApplicationPatchModel applicationPatch = new ApplicationPatchModel();
        applicationPatch.setClaimConfiguration(claimConfigurationsForApp);
        oAuth2RestClient.updateApplication(applicationId, applicationPatch);
    }

    private void switchApplicationAudience(String appId, AssociatedRolesConfig.AllowedAudienceEnum newAudience)
            throws Exception {

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
        await().atMost(5, TimeUnit.SECONDS).until(() -> true);
    }

    // Methods to add users in organizations and sub organizations for testing purposes.

    protected UserObject createUserObject(String userDomain, String userName, String orgName) {

        String domainQualifiedUserName = userDomain + PATH_SEPARATOR + userName;
        UserObject user = new UserObject()
                .userName(domainQualifiedUserName)
                .password(ATTRIBUTE_USER_PASSWORD)
                .name(new Name().givenName(userName).familyName(orgName))
                .emails(new ArrayList<>());

        Email email = new Email();
        email.setValue(userName + ATTRIBUTE_USER_EMAIL_DOMAIN);
        email.setPrimary(true);
        user.getEmails().add(email);

        List<String> schemas = new ArrayList<>();
        schemas.add(ATTRIBUTE_USER_SCHEMA_SCIM2_USER);
        user.setSchemas(schemas);

        return user;
    }

    protected String createUser(UserObject user) throws Exception {

        String userId = scim2RestClient.createUser(user);
        String domainQualifiedUserName = user.getUserName();
        String domainQualifiedUserNameWithOrg = domainQualifiedUserName + PATH_SEPARATOR + ROOT_ORG_NAME;

        Map<String, Object> userDetail = new HashMap<>();
        userDetail.put(MAP_USER_DETAILS_KEY_DOMAIN_QUALIFIED_USER_NAME, domainQualifiedUserName);
        userDetail.put(MAP_USER_DETAILS_KEY_USER_NAME,
                getUserNameAndUserDomain(domainQualifiedUserName).get(MAP_USER_DOMAIN_QUALIFIED_USER_NAME_USER_NAME));
        userDetail.put(MAP_USER_DETAILS_KEY_USER_DOMAIN,
                getUserNameAndUserDomain(domainQualifiedUserName).get(MAP_USER_DOMAIN_QUALIFIED_USER_NAME_USER_DOMAIN));
        userDetail.put(MAP_USER_DETAILS_KEY_USER_ID, userId);
        userDetail.put(MAP_USER_DETAILS_KEY_IS_ROOT_ORG_USER, true);
        userDetail.put(MAP_USER_DETAILS_KEY_USER_ORG_NAME, ROOT_ORG_NAME);
        userDetail.put(MAP_USER_DETAILS_KEY_USER_ORG_ID, ROOT_ORG_ID);
        userDetail.put(MAP_USER_DETAILS_KEY_USER_ORG_LEVEL, 0);

        userDetails.put(domainQualifiedUserNameWithOrg, userDetail);
        return userId;
    }

    protected String createSuborgUser(UserObject user, String suborg) throws Exception {

        String userId = scim2RestClient.createSubOrgUser(user,
                (String) orgDetails.get(suborg).get(MAP_ORG_DETAILS_KEY_ORG_SWITCH_TOKEN));
        String domainQualifiedUserName = user.getUserName();
        String domainQualifiedUserNameWithOrg = domainQualifiedUserName + PATH_SEPARATOR + suborg;

        Map<String, Object> userDetail = new HashMap<>();
        userDetail.put(MAP_USER_DETAILS_KEY_DOMAIN_QUALIFIED_USER_NAME, domainQualifiedUserName);
        userDetail.put(MAP_USER_DETAILS_KEY_USER_NAME,
                getUserNameAndUserDomain(domainQualifiedUserName).get(MAP_USER_DOMAIN_QUALIFIED_USER_NAME_USER_NAME));
        userDetail.put(MAP_USER_DETAILS_KEY_USER_DOMAIN,
                getUserNameAndUserDomain(domainQualifiedUserName).get(MAP_USER_DOMAIN_QUALIFIED_USER_NAME_USER_DOMAIN));
        userDetail.put(MAP_USER_DETAILS_KEY_USER_ID, userId);
        userDetail.put(MAP_USER_DETAILS_KEY_IS_ROOT_ORG_USER, false);
        userDetail.put(MAP_USER_DETAILS_KEY_USER_ORG_NAME, suborg);
        userDetail.put(MAP_USER_DETAILS_KEY_USER_ORG_ID, orgDetails.get(suborg).get(MAP_ORG_DETAILS_KEY_ORG_ID));
        userDetail.put(MAP_USER_DETAILS_KEY_USER_ORG_LEVEL, orgDetails.get(suborg).get(MAP_ORG_DETAILS_KEY_ORG_LEVEL));

        userDetails.put(domainQualifiedUserNameWithOrg, userDetail);
        return userId;
    }

    protected String getUserId(String userName, String userDomain, String orgName) {

        String domainQualifiedUserNameWithOrg = userDomain + PATH_SEPARATOR + userName + PATH_SEPARATOR + orgName;
        return userDetails.get(domainQualifiedUserNameWithOrg).get(MAP_USER_DETAILS_KEY_USER_ID).toString();
    }

    private Map<String, String> getUserNameAndUserDomain(String domainQualifiedUserName) {

        String[] parts = domainQualifiedUserName.split(PATH_SEPARATOR);
        Map<String, String> userNameAndUserDomain = new HashMap<>();
        userNameAndUserDomain.put(MAP_USER_DOMAIN_QUALIFIED_USER_NAME_USER_NAME, parts[1]);
        userNameAndUserDomain.put(MAP_USER_DOMAIN_QUALIFIED_USER_NAME_USER_DOMAIN, parts[0]);
        return userNameAndUserDomain;
    }

    // Method to validate user shared organizations and assigned roles.

    /**
     * Validates the user sharing results by checking if the users have been shared to the expected organizations
     * with the expected roles.
     * This method uses the Awaitility library to wait for up to 20 seconds, polling every 2 seconds, to ensure that
     * the user sharing results are as expected. If the validation fails within this period, an exception is thrown.
     *
     * @param userIds         The list of user IDs to validate.
     * @param expectedResults A map containing the expected results, including the expected organization count,
     *                        expected organization IDs, expected organization names, and expected roles per organization.
     * @throws Exception If an error occurs during validation.
     */
    protected void validateUserSharingResults(List<String> userIds, Map<String, Object> expectedResults)
            throws Exception {

        final Object[] lastException = {null};

        await().atMost(20, TimeUnit.SECONDS)
                .pollInterval(2, TimeUnit.SECONDS)
                .ignoreExceptions()
                .until(() -> {
                    try {
                        for (String userId : userIds) {
                            validateUserHasBeenSharedToExpectedOrgsWithExpectedRoles(userId, expectedResults);
                        }
                        lastException[0] = null;
                        return true;
                    } catch (AssertionError | Exception e) {
                        lastException[0] = e;
                        return false;
                    }
                });

        if (lastException[0] != null) {
            throw (Exception) lastException[0];
        }
    }

    /**
     * Validates user sharing results and retrieves the list of shared user IDs.
     * This method uses the Awaitility library to wait for up to 20 seconds, polling every 2 seconds,
     * to ensure that the user sharing results are as expected. If the validation fails within this period,
     * an exception is thrown.
     *
     * @param userIds                The list of user IDs to validate.
     * @param reSharingSubOrgDetails The details of the sub-organization for re-sharing.
     * @param expectedSharedResults  A map containing the expected results, including the expected organization count,
     *                               expected organization IDs, expected organization names, and expected roles per organization.
     * @return A list of shared user IDs.
     * @throws Exception If an error occurs during validation.
     */
    protected List<String> validateUserSharingResultsAndGetSharedUsersList(List<String> userIds,
                                                                           Map<String, Object> reSharingSubOrgDetails,
                                                                           Map<String, Object> expectedSharedResults)
            throws Exception {

        final Object[] lastException = {null};

        List<String> sharedUserIds = new ArrayList<>();
        await().atMost(20, TimeUnit.SECONDS)
                .pollInterval(2, TimeUnit.SECONDS)
                .ignoreExceptions()
                .until(() -> {
                    try {
                        for (String userId : userIds) {
                            validateUserHasBeenSharedToExpectedOrgsWithExpectedRoles(userId, expectedSharedResults);
                        }
                        lastException[0] = null;
                        return true;
                    } catch (AssertionError | Exception e) {
                        lastException[0] = e;
                        return false;
                    }
                });

        if (lastException[0] != null) {
            throw (Exception) lastException[0];
        }

        // Once assertions pass, extract shared user IDs
        for (String userId : userIds) {
            Response sharedOrgsResponseOfUserId =
                    getResponseOfGet(USER_SHARING_API_BASE_PATH + "/" + userId + SHARED_ORGANIZATIONS_PATH);
            String sharedUserId = extractSharedUserId(sharedOrgsResponseOfUserId,
                    reSharingSubOrgDetails.get(MAP_ORG_DETAILS_KEY_ORG_NAME).toString());
            sharedUserIds.add(sharedUserId);
        }
        return sharedUserIds;
    }

    /**
     * Validate that the user has been shared to the expected organizations with the expected roles.
     *
     * @param userId          The ID of the user to validate.
     * @param expectedResults A map containing the expected results, including the expected organization count,
     *                        expected organization IDs, expected organization names, and expected roles per
     *                        organization.
     *
     * <p>
     * The `@SuppressWarnings("unchecked")` annotation is used in this method because the values being cast are
     * predefined in the test data providers.
     * </p>
     */
    @SuppressWarnings("unchecked")
    protected void validateUserHasBeenSharedToExpectedOrgsWithExpectedRoles(String userId,
                                                                            Map<String, Object> expectedResults) {

        testGetSharedOrganizations(
                userId,
                (int) expectedResults.get(MAP_KEY_EXPECTED_ORG_COUNT),
                (List<String>) expectedResults.get(MAP_KEY_EXPECTED_ORG_IDS),
                (List<String>) expectedResults.get(MAP_KEY_EXPECTED_ORG_NAMES)
                                  );

        Map<String, List<RoleWithAudience>> expectedRolesPerExpectedOrg =
                (Map<String, List<RoleWithAudience>>) expectedResults.get(MAP_KEY_EXPECTED_ROLES_PER_EXPECTED_ORG);
        for (Map.Entry<String, List<RoleWithAudience>> entry : expectedRolesPerExpectedOrg.entrySet()) {
            testGetSharedRolesForOrg(userId, entry.getKey(), entry.getValue());
        }
    }

    /**
     * Test method for GET /user-sharing/{userId}/shared-organizations.
     *
     * @param userId           The ID of the user to get shared organizations for.
     * @param expectedOrgCount The expected number of shared organizations.
     * @param expectedOrgIds   The expected IDs of the shared organizations.
     * @param expectedOrgNames The expected names of the shared organizations.
     */
    protected void testGetSharedOrganizations(String userId, int expectedOrgCount, List<String> expectedOrgIds,
                                              List<String> expectedOrgNames) {

        Response response =
                getResponseOfGet(USER_SHARING_API_BASE_PATH + "/" + userId + SHARED_ORGANIZATIONS_PATH);

        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body(RESPONSE_LINKS_SIZE, equalTo(1))
                .body(RESPONSE_LINKS_EMPTY, equalTo(true))
                .body(RESPONSE_LINKS_SHARED_ORGS, notNullValue())
                .body(RESPONSE_LINKS_SHARED_ORGS_SIZE, equalTo(expectedOrgCount))
                .body(RESPONSE_LINKS_SHARED_ORGS_ID, hasItems(expectedOrgIds.toArray(new String[0])))
                .body(RESPONSE_LINKS_SHARED_ORGS_NAME, hasItems(expectedOrgNames.toArray(new String[0])))
                .body(RESPONSE_LINKS_SHARED_ORGS_SHARED_TYPE, everyItem(equalTo(SHARED_TYPE_SHARED)))
                .body(RESPONSE_LINKS_SHARED_ORGS_ROLES_REF, hasItems(
                        expectedOrgIds.stream()
                                .map(orgId -> getSharedOrgsRolesRef(userId, orgId))
                                .toArray(String[]::new)));
    }

    /**
     * Test method for GET /user-sharing/{userId}/shared-roles?orgId={orgId}.
     *
     * @param userId        The ID of the user to get shared roles for.
     * @param orgId         The ID of the organization to get shared roles for.
     * @param expectedRoles The expected roles for the user in the specified organization.
     */
    protected void testGetSharedRolesForOrg(String userId, String orgId, List<RoleWithAudience> expectedRoles) {

        Response response = getResponseOfGet(USER_SHARING_API_BASE_PATH + "/" + userId + SHARED_ROLES_PATH,
                Collections.singletonMap(QUERY_PARAM_ORG_ID, orgId));

        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body(RESPONSE_LINKS_SIZE, equalTo(1))
                .body(RESPONSE_LINKS_EMPTY, equalTo(true))
                .body(RESPONSE_LINKS_SHARED_ORGS_ROLES, notNullValue())
                .body(RESPONSE_LINKS_SHARED_ORGS_ROLES_SIZE, equalTo(expectedRoles.size()));

        if (!expectedRoles.isEmpty()) {
            response.then()
                    .body(RESPONSE_LINKS_SHARED_ORGS_ROLES_NAME, hasItems(
                            expectedRoles.stream()
                                    .map(RoleWithAudience::getDisplayName)
                                    .toArray(String[]::new)))
                    .body(RESPONSE_LINKS_SHARED_ORGS_ROLES_AUDIENCE_NAME, hasItems(
                            expectedRoles.stream()
                                    .map(role -> role.getAudience().getDisplay())
                                    .toArray(String[]::new)))
                    .body(RESPONSE_LINKS_SHARED_ORGS_ROLES_AUDIENCE_TYPE, hasItems(
                            expectedRoles.stream()
                                    .map(role -> role.getAudience().getType())
                                    .toArray(String[]::new)));
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
     *
     * <p>
     * The `@SuppressWarnings("unchecked")` annotation is used in this method because the values being cast are
     * predefined in the test data providers.
     * </p>
     */
    @SuppressWarnings("unchecked")
    protected List<UserShareRequestBodyOrganizations> getOrganizationsForSelectiveUserSharing(
            Map<String, Map<String, Object>> organizations) {

        List<UserShareRequestBodyOrganizations> orgs = new ArrayList<>();

        for (Map.Entry<String, Map<String, Object>> entry : organizations.entrySet()) {

            Map<String, Object> orgDetail = entry.getValue();

            UserShareRequestBodyOrganizations org = new UserShareRequestBodyOrganizations();
            org.setOrgId((String) orgDetail.get(MAP_KEY_SELECTIVE_ORG_ID));
            org.setPolicy((UserShareRequestBodyOrganizations.PolicyEnum) orgDetail.get(MAP_KEY_SELECTIVE_POLICY));
            org.setRoles((List<RoleWithAudience>) orgDetail.get(MAP_KEY_SELECTIVE_ROLES));

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
    protected UserShareWithAllRequestBody.PolicyEnum getPolicyEnumForGeneralUserSharing(
            Map<String, Object> policyWithRoles) {

        return (UserShareWithAllRequestBody.PolicyEnum) policyWithRoles.get(MAP_KEY_GENERAL_POLICY);
    }

    /**
     * Retrieves the roles for general user sharing from the provided map.
     *
     * @param policyWithRoles A map containing the policy and roles for general user sharing.
     * @return A list of `RoleWithAudience` objects representing the roles for general user sharing.
     *
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

    // Methods to clean up the resources created for testing purposes.

    /**
     * Clean up users by deleting them if they exist.
     *
     * @throws Exception If an error occurs while deleting the users.
     */
    protected void cleanUpUsers() throws Exception {

        for (Map.Entry<String, Map<String, Object>> entry : userDetails.entrySet()) {
            String userId = (String) entry.getValue().get(MAP_USER_DETAILS_KEY_USER_ID);
            String orgName = (String) entry.getValue().get(MAP_USER_DETAILS_KEY_USER_ORG_NAME);
            int orgLevel = (int) entry.getValue().get(MAP_USER_DETAILS_KEY_USER_ORG_LEVEL);

            if (orgLevel == 0) {
                deleteUserIfExists(userId);
            } else {
                deleteSubOrgUserIfExists(userId,
                        (String) orgDetails.get(orgName).get(MAP_ORG_DETAILS_KEY_ORG_SWITCH_TOKEN));
            }
        }
    }

    /**
     * Cleans up roles for the specified audiences if exists.
     * Audiences will always be either ORGANIZATION_AUDIENCE or APPLICATION_AUDIENCE or both.
     *
     * @param audiences The audiences for which roles need to be cleaned up.
     * @throws Exception If an error occurs during the cleanup process.
     *
     * <p>
     * The `@SuppressWarnings("unchecked")` annotation is used in this method because the values being cast are
     * predefined in the test data providers.
     * </p>
     */
    @SuppressWarnings("unchecked")
    protected void cleanUpRoles(String... audiences) throws Exception {

        for (String audience : audiences) {
            Map<String, Object> orgWiseRolesOfAudience = roleDetails.get(audience);
            for (Map.Entry<String, Object> entry : orgWiseRolesOfAudience.entrySet()) {
                String audienceName = entry.getKey();
                Map<String, String> roles = (Map<String, String>) entry.getValue();
                for (Map.Entry<String, String> role : roles.entrySet()) {
                    String roleId = role.getValue();
                    if (audienceName.contains(ROOT_ORG_NAME)) {
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
            deleteApplicationIfExists(details.get(MAP_APP_DETAILS_KEY_APP_ID).toString());
        }
    }

    /**
     * Cleans up organizations by deleting them from the deepest level to the root level.
     *
     * @throws Exception If an error occurs while deleting the organizations.
     */
    protected void cleanUpOrganizations() throws Exception {

        // Determine the deepest organization level in the hierarchy.
        int maxDepth = orgDetails.values().stream()
                .mapToInt(details -> (int) details.get(MAP_ORG_DETAILS_KEY_ORG_LEVEL))
                .max()
                .orElse(1);

        // Delete organizations starting from the deepest level down to the root level.
        for (int level = maxDepth; level >= 1; level--) {
            for (Map.Entry<String, Map<String, Object>> entry : orgDetails.entrySet()) {
                if ((int) entry.getValue().get(MAP_ORG_DETAILS_KEY_ORG_LEVEL) == level) {
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

    private void deleteOrganization(String orgName, Map<String, Object> orgDetail) throws Exception {

        String orgId = getOrgId(orgName);
        String parentOrgId = (String) orgDetail.get(MAP_ORG_DETAILS_KEY_PARENT_ORG_ID);

        if ((int) orgDetail.get(MAP_ORG_DETAILS_KEY_ORG_LEVEL) > 1) {
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

    // Helper methods.

    protected String extractSharedUserId(Response response, String orgName) {

        JsonPath jsonPath = response.jsonPath();
        return jsonPath.getString(String.format(SHARED_USER_ID_JSON_PATH, orgName));
    }

    protected String toJSONString(java.lang.Object object) {

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(object);
    }

    private Header[] getHeaders(String token) {

        return new Header[]{
                new BasicHeader(HEADER_AUTHORIZATION, HEADER_AUTHORIZATION_VALUE_BEARER + token),
                new BasicHeader(HEADER_CONTENT_TYPE, String.valueOf(ContentType.JSON))
        };
    }
}

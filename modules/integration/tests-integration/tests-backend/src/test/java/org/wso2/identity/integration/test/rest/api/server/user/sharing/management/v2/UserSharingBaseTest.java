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

package org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2;

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
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
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
import org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.model.ExpectedSharingMode;
import org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.model.PatchOperation;
import org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.model.RoleAssignments;
import org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.model.RoleWithAudience;
import org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.model.RoleWithAudienceAudience;
import org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.model.SelectiveShareOrgDetails;
import org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.model.UserShareRequestBodyUserCriteria;
import org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.model.UserShareWithAllRequestBody;
import org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.model.UserUnshareRequestBodyUserCriteria;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.everyItem;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.API_DEFINITION_NAME;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.API_PACKAGE_NAME;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.API_SERVER_V2_BASE_PATH;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.APPLICATION_AUDIENCE;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.ATTRIBUTE_ROLES;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.ATTRIBUTE_SHARING_MODE;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.ATTRIBUTE_USER_EMAIL_DOMAIN;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.ATTRIBUTE_USER_PASSWORD;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.ATTRIBUTE_USER_SCHEMA_SCIM2_USER;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.CLAIM_COUNTRY_URI;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.CLAIM_EMAIL_URI;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.CLAIM_GROUPS_URI;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.CLAIM_ROLES_URI;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.ERROR_SETUP_SWAGGER_DEFINITION;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.GRANT_AUTHORIZATION_CODE;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.GRANT_CLIENT_CREDENTIALS;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.GRANT_IMPLICIT;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.GRANT_ORGANIZATION_SWITCH;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.GRANT_PASSWORD;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.GRANT_REFRESH_TOKEN;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.HEADER_AUTHORIZATION;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.HEADER_AUTHORIZATION_VALUE_BEARER;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.HEADER_CONTENT_TYPE;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.MAP_APP_DETAILS_KEY_APP_AUDIENCE;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.MAP_APP_DETAILS_KEY_APP_DETAILS_OF_SUB_ORGS;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.MAP_APP_DETAILS_KEY_APP_ID;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.MAP_APP_DETAILS_KEY_APP_NAME;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.MAP_APP_DETAILS_KEY_APP_SUB_ORG_NAME;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.MAP_APP_DETAILS_KEY_CLIENT_ID;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.MAP_APP_DETAILS_KEY_CLIENT_SECRET;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.MAP_APP_DETAILS_KEY_ROLE_IDS_BY_NAME;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.MAP_APP_DETAILS_KEY_ROLE_NAMES;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.MAP_KEY_EXPECTED_ORG_COUNT;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.MAP_KEY_EXPECTED_ORG_IDS;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.MAP_KEY_EXPECTED_ORG_NAMES;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.MAP_KEY_EXPECTED_PER_ORG_SHARING_MODE;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.MAP_KEY_EXPECTED_ROLES_PER_EXPECTED_ORG;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.MAP_KEY_EXPECTED_TOP_LEVEL_SHARING_MODE;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.MAP_KEY_GENERAL_POLICY;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.MAP_KEY_GENERAL_ROLE_ASSIGNMENTS;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.MAP_KEY_SELECTIVE_ORG_ID;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.MAP_KEY_SELECTIVE_POLICY;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.MAP_KEY_SELECTIVE_ROLE_ASSIGNMENTS;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.MAP_ORG_DETAILS_KEY_ORG_ID;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.MAP_ORG_DETAILS_KEY_ORG_LEVEL;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.MAP_ORG_DETAILS_KEY_ORG_NAME;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.MAP_ORG_DETAILS_KEY_ORG_SWITCH_TOKEN;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.MAP_ORG_DETAILS_KEY_PARENT_ORG_ID;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.MAP_USER_DETAILS_KEY_DOMAIN_QUALIFIED_USER_NAME;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.MAP_USER_DETAILS_KEY_IS_ROOT_ORG_USER;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.MAP_USER_DETAILS_KEY_USER_DOMAIN;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.MAP_USER_DETAILS_KEY_USER_ID;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.MAP_USER_DETAILS_KEY_USER_NAME;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.MAP_USER_DETAILS_KEY_USER_ORG_ID;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.MAP_USER_DETAILS_KEY_USER_ORG_LEVEL;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.MAP_USER_DETAILS_KEY_USER_ORG_NAME;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.MAP_USER_DOMAIN_QUALIFIED_USER_NAME_USER_DOMAIN;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.MAP_USER_DOMAIN_QUALIFIED_USER_NAME_USER_NAME;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.ORGANIZATION_API_PATH;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.ORGANIZATION_AUDIENCE;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.PATCH_PATH_ORG_ROLES_FORMAT;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.PATH_SEPARATOR;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.QUERY_PARAM_ATTRIBUTES;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.QUERY_PARAM_FILTER;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.QUERY_PARAM_SEPARATOR;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.QUERY_PARAM_VALUE_SEPARATOR;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.RESPONSE_LINKS_EMPTY;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.RESPONSE_LINKS_SIZE;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.RESPONSE_ORGANIZATIONS;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.RESPONSE_ORGANIZATIONS_ORG_ID;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.RESPONSE_ORGANIZATIONS_ORG_NAME;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.RESPONSE_ORGANIZATIONS_SHARED_TYPE;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.RESPONSE_ORGANIZATIONS_SIZE;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.RESPONSE_PER_ORG_ROLES_AUDIENCE_DISPLAY_FORMAT;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.RESPONSE_PER_ORG_ROLES_AUDIENCE_TYPE_FORMAT;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.RESPONSE_PER_ORG_ROLES_DISPLAY_NAME_FORMAT;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.RESPONSE_PER_ORG_ROLES_FORMAT;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.RESPONSE_PER_ORG_ROLES_SIZE_FORMAT;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.RESPONSE_PER_ORG_SHARING_MODE_FORMAT;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.RESPONSE_PER_ORG_SHARING_MODE_POLICY_FORMAT;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.RESPONSE_PER_ORG_SHARING_MODE_RA_MODE_FORMAT;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.RESPONSE_PER_ORG_SHARING_MODE_RA_ROLES_FORMAT;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.RESPONSE_TOP_LEVEL_SHARING_MODE;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.RESPONSE_TOP_LEVEL_SHARING_MODE_POLICY;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.RESPONSE_TOP_LEVEL_SHARING_MODE_RA_MODE;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.RESPONSE_TOP_LEVEL_SHARING_MODE_RA_ROLES;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.ROOT_ORG_ID;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.ROOT_ORG_NAME;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.SHARED_ORGANIZATIONS_PATH;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.SHARED_TYPE_SHARED;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.SHARED_USER_ID_JSON_PATH;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.UNDERSCORE;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.USER_SHARING_API_BASE_PATH;
import static org.wso2.identity.integration.test.restclients.RestBaseClient.TENANT_PATH;
import static org.wso2.identity.integration.test.restclients.RestBaseClient.USER_AGENT_ATTRIBUTE;
import static org.awaitility.Awaitility.await;

/**
 * Base test class for the V2 User Sharing REST APIs.
 *
 * <p>Key V2 differences from V1 that are reflected here:
 * <ul>
 *   <li>The GET /shared-organizations endpoint is the single source of truth for both roles and
 *       sharingMode. There is no separate /shared-roles endpoint. Roles are returned inline via
 *       {@code ?attributes=roles} and sharingMode via {@code ?attributes=sharingMode}.</li>
 *   <li>Role assignment intent is expressed through a {@link RoleAssignments} object (mode +
 *       roles) instead of the flat role list used in V1.</li>
 *   <li>Selective share uses {@link SelectiveShareOrgDetails} with {@code orgId} and
 *       a reduced V2 policy enum (two values instead of four).</li>
 *   <li>A new PATCH endpoint allows role additions and removals on already-shared users. PATCH
 *       never modifies the ResourceSharingPolicy table, so
 *       {@code sharingMode.roleAssignment.roles} always reflects the original policy-time roles.</li>
 *   <li>The {@link ExpectedSharingMode} model drives sharingMode assertions. A {@code null}
 *       value signals that the field must be absent in the response; mode=NONE signals that
 *       {@code roleAssignment.roles} must be absent (assert with {@code nullValue()}, not
 *       {@code equalTo(emptyList())}).</li>
 * </ul>
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

    // =========================================================================
    // Request Sending Methods
    // =========================================================================

    protected HttpResponse getResponseOfPostToSubOrg(String path, String body, String token) throws Exception {

        HttpPost request = new HttpPost(
                serverURL + TENANT_PATH + tenant + ORGANIZATION_API_PATH + API_SERVER_V2_BASE_PATH + path);
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

    // =========================================================================
    // Organization Setup Methods
    // =========================================================================

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

    // =========================================================================
    // Application and Role Setup Methods
    // =========================================================================

    protected Map<String, Object> createApplication(String appName, String audience, List<String> roleNames)
            throws Exception {

        Map<String, Object> createdAppDetails = new HashMap<>();
        String rootOrgAppName = appName + UNDERSCORE + ROOT_ORG_NAME;

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

        updateRequestedClaimsOfApp(appId, getClaimConfigurationsWithRolesAndGroups());
        shareApplication(appId, appName);

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

    protected Map<String, String> setUpOrganizationRoles(String orgName, List<String> orgRoleNames)
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

    /**
     * Creates a {@link RoleWithAudience} using the nested audience structure matching the
     * {@code RoleShareConfig} schema in the V2 swagger spec:
     * {@code displayName} + {@code audience.display} / {@code audience.type}.
     *
     * @param roleName     The role display name (e.g. "app-role-1").
     * @param audienceName The audience display name (application name or organization name).
     * @param audienceType The audience type — either "application" or "organization".
     * @return A populated {@link RoleWithAudience}.
     */
    protected RoleWithAudience createRoleWithAudience(String roleName, String audienceName,
                                                       String audienceType) {

        RoleWithAudienceAudience audience = new RoleWithAudienceAudience();
        audience.setDisplay(audienceName);
        audience.setType(audienceType);

        RoleWithAudience roleWithAudience = new RoleWithAudience();
        roleWithAudience.setDisplayName(roleName);
        roleWithAudience.setAudience(audience);

        return roleWithAudience;
    }

    /**
     * Creates a {@link RoleAssignments} object with the given mode and roles list.
     *
     * <p>When {@code mode} is {@link RoleAssignments.ModeEnum#NONE}, pass an empty list for
     * {@code roles}. The GET response will omit the {@code roleAssignment.roles} field entirely
     * for NONE-mode shares; assertions must use {@code nullValue()}, not {@code equalTo(emptyList())}.
     *
     * @param mode  The assignment mode: {@code SELECTED} or {@code NONE}.
     * @param roles The roles to assign. Must be empty when mode is {@code NONE}.
     * @return A populated {@link RoleAssignments}.
     */
    protected RoleAssignments createRoleAssignments(RoleAssignments.ModeEnum mode, List<RoleWithAudience> roles) {

        RoleAssignments roleAssignments = new RoleAssignments();
        roleAssignments.setMode(mode);
        roleAssignments.setRoles(roles);
        return roleAssignments;
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

    private void shareApplication(String applicationId, String appName) throws Exception {

        ApplicationSharePOSTRequest applicationSharePOSTRequest = new ApplicationSharePOSTRequest();
        applicationSharePOSTRequest.setShareWithAllChildren(true);
        oAuth2RestClient.shareApplication(applicationId, applicationSharePOSTRequest);

        // Wait until the shared app is visible in all sub orgs before returning.
        for (Map<String, Object> orgDetail : orgDetails.values()) {
            String subOrgSwitchToken = (String) orgDetail.get(MAP_ORG_DETAILS_KEY_ORG_SWITCH_TOKEN);
            await().atMost(30, TimeUnit.SECONDS)
                    .pollInterval(2, TimeUnit.SECONDS)
                    .until(() -> StringUtils.isNotEmpty(
                            oAuth2RestClient.getAppIdUsingAppNameInOrganization(appName, subOrgSwitchToken)));
        }
    }

    // =========================================================================
    // User Setup Methods
    // =========================================================================

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

    // =========================================================================
    // Validation Orchestration Methods
    // =========================================================================

    /**
     * Validates the user sharing results using Awaitility (20 s timeout, 2 s poll interval).
     * Each user ID is validated independently against the provided expected results map.
     *
     * @param userIds         The list of user IDs to validate.
     * @param expectedResults The expected results map. Required keys:
     *                        {@code MAP_KEY_EXPECTED_ORG_COUNT},
     *                        {@code MAP_KEY_EXPECTED_ORG_IDS},
     *                        {@code MAP_KEY_EXPECTED_ORG_NAMES},
     *                        {@code MAP_KEY_EXPECTED_ROLES_PER_EXPECTED_ORG}.
     *                        Optional keys (V2-specific):
     *                        {@code MAP_KEY_EXPECTED_TOP_LEVEL_SHARING_MODE},
     *                        {@code MAP_KEY_EXPECTED_PER_ORG_SHARING_MODE}.
     * @throws Exception If validation does not pass within the timeout window.
     */
    protected void validateUserSharingResults(List<String> userIds, Map<String, Object> expectedResults)
            throws Exception {

        final Throwable[] lastException = {null};

        // Waits up to 20 seconds, checking every 2 seconds.
        await().atMost(20, TimeUnit.SECONDS)
                .pollInterval(2, TimeUnit.SECONDS)
                .ignoreExceptions()
                .until(() -> {
                    try {
                        for (String userId : userIds) {
                            validateUserHasBeenSharedToExpectedOrgsWithExpectedRoles(userId, expectedResults);
                        }
                        // If we reach here, all assertions passed
                        lastException[0] = null;
                        return true;
                    } catch (AssertionError | Exception e) {
                        // Catch the failure, save it, and return false to trigger the next poll
                        lastException[0] = e;
                        return false;
                    }
                });

        // If the 20 seconds run out and it still failed, throw the last caught exception
        if (lastException[0] != null) {
            Throwable t = lastException[0];
            if (t instanceof Error) {
                throw (Error) t;
            }
            throw (Exception) t;
        }
    }

    /**
     * Validates user sharing results and returns the list of shared user IDs found in the response.
     * Uses the same Awaitility window as {@link #validateUserSharingResults}.
     *
     * @param userIds                The list of user IDs to validate.
     * @param reSharingSubOrgDetails The org details map used to look up the org name for sharedUserId extraction.
     * @param expectedSharedResults  Expected results map (same shape as {@link #validateUserSharingResults}).
     * @return The shared user IDs extracted from {@code organizations[].sharedUserId} for each user.
     * @throws Exception If validation does not pass within the timeout window.
     */
    protected List<String> validateUserSharingResultsAndGetSharedUsersList(List<String> userIds,
                                                                           Map<String, Object> reSharingSubOrgDetails,
                                                                           Map<String, Object> expectedSharedResults)
            throws Exception {

        final Throwable[] lastException = {null};

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
            Throwable t = lastException[0];
            if (t instanceof Error) {
                throw (Error) t;
            }
            throw (Exception) t;
        }

        // Assertions have passed — extract sharedUserId values from a fresh GET call per user.
        List<String> sharedUserIds = new ArrayList<>();
        for (String userId : userIds) {
            Response sharedOrgsResponse =
                    getResponseOfGet(USER_SHARING_API_BASE_PATH + PATH_SEPARATOR + userId + SHARED_ORGANIZATIONS_PATH,
                            Collections.singletonMap(QUERY_PARAM_FILTER, ""));
            String sharedUserId = extractSharedUserId(sharedOrgsResponse,
                    reSharingSubOrgDetails.get(MAP_ORG_DETAILS_KEY_ORG_NAME).toString());
            sharedUserIds.add(sharedUserId);
        }
        return sharedUserIds;
    }

    /**
     * Validates that the given user has been shared to the expected organizations with the expected
     * inline roles and sharingMode by calling the V2 GET endpoint with
     * {@code ?attributes=roles,sharingMode}.
     *
     * <p>The {@code @SuppressWarnings("unchecked")} annotation is applied because all map values
     * are set by the test data providers with known types.
     *
     * @param userId          The ID of the user to validate.
     * @param expectedResults The expected results map (see {@link #validateUserSharingResults}).
     */
    @SuppressWarnings("unchecked")
    protected void validateUserHasBeenSharedToExpectedOrgsWithExpectedRoles(String userId,
                                                                            Map<String, Object> expectedResults) {

        int expectedOrgCount = (int) expectedResults.get(MAP_KEY_EXPECTED_ORG_COUNT);
        List<String> expectedOrgIds = (List<String>) expectedResults.get(MAP_KEY_EXPECTED_ORG_IDS);
        List<String> expectedOrgNames = (List<String>) expectedResults.get(MAP_KEY_EXPECTED_ORG_NAMES);
        Map<String, List<RoleWithAudience>> expectedRolesPerOrg =
                (Map<String, List<RoleWithAudience>>) expectedResults.get(MAP_KEY_EXPECTED_ROLES_PER_EXPECTED_ORG);
        ExpectedSharingMode expectedTopLevelSharingMode =
                (ExpectedSharingMode) expectedResults.get(MAP_KEY_EXPECTED_TOP_LEVEL_SHARING_MODE);
        Map<String, ExpectedSharingMode> expectedPerOrgSharingMode =
                (Map<String, ExpectedSharingMode>) expectedResults.get(MAP_KEY_EXPECTED_PER_ORG_SHARING_MODE);

        testGetSharedOrganizationsWithRolesAndSharingMode(userId, expectedOrgCount, expectedOrgIds, expectedOrgNames,
                expectedRolesPerOrg, expectedTopLevelSharingMode, expectedPerOrgSharingMode);
    }

    // =========================================================================
    // GET Validation — V2 Unified Endpoint
    // =========================================================================

    /**
     * Tests the V2 GET /users/{userId}/shared-organizations endpoint with
     * {@code ?attributes=roles,sharingMode}, asserting all fields in a single response.
     *
     * <p>V2 has no separate /shared-roles endpoint. Roles and sharingMode are returned inline in
     * the {@code organizations[]} array when the corresponding attribute values are requested.
     *
     * <p>The {@code @SuppressWarnings("unchecked")} annotation is applied because all map values
     * are set by the test data providers with known types.
     *
     * @param userId                    The root-org user ID to query.
     * @param expectedOrgCount          Expected total number of organizations in the response.
     * @param expectedOrgIds            Expected organization IDs present in {@code organizations[].orgId}.
     * @param expectedOrgNames          Expected organization names present in {@code organizations[].orgName}.
     * @param expectedRolesPerOrg       Map of orgId → expected role list for that org.
     *                                  Pass an empty list for orgs that should have no roles.
     * @param expectedTopLevelSharingMode  Expected top-level {@code sharingMode} object.
     *                                    {@code null} asserts the field is absent (selective share
     *                                    or SELECTED_ORG_ONLY policy).
     * @param expectedPerOrgSharingMode    Map of orgId → expected per-org {@code sharingMode}.
     *                                    A {@code null} value for a given orgId asserts the field is
     *                                    absent for that org. The outer map may be {@code null} when
     *                                    no per-org sharingMode is expected for any org.
     */
    protected void testGetSharedOrganizationsWithRolesAndSharingMode(
            String userId,
            int expectedOrgCount,
            List<String> expectedOrgIds,
            List<String> expectedOrgNames,
            Map<String, List<RoleWithAudience>> expectedRolesPerOrg,
            ExpectedSharingMode expectedTopLevelSharingMode,
            Map<String, ExpectedSharingMode> expectedPerOrgSharingMode) {

        Map<String, Object> queryParams = new LinkedHashMap<>();
        queryParams.put(QUERY_PARAM_FILTER, "");
        queryParams.put(QUERY_PARAM_ATTRIBUTES, ATTRIBUTE_ROLES + "," + ATTRIBUTE_SHARING_MODE);

        Response response = getResponseOfGet(
                USER_SHARING_API_BASE_PATH + PATH_SEPARATOR + userId + SHARED_ORGANIZATIONS_PATH, queryParams);

        // ---- Base assertions (org list, counts, sharedType) ----
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body(RESPONSE_ORGANIZATIONS, notNullValue())
                .body(RESPONSE_ORGANIZATIONS_SIZE, equalTo(expectedOrgCount));

        if (!expectedOrgIds.isEmpty()) {
            response.then()
                    .body(RESPONSE_ORGANIZATIONS_ORG_ID, hasItems(expectedOrgIds.toArray(new String[0])))
                    .body(RESPONSE_ORGANIZATIONS_ORG_NAME, hasItems(expectedOrgNames.toArray(new String[0])))
                    .body(RESPONSE_ORGANIZATIONS_SHARED_TYPE, everyItem(equalTo(SHARED_TYPE_SHARED)));
        }

        // ---- Top-level sharingMode (general share only) ----
        assertTopLevelSharingMode(response, expectedTopLevelSharingMode);

        // ---- Per-org inline roles and sharingMode ----
        for (String orgId : expectedOrgIds) {
            assertRolesForOrg(response, orgId, expectedRolesPerOrg.get(orgId));
            assertPerOrgSharingMode(response, orgId,
                    expectedPerOrgSharingMode != null ? expectedPerOrgSharingMode.get(orgId) : null);
        }
    }

    /**
     * Asserts the top-level {@code sharingMode} field in the GET response.
     *
     * <p>Pass {@code null} for {@code expected} to assert that the field is entirely absent —
     * which is correct for all selective shares and when {@code attributes} does not include
     * {@code sharingMode}.
     *
     * @param response The GET response to assert against.
     * @param expected The expected sharingMode descriptor, or {@code null} if field must be absent.
     */
    private void assertTopLevelSharingMode(Response response, ExpectedSharingMode expected) {

        if (expected == null) {
            response.then().body(RESPONSE_TOP_LEVEL_SHARING_MODE, nullValue());
            return;
        }

        response.then()
                .body(RESPONSE_TOP_LEVEL_SHARING_MODE_POLICY, equalTo(expected.getPolicy()))
                .body(RESPONSE_TOP_LEVEL_SHARING_MODE_RA_MODE, equalTo(expected.getRoleAssignmentMode()));

        if (expected.isNoneMode()) {
            // NONE mode: API returns an empty list for roles.
            response.then().body(RESPONSE_TOP_LEVEL_SHARING_MODE_RA_ROLES, equalTo(Collections.emptyList()));
        } else {
            List<RoleWithAudience> roles = expected.getRoleAssignmentRoles();
            if (roles != null && !roles.isEmpty()) {
                response.then()
                        .body(RESPONSE_TOP_LEVEL_SHARING_MODE_RA_ROLES + ".displayName",
                                hasItems(roles.stream()
                                        .map(RoleWithAudience::getDisplayName)
                                        .toArray(String[]::new)));
            }
        }
    }

    /**
     * Asserts the inline {@code roles[]} field for a specific organization in the GET response.
     *
     * <p>The roles are located via the Groovy path
     * {@code organizations.find { it.orgId == '<orgId>' }.roles}.
     *
     * @param response      The GET response to assert against.
     * @param orgId         The ID of the organization whose roles are asserted.
     * @param expectedRoles The expected role list for this org. Pass an empty list to assert no
     *                      roles are assigned. Must not be {@code null}.
     */
    private void assertRolesForOrg(Response response, String orgId, List<RoleWithAudience> expectedRoles) {

        String rolesPath = String.format(RESPONSE_PER_ORG_ROLES_FORMAT, orgId);
        String rolesSizePath = String.format(RESPONSE_PER_ORG_ROLES_SIZE_FORMAT, orgId);

        response.then()
                .body(rolesPath, notNullValue())
                .body(rolesSizePath, equalTo(expectedRoles.size()));

        if (!expectedRoles.isEmpty()) {
            response.then()
                    .body(String.format(RESPONSE_PER_ORG_ROLES_DISPLAY_NAME_FORMAT, orgId),
                            hasItems(expectedRoles.stream()
                                    .map(RoleWithAudience::getDisplayName)
                                    .toArray(String[]::new)))
                    .body(String.format(RESPONSE_PER_ORG_ROLES_AUDIENCE_DISPLAY_FORMAT, orgId),
                            hasItems(expectedRoles.stream()
                                    .map(r -> r.getAudience().getDisplay())
                                    .toArray(String[]::new)))
                    .body(String.format(RESPONSE_PER_ORG_ROLES_AUDIENCE_TYPE_FORMAT, orgId),
                            hasItems(expectedRoles.stream()
                                    .map(r -> r.getAudience().getType())
                                    .toArray(String[]::new)));
        }
    }

    /**
     * Asserts the per-org {@code sharingMode} field for a specific organization in the GET response.
     *
     * <p>The field is present only when {@code attributes} includes {@code sharingMode} AND this
     * org is the {@code policyHoldingOrgId} of a
     * {@code SELECTED_ORG_WITH_ALL_EXISTING_AND_FUTURE_CHILDREN} share. For all other orgs (child
     * orgs, SELECTED_ORG_ONLY orgs, and all orgs in a general share) the field must be absent.
     *
     * <p>Pass {@code null} for {@code expected} to assert absence. When mode is {@code NONE},
     * the API returns an empty list for {@code roleAssignment.roles} — asserted with
     * {@code equalTo(Collections.emptyList())}.
     *
     * @param response The GET response to assert against.
     * @param orgId    The ID of the organization to check.
     * @param expected The expected sharingMode descriptor, or {@code null} if field must be absent.
     */
    private void assertPerOrgSharingMode(Response response, String orgId, ExpectedSharingMode expected) {

        String sharingModePath = String.format(RESPONSE_PER_ORG_SHARING_MODE_FORMAT, orgId);

        if (expected == null) {
            response.then().body(sharingModePath, nullValue());
            return;
        }

        response.then()
                .body(String.format(RESPONSE_PER_ORG_SHARING_MODE_POLICY_FORMAT, orgId),
                        equalTo(expected.getPolicy()))
                .body(String.format(RESPONSE_PER_ORG_SHARING_MODE_RA_MODE_FORMAT, orgId),
                        equalTo(expected.getRoleAssignmentMode()));

        String raRolesPath = String.format(RESPONSE_PER_ORG_SHARING_MODE_RA_ROLES_FORMAT, orgId);

        if (expected.isNoneMode()) {
            // NONE mode: API returns an empty list for roles.
            response.then().body(raRolesPath, equalTo(Collections.emptyList()));
        } else {
            List<RoleWithAudience> roles = expected.getRoleAssignmentRoles();
            if (roles != null && !roles.isEmpty()) {
                response.then()
                        .body(raRolesPath + ".displayName",
                                hasItems(roles.stream()
                                        .map(RoleWithAudience::getDisplayName)
                                        .toArray(String[]::new)));
            }
        }
    }

    // =========================================================================
    // Request Body Builder Methods
    // =========================================================================

    /**
     * Creates a {@link UserShareRequestBodyUserCriteria} from the given user ID list.
     *
     * @param userIds The user IDs to include.
     * @return A populated criteria object.
     */
    protected UserShareRequestBodyUserCriteria getUserCriteriaForBaseUserSharing(List<String> userIds) {

        UserShareRequestBodyUserCriteria criteria = new UserShareRequestBodyUserCriteria();
        criteria.setUserIds(userIds);
        return criteria;
    }

    /**
     * Creates a {@link UserUnshareRequestBodyUserCriteria} from the given user ID list.
     *
     * @param userIds The user IDs to include.
     * @return A populated criteria object.
     */
    protected UserUnshareRequestBodyUserCriteria getUserCriteriaForBaseUserUnsharing(List<String> userIds) {

        UserUnshareRequestBodyUserCriteria criteria = new UserUnshareRequestBodyUserCriteria();
        criteria.setUserIds(userIds);
        return criteria;
    }

    /**
     * Converts a map of organization details into a list of {@link SelectiveShareOrgDetails} objects
     * for use in the V2 selective share request body.
     *
     * <p>Each map entry must contain keys:
     * <ul>
     *   <li>{@code MAP_KEY_SELECTIVE_ORG_ID} — the organization UUID.</li>
     *   <li>{@code MAP_KEY_SELECTIVE_POLICY} — a {@link SelectiveShareOrgDetails.PolicyEnum} value.</li>
     *   <li>{@code MAP_KEY_SELECTIVE_ROLE_ASSIGNMENTS} — a {@link RoleAssignments} object.</li>
     * </ul>
     *
     * <p>The {@code @SuppressWarnings("unchecked")} annotation is applied because all map values
     * are set by the test data providers with known types.
     *
     * @param organizations Map of org name → org detail map.
     * @return List of {@link SelectiveShareOrgDetails} ready for use in the request body.
     */
    @SuppressWarnings("unchecked")
    protected List<SelectiveShareOrgDetails> getOrganizationsForSelectiveUserSharing(
            Map<String, Map<String, Object>> organizations) {

        List<SelectiveShareOrgDetails> orgs = new ArrayList<>();

        for (Map.Entry<String, Map<String, Object>> entry : organizations.entrySet()) {
            Map<String, Object> orgDetail = entry.getValue();

            SelectiveShareOrgDetails org = new SelectiveShareOrgDetails();
            org.setOrgId((String) orgDetail.get(MAP_KEY_SELECTIVE_ORG_ID));
            org.setPolicy((SelectiveShareOrgDetails.PolicyEnum) orgDetail.get(MAP_KEY_SELECTIVE_POLICY));
            org.setRoleAssignment((RoleAssignments) orgDetail.get(MAP_KEY_SELECTIVE_ROLE_ASSIGNMENTS));

            orgs.add(org);
        }
        return orgs;
    }

    /**
     * Retrieves the general share policy enum from the provided policy map.
     *
     * @param policyWithRoleAssignments Map containing {@code MAP_KEY_GENERAL_POLICY}.
     * @return The {@link UserShareWithAllRequestBody.PolicyEnum} for the general share.
     */
    protected UserShareWithAllRequestBody.PolicyEnum getPolicyEnumForGeneralUserSharing(
            Map<String, Object> policyWithRoleAssignments) {

        return (UserShareWithAllRequestBody.PolicyEnum) policyWithRoleAssignments.get(MAP_KEY_GENERAL_POLICY);
    }

    /**
     * Retrieves the {@link RoleAssignments} object for general user sharing from the provided map.
     *
     * <p>In V2 this replaces the V1 {@code getRolesForGeneralUserSharing} method. The
     * {@link RoleAssignments} object encapsulates both the assignment mode (SELECTED or NONE) and
     * the role list, instead of the flat role list used in V1.
     *
     * @param policyWithRoleAssignments Map containing {@code MAP_KEY_GENERAL_ROLE_ASSIGNMENTS}.
     * @return The {@link RoleAssignments} for the general share.
     */
    protected RoleAssignments getRoleAssignmentsForGeneralUserSharing(
            Map<String, Object> policyWithRoleAssignments) {

        return (RoleAssignments) policyWithRoleAssignments.get(MAP_KEY_GENERAL_ROLE_ASSIGNMENTS);
    }

    /**
     * Returns the list of organization IDs for a selective unshare request body.
     * This is a pass-through but is kept as a named helper to match the V1 pattern and allow
     * future transformation if needed.
     *
     * @param removingOrgIds The organization IDs to unshare from.
     * @return The same list, usable directly as the {@code organizations} field.
     */
    protected List<String> getOrganizationsForSelectiveUserUnsharing(List<String> removingOrgIds) {

        return removingOrgIds;
    }

    /**
     * Builds a single {@link PatchOperation} targeting the {@code roles} of a specific organization.
     *
     * <p>The {@code path} is built from {@link
     * org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants#PATCH_PATH_ORG_ROLES_FORMAT}.
     *
     * <p>Behavioral notes:
     * <ul>
     *   <li>{@code ADD} is additive — existing roles are preserved.</li>
     *   <li>{@code REMOVE} removes only the specified roles; other roles are unaffected.</li>
     *   <li>Neither operation modifies the ResourceSharingPolicy table; {@code sharingMode} in
     *       the GET response always reflects the original policy-time roles.</li>
     * </ul>
     *
     * @param op    The operation type: {@link PatchOperation.OpEnum#ADD} or
     *              {@link PatchOperation.OpEnum#REMOVE}.
     * @param orgId The organization ID whose roles are modified.
     * @param roles The roles to add or remove.
     * @return A fully populated {@link PatchOperation}.
     */
    protected PatchOperation buildPatchOperation(PatchOperation.OpEnum op, String orgId,
                                                 List<RoleWithAudience> roles) {

        PatchOperation operation = new PatchOperation();
        operation.setOp(op);
        operation.setPath(String.format(PATCH_PATH_ORG_ROLES_FORMAT, orgId));
        operation.setValues(roles);
        return operation;
    }

    // =========================================================================
    // Cleanup Methods
    // =========================================================================

    /**
     * Deletes all users registered in {@code userDetails}. Root-org users are deleted directly;
     * sub-org users are deleted via their org's switch token.
     *
     * @throws Exception If any deletion fails.
     */
    protected void cleanUpUsers() throws Exception {

        if (userDetails == null) {
            return;
        }
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
     * Deletes all roles registered under the given audience types. Only roles whose audience name
     * contains {@code ROOT_ORG_NAME} are deleted (sub-org role copies are cleaned up automatically).
     *
     * <p>The {@code @SuppressWarnings("unchecked")} annotation is applied because the map values
     * are set by {@link #storeRoleDetails} with known types.
     *
     * @param audiences One or more of {@code APPLICATION_AUDIENCE} or {@code ORGANIZATION_AUDIENCE}.
     * @throws Exception If any deletion fails.
     */
    @SuppressWarnings("unchecked")
    protected void cleanUpRoles(String... audiences) throws Exception {

        for (String audience : audiences) {
            Map<String, Object> orgWiseRolesOfAudience = roleDetails.get(audience);
            if (orgWiseRolesOfAudience == null) {
                continue;
            }
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
     * Deletes all applications registered in {@code appDetails}.
     *
     * @throws Exception If any deletion fails.
     */
    protected void cleanUpApplications() throws Exception {

        for (Map.Entry<String, Map<String, Object>> entry : appDetails.entrySet()) {
            Map<String, Object> details = entry.getValue();
            deleteApplicationIfExists(details.get(MAP_APP_DETAILS_KEY_APP_ID).toString());
        }
    }

    /**
     * Deletes all organizations registered in {@code orgDetails}, starting from the deepest level
     * to avoid constraint violations on parent–child references.
     *
     * @throws Exception If any deletion fails.
     */
    protected void cleanUpOrganizations() throws Exception {

        int maxDepth = orgDetails.values().stream()
                .mapToInt(details -> (int) details.get(MAP_ORG_DETAILS_KEY_ORG_LEVEL))
                .max()
                .orElse(1);

        for (int level = maxDepth; level >= 1; level--) {
            for (Map.Entry<String, Map<String, Object>> entry : orgDetails.entrySet()) {
                if ((int) entry.getValue().get(MAP_ORG_DETAILS_KEY_ORG_LEVEL) == level) {
                    deleteOrganization(entry.getKey(), entry.getValue());
                }
            }
        }
    }

    /**
     * Clears all in-memory detail maps. Call this at the end of each test class lifecycle.
     */
    protected void cleanUpDetailMaps() {

        userDetails.clear();
        orgDetails.clear();
        appDetails.clear();
        roleDetails.clear();
    }

    /**
     * Closes the HTTP clients for OAuth2, SCIM2, and Organization Management.
     *
     * @throws IOException If any client fails to close.
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

    // =========================================================================
    // Helper Methods
    // =========================================================================

    /**
     * Extracts the {@code sharedUserId} value from the GET /shared-organizations response for the
     * organization identified by {@code orgName}.
     *
     * @param response The GET response.
     * @param orgName  The display name of the organization to look up.
     * @return The sharedUserId string, or {@code null} if the org is not found.
     */
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

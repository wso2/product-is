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
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.entity.StringEntity;
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
import org.wso2.identity.integration.test.rest.api.server.roles.v2.model.Permission;
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
import java.util.List;

import static org.wso2.identity.integration.test.restclients.RestBaseClient.USER_AGENT_ATTRIBUTE;

/**
 * Base test class for the User Sharing REST APIs.
 */
public class UserSharingBaseTest extends RESTAPIServerTestBase {

    private static final String API_DEFINITION_NAME = "organization-user-share.yaml";
    protected static final String AUTHORIZED_APIS_JSON = "user-sharing-apis.json";
    static final String API_VERSION = "v1";
    private static final String API_PACKAGE_NAME =
            "org.wso2.carbon.identity.api.server.organization.user.sharing.management.v1";

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

    protected static final String INTERNAL_USER_SHARE = "internal_user_share";
    protected static final String INTERNAL_USER_UNSHARE = "internal_user_unshare";
    protected static final String INTERNAL_USER_SHARED_ACCESS_VIEW = "internal_user_shared_access_view";
    protected static final String INTERNAL_ORG_USER_SHARE = "internal_org_user_share";
    protected static final String INTERNAL_ORG_USER_UNSHARE = "internal_org_user_unshare";
    protected static final String INTERNAL_ORG_USER_SHARED_ACCESS_VIEW = "internal_org_user_shared_access_view";

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

    protected static String swaggerDefinition;
    protected OAuth2RestClient oAuth2RestClient;
    protected SCIM2RestClient scim2RestClient;
    protected OrgMgtRestClient orgMgtRestClient;

    static {
        try {
            swaggerDefinition = getAPISwaggerDefinition(API_PACKAGE_NAME, API_DEFINITION_NAME);
        } catch (IOException e) {
            Assert.fail(String.format("Unable to read the swagger definition %s from %s", API_DEFINITION_NAME,
                    API_PACKAGE_NAME), e);
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

    protected String getAppClientId(String applicationId) throws Exception {

        OpenIDConnectConfiguration oidcConfig = oAuth2RestClient.getOIDCInboundDetails(applicationId);
        return oidcConfig.getClientId();
    }

    protected String getAppClientSecret(String applicationId) throws Exception {

        OpenIDConnectConfiguration oidcConfig = oAuth2RestClient.getOIDCInboundDetails(applicationId);
        return oidcConfig.getClientSecret();
    }

    protected HttpResponse sendGetRequest(String endpointURL, HttpClient client) throws IOException {

        HttpGet request = new HttpGet(endpointURL);
        request.setHeader(USER_AGENT_ATTRIBUTE, OAuth2Constant.USER_AGENT);
        return client.execute(request);
    }

    protected HttpResponse sendPostRequest(String endpointURL, List<NameValuePair> urlParameters, HttpClient client)
            throws IOException {

        HttpPost request = new HttpPost(endpointURL);
        request.setHeader(USER_AGENT_ATTRIBUTE, OAuth2Constant.USER_AGENT);
        request.setEntity(new UrlEncodedFormEntity(urlParameters));
        return client.execute(request);
    }

    protected HttpResponse sendPutRequest(String endpointURL, String body, HttpClient client) throws IOException {

        HttpPut request = new HttpPut(endpointURL);
        request.setHeader(USER_AGENT_ATTRIBUTE, OAuth2Constant.USER_AGENT);
        request.setHeader("Content-Type", "application/json");
        request.setEntity(new StringEntity(body));
        return client.execute(request);
    }

    protected HttpResponse sendDeleteRequest(String endpointURL, HttpClient client) throws IOException {

        HttpDelete request = new HttpDelete(endpointURL);
        request.setHeader(USER_AGENT_ATTRIBUTE, OAuth2Constant.USER_AGENT);
        return client.execute(request);
    }

    /**
     * Ged permissions based on the provided custom scopes.
     *
     * @return A list of permissions including predefined permissions
     */
    protected List<Permission> getPermissions() {

        List<Permission> userPermissions = new ArrayList<>();

        Collections.addAll(userPermissions,
                new Permission(INTERNAL_USER_SHARE),
                new Permission(INTERNAL_USER_UNSHARE),
                new Permission(INTERNAL_USER_SHARED_ACCESS_VIEW));

        return userPermissions;
    }

    protected List<String> getRoleV2Schema() {

        List<String> schemas = new ArrayList<>();
        schemas.add("urn:ietf:params:scim:schemas:extension:2.0:Role");
        return schemas;
    }

    protected ApplicationResponseModel addApplication(String appName) throws Exception {

        ApplicationModel application = new ApplicationModel();

        List<String> grantTypes = new ArrayList<>();
        Collections.addAll(grantTypes, "authorization_code", "implicit", "password", "client_credentials",
                "refresh_token", "organization_switch");

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
        emailClaim.setLocalClaim(
                new org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.Claim().uri(
                        EMAIL_CLAIM_URI));
        ClaimMappings countryClaim = new ClaimMappings().applicationClaim(COUNTRY_CLAIM_URI);
        countryClaim.setLocalClaim(
                new org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.Claim().uri(
                        COUNTRY_CLAIM_URI));

        RequestedClaimConfiguration emailRequestedClaim = new RequestedClaimConfiguration();
        emailRequestedClaim.setClaim(
                new org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.Claim().uri(
                        EMAIL_CLAIM_URI));
        RequestedClaimConfiguration countryRequestedClaim = new RequestedClaimConfiguration();
        countryRequestedClaim.setClaim(
                new org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.Claim().uri(
                        COUNTRY_CLAIM_URI));

        ClaimConfiguration claimConfiguration = new ClaimConfiguration().dialect(ClaimConfiguration.DialectEnum.CUSTOM);
        claimConfiguration.addClaimMappingsItem(emailClaim);
        claimConfiguration.addClaimMappingsItem(countryClaim);
        claimConfiguration.addRequestedClaimsItem(emailRequestedClaim);
        claimConfiguration.addRequestedClaimsItem(countryRequestedClaim);

        return claimConfiguration;
    }

    protected void shareApplication(String applicationId) throws Exception {

        ApplicationSharePOSTRequest applicationSharePOSTRequest = new ApplicationSharePOSTRequest();
        applicationSharePOSTRequest.setShareWithAllChildren(true);
        oAuth2RestClient.shareApplication(applicationId, applicationSharePOSTRequest);

        // Since application sharing is an async operation, wait for some time for it to finish.
        Thread.sleep(5000);
    }

    protected void switchApplicationAudience(String appId, AssociatedRolesConfig.AllowedAudienceEnum newAudience)
            throws Exception {

        AssociatedRolesConfig associatedRolesConfigApp2 = new AssociatedRolesConfig();
        associatedRolesConfigApp2.setAllowedAudience(newAudience);

        ApplicationPatchModel patchModelApp2 = new ApplicationPatchModel();
        patchModelApp2.setAssociatedRoles(associatedRolesConfigApp2);

        oAuth2RestClient.updateApplication(appId, patchModelApp2);
    }

    protected void updateRequestedClaimsOfApp(String applicationId, ClaimConfiguration claimConfigurationsForApp)
            throws IOException {

        ApplicationPatchModel applicationPatch = new ApplicationPatchModel();
        applicationPatch.setClaimConfiguration(claimConfigurationsForApp);
        oAuth2RestClient.updateApplication(applicationId, applicationPatch);
    }

    protected ClaimConfiguration getClaimConfigurationsWithRolesAndGroups() {

        ClaimConfiguration claimConfiguration = new ClaimConfiguration();
        claimConfiguration.addRequestedClaimsItem(getRequestedClaim(ROLES_CLAIM_URI));
        claimConfiguration.addRequestedClaimsItem(getRequestedClaim(GROUPS_CLAIM_URI));
        return claimConfiguration;
    }

    protected RequestedClaimConfiguration getRequestedClaim(String claimUri) {

        RequestedClaimConfiguration requestedClaim = new RequestedClaimConfiguration();
        requestedClaim.setClaim(
                new org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.Claim().uri(
                        claimUri));
        return requestedClaim;
    }

    protected static UserObject createUserObject(String userName, String orgName) {

        String domainQualifiedUserName = USER_DOMAIN_PRIMARY + "/" + userName;
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

    public String toJSONString(java.lang.Object object) {

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(object);
    }
}

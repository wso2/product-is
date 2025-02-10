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

package org.wso2.identity.integration.test.user.store;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.config.Lookup;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.cookie.CookieSpecProvider;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.RFC6265CookieSpecProvider;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.json.simple.JSONArray;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.identity.user.store.configuration.stub.dto.PropertyDTO;
import org.wso2.identity.integration.common.clients.Idp.IdentityProviderMgtServiceClient;
import org.wso2.identity.integration.common.utils.UserStoreConfigUtils;
import org.wso2.identity.integration.test.oauth2.OAuth2ServiceAbstractIntegrationTest;
import org.wso2.identity.integration.test.rest.api.common.RESTTestBase;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationResponseModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationSharePOSTRequest;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.OpenIDConnectConfiguration;
import org.wso2.identity.integration.test.rest.api.server.user.store.v1.model.UserStoreReq;
import org.wso2.identity.integration.test.rest.api.server.user.store.v1.model.UserStoreReq.Property;
import org.wso2.identity.integration.test.rest.api.user.common.model.GroupRequestObject;
import org.wso2.identity.integration.test.rest.api.user.common.model.UserObject;
import org.wso2.identity.integration.test.restclients.ClaimManagementRestClient;
import org.wso2.identity.integration.test.restclients.OAuth2RestClient;
import org.wso2.identity.integration.test.restclients.OrgMgtRestClient;
import org.wso2.identity.integration.test.restclients.SCIM2RestClient;
import org.wso2.identity.integration.test.restclients.UserStoreMgtRestClient;
import org.wso2.identity.integration.test.utils.DataExtractUtil;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.wso2.identity.integration.test.restclients.RestBaseClient.ORGANIZATION_PATH;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.ACCESS_TOKEN;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.ACCESS_TOKEN_ENDPOINT;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.AUTHORIZATION_CODE_NAME;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.AUTHORIZE_ENDPOINT_URL;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.COMMON_AUTH_URL;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.OAUTH2_CLIENT_ID;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.OAUTH2_RESPONSE_TYPE;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.REDIRECT_URI_NAME;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.SCOPE_PLAYGROUND_NAME;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.SESSION_DATA_KEY;

/**
 * Integration tests for secondary user store operations in sub organizations.
 */
public class OrganizationSecondaryUserStoreTestCase extends OAuth2ServiceAbstractIntegrationTest {

    private static final String AUTHORIZED_APIS_JSON = "authorized-apis.json";
    private static final String SUPER_TENANT_SUB_ORG_SECONDARY_USER_STORE = "SUPER_TENANT_SUB_ORG_SECONDARY_USER_STORE";
    private static final String TENANTED_SUB_ORG_SECONDARY_USER_STORE = "TENANTED_SUB_ORG_SECONDARY_USER_STORE";
    private static final String USER_STORE_TYPE = "VW5pcXVlSURKREJDVXNlclN0b3JlTWFuYWdlcg";
    private static final String DOMAIN_NAME = "JDBC";
    private static final String SECONDARY_USERNAME = "secondaryUser";
    private static final String SECONDARY_PASSWORD = "Wso2@123";
    private static final String SECONDARY_GROUP = "secondaryGroup";
    private static final String SUB_ORG_NAME = "subOrg";
    private static final String COMMON_AUTH_ENDPOINT = "/commonauth";
    private static final String CLIENT_SECRET_PARAM = "client_secret";
    private static final String USER_NAME = "userName";
    private static final String GROUPS = "groups";
    private static final String DISPLAY = "display";
    private static final String DISPLAY_NAME = "displayName";
    private static final String MEMBERS = "members";
    private static final String LOCAL_CLAIM_DIALECT = "local";
    private static final String CLAIM_UPDATE_JSON = "update-claim.json";
    private static final String PRIMARY_CLAIM_MAPPING_UPDATE_JSON = "update-claim-mapping-primary.json";
    private static final String CLAIM_MAPPING_UPDATE_JSON = "update-claim-mapping.json";
    public static final String ADDRESS_CLAIM_ID = "aHR0cDovL3dzbzIub3JnL2NsYWltcy9zdHJlZXRhZGRyZXNz";

    private final UserStoreConfigUtils userStoreConfigUtils = new UserStoreConfigUtils();
    private final CookieStore cookieStore = new BasicCookieStore();
    private final TestUserMode userMode;
    private UserStoreMgtRestClient userStoreMgtRestClient;
    private OAuth2RestClient oAuth2RestClient;
    private OrgMgtRestClient orgMgtRestClient;
    private SCIM2RestClient scim2RestClient;
    private ClaimManagementRestClient claimManagementRestClient;
    private IdentityProviderMgtServiceClient idpMgtServiceClient;
    private CloseableHttpClient client;
    private String switchedM2MToken;
    private ApplicationResponseModel application;
    private String clientId;
    private String clientSecret;
    private String userId;
    private String groupId;
    private String subOrgId;

    @DataProvider(name = "testExecutionContextProvider")
    public static Object[][] getTestExecutionContext() {

        return new Object[][]{
                {TestUserMode.SUPER_TENANT_ADMIN},
                {TestUserMode.TENANT_ADMIN}
        };
    }

    @Factory(dataProvider = "testExecutionContextProvider")
    public OrganizationSecondaryUserStoreTestCase(TestUserMode userMode) {

        this.userMode = userMode;
    }

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {

        super.init(userMode);

        Lookup<CookieSpecProvider> cookieSpecRegistry = RegistryBuilder.<CookieSpecProvider>create()
                .register(CookieSpecs.DEFAULT, new RFC6265CookieSpecProvider())
                .build();
        RequestConfig requestConfig = RequestConfig.custom()
                .setCookieSpec(CookieSpecs.DEFAULT)
                .build();
        client = HttpClientBuilder.create().disableRedirectHandling()
                .setDefaultRequestConfig(requestConfig)
                .setDefaultCookieStore(cookieStore)
                .setDefaultCookieSpecRegistry(cookieSpecRegistry)
                .build();

        userStoreMgtRestClient = new UserStoreMgtRestClient(serverURL, tenantInfo);
        oAuth2RestClient = new OAuth2RestClient(serverURL, tenantInfo);
        scim2RestClient = new SCIM2RestClient(serverURL, tenantInfo);
        claimManagementRestClient = new ClaimManagementRestClient(serverURL, tenantInfo);
        orgMgtRestClient = new OrgMgtRestClient(isServer, tenantInfo, serverURL,
                new JSONObject(RESTTestBase.readResource(AUTHORIZED_APIS_JSON, this.getClass())));

        application = addApplication();
        String applicationId = application.getId();
        OpenIDConnectConfiguration oidcConfig = getOIDCInboundDetailsOfApplication(applicationId);
        clientId = oidcConfig.getClientId();
        clientSecret = oidcConfig.getClientSecret();
        shareApplication();

        subOrgId = orgMgtRestClient.addOrganization(SUB_ORG_NAME);
        switchedM2MToken = orgMgtRestClient.switchM2MToken(subOrgId);

        ConfigurationContext configContext =
                ConfigurationContextFactory.createConfigurationContextFromFileSystem(null, null);
        idpMgtServiceClient = new IdentityProviderMgtServiceClient(sessionCookie, backendURL, configContext);
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {

        deleteApp(application.getId());
        orgMgtRestClient.deleteOrganization(subOrgId);
        idpMgtServiceClient.deleteIdP("SSO");
        orgMgtRestClient.closeHttpClient();
        scim2RestClient.closeHttpClient();
        oAuth2RestClient.closeHttpClient();
        userStoreMgtRestClient.closeHttpClient();
        claimManagementRestClient.closeHttpClient();
        client.close();
    }

    @Test(description = "Add a secondary JDBC user store to a sub organization.")
    public void testAddSecondaryJDBCUserStore() throws Exception {

        PropertyDTO[] userStoreProperties = getJDBCUserStoreProperties();
        UserStoreReq userStoreReq = new UserStoreReq().typeId(USER_STORE_TYPE).name(DOMAIN_NAME);
        for (PropertyDTO propertyDTO : userStoreProperties) {
            userStoreReq.addPropertiesItem(new Property().name(propertyDTO.getName()).value(propertyDTO.getValue()));
        }

        userStoreMgtRestClient.addSubOrgUserStore(userStoreReq, switchedM2MToken);
        boolean isUserStoreDeployed =
                userStoreMgtRestClient.waitForSubOrgUserStoreDeployment(DOMAIN_NAME, switchedM2MToken);
        Assert.assertTrue(isUserStoreDeployed, "Secondary JDBC user store is not deployed.");
    }

    @Test(dependsOnMethods = {"testAddSecondaryJDBCUserStore"},
            description = "Add a user to the secondary JDBC user store in the sub organization.")
    public void testAddUserSecondaryJDBCUserStore() throws Exception {

        userId = scim2RestClient.createSubOrgUser(
                new UserObject().userName(DOMAIN_NAME + "/" + SECONDARY_USERNAME).password(SECONDARY_PASSWORD),
                switchedM2MToken);
        Assert.assertNotNull(userId, "Failed to add user to the secondary JDBC user store.");
    }

    @Test(dependsOnMethods = {"testAddUserSecondaryJDBCUserStore"},
            description = "Add a group to the secondary JDBC user store and assign a user.")
    public void testAddGroupSecondaryJDBCUserStore() throws Exception {

        groupId = scim2RestClient.createSubOrgGroup(
                new GroupRequestObject().displayName(DOMAIN_NAME + "/" + SECONDARY_GROUP)
                        .addMember(new GroupRequestObject.MemberItem().value(userId)), switchedM2MToken);
        Assert.assertNotNull(groupId, "Failed to add group to the secondary JDBC user store.");
    }

    @Test(dependsOnMethods = {"testAddGroupSecondaryJDBCUserStore"},
            description = "Get a sub organization user in the secondary JDBC user store.")
    public void testGetUserSecondaryJDBCUserStore() throws Exception {

        org.json.simple.JSONObject userResponse = scim2RestClient.getSubOrgUser(userId, switchedM2MToken);

        // Assert username.
        Assert.assertNotNull(userResponse.get(USER_NAME), "User name is null.");
        Assert.assertEquals(userResponse.get(USER_NAME), DOMAIN_NAME + "/" + SECONDARY_USERNAME,
                "Unexpected user name.");

        // Assert assigned group.
        Assert.assertNotNull(userResponse.get(GROUPS), "Groups are null.");
        Assert.assertEquals(((JSONArray) userResponse.get(GROUPS)).size(), 1,
                "Unexpected number of groups.");

        org.json.simple.JSONObject userGroup =
                (org.json.simple.JSONObject) ((JSONArray) userResponse.get(GROUPS)).get(0);
        Assert.assertEquals(userGroup.get(DISPLAY),
                DOMAIN_NAME + "/" + SECONDARY_GROUP,
                "Unexpected group name.");
    }

    @Test(dependsOnMethods = {"testGetUserSecondaryJDBCUserStore"},
            description = "Get a sub organization group in the secondary JDBC user store.")
    public void testGetGroupSecondaryJDBCUserStore() throws Exception {

        org.json.simple.JSONObject groupResponse = scim2RestClient.getSubOrgGroup(groupId, switchedM2MToken);

        // Assert group name.
        Assert.assertNotNull(groupResponse.get(DISPLAY_NAME), "Group name is null.");
        Assert.assertEquals(groupResponse.get(DISPLAY_NAME), DOMAIN_NAME + "/" + SECONDARY_GROUP,
                "Unexpected group name.");

        // Assert assigned user.
        Assert.assertNotNull(groupResponse.get(MEMBERS), "Members are null.");
        Assert.assertEquals(((JSONArray) groupResponse.get(MEMBERS)).size(), 1,
                "Unexpected number of members.");
        org.json.simple.JSONObject groupUser =
                (org.json.simple.JSONObject) ((JSONArray) groupResponse.get(MEMBERS)).get(0);
        Assert.assertEquals(groupUser.get(DISPLAY), DOMAIN_NAME + "/" + SECONDARY_USERNAME,
                "Unexpected member name.");
    }

    @Test(dependsOnMethods = {"testGetGroupSecondaryJDBCUserStore"},
            description = "Get an access token from a user in the secondary JDBC user store.")
    public void testGetAccessTokenSecondaryJDBCUserStore() throws Exception {

        String sessionDataKey = initiateAuthRequest();
        Assert.assertNotNull(sessionDataKey, "Session data key is null.");
        String subOrgSessionDataKey = getSubOrgSessionDataKey(sessionDataKey);

        String code = getAuthorizationCode(subOrgSessionDataKey);
        Assert.assertNotNull(code, "Authorization code is null.");

        String accessToken = getAccessToken(code);
        Assert.assertNotNull(accessToken, "Access token is null.");
    }

    @Test(dependsOnMethods = {"testGetAccessTokenSecondaryJDBCUserStore"},
            description = "Update the general claim details of a claim in the sub organization.")
    public void testUpdateSubOrgClaim() throws Exception {

        String claimUpdateRequestBody = RESTTestBase.readResource(CLAIM_UPDATE_JSON, this.getClass());
        int responseCode = claimManagementRestClient.updateSubOrgClaim(LOCAL_CLAIM_DIALECT, ADDRESS_CLAIM_ID,
                claimUpdateRequestBody, switchedM2MToken);
        Assert.assertEquals(responseCode, HttpStatus.SC_FORBIDDEN,
                "Claim property update should be restricted for sub organizations.");
    }

    @Test(dependsOnMethods = {"testUpdateSubOrgClaim"},
            description = "Update the claim mapping for the primary user store in a sub organization claim.")
    public void testUpdateSubOrgClaimMappingPrimaryUserStore() throws Exception {

        String claimUpdateRequestBody = RESTTestBase.readResource(PRIMARY_CLAIM_MAPPING_UPDATE_JSON, this.getClass());
        int responseCode = claimManagementRestClient.updateSubOrgClaim(LOCAL_CLAIM_DIALECT, ADDRESS_CLAIM_ID,
                claimUpdateRequestBody, switchedM2MToken);
        Assert.assertEquals(responseCode, HttpStatus.SC_FORBIDDEN,
                "Claim mapping update of PRIMARY user store should be restricted for sub organizations.");
    }

    @Test(dependsOnMethods = {"testUpdateSubOrgClaimMappingPrimaryUserStore"},
            description = "Update the claim mapping for the primary user store in a sub organization claim.")
    public void testUpdateSubOrgClaimMappingSecondaryUserStore() throws Exception {

        String claimUpdateRequestBody = RESTTestBase.readResource(CLAIM_MAPPING_UPDATE_JSON, this.getClass());
        int responseCode = claimManagementRestClient.updateSubOrgClaim(LOCAL_CLAIM_DIALECT, ADDRESS_CLAIM_ID,
                claimUpdateRequestBody, switchedM2MToken);
        Assert.assertEquals(responseCode, HttpStatus.SC_OK,
                "Secondary user store claim mapping update should be successful for sub organizations.");
    }

    @Test(dependsOnMethods = {"testGetAccessTokenSecondaryJDBCUserStore"},
            description = "Delete the group from the secondary JDBC user store.")
    public void testDeleteGroupSecondaryJDBCUserStore() throws Exception {

        scim2RestClient.deleteSubOrgGroup(groupId, switchedM2MToken);
    }

    @Test(dependsOnMethods = {"testDeleteGroupSecondaryJDBCUserStore"},
            description = "Delete the user from the secondary JDBC user store.")
    public void testDeleteUserSecondaryJDBCUserStore() throws Exception {

        scim2RestClient.deleteSubOrgUser(userId, switchedM2MToken);
    }

    @Test(dependsOnMethods = {"testDeleteUserSecondaryJDBCUserStore"},
            description = "Delete the secondary JDBC user store from the sub organization.")
    public void testDeleteSecondaryJDBCUserStore() throws Exception {

        userStoreMgtRestClient.deleteSubOrgUserStore(DOMAIN_NAME, switchedM2MToken);
    }

    private PropertyDTO[] getJDBCUserStoreProperties() throws IOException, SQLException, ClassNotFoundException {

        if (userMode == TestUserMode.SUPER_TENANT_ADMIN) {
            return userStoreConfigUtils.getJDBCUserStoreProperties(SUPER_TENANT_SUB_ORG_SECONDARY_USER_STORE);
        }

        return userStoreConfigUtils.getJDBCUserStoreProperties(TENANTED_SUB_ORG_SECONDARY_USER_STORE);
    }

    private String getAccessToken(String authCode) throws Exception {

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.GRANT_TYPE_NAME,
                OAuth2Constant.OAUTH2_GRANT_TYPE_AUTHORIZATION_CODE));
        urlParameters.add(new BasicNameValuePair(AUTHORIZATION_CODE_NAME, authCode));
        urlParameters.add(new BasicNameValuePair(REDIRECT_URI_NAME, OAuth2Constant.CALLBACK_URL));
        urlParameters.add(new BasicNameValuePair(OAUTH2_CLIENT_ID, clientId));
        urlParameters.add(new BasicNameValuePair(CLIENT_SECRET_PARAM, clientSecret));

        HttpResponse response = sendPostRequestWithParameters(client, urlParameters,
                getTenantQualifiedURL(ACCESS_TOKEN_ENDPOINT, tenantInfo.getDomain()));
        Assert.assertNotNull(response, "Access token response is null");
        Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_OK,
                "Unexpected response status code.");

        JSONObject jsonObject = new JSONObject(EntityUtils.toString(response.getEntity()));
        EntityUtils.consume(response.getEntity());
        return jsonObject.getString(ACCESS_TOKEN);
    }

    private String getAuthorizationCode(String sessionDataKey) throws Exception {

        String commonAuthURL = serverURL + ORGANIZATION_PATH + subOrgId + COMMON_AUTH_ENDPOINT;
        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair(SESSION_DATA_KEY, sessionDataKey));
        urlParameters.add(new BasicNameValuePair("username", DOMAIN_NAME + "/" + SECONDARY_USERNAME));
        urlParameters.add(new BasicNameValuePair("password", SECONDARY_PASSWORD));

        // Sub organization commonauth request.
        HttpResponse response = sendPostRequestWithParameters(client, urlParameters, commonAuthURL);
        Assert.assertNotNull(response, "Sub organization commonauth response is null");
        Header locationHeader = getLocationHeader(response);

        // Sub organization auth request.
        response = sendGetRequest(client, locationHeader.getValue());
        Assert.assertNotNull(response, "Sub organization authorized response is null");
        locationHeader = getLocationHeader(response);

        // Root organization commonauth request.
        response = sendGetRequest(client, locationHeader.getValue());
        Assert.assertNotNull(response, "Root organization commonauth response is null");
        locationHeader = getLocationHeader(response);

        // Root organization auth request.
        response = sendGetRequest(client, locationHeader.getValue());
        Assert.assertNotNull(response, "Root organization authorized response is null");
        locationHeader = getLocationHeader(response);
        Map<String, String> queryParams = extractQueryParams(locationHeader.getValue());
        return queryParams.get("code");
    }

    private String getSubOrgSessionDataKey(String parentSessionDataKey) throws Exception {

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair(SESSION_DATA_KEY, parentSessionDataKey));
        urlParameters.add(new BasicNameValuePair("org", SUB_ORG_NAME));
        urlParameters.add(new BasicNameValuePair("idp", "SSO"));
        urlParameters.add(new BasicNameValuePair("authenticator", "OrganizationAuthenticator"));

        // Common auth request.
        HttpResponse response = sendPostRequestWithParameters(client, urlParameters,
                getTenantQualifiedURL(COMMON_AUTH_URL, tenantInfo.getDomain()));
        Assert.assertNotNull(response, "Authorized response is null");
        Header locationHeader = getLocationHeader(response);

        // Sub organization authorize request.
        response = sendGetRequest(client, locationHeader.getValue());
        Assert.assertNotNull(response, "Authorized user response is null.");
        locationHeader = getLocationHeader(response);

        // Sub organization login page.
        response = sendGetRequest(client, locationHeader.getValue());
        Map<String, Integer> keyPositionMap = new HashMap<>(1);
        keyPositionMap.put("name=\"sessionDataKey\"", 1);
        List<DataExtractUtil.KeyValue> keyValues =
                DataExtractUtil.extractDataFromResponse(response, keyPositionMap);
        Assert.assertNotNull(keyValues, "sessionDataKey key value is null");
        String sessionDataKey = keyValues.get(0).getValue();
        EntityUtils.consume(response.getEntity());
        return sessionDataKey;
    }

    private String initiateAuthRequest() throws Exception {

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair(OAUTH2_RESPONSE_TYPE, OAuth2Constant.OAUTH2_GRANT_TYPE_CODE));
        urlParameters.add(new BasicNameValuePair(OAUTH2_CLIENT_ID, clientId));
        urlParameters.add(new BasicNameValuePair(REDIRECT_URI_NAME, OAuth2Constant.CALLBACK_URL));
        urlParameters.add(new BasicNameValuePair(SCOPE_PLAYGROUND_NAME, ""));
        urlParameters.add(new BasicNameValuePair("fidp", "OrganizationSSO"));

        HttpResponse response = sendPostRequestWithParameters(client, urlParameters,
                getTenantQualifiedURL(AUTHORIZE_ENDPOINT_URL, tenantInfo.getDomain()));
        Assert.assertNotNull(response, "Authorized response is null");
        Header locationHeader = getLocationHeader(response);
        Map<String, String> queryParams = extractQueryParams(locationHeader.getValue());
        return queryParams.get(SESSION_DATA_KEY);
    }

    private static Header getLocationHeader(HttpResponse response) throws IOException {

        Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_MOVED_TEMPORARILY,
                "Unexpected response status code.");
        Header locationHeader =
                response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        Assert.assertNotNull(locationHeader, "Location header is null");
        EntityUtils.consume(response.getEntity());
        return locationHeader;
    }

    private Map<String, String> extractQueryParams(String url) throws Exception {

        Map<String, String> queryParams = new HashMap<>();
        List<NameValuePair> params = URLEncodedUtils.parse(new URI(url), StandardCharsets.UTF_8);
        if (params.isEmpty()) {
            return queryParams;
        }

        for (NameValuePair param : params) {
            queryParams.put(param.getName(), param.getValue());
        }

        return queryParams;
    }

    private void shareApplication() throws Exception {

        ApplicationSharePOSTRequest applicationSharePOSTRequest = new ApplicationSharePOSTRequest();
        applicationSharePOSTRequest.setShareWithAllChildren(true);
        oAuth2RestClient.shareApplication(application.getId(), applicationSharePOSTRequest);
    }
}

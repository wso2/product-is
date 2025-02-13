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
import org.apache.http.message.BasicNameValuePair;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.AdvancedApplicationConfiguration;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationSharePOSTRequest;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.InboundProtocols;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.OpenIDConnectConfiguration;
import org.wso2.identity.integration.test.rest.api.server.common.RESTAPIServerTestBase;
import org.wso2.identity.integration.test.rest.api.server.roles.v2.model.Permission;
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

    protected static final String USER_ID = "userId";
    protected static final String ORG_ID = "orgId";
    protected static final String LIMIT_QUERY_PARAM = "limit";
    protected static final String AFTER_QUERY_PARAM = "after";
    protected static final String BEFORE_QUERY_PARAM = "before";
    protected static final String FILTER_QUERY_PARAM = "filter";
    protected static final String RECURSIVE_QUERY_PARAM = "recursive";

    protected static final String ERROR_CODE_BAD_REQUEST = "UE-10000";
    protected static final String ERROR_CODE_INVALID_PAGINATION_CURSOR = "ORG-60026";
    protected static final String ERROR_CODE_SERVER_ERROR = "SE-50000";

    protected static final String ROOT_ORG_NAME = "Root - Organization";
    protected static final String L1_ORG_1_NAME = "L1 - Organization 1";
    protected static final String L1_ORG_2_NAME = "L1 - Organization 2";
    protected static final String L2_ORG_1_NAME = "L2 - Organization 1";
    protected static final String L2_ORG_2_NAME = "L2 - Organization 2";
    protected static final String L2_ORG_3_NAME = "L2 - Organization 3";
    protected static final String L3_ORG_1_NAME = "L3 - Organization 1";

    protected static final String APP_1 = "App 1";
    protected static final String APP_2 = "App 2";

    protected static final String SUPER_ORG = "Super";

    protected static final String APPLICATION_AUDIENCE = "APPLICATION";
    protected static final String ORGANIZATION_AUDIENCE = "ORGANIZATION";

    protected static final String APP_ROLE_1 = "app-role-1";
    protected static final String ORG_ROLE_1 = "org-role-1";

    protected static final String ROOT_ORG_USERNAME = "rootUser";
    protected static final String L1_ORG_1_USERNAME = "l1Org1User";

    protected static final String INTERNAL_USER_SHARE = "internal_user_share";
    protected static final String INTERNAL_USER_UNSHARE = "internal_user_unshare";
    protected static final String INTERNAL_USER_SHARED_ACCESS_VIEW = "internal_user_shared_access_view";
    protected static final String INTERNAL_ORG_USER_SHARE = "internal_org_user_share";
    protected static final String INTERNAL_ORG_USER_UNSHARE = "internal_org_user_unshare";
    protected static final String INTERNAL_ORG_USER_SHARED_ACCESS_VIEW = "internal_org_user_shared_access_view";

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

    /**
     * Get the schema for the roles v2.
     *
     * @return A list of schemas
     */
    protected List<String> getRoleV2Schema() {

        List<String> schemas = new ArrayList<>();
        schemas.add("urn:ietf:params:scim:schemas:extension:2.0:Role");
        return schemas;
    }

    /**
     * Create an application with the given name and predefined OAuth2 configurations.
     *
     * @param appName The name of the application.
     * @return The unique identifier of the created application.
     * @throws Exception If an error occurs while creating the application.
     */
    protected String addApplication(String appName) throws Exception {

        ApplicationModel application = new ApplicationModel();
        List<String> grantTypes = new ArrayList<>();
        Collections.addAll(grantTypes, OAuth2Constant.OAUTH2_GRANT_TYPE_AUTHORIZATION_CODE);
        OpenIDConnectConfiguration oidcConfig = new OpenIDConnectConfiguration();
        oidcConfig.setGrantTypes(grantTypes);
        oidcConfig.setCallbackURLs(Collections.singletonList(OAuth2Constant.CALLBACK_URL));
        InboundProtocols inboundProtocolsConfig = new InboundProtocols();
        inboundProtocolsConfig.setOidc(oidcConfig);
        application.setInboundProtocolConfiguration(inboundProtocolsConfig);
        application.setName(appName);

        AdvancedApplicationConfiguration advancedApplicationConfiguration = new AdvancedApplicationConfiguration();
        advancedApplicationConfiguration.setSkipLoginConsent(true);
        application.setAdvancedConfigurations(advancedApplicationConfiguration);

        String applicationID = oAuth2RestClient.createApplication(application);
        Assert.assertNotNull(applicationID, "Application id cannot be empty.");
        return applicationID;
    }

    /**
     * Share the specified application with all organizations.
     *
     * @param appId The unique identifier of the application to be shared.
     * @throws Exception If an error occurs while sharing the application.
     */
    protected void shareApplicationWithAllOrgs(String appId) throws Exception {

        ApplicationSharePOSTRequest applicationSharePOSTRequest = new ApplicationSharePOSTRequest();
        applicationSharePOSTRequest.setShareWithAllChildren(true);
        oAuth2RestClient.shareApplication(appId, applicationSharePOSTRequest);

        // Since application sharing is an async operation, wait for some time for it to finish.
        Thread.sleep(5000);
    }

    /**
     * To convert object to a json string.
     *
     * @param object Respective java object.
     * @return Relevant json string.
     */
    public String toJSONString(java.lang.Object object) {

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(object);
    }
}

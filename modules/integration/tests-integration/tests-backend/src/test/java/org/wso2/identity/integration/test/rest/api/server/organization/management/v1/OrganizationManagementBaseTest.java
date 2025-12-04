/*
 * Copyright (c) 2023-2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.identity.integration.test.rest.api.server.organization.management.v1;

import io.restassured.RestAssured;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.wso2.identity.integration.test.rest.api.server.api.resource.v1.model.APIResourceListItem;
import org.wso2.identity.integration.test.rest.api.server.api.resource.v1.model.ScopeGetModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.AdvancedApplicationConfiguration;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationSharePOSTRequest;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.AuthorizedAPICreationModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.InboundProtocols;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.OpenIDConnectConfiguration;
import org.wso2.identity.integration.test.rest.api.server.common.RESTAPIServerTestBase;
import org.wso2.identity.integration.test.rest.api.user.common.model.Email;
import org.wso2.identity.integration.test.rest.api.user.common.model.Name;
import org.wso2.identity.integration.test.rest.api.user.common.model.UserObject;
import org.wso2.identity.integration.test.restclients.OAuth2RestClient;
import org.wso2.identity.integration.test.restclients.SCIM2RestClient;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;

import static org.wso2.identity.integration.test.restclients.RestBaseClient.USER_AGENT_ATTRIBUTE;

/**
 * Base test class for the Organization Management REST APIs.
 */
public class OrganizationManagementBaseTest extends RESTAPIServerTestBase {

    private static final String API_DEFINITION_NAME = "org.wso2.carbon.identity.organization.management.yaml";
    static final String API_VERSION = "v1";

    static final String ORGANIZATION_MANAGEMENT_API_BASE_PATH = "/organizations";
    static final String ORGANIZATION_CONFIGS_API_BASE_PATH = "/organization-configs";
    static final String ORGANIZATION_DISCOVERY_API_PATH = "/discovery";
    static final String ORGANIZATION_META_ATTRIBUTES_API_PATH = "/meta-attributes";
    static final String CHECK_HANDLE_API_PATH = "/check-handle";
    static final String ORGANIZATION_META_DATA_PATH = "/metadata";
    static final String CHECK_DISCOVERY_API_PATH = "/check-discovery";
    static final String AUTHORIZE_ENDPOINT = "oauth2/authorize";
    static final String TOKEN_ENDPOINT = "oauth2/token";
    static final String INTROSPECT_ENDPOINT = "oauth2/introspect";
    static final String COMMON_AUTH_ENDPOINT = "/commonauth";
    static final String PATH_SEPARATOR = "/";
    static final String SELF_ENDPOINT = "/self";

    protected static final String ORGANIZATION_ID = "id";
    protected static final String ORGANIZATION_NAME = "name";
    protected static final String ORGANIZATION_HANDLE = "orgHandle";
    protected static final String HAS_CHILDREN = "hasChildren";
    protected static final String ANCESTOR_PATH = "ancestorPath";
    protected static final String ORGANIZATION_NAME_FORMAT = "Org-%d";

    protected static final String ORGANIZATION_EMAIL_FORMAT_1 = "org%d.com";
    protected static final String ORGANIZATION_EMAIL_FORMAT_2 = "organization%d.com";

    protected static final String LIMIT_QUERY_PARAM = "limit";
    protected static final String AFTER_QUERY_PARAM = "after";
    protected static final String BEFORE_QUERY_PARAM = "before";
    protected static final String RECURSIVE_QUERY_PARAM = "recursive";
    protected static final String OFFSET_QUERY_PARAM = "offset";
    protected static final String FILTER_QUERY_PARAM = "filter";
    public static final String FIDP_QUERY_PARAM = "fidp";
    protected static final String LOGIN_HINT_QUERY_PARAM = "login_hint";
    protected static final String ORG_DISCOVERY_TYPE_QUERY_PARAM = "orgDiscoveryType";
    protected static final String AUTH_FAILURE_MSG_QUERY_PARAM = "authFailureMsg";
    protected static final String AVAILABLE = "available";
    protected static final String LIMIT_10_QUERY_PARAM = "10";
    protected static final String LIMIT_MINUS_1_QUERY_PARAM = "-1";

    protected static final String ORGANIZATIONS_PATH_PARAM = "organizations";
    protected static final String LINKS_PATH_PARAM = "links";
    protected static final String COUNT_PATH_PARAM = "count";
    protected static final String TOTAL_RESULTS_PATH_PARAM = "totalResults";
    protected static final String START_INDEX_PATH_PARAM = "startIndex";

    protected static final String ORGANIZATION_NAME_ATTRIBUTE = "organizationName";
    protected static final String ORGANIZATION_MULTIPLE_META_ATTRIBUTE_ATTRIBUTES = "attributes";

    protected static final String CLIENT_SECRET_PARAM = "client_secret";
    protected static final String USERNAME_PARAM = "username";
    protected static final String PASSWORD_PARAM = "password";
    protected static final String SESSION_DATA_KEY_PARAM = "sessionDataKey";

    protected static final String LINK_REL_PREVIOUS = "previous";
    protected static final String LINK_REL_NEXT = "next";
    protected static final String REL = "rel";
    protected static final String HREF = "href";

    protected static final String AMPERSAND = "&";
    protected static final String QUESTION_MARK = "?";
    protected static final String EQUAL = "=";
    protected static final String PLUS = "+";
    protected static final String COLON = ":";

    protected static final String ZERO = "0";

    protected static final String FALSE = "false";

    protected static final String TOTAL_RESULT_MISMATCH_ERROR = "Total results mismatched.";
    protected static final String START_INDEX_MISMATCH_ERROR = "Start index mismatched.";
    protected static final String COUNT_MISMATCH_ERROR = "Count mismatch";

    protected static final int NUM_OF_ORGANIZATIONS_FOR_PAGINATION_TESTS = 20;
    protected static final int DEFAULT_ORG_LIMIT = 15;
    protected static final int NUM_OF_ORGANIZATIONS_WITH_META_ATTRIBUTES = 3;
    protected static final int DEFAULT_META_ATTRIBUTES_LIMIT = 15;

    public static final String ORGANIZATION_SSO = "OrganizationSSO";
    protected static final String EMAIL_DOMAIN_DISCOVERY = "emailDomain";
    protected static final String B2B_APP_NAME = "Guardio-Business-App";
    protected static final String B2B_USER_NAME = "John";
    protected static final String B2B_USER_PASSWORD = "Test@1234";
    protected static final String B2B_USER_EMAIL = "johndoe@abc.com";

    protected static final String ORG_HANDLE_CENTRAL_HOSPITAL = "centralhospital.com";
    protected static final String ORG_NAME_LITTLE_HOSPITAL = "Little Hospital";
    protected static final String ORG_NAME_CENTRAL_HOSPITAL = "Central Hospital";
    protected static final String ORG_NAME_MAIN_HOSPITAL = "Main Hospital";
    protected static final String ORG_HANDLE_PLACEHOLDER = "${orgHandle}";
    protected static final String NEW_ORG_NAME_PLACEHOLDER = "${newOrganizationName}";

    protected static final String EMPTY_REQUEST_BODY = "{}";
    protected static final String RENAME_ORGANIZATION_REQUEST_BODY = "rename-organization-request-body.json";
    protected static final String ORGANIZATION_UPDATE_REQUEST_BODY
            = "update-hospital-organization-request-body.json";

    protected static final String ORGANIZATION_ID_PLACEHOLDER = "${organizationID}";
    protected static final String PARENT_ID_PLACEHOLDER = "${parentId}";

    protected static final String ADD_DISCOVERY_ATTRIBUTES_REQUEST_BODY = "add-discovery-attributes-request-body.json";
    protected static final String ORGANIZATION_SELF_SERVICE_APIS = "organization-self-service-apis.json";
    protected static final String ADD_DISCOVERY_CONFIG_REQUEST_BODY = "add-discovery-config-request-body.json";
    protected static final String UPDATE_DISCOVERY_ATTRIBUTES_REQUEST_BODY
            = "update-discovery-attributes-request-body.json";
    protected static final String ADD_SMALLER_HOSPITAL_ORGANIZATION_REQUEST_BODY
            = "add-smaller-hospital-organization-request-body.json";
    protected static final String ADD_GREATER_HOSPITAL_ORGANIZATION_REQUEST_BODY
            = "add-greater-hospital-organization-request-body.json";
    protected static final String ADD_ORGANIZATION_WITH_HANDLE_REQUEST_BODY
            = "add-organization-with-handle-request-body.json";
    protected static final String CHECK_DISCOVERY_ATTRIBUTES_AVAILABLE_REQUEST_BODY
            = "check-discovery-attributes-available-request-body.json";
    protected static final String CHECK_DISCOVERY_ATTRIBUTES_UNAVAILABLE_REQUEST_BODY
            = "check-discovery-attributes-unavailable-request-body.json";

    protected static String swaggerDefinition;
    protected OAuth2RestClient oAuth2RestClient;
    protected SCIM2RestClient scim2RestClient;

    static {
        String API_PACKAGE_NAME = "org.wso2.carbon.identity.api.server.organization.management.v1";

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

    protected String getSubOrgAppClientId(String applicationId, String switchedToken) throws Exception {

        OpenIDConnectConfiguration oidcConfig = oAuth2RestClient.getOIDCInboundDetailsForSubOrgApplications(
                applicationId, switchedToken);
        return oidcConfig.getClientId();
    }

    protected String getAppClientSecret(String applicationId) throws Exception {

        OpenIDConnectConfiguration oidcConfig = oAuth2RestClient.getOIDCInboundDetails(applicationId);
        return oidcConfig.getClientSecret();
    }

    protected String getSubOrgAppClientSecret(String applicationId, String switchedM2MToken) throws Exception {

        OpenIDConnectConfiguration oidcConfig = oAuth2RestClient.getOIDCInboundDetailsForSubOrgApplications(
                applicationId, switchedM2MToken);
        return oidcConfig.getClientSecret();
    }

    protected String buildGetRequestURL(String endpointURL, String tenantDomain, List<NameValuePair> queryParams) {

        String authorizeEndpoint = getTenantQualifiedURL(endpointURL, tenantDomain);

        if (queryParams == null || queryParams.isEmpty()) {
            return authorizeEndpoint;
        }

        StringJoiner queryParamJoiner = new StringJoiner(AMPERSAND);
        for (NameValuePair queryParam : queryParams) {
            queryParamJoiner.add(queryParam.getName() + EQUAL + queryParam.getValue());
        }

        return authorizeEndpoint + QUESTION_MARK + queryParamJoiner;
    }

    protected HttpResponse sendGetRequest(String endpointURL, HttpClient client) throws IOException {

        HttpGet request = new HttpGet(endpointURL);
        request.setHeader(USER_AGENT_ATTRIBUTE, OAuth2Constant.USER_AGENT);
        return client.execute(request);
    }

    protected HttpResponse sendPostRequest(String commonAuthURL, List<NameValuePair> urlParameters, HttpClient client)
            throws IOException {

        HttpPost request = new HttpPost(commonAuthURL);
        request.setHeader(USER_AGENT_ATTRIBUTE, OAuth2Constant.USER_AGENT);
        request.setEntity(new UrlEncodedFormEntity(urlParameters));
        return client.execute(request);
    }

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

    protected void shareApplication(String applicationID) throws IOException {

        ApplicationSharePOSTRequest applicationSharePOSTRequest = new ApplicationSharePOSTRequest();
        applicationSharePOSTRequest.setShareWithAllChildren(true);
        oAuth2RestClient.shareApplication(applicationID, applicationSharePOSTRequest);
    }

    protected void unShareApplication(String applicationID) throws IOException {

        ApplicationSharePOSTRequest applicationSharePOSTRequest = new ApplicationSharePOSTRequest();
        applicationSharePOSTRequest.setShareWithAllChildren(false);
        oAuth2RestClient.shareApplication(applicationID, applicationSharePOSTRequest);
    }

    protected String createB2BUser(String switchedM2MToken) throws Exception {

        UserObject userInfo = new UserObject();
        userInfo.setUserName(B2B_USER_EMAIL);
        userInfo.setPassword(B2B_USER_PASSWORD);
        userInfo.setName(new Name().givenName(B2B_USER_NAME));
        userInfo.addEmail(new Email().value(B2B_USER_EMAIL));

        String b2bUserID = scim2RestClient.createSubOrgUser(userInfo, switchedM2MToken);
        Assert.assertNotNull(b2bUserID, "B2B user creation failed.");
        return b2bUserID;
    }

    /**
     * Authorize list of SYSTEM APIs to an application registered in sub organization.
     *
     * @param applicationId  Application id.
     * @param apiIdentifiers API identifiers to authorize.
     * @throws Exception Error occured while authorizing APIs.
     */
    public void authorizeSystemAPIsToSubOrganizationApp(OAuth2RestClient restClient, String applicationId, List<String> apiIdentifiers,
                                                        String switchedM2MToken) {

        apiIdentifiers.stream().forEach(apiIdentifier -> {
            try {
                List<APIResourceListItem> filteredAPIResource =
                        restClient.getAPIResourcesWithFilteringFromSubOrganization("identifier+eq+" + apiIdentifier,
                                switchedM2MToken);
                if (filteredAPIResource == null || filteredAPIResource.isEmpty()) {
                    return;
                }
                String apiId = filteredAPIResource.get(0).getId();
                // Get API scopes.
                List<ScopeGetModel> apiResourceScopes = restClient.getAPIResourceScopesInSubOrganization(apiId,
                        switchedM2MToken);
                AuthorizedAPICreationModel authorizedAPICreationModel = new AuthorizedAPICreationModel();
                authorizedAPICreationModel.setId(apiId);
                authorizedAPICreationModel.setPolicyIdentifier("RBAC");
                apiResourceScopes.forEach(scope -> {
                    authorizedAPICreationModel.addScopesItem(scope.getName());
                });
                restClient.addAPIAuthorizationToSubOrgApplication(applicationId, authorizedAPICreationModel,
                        switchedM2MToken);
            } catch (Exception e) {
                throw new RuntimeException("Error while authorizing system API " + apiIdentifier + " to application "
                        + applicationId, e);
            }
        });
    }
}

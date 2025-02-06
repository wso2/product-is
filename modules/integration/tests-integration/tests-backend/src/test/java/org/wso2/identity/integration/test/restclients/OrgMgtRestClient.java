/*
 * Copyright (c) 2024-2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.identity.integration.test.restclients;

import com.nimbusds.oauth2.sdk.AccessTokenResponse;
import com.nimbusds.oauth2.sdk.AuthorizationGrant;
import com.nimbusds.oauth2.sdk.ClientCredentialsGrant;
import com.nimbusds.oauth2.sdk.Scope;
import com.nimbusds.oauth2.sdk.TokenRequest;
import com.nimbusds.oauth2.sdk.TokenResponse;
import com.nimbusds.oauth2.sdk.auth.ClientAuthentication;
import com.nimbusds.oauth2.sdk.auth.ClientSecretBasic;
import com.nimbusds.oauth2.sdk.auth.Secret;
import com.nimbusds.oauth2.sdk.http.HTTPResponse;
import com.nimbusds.oauth2.sdk.id.ClientID;
import io.restassured.http.ContentType;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.testng.Assert;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.beans.Tenant;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationSharePOSTRequest;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.OpenIDConnectConfiguration;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.testng.Assert.assertEquals;

/**
 * Rest client for the organization management REST APIs.
 */
public class OrgMgtRestClient extends RestBaseClient {

    private static final String API_SERVER_BASE_PATH = "api/server/v1";
    private static final String APPLICATION_MANAGEMENT_PATH = "/applications";
    private static final String ORGANIZATION_MANAGEMENT_PATH = "/organizations";
    private static final String API_RESOURCE_MANAGEMENT_PATH = "/api-resources";
    private static final String AUTHORIZED_APIS_PATH = "/authorized-apis";
    private static final String B2B_APP_NAME = "b2b-app";
    private static final String API_RESOURCES = "apiResources";
    private static final String ID = "id";
    private static final String POLICY_IDENTIFIER = "policyIdentifier";
    private static final String RBAC_POLICY = "RBAC";
    private static final String SCOPES = "scopes";

    private final OAuth2RestClient oAuth2RestClient;
    private final Tenant tenantInfo;
    private final String baseUrl;
    private final String applicationManagementApiBasePath;
    private final String organizationManagementApiBasePath;
    private final String subOrganizationManagementApiBasePath;
    private final String apiResourceManagementApiBasePath;
    private final String authenticatingUserName;
    private final String authenticatingCredential;

    private String b2bAppId;
    private String b2bAppClientId;
    private String b2bAppClientSecret;

    public OrgMgtRestClient(AutomationContext context, Tenant tenantInfo, String baseUrl, JSONObject authorizedAPIs)
            throws Exception {

        this.oAuth2RestClient = new OAuth2RestClient(baseUrl, tenantInfo);
        this.authenticatingUserName = context.getContextTenant().getTenantAdmin().getUserName();
        this.authenticatingCredential = context.getContextTenant().getTenantAdmin().getPassword();
        this.tenantInfo = tenantInfo;
        this.baseUrl = baseUrl;

        this.applicationManagementApiBasePath =
                buildPath(baseUrl, tenantInfo.getDomain(), APPLICATION_MANAGEMENT_PATH);
        this.organizationManagementApiBasePath =
                buildPath(baseUrl, tenantInfo.getDomain(), ORGANIZATION_MANAGEMENT_PATH);
        this.apiResourceManagementApiBasePath =
                buildPath(baseUrl, tenantInfo.getDomain(), API_RESOURCE_MANAGEMENT_PATH);
        this.subOrganizationManagementApiBasePath =
                buildSubOrgPath(baseUrl, tenantInfo.getDomain(), ORGANIZATION_MANAGEMENT_PATH);

        createB2BApplication(authorizedAPIs);
    }

    /**
     * Add an organization within the root organization.
     *
     * @param orgName Name of the organization.
     * @return ID of the created organization.
     * @throws Exception If an error occurs while creating the organization.
     */
    public String addOrganization(String orgName) throws Exception {

        String m2mToken = getM2MAccessToken();
        String body = buildOrgCreationRequestBody(orgName, null);
        try (CloseableHttpResponse response = getResponseOfHttpPost(organizationManagementApiBasePath, body,
                getHeadersWithBearerToken(m2mToken))) {
            String[] locationElements = response.getHeaders(LOCATION_HEADER)[0].toString().split(PATH_SEPARATOR);
            return locationElements[locationElements.length - 1];
        }
    }

    /**
     * Add an organization within another organization.
     *
     * @param orgName     Name of the sub-organization.
     * @param parentOrgId ID of the parent organization.
     * @return ID of the created sub-organization.
     * @throws Exception If an error occurs while creating the sub-organization.
     */
    public String addSubOrganization(String orgName, String parentOrgId) throws Exception {

        String m2mToken = switchM2MToken(parentOrgId);
        String body = buildOrgCreationRequestBody(orgName, parentOrgId);
        try (CloseableHttpResponse response = getResponseOfHttpPost(subOrganizationManagementApiBasePath, body,
                getHeadersWithBearerToken(m2mToken))) {
            String[] locationElements = response.getHeaders(LOCATION_HEADER)[0].toString().split(PATH_SEPARATOR);
            return locationElements[locationElements.length - 1];
        }
    }

    /**
     * Delete an organization within root organization.
     *
     * @param orgId ID of the organization.
     * @throws Exception If an error occurs while deleting the organization.
     */
    public void deleteOrganization(String orgId) throws Exception {

        String m2mToken = getM2MAccessToken();
        try (CloseableHttpResponse response = getResponseOfHttpDelete(organizationManagementApiBasePath +
                PATH_SEPARATOR + orgId, getHeadersWithBearerToken(m2mToken))) {
            assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_NO_CONTENT,
                    "Organization deletion failed for organization with ID: " + orgId);
        }
    }

    /**
     * Delete an organization within another organization.
     *
     * @param orgId       ID of the organization that needs to be deleted.
     * @param parentOrgId ID of the parent organization.
     * @throws Exception If an error occurs while deleting the organization.
     */
    public void deleteSubOrganization(String orgId, String parentOrgId) throws Exception {

        String m2mToken = switchM2MToken(parentOrgId);
        try (CloseableHttpResponse response = getResponseOfHttpDelete(subOrganizationManagementApiBasePath +
                PATH_SEPARATOR + orgId, getHeadersWithBearerToken(m2mToken))) {
            assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_NO_CONTENT,
                    "Sub-organization deletion failed for organization with ID: " + orgId);
        }
    }

    /**
     * Get an M2M access token from the B2B application.
     *
     * @return Access token of the B2B application.
     * @throws Exception If an error occurs while retrieving the access token.
     */
    public String getM2MAccessToken() throws Exception {

        OpenIDConnectConfiguration openIDConnectConfiguration =
                oAuth2RestClient.getOIDCInboundDetails(b2bAppId);

        URI tokenEndpoint = new URI(getTenantQualifiedURL(OAuth2Constant.ACCESS_TOKEN_ENDPOINT,
                tenantInfo.getDomain()));
        TokenRequest request = getTokenRequest(tokenEndpoint, openIDConnectConfiguration);
        HTTPResponse tokenHTTPResp = request.toHTTPRequest().send();
        TokenResponse tokenResponse = TokenResponse.parse(tokenHTTPResp);
        AccessTokenResponse accessTokenResponse = tokenResponse.toSuccessResponse();

        return accessTokenResponse.getTokens().getAccessToken().getValue();
    }

    /**
     * Retrieve a switched M2M token for the given organization.
     *
     * @param organizationID ID of the organization.
     * @return Switched M2M token for the given organization.
     * @throws Exception If an error occurs while switching the M2M token.
     */
    public String switchM2MToken(String organizationID) throws Exception {

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.GRANT_TYPE_NAME, "organization_switch"));
        urlParameters.add(new BasicNameValuePair("token", getM2MAccessToken()));
        urlParameters.add(new BasicNameValuePair("scope", "SYSTEM"));
        urlParameters.add(new BasicNameValuePair("switching_organization", organizationID));

        HttpPost httpPost =
                new HttpPost(getTenantQualifiedURL(OAuth2Constant.ACCESS_TOKEN_ENDPOINT, tenantInfo.getDomain()));
        httpPost.setHeader(AUTHORIZATION_ATTRIBUTE, BASIC_AUTHORIZATION_ATTRIBUTE + new String(Base64.encodeBase64(
                (b2bAppClientId + ":" + b2bAppClientSecret).getBytes())));
        httpPost.setHeader(CONTENT_TYPE_ATTRIBUTE, "application/x-www-form-urlencoded");
        httpPost.setEntity(new UrlEncodedFormEntity(urlParameters));

        HttpResponse response = client.execute(httpPost);
        String responseBody = EntityUtils.toString(response.getEntity(), "UTF-8");
        EntityUtils.consume(response.getEntity());

        JSONParser parser = new JSONParser();
        org.json.simple.JSONObject responseJSONBody = (org.json.simple.JSONObject) parser.parse(responseBody);
        Assert.assertNotNull(responseJSONBody, "Access token response is null.");
        Assert.assertNotNull(responseJSONBody.get(OAuth2Constant.ACCESS_TOKEN), "Access token is null.");

        return (String) responseJSONBody.get(OAuth2Constant.ACCESS_TOKEN);
    }

    /**
     * Authorize the given API for the B2B application.
     * Note: Use this method only for api resources in which api resource identifier cannot be used for filtering.
     * For other APIs, authorize APIs using {@link #OrgMgtRestClient(AutomationContext, Tenant, String, JSONObject)}.
     *
     * @param apiName Name of the API.
     * @param type    Type of the API.
     * @param scopes  Scopes to be authorized.
     * @throws Exception If an error occurs while authorizing the API.
     */
    public void authorizeAPIForB2BApp(String apiName, String type, List<String> scopes) throws Exception {

        String apiUUID;
        try (CloseableHttpResponse apiResourceResponse = getResponseOfHttpGet(
                apiResourceManagementApiBasePath + "?filter=name+eq+" + URLEncoder.encode(apiName) + "+and+type+eq+" +
                        type,
                getHeaders())) {
            JSONObject apiResourceResponseBody =
                    new JSONObject(EntityUtils.toString(apiResourceResponse.getEntity()));
            apiUUID = apiResourceResponseBody.getJSONArray(API_RESOURCES).getJSONObject(0).getString(ID);
        }

        JSONArray requiredScopes = new JSONArray();
        scopes.forEach(requiredScopes::put);

        JSONObject authorizedAPIRequestBody = new JSONObject();
        authorizedAPIRequestBody.put(ID, apiUUID);
        authorizedAPIRequestBody.put(POLICY_IDENTIFIER, RBAC_POLICY);
        authorizedAPIRequestBody.put(SCOPES, requiredScopes);

        try (CloseableHttpResponse appAuthorizedAPIsResponse = getResponseOfHttpPost(
                applicationManagementApiBasePath + PATH_SEPARATOR + b2bAppId + AUTHORIZED_APIS_PATH,
                authorizedAPIRequestBody.toString(), getHeaders())) {
            assertEquals(appAuthorizedAPIsResponse.getStatusLine().getStatusCode(), HttpStatus.SC_OK,
                    String.format("API authorization failed for the application with ID: %s for API: %s.",
                            b2bAppId, apiName));
        }
    }

    private void createB2BApplication(JSONObject authorizedAPIs)
            throws IOException, JSONException, InterruptedException {

        String body = getB2BAppCreationRequestBody();

        try (CloseableHttpResponse appCreationResponse = getResponseOfHttpPost(applicationManagementApiBasePath, body,
                getHeaders())) {
            String[] locationElements =
                    appCreationResponse.getHeaders(LOCATION_HEADER)[0].toString().split(PATH_SEPARATOR);
            b2bAppId = locationElements[locationElements.length - 1];
        }

        // Authorize necessary APIs for app.
        updateB2BAppAuthorizedAPIs(authorizedAPIs);

        // Share the B2B app with all child organizations.
        ApplicationSharePOSTRequest applicationSharePOSTRequest = new ApplicationSharePOSTRequest();
        applicationSharePOSTRequest.setShareWithAllChildren(true);
        oAuth2RestClient.shareApplication(b2bAppId, applicationSharePOSTRequest);

        // Since application sharing is an async operation, wait for some time for it to finish.
        Thread.sleep(5000);
    }

    private void updateB2BAppAuthorizedAPIs(JSONObject authorizedAPIs) throws JSONException, IOException {

        for (Iterator<String> apiNameIterator = authorizedAPIs.keys(); apiNameIterator.hasNext(); ) {
            String apiName = apiNameIterator.next();
            Object requiredScopes = authorizedAPIs.get(apiName);

            String apiUUID;
            try (CloseableHttpResponse apiResourceResponse = getResponseOfHttpGet(
                    apiResourceManagementApiBasePath + "?filter=identifier+eq+" + apiName,
                    getHeaders())) {
                JSONObject apiResourceResponseBody =
                        new JSONObject(EntityUtils.toString(apiResourceResponse.getEntity()));
                apiUUID = apiResourceResponseBody.getJSONArray(API_RESOURCES).getJSONObject(0).getString(ID);
            }

            JSONObject authorizedAPIRequestBody = new JSONObject();
            authorizedAPIRequestBody.put(ID, apiUUID);
            authorizedAPIRequestBody.put(POLICY_IDENTIFIER, RBAC_POLICY);
            authorizedAPIRequestBody.put(SCOPES, requiredScopes);

            try (CloseableHttpResponse appAuthorizedAPIsResponse = getResponseOfHttpPost(
                    applicationManagementApiBasePath + PATH_SEPARATOR + b2bAppId + AUTHORIZED_APIS_PATH,
                    authorizedAPIRequestBody.toString(), getHeaders())) {
                assertEquals(appAuthorizedAPIsResponse.getStatusLine().getStatusCode(), HttpStatus.SC_OK,
                        String.format("Authorized APIs update failed for application with ID: %s for API: %s.",
                                b2bAppId, apiName));
            }
        }
    }

    private String getB2BAppCreationRequestBody() throws JSONException {

        JSONObject oidc = new JSONObject();
        oidc.put("grantTypes", new String[]{"client_credentials", "organization_switch"});

        JSONObject inboundProtocolConfiguration = new JSONObject();
        inboundProtocolConfiguration.put("oidc", oidc);

        JSONObject b2bSelfServiceApp = new JSONObject();
        b2bSelfServiceApp.put("name", B2B_APP_NAME);
        b2bSelfServiceApp.put("templateId", "custom-application-oidc");
        b2bSelfServiceApp.put("inboundProtocolConfiguration", inboundProtocolConfiguration);

        return b2bSelfServiceApp.toString();
    }

    private String buildOrgCreationRequestBody(String orgName, String parentOrgId) throws JSONException {

        JSONObject organization = new JSONObject();
        organization.put("name", orgName);
        if (StringUtils.isNotBlank(parentOrgId)) {
            organization.put("parentId", parentOrgId);
        }
        return organization.toString();
    }

    private TokenRequest getTokenRequest(URI tokenEndpoint,
                                         OpenIDConnectConfiguration openIDConnectConfiguration) {

        b2bAppClientId = openIDConnectConfiguration.getClientId();
        b2bAppClientSecret = openIDConnectConfiguration.getClientSecret();
        ClientID clientID = new ClientID(b2bAppClientId);
        Secret clientSecret = new Secret(b2bAppClientSecret);
        ClientAuthentication clientAuth = new ClientSecretBasic(clientID, clientSecret);

        AuthorizationGrant clientCredentialsGrant = new ClientCredentialsGrant();
        Scope scope = new Scope("SYSTEM");

        return new TokenRequest(tokenEndpoint, clientAuth, clientCredentialsGrant, scope);
    }

    private String getTenantQualifiedURL(String endpointURL, String tenantDomain) {

        if (!tenantDomain.isBlank() && !tenantDomain.equalsIgnoreCase(
                MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {

            endpointURL = endpointURL.replace(baseUrl,
                    baseUrl + TENANT_PATH + tenantDomain + PATH_SEPARATOR);
        }
        return endpointURL;
    }

    private Header[] getHeaders() {

        Header[] headerList = new Header[3];
        headerList[0] = new BasicHeader(USER_AGENT_ATTRIBUTE, OAuth2Constant.USER_AGENT);
        headerList[1] = new BasicHeader(AUTHORIZATION_ATTRIBUTE, BASIC_AUTHORIZATION_ATTRIBUTE +
                Base64.encodeBase64String((authenticatingUserName + ":" + authenticatingCredential).getBytes()).trim());
        headerList[2] = new BasicHeader(CONTENT_TYPE_ATTRIBUTE, String.valueOf(ContentType.JSON));

        return headerList;
    }

    private Header[] getHeadersWithBearerToken(String accessToken) {

        Header[] headerList = new Header[3];
        headerList[0] = new BasicHeader(USER_AGENT_ATTRIBUTE, OAuth2Constant.USER_AGENT);
        headerList[1] = new BasicHeader(AUTHORIZATION_ATTRIBUTE, BEARER_TOKEN_AUTHORIZATION_ATTRIBUTE +
                accessToken);
        headerList[2] = new BasicHeader(CONTENT_TYPE_ATTRIBUTE, String.valueOf(ContentType.JSON));

        return headerList;
    }

    private String buildPath(String serverUrl, String tenantDomain, String endpointURL) {

        if (tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
            return serverUrl + API_SERVER_BASE_PATH + endpointURL;
        }
        return serverUrl + TENANT_PATH + tenantDomain + PATH_SEPARATOR + API_SERVER_BASE_PATH + endpointURL;
    }

    private String buildSubOrgPath(String serverUrl, String tenantDomain, String endpointURL) {

        if (tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
            return serverUrl + ORGANIZATION_PATH + API_SERVER_BASE_PATH + endpointURL;
        }
        return serverUrl + TENANT_PATH + tenantDomain + PATH_SEPARATOR + ORGANIZATION_PATH + API_SERVER_BASE_PATH +
                endpointURL;
    }

    /**
     * Close the HTTP client.
     *
     * @throws IOException If an error occurred while closing the Http Client.
     */
    public void closeHttpClient() throws IOException {

        oAuth2RestClient.deleteApplication(b2bAppId);
        oAuth2RestClient.closeHttpClient();
        client.close();
    }
}

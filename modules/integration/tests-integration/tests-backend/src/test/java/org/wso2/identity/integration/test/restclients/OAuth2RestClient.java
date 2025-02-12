/*
 * Copyright (c) 2023-2024, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.identity.integration.test.restclients;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.http.ContentType;
import java.util.Map;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.testng.Assert;
import org.wso2.carbon.automation.engine.context.beans.Tenant;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.identity.integration.test.rest.api.server.api.resource.v1.model.APIResourceListItem;
import org.wso2.identity.integration.test.rest.api.server.api.resource.v1.model.APIResourceListResponse;
import org.wso2.identity.integration.test.rest.api.server.api.resource.v1.model.APIResourcePatchModel;
import org.wso2.identity.integration.test.rest.api.server.api.resource.v1.model.APIResourceResponse;
import org.wso2.identity.integration.test.rest.api.server.api.resource.v1.model.AuthorizationDetailsType;
import org.wso2.identity.integration.test.rest.api.server.api.resource.v1.model.ScopeGetModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationListItem;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationListResponse;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.AuthorizedAPIPatchModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.AuthorizedAPIResponse;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.DomainAPICreationModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationPatchModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationResponseModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationSharePOSTRequest;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.AuthorizedAPICreationModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.AuthorizedDomainAPIResponse;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.OpenIDConnectConfiguration;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.SAML2ServiceProvider;
import org.wso2.identity.integration.test.rest.api.server.roles.v2.model.RoleV2;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.wso2.identity.integration.test.utils.CarbonUtils.isLegacyAuthzRuntimeEnabled;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.AUTHORIZE_ENDPOINT_URL;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.OAUTH2_CLIENT_ID;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.OAUTH2_RESPONSE_TYPE;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.REDIRECT_URI_NAME;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.SCOPE_PLAYGROUND_NAME;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.SESSION_DATA_KEY;

public class OAuth2RestClient extends RestBaseClient {

    private static final String API_SERVER_BASE_PATH = "api/server/v1";
    private static final String SCIM_V2_PATH = "scim2/v2";
    private static final String APPLICATION_MANAGEMENT_PATH = "/applications";
    private static final String API_RESOURCE_MANAGEMENT_PATH = "/api-resources";
    private static final String INBOUND_PROTOCOLS_BASE_PATH = "/inbound-protocols";
    private static final String AUTHORIZED_API_BASE_PATH = "/authorized-apis";
    private static final String SCIM_BASE_PATH = "scim2";
    private static final String ROLE_V2_BASE_PATH = "/v2/Roles";
    private static final String AUTHORIZATION_DETAILS_TYPES_PATH = "/authorization-details-types";
    private final String applicationManagementApiBasePath;
    private final String subOrgApplicationManagementApiBasePath;
    private final String apiResourceManagementApiBasePath;
    private final String roleV2ApiBasePath;
    private final String username;
    private final String password;

    public OAuth2RestClient(String backendUrl, Tenant tenantInfo) {

        this.username = tenantInfo.getContextUser().getUserName();
        this.password = tenantInfo.getContextUser().getPassword();

        String tenantDomain = tenantInfo.getContextUser().getUserDomain();
        applicationManagementApiBasePath = getApplicationsPath(backendUrl, tenantDomain);
        subOrgApplicationManagementApiBasePath = getSubOrgApplicationsPath(backendUrl, tenantDomain);
        apiResourceManagementApiBasePath = getAPIResourcesPath(backendUrl, tenantDomain);
        roleV2ApiBasePath = getSCIM2RoleV2Path(backendUrl, tenantDomain);

    }

    /**
     * Create an Application.
     *
     * @param application Application Model with application creation details.
     * @return Id of the created application.
     * @throws IOException   If an error occurred while creating an application.
     * @throws JSONException If an error occurred while creating the json string.
     */
    public String createApplication(ApplicationModel application) throws IOException, JSONException {

        String jsonRequest = toJSONString(application);

        try (CloseableHttpResponse response = getResponseOfHttpPost(applicationManagementApiBasePath, jsonRequest,
                getHeaders())) {

            if (response.getStatusLine().getStatusCode() >= 400) {
                String responseBody = EntityUtils.toString(response.getEntity());
                throw new RuntimeException("Error occurred while creating the application. Response: " + responseBody);
            }
            String[] locationElements = response.getHeaders(LOCATION_HEADER)[0].toString().split(PATH_SEPARATOR);
            return locationElements[locationElements.length - 1];
        }
    }

    /**
     * To create V2 roles.
     *
     * @param role an instance of RoleV2
     * @return the roleID
     * @throws IOException throws if an error occurs while creating the role.
     */
    public String createV2Roles(RoleV2 role) throws IOException {

        String jsonRequest = toJSONString(role);
        try (CloseableHttpResponse response = getResponseOfHttpPost(roleV2ApiBasePath, jsonRequest,
                getHeaders())) {
            String[] locationElements = response.getHeaders(LOCATION_HEADER)[0].toString().split(PATH_SEPARATOR);
            return locationElements[locationElements.length - 1];
        }
    }

    /**
     * To delete V2 roles.
     *
     * @param roleId roleID
     * @throws IOException if an error occurs while deleting the role.
     */
    public void deleteV2Role(String roleId) throws IOException {

        String endpointUrl = roleV2ApiBasePath + PATH_SEPARATOR + roleId;
        try (CloseableHttpResponse response = getResponseOfHttpDelete(endpointUrl, getHeaders())) {
            Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpServletResponse.SC_NO_CONTENT,
                    "Application deletion failed");
        }
    }

    /**
     * Create an Application.
     *
     * @param application Application Model with application creation details.
     * @return Application creation response.
     * @throws IOException If an error occurred while creating an application.
     */
    public StatusLine createApplicationWithResponse(ApplicationModel application) throws IOException {

        String jsonRequest = toJSONString(application);
        try (CloseableHttpResponse response = getResponseOfHttpPost(applicationManagementApiBasePath, jsonRequest,
                getHeaders())) {
            return response.getStatusLine();
        }
    }

    /**
     * Get Application details.
     *
     * @param appId Application id.
     * @return ApplicationResponseModel object.
     * @throws IOException If an error occurred while getting an application.
     */
    public ApplicationResponseModel getApplication(String appId) throws IOException {

        String endPointUrl = applicationManagementApiBasePath + PATH_SEPARATOR + appId;

        try (CloseableHttpResponse response = getResponseOfHttpGet(endPointUrl, getHeaders())) {
            String responseBody = EntityUtils.toString(response.getEntity());

            ObjectMapper jsonWriter = new ObjectMapper(new JsonFactory());
            return jsonWriter.readValue(responseBody, ApplicationResponseModel.class);
        }
    }

    /**
     * Get Application details by client id.
     *
     * @param clientId Client id of the application.
     * @return Application list.
     * @throws IOException If an error occurred while filtering an application using client id.
     */
    public List<ApplicationListItem> getApplicationsByClientId(String clientId) throws IOException {

        String endPointUrl = applicationManagementApiBasePath + "?filter=clientId eq " + clientId;
        endPointUrl = endPointUrl.replace(" ", "%20");

        try (CloseableHttpResponse response = getResponseOfHttpGet(endPointUrl, getHeaders())) {
            String responseBody = EntityUtils.toString(response.getEntity());

            ObjectMapper jsonWriter = new ObjectMapper(new JsonFactory());
            ApplicationListResponse applications = jsonWriter.readValue(responseBody, ApplicationListResponse.class);
            return applications.getApplications();
        }
    }

    /**
     * Get application id using application name in a root organization.
     *
     * @param appName Application name.
     * @return Application id.
     * @throws IOException If an error occurred while retrieving the application.
     */
    public String getAppIdUsingAppName(String appName) throws IOException {

        return getAppIdUsingAppNameInOrganization(appName, null);
    }

    /**
     * Get application id using application name in an organization.
     *
     * @param appName          Application name.
     * @param switchedM2MToken Switched m2m token generated for the given organization.
     * @return Application id.
     * @throws IOException If an error occurred while retrieving the application.
     */
    public String getAppIdUsingAppNameInOrganization(String appName, String switchedM2MToken) throws IOException {

        String endPointUrl = (switchedM2MToken != null
                ? subOrgApplicationManagementApiBasePath : applicationManagementApiBasePath) +
                "?filter=name eq " + appName;
        endPointUrl = endPointUrl.replace(" ", "%20");

        try (CloseableHttpResponse response = getResponseOfHttpGet(endPointUrl,
                switchedM2MToken != null ? getHeadersWithBearerToken(switchedM2MToken) : getHeaders())) {
            String responseBody = EntityUtils.toString(response.getEntity());

            ObjectMapper jsonWriter = new ObjectMapper(new JsonFactory());
            ApplicationListResponse applicationResponse =
                    jsonWriter.readValue(responseBody, ApplicationListResponse.class);
            List<ApplicationListItem> applications = applicationResponse.getApplications();
            if (applications != null && !applications.isEmpty()) {
                return applications.get(0).getId();
            }
            return StringUtils.EMPTY;
        }
    }

    /**
     * Update an existing application.
     *
     * @param appId       Application id.
     * @param application Updated application patch object.
     * @throws IOException If an error occurred while updating an application.
     */
    public void updateApplication(String appId, ApplicationPatchModel application)
            throws IOException {

        String jsonRequest = toJSONString(application);
        String endPointUrl = applicationManagementApiBasePath + PATH_SEPARATOR + appId;

        try {
            if (isLegacyAuthzRuntimeEnabled()) {
                try (CloseableHttpResponse response = getResponseOfHttpPatch(endPointUrl, jsonRequest, getHeaders())) {
                    Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpServletResponse.SC_OK,
                            "Application update failed");
                }
            }
            if (!isLegacyAuthzRuntimeEnabled()) {
                if ((application.getAssociatedRoles() != null) && application.getAssociatedRoles().getRoles() != null) {
                    try (CloseableHttpResponse response = getResponseOfHttpPatch(endPointUrl, jsonRequest,
                            getHeaders())) {
                        Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpServletResponse.SC_FORBIDDEN,
                                "Application update failed");
                    }
                } else {
                    try (CloseableHttpResponse response = getResponseOfHttpPatch(endPointUrl, jsonRequest,
                            getHeaders())) {
                        Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpServletResponse.SC_OK,
                                "Application update failed");
                    }
                }
            }
        } catch (Exception e) {
            throw new Error("Unable to update the Application");
        }
    }

    /**
     * Update an existing sub organization application.
     *
     * @param appId            Application id.
     * @param application      Updated application patch object.
     * @param switchedM2MToken Switched m2m token generated for the given organization.
     */
    public void updateSubOrgApplication(String appId, ApplicationPatchModel application, String switchedM2MToken) {

        String jsonRequest = toJSONString(application);
        String endPointUrl = subOrgApplicationManagementApiBasePath + PATH_SEPARATOR + appId;

        try {
            if (isLegacyAuthzRuntimeEnabled()) {
                try (CloseableHttpResponse response = getResponseOfHttpPatch(endPointUrl, jsonRequest,
                        getHeadersWithBearerToken(switchedM2MToken))) {
                    Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpServletResponse.SC_OK,
                            "Application update failed");
                }
            }
            if (!isLegacyAuthzRuntimeEnabled()) {
                if ((application.getAssociatedRoles() != null) && application.getAssociatedRoles().getRoles() != null) {
                    try (CloseableHttpResponse response = getResponseOfHttpPatch(endPointUrl, jsonRequest,
                            getHeadersWithBearerToken(switchedM2MToken))) {
                        Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpServletResponse.SC_FORBIDDEN,
                                "Application update failed");
                    }
                } else {
                    try (CloseableHttpResponse response = getResponseOfHttpPatch(endPointUrl, jsonRequest,
                            getHeadersWithBearerToken(switchedM2MToken))) {
                        Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpServletResponse.SC_OK,
                                "Application update failed");
                    }
                }
            }
        } catch (Exception e) {
            throw new Error("Unable to update the Application");
        }
    }

    /**
     * Get all applications.
     *
     * @return ApplicationListResponse object.
     * @throws IOException If an error occurred while getting all applications.
     */
    public ApplicationListResponse getAllApplications() throws IOException {

        try (CloseableHttpResponse response = getResponseOfHttpGet(applicationManagementApiBasePath, getHeaders())) {
            String responseBody = EntityUtils.toString(response.getEntity());

            ObjectMapper jsonWriter = new ObjectMapper(new JsonFactory());
            return jsonWriter.readValue(responseBody, ApplicationListResponse.class);
        }
    }

    /**
     * Delete an application.
     *
     * @param appId Application id.
     * @throws IOException If an error occurred while deleting an application.
     */
    public void deleteApplication(String appId) throws IOException {

        String endpointUrl = applicationManagementApiBasePath + PATH_SEPARATOR + appId;

        try (CloseableHttpResponse response = getResponseOfHttpDelete(endpointUrl, getHeaders())) {
            Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpServletResponse.SC_NO_CONTENT,
                    "Application deletion failed");
        }
    }

    /**
     * Get OIDC inbound configuration details of an application.
     *
     * @param appId Application id.
     * @return OpenIDConnectConfiguration object with oidc configuration details.
     * @throws Exception If an error occurred while getting OIDC inbound configuration details.
     */
    public OpenIDConnectConfiguration getOIDCInboundDetails(String appId) throws Exception {

        String responseBody = getConfig(appId, OIDC);
        ObjectMapper jsonWriter = new ObjectMapper(new JsonFactory());
        return jsonWriter.readValue(responseBody, OpenIDConnectConfiguration.class);
    }

    /**
     * Get SAML inbound configuration details of an application.
     *
     * @param appId Application id.
     * @return SAML2ServiceProvider object with saml configuration details.
     * @throws Exception If an error occurred while getting SAML inbound configuration details.
     */
    public SAML2ServiceProvider getSAMLInboundDetails(String appId) throws Exception {

        String responseBody = getConfig(appId, SAML);
        ObjectMapper jsonWriter = new ObjectMapper(new JsonFactory());

        return jsonWriter.readValue(responseBody, SAML2ServiceProvider.class);
    }

    private String getConfig(String appId, String inboundType) throws Exception {

        String endPointUrl = applicationManagementApiBasePath + PATH_SEPARATOR + appId + INBOUND_PROTOCOLS_BASE_PATH +
                PATH_SEPARATOR + inboundType;

        try (CloseableHttpResponse response = getResponseOfHttpGet(endPointUrl, getHeaders())) {
            return EntityUtils.toString(response.getEntity());
        }
    }

    /**
     * Update inbound configuration details of an application.
     *
     * @param appId         Application id.
     * @param inboundConfig Inbound configuration object to be updated.
     * @param inboundType   Type of the inbound configuration.
     * @throws IOException If an error occurred while updating an inbound configuration.
     */
    public void updateInboundDetailsOfApplication(String appId, Object inboundConfig, String inboundType)
            throws IOException {

        String jsonRequest = toJSONString(inboundConfig);
        String endPointUrl = applicationManagementApiBasePath + PATH_SEPARATOR + appId + INBOUND_PROTOCOLS_BASE_PATH +
                PATH_SEPARATOR + inboundType;

        try (CloseableHttpResponse response = getResponseOfHttpPut(endPointUrl, jsonRequest, getHeaders())) {
            Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpServletResponse.SC_OK,
                    String.format("Application %s inbound config update failed", inboundType));
        }
    }

    /**
     * Delete an Inbound Configuration.
     *
     * @param appId       Application id.
     * @param inboundType Inbound Type to be deleted.
     * @throws IOException If an error occurred while deleting an inbound configuration.
     */
    public Boolean deleteInboundConfiguration(String appId, String inboundType) throws IOException {

        String endpointUrl = applicationManagementApiBasePath + PATH_SEPARATOR + appId + INBOUND_PROTOCOLS_BASE_PATH +
                PATH_SEPARATOR + inboundType;

        try (CloseableHttpResponse response = getResponseOfHttpDelete(endpointUrl, getHeaders())) {
            return response.getStatusLine().getStatusCode() == HttpServletResponse.SC_NO_CONTENT;
        }
    }

    public String initiateAuthRequest(String clientId, String scopes, boolean isOrganizationSSO) throws Exception {

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair(OAUTH2_RESPONSE_TYPE, OAuth2Constant.OAUTH2_GRANT_TYPE_CODE));
        urlParameters.add(new BasicNameValuePair(OAUTH2_CLIENT_ID, clientId));
        urlParameters.add(new BasicNameValuePair(REDIRECT_URI_NAME, OAuth2Constant.CALLBACK_URL));
        urlParameters.add(new BasicNameValuePair(SCOPE_PLAYGROUND_NAME, scopes));
        if (isOrganizationSSO) {
            urlParameters.add(new BasicNameValuePair("fidp", "OrganizationSSO"));
        }

        HttpResponse response = getResponseOfHttpPost(client, urlParameters,
                getTenantQualifiedURL(AUTHORIZE_ENDPOINT_URL, tenantInfo.getDomain()));
        Assert.assertNotNull(response, "Authorized response is null");
        Header locationHeader = getLocationHeader(response);
        Map<String, String> queryParams = extractQueryParams(locationHeader.getValue());
        return queryParams.get(SESSION_DATA_KEY);
    }

    private String getApplicationsPath(String serverUrl, String tenantDomain) {

        if (tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
            return serverUrl + API_SERVER_BASE_PATH + APPLICATION_MANAGEMENT_PATH;
        } else {
            return serverUrl + TENANT_PATH + tenantDomain + PATH_SEPARATOR + API_SERVER_BASE_PATH +
                    APPLICATION_MANAGEMENT_PATH;
        }
    }

    private String getSubOrgApplicationsPath(String serverUrl, String tenantDomain) {

        if (tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
            return serverUrl + ORGANIZATION_PATH + API_SERVER_BASE_PATH + APPLICATION_MANAGEMENT_PATH;
        }
        return serverUrl + TENANT_PATH + tenantDomain + PATH_SEPARATOR + ORGANIZATION_PATH + API_SERVER_BASE_PATH +
                APPLICATION_MANAGEMENT_PATH;
    }

    private String getAPIResourcesPath(String serverUrl, String tenantDomain) {

        if (tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
            return serverUrl + API_SERVER_BASE_PATH + API_RESOURCE_MANAGEMENT_PATH;
        } else {
            return serverUrl + TENANT_PATH + tenantDomain + PATH_SEPARATOR + API_SERVER_BASE_PATH +
                    API_RESOURCE_MANAGEMENT_PATH;
        }
    }

    private String getSCIM2RoleV2Path(String serverUrl, String tenantDomain) {

        if (tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
            return serverUrl + SCIM_BASE_PATH + ROLE_V2_BASE_PATH;
        } else {
            return serverUrl + TENANT_PATH + tenantDomain + PATH_SEPARATOR + SCIM_BASE_PATH + ROLE_V2_BASE_PATH;
        }
    }

    private Header[] getHeaders() {

        Header[] headerList = new Header[3];
        headerList[0] = new BasicHeader(USER_AGENT_ATTRIBUTE, OAuth2Constant.USER_AGENT);
        headerList[1] = new BasicHeader(AUTHORIZATION_ATTRIBUTE, BASIC_AUTHORIZATION_ATTRIBUTE +
                Base64.encodeBase64String((username + ":" + password).getBytes()).trim());
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

    /**
     * Add API authorization to an application.
     *
     * @param appId                      Application id.
     * @param authorizedAPICreationModel AuthorizedAPICreationModel object with api authorization details.
     * @return Status code of the response.
     * @throws Exception Error when getting the response.
     */
    public int addAPIAuthorizationToApplication(String appId, AuthorizedAPICreationModel authorizedAPICreationModel)
            throws IOException {

        String jsonRequestBody = toJSONString(authorizedAPICreationModel);
        String endPointUrl = applicationManagementApiBasePath + PATH_SEPARATOR + appId + AUTHORIZED_API_BASE_PATH;

        try (CloseableHttpResponse response = getResponseOfHttpPost(endPointUrl, jsonRequestBody, getHeaders())) {
            return response.getStatusLine().getStatusCode();
        }
    }

    /**
     * Get API resources by filtering.
     *
     * @param apiResourceFilter API resource filter.
     * @return List of API resources.
     * @throws IOException Error when getting the filtered API resource.
     */
    public List<APIResourceListItem> getAPIResourcesWithFiltering(String apiResourceFilter) throws IOException {

        String endPointUrl = apiResourceManagementApiBasePath + "?filter=" + apiResourceFilter;
        try (CloseableHttpResponse response = getResponseOfHttpGet(endPointUrl, getHeaders())) {
            String responseBody = EntityUtils.toString(response.getEntity());
            ObjectMapper jsonWriter = new ObjectMapper(new JsonFactory());
            APIResourceListResponse apiResourceListResponse =
                    jsonWriter.readValue(responseBody, APIResourceListResponse.class);
            return apiResourceListResponse.getApiResources();
        }
    }

    /**
     * Get API resource scopes.
     *
     * @param apiIdentifier API identifier.
     * @return List of API resource scopes.
     * @throws IOException Error when getting the scopes.
     */
    public List<ScopeGetModel> getAPIResourceScopes(String apiIdentifier) throws IOException {

        String endPointUrl = apiResourceManagementApiBasePath + PATH_SEPARATOR + apiIdentifier;
        try (CloseableHttpResponse response = getResponseOfHttpGet(endPointUrl, getHeaders())) {
            String responseBody = EntityUtils.toString(response.getEntity());
            ObjectMapper jsonWriter = new ObjectMapper(new JsonFactory());
            APIResourceResponse apiResourceResponse = jsonWriter.readValue(responseBody, APIResourceResponse.class);
            return apiResourceResponse.getScopes();
        }
    }

    /**
     * Creates a domain API.
     *
     * @param domainAPICreationModel Domain API create request model
     * @return ID of the created domain API resource
     */
    public String createDomainAPIResource(DomainAPICreationModel domainAPICreationModel) throws IOException {

        String jsonRequestBody = toJSONString(domainAPICreationModel);

        try (CloseableHttpResponse response = getResponseOfHttpPost(apiResourceManagementApiBasePath, jsonRequestBody,
                getHeaders())) {
            String[] locationElements = response.getHeaders(LOCATION_HEADER)[0].toString().split(PATH_SEPARATOR);
            return locationElements[locationElements.length - 1];
        }
    }

    /**
     * Deletes a domain API.
     *
     * @param domainAPIId ID of the domain API to be deleted
     * @return Status code of the action creation
     * @throws IOException If an error occurred while deleting the domain API
     */
    public int deleteDomainAPIResource(String domainAPIId) throws IOException {

        String endpointUrl = apiResourceManagementApiBasePath + "/" + domainAPIId;

        try (CloseableHttpResponse response = getResponseOfHttpDelete(endpointUrl, getHeaders())) {
            return response.getStatusLine().getStatusCode();
        }
    }

    public List<String> getRoles(String roleName, String audienceType, String audienceId) throws Exception {

        String endPointUrl = buildRoleSearchEndpoint(roleName, audienceType, audienceId);
        try (CloseableHttpResponse response = getResponseOfHttpGet(endPointUrl, getHeaders())) {
            if (response.getStatusLine().getStatusCode() != 200) {
                return Collections.emptyList();
            }
            String responseBody = EntityUtils.toString(response.getEntity());
            ObjectMapper objectMapper = new ObjectMapper();
            // Parse the JSON response.
            JsonNode jsonNode = objectMapper.readTree(responseBody);
            JsonNode totalResults = jsonNode.get("totalResults");
            if (totalResults.asInt() <= 0) {
                return Collections.emptyList();
            }
            // Extract the "Resources" array.
            JsonNode resourcesArray = jsonNode.get("Resources");
            // Initialize a list to store the "id" values
            List<String> idList = new ArrayList<>();
            // Iterate through the "Resources" array and extract "id" values.
            for (JsonNode resource : resourcesArray) {
                JsonNode idNode = resource.get("id");
                if (idNode != null && idNode.isTextual()) {
                    idList.add(idNode.asText());
                }
            }
            return idList;
        }
    }

    private String buildRoleSearchEndpoint(String roleName, String audienceType, String audienceId) {

        StringBuilder filter = new StringBuilder(roleV2ApiBasePath + "?");
        if (StringUtils.isNotBlank(roleName)) {
            filter.append("filter=displayName+eq+").append(roleName);
        }
        if (StringUtils.isNotBlank(audienceType)) {
            if (filter.length() > 0) {
                filter.append("+and+");
            }
            filter.append("audience.type+eq+").append(audienceType);
        }
        if (StringUtils.isNotBlank(audienceId)) {
            if (filter.length() > 0) {
                filter.append("+and+");
            }
            filter.append("audience.id+eq+").append(audienceId);
        }
        // If no parameters were provided, return the base URL.
        if (filter.toString().equals(roleV2ApiBasePath + "?")) {
            return roleV2ApiBasePath;
        }
        return filter.toString();
    }

    /**
     * Share the application with the organizations.
     *
     * @param appId                       The application ID.
     * @param applicationSharePOSTRequest The application sharing details.
     * @throws IOException Error when sharing the application.
     */
    public void shareApplication(String appId, ApplicationSharePOSTRequest applicationSharePOSTRequest) throws
            IOException {

        String jsonRequest = toJSONString(applicationSharePOSTRequest);
        try (CloseableHttpResponse response = getResponseOfHttpPost(applicationManagementApiBasePath +
                PATH_SEPARATOR + appId + PATH_SEPARATOR + "share", jsonRequest, getHeaders())) {
            Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpServletResponse.SC_OK,
                    "Application sharing failed");
        }
    }


    /**
     * To create API Resources.
     *
     * @param apiResource an instance of APIResource
     * @return the api resource ID
     * @throws IOException throws if an error occurs while creating the api resource.
     */
    public String createAPIResource(APIResourceResponse apiResource) throws IOException {

        String jsonRequest = toJSONString(apiResource);
        try (CloseableHttpResponse response = getResponseOfHttpPost(apiResourceManagementApiBasePath, jsonRequest,
                getHeaders())) {
            String[] locationElements = response.getHeaders(LOCATION_HEADER)[0].toString().split(PATH_SEPARATOR);
            return locationElements[locationElements.length - 1];
        }
    }

    /**
     * Delete API Resources.
     *
     * @param apiId API resource id.
     * @throws IOException If an error occurred while deleting an application.
     */
    public void deleteAPIResource(String apiId) throws IOException {

        String endpointUrl = apiResourceManagementApiBasePath + PATH_SEPARATOR + apiId;

        try (CloseableHttpResponse response = getResponseOfHttpDelete(endpointUrl, getHeaders())) {
            Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpServletResponse.SC_NO_CONTENT,
                    "API resource deletion failed");
        }
    }

    /**
     * Update an existing API resource.
     *
     * @param apiId       API resource id.
     * @param apiResource an instance of APIResource to be updated
     * @throws IOException If an error occurred while updating an API resource.
     */
    public void updateAPIResource(String apiId, APIResourcePatchModel apiResource)
            throws IOException {

        String endPointUrl = apiResourceManagementApiBasePath + PATH_SEPARATOR + apiId;

        try (CloseableHttpResponse response =
                     getResponseOfHttpPatch(endPointUrl, toJSONString(apiResource), getHeaders())) {
            Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpServletResponse.SC_NO_CONTENT,
                    "API resource update failed");
        }
    }

    /**
     * Get API resource authorization details types.
     *
     * @param apiIdentifier API identifier.
     * @return List of API resource authorization details types.
     * @throws IOException Error when getting the authorization details types.
     */
    public List<AuthorizationDetailsType> getAPIResourceAuthorizationDetailsTypes(String apiIdentifier)
            throws IOException {

        String endPointUrl = apiResourceManagementApiBasePath + PATH_SEPARATOR + apiIdentifier;
        try (CloseableHttpResponse response = getResponseOfHttpGet(endPointUrl, getHeaders())) {

            if (HttpServletResponse.SC_NOT_FOUND == response.getStatusLine().getStatusCode()) {
                return null;
            }

            String responseBody = EntityUtils.toString(response.getEntity());
            ObjectMapper jsonWriter = new ObjectMapper(new JsonFactory());
            APIResourceResponse apiResourceResponse = jsonWriter.readValue(responseBody, APIResourceResponse.class);
            return apiResourceResponse.getAuthorizationDetailsTypes();
        }
    }

    /**
     * Get API authorizations to an application.
     *
     * @param appId Application id.
     * @return Authorized API response.
     * @throws IOException Error when getting the response.
     */
    public List<AuthorizedAPIResponse> getAPIAuthorizationsFromApplication(String appId) throws IOException {

        final String endPointUrl = applicationManagementApiBasePath + PATH_SEPARATOR + appId + AUTHORIZED_API_BASE_PATH;

        try (CloseableHttpResponse response = getResponseOfHttpGet(endPointUrl, getHeaders())) {

            if (HttpServletResponse.SC_NOT_FOUND == response.getStatusLine().getStatusCode()) {
                return null;
            }

            String responseBody = EntityUtils.toString(response.getEntity());
            ObjectMapper jsonWriter = new ObjectMapper(new JsonFactory());
            return jsonWriter.readValue(responseBody,
                    jsonWriter.getTypeFactory().constructCollectionType(List.class, AuthorizedAPIResponse.class));
        }
    }

    /**
     * Update an existing authorized API resource.
     *
     * @param appId                   Application id.
     * @param authorizedApiId         API identifier.
     * @param authorizedAPIPatchModel an instance of Authorized APIResource to be updated
     * @throws IOException If an error occurred while updating an API resource.
     */
    public int updateAPIAuthorizationsFromApplication(String appId, String authorizedApiId,
                                                      AuthorizedAPIPatchModel authorizedAPIPatchModel)
            throws IOException {

        String jsonRequest = toJSONString(authorizedAPIPatchModel);
        final String endPointUrl = applicationManagementApiBasePath + PATH_SEPARATOR + appId +
                AUTHORIZED_API_BASE_PATH + PATH_SEPARATOR + authorizedApiId;

        try (CloseableHttpResponse response = getResponseOfHttpPatch(endPointUrl, jsonRequest, getHeaders())) {
            return response.getStatusLine().getStatusCode();
        }
    }

    /**
     * Delete an existing authorized API resource.
     *
     * @param appId           Application id.
     * @param authorizedApiId API identifier.
     * @throws IOException If an error occurred while updating an API resource.
     */
    public void deleteAPIAuthorizationsFromApplication(String appId, String authorizedApiId)
            throws IOException {

        final String endpointUrl = applicationManagementApiBasePath + PATH_SEPARATOR + appId +
                AUTHORIZED_API_BASE_PATH + PATH_SEPARATOR + authorizedApiId;

        try (CloseableHttpResponse response = getResponseOfHttpDelete(endpointUrl, getHeaders())) {
            Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpServletResponse.SC_NO_CONTENT,
                    "Authorized API resource deletion failed");
        }
    }

    /**
     * To create authorization details type.
     *
     * @param apiId       API resource id.
     * @param detailTypes authorization details types to be added
     * @return a list of authorization details type IDs
     * @throws IOException throws if an error occurs while creating the api resource.
     */
    public List<AuthorizationDetailsType> addAuthorizationDetailsTypes(
            String apiId, List<AuthorizationDetailsType> detailTypes) throws IOException {

        final String endPointUrl = apiResourceManagementApiBasePath + PATH_SEPARATOR + apiId +
                AUTHORIZATION_DETAILS_TYPES_PATH;
        String jsonRequest = toJSONString(detailTypes);
        try (CloseableHttpResponse response = getResponseOfHttpPut(endPointUrl, jsonRequest, getHeaders())) {

            Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpServletResponse.SC_OK,
                    "Authorization details type addition failed");

            String responseBody = EntityUtils.toString(response.getEntity());
            ObjectMapper jsonWriter = new ObjectMapper(new JsonFactory());
            return jsonWriter.readValue(responseBody,
                    jsonWriter.getTypeFactory().constructCollectionType(List.class, AuthorizationDetailsType.class));
        }
    }

    /**
     * Get authorization details type.
     *
     * @param apiId       API resource id.
     * @param typeId       Authorization details type id.
     * @return Authorization Details Type response.
     * @throws IOException Error when getting the response.
     */
    public AuthorizationDetailsType getAuthorizationDetailsType(String apiId, String typeId) throws IOException {

        final String endPointUrl = apiResourceManagementApiBasePath + PATH_SEPARATOR + apiId +
                AUTHORIZATION_DETAILS_TYPES_PATH + PATH_SEPARATOR + typeId;

        try (CloseableHttpResponse response = getResponseOfHttpGet(endPointUrl, getHeaders())) {

            if (HttpServletResponse.SC_NOT_FOUND == response.getStatusLine().getStatusCode()) {
                return null;
            }

            String responseBody = EntityUtils.toString(response.getEntity());
            ObjectMapper jsonWriter = new ObjectMapper(new JsonFactory());
            return jsonWriter.readValue(responseBody, AuthorizationDetailsType.class);
        }
    }

    /**
     * Update authorization details type.
     *
     * @param apiId       API resource id.
     * @param typeId       Authorization details type id.
     * @param detailTypes authorization details types to be updated
     * @throws IOException Error when getting the response.
     */
    public void updateAuthorizationDetailsType(String apiId, String typeId, AuthorizationDetailsType detailTypes)
            throws IOException {

        final String endPointUrl = apiResourceManagementApiBasePath + PATH_SEPARATOR + apiId +
                AUTHORIZATION_DETAILS_TYPES_PATH + PATH_SEPARATOR + typeId;

        String jsonRequest = toJSONString(detailTypes);
        try (CloseableHttpResponse response = getResponseOfHttpPatch(endPointUrl, jsonRequest, getHeaders())) {

            Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpServletResponse.SC_NO_CONTENT,
                    "Authorization details type update failed");
        }
    }

    /**
     * Delete authorization details type.
     *
     * @param apiId       API resource id.
     * @param typeId       Authorization details type id.
     * @throws IOException Error when getting the response.
     */
    public void deleteAuthorizationDetailsType(String apiId, String typeId)
            throws IOException {

        final String endpointUrl = apiResourceManagementApiBasePath + PATH_SEPARATOR + apiId +
                AUTHORIZATION_DETAILS_TYPES_PATH + PATH_SEPARATOR + typeId;

        try (CloseableHttpResponse response = getResponseOfHttpDelete(endpointUrl, getHeaders())) {
            Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpServletResponse.SC_NO_CONTENT,
                    "Authorization details type deletion failed");
        }
    }

    /**
     * Close the HTTP client.
     *
     * @throws IOException If an error occurred while closing the Http Client.
     */
    public void closeHttpClient() throws IOException {

        client.close();
    }
}

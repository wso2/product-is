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

package org.wso2.identity.integration.test.restclients;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.http.ContentType;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.wso2.carbon.automation.engine.context.beans.Tenant;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.identity.integration.test.rest.api.server.api.resource.v1.model.APIResourceCreationModel;
import org.wso2.identity.integration.test.rest.api.server.api.resource.v1.model.APIResourceListItem;
import org.wso2.identity.integration.test.rest.api.server.api.resource.v1.model.APIResourceListResponse;
import org.wso2.identity.integration.test.rest.api.server.api.resource.v1.model.APIResourcePatchModel;
import org.wso2.identity.integration.test.rest.api.server.api.resource.v1.model.APIResourceResponse;
import org.wso2.identity.integration.test.rest.api.server.api.resource.v1.model.ScopeGetModel;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.io.IOException;
import java.util.List;

/**
 * Rest client which provides methods to interact with the API Resource Management REST API.
 */
public class APIResourceManagementClient extends RestBaseClient {

    private static final String API_RESOURCE_MANAGEMENT_PATH = "api-resources";
    private static final String SCOPES_PATH = "scopes";
    private final String serverUrl;
    private final String tenantDomain;
    private final String username;
    private final String password;
    private final String apiResourceManagementBasePath;

    public APIResourceManagementClient(String serverUrl, Tenant tenantInfo) {

        this.serverUrl = serverUrl;
        this.tenantDomain = tenantInfo.getContextUser().getUserDomain();
        this.username = tenantInfo.getContextUser().getUserName();
        this.password = tenantInfo.getContextUser().getPassword();

        apiResourceManagementBasePath = getAPIResourceMgtPath(serverUrl, tenantDomain);
    }

    private String getAPIResourceMgtPath(String serverUrl, String tenantDomain) {

        if (tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
            return serverUrl + API_SERVER_PATH + PATH_SEPARATOR + API_RESOURCE_MANAGEMENT_PATH;
        }
        return serverUrl + TENANT_PATH + tenantDomain + PATH_SEPARATOR + API_SERVER_PATH + PATH_SEPARATOR +
                API_RESOURCE_MANAGEMENT_PATH;
    }

    /**
     * Create an API Resource.
     *
     * @param apiResourceCreationModel API Resource creation model.
     * @return API Resource ID.
     * @throws Exception If an error occurred while creating the API Resource.
     */
    public String createAPIResource(APIResourceCreationModel apiResourceCreationModel) throws Exception {

        String jsonRequestBody = toJSONString(apiResourceCreationModel);

        try (CloseableHttpResponse response = getResponseOfHttpPost(apiResourceManagementBasePath, jsonRequestBody,
                getHeadersWithBasicAuth())) {
            String[] locationElements = response.getHeaders(LOCATION_HEADER)[0].toString().split(PATH_SEPARATOR);
            return locationElements[locationElements.length - 1];
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

        String endPointUrl = apiResourceManagementBasePath;
        if (StringUtils.isNotBlank(apiResourceFilter)) {
            endPointUrl = apiResourceManagementBasePath + "?filter=" + apiResourceFilter;
        }
        try (CloseableHttpResponse response = getResponseOfHttpGet(endPointUrl, getHeadersWithBasicAuth())) {
            String responseBody = EntityUtils.toString(response.getEntity());
            ObjectMapper jsonWriter = new ObjectMapper(new JsonFactory());
            APIResourceListResponse apiResourceListResponse =
                    jsonWriter.readValue(responseBody, APIResourceListResponse.class);
            return apiResourceListResponse.getApiResources();
        }
    }

    /**
     * Get API resources by filtering from a sub organization.
     *
     * @param apiResourceFilter API resource filter.
     * @param switchedM2MToken  Switched M2M token.
     * @return List of API resources.
     * @throws Exception Error when getting the filtered API resources.
     */
    public List<APIResourceListItem> getAPIResourcesWithFilteringFromSubOrg(String apiResourceFilter,
                                                                            String switchedM2MToken) throws Exception {

        String endPointUrl = getSubOrgAPIResourceMgtPath();
        if (StringUtils.isNotBlank(apiResourceFilter)) {
            endPointUrl = endPointUrl + "?filter=" + apiResourceFilter;
        }
        try (CloseableHttpResponse response = getResponseOfHttpGet(endPointUrl,
                getHeadersWithBearerToken(switchedM2MToken))) {
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
     * @param apiId API Id.
     * @return List of API resource scopes.
     * @throws IOException Error when getting the scopes.
     */
    public List<ScopeGetModel> getAPIResourceScopes(String apiId) throws IOException {

        String endPointUrl = apiResourceManagementBasePath + PATH_SEPARATOR + apiId;
        try (CloseableHttpResponse response = getResponseOfHttpGet(endPointUrl, getHeadersWithBasicAuth())) {
            String responseBody = EntityUtils.toString(response.getEntity());
            ObjectMapper jsonWriter = new ObjectMapper(new JsonFactory());
            APIResourceResponse apiResourceResponse = jsonWriter.readValue(responseBody, APIResourceResponse.class);
            return apiResourceResponse.getScopes();
        }
    }

    /**
     * Get API resource scopes from a sub organization.
     *
     * @param apiId            API Id.
     * @param switchedM2MToken Switched M2M token.
     * @return List of API resource scopes.
     * @throws Exception Error when getting the scopes.
     */
    public List<ScopeGetModel> getAPIResourceScopesFromSubOrg(String apiId, String switchedM2MToken)
            throws Exception {

        String endPointUrl = getSubOrgAPIResourceMgtPath() + PATH_SEPARATOR + apiId;
        try (CloseableHttpResponse response = getResponseOfHttpGet(endPointUrl,
                getHeadersWithBearerToken(switchedM2MToken))) {
            String responseBody = EntityUtils.toString(response.getEntity());
            ObjectMapper jsonWriter = new ObjectMapper(new JsonFactory());
            APIResourceResponse apiResourceResponse = jsonWriter.readValue(responseBody, APIResourceResponse.class);
            return apiResourceResponse.getScopes();
        }
    }

    /**
     * Update an API resource.
     *
     * @param apiIdentifier         API identifier.
     * @param apiResourcePatchModel API resource patch model.
     * @return Status code of the response.
     * @throws IOException Error when updating the API resource.
     */
    public int updateAPIResource(String apiIdentifier, APIResourcePatchModel apiResourcePatchModel)
            throws IOException {

        String jsonRequestBody = toJSONString(apiResourcePatchModel);

        String endPointUrl = apiResourceManagementBasePath + PATH_SEPARATOR + apiIdentifier;
        try (CloseableHttpResponse response = getResponseOfHttpPatch(endPointUrl, jsonRequestBody,
                getHeadersWithBasicAuth())) {
            return response.getStatusLine().getStatusCode();
        }
    }

    /**
     * Delete an API resource.
     *
     * @param apiId API Id.
     * @return Status code of the response.
     * @throws IOException Error when deleting the API resource.
     */
    public int deleteAPIResource(String apiId) throws IOException {

        String endPointUrl = apiResourceManagementBasePath + PATH_SEPARATOR + apiId;
        try (CloseableHttpResponse response = getResponseOfHttpDelete(endPointUrl, getHeadersWithBasicAuth())) {
            return response.getStatusLine().getStatusCode();
        }
    }

    /**
     * Delete a scope of an API resource.
     *
     * @param apiId     API Id.
     * @param scopeName Scope name.
     * @return Status code of the response.
     * @throws IOException Error when deleting the scope of an API resource.
     */
    public int deleteScopeOfAPIResource(String apiId, String scopeName) throws IOException {

        String endPointUrl = apiResourceManagementBasePath + PATH_SEPARATOR + apiId + PATH_SEPARATOR + SCOPES_PATH +
                PATH_SEPARATOR + scopeName;
        try (CloseableHttpResponse response = getResponseOfHttpDelete(endPointUrl, getHeadersWithBasicAuth())) {
            return response.getStatusLine().getStatusCode();
        }
    }

    /**
     * Get all scopes of a sub organization.
     *
     * @return List of scopes.
     * @throws IOException Error when getting the scopes.
     */
    public List<ScopeGetModel> getAllScopesInSubOrg(String switchedM2MToken) throws IOException {

        String endPointUrl = getSubOrgScopesAPIPath();
        try (CloseableHttpResponse response = getResponseOfHttpGet(endPointUrl,
                getHeadersWithBearerToken(switchedM2MToken))) {
            String responseBody = EntityUtils.toString(response.getEntity());
            ObjectMapper jsonWriter = new ObjectMapper(new JsonFactory());
            return jsonWriter.readValue(responseBody, List.class);
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

    private String getSubOrgAPIResourceMgtPath() {

        if (tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
            return serverUrl + ORGANIZATION_PATH + API_SERVER_PATH + PATH_SEPARATOR + API_RESOURCE_MANAGEMENT_PATH;
        }
        return serverUrl + TENANT_PATH + tenantDomain + PATH_SEPARATOR + ORGANIZATION_PATH + API_SERVER_PATH +
                PATH_SEPARATOR + API_RESOURCE_MANAGEMENT_PATH;
    }

    private String getSubOrgScopesAPIPath() {

        if (tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
            return serverUrl + ORGANIZATION_PATH + API_SERVER_PATH + PATH_SEPARATOR + SCOPES_PATH;
        }
        return serverUrl + TENANT_PATH + tenantDomain + PATH_SEPARATOR + ORGANIZATION_PATH + API_SERVER_PATH +
                PATH_SEPARATOR + SCOPES_PATH;
    }

    private Header[] getHeadersWithBasicAuth() {

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
}

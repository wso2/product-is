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
import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.wso2.carbon.automation.engine.context.beans.Tenant;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.identity.integration.test.rest.api.server.flow.execution.v1.model.FlowConfig;
import org.wso2.identity.integration.test.rest.api.server.flow.management.v1.model.FlowRequest;
import org.wso2.identity.integration.test.rest.api.server.flow.management.v1.model.FlowResponse;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.io.IOException;

import static java.net.HttpURLConnection.HTTP_OK;

/**
 * Rest client which provides methods to interact with the Flow Management REST API.
 */
public class FlowManagementClient extends RestBaseClient {

    private static final String FLOW_MANAGEMENT_API_PATH = "flow";
    private final String tenantDomain;
    private final String username;
    private final String password;
    private final String flowManagementBasePath;

    public FlowManagementClient(String serverUrl, Tenant tenantInfo) {

        this.tenantDomain = tenantInfo.getContextUser().getUserDomain();
        this.username = tenantInfo.getContextUser().getUserName();
        this.password = tenantInfo.getContextUser().getPassword();
        this.flowManagementBasePath = getFlowMgtPath(serverUrl, tenantDomain);
    }

    /**
     * Update a flow.
     *
     * @param flowRequest Flow request.
     * @throws Exception If an error occurs while updating the flow.
     */
    public void putFlow(FlowRequest flowRequest)
            throws Exception {

        String jsonRequestBody = toJSONString(flowRequest);

        try (CloseableHttpResponse response = getResponseOfHttpPut(flowManagementBasePath, jsonRequestBody,
                getHeadersWithBasicAuth())) {
            if (response.getStatusLine().getStatusCode() != HTTP_OK) {
                throw new Exception("Error code " + response.getStatusLine().getStatusCode() +
                        String.format(" occurred while updating the %s flow", flowRequest.getFlowType()));
            }
        }
    }

    /**
     * Get the flow.
     *
     * @return Flow response.
     * @throws Exception If an error occurs while getting the flow.
     */
    public FlowResponse getFlow(String flowType) throws Exception {

        String flowPath = flowManagementBasePath + "?flowType=" + flowType;
        try (CloseableHttpResponse response = getResponseOfHttpGet(flowPath, getHeadersWithBasicAuth())) {
            if (response.getStatusLine().getStatusCode() != 200) {
                throw new Exception(String.format("Failed to get the %s flow", flowType));
            }
            String responseBody = EntityUtils.toString(response.getEntity());
            ObjectMapper jsonWriter = new ObjectMapper(new JsonFactory());
            return jsonWriter.readValue(responseBody, FlowResponse.class);
        }
    }

    public FlowConfig getFlowConfig(String flowType) throws Exception {

        String flowConfigPath = flowManagementBasePath + "/config?flowType=" + flowType;
        try (CloseableHttpResponse response = getResponseOfHttpGet(flowConfigPath,
                getHeadersWithBasicAuth())) {
            if (response.getStatusLine().getStatusCode() != 200) {
                throw new Exception(String.format("Failed to get the %s flow config", flowType));
            }
            String responseBody = EntityUtils.toString(response.getEntity());
            ObjectMapper jsonWriter = new ObjectMapper(new JsonFactory());
            return jsonWriter.readValue(responseBody, FlowConfig.class);
        }
    }

    public FlowConfig updateFlowConfig(FlowConfig flowConfig) throws Exception {

        String flowConfigPath = flowManagementBasePath + "/config";
        String jsonRequestBody = toJSONString(flowConfig);
        try (CloseableHttpResponse response = getResponseOfHttpPatch(flowConfigPath, jsonRequestBody,
                getHeadersWithBasicAuth())) {
            if (response.getStatusLine().getStatusCode() != HTTP_OK) {
                throw new Exception("Error code " + response.getStatusLine().getStatusCode() +
                        String.format(" occurred while updating the %s flow config", flowConfig.getFlowType()));
            }
            String responseBody = EntityUtils.toString(response.getEntity());
            ObjectMapper jsonWriter = new ObjectMapper(new JsonFactory());
            return jsonWriter.readValue(responseBody, FlowConfig.class);
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

    private String getFlowMgtPath(String serverUrl, String tenantDomain) {

        if (tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
            return serverUrl + API_SERVER_PATH + PATH_SEPARATOR + FLOW_MANAGEMENT_API_PATH;
        }
        return serverUrl + TENANT_PATH + tenantDomain + PATH_SEPARATOR + API_SERVER_PATH + PATH_SEPARATOR +
                FLOW_MANAGEMENT_API_PATH;
    }

    private Header[] getHeadersWithBasicAuth() {

        Header[] headerList = new Header[3];
        headerList[0] = new BasicHeader(USER_AGENT_ATTRIBUTE, OAuth2Constant.USER_AGENT);
        headerList[1] = new BasicHeader(AUTHORIZATION_ATTRIBUTE, BASIC_AUTHORIZATION_ATTRIBUTE +
                Base64.encodeBase64String((username + ":" + password).getBytes()).trim());
        headerList[2] = new BasicHeader(CONTENT_TYPE_ATTRIBUTE, String.valueOf(ContentType.JSON));

        return headerList;
    }
}

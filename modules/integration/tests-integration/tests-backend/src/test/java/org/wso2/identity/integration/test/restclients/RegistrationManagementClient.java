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
import org.wso2.identity.integration.test.rest.api.server.flow.management.v1.model.RegistrationFlowRequest;
import org.wso2.identity.integration.test.rest.api.server.flow.management.v1.model.RegistrationFlowResponse;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.io.IOException;

import static java.net.HttpURLConnection.HTTP_OK;

/**
 * Rest client which provides methods to interact with the Registration Management REST API.
 */
public class RegistrationManagementClient extends RestBaseClient {

    private static final String REGISTRATION_MANAGEMENT_API_PATH = "flow-flow";
    private final String tenantDomain;
    private final String username;
    private final String password;
    private final String registrationManagementBasePath;

    public RegistrationManagementClient(String serverUrl, Tenant tenantInfo) {

        this.tenantDomain = tenantInfo.getContextUser().getUserDomain();
        this.username = tenantInfo.getContextUser().getUserName();
        this.password = tenantInfo.getContextUser().getPassword();
        this.registrationManagementBasePath = getRegistrationMgtPath(serverUrl, tenantDomain);
    }

    /**
     * Update a flow flow.
     *
     * @param registrationFlowRequest Registration flow request.
     * @throws Exception If an error occurs while updating the flow flow.
     */
    public void putRegistrationFlow(RegistrationFlowRequest registrationFlowRequest)
            throws Exception {

        String jsonRequestBody = toJSONString(registrationFlowRequest);

        try (CloseableHttpResponse response = getResponseOfHttpPut(registrationManagementBasePath, jsonRequestBody,
                getHeadersWithBasicAuth())) {
            if (response.getStatusLine().getStatusCode() != HTTP_OK) {
                throw new Exception("Error code " + response.getStatusLine().getStatusCode() +
                        " occurred while updating the flow flow");
            }
        }
    }

    /**
     * Get the flow flow.
     *
     * @return Registration flow response.
     * @throws Exception If an error occurs while getting the flow flow.
     */
    public RegistrationFlowResponse getRegistrationFlow() throws Exception {

        try (CloseableHttpResponse response = getResponseOfHttpGet(registrationManagementBasePath,
                getHeadersWithBasicAuth())) {
            if (response.getStatusLine().getStatusCode() != 200) {
                throw new Exception("Failed to get the flow flow");
            }
            String responseBody = EntityUtils.toString(response.getEntity());
            ObjectMapper jsonWriter = new ObjectMapper(new JsonFactory());
            return jsonWriter.readValue(responseBody, RegistrationFlowResponse.class);
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

    private String getRegistrationMgtPath(String serverUrl, String tenantDomain) {

        if (tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
            return serverUrl + API_SERVER_PATH + PATH_SEPARATOR + REGISTRATION_MANAGEMENT_API_PATH;
        }
        return serverUrl + TENANT_PATH + tenantDomain + PATH_SEPARATOR + API_SERVER_PATH + PATH_SEPARATOR +
                REGISTRATION_MANAGEMENT_API_PATH;
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

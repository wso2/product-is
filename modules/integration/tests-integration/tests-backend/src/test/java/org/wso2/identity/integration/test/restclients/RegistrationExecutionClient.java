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
import org.wso2.identity.integration.test.rest.api.server.registration.execution.v1.model.FlowExecutionRequest;
import org.wso2.identity.integration.test.rest.api.server.registration.execution.v1.model.FlowExecutionResponse;
import org.wso2.identity.integration.test.rest.api.server.registration.management.v1.model.Error;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.io.IOException;

import static java.net.HttpURLConnection.HTTP_OK;

/**
 * This class is used to perform REST API calls to the Registration Execution API.
 */
public class RegistrationExecutionClient extends RestBaseClient {

    private static final String REGISTRATION_EXECUTION_API_PATH = "registration";
    private static final String REGISTRATION_EXECUTION_ENDPOINT = "execute";
    private final String tenantDomain;
    private final String username;
    private final String password;
    private final String registrationExecutionBasePath;

    public RegistrationExecutionClient(String serverUrl, Tenant tenantInfo) {

        this.tenantDomain = tenantInfo.getContextUser().getUserDomain();
        this.username = tenantInfo.getContextUser().getUserName();
        this.password = tenantInfo.getContextUser().getPassword();
        this.registrationExecutionBasePath = getRegistrationExecutionPath(serverUrl, tenantDomain);
    }

    /**
     * Initiate the registration execution.
     *
     * @return The response of the registration initiation.
     * @throws Exception If an error occurred while initiating the registration execution.
     */
    public Object initiateRegistrationExecution() throws Exception {

        String jsonRequestBody = "{}";
        String executionUrl = registrationExecutionBasePath + PATH_SEPARATOR + REGISTRATION_EXECUTION_ENDPOINT;

        try (CloseableHttpResponse response = getResponseOfHttpPost(executionUrl, jsonRequestBody,
                getHeadersWithBasicAuth())) {
            ObjectMapper objectMapper = new ObjectMapper(new JsonFactory());
            String responseString = EntityUtils.toString(response.getEntity());
            if (response.getStatusLine().getStatusCode() != HTTP_OK) {
                return objectMapper.readValue(responseString, Error.class);
            } else {
                return objectMapper.readValue(responseString, FlowExecutionResponse.class);
            }
        }
    }

    /**
     * Submit the registration request.
     *
     * @param registrationExecutionRequest The registration execution request.
     * @return The response of the registration submission.
     * @throws Exception If an error occurred while submitting the registration request.
     */
    public Object submitRegistration(FlowExecutionRequest registrationExecutionRequest) throws Exception {

        String jsonRequestBody = toJSONString(registrationExecutionRequest);
        String executionUrl = registrationExecutionBasePath + PATH_SEPARATOR + REGISTRATION_EXECUTION_ENDPOINT;
        try (CloseableHttpResponse response = getResponseOfHttpPost(executionUrl, jsonRequestBody,
                getHeadersWithBasicAuth())) {
            ObjectMapper objectMapper = new ObjectMapper(new JsonFactory());
            String responseString = EntityUtils.toString(response.getEntity());
            if (response.getStatusLine().getStatusCode() != HTTP_OK) {
                return objectMapper.readValue(responseString, Error.class);
            } else {
                return objectMapper.readValue(responseString, FlowExecutionResponse.class);
            }
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

    private String getRegistrationExecutionPath(String serverUrl, String tenantDomain) {

        if (MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
            return serverUrl + API_SERVER_PATH + PATH_SEPARATOR + REGISTRATION_EXECUTION_API_PATH;
        }
        return serverUrl + TENANT_PATH + tenantDomain + PATH_SEPARATOR + API_SERVER_PATH + PATH_SEPARATOR +
                REGISTRATION_EXECUTION_API_PATH;
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

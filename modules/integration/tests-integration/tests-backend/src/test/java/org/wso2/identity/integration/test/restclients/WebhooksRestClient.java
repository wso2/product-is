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

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.http.ContentType;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.message.BasicHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.automation.engine.context.beans.Tenant;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.identity.integration.test.rest.api.server.webhook.management.v1.exception.WebhookManagementException;
import org.wso2.identity.integration.test.rest.api.server.webhook.management.v1.model.WebhookRequest;
import org.wso2.identity.integration.test.rest.api.server.webhook.management.v1.model.WebhookResponse;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * Rest client for managing webhooks.
 */
public class WebhooksRestClient extends RestBaseClient {

    private static final String WEBHOOKS_API_PATH = "/webhooks";

    private final String tenantDomain;
    private final String username;
    private final String password;
    private final String webhooksAPIBaseURL;

    private static final Logger LOG = LoggerFactory.getLogger(WebhooksRestClient.class);

    public WebhooksRestClient(String serverUrl, Tenant tenantInfo) {

        this.tenantDomain = tenantInfo.getContextUser().getUserDomain();
        this.username = tenantInfo.getContextUser().getUserName();
        this.password = tenantInfo.getContextUser().getPassword();
        this.webhooksAPIBaseURL = getWebhooksAPIBaseURL(serverUrl, tenantDomain);
    }

    public WebhookResponse createWebhook(WebhookRequest webhookRequest) throws WebhookManagementException {

        String jsonRequestBody = toJSONString(webhookRequest);
        ObjectMapper objectMapper = new ObjectMapper();

        try (CloseableHttpResponse response = getResponseOfHttpPost(webhooksAPIBaseURL, jsonRequestBody,
                getHeaders())) {
            if (response.getEntity() != null) {
                StringBuilder jsonResponseBuilder = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(response.getEntity().getContent(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        jsonResponseBuilder.append(line);
                    }
                }
                String jsonResponse = jsonResponseBuilder.toString();

                LOG.info("Response body for webhook create request: {}", jsonResponse);

                return objectMapper.readValue(jsonResponse, WebhookResponse.class);
            } else {
                throw new WebhookManagementException(
                        "Response body for webhook create request is empty." +
                                ". Response status code: " + response.getStatusLine().getStatusCode() + ". ");
            }
        } catch (Exception e) {
            throw new WebhookManagementException(
                    "Error occurred while creating webhook. Webhook create request: " + jsonRequestBody + ". ", e);
        }
    }

    public void deleteWebhook(String webhookId) throws WebhookManagementException {

        String endPointUrl = webhooksAPIBaseURL + "/" + webhookId;

        try (CloseableHttpResponse response = getResponseOfHttpDelete(endPointUrl, getHeaders())) {
            if (response.getStatusLine().getStatusCode() != 204) {
                throw new WebhookManagementException(
                        "Error occurred while deleting webhook with ID: " + webhookId + ". Response status code: " +
                                response.getStatusLine().getStatusCode() + ". ");
            }
        } catch (IOException e) {
            throw new WebhookManagementException(
                    "Error occurred while deleting webhook with ID: " + webhookId + ". ", e);
        }
    }

    public WebhookResponse getWebhook(String webhookId) throws WebhookManagementException {

        String endPointUrl = webhooksAPIBaseURL + "/" + webhookId;
        ObjectMapper objectMapper = new ObjectMapper();

        try (CloseableHttpResponse response = getResponseOfHttpGet(endPointUrl, getHeaders())) {
            if (response.getEntity() != null) {
                StringBuilder jsonResponseBuilder = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(response.getEntity().getContent(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        jsonResponseBuilder.append(line);
                    }
                }
                String jsonResponse = jsonResponseBuilder.toString();

                LOG.info("Response body for webhook get request: {}", jsonResponse);

                return objectMapper.readValue(jsonResponse, WebhookResponse.class);
            } else {
                throw new WebhookManagementException(
                        "Response body for webhook get request is empty. Webhook ID: " + webhookId +
                                ". Response status code: " + response.getStatusLine().getStatusCode() + ". ");
            }
        } catch (Exception e) {
            throw new WebhookManagementException(
                    "Error occurred while getting webhook with ID: " + webhookId + ". ", e);
        }
    }

    public WebhookResponse updateWebhook(String webhookId, WebhookRequest webhookRequest)
            throws WebhookManagementException {

        String jsonRequestBody = toJSONString(webhookRequest);
        ObjectMapper objectMapper = new ObjectMapper();
        String endPointUrl = webhooksAPIBaseURL + "/" + webhookId;

        try (CloseableHttpResponse response = getResponseOfHttpPatch(endPointUrl, jsonRequestBody, getHeaders())) {
            if (response.getEntity() != null) {
                StringBuilder jsonResponseBuilder = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(response.getEntity().getContent(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        jsonResponseBuilder.append(line);
                    }
                }
                String jsonResponse = jsonResponseBuilder.toString();

                LOG.info("Response body for webhook update request: {}", jsonResponse);

                return objectMapper.readValue(jsonResponse, WebhookResponse.class);
            } else {
                throw new WebhookManagementException(
                        "Response body for webhook update request is empty. Webhook ID: " + webhookId +
                                ". Response status code: " + response.getStatusLine().getStatusCode() + ". ");
            }
        } catch (Exception e) {
            throw new WebhookManagementException(
                    "Error occurred while updating webhook with ID: " + webhookId + ". ", e);
        }
    }

    private String getWebhooksAPIBaseURL(String serverUrl, String tenantDomain) {

        if (tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
            return serverUrl + API_SERVER_PATH + WEBHOOKS_API_PATH;
        } else {
            return serverUrl + TENANT_PATH + tenantDomain + PATH_SEPARATOR + API_SERVER_PATH +
                    WEBHOOKS_API_PATH;
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
}

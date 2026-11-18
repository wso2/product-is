/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
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
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.automation.engine.context.beans.Tenant;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.identity.integration.test.rest.api.server.webhook.management.v1.exception.WebhookManagementException;
import org.wso2.identity.integration.test.rest.api.server.webhook.management.v2.model.WebhookCreateRequest;
import org.wso2.identity.integration.test.rest.api.server.webhook.management.v2.model.WebhookResponse;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.io.IOException;

/**
 * Rest client for managing webhooks through the webhook management v2 API.
 */
public class WebhooksV2RestClient extends RestBaseClient {

    private static final String API_SERVER_V2_PATH = "api/server/v2";
    private static final String WEBHOOKS_API_PATH = "/webhooks";

    private final String username;
    private final String password;
    private final String webhooksAPIBaseURL;

    private static final Logger LOG = LoggerFactory.getLogger(WebhooksV2RestClient.class);

    public WebhooksV2RestClient(String serverUrl, Tenant tenantInfo) {

        String tenantDomain = tenantInfo.getContextUser().getUserDomain();
        this.username = tenantInfo.getContextUser().getUserName();
        this.password = tenantInfo.getContextUser().getPassword();
        this.webhooksAPIBaseURL = getWebhooksAPIBaseURL(serverUrl, tenantDomain);
    }

    public WebhookResponse createWebhook(WebhookCreateRequest webhookCreateRequest)
            throws WebhookManagementException {

        String jsonRequestBody = toJSONString(webhookCreateRequest);

        try (CloseableHttpResponse response = getResponseOfHttpPost(webhooksAPIBaseURL, jsonRequestBody,
                getHeaders())) {
            int statusCode = response.getStatusLine().getStatusCode();
            String jsonResponse = response.getEntity() != null ? EntityUtils.toString(response.getEntity()) : null;
            LOG.info("Response body for webhook create request: {}", jsonResponse);

            if (statusCode != 201 || jsonResponse == null) {
                throw new WebhookManagementException("Webhook create request failed. Response status code: " +
                        statusCode + ". Response body: " + jsonResponse + ". ");
            }
            return new ObjectMapper().readValue(jsonResponse, WebhookResponse.class);
        } catch (IOException e) {
            throw new WebhookManagementException(
                    "Error occurred while creating webhook. Webhook create request: " + jsonRequestBody + ". ", e);
        }
    }

    public WebhookResponse getWebhook(String webhookId) throws WebhookManagementException {

        String endPointUrl = webhooksAPIBaseURL + PATH_SEPARATOR + webhookId;

        try (CloseableHttpResponse response = getResponseOfHttpGet(endPointUrl, getHeaders())) {
            int statusCode = response.getStatusLine().getStatusCode();
            String jsonResponse = response.getEntity() != null ? EntityUtils.toString(response.getEntity()) : null;
            LOG.info("Response body for webhook get request: {}", jsonResponse);

            if (statusCode != 200 || jsonResponse == null) {
                throw new WebhookManagementException("Webhook get request failed for webhook ID: " + webhookId +
                        ". Response status code: " + statusCode + ". ");
            }
            return new ObjectMapper().readValue(jsonResponse, WebhookResponse.class);
        } catch (IOException e) {
            throw new WebhookManagementException(
                    "Error occurred while getting webhook with ID: " + webhookId + ". ", e);
        }
    }

    public void deleteWebhook(String webhookId) throws WebhookManagementException {

        String endPointUrl = webhooksAPIBaseURL + PATH_SEPARATOR + webhookId;

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

    private String getWebhooksAPIBaseURL(String serverUrl, String tenantDomain) {

        if (tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
            return serverUrl + API_SERVER_V2_PATH + WEBHOOKS_API_PATH;
        }
        return serverUrl + TENANT_PATH + tenantDomain + PATH_SEPARATOR + API_SERVER_V2_PATH + WEBHOOKS_API_PATH;
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

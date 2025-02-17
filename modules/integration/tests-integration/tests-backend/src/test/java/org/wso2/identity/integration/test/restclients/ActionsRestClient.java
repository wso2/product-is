/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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

import io.restassured.http.ContentType;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.message.BasicHeader;
import org.wso2.carbon.automation.engine.context.beans.Tenant;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.identity.integration.test.rest.api.server.action.management.v1.common.model.ActionModel;
import org.wso2.identity.integration.test.rest.api.server.action.management.v1.common.model.ActionUpdateModel;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.io.IOException;

/**
 * Rest client which provides methods to interact with the Actions REST API.
 * This client is responsible for managing actions of various types.
 */
public class ActionsRestClient extends RestBaseClient {

    private static final String PRE_ISSUE_ACCESS_TOKEN_TYPE = "preIssueAccessToken";
    private static final String PRE_UPDATE_PASSWORD_TYPE = "preUpdatePassword";
    private static final String ACTIONS_PATH = "/actions";
    private static final String PRE_ISSUE_ACCESS_TOKEN_PATH = "/preIssueAccessToken";
    private static final String PRE_UPDATE_PASSWORD_PATH = "/preUpdatePassword";
    private final String serverUrl;
    private final String tenantDomain;
    private final String username;
    private final String password;
    private final String actionsBasePath;

    public ActionsRestClient(String serverUrl, Tenant tenantInfo) {

        this.serverUrl = serverUrl;
        this.tenantDomain = tenantInfo.getContextUser().getUserDomain();
        this.username = tenantInfo.getContextUser().getUserName();
        this.password = tenantInfo.getContextUser().getPassword();

        actionsBasePath = getActionsPath(serverUrl, tenantDomain);
    }

    /**
     * Create an action of the specified type.
     *
     * @param actionModel Request object to create the action
     * @param actionType  Type of the action
     * @return ID of the created action
     * @throws IOException If an error occurred while creating the action
     */
    public String createActionType(ActionModel actionModel, String actionType) throws IOException {

        String jsonRequestBody = toJSONString(actionModel);

        String endPointUrl;
        endPointUrl = getActionEndpointOfType(actionType);

        try (CloseableHttpResponse response = getResponseOfHttpPost(endPointUrl, jsonRequestBody, getHeaders())) {
            String[] locationElements = response.getHeaders(LOCATION_HEADER)[0].toString().split(PATH_SEPARATOR);
            return locationElements[locationElements.length - 1];
        }
    }

    /**
     * Update an action of the specified type by the provided ID.
     *
     * @param actionType  Type of action
     * @param actionId    ID of the action
     * @param actionModel Request object to update the action
     * @return Status of the action update
     * @throws IOException If an error occurred while updating the action
     */
    public boolean updateAction(String actionType, String actionId, ActionUpdateModel actionModel)
            throws IOException {

        String jsonRequestBody = toJSONString(actionModel);

        String endPointUrl;
        endPointUrl = getActionEndpointOfType(actionType) + "/" + actionId;

        try (CloseableHttpResponse response = getResponseOfHttpPatch(endPointUrl, jsonRequestBody, getHeaders())) {
            return response.getStatusLine().getStatusCode() == HttpStatus.SC_OK;
        }
    }

    /**
     * Delete an action of the specified type by the provided ID.
     *
     * @param actionType Type of action
     * @param actionId   ID of the action
     * @return Status code of the action deletion
     * @throws IOException If an error occurred while deleting the action
     */
    public int deleteActionType(String actionType, String actionId) throws IOException {

        String endPointUrl;
        endPointUrl = getActionEndpointOfType(actionType) + "/" + actionId;

        try (CloseableHttpResponse response = getResponseOfHttpDelete(endPointUrl, getHeaders())) {
            return response.getStatusLine().getStatusCode();
        }
    }

    /**
     * Retrieve the action endpoint according to the action type.
     *
     * @param actionType Type of action
     * @return Action endpoint
     */
    private String getActionEndpointOfType(String actionType) {

        switch (actionType) {
            case PRE_ISSUE_ACCESS_TOKEN_TYPE:
                return actionsBasePath + PRE_ISSUE_ACCESS_TOKEN_PATH;
            case PRE_UPDATE_PASSWORD_TYPE:
                return actionsBasePath + PRE_UPDATE_PASSWORD_PATH;
            default:
                return StringUtils.EMPTY;
        }
    }

    /**
     * Get path of the action endpoint.
     *
     * @param serverUrl    Server URL
     * @param tenantDomain Tenant Domain
     * @return Path of the action endpoint
     */
    private String getActionsPath(String serverUrl, String tenantDomain) {

        if (tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
            return serverUrl + API_SERVER_PATH + ACTIONS_PATH;
        } else {
            return serverUrl + TENANT_PATH + tenantDomain + PATH_SEPARATOR + API_SERVER_PATH +
                    ACTIONS_PATH;
        }
    }

    /**
     * Retrieve headers.
     *
     * @return An array of headers
     */
    private Header[] getHeaders() {

        Header[] headerList = new Header[3];
        headerList[0] = new BasicHeader(USER_AGENT_ATTRIBUTE, OAuth2Constant.USER_AGENT);
        headerList[1] = new BasicHeader(AUTHORIZATION_ATTRIBUTE, BASIC_AUTHORIZATION_ATTRIBUTE +
                Base64.encodeBase64String((username + ":" + password).getBytes()).trim());
        headerList[2] = new BasicHeader(CONTENT_TYPE_ATTRIBUTE, String.valueOf(ContentType.JSON));

        return headerList;
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

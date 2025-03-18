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

package org.wso2.identity.integration.test.restclients;

import io.restassured.http.ContentType;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.testng.Assert;
import org.wso2.carbon.automation.engine.context.beans.Tenant;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;
import org.wso2.identity.integration.test.rest.api.server.user.store.v1.model.UserStoreReq;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class UserStoreMgtRestClient extends RestBaseClient {

    private static final String API_SERVER_BASE_PATH = "/api/server/v1";
    public static final String USER_STORES_ENDPOINT_URI = "/userstores";
    public static final String ORGANIZATION_PATH = "o/";
    public static final String PATH_SEPARATOR = "/";
    public static final int TIMEOUT_MILLIS = 30000;
    public static final int POLLING_INTERVAL_MILLIS = 500;

    private final CloseableHttpClient client;
    private final String username;
    private final String password;
    private final String userStoreBasePath;
    private final String userStoreSubOrgBasePath;

    public UserStoreMgtRestClient(String backendURL, Tenant tenantInfo) {

        client = HttpClients.createDefault();

        this.username = tenantInfo.getContextUser().getUserName();
        this.password = tenantInfo.getContextUser().getPassword();
        String tenantDomain = tenantInfo.getContextUser().getUserDomain();

        userStoreBasePath = backendURL +
                ISIntegrationTest.getTenantedRelativePath(API_SERVER_BASE_PATH + USER_STORES_ENDPOINT_URI,
                        tenantDomain);

        userStoreSubOrgBasePath = backendURL + ISIntegrationTest.getTenantedRelativePath(
                PATH_SEPARATOR + ORGANIZATION_PATH + API_SERVER_BASE_PATH + USER_STORES_ENDPOINT_URI, tenantDomain);
    }

    /**
     * Add a secondary user store.
     *
     * @param userStoreReq Secondary user store request object.
     * @throws IOException If an error occurred while adding a user store.
     */
    public String addUserStore(UserStoreReq userStoreReq) throws IOException {

        String jsonRequest = toJSONString(userStoreReq);
        try (CloseableHttpResponse response = getResponseOfHttpPost(userStoreBasePath, jsonRequest, getHeaders())) {
            String[] locationElements = response.getHeaders(LOCATION_HEADER)[0].toString().split(PATH_SEPARATOR);
            return locationElements[locationElements.length - 1];
        }
    }

    /**
     * Add a secondary user store.
     *
     * @param userStoreReq     Secondary user store request object.
     * @param switchedM2MToken Switched M2M token.
     * @throws IOException If an error occurred while adding a user store.
     */
    public void addSubOrgUserStore(UserStoreReq userStoreReq, String switchedM2MToken) throws IOException {

        String jsonRequest = toJSONString(userStoreReq);
        try (CloseableHttpResponse response = getResponseOfHttpPost(userStoreSubOrgBasePath, jsonRequest,
                getHeadersWithBearerToken(switchedM2MToken))) {
            Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpServletResponse.SC_CREATED,
                    "User store creation failed");
        }
    }

    /**
     * Get secondary user stores.
     *
     * @return JSONArray element of the user stores.
     * @throws Exception If an error occurred while getting a user store.
     */
    public JSONArray getUserStores() throws Exception {

        try (CloseableHttpResponse response = getResponseOfHttpGet(userStoreBasePath, getHeaders())) {
            return getJSONArray(EntityUtils.toString(response.getEntity()));
        }
    }

    /**
     * Get secondary user stores in a sub organization.
     *
     * @param switchedM2MToken Switched M2M token.
     * @return JSONArray element of the user stores.
     * @throws Exception If an error occurred while getting a user store.
     */
    public JSONArray getSubOrgUserStores(String switchedM2MToken) throws Exception {

        try (CloseableHttpResponse response = getResponseOfHttpGet(userStoreSubOrgBasePath,
                getHeadersWithBearerToken(switchedM2MToken))) {
            return getJSONArray(EntityUtils.toString(response.getEntity()));
        }
    }

    /**
     * Delete a user store.
     *
     * @param domain User store domain(id).
     * @throws IOException If an error occurred while deleting a user store.
     */
    public void deleteUserStore(String domain) throws IOException {

        String endpointUrl = userStoreBasePath + PATH_SEPARATOR + domain;

        try (CloseableHttpResponse response = getResponseOfHttpDelete(endpointUrl, getHeaders())) {
            Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpServletResponse.SC_NO_CONTENT,
                    "Application deletion failed");
        }
    }

    /**
     * Delete a user store in a sub organization.
     *
     * @param domain           User store domain(id).
     * @param switchedM2MToken Switched M2M token.
     * @throws IOException If an error occurred while deleting a user store.
     */
    public void deleteSubOrgUserStore(String domain, String switchedM2MToken) throws IOException {

        String endpointUrl = userStoreSubOrgBasePath + PATH_SEPARATOR + domain;

        try (CloseableHttpResponse response = getResponseOfHttpDelete(endpointUrl,
                getHeadersWithBearerToken(switchedM2MToken))) {
            Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpServletResponse.SC_NO_CONTENT,
                    "User store deletion failed");
        }
    }

    /**
     * Check user store deployment.
     *
     * @param domain User Store name.
     * @return boolean response of the user store deployment.
     * @throws Exception If an error occurred while checking the user store creation.
     */
    public boolean waitForUserStoreDeployment(String domain) throws Exception {

        long waitTime = System.currentTimeMillis() + TIMEOUT_MILLIS; //wait for 30 seconds
        while (System.currentTimeMillis() < waitTime) {
            JSONArray userStores = getUserStores();
            for (Object userStore : userStores) {
                String userStoreName = ((JSONObject) userStore).get("name").toString();
                if (userStoreName.equalsIgnoreCase(domain)) {
                    return true;
                }
            }
            Thread.sleep(POLLING_INTERVAL_MILLIS);
        }
        return false;
    }

    /**
     * Check user store deployment in a sub organization.
     *
     * @param domain   User Store name.
     * @param switchedM2MToken Switched M2M token.
     * @return True if the user store is deployed.
     * @throws Exception If an error occurred while checking the user store creation.
     */
    public boolean waitForSubOrgUserStoreDeployment(String domain, String switchedM2MToken) throws Exception {

        long waitTime = System.currentTimeMillis() + TIMEOUT_MILLIS; //wait for 30 seconds
        while (System.currentTimeMillis() < waitTime) {
            JSONArray userStores = getSubOrgUserStores(switchedM2MToken);
            for (Object userStore : userStores) {
                String userStoreName = ((JSONObject) userStore).get("name").toString();
                if (userStoreName.equalsIgnoreCase(domain)) {
                    return true;
                }
            }
            Thread.sleep(POLLING_INTERVAL_MILLIS);
        }
        return false;
    }

    private Header[] getHeaders() {

        Header[] headerList = new Header[2];
        headerList[0] = new BasicHeader(AUTHORIZATION_ATTRIBUTE, BASIC_AUTHORIZATION_ATTRIBUTE +
                Base64.encodeBase64String((username + ":" + password).getBytes()).trim());
        headerList[1] = new BasicHeader(CONTENT_TYPE_ATTRIBUTE, String.valueOf(ContentType.JSON));

        return headerList;
    }

    private Header[] getHeadersWithBearerToken(String accessToken) {

        Header[] headerList = new Header[2];
        headerList[0] = new BasicHeader(AUTHORIZATION_ATTRIBUTE, BEARER_TOKEN_AUTHORIZATION_ATTRIBUTE + accessToken);
        headerList[1] = new BasicHeader(CONTENT_TYPE_ATTRIBUTE, String.valueOf(ContentType.JSON));

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

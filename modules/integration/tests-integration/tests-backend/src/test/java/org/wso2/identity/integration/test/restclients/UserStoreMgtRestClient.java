/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
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
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.testng.Assert;
import org.wso2.carbon.automation.engine.context.beans.Tenant;
import org.wso2.carbon.identity.user.store.configuration.stub.dto.UserStoreDTO;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;
import org.wso2.identity.integration.test.rest.api.server.user.store.v1.model.UserStoreReq;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class UserStoreMgtRestClient extends RestBaseClient {
    private static final String API_SERVER_BASE_PATH = "/api/server/v1";
    public static final String USER_STORES_ENDPOINT_URI = "/userstores";
    private final CloseableHttpClient client;
    private final String username;
    private final String password;
    private final String userStoreBasePath;

    public UserStoreMgtRestClient(String backendURL, Tenant tenantInfo) {
        client = HttpClients.createDefault();

        this.username = tenantInfo.getContextUser().getUserName();
        this.password = tenantInfo.getContextUser().getPassword();
        String tenantDomain = tenantInfo.getContextUser().getUserDomain();

        userStoreBasePath = backendURL + ISIntegrationTest.getTenantedRelativePath(API_SERVER_BASE_PATH
                + USER_STORES_ENDPOINT_URI, tenantDomain);
    }

    /**
     * Add a secondary user store.
     *
     * @param UserStoreReq Secondary user store request object.
     */
    public String addUserStore(UserStoreReq UserStoreReq) throws Exception {
        String jsonRequest = toJSONString(UserStoreReq);
        try (CloseableHttpResponse response = getResponseOfHttpPost(userStoreBasePath, jsonRequest, getHeaders())) {
            String[] locationElements = response.getHeaders(LOCATION_HEADER)[0].toString().split(PATH_SEPARATOR);
            return locationElements[locationElements.length - 1];
        }
    }

    /**
     * Get secondary user stores.
     *
     * @return JSONArray element of the user stores.
     */
    public JSONArray getUserStores() throws Exception {

        try (CloseableHttpResponse response = getResponseOfHttpGet(userStoreBasePath, getHeaders())) {
            return getJSONArray(EntityUtils.toString(response.getEntity()));
        }
    }

    /**
     * Delete a user store
     *
     * @param domain User store domain(id).
     */
    public void deleteUserStore(String domain) throws IOException {
        String endpointUrl = userStoreBasePath + PATH_SEPARATOR + domain;

        try (CloseableHttpResponse response = getResponseOfHttpDelete(endpointUrl, getHeaders())) {
            Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpServletResponse.SC_NO_CONTENT,
                    "Application deletion failed");
        }
    }

    /**
     * Check user store deployment
     *
     * @param domain User Store name
     * @return boolean response of the user store deployment.
     */
    public boolean waitForUserStoreDeployment(String domain) throws Exception {

        long waitTime = System.currentTimeMillis() + 30000; //wait for 30 seconds
        while (System.currentTimeMillis() < waitTime) {
            JSONArray userStores = getUserStores();
            for (Object userStore : userStores) {
                String userStoreName = ((JSONObject) userStore).get("name").toString();
                if (userStoreName.equalsIgnoreCase(domain)) {
                    return true;
                }
            }
            Thread.sleep(500);
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

    /**
     * Close the HTTP client.
     */
    public void closeHttpClient() throws IOException {
        client.close();
    }
}

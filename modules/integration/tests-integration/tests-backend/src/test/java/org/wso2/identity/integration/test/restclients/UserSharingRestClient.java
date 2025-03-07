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

import io.restassured.http.ContentType;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.message.BasicHeader;
import org.testng.Assert;
import org.wso2.carbon.automation.engine.context.beans.Tenant;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;
import org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v1.model.UserShareRequestBody;
import org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v1.model.UserShareWithAllRequestBody;
import org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v1.model.UserUnshareRequestBody;
import org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v1.model.UserUnshareWithAllRequestBody;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

/**
 * Rest client for user sharing.
 */
public class UserSharingRestClient extends RestBaseClient {

    private static final String API_SERVER_BASE_PATH = "/api/server/v1";
    static final String USER_SHARING_API_BASE_PATH = "/users";
    static final String SHARE_PATH = "/share";
    static final String SHARE_WITH_ALL_PATH = "/share-with-all";
    static final String UNSHARE_PATH = "/unshare";
    static final String UNSHARE_WITH_ALL_PATH = "/unshare-with-all";
    static final String SHARED_ORGANIZATIONS_PATH = "/shared-organizations";
    static final String SHARED_ROLES_PATH = "/shared-roles";

    public static final String PATH_SEPARATOR = "/";

    private final String serverUrl;
    private final String tenantDomain;
    private final String username;
    private final String password;

    private final String selectiveUserShareEndpoint;
    private final String generalUserShareEndpoint;
    private final String selectiveUserUnshareEndpoint;
    private final String generalUserUnshareEndpoint;

    public UserSharingRestClient(String serverUrl, Tenant tenantInfo) {

        this.serverUrl = serverUrl;
        this.tenantDomain = tenantInfo.getContextUser().getUserDomain();
        this.username = tenantInfo.getContextUser().getUserName();
        this.password = tenantInfo.getContextUser().getPassword();

        selectiveUserShareEndpoint = serverUrl + ISIntegrationTest.getTenantedRelativePath(
                API_SERVER_BASE_PATH + USER_SHARING_API_BASE_PATH + SHARE_PATH, tenantDomain);
        generalUserShareEndpoint = serverUrl + ISIntegrationTest.getTenantedRelativePath(
                API_SERVER_BASE_PATH + USER_SHARING_API_BASE_PATH + SHARE_WITH_ALL_PATH, tenantDomain);
        selectiveUserUnshareEndpoint = serverUrl + ISIntegrationTest.getTenantedRelativePath(
                API_SERVER_BASE_PATH + USER_SHARING_API_BASE_PATH + UNSHARE_PATH, tenantDomain);
        generalUserUnshareEndpoint = serverUrl + ISIntegrationTest.getTenantedRelativePath(
                API_SERVER_BASE_PATH + USER_SHARING_API_BASE_PATH + UNSHARE_WITH_ALL_PATH, tenantDomain);

    }

    /**
     * Share users with all.
     *
     * @param userShareRequestBody Selective User Share request body.
     * @throws Exception If an error occurs while sharing users with all.
     */
    public void shareUsers(UserShareRequestBody userShareRequestBody) throws Exception {

        String jsonRequest = toJSONString(userShareRequestBody);
        try (CloseableHttpResponse response = getResponseOfHttpPost(selectiveUserShareEndpoint, jsonRequest,
                getHeaders())) {
            Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpServletResponse.SC_ACCEPTED,
                    "Selective User Sharing request accepted.");
        }
    }

    /**
     * Share users with all.
     *
     * @param userShareWithAllRequestBody General User Share request body.
     * @throws Exception If an error occurs while sharing users with all.
     */
    public void shareUsersWithAll(UserShareWithAllRequestBody userShareWithAllRequestBody) throws Exception {

        String jsonRequest = toJSONString(userShareWithAllRequestBody);
        try (CloseableHttpResponse response = getResponseOfHttpPost(generalUserShareEndpoint, jsonRequest,
                getHeaders())) {
            Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpServletResponse.SC_ACCEPTED,
                    "General User Sharing request accepted.");
        }
    }

    /**
     * Unshare users with all.
     *
     * @param userUnshareRequestBody Selective User Unshare request body.
     * @throws Exception If an error occurs while unsharing users with all.
     */
    public void unshareUsers(UserUnshareRequestBody userUnshareRequestBody) throws Exception {

        String jsonRequest = toJSONString(userUnshareRequestBody);
        try (CloseableHttpResponse response = getResponseOfHttpPost(selectiveUserUnshareEndpoint, jsonRequest,
                getHeaders())) {
            Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpServletResponse.SC_ACCEPTED,
                    "Selective User Unsharing request accepted.");
        }
    }

    /**
     * Unshare users with all.
     *
     * @param userUnshareWithAllRequestBody General User Unshare request body.
     * @throws Exception If an error occurs while unsharing users with all.
     */
    public void unshareUsersWithAll(UserUnshareWithAllRequestBody userUnshareWithAllRequestBody) throws Exception {

        String jsonRequest = toJSONString(userUnshareWithAllRequestBody);
        try (CloseableHttpResponse response = getResponseOfHttpPost(generalUserUnshareEndpoint, jsonRequest,
                getHeaders())) {
            Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpServletResponse.SC_ACCEPTED,
                    "General User Unsharing request accepted.");
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

    private Header[] getHeaders() {

        Header[] headerList = new Header[2];
        headerList[0] = new BasicHeader(AUTHORIZATION_ATTRIBUTE, BASIC_AUTHORIZATION_ATTRIBUTE +
                Base64.encodeBase64String((username + ":" + password).getBytes()).trim());
        headerList[1] = new BasicHeader(CONTENT_TYPE_ATTRIBUTE, String.valueOf(ContentType.JSON));
        return headerList;
    }
}

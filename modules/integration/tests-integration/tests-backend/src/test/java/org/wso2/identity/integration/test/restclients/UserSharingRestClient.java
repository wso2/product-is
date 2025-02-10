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
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.context.beans.Tenant;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;
import org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v1.model.UserShareWithAllRequestBody;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

/**
 * Rest client for user sharing.
 */
public class UserSharingRestClient extends RestBaseClient {

    private static final String API_SERVER_BASE_PATH = "/api/server/v1";
    public static final String USER_SHARE_WITH_ALL_ENDPOINT_URI = "/users/share-with-all";
    public static final String PATH_SEPARATOR = "/";
    private final String serverUrl;
    private final String tenantDomain;
    private final String username;
    private final String password;
    private final String userShareWithAllBasePath;

    public UserSharingRestClient(String serverUrl, Tenant tenantInfo) {

        this.serverUrl = serverUrl;
        this.tenantDomain = tenantInfo.getContextUser().getUserDomain();
        this.username = tenantInfo.getContextUser().getUserName();
        this.password = tenantInfo.getContextUser().getPassword();

        userShareWithAllBasePath = serverUrl +
                ISIntegrationTest.getTenantedRelativePath(API_SERVER_BASE_PATH + USER_SHARE_WITH_ALL_ENDPOINT_URI,
                        tenantDomain);
    }

    /**
     * Share users with all.
     *
     * @param userShareWithAllRequestBody User share with all request body.
     * @throws Exception If an error occurs while sharing users with all.
     */
    public void shareUsersWithAll(UserShareWithAllRequestBody userShareWithAllRequestBody) throws Exception {

        String jsonRequest = toJSONString(userShareWithAllRequestBody);
        try (CloseableHttpResponse response = getResponseOfHttpPost(userShareWithAllBasePath, jsonRequest,
                getHeaders())) {
            Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpServletResponse.SC_ACCEPTED,
                    "User sharing request accepted.");
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

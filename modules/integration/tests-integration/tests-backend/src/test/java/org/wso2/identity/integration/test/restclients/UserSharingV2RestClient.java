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

import io.restassured.http.ContentType;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.wso2.carbon.automation.engine.context.beans.Tenant;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.model.UserShareRequestBody;
import org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.model.UserShareWithAllRequestBody;
import org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.model.UserUnshareRequestBody;
import org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.model.UserUnshareWithAllRequestBody;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import static org.testng.Assert.assertEquals;

/**
 * Rest client for the user sharing management v2 API.
 */
public class UserSharingV2RestClient extends RestBaseClient {

    private static final String API_SERVER_V2_PATH = "api/server/v2";
    private static final String USERS_PATH = "/users";
    private static final String SHARE_PATH = "/share";
    private static final String SHARE_WITH_ALL_PATH = "/share-with-all";
    private static final String UNSHARE_PATH = "/unshare";
    private static final String UNSHARE_WITH_ALL_PATH = "/unshare-with-all";

    private final String username;
    private final String password;
    private final String usersAPIBaseURL;

    public UserSharingV2RestClient(String serverUrl, Tenant tenantInfo) {

        String tenantDomain = tenantInfo.getContextUser().getUserDomain();
        this.username = tenantInfo.getContextUser().getUserName();
        this.password = tenantInfo.getContextUser().getPassword();
        this.usersAPIBaseURL = getUsersAPIBaseURL(serverUrl, tenantDomain);
    }

    /**
     * Share users with the selected organizations.
     *
     * @param requestBody Selective user share request body.
     * @throws IOException If an error occurs while sending the request.
     */
    public void shareUsers(UserShareRequestBody requestBody) throws IOException {

        postAndAssertAccepted(usersAPIBaseURL + SHARE_PATH, toJSONString(requestBody));
    }

    /**
     * Share users with all organizations.
     *
     * @param requestBody General user share request body.
     * @throws IOException If an error occurs while sending the request.
     */
    public void shareUsersWithAll(UserShareWithAllRequestBody requestBody) throws IOException {

        postAndAssertAccepted(usersAPIBaseURL + SHARE_WITH_ALL_PATH, toJSONString(requestBody));
    }

    /**
     * Unshare users from the selected organizations.
     *
     * @param requestBody Selective user unshare request body.
     * @throws IOException If an error occurs while sending the request.
     */
    public void unshareUsers(UserUnshareRequestBody requestBody) throws IOException {

        postAndAssertAccepted(usersAPIBaseURL + UNSHARE_PATH, toJSONString(requestBody));
    }

    /**
     * Unshare users from all organizations.
     *
     * @param requestBody General user unshare request body.
     * @throws IOException If an error occurs while sending the request.
     */
    public void unshareUsersWithAll(UserUnshareWithAllRequestBody requestBody) throws IOException {

        postAndAssertAccepted(usersAPIBaseURL + UNSHARE_WITH_ALL_PATH, toJSONString(requestBody));
    }

    /**
     * Get the organizations a user is shared with.
     *
     * @param userId ID of the user.
     * @return The response, with the shared organizations under "organizations".
     * @throws IOException If an error occurs while sending the request.
     * @throws JSONException If the response is not valid JSON.
     */
    public JSONObject getSharedOrganizations(String userId) throws IOException, JSONException {

        String endpointUrl = usersAPIBaseURL + PATH_SEPARATOR + userId + SHARE_PATH;
        try (CloseableHttpResponse response = getResponseOfHttpGet(endpointUrl, getHeaders())) {
            assertEquals(response.getStatusLine().getStatusCode(), HttpServletResponse.SC_OK,
                    "Failed to get the shared organizations of the user: " + userId);
            return new JSONObject(EntityUtils.toString(response.getEntity()));
        }
    }

    /**
     * Close the HTTP client.
     *
     * @throws IOException If an error occurred while closing the HTTP client.
     */
    public void closeHttpClient() throws IOException {

        client.close();
    }

    private void postAndAssertAccepted(String endpointUrl, String jsonRequest) throws IOException {

        try (CloseableHttpResponse response = getResponseOfHttpPost(endpointUrl, jsonRequest, getHeaders())) {
            assertEquals(response.getStatusLine().getStatusCode(), HttpServletResponse.SC_ACCEPTED,
                    "User sharing request was not accepted: " + endpointUrl);
        }
    }

    private String getUsersAPIBaseURL(String serverUrl, String tenantDomain) {

        if (MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
            return serverUrl + API_SERVER_V2_PATH + USERS_PATH;
        }
        return serverUrl + TENANT_PATH + tenantDomain + PATH_SEPARATOR + API_SERVER_V2_PATH + USERS_PATH;
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

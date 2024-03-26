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
import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONObject;
import org.wso2.identity.integration.test.rest.api.user.common.model.AuthenticationRequest;

import java.io.IOException;

public class AuthenticatorRestClient extends RestBaseClient {

    private final String serverUrl;
    private final String AUTHENTICATION_BASE_PATH = "api/identity/auth/v1.1/authenticate";

    public AuthenticatorRestClient(String serverUrl) {

        this.serverUrl = serverUrl;
    }

    /**
     * Login operation.
     *
     * @param username username.
     * @param password password.
     * @return JSONObject with login details.
     * @throws Exception If an error occurred while authenticating.
     */
    public JSONObject login(String username, String password) throws Exception {

        AuthenticationRequest loginRequest = new AuthenticationRequest();
        loginRequest.setUsername(username);
        loginRequest.setPassword(password);

        String jsonRequest = toJSONString(loginRequest);
        String endPointUrl =  serverUrl + AUTHENTICATION_BASE_PATH;

        try (CloseableHttpResponse response = getResponseOfHttpPost(endPointUrl, jsonRequest, getHeaders())) {
            return getJSONObject(EntityUtils.toString(response.getEntity()));
        }
    }

    private Header[] getHeaders() {

        return new Header[]{new BasicHeader(CONTENT_TYPE_ATTRIBUTE, String.valueOf(ContentType.JSON))};
    }

    /**
     * Close the HTTP client.
     *
     * @throws IOException Exception.
     */
    public void closeHttpClient() throws IOException {

        client.close();
    }
}

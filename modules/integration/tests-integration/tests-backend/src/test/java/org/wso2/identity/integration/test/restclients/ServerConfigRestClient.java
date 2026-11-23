/*
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
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
import org.json.simple.JSONObject;
import org.testng.Assert;
import org.wso2.carbon.automation.engine.context.beans.Tenant;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Rest client for the server configuration management (/configs) REST API.
 * Unlike the RestAssured based test bases, this client keeps no static state, so test classes that use it can run
 * concurrently against different tenants.
 */
public class ServerConfigRestClient extends RestBaseClient {

    private static final String CONFIGS_PATH = "/configs";

    private final String configsEndpoint;
    private final String username;
    private final String password;

    public ServerConfigRestClient(String serverUrl, Tenant tenantInfo) {

        this.username = tenantInfo.getContextUser().getUserName();
        this.password = tenantInfo.getContextUser().getPassword();
        this.configsEndpoint = serverUrl + TENANT_PATH + tenantInfo.getContextUser().getUserDomain() +
                PATH_SEPARATOR + API_SERVER_PATH + CONFIGS_PATH;
    }

    /**
     * Get the server configurations of the tenant.
     *
     * @return Server configurations.
     * @throws Exception If an error occurred while getting the server configurations.
     */
    public JSONObject getServerConfigurations() throws Exception {

        try (CloseableHttpResponse response = getResponseOfHttpGet(configsEndpoint, getHeaders())) {
            Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpServletResponse.SC_OK,
                    "Failed to get the server configurations.");
            return getJSONObject(EntityUtils.toString(response.getEntity()));
        }
    }

    /**
     * Patch the server configurations of the tenant.
     *
     * @param patchRequest JSON array of patch operations.
     * @throws IOException If an error occurred while patching the server configurations.
     */
    public void patchServerConfigurations(String patchRequest) throws IOException {

        try (CloseableHttpResponse response = getResponseOfHttpPatch(configsEndpoint, patchRequest, getHeaders())) {
            Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpServletResponse.SC_OK,
                    "Failed to patch the server configurations.");
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

        Header[] headerList = new Header[3];
        headerList[0] = new BasicHeader(USER_AGENT_ATTRIBUTE, OAuth2Constant.USER_AGENT);
        headerList[1] = new BasicHeader(AUTHORIZATION_ATTRIBUTE, BASIC_AUTHORIZATION_ATTRIBUTE +
                Base64.encodeBase64String((username + ":" + password).getBytes()).trim());
        headerList[2] = new BasicHeader(CONTENT_TYPE_ATTRIBUTE, String.valueOf(ContentType.JSON));
        return headerList;
    }
}

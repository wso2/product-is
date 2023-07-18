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

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.http.ContentType;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONObject;
import org.testng.Assert;
import org.wso2.carbon.automation.engine.context.beans.Tenant;
import org.wso2.identity.integration.test.rest.api.server.oidc.scope.management.v1.model.ScopeUpdateRequest;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class OIDCScopeMgtRestClient extends RestBaseClient {
    private final String serverUrl;
    private final String OIDC_SCOPE_MGT_BASE_PATH = "t/%s/api/server/v1/oidc/scopes";
    private final String tenantDomain;
    private final String username;
    private final String password;

    public OIDCScopeMgtRestClient(String serverUrl, Tenant tenantInfo) {

        this.serverUrl = serverUrl;
        this.tenantDomain = tenantInfo.getContextUser().getUserDomain();
        this.username = tenantInfo.getContextUser().getUserName();
        this.password = tenantInfo.getContextUser().getPassword();
    }

    /**
     * Get an OIDC scope
     *
     * @param scopeId userId.
     * @return Scope object.
     */
    public JSONObject getScope(String scopeId) throws Exception {
        String endPointUrl = serverUrl + String.format(OIDC_SCOPE_MGT_BASE_PATH, tenantDomain) +
                PATH_SEPARATOR +  scopeId;

        try (CloseableHttpResponse response = getResponseOfHttpGet(endPointUrl, getHeaders())) {
            return getJSONObject(EntityUtils.toString(response.getEntity()));
        }
    }

    /**
     * update an OIDC scope
     *
     * @param scopeId userId.
     * @param scopeUpdateObj Scope update request object.
     */
    public void updateScope(String scopeId, ScopeUpdateRequest scopeUpdateObj) throws Exception {
        String jsonRequest = toJSONString(scopeUpdateObj);
        String endPointUrl = serverUrl + String.format(OIDC_SCOPE_MGT_BASE_PATH, tenantDomain) +
                PATH_SEPARATOR +  scopeId;

        try (CloseableHttpResponse response = getResponseOfHttpPut(endPointUrl, jsonRequest, getHeaders())) {
            Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpServletResponse.SC_OK,
                    "Scope update failed");
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

    /**
     * Close the HTTP client.
     *
     */
    public void closeHttpClient() throws IOException {
        client.close();
    }
}

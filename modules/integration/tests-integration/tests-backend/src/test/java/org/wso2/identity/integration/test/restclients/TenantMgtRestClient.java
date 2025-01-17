/*
 * Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com).
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

import com.google.gson.Gson;
import io.restassured.http.ContentType;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONObject;
import org.testng.Assert;
import org.wso2.carbon.automation.engine.context.beans.Tenant;
import org.wso2.identity.integration.test.rest.api.server.tenant.management.v1.model.TenantModel;
import org.wso2.identity.integration.test.rest.api.user.common.model.UserObject;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class TenantMgtRestClient extends RestBaseClient {

    private final String serverUrl;
    private final String tenantDomain;
    private final String username;
    private final String password;
    private static final String TENANT_MGT_BASE_PATH = "api/server/v1/tenants";

    public TenantMgtRestClient(String serverUrl, Tenant tenantInfo) {

        this.serverUrl = serverUrl;
        this.tenantDomain = tenantInfo.getContextUser().getUserDomain();
        this.username = tenantInfo.getContextUser().getUserName();
        this.password = tenantInfo.getContextUser().getPassword();
    }

    /**
     * Create a tenant
     *
     * @param TenantReqModel object with tenant creation details.
     * @return Id of the created tenant.
     * @throws Exception If an error occurred while adding a tenant.
     */
    public String addTenant(TenantModel TenantReqModel) throws Exception {
        String endPoint = serverUrl + TENANT_MGT_BASE_PATH;
        String jsonRequest = toJSONString(TenantReqModel);

        try (CloseableHttpResponse response = getResponseOfHttpPost(endPoint, jsonRequest, getHeaders())) {
            Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpServletResponse.SC_CREATED,
                    "Tenant creation failed");
            String[] locationElements = response.getHeaders(LOCATION_HEADER)[0].toString().split(PATH_SEPARATOR);
            return locationElements[locationElements.length - 1];
        }
    }

    /**
     * Create a tenant
     *
     * @param tenantId Id of the tenant.
     * @return TenantModel object with tenant details.
     * @throws Exception If an error occurred while adding a tenant.
     */
    public TenantModel getTenantById(String tenantId) throws Exception {
        String endPoint = serverUrl + TENANT_MGT_BASE_PATH + PATH_SEPARATOR + tenantId;

        try (CloseableHttpResponse response = getResponseOfHttpGet(endPoint, getHeaders())) {
            Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpServletResponse.SC_OK,
                    "Tenant retrieval failed");
            //convert the response to a TenantModel object
            return new Gson().fromJson(EntityUtils.toString(response.getEntity()), TenantModel.class);
        }
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
     *
     * @throws IOException If an error occurred while closing the Http Client.
     */
    public void closeHttpClient() throws IOException {

        client.close();
    }
}

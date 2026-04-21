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

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.http.ContentType;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.Header;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.testng.Assert;
import org.wso2.carbon.automation.engine.context.beans.Tenant;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.identity.integration.test.rest.api.server.vc.template.management.v1.model.VCTemplate;
import org.wso2.identity.integration.test.rest.api.server.vc.template.management.v1.model.VCTemplateCreateRequest;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.io.IOException;

/**
 * Rest client for VC template management APIs.
 */
public class VCTemplateManagementRestClient extends RestBaseClient {

    private static final String VC_TEMPLATE_MANAGEMENT_PATH = "vc-templates";
    private final String vcTemplateManagementBasePath;
    private final String username;
    private final String password;

    public VCTemplateManagementRestClient(String serverUrl, Tenant tenantInfo) {

        String tenantDomain = tenantInfo.getContextUser().getUserDomain();
        this.username = tenantInfo.getContextUser().getUserName();
        this.password = tenantInfo.getContextUser().getPassword();
        this.vcTemplateManagementBasePath = getVCTemplateManagementPath(serverUrl, tenantDomain);
    }

    /**
     * Create a VC template.
     *
     * @param request VC template create request.
     * @return Created template response.
     * @throws Exception If request fails.
     */
    public VCTemplate createTemplate(VCTemplateCreateRequest request) throws Exception {

        String requestBody = toJSONString(request);
        try (CloseableHttpResponse response = getResponseOfHttpPost(vcTemplateManagementBasePath, requestBody,
                getHeadersWithBasicAuth())) {
            int statusCode = response.getStatusLine().getStatusCode();
            Assert.assertTrue(statusCode == HttpStatus.SC_CREATED || statusCode == HttpStatus.SC_OK,
                    "VC template creation failed. Status: " + statusCode);
            String responseBody = EntityUtils.toString(response.getEntity());
            if (responseBody == null || responseBody.trim().isEmpty()) {
                String locationHeader = response.getFirstHeader(LOCATION_HEADER) != null ?
                        response.getFirstHeader(LOCATION_HEADER).getValue() : null;
                Assert.assertNotNull(locationHeader, "VC template create response does not contain a body or Location.");
                String[] locationElements = locationHeader.split(PATH_SEPARATOR);
                String templateId = locationElements[locationElements.length - 1];
                return getTemplate(templateId);
            }
            ObjectMapper jsonWriter = new ObjectMapper(new JsonFactory());
            return jsonWriter.readValue(responseBody, VCTemplate.class);
        }
    }

    /**
     * Get VC template by id.
     *
     * @param templateId Template id.
     * @return VC template response.
     * @throws Exception If request fails.
     */
    public VCTemplate getTemplate(String templateId) throws Exception {

        String endpoint = vcTemplateManagementBasePath + PATH_SEPARATOR + templateId;
        try (CloseableHttpResponse response = getResponseOfHttpGet(endpoint, getHeadersWithBasicAuth())) {
            int statusCode = response.getStatusLine().getStatusCode();
            Assert.assertEquals(statusCode, HttpStatus.SC_OK, "Get VC template failed.");
            String responseBody = EntityUtils.toString(response.getEntity());
            ObjectMapper jsonWriter = new ObjectMapper(new JsonFactory());
            return jsonWriter.readValue(responseBody, VCTemplate.class);
        }
    }

    /**
     * Delete VC template.
     *
     * @param templateId Template id.
     * @throws Exception If request fails.
     */
    public void deleteTemplate(String templateId) throws Exception {

        String endpoint = vcTemplateManagementBasePath + PATH_SEPARATOR + templateId;
        try (CloseableHttpResponse response = getResponseOfHttpDelete(endpoint, getHeadersWithBasicAuth())) {
            int statusCode = response.getStatusLine().getStatusCode();
            Assert.assertTrue(statusCode == HttpStatus.SC_NO_CONTENT || statusCode == HttpStatus.SC_NOT_FOUND,
                    "Delete VC template failed. Status: " + statusCode);
        }
    }

    public void closeHttpClient() throws IOException {

        client.close();
    }

    private String getVCTemplateManagementPath(String serverUrl, String tenantDomain) {

        if (tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
            return serverUrl + API_SERVER_PATH + PATH_SEPARATOR + VC_TEMPLATE_MANAGEMENT_PATH;
        }
        return serverUrl + TENANT_PATH + tenantDomain + PATH_SEPARATOR + API_SERVER_PATH + PATH_SEPARATOR +
                VC_TEMPLATE_MANAGEMENT_PATH;
    }

    private Header[] getHeadersWithBasicAuth() {

        Header[] headerList = new Header[3];
        headerList[0] = new BasicHeader(USER_AGENT_ATTRIBUTE, OAuth2Constant.USER_AGENT);
        headerList[1] = new BasicHeader(AUTHORIZATION_ATTRIBUTE, BASIC_AUTHORIZATION_ATTRIBUTE +
                Base64.encodeBase64String((username + ":" + password).getBytes()).trim());
        headerList[2] = new BasicHeader(CONTENT_TYPE_ATTRIBUTE, String.valueOf(ContentType.JSON));
        return headerList;
    }
}

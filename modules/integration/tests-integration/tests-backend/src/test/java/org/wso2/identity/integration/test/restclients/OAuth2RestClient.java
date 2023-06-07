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

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.http.ContentType;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.testng.Assert;
import org.wso2.carbon.automation.engine.context.beans.Tenant;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationListResponse;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationPatchModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationResponseModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.OpenIDConnectConfiguration;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.SAML2ServiceProvider;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class OAuth2RestClient extends RestBaseClient {

    private static final String API_SERVER_BASE_PATH = "api/server/v1";
    private static final String APPLICATION_MANAGEMENT_PATH = "/applications";
    private static final String INBOUND_PROTOCOLS_BASE_PATH = "/inbound-protocols";
    private final String applicationManagementApiBasePath;
    private final String username;
    private final String password;

    public OAuth2RestClient(String backendUrl, Tenant tenantInfo) {
        this.username = tenantInfo.getContextUser().getUserName();
        this.password = tenantInfo.getContextUser().getPassword();

        String tenantDomain = tenantInfo.getContextUser().getUserDomain();
        applicationManagementApiBasePath = getApplicationsPath(backendUrl, tenantDomain);
    }

    public String createApplication(ApplicationModel application) throws IOException, JSONException {
        String jsonRequest = toJSONString(application);

        CloseableHttpResponse response = getResponseOfHttpPost(applicationManagementApiBasePath, jsonRequest,
                getHeaders());
        response.close();

        String[] self = response.getHeaders(LOCATION_HEADER)[0].toString().split(PATH_SEPARATOR);

        return self[self.length-1];
    }

    public ApplicationResponseModel getApplication(String appId) throws IOException {
        String endPointUrl = applicationManagementApiBasePath + PATH_SEPARATOR + appId;
        CloseableHttpResponse response = getResponseOfHttpGet(endPointUrl, getHeaders());

        String responseBody = EntityUtils.toString(response.getEntity());
        response.close();

        ObjectMapper jsonWriter = new ObjectMapper(new JsonFactory());

        return jsonWriter.readValue(responseBody, ApplicationResponseModel.class);
    }

    public void updateApplication(String appId, ApplicationPatchModel application) throws IOException {
        String jsonRequest = toJSONString(application);
        String endPointUrl = applicationManagementApiBasePath + PATH_SEPARATOR + appId;
        CloseableHttpResponse response = getResponseOfHttpPatch(endPointUrl, jsonRequest, getHeaders());

        Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpServletResponse.SC_OK,
                "Application update failed");
        response.close();
    }

    public ApplicationListResponse getAllApplications() throws IOException {
        CloseableHttpResponse response = getResponseOfHttpGet(applicationManagementApiBasePath, getHeaders());

        String responseBody = EntityUtils.toString(response.getEntity());
        response.close();

        ObjectMapper jsonWriter = new ObjectMapper(new JsonFactory());

        return jsonWriter.readValue(responseBody, ApplicationListResponse.class);
    }

    public void deleteApplication(String appId) throws IOException {
        String endpointUrl = applicationManagementApiBasePath + PATH_SEPARATOR + appId;
        CloseableHttpResponse response = getResponseOfHttpDelete(endpointUrl, getHeaders());

        Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpServletResponse.SC_NO_CONTENT,
                "Application deletion failed");
        response.close();
    }
    public String getConfig(String appId, String inboundType) throws Exception {
        String endPointUrl = applicationManagementApiBasePath + PATH_SEPARATOR + appId + INBOUND_PROTOCOLS_BASE_PATH +
                PATH_SEPARATOR + inboundType;
        CloseableHttpResponse response = getResponseOfHttpGet(endPointUrl, getHeaders());

        String responseBody = EntityUtils.toString(response.getEntity());
        response.close();

        return responseBody;
    }

    public OpenIDConnectConfiguration getOIDCInboundDetails(String appId) throws Exception {
        String responseBody = getConfig(appId, OIDC);
        ObjectMapper jsonWriter = new ObjectMapper(new JsonFactory());
        return jsonWriter.readValue(responseBody, OpenIDConnectConfiguration.class);
    }

    public SAML2ServiceProvider getSAMLInboundDetails(String appId) throws Exception {
        String responseBody = getConfig(appId, SAML);
        ObjectMapper jsonWriter = new ObjectMapper(new JsonFactory());

        return jsonWriter.readValue(responseBody, SAML2ServiceProvider.class);
    }

    public void updateInboundDetailsOfApplication(String appId, Object inboundConfig, String inboundType)
            throws IOException {
        String jsonRequest = toJSONString(inboundConfig);
        String endPointUrl = applicationManagementApiBasePath + PATH_SEPARATOR + appId + INBOUND_PROTOCOLS_BASE_PATH +
                PATH_SEPARATOR + inboundType;
        CloseableHttpResponse response = getResponseOfHttpPut(endPointUrl, jsonRequest, getHeaders());

        Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpServletResponse.SC_OK,
                String.format("Application %s inbound config update failed", inboundType));
        response.close();
    }

    private String getApplicationsPath(String serverUrl, String tenantDomain) {
        if (tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
            return serverUrl + API_SERVER_BASE_PATH + APPLICATION_MANAGEMENT_PATH;
        } else {
            return serverUrl + TENANT_PATH + tenantDomain + PATH_SEPARATOR + API_SERVER_BASE_PATH + APPLICATION_MANAGEMENT_PATH;
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

    public void closeHttpClient() throws IOException {
        client.close();
    }
}

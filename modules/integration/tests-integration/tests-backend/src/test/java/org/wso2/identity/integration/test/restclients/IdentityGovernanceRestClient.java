/**
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
import com.google.gson.GsonBuilder;
import io.restassured.http.ContentType;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.testng.Assert;
import org.wso2.carbon.automation.engine.context.beans.Tenant;
import org.wso2.identity.integration.test.rest.api.server.identity.governance.v1.dto.ConnectorsPatchReq;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class IdentityGovernanceRestClient {

    private static final String TENANT_PATH = "t/%s";
    private static final String API_SERVER_BASE_PATH = "/api/server/v1";
    private static final String IDENTITY_GOVERNANCE_BASE_PATH = "/identity-governance";
    private static final String CONNECTORS_BASE_PATH = "/connectors";
    private static final String PATH_SEPARATOR = "/";
    private static final String BASIC_AUTHORIZATION_ATTRIBUTE = "Basic ";
    private static final String CONTENT_TYPE_ATTRIBUTE = "Content-Type";
    private static final String AUTHORIZATION_ATTRIBUTE = "Authorization";
    private final String identityGovernanceApiBasePath;
    private final CloseableHttpClient client;
    private final String username;
    private final String password;

    public IdentityGovernanceRestClient(String backendURL, Tenant tenantInfo) {
        client = HttpClients.createDefault();

        this.username = tenantInfo.getContextUser().getUserName();
        this.password = tenantInfo.getContextUser().getPassword();

        String tenantDomain = tenantInfo.getContextUser().getUserDomain();

        identityGovernanceApiBasePath = backendURL + String.format(TENANT_PATH, tenantDomain) + API_SERVER_BASE_PATH +
                IDENTITY_GOVERNANCE_BASE_PATH;
    }

    public void updateConnectors(String categoryId, String connectorId, ConnectorsPatchReq connectorPatch)
            throws IOException {
        String jsonRequest = toJSONString(connectorPatch);
        String endPointUrl = identityGovernanceApiBasePath + PATH_SEPARATOR + categoryId +
                CONNECTORS_BASE_PATH + PATH_SEPARATOR + connectorId;

        CloseableHttpResponse response = getResponseOfHttpPatch(endPointUrl, jsonRequest);

        Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpServletResponse.SC_OK,
                "Connector update failed");
        response.close();
    }

    private String toJSONString(java.lang.Object object) {

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(object);
    }

    private CloseableHttpResponse getResponseOfHttpPatch(String endPointUrl, String jsonRequest) throws IOException {

        HttpPatch request = new HttpPatch(endPointUrl);
        request.setHeaders(getHeaders());
        request.setEntity(new StringEntity(jsonRequest));

        return client.execute(request);
    }

    private Header[] getHeaders() {

        Header[] headerList = new Header[2];
        headerList[0] = new BasicHeader(AUTHORIZATION_ATTRIBUTE, BASIC_AUTHORIZATION_ATTRIBUTE +
                Base64.encodeBase64String((username + ":" + password).getBytes()).trim());
        headerList[1] = new BasicHeader(CONTENT_TYPE_ATTRIBUTE, String.valueOf(ContentType.JSON));

        return headerList;
    }

    public void closeHttpClient() throws IOException {
        client.close();
    }
}

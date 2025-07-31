/**
 * Copyright (c) 2023-2025, WSO2 LLC. (https://www.wso2.com).
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
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONObject;
import org.testng.Assert;
import org.wso2.carbon.automation.engine.context.beans.Tenant;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;
import org.wso2.identity.integration.test.rest.api.server.identity.governance.v1.dto.ConnectorsPatchReq;
import org.wso2.identity.integration.test.rest.api.server.identity.governance.v1.dto.PropertyRevertReq;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class IdentityGovernanceRestClient extends RestBaseClient {

    private static final String API_SERVER_BASE_PATH = "/api/server/v1";
    private static final String IDENTITY_GOVERNANCE_BASE_PATH = "/identity-governance";
    private static final String CONNECTORS_BASE_PATH = "/connectors";
    private static final String CONNECTOR_REVERT_PATH = "/revert";
    private static final String CONNECTOR_ID_QUERY_PARAM = "?connectorId=";

    private final String identityGovernanceApiBasePath;
    private final String identityGovernanceSubOrgApiBasePath;
    private final CloseableHttpClient client;
    private final String username;
    private final String password;

    public IdentityGovernanceRestClient(String backendURL, Tenant tenantInfo) {

        client = HttpClients.createDefault();

        this.username = tenantInfo.getContextUser().getUserName();
        this.password = tenantInfo.getContextUser().getPassword();

        String tenantDomain = tenantInfo.getContextUser().getUserDomain();

        identityGovernanceApiBasePath = backendURL + ISIntegrationTest.getTenantedRelativePath(
                API_SERVER_BASE_PATH + IDENTITY_GOVERNANCE_BASE_PATH, tenantDomain);
        identityGovernanceSubOrgApiBasePath = backendURL + ISIntegrationTest.getTenantedOrgRelativePath(
                API_SERVER_BASE_PATH + IDENTITY_GOVERNANCE_BASE_PATH, tenantDomain);
    }

    /**
     * Update connector properties.
     *
     * @param categoryId     Connector category id.
     * @param connectorId    Connector id.
     * @param connectorPatch Connector patch request object.
     * @throws IOException If an error occurred while updating the governance connectors.
     */
    public void updateConnectors(String categoryId, String connectorId, ConnectorsPatchReq connectorPatch)
            throws IOException {

        String jsonRequest = toJSONString(connectorPatch);
        String endPointUrl = identityGovernanceApiBasePath + PATH_SEPARATOR + categoryId +
                CONNECTORS_BASE_PATH + PATH_SEPARATOR + connectorId;

        try (CloseableHttpResponse response = getResponseOfHttpPatch(endPointUrl, jsonRequest, getHeaders())) {
            Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpServletResponse.SC_OK,
                    "Connector update failed");
        }
    }

    /**
     * Get a governance connector in sub-organization.
     *
     * @param categoryId       Connector category id.
     * @param connectorId      Connector id.
     * @param switchedM2MToken M2M token of the sub-organization.
     * @return JSON object representing the governance connector.
     * @throws Exception If an error occurred while retrieving the governance connectors in sub-organization.
     */
    public JSONObject getConnectorInSubOrg(String categoryId, String connectorId, String switchedM2MToken)
            throws Exception {

        String endPointUrl = identityGovernanceSubOrgApiBasePath + PATH_SEPARATOR + categoryId +
                CONNECTORS_BASE_PATH + PATH_SEPARATOR + connectorId;

        try (CloseableHttpResponse response = getResponseOfHttpGet(endPointUrl,
                getHeadersWithBearerToken(switchedM2MToken))) {
            Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpServletResponse.SC_OK,
                    "Connector retrieval failed");
            return getJSONObject(EntityUtils.toString(response.getEntity()));
        }
    }

    /**
     * Update a governance connector in a sub-organization.
     *
     * @param categoryId            Connector category id.
     * @param connectorId           Connector id.
     * @param connectorPatchRequest Connector patch request object.
     * @param switchedM2MToken      M2M token of the sub-organization.
     * @throws IOException If an error occurred while updating the governance connectors in sub-organization.
     */
    public void updateSubOrgConnector(String categoryId, String connectorId, ConnectorsPatchReq connectorPatchRequest,
                                      String switchedM2MToken) throws IOException {

        String endPointUrl = identityGovernanceSubOrgApiBasePath + PATH_SEPARATOR + categoryId +
                CONNECTORS_BASE_PATH + PATH_SEPARATOR + connectorId;

        try (CloseableHttpResponse response = getResponseOfHttpPatch(endPointUrl, toJSONString(connectorPatchRequest),
                getHeadersWithBearerToken(switchedM2MToken))) {
            Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpServletResponse.SC_OK,
                    "Connector update in sub-organization failed");
        }
    }

    /**
     * Revert governance connector properties in a sub-organization.
     *
     * @param categoryId         Connector category id.
     * @param connectorId        Connector id.
     * @param propertyRevertReq  Property revert request object.
     * @param switchedM2MToken   M2M token of the sub-organization.
     * @throws IOException If an error occurred while reverting the governance connector properties in sub-organization.
     */
    public void revertSubOrgConnectorProperties(String categoryId, String connectorId,
                                                PropertyRevertReq propertyRevertReq, String switchedM2MToken)
            throws IOException {

        String endpointUrl = identityGovernanceSubOrgApiBasePath + PATH_SEPARATOR + categoryId +
                CONNECTORS_BASE_PATH + PATH_SEPARATOR + CONNECTOR_REVERT_PATH + CONNECTOR_ID_QUERY_PARAM + connectorId;

        try (CloseableHttpResponse response = getResponseOfHttpPost(endpointUrl,
                toJSONString(propertyRevertReq), getHeadersWithBearerToken(switchedM2MToken))) {
            Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpServletResponse.SC_OK,
                    "Connector property revert in sub-organization failed");
        }
    }

    /**
     * Revert governance connector properties.
     *
     * @param categoryId        Connector category id.
     * @param connectorId       Connector id.
     * @param propertyRevertReq Property revert request object.
     * @throws IOException If an error occurred while reverting the governance connector properties.
     */
    public void revertConnectorProperties(String categoryId, String connectorId,
                                                PropertyRevertReq propertyRevertReq) throws IOException {

        String endpointUrl = identityGovernanceApiBasePath + PATH_SEPARATOR + categoryId +
                CONNECTORS_BASE_PATH + PATH_SEPARATOR + CONNECTOR_REVERT_PATH + CONNECTOR_ID_QUERY_PARAM + connectorId;

        try (CloseableHttpResponse response = getResponseOfHttpPost(endpointUrl,
                toJSONString(propertyRevertReq), getHeaders())) {
            Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpServletResponse.SC_OK,
                    "Connector property revert failed");
        }
    }

    private Header[] getHeaders() {

        Header[] headerList = new Header[2];
        headerList[0] = new BasicHeader(AUTHORIZATION_ATTRIBUTE, BASIC_AUTHORIZATION_ATTRIBUTE +
                Base64.encodeBase64String((username + ":" + password).getBytes()).trim());
        headerList[1] = new BasicHeader(CONTENT_TYPE_ATTRIBUTE, String.valueOf(ContentType.JSON));

        return headerList;
    }

    private Header[] getHeadersWithBearerToken(String accessToken) {

        Header[] headerList = new Header[2];
        headerList[0] = new BasicHeader(AUTHORIZATION_ATTRIBUTE, BEARER_TOKEN_AUTHORIZATION_ATTRIBUTE +
                accessToken);
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

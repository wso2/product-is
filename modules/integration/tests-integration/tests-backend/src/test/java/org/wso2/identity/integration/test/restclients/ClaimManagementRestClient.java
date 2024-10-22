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
import org.apache.commons.codec.binary.Base64;
import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONObject;
import org.testng.Assert;
import org.wso2.carbon.automation.engine.context.beans.Tenant;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;
import org.wso2.identity.integration.test.rest.api.server.claim.management.v1.model.ExternalClaimReq;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class ClaimManagementRestClient extends RestBaseClient {

    private static final String API_SERVER_BASE_PATH = "/api/server/v1";
    private static final String CLAIM_DIALECTS_ENDPOINT_URI = "/claim-dialects";

    public static final String CLAIMS_ENDPOINT_URI = "/claims";
    private final CloseableHttpClient client;
    private final String username;
    private final String password;
    private final String serverBasePath;

    public ClaimManagementRestClient(String backendURL, Tenant tenantInfo) {

        client = HttpClients.createDefault();

        this.username = tenantInfo.getContextUser().getUserName();
        this.password = tenantInfo.getContextUser().getPassword();

        String tenantDomain = tenantInfo.getContextUser().getUserDomain();

        serverBasePath = backendURL + ISIntegrationTest.getTenantedRelativePath(API_SERVER_BASE_PATH, tenantDomain);
    }

    /**
     * Add External Claim.
     *
     * @param dialectId    Claim dialect id.
     * @param claimRequest External Claim request object.
     * @throws Exception If an error occurred while adding an external claim.
     */
    public String addExternalClaim(String dialectId, ExternalClaimReq claimRequest) throws Exception {

        String endPointUrl = serverBasePath + CLAIM_DIALECTS_ENDPOINT_URI + PATH_SEPARATOR + dialectId +
                CLAIMS_ENDPOINT_URI;
        String jsonRequest = toJSONString(claimRequest);
        try (CloseableHttpResponse response = getResponseOfHttpPost(endPointUrl, jsonRequest, getHeaders())) {
            String[] locationElements = response.getHeaders(LOCATION_HEADER)[0].toString().split(PATH_SEPARATOR);
            return locationElements[locationElements.length - 1];
        }
    }

    /**
     * Get an External Claim.
     *
     * @param dialectId Claim dialect id.
     * @param claimId   Claim id.
     * @return JSONObject JSON object of the response.
     * @throws Exception If an error occurred while getting an external claim.
     */
    public JSONObject getExternalClaim(String dialectId, String claimId) throws Exception {

        String endPointUrl = serverBasePath + CLAIM_DIALECTS_ENDPOINT_URI + PATH_SEPARATOR + dialectId +
                CLAIMS_ENDPOINT_URI + PATH_SEPARATOR + claimId;

        try (CloseableHttpResponse response = getResponseOfHttpGet(endPointUrl, getHeaders())) {
            return getJSONObject(EntityUtils.toString(response.getEntity()));
        }
    }

    /**
     * Delete an External Claim.
     *
     * @param dialectId Claim dialect id.
     * @param claimId   Claim id.
     * @throws IOException If an error occurred while deleting an external claim.
     */
    public void deleteExternalClaim(String dialectId, String claimId) throws IOException {

        String endPointUrl = serverBasePath + CLAIM_DIALECTS_ENDPOINT_URI + PATH_SEPARATOR + dialectId +
                CLAIMS_ENDPOINT_URI + PATH_SEPARATOR + claimId;
        try (CloseableHttpResponse response = getResponseOfHttpDelete(endPointUrl, getHeaders())) {
            Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpServletResponse.SC_NO_CONTENT,
                    "External claim deletion failed");
        }
    }

    public void updateExternalClaim(String dialectId, String claimId, ExternalClaimReq claimRequest) throws IOException {

        String endPointUrl = serverBasePath + CLAIM_DIALECTS_ENDPOINT_URI + PATH_SEPARATOR + dialectId +
                CLAIMS_ENDPOINT_URI + PATH_SEPARATOR + claimId;
        String jsonRequest = toJSONString(claimRequest);
        try (CloseableHttpResponse response = getResponseOfHttpPut(endPointUrl, jsonRequest, getHeaders())) {
            Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpServletResponse.SC_OK,
                    "External claim update failed");
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

/*
 * Copyright (c) 2023-2025, WSO2 LLC. (http://www.wso2.com).
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
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;
import org.wso2.identity.integration.test.rest.api.server.claim.management.v1.model.ClaimDialectReqDTO;
import org.wso2.identity.integration.test.rest.api.server.claim.management.v1.model.ExternalClaimReq;
import org.wso2.identity.integration.test.rest.api.server.claim.management.v1.model.LocalClaimReq;
import org.wso2.identity.integration.test.rest.api.server.claim.management.v1.model.LocalClaimRes;

import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ClaimManagementRestClient extends RestBaseClient {

    private static final String API_SERVER_BASE_PATH = "/api/server/v1";
    private static final String CLAIM_DIALECTS_ENDPOINT_URI = "/claim-dialects";

    public static final String LOCAL_CLAIMS_ENDPOINT_URI = "/local";
    public static final String CLAIMS_ENDPOINT_URI = "/claims";
    public static final String ORGANIZATION_PATH = "o";
    public static final String PATH_SEPARATOR = "/";
    private static final int TIMEOUT_MILLIS = 30000;
    private static final int POLLING_INTERVAL_MILLIS = 500;
    private final CloseableHttpClient client;
    private final String username;
    private final String password;
    private final String serverBasePath;
    private final String subOrgBasePath;

    public ClaimManagementRestClient(String backendURL, Tenant tenantInfo) {

        client = HttpClients.createDefault();

        this.username = tenantInfo.getContextUser().getUserName();
        this.password = tenantInfo.getContextUser().getPassword();

        String tenantDomain = tenantInfo.getContextUser().getUserDomain();

        serverBasePath = backendURL + ISIntegrationTest.getTenantedRelativePath(API_SERVER_BASE_PATH, tenantDomain);
        subOrgBasePath = getSubOrgBasePath(backendURL, tenantDomain);
    }

    /**
     * Add Local Claim.
     *
     * @param claimRequest Local Claim request object.
     * @return Claim id.
     * @throws IOException If an error occurred while adding a local claim.
     */
    public String addLocalClaim(LocalClaimReq claimRequest) throws IOException {

        String endPointUrl = serverBasePath + CLAIM_DIALECTS_ENDPOINT_URI + PATH_SEPARATOR + LOCAL_CLAIMS_ENDPOINT_URI +
                CLAIMS_ENDPOINT_URI;
        try (CloseableHttpResponse response = getResponseOfHttpPost(endPointUrl, toJSONString(claimRequest),
                getHeaders())) {
            Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpServletResponse.SC_CREATED,
                    "Local claim addition failed");
            String[] locationElements = response.getHeaders(LOCATION_HEADER)[0].toString().split(PATH_SEPARATOR);
            return locationElements[locationElements.length - 1];
        }
    }

    /**
     * Delete a Local Claim.
     *
     * @param claimId   Claim id.
     * @throws IOException If an error occurred while deleting a local claim.
     */
    public void deleteLocalClaim(String claimId) throws IOException {

        String endPointUrl = serverBasePath + CLAIM_DIALECTS_ENDPOINT_URI + PATH_SEPARATOR + LOCAL_CLAIMS_ENDPOINT_URI +
                CLAIMS_ENDPOINT_URI + PATH_SEPARATOR + claimId;
        try (CloseableHttpResponse response = getResponseOfHttpDelete(endPointUrl, getHeaders())) {
            Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpServletResponse.SC_NO_CONTENT,
                    "Local claim deletion failed");
        }
    }

    /**
     * Update a Local Claim.
     *
     * @param claimId      Claim ID to update.
     * @param claimRequest Updated claim request.
     * @throws IOException If an error occurs while updating the claim.
     */
    public void updateLocalClaim(String claimId, LocalClaimReq claimRequest) throws IOException {

        String endPointUrl = serverBasePath + CLAIM_DIALECTS_ENDPOINT_URI + PATH_SEPARATOR + LOCAL_CLAIMS_ENDPOINT_URI +
                CLAIMS_ENDPOINT_URI + PATH_SEPARATOR + claimId;

        try (CloseableHttpResponse response = getResponseOfHttpPut(endPointUrl, toJSONString(claimRequest),
                getHeaders())) {
            Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpServletResponse.SC_OK,
                    "Local claim update failed");
        }
    }

    /**
     * Get a Local Claim.
     *
     * @param claimId Claim id.
     * @return JSON object of the response.
     * @throws IOException If an error occurred while getting a local claim.
     */
    public JSONObject getLocalClaim(String claimId) throws Exception {

        String endPointUrl = serverBasePath + CLAIM_DIALECTS_ENDPOINT_URI + PATH_SEPARATOR + LOCAL_CLAIMS_ENDPOINT_URI +
                CLAIMS_ENDPOINT_URI + PATH_SEPARATOR + claimId;
        try (CloseableHttpResponse response = getResponseOfHttpGet(endPointUrl, getHeaders())) {
            return getJSONObject(EntityUtils.toString(response.getEntity()));
        }
    }

    /**
     * Get a Local Claim in sub organization.
     *
     * @param claimId          Claim id.
     * @param switchedM2MToken Switched M2M token.
     * @return JSON object of the response.
     * @throws Exception If an error occurred while getting a local claim.
     */
    public JSONObject getSubOrgLocalClaim(String claimId, String switchedM2MToken) throws Exception {

        String endPointUrl = subOrgBasePath + CLAIM_DIALECTS_ENDPOINT_URI + PATH_SEPARATOR + LOCAL_CLAIMS_ENDPOINT_URI +
                CLAIMS_ENDPOINT_URI + PATH_SEPARATOR + claimId;
        try (CloseableHttpResponse response = getResponseOfHttpGet(endPointUrl,
                getHeadersWithBearerToken(switchedM2MToken))) {
            return getJSONObject(EntityUtils.toString(response.getEntity()));
        }
    }

    /**
     * Check whether the claim sharing is completed.
     *
     * @param claimId          Claim id.
     * @param switchedM2MToken Switched M2M token.
     * @return True if the claim sharing is completed.
     * @throws Exception If an error occurred while checking the claim sharing status.
     */
    public boolean isClaimSharingCompleted(String claimId, String switchedM2MToken) throws Exception {

        // Wait for 30 seconds.
        long waitTime = System.currentTimeMillis() + TIMEOUT_MILLIS;
        while (System.currentTimeMillis() < waitTime) {
            JSONObject claimGetObject = getSubOrgLocalClaim(claimId, switchedM2MToken);
            String id = (String) claimGetObject.get("id");
            if (claimId.equals(id)) {
                return true;
            }
            Thread.sleep(POLLING_INTERVAL_MILLIS);
        }
        return false;
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

    /**
     * Update the claim referenced by the provided id.
     *
     * @param dialectId   Claim dialect id.
     * @param claimId     Claim id.
     * @param requestBody Request body.
     */
    public void updateClaim(String dialectId, String claimId, String requestBody) {

        String endPointUrl = serverBasePath + CLAIM_DIALECTS_ENDPOINT_URI + PATH_SEPARATOR + dialectId +
                CLAIMS_ENDPOINT_URI + PATH_SEPARATOR + claimId;
        try (CloseableHttpResponse response = getResponseOfHttpPut(endPointUrl, requestBody, getHeaders())) {
            Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpServletResponse.SC_OK,
                    "Claim update failed");
        } catch (IOException e) {
            Assert.fail("Error occurred while updating the claim.");
        }
    }

    /**
     * Update the sub organization claim referenced by the provided id.
     *
     * @param dialectId        Claim dialect id.
     * @param claimId          Claim id.
     * @param requestBody      Request body.
     * @param switchedM2MToken Switched M2M token.
     * @return status code of the response.
     * @throws Exception If an error occurred while updating the claim.
     */
    public int updateSubOrgClaim(String dialectId, String claimId, String requestBody, String switchedM2MToken)
            throws Exception {

        String endPointUrl = subOrgBasePath + CLAIM_DIALECTS_ENDPOINT_URI + PATH_SEPARATOR + dialectId +
                CLAIMS_ENDPOINT_URI + PATH_SEPARATOR + claimId;
        try (CloseableHttpResponse response = getResponseOfHttpPut(endPointUrl, requestBody,
                getHeadersWithBearerToken(switchedM2MToken))) {
            return response.getStatusLine().getStatusCode();
        }
    }

    public void updateExternalClaim(String dialectId, String claimId, ExternalClaimReq claimRequest)
            throws IOException {

        String endPointUrl = serverBasePath + CLAIM_DIALECTS_ENDPOINT_URI + PATH_SEPARATOR + dialectId +
                CLAIMS_ENDPOINT_URI + PATH_SEPARATOR + claimId;
        String jsonRequest = toJSONString(claimRequest);
        try (CloseableHttpResponse response = getResponseOfHttpPut(endPointUrl, jsonRequest, getHeaders())) {
            Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpServletResponse.SC_OK,
                    "External claim update failed");
        }
    }

    /**
     * Get External Dialect.
     *
     * @param dialectId Dialect id.
     * @return JSON object of the response.
     * @throws Exception If an error occurred while getting an external dialect.
     */
    public JSONObject getExternalDialect(String dialectId) throws Exception {

        String endPointUrl = serverBasePath + CLAIM_DIALECTS_ENDPOINT_URI + PATH_SEPARATOR + dialectId;
        try (CloseableHttpResponse response = getResponseOfHttpGet(endPointUrl, getHeaders())) {
            return getJSONObject(EntityUtils.toString(response.getEntity()));
        }
    }

    /**
     * Add External Dialect.
     *
     * @param claimDialectReqDTO Claim Dialect request object.
     * @return Dialect id.
     * @throws IOException If an error occurred while adding an external dialect.
     */
    public String addExternalDialect(ClaimDialectReqDTO claimDialectReqDTO) throws IOException {

        String endPointUrl = serverBasePath + CLAIM_DIALECTS_ENDPOINT_URI;
        try (CloseableHttpResponse response = getResponseOfHttpPost(endPointUrl, toJSONString(claimDialectReqDTO),
                getHeaders())) {
            Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpServletResponse.SC_CREATED,
                    "External dialect addition failed.");
            String[] locationElements = response.getHeaders(LOCATION_HEADER)[0].toString().split(PATH_SEPARATOR);
            return locationElements[locationElements.length - 1];
        }
    }

    /**
     * Delete an External Dialect.
     *
     * @param dialectId Dialect id.
     * @throws IOException If an error occurred while deleting an external dialect.
     */
    public void deleteExternalDialect(String dialectId) throws IOException {

        String endPointUrl = serverBasePath + CLAIM_DIALECTS_ENDPOINT_URI + PATH_SEPARATOR + dialectId;
        try (CloseableHttpResponse response = getResponseOfHttpDelete(endPointUrl, getHeaders())) {
            Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpServletResponse.SC_NO_CONTENT,
                    "External dialect deletion failed.");
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

    private Header[] getHeadersWithBearerToken(String accessToken) {

        Header[] headerList = new Header[2];
        headerList[0] = new BasicHeader(AUTHORIZATION_ATTRIBUTE, BEARER_TOKEN_AUTHORIZATION_ATTRIBUTE +
                accessToken);
        headerList[1] = new BasicHeader(CONTENT_TYPE_ATTRIBUTE, String.valueOf(ContentType.JSON));

        return headerList;
    }

    private String getSubOrgBasePath(String serverUrl, String tenantDomain) {

        if (tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
            return serverUrl + ORGANIZATION_PATH + API_SERVER_BASE_PATH;
        }
        return serverUrl + TENANT_PATH + tenantDomain + PATH_SEPARATOR + ORGANIZATION_PATH + API_SERVER_BASE_PATH;
    }

    /**
     * Get local claim by URI.
     *
     * @param claimUri Claim URI to retrieve.
     * @return LocalClaimRes object containing claim details.
     * @throws IOException If an error occurs while making the request.
     * @throws AssertionError If the claim URI is not found.
     */
    public LocalClaimRes getLocalClaimByUri(String claimUri) throws IOException {

        String endPointUrl = serverBasePath + CLAIM_DIALECTS_ENDPOINT_URI + PATH_SEPARATOR + LOCAL_CLAIMS_ENDPOINT_URI +
                CLAIMS_ENDPOINT_URI;

        try (CloseableHttpResponse response = getResponseOfHttpGet(endPointUrl, getHeaders())) {
            Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpServletResponse.SC_OK,
                    "Failed to get local claims");

            ObjectMapper mapper = new ObjectMapper();
            LocalClaimRes[] claims =
                    mapper.readValue(EntityUtils.toString(response.getEntity()), LocalClaimRes[].class);

            return Stream.of(claims)
                    .filter(claim -> claimUri.equals(claim.getClaimURI()))
                    .findFirst()
                    .orElseThrow(() -> new AssertionError("Could not find claim with URI: " + claimUri));
        }
    }
}

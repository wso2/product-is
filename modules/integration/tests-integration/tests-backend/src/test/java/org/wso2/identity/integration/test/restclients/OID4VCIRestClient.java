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

import io.restassured.http.ContentType;
import org.apache.commons.lang.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONObject;
import org.testng.Assert;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.io.IOException;

/**
 * Rest client for OID4VCI runtime APIs.
 */
public class OID4VCIRestClient extends RestBaseClient {

    private static final String OID4VCI_PATH = "oid4vci";
    private static final String WELL_KNOWN_CREDENTIAL_ISSUER_PATH = ".well-known/openid-credential-issuer";
    private final String tenantAwareIssuerMetadataEndpoint;
    private final String rootIssuerMetadataEndpoint;

    public OID4VCIRestClient(String serverUrl, String tenantDomain) {

        this.tenantAwareIssuerMetadataEndpoint = serverUrl + TENANT_PATH + tenantDomain + PATH_SEPARATOR +
                OID4VCI_PATH + PATH_SEPARATOR + WELL_KNOWN_CREDENTIAL_ISSUER_PATH;
        this.rootIssuerMetadataEndpoint = serverUrl + OID4VCI_PATH + PATH_SEPARATOR + WELL_KNOWN_CREDENTIAL_ISSUER_PATH;
    }

    /**
     * Read credential issuer metadata. Tries tenant-aware endpoint first, then root endpoint.
     *
     * @return Metadata response.
     * @throws Exception If both attempts fail.
     */
    public JSONObject getCredentialIssuerMetadata() throws Exception {

        JSONObject tenantAwareMetadata = tryGetCredentialIssuerMetadata(tenantAwareIssuerMetadataEndpoint);
        if (tenantAwareMetadata != null) {
            return tenantAwareMetadata;
        }
        JSONObject rootMetadata = tryGetCredentialIssuerMetadata(rootIssuerMetadataEndpoint);
        if (rootMetadata != null) {
            return rootMetadata;
        }
        throw new Exception("Unable to read OID4VCI metadata from: " + tenantAwareIssuerMetadataEndpoint + " or "
                + rootIssuerMetadataEndpoint);
    }

    /**
     * Request VC issuance.
     *
     * @param accessToken Access token with VC scope.
     * @param credentialConfigurationId Credential configuration identifier.
     * @return Issuance response payload.
     * @throws Exception If request fails.
     */
    public JSONObject requestCredential(String accessToken, String credentialConfigurationId) throws Exception {

        JSONObject metadata = getCredentialIssuerMetadata();
        String credentialEndpoint = (String) metadata.get("credential_endpoint");
        Assert.assertTrue(StringUtils.isNotBlank(credentialEndpoint),
                "credential_endpoint is not present in OID4VCI metadata.");

        JSONObject requestPayload = new JSONObject();
        requestPayload.put("credential_configuration_id", credentialConfigurationId);

        try (CloseableHttpResponse response = getResponseOfHttpPost(credentialEndpoint, requestPayload.toJSONString(),
                getHeadersWithBearerToken(accessToken))) {
            int statusCode = response.getStatusLine().getStatusCode();
            String responseBody = EntityUtils.toString(response.getEntity());
            Assert.assertEquals(statusCode, HttpStatus.SC_OK,
                    "VC issuance failed. Response: " + responseBody);
            return getJSONObject(responseBody);
        }
    }

    public void closeHttpClient() throws IOException {

        client.close();
    }

    private JSONObject tryGetCredentialIssuerMetadata(String endpoint) throws Exception {

        try (CloseableHttpResponse response = getResponseOfHttpGet(endpoint, getHeaders())) {
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_OK) {
                EntityUtils.consume(response.getEntity());
                return null;
            }
            return getJSONObject(EntityUtils.toString(response.getEntity()));
        }
    }

    private Header[] getHeaders() {

        Header[] headerList = new Header[2];
        headerList[0] = new BasicHeader(USER_AGENT_ATTRIBUTE, OAuth2Constant.USER_AGENT);
        headerList[1] = new BasicHeader(CONTENT_TYPE_ATTRIBUTE, String.valueOf(ContentType.JSON));
        return headerList;
    }

    private Header[] getHeadersWithBearerToken(String accessToken) {

        Header[] headerList = new Header[3];
        headerList[0] = new BasicHeader(USER_AGENT_ATTRIBUTE, OAuth2Constant.USER_AGENT);
        headerList[1] = new BasicHeader(AUTHORIZATION_ATTRIBUTE, BEARER_TOKEN_AUTHORIZATION_ATTRIBUTE + accessToken);
        headerList[2] = new BasicHeader(CONTENT_TYPE_ATTRIBUTE, String.valueOf(ContentType.JSON));
        return headerList;
    }
}

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
import org.apache.http.message.BasicHeader;
import org.testng.Assert;
import org.wso2.carbon.automation.engine.context.beans.Tenant;
import org.wso2.identity.integration.test.rest.api.server.keystore.management.v1.model.CertificateRequest;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class KeystoreMgtRestClient extends RestBaseClient {

    private static final String KEYSTORE_BASE_PATH = "t/%s/api/server/v1/keystores/certs";
    private final String serverUrl;
    private final String tenantDomain;
    private final String username;
    private final String password;

    public KeystoreMgtRestClient(String serverUrl, Tenant tenantInfo) {

        this.serverUrl = serverUrl;
        this.tenantDomain = tenantInfo.getContextUser().getUserDomain();
        this.username = tenantInfo.getContextUser().getUserName();
        this.password = tenantInfo.getContextUser().getPassword();
    }

    /**
     * Upload the certificate to the tenant keystore. This API is not supported for super tenant.
     *
     * @param certificateRequest Certificate request object.
     */
    public void importCertToStore(CertificateRequest certificateRequest) throws Exception {
        String jsonRequest = toJSONString(certificateRequest);
        String endPointUrl = serverUrl + String.format(KEYSTORE_BASE_PATH, tenantDomain);

        try (CloseableHttpResponse response = getResponseOfHttpPost(endPointUrl, jsonRequest, getHeaders())) {
            Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpServletResponse.SC_CREATED,
                    "Certificate upload failed");
        }
    }

    /**
     * Check whether a certain certificate is already added in the tenant keystore.
     *
     * @param alias alias.
     */
    public Boolean checkCertInStore(String alias) throws Exception {
        String endPointUrl = serverUrl + String.format(KEYSTORE_BASE_PATH, tenantDomain) + PATH_SEPARATOR + alias;

        try (CloseableHttpResponse response = getResponseOfHttpGet(endPointUrl, getHeaders())) {
            if (response.getStatusLine().getStatusCode() == HttpServletResponse.SC_OK) {
                return true;
            } else {
                return false;
            }
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
     */
    public void closeHttpClient() throws IOException {
        client.close();
    }
}

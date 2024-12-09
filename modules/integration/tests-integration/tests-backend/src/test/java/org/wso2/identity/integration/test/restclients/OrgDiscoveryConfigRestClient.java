/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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
import org.wso2.identity.integration.common.utils.ISIntegrationTest;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

/**
 * Rest client for organization discovery configuration management API.
 */
public class OrgDiscoveryConfigRestClient extends RestBaseClient {

    private static final String API_BASE_PATH = "api/server/v1";
    private static final String ORGANIZATION_CONFIG_PATH = "/organization-configs";
    private static final String ORGANIZATIONS_PATH = "/organizations";
    private static final String DISCOVERY_PATH = "/discovery";

    private final String username;
    private final String password;
    private final String serverBasePath;

    public OrgDiscoveryConfigRestClient(String backendURL, Tenant tenantInfo) {

        this.username = tenantInfo.getContextUser().getUserName();
        this.password = tenantInfo.getContextUser().getPassword();

        String tenantDomain = tenantInfo.getContextUser().getUserDomain();
        this.serverBasePath = backendURL + ISIntegrationTest.getTenantedRelativePath(API_BASE_PATH, tenantDomain);
    }

    /**
     * Add organization discovery config to the root organization.
     *
     * @param requestBody Request body.
     */
    public void addOrganizationDiscoveryConfig(String requestBody) {

        try (CloseableHttpResponse httpResponse = getResponseOfHttpPost(
                serverBasePath + ORGANIZATION_CONFIG_PATH + DISCOVERY_PATH, requestBody, getHeaders())) {
            Assert.assertEquals(httpResponse.getStatusLine().getStatusCode(), HttpServletResponse.SC_CREATED,
                    "Failed to add organization discovery config.");
        } catch (IOException e) {
            throw new RuntimeException("Error occurred while adding organization discovery config.", e);
        }
    }

    /**
     * Delete organization discovery config of the root organization.
     */
    public void deleteOrganizationDiscoveryConfig() {

        try (CloseableHttpResponse httpResponse = getResponseOfHttpDelete(
                serverBasePath + ORGANIZATION_CONFIG_PATH + DISCOVERY_PATH, getHeaders())) {
            Assert.assertEquals(httpResponse.getStatusLine().getStatusCode(), HttpServletResponse.SC_NO_CONTENT,
                    "Failed to delete organization discovery config.");
        } catch (IOException e) {
            throw new RuntimeException("Error occurred while deleting organization discovery config.", e);
        }
    }

    /**
     * Map discovery attributes to an organization.
     *
     * @param orgId       Organization ID.
     * @param requestBody Request body.
     */
    public void mapDiscoveryAttributes(String orgId, String requestBody) {

        String endpointUrl = serverBasePath + ORGANIZATIONS_PATH + PATH_SEPARATOR + orgId + DISCOVERY_PATH;
        try (CloseableHttpResponse httpResponse = getResponseOfHttpPut(endpointUrl, requestBody, getHeaders())) {
            Assert.assertEquals(httpResponse.getStatusLine().getStatusCode(), HttpServletResponse.SC_OK,
                    "Failed to map discovery attributes.");
        } catch (IOException e) {
            throw new RuntimeException("Error occurred while mapping discovery attributes.", e);
        }
    }

    public void closeHttpClient() throws IOException {

        client.close();
    }

    private Header[] getHeaders() {

        return new Header[]{
                new BasicHeader(CONTENT_TYPE_ATTRIBUTE, String.valueOf(ContentType.JSON)),
                new BasicHeader(AUTHORIZATION_ATTRIBUTE, BASIC_AUTHORIZATION_ATTRIBUTE +
                        Base64.encodeBase64String((username + ":" + password).getBytes()).trim())
        };
    }
}

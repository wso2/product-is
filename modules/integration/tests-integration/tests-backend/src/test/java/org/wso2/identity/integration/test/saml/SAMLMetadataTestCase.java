/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
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

package org.wso2.identity.integration.test.saml;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONArray;
import org.json.JSONException;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.tenant.mgt.stub.TenantMgtAdminServiceExceptionException;
import org.wso2.identity.integration.common.clients.TenantManagementServiceClient;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;
import org.wso2.identity.integration.test.utils.DataExtractUtil;
import org.json.JSONObject;
import org.json.XML;

import java.io.IOException;

public class SAMLMetadataTestCase extends ISIntegrationTest {

    private static final String SUPER_TENANT_URL = "https://localhost:9853/identity/metadata/saml2";
    private static final String TENANT_URL = "https://localhost:9853/t/wso2.com/identity/metadata/saml2";
    private static final String SAML_METADATA_ENDPOINT_TENANT = "https://localhost:9853/samlsso?tenantDomain=wso2.com";
    private static final String SAML_METADATA_ENDPOINT_SUPER_TENANT = "https://localhost:9853/samlsso";
    private static final String SAMLARTRESOLVE_ENDPOINT = "https://localhost:9853/samlartresolve";
    private TenantManagementServiceClient tenantServiceClient;

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.init();
        tenantServiceClient = new TenantManagementServiceClient(isServer.getContextUrls().getBackEndUrl(),
                sessionCookie);
    }

    @Test(groups = "wso2.is", description = "This test method will test, invoking SAML Metadata endpoint.")
    public void getSAMLMetadata() throws IOException, TenantMgtAdminServiceExceptionException, JSONException {

        testResponseContent(SUPER_TENANT_URL, SAML_METADATA_ENDPOINT_SUPER_TENANT);
        tenantServiceClient.addTenant("wso2.com", "John", "password",
                "john@friends.com", "John", "Smith");
        testResponseContent(TENANT_URL, SAML_METADATA_ENDPOINT_TENANT);
    }

    private void testResponseContent(String locationURL, String samlMetadataEndpoint)
            throws IOException, JSONException {

        HttpClient client = HttpClientBuilder.create().build();
        HttpResponse httpResponse = sendGetRequest(client, locationURL);
        String content = DataExtractUtil.getContentData(httpResponse);
        Assert.assertNotNull(content);
        JSONArray singleLogoutServices = XML.toJSONObject(content).getJSONObject("EntityDescriptor").getJSONObject(
                "IDPSSODescriptor").getJSONArray("SingleLogoutService");

        for (int i = 0; i < singleLogoutServices.length(); i++) {

            JSONObject singleLogoutService = singleLogoutServices.getJSONObject(i);
            Assert.assertEquals(singleLogoutService.getString("Location"),
                    samlMetadataEndpoint, String.format("Expected location was not received for single logout" +
                            " service for the binding %S.", singleLogoutService.getString("Binding")));
            Assert.assertEquals(singleLogoutService.getString("ResponseLocation"),
                    samlMetadataEndpoint, String.format("Expected response location was not received for single " +
                            "logout service for the binding %S.", singleLogoutService.getString("Binding")));
        }

        JSONArray singleSignOnServices = XML.toJSONObject(content).getJSONObject("EntityDescriptor").getJSONObject(
                "IDPSSODescriptor").getJSONArray("SingleSignOnService");

        for (int i = 0; i < singleSignOnServices.length(); i++) {
            JSONObject singleSignOnService = singleSignOnServices.getJSONObject(i);
            Assert.assertEquals(singleSignOnService.getString("Location"),
                    samlMetadataEndpoint, String.format("Expected location was not received for single sign-on " +
                            "service for the binding %S.", singleSignOnService.getString("Binding")));
        }

        JSONObject artifactResolutionService =
                XML.toJSONObject(content).getJSONObject("EntityDescriptor").getJSONObject(
                        "IDPSSODescriptor").getJSONObject("ArtifactResolutionService");
        Assert.assertEquals(artifactResolutionService.getString("Location"),
                SAMLARTRESOLVE_ENDPOINT, String.format("Expected location was not received for artifact resolution" +
                        "service for the binding %S.", artifactResolutionService.getString("Binding")));
    }

    private HttpResponse sendGetRequest(HttpClient client, String locationURL) throws IOException {

        HttpGet getRequest = new HttpGet(locationURL);
        HttpResponse response = client.execute(getRequest);
        return response;
    }
}

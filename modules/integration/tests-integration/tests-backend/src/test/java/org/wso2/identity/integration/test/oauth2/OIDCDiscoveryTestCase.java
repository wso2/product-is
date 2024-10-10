/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.identity.integration.test.oauth2;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.wink.client.ClientConfig;
import org.apache.wink.client.Resource;
import org.apache.wink.client.RestClient;
import org.apache.wink.client.handlers.BasicAuthSecurityHandler;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.charon.core.schema.SCIMConstants;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;

import java.io.IOException;

import static org.testng.Assert.assertEquals;

public class OIDCDiscoveryTestCase extends ISIntegrationTest {

    public static final String WEBFINGER_ENDPOINT_SUFFIX = "/.well-known/webfinger";
    public static final String RESOURCE = "resource";
    public static final String REL = "rel";
    private String isServerBackendUrl;

    private static final String[] expectedResponseModes = {"fragment", "jwt", "fragment.jwt", "query", "form_post",
            "query.jwt", "form_post.jwt"};
    private static final String[] expectedAuthModes = {"private_key_jwt", "client_secret_post", "tls_client_auth",
            "client_secret_basic"};
    private static final String[] expectedResponseTypes = {"id_token token", "code", "code id_token token",
            "code id_token", "id_token", "code token", "none", "device", "subject_token", "id_token subject_token",
            "token"};
    private static final String[] expectedGrantTypes = {"refresh_token", "password", "client_credentials", "iwa:ntlm",
            "urn:ietf:params:oauth:grant-type:saml2-bearer", "urn:ietf:params:oauth:grant-type:device_code",
            "authorization_code", "account_switch", "urn:ietf:params:oauth:grant-type:token-exchange",
            "organization_switch", "urn:ietf:params:oauth:grant-type:jwt-bearer"};

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {
        super.init();
        isServerBackendUrl = isServer.getContextUrls().getWebAppURLHttps();
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() {

    }

    @Test(alwaysRun = true, groups = "wso2.is", description = "webfinger test",
            dataProvider = "webfingerConfigProvider")
    public void testWebFinger(DiscoveryConfig config) {

        String relUri = "http://openid.net/specs/connect/1.0/issuer";
        String webFingerEndpoint = isServerBackendUrl + WEBFINGER_ENDPOINT_SUFFIX + "?" + RESOURCE + "=" + config
                .getResource() + "&" + REL + "=" + relUri;
        ClientConfig clientConfig = new ClientConfig();
        BasicAuthSecurityHandler basicAuth = new BasicAuthSecurityHandler();
        basicAuth.setUserName(userInfo.getUserName());
        basicAuth.setPassword(userInfo.getPassword());
        clientConfig.handlers(basicAuth);

        RestClient restClient = new RestClient(clientConfig);
        Resource userResource = restClient.resource(webFingerEndpoint);
        String response = userResource.accept(SCIMConstants.APPLICATION_JSON).get(String.class);
        Object obj= JSONValue.parse(response);
        Object links = ((JSONObject)obj).get("links");
        Assert.assertNotNull(links);
        String openIdProviderIssuerLocation = ((JSONObject)((JSONArray)links).get(0)).get("href").toString();
        String urlExpected =  getTenantQualifiedURL(isServerBackendUrl + "/oauth2/token", config.getTenant());
        Assert.assertEquals(openIdProviderIssuerLocation, urlExpected);
    }

    @Test(alwaysRun = true, groups = "wso2.is", description = "Discovery test", dependsOnMethods = { "testWebFinger" },
            dataProvider = "oidcDiscoveryConfigProvider")
    public void testDiscovery(String tenantDomain, String issuer) {

        ClientConfig clientConfig = new ClientConfig();
        BasicAuthSecurityHandler basicAuth = new BasicAuthSecurityHandler();
        basicAuth.setUserName(userInfo.getUserName());
        basicAuth.setPassword(userInfo.getPassword());
        clientConfig.handlers(basicAuth);

        RestClient restClient = new RestClient(clientConfig);
        String discoveryUrl =  getTenantQualifiedURL(isServerBackendUrl +
                "/oauth2/" + issuer + "/.well-known/openid-configuration", tenantDomain);

        Resource userResource = restClient.resource(discoveryUrl);
        String response = userResource.accept(SCIMConstants.APPLICATION_JSON).get(String.class);
        JSONObject jsonResponse = (JSONObject) JSONValue.parse(response);

        // Extract and validate the endpoints
        validateEndpoint(jsonResponse, "authorization_endpoint", "/oauth2/authorize", tenantDomain);
        validateEndpoint(jsonResponse, "token_endpoint", "/oauth2/token", tenantDomain);
        validateEndpoint(jsonResponse, "userinfo_endpoint", "/oauth2/userinfo", tenantDomain);
        validateEndpoint(jsonResponse, "pushed_authorization_request_endpoint", "/oauth2/par", tenantDomain);
        validateEndpoint(jsonResponse, "introspection_endpoint", "/oauth2/introspect", tenantDomain);
        validateEndpoint(jsonResponse, "device_authorization_endpoint", "/oauth2/device_authorize", tenantDomain);
        validateEndpoint(jsonResponse, "end_session_endpoint", "/oidc/logout", tenantDomain);
        validateEndpoint(jsonResponse, "revocation_endpoint", "/oauth2/revoke", tenantDomain);
        validateEndpoint(jsonResponse, "jwks_uri", "/oauth2/jwks", tenantDomain);
        validateEndpoint(jsonResponse, "registration_endpoint",
                "/api/identity/oauth2/dcr/v1.1/register", tenantDomain);
        validateArrayElements(jsonResponse, "response_modes_supported", expectedResponseModes);
        validateArrayElements(jsonResponse, "token_endpoint_auth_methods_supported", expectedAuthModes);
        validateArrayElements(jsonResponse, "response_types_supported", expectedResponseTypes);
        validateArrayElements(jsonResponse, "grant_types_supported", expectedGrantTypes);
    }

    @DataProvider(name = "oidcDiscoveryConfigProvider")
    public static Object[][] configProvider() {
        return new Object[][]{
                {"", "oidcdiscovery"},
                {"carbon.super", "oidcdiscovery"},
                {"wso2.com", "oidcdiscovery"},
                {"", "token"},
                {"carbon.super", "token"},
                {"wso2.com", "token"},
        };
    }

    @DataProvider(name = "webfingerConfigProvider")
    public static Object[][] webfingerConfigProvider(){
        return new DiscoveryConfig[][]{
                {new DiscoveryConfig("acct:admin@localhost", "")},
                {new DiscoveryConfig("acct:admin%40wso2.com@localhost", "wso2.com")},
                {new DiscoveryConfig("https://localhost:9443/joe", "")},
                {new DiscoveryConfig("https://localhost:9443", "")}
        };
    }

    private static class DiscoveryConfig{
        private String resource;
        private String tenant;

        private DiscoveryConfig(String resource, String tenant) {
            this.setResource(resource);
            this.setTenant(tenant);
        }

        public String getResource() {
            return resource;
        }

        public void setResource(String resource) {
            this.resource = resource;
        }

        public String getTenant() {
            return tenant;
        }

        public void setTenant(String tenant) {
            this.tenant = tenant;
        }
    }

    /**
     * Validates the specific endpoint from the OIDC discovery response.
     *
     * @param jsonResponse        The parsed JSON object from the response.
     * @param endpointKey         The key of the endpoint in the JSON response.
     * @param expectedEndpoint    The expected suffix for the endpoint URL.
     * @param tenantDomain        Tenant domain intend for testing.
     */
    private void validateEndpoint(JSONObject jsonResponse, String endpointKey, String expectedEndpoint,
                                  String tenantDomain) {
        String endpointUrl = jsonResponse.get(endpointKey).toString();
        String expectedUrl = getTenantQualifiedURL(isServerBackendUrl + expectedEndpoint, tenantDomain);
        Assert.assertEquals(endpointUrl, expectedUrl,
                String.format("Expected %s to be %s, but found %s", endpointKey, expectedUrl, endpointUrl));
    }

    private void validateArrayElements(JSONObject jsonResponse, String key, String[] expectedElements) {
        JSONArray elementsArray = (JSONArray) jsonResponse.get(key);
        String[] actualElements = new String[elementsArray.size()];
        for (int i = 0; i < elementsArray.size(); i++) {
            actualElements[i] = (String) elementsArray.get(i);
        }

        Assert.assertTrue(containsAll(actualElements, expectedElements),
                String.format("Expected elements to include %s, but found %s",
                        String.join(", ", expectedElements),
                        String.join(", ", actualElements)));
    }

    private boolean containsAll(String[] actualElements, String[] expectedElements) {
        for (String expectedElement : expectedElements) {
            boolean found = false;
            for (String actualElement : actualElements) {
                if (actualElement.equals(expectedElement)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                return false;
            }
        }
        return true;
    }

    @Test(alwaysRun = true, groups = "wso2.is", description = "Discovery test", dependsOnMethods = { "testDiscovery" })
    public void testDiscoveryForInvalidIssuer() throws IOException {


        try (CloseableHttpClient client = HttpClients.createDefault()) {
            String discoveryUrl =  isServerBackendUrl + "/oauth2/invalidIssuer/.well-known/openid-configuration";
            HttpGet  request = new HttpGet(discoveryUrl);
            HttpResponse response = client.execute(request);
            assertEquals(response.getStatusLine().getStatusCode(), 400, "Expected a Bad Request " +
                    "(HTTP 400) response");
        }
    }
}

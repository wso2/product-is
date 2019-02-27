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

import org.apache.commons.lang.StringUtils;
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
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.charon.core.schema.SCIMConstants;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;

import java.io.IOException;

public class OIDCDiscoveryTestCase extends ISIntegrationTest {

    public static final String WEBFINGER_ENDPOINT_SUFFIX = "/.well-known/webfinger";
    public static final String RESOURCE = "resource";
    public static final String REL = "rel";
    private String isServerBackendUrl;
    private String webfingerEndpoint;
    private String relUri = "http://openid.net/specs/connect/1.0/issuer";
    private String discoveryBasePath;
    private DiscoveryConfig config;

    @Factory(dataProvider = "webfingerConfigProvider")
    public OIDCDiscoveryTestCase(DiscoveryConfig config) {
        if (log.isDebugEnabled()){
            log.info("SAML SSO Test initialized for " + config);
        }
        this.config = config;
    }


    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {
        super.init();
        isServerBackendUrl = isServer.getContextUrls().getWebAppURLHttps();
        webfingerEndpoint = isServerBackendUrl + WEBFINGER_ENDPOINT_SUFFIX + "?" + RESOURCE + "=" + config
                .getResource() + "&" + REL + "=" + relUri;
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() {

    }

    @Test(alwaysRun = true, groups = "wso2.is", description = "webfinger test")
    public void testWebFinger() throws IOException {

        ClientConfig clientConfig = new ClientConfig();
        BasicAuthSecurityHandler basicAuth = new BasicAuthSecurityHandler();
        basicAuth.setUserName(userInfo.getUserName());
        basicAuth.setPassword(userInfo.getPassword());
        clientConfig.handlers(basicAuth);

        RestClient restClient = new RestClient(clientConfig);
        Resource userResource = restClient.resource(webfingerEndpoint);
        String response = userResource.accept(SCIMConstants.APPLICATION_JSON).get(String.class);
        Object obj= JSONValue.parse(response);
        Object links = ((JSONObject)obj).get("links");
        Assert.assertNotNull(links);
        discoveryBasePath = ((JSONObject)((JSONArray)links).get(0)).get("href").toString();
        String urlExpected = isServerBackendUrl + "/oauth2/token";
        Assert.assertEquals(discoveryBasePath, urlExpected);
    }

    @Test(alwaysRun = true, groups = "wso2.is", description = "Discovery test", dependsOnMethods = { "testWebFinger" })
    public void testDiscovery() throws IOException {

        ClientConfig clientConfig = new ClientConfig();
        BasicAuthSecurityHandler basicAuth = new BasicAuthSecurityHandler();
        basicAuth.setUserName(userInfo.getUserName());
        basicAuth.setPassword(userInfo.getPassword());
        clientConfig.handlers(basicAuth);

        RestClient restClient = new RestClient(clientConfig);
        String discoveryUrl;
        if(discoveryBasePath.endsWith("/")){
            discoveryUrl = discoveryBasePath + ".well-known/openid-configuration";
        }else {
            discoveryUrl = discoveryBasePath + "/.well-known/openid-configuration";
        }
        Resource userResource = restClient.resource(discoveryUrl);
        String response = userResource.accept(SCIMConstants.APPLICATION_JSON).get(String.class);
        Object obj= JSONValue.parse(response);
        String authorization_endpoint = ((JSONObject)obj).get("authorization_endpoint").toString();
        Assert.assertEquals(authorization_endpoint, isServerBackendUrl + "/oauth2/authorize");
        String token_endpoint = ((JSONObject)obj).get("token_endpoint").toString();
        Assert.assertEquals(token_endpoint, isServerBackendUrl + "/oauth2/token");
        String userinfo_endpoint = ((JSONObject)obj).get("userinfo_endpoint").toString();
        Assert.assertEquals(userinfo_endpoint, isServerBackendUrl + "/oauth2/userinfo");
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

}

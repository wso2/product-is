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

package org.wso2.identity.integration.test.oauth2;

import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.cookie.CookieSpecProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.RFC6265CookieSpecProvider;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.AccessTokenConfiguration;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.InboundProtocols;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.OpenIDConnectConfiguration;

import static org.testng.Assert.assertNotNull;


public class OAuth2DPopTestCase extends OAuth2ServiceAbstractIntegrationTest {
    private static final String BINDING_TYPE = "DPoP";
    private static final boolean VALIDATE_TOKEN_BINDING = true;

    private CloseableHttpClient client;
    private OpenIDConnectConfiguration oidcConfig;

    private String appID;
    private String clientId;

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_USER);
        super.setSystemproperties();
        this.client = HttpClientBuilder.create()
                .disableRedirectHandling()
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setCookieSpec(CookieSpecs.DEFAULT)
                        .build())
                .setDefaultCookieSpecRegistry(RegistryBuilder.<CookieSpecProvider>create()
                        .register(CookieSpecs.DEFAULT, new RFC6265CookieSpecProvider())
                        .build())
                .build();

        this.appID = super.addApplication(this.getApplicationWithDpopEnabled());
        assertNotNull(this.appID, "Error while creating the application with DPoP enabled.");

        this.oidcConfig = super.restClient.getOIDCInboundDetails(this.appID);
        assertNotNull(this.oidcConfig, "Error while retrieving the OIDC configuration of the application.");

        this.clientId = this.oidcConfig.getClientId();
    }

    @AfterClass(alwaysRun = true)
    public void testClear() throws Exception {
        super.deleteApp(this.appID);
        this.client.close();
        super.restClient.closeHttpClient();
    }

    private ApplicationModel getApplicationWithDpopEnabled() {
        final ApplicationModel application = new ApplicationModel();
        AccessTokenConfiguration accessTokenConfiguration = new AccessTokenConfiguration().type("JWT");
        accessTokenConfiguration.setBindingType(BINDING_TYPE);
        accessTokenConfiguration.setValidateTokenBinding(VALIDATE_TOKEN_BINDING);

        OpenIDConnectConfiguration oidcConfig = new OpenIDConnectConfiguration();
        oidcConfig.setAccessToken(accessTokenConfiguration);

        InboundProtocols inboundProtocolsConfig = new InboundProtocols();
        inboundProtocolsConfig.setOidc(oidcConfig);

        application.setInboundProtocolConfiguration(inboundProtocolsConfig);
        application.setName("DPoPTestSP");

        return application;
    }

    @Test(groups = "wso2.is",
            description = "")
    public void test() throws Exception {

    }
}

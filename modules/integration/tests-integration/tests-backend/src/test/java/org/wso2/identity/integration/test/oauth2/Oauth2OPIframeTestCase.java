/*
 * Copyright (c) 2020, WSO2 LLC. (http://www.wso2.com).
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

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.context.beans.Tenant;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.InboundProtocols;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.OpenIDConnectConfiguration;
import org.wso2.identity.integration.test.restclients.OAuth2RestClient;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.wso2.identity.integration.test.utils.OAuth2Constant.USER_AGENT;

public class Oauth2OPIframeTestCase extends OAuth2ServiceAbstractIntegrationTest {

    public static final String CALL_BACK_URL =
            "https://localhost:9853/oidc/checksession?client_id=%s&redirect_uri=http" +
                    "://localhost:8888/playground2/oauth2client";
    private CloseableHttpClient client;
    private final AutomationContext context;
    private String applicationId;

    @DataProvider(name = "configProvider")
    public static Object[][] configProvider() {

        return new Object[][]{{TestUserMode.SUPER_TENANT_ADMIN}, {TestUserMode.TENANT_ADMIN}};
    }

    @Factory(dataProvider = "configProvider")
    public Oauth2OPIframeTestCase(TestUserMode userMode) throws Exception {

        super.init(userMode);
        context = new AutomationContext("IDENTITY", userMode);
    }

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        Tenant tenantInfo = context.getContextTenant();
        restClient = new OAuth2RestClient(serverURL, tenantInfo);
        setSystemproperties();
        client = HttpClients.createDefault();
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {

        deleteApp(applicationId);
        restClient.closeHttpClient();
        consumerKey = null;
    }

    @Test(groups = "wso2.is", description = "Check Oauth2 application registration")
    public void testOPIFrameRegex() throws Exception {

        ApplicationModel application = new ApplicationModel();

        List<String> grantTypes = new ArrayList<>();
        Collections.addAll(grantTypes, "authorization_code", "implicit", "password", "client_credentials",
                "refresh_token", "urn:ietf:params:oauth:grant-type:saml2-bearer", "iwa:ntlm");

        List<String> callBackUrls = new ArrayList<>();
        Collections.addAll(callBackUrls, OAuth2Constant.CALLBACK_URL_REGEXP);

        OpenIDConnectConfiguration oidcConfig = new OpenIDConnectConfiguration();
        oidcConfig.setGrantTypes(grantTypes);
        oidcConfig.setCallbackURLs(callBackUrls);

        InboundProtocols inboundProtocolsConfig = new InboundProtocols();
        inboundProtocolsConfig.setOidc(oidcConfig);

        application.setInboundProtocolConfiguration(inboundProtocolsConfig);
        application.setName(SERVICE_PROVIDER_NAME);

        applicationId = addApplication(application);
        Assert.assertNotNull(applicationId, "Application creation failed.");

        oidcConfig = getOIDCInboundDetailsOfApplication(applicationId);
        consumerKey = oidcConfig.getClientId();
        Assert.assertNotNull(consumerKey, "Application creation failed.");
        consumerSecret = oidcConfig.getClientSecret();

        HttpResponse response;

        String url = String.format(CALL_BACK_URL, consumerKey);
        response = sendGetRequest(url);
        EntityUtils.consume(response.getEntity());

    }

    private HttpResponse sendGetRequest(String url) throws Exception {

        HttpGet request = new HttpGet(url);
        request.addHeader("User-Agent", USER_AGENT);
        return client.execute(request);
    }
}

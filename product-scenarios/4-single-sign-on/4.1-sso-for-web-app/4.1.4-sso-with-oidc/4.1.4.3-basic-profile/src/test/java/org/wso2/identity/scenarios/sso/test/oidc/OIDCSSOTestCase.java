/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.identity.scenarios.sso.test.oidc;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.common.model.xsd.ServiceProvider;
import org.wso2.carbon.identity.oauth.stub.dto.OAuthConsumerAppDTO;
import org.wso2.identity.scenarios.commons.OAuth2CommonClient;
import org.wso2.identity.scenarios.commons.ScenarioTestBase;

import static org.wso2.identity.scenarios.commons.util.Constants.APPROVE_ONCE;
import static org.wso2.identity.scenarios.commons.util.Constants.IS_HTTPS_URL;

public class OIDCSSOTestCase extends ScenarioTestBase {

    private static final Log log = LogFactory.getLog(OIDCSSOTestCase.class);

    private static final String SP_CONFIG_FILE_1 = "sso-oidc.app1.xml";

    private static final String SP_CONFIG_FILE_2 = "sso-oidc.app2.xml";

    private CloseableHttpClient client;

    private ServiceProvider serviceProvider1;

    private ServiceProvider serviceProvider2;

    private OAuthConsumerAppDTO oAuthConsumerAppDTO1;

    private OAuthConsumerAppDTO oAuthConsumerAppDTO2;

    private OAuth2CommonClient oAuth2Client;

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {

        super.init();
        super.loginAndObtainSessionCookie();
        oAuth2Client = new OAuth2CommonClient(sessionCookie, backendServiceURL, getDeploymentProperty(IS_HTTPS_URL),
                configContext);

        populateTestData();
        client = createHttpClient();
    }

    @AfterClass(alwaysRun = true)
    public void clear() throws Exception {

        clearTestData();
        oAuth2Client.clearRuntimeVariables();
        client.close();
    }

    private void populateTestData() throws Exception {

        String spName1 = oAuth2Client.createServiceProvider(SP_CONFIG_FILE_1);
        Assert.assertNotNull(spName1, "Failed to create service provider from file: " + SP_CONFIG_FILE_1);
        serviceProvider1 = oAuth2Client.getServiceProvider(spName1);
        Assert.assertNotNull(serviceProvider1, "Failed to load service provider : " + spName1);
        oAuthConsumerAppDTO1 = oAuth2Client.getOAuthConsumerApp(serviceProvider1.getApplicationName());
        Assert.assertNotNull(oAuthConsumerAppDTO1, "Failed to load OAuth2 application in SP : " + spName1);

        String spName2 = oAuth2Client.createServiceProvider(SP_CONFIG_FILE_2);
        Assert.assertNotNull(spName1, "Failed to create service provider from file: " + SP_CONFIG_FILE_2);
        serviceProvider2 = oAuth2Client.getServiceProvider(spName2);
        Assert.assertNotNull(serviceProvider2, "Failed to load service provider : " + spName2);
        oAuthConsumerAppDTO2 = oAuth2Client.getOAuthConsumerApp(serviceProvider2.getApplicationName());
        Assert.assertNotNull(oAuthConsumerAppDTO2, "Failed to load OAuth2 application in SP : " + spName2);
    }

    private void clearTestData() throws Exception {

        oAuth2Client.deleteServiceProvider(serviceProvider1.getApplicationName());
        oAuth2Client.deleteServiceProvider(serviceProvider2.getApplicationName());
    }

    @Test(description = "4.1.4.1")
    public void sendAuthorizeRequestForSP1() throws Exception {

        HttpResponse response = oAuth2Client.sendAuthorizeGet(client, oAuthConsumerAppDTO1, "openid", null);
        Assert.assertNotNull(response, "Http response is null");

        try {
            oAuth2Client.setSessionDataKey(response);
        } finally {
            EntityUtils.consume(response.getEntity());
        }
        Assert.assertNotNull(oAuth2Client.getSessionDataKey(), "Authorization request failed. sessionDataKey is null");
    }

    @Test(description = "4.1.4.2",
          dependsOnMethods = "sendAuthorizeRequestForSP1")
    public void authenticateForSP1() throws Exception {

        HttpResponse response = oAuth2Client.sendLoginPost(client, ADMIN_USERNAME, ADMIN_PASSWORD);
        Assert.assertNotNull(response, "Http response is null");

        try {
            oAuth2Client.setConsentUrl(response);
        } finally {
            EntityUtils.consume(response.getEntity());
        }
        Assert.assertNotNull(oAuth2Client.getConsentUrl(), "Authentication failed. Consent URL is null");
    }

    @Test(description = "4.1.4.3",
          dependsOnMethods = "authenticateForSP1")
    public void initOAuthConsentForSP1() throws Exception {

        HttpResponse response = oAuth2Client.sendOAuthConsentRequest(client);
        Assert.assertNotNull(response, "Http response is null");

        try {
            oAuth2Client.setSessionDataKeyConsent(response);
        } finally {
            EntityUtils.consume(response.getEntity());
        }
        Assert.assertNotNull(oAuth2Client.getSessionDataKeyConsent(),
                "Failed to get the consent url. SessionDataKeyConsent is null");
    }

    @Test(description = "4.1.4.4",
          dependsOnMethods = "initOAuthConsentForSP1")
    public void submitOAuthConsentForSP1() throws Exception {

        HttpResponse response = oAuth2Client
                .postOAuthConsent(client, oAuth2Client.getSessionDataKeyConsent(), APPROVE_ONCE);
        Assert.assertNotNull(response, "Http response is null");

        String authorizeCode;
        try {
            authorizeCode = oAuth2Client.getAuthorizeCode(response);
        } finally {
            EntityUtils.consume(response.getEntity());
        }
        Assert.assertNotNull(authorizeCode, "Failed to get authorize code.");

        oAuth2Client.clearRuntimeVariables();
    }

    @Test(description = "4.1.4.5",
          dependsOnMethods = "submitOAuthConsentForSP1")
    public void sendAuthorizeRequestForSP2() throws Exception {

        HttpResponse response = oAuth2Client.sendAuthorizeGet(client, oAuthConsumerAppDTO2, "openid", null);
        Assert.assertNotNull(response, "Http response is null");

        try {
            oAuth2Client.setSessionDataKeyConsent(response);
        } finally {
            EntityUtils.consume(response.getEntity());
        }
        Assert.assertNotNull(oAuth2Client.getSessionDataKeyConsent(),
                "Failed to get the consent url. SessionDataKeyConsent is null. SSO failed.");
    }

    @Test(description = "4.1.4.6",
          dependsOnMethods = "sendAuthorizeRequestForSP2")
    public void submitOAuthConsentForSP2() throws Exception {

        HttpResponse response = oAuth2Client
                .postOAuthConsent(client, oAuth2Client.getSessionDataKeyConsent(), APPROVE_ONCE);
        Assert.assertNotNull(response, "Http response is null");

        String authorizeCode;
        try {
            authorizeCode = oAuth2Client.getAuthorizeCode(response);
        } finally {
            EntityUtils.consume(response.getEntity());
        }
        Assert.assertNotNull(authorizeCode, "Failed to get authorize code. SSO failed.");
    }
}

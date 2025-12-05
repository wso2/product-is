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

package org.wso2.identity.integration.test.saml;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Lookup;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.cookie.CookieSpecProvider;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.RFC6265CookieSpecProvider;
import org.apache.http.message.BasicNameValuePair;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.identity.integration.test.util.Utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;

import static org.wso2.identity.integration.test.utils.CommonConstants.IS_DEFAULT_HTTPS_PORT;

/**
 * This class contains the test method for IDP initiated SAML SSO login process with the relevant other behaviors.
 */
public class SAMLIdPInitiatedSSOTestCase extends AbstractSAMLSSOTestCase {

    private Log log = LogFactory.getLog(getClass());

    private static final String IDP_INIT_SSO_URL = "https://localhost:%s/samlsso?spEntityID=%s";
    private static final String IDP_INIT_SSO_TENANT_URL
            = "https://localhost:%s/t/wso2.com/samlsso?spEntityID=%s";
    private static final String SAML_SSO_URL = "https://localhost:%s/samlsso";
    private static final String SP_ACS_URL = "http://localhost:8490/%s/home.jsp";
    private HttpClient httpClient;
    private CookieStore cookieStore = new BasicCookieStore();
    private Lookup<CookieSpecProvider> cookieSpecRegistry;
    private RequestConfig requestConfig;
    private SAMLConfig samlConfig;
    private String appId;
    private String userId;

    @Factory(dataProvider = "samlConfigProvider")
    public SAMLIdPInitiatedSSOTestCase(SAMLConfig config) {

        if (log.isDebugEnabled()) {
            log.debug("SAML SSO Test initialized for " + config);
        }
        this.samlConfig = config;
    }

    @DataProvider(name = "samlConfigProvider")
    public static Object[][] samlConfigProvider() {

        return new SAMLConfig[][]{
                {new SAMLConfig(TestUserMode.SUPER_TENANT_ADMIN, User.SUPER_TENANT_USER, HttpBinding.HTTP_REDIRECT,
                        ClaimType.NONE, App.SUPER_TENANT_APP_WITH_SIGNING)},
                {new SAMLConfig(TestUserMode.TENANT_ADMIN, User.TENANT_USER, HttpBinding.HTTP_REDIRECT,
                        ClaimType.NONE, App.TENANT_APP_WITHOUT_SIGNING)},
        };
    }

    @BeforeClass(alwaysRun = true)
    public void initTest() throws Exception {

        super.init(samlConfig.getUserMode());
        super.testInit();
        appId = super.addApplication(samlConfig, samlConfig.getApp().getArtifact());
        cookieSpecRegistry = RegistryBuilder.<CookieSpecProvider>create()
                .register(CookieSpecs.DEFAULT, new RFC6265CookieSpecProvider())
                .build();
        requestConfig = RequestConfig.custom()
                .setCookieSpec(CookieSpecs.DEFAULT)
                .build();
        httpClient = HttpClientBuilder.create()
                .setDefaultCookieSpecRegistry(cookieSpecRegistry)
                .setDefaultRequestConfig(requestConfig)
                .setDefaultCookieStore(cookieStore).disableRedirectHandling().build();
        userId = super.addUser(samlConfig);
    }

    @AfterClass(alwaysRun = true)
    public void testClear() throws Exception {

        super.deleteUser(userId);
        super.deleteApp(appId);
        super.testClear();
    }

    @Test(groups = "wso2.is", description = "This test method will test the IdP initiated SAML SSO process.")
    public void testIdPInitiatedSSO() throws Exception {

        HttpResponse idpInitResponse;
        applicationMgtRestClient.updateInboundDetailsOfApplication(appId,
                super.getSAMLConfigurationsForIdPInit(samlConfig), "saml");
        if (samlConfig.getUserMode().name().equals("TENANT_ADMIN")) {
            idpInitResponse = Utils.sendGetRequest(String.format(IDP_INIT_SSO_TENANT_URL, IS_DEFAULT_HTTPS_PORT,
                    samlConfig.getApp().getArtifact()), USER_AGENT, httpClient);
        } else {
            idpInitResponse = Utils.sendGetRequest(String.format(IDP_INIT_SSO_URL, IS_DEFAULT_HTTPS_PORT,
                    samlConfig.getApp().getArtifact()), USER_AGENT, httpClient);
        }

        String redirectUrl = Utils.getRedirectUrl(idpInitResponse);
        Assert.assertTrue(redirectUrl.contains("/authenticationendpoint/login.do"), "Cannot find the login page.");

        String sessionDataKey = getSessionDataKeyFromRedirectUrl(redirectUrl);

        HttpResponse samlssoResponse = Utils.sendPOSTMessage(sessionDataKey, getTenantQualifiedURL(String.format(
                SAML_SSO_URL, IS_DEFAULT_HTTPS_PORT), samlConfig.getUser().getTenantDomain()), USER_AGENT, SP_ACS_URL,
                samlConfig.getApp().getArtifact(), samlConfig.getUser().getTenantAwareUsername(),
                samlConfig.getUser().getPassword(), httpClient,
                getTenantQualifiedURL(String.format(SAML_SSO_URL, IS_DEFAULT_HTTPS_PORT),
                        samlConfig.getUser().getTenantDomain()));

        List<NameValuePair> consentRequiredClaims = Utils.getConsentRequiredClaimsFromResponse(samlssoResponse);
        HttpResponse commonAuthResponse = setConsentForSP(sessionDataKey, consentRequiredClaims);

        String samlRedirectUrl = Utils.getRedirectUrl(commonAuthResponse);
        HttpResponse samlRedirectResponse = Utils.sendGetRequest(samlRedirectUrl, USER_AGENT, httpClient);

        String samlRedirectPage = extractDataFromResponse(samlRedirectResponse);
        Assert.assertTrue(samlRedirectPage.contains(String.format(SP_ACS_URL, samlConfig.getApp().getArtifact())), "Cannot find the assertion " +
                "consumer URL in the resulting page.");
    }

    private String getSessionDataKeyFromRedirectUrl(String redirectUrl) throws Exception {

        String sessionDataKey = "";
        Map<String, String> queryParams = Utils.getQueryParams(redirectUrl);
        for (Map.Entry<String, String> paramEntry : queryParams.entrySet()) {
            if (("sessionDataKey").equals(paramEntry.getKey())) {
                sessionDataKey = paramEntry.getValue();
            }
        }
        return sessionDataKey;
    }

    private HttpResponse setConsentForSP(String sessionDataKey, List<NameValuePair> consentRequiredClaims)
            throws IOException {

        String commonAuthUrl = "https://localhost:%s/commonauth";
        consentRequiredClaims.add(new BasicNameValuePair("consent", "approve"));
        consentRequiredClaims.add(new BasicNameValuePair("sessionDataKey", sessionDataKey));
        HttpPost consentPostRequest = new HttpPost(String.format(commonAuthUrl, IS_DEFAULT_HTTPS_PORT));
        consentPostRequest.addHeader("User-Agent", USER_AGENT);
        consentPostRequest.setEntity(new UrlEncodedFormEntity(consentRequiredClaims));
        return httpClient.execute(consentPostRequest);
    }

    private String extractDataFromResponse(HttpResponse response) throws IOException {

        BufferedReader rd = new BufferedReader(
                new InputStreamReader(response.getEntity().getContent()));
        StringBuilder result = new StringBuilder();
        String line;
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }
        rd.close();
        return result.toString();
    }
}

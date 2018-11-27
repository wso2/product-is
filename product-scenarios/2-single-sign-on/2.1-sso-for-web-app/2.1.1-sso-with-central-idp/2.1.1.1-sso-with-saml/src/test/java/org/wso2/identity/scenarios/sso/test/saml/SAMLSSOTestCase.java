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
package org.wso2.identity.scenarios.sso.test.saml;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.sso.saml.stub.types.SAMLSSOServiceProviderDTO;
import org.wso2.identity.scenarios.commons.TestUserMode;
import org.wso2.identity.scenarios.commons.util.IdentityScenarioUtil;

import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.assertTrue;
import static org.testng.AssertJUnit.assertNotNull;
import static org.wso2.identity.scenarios.commons.util.Constants.SAML_REQUEST_PARAM;
import static org.wso2.identity.scenarios.commons.util.Constants.SAML_RESPONSE_PARAM;
import static org.wso2.identity.scenarios.commons.util.DataExtractUtil.extractFullContentFromResponse;
import static org.wso2.identity.scenarios.commons.util.DataExtractUtil.getCookieFromResponse;
import static org.wso2.identity.scenarios.commons.util.DataExtractUtil.getRedirectUrlFromResponse;
import static org.wso2.identity.scenarios.commons.util.DataExtractUtil.getSessionDataKey;
import static org.wso2.identity.scenarios.commons.util.DataExtractUtil.isConsentRequested;
import static org.wso2.identity.scenarios.commons.util.IdentityScenarioUtil.sendGetRequest;
import static org.wso2.identity.scenarios.commons.util.SAMLSSOUtil.extractSAMLRequest;
import static org.wso2.identity.scenarios.commons.util.SAMLSSOUtil.extractSAMLResponse;
import static org.wso2.identity.scenarios.commons.util.SAMLSSOUtil.sendLoginPostMessage;
import static org.wso2.identity.scenarios.commons.util.SSOUtil.sendPOSTConsentMessage;
import static org.wso2.identity.scenarios.commons.util.SSOUtil.sendRedirectRequest;

public class SAMLSSOTestCase extends AbstractSAMLSSOTestCase {

    private static final Log log = LogFactory.getLog(SAMLSSOTestCase.class);

    // SAML Application attributes
    private static final String APPLICATION_NAME = "SAML-SSO-TestApplication";

    private static final String SAML_SSO_INDEX_URL = "/%s/";
    private static final String SAML_SSO_LOGOUT_URL = "/%s/logout?SAML2.HTTPBinding=%s";

    //Claim Uris
    private static final String firstNameClaimURI = "http://wso2.org/claims/givenname";
    private static final String lastNameClaimURI = "http://wso2.org/claims/lastname";
    private static final String emailClaimURI = "http://wso2.org/claims/emailaddress";

    private SAMLConfig config;
    private Header userAgentHeader;

    private String resultPage;

    @Factory(dataProvider = "samlConfigProvider")
    public SAMLSSOTestCase(SAMLConfig config) {
        if (log.isDebugEnabled()) {
            log.info("SAML SSO Test initialized for " + config);
        }
        this.config = config;
    }

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {
        userAgentHeader = new BasicHeader(HttpHeaders.USER_AGENT, USER_AGENT);
        super.testInit();
        super.createUser(config);
        super.createApplication(config, APPLICATION_NAME);

    }

    @AfterClass(alwaysRun = true)
    public void testClear() throws Exception {
        super.deleteUser(config);
        super.deleteApplication(APPLICATION_NAME);
        super.testClear();
    }

    @Test(description = "2.1.1.1.1", priority = 1)
    public void testAddSP() throws Exception {
        Boolean isAddSuccess = ssoConfigServiceClient.addServiceProvider(super.createSsoServiceProviderDTO(config));
        assertTrue(isAddSuccess, "Adding a service provider has failed for " + config);

        SAMLSSOServiceProviderDTO[] samlssoServiceProviderDTOs = ssoConfigServiceClient
                .getServiceProviders().getServiceProviders();
        Assert.assertEquals(samlssoServiceProviderDTOs[0].getIssuer(), config.getApp().getArtifact(),
                "Adding a service provider has failed for " + config);
    }

    @Test(description = "2.1.1.1.3", groups = "wso2.is", dependsOnMethods = {"testSAMLSSOLogin"})
    public void testRemoveSP()
            throws Exception {
        Boolean isAddSuccess = ssoConfigServiceClient.removeServiceProvider(config.getApp().getArtifact());
        assertTrue(isAddSuccess, "Removing a service provider has failed for " + config);
    }

    @Test(alwaysRun = true, description = "2.1.1.1.2", dependsOnMethods = {"testAddSP"})
    public void testSAMLSSOIsPassiveLogin() throws Exception {
        try {

            CloseableHttpClient client = HttpClients.createDefault();
            HttpResponse response;
            response = sendGetRequest(client, String.format(webAppHost + SAML_SSO_INDEX_URL, config.getApp()
                    .getArtifact(), config.getHttpBinding().binding), null, new Header[]{userAgentHeader});
            String samlResponse = extractSAMLResponse(response);
            assertNotNull(samlResponse, "SAMLResponse is not recived in Passive Login.");
            samlResponse = IdentityScenarioUtil.bese64Decode(samlResponse);
            assertTrue(samlResponse.contains("Destination=\"" + String.format(webAppHost + ACS_URL, config.getApp()
                    .getArtifact()) + "\""));
        } catch (Exception e) {
            Assert.fail("SAML SSO Login test failed for " + config, e);
        }
    }

    @Test(alwaysRun = true, description = "2.1.1.1.4", groups = "wso2.is",
            dependsOnMethods = {"testSAMLSSOIsPassiveLogin"})
    public void testSAMLSSOLogin() {
        try {
            HttpResponse response;

            response = sendGetRequest(httpClient, String.format(webAppHost + SAML_SSO_LOGIN_URL, config.getApp
                    ().getArtifact(), config.getHttpBinding().binding), null, new Header[]{userAgentHeader});

            if (config.getHttpBinding() == HttpBinding.HTTP_POST) {
                String samlRequest = extractSAMLRequest(response);
                assertNotNull(samlRequest, "SAML Request is not available");
                response = super.sendSAMLMessage(samlSSOIDPUrl, SAML_REQUEST_PARAM, samlRequest, config);
                EntityUtils.consume(response.getEntity());

                response = sendRedirectRequest(response, USER_AGENT, webAppHost + ACS_URL, config.getApp()
                                .getArtifact(),
                        httpClient);
            }

            String sessionKey = getSessionDataKey(response);
            assertNotNull(sessionKey, "SessionDataKey is not available in the response.");
            response = sendLoginPostMessage(sessionKey, samlSSOIDPUrl, USER_AGENT, webAppHost + ACS_URL, config
                            .getApp().getArtifact(), config.getUser().getUsername(), config.getUser().getPassword(),
                    httpClient);

            if (isConsentRequested(response)) {
                String pastrCookie = getCookieFromResponse(response, "pastr");
                assertNotNull(pastrCookie, "pastr cookie not found in response.");
                EntityUtils.consume(response.getEntity());

                response = sendPOSTConsentMessage(response, commonAuthUrl, USER_AGENT, String.format(webAppHost +
                        ACS_URL, config.getApp().getArtifact()), httpClient, pastrCookie);
                EntityUtils.consume(response.getEntity());
            }

            String redirectUrl = getRedirectUrlFromResponse(response);
            if (StringUtils.isNotBlank(redirectUrl)) {
                response = sendRedirectRequest(response, USER_AGENT, webAppHost + ACS_URL, config.getApp()
                        .getArtifact(), httpClient);
            }
            String samlResponse = extractSAMLResponse(response);
            EntityUtils.consume(response.getEntity());

            response = super.sendSAMLMessage(String.format(webAppHost + ACS_URL, config.getApp().getArtifact()),
                    SAML_RESPONSE_PARAM, samlResponse, config);
            resultPage = extractFullContentFromResponse(response);

            assertTrue(resultPage.contains("You are logged in as " + config.getUser().getTenantAwareUsername()),
                    "SAML SSO Login failed for " + config);
        } catch (Exception e) {
            Assert.fail("SAML SSO Login test failed for " + config, e);
        }
    }

    @DataProvider(name = "samlConfigProvider")
    public static SAMLConfig[][] samlConfigProvider() {
        return new SAMLConfig[][]{
                {new SAMLConfig(TestUserMode.SUPER_TENANT_ADMIN, User.SUPER_TENANT_USER, HttpBinding.HTTP_REDIRECT,
                        ClaimType.NONE, App.SUPER_TENANT_APP_WITH_SIGNING)}
        };
    }

    private void assertLocalClaims(String claims) {
        Map<String, String> attributeMap = extractClaims(claims);
        assertTrue(attributeMap.containsKey(firstNameClaimURI), "Claim nickname is expected");
        Assert.assertEquals(attributeMap.get(firstNameClaimURI), config.getUser().getNickname(),
                "Expected claim value for nickname is " + config.getUser().getNickname());
        assertTrue(attributeMap.containsKey(lastNameClaimURI), "Claim lastname is expected");
        Assert.assertEquals(attributeMap.get(lastNameClaimURI), config.getUser().getUsername(),
                "Expected claim value for lastname is " + config.getUser().getUsername());
        assertTrue(attributeMap.containsKey(emailClaimURI), "Claim email is expected");
        Assert.assertEquals(attributeMap.get(emailClaimURI), config.getUser().getEmail(),
                "Expected claim value for email is " + config.getUser().getEmail());
    }

    private void assertNoneClaims(String claims) {
        String[] dataArray = StringUtils.substringsBetween(claims, "<td>", "</td>");
        Assert.assertNull(dataArray, "Claims are not expected for " + config);
    }


    private Map<String, String> extractClaims(String claimString) {
        String[] dataArray = StringUtils.substringsBetween(claimString, "<td>", "</td>");
        Map<String, String> attributeMap = new HashMap<String, String>();
        String key = null;
        String value;
        for (int i = 0; i < dataArray.length; i++) {
            if ((i % 2) == 0) {
                key = dataArray[i];
            } else {
                value = dataArray[i].trim();
                attributeMap.put(key, value);
            }
        }

        return attributeMap;
    }

}

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
import org.apache.http.HttpStatus;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.apache.xml.security.signature.XMLSignature;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.sso.saml.stub.types.SAMLSSOServiceProviderDTO;
import org.wso2.identity.scenarios.commons.SAML2SSOTestBase;
import org.wso2.identity.scenarios.commons.SAMLConfig;
import org.wso2.identity.scenarios.commons.SAMLSSOExternalAppTestClient;
import org.wso2.identity.scenarios.commons.SCIM2CommonClient;
import org.wso2.identity.scenarios.commons.ScenarioTestBase;
import org.wso2.identity.scenarios.commons.TestConfig;
import org.wso2.identity.scenarios.commons.TestUserMode;
import org.wso2.identity.scenarios.commons.clients.usermgt.remote.RemoteUserStoreManagerServiceClient;
import org.wso2.identity.scenarios.commons.util.IdentityScenarioUtil;

import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;
import static org.testng.AssertJUnit.assertNotNull;
import static org.wso2.carbon.utils.multitenancy.MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
import static org.wso2.identity.scenarios.commons.util.Constants.ClaimURIs.EMAIL_CLAIM_URI;
import static org.wso2.identity.scenarios.commons.util.Constants.ClaimURIs.FIRST_NAME_CLAIM_URI;
import static org.wso2.identity.scenarios.commons.util.Constants.ClaimURIs.LAST_NAME_CLAIM_URI;
import static org.wso2.identity.scenarios.commons.util.Constants.HttpBinding.HTTP_POST;
import static org.wso2.identity.scenarios.commons.util.Constants.HttpBinding.HTTP_REDIRECT;
import static org.wso2.identity.scenarios.commons.util.Constants.IS_HTTPS_URL;
import static org.wso2.identity.scenarios.commons.util.Constants.SAML_REQUEST_PARAM;
import static org.wso2.identity.scenarios.commons.util.Constants.SAML_RESPONSE_PARAM;
import static org.wso2.identity.scenarios.commons.util.DataExtractUtil.extractFullContentFromResponse;
import static org.wso2.identity.scenarios.commons.util.DataExtractUtil.getCookieFromResponse;
import static org.wso2.identity.scenarios.commons.util.DataExtractUtil.getRedirectUrlFromResponse;
import static org.wso2.identity.scenarios.commons.util.DataExtractUtil.getSessionDataKey;
import static org.wso2.identity.scenarios.commons.util.DataExtractUtil.getTestUser;
import static org.wso2.identity.scenarios.commons.util.DataExtractUtil.isConsentRequested;
import static org.wso2.identity.scenarios.commons.util.IdentityScenarioUtil.getJSONFromResponse;
import static org.wso2.identity.scenarios.commons.util.IdentityScenarioUtil.sendGetRequest;
import static org.wso2.identity.scenarios.commons.util.SAMLSSOUtil.sendLoginPostMessage;
import static org.wso2.identity.scenarios.commons.util.SSOUtil.sendPOSTConsentMessage;
import static org.wso2.identity.scenarios.commons.util.SSOUtil.sendRedirectRequest;

public class SAMLSSOWithExternalAppTestCase extends ScenarioTestBase {

    private static final Log log = LogFactory.getLog(SAMLSSOWithExternalAppTestCase.class);
    String testScenarioIdentifier;

    // SAML Application attributes
    private static final String APPLICATION_NAME = "SAML-SSO-TestApplication";
    private static final String ISSUER_NAME = "travelocity.com";

    private static final String USER_AGENT = "Apache-HttpClient/4.2.5 (java 1.5)";

    private SAMLConfig config;
    private Header userAgentHeader;

    private SAMLSSOExternalAppTestClient samlssoExternalAppClient;
    private RemoteUserStoreManagerServiceClient remoteUSMServiceClient;
    private CloseableHttpClient httpClient;

    private SCIM2CommonClient scim2Client;

    @Factory(dataProvider = "samlSSOConfigProvider")
    public SAMLSSOWithExternalAppTestCase(SAMLConfig config, String testScenarioIdentifier) {
        if (log.isDebugEnabled()) {
            log.info("SAML SSO Test initialized for " + config);
        }
        this.config = config;
        this.testScenarioIdentifier = testScenarioIdentifier + " - " + config.getHttpBinding();
    }

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {
        userAgentHeader = new BasicHeader(HttpHeaders.USER_AGENT, USER_AGENT);
        httpClient = createHttpClient();
        scim2Client = new SCIM2CommonClient(getDeploymentProperty(IS_HTTPS_URL));
        super.init();
        loginAndObtainSessionCookie();
        remoteUSMServiceClient = new RemoteUserStoreManagerServiceClient(backendServiceURL, sessionCookie);
        samlssoExternalAppClient = new SAMLSSOExternalAppTestClient(backendURL, sessionCookie, backendServiceURL,
                webAppHost, configContext, config);
        Thread.sleep(5000);
    }

    @AfterClass(alwaysRun = true)
    public void testClear() {
        samlssoExternalAppClient.clear();
    }

    @Test(description = "4.1.1.1", priority = 1)
    public void retrieveSAMLSSOServiceProvider() throws Exception {

        SAMLSSOServiceProviderDTO samlssoServiceProviderDTOs = samlssoExternalAppClient.getSAMLSSOServiceProviderByIssuer(config.getArtifact());
        Assert.assertNotNull(samlssoServiceProviderDTOs, "Adding a service provider has failed for " + config);
        samlssoExternalAppClient.clear();
        httpClient.close();
    }

    @Test(alwaysRun = true, description = "4.1.1.2", dependsOnMethods = {"retrieveSAMLSSOServiceProvider"})
    public void testSAMLSSOIsPassiveLogin() {
        try {

            samlssoExternalAppClient = new SAMLSSOExternalAppTestClient(backendURL, sessionCookie, backendServiceURL,
                    webAppHost, configContext, config);
            httpClient = createHttpClient();
            HttpResponse response;
            response = sendGetRequest(httpClient, samlssoExternalAppClient.getSamlAppIndexUrl(), null, new
                    Header[]{userAgentHeader});
            String samlResponse = SAML2SSOTestBase.extractSAMLResponse(response);
            assertNotNull(samlResponse, "SAMLResponse is not recived in Passive Login.");
            samlResponse = IdentityScenarioUtil.bese64Decode(samlResponse);
            assertTrue(samlResponse.contains("Destination=\"" + samlssoExternalAppClient.getAcsUrl() + "\""));
            samlssoExternalAppClient.clear();
            httpClient.close();
        } catch (Exception e) {
            Assert.fail("SAML SSO Login test failed for " + config, e);
        }
    }

    @Test(alwaysRun = true, description = "4.1.1.4", groups = "wso2.is",
            dependsOnMethods = {"testSAMLSSOIsPassiveLogin"})
    public void testSAMLSSOLogin() {
        try {
            HttpResponse response;

            samlssoExternalAppClient = new SAMLSSOExternalAppTestClient(backendURL, sessionCookie, backendServiceURL,
                    webAppHost, configContext, config);
            httpClient = createHttpClient();

            response = sendGetRequest(httpClient, samlssoExternalAppClient.getSamlSSOLoginUrl(), null, new
                    Header[]{userAgentHeader});

            if (HTTP_POST.equals(config.getHttpBinding())) {
                String samlRequest = SAMLSSOExternalAppTestClient.extractSAMLRequest(response);
                assertNotNull(samlRequest, "SAML Request is not available");
                response = samlssoExternalAppClient.sendSAMLMessage(samlssoExternalAppClient.getSamlSSOIDPUrl(),
                        SAML_REQUEST_PARAM, samlRequest, config, httpClient);
                EntityUtils.consume(response.getEntity());

                response = sendRedirectRequest(response, USER_AGENT, samlssoExternalAppClient.getAcsUrl(),
                        httpClient);
            }

            String sessionKey = getSessionDataKey(response);
            assertNotNull(sessionKey, "SessionDataKey is not available in the response.");
            response = sendLoginPostMessage(sessionKey, samlssoExternalAppClient.getSamlSSOIDPUrl(), USER_AGENT,
                    samlssoExternalAppClient.getAcsUrl(), config.getUser().getUsername(), config.getUser().getPassword(),
                    httpClient);

            if (isConsentRequested(response)) {
                String pastrCookie = getCookieFromResponse(response, "pastr");
                assertNotNull(pastrCookie, "pastr cookie not found in response.");
                EntityUtils.consume(response.getEntity());

                response = sendPOSTConsentMessage(response, samlssoExternalAppClient.getCommonAuthUrl(), USER_AGENT,
                        samlssoExternalAppClient.getAcsUrl(), httpClient, pastrCookie);
                EntityUtils.consume(response.getEntity());
            }

            String redirectUrl = getRedirectUrlFromResponse(response);
            if (StringUtils.isNotBlank(redirectUrl)) {
                response = sendRedirectRequest(response, USER_AGENT, samlssoExternalAppClient.getAcsUrl(),
                        httpClient);
            }
            String samlResponse = SAML2SSOTestBase.extractSAMLResponse(response);
            EntityUtils.consume(response.getEntity());

            response = samlssoExternalAppClient.sendSAMLMessage(samlssoExternalAppClient.getAcsUrl(),
                    SAML_RESPONSE_PARAM, samlResponse, config, httpClient);
            String resultPage = extractFullContentFromResponse(response);
            if (resultPage.contains("You are logged in as")) {
                HttpResponse user = scim2Client.filterUserByAttribute(
                        httpClient, "username", "Eq" , config.getUser().getTenantAwareUsername() ,
                        ADMIN_USERNAME, ADMIN_PASSWORD);
                assertEquals(user.getStatusLine().getStatusCode(), HttpStatus.SC_OK,
                        "Failed to retrieve the user");
                JSONObject list = getJSONFromResponse(user);
                JSONArray resourcesArray = (JSONArray) list.get("Resources");
                JSONObject userObject = (JSONObject) resourcesArray.get(0);
                assertTrue(resultPage.contains("You are logged in as " + userObject.get("id")),
                        "SAML SSO Login failed for " + config);
            } else {
                fail("SAML SSO Login failed for" + config);
            }
            samlssoExternalAppClient.clear();
            httpClient.close();
        } catch (Exception e) {
            Assert.fail("SAML SSO Login test failed for " + config, e);
        }
    }

    @DataProvider(name = "samlSSOConfigProvider")
    public static Object[][] samlSSOConfigProvider() throws Exception {
        return new Object[][]{
                {new SAMLConfig(TestUserMode.SUPER_TENANT_ADMIN, new TestConfig.User(getTestUser("super-tenant-user" +
                        ".json"), SUPER_TENANT_DOMAIN_NAME), TestConfig.ClaimType.NONE, HTTP_REDIRECT, null,
                        ISSUER_NAME, "", XMLSignature.ALGO_ID_SIGNATURE_RSA, "", true), "SAML Login"},
                {new SAMLConfig(TestUserMode.SUPER_TENANT_ADMIN, new TestConfig.User(getTestUser("super-tenant-user" +
                        ".json"), SUPER_TENANT_DOMAIN_NAME), TestConfig.ClaimType.NONE, HTTP_POST, null,
                        ISSUER_NAME, "", XMLSignature.ALGO_ID_SIGNATURE_RSA, "", true), "SAML Login"}
        };
    }

    private void assertLocalClaims(String claims) {
        Map<String, String> attributeMap = extractClaims(claims);
        assertTrue(attributeMap.containsKey(FIRST_NAME_CLAIM_URI), "Claim nickname is expected");
        Assert.assertEquals(attributeMap.get(FIRST_NAME_CLAIM_URI), config.getUser().getUserClaim(FIRST_NAME_CLAIM_URI),
                "Expected claim value for nickname is " + config.getUser().getUserClaim(FIRST_NAME_CLAIM_URI));
        assertTrue(attributeMap.containsKey(LAST_NAME_CLAIM_URI), "Claim lastname is expected");
        Assert.assertEquals(attributeMap.get(LAST_NAME_CLAIM_URI), config.getUser().getUserClaim(LAST_NAME_CLAIM_URI),
                "Expected claim value for lastname is " + config.getUser().getUsername());
        assertTrue(attributeMap.containsKey(EMAIL_CLAIM_URI), "Claim email is expected");
        Assert.assertEquals(attributeMap.get(EMAIL_CLAIM_URI), config.getUser().getUserClaim(EMAIL_CLAIM_URI),
                "Expected claim value for email is " + config.getUser().getUserClaim(EMAIL_CLAIM_URI));
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

    @AfterMethod(alwaysRun = true)
    public void changeTestCaseName(ITestResult result) {
        result.getMethod().setDescription(result.getMethod().getDescription() + "-" + testScenarioIdentifier);
    }

}

/*
 * Copyright (c) 2016, WSO2 LLC. (http://www.wso2.com).
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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.opensaml.xml.util.Base64;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.identity.integration.common.clients.sso.saml.query.ClientSignKeyDataHolder;
import org.wso2.identity.integration.common.clients.sso.saml.query.QueryClientUtils;
import org.wso2.identity.integration.common.clients.sso.saml.query.SAMLQueryClient;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.SAML2Configuration;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.SAML2ServiceProvider;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.SAMLRequestValidation;
import org.wso2.identity.integration.test.rest.api.server.keystore.management.v1.model.CertificateRequest;
import org.wso2.identity.integration.test.restclients.KeystoreMgtRestClient;
import org.wso2.identity.integration.test.util.Utils;
import org.wso2.identity.integration.test.utils.CommonConstants;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.wso2.identity.integration.test.util.Utils.requestMissingClaims;

/**
 * Test case of SAMLQuery service.
 */
public class SAMLQueryProfileTestCase extends AbstractSAMLSSOTestCase {

    private static final Log log = LogFactory.getLog(SAMLQueryProfileTestCase.class);
    // SAML Application attributes
    private static final String USER_AGENT = "Apache-HttpClient/4.2.5 (java 1.5)";
    private static final String APPLICATION_NAME = "SAML-SSO-Query-TestApplication";
    private static final String WSO2IS_URL = "https://localhost:9853/";
    private static final String WSO2IS_TENANT_URL = WSO2IS_URL + "t/wso2.com";
    private String tenantedSamlSSOUrl;
    private static final String COMMON_AUTH_URL = WSO2IS_URL + "/commonauth";
    private static final String ACS_URL = "http://localhost:8490/%s/home.jsp";
    private static final String SAML_SSO_LOGIN_URL = "http://localhost:8490/%s/samlsso?SAML2.HTTPBinding=%s";
    private static final String KEYSTORE_ALIAS = "wso2carbon";
    //Claim Uris
    private static final String firstNameClaimURI = "http://wso2.org/claims/givenname";
    private static final String lastNameClaimURI = "http://wso2.org/claims/lastname";
    private static final String emailClaimURI = "http://wso2.org/claims/emailaddress";

    private final SAMLConfig config;
    private String resultPage;
    private String samlResponse;
    private String userId;
    private String appId;

    @Factory(dataProvider = "samlConfigProvider")
    public SAMLQueryProfileTestCase(SAMLConfig config) {

        if (log.isDebugEnabled()) {
            log.info("SAML SSO Test initialized for " + config);
        }
        this.config = config;
    }

    @BeforeTest
    public void initiateTenant() throws Exception {

        // Since all the requests sign with default wso2 key, upload that public key to tenants
        super.init(TestUserMode.TENANT_ADMIN);
        tenantedSamlSSOUrl = getTenantQualifiedURL(WSO2IS_URL + "samlsso", config.getUser().getTenantDomain());
        KeystoreMgtRestClient keystoreMgtRestClient = new KeystoreMgtRestClient(serverURL, tenantInfo);

        if (!keystoreMgtRestClient.checkCertInStore(KEYSTORE_ALIAS)) {
            String filePath = FrameworkPathUtil.getSystemResourceLocation() +
                    "keystores" + File.separator + "products" + File.separator + "wso2carbon.pem";
            String cert = StringUtils.substringBetween(new String(Files.readAllBytes(Paths.get(filePath))),
                    "-----BEGIN CERTIFICATE-----\n", "-----END CERTIFICATE-----\n");

            keystoreMgtRestClient.importCertToStore(new CertificateRequest()
                    .alias(KEYSTORE_ALIAS)
                    .certificate(cert));
        }
    }

    @DataProvider(name = "samlConfigProvider")
    public static SAMLConfig[][] samlConfigProvider() {

        return new SAMLConfig[][]{
                {new SAMLConfig(TestUserMode.SUPER_TENANT_ADMIN, User.SUPER_TENANT_USER, HttpBinding.HTTP_REDIRECT,
                        ClaimType.NONE, App.SUPER_TENANT_APP_WITH_SIGNING)},
                {new SAMLConfig(TestUserMode.SUPER_TENANT_ADMIN, User.SUPER_TENANT_USER, HttpBinding.HTTP_REDIRECT,
                        ClaimType.LOCAL, App.SUPER_TENANT_APP_WITH_SIGNING)},
                {new SAMLConfig(TestUserMode.SUPER_TENANT_ADMIN, User.SUPER_TENANT_USER, HttpBinding.HTTP_POST,
                        ClaimType.NONE, App.SUPER_TENANT_APP_WITH_SIGNING)},
                {new SAMLConfig(TestUserMode.SUPER_TENANT_ADMIN, User.SUPER_TENANT_USER, HttpBinding.HTTP_POST,
                        ClaimType.LOCAL, App.SUPER_TENANT_APP_WITH_SIGNING)},
                {new SAMLConfig(TestUserMode.TENANT_ADMIN, User.TENANT_USER, HttpBinding.HTTP_REDIRECT,
                        ClaimType.NONE, App.TENANT_APP_WITHOUT_SIGNING)},
                {new SAMLConfig(TestUserMode.TENANT_ADMIN, User.TENANT_USER, HttpBinding.HTTP_REDIRECT,
                        ClaimType.LOCAL, App.TENANT_APP_WITHOUT_SIGNING)},
                {new SAMLConfig(TestUserMode.TENANT_ADMIN, User.TENANT_USER, HttpBinding.HTTP_POST,
                        ClaimType.NONE, App.TENANT_APP_WITHOUT_SIGNING)},
                {new SAMLConfig(TestUserMode.TENANT_ADMIN, User.TENANT_USER, HttpBinding.HTTP_POST,
                        ClaimType.LOCAL, App.TENANT_APP_WITHOUT_SIGNING)},
        };
    }

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.init();

        super.init(config.getUserMode());

        super.testInit();
        userId = super.addUser(config);
        appId = super.addApplication(config, APPLICATION_NAME);
    }

    @AfterClass(alwaysRun = true)
    public void testClear() throws Exception {

        super.deleteUser(userId);
        super.deleteApp(appId);

        super.testClear();
    }

    @Test(description = "Add service provider", groups = "wso2.is", priority = 1)
    public void testAddSP() throws Exception {

        applicationMgtRestClient.updateInboundDetailsOfApplication(appId, getSAMLConfigurations(config), SAML);
        SAML2ServiceProvider samlConfig = applicationMgtRestClient.getSAMLInboundDetails(appId);
        Assert.assertNotNull(samlConfig, "Adding a service provider has failed for " + config);
        Assert.assertEquals(samlConfig.getIssuer(), config.getApp().getArtifact(),
                "Adding a service provider has failed for " + config);
    }

    @Test(description = "Remove service provider", groups = "wso2.is", dependsOnMethods = {"testClaims"})
    public void testRemoveSP()
            throws Exception {

        Boolean isDeleteSuccess = applicationMgtRestClient.deleteInboundConfiguration(appId, "saml");
        Assert.assertTrue(isDeleteSuccess, "Removing a service provider has failed for " + config);
    }

    @Test(alwaysRun = true, description = "Testing SAML SSO login", groups = "wso2.is",
            dependsOnMethods = {"testAddSP"})
    public void testSAMLSSOLogin() {

        try {
            HttpResponse response;

            response = Utils.sendGetRequest(String.format(SAML_SSO_LOGIN_URL, config.getApp().getArtifact(), config
                    .getHttpBinding().binding), USER_AGENT, httpClient);

            if (config.getHttpBinding() == AbstractSAMLSSOTestCase.HttpBinding.HTTP_POST){
                String samlRequest = Utils.extractDataFromResponse(response, CommonConstants.SAML_REQUEST_PARAM, 5);
                response = super.sendSAMLMessage(SAML_SSO_URL, CommonConstants.SAML_REQUEST_PARAM, samlRequest, config);
                EntityUtils.consume(response.getEntity());

                response = Utils.sendRedirectRequest(response, USER_AGENT, ACS_URL, config.getApp().getArtifact(),
                        httpClient);
            }

            String sessionKey = Utils.extractDataFromResponse(response, CommonConstants.SESSION_DATA_KEY, 1);
            response = Utils.sendPOSTMessage(sessionKey, tenantedSamlSSOUrl, USER_AGENT, ACS_URL, config.getApp()
                            .getArtifact(), config.getUser().getUsername(), config.getUser().getPassword(), httpClient,
                    tenantedSamlSSOUrl);


            if (requestMissingClaims(response)) {
                String pastrCookie = Utils.getPastreCookie(response);
                Assert.assertNotNull(pastrCookie, "pastr cookie not found in response.");
                EntityUtils.consume(response.getEntity());

                response = Utils.sendPOSTConsentMessage(response, COMMON_AUTH_URL, USER_AGENT, String.format(ACS_URL, config.getApp()
                        .getArtifact()), httpClient, pastrCookie);
                EntityUtils.consume(response.getEntity());
            }

            String redirectUrl = Utils.getRedirectUrl(response);
            if(StringUtils.isNotBlank(redirectUrl)) {
                response = Utils.sendRedirectRequest(response, USER_AGENT, ACS_URL, config.getApp().getArtifact(),
                        httpClient);
            }
            String samlResponse = Utils.extractDataFromResponse(response, CommonConstants.SAML_RESPONSE_PARAM, 5);
            EntityUtils.consume(response.getEntity());

            response = super.sendSAMLMessage(String.format(ACS_URL, config.getApp().getArtifact()), CommonConstants
                    .SAML_RESPONSE_PARAM, samlResponse, config);
            resultPage = extractDataFromResponse(response);

            Assert.assertTrue(resultPage.contains("You are logged in as " + userId),
                    "SAML SSO Login failed for " + config);
            this.samlResponse = new String(Base64.decode(samlResponse));

        } catch (Exception e) {
            Assert.fail("SAML SSO Login test failed for " + config, e);
        }
    }

    @Test(alwaysRun = true, description = "Testing SAML SSO Claims", groups = "wso2.is",
            dependsOnMethods = {"testSAMLAttributeQueryRequest"})
    public void testClaims() {

        String claimString = resultPage.substring(resultPage.lastIndexOf("<table>"));

        switch (config.getClaimType()) {
            case LOCAL:
                assertLocalClaims(claimString);
                break;
            case NONE:
                assertNoneClaims(claimString);
                break;
        }
    }

    @Test(alwaysRun = true, description = "Testing Assertion ID Request", groups = "wso2.is",
            dependsOnMethods = {"testSAMLSSOLogin"})
    public void testSAMLAssertionIDRequest() {

        try {
            log.info("RESPONSE " + this.samlResponse);
            String id = QueryClientUtils.getAssertionId(this.samlResponse);
            URL resourceUrl = getClass().getResource(ISIntegrationTest.URL_SEPARATOR + "keystores" + ISIntegrationTest.URL_SEPARATOR
                    + "products" + ISIntegrationTest.URL_SEPARATOR + "wso2carbon.p12");
            ClientSignKeyDataHolder signKeyDataHolder = null;
            try {
                signKeyDataHolder = new ClientSignKeyDataHolder(resourceUrl.getPath(),
                        "wso2carbon", "wso2carbon");
            } catch (Exception e) {
                Assert.fail("Unable to initiate client sign key data holder" + config, e);
            }
            String serverURL = TestUserMode.TENANT_ADMIN.equals(config.getUserMode()) ? WSO2IS_TENANT_URL : WSO2IS_URL;
            SAMLQueryClient queryClient = new SAMLQueryClient(serverURL, signKeyDataHolder);
            String response = queryClient.executeIDRequest(config.getApp().getArtifact(), id);
            Assert.assertTrue(response.contains(id));
        } catch (Exception e) {
            Assert.fail("SAML SSO Logout test failed for " + config, e);
        }
    }

    @Test(alwaysRun = true, description = "Testing Attribute Query Request", groups = "wso2.is",
            dependsOnMethods = {"testSAMLAssertionIDRequest"})
    public void testSAMLAttributeQueryRequest() {

        try {
            URL resourceUrl = getClass().getResource(ISIntegrationTest.URL_SEPARATOR + "keystores" + ISIntegrationTest.URL_SEPARATOR
                    + "products" + ISIntegrationTest.URL_SEPARATOR + "wso2carbon.p12");
            ClientSignKeyDataHolder signKeyDataHolder = new ClientSignKeyDataHolder(resourceUrl.getPath(),
                    "wso2carbon", "wso2carbon");
            String serverURL = TestUserMode.TENANT_ADMIN.equals(config.getUserMode()) ? WSO2IS_TENANT_URL : WSO2IS_URL;
            SAMLQueryClient queryClient = new SAMLQueryClient(serverURL, signKeyDataHolder);

            List<String> attributes = new ArrayList<>();
            attributes.add(firstNameClaimURI);
            attributes.add(lastNameClaimURI);
            attributes.add(emailClaimURI);

            String response = queryClient.executeAttributeQuery(config.getApp().getArtifact(),
                    config.getUser().getUsername(), attributes);
            Assert.assertTrue(response.contains(config.getUser().getEmail()));
        } catch (Exception e) {
            Assert.fail("SAML SSO Logout test failed for " + config, e);
        }
    }

    private void assertLocalClaims(String claims) {

        Map<String, String> attributeMap = extractClaims(claims);
        Assert.assertTrue(attributeMap.containsKey(firstNameClaimURI), "Claim nickname is expected");
        Assert.assertEquals(attributeMap.get(firstNameClaimURI), config.getUser().getNickname(),
                "Expected claim value for nickname is " + config.getUser().getNickname());
        Assert.assertTrue(attributeMap.containsKey(lastNameClaimURI), "Claim lastname is expected");
        Assert.assertEquals(attributeMap.get(lastNameClaimURI), config.getUser().getUsername(),
                "Expected claim value for lastname is " + config.getUser().getUsername());
        Assert.assertTrue(attributeMap.containsKey(emailClaimURI), "Claim email is expected");
        Assert.assertEquals(attributeMap.get(emailClaimURI), config.getUser().getEmail(),
                "Expected claim value for email is " + config.getUser().getEmail());
    }

    private void assertNoneClaims(String claims) {

        String[] dataArray = StringUtils.substringsBetween(claims, "<td>", "</td>");
        Assert.assertNull(dataArray, "Claims are not expected for " + config);
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

    private Map<String, String> extractClaims(String claimString) {

        String[] dataArray = StringUtils.substringsBetween(claimString, "<td>", "</td>");
        Map<String, String> attributeMap = new HashMap<>();
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

    public SAML2Configuration getSAMLConfigurations(SAMLConfig config) {

        SAML2Configuration samlConfig = super.getSAMLConfigurations(config);
        samlConfig.getManualConfiguration().setRequestValidation(new SAMLRequestValidation()
                .enableSignatureValidation(false)
                .signatureValidationCertAlias("wso2carbon"));
        samlConfig.getManualConfiguration().setEnableAssertionQueryProfile(true);

        return samlConfig;
    }
}

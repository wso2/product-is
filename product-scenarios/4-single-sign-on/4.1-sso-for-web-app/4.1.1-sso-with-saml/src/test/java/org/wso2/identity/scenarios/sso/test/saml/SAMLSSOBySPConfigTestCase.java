/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.xml.security.signature.XMLSignature;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.AuthnRequest;
import org.opensaml.saml2.core.Response;
import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.common.model.xsd.ServiceProvider;
import org.wso2.carbon.identity.sso.saml.stub.types.SAMLSSOServiceProviderDTO;
import org.wso2.identity.scenarios.commons.SAML2SSOTestBase;
import org.wso2.identity.scenarios.commons.SAMLConfig;
import org.wso2.identity.scenarios.commons.ScenarioTestBase;
import org.wso2.identity.scenarios.commons.TestConfig;
import org.wso2.identity.scenarios.commons.TestUserMode;
import org.wso2.identity.scenarios.commons.clients.usermgt.remote.RemoteUserStoreManagerServiceClient;

import java.util.HashMap;
import java.util.Map;

import static org.wso2.carbon.utils.multitenancy.MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
import static org.wso2.identity.scenarios.commons.util.Constants.SigningProperties.SIGNATURE_ALGORITHM_SHA1_RSA;
import static org.wso2.identity.scenarios.commons.util.Constants.SigningProperties.XML_DIGEST_ALGORITHM_SHA1;
import static org.wso2.identity.scenarios.commons.util.DataExtractUtil.getCookieFromResponse;
import static org.wso2.identity.scenarios.commons.util.DataExtractUtil.getRedirectUrlFromResponse;
import static org.wso2.identity.scenarios.commons.util.DataExtractUtil.getTestUser;
import static org.wso2.identity.scenarios.commons.util.DataExtractUtil.isConsentRequested;
import static org.wso2.identity.scenarios.commons.util.SSOUtil.sendPOSTConsentMessage;
import static org.wso2.identity.scenarios.commons.util.SSOUtil.sendRedirectRequest;

/**
 * SAML SSO tests using SP configuration file.
 */
public class SAMLSSOBySPConfigTestCase extends ScenarioTestBase {

    private static final Log log = LogFactory.getLog(SAMLSSOBySPConfigTestCase.class);
    private static final String SP_CONFIG_FILE = "sso-saml-app.xml";
    private static final String SP_CONFIG_FILE_2 = "sso-saml-app-2.xml";
    private static final String SP_NAME = "sso-saml.app";
    private static final String SP_NAME_2 = "sso-saml.app-2";
    private static final String USER_AGENT = "Apache-HttpClient/4.2.5 (java 1.5)";
    private static final String defaultProfileName = "default";

    private CloseableHttpClient client;
    private ServiceProvider serviceProvider;
    private SAMLSSOServiceProviderDTO samlssoServiceProviderDTO;
    private SAMLConfig samlConfig;
    private String spName;
    private String spConfigFile;
    private SAML2SSOTestBase saml2SSOTestBase;
    private RemoteUserStoreManagerServiceClient remoteUSMServiceClient;
    private String testScenarioIdentifier;
    Response samlResponse;

    @Factory(dataProvider = "samlConfigProvider")
    public SAMLSSOBySPConfigTestCase(String spName, String spConfigFile,  SAMLConfig config, String
            testScenarioIdentifier) {

        this.samlConfig = config;
        this.spName = spName;
        this.spConfigFile = spConfigFile;
        this.testScenarioIdentifier = testScenarioIdentifier;
        if (log.isDebugEnabled()) {
            log.debug("SAML SSO Test initialized for " + config + " with SP: " + spName);
        }
    }

    @DataProvider(name = "samlConfigProvider")
    public static Object[][] samlConfigProvider() throws Exception {

        Map<String, String[]> params = new HashMap<>();
        return new Object[][]{
                {SP_NAME,SP_CONFIG_FILE, new SAMLConfig(TestUserMode.SUPER_TENANT_USER, new TestConfig.User(getTestUser
                        ("super-tenant-user-2.json"), SUPER_TENANT_DOMAIN_NAME), TestConfig.ClaimType.NONE,
                        SAMLConstants.SAML2_REDIRECT_BINDING_URI, params, "travelocity", SIGNATURE_ALGORITHM_SHA1_RSA, XMLSignature
                        .ALGO_ID_SIGNATURE_RSA, XML_DIGEST_ALGORITHM_SHA1, true), "SAML Login with Assertion " +
                        "Encryption" },
                {SP_NAME_2 , SP_CONFIG_FILE_2,  new SAMLConfig(TestUserMode.SUPER_TENANT_USER, new TestConfig.User
                (getTestUser("super-tenant-user-3.json"), SUPER_TENANT_DOMAIN_NAME), TestConfig.ClaimType.NONE,
                        SAMLConstants.SAML2_REDIRECT_BINDING_URI, params, "travelocity", SIGNATURE_ALGORITHM_SHA1_RSA, XMLSignature
                        .ALGO_ID_SIGNATURE_RSA, XML_DIGEST_ALGORITHM_SHA1, true), "SAML Login without Assertion " +
                        "Encryption" }
        };
    }

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.init();
        this.client = createHttpClient();
        loginAndObtainSessionCookie();
        this.saml2SSOTestBase = new SAML2SSOTestBase(backendURL, backendServiceURL, sessionCookie, configContext);
        this.remoteUSMServiceClient = new RemoteUserStoreManagerServiceClient(backendServiceURL, sessionCookie);

        populateTestData();

        this.serviceProvider = saml2SSOTestBase.getServiceProvider(spName);
        Assert.assertNotNull(serviceProvider, "Failed to load service provider : " + spName);

        this.samlssoServiceProviderDTO = saml2SSOTestBase.getSAMLSSOServiceProvider(serviceProvider);
        Assert.assertNotNull(samlssoServiceProviderDTO, "Failed to load SAML2 application in SP : " + spName);

        Thread.sleep(5000);
    }

    private void populateTestData() throws Exception {

        super.createUser(samlConfig, remoteUSMServiceClient, defaultProfileName);
        spName = saml2SSOTestBase.createServiceProvider(spConfigFile);
        Assert.assertNotNull(spName, "Failed to create service provider from file: " + spConfigFile);
    }

    @AfterClass(alwaysRun = true)
    public void clear() throws Exception {

        cleanUpTestData();
        saml2SSOTestBase.clearRuntimeVariables();
        client.close();
    }

    private void cleanUpTestData() throws Exception {
        if (serviceProvider != null) {
            saml2SSOTestBase.deleteServiceProvider(serviceProvider.getApplicationName());
        }
        deleteUser(samlConfig, remoteUSMServiceClient);
    }

    @AfterMethod(alwaysRun = true)
    public void changeTestCaseName(ITestResult result) {
        result.getMethod().setDescription(result.getMethod().getDescription() + "-" + testScenarioIdentifier);
    }

    @Test(description = "4.1.1.4", priority = 1)
    public void testSAMLSSOLogin() {

        try {
            HttpResponse response = sendSAMLAuthenticationRequest();

            response = saml2SSOTestBase.sendLoginPostMessage(response, samlssoServiceProviderDTO
                            .getDefaultAssertionConsumerUrl(), samlConfig.getArtifact(), samlConfig.getUser()
                    .getUsername(), samlConfig.getUser().getPassword(), client, USER_AGENT);

            response = handleUserConsent(response);

            String redirectUrl = getRedirectUrlFromResponse(response);
            if (StringUtils.isNotBlank(redirectUrl)) {
                response = sendRedirectRequest(response, USER_AGENT, samlssoServiceProviderDTO
                        .getDefaultAssertionConsumerUrl(), client);
            }

            samlResponse = saml2SSOTestBase.extractAndProcessSAMLResponse(response);
            Assertion assertion = saml2SSOTestBase.getAssertionFromSAMLResponse(samlResponse, samlssoServiceProviderDTO,
                    saml2SSOTestBase.getDefaultX509Cred());
            Assert.assertNotNull(assertion, "SAML Assertion was not found in the response for " +  samlConfig
                    .toString());

        } catch (Exception e) {
            Assert.fail("SAML SSO Login test failed for " + samlConfig.toString(), e);
        }
    }
    @Test(description = "4.1.1.4.1", dependsOnMethods = "testSAMLSSOLogin")
    public void validateSAMLResponseSignature() throws Exception {
        Assert.assertTrue(saml2SSOTestBase.validateSAMLResponseSignature(samlResponse, samlssoServiceProviderDTO,
                saml2SSOTestBase.getDefaultX509Cred()), "SAML response signature validation failed for " +
                samlConfig.toString());
    }

    @Test(description = "4.1.1.4.2", dependsOnMethods = "testSAMLSSOLogin")
    public void validateAssertionSignature() throws Exception {
        Assert.assertTrue(saml2SSOTestBase.validateSAMLAssertionSignature(samlResponse, samlssoServiceProviderDTO,
                saml2SSOTestBase.getDefaultX509Cred()), "Assertion signature validation failed for " +
                samlConfig.toString());
    }

    @Test(description = "4.1.1.4.3", dependsOnMethods = "testSAMLSSOLogin")
    public void validateAudiance() throws Exception {
        Assert.assertTrue(saml2SSOTestBase.validateAudienceRestrictionBySAMLSSOSPConfig(samlResponse,
                samlssoServiceProviderDTO, saml2SSOTestBase.getDefaultX509Cred()), "Audience restriction " +
                "validation failed for " + samlConfig.toString());
    }

    private HttpResponse handleUserConsent(HttpResponse response) throws Exception {
        if (isConsentRequested(response)) {
            String pastrCookie = getCookieFromResponse(response, "pastr");
            Assert.assertNotNull(pastrCookie, "pastr cookie not found in response.");
            EntityUtils.consume(response.getEntity());

            response = sendPOSTConsentMessage(response, saml2SSOTestBase.getCommonauthEndpoint(), USER_AGENT,
                    String.format(samlssoServiceProviderDTO.getDefaultAssertionConsumerUrl(),
                            samlConfig.getArtifact()), client, pastrCookie);
            EntityUtils.consume(response.getEntity());
        }
        return response;
    }

    private HttpResponse sendSAMLAuthenticationRequest() throws Exception {
        AuthnRequest authnRequest = saml2SSOTestBase.buildAuthnRequest(samlssoServiceProviderDTO, false,
                false, samlConfig);
        return saml2SSOTestBase.sendSAMLAuthenticationRequest(client, authnRequest, samlConfig,
                samlssoServiceProviderDTO, saml2SSOTestBase.getDefaultX509Cred(), USER_AGENT);
    }
}

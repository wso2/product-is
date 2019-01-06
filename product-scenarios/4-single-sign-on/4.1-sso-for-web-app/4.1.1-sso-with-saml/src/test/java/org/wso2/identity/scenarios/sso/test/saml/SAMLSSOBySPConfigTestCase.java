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
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.xml.security.signature.XMLSignature;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.AuthnRequest;
import org.opensaml.saml2.core.Response;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.common.model.xsd.ServiceProvider;
import org.wso2.carbon.identity.sso.saml.stub.types.SAMLSSOServiceProviderDTO;
import org.wso2.identity.scenarios.commons.SAML2SSOTestBase;
import org.wso2.identity.scenarios.commons.SAMLConfig;
import org.wso2.identity.scenarios.commons.TestConfig;
import org.wso2.identity.scenarios.commons.TestUserMode;

import java.util.HashMap;
import java.util.Map;

import static org.wso2.identity.scenarios.commons.util.DataExtractUtil.getCookieFromResponse;
import static org.wso2.identity.scenarios.commons.util.DataExtractUtil.getRedirectUrlFromResponse;
import static org.wso2.identity.scenarios.commons.util.DataExtractUtil.isConsentRequested;
import static org.wso2.identity.scenarios.commons.util.SSOUtil.sendPOSTConsentMessage;
import static org.wso2.identity.scenarios.commons.util.SSOUtil.sendRedirectRequest;

public class SAMLSSOBySPConfigTestCase extends SAML2SSOTestBase {

    private static final Log log = LogFactory.getLog(SAMLSSOBySPConfigTestCase.class);
    private static final String SP_CONFIG_FILE = "sso-saml-app.xml";
    private static final String SIGNATURE_ALGORITHM = "SHA1withRSA";
    private static final String XML_DIGEST_ALGORITHM_SHA1 = "http://www.w3.org/2000/09/xmldsig#sha1";

    private CloseableHttpClient client;
    private ServiceProvider serviceProvider;
    private SAMLSSOServiceProviderDTO samlssoServiceProviderDTO;
    private SAMLConfig samlConfig;

    @Factory(dataProvider = "samlConfigProvider")
    public SAMLSSOBySPConfigTestCase(SAMLConfig config) {

        if (log.isDebugEnabled()) {
            log.info("SAML SSO Test initialized for " + config);
        }
        this.samlConfig = config;
    }

    @DataProvider(name = "samlConfigProvider")
    public static SAMLConfig[][] samlConfigProvider() {

        Map<String, String[]> params = new HashMap<>();
        return new SAMLConfig[][]{
                {new SAMLConfig(TestUserMode.SUPER_TENANT_ADMIN, TestConfig.User.SUPER_TENANT_USER,
                        TestConfig.ClaimType.NONE, SAMLConstants.SAML2_REDIRECT_BINDING_URI, params, "travelocity",
                        SIGNATURE_ALGORITHM, XMLSignature.ALGO_ID_SIGNATURE_RSA,
                        XML_DIGEST_ALGORITHM_SHA1, true)}
        };
    }

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.init();
        super.createUserFromTestConfig(samlConfig);

        client = HttpClients.createDefault();
        String spName = createServiceProvider(SP_CONFIG_FILE);
        Assert.assertNotNull(spName, "Failed to create service provider from file: " + SP_CONFIG_FILE);

        serviceProvider = getServiceProvider(spName);
        Assert.assertNotNull(serviceProvider, "Failed to load service provider : " + spName);

        samlssoServiceProviderDTO = getSAMLSSOServiceProvider(serviceProvider);
        Assert.assertNotNull(samlssoServiceProviderDTO, "Failed to load SAML2 application in SP : " + spName);
    }

    @AfterClass(alwaysRun = true)
    public void clear() throws Exception {

        if (serviceProvider != null) {
            deleteServiceProvider(serviceProvider.getApplicationName());
        }
        deleteUser(samlConfig, remoteUSMServiceClient);
        clearRuntimeVariables();
        client.close();
    }

    @Test(description = "4.1.1.4", priority = 1)
    public void testSAMLSSOLogin() {

        try {
            AuthnRequest authnRequest = buildAuthnRequest(samlssoServiceProviderDTO, false,
                    false, samlConfig, samlSSOIDPUrl);
            HttpResponse response = sendSAMLAuthenticationRequest(client, authnRequest, samlConfig, samlSSOIDPUrl,
                    samlssoServiceProviderDTO, getDefaultX509Cred());
            response = sendLoginPostMessage(response, samlSSOIDPUrl, USER_AGENT,
                    samlssoServiceProviderDTO.getDefaultAssertionConsumerUrl(), samlConfig.getArtifact(),
                    samlConfig.getUser().getUsername(), samlConfig.getUser().getPassword(), client);

            if (isConsentRequested(response)) {
                String pastrCookie = getCookieFromResponse(response, "pastr");
                Assert.assertNotNull(pastrCookie, "pastr cookie not found in response.");
                EntityUtils.consume(response.getEntity());

                response = sendPOSTConsentMessage(response, getCommonauthEndpoint(), USER_AGENT,
                        String.format(samlssoServiceProviderDTO.getDefaultAssertionConsumerUrl(),
                                samlConfig.getArtifact()), client, pastrCookie);
                EntityUtils.consume(response.getEntity());
            }
            String redirectUrl = getRedirectUrlFromResponse(response);
            if (StringUtils.isNotBlank(redirectUrl)) {
                response = sendRedirectRequest(response, USER_AGENT, samlssoServiceProviderDTO
                        .getDefaultAssertionConsumerUrl(), samlConfig.getArtifact(), client);
            }
            Response samlResponse = extractAndProcessSAMLResponse(response);
            Assertion assertion = getAssertionFromSAMLResponse(samlResponse, samlssoServiceProviderDTO,
                    getDefaultX509Cred());
            Assert.assertNotNull(assertion, "SAML Assertion was not found in the response.");

            Assert.assertTrue(validateAudienceRestrictionBySAMLSSOSPConfig(samlResponse, samlssoServiceProviderDTO,
                    getDefaultX509Cred()), "Audience restriction validation failed.");
            Assert.assertTrue(validateSAMLAssertionSignature(samlResponse, samlssoServiceProviderDTO,
                    getDefaultX509Cred()), "Assertion signature validation failed.");
            Assert.assertTrue(validateSAMLResponseSignature(samlResponse, samlssoServiceProviderDTO,
                    getDefaultX509Cred()), "SAML response signature validation failed.");
        } catch (Exception e) {
            Assert.fail("SAML SSO Login test failed for " + samlConfig.toString(), e);
        }
    }
}

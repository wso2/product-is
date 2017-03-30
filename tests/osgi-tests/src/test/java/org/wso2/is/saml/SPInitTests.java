/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.is.saml;

import com.google.common.net.HttpHeaders;
import org.apache.commons.io.Charsets;
import org.opensaml.saml2.core.AuthnRequest;
import org.opensaml.saml2.core.Response;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.CoreOptions;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerSuite;
import org.ops4j.pax.exam.testng.listener.PaxExam;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.auth.saml2.common.SAML2AuthConstants;
import org.wso2.carbon.identity.auth.saml2.common.SAML2AuthUtils;
import org.wso2.carbon.identity.authenticator.inbound.saml2sso.exception.SAML2SSOServerException;
import org.wso2.carbon.identity.common.base.exception.IdentityException;
import org.wso2.carbon.identity.gateway.common.model.sp.ServiceProviderConfig;
import org.wso2.carbon.kernel.utils.CarbonServerInfo;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;
import javax.inject.Inject;
import javax.ws.rs.HttpMethod;

/**
 * General tests for SAML inbound SP Init.
 */
@Listeners(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
public class SPInitTests {

    private static final Logger log = LoggerFactory.getLogger(SPInitTests.class);

    @Inject
    private BundleContext bundleContext;

    @Inject
    private CarbonServerInfo carbonServerInfo;


    @Configuration
    public Option[] createConfiguration() {

        List<Option> optionList = OSGiTestUtils.getDefaultSecurityPAXOptions();

        optionList.add(CoreOptions.systemProperty("java.security.auth.login.config")
                .value(Paths.get(OSGiTestUtils.getCarbonHome(), "conf", "security", "carbon-jaas.config")
                        .toString()));

        return optionList.toArray(new Option[optionList.size()]);
    }

    /**
     * SAML request without signature validation turned on.
     */
    @Test
    public void testSPInitSSOWithAllConfigs() throws IdentityException {
        ServiceProviderConfig serviceProviderConfig = TestUtils.getServiceProviderConfigs
                (TestConstants.SAMPLE_ISSUER_NAME, bundleContext);
        Properties originalReqValidatorConfigs = serviceProviderConfig.getRequestValidationConfig()
                .getRequestValidatorConfigs().get(0).getProperties();
        Properties originalResponseBuilderConfigs = serviceProviderConfig.getResponseBuildingConfig()
                .getResponseBuilderConfigs().get(0).getProperties();
        try {

            applyAllConfigs(originalReqValidatorConfigs, originalResponseBuilderConfigs, serviceProviderConfig);
            AuthnRequest samlRequest = TestUtils.buildAuthnRequest("https://localhost:9292/gateway",
                    false, false, TestConstants.SAMPLE_ISSUER_NAME, TestConstants.ACS_URL);
            String samlRequestString = SAML2AuthUtils.encodeForRedirect(samlRequest);
            SAML2AuthUtils.encodeForPost(SAML2AuthUtils.marshall(samlRequest));

            StringBuilder httpQueryString = new StringBuilder(SAML2AuthConstants.SAML_REQUEST + "=" +
                    samlRequestString);
            httpQueryString.append("&" + SAML2AuthConstants.RELAY_STATE + "=" + URLEncoder.encode("relayState",
                    StandardCharsets.UTF_8.name()).trim());
            SAML2AuthUtils.addSignatureToHTTPQueryString(httpQueryString, SAML2AuthConstants.XML
                            .SignatureAlgorithmURI.RSA_SHA1,
                    SAML2AuthUtils.getServerCredentials());

            HttpURLConnection urlConnection = TestUtils.request(TestConstants.GATEWAY_ENDPOINT
                    + "?" + httpQueryString.toString(), HttpMethod.GET, false);

            String content = TestUtils.getContent(urlConnection);
            String relayState = TestUtils.getParameterFromHTML(content, "'RelayState' value='", "'>");
            String fedSamlResponse = TestUtils.getSAMLResponse(false, TestConstants
                    .CARBON_SERVER, true, true);
            fedSamlResponse = URLEncoder.encode(fedSamlResponse);
            urlConnection = TestUtils.request(TestConstants.GATEWAY_ENDPOINT, HttpMethod.POST,
                    true);
            String postData = "SAMLResponse=" + fedSamlResponse + "&" + "RelayState=" + relayState;
            urlConnection.setDoOutput(true);
            urlConnection.getOutputStream().write(postData.toString().getBytes(Charsets.UTF_8));
            urlConnection.getResponseCode();


            String cookie = TestUtils.getResponseHeader(HttpHeaders.SET_COOKIE, urlConnection);

            cookie = cookie.split(org.wso2.carbon.identity.gateway.common.util.Constants.GATEWAY_COOKIE + "=")[1];
            Assert.assertNotNull(cookie);
            String response = TestUtils.getContent(urlConnection);
            String samlResponse = response.split("SAMLResponse' value='")[1].split("'>")[0];
            try {
                Response samlResponseObject = TestUtils.getSAMLResponse(samlResponse);
                Assert.assertTrue(samlResponseObject.getAssertions().isEmpty());
                Assert.assertNotNull(samlResponseObject.getSignature());
                Assert.assertFalse(samlResponseObject.getEncryptedAssertions().isEmpty());
            } catch (SAML2SSOServerException e) {
                log.error("Error while building response object from SAML response string", e);
            }

        } catch (IOException e) {
            Assert.fail("Error while running testSAMLAssertionWithoutRequestValidation test case");
        } finally {
            serviceProviderConfig.getRequestValidationConfig().getRequestValidatorConfigs().get(0).setProperties
                    (originalReqValidatorConfigs);
            serviceProviderConfig.getResponseBuildingConfig().getResponseBuilderConfigs().get(0).setProperties
                    (originalResponseBuilderConfigs);
        }
    }


    private void applyAllConfigs(Properties originalReqValidatorConfigs, Properties
            originalResponseBuilderConfigs, ServiceProviderConfig serviceProviderConfig) {
        Properties newReqValidatorConfigs = (Properties) originalReqValidatorConfigs.clone();
        Properties newResponseBuilderConfigs = (Properties) originalResponseBuilderConfigs.clone();

        // ACS, defaultACS, signingAlgo, DigestAlgo, EncryptionCert, Signing Certificate, AttributeConsumerUrl,
        // NameIdFormat,
        // NotOrAfterPeriod are set in sample.yaml SP by def`ault. Therefore no need to explicitly enable them
        newReqValidatorConfigs.put(SAML2AuthConstants.Config.Name.IDP_INIT_SSO_ENABLED, "true");
        newReqValidatorConfigs.put(SAML2AuthConstants.Config.Name.AUTHN_REQUEST_SIGNED, "true");
        newResponseBuilderConfigs.put(SAML2AuthConstants.Config.Name.AUTHN_RESPONSE_ENCRYPTED, "true");
        newResponseBuilderConfigs.put(SAML2AuthConstants.Config.Name.AUTHN_RESPONSE_SIGNED, "true");
        newResponseBuilderConfigs.put(SAML2AuthConstants.Config.Name.SEND_CLAIMS_ALWAYS, "true");
        serviceProviderConfig.getRequestValidationConfig().getRequestValidatorConfigs().get(0).setProperties
                (newReqValidatorConfigs);
        serviceProviderConfig.getResponseBuildingConfig().getResponseBuilderConfigs().get(0).setProperties
                (newResponseBuilderConfigs);
    }

}

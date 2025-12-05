/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.identity.integration.test.oauth2;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Lookup;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.cookie.CookieSpecProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.RFC6265CookieSpecProvider;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationResponseModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.InboundProtocols;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.OpenIDConnectConfiguration;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.SAML2Configuration;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.SAML2ServiceProvider;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.SAMLAssertionConfiguration;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.SAMLAttributeProfile;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.SAMLResponseSigning;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.SingleLogoutProfile;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.SingleSignOnProfile;
import org.wso2.identity.integration.test.util.Utils;
import org.wso2.identity.integration.test.utils.CommonConstants;
import org.wso2.identity.integration.test.utils.OAuth2Constant;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

/**
 * Test cases related to the OAuth2 SAML2 bearer grant type.
 */
public class OAuth2ServiceSAML2BearerGrantTestCase extends OAuth2ServiceAbstractIntegrationTest {

    private static final Log log = LogFactory.getLog(OAuth2ServiceSAML2BearerGrantTestCase.class);

    private static final String COMMON_AUTH_URL = "https://localhost:9853/commonauth";
    private static final String USER_AGENT = "Apache-HttpClient/4.2.5 (java 1.5)";
    private static final String ACS_URL = "http://localhost:8490/%s/home.jsp";
    private static final String SAML_SSO_URL = "https://localhost:9853/samlsso";
    private static final String ISSUER = "travelocity.com";

    private Lookup<CookieSpecProvider> cookieSpecRegistry;
    private RequestConfig requestConfig;
    private CloseableHttpClient client;
    private String samlAppId;

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.init(TestUserMode.SUPER_TENANT_USER);

        ApplicationResponseModel application = createSAMLApplication();
        samlAppId = application.getId();
        OpenIDConnectConfiguration oidcConfig = getOIDCInboundDetailsOfApplication(samlAppId);
        consumerKey = oidcConfig.getClientId();
        consumerSecret = oidcConfig.getClientSecret();

        cookieSpecRegistry = RegistryBuilder.<CookieSpecProvider>create()
                .register(CookieSpecs.DEFAULT, new RFC6265CookieSpecProvider())
                .build();
        requestConfig = RequestConfig.custom()
                .setCookieSpec(CookieSpecs.DEFAULT)
                .build();
        client = HttpClientBuilder.create()
                .setDefaultRequestConfig(requestConfig)
                .setDefaultCookieSpecRegistry(cookieSpecRegistry)
                .build();
        log.info(String.format("Oauth app initialized with key: %s, secret: %s.", consumerKey, consumerSecret));
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {

        deleteApp(samlAppId);
        client.close();
        restClient.closeHttpClient();
    }

    @Test
    public void testSAML2BearerValidSAMLAssertion() {

        try {
            // Get a SAML response.
            String samlResponse = getSAMLResponse();

            // Extract the assertion from SAML response.
            String samlAssersion = getSAMLAssersion(samlResponse);

            // Send the extracted SAML assertion to token endpoint in SAML2 bearer grant.
            HttpResponse httpResponse = sendSAMLAssertion(samlAssersion);

            // Extract the response value from http response.
            String responseValue = IOUtils.toString(httpResponse.getEntity().getContent());

            // If we have an access token in the response test is successful.
            Assert.assertTrue(responseValue.contains("access_token"));
        } catch (Exception e) {
            Assert.fail("SAML Bearer Grant test failed with an exception.", e);
        }
    }

    @Test(dependsOnMethods = "testSAML2BearerValidSAMLAssertion")
    public void testSAML2BearerInvalidAudience() {

        try {

            client = HttpClientBuilder.create()
                    .setDefaultRequestConfig(requestConfig)
                    .setDefaultCookieSpecRegistry(cookieSpecRegistry)
                    .build();
            // Set some invalid audience.
            SAML2ServiceProvider saml2AppConfig = getSAMLInboundDetailsOfApplication(samlAppId);
            Assert.assertNotNull(saml2AppConfig, "No service provider exists for issuer" + ISSUER);

            saml2AppConfig.getSingleSignOnProfile().getAssertion().setAudiences(new ArrayList<>());
            updateApplicationInboundConfig(samlAppId, new SAML2Configuration().manualConfiguration(saml2AppConfig),
                    SAML);

            // Get a SAML response.
            String samlResponse = getSAMLResponse();

            // Extract the assertion from SAML response.
            String samlAssersion = getSAMLAssersion(samlResponse);

            // Send the extracted SAML assertion to token endpoint in SAML2 bearer grant.
            HttpResponse httpResponse = sendSAMLAssertion(samlAssersion);

            // We should get an http 400 error code.
            Assert.assertEquals(httpResponse.getStatusLine().getStatusCode(), 400);

            // We should get a non-empty error message.
            Assert.assertTrue(StringUtils.isNotBlank(IOUtils.toString(httpResponse.getEntity().getContent())));
        } catch (Exception e) {
            Assert.fail("SAML Bearer Grant test failed with an exception.", e);
        } finally {
            // We have to initiate the http client again or other tests will fail.
            client = HttpClientBuilder.create()
                    .setDefaultRequestConfig(requestConfig)
                    .setDefaultCookieSpecRegistry(cookieSpecRegistry)
                    .build();
        }
    }

    /**
     * Create a SAML Application for testing.
     *
     * @return ApplicationResponseModel application.
     * @throws Exception If an error occurred while creating a SAML application.
     */
    private ApplicationResponseModel createSAMLApplication() throws Exception {

        ApplicationModel applicationCreationModel = new ApplicationModel().name(SERVICE_PROVIDER_NAME);
        applicationCreationModel.inboundProtocolConfiguration(new InboundProtocols().oidc(getOIDCConfigurations()));
        applicationCreationModel.getInboundProtocolConfiguration().setSaml(getSAMLConfigurations());

        String appId = addApplication(applicationCreationModel);

        return getApplication(appId);
    }

    /**
     * Create OIDC Configured ApplicationModel object.
     *
     * @return ApplicationModel Application.
     */
    private OpenIDConnectConfiguration getOIDCConfigurations() {

        List<String> grantTypes = new ArrayList<>();
        Collections.addAll(grantTypes, "urn:ietf:params:oauth:grant-type:saml2-bearer");

        List<String> callBackUrls = new ArrayList<>();
        Collections.addAll(callBackUrls, OAuth2Constant.AUTHORIZED_URL);

        OpenIDConnectConfiguration oidcConfig = new OpenIDConnectConfiguration();
        oidcConfig.setGrantTypes(grantTypes);
        oidcConfig.setCallbackURLs(callBackUrls);

        return oidcConfig;
    }

    /**
     * Create SAML Configured ApplicationModel object.
     *
     * @return ApplicationModel application.
     */
    private SAML2Configuration getSAMLConfigurations() {

        SAML2ServiceProvider serviceProvider = new SAML2ServiceProvider();
        serviceProvider.setIssuer(ISSUER);
        serviceProvider.addAssertionConsumerUrl(String.format("http://localhost:8490/%s/home.jsp", ISSUER));
        serviceProvider.setDefaultAssertionConsumerUrl(String.format("http://localhost:8490/%s/home.jsp", ISSUER));
        serviceProvider.setAttributeProfile(new SAMLAttributeProfile().enabled(true));
        serviceProvider.setSingleLogoutProfile(new SingleLogoutProfile().enabled(true));
        serviceProvider.setResponseSigning(new SAMLResponseSigning().enabled(true));

        SAMLAssertionConfiguration assertion = new SAMLAssertionConfiguration();
        assertion.addAudiencesItem(OAuth2Constant.ACCESS_TOKEN_ENDPOINT);
        assertion.addRecipientsItem(OAuth2Constant.ACCESS_TOKEN_ENDPOINT);

        SingleSignOnProfile ssoProfile = new SingleSignOnProfile().attributeConsumingServiceIndex("1239245949");
        ssoProfile.setAssertion(assertion);

        serviceProvider.setSingleSignOnProfile(ssoProfile);

        SAML2Configuration saml2Configuration = new SAML2Configuration();
        saml2Configuration.setManualConfiguration(serviceProvider);

        return saml2Configuration;
    }

    /**
     * Get the SAML response by calling the default SAML endpoint.
     *
     * @return SAML response.
     * @throws Exception If an error occurred while getting the SAML response.
     */
    private String getSAMLResponse() throws Exception {

        HttpResponse response;
        response = Utils.sendGetRequest(String.format("http://localhost:8490/%s/samlsso?SAML2.HTTPBinding=%s",
                "travelocity.com", "HTTP-POST"), USER_AGENT, client);

        Assert.assertEquals(response.getStatusLine().getStatusCode(), 200, "HTTP 200 expected for POST binding " +
                "initiation request");
        String samlRequest = Utils.extractDataFromResponse(response, CommonConstants.SAML_REQUEST_PARAM, 5);
        Assert.assertTrue(StringUtils.isNotBlank(samlRequest), "SAML request in response body is empty");
        response = sendSAMLRequest(samlRequest);
        EntityUtils.consume(response.getEntity());

        // Added temporarily to debug intermittent failure.
        if (response.getStatusLine().getStatusCode() != 302) {
            String responseBody = EntityUtils.toString(response.getEntity());
            log.error("Unexpected status code " + response.getStatusLine().getStatusCode() + "\n\nResponse " +
                    "body:\n" + responseBody);
        }
        Assert.assertEquals(response.getStatusLine().getStatusCode(), 302, "Expected a redirection response to SAML " +
                "request.");
        Assert.assertTrue(StringUtils.isNotBlank(Utils.getRedirectUrl(response)), "Location header not present in the" +
                " response to SAML request");

        response = Utils.sendRedirectRequest(response, USER_AGENT, ACS_URL, "travelocity.com", client);

        Assert.assertEquals(response.getStatusLine().getStatusCode(), 200, "HTTP 200 expected for request login page.");
        String sessionKey = Utils.extractDataFromResponse(response, CommonConstants.SESSION_DATA_KEY, 1);
        Assert.assertTrue(StringUtils.isNotBlank(sessionKey), "Session Key can't be empty.");
        response = Utils.sendPOSTMessage(sessionKey, getTenantQualifiedURL(COMMON_AUTH_URL, userInfo.getUserDomain()),
                USER_AGENT, ACS_URL, "travelocity.com", userInfo.getUserNameWithoutDomain(),
                userInfo.getPassword(), client);
        EntityUtils.consume(response.getEntity());

        if (requestMissingClaims(response)) {
            String pastrCookie = Utils.getPastreCookie(response);
            Assert.assertNotNull(pastrCookie, "pastr cookie not found in response.");
            EntityUtils.consume(response.getEntity());

            response = Utils.sendPOSTConsentMessage(response, COMMON_AUTH_URL, USER_AGENT, String.format(ACS_URL,
                    "travelocity.com"), client, pastrCookie);
            EntityUtils.consume(response.getEntity());
        }

        String redirectUrl = Utils.getRedirectUrl(response);
        if (StringUtils.isNotBlank(redirectUrl)) {
            response = Utils.sendRedirectRequest(response, USER_AGENT, ACS_URL, "travelocity.com", client);
        }

        String samlResponse = Utils.extractDataFromResponse(response, CommonConstants.SAML_RESPONSE_PARAM, 5);
        Assert.assertTrue(StringUtils.isNotBlank(samlResponse), "SAML response in response body is empty");
        return new String(Base64.decodeBase64(samlResponse));
    }

    /**
     * Extract the SAML assertion from SAML response.
     *
     * @param samlResponse SAML response.
     * @return Extracted SAML assertion.
     * @throws ParserConfigurationException If an error occurred while creating document builder.
     * @throws IOException                  If an error occurred while getting the SAML assersion.
     * @throws SAXException                 If an error occurred while parsing.
     */
    private String getSAMLAssersion(String samlResponse) throws ParserConfigurationException, IOException,
            SAXException {

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        InputSource is = new InputSource(new StringReader(samlResponse));
        Document document = builder.parse(is);

        StringWriter sw = new StringWriter();
        try {
            Transformer t = TransformerFactory.newInstance().newTransformer();
            t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            t.transform(new DOMSource(document.getDocumentElement().getElementsByTagName("saml2:Assertion").item(0))
                    , new StreamResult(sw));
        } catch (TransformerException te) {
            Assert.fail("Error while parsing the SAML response.");
        }

        return Base64.encodeBase64String(sw.toString().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Send SAML request to the SAML endpoint.
     *
     * @param samlMsgValue Message value.
     * @return HTTP Response object that we get from calling the SAML endpoint.
     * @throws IOException If an error occurred while sending the SAML request.
     */
    private HttpResponse sendSAMLRequest(String samlMsgValue) throws IOException {

        List<NameValuePair> urlParameters = new ArrayList<>();

        HttpPost post = new HttpPost(SAML_SSO_URL);
        post.setHeader("User-Agent", USER_AGENT);

        urlParameters.add(new BasicNameValuePair(CommonConstants.SAML_REQUEST_PARAM, samlMsgValue));

        post.setEntity(new UrlEncodedFormEntity(urlParameters));

        return client.execute(post);
    }

    /**
     * Send the SAML assertion to the token endpoint.
     *
     * @param samlAssertion SAML assertion.
     * @return HTTP Response object that we get from calling the token endpoint.
     * @throws IOException If an error occurred while sending the SAML assersion to the token endpoint.
     */
    private HttpResponse sendSAMLAssertion(String samlAssertion) throws IOException {

        HttpPost request = new HttpPost(OAuth2Constant.ACCESS_TOKEN_ENDPOINT);
        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("grant_type", OAuth2Constant.OAUTH2_GRANT_TYPE_SAML2_BEARER));
        urlParameters.add(new BasicNameValuePair("assertion", samlAssertion));

        request.setHeader("Authorization", "Basic " + Base64.encodeBase64String((consumerKey + ":" + consumerSecret)
                .getBytes()).trim());
        request.setHeader("Content-Type", "application/x-www-form-urlencoded");
        request.setHeader("User-Agent", OAuth2Constant.USER_AGENT);

        request.setEntity(new UrlEncodedFormEntity(urlParameters));

        return client.execute(request);
    }

    private boolean requestMissingClaims(HttpResponse response) {

        String redirectUrl = Utils.getRedirectUrl(response);
        return redirectUrl.contains("consent.do");
    }
}

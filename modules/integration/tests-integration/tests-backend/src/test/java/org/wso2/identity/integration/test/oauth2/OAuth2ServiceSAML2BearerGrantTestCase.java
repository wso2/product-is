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

import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.identity.application.common.model.xsd.InboundAuthenticationConfig;
import org.wso2.carbon.identity.application.common.model.xsd.InboundAuthenticationRequestConfig;
import org.wso2.carbon.identity.application.common.model.xsd.ServiceProvider;
import org.wso2.carbon.identity.oauth.stub.dto.OAuthConsumerAppDTO;
import org.wso2.carbon.identity.sso.saml.stub.IdentitySAMLSSOConfigServiceIdentityException;
import org.wso2.carbon.identity.sso.saml.stub.types.SAMLSSOServiceProviderDTO;
import org.wso2.identity.integration.common.clients.sso.saml.SAMLSSOConfigServiceClient;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;
import org.wso2.identity.integration.test.util.Utils;
import org.wso2.identity.integration.test.utils.CommonConstants;
import org.wso2.identity.integration.test.utils.OAuth2Constant;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.nio.charset.Charset;
import java.rmi.RemoteException;
import java.util.ArrayList;
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

    private static final String COMMON_AUTH_URL = "https://localhost:9853/commonauth";
    private static final String USER_AGENT = "Apache-HttpClient/4.2.5 (java 1.5)";
    private static final String ACS_URL = "http://localhost:8490/%s/home.jsp";
    private static final String TENANT_DOMAIN_PARAM = "tenantDomain";
    private static final String SAML_SSO_URL = "https://localhost:9853/samlsso";

    private Tomcat tomcat;
    private HttpClient client;

    private SAMLSSOConfigServiceClient ssoConfigServiceClient;

    private SAMLSSOServiceProviderDTO samlApp;
    private OAuthConsumerAppDTO oauthApp;

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.init(TestUserMode.SUPER_TENANT_USER);
        deployTravelocity();

        ssoConfigServiceClient = new SAMLSSOConfigServiceClient(backendURL, sessionCookie);

        oauthApp = createDefaultOAuthApplication();
        samlApp = createDefaultSAMLApplication();

        consumerKey = oauthApp.getOauthConsumerKey();
        consumerSecret = oauthApp.getOauthConsumerSecret();

        client = HttpClientBuilder.create().build();
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {

        deleteApplication();
        removeOAuthApplicationData();

        tomcat.stop();
        tomcat.destroy();
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

    @Test
    public void testSAML2BearerInvalidAudience() throws RemoteException, IdentitySAMLSSOConfigServiceIdentityException {

        try {

            // Set some invalid audience.
            SAMLSSOServiceProviderDTO serviceProvider = ssoConfigServiceClient.getServiceProviders()
                    .getServiceProviders()[0];
            serviceProvider.setRequestedAudiences(new String [] {});
            ssoConfigServiceClient.removeServiceProvider("travelocity.com");
            ssoConfigServiceClient.addServiceProvider(serviceProvider);

            // Get a SAML response.
            String samlResponse = getSAMLResponse();

            // Extract the assertion from SAML response.
            String samlAssersion = getSAMLAssersion(samlResponse);

            // Send the extracted SAML assertion to token endpoint in SAML2 bearer grant.
            HttpResponse httpResponse = sendSAMLAssertion(samlAssersion);

            // We should get an http 400 error code.
            Assert.assertEquals(httpResponse.getStatusLine().getStatusCode(), 400);

            // We should get a non empty error message.
            Assert.assertTrue(StringUtils.isNotBlank(IOUtils.toString(httpResponse.getEntity().getContent())));
        } catch (Exception e) {
            Assert.fail("SAML Bearer Grant test failed with an exception.", e);
        } finally {

            // Restore the default service provider.
            ssoConfigServiceClient.removeServiceProvider("travelocity.com");
            ssoConfigServiceClient.addServiceProvider(createDefaultSSOServiceProviderDTO());

            // We have to initiate the http client again or other tests will fail.
            client = HttpClientBuilder.create().build();
        }
    }

    /**
     * Create and attache the default OAUTH application to a service provider for testing.
     * @return OAuth app DTO.
     * @throws Exception
     */
    private OAuthConsumerAppDTO createDefaultOAuthApplication() throws Exception {

        OAuthConsumerAppDTO appDTO = new OAuthConsumerAppDTO();
        appDTO.setApplicationName(OAuth2Constant.OAUTH_APPLICATION_NAME);
        appDTO.setCallbackUrl(OAuth2Constant.AUTHORIZED_URL);
        appDTO.setOAuthVersion(OAuth2Constant.OAUTH_VERSION_2);
        appDTO.setGrantTypes("urn:ietf:params:oauth:grant-type:saml2-bearer");
        return createApplication(appDTO);
    }

    /**
     * Create and attach the SAML application to a service provider for testing.
     * @return SAML app DTO.
     * @throws Exception
     */
    private SAMLSSOServiceProviderDTO createDefaultSAMLApplication() throws Exception {

        ServiceProvider serviceProvider = appMgtclient.getApplication(SERVICE_PROVIDER_NAME);

        InboundAuthenticationRequestConfig inboundAuthenticationRequestConfig =
                new InboundAuthenticationRequestConfig();
        inboundAuthenticationRequestConfig.setInboundAuthType("samlsso");
        inboundAuthenticationRequestConfig.setInboundAuthKey("travelocity.com");

        InboundAuthenticationConfig inboundAuthenticationConfig = new InboundAuthenticationConfig();
        inboundAuthenticationConfig.setInboundAuthenticationRequestConfigs(new InboundAuthenticationRequestConfig []
                { inboundAuthenticationRequestConfig });

        serviceProvider.setInboundAuthenticationConfig(inboundAuthenticationConfig);

        SAMLSSOServiceProviderDTO samlssoServiceProviderDTO = createDefaultSSOServiceProviderDTO();
        boolean isCreated = ssoConfigServiceClient.addServiceProvider(samlssoServiceProviderDTO);
        if (!isCreated) {
            throw new Exception("App creation failed.");
        }

        return samlssoServiceProviderDTO;
    }

    /**
     * Create the SAML SSO DTO.
     * @return SAML SSO DTO.
     */
    private SAMLSSOServiceProviderDTO createDefaultSSOServiceProviderDTO() {

        SAMLSSOServiceProviderDTO samlssoServiceProviderDTO = new SAMLSSOServiceProviderDTO();
        samlssoServiceProviderDTO.setIssuer("travelocity.com");
        samlssoServiceProviderDTO.setAssertionConsumerUrls(
                new String[] { String.format("http://localhost:8490/%s/home.jsp", "travelocity.com")});
        samlssoServiceProviderDTO.setDefaultAssertionConsumerUrl(
                String.format("http://localhost:8490/%s/home.jsp", "travelocity.com"));
        samlssoServiceProviderDTO.setAttributeConsumingServiceIndex("1239245949");
        samlssoServiceProviderDTO.setNameIDFormat("urn:oasis:names:tc:SAML:1.1:nameid-format:emailAddress");
        samlssoServiceProviderDTO.setDoSignAssertions(true);
        samlssoServiceProviderDTO.setDoSignResponse(true);
        samlssoServiceProviderDTO.setDoSingleLogout(true);
        samlssoServiceProviderDTO.addRequestedAudiences(OAuth2Constant.ACCESS_TOKEN_ENDPOINT);
        samlssoServiceProviderDTO.addRequestedRecipients(OAuth2Constant.ACCESS_TOKEN_ENDPOINT);
        samlssoServiceProviderDTO.setLoginPageURL("/carbon/admin/login.jsp");
        samlssoServiceProviderDTO.setEnableAttributeProfile(true);
        samlssoServiceProviderDTO.setEnableAttributesByDefault(true);

        return samlssoServiceProviderDTO;
    }

    /**
     * Get the SAML response by calling the default SAML endpoint.
     * @return SAML response.
     * @throws Exception
     */
    private String getSAMLResponse() throws Exception {

        HttpResponse response;
        response = Utils.sendGetRequest(String.format("http://localhost:8490/%s/samlsso?SAML2.HTTPBinding=%s",
                "travelocity.com", "HTTP-POST"), USER_AGENT, client);

        String samlRequest = Utils.extractDataFromResponse(response, CommonConstants.SAML_REQUEST_PARAM, 5);
        response = sendSAMLRequest(SAML_SSO_URL, CommonConstants.SAML_REQUEST_PARAM, samlRequest);
        EntityUtils.consume(response.getEntity());

        response = Utils.sendRedirectRequest(response, USER_AGENT, ACS_URL, "travelocity.com", client);

        String sessionKey = Utils.extractDataFromResponse(response, CommonConstants.SESSION_DATA_KEY, 1);
        response = Utils.sendPOSTMessage(sessionKey, COMMON_AUTH_URL, USER_AGENT, ACS_URL, "travelocity.com",
                userInfo.getUserName(), userInfo.getPassword(), client);
        EntityUtils.consume(response.getEntity());

        response = Utils.sendRedirectRequest(response, USER_AGENT, ACS_URL, "travelocity.com", client);
        String samlResponse = Utils.extractDataFromResponse(response, CommonConstants.SAML_RESPONSE_PARAM, 5);
        return new String(Base64.decodeBase64(samlResponse));
    }

    /**
     * Extract the SAML assertion from SAML response.
     * @param samlResponse SAML response.
     * @return Extracted SAML assertion.
     * @throws ParserConfigurationException
     * @throws IOException
     * @throws SAXException
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

        return Base64.encodeBase64String(sw.toString().getBytes(Charset.forName("UTF-8")));
    }

    /**
     * Send SAML request to the SAML endpoint.
     * @param url URL of the endpoint.
     * @param samlMsgKey Message key.
     * @param samlMsgValue Message value.
     * @return HTTP Response object that we get from calling the SAML endpoint.
     * @throws IOException
     */
    private HttpResponse sendSAMLRequest(String url, String samlMsgKey, String samlMsgValue) throws IOException {

        List<NameValuePair> urlParameters = new ArrayList<>();

        HttpPost post = new HttpPost(url);
        post.setHeader("User-Agent", USER_AGENT);

        urlParameters.add(new BasicNameValuePair(samlMsgKey, samlMsgValue));
        urlParameters.add(new BasicNameValuePair(TENANT_DOMAIN_PARAM, "carbon.super"));

        post.setEntity(new UrlEncodedFormEntity(urlParameters));

        return client.execute(post);
    }

    /**
     * Send the SAML assertion to the token endpoint.
     * @param samlAssertion SAML assertion.
     * @return HTTP Response object that we get from calling the token endpoint.
     * @throws IOException
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

    /**
     * Deploy the travelocity.com war in the Tomcat.
     * @throws Exception If error occurred.
     */
    private void deployTravelocity() throws Exception {

        tomcat = getTomcat();
        URL resourceUrl = getClass().getResource(ISIntegrationTest.URL_SEPARATOR + "samples" + ISIntegrationTest.URL_SEPARATOR +
                "travelocity.com.war");
        startTomcat(tomcat, OAuth2Constant.TRAVELOCITY_APP_CONTEXT_ROOT, resourceUrl.getPath());
    }
}

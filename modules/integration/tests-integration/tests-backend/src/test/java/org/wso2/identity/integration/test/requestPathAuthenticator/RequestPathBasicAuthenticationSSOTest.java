/*
*  Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.identity.integration.test.requestPathAuthenticator;

import org.apache.catalina.core.StandardHost;
import org.apache.catalina.startup.Tomcat;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.common.model.xsd.InboundAuthenticationConfig;
import org.wso2.carbon.identity.application.common.model.xsd.InboundAuthenticationRequestConfig;
import org.wso2.carbon.identity.application.common.model.xsd.Property;
import org.wso2.carbon.identity.application.common.model.xsd.RequestPathAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.xsd.ServiceProvider;
import org.wso2.carbon.identity.sso.saml.stub.types.SAMLSSOServiceProviderDTO;
import org.wso2.carbon.integration.common.admin.client.AuthenticatorClient;
import org.wso2.identity.integration.common.clients.application.mgt.ApplicationManagementServiceClient;
import org.wso2.identity.integration.common.clients.sso.saml.SAMLSSOConfigServiceClient;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;
import org.wso2.identity.integration.test.util.Utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Tests Basic authenticator and request path basic authenticator single sign on.
 */
public class RequestPathBasicAuthenticationSSOTest extends ISIntegrationTest {

    private static final String SERVICE_PROVIDER_NAME_TRAVELOCITY = "travelocity-requestpath";
    private static final String SERVICE_PROVIDER_NAME_AVIS = "avis-basic";
    private static final String SERVICE_PROVIDER_DESC = "Service Provider with Request Path Authentication";
    protected static final String SAMPLE_APP_URL = "http://localhost:8490/%s";
    protected static final String USER_AGENT = "Apache-HttpClient/4.2.5 (java 1.5)";
    private static final String SAML_SUCCESS_TAG =
            "<saml2p:StatusCode Value=\"urn:oasis:names:tc:SAML:2" + ".0:status:Success\"/>";
    private static final String ACS_URL = "http://localhost:8490/%s/home.jsp";
    public static final String ISSUER_TRAVELOCITY_COM = "travelocity.com";
    public static final String ISSUER_AVIS_COM = "avis.com";

    protected String adminUsername;
    protected String adminPassword;
    private Tomcat tomcat;
    private AuthenticatorClient logManger;
    private ApplicationManagementServiceClient appMgtclient;
    private SAMLSSOConfigServiceClient ssoConfigServiceClient;
    private ServiceProvider serviceProviderTravelocity;
    private ServiceProvider serviceProviderAvis;
    protected CloseableHttpClient client;
    protected String isURL;

    @BeforeClass(alwaysRun = true) public void testInit() throws Exception {
        super.init();

        logManger = new AuthenticatorClient(backendURL);
        adminUsername = userInfo.getUserName();
        adminPassword = userInfo.getPassword();
        logManger.login(isServer.getSuperTenant().getTenantAdmin().getUserName(),
                isServer.getSuperTenant().getTenantAdmin().getPassword(),
                isServer.getInstance().getHosts().get("default"));

        appMgtclient = new ApplicationManagementServiceClient(sessionCookie, backendURL, null);
        ssoConfigServiceClient = new SAMLSSOConfigServiceClient(backendURL, sessionCookie);

        client = HttpClientBuilder.create().build();
        isURL = backendURL.substring(0, backendURL.indexOf("services/"));

        try {
            tomcat = getTomcat();
            URL travelocityResourceUrl = getClass()
                    .getResource(ISIntegrationTest.URL_SEPARATOR + "samples" + ISIntegrationTest.URL_SEPARATOR +
                            "travelocity.com.war");
            URL avisResourceUrl = getClass()
                    .getResource(ISIntegrationTest.URL_SEPARATOR + "samples" + ISIntegrationTest.URL_SEPARATOR +
                            "avis.com.war");
            tomcat.addWebapp(tomcat.getHost(), "/travelocity.com", travelocityResourceUrl.getPath());
            tomcat.addWebapp(tomcat.getHost(), "/avis.com", avisResourceUrl.getPath());
            tomcat.start();

        } catch (Exception e) {
            Assert.fail("travelocity.com application deployment failed.", e);
        }

        ssoConfigServiceClient.addServiceProvider(createSAMLServiceProviderDTO(ISSUER_TRAVELOCITY_COM));
        ssoConfigServiceClient.addServiceProvider(createSAMLServiceProviderDTO(ISSUER_AVIS_COM));
        serviceProviderTravelocity = new ServiceProvider();
        serviceProviderTravelocity.setApplicationName(SERVICE_PROVIDER_NAME_TRAVELOCITY);

        serviceProviderTravelocity.setDescription(SERVICE_PROVIDER_DESC);
        appMgtclient.createApplication(serviceProviderTravelocity);
        serviceProviderTravelocity = appMgtclient.getApplication(SERVICE_PROVIDER_NAME_TRAVELOCITY);
        InboundAuthenticationConfig inboundAuthenticationConfig = new InboundAuthenticationConfig();
        InboundAuthenticationRequestConfig requestConfig = new InboundAuthenticationRequestConfig();
        requestConfig.setInboundAuthKey(ISSUER_TRAVELOCITY_COM);
        requestConfig.setInboundAuthType("samlsso");

        Property attributeConsumerServiceIndexProp = new Property();
        attributeConsumerServiceIndexProp.setName("attrConsumServiceIndex");
        attributeConsumerServiceIndexProp.setValue("1239245949");
        requestConfig.setProperties(new Property[] { attributeConsumerServiceIndexProp });

        inboundAuthenticationConfig
                .setInboundAuthenticationRequestConfigs(new InboundAuthenticationRequestConfig[] { requestConfig });

        serviceProviderTravelocity.setInboundAuthenticationConfig(inboundAuthenticationConfig);
        RequestPathAuthenticatorConfig requestPathAuthenticatorConfig = new RequestPathAuthenticatorConfig();
        requestPathAuthenticatorConfig.setName("BasicAuthRequestPathAuthenticator");

        serviceProviderTravelocity.setRequestPathAuthenticatorConfigs(
                new RequestPathAuthenticatorConfig[] { requestPathAuthenticatorConfig });
        appMgtclient.updateApplicationData(serviceProviderTravelocity);
        serviceProviderTravelocity = appMgtclient.getApplication(SERVICE_PROVIDER_NAME_TRAVELOCITY);

        serviceProviderAvis = new ServiceProvider();
        serviceProviderAvis.setApplicationName(SERVICE_PROVIDER_NAME_AVIS);

        serviceProviderAvis.setDescription(SERVICE_PROVIDER_DESC);
        appMgtclient.createApplication(serviceProviderAvis);
        serviceProviderAvis = appMgtclient.getApplication(SERVICE_PROVIDER_NAME_AVIS);
        inboundAuthenticationConfig = new InboundAuthenticationConfig();
        requestConfig = new InboundAuthenticationRequestConfig();
        requestConfig.setInboundAuthKey(ISSUER_AVIS_COM);
        requestConfig.setInboundAuthType("samlsso");

        attributeConsumerServiceIndexProp = new Property();
        attributeConsumerServiceIndexProp.setName("attrConsumServiceIndex");
        attributeConsumerServiceIndexProp.setValue("1239245949");
        requestConfig.setProperties(new Property[] { attributeConsumerServiceIndexProp });

        inboundAuthenticationConfig
                .setInboundAuthenticationRequestConfigs(new InboundAuthenticationRequestConfig[] { requestConfig });

        serviceProviderAvis.setInboundAuthenticationConfig(inboundAuthenticationConfig);
        appMgtclient.updateApplicationData(serviceProviderAvis);
        serviceProviderAvis = appMgtclient.getApplication(SERVICE_PROVIDER_NAME_AVIS);
    }

    @AfterClass(alwaysRun = true) public void atEnd() throws Exception {
        appMgtclient.deleteApplication(serviceProviderTravelocity.getApplicationName());
        appMgtclient.deleteApplication(serviceProviderAvis.getApplicationName());
        if (tomcat != null) {
            tomcat.stop();
            tomcat.destroy();
            Thread.sleep(10000);
        }
    }

    @Test(alwaysRun = true, description = "Request path authenticator login success")
    public void testLoginSuccessRequestPath() throws Exception {
        HttpPost request = new HttpPost(String.format(SAMPLE_APP_URL, ISSUER_TRAVELOCITY_COM) + "/samlsso" +
                "?SAML2.HTTPBinding=HTTP-POST");
        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("username", adminUsername));
        urlParameters.add(new BasicNameValuePair("password", adminPassword));
        request.setEntity(new UrlEncodedFormEntity(urlParameters));
        HttpResponse response = client.execute(request);
        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        String line;
        String samlRequest = "";
        String secToken = "";

        while ((line = rd.readLine()) != null) {
            if (line.contains("name='SAMLRequest'")) {
                String[] tokens = line.split("'");
                samlRequest = tokens[5];
            }
            if (line.contains("name='sectoken'")) {
                String[] tokens = line.split("'");
                secToken = tokens[5];
            }
        }
        EntityUtils.consume(response.getEntity());
        request = new HttpPost(isURL + "samlsso");
        urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("sectoken", secToken));
        urlParameters.add(new BasicNameValuePair("SAMLRequest", samlRequest));
        request.setEntity(new UrlEncodedFormEntity(urlParameters));
        response = client.execute(request);

        if (Utils.requestMissingClaims(response)) {
            String pastrCookie = Utils.getPastreCookie(response);
            Assert.assertNotNull(pastrCookie, "pastr cookie not found in response.");
            EntityUtils.consume(response.getEntity());

            response = Utils.sendPOSTConsentMessage(response, isURL + "commonauth", USER_AGENT, String.format(ACS_URL
                    , ISSUER_TRAVELOCITY_COM), client, pastrCookie);
            EntityUtils.consume(response.getEntity());
        }

        response = Utils.sendRedirectRequest(response, USER_AGENT, String.format(ACS_URL, ISSUER_TRAVELOCITY_COM), "", client);
        int responseCode = response.getStatusLine().getStatusCode();
        Assert.assertEquals(responseCode, 200, "Successful login response returned code " + responseCode);
        String samlResponse = "";
        rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        while ((line = rd.readLine()) != null) {
            if (line.contains("name='SAMLResponse'")) {
                String[] tokens = line.split("'");
                samlResponse = tokens[5];
            }
        }
        Base64 base64Decoder = new Base64();
        samlResponse = new String(base64Decoder.decode(samlResponse));
        Assert.assertTrue(samlResponse.contains(SAML_SUCCESS_TAG), "SAML response did not contained success state");
        EntityUtils.consume(response.getEntity());
    }

    @Test(alwaysRun = true, description = "Basic auth authenticator login success when user is authenticated with " +
            "request path", dependsOnMethods = { "testLoginSuccessRequestPath" })
    public void testLoginSuccessBasicAuth() throws Exception {
        HttpPost request = new HttpPost(String.format(SAMPLE_APP_URL, ISSUER_AVIS_COM) + "/samlsso" +
                "?SAML2.HTTPBinding=HTTP-POST");
        HttpResponse response = client.execute(request);
        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        String line;
        String samlRequest = "";

        while ((line = rd.readLine()) != null) {
            if (line.contains("name='SAMLRequest'")) {
                String[] tokens = line.split("'");
                samlRequest = tokens[5];
            }
        }
        EntityUtils.consume(response.getEntity());
        request = new HttpPost(isURL + "samlsso");
        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("SAMLRequest", samlRequest));
        request.setEntity(new UrlEncodedFormEntity(urlParameters));
        response = client.execute(request);

        if (Utils.requestMissingClaims(response)) {
            String pastrCookie = Utils.getPastreCookie(response);
            Assert.assertNotNull(pastrCookie, "pastr cookie not found in response.");
            EntityUtils.consume(response.getEntity());

            response = Utils.sendPOSTConsentMessage(response, isURL + "commonauth", USER_AGENT, String.format(ACS_URL
                    , ISSUER_AVIS_COM), client, pastrCookie);
            EntityUtils.consume(response.getEntity());
        }

        response = Utils.sendRedirectRequest(response, USER_AGENT, String.format(ACS_URL, ISSUER_AVIS_COM), "", client);
        int responseCode = response.getStatusLine().getStatusCode();
        Assert.assertEquals(responseCode, 200, "Successful login response returned code " + responseCode);
        String samlResponse = "";
        rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        while ((line = rd.readLine()) != null) {
            if (line.contains("name='SAMLResponse'")) {
                String[] tokens = line.split("'");
                samlResponse = tokens[5];
            }
        }
        Base64 base64Decoder = new Base64();
        samlResponse = new String(base64Decoder.decode(samlResponse));
        Assert.assertTrue(samlResponse.contains(SAML_SUCCESS_TAG), "SAML response did not contained success state");
        EntityUtils.consume(response.getEntity());
    }

    private Tomcat getTomcat() {
        Tomcat tomcat = new Tomcat();
        tomcat.getService().setContainer(tomcat.getEngine());
        tomcat.setPort(8490);
        tomcat.setBaseDir("");

        StandardHost stdHost = (StandardHost) tomcat.getHost();

        stdHost.setAppBase("");
        stdHost.setAutoDeploy(true);
        stdHost.setDeployOnStartup(true);
        stdHost.setUnpackWARs(true);
        tomcat.setHost(stdHost);

        return tomcat;
    }

    private SAMLSSOServiceProviderDTO createSAMLServiceProviderDTO(String issuer) {
        SAMLSSOServiceProviderDTO samlssoServiceProviderDTO = new SAMLSSOServiceProviderDTO();
        samlssoServiceProviderDTO.setIssuer(issuer);
        samlssoServiceProviderDTO.setAssertionConsumerUrls(new String[]
                { String.format(SAMPLE_APP_URL, issuer) + "/home.jsp" });
        samlssoServiceProviderDTO.setDefaultAssertionConsumerUrl(String.format(SAMPLE_APP_URL, issuer) +
                "/home.jsp");
        samlssoServiceProviderDTO.setAttributeConsumingServiceIndex("1239245949");
        samlssoServiceProviderDTO.setNameIDFormat("urn:oasis:names:tc:SAML:1.1:nameid-format:emailAddress");
        samlssoServiceProviderDTO.setDoSignAssertions(true);
        samlssoServiceProviderDTO.setDoSignResponse(true);
        samlssoServiceProviderDTO.setDoSingleLogout(true);
        samlssoServiceProviderDTO.setLoginPageURL("/carbon/admin/login.jsp");
        return samlssoServiceProviderDTO;
    }

}

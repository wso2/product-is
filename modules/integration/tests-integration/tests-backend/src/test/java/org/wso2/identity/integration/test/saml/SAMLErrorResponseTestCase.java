/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.identity.integration.test.saml;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.core.StandardHost;
import org.apache.catalina.startup.Tomcat;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.identity.application.common.model.xsd.InboundAuthenticationConfig;
import org.wso2.carbon.identity.application.common.model.xsd.InboundAuthenticationRequestConfig;
import org.wso2.carbon.identity.application.common.model.xsd.ServiceProvider;
import org.wso2.carbon.identity.sso.saml.stub.types.SAMLSSOServiceProviderDTO;
import org.wso2.identity.integration.common.clients.application.mgt.ApplicationManagementServiceClient;
import org.wso2.identity.integration.common.clients.sso.saml.SAMLSSOConfigServiceClient;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertTrue;
import static org.wso2.identity.integration.test.utils.CommonConstants.DEFAULT_TOMCAT_PORT;

public class SAMLErrorResponseTestCase extends ISIntegrationTest {

    private static final Log log = LogFactory.getLog(SAMLSSOTestCase.class);

    // SAML Application attributes
    private static final String USER_AGENT = "Apache-HttpClient/4.2.5 (java 1.5)";
    private static final String APPLICATION_NAME = "SAML-SSO-TestApplication";
    private static final String ARTIFACT_ID = "travelocity.com";
    private static final String INBOUND_AUTH_TYPE = "samlsso";
    private static final String TENANT_DOMAIN_PARAM = "tenantDomain";

    private static final String SAML_SSO_URL = "https://localhost:9853/samlsso";
    private static final String ACS_URL = "http://localhost:8490/%s/home.jsp";
    private static final String INVALID_ACS_URL = "http://localhost:8490/%s/index.jsp";
    private static final String SAML_SSO_LOGIN_URL = "http://localhost:8490/%s/samlsso?SAML2.HTTPBinding=%s";

    private static final String NAMEID_FORMAT = "urn:oasis:names:tc:SAML:1.1:nameid-format:emailAddress";
    private static final String LOGIN_URL = "/carbon/admin/login.jsp";
    private static final String RELAY_STATE = "token";
    private static final String HTTP_POST_BINDING = "HTTP-POST";

    private ApplicationManagementServiceClient applicationManagementServiceClient;
    private SAMLSSOConfigServiceClient ssoConfigServiceClient;
    private HttpClient httpClient;
    private Tomcat tomcatServer;

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.init(TestUserMode.SUPER_TENANT_ADMIN);

        ConfigurationContext configContext = ConfigurationContextFactory
                .createConfigurationContextFromFileSystem(null, null);
        applicationManagementServiceClient = new ApplicationManagementServiceClient(sessionCookie, backendURL,
                configContext);
        ssoConfigServiceClient = new SAMLSSOConfigServiceClient(backendURL, sessionCookie);
        httpClient = HttpClientBuilder.create().build();
        createApplication();

        // Starting tomcat
        log.info("Starting Tomcat");
        tomcatServer = getTomcat();

        URL resourceUrl = getClass()
                .getResource(File.separator + "samples" + File.separator + ARTIFACT_ID + ".war");
        startTomcat(tomcatServer, "/" + ARTIFACT_ID, resourceUrl.getPath());
    }

    @AfterClass(alwaysRun = true)
    public void testClear() throws Exception {

        deleteApplication();

        ssoConfigServiceClient = null;
        applicationManagementServiceClient = null;
        httpClient = null;
        //Stopping tomcat
        tomcatServer.stop();
        tomcatServer.destroy();
        Thread.sleep(10000);
    }

    /**
     * Tests whether the RelayState and ACS Url parameters are sent to the notification.do page in SAML error
     * scenarios.
     *
     * @throws Exception
     */
    @Test(alwaysRun = true, description = "Testing Relay state and ACS parameters in SAML error response", groups =
            "wso2.is")
    public void testRelayStateAndACSWithSAMLErrorResponse() throws Exception {

        // Create service provider config with mis-matching ACS to generate SAML error response
        boolean isAddSuccess = ssoConfigServiceClient
                .addServiceProvider(createSsoServiceProviderDTO());
        assertTrue(isAddSuccess, "Adding a service provider has failed for " + ARTIFACT_ID);

        HttpResponse response;
        response = sendGetRequest(String.format(SAML_SSO_LOGIN_URL, ARTIFACT_ID, HTTP_POST_BINDING));
        String samlRequest = extractDataFromResponse(response, "SAMLRequest", 5);
        Map<String, String> paramters = new HashMap<String, String>();
        paramters.put("SAMLRequest", samlRequest);
        paramters.put("RelayState", RELAY_STATE);
        response = sendSAMLMessage(SAML_SSO_URL, paramters);
        EntityUtils.consume(response.getEntity());
        response = sendRedirectRequest(response);

        String sessionKey = extractDataFromResponse(response, "name=\"sessionDataKey\"", 1);
        response = sendPOSTMessage(sessionKey);
        EntityUtils.consume(response.getEntity());

        String location = response.getFirstHeader("Location").getValue();
        assertTrue(location.contains("RelayState=" + RELAY_STATE), "Redirection header to notification.do" +
                " page should contain RelayState query param sent with the request");
        assertTrue(location.contains("ACSUrl=" + URLEncoder.encode(String.format(ACS_URL,
                ARTIFACT_ID), "UTF-8")), "Redirection header to notification.do" +
                " page should contain ACS query param");
    }


    private void createApplication() throws Exception {

        ServiceProvider serviceProvider = new ServiceProvider();
        serviceProvider.setApplicationName(APPLICATION_NAME);
        serviceProvider.setDescription("This is a test Service Provider");
        applicationManagementServiceClient.createApplication(serviceProvider);
        serviceProvider = applicationManagementServiceClient.getApplication(APPLICATION_NAME);

        InboundAuthenticationRequestConfig requestConfig = new InboundAuthenticationRequestConfig();
        requestConfig.setInboundAuthType(INBOUND_AUTH_TYPE);
        requestConfig.setInboundAuthKey(ARTIFACT_ID);

        InboundAuthenticationConfig inboundAuthenticationConfig = new InboundAuthenticationConfig();
        inboundAuthenticationConfig.setInboundAuthenticationRequestConfigs(
                new InboundAuthenticationRequestConfig[]{requestConfig});

        serviceProvider.setInboundAuthenticationConfig(inboundAuthenticationConfig);
        applicationManagementServiceClient.updateApplicationData(serviceProvider);
    }

    private Tomcat getTomcat() {

        Tomcat tomcat = new Tomcat();
        tomcat.getService().setContainer(tomcat.getEngine());
        tomcat.setPort(DEFAULT_TOMCAT_PORT);
        tomcat.setBaseDir("");

        StandardHost stdHost = (StandardHost) tomcat.getHost();

        stdHost.setAppBase("");
        stdHost.setAutoDeploy(true);
        stdHost.setDeployOnStartup(true);
        stdHost.setUnpackWARs(true);
        tomcat.setHost(stdHost);

        setSystemProperties();
        return tomcat;
    }

    private void setSystemProperties() {

        URL resourceUrl = getClass().getResource(File.separator + "keystores" + File.separator
                + "products" + File.separator + "wso2carbon.jks");
        System.setProperty("javax.net.ssl.trustStore", resourceUrl.getPath());
        System.setProperty("javax.net.ssl.trustStorePassword",
                "wso2carbon");
        System.setProperty("javax.net.ssl.trustStoreType", "JKS");
    }

    private void startTomcat(Tomcat tomcat, String webAppUrl, String webAppPath) throws LifecycleException {

        tomcat.addWebapp(tomcat.getHost(), webAppUrl, webAppPath);
        tomcat.start();
    }

    private HttpResponse sendSAMLMessage(String url, Map<String, String> parameters) throws Exception {

        List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
        HttpPost post = new HttpPost(url);
        post.setHeader("User-Agent", USER_AGENT);
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            urlParameters.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
        }
        urlParameters.add(new BasicNameValuePair(TENANT_DOMAIN_PARAM, isServer.getSuperTenant().getDomain()));
        post.setEntity(new UrlEncodedFormEntity(urlParameters));
        return httpClient.execute(post);
    }

    private String extractDataFromResponse(HttpResponse response, String key, int token)
            throws IOException {

        BufferedReader rd = new BufferedReader(
                new InputStreamReader(response.getEntity().getContent()));
        String line;
        String value = "";

        while ((line = rd.readLine()) != null) {
            if (line.contains(key)) {
                String[] tokens = line.split("'");
                value = tokens[token];
            }
        }
        rd.close();
        return value;
    }

    private HttpResponse sendGetRequest(String url) throws Exception {

        HttpGet request = new HttpGet(url);
        request.addHeader("User-Agent", USER_AGENT);
        return httpClient.execute(request);
    }

    private HttpResponse sendRedirectRequest(HttpResponse response) throws IOException {

        Header[] headers = response.getAllHeaders();
        String url = "";
        for (Header header : headers) {
            if ("Location".equals(header.getName())) {
                url = header.getValue();
            }
        }

        HttpGet request = new HttpGet(url);
        request.addHeader("User-Agent", USER_AGENT);
        request.addHeader("Referer", String.format(ACS_URL, ARTIFACT_ID));
        return httpClient.execute(request);
    }

    private HttpResponse sendPOSTMessage(String sessionKey) throws Exception {

        HttpPost post = new HttpPost(SAML_SSO_URL);
        post.setHeader("User-Agent", USER_AGENT);
        post.addHeader("Referer", String.format(ACS_URL, ARTIFACT_ID));
        List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
        urlParameters.add(new BasicNameValuePair("username", isServer.getSuperTenant().getTenantAdmin().getUserName()));
        urlParameters.add(new BasicNameValuePair("password", isServer.getSuperTenant().getTenantAdmin().getUserName()));
        urlParameters.add(new BasicNameValuePair("sessionDataKey", sessionKey));
        urlParameters.add(new BasicNameValuePair("tocommonauth", "true"));

        post.setEntity(new UrlEncodedFormEntity(urlParameters));
        return httpClient.execute(post);
    }

    private SAMLSSOServiceProviderDTO createSsoServiceProviderDTO() {

        SAMLSSOServiceProviderDTO samlssoServiceProviderDTO = new SAMLSSOServiceProviderDTO();
        samlssoServiceProviderDTO.setIssuer(ARTIFACT_ID);
        samlssoServiceProviderDTO.setAssertionConsumerUrls(new String[]{String.format(INVALID_ACS_URL,
                ARTIFACT_ID)});
        samlssoServiceProviderDTO.setDefaultAssertionConsumerUrl(String.format(INVALID_ACS_URL, ARTIFACT_ID));
        samlssoServiceProviderDTO.setNameIDFormat(NAMEID_FORMAT);
        samlssoServiceProviderDTO.setDoSignAssertions(false);
        samlssoServiceProviderDTO.setDoSignResponse(false);
        samlssoServiceProviderDTO.setDoSingleLogout(true);
        samlssoServiceProviderDTO.setLoginPageURL(LOGIN_URL);
        return samlssoServiceProviderDTO;
    }

    private void deleteApplication() throws Exception {

        applicationManagementServiceClient.deleteApplication(APPLICATION_NAME);
    }
}

/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.apache.http.*;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultRedirectHandler;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.testng.Assert;
import org.testng.annotations.*;
import org.wso2.carbon.identity.application.common.model.xsd.*;
import org.wso2.carbon.um.ws.api.stub.ClaimValue;
import org.wso2.identity.integration.common.clients.application.mgt.ApplicationManagementServiceClient;
import org.wso2.identity.integration.common.clients.usermgt.remote.RemoteUserStoreManagerServiceClient;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class SAMLInvalidIssuerTestCase extends ISIntegrationTest {
    private static final Log log = LogFactory.getLog(SAMLInvalidIssuerTestCase.class);

    // SAML Application attributes
    private static final String USER_AGENT = "Apache-HttpClient/4.2.5 (java 1.5)";
    private static final String ISSUER_NAME = "dummy.com";
    private static final String APPLICATION_NAME = "SAML-SSO-TestApplication";
    private static final String INBOUND_AUTH_TYPE = "samlsso";
    private static final String ATTRIBUTE_CS_INDEX_VALUE = "1239245949";
    private static final String ATTRIBUTE_CS_INDEX_NAME = "attrConsumServiceIndex";

    // User Attributes
    private static final String USERNAME = "testUser";
    private static final String PASSWORD = "testUser";
    private static final String EMAIL = "testUser@wso2.com";
    private static final String NICKNAME = "testUserNick";

    private static final String ACS_URL = "http://localhost:8090/travelocity.com/home.jsp";
    private static final String COMMON_AUTH_URL = "https://localhost:9443/commonauth";
    private static final String SAML_SSO_LOGIN_URL =
            "http://localhost:8090/travelocity.com/samlsso?SAML2.HTTPBinding=HTTP-Redirect";
    private static final String SAML_ERROR_NOTIFICATION_PATH = "/authenticationendpoint/samlsso_notification.do";

    //Claim Uris
    private static final String firstNameClaimURI = "http://wso2.org/claims/givenname";
    private static final String lastNameClaimURI = "http://wso2.org/claims/lastname";
    private static final String emailClaimURI = "http://wso2.org/claims/emailaddress";

    private static final String profileName = "default";

    private ApplicationManagementServiceClient applicationManagementServiceClient;
    private RemoteUserStoreManagerServiceClient remoteUSMServiceClient;
    private DefaultHttpClient httpClient;
    private Tomcat tomcatServer;

    private boolean isSAMLReturned;

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {
        super.init();

        ConfigurationContext configContext = ConfigurationContextFactory
                .createConfigurationContextFromFileSystem(null
                        , null);
        applicationManagementServiceClient =
                new ApplicationManagementServiceClient(sessionCookie, backendURL, configContext);
        remoteUSMServiceClient = new RemoteUserStoreManagerServiceClient(backendURL, sessionCookie);

        isSAMLReturned = false;

        httpClient = new DefaultHttpClient();
        httpClient.setRedirectHandler(new DefaultRedirectHandler() {
            @Override
            public URI getLocationURI(HttpResponse response,
                                      HttpContext context) throws ProtocolException {

                if (response == null) {
                    throw new IllegalArgumentException("HTTP Response should not be null");
                }
                //get the location header to find out where to redirect to
                Header locationHeader = response.getFirstHeader("Location");
                if (locationHeader == null) {
                    // got a redirect resp, but no location header
                    throw new ProtocolException(
                            "Received redirect resp " + response.getStatusLine()
                            + " but no location header");
                }

                URL url = null;
                try {
                    url = new URL(locationHeader.getValue());
                    if (SAML_ERROR_NOTIFICATION_PATH.equals(url.getPath()) &&
                        url.getQuery().contains("SAMLResponse")) {
                        isSAMLReturned = true;
                    }
                } catch (MalformedURLException e) {
                    throw new ProtocolException("Invalid redirect URI: " + locationHeader.getValue(), e);
                }

                return super.getLocationURI(response, context);
            }
        });

        createUser();
        createApplication();

        //Starting tomcat
        log.info("Starting Tomcat");
        tomcatServer = getTomcat();

        //TODO: Uncomment below once the tomcat dependency issue is resolved
//        URL resourceUrl = getClass()
//                .getResource(File.separator + "samples" + File.separator + "org.wso2.sample.is .sso.agent.war");
        URL resourceUrl = getClass().getResource(File.separator + "samples" + File.separator + "travelocity.com.war");
        startTomcat(tomcatServer, "/travelocity.com", resourceUrl.getPath());

    }

    @AfterClass(alwaysRun = true)
    public void testClear() throws Exception {
        deleteUser();
        deleteApplication();

        applicationManagementServiceClient = null;
        remoteUSMServiceClient = null;
        httpClient = null;
        //Stopping tomcat
        tomcatServer.stop();
        tomcatServer.destroy();
    }

    @Test(alwaysRun = true, description = "Testing SAML SSO login", groups = "wso2.is",
          priority = 1)
    public void testSAMLSSOLogin() {
        try {
            HttpResponse response;

            response = sendGetRequest(SAML_SSO_LOGIN_URL);

            String sessionKey = extractDataFromResponse(response, "name=\"sessionDataKey\"", 1);
            response = sendPOSTMessage(sessionKey);
            EntityUtils.consume(response.getEntity());

            sendRedirectRequest(response);

            Assert.assertTrue(isSAMLReturned,
                              "Sending SAML response to the samlsso_notification page failed");

        } catch (Exception e) {
            Assert.fail("SAML SSO Login test failed.", e);
        }
    }

    private void startTomcat(Tomcat tomcat, String webAppUrl, String webAppPath)
            throws LifecycleException {
        tomcat.addWebapp(tomcat.getHost(), webAppUrl, webAppPath);
        tomcat.start();
    }

    private Tomcat getTomcat() {
        Tomcat tomcat = new Tomcat();
        tomcat.getService().setContainer(tomcat.getEngine());
        tomcat.setPort(8090);
        tomcat.setBaseDir(".");

        StandardHost stdHost = (StandardHost) tomcat.getHost();

        stdHost.setAppBase(".");
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

    private HttpResponse sendPOSTMessage(String sessionKey) throws Exception {
        HttpPost post = new HttpPost(COMMON_AUTH_URL);
        post.setHeader("User-Agent", USER_AGENT);
        post.addHeader("Referer", ACS_URL);
        List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
        urlParameters.add(new BasicNameValuePair("username", USERNAME));
        urlParameters.add(new BasicNameValuePair("password", PASSWORD));
        urlParameters.add(new BasicNameValuePair("sessionDataKey", sessionKey));
        post.setEntity(new UrlEncodedFormEntity(urlParameters));
        return httpClient.execute(post);
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
        request.addHeader("Referer", ACS_URL);

        return httpClient.execute(request);
    }

    private void createApplication() throws Exception {
        ServiceProvider serviceProvider = new ServiceProvider();
        serviceProvider.setApplicationName(APPLICATION_NAME);
        serviceProvider.setDescription("This is a test Service Provider");
        applicationManagementServiceClient.createApplication(serviceProvider);

        serviceProvider = applicationManagementServiceClient.getApplication(APPLICATION_NAME);

        InboundAuthenticationRequestConfig requestConfig = new InboundAuthenticationRequestConfig();
        requestConfig.setInboundAuthType(INBOUND_AUTH_TYPE);
        requestConfig.setInboundAuthKey(ISSUER_NAME);

        Property attributeConsumerServiceIndexProp = new Property();
        attributeConsumerServiceIndexProp.setName(ATTRIBUTE_CS_INDEX_NAME);
        attributeConsumerServiceIndexProp.setValue(ATTRIBUTE_CS_INDEX_VALUE);
        requestConfig.setProperties(new Property[]{attributeConsumerServiceIndexProp});

        InboundAuthenticationConfig inboundAuthenticationConfig = new InboundAuthenticationConfig();
        inboundAuthenticationConfig.setInboundAuthenticationRequestConfigs(
                new InboundAuthenticationRequestConfig[]{requestConfig});

        serviceProvider.setInboundAuthenticationConfig(inboundAuthenticationConfig);
        applicationManagementServiceClient.updateApplicationData(serviceProvider);
    }

    private void deleteApplication() throws Exception {
        applicationManagementServiceClient.deleteApplication(APPLICATION_NAME);
    }

    private void createUser() {
        log.info("Creating User " + USERNAME);
        try {
            // creating the user
            remoteUSMServiceClient.addUser(USERNAME, PASSWORD,
                                           null, getUserClaims(),
                                           profileName, true);
        } catch (Exception e) {
            Assert.fail("Error while creating the user", e);
        }

    }

    private void deleteUser() {
        log.info("Deleting User " + USERNAME);
        try {
            remoteUSMServiceClient.deleteUser(USERNAME);
        } catch (Exception e) {
            Assert.fail("Error while deleting the user", e);
        }
    }

    private ClaimValue[] getUserClaims() {
        ClaimValue[] claimValues = new ClaimValue[3];

        ClaimValue firstName = new ClaimValue();
        firstName.setClaimURI(firstNameClaimURI);
        firstName.setValue(NICKNAME);
        claimValues[0] = firstName;

        ClaimValue lastName = new ClaimValue();
        lastName.setClaimURI(lastNameClaimURI);
        lastName.setValue(USERNAME);
        claimValues[1] = lastName;

        ClaimValue email = new ClaimValue();
        email.setClaimURI(emailClaimURI);
        email.setValue(EMAIL);
        claimValues[2] = email;

        return claimValues;
    }
}

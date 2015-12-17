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
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.identity.application.common.model.xsd.Claim;
import org.wso2.carbon.identity.application.common.model.xsd.ClaimMapping;
import org.wso2.carbon.identity.application.common.model.xsd.InboundAuthenticationConfig;
import org.wso2.carbon.identity.application.common.model.xsd.InboundAuthenticationRequestConfig;
import org.wso2.carbon.identity.application.common.model.xsd.Property;
import org.wso2.carbon.identity.application.common.model.xsd.ServiceProvider;
import org.wso2.carbon.identity.sso.saml.stub.types.SAMLSSOServiceProviderDTO;
import org.wso2.carbon.um.ws.api.stub.ClaimValue;
import org.wso2.identity.integration.common.clients.application.mgt.ApplicationManagementServiceClient;
import org.wso2.identity.integration.common.clients.sso.saml.SAMLSSOConfigServiceClient;
import org.wso2.identity.integration.common.clients.usermgt.remote.RemoteUserStoreManagerServiceClient;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SAMLSSOTestCase extends ISIntegrationTest {

    private static final Log log = LogFactory.getLog(SAMLSSOTestCase.class);

    // SAML Application attributes
    private static final String USER_AGENT = "Apache-HttpClient/4.2.5 (java 1.5)";
    private static final String APPLICATION_NAME = "SAML-SSO-TestApplication";
    private static final String INBOUND_AUTH_TYPE = "samlsso";
    private static final String ATTRIBUTE_CS_INDEX_VALUE = "1239245949";
    private static final String ATTRIBUTE_CS_INDEX_NAME = "attrConsumServiceIndex";
    public static final String TENANT_DOMAIN_PARAM = "tenantDomain";

    private static final String SAML_SSO_URL = "https://localhost:9853/samlsso";
    private static final String ACS_URL = "http://localhost:8490/%s/home.jsp";
    private static final String COMMON_AUTH_URL = "https://localhost:9853/commonauth";
    private static final String SAML_SSO_LOGIN_URL =
            "http://localhost:8490/%s/samlsso?SAML2.HTTPBinding=%s";
    private static final String SAML_SSO_LOGOUT_URL =
            "http://localhost:8490/%s/logout?SAML2.HTTPBinding=%s";

    private static final String NAMEID_FORMAT =
            "urn:oasis:names:tc:SAML:1.1:nameid-format:emailAddress";
    private static final String LOGIN_URL = "/carbon/admin/login.jsp";

    //Claim Uris
    private static final String firstNameClaimURI = "http://wso2.org/claims/givenname";
    private static final String lastNameClaimURI = "http://wso2.org/claims/lastname";
    private static final String emailClaimURI = "http://wso2.org/claims/emailaddress";

    private static final String profileName = "default";

    private ApplicationManagementServiceClient applicationManagementServiceClient;
    private SAMLSSOConfigServiceClient ssoConfigServiceClient;
    private RemoteUserStoreManagerServiceClient remoteUSMServiceClient;
    private HttpClient httpClient;
    private SAMLConfig config;
    private Tomcat tomcatServer;

    private String resultPage;

    private enum HttpBinding{
        HTTP_REDIRECT("HTTP-Redirect"),
        HTTP_POST("HTTP-POST");

        String binding;

        HttpBinding(String binding) {
            this.binding = binding;
        }
    }

    private enum ClaimType{
        LOCAL, CUSTOM, NONE
    }

    private enum User {
        SUPER_TENANT_USER("samluser1", "samluser1", "carbon.super", "samluser1", "samluser1@abc.com", "samlnickuser1"),
        TENANT_USER("samluser2@wso2.com", "samluser2", "wso2.com", "samluser2", "samluser2@abc.com", "samlnickuser2");

        private String username;
        private String password;
        private String tenantDomain;
        private String tenantAwareUsername;
        private String email;
        private String nickname;

        User(String username, String password, String tenantDomain, String tenantAwareUsername, String email,
             String nickname) {
            this.username = username;
            this.password = password;
            this.tenantDomain = tenantDomain;
            this.tenantAwareUsername = tenantAwareUsername;
            this.email = email;
            this.nickname = nickname;
        }

        public String getUsername() {
            return username;
        }

        public String getPassword() {
            return password;
        }

        public String getTenantDomain() {
            return tenantDomain;
        }

        public String getTenantAwareUsername() {
            return tenantAwareUsername;
        }

        public String getEmail() {
            return email;
        }

        public String getNickname() {
            return nickname;
        }
    };

    private enum App{
        SUPER_TENANT_APP_WITH_SIGNING("travelocity.com", true),
        TENANT_APP_WITHOUT_SIGNING("travelocity.com-saml-tenantwithoutsigning", false);

        private String artifact;
        private boolean signingEnabled;

        App(String artifact, boolean signingEnabled) {
            this.artifact = artifact;
            this.signingEnabled = signingEnabled;
        }

        public String getArtifact() {
            return artifact;
        }

        public boolean isSigningEnabled() {
            return signingEnabled;
        }
    }

    private static class SAMLConfig{
        private TestUserMode userMode;
        private User user;
        private HttpBinding httpBinding;
        private ClaimType claimType;
        private App app;

        private SAMLConfig(TestUserMode userMode, User user, HttpBinding httpBinding, ClaimType claimType, App app) {
            this.userMode = userMode;
            this.user = user;
            this.httpBinding = httpBinding;
            this.claimType = claimType;
            this.app = app;
        }

        public TestUserMode getUserMode() {
            return userMode;
        }

        public App getApp() {
            return app;
        }

        public User getUser() {
            return user;
        }

        public ClaimType getClaimType() {
            return claimType;
        }

        public HttpBinding getHttpBinding() {
            return httpBinding;
        }

        @Override
        public String toString() {
            return "SAMLConfig[" +
                   ", userMode=" + userMode.name() +
                   ", user=" + user.getUsername() +
                   ", httpBinding=" + httpBinding +
                   ", claimType=" + claimType +
                   ", app=" + app.getArtifact() +
                   ']';
        }
    }

    @Factory(dataProvider = "samlConfigProvider")
    public SAMLSSOTestCase(SAMLConfig config) {
        if (log.isDebugEnabled()){
            log.info("SAML SSO Test initialized for " + config);
        }
        this.config = config;
    }

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {
        super.init(config.getUserMode());

        ConfigurationContext configContext = ConfigurationContextFactory
                .createConfigurationContextFromFileSystem(null
                        , null);
        applicationManagementServiceClient =
                new ApplicationManagementServiceClient(sessionCookie, backendURL, configContext);
        ssoConfigServiceClient =
                new SAMLSSOConfigServiceClient(backendURL, sessionCookie);
        remoteUSMServiceClient = new RemoteUserStoreManagerServiceClient(backendURL, sessionCookie);

        httpClient = new DefaultHttpClient();

        createUser();
        createApplication();

        //Starting tomcat
        log.info("Starting Tomcat");
        tomcatServer = getTomcat();

        URL resourceUrl = getClass()
                .getResource(File.separator + "samples" + File.separator + config.getApp().getArtifact() + ".war");
        startTomcat(tomcatServer, "/" + config.getApp().getArtifact(), resourceUrl.getPath());

    }

    @AfterClass(alwaysRun = true)
    public void testClear() throws Exception{
        deleteUser();
        deleteApplication();

        ssoConfigServiceClient = null;
        applicationManagementServiceClient = null;
        remoteUSMServiceClient = null;
        httpClient = null;
        //Stopping tomcat
        tomcatServer.stop();
        tomcatServer.destroy();
        Thread.sleep(10000);
    }

    @Test(description = "Add service provider", groups = "wso2.is", priority = 1)
    public void testAddSP() throws Exception {
        Boolean isAddSuccess = ssoConfigServiceClient
                .addServiceProvider(createSsoServiceProviderDTO());
        Assert.assertTrue(isAddSuccess, "Adding a service provider has failed for " + config);

        SAMLSSOServiceProviderDTO[] samlssoServiceProviderDTOs = ssoConfigServiceClient
                .getServiceProviders().getServiceProviders();
        Assert.assertEquals(samlssoServiceProviderDTOs[0].getIssuer(), config.getApp().getArtifact(),
                            "Adding a service provider has failed for " + config);
    }

    @Test(description = "Remove service provider", groups = "wso2.is", dependsOnMethods = { "testSAMLRelayStateDecode" })
    public void testRemoveSP()
            throws Exception {
        Boolean isAddSuccess = ssoConfigServiceClient.removeServiceProvider(config.getApp().getArtifact());
        Assert.assertTrue(isAddSuccess, "Removing a service provider has failed for " + config);
    }

    @Test(alwaysRun = true, description = "Testing SAML SSO login", groups = "wso2.is",
          dependsOnMethods = { "testAddSP" })
    public void testSAMLSSOLogin() {
        try {
            HttpResponse response;

            response = sendGetRequest(
                    String.format(SAML_SSO_LOGIN_URL, config.getApp().getArtifact(), config.getHttpBinding().binding));

            if (config.getHttpBinding() == HttpBinding.HTTP_POST){
                String samlRequest = extractDataFromResponse(response, "SAMLRequest", 5);
                response = sendSAMLMessage(SAML_SSO_URL, "SAMLRequest", samlRequest);
                EntityUtils.consume(response.getEntity());

                response = sendRedirectRequest(response);
            }

            String sessionKey = extractDataFromResponse(response, "name=\"sessionDataKey\"", 1);
            response = sendPOSTMessage(sessionKey);
            EntityUtils.consume(response.getEntity());

            response = sendRedirectRequest(response);
            String samlResponse = extractDataFromResponse(response, "SAMLResponse", 5);

            response = sendSAMLMessage(String.format(ACS_URL, config.getApp().getArtifact()), "SAMLResponse",
                                       samlResponse);
            resultPage = extractDataFromResponse(response);

            Assert.assertTrue(resultPage.contains("You are logged in as " + config.getUser().getTenantAwareUsername()),
                              "SAML SSO Login failed for " + config);
        } catch (Exception e) {
            Assert.fail("SAML SSO Login test failed for " + config, e);
        }
    }

    @Test(alwaysRun = true, description = "Testing SAML SSO Claims", groups = "wso2.is",
          dependsOnMethods = { "testSAMLSSOLogin" })
    public void testClaims(){
        String claimString = resultPage.substring(resultPage.lastIndexOf("<table>"));

        switch (config.getClaimType()){
            case LOCAL:
                assertLocalClaims(claimString);
                break;
            case NONE:
                assertNoneClaims(claimString);
                break;
        }
    }

    @Test(alwaysRun = true, description = "Testing SAML SSO logout", groups = "wso2.is",
          dependsOnMethods = { "testSAMLSSOLogin" })
    public void testSAMLSSOLogout() throws Exception {
        try {
            HttpResponse response;

            response = sendGetRequest(
                    String.format(SAML_SSO_LOGOUT_URL, config.getApp().getArtifact(), config.getHttpBinding().binding));

            if (config.getHttpBinding() == HttpBinding.HTTP_POST){
                String samlRequest = extractDataFromResponse(response, "SAMLRequest", 5);
                response = sendSAMLMessage(SAML_SSO_URL, "SAMLRequest", samlRequest);
            }

            String samlResponse = extractDataFromResponse(response, "SAMLResponse", 5);
            response = sendSAMLMessage(String.format(ACS_URL, config.getApp().getArtifact()), "SAMLResponse",
                                       samlResponse);
            String resultPage = extractDataFromResponse(response);

            Assert.assertTrue(resultPage.contains("index.jsp") && !resultPage.contains("error"),
                              "SAML SSO Logout failed for " + config);
        } catch (Exception e) {
            Assert.fail("SAML SSO Logout test failed for " + config, e);
        }
    }


    @Test(alwaysRun = true, description = "Testing SAML RelayState decode", groups = "wso2.is", dependsOnMethods =
            {"testSAMLSSOLogout"})
    public void testSAMLRelayStateDecode() throws Exception {
        try {
            String relayState = "https%3A%2F%2Fwww.google.com%2Fa%2Fcoolguseconcepts" +
                    ".com%2FServiceLogin%3Fservice%3Dmail%26passive%3Dtrue%26rm%3Dfalse%26continue%3Dhttps%253A%252F" +
                    "%252Fmail.google.com%252Fa%252Fcoolguseconcepts" +
                    ".com%252F%26ss%3D1%26ltmpl%3Ddefault%26ltmplcache%3D2%26emr%3D1%26osid%3D1%26scope%3Dhttp%3A%2F" +
                    "%2Fmeyerweb.com%2Feric%2Ftools%2Fdencoder%2F%2bhttp%3A%2F%2Fmeyerweb" +
                    ".com%2Feric%2Ftools%2Fdencoder%2F&";
            HttpResponse response;
            response = sendGetRequest(
                    String.format(SAML_SSO_LOGIN_URL, config.getApp().getArtifact(), config.getHttpBinding().binding));

            if (config.getHttpBinding() == HttpBinding.HTTP_POST) {
                String samlRequest = extractDataFromResponse(response, "SAMLRequest", 5);
                Map<String, String> paramters = new HashMap<String, String>();
                paramters.put("SAMLRequest", samlRequest);
                paramters.put("RelayState", relayState);
                response = sendSAMLMessage(SAML_SSO_URL, paramters);
                EntityUtils.consume(response.getEntity());
                response = sendRedirectRequest(response);

                String sessionKey = extractDataFromResponse(response, "name=\"sessionDataKey\"", 1);
                response = sendPOSTMessage(sessionKey);
                EntityUtils.consume(response.getEntity());

                response = sendRedirectRequest(response);
                String receivedRelayState = extractDataFromResponse(response, "RelayState", 5);
                relayState = relayState.replaceAll("&", "&amp;").replaceAll("\"", "&quot;").replaceAll("'", "&apos;").
                        replaceAll("<", "&lt;").replaceAll(">", "&gt;").replace("\n", "");
                Assert.assertEquals(relayState, receivedRelayState, "Sent parameter : " + relayState + "\nRecieved : " +
                        "" + receivedRelayState + "\n");
            }

        } catch (Exception e) {
            Assert.fail("SAML SSO Logout test failed for " + config, e);
        }
    }

    @DataProvider(name = "samlConfigProvider")
    public static SAMLConfig[][] samlConfigProvider(){
        return  new SAMLConfig[][]{
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

    private void assertLocalClaims(String claims){
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

    private void assertNoneClaims(String claims){
        String[] dataArray = StringUtils.substringsBetween(claims, "<td>", "</td>");
        Assert.assertNull(dataArray, "Claims are not expected for " + config);
    }

    private void startTomcat(Tomcat tomcat, String webAppUrl, String webAppPath)
            throws LifecycleException {
        tomcat.addWebapp(tomcat.getHost(), webAppUrl, webAppPath);
        tomcat.start();
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
        post.addHeader("Referer", String.format(ACS_URL, config.getApp().getArtifact()));
        List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
        urlParameters.add(new BasicNameValuePair("username", config.getUser().getUsername()));
        urlParameters.add(new BasicNameValuePair("password", config.getUser().getPassword()));
        urlParameters.add(new BasicNameValuePair("sessionDataKey", sessionKey));
        post.setEntity(new UrlEncodedFormEntity(urlParameters));
        return httpClient.execute(post);
    }

    private HttpResponse sendGetRequest(String url) throws Exception {
        HttpGet request = new HttpGet(url);
        request.addHeader("User-Agent", USER_AGENT);
        return httpClient.execute(request);
    }

    private HttpResponse sendSAMLMessage(String url, Map<String, String> parameters) throws IOException {
        List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
        HttpPost post = new HttpPost(url);
        post.setHeader("User-Agent", USER_AGENT);
        for (Map.Entry<String,String> entry : parameters.entrySet()) {
            urlParameters.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
        }
        if (config.getUserMode() == TestUserMode.TENANT_ADMIN || config.getUserMode() == TestUserMode.TENANT_USER){
            urlParameters.add(new BasicNameValuePair(TENANT_DOMAIN_PARAM, config.getUser().getTenantDomain()));
        }
        post.setEntity(new UrlEncodedFormEntity(urlParameters));
        return httpClient.execute(post);
    }

    private HttpResponse sendSAMLMessage(String url, String samlMsgKey, String samlMsgValue) throws IOException {
        List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
        HttpPost post = new HttpPost(url);
        post.setHeader("User-Agent", USER_AGENT);
        urlParameters.add(new BasicNameValuePair(samlMsgKey, samlMsgValue));
        if (config.getUserMode() == TestUserMode.TENANT_ADMIN || config.getUserMode() == TestUserMode.TENANT_USER){
            urlParameters.add(new BasicNameValuePair(TENANT_DOMAIN_PARAM, config.getUser().getTenantDomain()));
        }
        post.setEntity(new UrlEncodedFormEntity(urlParameters));
        return httpClient.execute(post);
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
        request.addHeader("Referer", String.format(ACS_URL, config.getApp().getArtifact()));
        return httpClient.execute(request);
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

    private Map<String,String> extractClaims(String claimString){
        String[] dataArray = StringUtils.substringsBetween(claimString, "<td>", "</td>");
        Map<String,String> attributeMap = new HashMap<String, String>();
        String key = null;
        String value;
        for (int i = 0; i< dataArray.length; i++){
            if((i%2) == 0){
                key = dataArray[i];
            }else{
                value = dataArray[i].trim();
                attributeMap.put(key,value);
            }
        }

        return attributeMap;
    }

    private void createApplication() throws Exception{
        ServiceProvider serviceProvider = new ServiceProvider();
        serviceProvider.setApplicationName(APPLICATION_NAME);
        serviceProvider.setDescription("This is a test Service Provider");
        applicationManagementServiceClient.createApplication(serviceProvider);

        serviceProvider = applicationManagementServiceClient.getApplication(APPLICATION_NAME);

        serviceProvider.getClaimConfig().setClaimMappings(getClaimMappings());

        InboundAuthenticationRequestConfig requestConfig = new InboundAuthenticationRequestConfig();
        requestConfig.setInboundAuthType(INBOUND_AUTH_TYPE);
        requestConfig.setInboundAuthKey(config.getApp().getArtifact());

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

    private void deleteApplication() throws Exception{
        applicationManagementServiceClient.deleteApplication(APPLICATION_NAME);
    }

    private void createUser(){
        log.info("Creating User " + config.getUser().getUsername());
        try {
            // creating the user
            remoteUSMServiceClient.addUser(config.getUser().getTenantAwareUsername(), config.getUser().getPassword(),
                                           null, getUserClaims(),
                                           profileName, true);
        } catch (Exception e) {
            Assert.fail("Error while creating the user", e);
        }

    }

    private void deleteUser(){
        log.info("Deleting User " + config.getUser().getUsername());
        try {
            remoteUSMServiceClient.deleteUser(config.getUser().getTenantAwareUsername());
        } catch (Exception e) {
            Assert.fail("Error while deleting the user", e);
        }
    }

    private SAMLSSOServiceProviderDTO createSsoServiceProviderDTO() {
        SAMLSSOServiceProviderDTO samlssoServiceProviderDTO = new SAMLSSOServiceProviderDTO();
        samlssoServiceProviderDTO.setIssuer(config.getApp().getArtifact());
        samlssoServiceProviderDTO.setAssertionConsumerUrls(new String[] {String.format(ACS_URL,
                                                                                config.getApp().getArtifact())});
        samlssoServiceProviderDTO.setDefaultAssertionConsumerUrl(String.format(ACS_URL, config.getApp().getArtifact()));
        samlssoServiceProviderDTO.setAttributeConsumingServiceIndex(ATTRIBUTE_CS_INDEX_VALUE);
        samlssoServiceProviderDTO.setNameIDFormat(NAMEID_FORMAT);
        samlssoServiceProviderDTO.setDoSignAssertions(config.getApp().isSigningEnabled());
        samlssoServiceProviderDTO.setDoSignResponse(config.getApp().isSigningEnabled());
        samlssoServiceProviderDTO.setDoSingleLogout(true);
        samlssoServiceProviderDTO.setLoginPageURL(LOGIN_URL);
        if (config.getClaimType() != ClaimType.NONE){
            samlssoServiceProviderDTO.setEnableAttributeProfile(true);
            samlssoServiceProviderDTO.setEnableAttributesByDefault(true);
        }

        return samlssoServiceProviderDTO;
    }

    private ClaimMapping[] getClaimMappings(){
        List<ClaimMapping> claimMappingList = new ArrayList<ClaimMapping>();

        Claim firstNameClaim = new Claim();
        firstNameClaim.setClaimUri(firstNameClaimURI);
        ClaimMapping firstNameClaimMapping = new ClaimMapping();
        firstNameClaimMapping.setRequested(true);
        firstNameClaimMapping.setLocalClaim(firstNameClaim);
        firstNameClaimMapping.setRemoteClaim(firstNameClaim);
        claimMappingList.add(firstNameClaimMapping);

        Claim lastNameClaim = new Claim();
        lastNameClaim.setClaimUri(lastNameClaimURI);
        ClaimMapping lastNameClaimMapping = new ClaimMapping();
        lastNameClaimMapping.setRequested(true);
        lastNameClaimMapping.setLocalClaim(lastNameClaim);
        lastNameClaimMapping.setRemoteClaim(lastNameClaim);
        claimMappingList.add(lastNameClaimMapping);

        Claim emailClaim = new Claim();
        emailClaim.setClaimUri(emailClaimURI);
        ClaimMapping emailClaimMapping = new ClaimMapping();
        emailClaimMapping.setRequested(true);
        emailClaimMapping.setLocalClaim(emailClaim);
        emailClaimMapping.setRemoteClaim(emailClaim);
        claimMappingList.add(emailClaimMapping);

        return claimMappingList.toArray(new ClaimMapping[claimMappingList.size()]);
    }

    private ClaimValue[] getUserClaims(){
        ClaimValue[] claimValues = new ClaimValue[3];

        ClaimValue firstName = new ClaimValue();
        firstName.setClaimURI(firstNameClaimURI);
        firstName.setValue(config.getUser().getNickname());
        claimValues[0] = firstName;

        ClaimValue lastName = new ClaimValue();
        lastName.setClaimURI(lastNameClaimURI);
        lastName.setValue(config.getUser().getUsername());
        claimValues[1] = lastName;

        ClaimValue email = new ClaimValue();
        email.setClaimURI(emailClaimURI);
        email.setValue(config.getUser().getEmail());
        claimValues[2] = email;

        return claimValues;
    }

}